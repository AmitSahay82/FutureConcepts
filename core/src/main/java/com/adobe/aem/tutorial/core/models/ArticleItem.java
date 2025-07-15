package com.adobe.aem.tutorial.core.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import java.text.SimpleDateFormat;
import java.util.Calendar;

@Model(adaptables = Resource.class)
public class ArticleItem {

    @ValueMapValue
    private String artTitle;

    @ValueMapValue
    private String artDescription;

    @ValueMapValue
    private String author;

    @ValueMapValue
    private Calendar date;

    public String getArtTitle() {
        return artTitle;
    }

    public String getArtDescription() {
        return artDescription;
    }

    public String getAuthor() {
        return author;
    }

    public String getDate() {

        if (date != null) {
            return new SimpleDateFormat("dd-MMM-yyyy").format(date.getTime());
        }
        return "";
    }
}