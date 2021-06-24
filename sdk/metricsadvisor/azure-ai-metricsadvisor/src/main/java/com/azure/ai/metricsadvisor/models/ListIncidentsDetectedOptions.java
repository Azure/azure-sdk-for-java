// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import com.azure.core.annotation.Fluent;

import java.util.List;

/**
 * Describes the additional parameters for the API to list incidents detected.
 */
@Fluent
public final class ListIncidentsDetectedOptions {
    private Integer maxPageSize;
    private List<DimensionKey> dimensionsToFilter;

    /**
     * Gets limit indicating the number of items that will be included in a service returned page.
     *
     * @return The max page size value.
     */
    public Integer getMaxPageSize() {
        return this.maxPageSize;
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
     * @param maxPageSize The max page size value.
     * @return ListIncidentsDetectedOptions itself.
     */
    public ListIncidentsDetectedOptions setMaxPageSize(Integer maxPageSize) {
        this.maxPageSize = maxPageSize;
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
