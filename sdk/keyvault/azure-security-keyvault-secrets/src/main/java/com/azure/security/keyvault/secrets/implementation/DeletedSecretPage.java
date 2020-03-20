// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets.implementation;

import com.azure.core.http.rest.Page;
import com.azure.security.keyvault.secrets.models.DeletedSecret;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * A page of Deleted Secret {@link DeletedSecret} resources and a link to get the next page of
 * resources, if any.
 */
public final class DeletedSecretPage implements Page<DeletedSecret> {

    /**
     * The link to the next page.
     */
    @JsonProperty("nextLink")
    private String continuationToken;

    /**
     * The list of items.
     */
    @JsonProperty("value")
    private List<DeletedSecret> items;

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
     * Gets the list of {@link DeletedSecret deletedSecrets} on this page.
     *
     * @return The list of items in {@link List}.
     */
    @Override
    public List<DeletedSecret> getItems() {
        return items;
    }
}
