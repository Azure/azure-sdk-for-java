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
    private final byte[] plaintext;

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
     * @param plaintext The content to be encryption.
     * @return The {@link EncryptOptions}.
     */
    public static EncryptOptions createAes128CbcOptions(byte[] plaintext) {
        return createAes128CbcOptions(plaintext, null);
    }

    /**
     * Factory method to create an instance of {@link EncryptOptions} with the given parameters for
     * {@link EncryptionAlgorithm#A128CBC}.
     *
     * @param plaintext The content to be encryption.
     * @param iv Initialization vector for the encryption operation.
     * @return The {@link EncryptOptions}.
     */
    public static EncryptOptions createAes128CbcOptions(byte[] plaintext, byte[] iv) {
        return new EncryptOptions(EncryptionAlgorithm.A128CBC, plaintext, iv, null);
    }

    /**
     * Factory method to create an instance of {@link EncryptOptions} with the given parameters for
     * {@link EncryptionAlgorithm#A128CBCPAD}.
     *
     * @param plaintext The content to be encryption.
     * @return The {@link EncryptOptions}.
     */
    public static EncryptOptions createAes128CbcPadOptions(byte[] plaintext) {
        return createAes128CbcPadOptions(plaintext, null);
    }

    /**
     * Factory method to create an instance of {@link EncryptOptions} with the given parameters for
     * {@link EncryptionAlgorithm#A128CBCPAD}.
     *
     * @param plaintext The content to be encryption.
     * @param iv Initialization vector for the encryption operation.
     * @return The {@link EncryptOptions}.
     */
    public static EncryptOptions createAes128CbcPadOptions(byte[] plaintext, byte[] iv) {
        return new EncryptOptions(EncryptionAlgorithm.A128CBCPAD, plaintext, iv, null);
    }

    /**
     * Factory method to create an instance of {@link EncryptOptions} with the given parameters for
     * {@link EncryptionAlgorithm#A128GCM}.
     *
     * @param plaintext The content to be encryption.
     * @param iv Initialization vector for the encryption operation.
     * @return The {@link EncryptOptions}.
     */
    public static EncryptOptions createAes128GcmOptions(byte[] plaintext, byte[] iv) {
        return createAes128GcmOptions(plaintext, iv, null);
    }

    /**
     * Factory method to create an instance of {@link EncryptOptions} with the given parameters for
     * {@link EncryptionAlgorithm#A128GCM}.
     *
     * @param plaintext The content to be encryption.
     * @param iv Initialization vector for the encryption operation.
     * @param additionalAuthenticatedData Additional data to authenticate when using authenticated crypto algorithms.
     * @return The {@link EncryptOptions}.
     */
    public static EncryptOptions createAes128GcmOptions(byte[] plaintext, byte[] iv,
                                                        byte[] additionalAuthenticatedData) {
        return new EncryptOptions(EncryptionAlgorithm.A128GCM, plaintext, iv, additionalAuthenticatedData);
    }

    /**
     * Factory method to create an instance of {@link EncryptOptions} with the given parameters for
     * {@link EncryptionAlgorithm#A192CBC}.
     *
     * @param plaintext The content to be encryption.
     * @return The {@link EncryptOptions}.
     */
    public static EncryptOptions createAes192CbcOptions(byte[] plaintext) {
        return createAes192CbcOptions(plaintext, null);
    }

    /**
     * Factory method to create an instance of {@link EncryptOptions} with the given parameters for
     * {@link EncryptionAlgorithm#A192CBC}.
     *
     * @param plaintext The content to be encryption.
     * @param iv Initialization vector for the encryption operation.
     * @return The {@link EncryptOptions}.
     */
    public static EncryptOptions createAes192CbcOptions(byte[] plaintext, byte[] iv) {
        return new EncryptOptions(EncryptionAlgorithm.A192CBC, plaintext, iv, null);
    }

    /**
     * Factory method to create an instance of {@link EncryptOptions} with the given parameters for
     * {@link EncryptionAlgorithm#A192CBCPAD}.
     *
     * @param plaintext The content to be encryption.
     * @return The {@link EncryptOptions}.
     */
    public static EncryptOptions createAes192CbcPadOptions(byte[] plaintext) {
        return createAes192CbcPadOptions(plaintext, null);
    }

    /**
     * Factory method to create an instance of {@link EncryptOptions} with the given parameters for
     * {@link EncryptionAlgorithm#A192CBCPAD}.
     *
     * @param plaintext The content to be encryption.
     * @param iv Initialization vector for the encryption operation.
     * @return The {@link EncryptOptions}.
     */
    public static EncryptOptions createAes192CbcPadOptions(byte[] plaintext, byte[] iv) {
        return new EncryptOptions(EncryptionAlgorithm.A192CBCPAD, plaintext, iv, null);
    }

    /**
     * Factory method to create an instance of {@link EncryptOptions} with the given parameters for
     * {@link EncryptionAlgorithm#A192GCM}.
     *
     * @param plaintext The content to be encryption.
     * @param iv Initialization vector for the encryption operation.
     * @return The {@link EncryptOptions}.
     */
    public static EncryptOptions createAes192GcmOptions(byte[] plaintext, byte[] iv) {
        return createAes192GcmOptions(plaintext, iv, null);
    }

    /**
     * Factory method to create an instance of {@link EncryptOptions} with the given parameters for
     * {@link EncryptionAlgorithm#A192GCM}.
     *
     * @param plaintext The content to be encryption.
     * @param iv Initialization vector for the encryption operation.
     * @param additionalAuthenticatedData Additional data to authenticate when using authenticated crypto algorithms.
     * @return The {@link EncryptOptions}.
     */
    public static EncryptOptions createAes192GcmOptions(byte[] plaintext, byte[] iv,
                                                        byte[] additionalAuthenticatedData) {
        return new EncryptOptions(EncryptionAlgorithm.A192GCM, plaintext, iv, additionalAuthenticatedData);
    }

    /**
     * Factory method to create an instance of {@link EncryptOptions} with the given parameters for
     * {@link EncryptionAlgorithm#A256CBC}.
     *
     * @param plaintext The content to be encryption.
     * @return The {@link EncryptOptions}.
     */
    public static EncryptOptions createAes256CbcOptions(byte[] plaintext) {
        return createAes256CbcOptions(plaintext, null);
    }

    /**
     * Factory method to create an instance of {@link EncryptOptions} with the given parameters for
     * {@link EncryptionAlgorithm#A256CBC}.
     *
     * @param plaintext The content to be encryption.
     * @param iv Initialization vector for the encryption operation.
     * @return The {@link EncryptOptions}.
     */
    public static EncryptOptions createAes256CbcOptions(byte[] plaintext, byte[] iv) {
        return new EncryptOptions(EncryptionAlgorithm.A256CBC, plaintext, iv, null);
    }

    /**
     * Factory method to create an instance of {@link EncryptOptions} with the given parameters for
     * {@link EncryptionAlgorithm#A256CBCPAD}.
     *
     * @param plaintext The content to be encryption.
     * @return The {@link EncryptOptions}.
     */
    public static EncryptOptions createAes256CbcPadOptions(byte[] plaintext) {
        return createAes256CbcPadOptions(plaintext, null);
    }

    /**
     * Factory method to create an instance of {@link EncryptOptions} with the given parameters for
     * {@link EncryptionAlgorithm#A256CBCPAD}.
     *
     * @param plaintext The content to be encryption.
     * @param iv Initialization vector for the encryption operation.
     * @return The {@link EncryptOptions}.
     */
    public static EncryptOptions createAes256CbcPadOptions(byte[] plaintext, byte[] iv) {
        return new EncryptOptions(EncryptionAlgorithm.A256CBCPAD, plaintext, iv, null);
    }

    /**
     * Factory method to create an instance of {@link EncryptOptions} with the given parameters for
     * {@link EncryptionAlgorithm#A256GCM}.
     *
     * @param plaintext The content to be encryption.
     * @param iv Initialization vector for the encryption operation.
     * @return The {@link EncryptOptions}.
     */
    public static EncryptOptions createAes256GcmOptions(byte[] plaintext, byte[] iv) {
        return createAes256GcmOptions(plaintext, iv, null);
    }

    /**
     * Factory method to create an instance of {@link EncryptOptions} with the given parameters for
     * {@link EncryptionAlgorithm#A256GCM}.
     *
     * @param plaintext The content to be encryption.
     * @param iv Initialization vector for the encryption operation.
     * @param additionalAuthenticatedData Additional data to authenticate when using authenticated crypto algorithms.
     * @return The {@link EncryptOptions}.
     */
    public static EncryptOptions createAes256GcmOptions(byte[] plaintext, byte[] iv,
                                                        byte[] additionalAuthenticatedData) {
        return new EncryptOptions(EncryptionAlgorithm.A256GCM, plaintext, iv, additionalAuthenticatedData);
    }

    /**
     * Creates an instance of {@link EncryptOptions} with the given parameters.
     *
     * @param algorithm The algorithm to be used for encryption.
     * @param plaintext The content to be encrypted.
     * @param iv Initialization vector for the encryption operation.
     * @param additionalAuthenticatedData Additional data to authenticate when using authenticated crypto algorithms.
     */
    EncryptOptions(EncryptionAlgorithm algorithm, byte[] plaintext, byte[] iv, byte[] additionalAuthenticatedData) {
        Objects.requireNonNull(algorithm, "Encryption algorithm cannot be null.");
        Objects.requireNonNull(plaintext, "Plain text content to be encrypted cannot be null.");

        this.algorithm = algorithm;
        this.plaintext = CoreUtils.clone(plaintext);
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
    public byte[] getPlaintext() {
        return CoreUtils.clone(plaintext);
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
