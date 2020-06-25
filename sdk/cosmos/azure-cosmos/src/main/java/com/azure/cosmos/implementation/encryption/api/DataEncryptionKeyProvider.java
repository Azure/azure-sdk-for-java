// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption.api;

/**
 * Abstraction for a provider to get data encryption keys for use in client-side encryption.
 * See https://aka.ms/CosmosClientEncryption for more information on client-side encryption support in Azure Cosmos DB.
 */
public interface DataEncryptionKeyProvider {

    /**
     * Retrieves the data encryption key for the given id.
     * @param id Identifier of the data encryption key.
     * @param encryptionAlgorithm Encryption algorithm that the retrieved key will be used with.
     * @return Data encryption key bytes.
     * TODO: @moderakh look into if this method needs to be async.
     */
    DataEncryptionKey getDataEncryptionKey(
        String id,
        String encryptionAlgorithm);
}
