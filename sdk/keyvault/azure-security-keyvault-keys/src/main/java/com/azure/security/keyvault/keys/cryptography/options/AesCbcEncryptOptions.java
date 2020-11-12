// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography.options;

import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;

/**
 * A class containing configuration parameters that can be applied when encrypting AES-CBC keys with and without
 * padding.
 */
public class AesCbcEncryptOptions extends EncryptOptions {
    /**
     * Creates an instance of {@link AesCbcEncryptOptions} with the given parameters.
     *
     * @param algorithm The algorithm to be used for encryption.
     * @param plaintext The content to be encrypted.
     */
    AesCbcEncryptOptions(EncryptionAlgorithm algorithm, byte[] plaintext) {
        super(algorithm, plaintext);
    }

    /**
     * Set the given initialization vector to be used in this encryption operation.
     *
     * @param iv Initialization vector for the encryption operation.
     * @return The updated {@link AesCbcEncryptOptions} object.
     */
    public AesCbcEncryptOptions setIv(byte[] iv) {
        if (iv == null) {
            this.iv = null;
        } else {
            this.iv = new byte[iv.length];
            System.arraycopy(iv, 0, this.iv, 0, iv.length);
        }

        return this;
    }
}
