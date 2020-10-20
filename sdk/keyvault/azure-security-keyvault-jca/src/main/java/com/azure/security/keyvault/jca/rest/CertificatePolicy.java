// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.rest;

import java.io.Serializable;

/**
 * The CertificatePolicy REST model.
 */
public class CertificatePolicy implements Serializable {

    /**
     * Stores the key properties.
     */
    private KeyProperties keyProperties;

    /**
     * Get the key properties.
     *
     * @return the key properties.
     */
    public KeyProperties getKey_props() {
        return keyProperties;
    }

    /**
     * Set the key properties.
     *
     * @param keyProperties the key properties.
     */
    public void setKey_props(KeyProperties keyProperties) {
        this.keyProperties = keyProperties;
    }
}
