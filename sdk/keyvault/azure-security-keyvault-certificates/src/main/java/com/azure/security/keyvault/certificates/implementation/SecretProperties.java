// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.implementation;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Properties of the key backing a certificate.
 */
@Fluent
public final class SecretProperties {

    /**
     * Creates an instance of {@link SecretProperties}.
     *
     * @param contentType The content type.
     */
    public SecretProperties(String contentType) {
        this.contentType = contentType;
    }

    /**
     * The media type (MIME type).
     */
    @JsonProperty(value = "contentType")
    private String contentType;

    /**
     * Get the contentType value.
     *
     * @return the contentType value
     */
    public String contentType() {
        return this.contentType;
    }

    /**
     * Set the contentType value.
     *
     * @param contentType the contentType value to set
     * @return the SecretProperties object itself.
     */
    public SecretProperties contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }
}
