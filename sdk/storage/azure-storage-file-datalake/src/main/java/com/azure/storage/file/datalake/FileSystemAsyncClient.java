// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.implementation.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.file.datalake.implementation.DataLakeStorageClientImpl;
import com.azure.storage.file.datalake.implementation.models.LeaseAccessConditions;
import com.azure.storage.file.datalake.models.FileSystemAccessConditions;
import com.azure.storage.file.datalake.models.FileSystemProperties;
import com.azure.storage.file.datalake.models.PublicAccessType;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Client to a file system. It may only be instantiated through a {@link FileSystemClientBuilder} or via the method
 * {@link DataLakeServiceAsyncClient#getFileSystemAsyncClient(String)}. This class does not hold any state about a
 * particular blob but is instead a convenient way of sending off appropriate requests to the resource on the service.
 * It may also be used to construct URLs to blobs.
 *
 * <p>
 * This client contains operations on a file system. Operations on a path are available on {@link FileAsyncClient} and
 * {@link DirectoryAsyncClient} through {@link #getFileAsyncClient(String)} and
 * {@link #getDirectoryAsyncClient(String)} respectively, and operations on the service are available on
 * {@link DataLakeServiceAsyncClient}.
 *
 * <p>
 * Please refer to the <a href=https://docs.microsoft.com/en-us/azure/storage/blobs/data-lake-storage-introduction?toc=%2fazure%2fstorage%2fblobs%2ftoc.json>
 *     Azure Docs</a> for more information on file systems.
 *
 * <p>
 * Note this client is an async client that returns reactive responses from Spring Reactor Core project
 * (https://projectreactor.io/). Calling the methods in this client will <strong>NOT</strong> start the actual network
 * operation, until {@code .subscribe()} is called on the reactive response. You can simply convert one of these
 * responses to a {@link java.util.concurrent.CompletableFuture} object through {@link Mono#toFuture()}.
 */
@ServiceClient(builder = FileSystemClientBuilder.class, isAsync = true)
public class FileSystemAsyncClient {

    public static final String ROOT_FILESYSTEM_NAME = "$root";
    private final ClientLogger logger = new ClientLogger(FileSystemAsyncClient.class);

    private final BlobContainerAsyncClient blobContainerAsyncClient;
    private final DataLakeStorageClientImpl azureDataLakeStorage;
    private final CpkInfo customerProvidedKey; // only used to pass down to blob clients
    private final String accountName;
    private final String fileSystemName;

    /**
     * Package-private constructor for use by {@link FileSystemClientBuilder}.
     *
     * @param azureBlobStorage the API client for blob storage
     */
    protected FileSystemAsyncClient(BlobContainerAsyncClient blobContainerAsyncClient,
        DataLakeStorageClientImpl dataLakeImpl, String accountName, String fileSystemName) {
        this.blobContainerAsyncClient = blobContainerAsyncClient;
        this.azureDataLakeStorage = dataLakeImpl;
        this.accountName = accountName;
        this.fileSystemName = fileSystemName;
    }

    /**
     * Gets the URL of the file system represented by this client.
     *
     * @return the URL.
     */
    public String getFileSystemUrl() {
        return azureDataLakeStorage.getUrl();
    }

    /**
     * Get the file system name.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobContainerAsyncClient.getBlobContainerName}
     *
     * @return The name of container.
     */
    public String getFileSystemName() {
        return this.fileSystemName;
    }

    /**
     * Get associated account name.
     *
     * @return account name associated with this storage resource.
     */
    public String getAccountName() {
        return this.accountName;
    }

    /**
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return The pipeline.
     */
    public HttpPipeline getHttpPipeline() {
        return azureDataLakeStorage.getHttpPipeline();
    }

    public Mono<Void> create() {
        return createWithResponse(null, null).flatMap(FluxUtil::toMono);
    }

    public Mono<Response<Void>> createWithResponse(Map<String, String> metadata, PublicAccessType
        publicAccessType) {
        return blobContainerAsyncClient.createWithResponse(metadata,
            Transforms.toBlobPublicAccessType(publicAccessType));
    }

    public Mono<Void> delete() {
        return deleteWithResponse(null).flatMap(FluxUtil::toMono);
    }

    public Mono<Response<Void>> deleteWithResponse(FileSystemAccessConditions accessConditions) {
        return blobContainerAsyncClient.deleteWithResponse(
            Transforms.toBlobContainerAccessConditions(accessConditions));
    }

    public BlobContainerAsyncClient getBlobContainerAsyncClient() {
        return blobContainerAsyncClient;
    }

    public DirectoryAsyncClient getDirectoryAsyncClient() {
        return null;
    }

    public FileAsyncClient getFileAsyncClient() {
        return null;
    }

    // TODO (gapra) : DFS endpoint GetPaths

    public Mono<FileSystemProperties> getProperties() {
        return getPropertiesWithResponse(null).flatMap(FluxUtil::toMono);
    }

    public Mono<Response<FileSystemProperties>> getPropertiesWithResponse(LeaseAccessConditions leaseAccessConditions) {
        return blobContainerAsyncClient.getPropertiesWithResponse(
            Transforms.toBlobLeaseAccessConditions(leaseAccessConditions))
            .map(blobContainerPropertiesResponse -> new SimpleResponse<>(
                blobContainerPropertiesResponse.getRequest(),
                blobContainerPropertiesResponse.getStatusCode(),
                blobContainerPropertiesResponse.getHeaders(),
                Transforms.toFileSystemProperties(blobContainerPropertiesResponse.getValue())));
    }

    public Mono<Void> setMetadata(Map<String, String> metadata) {
        return setMetadataWithResponse(metadata, null).flatMap(FluxUtil::toMono);
    }

    public Mono<Response<Void>> setMetadataWithResponse(Map<String, String> metadata,
        FileSystemAccessConditions accessConditions) {
        return blobContainerAsyncClient.setMetadataWithResponse(metadata,
            Transforms.toBlobContainerAccessConditions(accessConditions));
    }


}
