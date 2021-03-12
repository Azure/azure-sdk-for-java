// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.util.Beta;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Path that needs encryption and the associated settings within {@link ClientEncryptionPolicy}.
 */
public final class ClientEncryptionIncludedPath {

    @JsonProperty("path")
    private String path;

    @JsonProperty("clientEncryptionKeyId")
    private String clientEncryptionKeyId;

    @JsonProperty("EncryptionType")
    private String encryptionType;


    @JsonProperty("encryptionAlgorithm")
    private String encryptionAlgorithm;

    /**
     * Gets the path to be encrypted. Must be a top level path, eg. /salary.
     *
     * @return path
     */
    @Beta(value = Beta.SinceVersion.V4_11_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public String getPath() {
        return path;
    }

    /**
     * Sets the path to be encrypted. Must be a top level path, eg. /salary.
     *
     * @param path path to be encrypted
     */
    @Beta(value = Beta.SinceVersion.V4_11_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Gets the identifier of the Data Encryption Key to be used to encrypt the path.
     *
     * @return clientEncryptionKeyId
     */
    @Beta(value = Beta.SinceVersion.V4_11_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public String getClientEncryptionKeyId() {
        return clientEncryptionKeyId;
    }

    /**
     * Sets the identifier of the Data Encryption Key to be used to encrypt the path.
     *
     * @param clientEncryptionKeyId identifier of the Data Encryption Key
     */
    @Beta(value = Beta.SinceVersion.V4_11_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public void setClientEncryptionKeyId(String clientEncryptionKeyId) {
        this.clientEncryptionKeyId = clientEncryptionKeyId;
    }

    /**
     * Gets the type of encryption to be performed. Egs.: Deterministic Randomized.
     *
     * @return encryptionType
     */
    @Beta(value = Beta.SinceVersion.V4_11_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public String getEncryptionType() {
        return encryptionType;
    }

    /**
     * Sets the type of encryption to be performed. Egs.: Deterministic Randomized.
     *
     * @param encryptionType type of encryption
     */
    @Beta(value = Beta.SinceVersion.V4_11_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public void setEncryptionType(String encryptionType) {
        this.encryptionType = encryptionType;
    }

    /**
     * Gets the type of encryption algorithm to be performed. Eg - AEAD_AES_256_CBC_HMAC_SHA256.
     *
     * @return encryptionAlgorithm
     */
    @Beta(value = Beta.SinceVersion.V4_11_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public String getEncryptionAlgorithm() {
        return encryptionAlgorithm;
    }

    /**
     * Sets the type of encryption algorithm to be performed. Eg - AEAD_AES_256_CBC_HMAC_SHA256.
     *
     * @param encryptionAlgorithm type of encryption algorithm
     */
    @Beta(value = Beta.SinceVersion.V4_11_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public void setEncryptionAlgorithm(String encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
    }
}
