// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.applicationconfig.implementation;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * An instance of this class defines a page of Azure App Configuration resources and a link to get the next page of
 * resources, if any.
 *
 * @param <T> type of Azure App Configuration resource to deserialize.
 */
public final class Page<T> {
    /**
     * The link to the next page.
     */
    @JsonProperty("@nextLink")
    private String nextPageLink;

    /**
     * The list of items.
     */
    @JsonProperty()
    private List<T> items;

    /**
     * Gets the link to the next page.
     *
     * @return the link to the next page.
     */
    public String nextPageLink() {
        return this.nextPageLink;
    }

    /**
     * Gets the list of items.
     *
     * @return the list of items in {@link List}.
     */
    public List<T> items() {
        return items;
    }
}
