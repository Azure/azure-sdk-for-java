// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography.models;

import com.azure.core.util.CoreUtils;

import java.util.Objects;

/**
 * A class containing various configuration parameters that can be applied when performing decryption operations.
 */
public final class DecryptParameters {
    /**
     * The algorithm to be used for decryption.
     */
    private final EncryptionAlgorithm algorithm;

    /**
     * The content to be decrypted.
     */
    private final byte[] ciphertext;

    /**
     * Initialization vector to be used in the decryption operation using a symmetric algorithm.
     */
    private final byte[] iv;

    /**
     * Get additional data to authenticate when performing decryption with an authenticated algorithm.
     */
    private final byte[] additionalAuthenticatedData;

    /**
     * The tag to authenticate when performing decryption with an authenticated algorithm.
     */
    private final byte[] authenticationTag;

    /**
     * Factory method to create an instance of {@link DecryptParameters} with the given parameters for
     * {@link EncryptionAlgorithm#A128CBC}.
     *
     * @param ciphertext The content to be decrypted.
     * @param iv Initialization vector for the decryption operation.
     *
     * @return The {@link DecryptParameters}.
     */
    public static DecryptParameters createA128CbcParameters(byte[] ciphertext, byte[] iv) {
        return new DecryptParameters(EncryptionAlgorithm.A128CBC, ciphertext, iv, null, null);
    }

    /**
     * Factory method to create an instance of {@link DecryptParameters} with the given parameters for
     * {@link EncryptionAlgorithm#A128CBCPAD}.
     *
     * @param ciphertext The content to be decrypted.
     * @param iv Initialization vector for the decryption operation.
     *
     * @return The {@link DecryptParameters}.
     */
    public static DecryptParameters createA128CbcPadParameters(byte[] ciphertext, byte[] iv) {
        return new DecryptParameters(EncryptionAlgorithm.A128CBCPAD, ciphertext, iv, null, null);
    }

    /**
     * Factory method to create an instance of {@link DecryptParameters} with the given parameters for
     * {@link EncryptionAlgorithm#A128GCM}.
     *
     * @param ciphertext The content to be decrypted.
     * @param iv Initialization vector for the decryption operation.
     * @param authenticationTag The tag to authenticate when performing decryption.
     *
     * @return The {@link DecryptParameters}.
     */
    public static DecryptParameters createA128GcmParameters(byte[] ciphertext, byte[] iv, byte[] authenticationTag) {
        return createA128GcmParameters(ciphertext, iv, authenticationTag, null);
    }

    /**
     * Factory method to create an instance of {@link DecryptParameters} with the given parameters for
     * {@link EncryptionAlgorithm#A128GCM}.
     *
     * @param ciphertext The content to be decrypted.
     * @param iv Initialization vector for the decryption operation.
     * @param authenticationTag The tag to authenticate when performing decryption.
     * @param additionalAuthenticatedData Additional data to authenticate when using authenticated crypto algorithms.
     *
     * @return The {@link DecryptParameters}.
     */
    public static DecryptParameters createA128GcmParameters(byte[] ciphertext, byte[] iv, byte[] authenticationTag,
                                                            byte[] additionalAuthenticatedData) {
        return new DecryptParameters(EncryptionAlgorithm.A128GCM, ciphertext, iv, authenticationTag,
            additionalAuthenticatedData);
    }

    /**
     * Factory method to create an instance of {@link DecryptParameters} with the given parameters for
     * {@link EncryptionAlgorithm#A192CBC}.
     *
     * @param ciphertext The content to be decrypted.
     * @param iv Initialization vector for the decryption operation.
     *
     * @return The {@link DecryptParameters}.
     */
    public static DecryptParameters createA192CbcParameters(byte[] ciphertext, byte[] iv) {
        return new DecryptParameters(EncryptionAlgorithm.A192CBC, ciphertext, iv, null, null);
    }

