package com.adobe.aem.tutorial.core.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import java.util.List;

@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class CarouselModel {

    @ChildResource(name = "slide") // assumes multifield node name is "slide"
    private List<LinkItem> slide;

    public List<LinkItem> getSlide() {
        return slide;
    }

    @Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
    public static class LinkItem {

        @ValueMapValue
        private String title;

        @ValueMapValue
        private String image;

        public String getTitle() {
            return title;
        }

        public String getImage() {
            return image;
        }
    }
}
