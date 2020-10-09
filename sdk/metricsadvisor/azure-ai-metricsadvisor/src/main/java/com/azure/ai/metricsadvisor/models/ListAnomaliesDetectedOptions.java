// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import java.time.OffsetDateTime;

/**
 * Describes the additional parameters for the API to list anomalies detected.
 */
public final class ListAnomaliesDetectedOptions {
    private final OffsetDateTime startTime;
    private final OffsetDateTime endTime;
    private Integer top;
    private Integer skip;
    private ListAnomaliesDetectedFilter filter;

    /**
     * Creates a new instance of ListAnomaliesDetectedOptions.
     *
     * @param startTime The start time of the time range within which the anomalies were detected.
     * @param endTime The end time of the time range within which the anomalies were detected.
     */
    public ListAnomaliesDetectedOptions(OffsetDateTime startTime,
                                        OffsetDateTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * Gets the start time of the time range within which the incidents were detected.
     *
     * @return The start time.
     */
    public OffsetDateTime getStartTime() {
        return this.startTime;
    }

    /**
     * Gets the end time of the time range within which the incidents were detected.
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
     * Gets additional conditions to filter the anomalies.
     *
     * @return The filter.
     */
    public ListAnomaliesDetectedFilter getFilter() {
        return this.filter;
    }

    /**
     * Gets limit indicating the number of items to be included in a service returned page.
     *
     * @param top The top value.
     * @return The ListAnomaliesDetectedOptions object itself.
     */
    public ListAnomaliesDetectedOptions setTop(int top) {
        this.top = top;
        return this;
    }

    /**
     * Sets the number of items in the queried collection that are to be skipped and not included
     * in the returned result.
     *
     * @param skip The skip value.
     * @return The ListAnomaliesDetectedOptions object itself.
     */
    public ListAnomaliesDetectedOptions setSkip(int skip) {
        this.skip = skip;
        return this;
    }

    /**
     * Sets additional conditions to filter the anomalies.
     *
     * @param filter The filter.
     * @return The ListAnomaliesDetectedOptions object itself.
     */
    public ListAnomaliesDetectedOptions setFilter(ListAnomaliesDetectedFilter filter) {
        this.filter = filter;
        return this;
    }
}
