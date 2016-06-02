/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Azure Storage account information.
 */
public class StorageAccountInfoInner {
    /**
     * Gets or sets the account name associated with the Azure storage account.
     */
    @JsonProperty(required = true)
    private String name;

    /**
     * Gets or sets the properties associated with this storage account.
     */
    @JsonProperty(required = true)
    private StorageAccountProperties properties;

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
     * @return the StorageAccountInfoInner object itself.
     */
    public StorageAccountInfoInner withName(String name) {
        this.name = name;
        return this;
    }

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
     * @return the StorageAccountInfoInner object itself.
     */
    public StorageAccountInfoInner withProperties(StorageAccountProperties properties) {
        this.properties = properties;
        return this;
    }

}
