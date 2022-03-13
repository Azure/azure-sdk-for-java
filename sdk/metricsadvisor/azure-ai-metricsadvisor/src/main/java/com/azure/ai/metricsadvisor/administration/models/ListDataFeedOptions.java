// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.core.annotation.Fluent;

/**
 * Additional properties for filtering results on the listDataFeeds operation.
 */
@Fluent
public final class ListDataFeedOptions {
    private ListDataFeedFilter listDataFeedFilter;
    private Integer maxPageSize;
    private Integer skip;

    /**
     * Get the additional data feed filter options that can be passed for filtering the result of the data feeds
     * returned by the service
     *
     * @return The data feed filter options used for filtering the result of the data feeds
     * returned by the service
     */
    public ListDataFeedFilter getListDataFeedFilter() {
        return listDataFeedFilter;
    }

    /**
     * Set the additional data feed filter options that can be passed for filtering the result of listDataFeeds
     * operation.
     *
     * @param listDataFeedFilter the additional data feed filter options that can be passed for filtering the data feeds
     * returned by the service.
     *
     * @return the updated {@code ListDataFeedOptions} value.
     */
    public ListDataFeedOptions setListDataFeedFilter(final ListDataFeedFilter listDataFeedFilter) {
        this.listDataFeedFilter = listDataFeedFilter;
        return this;
    }

    /**
     * Gets limit indicating the number of items that will be included in a service returned page.
     *
     * @return The max page size value.
     */
    public Integer getMaxPageSize() {
        return maxPageSize;
    }

    /**
     * Sets limit indicating the number of items to be included in a service returned page.
     *
     * @param maxPageSize The max page size value.
     *
     * @return The ListDataFeedOptions object itself.
     */
    public ListDataFeedOptions setMaxPageSize(Integer maxPageSize) {
        this.maxPageSize = maxPageSize;
        return this;
    }

    /**
     * Gets the number of items in the queried collection that will be skipped and not included
     * in the returned result.
     *
     * @return The skip value.
     */
    public Integer getSkip() {
        return skip;
    }

    /**
     * Sets the number of items in the queried collection that are to be skipped and not included
     * in the returned result.
     *
     * @param skip The skip value.
     *
     * @return ListDataFeedOptions itself.
     */
    public ListDataFeedOptions setSkip(final Integer skip) {
        this.skip = skip;
        return this;
    }
}
