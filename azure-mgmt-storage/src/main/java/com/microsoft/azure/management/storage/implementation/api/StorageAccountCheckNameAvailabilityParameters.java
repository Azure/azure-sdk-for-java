/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.storage.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The StorageAccountCheckNameAvailabilityParameters model.
 */
public class StorageAccountCheckNameAvailabilityParameters {
    /**
     * The name property.
     */
    @JsonProperty(required = true)
    private String name;

    /**
     * The type property.
     */
    private String type;

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
     * @return the StorageAccountCheckNameAvailabilityParameters object itself.
     */
    public StorageAccountCheckNameAvailabilityParameters withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the type value.
     *
     * @return the type value
     */
    public String type() {
        return this.type;
    }

    /**
     * Set the type value.
     *
     * @param type the type value to set
     * @return the StorageAccountCheckNameAvailabilityParameters object itself.
     */
    public StorageAccountCheckNameAvailabilityParameters withType(String type) {
        this.type = type;
        return this;
    }

}
