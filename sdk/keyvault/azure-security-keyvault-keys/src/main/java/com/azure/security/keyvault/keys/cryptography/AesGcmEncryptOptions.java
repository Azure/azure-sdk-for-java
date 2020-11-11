// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

/**
 * A class containing configuration parameters that can be applied when encrypting AES-GCM keys.
 */
public class AesGcmEncryptOptions extends EncryptOptions {
    /**
     * Creates an instance of {@link AesGcmEncryptOptions} with the given parameters.
     *
     * @param iv Initialization vector for the encryption operation.
     * @param additionalAuthenticatedData Additional data to authenticate but not encrypt/decrypt when using
     * authenticated crypto algorithms.
     */
    public AesGcmEncryptOptions(byte[] iv, byte[] additionalAuthenticatedData) {
        super(iv, additionalAuthenticatedData);
    }
}
