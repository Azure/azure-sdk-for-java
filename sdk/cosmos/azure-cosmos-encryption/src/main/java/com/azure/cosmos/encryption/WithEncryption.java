// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.encryption.CosmosDataEncryptionKeyProvider;
import com.azure.cosmos.implementation.encryption.CosmosEncryptor;

public class WithEncryption {

    /**
     * Gets a Container with Encryption capabilities
     *
     * @param container original container
     * @param encryptor encryptor to be used
     * @return container with encryption capabilities
     */
    public static EncryptionCosmosAsyncContainer withEncryptor(CosmosAsyncContainer container, Encryptor encryptor) {
        return new EncryptionCosmosAsyncContainer(container.getId(), container.getDatabase(), encryptor);
    }

    /**
     * Creates Cosmos Encryptor given a data encryption Key provider.
     *
     * @param dataEncryptionKeyProvider encryption key wrap provider.
     * @return Encryptor used for encryption/decryption.
     */
    public static Encryptor createCosmosEncryptor(DataEncryptionKeyProvider dataEncryptionKeyProvider) {
        return new CosmosEncryptor(dataEncryptionKeyProvider);
    }

    /**
     * Creates Cosmos Data Encryption Key Provider.
     *
     * @param encryptionKeyWrapProvider encryption key wrap provider.
     * @return DataEncryptionKeyProvider.
     */
    public static DataEncryptionKeyProvider createCosmosDataEncryptionKeyProvider(EncryptionKeyWrapProvider encryptionKeyWrapProvider) {
        return new CosmosDataEncryptionKeyProvider(encryptionKeyWrapProvider);
    }
}
