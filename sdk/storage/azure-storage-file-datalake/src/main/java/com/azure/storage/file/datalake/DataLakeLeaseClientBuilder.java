// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.http.HttpPipeline;
import com.azure.storage.blob.specialized.BlobLeaseClientBuilder;

import java.net.URL;
import java.util.UUID;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of Storage Lease
 * clients. Lease clients are able to interact with both container and blob clients and act as a supplement client. A
 * new instance of {@link DataLakeLeaseClient} and {@link DataLakeLeaseAsyncClient} are constructed every time
 * {@link #buildClient() buildClient} and {@link #buildAsyncClient() buildAsyncClient} are called
 * respectively.
 *
 * <p>When a client is instantiated and a {@link #leaseId(String) leaseId} hasn't been set a {@link UUID} will be used
 * as the lease identifier.</p>
 *
 * <p><strong>Instantiating LeaseClients</strong></p>
 *
 * {@codesnippet com.azure.storage.file.datalake.DataLakeLeaseClientBuilder.syncInstantiationWithFileAndLeaseId}
 *
 * {@codesnippet com.azure.storage.file.datalake.DataLakeLeaseClientBuilder.syncInstantiationWithDirectoryAndLeaseId}
 *
 * {@codesnippet com.azure.storage.file.datalake.DataLakeLeaseClientBuilder.syncInstantiationWithFileSystemAndLeaseId}
 *
 * <p><strong>Instantiating LeaseAsyncClients</strong></p>
 *
 * {@codesnippet com.azure.storage.file.datalake.DataLakeLeaseClientBuilder.asyncInstantiationWithFileAndLeaseId}
 *
 * {@codesnippet com.azure.storage.file.datalake.DataLakeLeaseClientBuilder.asyncInstantiationWithDirectoryAndLeaseId}
 *
 * {@codesnippet com.azure.storage.file.datalake.DataLakeLeaseClientBuilder.asyncInstantiationWithFileSystemAndLeaseId}
 *
 * @see DataLakeLeaseClient
 * @see DataLakeLeaseAsyncClient
 */
@ServiceClientBuilder(serviceClients = { DataLakeLeaseClient.class, DataLakeLeaseAsyncClient.class })
public final class DataLakeLeaseClientBuilder {

    BlobLeaseClientBuilder blobLeaseClientBuilder;

    public DataLakeLeaseClientBuilder() {
        blobLeaseClientBuilder = new BlobLeaseClientBuilder();
    }

    /**
     * Creates a {@link DataLakeLeaseClient} based on the configurations set in the builder.
     *
     * @return a {@link DataLakeLeaseClient} based on the configurations in this builder.
     */
    public DataLakeLeaseClient buildClient() {
        return new DataLakeLeaseClient(blobLeaseClientBuilder.buildClient());
    }

    /**
     * Creates a {@link DataLakeLeaseAsyncClient} based on the configurations set in the builder.
     *
     * @return a {@link DataLakeLeaseAsyncClient} based on the configurations in this builder.
     */
    public DataLakeLeaseAsyncClient buildAsyncClient() {
        return new DataLakeLeaseAsyncClient(blobLeaseClientBuilder.buildAsyncClient());
    }

    /**
     * Configures the builder based on the passed {@link PathClient}. This will set the {@link HttpPipeline} and
     * {@link URL} that are used to interact with the service.
     *
     * @param pathClient Client used to configure the builder.
     * @return the updated DataLakeLeaseClientBuilder object
     * @throws NullPointerException If {@code pathClient} is {@code null}.
     */
    public DataLakeLeaseClientBuilder pathClient(PathClient pathClient) {
        blobLeaseClientBuilder.blobClient(pathClient.getBlockBlobClient());
        return this;
    }

    /**
     * Configures the builder based on the passed {@link PathAsyncClient}. This will set the {@link HttpPipeline} and
     * {@link URL} that are used to interact with the service.
     *
     * @param pathAsyncClient PathAsyncClient used to configure the builder.
     * @return the updated DataLakeLeaseClientBuilder object
     * @throws NullPointerException If {@code pathAsyncClient} is {@code null}.
     */
    public DataLakeLeaseClientBuilder pathAsyncClient(PathAsyncClient pathAsyncClient) {
        blobLeaseClientBuilder.blobAsyncClient(pathAsyncClient.getBlockBlobAsyncClient());
        return this;
    }

    /**
     * Configures the builder based on the passed {@link FileSystemClient}. This will set the {@link HttpPipeline}
     * and {@link URL} that are used to interact with the service.
     *
     * @param fileSystemClient FileSystemClient used to configure the builder.
     * @return the updated DataLakeLeaseClientBuilder object
     * @throws NullPointerException If {@code fileSystemClient} is {@code null}.
     */
    public DataLakeLeaseClientBuilder fileSystemClient(FileSystemClient fileSystemClient) {
        blobLeaseClientBuilder.containerClient(fileSystemClient.getBlobContainerClient());
        return this;
    }

    /**
     * Configures the builder based on the passed {@link FileSystemAsyncClient}. This will set the {@link
     * HttpPipeline} and {@link URL} that are used to interact with the service.
     *
     * @param fileSystemAsyncClient FileSystemAsyncClient used to configure the builder.
     * @return the updated DataLakeLeaseClientBuilder object
     * @throws NullPointerException If {@code fileSystemAsyncClient} is {@code null}.
     */
    public DataLakeLeaseClientBuilder fileSystemAsyncClient(FileSystemAsyncClient fileSystemAsyncClient) {
        blobLeaseClientBuilder.containerAsyncClient(fileSystemAsyncClient.getBlobContainerAsyncClient());
        return this;
    }

    /**
     * Sets the identifier for the lease.
     *
     * <p>If a lease ID isn't set then a {@link UUID} will be used.</p>
     *
     * @param leaseId Identifier for the lease.
     * @return the updated DataLakeLeaseClientBuilder object
     */
    public DataLakeLeaseClientBuilder leaseId(String leaseId) {
        blobLeaseClientBuilder.leaseId(leaseId);
        return this;
    }
}
