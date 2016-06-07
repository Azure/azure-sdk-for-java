/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Additional Azure Storage account parameters.
 */
public class AddStorageAccountParametersInner {
    /**
     * Gets or sets the properties for the Azure Storage account being added.
     */
    @JsonProperty(required = true)
    private StorageAccountProperties properties;

    /**
     * Get the properties value.
     *
     * @return the properties value
     */
    public StorageAccountProperties properties() {
        return this.properties;
    }

    /**
     * Set the properties value.
     *
     * @param properties the properties value to set
     * @return the AddStorageAccountParametersInner object itself.
     */
    public AddStorageAccountParametersInner withProperties(StorageAccountProperties properties) {
        this.properties = properties;
        return this;
    }

}
