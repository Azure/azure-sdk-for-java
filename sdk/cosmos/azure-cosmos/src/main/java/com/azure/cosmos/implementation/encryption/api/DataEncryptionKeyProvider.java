// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption.api;

public interface DataEncryptionKeyProvider {

    /**
     * Loads the data encryption key for the given id.
     * @param id Identifier of the data encryption key.
     * @return Data encryption key bytes
     */
    DataEncryptionKey loadDataEncryptionKey(
        String id,
        CosmosEncryptionAlgorithm algorithm);
}
