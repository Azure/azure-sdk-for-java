// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.SasImplUtils;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.file.share.implementation.AzureFileStorageImpl;
import com.azure.storage.file.share.implementation.models.DirectorysCreateResponse;
import com.azure.storage.file.share.implementation.models.DirectorysGetPropertiesResponse;
import com.azure.storage.file.share.implementation.models.DirectorysListFilesAndDirectoriesSegmentResponse;
import com.azure.storage.file.share.implementation.models.DirectorysSetMetadataResponse;
import com.azure.storage.file.share.implementation.models.DirectorysSetPropertiesResponse;
import com.azure.storage.file.share.implementation.util.ShareSasImplUtil;
import com.azure.storage.file.share.models.CloseHandlesInfo;
import com.azure.storage.file.share.models.ShareDirectoryInfo;
import com.azure.storage.file.share.models.ShareDirectoryProperties;
import com.azure.storage.file.share.models.ShareDirectorySetMetadataInfo;
import com.azure.storage.file.share.models.ShareFileHttpHeaders;
import com.azure.storage.file.share.models.ShareStorageException;
import com.azure.storage.file.share.models.HandleItem;
import com.azure.storage.file.share.models.ShareFileItem;
import com.azure.storage.file.share.sas.ShareServiceSasSignatureValues;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.pagedFluxError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * This class provides a client that contains all the operations for interacting with directory in Azure Storage File
 * Service. Operations allowed by the client are creating, deleting and listing subdirectory and file, retrieving
 * properties, setting metadata and list or force close handles of the directory or file.
 *
 * <p><strong>Instantiating an Asynchronous Directory Client</strong></p>
 *
 * {@codesnippet com.azure.storage.file.share.ShareDirectoryAsyncClient.instantiation}
 *
 * <p>View {@link ShareFileClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see ShareFileClientBuilder
 * @see ShareDirectoryClient
 * @see StorageSharedKeyCredential
 */
@ServiceClient(builder = ShareFileClientBuilder.class, isAsync = true)
public class ShareDirectoryAsyncClient {
    private final ClientLogger logger = new ClientLogger(ShareDirectoryAsyncClient.class);

    private final AzureFileStorageImpl azureFileStorageClient;
    private final String shareName;
    private final String directoryPath;
    private final String snapshot;
    private final String accountName;
    private final ShareServiceVersion serviceVersion;

    /**
     * Creates a ShareDirectoryAsyncClient that sends requests to the storage directory at {@link
     * AzureFileStorageImpl#getUrl() endpoint}. Each service call goes through the {@link HttpPipeline pipeline} in the
     * {@code client}.
     *
     * @param azureFileStorageClient Client that interacts with the service interfaces
     * @param shareName Name of the share
     * @param directoryPath Name of the directory
     * @param snapshot The snapshot of the share
     */
    ShareDirectoryAsyncClient(AzureFileStorageImpl azureFileStorageClient, String shareName, String directoryPath,
                              String snapshot, String accountName, ShareServiceVersion serviceVersion) {
        Objects.requireNonNull(shareName, "'shareName' cannot be null.");
        Objects.requireNonNull(directoryPath);
        this.shareName = shareName;
        this.directoryPath = directoryPath;
        this.snapshot = snapshot;
        this.azureFileStorageClient = azureFileStorageClient;
        this.accountName = accountName;
        this.serviceVersion = serviceVersion;
    }

    /**
     * Get the url of the storage directory client.
     *
     * @return the URL of the storage directory client
     */
    public String getDirectoryUrl() {
        StringBuilder directoryUrlString = new StringBuilder(azureFileStorageClient.getUrl()).append("/")
            .append(shareName).append("/").append(directoryPath);
        if (snapshot != null) {
            directoryUrlString.append("?snapshot=").append(snapshot);
        }
        return directoryUrlString.toString();
    }

    /**
     * Gets the service version the client is using.
     *
     * @return the service version the client is using.
     */
    public ShareServiceVersion getServiceVersion() {
        return serviceVersion;
    }

    /**
     * Constructs a ShareFileAsyncClient that interacts with the specified file.
     *
     * <p>If the file doesn't exist in this directory {@link ShareFileAsyncClient#create(long)} create} in the client
     * will need to be called before interaction with the file can happen.</p>
     *
     * @param fileName Name of the file
     * @return a ShareFileAsyncClient that interacts with the specified share
     */
    public ShareFileAsyncClient getFileClient(String fileName) {
        String filePath = directoryPath + "/" + fileName;
        return new ShareFileAsyncClient(azureFileStorageClient, shareName, filePath, null, accountName,
            serviceVersion);
    }

    /**
     * Constructs a ShareDirectoryAsyncClient that interacts with the specified directory.
     *
     * <p>If the file doesn't exist in this directory {@link ShareDirectoryAsyncClient#create()} create} in the client
     * will need to be called before interaction with the directory can happen.</p>
     *
     * @param subdirectoryName Name of the directory
     * @return a ShareDirectoryAsyncClient that interacts with the specified directory
     */
    public ShareDirectoryAsyncClient getSubdirectoryClient(String subdirectoryName) {
        String directoryPath = this.directoryPath + "/" + subdirectoryName;
        return new ShareDirectoryAsyncClient(azureFileStorageClient, shareName, directoryPath, snapshot, accountName,
            serviceVersion);
    }

    /**
     * Creates this directory in the file share and returns a response of {@link ShareDirectoryInfo} to interact
     * with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the directory</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryAsyncClient.create}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @return The {@link ShareDirectoryInfo directory info}.
     * @throws ShareStorageException If the directory has already existed, the parent directory does not exist or
     * directory name is an invalid resource name.
     */
    public Mono<ShareDirectoryInfo> create() {
        try {
            return createWithResponse(null, null, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a directory in the file share and returns a response of ShareDirectoryInfo to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the directory</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryAsyncClient.createWithResponse#FileSmbProperties-String-Map}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @param smbProperties The SMB properties of the directory.
     * @param filePermission The file permission of the directory.
     * @param metadata Optional metadata to associate with the directory
     * @return A response containing the directory info and the status of creating the directory.
     * @throws ShareStorageException If the directory has already existed, the parent directory does not exist or
     * directory name is an invalid resource name.
     */
    public Mono<Response<ShareDirectoryInfo>> createWithResponse(FileSmbProperties smbProperties, String filePermission,
                                                                 Map<String, String> metadata) {
        try {
            return withContext(context -> createWithResponse(smbProperties, filePermission, metadata, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<ShareDirectoryInfo>> createWithResponse(FileSmbProperties smbProperties, String filePermission,
                                                          Map<String, String> metadata, Context context) {
        FileSmbProperties properties = smbProperties == null ? new FileSmbProperties() : smbProperties;

        // Checks that file permission and file permission key are valid
        validateFilePermissionAndKey(filePermission, properties.getFilePermissionKey());

        // If file permission and file permission key are both not set then set default value
        filePermission = properties.setFilePermission(filePermission, FileConstants.FILE_PERMISSION_INHERIT);
        String filePermissionKey = properties.getFilePermissionKey();

        String fileAttributes = properties.setNtfsFileAttributes(FileConstants.FILE_ATTRIBUTES_NONE);
        String fileCreationTime = properties.setFileCreationTime(FileConstants.FILE_TIME_NOW);
        String fileLastWriteTime = properties.setFileLastWriteTime(FileConstants.FILE_TIME_NOW);

        return azureFileStorageClient.directorys()
            .createWithRestResponseAsync(shareName, directoryPath, fileAttributes, fileCreationTime, fileLastWriteTime,
                null, metadata, filePermission, filePermissionKey, context)
            .map(this::createWithRestResponse);
    }

    /**
     * Deletes the directory in the file share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the directory</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryAsyncClient.delete}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-directory">Azure Docs</a>.</p>
     *
     * @return An empty response.
     * @throws ShareStorageException If the share doesn't exist
     */
    public Mono<Void> delete() {
        try {
            return deleteWithResponse().flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes the directory in the file share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the directory</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryAsyncClient.deleteWithResponse}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-directory">Azure Docs</a>.</p>
     *
     * @return A response that only contains headers and response status code
     * @throws ShareStorageException If the share doesn't exist
     */
    public Mono<Response<Void>> deleteWithResponse() {
        try {
            return withContext(this::deleteWithResponse);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> deleteWithResponse(Context context) {
        return azureFileStorageClient.directorys().deleteWithRestResponseAsync(shareName, directoryPath, context)
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Retrieves the properties of this directory. The properties includes directory metadata, last modified date, is
     * server encrypted, and eTag.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve directory properties</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryAsyncClient.getProperties}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-directory-properties">Azure Docs</a>.</p>
     *
     * @return Storage directory properties
     */
    public Mono<ShareDirectoryProperties> getProperties() {
        try {
            return getPropertiesWithResponse().flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Retrieves the properties of this directory. The properties includes directory metadata, last modified date, is
     * server encrypted, and eTag.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve directory properties</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryAsyncClient.getPropertiesWithResponse}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-directory-properties">Azure Docs</a>.</p>
     *
     * @return A response containing the storage directory properties with headers and response status code
     */
    public Mono<Response<ShareDirectoryProperties>> getPropertiesWithResponse() {
        try {
            return withContext(this::getPropertiesWithResponse);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<ShareDirectoryProperties>> getPropertiesWithResponse(Context context) {
        return azureFileStorageClient.directorys()
            .getPropertiesWithRestResponseAsync(shareName, directoryPath, snapshot, null, context)
            .map(this::getPropertiesResponse);
    }

    /**
     * Sets the properties of this directory. The properties include the file SMB properties and the file permission.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set directory properties</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryAsyncClient.setProperties#FileSmbProperties-String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-directory-properties">Azure Docs</a>.</p>
     *
     * @param smbProperties The SMB properties of the directory.
     * @param filePermission The file permission of the directory.
     * @return The storage directory SMB properties
     */
    public Mono<ShareDirectoryInfo> setProperties(FileSmbProperties smbProperties, String filePermission) {
        try {
            return setPropertiesWithResponse(smbProperties, filePermission).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Sets the properties of this directory. The properties include the file SMB properties and the file permission.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set directory properties</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryAsyncClient.setPropertiesWithResponse#FileSmbProperties-String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-directory-properties">Azure Docs</a>.</p>
     *
     * @param smbProperties The SMB properties of the directory.
     * @param filePermission The file permission of the directory.
     * @return A response containing the storage directory smb properties with headers and response status code
     */
    public Mono<Response<ShareDirectoryInfo>> setPropertiesWithResponse(FileSmbProperties smbProperties,
                                                                        String filePermission) {
        try {
            return withContext(context -> setPropertiesWithResponse(smbProperties, filePermission, Context.NONE));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<ShareDirectoryInfo>> setPropertiesWithResponse(FileSmbProperties smbProperties, String filePermission,
                                                                 Context context) {

        FileSmbProperties properties = smbProperties == null ? new FileSmbProperties() : smbProperties;

        // Checks that file permission and file permission key are valid
        validateFilePermissionAndKey(filePermission, properties.getFilePermissionKey());

        // If file permission and file permission key are both not set then set default value
        filePermission = properties.setFilePermission(filePermission, FileConstants.PRESERVE);
        String filePermissionKey = properties.getFilePermissionKey();

        String fileAttributes = properties.setNtfsFileAttributes(FileConstants.PRESERVE);
        String fileCreationTime = properties.setFileCreationTime(FileConstants.PRESERVE);
        String fileLastWriteTime = properties.setFileLastWriteTime(FileConstants.PRESERVE);

        return azureFileStorageClient.directorys()
            .setPropertiesWithRestResponseAsync(shareName, directoryPath, fileAttributes, fileCreationTime,
                fileLastWriteTime, null, filePermission, filePermissionKey, context)
            .map(this::setPropertiesResponse);
    }

    /**
     * Sets the user-defined metadata to associate to the directory.
     *
     * <p>If {@code null} is passed for the metadata it will clear the metadata associated to the directory.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set the metadata to "directory:updatedMetadata"</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryAsyncClient.setMetadata#map}
     *
     * <p>Clear the metadata of the directory</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryAsyncClient.setMetadata#map.clearMetadata}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-directory-metadata">Azure Docs</a>.</p>
     *
     * @param metadata Optional metadata to set on the directory, if null is passed the metadata for the directory is
     * cleared
     * @return information about the directory
     * @throws ShareStorageException If the directory doesn't exist or the metadata contains invalid keys
     */
    public Mono<ShareDirectorySetMetadataInfo> setMetadata(Map<String, String> metadata) {
        try {
            return setMetadataWithResponse(metadata).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Sets the user-defined metadata to associate to the directory.
     *
     * <p>If {@code null} is passed for the metadata it will clear the metadata associated to the directory.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set the metadata to "directory:updatedMetadata"</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryAsyncClient.setMetadataWithResponse#map}
     *
     * <p>Clear the metadata of the directory</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryAsyncClient.setMetadataWithResponse#map.clearMetadata}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-directory-metadata">Azure Docs</a>.</p>
     *
     * @param metadata Optional metadata to set on the directory, if null is passed the metadata for the directory is
     * cleared
     * @return A response containing the information about the directory with headers and response status code
     * @throws ShareStorageException If the directory doesn't exist or the metadata contains invalid keys
     */
    public Mono<Response<ShareDirectorySetMetadataInfo>> setMetadataWithResponse(Map<String, String> metadata) {
        try {
            return withContext(context -> setMetadataWithResponse(metadata, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<ShareDirectorySetMetadataInfo>> setMetadataWithResponse(Map<String, String> metadata,
        Context context) {
        return azureFileStorageClient.directorys()
            .setMetadataWithRestResponseAsync(shareName, directoryPath, null, metadata, context)
            .map(this::setMetadataResponse);
    }

    /**
     * Lists all sub-directories and files in this directory without their prefix or maxResults in single page.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all sub-directories and files in the account</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryAsyncClient.listFilesAndDirectories}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/list-directories-and-files">Azure
     * Docs</a>.</p>
     *
     * @return {@link ShareFileItem File info} in the storage directory
     */
    public PagedFlux<ShareFileItem> listFilesAndDirectories() {
        try {
            return listFilesAndDirectories(null, null);
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    /**
     * Lists all sub-directories and files in this directory with their prefix or snapshots.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all sub-directories with "subdir" prefix and return 10 results in the account</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryAsyncClient.listFilesAndDirectories#string-integer}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/list-directories-and-files">Azure
     * Docs</a>.</p>
     *
     * @param prefix Optional prefix which filters the results to return only files and directories whose name begins
     * with.
     * @param maxResultsPerPage Optional maximum number of files and/or directories to return per page. If the request
     * does not specify maxResultsPerPage or specifies a value greater than 5,000,
     * the server will return up to 5,000 items.
     * @return {@link ShareFileItem File info} in this directory with prefix and max number of return results.
     */
    public PagedFlux<ShareFileItem> listFilesAndDirectories(String prefix, Integer maxResultsPerPage) {
        try {
            return listFilesAndDirectoriesWithOptionalTimeout(prefix, maxResultsPerPage, null, Context.NONE);
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    PagedFlux<ShareFileItem> listFilesAndDirectoriesWithOptionalTimeout(String prefix, Integer maxResultsPerPage,
                                                                        Duration timeout, Context context) {
        Function<String, Mono<PagedResponse<ShareFileItem>>> retriever =
            marker -> StorageImplUtils.applyOptionalTimeout(this.azureFileStorageClient.directorys()
                .listFilesAndDirectoriesSegmentWithRestResponseAsync(shareName, directoryPath, prefix, snapshot,
                    marker, maxResultsPerPage, null, context), timeout)
                .map(response -> new PagedResponseBase<>(response.getRequest(),
                    response.getStatusCode(),
                    response.getHeaders(),
                    convertResponseAndGetNumOfResults(response),
                    response.getValue().getNextMarker(),
                    response.getDeserializedHeaders()));

        return new PagedFlux<>(() -> retriever.apply(null), retriever);
    }

    /**
     * List of open handles on a directory or a file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Get 10 handles with recursive call.</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryAsyncClient.listHandles#integer-boolean}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/list-handles">Azure Docs</a>.</p>
     *
     * @param maxResultPerPage Optional maximum number of results will return per page
     * @param recursive Specifies operation should apply to the directory specified in the URI, its files, its
     * subdirectories and their files.
     * @return {@link HandleItem handles} in the directory that satisfy the requirements
     */
    public PagedFlux<HandleItem> listHandles(Integer maxResultPerPage, boolean recursive) {
        try {
            return listHandlesWithOptionalTimeout(maxResultPerPage, recursive, null, Context.NONE);
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    PagedFlux<HandleItem> listHandlesWithOptionalTimeout(Integer maxResultPerPage, boolean recursive, Duration timeout,
        Context context) {
        Function<String, Mono<PagedResponse<HandleItem>>> retriever =
            marker -> StorageImplUtils.applyOptionalTimeout(this.azureFileStorageClient.directorys()
                .listHandlesWithRestResponseAsync(shareName, directoryPath, marker, maxResultPerPage, null, snapshot,
                    recursive, context), timeout)
                .map(response -> new PagedResponseBase<>(response.getRequest(),
                    response.getStatusCode(),
                    response.getHeaders(),
                    response.getValue().getHandleList(),
                    response.getValue().getNextMarker(),
                    response.getDeserializedHeaders()));

        return new PagedFlux<>(() -> retriever.apply(null), retriever);
    }

    /**
     * Closes a handle on the directory. This is intended to be used alongside {@link #listHandles(Integer, boolean)}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Force close handles returned by list handles.</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryAsyncClient.forceCloseHandle#String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/force-close-handles">Azure Docs</a>.</p>
     *
     * @param handleId Handle ID to be closed.
     * @return A response that contains information about the closed handles.
     */
    public Mono<CloseHandlesInfo> forceCloseHandle(String handleId) {
        try {
            return withContext(context -> forceCloseHandleWithResponse(handleId, context)).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Closes a handle on the directory. This is intended to be used alongside {@link #listHandles(Integer, boolean)}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Force close handles returned by list handles.</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryAsyncClient.forceCloseHandleWithResponse#String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/force-close-handles">Azure Docs</a>.</p>
     *
     * @param handleId Handle ID to be closed.
     * @return A response that contains information about the closed handles along with headers and response status
     * code.
     */
    public Mono<Response<CloseHandlesInfo>> forceCloseHandleWithResponse(String handleId) {
        try {
            return withContext(context -> forceCloseHandleWithResponse(handleId, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<CloseHandlesInfo>> forceCloseHandleWithResponse(String handleId, Context context) {
        return this.azureFileStorageClient.directorys().forceCloseHandlesWithRestResponseAsync(shareName, directoryPath,
            handleId, null, null, snapshot, false, context)
            .map(response -> new SimpleResponse<>(response,
                new CloseHandlesInfo(response.getDeserializedHeaders().getNumberOfHandlesClosed())));
    }

    /**
     * Closes all handles opened on the directory at the service.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Force close all handles recursively.</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryAsyncClient.forceCloseAllHandles#boolean}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/force-close-handles">Azure Docs</a>.</p>
     *
     * @param recursive Flag indicating if the operation should apply to all subdirectories and files contained in the
     * directory.
     * @return A response that contains information about the closed handles.
     */
    public Mono<CloseHandlesInfo> forceCloseAllHandles(boolean recursive) {
        try {
            return withContext(context -> forceCloseAllHandlesWithTimeout(recursive, null, context)
                .reduce(new CloseHandlesInfo(0),
                    (accu, next) -> new CloseHandlesInfo(accu.getClosedHandles() + next.getClosedHandles())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    PagedFlux<CloseHandlesInfo> forceCloseAllHandlesWithTimeout(boolean recursive, Duration timeout, Context context) {
        Function<String, Mono<PagedResponse<CloseHandlesInfo>>> retriever =
            marker -> StorageImplUtils.applyOptionalTimeout(this.azureFileStorageClient.directorys()
                .forceCloseHandlesWithRestResponseAsync(shareName, directoryPath, "*", null, marker, snapshot,
                    recursive, context), timeout)
                .map(response -> new PagedResponseBase<>(response.getRequest(),
                    response.getStatusCode(),
                    response.getHeaders(),
                    Collections.singletonList(
                        new CloseHandlesInfo(response.getDeserializedHeaders().getNumberOfHandlesClosed())),
                    response.getDeserializedHeaders().getMarker(),
                    response.getDeserializedHeaders()));

        return new PagedFlux<>(() -> retriever.apply(null), retriever);
    }

    /**
     * Creates a subdirectory under current directory with specific name and returns a response of
     * ShareDirectoryAsyncClient to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the sub directory "subdir" </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryAsyncClient.createSubdirectory#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @param subdirectoryName Name of the subdirectory
     * @return A subdirectory client.
     * @throws ShareStorageException If the subdirectory has already existed, the parent directory does not exist or
     * directory is an invalid resource name.
     */
    public Mono<ShareDirectoryAsyncClient> createSubdirectory(String subdirectoryName) {
        try {
            return createSubdirectoryWithResponse(subdirectoryName, null, null, null)
                .flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a subdirectory under current directory with specific name , metadata and returns a response of
     * ShareDirectoryAsyncClient to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the subdirectory named "subdir", with metadata</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryAsyncClient.createSubdirectoryWithResponse#String-FileSmbProperties-String-Map}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @param subdirectoryName Name of the subdirectory
     * @param smbProperties The SMB properties of the directory.
     * @param filePermission The file permission of the directory.
     * @param metadata Optional metadata to associate with the subdirectory
     * @return A response containing the subdirectory client and the status of creating the directory.
     * @throws ShareStorageException If the directory has already existed, the parent directory does not exist or
     * subdirectory is an invalid resource name.
     */
    public Mono<Response<ShareDirectoryAsyncClient>> createSubdirectoryWithResponse(String subdirectoryName,
        FileSmbProperties smbProperties, String filePermission, Map<String, String> metadata) {
        try {
            return withContext(
                context -> createSubdirectoryWithResponse(subdirectoryName, smbProperties, filePermission,
                    metadata, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<ShareDirectoryAsyncClient>> createSubdirectoryWithResponse(String subdirectoryName,
        FileSmbProperties smbProperties, String filePermission, Map<String, String> metadata, Context context) {
        ShareDirectoryAsyncClient createSubClient = getSubdirectoryClient(subdirectoryName);
        return createSubClient.createWithResponse(smbProperties, filePermission, metadata, context)
            .map(response -> new SimpleResponse<>(response, createSubClient));
    }

    /**
     * Deletes the subdirectory with specific name in this directory.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the subdirectory named "subdir"</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryAsyncClient.deleteSubdirectory#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-directory">Azure Docs</a>.</p>
     *
     * @param subdirectoryName Name of the subdirectory
     * @return An empty response.
     * @throws ShareStorageException If the subdirectory doesn't exist, the parent directory does not exist or
     * subdirectory name is an invalid resource name.
     */
    public Mono<Void> deleteSubdirectory(String subdirectoryName) {
        try {
            return deleteSubdirectoryWithResponse(subdirectoryName).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes the subdirectory with specific name in this directory.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the subdirectory named "subdir"</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryAsyncClient.deleteSubdirectoryWithResponse#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-directory">Azure Docs</a>.</p>
     *
     * @param subdirectoryName Name of the subdirectory
     * @return A response that only contains headers and response status code
     * @throws ShareStorageException If the subdirectory doesn't exist, the parent directory does not exist or
     * subdirectory name is an invalid resource name.
     */
    public Mono<Response<Void>> deleteSubdirectoryWithResponse(String subdirectoryName) {
        try {
            return withContext(context -> deleteSubdirectoryWithResponse(subdirectoryName, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> deleteSubdirectoryWithResponse(String subdirectoryName, Context context) {
        ShareDirectoryAsyncClient deleteSubClient = getSubdirectoryClient(subdirectoryName);
        return deleteSubClient.deleteWithResponse(context);
    }

    /**
     * Creates a file in this directory with specific name, max number of results and returns a response of
     * ShareDirectoryInfo to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create 1k file with named "myFile"</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryAsyncClient.createFile#string-long}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-file">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file
     * @param maxSize Size of the file
     * @return The ShareFileAsyncClient.
     * @throws ShareStorageException If the file has already existed, the parent directory does not exist or file name
     * is an invalid resource name.
     */
    public Mono<ShareFileAsyncClient> createFile(String fileName, long maxSize) {
        try {
            return createFileWithResponse(fileName, maxSize, null, null, null, null)
                .flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a file in this directory with specific name and returns a response of ShareDirectoryInfo to
     * interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the file named "myFile"</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryAsyncClient.createFileWithResponse#com.azure.storage.file.share.ShareDirectoryAsyncClient.createFileWithResponse#String-long-ShareFileHttpHeaders-FileSmbProperties-String-Map}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-file">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file
     * @param maxSize Max size of the file
     * @param httpHeaders The user settable file http headers.
     * @param smbProperties The user settable file smb properties.
     * @param filePermission The file permission of the file.
     * @param metadata Optional name-value pairs associated with the file as metadata.
     * @return A response containing the directory info and the status of creating the directory.
     * @throws ShareStorageException If the directory has already existed, the parent directory does not exist or file
     * name is an invalid resource name.
     */
    public Mono<Response<ShareFileAsyncClient>> createFileWithResponse(String fileName, long maxSize,
        ShareFileHttpHeaders httpHeaders, FileSmbProperties smbProperties, String filePermission,
        Map<String, String> metadata) {
        try {
            return withContext(context ->
                createFileWithResponse(fileName, maxSize, httpHeaders, smbProperties, filePermission, metadata,
                    context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<ShareFileAsyncClient>> createFileWithResponse(String fileName, long maxSize,
        ShareFileHttpHeaders httpHeaders, FileSmbProperties smbProperties, String filePermission,
        Map<String, String> metadata, Context context) {
        ShareFileAsyncClient shareFileAsyncClient = getFileClient(fileName);
        return shareFileAsyncClient
            .createWithResponse(maxSize, httpHeaders, smbProperties, filePermission, metadata, context)
            .map(response -> new SimpleResponse<>(response, shareFileAsyncClient));
    }

    /**
     * Deletes the file with specific name in this directory.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the file "filetest"</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryAsyncClient.deleteFile#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-file2">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file
     * @return An empty response.
     * @throws ShareStorageException If the directory doesn't exist or the file doesn't exist or file name is an invalid
     * resource name.
     */
    public Mono<Void> deleteFile(String fileName) {
        try {
            return deleteFileWithResponse(fileName).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes the file with specific name in this directory.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the file "filetest"</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryAsyncClient.deleteFileWithResponse#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-file2">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file
     * @return A response that only contains headers and response status code
     * @throws ShareStorageException If the directory doesn't exist or the file doesn't exist or file name is an invalid
     * resource name.
     */
    public Mono<Response<Void>> deleteFileWithResponse(String fileName) {
        try {
            return withContext(context -> deleteFileWithResponse(fileName, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> deleteFileWithResponse(String fileName, Context context) {
        ShareFileAsyncClient shareFileAsyncClient = getFileClient(fileName);
        return shareFileAsyncClient.deleteWithResponse(context);
    }

    /**
     * Get snapshot id which attached to {@link ShareDirectoryAsyncClient}. Return {@code null} if no snapshot id
     * attached.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Get the share snapshot id. </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryAsyncClient.getShareSnapshotId}
     *
     * @return The snapshot id which is a unique {@code DateTime} value that identifies the share snapshot to its base
     * share.
     */
    public String getShareSnapshotId() {
        return this.snapshot;
    }

    /**
     * Get the share name of directory client.
     *
     * <p>Get the share name. </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryAsyncClient.getShareName}
     *
     * @return The share name of the directory.
     */
    public String getShareName() {
        return shareName;
    }

    /**
     * Get directory path of the client.
     *
     * <p>Get directory path. </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryAsyncClient.getDirectoryPath}
     *
     * @return The path of the directory.
     */
    public String getDirectoryPath() {
        return directoryPath;
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
        return azureFileStorageClient.getHttpPipeline();
    }

    /**
     * Generates a service SAS for the directory using the specified {@link ShareServiceSasSignatureValues}
     * Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link ShareServiceSasSignatureValues} for more information on how to construct a service SAS.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryAsyncClient.generateSas#ShareServiceSasSignatureValues}
     *
     * @param shareServiceSasSignatureValues {@link ShareServiceSasSignatureValues}
     *
     * @return A {@code String} representing all SAS query parameters.
     */
    public String generateSas(ShareServiceSasSignatureValues shareServiceSasSignatureValues) {
        return new ShareSasImplUtil(shareServiceSasSignatureValues, getShareName(), getDirectoryPath())
            .generateSas(SasImplUtils.extractSharedKeyCredential(getHttpPipeline()));
    }

    private Response<ShareDirectoryInfo> createWithRestResponse(final DirectorysCreateResponse response) {
        String eTag = response.getDeserializedHeaders().getETag();
        OffsetDateTime lastModified = response.getDeserializedHeaders().getLastModified();
        FileSmbProperties smbProperties = new FileSmbProperties(response.getHeaders());
        ShareDirectoryInfo shareDirectoryInfo = new ShareDirectoryInfo(eTag, lastModified, smbProperties);
        return new SimpleResponse<>(response, shareDirectoryInfo);
    }

    private Response<ShareDirectoryProperties> getPropertiesResponse(DirectorysGetPropertiesResponse response) {
        Map<String, String> metadata = response.getDeserializedHeaders().getMetadata();
        String eTag = response.getDeserializedHeaders().getETag();
        OffsetDateTime offsetDateTime = response.getDeserializedHeaders().getLastModified();
        boolean isServerEncrypted = response.getDeserializedHeaders().isServerEncrypted();
        FileSmbProperties smbProperties = new FileSmbProperties(response.getHeaders());
        ShareDirectoryProperties shareDirectoryProperties =
            new ShareDirectoryProperties(metadata, eTag, offsetDateTime, isServerEncrypted, smbProperties);
        return new SimpleResponse<>(response, shareDirectoryProperties);
    }

    private Response<ShareDirectoryInfo> setPropertiesResponse(final DirectorysSetPropertiesResponse response) {
        String eTag = response.getDeserializedHeaders().getETag();
        OffsetDateTime lastModified = response.getDeserializedHeaders().getLastModified();
        FileSmbProperties smbProperties = new FileSmbProperties(response.getHeaders());
        ShareDirectoryInfo shareDirectoryInfo = new ShareDirectoryInfo(eTag, lastModified, smbProperties);
        return new SimpleResponse<>(response, shareDirectoryInfo);
    }

    private Response<ShareDirectorySetMetadataInfo> setMetadataResponse(final DirectorysSetMetadataResponse response) {
        String eTag = response.getDeserializedHeaders().getETag();
        boolean isServerEncrypted = response.getDeserializedHeaders().isServerEncrypted();
        ShareDirectorySetMetadataInfo shareDirectorySetMetadataInfo = new ShareDirectorySetMetadataInfo(eTag,
            isServerEncrypted);
        return new SimpleResponse<>(response, shareDirectorySetMetadataInfo);
    }

    private List<ShareFileItem> convertResponseAndGetNumOfResults(
        DirectorysListFilesAndDirectoriesSegmentResponse response) {
        Set<ShareFileItem> shareFileItems = new TreeSet<>(Comparator.comparing(ShareFileItem::getName));
        if (response.getValue().getSegment() != null) {
            response.getValue().getSegment().getDirectoryItems()
                .forEach(directoryItem -> shareFileItems.add(new ShareFileItem(directoryItem.getName(),
                    true, null)));
            response.getValue().getSegment().getFileItems()
                .forEach(fileItem -> shareFileItems.add(new ShareFileItem(fileItem.getName(), false,
                    fileItem.getProperties().getContentLength())));
        }

        return new ArrayList<>(shareFileItems);
    }

    /**
     * Verifies that the file permission and file permission key are not both set and if the file permission is set,
     * the file permission is of valid length.
     * @param filePermission The file permission.
     * @param filePermissionKey The file permission key.
     * @throws IllegalArgumentException for invalid file permission or file permission keys.
     */
    private void validateFilePermissionAndKey(String filePermission, String  filePermissionKey) {
        if (filePermission != null && filePermissionKey != null) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                FileConstants.MessageConstants.FILE_PERMISSION_FILE_PERMISSION_KEY_INVALID));
        }

        if (filePermission != null) {
            StorageImplUtils.assertInBounds("filePermission",
                filePermission.getBytes(StandardCharsets.UTF_8).length, 0, 8 * Constants.KB);
        }
    }
}
