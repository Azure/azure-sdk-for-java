/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Lake Store account information.
 */
public class DataLakeStoreAccountInfoInner {
    /**
     * Gets or sets the account name of the Data Lake Store account.
     */
    @JsonProperty(required = true)
    private String name;

    /**
     * Gets or sets the properties associated with this Data Lake Store
     * account.
     */
    private DataLakeStoreAccountInfoProperties properties;

    /**
     * Get the name value.
     *
     * @return the name value
     */
    public String name() {
        return this.name;
    }

    /**
     * Set the name value.
     *
     * @param name the name value to set
     * @return the DataLakeStoreAccountInfoInner object itself.
     */
    public DataLakeStoreAccountInfoInner withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the properties value.
     *
     * @return the properties value
     */
    public DataLakeStoreAccountInfoProperties properties() {
        return this.properties;
    }

    /**
     * Set the properties value.
     *
     * @param properties the properties value to set
     * @return the DataLakeStoreAccountInfoInner object itself.
     */
    public DataLakeStoreAccountInfoInner withProperties(DataLakeStoreAccountInfoProperties properties) {
        this.properties = properties;
        return this;
    }

}
