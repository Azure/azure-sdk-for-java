// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.quickquery;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.http.HttpPipeline;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.models.CpkInfo;
import com.azure.storage.blob.specialized.BlobAsyncClientBase;
import com.azure.storage.blob.specialized.BlobClientBase;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of 
 * {@link BlobQuickQueryClient BlobQuickQueryClients} and {@link BlobQuickQueryAsyncClient BlobQuickQueryAsyncClients} 
 * when {@link #buildClient() buildClient} and {@link #buildAsyncClient() buildAsyncClient} as called respectively.
 */
@ServiceClientBuilder(serviceClients = {BlobQuickQueryClient.class, BlobQuickQueryAsyncClient.class})
public class BlobQuickQueryClientBuilder {

    private final String blobUrl;
    private final HttpPipeline pipeline;
    private final BlobServiceVersion version;
    private final CpkInfo customerProvidedKey;

    /**
     * Constructs the {@link BlobQuickQueryClientBuilder} using the {@link BlobClientBase#getBlobUrl()} () blob URL} 
     * and {@link BlobClientBase#getHttpPipeline() HttpPipeline} properties of the passed {@link BlobClientBase}.
     *
     * @param client {@link BlobClientBase} whose properties are used to configure the builder.
     */
    public BlobQuickQueryClientBuilder(BlobClientBase client) {
        this.blobUrl = client.getBlobUrl();
        this.pipeline = client.getHttpPipeline();
        this.version = client.getServiceVersion();
        this.customerProvidedKey = client.getCustomerProvidedKey();
    }

    /**
     * Constructs the {@link BlobQuickQueryClientBuilder} using the {@link BlobAsyncClientBase#getBlobUrl() blob
     * URL} and {@link BlobAsyncClientBase#getHttpPipeline() HttpPipeline} properties of the passed {@link
     * BlobAsyncClientBase}.
     *
     * @param client {@link BlobClientBase} whose properties are used to configure the builder.
     */
    public BlobQuickQueryClientBuilder(BlobAsyncClientBase client) {
        this.blobUrl = client.getBlobUrl();
        this.pipeline = client.getHttpPipeline();
        this.version = client.getServiceVersion();
        this.customerProvidedKey = client.getCustomerProvidedKey();
    }

    /**
     * Creates a {@link BlobQuickQueryClient} based on options set in the builder.
     *
     * <p><strong>Code sample</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.batch.BlobQuickQueryClientBuilder#buildClient}
     *
     * @return a {@link BlobQuickQueryClient} created from the configurations in this builder.
     */
    public BlobQuickQueryClient buildClient() {
        return new BlobQuickQueryClient(buildAsyncClient());
    }

    /**
     * Creates a {@link BlobQuickQueryAsyncClient} based on options set in the builder.
     *
     * <p><strong>Code sample</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.batch.BlobQuickQueryClientBuilder#buildAsyncClient}
     *
     * @return a {@link BlobQuickQueryAsyncClient} created from the configurations in this builder.
     */
    public BlobQuickQueryAsyncClient buildAsyncClient() {
        return new BlobQuickQueryAsyncClient(blobUrl, pipeline, version, customerProvidedKey);
    }
}
