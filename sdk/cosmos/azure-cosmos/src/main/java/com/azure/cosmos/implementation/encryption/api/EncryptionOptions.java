// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption.api;

import java.util.List;

public class EncryptionOptions {

    public CosmosEncryptionAlgorithm getEncryptionAlgorithm() {
        return encryptionAlgorithm;
    }

    public EncryptionOptions setEncryptionAlgorithm(CosmosEncryptionAlgorithm encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
        return this;
    }

    public String getDataEncryptionKeyId() {
        return dataEncryptionKeyId;
    }

    public EncryptionOptions setDataEncryptionKeyId(String dataEncryptionKeyId) {
        this.dataEncryptionKeyId = dataEncryptionKeyId;
        return this;
    }

    public List<String> getPathsToEncrypt() {
        return pathsToEncrypt;
    }

    public EncryptionOptions setPathsToEncrypt(List<String> pathsToEncrypt) {
        this.pathsToEncrypt = pathsToEncrypt;
        return this;
    }

    /**
     * Identifier of the data encryption key to be used for encrypting the data in the request payload.
     * The data encryption key must be suitable for use with the {@link com.azure.cosmos.implementation.encryption.api.DataEncryptionKey} provided.
     * <p>
     * The {@link DataEncryptionKeyProvider} configured on the client is used to retrieve the actual data encryption key.
     */
    private String dataEncryptionKeyId;

    /**
     * For the request payload, list of JSON paths to encrypt.
     * Only top level paths are supported.
     * Example of a path specification: /sensitive
     */
    private List<String> pathsToEncrypt;

    /**
     * Algorithm to be used for encrypting the data in the request payload.
     */
    private CosmosEncryptionAlgorithm encryptionAlgorithm;
}
