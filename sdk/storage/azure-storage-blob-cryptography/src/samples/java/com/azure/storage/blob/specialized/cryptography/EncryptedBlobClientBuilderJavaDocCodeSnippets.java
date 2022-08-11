// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.cryptography.AsyncKeyEncryptionKey;
import com.azure.core.cryptography.AsyncKeyEncryptionKeyResolver;

/**
 *
 */
public class EncryptedBlobClientBuilderJavaDocCodeSnippets {
    private String connectionString = "AccountName=name;AccountKey=key;DefaultEndpointProtocol=protocol;EndpointSuffix=suffix";

    private AsyncKeyEncryptionKey key = JavaDocCodeSnippetsHelpers.getKey();
    private AsyncKeyEncryptionKeyResolver keyResolver = JavaDocCodeSnippetsHelpers.getKeyResolver();
    private String keyWrapAlgorithm = "keyWrapAlgorithm";

    /**
     * Code snippet for {@link EncryptedBlobClientBuilder#buildEncryptedBlobAsyncClient()}
     */
    public void buildEncryptedBlockBlobAsyncClient() {
        // BEGIN: com.azure.storage.blob.specialized.cryptography.EncryptedBlobClientBuilder.buildEncryptedBlobAsyncClient
        EncryptedBlobAsyncClient client = new EncryptedBlobClientBuilder()
            .key(key, keyWrapAlgorithm)
            .keyResolver(keyResolver)
            .connectionString(connectionString)
            .buildEncryptedBlobAsyncClient();
        // END: com.azure.storage.blob.specialized.cryptography.EncryptedBlobClientBuilder.buildEncryptedBlobAsyncClient
    }

    /**
     * Code snippet for {@link EncryptedBlobClientBuilder#buildEncryptedBlobClient()}
     */
    public void buildEncryptedBlockBlobClient() {
        // BEGIN: com.azure.storage.blob.specialized.cryptography.EncryptedBlobClientBuilder.buildEncryptedBlobClient
        EncryptedBlobClient client = new EncryptedBlobClientBuilder()
            .key(key, keyWrapAlgorithm)
            .keyResolver(keyResolver)
            .connectionString(connectionString)
            .buildEncryptedBlobClient();
        // END: com.azure.storage.blob.specialized.cryptography.EncryptedBlobClientBuilder.buildEncryptedBlobClient
    }

}
