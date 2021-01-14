// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.util.CoreUtils;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;

import java.util.Objects;

/**
 * A class containing various configuration parameters that can be applied when performing encryption operations.
 */
public class EncryptOptions {
    /**
     * The algorithm to be used for encryption.
     */
    private final EncryptionAlgorithm algorithm;

    /**
     * The content to be encrypted.
     */
    private final byte[] plainText;

    /**
     * Initialization vector to be used in the encryption operation using a symmetric algorithm.
     */
    private final byte[] iv;

    /**
     * Get additional data to authenticate when performing encryption with an authenticated algorithm.
     */
    private final byte[] additionalAuthenticatedData;

    /**
     * Factory method to create an instance of {@link EncryptOptions} with the given parameters for
     * {@link EncryptionAlgorithm#A128CBC}.
     *
     * @param plainText The content to be encryption.
     * @return The {@link EncryptOptions}.
     */
    public static EncryptOptions createAes128CbcOptions(byte[] plainText) {
        return createAes128CbcOptions(plainText, null);
    }

    /**
     * Factory method to create an instance of {@link EncryptOptions} with the given parameters for
     * {@link EncryptionAlgorithm#A128CBC}.
     *
     * @param plainText The content to be encryption.
     * @param iv Initialization vector for the encryption operation.
     * @return The {@link EncryptOptions}.
     */
    public static EncryptOptions createAes128CbcOptions(byte[] plainText, byte[] iv) {
        return new EncryptOptions(EncryptionAlgorithm.A128CBC, plainText, iv, null);
    }

    /**
     * Factory method to create an instance of {@link EncryptOptions} with the given parameters for
     * {@link EncryptionAlgorithm#A128CBCPAD}.
     *
     * @param plainText The content to be encryption.
     * @return The {@link EncryptOptions}.
     */
    public static EncryptOptions createAes128CbcPadOptions(byte[] plainText) {
        return createAes128CbcPadOptions(plainText, null);
    }

    /**
     * Factory method to create an instance of {@link EncryptOptions} with the given parameters for
     * {@link EncryptionAlgorithm#A128CBCPAD}.
     *
     * @param plainText The content to be encryption.
     * @param iv Initialization vector for the encryption operation.
     * @return The {@link EncryptOptions}.
     */
    public static EncryptOptions createAes128CbcPadOptions(byte[] plainText, byte[] iv) {
        return new EncryptOptions(EncryptionAlgorithm.A128CBCPAD, plainText, iv, null);
    }

    /**
     * Factory method to create an instance of {@link EncryptOptions} with the given parameters for
     * {@link EncryptionAlgorithm#A128GCM}.
     *
     * @param plainText The content to be encryption.
     * @param iv Initialization vector for the encryption operation.
     * @return The {@link EncryptOptions}.
     */
    public static EncryptOptions createAes128GcmOptions(byte[] plainText, byte[] iv) {
        return createAes128GcmOptions(plainText, iv, null);
    }

    /**
     * Factory method to create an instance of {@link EncryptOptions} with the given parameters for
     * {@link EncryptionAlgorithm#A128GCM}.
     *
     * @param plainText The content to be encryption.
     * @param iv Initialization vector for the encryption operation.
     * @param additionalAuthenticatedData Additional data to authenticate when using authenticated crypto algorithms.
     * @return The {@link EncryptOptions}.
     */
    public static EncryptOptions createAes128GcmOptions(byte[] plainText, byte[] iv,
                                                        byte[] additionalAuthenticatedData) {
        return new EncryptOptions(EncryptionAlgorithm.A128GCM, plainText, iv, additionalAuthenticatedData);
    }

    /**
     * Factory method to create an instance of {@link EncryptOptions} with the given parameters for
     * {@link EncryptionAlgorithm#A192CBC}.
     *
     * @param plainText The content to be encryption.
     * @return The {@link EncryptOptions}.
     */
    public static EncryptOptions createAes192CbcOptions(byte[] plainText) {
        return createAes192CbcOptions(plainText, null);
    }

    /**
     * Factory method to create an instance of {@link EncryptOptions} with the given parameters for
     * {@link EncryptionAlgorithm#A192CBC}.
     *
     * @param plainText The content to be encryption.
     * @param iv Initialization vector for the encryption operation.
     * @return The {@link EncryptOptions}.
     */
    public static EncryptOptions createAes192CbcOptions(byte[] plainText, byte[] iv) {
        return new EncryptOptions(EncryptionAlgorithm.A192CBC, plainText, iv, null);
    }

    /**
     * Factory method to create an instance of {@link EncryptOptions} with the given parameters for
     * {@link EncryptionAlgorithm#A192CBCPAD}.
     *
     * @param plainText The content to be encryption.
     * @return The {@link EncryptOptions}.
     */
    public static EncryptOptions createAes192CbcPadOptions(byte[] plainText) {
        return createAes192CbcPadOptions(plainText, null);
    }

    /**
     * Factory method to create an instance of {@link EncryptOptions} with the given parameters for
     * {@link EncryptionAlgorithm#A192CBCPAD}.
     *
     * @param plainText The content to be encryption.
     * @param iv Initialization vector for the encryption operation.
     * @return The {@link EncryptOptions}.
     */
    public static EncryptOptions createAes192CbcPadOptions(byte[] plainText, byte[] iv) {
        return new EncryptOptions(EncryptionAlgorithm.A192CBCPAD, plainText, iv, null);
    }

