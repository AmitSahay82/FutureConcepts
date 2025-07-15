package com.adobe.aem.tutorial.core.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Model(adaptables = Resource.class,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ArticleDetailsModel {

    @ValueMapValue(name = "fileReference")
    private String image;

    @ValueMapValue
    private String title;

    @ValueMapValue
    private String description;

    @ChildResource
    private List<ArticleItem> article;

    public String getImage() {
        return image;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<ArticleItem> getArticle() {
        if (article != null) {
            List<ArticleItem> reversed = new ArrayList<>(article);
            Collections.reverse(reversed);
            return reversed;
        }
        return Collections.emptyList();
    }
}
