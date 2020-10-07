// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

/**
 * Describes the additional parameters for the API to list hooks.
 */
public final class ListHookOptions {
    private String hookNameFilter;
    private Integer top;
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
     * @param top The top value.
     * @return The ListHookOptions object itself.
     */
    public ListHookOptions setTop(int top) {
        this.top = top;
        return this;
    }

    /**
     * Sets the number of items in the queried collection that are to be skipped and not included
     * in the returned result.
     *
     * @param skip The skip value.
     * @return ListHookOptions object itself.
     */
    public ListHookOptions setSkip(int skip) {
        this.skip = skip;
        return this;
    }
}
