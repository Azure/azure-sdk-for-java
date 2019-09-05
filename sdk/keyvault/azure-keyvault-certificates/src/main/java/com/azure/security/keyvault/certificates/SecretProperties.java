// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Properties of the key backing a certificate.
 */
class SecretProperties {

    SecretProperties(String contentType) {
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
    String contentType() {
        return this.contentType;
    }

    /**
     * Set the contentType value.
     *
     * @param contentType the contentType value to set
     * @return the SecretProperties object itself.
     */
    SecretProperties contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }
}
