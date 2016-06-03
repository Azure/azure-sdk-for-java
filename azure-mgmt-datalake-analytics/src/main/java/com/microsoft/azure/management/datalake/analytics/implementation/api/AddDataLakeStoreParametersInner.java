/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Additional Data Lake Store parameters.
 */
public class AddDataLakeStoreParametersInner {
    /**
     * Gets or sets the properties for the Data Lake Store account being added.
     */
    @JsonProperty(required = true)
    private DataLakeStoreAccountInfoProperties properties;

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
     * @return the AddDataLakeStoreParametersInner object itself.
     */
    public AddDataLakeStoreParametersInner withProperties(DataLakeStoreAccountInfoProperties properties) {
        this.properties = properties;
        return this;
    }

}
