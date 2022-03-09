// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.core.annotation.Fluent;

/**
 * Additional properties to filter result for data feed list operations.
 */
@Fluent
public final class ListDataFeedFilter {
    private String dataFeedName;
    private String creator;
    private DataFeedSourceType dataFeedSourceType;
    private DataFeedStatus dataFeedStatus;
    private DataFeedGranularityType dataFeedGranularityType;

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
     * Gets the source type of the data feed.
     *
     * @return the source type of the data feed.
     */
    public DataFeedSourceType getSourceType() {
        return this.dataFeedSourceType;
    }

    /**
     * Gets the status of the data feed.
     *
     * @return the status of the data feed.
     */
    public DataFeedStatus getStatus() {
        return this.dataFeedStatus;
    }

    /**
     * Gets the granularity/refresh type of the data feed source.
     *
     * @return the granularity/refresh type of the data feed source.
     */
    public DataFeedGranularityType getGranularityType() {
        return this.dataFeedGranularityType;
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

    /**
     * Set the source type of the data feed.
     *
     * @param dataFeedSourceType the source type of the data feed.
     *
     * @return the updated {@code ListDataFeedFilter} value.
     */
    public ListDataFeedFilter setDataFeedSourceType(final DataFeedSourceType dataFeedSourceType) {
        this.dataFeedSourceType = dataFeedSourceType;
        return this;
    }

    /**
     * Set the status of the data feed.
     *
     * @param dataFeedStatus the status of the data feed.
     *
     * @return the updated {@code ListDataFeedFilter} value.
     */
    public ListDataFeedFilter setDataFeedStatus(final DataFeedStatus dataFeedStatus) {
        this.dataFeedStatus = dataFeedStatus;
        return this;
    }

    /**
     * Set the granularity type for the data feed.
     *
     * @param dataFeedGranularityType the granularity type for the data feed.
     *
     * @return the updated {@code ListDataFeedFilter} value.
     */
    public ListDataFeedFilter setDataFeedGranularityType(final DataFeedGranularityType dataFeedGranularityType) {
        this.dataFeedGranularityType = dataFeedGranularityType;
        return this;
    }
}
