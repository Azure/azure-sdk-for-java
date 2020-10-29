// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.rest;

import java.io.Serializable;

/**
 * The CertificateItem REST model.
 */
public class CertificateItem implements Serializable {

    /**
     * Stores the id.
     */
    private String id;

    /**
     * Get the id.
     *
     * @return the id.
     */
    public String getId() {
        return id;
    }

    /**
     * Set the id.
     *
     * @param id the id.
     */
    public void setId(String id) {
        this.id = id;
    }
}
