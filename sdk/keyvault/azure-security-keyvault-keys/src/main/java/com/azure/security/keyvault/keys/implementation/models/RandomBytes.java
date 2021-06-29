// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.keys.implementation.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.Base64Url;
import com.azure.core.util.CoreUtils;
import com.azure.security.keyvault.keys.KeyAsyncClient;
import com.azure.security.keyvault.keys.KeyClient;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A class containing random bytes obtained by calling {@link KeyClient#getRandomBytes(int)} or
 * {@link KeyAsyncClient#getRandomBytes(int)}.
 */
@Fluent
public class RandomBytes {
    /*
     * The bytes encoded as a base64url string.
     */
    @JsonProperty(value = "value")
    private Base64Url bytes;

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

    /**
     * Set the random bytes.
     *
     * @param bytes The random bytes to set.
     * @return The updated {@link RandomBytes} object.
     */
    public RandomBytes setValue(byte[] bytes) {
        if (bytes == null) {
            this.bytes = null;
        } else {
            this.bytes = Base64Url.encode(CoreUtils.clone(bytes));
        }

        return this;
    }
}
