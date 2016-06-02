/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.implementation.api;


/**
 * A Data Lake Analytics catalog item list.
 */
public class CatalogItemList {
    /**
     * Gets or sets the count of items in the list.
     */
    private Integer count;

    /**
     * Gets or sets the link to the next page of results.
     */
    private String nextLink;

    /**
     * Get the count value.
     *
     * @return the count value
     */
    public Integer count() {
        return this.count;
    }

    /**
     * Set the count value.
     *
     * @param count the count value to set
     * @return the CatalogItemList object itself.
     */
    public CatalogItemList withCount(Integer count) {
        this.count = count;
        return this;
    }

    /**
     * Get the nextLink value.
     *
     * @return the nextLink value
     */
    public String nextLink() {
        return this.nextLink;
    }

    /**
     * Set the nextLink value.
     *
     * @param nextLink the nextLink value to set
     * @return the CatalogItemList object itself.
     */
    public CatalogItemList withNextLink(String nextLink) {
        this.nextLink = nextLink;
        return this;
    }

}