    /**
     * Factory method to create an instance of {@link DecryptParameters} with the given parameters for
     * {@link EncryptionAlgorithm#A192CBCPAD}.
     *
     * @param ciphertext The content to be decrypted.
     * @param iv Initialization vector for the decryption operation.
     *
     * @return The {@link DecryptParameters}.
     */
    public static DecryptParameters createA192CbcPadParameters(byte[] ciphertext, byte[] iv) {
        return new DecryptParameters(EncryptionAlgorithm.A192CBCPAD, ciphertext, iv, null, null);
    }

    /**
     * Factory method to create an instance of {@link DecryptParameters} with the given parameters for
     * {@link EncryptionAlgorithm#A192GCM}.
     *
     * @param ciphertext The content to be decrypted.
     * @param iv Initialization vector for the decryption operation.
     * @param authenticationTag The tag to authenticate when performing decryption.
     *
     * @return The {@link DecryptParameters}.
     */
    public static DecryptParameters createA192GcmParameters(byte[] ciphertext, byte[] iv, byte[] authenticationTag) {
        return createA192GcmParameters(ciphertext, iv, authenticationTag, null);
    }

    /**
     * Factory method to create an instance of {@link DecryptParameters} with the given parameters for
     * {@link EncryptionAlgorithm#A192GCM}.
     *
     * @param ciphertext The content to be decrypted.
     * @param iv Initialization vector for the decryption operation.
     * @param authenticationTag The tag to authenticate when performing decryption.
     * @param additionalAuthenticatedData Additional data to authenticate when using authenticated crypto algorithms.
     *
     * @return The {@link DecryptParameters}.
     */
    public static DecryptParameters createA192GcmParameters(byte[] ciphertext, byte[] iv, byte[] authenticationTag,
                                                            byte[] additionalAuthenticatedData) {
        return new DecryptParameters(EncryptionAlgorithm.A192GCM, ciphertext, iv, authenticationTag,
            additionalAuthenticatedData);
    }

    /**
     * Factory method to create an instance of {@link DecryptParameters} with the given parameters for
     * {@link EncryptionAlgorithm#A256CBC}.
     *
     * @param ciphertext The content to be decrypted.
     * @param iv Initialization vector for the decryption operation.
     *
     * @return The {@link DecryptParameters}.
     */
    public static DecryptParameters createA256CbcParameters(byte[] ciphertext, byte[] iv) {
        return new DecryptParameters(EncryptionAlgorithm.A256CBC, ciphertext, iv, null, null);
    }

    /**
     * Factory method to create an instance of {@link DecryptParameters} with the given parameters for
     * {@link EncryptionAlgorithm#A256CBCPAD}.
     *
     * @param ciphertext The content to be decrypted.
     * @param iv Initialization vector for the decryption operation.
     *
     * @return The {@link DecryptParameters}.
     */
    public static DecryptParameters createA256CbcPadParameters(byte[] ciphertext, byte[] iv) {
        return new DecryptParameters(EncryptionAlgorithm.A256CBCPAD, ciphertext, iv, null, null);
    }

    /**
     * Factory method to create an instance of {@link DecryptParameters} with the given parameters for
     * {@link EncryptionAlgorithm#A256GCM}.
     *
     * @param ciphertext The content to be decrypted.
     * @param iv Initialization vector for the decryption operation.
     * @param authenticationTag The tag to authenticate when performing decryption.
     *
     * @return The {@link DecryptParameters}.
     */
    public static DecryptParameters createA256GcmParameters(byte[] ciphertext, byte[] iv, byte[] authenticationTag) {
        return createA256GcmParameters(ciphertext, iv, authenticationTag, null);
    }

    /**
     * Factory method to create an instance of {@link DecryptParameters} with the given parameters for
     * {@link EncryptionAlgorithm#A256GCM}.
     *
     * @param ciphertext The content to be decrypted.
     * @param iv Initialization vector for the decryption operation.
     * @param authenticationTag The tag to authenticate when performing decryption.
     * @param additionalAuthenticatedData Additional data to authenticate when using authenticated crypto algorithms.
     *
     * @return The {@link DecryptParameters}.
     */
    public static DecryptParameters createA256GcmParameters(byte[] ciphertext, byte[] iv, byte[] authenticationTag,
                                                            byte[] additionalAuthenticatedData) {
        return new DecryptParameters(EncryptionAlgorithm.A256GCM, ciphertext, iv, authenticationTag,
            additionalAuthenticatedData);
    }

