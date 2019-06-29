// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.blob.models;

import com.azure.storage.blob.models.AccessPolicy;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * signed identifier.
 */
@JacksonXmlRootElement(localName = "SignedIdentifier")
public final class SignedIdentifier {
    /*
     * a unique id
     */
    @JsonProperty(value = "Id", required = true)
    private String id;

    /*
     * The accessPolicy property.
     */
    @JsonProperty(value = "AccessPolicy", required = true)
    private com.azure.storage.blob.models.AccessPolicy accessPolicy;

    /**
     * Get the id property: a unique id.
     *
     * @return the id value.
     */
    public String id() {
        return this.id;
    }

    /**
     * Set the id property: a unique id.
     *
     * @param id the id value to set.
     * @return the SignedIdentifier object itself.
     */
    public SignedIdentifier id(String id) {
        this.id = id;
        return this;
    }

    /**
     * Get the accessPolicy property: The accessPolicy property.
     *
     * @return the accessPolicy value.
     */
    public com.azure.storage.blob.models.AccessPolicy accessPolicy() {
        return this.accessPolicy;
    }

    /**
     * Set the accessPolicy property: The accessPolicy property.
     *
     * @param accessPolicy the accessPolicy value to set.
     * @return the SignedIdentifier object itself.
     */
    public SignedIdentifier accessPolicy(AccessPolicy accessPolicy) {
        this.accessPolicy = accessPolicy;
        return this;
    }
}
