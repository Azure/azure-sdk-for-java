// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.implementation;

import com.azure.core.http.rest.Page;
import com.azure.security.keyvault.certificates.models.IssuerBase;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * A page of Issuer {@link IssuerBase} resources and a link to get the next page of
 * resources, if any.
 */
public final class IssuerBasePage implements Page<IssuerBase> {

    /**
     * The link to the next page.
     */
    @JsonProperty("nextLink")
    private String nextLink;

    /**
     * The list of items.
     */
    @JsonProperty("value")
    private List<IssuerBase> items;

    /**
     * Gets the link to the next page. Or {@code null} if there are no more resources to fetch.
     *
     * @return The link to the next page.
     */
    @Override
    public String nextLink() {
        return this.nextLink;
    }

    /**
     * Gets the list of {@link IssuerBase issuers} on this page.
     *
     * @return The list of items in {@link List}.
     */
    @Override
    public List<IssuerBase> items() {
        return items;
    }
}
