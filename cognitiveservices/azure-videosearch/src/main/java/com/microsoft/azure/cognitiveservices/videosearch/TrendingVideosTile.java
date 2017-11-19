/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.videosearch;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The TrendingVideosTile model.
 */
public class TrendingVideosTile {
    /**
     * The query property.
     */
    @JsonProperty(value = "query", required = true)
    private Query query;

    /**
     * The image property.
     */
    @JsonProperty(value = "image", required = true)
    private ImageObject image;

    /**
     * Get the query value.
     *
     * @return the query value
     */
    public Query query() {
        return this.query;
    }

    /**
     * Set the query value.
     *
     * @param query the query value to set
     * @return the TrendingVideosTile object itself.
     */
    public TrendingVideosTile withQuery(Query query) {
        this.query = query;
        return this;
    }

    /**
     * Get the image value.
     *
     * @return the image value
     */
    public ImageObject image() {
        return this.image;
    }

    /**
     * Set the image value.
     *
     * @param image the image value to set
     * @return the TrendingVideosTile object itself.
     */
    public TrendingVideosTile withImage(ImageObject image) {
        this.image = image;
        return this;
    }

}
