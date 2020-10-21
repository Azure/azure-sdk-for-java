// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.rest;

import java.io.Serializable;
import java.util.List;

/**
 * The CertificateItem REST model.
 */
public class CertificateListResult implements Serializable {

    /**
     * Stores the value.
     */
    private List<CertificateItem> value;

    /**
     * Get the value.
     *
     * @return the id.
     */
    public List<CertificateItem> getValue() {
        return value;
    }

    /**
     * Set the value.
     *
     * @param value the value.
     */
    public void setValue(List<CertificateItem> value) {
        this.value = value;
    }
}
