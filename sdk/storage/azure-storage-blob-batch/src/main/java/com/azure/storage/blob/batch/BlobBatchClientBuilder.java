// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.batch;

import com.azure.core.http.HttpPipeline;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClient;

/**
 *
 */
public final class BlobBatchClientBuilder {
    private final ClientLogger logger = new ClientLogger(BlobBatchClientBuilder.class);

    private String accountUrl;
    private HttpPipeline pipeline;

    /**
     *
     * @param client
     */
    public BlobBatchClientBuilder(BlobServiceClient client) {
        this.accountUrl = client.getAccountUrl();
        this.pipeline = client.getHttpPipeline();
    }

    /**
     *
     * @param client
     */
    public BlobBatchClientBuilder(BlobServiceAsyncClient client) {
        this.accountUrl = client.getAccountUrl();
        this.pipeline = client.getHttpPipeline();
    }

    /**
     *
     * @return
     */
    public BlobBatchClient buildClient() {
        return new BlobBatchClient(buildAsyncClient());
    }

    /**
     *
     * @return
     */
    public BlobBatchAsyncClient buildAsyncClient() {
        return new BlobBatchAsyncClient(accountUrl, pipeline);
    }
}
