/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import java.util.List;

/**
 * Collection of certificates.
 */
public class CertificateCollectionInner {
    /**
     * Collection of resources.
     */
    private List<CertificateInner> value;

    /**
     * Link to next page of resources.
     */
    private String nextLink;

    /**
     * Get the value value.
     *
     * @return the value value
     */
    public List<CertificateInner> value() {
        return this.value;
    }

    /**
     * Set the value value.
     *
     * @param value the value value to set
     * @return the CertificateCollectionInner object itself.
     */
    public CertificateCollectionInner withValue(List<CertificateInner> value) {
        this.value = value;
        return this;
    }

    /**
     * Get the nextLink value.
     *
     * @return the nextLink value
     */
    public String nextLink() {
        return this.nextLink;
    }

    /**
     * Set the nextLink value.
     *
     * @param nextLink the nextLink value to set
     * @return the CertificateCollectionInner object itself.
     */
    public CertificateCollectionInner withNextLink(String nextLink) {
        this.nextLink = nextLink;
        return this;
    }

}
