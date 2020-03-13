// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.http.HttpPipeline;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceVersion;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of {@link BlobChangefeedClient
 * BlobChangefeedClients} and {@link BlobChangefeedAsyncClient BlobChangefeedAsyncClients} when {@link #buildClient() buildClient} 
 * and {@link #buildAsyncClient() buildAsyncClient} as called respectively.
 */
@ServiceClientBuilder(serviceClients = {BlobChangefeedClient.class, BlobChangefeedAsyncClient.class})
public final class BlobChangefeedClientBuilder {
    private final String accountUrl;
    private final HttpPipeline pipeline;
    private final BlobServiceVersion version;

    /**
     * Constructs the {@link BlobChangefeedClientBuilder} using the {@link BlobServiceClient#getAccountUrl() account URL} and
     * {@link BlobServiceClient#getHttpPipeline() HttpPipeline} properties of the passed {@link BlobServiceClient}.
     *
     * @param client {@link BlobServiceClient} whose properties are used to configure the builder.
     */
    public BlobChangefeedClientBuilder(BlobServiceClient client) {
        this.accountUrl = client.getAccountUrl();
        this.pipeline = client.getHttpPipeline();
        this.version = client.getServiceVersion();
    }

    /**
     * Constructs the {@link BlobChangefeedClientBuilder} using the {@link BlobServiceAsyncClient#getAccountUrl() account
     * URL} and {@link BlobServiceAsyncClient#getHttpPipeline() HttpPipeline} properties of the passed {@link
     * BlobServiceAsyncClient}.
     *
     * @param client {@link BlobServiceClient} whose properties are used to configure the builder.
     */
    public BlobChangefeedClientBuilder(BlobServiceAsyncClient client) {
        this.accountUrl = client.getAccountUrl();
        this.pipeline = client.getHttpPipeline();
        this.version = client.getServiceVersion();
    }

    /**
     * Creates a {@link BlobChangefeedClient} based on options set in the builder.
     *
     * <p><strong>Code sample</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.changefeed.BlobChangefeedClientBuilder#buildClient}
     *
     * @return a {@link BlobChangefeedClient} created from the configurations in this builder.
     */
    public BlobChangefeedClient buildClient() {
        return new BlobChangefeedClient(buildAsyncClient());
    }

    /**
     * Creates a {@link BlobChangefeedAsyncClient} based on options set in the builder.
     *
     * <p><strong>Code sample</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.changefeed.BlobChangefeedClientBuilder#buildAsyncClient}
     *
     * @return a {@link BlobChangefeedAsyncClient} created from the configurations in this builder.
     */
    public BlobChangefeedAsyncClient buildAsyncClient() {
        BlobServiceVersion serviceVersion = version != null ? version : BlobServiceVersion.getLatest();
        return new BlobChangefeedAsyncClient(accountUrl, pipeline, serviceVersion);
    }
}
