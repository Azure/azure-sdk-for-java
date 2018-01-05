/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.imagesearch.implementation;

import java.util.List;
import com.microsoft.azure.cognitiveservices.imagesearch.TrendingImagesCategory;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.microsoft.azure.cognitiveservices.imagesearch.Response;

/**
 * The top-level object that the response includes when a trending images
 * request succeeds.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type",
        defaultImpl = TrendingImagesInner.class)
@JsonTypeName("TrendingImages")
public class TrendingImagesInner extends Response {
    /**
     * A list that identifies categories of images and a list of trending
     * images in that category.
     */
    @JsonProperty(value = "categories", required = true)
    private List<TrendingImagesCategory> categories;

    /**
     * Get the categories value.
     *
     * @return the categories value
     */
    public List<TrendingImagesCategory> categories() {
        return this.categories;
    }

    /**
     * Set the categories value.
     *
     * @param categories the categories value to set
     * @return the TrendingImagesInner object itself.
     */
    public TrendingImagesInner withCategories(List<TrendingImagesCategory> categories) {
        this.categories = categories;
        return this;
    }

}
