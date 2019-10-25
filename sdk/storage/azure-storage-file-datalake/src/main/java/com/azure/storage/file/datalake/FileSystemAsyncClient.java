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
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.file.datalake.implementation.DataLakeStorageClientBuilder;
import com.azure.storage.file.datalake.implementation.DataLakeStorageClientImpl;
import com.azure.storage.file.datalake.implementation.models.FileSystemsListPathsResponse;
import com.azure.storage.file.datalake.models.AccessTier;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import com.azure.storage.file.datalake.models.LeaseAccessConditions;
import com.azure.storage.file.datalake.implementation.models.Path;
import com.azure.storage.file.datalake.models.FileSystemAccessConditions;
import com.azure.storage.file.datalake.models.GetPathsOptions;
import com.azure.storage.file.datalake.models.PathAccessConditions;
import com.azure.storage.file.datalake.models.PathItem;
import com.azure.storage.file.datalake.models.PublicAccessType;
import com.azure.storage.file.datalake.models.ModifiedAccessConditions;

import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.function.Function;

import static com.azure.core.implementation.util.FluxUtil.monoError;
import static com.azure.core.implementation.util.FluxUtil.pagedFluxError;


/**
 * Client to a file system. It may only be instantiated through a {@link FileSystemClientBuilder} or via the method
 * {@link DataLakeServiceAsyncClient#getFileSystemAsyncClient(String)}. This class does not hold any state about a
 * particular blob but is instead a convenient way of sending off appropriate requests to the resource on the service.
 * It may also be used to construct clients for files/directories.
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

//    public static final String STATIC_WEBSITE_FILESYSTEM_NAME = "$web";

//    public static final String LOG_FILESYSTEM_NAME = "$logs";

    private final ClientLogger logger = new ClientLogger(BlobContainerAsyncClient.class);
    private final DataLakeStorageClientImpl azureDataLakeStorage;
    private final BlobContainerAsyncClient blobContainerAsyncClient;

    private final String accountName;
    private final String fileSystemName;
    private final DataLakeServiceVersion serviceVersion;

    /**
     * Package-private constructor for use by {@link FileSystemClientBuilder}.
     *
     * @param pipeline The pipeline used to send and receive service requests.
     * @param url The endpoint where to send service requests.
     * @param serviceVersion The version of the service to receive requests.
     * @param accountName The storage account name.
     * @param fileSystemName The file system name.
     * @param blobContainerAsyncClient The underlying {@link BlobContainerAsyncClient}
     */
    FileSystemAsyncClient(HttpPipeline pipeline, String url, DataLakeServiceVersion serviceVersion,
        String accountName, String fileSystemName, BlobContainerAsyncClient blobContainerAsyncClient) {
        this.azureDataLakeStorage = new DataLakeStorageClientBuilder()
            .pipeline(pipeline)
            .url(url)
            .version(serviceVersion.getVersion())
            .build();
        this.serviceVersion = serviceVersion;

        this.accountName = accountName;
        this.fileSystemName = fileSystemName;
        this.blobContainerAsyncClient = blobContainerAsyncClient;
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
        if (ImplUtils.isNullOrEmpty(fileName)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'fileName' can not be set to null"));
        }
        BlockBlobAsyncClient blockBlobAsyncClient = blobContainerAsyncClient.getBlobAsyncClient(fileName,
            null).getBlockBlobAsyncClient();

        return new FileAsyncClient(getHttpPipeline(),
            StorageImplUtils.appendToUrlPath(getFileSystemUrl(), fileName).toString(), getServiceVersion(),
            getAccountName(), getFileSystemName(), fileName, blockBlobAsyncClient);
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
        if (ImplUtils.isNullOrEmpty(directoryName)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'directoryName' can not be set to null"));
        }
        BlockBlobAsyncClient blockBlobAsyncClient = blobContainerAsyncClient.getBlobAsyncClient(directoryName,
            null).getBlockBlobAsyncClient();
        return new DirectoryAsyncClient(getHttpPipeline(),
            StorageImplUtils.appendToUrlPath(getFileSystemUrl(), directoryName).toString(), getServiceVersion(),
            getAccountName(), getFileSystemName(), directoryName, blockBlobAsyncClient);
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
        return fileSystemName;
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
     * Gets the service version the client is using.
     *
     * @return the service version the client is using.
     */
    public DataLakeServiceVersion getServiceVersion() {
        return serviceVersion;
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
        try {
            return createWithResponse(null, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
    public Mono<Response<Void>> createWithResponse(Map<String, String> metadata, PublicAccessType accessType) {
        try {
            return blobContainerAsyncClient.createWithResponse(metadata, Transforms.toBlobPublicAccessType(accessType));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
        try {
            return deleteWithResponse(null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
        try {
            return blobContainerAsyncClient.deleteWithResponse(
                Transforms.toBlobContainerAccessConditions(accessConditions));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
        try {
            return getPropertiesWithResponse(null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
        try {
            return blobContainerAsyncClient.getPropertiesWithResponse(
                Transforms.toBlobLeaseAccessConditions(leaseAccessConditions))
                .map(response -> new SimpleResponse<>(response,
                    Transforms.toFileSystemProperties(response.getValue())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Sets the file system's metadata. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-metadata">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileSystemAsyncClient.setMetadata#Map}
     *
     * @param metadata Metadata to associate with the container.
     * @return A reactive response signalling completion.
     */
    public Mono<Void> setMetadata(Map<String, String> metadata) {
        try {
            return setMetadataWithResponse(metadata, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains signalling
     * completion.
     * @throws UnsupportedOperationException If {@link FileSystemAccessConditions#getModifiedAccessConditions()} has
     * anything set other than {@link ModifiedAccessConditions#getIfModifiedSince()}.
     */
    public Mono<Response<Void>> setMetadataWithResponse(Map<String, String> metadata,
        FileSystemAccessConditions accessConditions) {
        try {
            return blobContainerAsyncClient.setMetadataWithResponse(metadata,
                Transforms.toBlobContainerAccessConditions(accessConditions));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

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
    public PagedFlux<PathItem> getPaths() {
        try {
            return this.getPaths(new GetPathsOptions());
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
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
    public PagedFlux<PathItem> getPaths(GetPathsOptions options) {
        return getPathsWithOptionalTimeout(options, null);
    }

    PagedFlux<PathItem> getPathsWithOptionalTimeout(GetPathsOptions options,
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

        return new PagedFlux<>(() -> func.apply(null), func).mapPage(Transforms::toPathItem);
    }

    private Mono<FileSystemsListPathsResponse> getPathsSegment(String marker,
        GetPathsOptions options, Duration timeout) {
        options = options == null ? new GetPathsOptions() : options;

        return StorageImplUtils.applyOptionalTimeout(
            this.azureDataLakeStorage.fileSystems().listPathsWithRestResponseAsync(
                options.isRecursive(), marker, options.getPath(), options.getMaxResults(), options.isReturnUpn(), null,
                null, Context.NONE), timeout);
    }

    /**
     * Creates a new file within a file system. If a file with the same name already exists, the file will be
     * overwritten. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileSystemAsyncClient.createFile#String}
     *
     * @param fileName Name of the file to create.
     * @return A {@link Mono} containing a {@link FileAsyncClient} used to interact with the file created.
     */
    public Mono<FileAsyncClient> createFile(String fileName) {
        try {
            return createFileWithResponse(fileName, null, null, null, null, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new file within a file system. If a file with the same name already exists, the file will be
     * overwritten. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileSystemAsyncClient.createFileWithResponse#String-PathHttpHeaders-Map-PathAccessConditions-String-String}
     *
     * @param fileName Name of the file to create.
     * @param headers {@link PathHttpHeaders}
     * @param metadata Metadata to associate with the file.
     * @param accessConditions {@link PathAccessConditions}
     * @param permissions POSIX access permissions for the file owner, the file owning group, and others.
     * @param umask Restricts permissions of the file to be created.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a {@link
     * FileAsyncClient} used to interact with the file created.
     */
    public Mono<Response<FileAsyncClient>> createFileWithResponse(String fileName,
        PathHttpHeaders headers, Map<String, String> metadata, PathAccessConditions accessConditions,
        String permissions, String umask) {
        try {
            FileAsyncClient fileAsyncClient = getFileAsyncClient(fileName);

            return fileAsyncClient.createWithResponse(headers, metadata, accessConditions, permissions, umask)
                .map(response -> new SimpleResponse<>(response, fileAsyncClient));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes the specified file in the file system. If the file doesn't exist the operation fails.
     * For more information see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileSystemAsyncClient.deleteFile#String}
     *
     * @param fileName Name of the file to delete.
     * @return A reactive response signalling completion.
     */
    public Mono<Void> deleteFile(String fileName) {
        try {
            return deleteFileWithResponse(fileName, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes the specified file in the file system. If the file doesn't exist the operation fails.
     * For more information see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileSystemAsyncClient.deleteFileWithResponse#String-PathAccessConditions}
     *
     * @param fileName Name of the file to delete.
     * @param accessConditions {@link PathAccessConditions}
     * @return A {@link Mono} containing containing status code and HTTP headers
     */
    public Mono<Response<Void>> deleteFileWithResponse(String fileName, PathAccessConditions accessConditions) {
        try {
            return getFileAsyncClient(fileName).deleteWithResponse(accessConditions);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new directory within a file system. If a directory with the same name already exists, the directory
     * will be overwritten. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileSystemAsyncClient.createDirectory#String}
     *
     * @param directoryName Name of the directory to create.
     * @return A {@link Mono} containing a {@link DirectoryAsyncClient} used to interact with the directory created.
     */
    public Mono<DirectoryAsyncClient> createDirectory(String directoryName) {
        try {
            return createDirectoryWithResponse(directoryName, null, null, null, null, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new directory within a file system. If a directory with the same name already exists, the directory
     * will be overwritten. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileSystemAsyncClient.createDirectoryWithResponse#String-PathHttpHeaders-Map-PathAccessConditions-String-String}
     *
     * @param directoryName Name of the directory to create.
     * @param headers {@link PathHttpHeaders}
     * @param metadata Metadata to associate with the directory.
     * @param accessConditions {@link PathAccessConditions}
     * @param permissions POSIX access permissions for the directory owner, the directory owning group, and others.
     * @param umask Restricts permissions of the directory to be created.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a {@link
     * DirectoryAsyncClient} used to interact with the directory created.
     */
    public Mono<Response<DirectoryAsyncClient>> createDirectoryWithResponse(String directoryName,
        PathHttpHeaders headers, Map<String, String> metadata, PathAccessConditions accessConditions,
        String permissions, String umask) {
        try {
            DirectoryAsyncClient directoryAsyncClient = getDirectoryAsyncClient(directoryName);

            return directoryAsyncClient.createWithResponse(headers, metadata, accessConditions, permissions, umask)
                .map(response -> new SimpleResponse<>(response, directoryAsyncClient));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes the specified directory in the file system. If the directory doesn't exist the operation fails.
     * For more information see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileSystemAsyncClient.deleteDirectory#String}
     *
     * @param directoryName Name of the directory to delete.
     * @return A reactive response signalling completion.
     */
    public Mono<Void> deleteDirectory(String directoryName) {
        return deleteDirectoryWithResponse(directoryName, false, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes the specified directory in the file system. If the directory doesn't exist the operation fails.
     * For more information see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileSystemAsyncClient.deleteDirectoryWithResponse#String-boolean-PathAccessConditions}
     *
     * @param directoryName Name of the directory to delete.
     * @param recursive Whether or not to delete all paths beneath the directory.
     * @param accessConditions {@link PathAccessConditions}
     * @return A {@link Mono} containing containing status code and HTTP headers
     */
    public Mono<Response<Void>> deleteDirectoryWithResponse(String directoryName, boolean recursive,
        PathAccessConditions accessConditions) {
        return getDirectoryAsyncClient(directoryName).deleteWithResponse(recursive, accessConditions);
    }

    BlobContainerAsyncClient getBlobContainerAsyncClient() {
        return blobContainerAsyncClient;
    }
}
