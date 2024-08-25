// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the envelope key details stored on the service.
 */
final class WrappedKey {

    /**
     * The key identifier string.
     */
    @JsonProperty(value = "KeyId", required = true)
    private String keyId;

    /**
     * The encrypted content encryption key.
     */
    @JsonProperty(value = "EncryptedKey", required = true)
    private byte[] encryptedKey;

    /**
     * The algorithm used for wrapping.
     */
    @JsonProperty(value = "Algorithm", required = true)
    private String algorithm;

    /**
     * Initializes a new instance of the {@link WrappedKey} class.
     */
    WrappedKey() {
    }

    /**
     * Initializes a new instance of the {@link WrappedKey} class using the specified key id, encrypted key and
     * the algorithm.
     *
     * @param keyId The key identifier string.
     * @param encryptedKey The encrypted content encryption key.
     * @param algorithm The algorithm used for wrapping.
     */
    WrappedKey(String keyId, byte[] encryptedKey, String algorithm) {
        this.keyId = keyId;
        this.encryptedKey = encryptedKey;
        this.algorithm = algorithm;
    }

    /**
     * Gets the key identifier. This identifier is used to identify the key that is used to wrap/unwrap the content
     * encryption key.
     *
     * @return The key identifier string.
     */
    String getKeyId() {
        return keyId;
    }

    /**
     * Gets the encrypted content encryption key.
     *
     * @return The encrypted content encryption key.
     */
    byte[] getEncryptedKey() {
        return encryptedKey;
    }

    /**
     * Gets the algorithm used for wrapping.
     *
     * @return The algorithm used for wrapping.
     */
    String getAlgorithm() {
        return algorithm;
    }

    /**
     * Sets the key identifier. This identifier is used to identify the key that is used to wrap/unwrap the content
     * encryption key.
     *
     * @param keyId The key identifier string.
     *
     * @return this
     */
    WrappedKey setKeyId(String keyId) {
        this.keyId = keyId;
        return this;
    }

    /**
     * Sets the encrypted content encryption key.
     *
     * @param encryptedKey The encrypted content encryption key.
     *
     * @return this
     */
    WrappedKey setEncryptedKey(byte[] encryptedKey) {
        this.encryptedKey = encryptedKey;
        return this;
    }

    /**
     * Sets the algorithm used for wrapping.
     *
     * @param algorithm The algorithm used for wrapping.
     *
     * @return this
     */
    WrappedKey setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
        return this;
    }
}
