// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.quickquery;

import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.quickquery.BlobQuickQueryAsyncClient;
import com.azure.storage.quickquery.BlobQuickQueryClient;
import com.azure.storage.quickquery.BlobQuickQueryClientBuilder;

/**
 * Code snippets for {@link BlobQuickQueryClientBuilder}
 */
@SuppressWarnings({"unused"})
public class BlobQuickQueryClientBuilderJavaDocCodeSnippets {
    private BlobClient blobClient = new BlobClientBuilder().buildClient();
    private BlobAsyncClient blobAsyncClient = new BlobClientBuilder().buildAsyncClient();

    /**
     * Code snippet for {@link BlobQuickQueryClientBuilder#buildClient()}
     */
    public void constructClient() {
        // BEGIN: com.azure.storage.quickquery.BlobQuickQueryClientBuilder#buildClient
        BlobQuickQueryClient batchClient = new BlobQuickQueryClientBuilder(blobClient).buildClient();
        // END: com.azure.storage.quickquery.BlobQuickQueryClientBuilder#buildClient
    }

    /**
     * Code snippet for {@link BlobQuickQueryClientBuilder#buildAsyncClient()}
     */
    public void constructAsyncClient() {
        // BEGIN: com.azure.storage.quickquery.BlobQuickQueryClientBuilder#buildAsyncClient
        BlobQuickQueryAsyncClient batchClient = new BlobQuickQueryClientBuilder(blobAsyncClient).buildAsyncClient();
        // END: com.azure.storage.quickquery.BlobQuickQueryClientBuilder#buildAsyncClient
    }


}
