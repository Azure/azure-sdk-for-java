// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

class CryptoUtil {

    /*
     * Determines whether the key is valid and of required size.
     * 
     * @param key The key to be checked.
     * @param keySizeInBytes The minimum size required for the key
     */
    static void validate(byte[] key, int keySizeInBytes) {
        if (key == null) {
            throw new IllegalArgumentException("key must not be null");
        }

        if (key.length < keySizeInBytes) {
            throw new IllegalArgumentException(String.format("key must be at least %d bits long", keySizeInBytes << 3));
        }
    }
}
