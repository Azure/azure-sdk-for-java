// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * The CertificatePolicy REST model.
 */
public class CertificatePolicy implements Serializable {
    
    /**
     * Stores the serial version UID.
     */
    private static final long serialVersionUID = 1L;

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
