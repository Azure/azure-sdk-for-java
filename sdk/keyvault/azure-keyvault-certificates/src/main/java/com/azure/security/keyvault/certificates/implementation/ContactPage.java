// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.implementation;

import com.azure.core.http.rest.Page;
import com.azure.security.keyvault.certificates.models.Contact;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * A page of Contact {@link Contact} resources and a link to get the next page of
 * resources, if any.
 */
public final class ContactPage implements Page<Contact> {

    /**
     * The list of items.
     */
    @JsonProperty("contacts")
    private List<Contact> items;

    /**
     * Gets the list of {@link Contact contacts} on this page.
     *
     * @return The list of items in {@link List}.
     */
    @Override
    public List<Contact> items() {
        return items;
    }

    @Override
    public String nextLink() {
        return null;
    }
}
