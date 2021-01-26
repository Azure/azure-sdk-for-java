// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Path that needs encryption and the associated settings within {@link ClientEncryptionPolicy}.
 */
public final class ClientEncryptionIncludedPath {

    /**
     * Path to be encrypted. Must be a top level path, eg. /salary
     */
    @JsonProperty("path")
    public String path;

    /**
     * Identifier of the Data Encryption Key to be used to encrypt the path.
     */
    @JsonProperty("clientEncryptionKeyId")
    public String clientEncryptionKeyId;

    /**
     * Type of encryption to be performed. Egs.: Deterministic Randomized
     */
    @JsonProperty("EncryptionType")
    public String encryptionType;


    /**
     * Type of encryption algorithm to be performed. Eg - AEAD_AES_256_CBC_HMAC_SHA256
     */
    @JsonProperty("encryptionAlgorithm")
    public String encryptionAlgorithm;
}
