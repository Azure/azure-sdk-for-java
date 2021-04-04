// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.util.CoreUtils;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;

import java.util.Objects;

/**
 * A class containing various configuration parameters that can be applied when performing decryption operations.
 */
public class DecryptParameters {
    /**
     * The algorithm to be used for decryption.
     */
    private final EncryptionAlgorithm algorithm;

    /**
     * The content to be decrypted.
     */
    private final byte[] cipherText;

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
     * @param cipherText The content to be decrypted.
     * @param iv Initialization vector for the decryption operation.
     *
     * @return The {@link DecryptParameters}.
     */
    public static DecryptParameters createAes128CbcParameters(byte[] cipherText, byte[] iv) {
        return new DecryptParameters(EncryptionAlgorithm.A128CBC, cipherText, iv, null, null);
    }

    /**
     * Factory method to create an instance of {@link DecryptParameters} with the given parameters for
     * {@link EncryptionAlgorithm#A128CBCPAD}.
     *
     * @param cipherText The content to be decrypted.
     * @param iv Initialization vector for the decryption operation.
     *
     * @return The {@link DecryptParameters}.
     */
    public static DecryptParameters createAes128CbcPadParameters(byte[] cipherText, byte[] iv) {
        return new DecryptParameters(EncryptionAlgorithm.A128CBCPAD, cipherText, iv, null, null);
    }

    /**
     * Factory method to create an instance of {@link DecryptParameters} with the given parameters for
     * {@link EncryptionAlgorithm#A128GCM}.
     *
     * @param cipherText The content to be decrypted.
     * @param iv Initialization vector for the decryption operation.
     * @param authenticationTag The tag to authenticate when performing decryption.
     *
     * @return The {@link DecryptParameters}.
     */
    public static DecryptParameters createAes128GcmParameters(byte[] cipherText, byte[] iv, byte[] authenticationTag) {
        return createAes128GcmParameters(cipherText, iv, authenticationTag, null);
    }

    /**
     * Factory method to create an instance of {@link DecryptParameters} with the given parameters for
     * {@link EncryptionAlgorithm#A128GCM}.
     *
     * @param cipherText The content to be decrypted.
     * @param iv Initialization vector for the decryption operation.
     * @param authenticationTag The tag to authenticate when performing decryption.
     * @param additionalAuthenticatedData Additional data to authenticate when using authenticated crypto algorithms.
     *
     * @return The {@link DecryptParameters}.
     */
    public static DecryptParameters createAes128GcmParameters(byte[] cipherText, byte[] iv, byte[] authenticationTag,
                                                              byte[] additionalAuthenticatedData) {
        return new DecryptParameters(EncryptionAlgorithm.A128GCM, cipherText, iv, authenticationTag,
            additionalAuthenticatedData);
    }

    /**
     * Factory method to create an instance of {@link DecryptParameters} with the given parameters for
     * {@link EncryptionAlgorithm#A192CBC}.
     *
     * @param cipherText The content to be decrypted.
     * @param iv Initialization vector for the decryption operation.
     *
     * @return The {@link DecryptParameters}.
     */
    public static DecryptParameters createAes192CbcParameters(byte[] cipherText, byte[] iv) {
        return new DecryptParameters(EncryptionAlgorithm.A192CBC, cipherText, iv, null, null);
    }

    /**
     * Factory method to create an instance of {@link DecryptParameters} with the given parameters for
     * {@link EncryptionAlgorithm#A192CBCPAD}.
     *
     * @param cipherText The content to be decrypted.
     * @param iv Initialization vector for the decryption operation.
     *
     * @return The {@link DecryptParameters}.
     */
    public static DecryptParameters createAes192CbcPadParameters(byte[] cipherText, byte[] iv) {
        return new DecryptParameters(EncryptionAlgorithm.A192CBCPAD, cipherText, iv, null, null);
    }

    /**
     * Factory method to create an instance of {@link DecryptParameters} with the given parameters for
     * {@link EncryptionAlgorithm#A192GCM}.
     *
     * @param cipherText The content to be decrypted.
     * @param iv Initialization vector for the decryption operation.
     * @param authenticationTag The tag to authenticate when performing decryption.
     *
     * @return The {@link DecryptParameters}.
     */
    public static DecryptParameters createAes192GcmParameters(byte[] cipherText, byte[] iv, byte[] authenticationTag) {
        return createAes192GcmParameters(cipherText, iv, authenticationTag, null);
    }

