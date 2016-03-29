/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Additional Data Lake Store parameters.
 */
public class AddDataLakeStoreParameters {
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
    public DataLakeStoreAccountInfoProperties getProperties() {
        return this.properties;
    }

    /**
     * Set the properties value.
     *
     * @param properties the properties value to set
     */
    public void setProperties(DataLakeStoreAccountInfoProperties properties) {
        this.properties = properties;
    }

}
