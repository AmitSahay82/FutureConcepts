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
    private String tile1Title;

    @ValueMapValue
    private String tile1Description;

    @ValueMapValue
    private String tile1Image;

    // Tile 2 fields
    @ValueMapValue
    private String tile2Title;

    @ValueMapValue
    private String tile2Description;

    @ValueMapValue
    private String tile2Image;

    // Getter methods for HTL

    public String getTile1Title() {
        return tile1Title;
    }

    public String getTile1Description() {
        return tile1Description;
    }

    public String getTile1Image() {
        return tile1Image;
    }

    public String getTile2Title() {
        return tile2Title;
    }

    public String getTile2Description() {
        return tile2Description;
    }

    public String getTile2Image() {
        return tile2Image;
    }
}