    /**
     * Factory method to create an instance of {@link DecryptParameters} with the given parameters for
     * {@link EncryptionAlgorithm#A192GCM}.
     *
     * @param cipherText The content to be decrypted.
     * @param iv Initialization vector for the decryption operation.
     * @param authenticationTag The tag to authenticate when performing decryption.
     * @param additionalAuthenticatedData Additional data to authenticate when using authenticated crypto algorithms.
     *
     * @return The {@link DecryptParameters}.
     */
    public static DecryptParameters createAes192GcmParameters(byte[] cipherText, byte[] iv, byte[] authenticationTag,
                                                              byte[] additionalAuthenticatedData) {
        return new DecryptParameters(EncryptionAlgorithm.A192GCM, cipherText, iv, authenticationTag,
            additionalAuthenticatedData);
    }

    /**
     * Factory method to create an instance of {@link DecryptParameters} with the given parameters for
     * {@link EncryptionAlgorithm#A256CBC}.
     *
     * @param cipherText The content to be decrypted.
     * @param iv Initialization vector for the decryption operation.
     *
     * @return The {@link DecryptParameters}.
     */
    public static DecryptParameters createAes256CbcParameters(byte[] cipherText, byte[] iv) {
        return new DecryptParameters(EncryptionAlgorithm.A256CBC, cipherText, iv, null, null);
    }

    /**
     * Factory method to create an instance of {@link DecryptParameters} with the given parameters for
     * {@link EncryptionAlgorithm#A256CBCPAD}.
     *
     * @param cipherText The content to be decrypted.
     * @param iv Initialization vector for the decryption operation.
     *
     * @return The {@link DecryptParameters}.
     */
    public static DecryptParameters createAes256CbcPadParameters(byte[] cipherText, byte[] iv) {
        return new DecryptParameters(EncryptionAlgorithm.A256CBCPAD, cipherText, iv, null, null);
    }

    /**
     * Factory method to create an instance of {@link DecryptParameters} with the given parameters for
     * {@link EncryptionAlgorithm#A256GCM}.
     *
     * @param cipherText The content to be decrypted.
     * @param iv Initialization vector for the decryption operation.
     * @param authenticationTag The tag to authenticate when performing decryption.
     *
     * @return The {@link DecryptParameters}.
     */
    public static DecryptParameters createAes256GcmParameters(byte[] cipherText, byte[] iv, byte[] authenticationTag) {
        return createAes256GcmParameters(cipherText, iv, authenticationTag, null);
    }

    /**
     * Factory method to create an instance of {@link DecryptParameters} with the given parameters for
     * {@link EncryptionAlgorithm#A256GCM}.
     *
     * @param cipherText The content to be decrypted.
     * @param iv Initialization vector for the decryption operation.
     * @param authenticationTag The tag to authenticate when performing decryption.
     * @param additionalAuthenticatedData Additional data to authenticate when using authenticated crypto algorithms.
     *
     * @return The {@link DecryptParameters}.
     */
    public static DecryptParameters createAes256GcmParameters(byte[] cipherText, byte[] iv, byte[] authenticationTag,
                                                              byte[] additionalAuthenticatedData) {
        return new DecryptParameters(EncryptionAlgorithm.A256GCM, cipherText, iv, authenticationTag,
            additionalAuthenticatedData);
    }

    /**
     * Creates an instance of {@link DecryptParameters} with the given parameters.
     *
     * @param algorithm The algorithm to be used for decryption.
     * @param cipherText The content to be decrypted.
     * @param iv Initialization vector for the decryption operation.
     * @param authenticationTag The tag to authenticate when performing decryption.
     * @param additionalAuthenticatedData Additional data to authenticate when using authenticated crypto algorithms.
     */
    DecryptParameters(EncryptionAlgorithm algorithm, byte[] cipherText, byte[] iv, byte[] authenticationTag,
                      byte[] additionalAuthenticatedData) {
        Objects.requireNonNull(algorithm, "Encryption algorithm cannot be null.");
        Objects.requireNonNull(cipherText, "Cipher text content to be decrypted cannot be null.");

        if (algorithm == EncryptionAlgorithm.A128GCM || algorithm == EncryptionAlgorithm.A192GCM
            || algorithm == EncryptionAlgorithm.A256GCM) {

            Objects.requireNonNull(authenticationTag, "Authentication tag cannot be null for GCM decryption.");
        }

        this.algorithm = algorithm;
        this.cipherText = CoreUtils.clone(cipherText);
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
        return CoreUtils.clone(cipherText);
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
