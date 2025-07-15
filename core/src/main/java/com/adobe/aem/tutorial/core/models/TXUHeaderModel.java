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
       adapters = {TXUHeaderModel.class, ComponentExporter.class},
       resourceType = TXUHeaderModel.RESOURCE_TYPE,
       defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
@Exporter(name = ExporterConstants.SLING_MODEL_EXPORTER_NAME, extensions = ExporterConstants.SLING_MODEL_EXTENSION)
public class TXUHeaderModel implements ComponentExporter {

    static final String RESOURCE_TYPE = "FutureConcepts/components/header";

    @ValueMapValue
    private String logoImage;

    @ValueMapValue
    private String forHomeLink;

    @ValueMapValue
    private String forBusinessLink;

    @ValueMapValue
    private String shopPlansLink;

    @ValueMapValue
    private String signInLink;

    @ChildResource
    private List<LinkItem> plansDropdown;

    @ChildResource
    private List<LinkItem> renewableDropdown;

    @ChildResource
    private List<LinkItem> supportDropdown;

    public String getLogoImage() {
        return logoImage;
    }

    public String getForHomeLink() {
        return forHomeLink;
    }

    public String getShopPlansLink() {
        return shopPlansLink;
    }

    public String getForBusinessLink() {
        return forBusinessLink;
    }

    public String getSignInLink() {
        return signInLink;
    }

    public List<LinkItem> getPlansDropdown() {
        return plansDropdown;
    }

    public List<LinkItem> getRenewableDropdown() {
        return renewableDropdown;
    }

    public List<LinkItem> getSupportDropdown() {
        return supportDropdown;
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