// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.Context;
import com.azure.digitaltwins.core.DigitalTwinsAsyncClient;
import com.azure.digitaltwins.core.DigitalTwinsClient;

/**
 * The optional parameters for {@link DigitalTwinsClient#listEventRoutes(ListDigitalTwinsEventRoutesOptions, Context)}
 * and {@link DigitalTwinsAsyncClient#listEventRoutes(ListDigitalTwinsEventRoutesOptions)}
 */
@Fluent
public final class ListDigitalTwinsEventRoutesOptions {
    /*
     * The maximum number of items to retrieve per request. The server may choose to return less than the requested
     * number.
     */
    private Integer maxItemsPerPage;

    /**
     * Creates a new instance of {@link ListDigitalTwinsEventRoutesOptions}.
     */
    public ListDigitalTwinsEventRoutesOptions() {
    }

    /**
     * Get the maxItemsPerPage property: The maximum number of items to retrieve per request. The server may choose to
     * return less than the requested number.
     *
     * @return the maxItemsPerPage value.
     */
    public Integer getMaxItemsPerPage() {
        return this.maxItemsPerPage;
    }

    /**
     * Set the maxItemsPerPage property: The maximum number of items to retrieve per request. The server may choose to
     * return less than the requested number.
     *
     * @param maxItemsPerPage the maxItemsPerPage value to set.
     * @return the ListEventRoutesOptions object itself.
     */
    public ListDigitalTwinsEventRoutesOptions setMaxItemsPerPage(Integer maxItemsPerPage) {
        this.maxItemsPerPage = maxItemsPerPage;
        return this;
    }
}
