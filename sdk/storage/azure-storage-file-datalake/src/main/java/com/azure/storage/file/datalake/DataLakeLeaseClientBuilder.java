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
 * clients. Lease clients are able to interact with both file system and path clients and act as a supplement client. A
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

    final BlobLeaseClientBuilder blobLeaseClientBuilder;

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
     * Configures the builder based on the passed {@link DataLakePathClient}. This will set the {@link HttpPipeline} and
     * {@link URL} that are used to interact with the service.
     *
     * @param dataLakePathClient Client used to configure the builder.
     * @return the updated DataLakeLeaseClientBuilder object
     * @throws NullPointerException If {@code dataLakePathClient} is {@code null}.
     */
    public DataLakeLeaseClientBuilder pathClient(DataLakePathClient dataLakePathClient) {
        blobLeaseClientBuilder.blobClient(dataLakePathClient.getBlockBlobClient());
        return this;
    }

    /**
     * Configures the builder based on the passed {@link DataLakePathAsyncClient}. This will set the
     * {@link HttpPipeline} and {@link URL} that are used to interact with the service.
     *
     * @param dataLakePathAsyncClient DataLakePathAsyncClient used to configure the builder.
     * @return the updated DataLakeLeaseClientBuilder object
     * @throws NullPointerException If {@code dataLakePathAsyncClient} is {@code null}.
     */
    public DataLakeLeaseClientBuilder pathAsyncClient(DataLakePathAsyncClient dataLakePathAsyncClient) {
        blobLeaseClientBuilder.blobAsyncClient(dataLakePathAsyncClient.getBlockBlobAsyncClient());
        return this;
    }

    /**
     * Configures the builder based on the passed {@link DataLakeFileSystemClient}. This will set the
     * {@link HttpPipeline} and {@link URL} that are used to interact with the service.
     *
     * @param dataLakeFileSystemClient DataLakeFileSystemClient used to configure the builder.
     * @return the updated DataLakeLeaseClientBuilder object
     * @throws NullPointerException If {@code dataLakeFileSystemClient} is {@code null}.
     */
    public DataLakeLeaseClientBuilder fileSystemClient(DataLakeFileSystemClient dataLakeFileSystemClient) {
        blobLeaseClientBuilder.containerClient(dataLakeFileSystemClient.getBlobContainerClient());
        return this;
    }

    /**
     * Configures the builder based on the passed {@link DataLakeFileSystemAsyncClient}. This will set the {@link
     * HttpPipeline} and {@link URL} that are used to interact with the service.
     *
     * @param dataLakeFileSystemAsyncClient DataLakeFileSystemAsyncClient used to configure the builder.
     * @return the updated DataLakeLeaseClientBuilder object
     * @throws NullPointerException If {@code dataLakeFileSystemAsyncClient} is {@code null}.
     */
    public DataLakeLeaseClientBuilder fileSystemAsyncClient(
        DataLakeFileSystemAsyncClient dataLakeFileSystemAsyncClient) {
        blobLeaseClientBuilder.containerAsyncClient(dataLakeFileSystemAsyncClient.getBlobContainerAsyncClient());
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
