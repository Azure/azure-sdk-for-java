// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

/**
 * A class containing configuration parameters that can be applied when decrypting AES-CBC keys with and without
 * padding.
 */
public class AesCbcDecryptOptions extends DecryptOptions {
    /**
     * Creates an instance of {@link AesCbcDecryptOptions} with the given parameters.
     *
     * @param iv Initialization vector for the decryption operation.
     */
    public AesCbcDecryptOptions(byte[] iv) {
        super(iv, null, null);
    }
}