    /**
     * Factory method to create an instance of {@link EncryptOptions} with the given parameters for
     * {@link EncryptionAlgorithm#A192GCM}.
     *
     * @param plainText The content to be encryption.
     * @param iv Initialization vector for the encryption operation.
     * @return The {@link EncryptOptions}.
     */
    public static EncryptOptions createAes192GcmOptions(byte[] plainText, byte[] iv) {
        return createAes192GcmOptions(plainText, iv, null);
    }

    /**
     * Factory method to create an instance of {@link EncryptOptions} with the given parameters for
     * {@link EncryptionAlgorithm#A192GCM}.
     *
     * @param plainText The content to be encryption.
     * @param iv Initialization vector for the encryption operation.
     * @param additionalAuthenticatedData Additional data to authenticate when using authenticated crypto algorithms.
     * @return The {@link EncryptOptions}.
     */
    public static EncryptOptions createAes192GcmOptions(byte[] plainText, byte[] iv,
                                                        byte[] additionalAuthenticatedData) {
        return new EncryptOptions(EncryptionAlgorithm.A192GCM, plainText, iv, additionalAuthenticatedData);
    }

    /**
     * Factory method to create an instance of {@link EncryptOptions} with the given parameters for
     * {@link EncryptionAlgorithm#A256CBC}.
     *
     * @param plainText The content to be encryption.
     * @return The {@link EncryptOptions}.
     */
    public static EncryptOptions createAes256CbcOptions(byte[] plainText) {
        return createAes256CbcOptions(plainText, null);
    }

    /**
     * Factory method to create an instance of {@link EncryptOptions} with the given parameters for
     * {@link EncryptionAlgorithm#A256CBC}.
     *
     * @param plainText The content to be encryption.
     * @param iv Initialization vector for the encryption operation.
     * @return The {@link EncryptOptions}.
     */
    public static EncryptOptions createAes256CbcOptions(byte[] plainText, byte[] iv) {
        return new EncryptOptions(EncryptionAlgorithm.A256CBC, plainText, iv, null);
    }

    /**
     * Factory method to create an instance of {@link EncryptOptions} with the given parameters for
     * {@link EncryptionAlgorithm#A256CBCPAD}.
     *
     * @param plainText The content to be encryption.
     * @return The {@link EncryptOptions}.
     */
    public static EncryptOptions createAes256CbcPadOptions(byte[] plainText) {
        return createAes256CbcPadOptions(plainText, null);
    }

    /**
     * Factory method to create an instance of {@link EncryptOptions} with the given parameters for
     * {@link EncryptionAlgorithm#A256CBCPAD}.
     *
     * @param plainText The content to be encryption.
     * @param iv Initialization vector for the encryption operation.
     * @return The {@link EncryptOptions}.
     */
    public static EncryptOptions createAes256CbcPadOptions(byte[] plainText, byte[] iv) {
        return new EncryptOptions(EncryptionAlgorithm.A256CBCPAD, plainText, iv, null);
    }

    /**
     * Factory method to create an instance of {@link EncryptOptions} with the given parameters for
     * {@link EncryptionAlgorithm#A256GCM}.
     *
     * @param plainText The content to be encryption.
     * @param iv Initialization vector for the encryption operation.
     * @return The {@link EncryptOptions}.
     */
    public static EncryptOptions createAes256GcmOptions(byte[] plainText, byte[] iv) {
        return createAes256GcmOptions(plainText, iv, null);
    }

    /**
     * Factory method to create an instance of {@link EncryptOptions} with the given parameters for
     * {@link EncryptionAlgorithm#A256GCM}.
     *
     * @param plainText The content to be encryption.
     * @param iv Initialization vector for the encryption operation.
     * @param additionalAuthenticatedData Additional data to authenticate when using authenticated crypto algorithms.
     * @return The {@link EncryptOptions}.
     */
    public static EncryptOptions createAes256GcmOptions(byte[] plainText, byte[] iv,
                                                        byte[] additionalAuthenticatedData) {
        return new EncryptOptions(EncryptionAlgorithm.A256GCM, plainText, iv, additionalAuthenticatedData);
    }

    /**
     * Creates an instance of {@link EncryptOptions} with the given parameters.
     *
     * @param algorithm The algorithm to be used for encryption.
     * @param plainText The content to be encrypted.
     * @param iv Initialization vector for the encryption operation.
     * @param additionalAuthenticatedData Additional data to authenticate when using authenticated crypto algorithms.
     */
    EncryptOptions(EncryptionAlgorithm algorithm, byte[] plainText, byte[] iv, byte[] additionalAuthenticatedData) {
        Objects.requireNonNull(algorithm, "Encryption algorithm cannot be null.");
        Objects.requireNonNull(plainText, "Plain text content to be encrypted cannot be null.");

        this.algorithm = algorithm;
        this.plainText = CoreUtils.clone(plainText);
        this.iv = CoreUtils.clone(iv);
        this.additionalAuthenticatedData = CoreUtils.clone(additionalAuthenticatedData);
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
        return CoreUtils.clone(plainText);
    }

    /**
     * Get the initialization vector to be used in the encryption operation using a symmetric algorithm.
     *
     * @return The initialization vector.
     */
    public byte[] getIv() {
        return CoreUtils.clone(iv);
    }

    /**
     * Get additional data to authenticate when performing encryption with an authenticated algorithm.
     *
     * @return The additional authenticated data.
     */
    public byte[] getAdditionalAuthenticatedData() {
        return CoreUtils.clone(additionalAuthenticatedData);
    }
}
