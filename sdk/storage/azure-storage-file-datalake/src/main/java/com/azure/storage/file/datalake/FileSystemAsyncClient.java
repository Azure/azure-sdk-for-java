// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.implementation.http.PagedResponseBase;
import com.azure.core.implementation.util.FluxUtil;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobUrlParts;
import com.azure.storage.common.Utility;
import com.azure.storage.file.datalake.implementation.DataLakeStorageClientImpl;
import com.azure.storage.file.datalake.implementation.models.FileSystemsListPathsResponse;
import com.azure.storage.file.datalake.implementation.models.LeaseAccessConditions;
import com.azure.storage.file.datalake.implementation.models.Path;
import com.azure.storage.file.datalake.models.FileSystemAccessConditions;
import com.azure.storage.file.datalake.models.GetPathsOptions;
import com.azure.storage.file.datalake.models.PublicAccessType;
import com.azure.storage.file.datalake.implementation.models.ModifiedAccessConditions;

import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.function.Function;

import static com.azure.storage.blob.implementation.PostProcessor.postProcessResponse;

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
    private final String accountName;
    private final String fileSystemName;

    /**
     * Protected constructor for use by {@link FileSystemClientBuilder}.
     *
     */
    protected FileSystemAsyncClient(BlobContainerAsyncClient blobContainerAsyncClient,
        DataLakeStorageClientImpl dataLakeImpl, String accountName, String fileSystemName) {
        this.blobContainerAsyncClient = blobContainerAsyncClient;
        this.azureDataLakeStorage = dataLakeImpl;
        this.accountName = accountName;
        this.fileSystemName = fileSystemName;
    }

    /**
     * Creates a new FileAsyncClient object by concatenating fileName to the end of FileSystemAsyncClient's URL. The new
     * FileAsyncClient uses the same request policy pipeline as the FileSystemAsyncClient.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileSystemAsyncClient.getFileAsyncClient#String}
     *
     * @param fileName A {@code String} representing the name of the file.
     * @return A new {@link FileAsyncClient} object which references the file with the specified name in this file
     * system.
     */
    public FileAsyncClient getFileAsyncClient(String fileName) {
        BlobAsyncClient blobAsyncClient = blobContainerAsyncClient.getBlobAsyncClient(fileName,
            null);
        return null;
//        return new FileAsyncClient(blobAsyncClient, new DataLakeStorageClientBuilder()
//            .url(Utility.appendToURLPath(getFileSystemUrl(), fileName).toString())
//            .pipeline(azureDataLakeStorage.getHttpPipeline())
//            .build(), snapshot, customerProvidedKey, accountName);
    }

    /**
     * Creates a new DirectoryAsyncClient object by concatenating directoryName to the end of FileSystemAsyncClient's
     * URL. The new DirectoryAsyncClient uses the same request policy pipeline as the FileSystemAsyncClient.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileSystemAsyncClient.getDirectoryAsyncClient#String}
     *
     * @param directoryName A {@code String} representing the name of the directory.
     * @return A new {@link DirectoryAsyncClient} object which references the directory with the specified name in this
     * file system.
     */
    public DirectoryAsyncClient getDirectoryAsyncClient(String directoryName) {
        return getDirectoryAsyncClient(directoryName, null);
    }

    /**
     * Creates a new DirectoryAsyncClient object by concatenating directoryName to the end of FileSystemAsyncClient's
     * URL. The new DirectoryAsyncClient uses the same request policy pipeline as the FileSystemAsyncClient.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileSystemAsyncClient.getDirectoryAsyncClient#String-String}
     *
     * @param directoryName A {@code String} representing the name of the directory.
     * @param snapshot the snapshot identifier for the directory.
     * @return A new {@link DirectoryAsyncClient} object which references the directory with the specified name in this
     * file system.
     */
    public DirectoryAsyncClient getDirectoryAsyncClient(String directoryName, String snapshot) {
        BlobAsyncClient blobAsyncClient = blobContainerAsyncClient.getBlobAsyncClient(directoryName,
            snapshot);
        return null;
//        return new FileAsyncClient(blobAsyncClient, new DataLakeStorageClientBuilder()
//            .url(Utility.appendToURLPath(getFileSystemUrl(), fileName).toString())
//            .pipeline(azureDataLakeStorage.getHttpPipeline())
//            .build(), snapshot, customerProvidedKey, accountName);
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
     * {@codesnippet com.azure.storage.file.datalake.FileSystemAsyncClient.getFileSystemName}
     *
     * @return The name of file system.
     */
    public String getFileSystemName() {
        return BlobUrlParts.parse(this.blobContainerAsyncClient.getBlobContainerUrl(), logger).getBlobContainerName();
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

    /**
     * Creates a new file system within a storage account. If a file system with the same name already exists, the
     * operation fails. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileSystemAsyncClient.create}
     *
     * @return A reactive response signalling completion.
     */
    public Mono<Void> create() {
        return createWithResponse(null, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a new file system within a storage account. If a file system with the same name already exists, the
     * operation fails. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileSystemAsyncClient.createWithResponse#Map-PublicAccessType}
     *
     * @param metadata Metadata to associate with the file system.
     * @param accessType Specifies how the data in this file system is available to the public. See the
     * x-ms-blob-public-access header in the Azure Docs for more information. Pass null for no public access.
     * @return A reactive response signalling completion.
     */
    public Mono<Response<Void>> createWithResponse(Map<String, String> metadata, PublicAccessType
        accessType) {
        return blobContainerAsyncClient.createWithResponse(metadata,
            Transforms.toBlobPublicAccessType(accessType));
    }

    /**
     * Marks the specified file system for deletion. The file system and any files/directories contained within it are
     * later deleted during garbage collection. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileSystemAsyncClient.delete}
     *
     * @return A reactive response signalling completion.
     */
    public Mono<Void> delete() {
        return deleteWithResponse(null).flatMap(FluxUtil::toMono);
    }

    /**
     * Marks the specified file system for deletion. The file system and any files/directories contained within it are
     * later deleted during garbage collection. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileSystemAsyncClient.deleteWithResponse#FileSystemAccessConditions}
     *
     * @param accessConditions {@link FileSystemAccessConditions}
     * @return A reactive response signalling completion.
     * @throws UnsupportedOperationException If {@link FileSystemAccessConditions#getModifiedAccessConditions()} has
     * either {@link ModifiedAccessConditions#getIfMatch()} or {@link ModifiedAccessConditions#getIfNoneMatch()} set.
     */
    public Mono<Response<Void>> deleteWithResponse(FileSystemAccessConditions accessConditions) {
        return blobContainerAsyncClient.deleteWithResponse(
            Transforms.toBlobContainerAccessConditions(accessConditions));
    }

    BlobContainerAsyncClient getBlobContainerAsyncClient() {
        return blobContainerAsyncClient;
    }

    /**
     * Returns the file system's metadata and system properties. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-metadata">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileSystemAsyncClient.getProperties}
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} containing the
     * file system properties.
     */
    public Mono<FileSystemProperties> getProperties() {
        return getPropertiesWithResponse(null).flatMap(FluxUtil::toMono);
    }

    /**
     * Returns the file system's metadata and system properties. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-metadata">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileSystemAsyncClient.getPropertiesWithResponse#LeaseAccessConditions}
     *
     * @param leaseAccessConditions By setting lease access conditions, requests will fail if the provided lease does
     * not match the active lease on the file system.
     * @return A reactive response containing the file system properties.
     */
    public Mono<Response<FileSystemProperties>> getPropertiesWithResponse(LeaseAccessConditions leaseAccessConditions) {
        return blobContainerAsyncClient.getPropertiesWithResponse(
            Transforms.toBlobLeaseAccessConditions(leaseAccessConditions))
            .map(blobContainerPropertiesResponse -> new SimpleResponse<>(
                blobContainerPropertiesResponse.getRequest(),
                blobContainerPropertiesResponse.getStatusCode(),
                blobContainerPropertiesResponse.getHeaders(),
                Transforms.toFileSystemProperties(blobContainerPropertiesResponse.getValue())));
    }

    /**
     * Sets the file system's metadata. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-metadata">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.FileSystemAsyncClient.setMetadata#Map}
     *
     * @param metadata Metadata to associate with the container.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains signalling
     * completion.
     */
    public Mono<Void> setMetadata(Map<String, String> metadata) {
        return setMetadataWithResponse(metadata, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Sets the file systems's metadata. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-metadata">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileSystemAsyncClient.setMetadataWithResponse#Map-FileSystemAccessConditions}
     *
     * @param metadata Metadata to associate with the file system.
     * @param accessConditions {@link FileSystemAccessConditions}
     * @return A reactive response signalling completion.
     * @throws UnsupportedOperationException If {@link FileSystemAccessConditions#getModifiedAccessConditions()} has
     * anything set other than {@link ModifiedAccessConditions#getIfModifiedSince()}.
     */
    public Mono<Response<Void>> setMetadataWithResponse(Map<String, String> metadata,
        FileSystemAccessConditions accessConditions) {
        return blobContainerAsyncClient.setMetadataWithResponse(metadata,
            Transforms.toBlobContainerAccessConditions(accessConditions));
    }

    // TODO (gapra): Must return PathItem instead of Path
    /**
     * Returns a reactive Publisher emitting all the files/directories in this account lazily as needed. For more
     * information, see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/filesystem/list">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileSystemAsyncClient.getPaths}
     *
     * @return A reactive response emitting the list of files/directories.
     */
    public PagedFlux<Path> getPaths() {
        return this.getPaths(new GetPathsOptions());
    }

    /**
     * Returns a reactive Publisher emitting all the files/directories in this account lazily as needed. For more
     * information, see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/filesystem/list">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileSystemAsyncClient.getPaths#GetPathsOptions}
     *
     * @param options A {@link GetPathsOptions} which specifies what data should be returned by the service.
     * @return A reactive response emitting the list of files/directories.
     */
    public PagedFlux<Path> getPaths(GetPathsOptions options) {
        return getPathsWithOptionalTimeout(options, null);
    }

    PagedFlux<Path> getPathsWithOptionalTimeout(GetPathsOptions options,
        Duration timeout) {
        Function<String, Mono<PagedResponse<Path>>> func =
            marker -> getPathsSegment(marker, options, timeout)
                .map(response -> new PagedResponseBase<>(
                    response.getRequest(),
                    response.getStatusCode(),
                    response.getHeaders(),
                    response.getValue().getPaths(),
                    response.getDeserializedHeaders().getContinuation(),
                    response.getDeserializedHeaders()));

        return new PagedFlux<>(() -> func.apply(null), func);
    }

    private Mono<FileSystemsListPathsResponse> getPathsSegment(String marker,
        GetPathsOptions options, Duration timeout) {
        options = options == null ? new GetPathsOptions() : options;

        return postProcessResponse(Utility.applyOptionalTimeout(
            this.azureDataLakeStorage.fileSystems().listPathsWithRestResponseAsync(
                options.isRecursive(), marker, options.getPath(), options.getMaxResults(), options.isUpn(), null,
                null, Context.NONE), timeout));
    }

}
