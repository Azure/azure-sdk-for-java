// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets.implementation;

import com.azure.core.http.rest.Page;
import com.azure.security.keyvault.secrets.models.SecretBase;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * A page of Azure App Configuration {@link SecretBase} resources and a link to get the next page of
 * resources, if any.
 */
public final class SecretBasePage implements Page<SecretBase> {

    /**
     * The link to the next page.
     */
    @JsonProperty("nextLink")
    private String nextLink;

    /**
     * The list of items.
     */
    @JsonProperty("value")
    private List<SecretBase> items;

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
     * Gets the list of {@link SecretBase SecretBase} on this page.
     *
     * @return The list of items in {@link List}.
     */
    @Override
    public List<SecretBase> getItems() {
        return items;
    }
}
