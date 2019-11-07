// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.batch;

import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;

/**
 * Code snippets for {@link BlobBatchClientBuilder}
 */
public class BlobBatchClientBuilderJavaDocCodeSnippets {
    private BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().buildClient();
    private BlobServiceAsyncClient blobServiceAsyncClient = new BlobServiceClientBuilder().buildAsyncClient();

    /**
     * Code snippet for {@link BlobBatchClientBuilder#buildClient()}
     */
    public void constructClient() {
        // BEGIN: com.azure.storage.blob.batch.BlobBatchClientBuilder#buildClient
        BlobBatchClient batchClient = new BlobBatchClientBuilder(blobServiceClient).buildClient();
        // END: com.azure.storage.blob.batch.BlobBatchClientBuilder#buildClient
    }

    /**
     * Code snippet for {@link BlobBatchClientBuilder#buildAsyncClient()}
     */
    public void constructAsyncClient() {
        // BEGIN: com.azure.storage.blob.batch.BlobBatchClientBuilder#buildAsyncClient
        BlobBatchAsyncClient batchClient = new BlobBatchClientBuilder(blobServiceAsyncClient).buildAsyncClient();
        // END: com.azure.storage.blob.batch.BlobBatchClientBuilder#buildAsyncClient
    }
}
