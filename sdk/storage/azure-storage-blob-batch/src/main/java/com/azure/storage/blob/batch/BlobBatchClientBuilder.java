// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.batch;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.http.HttpPipeline;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceVersion;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of {@link BlobBatchClient
 * BlobBatchClients} and {@link BlobBatchAsyncClient BlobBatchAsyncClients} when {@link #buildClient() buildClient} and
 * {@link #buildAsyncClient() buildAsyncClient} as called respectively.
 */
@ServiceClientBuilder(serviceClients = {BlobBatchClient.class, BlobBatchAsyncClient.class})
public final class BlobBatchClientBuilder {
    private final String accountUrl;
    private final HttpPipeline pipeline;
    private BlobServiceVersion version;

    /**
     * Constructs the {@link BlobBatchClientBuilder} using the {@link BlobServiceClient#getAccountUrl() account URL} and
     * {@link BlobServiceClient#getHttpPipeline() HttpPipeline} properties of the passed {@link BlobServiceClient}.
     *
     * @param client {@link BlobServiceClient} whose properties are used to configure the builder.
     */
    public BlobBatchClientBuilder(BlobServiceClient client) {
        this.accountUrl = client.getAccountUrl();
        this.pipeline = client.getHttpPipeline();
    }

    /**
     * Constructs the {@link BlobBatchClientBuilder} using the {@link BlobServiceAsyncClient#getAccountUrl() account
     * URL} and {@link BlobServiceAsyncClient#getHttpPipeline() HttpPipeline} properties of the passed {@link
     * BlobServiceAsyncClient}.
     *
     * @param client {@link BlobServiceClient} whose properties are used to configure the builder.
     */
    public BlobBatchClientBuilder(BlobServiceAsyncClient client) {
        this.accountUrl = client.getAccountUrl();
        this.pipeline = client.getHttpPipeline();
    }

    /**
     * Creates a {@link BlobBatchClient} based on options set in the builder.
     *
     * <p><strong>Code sample</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.batch.BlobBatchClientBuilder#buildClient}
     *
     * @return a {@link BlobBatchClient} created from the configurations in this builder.
     */
    public BlobBatchClient buildClient() {
        return new BlobBatchClient(buildAsyncClient());
    }

    /**
     * Creates a {@link BlobBatchAsyncClient} based on options set in the builder.
     *
     * <p><strong>Code sample</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.batch.BlobBatchClientBuilder#buildAsyncClient}
     *
     * @return a {@link BlobBatchAsyncClient} created from the configurations in this builder.
     */
    public BlobBatchAsyncClient buildAsyncClient() {
        BlobServiceVersion serviceVersion = version != null ? version : BlobServiceVersion.getLatest();
        return new BlobBatchAsyncClient(accountUrl, pipeline, serviceVersion);
    }

    /**
     * Sets the {@link BlobServiceVersion} that is used when making API requests.
     * <p>
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version the client library will have the result of potentially moving to a newer service version.
     *
     * @param version {@link BlobServiceVersion} of the service to be used when making requests.
     * @return the updated BlobClientBuilder object
     */
    public BlobBatchClientBuilder version(BlobServiceVersion version) {
        this.version = version;
        return this;
    }
}
