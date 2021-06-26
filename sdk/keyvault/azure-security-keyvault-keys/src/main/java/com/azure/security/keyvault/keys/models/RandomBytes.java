// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.keys.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.Base64Url;
import com.azure.core.util.CoreUtils;
import com.azure.security.keyvault.keys.KeyAsyncClient;
import com.azure.security.keyvault.keys.KeyClient;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A class containing random bytes obtained by calling {@link KeyClient#getRandomBytes(int)} or
 * {@link KeyAsyncClient#getRandomBytes(int)}.
 */
@Immutable
public class RandomBytes {
    /*
     * The bytes encoded as a base64url string.
     */
    @JsonProperty(value = "value")
    private final Base64Url bytes;

    /**
     * Creates and instance of {@link RandomBytes}.
     *
     * @param bytes The bytes.
     */
    public RandomBytes(byte[] bytes) {
        if (bytes == null) {
            this.bytes = null;
        } else {
            this.bytes = Base64Url.encode(CoreUtils.clone(bytes));
        }
    }

    /**
     * Get the bytes encoded as a base64url string.
     *
     * @return The bytes encoded as a base64url string.
     */
    public byte[] getBytes() {
        if (this.bytes == null) {
            return null;
        }

        return this.bytes.decodedBytes();
    }
}
