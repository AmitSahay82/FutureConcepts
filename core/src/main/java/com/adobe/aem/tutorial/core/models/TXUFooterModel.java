package com.adobe.aem.tutorial.core.models;

import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.export.json.ExporterConstants;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import java.util.List;

@Model(adaptables = Resource.class,
       adapters = {TXUFooterModel.class, ComponentExporter.class},
       resourceType = TXUFooterModel.RESOURCE_TYPE,
       defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
@Exporter(name = ExporterConstants.SLING_MODEL_EXPORTER_NAME, extensions = ExporterConstants.SLING_MODEL_EXTENSION)
public class TXUFooterModel implements ComponentExporter {

    static final String RESOURCE_TYPE = "FutureConcepts/components/footer";

    @ValueMapValue
    private String logoImage;

    @ValueMapValue
    private String shopPlans;

    @ValueMapValue
    private String signIn;

    @ChildResource
    private List<LinkItem> columnOne;

    @ChildResource
    private List<LinkItem> columnTwo;

    public String getLogoImage() {
        return logoImage;
    }

    public String getShopPlans() {
        return shopPlans;
    }

    public String getSignIn() {
        return signIn;
    }

    public List<LinkItem> getColumnOne() {
        return columnOne;
    }

    public List<LinkItem> getColumnTwo() {
        return columnTwo;
    }

    @Override
    public String getExportedType() {
        return RESOURCE_TYPE;
    }

    @Model(adaptables = Resource.class)
    public static class LinkItem {
        @ValueMapValue
        private String text;

        @ValueMapValue
        private String link;

        public String getText() {
            return text;
        }

        public String getLink() {
            return link;
        }
    }
}