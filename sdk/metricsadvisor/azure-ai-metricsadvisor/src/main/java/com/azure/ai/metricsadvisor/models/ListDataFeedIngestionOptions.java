// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import java.time.OffsetDateTime;

/**
 * Describes the additional parameters for the API to list data feed ingestion status.
 */
public final class ListDataFeedIngestionOptions {
    private final OffsetDateTime startTime;
    private final OffsetDateTime endTime;
    private Integer top;
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
     * Gets limit indicating the number of items to be included in a service returned page.
     *
     * @return The top value.
     */
    public Integer getTop() {
        return this.top;
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
     * @param top The skip value.
     * @return The ListDataFeedIngestionOptions object itself.
     */
    public ListDataFeedIngestionOptions setTop(int top) {
        this.top = top;
        return this;
    }

    /**
     * Sets the number of items in the queried collection that are to be skipped and not included
     * in the returned result.
     *
     * @param skip The skip value.
     * @return The ListDataFeedIngestionOptions object itself.
     */
    public ListDataFeedIngestionOptions setSkip(int skip) {
        this.skip = skip;
        return this;
    }
}
