// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.EncryptionCosmosAsyncContainer;
import com.azure.cosmos.Encryptor;

public class WithEncryption {

    /**
     * Gets a Container with Encryption capabilities
     * @param container original container
     * @param encryptor encryptor to be used
     * @return container with encryption capabilities
     */
    public static EncryptionCosmosAsyncContainer withEncryptor(CosmosAsyncContainer container, Encryptor encryptor) {
        return new EncryptionCosmosAsyncContainer(container.getId(), container.getDatabase(), encryptor);
    }
}
