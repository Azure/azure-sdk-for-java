/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.websearch;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines a search results group, such as mainline.
 */
public class RankingRankingGroup {
    /**
     * A list of search result items to display in the group.
     */
    @JsonProperty(value = "items", required = true)
    private List<RankingRankingItem> items;

    /**
     * Get the items value.
     *
     * @return the items value
     */
    public List<RankingRankingItem> items() {
        return this.items;
    }

    /**
     * Set the items value.
     *
     * @param items the items value to set
     * @return the RankingRankingGroup object itself.
     */
    public RankingRankingGroup withItems(List<RankingRankingItem> items) {
        this.items = items;
        return this;
    }

}
