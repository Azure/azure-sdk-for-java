// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;

/**
 * A class containing configuration parameters that can be applied when encrypting AES-GCM keys.
 */
public class AesGcmEncryptOptions extends EncryptOptions {
    /**
     * Creates an instance of {@link AesGcmEncryptOptions} with the given parameters.
     *
     * @param algorithm The algorithm to be used for encryption.
     * @param ciphertext The content to be encrypted.
     * @param iv Initialization vector for the encryption operation.
     */
    AesGcmEncryptOptions(EncryptionAlgorithm algorithm, byte[] ciphertext, byte[] iv) {
        super(algorithm, ciphertext);

        if (iv == null) {
            this.iv = null;
        } else {
            this.iv = new byte[iv.length];
            System.arraycopy(iv, 0, this.iv, 0, iv.length);
        }
    }

    /**
     * Set additional data to authenticate when using authenticated crypto algorithms.
     *
     * @param additionalAuthenticatedData Additional data to authenticate when using authenticated crypto algorithms.
     * @return The updated {@link AesGcmEncryptOptions} object.
     */
    public AesGcmEncryptOptions setAdditionalAuthenticatedData(byte[] additionalAuthenticatedData) {
        if (additionalAuthenticatedData == null) {
            this.additionalAuthenticatedData = null;
        } else {
            this.additionalAuthenticatedData = new byte[additionalAuthenticatedData.length];
            System.arraycopy(additionalAuthenticatedData, 0, this.additionalAuthenticatedData, 0,
                additionalAuthenticatedData.length);
        }

        return this;
    }
}
