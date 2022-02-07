// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.model;

import java.util.List;

/**
 * The CertificateItem REST model.
 */
public class CertificateListResult {

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

    /**
     * Get the NextLint
     * @return the nextLink
     */
    public String getNextLink() {
        return nextLink;
    }

    /**
     * Set the NextLink
     * @param nextLink the nextLink
     */
    public void setNextLink(String nextLink) {
        this.nextLink = nextLink;
    }

    private String nextLink;
}
