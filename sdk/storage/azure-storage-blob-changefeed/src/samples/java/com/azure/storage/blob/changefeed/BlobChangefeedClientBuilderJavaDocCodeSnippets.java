// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed;

import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;

/**
 * Code snippets for {@link BlobChangefeedClientBuilder}
 */
public class BlobChangefeedClientBuilderJavaDocCodeSnippets {
    private String endpoint = "endpoint";
    private BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().endpoint(endpoint).buildClient();
    private BlobServiceAsyncClient blobServiceAsyncClient = new BlobServiceClientBuilder().endpoint(endpoint)
        .buildAsyncClient();

    /**
     * Code snippet for {@link BlobChangefeedClientBuilder#buildClient()}
     */
    public void constructClient() {
        // BEGIN: com.azure.storage.blob.changefeed.BlobChangefeedClientBuilder#buildClient
        BlobChangefeedClient changefeedClient = new BlobChangefeedClientBuilder(blobServiceClient).buildClient();
        // END: com.azure.storage.blob.changefeed.BlobChangefeedClientBuilder#buildClient
    }

    /**
     * Code snippet for {@link BlobChangefeedClientBuilder#buildAsyncClient()}
     */
    public void constructAsyncClient() {
        // BEGIN: com.azure.storage.blob.changefeed.BlobChangefeedClientBuilder#buildAsyncClient
        BlobChangefeedAsyncClient changefeedClient = new BlobChangefeedClientBuilder(blobServiceAsyncClient).buildAsyncClient();
        // END: com.azure.storage.blob.changefeed.BlobChangefeedClientBuilder#buildAsyncClient
    }
}
