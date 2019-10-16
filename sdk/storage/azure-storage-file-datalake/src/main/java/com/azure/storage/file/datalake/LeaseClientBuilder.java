// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.http.HttpPipeline;

import java.net.URL;
import java.util.Objects;
import java.util.UUID;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of Storage Lease
 * clients. Lease clients are able to interact with both container and blob clients and act as a supplement client. A
 * new instance of {@link LeaseClient} and {@link LeaseAsyncClient} are constructed every time
 * {@link #buildClient() buildClient} and {@link #buildAsyncClient() buildAsyncClient} are called
 * respectively.
 *
 * <p>When a client is instantiated and a {@link #leaseId(String) leaseId} hasn't been set a {@link UUID} will be used
 * as the lease identifier.</p>
 *
 * <p><strong>Instantiating LeaseClients</strong></p>
 *
 * {@codesnippet com.azure.storage.blob.specialized.LeaseClientBuilder.syncInstantiationWithBlobAndLeaseId}
 *
 * {@codesnippet com.azure.storage.blob.specialized.LeaseClientBuilder.syncInstantiationWithContainerAndLeaseId}
 *
 * <p><strong>Instantiating LeaseAsyncClients</strong></p>
 *
 * {@codesnippet com.azure.storage.blob.specialized.LeaseClientBuilder.asyncInstantiationWithBlobAndLeaseId}
 *
 * {@codesnippet com.azure.storage.blob.specialized.LeaseClientBuilder.asyncInstantiationWithContainerAndLeaseId}
 *
 * @see LeaseClient
 * @see LeaseAsyncClient
 */
@ServiceClientBuilder(serviceClients = { LeaseClient.class, LeaseAsyncClient.class })
public final class LeaseClientBuilder {

    com.azure.storage.blob.specialized.LeaseClientBuilder blobLeaseClientBuilder;

    public LeaseClientBuilder() {
        blobLeaseClientBuilder = new com.azure.storage.blob.specialized.LeaseClientBuilder();
    }

    /**
     * Creates a {@link LeaseClient} based on the configurations set in the builder.
     *
     * @return a {@link LeaseClient} based on the configurations in this builder.
     */
    public LeaseClient buildClient() {
        return new LeaseClient(blobLeaseClientBuilder.buildClient());
    }

    /**
     * Creates a {@link LeaseAsyncClient} based on the configurations set in the builder.
     *
     * @return a {@link LeaseAsyncClient} based on the configurations in this builder.
     */
    public LeaseAsyncClient buildAsyncClient() {
    return new LeaseAsyncClient(blobLeaseClientBuilder.buildAsyncClient());
    }

    /**
     * Configures the builder based on the passed {@link PathClient}. This will set the {@link HttpPipeline} and
     * {@link URL} that are used to interact with the service.
     *
     * @param pathClient Client used to configure the builder.
     * @return the updated LeaseClientBuilder object
     * @throws NullPointerException If {@code pathClient} is {@code null}.
     */
    public LeaseClientBuilder pathClient(PathClient pathClient) {
//        blobLeaseClientBuilder.blobClient(pathClient.getBlobClient());
        return this;
    }

    /**
     * Configures the builder based on the passed {@link PathAsyncClient}. This will set the {@link HttpPipeline} and
     * {@link URL} that are used to interact with the service.
     *
     * @param pathAsyncClient PathAsyncClient used to configure the builder.
     * @return the updated LeaseClientBuilder object
     * @throws NullPointerException If {@code pathAsyncClient} is {@code null}.
     */
    public LeaseClientBuilder pathAsyncClient(PathAsyncClient pathAsyncClient) {
//        blobLeaseClientBuilder.blobAsyncClient(pathAsyncClient.getBlobAsyncClient());
        return this;
    }

    /**
     * Configures the builder based on the passed {@link FileSystemClient}. This will set the {@link HttpPipeline}
     * and {@link URL} that are used to interact with the service.
     *
     * @param fileSystemClient FileSystemClient used to configure the builder.
     * @return the updated LeaseClientBuilder object
     * @throws NullPointerException If {@code fileSystemClient} is {@code null}.
     */
    public LeaseClientBuilder fileSystemClient(FileSystemClient fileSystemClient) {
        blobLeaseClientBuilder.containerClient(fileSystemClient.getBlobContainerClient());
        return this;
    }

    /**
     * Configures the builder based on the passed {@link FileSystemAsyncClient}. This will set the {@link
     * HttpPipeline} and {@link URL} that are used to interact with the service.
     *
     * @param fileSystemAsyncClient FileSystemAsyncClient used to configure the builder.
     * @return the updated LeaseClientBuilder object
     * @throws NullPointerException If {@code fileSystemAsyncClient} is {@code null}.
     */
    public LeaseClientBuilder fileSystemAsyncClient(FileSystemAsyncClient fileSystemAsyncClient) {
        blobLeaseClientBuilder.containerAsyncClient(fileSystemAsyncClient.getBlobContainerAsyncClient());
        return this;
    }

    /**
     * Sets the identifier for the lease.
     *
     * <p>If a lease ID isn't set then a {@link UUID} will be used.</p>
     *
     * @param leaseId Identifier for the lease.
     * @return the updated LeaseClientBuilder object
     */
    public LeaseClientBuilder leaseId(String leaseId) {
        blobLeaseClientBuilder.leaseId(leaseId);
        return this;
    }
}
