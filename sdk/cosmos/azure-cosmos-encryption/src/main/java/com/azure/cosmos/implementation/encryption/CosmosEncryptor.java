// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import com.azure.cosmos.encryption.DataEncryptionKey;
import com.azure.cosmos.encryption.DataEncryptionKeyProvider;
import com.azure.cosmos.encryption.Encryptor;
import com.azure.cosmos.implementation.guava25.base.Preconditions;
import reactor.core.publisher.Mono;

/**
 * Provides the default implementation for client-side encryption for Cosmos DB.
 * See https://aka.ms/CosmosClientEncryption for more information on client-side encryption support in Azure Cosmos DB.
 */
public class CosmosEncryptor implements Encryptor {
    private DataEncryptionKeyProvider dataEncryptionKeyProvider;

    /**
     * Gets Container for data encryption keys.
     * @return DataEncryptionKeyProvider
     */
    public DataEncryptionKeyProvider getDataEncryptionKeyProvider() {
        return dataEncryptionKeyProvider;
    }

    /**
     * Initializes a new instance of Cosmos Encryptor.
     * @param dataEncryptionKeyProvider
     */
    public CosmosEncryptor(DataEncryptionKeyProvider dataEncryptionKeyProvider) {
        this.dataEncryptionKeyProvider = dataEncryptionKeyProvider;
    }

    @Override
    public Mono<byte[]> decryptAsync(
        byte[] cipherText,
        String dataEncryptionKeyId,
        String encryptionAlgorithm) {

        Mono<DataEncryptionKey> dekMono =  this.dataEncryptionKeyProvider.getDataEncryptionKey(
            dataEncryptionKeyId,
            encryptionAlgorithm);

        return dekMono.switchIfEmpty(Mono.error(new NullPointerException("DataEncryptionKey returned from DataEncryptionKeyProvider.getDataEncryptionKey is null")))
            .flatMap(
                dek -> Mono.just(dek.decryptData(cipherText))
            );
    }

    @Override
    public Mono<byte[]> encryptAsync(
        byte[] plainText,
        String dataEncryptionKeyId,
        String encryptionAlgorithm) {

        Mono<DataEncryptionKey> dekMono =  this.dataEncryptionKeyProvider.getDataEncryptionKey(
            dataEncryptionKeyId,
            encryptionAlgorithm);

        return dekMono.switchIfEmpty(Mono.error(new NullPointerException("DataEncryptionKey returned from DataEncryptionKeyProvider.getDataEncryptionKey is null")))
                      .flatMap(
                          dek -> Mono.just(dek.encryptData(plainText))
                      );
    }
}
