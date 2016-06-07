/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.implementation.api;

import java.util.UUID;

/**
 * A Data Lake Analytics catalog item.
 */
public class CatalogItem {
    /**
     * Gets or sets the name of the Data Lake Analytics account.
     */
    private String computeAccountName;

    /**
     * Gets or sets the version of the catalog item.
     */
    private UUID version;

    /**
     * Get the computeAccountName value.
     *
     * @return the computeAccountName value
     */
    public String computeAccountName() {
        return this.computeAccountName;
    }

    /**
     * Set the computeAccountName value.
     *
     * @param computeAccountName the computeAccountName value to set
     * @return the CatalogItem object itself.
     */
    public CatalogItem withComputeAccountName(String computeAccountName) {
        this.computeAccountName = computeAccountName;
        return this;
    }

    /**
     * Get the version value.
     *
     * @return the version value
     */
    public UUID version() {
        return this.version;
    }

    /**
     * Set the version value.
     *
     * @param version the version value to set
     * @return the CatalogItem object itself.
     */
    public CatalogItem withVersion(UUID version) {
        this.version = version;
        return this;
    }

}
