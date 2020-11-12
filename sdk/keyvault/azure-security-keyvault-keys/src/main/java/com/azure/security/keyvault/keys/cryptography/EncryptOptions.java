// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;

import java.util.Objects;

/**
 * A class containing various configuration parameters that can be applied when performing encryption operations.
 */
public class EncryptOptions {
    /**
     * The algorithm to be used for encryption.
     */
    final EncryptionAlgorithm algorithm;

    /**
     * The content to be encrypted.
     */
    final byte[] plainText;

    /**
     * Initialization vector to be used in the encryption operation using a symmetric algorithm.
     */
    byte[] iv;

    /**
     * Get additional data to authenticate when performing encryption with an authenticated algorithm.
     */
    byte[] additionalAuthenticatedData;

    /**
     * Factory method to create an instance of {@link AesCbcEncryptOptions} with the given parameters.
     *
     * @param algorithm The algorithm to be used for encryption.
     * @param plaintext The content to be encryption.
     * @return The {@link AesCbcEncryptOptions}.
     */
    public static AesCbcEncryptOptions createAesCbcOptions(EncryptionAlgorithm algorithm, byte[] plaintext) {
        return new AesCbcEncryptOptions(algorithm, plaintext);
    }

    /**
     * Factory method to create an instance of {@link AesGcmEncryptOptions} with the given parameters.
     *
     * @param algorithm The algorithm to be used for encryption.
     * @param plaintext The content to be encryption.
     * @param iv Initialization vector for the encryption operation.
     * @return The {@link AesGcmEncryptOptions}.
     */
    public static AesGcmEncryptOptions createAesGcmOptions(EncryptionAlgorithm algorithm, byte[] plaintext, byte[] iv) {
        return new AesGcmEncryptOptions(algorithm, plaintext, iv);
    }

    /**
     * Creates an instance of {@link EncryptOptions} with the given parameters.
     *
     * @param algorithm The algorithm to be used for encryption.
     * @param plainText The content to be encrypted.
     */
    EncryptOptions(EncryptionAlgorithm algorithm, byte[] plainText) {
        Objects.requireNonNull(algorithm, "'algorithm cannot be null'");
        Objects.requireNonNull(plainText, "'plaintext' cannot be null");

        this.algorithm = algorithm;
        this.plainText = new byte[plainText.length];
        System.arraycopy(plainText, 0, this.plainText, 0, plainText.length);
    }

    /**
     * The algorithm to be used for encryption.
     *
     * @return The algorithm to be used for encryption.
     */
    public EncryptionAlgorithm getAlgorithm() {
        return algorithm;
    }

    /**
     * Get the content to be encrypted.
     *
     * @return The content to be encrypted.
     */
    public byte[] getPlainText() {
        if (plainText == null) {
            return null;
        } else {
            return plainText.clone();
        }
    }

    /**
     * Get the initialization vector to be used in the encryption operation using a symmetric algorithm.
     *
     * @return The initialization vector.
     */
    public byte[] getIv() {
        if (iv == null) {
            return null;
        } else {
            return iv.clone();
        }
    }

    /**
     * Get additional data to authenticate when performing encryption with an authenticated algorithm.
     *
     * @return The additional authenticated data.
     */
    public byte[] getAdditionalAuthenticatedData() {
        if (additionalAuthenticatedData == null) {
            return null;
        } else {
            return additionalAuthenticatedData.clone();
        }
    }
}
