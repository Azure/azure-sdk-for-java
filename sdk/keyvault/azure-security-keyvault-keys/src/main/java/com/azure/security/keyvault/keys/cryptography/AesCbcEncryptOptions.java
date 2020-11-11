// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

/**
 * A class containing configuration parameters that can be applied when encrypting AES-CBC keys with and without
 * padding.
 */
public class AesCbcEncryptOptions extends EncryptOptions {
    /**
     * Creates an instance of {@link AesCbcEncryptOptions} with the given parameters.
     *
     * @param iv Initialization vector for the encryption operation.
     */
    public AesCbcEncryptOptions(byte[] iv) {
        super(iv, null);
    }
}
