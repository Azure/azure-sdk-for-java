// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.cryptography.AsyncKeyEncryptionKey;
import com.azure.core.cryptography.AsyncKeyEncryptionKeyResolver;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.blob.specialized.BlockBlobClient;

/**
 *
 */
public class EncryptedBlobClientBuilderJavaDocCodeSnippets {
    private String connectionString = "AccountName=name;AccountKey=key;DefaultEndpointProtocol=protocol;EndpointSuffix=suffix";

    private AsyncKeyEncryptionKey key = JavaDocCodeSnippetsHelpers.getKey();
    private AsyncKeyEncryptionKeyResolver keyResolver = JavaDocCodeSnippetsHelpers.getKeyResolver();
    private KeyWrapAlgorithm keyWrapAlgorithm = KeyWrapAlgorithm.RSA_OAEP;

    private BlockBlobAsyncClient blockBlobAsyncClient = JavaDocCodeSnippetsHelpers.getBlockBlobAsyncClient(
        connectionString);
    private BlockBlobClient blockBlobClient = JavaDocCodeSnippetsHelpers.getBlockBlobClient(connectionString);


    /**
     * Code snippet for {@link EncryptedBlobClientBuilder#buildEncryptedBlockBlobAsyncClient()}
     */
    public void buildEncryptedBlockBlobAsyncClient() {
        // BEGIN: com.azure.storage.blob.specialized.cryptography.EncryptedBlobClientBuilder.buildEncryptedBlockBlobAsyncClient
        EncryptedBlockBlobAsyncClient client = new EncryptedBlobClientBuilder()
            .key(key, keyWrapAlgorithm)
            .keyResolver(keyResolver)
            .connectionString(connectionString)
            .buildEncryptedBlockBlobAsyncClient();
        // END: com.azure.storage.blob.specialized.cryptography.EncryptedBlobClientBuilder.buildEncryptedBlockBlobAsyncClient
    }

    /**
     * Code snippet for {@link EncryptedBlobClientBuilder#buildEncryptedBlockBlobClient()}
     */
    public void buildEncryptedBlockBlobClient() {
        // BEGIN: com.azure.storage.blob.specialized.cryptography.EncryptedBlobClientBuilder.buildEncryptedBlockBlobClient
        EncryptedBlockBlobClient client = new EncryptedBlobClientBuilder()
            .key(key, keyWrapAlgorithm)
            .keyResolver(keyResolver)
            .connectionString(connectionString)
            .buildEncryptedBlockBlobClient();
        // END: com.azure.storage.blob.specialized.cryptography.EncryptedBlobClientBuilder.buildEncryptedBlockBlobClient
    }

    /**
     * Code snippet for {@link EncryptedBlobClientBuilder#buildEncryptedBlockBlobAsyncClient(BlockBlobAsyncClient)}
     */
    public void buildEncryptedBlockBlobAsyncClientFromClient() {
        // BEGIN: com.azure.storage.blob.specialized.cryptography.EncryptedBlobClientBuilder.buildEncryptedBlockBlobAsyncClient#blockblobasyncclient
        EncryptedBlockBlobAsyncClient client = new EncryptedBlobClientBuilder()
            .key(key, keyWrapAlgorithm)
            .keyResolver(keyResolver)
            .connectionString(connectionString)
            .buildEncryptedBlockBlobAsyncClient(blockBlobAsyncClient);
        // END: com.azure.storage.blob.specialized.cryptography.EncryptedBlobClientBuilder.buildEncryptedBlockBlobAsyncClient#blockblobasyncclient
    }

    /**
     * Code snippet for {@link EncryptedBlobClientBuilder#buildEncryptedBlockBlobClient(BlockBlobClient)}
     */
    public void buildEncryptedBlockBlobClientFromClient() {
        // BEGIN: com.azure.storage.blob.specialized.cryptography.EncryptedBlobClientBuilder.buildEncryptedBlockBlobClient#blockblobclient
        EncryptedBlockBlobClient builder = new EncryptedBlobClientBuilder()
            .key(key, keyWrapAlgorithm)
            .keyResolver(keyResolver)
            .connectionString(connectionString)
            .buildEncryptedBlockBlobClient(blockBlobClient);
        // END: com.azure.storage.blob.specialized.cryptography.EncryptedBlobClientBuilder.buildEncryptedBlockBlobClient#blockblobclient
    }

}
