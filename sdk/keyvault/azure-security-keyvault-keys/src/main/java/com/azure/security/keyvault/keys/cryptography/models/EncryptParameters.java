// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography.models;

import com.azure.core.util.CoreUtils;

import java.util.Objects;

/**
 * A class containing various configuration parameters that can be applied when performing encryption operations.
 */
public final class EncryptParameters {
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
     * Factory method to create an instance of {@link EncryptParameters} with the given parameters for
     * {@link EncryptionAlgorithm#A128CBC}.
     *
     * @param plaintext The content to be encrypted.
     *
     * @return The {@link EncryptParameters}.
     */
    public static EncryptParameters createA128CbcParameters(byte[] plaintext) {
        return createA128CbcParameters(plaintext, null);
    }

    /**
     * Factory method to create an instance of {@link EncryptParameters} with the given parameters for
     * {@link EncryptionAlgorithm#A128CBC}.
     *
     * @param plaintext The content to be encrypted.
     * @param iv Initialization vector for the encryption operation.
     *
     * @return The {@link EncryptParameters}.
     */
    public static EncryptParameters createA128CbcParameters(byte[] plaintext, byte[] iv) {
        return new EncryptParameters(EncryptionAlgorithm.A128CBC, plaintext, iv, null);
    }

    /**
     * Factory method to create an instance of {@link EncryptParameters} with the given parameters for
     * {@link EncryptionAlgorithm#A128CBCPAD}.
     *
     * @param plaintext The content to be encrypted.
     *
     * @return The {@link EncryptParameters}.
     */
    public static EncryptParameters createA128CbcPadParameters(byte[] plaintext) {
        return createA128CbcPadParameters(plaintext, null);
    }

    /**
     * Factory method to create an instance of {@link EncryptParameters} with the given parameters for
     * {@link EncryptionAlgorithm#A128CBCPAD}.
     *
     * @param plaintext The content to be encrypted.
     * @param iv Initialization vector for the encryption operation.
     *
     * @return The {@link EncryptParameters}.
     */
    public static EncryptParameters createA128CbcPadParameters(byte[] plaintext, byte[] iv) {
        return new EncryptParameters(EncryptionAlgorithm.A128CBCPAD, plaintext, iv, null);
    }

    /**
     * Factory method to create an instance of {@link EncryptParameters} with the given parameters for
     * {@link EncryptionAlgorithm#A128GCM}.
     *
     * @param plaintext The content to be encrypted.
     *
     * @return The {@link EncryptParameters}.
     */
    public static EncryptParameters createA128GcmParameters(byte[] plaintext) {
        return createA128GcmParameters(plaintext, null);
    }

    /**
     * Factory method to create an instance of {@link EncryptParameters} with the given parameters for
     * {@link EncryptionAlgorithm#A128GCM}.
     *
     * @param plaintext The content to be encrypted.
     * @param additionalAuthenticatedData Additional data to authenticate when using authenticated crypto algorithms.
     *
     * @return The {@link EncryptParameters}.
     */
    public static EncryptParameters createA128GcmParameters(byte[] plaintext, byte[] additionalAuthenticatedData) {
        return new EncryptParameters(EncryptionAlgorithm.A128GCM, plaintext, null, additionalAuthenticatedData);
    }

    /**
     * Factory method to create an instance of {@link EncryptParameters} with the given parameters for
     * {@link EncryptionAlgorithm#A192CBC}.
     *
     * @param plaintext The content to be encrypted.
     *
     * @return The {@link EncryptParameters}.
     */
    public static EncryptParameters createA192CbcParameters(byte[] plaintext) {
        return createA192CbcParameters(plaintext, null);
    }

    /**
     * Factory method to create an instance of {@link EncryptParameters} with the given parameters for
     * {@link EncryptionAlgorithm#A192CBC}.
     *
     * @param plaintext The content to be encrypted.
     * @param iv Initialization vector for the encryption operation.
     *
     * @return The {@link EncryptParameters}.
     */
    public static EncryptParameters createA192CbcParameters(byte[] plaintext, byte[] iv) {
        return new EncryptParameters(EncryptionAlgorithm.A192CBC, plaintext, iv, null);
    }

    /**
     * Factory method to create an instance of {@link EncryptParameters} with the given parameters for
     * {@link EncryptionAlgorithm#A192CBCPAD}.
     *
     * @param plaintext The content to be encrypted.
     *
     * @return The {@link EncryptParameters}.
     */
    public static EncryptParameters createA192CbcPadParameters(byte[] plaintext) {
        return createA192CbcPadParameters(plaintext, null);
    }

    /**
     * Factory method to create an instance of {@link EncryptParameters} with the given parameters for
     * {@link EncryptionAlgorithm#A192CBCPAD}.
     *
     * @param plaintext The content to be encrypted.
     * @param iv Initialization vector for the encryption operation.
     *
     * @return The {@link EncryptParameters}.
     */
    public static EncryptParameters createA192CbcPadParameters(byte[] plaintext, byte[] iv) {
        return new EncryptParameters(EncryptionAlgorithm.A192CBCPAD, plaintext, iv, null);
    }

    /**
     * Factory method to create an instance of {@link EncryptParameters} with the given parameters for
     * {@link EncryptionAlgorithm#A192GCM}.
     *
     * @param plaintext The content to be encrypted.
     *
     * @return The {@link EncryptParameters}.
     */
    public static EncryptParameters createA192GcmParameters(byte[] plaintext) {
        return createA192GcmParameters(plaintext, null);
    }

    /**
     * Factory method to create an instance of {@link EncryptParameters} with the given parameters for
     * {@link EncryptionAlgorithm#A192GCM}.
     *
     * @param plaintext The content to be encrypted.
     * @param additionalAuthenticatedData Additional data to authenticate when using authenticated crypto algorithms.
     *
     * @return The {@link EncryptParameters}.
     */
    public static EncryptParameters createA192GcmParameters(byte[] plaintext, byte[] additionalAuthenticatedData) {
        return new EncryptParameters(EncryptionAlgorithm.A192GCM, plaintext, null, additionalAuthenticatedData);
    }

    /**
     * Factory method to create an instance of {@link EncryptParameters} with the given parameters for
     * {@link EncryptionAlgorithm#A256CBC}.
     *
     * @param plaintext The content to be encrypted.
     *
     * @return The {@link EncryptParameters}.
     */
    public static EncryptParameters createA256CbcParameters(byte[] plaintext) {
        return createA256CbcParameters(plaintext, null);
    }

    /**
     * Factory method to create an instance of {@link EncryptParameters} with the given parameters for
     * {@link EncryptionAlgorithm#A256CBC}.
     *
     * @param plaintext The content to be encrypted.
     * @param iv Initialization vector for the encryption operation.
     *
     * @return The {@link EncryptParameters}.
     */
    public static EncryptParameters createA256CbcParameters(byte[] plaintext, byte[] iv) {
        return new EncryptParameters(EncryptionAlgorithm.A256CBC, plaintext, iv, null);
    }

    /**
     * Factory method to create an instance of {@link EncryptParameters} with the given parameters for
     * {@link EncryptionAlgorithm#A256CBCPAD}.
     *
     * @param plaintext The content to be encrypted.
     *
     * @return The {@link EncryptParameters}.
     */
    public static EncryptParameters createA256CbcPadParameters(byte[] plaintext) {
        return createA256CbcPadParameters(plaintext, null);
    }

    /**
     * Factory method to create an instance of {@link EncryptParameters} with the given parameters for
     * {@link EncryptionAlgorithm#A256CBCPAD}.
     *
     * @param plaintext The content to be encrypted.
     * @param iv Initialization vector for the encryption operation.
     *
     * @return The {@link EncryptParameters}.
     */
    public static EncryptParameters createA256CbcPadParameters(byte[] plaintext, byte[] iv) {
        return new EncryptParameters(EncryptionAlgorithm.A256CBCPAD, plaintext, iv, null);
    }

    /**
     * Factory method to create an instance of {@link EncryptParameters} with the given parameters for
     * {@link EncryptionAlgorithm#A256GCM}.
     *
     * @param plaintext The content to be encrypted.
     *
     * @return The {@link EncryptParameters}.
     */
    public static EncryptParameters createA256GcmParameters(byte[] plaintext) {
        return createA256GcmParameters(plaintext, null);
    }

    /**
     * Factory method to create an instance of {@link EncryptParameters} with the given parameters for
     * {@link EncryptionAlgorithm#A256GCM}.
     *
     * @param plaintext The content to be encrypted.
     * @param additionalAuthenticatedData Additional data to authenticate when using authenticated crypto algorithms.
     *
     * @return The {@link EncryptParameters}.
     */
    public static EncryptParameters createA256GcmParameters(byte[] plaintext, byte[] additionalAuthenticatedData) {
        return new EncryptParameters(EncryptionAlgorithm.A256GCM, plaintext, null, additionalAuthenticatedData);
    }

    /**
     * Factory method to create an instance of {@link EncryptParameters} with the given parameters for
     * {@link EncryptionAlgorithm#RSA1_5}.
     *
     * @param plaintext The content to be encrypted.
     *
     * @return The {@link EncryptParameters}.
     */
    public static EncryptParameters createRsa15Parameters(byte[] plaintext) {
        return new EncryptParameters(EncryptionAlgorithm.RSA1_5, plaintext, null, null);
    }

    /**
     * Factory method to create an instance of {@link EncryptParameters} with the given parameters for
     * {@link EncryptionAlgorithm#RSA_OAEP}.
     *
     * @param plaintext The content to be encrypted.
     *
     * @return The {@link EncryptParameters}.
     */
    public static EncryptParameters createRsaOaepParameters(byte[] plaintext) {
        return new EncryptParameters(EncryptionAlgorithm.RSA_OAEP, plaintext, null, null);
    }

    /**
     * Factory method to create an instance of {@link EncryptParameters} with the given parameters for
     * {@link EncryptionAlgorithm#RSA_OAEP_256}.
     *
     * @param plaintext The content to be encrypted.
     *
     * @return The {@link EncryptParameters}.
     */
    public static EncryptParameters createRsaOaep256Parameters(byte[] plaintext) {
        return new EncryptParameters(EncryptionAlgorithm.RSA_OAEP_256, plaintext, null, null);
    }

    /**
     * Creates an instance of {@link EncryptParameters} with the given parameters.
     *
     * @param algorithm The algorithm to be used for encryption.
     * @param plaintext The content to be encrypted.
     * @param iv Initialization vector for the encryption operation.
     * @param additionalAuthenticatedData Additional data to authenticate when using authenticated crypto algorithms.
     */
    EncryptParameters(EncryptionAlgorithm algorithm, byte[] plaintext, byte[] iv, byte[] additionalAuthenticatedData) {
        Objects.requireNonNull(algorithm, "Encryption algorithm cannot be null.");
        Objects.requireNonNull(plaintext, "Plaintext content to be encrypted cannot be null.");

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
    public byte[] getPlainText() {
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
