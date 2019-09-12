// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.implementation;

import com.azure.core.http.rest.Page;
import com.azure.security.keyvault.certificates.models.CertificateBase;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * A page of Azure App Configuration {@link CertificateBase} resources and a link to get the next page of
 * resources, if any.
 */
public final class CertificateBasePage implements Page<CertificateBase> {

    /**
     * The link to the next page.
     */
    @JsonProperty("nextLink")
    private String nextLink;

    /**
     * The list of items.
     */
    @JsonProperty("value")
    private List<CertificateBase> items;

    /**
     * Gets the link to the next page. Or {@code null} if there are no more resources to fetch.
     *
     * @return The link to the next page.
     */
    @Override
    public String getNextLink() {
        return this.nextLink;
    }

    /**
     * Gets the list of {@link CertificateBase CertificateBase} on this page.
     *
     * @return The list of items in {@link List}.
     */
    @Override
    public List<CertificateBase> getItems() {
        return items;
    }
}

