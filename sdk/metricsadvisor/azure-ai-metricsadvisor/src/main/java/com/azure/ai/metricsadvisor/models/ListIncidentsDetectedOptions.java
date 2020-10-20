// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Describes the additional parameters for the API to list incidents detected.
 */
public final class ListIncidentsDetectedOptions {
    private final OffsetDateTime startTime;
    private final OffsetDateTime endTime;
    private Integer top;
    private List<DimensionKey> dimensionsToFilter;

    /**
     * Creates a new instance of ListIncidentsDetectedOptions.
     *
     * @param startTime The start time of the time range within which the incidents were detected.
     * @param endTime The end time of the time range within which the incidents were detected.
     */
    public ListIncidentsDetectedOptions(OffsetDateTime startTime,
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
     * Gets the dimension filter.
     *
     * @return The dimensions to filter.
     */
    public List<DimensionKey> getDimensionsToFilter() {
        return this.dimensionsToFilter;
    }

    /**
     * Gets limit indicating the number of items to be included in a service returned page.
     *
     * @param top The top value.
     * @return ListIncidentsDetectedOptions itself.
     */
    public ListIncidentsDetectedOptions setTop(int top) {
        this.top = top;
        return this;
    }

    /**
     * Sets the dimensions to filter.
     *
     * @param dimensionsToFilter The dimensions to filter.
     * @return ListIncidentsDetectedOptions itself.
     */
    public ListIncidentsDetectedOptions setDimensionsToFilter(List<DimensionKey> dimensionsToFilter) {
        this.dimensionsToFilter = dimensionsToFilter;
        return this;
    }
}
