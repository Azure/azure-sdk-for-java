// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.keys.implementation.models;

import com.azure.core.annotation.Fluent;
import com.azure.security.keyvault.keys.KeyAsyncClient;
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.implementation.Base64UrlJsonDeserializer;
import com.azure.security.keyvault.keys.implementation.Base64UrlJsonSerializer;
import com.azure.security.keyvault.keys.implementation.ByteExtensions;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * A class containing random bytes obtained by calling {@link KeyClient#getRandomBytes(int)} or
 * {@link KeyAsyncClient#getRandomBytes(int)}.
 */
@Fluent
public class RandomBytes {
    /*
     * The random bytes.
     */
    @JsonProperty(value = "value")
    @JsonSerialize(using = Base64UrlJsonSerializer.class)
    @JsonDeserialize(using = Base64UrlJsonDeserializer.class)
    private byte[] bytes;

    /**
     * Get the random bytes.
     *
     * @return The random bytes.
     */
    public byte[] getBytes() {
        return ByteExtensions.clone(this.bytes);
    }

    /**
     * Set the random bytes.
     *
     * @param bytes The random bytes to set.
     * @return The updated {@link RandomBytes} object.
     */
    public RandomBytes setBytes(byte[] bytes) {
        this.bytes = ByteExtensions.clone(bytes);

        return this;
    }
}
