/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.websearch;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines where on the search results page content should be placed and in
 * what order.
 */
public class RankingRankingResponse {
    /**
     * The search results that should be afforded the most visible treatment
     * (for example, displayed above the mainline and sidebar).
     */
    @JsonProperty(value = "pole", access = JsonProperty.Access.WRITE_ONLY)
    private RankingRankingGroup pole;

    /**
     * The search results to display in the mainline.
     */
    @JsonProperty(value = "mainline", access = JsonProperty.Access.WRITE_ONLY)
    private RankingRankingGroup mainline;

    /**
     * The search results to display in the sidebar.
     */
    @JsonProperty(value = "sidebar", access = JsonProperty.Access.WRITE_ONLY)
    private RankingRankingGroup sidebar;

    /**
     * Get the pole value.
     *
     * @return the pole value
     */
    public RankingRankingGroup pole() {
        return this.pole;
    }

    /**
     * Get the mainline value.
     *
     * @return the mainline value
     */
    public RankingRankingGroup mainline() {
        return this.mainline;
    }

    /**
     * Get the sidebar value.
     *
     * @return the sidebar value
     */
    public RankingRankingGroup sidebar() {
        return this.sidebar;
    }

}
