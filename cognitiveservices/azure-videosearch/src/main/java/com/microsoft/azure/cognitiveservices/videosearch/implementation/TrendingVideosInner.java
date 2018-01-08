/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.videosearch.implementation;

import java.util.List;
import com.microsoft.azure.cognitiveservices.videosearch.TrendingVideosTile;
import com.microsoft.azure.cognitiveservices.videosearch.TrendingVideosCategory;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.microsoft.azure.cognitiveservices.videosearch.Response;

/**
 * The TrendingVideosInner model.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type",
        defaultImpl = TrendingVideosInner.class)
@JsonTypeName("TrendingVideos")
public class TrendingVideosInner extends Response {
    /**
     * The bannerTiles property.
     */
    @JsonProperty(value = "bannerTiles", required = true)
    private List<TrendingVideosTile> bannerTiles;

    /**
     * The categories property.
     */
    @JsonProperty(value = "categories", required = true)
    private List<TrendingVideosCategory> categories;

    /**
     * Get the bannerTiles value.
     *
     * @return the bannerTiles value
     */
    public List<TrendingVideosTile> bannerTiles() {
        return this.bannerTiles;
    }

    /**
     * Set the bannerTiles value.
     *
     * @param bannerTiles the bannerTiles value to set
     * @return the TrendingVideosInner object itself.
     */
    public TrendingVideosInner withBannerTiles(List<TrendingVideosTile> bannerTiles) {
        this.bannerTiles = bannerTiles;
        return this;
    }

    /**
     * Get the categories value.
     *
     * @return the categories value
     */
    public List<TrendingVideosCategory> categories() {
        return this.categories;
    }

    /**
     * Set the categories value.
     *
     * @param categories the categories value to set
     * @return the TrendingVideosInner object itself.
     */
    public TrendingVideosInner withCategories(List<TrendingVideosCategory> categories) {
        this.categories = categories;
        return this;
    }

}
