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
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.InputStream;
import java.util.*;

@Component(service = WorkflowProcess.class, property = {
        "process.label=Create AEM Page from Excel"
})
public class CreatePageFromExcelWorkflowProcess implements WorkflowProcess {

    private static final Logger log = LoggerFactory.getLogger(CreatePageFromExcelWorkflowProcess.class);

    private static final String DAM_RENDITION_PATH = "/jcr:content/renditions/original/jcr:content";
    private static final String PAGE_PARENT_PATH = "/content/FutureConcepts/us/";

    @Override
    public void execute(WorkItem item, WorkflowSession workflowSession, MetaDataMap metaDataMap)
            throws WorkflowException {

        String payloadPath = item.getWorkflowData().getPayload().toString();
        log.info("Workflow triggered for asset: {}", payloadPath);

        // Trim the extra part after .xlsx
        int xlsxIndex = payloadPath.indexOf(".xlsx");
        if (xlsxIndex != -1) {
            payloadPath = payloadPath.substring(0, xlsxIndex + 5); // ".xlsx" is 5 characters
        }
        log.info("Workflow triggered for asset after trim: {}", payloadPath);

        String fileName = payloadPath.substring(payloadPath.lastIndexOf("/") + 1);
        String pageName = fileName.replaceFirst("[.][^.]+$", "");
        log.debug("Derived page name from file: {}", pageName);

        try (ResourceResolver resolver = workflowSession.adaptTo(ResourceResolver.class)) {
            log.debug("Got system user resolver successfully.");

            Resource fileRes = resolver.getResource(payloadPath);
            if (fileRes == null) {
                log.error("File resource not found at path: {}", payloadPath);
                return;
            }
            log.debug("File resource fetched: {}", fileRes.getPath());

            Asset asset = fileRes.adaptTo(Asset.class);
            if (asset == null) {
                log.error("Failed to adapt resource to Asset.");
                return;
            }
            InputStream inputStream = asset.getOriginal().getStream(); // Gets original binary
            log.debug("InputStream to Excel file obtained.");

            List<Map<String, String>> excelRows = parseExcel(inputStream);
            log.info("Parsed {} rows from Excel.", excelRows.size());

            createPageWithComponents(resolver, PAGE_PARENT_PATH, pageName, excelRows);
            log.info("Page '{}' successfully created at '{}'", pageName, PAGE_PARENT_PATH);

        } catch (Exception e) {
            log.error("Exception during workflow execution: ", e);
            throw new WorkflowException(e);
        }
    }

    private List<Map<String, String>> parseExcel(InputStream inputStream) throws Exception {
        List<Map<String, String>> rows = new ArrayList<>();
        log.debug("Starting Excel parsing...");

        Workbook workbook = WorkbookFactory.create(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.iterator();

        List<String> headers = new ArrayList<>();
        if (rowIterator.hasNext()) {
            Row headerRow = rowIterator.next();
            for (Cell cell : headerRow) {
                headers.add(cell.getStringCellValue().trim());
            }
            log.debug("Parsed headers: {}", headers);
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Map<String, String> rowData = new HashMap<>();
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = row.getCell(i);
                String cellValue = (cell != null) ? getCellValue(cell) : "";
                rowData.put(headers.get(i), cellValue);
            }
            rows.add(rowData);
        }

        log.debug("Completed parsing Excel. Total rows: {}", rows.size());
        return rows;
    }

    private String getCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

    private void createPageWithComponents(ResourceResolver resolver, String parentPath, String pageName,
                                          List<Map<String, String>> rows) throws PersistenceException, WCMException, RepositoryException {

        log.debug("Starting page creation logic...");

        PageManager pageManager = resolver.adaptTo(PageManager.class);
        if (pageManager == null) {
            log.error("PageManager is null. Cannot proceed.");
            return;
        }

        // Create page using the specified template
        String templatePath = "/conf/FutureConcepts/settings/wcm/templates/txu-template";
        Page page = pageManager.create(parentPath, pageName, templatePath, pageName, true);
        log.debug("Page created at: {}", page.getPath());

        Resource jcrContentRes = resolver.getResource(page.getPath() + "/jcr:content");
        if (jcrContentRes == null) {
            log.error("jcr:content not found for created page.");
            return;
        }

        Node jcrContentNode = jcrContentRes.adaptTo(Node.class);

        // Add root node under jcr:content
        Node rootNode = hasOrGetNode(jcrContentNode, "root");

        // Add container under root
        Node containerNode = hasOrGetNode(rootNode, "container");

        // Group Excel rows by component + groupId
        Map<String, List<Map<String, String>>> grouped = new LinkedHashMap<>();
        for (Map<String, String> row : rows) {
            String component = row.get("ComponentName");
            String groupId = row.getOrDefault("groupId", UUID.randomUUID().toString());
            String key = component + "::" + groupId;
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
        }

        log.debug("Grouped components: {}", grouped.keySet());

        for (Map.Entry<String, List<Map<String, String>>> entry : grouped.entrySet()) {
            List<Map<String, String>> groupRows = entry.getValue();
            Map<String, String> firstRow = groupRows.get(0);
            String componentType = firstRow.get("ComponentName");

            // Use the componentType directly as node name (no numbering)
            String nodeName = componentType;
            Node componentNode = containerNode.addNode(nodeName, "nt:unstructured");
            componentNode.setProperty("sling:resourceType", "FutureConcepts/components/" + componentType);

            if (groupRows.size() == 1) {
                // Single row: regular component
                for (Map.Entry<String, String> prop : firstRow.entrySet()) {
                    String key = prop.getKey();
                    String value = prop.getValue();
                    if (!key.equals("ComponentName") && !key.equals("groupId") && !value.isBlank()) {
                        componentNode.setProperty(key, value);
                        log.debug("Property set: {} = {}", key, value);
                    }
                }
            } else {
                // Multiple rows: multifield
                int mfCounter = 0;
                for (Map<String, String> mfRow : groupRows) {
                    Node listItems;
                    if (componentNode.hasNode("listItems"))
                    {
                        listItems = componentNode.getNode("listItems");
                    }
                    else {
                        listItems = componentNode.addNode("listItems", "nt:unstructured");
                    }
                    Node itemNode = listItems.addNode("item" + mfCounter++, "nt:unstructured");
                    for (Map.Entry<String, String> prop : mfRow.entrySet()) {
                        String key = prop.getKey();
                        String value = prop.getValue();
                        if (!key.equals("ComponentName") && !key.equals("groupId") && !value.isBlank()) {
                            itemNode.setProperty(key, value);
                            log.debug("Multifield item set: {} = {}", key, value);
                        }
                    }
                }
            }
        }
        resolver.commit();
        log.debug("All content committed to repository.");
    }

    private static Node hasOrGetNode(Node checkNode, String nodeName) throws RepositoryException {
        Node searchNode;
        if (checkNode.hasNode(nodeName)) {
            searchNode = checkNode.getNode(nodeName);
            log.debug(" {} node already exists, using existing one.", nodeName);
        } else {
            searchNode = checkNode.addNode(nodeName, "nt:unstructured");
            searchNode.setProperty("sling:resourceType", "FutureConcepts/components/container");
            searchNode.setProperty("layout", "responsiveGrid");
            log.debug("Created new {} node.", nodeName);
        }
        return searchNode;
    }
}
