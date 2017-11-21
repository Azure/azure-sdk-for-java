/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.videosearch;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The TrendingVideosCategory model.
 */
public class TrendingVideosCategory {
    /**
     * The title property.
     */
    @JsonProperty(value = "title", required = true)
    private String title;

    /**
     * The subcategories property.
     */
    @JsonProperty(value = "subcategories", required = true)
    private List<TrendingVideosSubcategory> subcategories;

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
     * @return the TrendingVideosCategory object itself.
     */
    public TrendingVideosCategory withTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * Get the subcategories value.
     *
     * @return the subcategories value
     */
    public List<TrendingVideosSubcategory> subcategories() {
        return this.subcategories;
    }

    /**
     * Set the subcategories value.
     *
     * @param subcategories the subcategories value to set
     * @return the TrendingVideosCategory object itself.
     */
    public TrendingVideosCategory withSubcategories(List<TrendingVideosSubcategory> subcategories) {
        this.subcategories = subcategories;
        return this;
    }

}
