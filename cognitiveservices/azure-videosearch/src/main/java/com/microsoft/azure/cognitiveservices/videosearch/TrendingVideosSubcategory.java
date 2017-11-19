/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.videosearch;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The TrendingVideosSubcategory model.
 */
public class TrendingVideosSubcategory {
    /**
     * The title property.
     */
    @JsonProperty(value = "title", required = true)
    private String title;

    /**
     * The tiles property.
     */
    @JsonProperty(value = "tiles", required = true)
    private List<TrendingVideosTile> tiles;

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
     * @return the TrendingVideosSubcategory object itself.
     */
    public TrendingVideosSubcategory withTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * Get the tiles value.
     *
     * @return the tiles value
     */
    public List<TrendingVideosTile> tiles() {
        return this.tiles;
    }

    /**
     * Set the tiles value.
     *
     * @param tiles the tiles value to set
     * @return the TrendingVideosSubcategory object itself.
     */
    public TrendingVideosSubcategory withTiles(List<TrendingVideosTile> tiles) {
        this.tiles = tiles;
        return this;
    }

}
