// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.http.HttpPipeline;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceVersion;

import java.net.URL;
import java.util.Objects;
import java.util.UUID;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of Storage Lease
 * clients. Lease clients are able to interact with both container and blob clients and act as a supplement client. A
 * new instance of {@link BlobLeaseClient} and {@link BlobLeaseAsyncClient} are constructed every time
 * {@link #buildClient() buildClient} and {@link #buildAsyncClient() buildAsyncClient} are called
 * respectively.
 *
 * <p>When a client is instantiated and a {@link #leaseId(String) leaseId} hasn't been set a {@link UUID} will be used
 * as the lease identifier.</p>
 *
 * <p><strong>Instantiating LeaseClients</strong></p>
 *
 * {@codesnippet com.azure.storage.blob.specialized.BlobLeaseClientBuilder.syncInstantiationWithBlobAndLeaseId}
 *
 * {@codesnippet com.azure.storage.blob.specialized.BlobLeaseClientBuilder.syncInstantiationWithContainerAndLeaseId}
 *
 * <p><strong>Instantiating LeaseAsyncClients</strong></p>
 *
 * {@codesnippet com.azure.storage.blob.specialized.BlobLeaseClientBuilder.asyncInstantiationWithBlobAndLeaseId}
 *
 * {@codesnippet com.azure.storage.blob.specialized.BlobLeaseClientBuilder.asyncInstantiationWithContainerAndLeaseId}
 *
 * @see BlobLeaseClient
 * @see BlobLeaseAsyncClient
 */
@ServiceClientBuilder(serviceClients = { BlobLeaseClient.class, BlobLeaseAsyncClient.class })
public final class BlobLeaseClientBuilder {
    private HttpPipeline pipeline;
    private String url;
    private String leaseId;
    private boolean isBlob;
    private String accountName;
    private BlobServiceVersion serviceVersion;

    /**
     * Creates a {@link BlobLeaseClient} based on the configurations set in the builder.
     *
     * @return a {@link BlobLeaseClient} based on the configurations in this builder.
     */
    public BlobLeaseClient buildClient() {
        return new BlobLeaseClient(buildAsyncClient());
    }

    /**
     * Creates a {@link BlobLeaseAsyncClient} based on the configurations set in the builder.
     *
     * @return a {@link BlobLeaseAsyncClient} based on the configurations in this builder.
     */
    public BlobLeaseAsyncClient buildAsyncClient() {
        BlobServiceVersion version = (serviceVersion == null) ? BlobServiceVersion.getLatest() : serviceVersion;
        return new BlobLeaseAsyncClient(pipeline, url, getLeaseId(), isBlob, accountName, version.getVersion());
    }

    /**
     * Configures the builder based on the passed {@link BlobClient}. This will set the {@link HttpPipeline} and
     * {@link URL} that are used to interact with the service.
     *
     * @param blobClient BlobClient used to configure the builder.
     * @return the updated BlobLeaseClientBuilder object
     * @throws NullPointerException If {@code blobClient} is {@code null}.
     */
    public BlobLeaseClientBuilder blobClient(BlobClientBase blobClient) {
        Objects.requireNonNull(blobClient);
        this.pipeline = blobClient.getHttpPipeline();
        this.url = blobClient.getBlobUrl();
        this.isBlob = true;
        this.accountName = blobClient.getAccountName();
        this.serviceVersion = blobClient.getServiceVersion();
        return this;
    }

    /**
     * Configures the builder based on the passed {@link BlobAsyncClient}. This will set the {@link HttpPipeline} and
     * {@link URL} that are used to interact with the service.
     *
     * @param blobAsyncClient BlobAsyncClient used to configure the builder.
     * @return the updated BlobLeaseClientBuilder object
     * @throws NullPointerException If {@code blobAsyncClient} is {@code null}.
     */
    public BlobLeaseClientBuilder blobAsyncClient(BlobAsyncClientBase blobAsyncClient) {
        Objects.requireNonNull(blobAsyncClient);
        this.pipeline = blobAsyncClient.getHttpPipeline();
        this.url = blobAsyncClient.getBlobUrl();
        this.isBlob = true;
        this.accountName = blobAsyncClient.getAccountName();
        this.serviceVersion = blobAsyncClient.getServiceVersion();
        return this;
    }

    /**
     * Configures the builder based on the passed {@link BlobContainerClient}. This will set the {@link HttpPipeline}
     * and {@link URL} that are used to interact with the service.
     *
     * @param blobContainerClient ContainerClient used to configure the builder.
     * @return the updated BlobLeaseClientBuilder object
     * @throws NullPointerException If {@code containerClient} is {@code null}.
     */
    public BlobLeaseClientBuilder containerClient(BlobContainerClient blobContainerClient) {
        Objects.requireNonNull(blobContainerClient);
        this.pipeline = blobContainerClient.getHttpPipeline();
        this.url = blobContainerClient.getBlobContainerUrl();
        this.isBlob = false;
        this.accountName = blobContainerClient.getAccountName();
        this.serviceVersion = blobContainerClient.getServiceVersion();
        return this;
    }

    /**
     * Configures the builder based on the passed {@link BlobContainerAsyncClient}. This will set the {@link
     * HttpPipeline} and {@link URL} that are used to interact with the service.
     *
     * @param blobContainerAsyncClient ContainerAsyncClient used to configure the builder.
     * @return the updated BlobLeaseClientBuilder object
     * @throws NullPointerException If {@code containerAsyncClient} is {@code null}.
     */
    public BlobLeaseClientBuilder containerAsyncClient(BlobContainerAsyncClient blobContainerAsyncClient) {
        Objects.requireNonNull(blobContainerAsyncClient);
        this.pipeline = blobContainerAsyncClient.getHttpPipeline();
        this.url = blobContainerAsyncClient.getBlobContainerUrl();
        this.isBlob = false;
        this.accountName = blobContainerAsyncClient.getAccountName();
        this.serviceVersion = blobContainerAsyncClient.getServiceVersion();
        return this;
    }

    /**
     * Sets the identifier for the lease.
     *
     * <p>If a lease ID isn't set then a {@link UUID} will be used.</p>
     *
     * @param leaseId Identifier for the lease.
     * @return the updated BlobLeaseClientBuilder object
     */
    public BlobLeaseClientBuilder leaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }

    private String getLeaseId() {
        return (leaseId == null) ? UUID.randomUUID().toString() : leaseId;
    }
}
