// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.util.Base64Url;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The key operation result.
 */
class KeyOperationResult {
    private static final byte[] EMPTY_ARRAY = new byte[0];

    /**
     * Key identifier.
     */
    @JsonProperty(value = "kid", access = JsonProperty.Access.WRITE_ONLY)
    private String kid;

    /**
     * The result property.
     */
    @JsonProperty(value = "value", access = JsonProperty.Access.WRITE_ONLY)
    private Base64Url result;

    /**
     * Initialization vector used for some cryptographic operations.
     */
    @JsonProperty(value = "iv", access = JsonProperty.Access.WRITE_ONLY)
    private Base64Url iv;

    /**
     * Authentication tag used for some cryptographic operations.
     */
    @JsonProperty(value = "tag", access = JsonProperty.Access.WRITE_ONLY)
    private Base64Url authenticationTag;

    /**
     * Additional authenticated data used for some cryptographic operations.
     */
    @JsonProperty(value = "aad", access = JsonProperty.Access.WRITE_ONLY)
    private Base64Url additionalAuthenticatedData;

    /**
     * Get the key identifier.
     *
     * @return The key identifier.
     */
    public String getKid() {
        return this.kid;
    }

    /**
     * Get the result.
     *
     * @return The result.
     */
    public byte[] getResult() {
        if (this.result == null) {
            return EMPTY_ARRAY;
        }

        return this.result.decodedBytes();
    }

    /**
     * Get the initialization vector.
     *
     * @return The initialization vector.
     */
    public byte[] getIv() {
        if (this.iv == null) {
            return EMPTY_ARRAY;
        }

        return this.iv.decodedBytes();
    }

    /**
     * Get the authentication tag.
     *
     * @return The authentication tag.
     */
    public byte[] getAuthenticationTag() {
        if (this.authenticationTag == null) {
            return EMPTY_ARRAY;
        }

        return this.authenticationTag.decodedBytes();
    }

    /**
     * Get the authentication tag.
     *
     * @return The additional authenticated data.
     */
    public byte[] getAdditionalAuthenticatedData() {
        if (this.additionalAuthenticatedData == null) {
            return EMPTY_ARRAY;
        }

        return this.additionalAuthenticatedData.decodedBytes();
    }
}
