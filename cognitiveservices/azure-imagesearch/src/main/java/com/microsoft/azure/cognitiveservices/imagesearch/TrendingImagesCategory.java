/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.imagesearch;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines the category of trending images.
 */
public class TrendingImagesCategory {
    /**
     * The name of the image category. For example, Popular People Searches.
     */
    @JsonProperty(value = "title", required = true)
    private String title;

    /**
     * A list of images that are trending in the category. Each tile contains
     * an image and a URL that returns more images of the subject. For example,
     * if the category is Popular People Searches, the image is of a popular
     * person and the URL would return more images of that person.
     */
    @JsonProperty(value = "tiles", required = true)
    private List<TrendingImagesTile> tiles;

    /**
     * Get the title value.
     *
     * @return the title value
     */
    public String title() {
        return this.title;
    }

    /**
     * Set the title value.
     *
     * @param title the title value to set
     * @return the TrendingImagesCategory object itself.
     */
    public TrendingImagesCategory withTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * Get the tiles value.
     *
     * @return the tiles value
     */
    public List<TrendingImagesTile> tiles() {
        return this.tiles;
    }

    /**
     * Set the tiles value.
     *
     * @param tiles the tiles value to set
     * @return the TrendingImagesCategory object itself.
     */
    public TrendingImagesCategory withTiles(List<TrendingImagesTile> tiles) {
        this.tiles = tiles;
        return this;
    }

}
