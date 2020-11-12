// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography.options;

import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;

/**
 * A class containing configuration parameters that can be applied when decrypting AES-CBC keys with and without
 * padding.
 */
public class AesCbcDecryptOptions extends DecryptOptions {
    /**
     * Creates an instance of {@link AesCbcDecryptOptions} with the given parameters.
     *
     * @param algorithm The algorithm to be used for decryption.
     * @param ciphertext The content to be decrypted.
     */
    AesCbcDecryptOptions(EncryptionAlgorithm algorithm, byte[] ciphertext) {
        super(algorithm, ciphertext);
    }

    /**
     * Set the given initialization vector to be used in this decryption operation.
     *
     * @param iv Initialization vector for the decryption operation.
     * @return The updated {@link AesCbcDecryptOptions} object.
     */
    public AesCbcDecryptOptions setIv(byte[] iv) {
        if (iv == null) {
            this.iv = null;
        } else {
            this.iv = new byte[iv.length];
            System.arraycopy(iv, 0, this.iv, 0, iv.length);
        }

        return this;
    }
}
