// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.core.annotation.Fluent;

/**
 * Additional properties for filtering results on the listCredentialEntities operation.
 */
@Fluent
public final class ListCredentialEntityOptions {
    private Integer maxPageSize;
    private Integer skip;

    /**
     * Gets limit indicating the number of items that will be included in a service returned page.
     *
     * @return The max page size value.
     */
    public Integer getMaxPageSize() {
        return this.maxPageSize;
    }

    /**
     * Sets limit indicating the number of items to be included in a service returned page.
     *
     * @param maxPageSize The max page size value.
     *
     * @return The ListCredentialEntityOptions object itself.
     */
    public ListCredentialEntityOptions setMaxPageSize(Integer maxPageSize) {
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
        return this.skip;
    }

    /**
     * Sets the number of items in the queried collection that are to be skipped and not included
     * in the returned result.
     *
     * @param skip The skip value.
     *
     * @return ListCredentialEntityOptions itself.
     */
    public ListCredentialEntityOptions setSkip(Integer skip) {
        this.skip = skip;
        return this;
    }
}
