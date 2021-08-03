// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
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
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.SasImplUtils;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.file.datalake.implementation.AzureDataLakeStorageRestAPIImpl;
import com.azure.storage.file.datalake.implementation.AzureDataLakeStorageRestAPIImplBuilder;
import com.azure.storage.file.datalake.implementation.models.FileSystemsListBlobHierarchySegmentResponse;
import com.azure.storage.file.datalake.implementation.models.FileSystemsListPathsResponse;
import com.azure.storage.file.datalake.implementation.models.ListBlobsShowOnly;
import com.azure.storage.file.datalake.implementation.models.PathResourceType;
import com.azure.storage.file.datalake.implementation.util.DataLakeImplUtils;
import com.azure.storage.file.datalake.implementation.util.DataLakeSasImplUtil;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.DataLakeSignedIdentifier;
import com.azure.storage.file.datalake.models.FileSystemAccessPolicies;
import com.azure.storage.file.datalake.models.FileSystemProperties;
import com.azure.storage.file.datalake.models.ListPathsOptions;
import com.azure.storage.file.datalake.models.PathDeletedItem;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import com.azure.storage.file.datalake.models.PathItem;
import com.azure.storage.file.datalake.models.PublicAccessType;
import com.azure.storage.file.datalake.models.UserDelegationKey;
import com.azure.storage.file.datalake.sas.DataLakeServiceSasSignatureValues;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.pagedFluxError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;
import static com.azure.storage.common.Utility.STORAGE_TRACING_NAMESPACE_VALUE;

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
 * Please refer to the
 *
 * <a href="https://docs.microsoft.com/azure/storage/blobs/data-lake-storage-introduction">
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

    private static final String ROOT_DIRECTORY_NAME = "";

    private final ClientLogger logger = new ClientLogger(DataLakeFileSystemAsyncClient.class);
    private final AzureDataLakeStorageRestAPIImpl azureDataLakeStorage;
    private final AzureDataLakeStorageRestAPIImpl blobDataLakeStorageFs; // Just for list deleted paths
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
        this.azureDataLakeStorage = new AzureDataLakeStorageRestAPIImplBuilder()
            .pipeline(pipeline)
            .url(url)
            .fileSystem(fileSystemName)
            .version(serviceVersion.getVersion())
            .buildClient();
        String blobUrl = DataLakeImplUtils.endpointToDesiredEndpoint(url, "blob", "dfs");
        this.blobDataLakeStorageFs = new AzureDataLakeStorageRestAPIImplBuilder()
            .pipeline(pipeline)
            .url(blobUrl)
            .fileSystem(fileSystemName)
            .version(serviceVersion.getVersion())
            .buildClient();

        this.serviceVersion = serviceVersion;

        this.accountName = accountName;
        this.fileSystemName = fileSystemName;
        this.blobContainerAsyncClient = blobContainerAsyncClient;
    }

    /**
     * Initializes a new DataLakeFileAsyncClient object by concatenating fileName to the end of
     * DataLakeFileSystemAsyncClient's URL. The new DataLakeFileAsyncClient uses the same request policy pipeline as
     * the DataLakeFileSystemAsyncClient.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.getFileAsyncClient#String}
     *
     * @param fileName A {@code String} representing the name of the file. If the path name contains special characters,
     * pass in the url encoded version of the path name.
     * @return A new {@link DataLakeFileAsyncClient} object which references the file with the specified name in this
     * file system.
     */
    public DataLakeFileAsyncClient getFileAsyncClient(String fileName) {
        Objects.requireNonNull(fileName, "'fileName' can not be set to null");

        BlockBlobAsyncClient blockBlobAsyncClient = blobContainerAsyncClient.getBlobAsyncClient(fileName,
            null).getBlockBlobAsyncClient();

        return new DataLakeFileAsyncClient(getHttpPipeline(), getAccountUrl(), getServiceVersion(), getAccountName(),
            getFileSystemName(), fileName, blockBlobAsyncClient);
    }

    /**
     * Initializes a new DataLakeDirectoryAsyncClient object by concatenating directoryName to the end of
     * DataLakeFileSystemAsyncClient's URL. The new DataLakeDirectoryAsyncClient uses the same request policy pipeline
     * as the DataLakeFileSystemAsyncClient.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.getDirectoryAsyncClient#String}
     *
     * @param directoryName A {@code String} representing the name of the directory. If the path name contains special
     * characters, pass in the url encoded version of the path name.
     * @return A new {@link DataLakeDirectoryAsyncClient} object which references the directory with the specified name
     * in this file system.
     */
    public DataLakeDirectoryAsyncClient getDirectoryAsyncClient(String directoryName) {
        Objects.requireNonNull(directoryName, "'directoryName' can not be set to null");

        BlockBlobAsyncClient blockBlobAsyncClient = blobContainerAsyncClient.getBlobAsyncClient(directoryName,
            null).getBlockBlobAsyncClient();
        return new DataLakeDirectoryAsyncClient(getHttpPipeline(), getAccountUrl(), getServiceVersion(),
            getAccountName(), getFileSystemName(), directoryName, blockBlobAsyncClient);
    }

    /**
     * Initializes a new DataLakeDirectoryAsyncClient object by concatenating {@code ""} to the end of
     * DataLakeFileSystemAsyncClient's URL. The new DataLakeDirectoryAsyncClient uses the same request policy pipeline
     * as the DataLakeFileSystemAsyncClient.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.getRootDirectoryAsyncClient}
     *
     * @return A new {@link DataLakeDirectoryAsyncClient} object which references the root directory
     * in this file system.
     */
    DataLakeDirectoryAsyncClient getRootDirectoryAsyncClient() {
        return getDirectoryAsyncClient(DataLakeFileSystemAsyncClient.ROOT_DIRECTORY_NAME);
    }

    /**
     * Get the url of the storage account.
     *
     * @return the URL of the storage account
     */
    public String getAccountUrl() {
        return azureDataLakeStorage.getUrl();
    }

    /**
     * Gets the URL of the file system represented by this client.
     *
     * @return the URL.
     */
    public String getFileSystemUrl() {
        return azureDataLakeStorage.getUrl() + "/" + fileSystemName;
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
    @ServiceMethod(returns = ReturnType.SINGLE)
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
     * @param metadata Metadata to associate with the file system. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param accessType Specifies how the data in this file system is available to the public. See the
     * x-ms-blob-public-access header in the Azure Docs for more information. Pass null for no public access.
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> createWithResponse(Map<String, String> metadata, PublicAccessType accessType) {
        try {
            return blobContainerAsyncClient.createWithResponse(metadata, Transforms.toBlobPublicAccessType(accessType))
                .onErrorMap(DataLakeImplUtils::transformBlobStorageException);
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
    @ServiceMethod(returns = ReturnType.SINGLE)
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
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteWithResponse(DataLakeRequestConditions requestConditions) {
        try {
            return blobContainerAsyncClient.deleteWithResponse(
                Transforms.toBlobRequestConditions(requestConditions))
                .onErrorMap(DataLakeImplUtils::transformBlobStorageException);
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
    @ServiceMethod(returns = ReturnType.SINGLE)
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
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<FileSystemProperties>> getPropertiesWithResponse(String leaseId) {
        try {
            return blobContainerAsyncClient.getPropertiesWithResponse(leaseId)
                .onErrorMap(DataLakeImplUtils::transformBlobStorageException)
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
     * @param metadata Metadata to associate with the file system. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
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
     * @param metadata Metadata to associate with the file system. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains signalling
     * completion.
     * @throws UnsupportedOperationException If one of {@link DataLakeRequestConditions#getIfMatch()},
     * {@link DataLakeRequestConditions#getIfNoneMatch()}, or {@link DataLakeRequestConditions#getIfUnmodifiedSince()}
     * is set.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> setMetadataWithResponse(Map<String, String> metadata,
        DataLakeRequestConditions requestConditions) {
        try {
            return blobContainerAsyncClient.setMetadataWithResponse(metadata,
                Transforms.toBlobRequestConditions(requestConditions))
                .onErrorMap(DataLakeImplUtils::transformBlobStorageException);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns a reactive Publisher emitting all the files/directories in this account lazily as needed. For more
     * information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/filesystem/list">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.listPaths}
     *
     * @return A reactive response emitting the list of files/directories.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PathItem> listPaths() {
        try {
            return this.listPaths(new ListPathsOptions());
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    /**
     * Returns a reactive Publisher emitting all the files/directories in this account lazily as needed. For more
     * information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/filesystem/list">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.listPaths#ListPathsOptions}
     *
     * @param options A {@link ListPathsOptions} which specifies what data should be returned by the service.
     * @return A reactive response emitting the list of files/directories.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PathItem> listPaths(ListPathsOptions options) {
        return listPathsWithOptionalTimeout(options, null);
    }

    PagedFlux<PathItem> listPathsWithOptionalTimeout(ListPathsOptions options,
        Duration timeout) {
        BiFunction<String, Integer, Mono<PagedResponse<PathItem>>> func =
            (marker, pageSize) -> {
                ListPathsOptions finalOptions;
                if (pageSize != null) {
                    if (options == null) {
                        finalOptions = new ListPathsOptions().setMaxResults(pageSize);
                    } else {
                        finalOptions = new ListPathsOptions()
                            .setMaxResults(pageSize)
                            .setPath(options.getPath())
                            .setRecursive(options.isRecursive())
                            .setUserPrincipalNameReturned(options.isUserPrincipalNameReturned());
                    }
                } else {
                    finalOptions = options;
                }
                return listPathsSegment(marker, finalOptions, timeout)
                    .map(response -> {
                        List<PathItem> value = response.getValue() == null
                            ? Collections.emptyList()
                            : response.getValue().getPaths().stream()
                            .map(Transforms::toPathItem)
                            .collect(Collectors.toList());

                        return new PagedResponseBase<>(
                            response.getRequest(),
                            response.getStatusCode(),
                            response.getHeaders(),
                            value,
                            response.getDeserializedHeaders().getXMsContinuation(),
                            response.getDeserializedHeaders());
                    });
            };
        return new PagedFlux<>(pageSize -> func.apply(null, pageSize), func);
    }

    private Mono<FileSystemsListPathsResponse> listPathsSegment(String marker,
        ListPathsOptions options, Duration timeout) {
        options = options == null ? new ListPathsOptions() : options;

        return StorageImplUtils.applyOptionalTimeout(
            this.azureDataLakeStorage.getFileSystems().listPathsWithResponseAsync(options.isRecursive(), null, null,
                marker, options.getPath(), options.getMaxResults(),
                options.isUserPrincipalNameReturned(),  Context.NONE), timeout);
    }

    /**
     * Returns a reactive Publisher emitting all the files/directories in this filesystem that have been recently soft
     * deleted lazily as needed. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/list-blobs">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.listDeletedPaths}
     *
     * @return A reactive response emitting the list of files/directories.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PathDeletedItem> listDeletedPaths() {
        try {
            return this.listDeletedPaths(null);
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    /**
     * Returns a reactive Publisher emitting all the files/directories in this account lazily as needed. For more
     * information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/list-blobs">Azure Docs</a>.
     *
     * Note: You can specify the page size by using byPaged methods that accept an integer such as
     * {@link PagedFlux#byPage(int)}. Please refer to the REST docs above for limitations on page size
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.listDeletedPaths#String}
     *
     * @param prefix Specifies the path to filter the results to.
     * @return A reactive response emitting the list of files/directories.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PathDeletedItem> listDeletedPaths(String prefix) {
        try {
            return new PagedFlux<>(pageSize -> withContext(context -> listDeletedPaths(null, pageSize, prefix,
                null, context)),
                (marker, pageSize) -> withContext(context -> listDeletedPaths(marker, pageSize, prefix, null,
                    context)));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    PagedFlux<PathDeletedItem> listDeletedPathsWithOptionalTimeout(String prefix, Duration timeout, Context context) {
        return new PagedFlux<>(pageSize -> listDeletedPaths(null, pageSize, prefix, timeout, context),
            (marker, pageSize) -> listDeletedPaths(marker, pageSize, prefix, timeout, context));
    }

    private Mono<PagedResponse<PathDeletedItem>> listDeletedPaths(String marker, Integer pageSize,
        String prefix, Duration timeout, Context context) {
        return listDeletedPathsSegment(marker, prefix, pageSize, timeout, context)
            .map(response -> {
                List<PathDeletedItem> value = response.getValue().getSegment() == null
                    ? Collections.emptyList()
                    : Stream.concat(
                    response.getValue().getSegment().getBlobItems().stream().map(Transforms::toPathDeletedItem),
                    response.getValue().getSegment().getBlobPrefixes().stream()
                        .map(Transforms::toPathDeletedItem)
                ).collect(Collectors.toList());
                return new PagedResponseBase<>(
                    response.getRequest(),
                    response.getStatusCode(),
                    response.getHeaders(),
                    value,
                    response.getValue().getNextMarker(),
                    response.getDeserializedHeaders());
            });
    }

    private Mono<FileSystemsListBlobHierarchySegmentResponse> listDeletedPathsSegment(String marker,
        String prefix, Integer maxResults, Duration timeout, Context context) {
        context = context == null ? Context.NONE : context;

        return StorageImplUtils.applyOptionalTimeout(
            this.blobDataLakeStorageFs.getFileSystems().listBlobHierarchySegmentWithResponseAsync(
                prefix, null, marker, maxResults,
                null, ListBlobsShowOnly.DELETED, null, null,
                context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE)), timeout);
    }

    /**
     * Creates a new file within a file system. By default this method will not overwrite an existing file. For more
     * information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createFile#String}
     *
     * @param fileName Name of the file to create. If the path name contains special characters, pass in the url encoded
     * version of the path name.
     * @return A {@link Mono} containing a {@link DataLakeFileAsyncClient} used to interact with the file created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DataLakeFileAsyncClient> createFile(String fileName) {
        return createFile(fileName, false);
    }

    /**
     * Creates a new file within a file system. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createFile#String-boolean}
     *
     * @param fileName Name of the file to create. If the path name contains special characters, pass in the url encoded
     * version of the path name.
     * @param overwrite Whether or not to overwrite, should a file exist.
     * @return A {@link Mono} containing a {@link DataLakeFileAsyncClient} used to interact with the file created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DataLakeFileAsyncClient> createFile(String fileName, boolean overwrite) {
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions();
        if (!overwrite) {
            requestConditions.setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD);
        }
        try {
            return createFileWithResponse(fileName, null, null, null, null, requestConditions)
                .flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new file within a file system. If a file with the same name already exists, the file will be
     * overwritten. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createFileWithResponse#String-String-String-PathHttpHeaders-Map-DataLakeRequestConditions}
     *
     * @param fileName Name of the file to create. If the path name contains special characters, pass in the url encoded
     * version of the path name.
     * @param permissions POSIX access permissions for the file owner, the file owning group, and others.
     * @param umask Restricts permissions of the file to be created.
     * @param headers {@link PathHttpHeaders}
     * @param metadata Metadata to associate with the file. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a {@link
     * DataLakeFileAsyncClient} used to interact with the file created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
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
     * For more information see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteFile#String}
     *
     * @param fileName Name of the file to delete. If the path name contains special characters, pass in the url encoded
     * version of the path name.
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteFile(String fileName) {
        try {
            return deleteFileWithResponse(fileName, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes the specified file in the file system. If the file doesn't exist the operation fails.
     * For more information see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteFileWithResponse#String-DataLakeRequestConditions}
     *
     * @param fileName Name of the file to delete. If the path name contains special characters, pass in the url encoded
     * version of the path name.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @return A {@link Mono} containing containing status code and HTTP headers
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteFileWithResponse(String fileName, DataLakeRequestConditions requestConditions) {
        try {
            return getFileAsyncClient(fileName).deleteWithResponse(requestConditions);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new directory within a file system. By default this method will not overwrite an existing directory.
     * For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createDirectory#String}
     *
     * @param directoryName Name of the directory to create. If the path name contains special characters, pass in the
     * url encoded version of the path name.
     * @return A {@link Mono} containing a {@link DataLakeDirectoryAsyncClient} used to interact with the directory
     * created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DataLakeDirectoryAsyncClient> createDirectory(String directoryName) {
        return createDirectory(directoryName, false);
    }

    /**
     * Creates a new directory within a file system. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createDirectory#String-boolean}
     *
     * @param directoryName Name of the directory to create. If the path name contains special characters, pass in the
     * url encoded version of the path name.
     * @param overwrite Whether or not to overwrite, should a directory exist.
     * @return A {@link Mono} containing a {@link DataLakeDirectoryAsyncClient} used to interact with the directory
     * created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DataLakeDirectoryAsyncClient> createDirectory(String directoryName, boolean overwrite) {
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions();
        if (!overwrite) {
            requestConditions.setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD);
        }
        try {
            return createDirectoryWithResponse(directoryName, null, null, null, null, requestConditions)
                .flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new directory within a file system. If a directory with the same name already exists, the directory
     * will be overwritten. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createDirectoryWithResponse#String-String-String-PathHttpHeaders-Map-DataLakeRequestConditions}
     *
     * @param directoryName Name of the directory to create. If the path name contains special characters, pass in the
     * url encoded version of the path name.
     * @param permissions POSIX access permissions for the directory owner, the directory owning group, and others.
     * @param umask Restricts permissions of the directory to be created.
     * @param headers {@link PathHttpHeaders}
     * @param metadata Metadata to associate with the resource. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a {@link
     * DataLakeDirectoryAsyncClient} used to interact with the directory created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
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
     * For more information see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteDirectory#String}
     *
     * @param directoryName Name of the directory to delete. If the path name contains special characters, pass in the
     * url encoded version of the path name.
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteDirectory(String directoryName) {
        return deleteDirectoryWithResponse(directoryName, false, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes the specified directory in the file system. If the directory doesn't exist the operation fails.
     * For more information see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteDirectoryWithResponse#String-boolean-DataLakeRequestConditions}
     *
     * @param directoryName Name of the directory to delete. If the path name contains special characters, pass in the
     * url encoded version of the path name.
     * @param recursive Whether or not to delete all paths beneath the directory.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @return A {@link Mono} containing containing status code and HTTP headers
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteDirectoryWithResponse(String directoryName, boolean recursive,
        DataLakeRequestConditions requestConditions) {
        return getDirectoryAsyncClient(directoryName).deleteWithResponse(recursive, requestConditions);
    }

    /**
     * Restores a soft deleted path in the file system. For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.undeletePath#String-String}
     *
     * @param deletedPath The deleted path
     * @param deletionId deletion ID associated with the soft deleted path that uniquely identifies a resource if
     * multiple have been soft deleted at this location.
     * You can get soft deleted paths and their associated deletion IDs with {@link #listDeletedPaths()}.
     * @return A reactive response signalling completion.
     * @throws NullPointerException if deletedPath or deletionId is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DataLakePathAsyncClient> undeletePath(String deletedPath, String deletionId) {
        return undeletePathWithResponse(deletedPath, deletionId).flatMap(FluxUtil::toMono);
    }

    /**
     * Restores a soft deleted path in the file system. For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.undeletePathWithResponse#String-String}
     *
     * @param deletedPath The deleted path
     * @param deletionId deletion ID associated with the soft deleted path that uniquely identifies a resource if
     * multiple have been soft deleted at this location.
     * You can get soft deleted paths and their associated deletion IDs with {@link #listDeletedPaths()}.
     * @return A reactive response signalling completion.
     * @throws NullPointerException if deletedPath or deletionId is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DataLakePathAsyncClient>> undeletePathWithResponse(String deletedPath, String deletionId) {
        try {
            return withContext(context -> undeletePathWithResponse(deletedPath, deletionId, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DataLakePathAsyncClient>> undeletePathWithResponse(String deletedPath, String deletionId,
        Context context) {
        Objects.requireNonNull(deletedPath);
        Objects.requireNonNull(deletionId);

        context = context == null ? Context.NONE : context;
        String blobUrl = DataLakeImplUtils.endpointToDesiredEndpoint(blobDataLakeStorageFs.getUrl(), "blob", "dfs");

        // This instance is to have a datalake impl that points to the blob endpoint
        AzureDataLakeStorageRestAPIImpl blobDataLakeStoragePath = new AzureDataLakeStorageRestAPIImplBuilder()
            .pipeline(blobDataLakeStorageFs.getHttpPipeline())
            .url(blobUrl)
            .fileSystem(blobDataLakeStorageFs.getFileSystem())
            .path(Utility.urlDecode(deletedPath))
            .version(serviceVersion.getVersion())
            .buildClient();

        // Initial rest call
        return blobDataLakeStoragePath.getPaths().undeleteWithResponseAsync(null,
            String.format("?%s=%s", Constants.UrlConstants.DELETIONID_QUERY_PARAMETER, deletionId), null,
            context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
                .onErrorMap(DataLakeImplUtils::transformBlobStorageException)
                // Construct the new client and final response from the undelete + getProperties responses
                .map(response -> {
                    DataLakePathAsyncClient client = new DataLakePathAsyncClient(getHttpPipeline(), getAccountUrl(),
                        serviceVersion, accountName, fileSystemName, deletedPath,
                        PathResourceType.fromString(response.getDeserializedHeaders().getXMsResourceType()),
                        blobContainerAsyncClient.getBlobAsyncClient(deletedPath, null)
                            .getBlockBlobAsyncClient());
                    if (PathResourceType.DIRECTORY.equals(client.pathResourceType)) {
                        return new SimpleResponse<>(response, new DataLakeDirectoryAsyncClient(client));
                    } else if (PathResourceType.FILE.equals(client.pathResourceType)) {
                        return new SimpleResponse<>(response, new DataLakeFileAsyncClient(client));
                    } else {
                        throw logger.logExceptionAsError(new IllegalStateException("'pathClient' expected to be either "
                            + "a file or directory client."));
                    }
                });
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/establishing-a-stored-access-policy">here</a>
     * for more information. Passing null will clear all access policies.
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/establishing-a-stored-access-policy">here</a>
     * for more information. Passing null will clear all access policies.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @return A reactive response signalling completion.
     * @throws UnsupportedOperationException If either {@link DataLakeRequestConditions#getIfMatch()} or
     * {@link DataLakeRequestConditions#getIfNoneMatch()} is set.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> setAccessPolicyWithResponse(PublicAccessType accessType,
        List<DataLakeSignedIdentifier> identifiers, DataLakeRequestConditions requestConditions) {
        try {
            return blobContainerAsyncClient.setAccessPolicyWithResponse(Transforms.toBlobPublicAccessType(accessType),
                Transforms.toBlobIdentifierList(identifiers), Transforms.toBlobRequestConditions(requestConditions))
                .onErrorMap(DataLakeImplUtils::transformBlobStorageException);
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
    @ServiceMethod(returns = ReturnType.SINGLE)
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
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<FileSystemAccessPolicies>> getAccessPolicyWithResponse(String leaseId) {
        try {
            return blobContainerAsyncClient.getAccessPolicyWithResponse(leaseId)
                .onErrorMap(DataLakeImplUtils::transformBlobStorageException)
                .map(response -> new SimpleResponse<>(response,
                Transforms.toFileSystemAccessPolicies(response.getValue())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

//    /**
//     * Renames an existing file system.
//     *
//     * <p><strong>Code Samples</strong></p>
//     *
//     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.rename#String}
//     *
//     * @param destinationContainerName The new name of the file system.
//     * @return A {@link Mono} containing a {@link DataLakeFileSystemAsyncClient} used to interact with the renamed file system.
//     */
//    @ServiceMethod(returns = ReturnType.SINGLE)
//    public Mono<DataLakeFileSystemAsyncClient> rename(String destinationContainerName) {
//        return renameWithResponse(new FileSystemRenameOptions(destinationContainerName)).flatMap(FluxUtil::toMono);
//    }
//
//    /**
//     * Renames an existing file system.
//     *
//     * <p><strong>Code Samples</strong></p>
//     *
//     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.renameWithResponse#FileSystemRenameOptions}
//     *
//     * @param options {@link FileSystemRenameOptions}
//     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a
//     * {@link DataLakeFileSystemAsyncClient} used to interact with the renamed file system.
//     */
//    @ServiceMethod(returns = ReturnType.SINGLE)
//    public Mono<Response<DataLakeFileSystemAsyncClient>> renameWithResponse(FileSystemRenameOptions options) {
//        try {
//            return blobContainerAsyncClient.renameWithResponse(Transforms.toBlobContainerRenameOptions(options))
//                .onErrorMap(DataLakeImplUtils::transformBlobStorageException)
//                .map(response -> new SimpleResponse<>(response,
//                    this.getFileSystemAsyncClient(options.getDestinationFileSystemName())));
//        } catch (RuntimeException ex) {
//            return monoError(logger, ex);
//        }
//    }
//
//    /**
//     * Takes in a destination and creates a DataLakeFileSystemAsyncClient with a new path
//     * @param destinationFileSystem The destination file system
//     * @return A DataLakeFileSystemAsyncClient
//     */
//    DataLakeFileSystemAsyncClient getFileSystemAsyncClient(String destinationFileSystem) {
//        if (CoreUtils.isNullOrEmpty(destinationFileSystem)) {
//            throw logger.logExceptionAsError(new IllegalArgumentException("'destinationFileSystem' can not be set to null"));
//        }
//        // Get current Datalake URL and replace current filesystem with user provided filesystem
//        String newDfsEndpoint = BlobUrlParts.parse(getFileSystemUrl())
//            .setContainerName(destinationFileSystem).toUrl().toString();
//
//        return new DataLakeFileSystemAsyncClient(getHttpPipeline(), newDfsEndpoint, serviceVersion, accountName,
//            destinationFileSystem, prepareBuilderReplacePath(destinationFileSystem).buildAsyncClient());
//    }
//
//    /**
//     * Takes in a destination path and creates a ContainerClientBuilder with a new path name
//     * @param destinationFileSystem The destination file system
//     * @return An updated SpecializedBlobClientBuilder
//     */
//    BlobContainerClientBuilder prepareBuilderReplacePath(String destinationFileSystem) {
//        if (CoreUtils.isNullOrEmpty(destinationFileSystem)) {
//            throw logger.logExceptionAsError(new IllegalArgumentException("'destinationFileSystem' can not be set to null"));
//        }
//        // Get current Blob URL and replace current filesystem with user provided filesystem
//        String newBlobEndpoint = BlobUrlParts.parse(DataLakeImplUtils.endpointToDesiredEndpoint(getFileSystemUrl(),
//            "blob", "dfs")).setContainerName(destinationFileSystem).toUrl().toString();
//
//        return new BlobContainerClientBuilder()
//            .pipeline(getHttpPipeline())
//            .endpoint(newBlobEndpoint)
//            .serviceVersion(TransformUtils.toBlobServiceVersion(getServiceVersion()));
//    }

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
     * See {@link DataLakeServiceAsyncClient#getUserDelegationKey(OffsetDateTime, OffsetDateTime)} for more information
     * on how to get a user delegation key.
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateUserDelegationSas(DataLakeServiceSasSignatureValues dataLakeServiceSasSignatureValues,
        UserDelegationKey userDelegationKey) {
        return generateUserDelegationSas(dataLakeServiceSasSignatureValues, userDelegationKey, getAccountName(),
            Context.NONE);
    }

    /**
     * Generates a user delegation SAS for the file system using the specified
     * {@link DataLakeServiceSasSignatureValues}.
     * <p>See {@link DataLakeServiceSasSignatureValues} for more information on how to construct a user delegation SAS.
     * </p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.generateUserDelegationSas#DataLakeServiceSasSignatureValues-UserDelegationKey-String-Context}
     *
     * @param dataLakeServiceSasSignatureValues {@link DataLakeServiceSasSignatureValues}
     * @param userDelegationKey A {@link UserDelegationKey} object used to sign the SAS values.
     * See {@link DataLakeServiceAsyncClient#getUserDelegationKey(OffsetDateTime, OffsetDateTime)} for more information
     * on how to get a user delegation key.
     * @param accountName The account name.
     * @param context Additional context that is passed through the code when generating a SAS.
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateUserDelegationSas(DataLakeServiceSasSignatureValues dataLakeServiceSasSignatureValues,
        UserDelegationKey userDelegationKey, String accountName, Context context) {
        return new DataLakeSasImplUtil(dataLakeServiceSasSignatureValues, getFileSystemName())
            .generateUserDelegationSas(userDelegationKey, accountName, context);
    }

    /**
     * Generates a service SAS for the file system using the specified {@link DataLakeServiceSasSignatureValues}
     * <p>Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link DataLakeServiceSasSignatureValues} for more information on how to construct a service SAS.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.generateSas#DataLakeServiceSasSignatureValues}
     *
     * @param dataLakeServiceSasSignatureValues {@link DataLakeServiceSasSignatureValues}
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateSas(DataLakeServiceSasSignatureValues dataLakeServiceSasSignatureValues) {
        return generateSas(dataLakeServiceSasSignatureValues, Context.NONE);
    }

    /**
     * Generates a service SAS for the file system using the specified {@link DataLakeServiceSasSignatureValues}
     * <p>Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link DataLakeServiceSasSignatureValues} for more information on how to construct a service SAS.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.generateSas#DataLakeServiceSasSignatureValues-Context}
     *
     * @param dataLakeServiceSasSignatureValues {@link DataLakeServiceSasSignatureValues}
     * @param context Additional context that is passed through the code when generating a SAS.
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateSas(DataLakeServiceSasSignatureValues dataLakeServiceSasSignatureValues, Context context) {
        return new DataLakeSasImplUtil(dataLakeServiceSasSignatureValues, getFileSystemName())
            .generateSas(SasImplUtils.extractSharedKeyCredential(getHttpPipeline()), context);
    }
}
