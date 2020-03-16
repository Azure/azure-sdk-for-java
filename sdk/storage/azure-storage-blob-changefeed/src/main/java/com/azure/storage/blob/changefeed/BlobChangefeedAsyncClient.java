// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.BlobServiceVersion;

/**
 * This class provides a client that contains all operations that apply to Azure Storage Blob changefeed.
 *
 * @see BlobChangefeedClientBuilder
 */
@ServiceClient(builder = BlobChangefeedClientBuilder.class, isAsync = true)
public class BlobChangefeedAsyncClient {
    private final ClientLogger logger = new ClientLogger(BlobChangefeedAsyncClient.class);

    private static final String CHANGEFEED_CONTAINER_NAME = "$blobchangefeed";

    private final BlobContainerAsyncClient client;

    BlobChangefeedAsyncClient(String accountUrl, HttpPipeline pipeline, BlobServiceVersion version) {
        this.client = new BlobContainerClientBuilder()
            .endpoint(accountUrl)
            .containerName(CHANGEFEED_CONTAINER_NAME)
            .pipeline(pipeline)
            .serviceVersion(version)
            .buildAsyncClient();
    }

    /**
     * This is a temporary method to pass CI for now because this.client was not used anywhere. Will remove in next PR.
     */
    public void tempMethod() {
        this.client.create();
    }
}
