package com.adobe.aem.tutorial.core.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Model(
        adaptables = Resource.class,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class TileModel {

    // Tile 1 fields
    @ValueMapValue
    private String title;

    @ValueMapValue
    private String description;

    @ValueMapValue
    private String image;

    // Tile 2 fields
    @ValueMapValue
    private String title2;

    @ValueMapValue
    private String description2;

    @ValueMapValue
    private String image2;

    // Getter methods for HTL

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImage() {
        return image;
    }

    public String getTitle2() {
        return title2;
    }

    public String getDescription2() {
        return description2;
    }

    public String getImage2() {
        return image2;
    }
}
