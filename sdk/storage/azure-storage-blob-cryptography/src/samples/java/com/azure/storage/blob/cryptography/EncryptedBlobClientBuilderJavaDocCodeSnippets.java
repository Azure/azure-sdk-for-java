// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.cryptography;

import com.azure.storage.blob.BlockBlobAsyncClient;
import com.azure.storage.blob.BlockBlobClient;
import java.security.NoSuchAlgorithmException;

/**
 *
 */
public class EncryptedBlobClientBuilderJavaDocCodeSnippets {
    private String connectionString = "AccountName=name;AccountKey=key;DefaultEndpointProtocol=protocol;EndpointSuffix=suffix";

    private IKey key = null;
    private IKeyResolver keyResolver = null;

    private BlockBlobAsyncClient blockBlobAsyncClient;
    private BlockBlobClient blockBlobClient;


    /**
     * Code snippet for {@link EncryptedBlobClientBuilder#buildEncryptedBlockBlobAsyncClient()}
     */
    public void buildEncryptedBlockBlobAsyncClient() {
        // BEGIN: com.azure.storage.blob.cryptography.EncryptedBlobClientBuilder.buildEncryptedBlockBlobAsyncClient
        EncryptedBlockBlobAsyncClient client = new EncryptedBlobClientBuilder()
            .keyAndKeyResolver(key, keyResolver)
            .connectionString(connectionString)
            .buildEncryptedBlockBlobAsyncClient();
        // END: com.azure.storage.blob.cryptography.EncryptedBlobClientBuilder.buildEncryptedBlockBlobAsyncClient
    }

    /**
     * Code snippet for {@link EncryptedBlobClientBuilder#buildEncryptedBlockBlobClient()}
     */
    public void buildEncryptedBlockBlobClient() {
        // BEGIN: com.azure.storage.blob.cryptography.EncryptedBlobClientBuilder.buildEncryptedBlockBlobClient
        EncryptedBlockBlobClient client = new EncryptedBlobClientBuilder()
            .keyAndKeyResolver(key, keyResolver)
            .connectionString(connectionString)
            .buildEncryptedBlockBlobClient();
        // END: com.azure.storage.blob.cryptography.EncryptedBlobClientBuilder.buildEncryptedBlockBlobClient
    }

    /**
     * Code snippet for {@link EncryptedBlobClientBuilder#buildEncryptedBlockBlobAsyncClient(BlockBlobAsyncClient)}
     */
    public void buildEncryptedBlockBlobAsyncClientFromClient() {
        // BEGIN: com.azure.storage.blob.cryptography.EncryptedBlobClientBuilder.buildEncryptedBlockBlobAsyncClient#blockblobasyncclient
        EncryptedBlockBlobAsyncClient client = new EncryptedBlobClientBuilder()
            .keyAndKeyResolver(key, keyResolver)
            .connectionString(connectionString)
            .buildEncryptedBlockBlobAsyncClient(blockBlobAsyncClient);
        // END: com.azure.storage.blob.cryptography.EncryptedBlobClientBuilder.buildEncryptedBlockBlobAsyncClient#blockblobasyncclient
    }

    /**
     * Code snippet for {@link EncryptedBlobClientBuilder#buildEncryptedBlockBlobClient(BlockBlobClient)}
     */
    public void buildEncryptedBlockBlobClientFromClient() {
        // BEGIN: com.azure.storage.blob.cryptography.EncryptedBlobClientBuilder.buildEncryptedBlockBlobClient#blockblobclient
        EncryptedBlockBlobClient builder = new EncryptedBlobClientBuilder()
            .keyAndKeyResolver(key, keyResolver)
            .connectionString(connectionString)
            .buildEncryptedBlockBlobClient(blockBlobClient);
        // END: com.azure.storage.blob.cryptography.EncryptedBlobClientBuilder.buildEncryptedBlockBlobClient#blockblobclient
    }

}
