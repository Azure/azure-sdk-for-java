/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.imagesearch;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines an image tile.
 */
public class TrendingImagesTile {
    /**
     * A query that returns a Bing search results page with more images of the
     * subject. For example, if the category is Popular People Searches, then
     * the thumbnail is of a popular person. The query would return a Bing
     * search results page with more images of that person.
     */
    @JsonProperty(value = "query", required = true)
    private Query query;

    /**
     * The image's thumbnail.
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
     * @return the TrendingImagesTile object itself.
     */
    public TrendingImagesTile withQuery(Query query) {
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
     * @return the TrendingImagesTile object itself.
     */
    public TrendingImagesTile withImage(ImageObject image) {
        this.image = image;
        return this;
    }

}
