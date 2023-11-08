// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Path that needs encryption and the associated settings within {@link ClientEncryptionPolicy}.
 */
public final class ClientEncryptionIncludedPath {

    @JsonProperty("path")
    private String path;

    @JsonProperty("clientEncryptionKeyId")
    private String clientEncryptionKeyId;

    @JsonProperty("encryptionType")
    private String encryptionType;


    @JsonProperty("encryptionAlgorithm")
    private String encryptionAlgorithm;

    /**
     * Gets the path to be encrypted. Must be a top level path, eg. /salary.
     *
     * @return path
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the path to be encrypted. Must be a top level path, eg. /salary.
     *
     * @param path path to be encrypted
     * @return ClientEncryptionIncludedPath.
     */
    public ClientEncryptionIncludedPath setPath(String path) {
        this.path = path;
        return this;
    }

    /**
     * Gets the identifier of the Data Encryption Key to be used to encrypt the path.
     *
     * @return clientEncryptionKeyId
     */
    public String getClientEncryptionKeyId() {
        return clientEncryptionKeyId;
    }

    /**
     * Sets the identifier of the Data Encryption Key to be used to encrypt the path.
     *
     * @param clientEncryptionKeyId identifier of the Data Encryption Key
     * @return ClientEncryptionIncludedPath.
     */
    public ClientEncryptionIncludedPath setClientEncryptionKeyId(String clientEncryptionKeyId) {
        this.clientEncryptionKeyId = clientEncryptionKeyId;
        return this;
    }

    /**
     * Gets the type of encryption to be performed. Egs.: Deterministic Randomized.
     *
     * @return encryptionType
     */
    public String getEncryptionType() {
        return encryptionType;
    }

    /**
     * Sets the type of encryption to be performed. Egs.: Deterministic Randomized.
     *
     * @param encryptionType type of encryption
     * @return ClientEncryptionIncludedPath.
     */
    public ClientEncryptionIncludedPath setEncryptionType(String encryptionType) {
        this.encryptionType = encryptionType;
        return this;
    }

    /**
     * Gets the type of encryption algorithm to be performed. Eg - AEAD_AES_256_CBC_HMAC_SHA256.
     *
     * @return encryptionAlgorithm
     */
    public String getEncryptionAlgorithm() {
        return encryptionAlgorithm;
    }

    /**
     * Sets the type of encryption algorithm to be performed. Eg - AEAD_AES_256_CBC_HMAC_SHA256.
     *
     * @param encryptionAlgorithm type of encryption algorithm
     * @return ClientEncryptionIncludedPath.
     */
    public ClientEncryptionIncludedPath setEncryptionAlgorithm(String encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
        return this;
    }
}
