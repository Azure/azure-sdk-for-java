// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.implementation;

import com.azure.core.http.rest.Page;
import com.azure.core.util.IterableStream;
import com.azure.security.keyvault.certificates.models.CertificateContact;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * A page of Contact {@link CertificateContact} resources and a link to get the next page of
 * resources, if any.
 */
public final class ContactPage implements Page<CertificateContact> {

    /**
     * The list of items.
     */
    @JsonProperty("contacts")
    private List<CertificateContact> items;

    /**
     * Gets the iterable stream of {@link CertificateContact contacts} on this page.
     *
     * @return The iterable stream of items in {@link List}.
     */
    @Override
    public IterableStream<CertificateContact> getElements() {
        return IterableStream.of(items);
    }

    @Override
    public String getContinuationToken() {
        return null;
    }
}
