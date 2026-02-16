package com.adobe.aem.tutorial.core.workflow;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.day.cq.dam.api.Asset;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.WCMException;
import org.apache.poi.ss.usermodel.*;
import org.apache.sling.api.resource.*;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.InputStream;
import java.util.*;

@Component(service = WorkflowProcess.class, property = {
        "process.label = Create AEM Page from Excel"
})
public class CreatePageFromExcelWorkflowProcess implements WorkflowProcess {

    private static final Logger log = LoggerFactory.getLogger(CreatePageFromExcelWorkflowProcess.class);
    private static final String TEMPLATE_PATH = "/conf/FutureConcepts/settings/wcm/templates/txu-template";
    private static final String PAGE_PARENT_PATH = "/content/FutureConcepts/us/";

    @Override
    public void execute(WorkItem item, WorkflowSession workflowSession, MetaDataMap metaDataMap)
            throws WorkflowException {

        String payloadPath = getSanitizedPayload(item.getWorkflowData().getPayload().toString());
        log.info("Workflow started for asset: {}", payloadPath);

        try (ResourceResolver resolver = workflowSession.adaptTo(ResourceResolver.class)) {
            Resource fileRes = resolver.getResource(payloadPath);
            if (fileRes == null) {
                log.error("File not found at payload: {}", payloadPath);
                return;
            }

            Asset asset = fileRes.adaptTo(Asset.class);
            if (asset == null) {
                log.error("Could not adapt to Asset at path: {}", payloadPath);
                return;
            }
            log.info("Successfully adapted resource to Asset: {}", asset.getPath());

            String fileName = payloadPath.substring(payloadPath.lastIndexOf('/') + 1);
            String pageName = fileName.replaceFirst("[.][^.]+$", "");

            try (InputStream excelStream = asset.getOriginal().getStream()) {
                List<Map<String, String>> excelRows = parseExcel(excelStream);
                log.info("Parsed Excel with {} row(s)", excelRows.size());

                Page page = createPage(resolver, pageName);
                createComponentStructure(resolver, page, excelRows);
                log.info("Page structure creation completed for: {}", page.getPath());
            }

        } catch (Exception e) {
            log.error("Workflow error for asset: {}", payloadPath, e);
            throw new WorkflowException(e);
        }

        log.info("Workflow finished successfully for: {}", payloadPath);
    }

    private String getSanitizedPayload(String payload) {
        int idx = payload.indexOf(".xlsx");
        return (idx != -1) ? payload.substring(0, idx + 5) : payload;
    }

    private Page createPage(ResourceResolver resolver, String pageName) throws WCMException {
        PageManager pageManager = resolver.adaptTo(PageManager.class);
        if (pageManager == null) {
            throw new IllegalStateException("PageManager unavailable.");
        }
        Page page = pageManager.create(PAGE_PARENT_PATH, pageName, TEMPLATE_PATH, pageName, true);
        log.info("Created page: {}", page.getPath());
        return page;
    }

    private List<Map<String, String>> parseExcel(InputStream inputStream) throws Exception {
        List<Map<String, String>> rows = new ArrayList<>();
        Workbook workbook = WorkbookFactory.create(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.iterator();

        List<String> headers = new ArrayList<>();
        if (rowIterator.hasNext()) {
            for (Cell cell : rowIterator.next()) {
                headers.add(cell.getStringCellValue().trim());
            }
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Map<String, String> rowData = new HashMap<>();
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = row.getCell(i);
                rowData.put(headers.get(i), getCellValue(cell));
            }
            rows.add(rowData);
        }
        return rows;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC: return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            default: return "";
        }
    }

    private void createComponentStructure(ResourceResolver resolver, Page page, List<Map<String, String>> rows)
            throws RepositoryException, PersistenceException {
        Resource jcrContentRes = resolver.getResource(page.getPath() + "/jcr:content");
        if (jcrContentRes == null) throw new IllegalStateException("jcr:content missing.");

        Node jcrContentNode = jcrContentRes.adaptTo(Node.class);
        Node rootNode = ensureNode(jcrContentNode, "root");
        Node containerNode = ensureNode(rootNode, "container");

        Map<String, List<Map<String, String>>> grouped = groupExcelRows(rows);

        for (Map.Entry<String, List<Map<String, String>>> entry : grouped.entrySet()) {
            List<Map<String, String>> groupRows = entry.getValue();
            Map<String, String> firstRow = groupRows.get(0);
            String componentType = firstRow.get("ComponentName");

            String groupId = firstRow.getOrDefault("groupId", UUID.randomUUID().toString());
            String uniqueNodeName = componentType + "-" + groupId;

            log.info("Creating component node: {}", uniqueNodeName);
            Node componentNode = containerNode.addNode(uniqueNodeName, "nt:unstructured");
            componentNode.setProperty("sling:resourceType", "FutureConcepts/components/" + componentType);

            if (groupRows.size() == 1) {
                addProperties(componentNode, firstRow);
            } else {
                addMultifieldItems(componentNode, groupRows);
            }

            log.info("Added component: {}", componentType);
        }

        resolver.commit();
        log.info("Changes committed to repository for page: {}", page.getPath());
    }

    private Map<String, List<Map<String, String>>> groupExcelRows(List<Map<String, String>> rows) {
        Map<String, List<Map<String, String>>> grouped = new LinkedHashMap<>();
        for (Map<String, String> row : rows) {
            String component = row.get("ComponentName");
            String groupId = row.getOrDefault("groupId", UUID.randomUUID().toString());
            String key = component + "::" + groupId;
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
        }
        return grouped;
    }

    private void addProperties(Node node, Map<String, String> row) throws RepositoryException {
        for (Map.Entry<String, String> entry : row.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (!"ComponentName".equals(key) && !"groupId".equals(key) && isNonEmptyField(value)) {
                node.setProperty(key, value);
            }
        }
    }

    private void addMultifieldItems(Node componentNode, List<Map<String, String>> rows) throws RepositoryException {
        Node listItems = componentNode.addNode("listItems", "nt:unstructured");
        int index = 0;
        for (Map<String, String> row : rows) {
            Node item = listItems.addNode("item" + index++, "nt:unstructured");
            addProperties(item, row);
        }
    }

    private Node ensureNode(Node parent, String name) throws RepositoryException {
        if (parent.hasNode(name)) {
            return parent.getNode(name);
        }
        Node node = parent.addNode(name, "nt:unstructured");
        node.setProperty("sling:resourceType", "FutureConcepts/components/container");
        node.setProperty("layout", "responsiveGrid");
        return node;
    }

    private boolean isNonEmptyField(String value) {
        return value != null && !value.trim().isEmpty();
    }
}