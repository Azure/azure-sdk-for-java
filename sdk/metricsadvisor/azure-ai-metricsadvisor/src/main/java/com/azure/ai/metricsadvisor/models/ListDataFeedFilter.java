// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import com.azure.core.annotation.Fluent;

/**
 * Additional properties to filter result for data feed list operations.
 */
@Fluent
public final class ListDataFeedFilter {
    private String dataFeedName;
    private String creator;

    /**
     * Gets the data feed name.
     *
     * @return the data feed name.
     */
    public String getName() {
        return this.dataFeedName;
    }

    /**
     * Gets the creator of the data feed.
     *
     * @return the creator of the data feed.
     */
    public String getCreator() {
        return this.creator;
    }

    /**
     * Sets the name for the data feed.
     *
     * @param dataFeedName the name for the data feed.
     *
     * @return the updated {@code ListDataFeedFilter} value.
     */
    public ListDataFeedFilter setName(String dataFeedName) {
        this.dataFeedName = dataFeedName;
        return this;
    }

    /**
     * Set the creator of the data feed.
     *
     * @param creator the creator of the data feed.
     *
     * @return the updated {@code ListDataFeedFilter} value.
     */
    public ListDataFeedFilter setCreator(final String creator) {
        this.creator = creator;
        return this;
    }
}
