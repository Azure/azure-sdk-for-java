// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.core.annotation.Fluent;

/**
 * Describes the additional parameters for the API to list hooks.
 */
@Fluent
public final class ListHookOptions {
    private String hookNameFilter;
    private Integer maxPageSize;
    private Integer skip;

    /**
     * Gets the hook name filter.
     *
     * @return The hook filter.
     */
    public String getHookNameFilter() {
        return this.hookNameFilter;
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
     * Sets the hook name filter.
     *
     * @param hookNameFilter The hook name filter.
     *
     * @return The ListHookOptions object itself.
     */
    public ListHookOptions setHookNameFilter(String hookNameFilter) {
        this.hookNameFilter = hookNameFilter;
        return this;
    }

    /**
     * Sets limit indicating the number of items to be included in a service returned page.
     *
     * @param maxPageSize The maxPageSize value.
     * @return The ListHookOptions object itself.
     */
    public ListHookOptions setMaxPageSize(Integer maxPageSize) {
        this.maxPageSize = maxPageSize;
        return this;
    }

    /**
     * Sets the number of items in the queried collection that are to be skipped and not included
     * in the returned result.
     *
     * @param skip The skip value.
     * @return ListHookOptions object itself.
     */
    public ListHookOptions setSkip(Integer skip) {
        this.skip = skip;
        return this;
    }
}
