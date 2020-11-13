// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.util.CoreUtils;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;

import java.util.Objects;

/**
 * A class containing various configuration parameters that can be applied when performing decryption operations.
 */
public class DecryptOptions {
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
     * Factory method to create an instance of {@link DecryptOptions} with the given parameters for
     * {@link EncryptionAlgorithm#A128CBC}.
     *
     * @param ciphertext The content to be decrypted.
     * @param iv Initialization vector for the decryption operation.
     * @return The {@link DecryptOptions}.
     */
    public static DecryptOptions createAes128CbcOptions(byte[] ciphertext, byte[] iv) {
        return new DecryptOptions(EncryptionAlgorithm.A128CBC, ciphertext, iv, null, null);
    }
    /**
     * Factory method to create an instance of {@link DecryptOptions} with the given parameters for
     * {@link EncryptionAlgorithm#A128CBCPAD}.
     *
     * @param ciphertext The content to be decrypted.
     * @param iv Initialization vector for the decryption operation.
     * @return The {@link DecryptOptions}.
     */
    public static DecryptOptions createAes128CbcPadOptions(byte[] ciphertext, byte[] iv) {
        return new DecryptOptions(EncryptionAlgorithm.A128CBCPAD, ciphertext, iv, null, null);
    }

    /**
     * Factory method to create an instance of {@link DecryptOptions} with the given parameters for
     * {@link EncryptionAlgorithm#A128GCM}.
     *
     * @param ciphertext The content to be decrypted.
     * @param iv Initialization vector for the decryption operation.
     * @param authenticationTag The tag to authenticate when performing decryption.
     * @return The {@link DecryptOptions}.
     */
    public static DecryptOptions createAes128GcmOptions(byte[] ciphertext, byte[] iv, byte[] authenticationTag) {
        return createAes128GcmOptions(ciphertext, iv, authenticationTag, null);
    }

    /**
     * Factory method to create an instance of {@link DecryptOptions} with the given parameters for
     * {@link EncryptionAlgorithm#A128GCM}.
     *
     * @param ciphertext The content to be decrypted.
     * @param iv Initialization vector for the decryption operation.
     * @param authenticationTag The tag to authenticate when performing decryption.
     * @param additionalAuthenticatedData Additional data to authenticate when using authenticated crypto algorithms.
     * @return The {@link DecryptOptions}.
     */
    public static DecryptOptions createAes128GcmOptions(byte[] ciphertext, byte[] iv, byte[] authenticationTag,
                                                        byte[] additionalAuthenticatedData) {
        return new DecryptOptions(EncryptionAlgorithm.A128GCM, ciphertext, iv, authenticationTag,
            additionalAuthenticatedData);
    }

    /**
     * Factory method to create an instance of {@link DecryptOptions} with the given parameters for
     * {@link EncryptionAlgorithm#A192CBC}.
     *
     * @param ciphertext The content to be decrypted.
     * @param iv Initialization vector for the decryption operation.
     * @return The {@link DecryptOptions}.
     */
    public static DecryptOptions createAes192CbcOptions(byte[] ciphertext, byte[] iv) {
        return new DecryptOptions(EncryptionAlgorithm.A192CBC, ciphertext, iv, null, null);
    }

    /**
     * Factory method to create an instance of {@link DecryptOptions} with the given parameters for
     * {@link EncryptionAlgorithm#A192CBCPAD}.
     *
     * @param ciphertext The content to be decrypted.
     * @param iv Initialization vector for the decryption operation.
     * @return The {@link DecryptOptions}.
     */
    public static DecryptOptions createAes192CbcPadOptions(byte[] ciphertext, byte[] iv) {
        return new DecryptOptions(EncryptionAlgorithm.A192CBCPAD, ciphertext, iv, null, null);
    }

    /**
     * Factory method to create an instance of {@link DecryptOptions} with the given parameters for
     * {@link EncryptionAlgorithm#A192GCM}.
     *
     * @param ciphertext The content to be decrypted.
     * @param iv Initialization vector for the decryption operation.
     * @param authenticationTag The tag to authenticate when performing decryption.
     * @return The {@link DecryptOptions}.
     */
    public static DecryptOptions createAes192GcmOptions(byte[] ciphertext, byte[] iv, byte[] authenticationTag) {
        return createAes192GcmOptions(ciphertext, iv, authenticationTag, null);
    }

    /**
     * Factory method to create an instance of {@link DecryptOptions} with the given parameters for
     * {@link EncryptionAlgorithm#A192GCM}.
     *
     * @param ciphertext The content to be decrypted.
     * @param iv Initialization vector for the decryption operation.
     * @param authenticationTag The tag to authenticate when performing decryption.
     * @param additionalAuthenticatedData Additional data to authenticate when using authenticated crypto algorithms.
     * @return The {@link DecryptOptions}.
     */
    public static DecryptOptions createAes192GcmOptions(byte[] ciphertext, byte[] iv, byte[] authenticationTag,
                                                        byte[] additionalAuthenticatedData) {
        return new DecryptOptions(EncryptionAlgorithm.A192GCM, ciphertext, iv, authenticationTag,
            additionalAuthenticatedData);
    }
    /**
     * Factory method to create an instance of {@link DecryptOptions} with the given parameters for
     * {@link EncryptionAlgorithm#A256CBC}.
     *
     * @param ciphertext The content to be decrypted.
     * @param iv Initialization vector for the decryption operation.
     * @return The {@link DecryptOptions}.
     */
    public static DecryptOptions createAes256CbcOptions(byte[] ciphertext, byte[] iv) {
        return new DecryptOptions(EncryptionAlgorithm.A256CBC, ciphertext, iv, null, null);
    }
    /**
     * Factory method to create an instance of {@link DecryptOptions} with the given parameters for
     * {@link EncryptionAlgorithm#A256CBCPAD}.
     *
     * @param ciphertext The content to be decrypted.
     * @param iv Initialization vector for the decryption operation.
     * @return The {@link DecryptOptions}.
     */
    public static DecryptOptions createAes256CbcPadOptions(byte[] ciphertext, byte[] iv) {
        return new DecryptOptions(EncryptionAlgorithm.A256CBCPAD, ciphertext, iv, null, null);
    }

    /**
     * Factory method to create an instance of {@link DecryptOptions} with the given parameters for
     * {@link EncryptionAlgorithm#A256GCM}.
     *
     * @param ciphertext The content to be decrypted.
     * @param iv Initialization vector for the decryption operation.
     * @param authenticationTag The tag to authenticate when performing decryption.
     * @return The {@link DecryptOptions}.
     */
    public static DecryptOptions createAes256GcmOptions(byte[] ciphertext, byte[] iv, byte[] authenticationTag) {
        return createAes256GcmOptions(ciphertext, iv, authenticationTag, null);
    }

    /**
     * Factory method to create an instance of {@link DecryptOptions} with the given parameters for
     * {@link EncryptionAlgorithm#A256GCM}.
     *
     * @param ciphertext The content to be decrypted.
     * @param iv Initialization vector for the decryption operation.
     * @param authenticationTag The tag to authenticate when performing decryption.
     * @param additionalAuthenticatedData Additional data to authenticate when using authenticated crypto algorithms.
     * @return The {@link DecryptOptions}.
     */
    public static DecryptOptions createAes256GcmOptions(byte[] ciphertext, byte[] iv, byte[] authenticationTag,
                                                        byte[] additionalAuthenticatedData) {
        return new DecryptOptions(EncryptionAlgorithm.A256GCM, ciphertext, iv, authenticationTag,
            additionalAuthenticatedData);
    }

    /**
     * Creates an instance of {@link DecryptOptions} with the given parameters.
     *
     * @param algorithm The algorithm to be used for decryption.
     * @param ciphertext The content to be decrypted.
     * @param iv Initialization vector for the decryption operation.
     * @param authenticationTag The tag to authenticate when performing decryption.
     * @param additionalAuthenticatedData Additional data to authenticate when using authenticated crypto algorithms.
     */
    DecryptOptions(EncryptionAlgorithm algorithm, byte[] ciphertext, byte[] iv, byte[] authenticationTag,
                   byte[] additionalAuthenticatedData) {
        Objects.requireNonNull(algorithm, "Encryption algorithm cannot be null.");
        Objects.requireNonNull(ciphertext, "Cipher text content to be decrypted cannot be null.");

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
    public byte[] getCiphertext() {
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
