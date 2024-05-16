// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The CertificatePolicy REST model.
 */
public class CertificatePolicy {

    /**
     * Stores the key properties.
     */
    @JsonProperty("key_props")
    private KeyProperties keyProperties;

    /**
     * Get the key properties.
     *
     * @return the key properties.
     */
    public KeyProperties getKeyProperties() {
        return keyProperties;
    }

    /**
     * Set the key properties.
     *
     * @param keyProperties the key properties.
     */
    public void setKeyProperties(KeyProperties keyProperties) {
        this.keyProperties = keyProperties;
    }
}
