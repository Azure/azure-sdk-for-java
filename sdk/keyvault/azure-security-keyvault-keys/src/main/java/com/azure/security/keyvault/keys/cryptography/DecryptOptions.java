// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;

import java.util.Objects;

/**
 * A class containing various configuration parameters that can be applied when performing decryption operations.
 */
public class DecryptOptions {
    /**
     * The algorithm to be used for decryption.
     */
    final EncryptionAlgorithm algorithm;

    /**
     * The content to be decrypted.
     */
    final byte[] cipherText;

    /**
     * Initialization vector to be used in the decryption operation using a symmetric algorithm.
     */
    byte[] iv;

    /**
     * Get additional data to authenticate when performing decryption with an authenticated algorithm.
     */
    byte[] additionalAuthenticatedData;

    /**
     * The tag to authenticate when performing decryption with an authenticated algorithm.
     */
    byte[] authenticationTag;

    /**
     * Factory method to create an instance of {@link AesCbcDecryptOptions} with the given parameters.
     *
     * @param algorithm The algorithm to be used for decryption.
     * @param ciphertext The content to be decrypted.
     * @return The {@link AesCbcDecryptOptions}.
     */
    public static AesCbcDecryptOptions createAesCbcOptions(EncryptionAlgorithm algorithm, byte[] ciphertext) {
        return new AesCbcDecryptOptions(algorithm, ciphertext);
    }

    /**
     * Factory method to create an instance of {@link AesGcmDecryptOptions} with the given parameters.
     *
     * @param algorithm The algorithm to be used for decryption.
     * @param ciphertext The content to be decrypted.
     * @param iv Initialization vector for the decryption operation.
     * @return The {@link AesGcmDecryptOptions}.
     */
    public static AesGcmDecryptOptions createAesGcmOptions(EncryptionAlgorithm algorithm, byte[] ciphertext, byte[] iv) {
        return new AesGcmDecryptOptions(algorithm, ciphertext, iv);
    }

    /**
     * Creates an instance of {@link DecryptOptions} with the given parameters.
     *
     * @param algorithm The algorithm to be used for decryption.
     * @param cipherText The content to be decrypted.
     */
    DecryptOptions(EncryptionAlgorithm algorithm, byte[] cipherText) {
        Objects.requireNonNull(algorithm, "'algorithm cannot be null'");
        Objects.requireNonNull(cipherText, "'ciphertext' cannot be null");

        this.algorithm = algorithm;
        this.cipherText = new byte[cipherText.length];
        System.arraycopy(cipherText, 0, this.cipherText, 0, cipherText.length);
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
    public byte[] getCipherText() {
        if (cipherText == null) {
            return null;
        } else {
            return cipherText.clone();
        }
    }

    /**
     * Get the initialization vector to be used in the decryption operation using a symmetric algorithm.
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
     * Get additional data to authenticate when performing decryption with an authenticated algorithm.
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

    /**
     * Get the tag to authenticate when performing decryption with an authenticated algorithm.
     *
     * @return The authentication tag.
     */
    public byte[] getAuthenticationTag() {
        if (authenticationTag == null) {
            return null;
        } else {
            return authenticationTag.clone();
        }
    }
}
