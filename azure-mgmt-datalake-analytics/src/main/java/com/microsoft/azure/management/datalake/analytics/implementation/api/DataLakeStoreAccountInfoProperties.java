/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.implementation.api;


/**
 * Data Lake Store account properties information.
 */
public class DataLakeStoreAccountInfoProperties {
    /**
     * Gets or sets the optional suffix for the Data Lake Store account.
     */
    private String suffix;

    /**
     * Get the suffix value.
     *
     * @return the suffix value
     */
    public String suffix() {
        return this.suffix;
    }

    /**
     * Set the suffix value.
     *
     * @param suffix the suffix value to set
     * @return the DataLakeStoreAccountInfoProperties object itself.
     */
    public DataLakeStoreAccountInfoProperties withSuffix(String suffix) {
        this.suffix = suffix;
        return this;
    }

}