    /**
     * Factory method to create an instance of {@link DecryptParameters} with the given parameters for
     * {@link EncryptionAlgorithm#RSA1_5}.
     *
     * @param ciphertext The content to be decrypted.
     *
     * @return The {@link DecryptParameters}.
     */
    public static DecryptParameters createRsa15Parameters(byte[] ciphertext) {
        return new DecryptParameters(EncryptionAlgorithm.RSA1_5, ciphertext, null, null,
            null);
    }

    /**
     * Factory method to create an instance of {@link DecryptParameters} with the given parameters for
     * {@link EncryptionAlgorithm#RSA_OAEP}.
     *
     * @param ciphertext The content to be decrypted.
     *
     * @return The {@link DecryptParameters}.
     */
    public static DecryptParameters createRsaOaepParameters(byte[] ciphertext) {
        return new DecryptParameters(EncryptionAlgorithm.RSA_OAEP, ciphertext, null, null,
            null);
    }

    /**
     * Factory method to create an instance of {@link DecryptParameters} with the given parameters for
     * {@link EncryptionAlgorithm#RSA_OAEP_256}.
     *
     * @param ciphertext The content to be decrypted.
     *
     * @return The {@link DecryptParameters}.
     */
    public static DecryptParameters createRsaOaep256Parameters(byte[] ciphertext) {
        return new DecryptParameters(EncryptionAlgorithm.RSA_OAEP_256, ciphertext, null, null,
            null);
    }

    /**
     * Creates an instance of {@link DecryptParameters} with the given parameters.
     *
     * @param algorithm The algorithm to be used for decryption.
     * @param ciphertext The content to be decrypted.
     * @param iv Initialization vector for the decryption operation.
     * @param authenticationTag The tag to authenticate when performing decryption.
     * @param additionalAuthenticatedData Additional data to authenticate when using authenticated crypto algorithms.
     */
    DecryptParameters(EncryptionAlgorithm algorithm, byte[] ciphertext, byte[] iv, byte[] authenticationTag,
                      byte[] additionalAuthenticatedData) {
        Objects.requireNonNull(algorithm, "Encryption algorithm cannot be null.");
        Objects.requireNonNull(ciphertext, "Cipher text content to be decrypted cannot be null.");

        if (algorithm == EncryptionAlgorithm.A128GCM || algorithm == EncryptionAlgorithm.A192GCM
            || algorithm == EncryptionAlgorithm.A256GCM) {

            Objects.requireNonNull(authenticationTag, "Authentication tag cannot be null for GCM decryption.");
        }

        this.algorithm = algorithm;
        this.ciphertext = CoreUtils.clone(ciphertext);
        this.iv = CoreUtils.clone(iv);
        this.additionalAuthenticatedData = CoreUtils.clone(additionalAuthenticatedData);
        this.authenticationTag = CoreUtils.clone(authenticationTag);
    }

    /**
     * The algorithm to be used for decryption.
     *
     * @return The algorithm to be used for decryption.
     */
    public EncryptionAlgorithm getAlgorithm() {
        return algorithm;
    }

    /**
     * Get the content to be decrypted.
     *
     * @return The content to be decrypted.
     */
    public byte[] getCipherText() {
        return CoreUtils.clone(ciphertext);
    }

    /**
     * Get the initialization vector to be used in the decryption operation using a symmetric algorithm.
     *
     * @return The initialization vector.
     */
    public byte[] getIv() {
        return CoreUtils.clone(iv);
    }

    /**
     * Get additional data to authenticate when performing decryption with an authenticated algorithm.
     *
     * @return The additional authenticated data.
     */
    public byte[] getAdditionalAuthenticatedData() {
        return CoreUtils.clone(additionalAuthenticatedData);
    }

    /**
     * Get the tag to authenticate when performing decryption with an authenticated algorithm.
     *
     * @return The authentication tag.
     */
    public byte[] getAuthenticationTag() {
        return CoreUtils.clone(authenticationTag);
    }
}
