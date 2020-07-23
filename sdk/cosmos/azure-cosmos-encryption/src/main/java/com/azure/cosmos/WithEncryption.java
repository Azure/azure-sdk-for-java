// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.encryption.Encryptor;

public class WithEncryption {

    public static EncryptionCosmosAsyncContainer withEncryptor(CosmosAsyncContainer container, Encryptor encryptor) {
        return new EncryptionCosmosAsyncContainer(container.getId(), container.getDatabase(), encryptor);
    }
}
