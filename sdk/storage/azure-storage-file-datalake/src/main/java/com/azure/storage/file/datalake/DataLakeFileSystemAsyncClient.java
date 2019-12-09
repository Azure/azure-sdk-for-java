// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.Utility;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.file.datalake.implementation.DataLakeStorageClientBuilder;
import com.azure.storage.file.datalake.implementation.DataLakeStorageClientImpl;
import com.azure.storage.file.datalake.implementation.models.FileSystemsListPathsResponse;
import com.azure.storage.file.datalake.implementation.models.Path;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.DataLakeSignedIdentifier;
import com.azure.storage.file.datalake.models.FileSystemAccessPolicies;
import com.azure.storage.file.datalake.models.FileSystemProperties;
import com.azure.storage.file.datalake.models.ListPathsOptions;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import com.azure.storage.file.datalake.models.PathItem;
import com.azure.storage.file.datalake.models.PublicAccessType;
import com.azure.storage.file.datalake.models.UserDelegationKey;
import com.azure.storage.file.datalake.sas.DataLakeServiceSasSignatureValues;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.pagedFluxError;


/**
 * Client to a file system. It may only be instantiated through a {@link DataLakeFileSystemClientBuilder} or via the
 * method {@link DataLakeServiceAsyncClient#getFileSystemAsyncClient(String)}. This class does not hold any state about
 * a particular blob but is instead a convenient way of sending off appropriate requests to the resource on the service.
 * It may also be used to construct clients for files/directories.
 *
 * <p>
 * This client contains operations on a file system. Operations on a path are available on
 * {@link DataLakeFileAsyncClient} and {@link DataLakeDirectoryAsyncClient} through {@link #getFileAsyncClient(String)}
 * and {@link #getDirectoryAsyncClient(String)} respectively, and operations on the service are available on
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
@ServiceClient(builder = DataLakeFileSystemClientBuilder.class, isAsync = true)
public class DataLakeFileSystemAsyncClient {

    public static final String ROOT_FILESYSTEM_NAME = "$root";

//    public static final String STATIC_WEBSITE_FILESYSTEM_NAME = "$web";

//    public static final String LOG_FILESYSTEM_NAME = "$logs";

    private final ClientLogger logger = new ClientLogger(DataLakeFileSystemAsyncClient.class);
    private final DataLakeStorageClientImpl azureDataLakeStorage;
    private final BlobContainerAsyncClient blobContainerAsyncClient;

    private final String accountName;
    private final String fileSystemName;
    private final DataLakeServiceVersion serviceVersion;

    /**
     * Package-private constructor for use by {@link DataLakeFileSystemClientBuilder}.
     *
     * @param pipeline The pipeline used to send and receive service requests.
     * @param url The endpoint where to send service requests.
     * @param serviceVersion The version of the service to receive requests.
     * @param accountName The storage account name.
     * @param fileSystemName The file system name.
     * @param blobContainerAsyncClient The underlying {@link BlobContainerAsyncClient}
     */
    DataLakeFileSystemAsyncClient(HttpPipeline pipeline, String url, DataLakeServiceVersion serviceVersion,
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
     * Creates a new DataLakeFileAsyncClient object by concatenating fileName to the end of
     * DataLakeFileSystemAsyncClient's URL. The new DataLakeFileAsyncClient uses the same request policy pipeline as
     * the DataLakeFileSystemAsyncClient.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.getFileAsyncClient#String}
     *
     * @param fileName A {@code String} representing the name of the file.
     * @return A new {@link DataLakeFileAsyncClient} object which references the file with the specified name in this
     * file system.
     */
    public DataLakeFileAsyncClient getFileAsyncClient(String fileName) {
        Objects.requireNonNull(fileName, "'fileName' can not be set to null");

        BlockBlobAsyncClient blockBlobAsyncClient = blobContainerAsyncClient.getBlobAsyncClient(fileName,
            null).getBlockBlobAsyncClient();

        return new DataLakeFileAsyncClient(getHttpPipeline(),
            StorageImplUtils.appendToUrlPath(getFileSystemUrl(), Utility.urlEncode(Utility.urlDecode(fileName)))
                .toString(), getServiceVersion(), getAccountName(), getFileSystemName(), fileName,
            blockBlobAsyncClient);
    }

    /**
     * Creates a new DataLakeDirectoryAsyncClient object by concatenating directoryName to the end of
     * DataLakeFileSystemAsyncClient's URL. The new DataLakeDirectoryAsyncClient uses the same request policy pipeline
     * as the DataLakeFileSystemAsyncClient.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.getDirectoryAsyncClient#String}
     *
     * @param directoryName A {@code String} representing the name of the directory.
     * @return A new {@link DataLakeDirectoryAsyncClient} object which references the directory with the specified name
     * in this file system.
     */
    public DataLakeDirectoryAsyncClient getDirectoryAsyncClient(String directoryName) {
        Objects.requireNonNull(directoryName, "'directoryName' can not be set to null");

        BlockBlobAsyncClient blockBlobAsyncClient = blobContainerAsyncClient.getBlobAsyncClient(directoryName,
            null).getBlockBlobAsyncClient();
        return new DataLakeDirectoryAsyncClient(getHttpPipeline(),
            StorageImplUtils.appendToUrlPath(getFileSystemUrl(), Utility.urlEncode(Utility.urlDecode(directoryName)))
                .toString(), getServiceVersion(), getAccountName(), getFileSystemName(), directoryName,
            blockBlobAsyncClient);
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
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.getFileSystemName}
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
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.create}
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
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createWithResponse#Map-PublicAccessType}
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
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.delete}
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
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteWithResponse#DataLakeRequestConditions}
     *
     * @param requestConditions {@link DataLakeRequestConditions}
     * @return A reactive response signalling completion.
     * @throws UnsupportedOperationException If either {@link DataLakeRequestConditions#getIfMatch()} or
     * {@link DataLakeRequestConditions#getIfNoneMatch()} is set.
     */
    public Mono<Response<Void>> deleteWithResponse(DataLakeRequestConditions requestConditions) {
        try {
            return blobContainerAsyncClient.deleteWithResponse(
                Transforms.toBlobRequestConditions(requestConditions));
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
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.getProperties}
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
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.getPropertiesWithResponse#String}
     *
     * @param leaseId The lease ID the active lease on the file system must match.
     * @return A reactive response containing the file system properties.
     */
    public Mono<Response<FileSystemProperties>> getPropertiesWithResponse(String leaseId) {
        try {
            return blobContainerAsyncClient.getPropertiesWithResponse(leaseId)
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
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.setMetadata#Map}
     *
     * @param metadata Metadata to associate with the file system.
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
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.setMetadataWithResponse#Map-DataLakeRequestConditions}
     *
     * @param metadata Metadata to associate with the file system.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains signalling
     * completion.
     * @throws UnsupportedOperationException If one of {@link DataLakeRequestConditions#getIfMatch()},
     * {@link DataLakeRequestConditions#getIfNoneMatch()}, or {@link DataLakeRequestConditions#getIfUnmodifiedSince()}
     * is set.
     */
    public Mono<Response<Void>> setMetadataWithResponse(Map<String, String> metadata,
        DataLakeRequestConditions requestConditions) {
        try {
            return blobContainerAsyncClient.setMetadataWithResponse(metadata,
                Transforms.toBlobRequestConditions(requestConditions));
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
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.listPaths}
     *
     * @return A reactive response emitting the list of files/directories.
     */
    public PagedFlux<PathItem> listPaths() {
        try {
            return this.listPaths(new ListPathsOptions());
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
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.listPaths#ListPathsOptions}
     *
     * @param options A {@link ListPathsOptions} which specifies what data should be returned by the service.
     * @return A reactive response emitting the list of files/directories.
     */
    public PagedFlux<PathItem> listPaths(ListPathsOptions options) {
        return listPathsWithOptionalTimeout(options, null);
    }

    PagedFlux<PathItem> listPathsWithOptionalTimeout(ListPathsOptions options,
        Duration timeout) {
        Function<String, Mono<PagedResponse<Path>>> func =
            marker -> listPathsSegment(marker, options, timeout)
                .map(response -> new PagedResponseBase<>(
                    response.getRequest(),
                    response.getStatusCode(),
                    response.getHeaders(),
                    response.getValue().getPaths(),
                    response.getDeserializedHeaders().getContinuation(),
                    response.getDeserializedHeaders()));

        return new PagedFlux<>(() -> func.apply(null), func).mapPage(Transforms::toPathItem);
    }

    private Mono<FileSystemsListPathsResponse> listPathsSegment(String marker,
        ListPathsOptions options, Duration timeout) {
        options = options == null ? new ListPathsOptions() : options;

        return StorageImplUtils.applyOptionalTimeout(
            this.azureDataLakeStorage.fileSystems().listPathsWithRestResponseAsync(
                options.isRecursive(), marker, options.getPath(), options.getMaxResults(),
                options.isUserPrincipalNameReturned(), null, null, Context.NONE), timeout);
    }

    /**
     * Creates a new file within a file system. If a file with the same name already exists, the file will be
     * overwritten. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createFile#String}
     *
     * @param fileName Name of the file to create.
     * @return A {@link Mono} containing a {@link DataLakeFileAsyncClient} used to interact with the file created.
     */
    public Mono<DataLakeFileAsyncClient> createFile(String fileName) {
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
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createFileWithResponse#String-String-String-PathHttpHeaders-Map-DataLakeRequestConditions}
     *
     * @param fileName Name of the file to create.
     * @param permissions POSIX access permissions for the file owner, the file owning group, and others.
     * @param umask Restricts permissions of the file to be created.
     * @param headers {@link PathHttpHeaders}
     * @param metadata Metadata to associate with the file.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a {@link
     * DataLakeFileAsyncClient} used to interact with the file created.
     */
    public Mono<Response<DataLakeFileAsyncClient>> createFileWithResponse(String fileName,
        String permissions, String umask, PathHttpHeaders headers, Map<String, String> metadata,
        DataLakeRequestConditions requestConditions) {
        try {
            DataLakeFileAsyncClient dataLakeFileAsyncClient = getFileAsyncClient(fileName);

            return dataLakeFileAsyncClient.createWithResponse(permissions, umask, headers, metadata, requestConditions)
                .map(response -> new SimpleResponse<>(response, dataLakeFileAsyncClient));
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
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteFile#String}
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
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteFileWithResponse#String-DataLakeRequestConditions}
     *
     * @param fileName Name of the file to delete.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @return A {@link Mono} containing containing status code and HTTP headers
     */
    public Mono<Response<Void>> deleteFileWithResponse(String fileName, DataLakeRequestConditions requestConditions) {
        try {
            return getFileAsyncClient(fileName).deleteWithResponse(requestConditions);
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
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createDirectory#String}
     *
     * @param directoryName Name of the directory to create.
     * @return A {@link Mono} containing a {@link DataLakeDirectoryAsyncClient} used to interact with the directory
     * created.
     */
    public Mono<DataLakeDirectoryAsyncClient> createDirectory(String directoryName) {
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
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createDirectoryWithResponse#String-String-String-PathHttpHeaders-Map-DataLakeRequestConditions}
     *
     * @param directoryName Name of the directory to create.
     * @param permissions POSIX access permissions for the directory owner, the directory owning group, and others.
     * @param umask Restricts permissions of the directory to be created.
     * @param headers {@link PathHttpHeaders}
     * @param metadata Metadata to associate with the directory.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a {@link
     * DataLakeDirectoryAsyncClient} used to interact with the directory created.
     */
    public Mono<Response<DataLakeDirectoryAsyncClient>> createDirectoryWithResponse(String directoryName,
        String permissions, String umask, PathHttpHeaders headers, Map<String, String> metadata,
        DataLakeRequestConditions requestConditions) {
        try {
            DataLakeDirectoryAsyncClient dataLakeDirectoryAsyncClient = getDirectoryAsyncClient(directoryName);

            return dataLakeDirectoryAsyncClient.createWithResponse(permissions, umask, headers, metadata,
                requestConditions).map(response -> new SimpleResponse<>(response, dataLakeDirectoryAsyncClient));
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
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteDirectory#String}
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
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteDirectoryWithResponse#String-boolean-DataLakeRequestConditions}
     *
     * @param directoryName Name of the directory to delete.
     * @param recursive Whether or not to delete all paths beneath the directory.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @return A {@link Mono} containing containing status code and HTTP headers
     */
    public Mono<Response<Void>> deleteDirectoryWithResponse(String directoryName, boolean recursive,
        DataLakeRequestConditions requestConditions) {
        return getDirectoryAsyncClient(directoryName).deleteWithResponse(recursive, requestConditions);
    }

    /**
     * Sets the file system's permissions. The permissions indicate whether paths in a file system may be accessed
     * publicly. Note that, for each signed identifier, we will truncate the start and expiry times to the nearest
     * second to ensure the time formatting is compatible with the service. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-acl">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.setAccessPolicy#PublicAccessType-List}
     *
     * @param accessType Specifies how the data in this file system is available to the public. See the
     * x-ms-blob-public-access header in the Azure Docs for more information. Pass null for no public access.
     * @param identifiers A list of {@link DataLakeSignedIdentifier} objects that specify the permissions for the file
     * system.
     * Please see
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/establishing-a-stored-access-policy">here</a>
     * for more information. Passing null will clear all access policies.
     * @return A reactive response signalling completion.
     */
    public Mono<Void> setAccessPolicy(PublicAccessType accessType, List<DataLakeSignedIdentifier> identifiers) {
        try {
            return setAccessPolicyWithResponse(accessType, identifiers, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Sets the file system's permissions. The permissions indicate whether paths in a file system may be accessed
     * publicly. Note that, for each signed identifier, we will truncate the start and expiry times to the nearest
     * second to ensure the time formatting is compatible with the service. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-acl">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.setAccessPolicyWithResponse#PublicAccessType-List-DataLakeRequestConditions}
     *
     * @param accessType Specifies how the data in this file system is available to the public. See the
     * x-ms-blob-public-access header in the Azure Docs for more information. Pass null for no public access.
     * @param identifiers A list of {@link DataLakeSignedIdentifier} objects that specify the permissions for the file
     * system.
     * Please see
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/establishing-a-stored-access-policy">here</a>
     * for more information. Passing null will clear all access policies.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @return A reactive response signalling completion.
     * @throws UnsupportedOperationException If either {@link DataLakeRequestConditions#getIfMatch()} or
     * {@link DataLakeRequestConditions#getIfNoneMatch()} is set.
     */
    public Mono<Response<Void>> setAccessPolicyWithResponse(PublicAccessType accessType,
        List<DataLakeSignedIdentifier> identifiers, DataLakeRequestConditions requestConditions) {
        try {
            return blobContainerAsyncClient.setAccessPolicyWithResponse(Transforms.toBlobPublicAccessType(accessType),
                Transforms.toBlobIdentifierList(identifiers), Transforms.toBlobRequestConditions(requestConditions));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns the file system's permissions. The permissions indicate whether file system's paths may be accessed
     * publicly. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-acl">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.getAccessPolicy}
     *
     * @return A reactive response containing the file system access policy.
     */
    public Mono<FileSystemAccessPolicies> getAccessPolicy() {
        try {
            return getAccessPolicyWithResponse(null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns the file system's permissions. The permissions indicate whether file system's paths may be accessed
     * publicly. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-acl">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.getAccessPolicyWithResponse#String}
     *
     * @param leaseId The lease ID the active lease on the file system must match.
     * @return A reactive response containing the file system access policy.
     */
    public Mono<Response<FileSystemAccessPolicies>> getAccessPolicyWithResponse(String leaseId) {
        try {
            return blobContainerAsyncClient.getAccessPolicyWithResponse(leaseId)
                .map(response -> new SimpleResponse<>(response,
                Transforms.toFileSystemAccessPolicies(response.getValue())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    BlobContainerAsyncClient getBlobContainerAsyncClient() {
        return blobContainerAsyncClient;
    }

    /**
     * Generates a user delegation SAS for the file system using the specified
     * {@link DataLakeServiceSasSignatureValues}.
     * <p>See {@link DataLakeServiceSasSignatureValues} for more information on how to construct a user delegation SAS.
     * </p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.generateUserDelegationSas#DataLakeServiceSasSignatureValues-UserDelegationKey}
     *
     * @param dataLakeServiceSasSignatureValues {@link DataLakeServiceSasSignatureValues}
     * @param userDelegationKey A {@link UserDelegationKey} object used to sign the SAS values.
     * @see DataLakeServiceAsyncClient#getUserDelegationKey(OffsetDateTime, OffsetDateTime) for more information on how
     * to get a user delegation key.
     *
     * @return A {@code String} representing all SAS query parameters.
     */
    public String generateUserDelegationSas(DataLakeServiceSasSignatureValues dataLakeServiceSasSignatureValues,
        UserDelegationKey userDelegationKey) {
        return blobContainerAsyncClient.generateUserDelegationSas(
            Transforms.toBlobSasValues(dataLakeServiceSasSignatureValues),
            Transforms.toBlobUserDelegationKey(userDelegationKey));
    }

    /**
     * Generates a service SAS for the file system using the specified {@link DataLakeServiceSasSignatureValues}
     * Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link DataLakeServiceSasSignatureValues} for more information on how to construct a service SAS.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.generateSas#DataLakeServiceSasSignatureValues}
     *
     * @param dataLakeServiceSasSignatureValues {@link DataLakeServiceSasSignatureValues}
     *
     * @return A {@code String} representing all SAS query parameters.
     */
    public String generateSas(DataLakeServiceSasSignatureValues dataLakeServiceSasSignatureValues) {
        return blobContainerAsyncClient.generateSas(Transforms.toBlobSasValues(dataLakeServiceSasSignatureValues));
    }
}
