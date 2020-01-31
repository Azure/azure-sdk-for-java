// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.implementation;

import com.azure.core.http.rest.Page;
import com.azure.core.util.IterableStream;
import com.azure.security.keyvault.certificates.models.IssuerProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * A page of Issuer {@link IssuerProperties} resources and a link to get the next page of
 * resources, if any.
 */
public final class IssuerPropertiesPage implements Page<IssuerProperties> {

    /**
     * The link to the next page.
     */
    @JsonProperty("nextLink")
    private String continuationToken;

    /**
     * The list of items.
     */
    @JsonProperty("value")
    private List<IssuerProperties> items;

    /**
     * Gets the link to the next page. Or {@code null} if there are no more resources to fetch.
     *
     * @return The link to the next page.
     */
    @Override
    public String getContinuationToken() {
        return this.continuationToken;
    }

    /**
     * Gets the iterable stream of {@link IssuerProperties issuers} on this page.
     *
     * @return The iterable stream of items in {@link List}.
     */
    @Override
    public IterableStream<IssuerProperties> getElements() {
        return IterableStream.of(items);
    }
}
