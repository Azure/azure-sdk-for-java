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

}
