// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.keys.models;

import com.azure.core.annotation.Immutable;
import com.azure.security.keyvault.keys.KeyAsyncClient;
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.implementation.ByteExtensions;

/**
 * A class containing random bytes obtained by calling {@link KeyClient#getRandomBytes(int)} or
 * {@link KeyAsyncClient#getRandomBytes(int)}.
 */
@Immutable
public class RandomBytes {
    /*
     * The random bytes.
     */
    private final byte[] bytes;

    /**
     * Creates and instance of {@link RandomBytes}.
     *
     * @param bytes The random bytes.
     */
    public RandomBytes(byte[] bytes) {
        this.bytes = ByteExtensions.clone(bytes);
    }

    /**
     * Get the random bytes.
     *
     * @return The random bytes.
     */
    public byte[] getBytes() {
        return ByteExtensions.clone(this.bytes);
    }
}
