// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.core.annotation.Fluent;

import java.time.OffsetDateTime;

/**
 * Describes the additional parameters for the API to list data feed ingestion status.
 */
@Fluent
public final class ListDataFeedIngestionOptions {
    private final OffsetDateTime startTime;
    private final OffsetDateTime endTime;
    private Integer maxPageSize;
    private Integer skip;

    /**
     * Creates a new instance of ListDataFeedIngestionOptions.
     *
     * @param startTime The start point of time range to query data ingestion status.
     * @param endTime The end point of time range to query data ingestion status.
     */
    public ListDataFeedIngestionOptions(OffsetDateTime startTime,
                            OffsetDateTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * Gets the start point of time range to query data ingestion status.
     *
     * @return The start time.
     */
    public OffsetDateTime getStartTime() {
        return this.startTime;
    }

    /**
     * Gets the end point of time range to query data ingestion status.
     *
     * @return The end time.
     */
    public OffsetDateTime getEndTime() {
        return this.endTime;
    }

    /**
     * Gets limit indicating the number of items that will be included in a service returned page.
     *
     * @return The max page size value.
     */
    public Integer getMaxPageSize() {
        return this.maxPageSize;
    }

    /**
     * Gets the number of items in the queried collection that are to be skipped and not included
     * in the returned result.
     *
     * @return The skip value.
     */
    public Integer getSkip() {
        return this.skip;
    }

    /**
     * Sets limit indicating the number of items to be included in a service returned page.
     *
     * @param maxPageSize The max page size value.
     * @return The ListDataFeedIngestionOptions object itself.
     */
    public ListDataFeedIngestionOptions setMaxPageSize(Integer maxPageSize) {
        this.maxPageSize = maxPageSize;
        return this;
    }

    /**
     * Sets the number of items in the queried collection that are to be skipped and not included
     * in the returned result.
     *
     * @param skip The skip value.
     * @return The ListDataFeedIngestionOptions object itself.
     */
    public ListDataFeedIngestionOptions setSkip(Integer skip) {
        this.skip = skip;
        return this;
    }
}
