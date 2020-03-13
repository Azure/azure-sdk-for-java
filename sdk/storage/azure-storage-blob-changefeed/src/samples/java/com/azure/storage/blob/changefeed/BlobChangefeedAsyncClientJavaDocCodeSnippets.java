// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed;

import com.azure.storage.blob.BlobServiceClientBuilder;

/**
 * Code snippets for {@link BlobChangefeedAsyncClient}
 */
public class BlobChangefeedAsyncClientJavaDocCodeSnippets {
    private BlobChangefeedAsyncClient changefeedAsyncClient = new BlobChangefeedClientBuilder(
        new BlobServiceClientBuilder().buildClient()).buildAsyncClient();
}
