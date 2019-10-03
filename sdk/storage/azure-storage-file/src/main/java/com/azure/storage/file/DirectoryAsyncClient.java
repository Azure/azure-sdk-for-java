// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

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
import com.azure.storage.common.Utility;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.file.implementation.AzureFileStorageImpl;
import com.azure.storage.file.implementation.models.DirectorysCreateResponse;
import com.azure.storage.file.implementation.models.DirectorysGetPropertiesResponse;
import com.azure.storage.file.implementation.models.DirectorysListFilesAndDirectoriesSegmentResponse;
import com.azure.storage.file.implementation.models.DirectorysSetMetadataResponse;
import com.azure.storage.file.implementation.models.DirectorysSetPropertiesResponse;
import com.azure.storage.file.models.DirectoryInfo;
import com.azure.storage.file.models.DirectoryProperties;
import com.azure.storage.file.models.DirectorySetMetadataInfo;
import com.azure.storage.file.models.FileHTTPHeaders;
import com.azure.storage.file.models.FileReference;
import com.azure.storage.file.models.HandleItem;
import com.azure.storage.file.models.StorageException;
import reactor.core.publisher.Mono;

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

import static com.azure.core.implementation.util.FluxUtil.withContext;
import static com.azure.storage.file.FileExtensions.filePermissionAndKeyHelper;
import static com.azure.storage.file.PostProcessor.postProcessResponse;

/**
 * This class provides a client that contains all the operations for interacting with directory in Azure Storage File
 * Service. Operations allowed by the client are creating, deleting and listing subdirectory and file, retrieving
 * properties, setting metadata and list or force close handles of the directory or file.
 *
 * <p><strong>Instantiating an Asynchronous Directory Client</strong></p>
 *
 * {@codesnippet com.azure.storage.file.directoryAsyncClient.instantiation}
 *
 * <p>View {@link FileClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see FileClientBuilder
 * @see DirectoryClient
 * @see SharedKeyCredential
 * @see SASTokenCredential
 */
@ServiceClient(builder = FileClientBuilder.class, isAsync = true)
public class DirectoryAsyncClient {
    private final ClientLogger logger = new ClientLogger(DirectoryAsyncClient.class);

    private final AzureFileStorageImpl azureFileStorageClient;
    private final String shareName;
    private final String directoryPath;
    private final String snapshot;

    /**
     * Creates a DirectoryAsyncClient that sends requests to the storage directory at {@link
     * AzureFileStorageImpl#getUrl() endpoint}. Each service call goes through the {@link HttpPipeline pipeline} in the
     * {@code client}.
     *
     * @param azureFileStorageClient Client that interacts with the service interfaces
     * @param shareName Name of the share
     * @param directoryPath Name of the directory
     * @param snapshot The snapshot of the share
     */
    DirectoryAsyncClient(AzureFileStorageImpl azureFileStorageClient, String shareName, String directoryPath,
        String snapshot) {
        Objects.requireNonNull(shareName);
        Objects.requireNonNull(directoryPath);
        this.shareName = shareName;
        this.directoryPath = directoryPath;
        this.snapshot = snapshot;
        this.azureFileStorageClient = azureFileStorageClient;
    }

    /**
     * Get the url of the storage directory client.
     *
     * @return the URL of the storage directory client
     */
    public String getDirectoryUrl() {
        StringBuilder directoryURLString = new StringBuilder(azureFileStorageClient.getUrl()).append("/")
            .append(shareName).append("/").append(directoryPath);
        if (snapshot != null) {
            directoryURLString.append("?snapshot=").append(snapshot);
        }
        return directoryURLString.toString();
    }

    /**
     * Constructs a FileAsyncClient that interacts with the specified file.
     *
     * <p>If the file doesn't exist in this directory {@link FileAsyncClient#create(long)} create} in the client will
     * need to be called before interaction with the file can happen.</p>
     *
     * @param fileName Name of the file
     * @return a FileAsyncClient that interacts with the specified share
     */
    public FileAsyncClient getFileClient(String fileName) {
        String filePath = directoryPath + "/" + fileName;
        return new FileAsyncClient(azureFileStorageClient, shareName, filePath, null);
    }

    /**
     * Constructs a DirectoryAsyncClient that interacts with the specified directory.
     *
     * <p>If the file doesn't exist in this directory {@link DirectoryAsyncClient#create()} create} in the client will
     * need to be called before interaction with the directory can happen.</p>
     *
     * @param subDirectoryName Name of the directory
     * @return a DirectoryAsyncClient that interacts with the specified directory
     */
    public DirectoryAsyncClient getSubDirectoryClient(String subDirectoryName) {
        String directoryPath = this.directoryPath + "/" + subDirectoryName;
        return new DirectoryAsyncClient(azureFileStorageClient, shareName, directoryPath, snapshot);
    }

    /**
     * Creates this directory in the file share and returns a response of {@link DirectoryInfo} to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the directory</p>
     *
     * {@codesnippet com.azure.storage.file.directoryAsyncClient.create}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @return The {@link DirectoryInfo directory info}.
     * @throws StorageException If the directory has already existed, the parent directory does not exist or directory
     * name is an invalid resource name.
     */
    public Mono<DirectoryInfo> create() {
        return createWithResponse(null, null, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a directory in the file share and returns a response of DirectoryInfo to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the directory</p>
     *
     * {@codesnippet com.azure.storage.file.directoryAsyncClient.createWithResponse#filesmbproperties-string-map}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @param smbProperties The SMB properties of the directory.
     * @param filePermission The file permission of the directory.
     * @param metadata Optional metadata to associate with the directory
     * @return A response containing the directory info and the status of creating the directory.
     * @throws StorageException If the directory has already existed, the parent directory does not exist or directory
     * name is an invalid resource name.
     */
    public Mono<Response<DirectoryInfo>> createWithResponse(FileSmbProperties smbProperties, String filePermission,
        Map<String, String> metadata) {
        return withContext(context -> createWithResponse(smbProperties, filePermission, metadata, context));
    }

    Mono<Response<DirectoryInfo>> createWithResponse(FileSmbProperties smbProperties, String filePermission,
        Map<String, String> metadata, Context context) {
        FileSmbProperties properties = smbProperties == null ? new FileSmbProperties() : smbProperties;

        // Checks that file permission and file permission key are valid
        filePermissionAndKeyHelper(filePermission, properties.getFilePermissionKey());

        // If file permission and file permission key are both not set then set default value
        filePermission = properties.setFilePermission(filePermission, FileConstants.FILE_PERMISSION_INHERIT);
        String filePermissionKey = properties.getFilePermissionKey();

        String fileAttributes = properties.setNtfsFileAttributes(FileConstants.FILE_ATTRIBUTES_NONE);
        String fileCreationTime = properties.setFileCreationTime(FileConstants.FILE_TIME_NOW);
        String fileLastWriteTime = properties.setFileLastWriteTime(FileConstants.FILE_TIME_NOW);

        return postProcessResponse(azureFileStorageClient.directorys()
            .createWithRestResponseAsync(shareName, directoryPath, fileAttributes, fileCreationTime, fileLastWriteTime,
                null, metadata, filePermission, filePermissionKey, context))
            .map(this::createWithRestResponse);
    }

    /**
     * Deletes the directory in the file share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the directory</p>
     *
     * {@codesnippet com.azure.storage.file.directoryAsyncClient.delete}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-directory">Azure Docs</a>.</p>
     *
     * @return An empty response.
     * @throws StorageException If the share doesn't exist
     */
    public Mono<Void> delete() {
        return deleteWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes the directory in the file share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the directory</p>
     *
     * {@codesnippet com.azure.storage.file.directoryAsyncClient.deleteWithResponse}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-directory">Azure Docs</a>.</p>
     *
     * @return A response that only contains headers and response status code
     * @throws StorageException If the share doesn't exist
     */
    public Mono<Response<Void>> deleteWithResponse() {
        return withContext(this::deleteWithResponse);
    }

    Mono<Response<Void>> deleteWithResponse(Context context) {
        return postProcessResponse(azureFileStorageClient.directorys()
            .deleteWithRestResponseAsync(shareName, directoryPath, context))
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
     * {@codesnippet com.azure.storage.file.directoryAsyncClient.getProperties}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-directory-properties">Azure Docs</a>.</p>
     *
     * @return Storage directory properties
     */
    public Mono<DirectoryProperties> getProperties() {
        return getPropertiesWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Retrieves the properties of this directory. The properties includes directory metadata, last modified date, is
     * server encrypted, and eTag.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve directory properties</p>
     *
     * {@codesnippet com.azure.storage.file.directoryAsyncClient.getPropertiesWithResponse}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-directory-properties">Azure Docs</a>.</p>
     *
     * @return A response containing the storage directory properties with headers and response status code
     */
    public Mono<Response<DirectoryProperties>> getPropertiesWithResponse() {
        return withContext(this::getPropertiesWithResponse);
    }

    Mono<Response<DirectoryProperties>> getPropertiesWithResponse(Context context) {
        return postProcessResponse(azureFileStorageClient.directorys()
            .getPropertiesWithRestResponseAsync(shareName, directoryPath, snapshot, null, context))
            .map(this::getPropertiesResponse);
    }

    /**
     * Sets the properties of this directory. The properties include the file SMB properties and the file permission.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set directory properties</p>
     *
     * {@codesnippet com.azure.storage.file.directoryAsyncClient.setProperties#filesmbproperties-string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-directory-properties">Azure Docs</a>.</p>
     *
     * @param smbProperties The SMB properties of the directory.
     * @param filePermission The file permission of the directory.
     * @return The storage directory SMB properties
     */
    public Mono<DirectoryInfo> setProperties(FileSmbProperties smbProperties, String filePermission) {
        return setPropertiesWithResponse(smbProperties, filePermission).flatMap(FluxUtil::toMono);
    }

    /**
     * Sets the properties of this directory. The properties include the file SMB properties and the file permission.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set directory properties</p>
     *
     * {@codesnippet com.azure.storage.file.directoryAsyncClient.setPropertiesWithResponse#filesmbproperties-string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-directory-properties">Azure Docs</a>.</p>
     *
     * @param smbProperties The SMB properties of the directory.
     * @param filePermission The file permission of the directory.
     * @return A response containing the storage directory smb properties with headers and response status code
     */
    public Mono<Response<DirectoryInfo>> setPropertiesWithResponse(FileSmbProperties smbProperties,
        String filePermission) {
        return withContext(context -> setPropertiesWithResponse(smbProperties, filePermission, Context.NONE));
    }

    Mono<Response<DirectoryInfo>> setPropertiesWithResponse(FileSmbProperties smbProperties, String filePermission,
        Context context) {

        FileSmbProperties properties = smbProperties == null ? new FileSmbProperties() : smbProperties;

        // Checks that file permission and file permission key are valid
        filePermissionAndKeyHelper(filePermission, properties.getFilePermissionKey());

        // If file permission and file permission key are both not set then set default value
        filePermission = properties.setFilePermission(filePermission, FileConstants.PRESERVE);
        String filePermissionKey = properties.getFilePermissionKey();

        String fileAttributes = properties.setNtfsFileAttributes(FileConstants.PRESERVE);
        String fileCreationTime = properties.setFileCreationTime(FileConstants.PRESERVE);
        String fileLastWriteTime = properties.setFileLastWriteTime(FileConstants.PRESERVE);

        return postProcessResponse(azureFileStorageClient.directorys()
            .setPropertiesWithRestResponseAsync(shareName, directoryPath, fileAttributes, fileCreationTime,
                fileLastWriteTime, null, filePermission, filePermissionKey, context)
            .map(this::setPropertiesResponse));
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
     * {@codesnippet com.azure.storage.file.directoryAsyncClient.setMetadata#map}
     *
     * <p>Clear the metadata of the directory</p>
     *
     * {@codesnippet com.azure.storage.file.directoryAsyncClient.setMetadata#map.clearMetadata}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-directory-metadata">Azure Docs</a>.</p>
     *
     * @param metadata Optional metadata to set on the directory, if null is passed the metadata for the directory is
     * cleared
     * @return information about the directory
     * @throws StorageException If the directory doesn't exist or the metadata contains invalid keys
     */
    public Mono<DirectorySetMetadataInfo> setMetadata(Map<String, String> metadata) {
        return setMetadataWithResponse(metadata).flatMap(FluxUtil::toMono);
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
     * {@codesnippet com.azure.storage.file.directoryAsyncClient.setMetadataWithResponse#map}
     *
     * <p>Clear the metadata of the directory</p>
     *
     * {@codesnippet com.azure.storage.file.directoryAsyncClient.setMetadataWithResponse#map.clearMetadata}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-directory-metadata">Azure Docs</a>.</p>
     *
     * @param metadata Optional metadata to set on the directory, if null is passed the metadata for the directory is
     * cleared
     * @return A response containing the information about the directory with headers and response status code
     * @throws StorageException If the directory doesn't exist or the metadata contains invalid keys
     */
    public Mono<Response<DirectorySetMetadataInfo>> setMetadataWithResponse(Map<String, String> metadata) {
        return withContext(context -> setMetadataWithResponse(metadata, context));
    }

    Mono<Response<DirectorySetMetadataInfo>> setMetadataWithResponse(Map<String, String> metadata, Context context) {
        return postProcessResponse(azureFileStorageClient.directorys()
            .setMetadataWithRestResponseAsync(shareName, directoryPath, null, metadata, context))
            .map(this::setMetadataResponse);
    }

    /**
     * Lists all sub-directories and files in this directory without their prefix or maxResult.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all sub-directories and files in the account</p>
     *
     * {@codesnippet com.azure.storage.file.directoryAsyncClient.listFilesAndDirectories}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/list-directories-and-files">Azure
     * Docs</a>.</p>
     *
     * @return {@link FileReference File info} in the storage directory
     */
    public PagedFlux<FileReference> listFilesAndDirectories() {
        return listFilesAndDirectories(null, null);
    }

    /**
     * Lists all sub-directories and files in this directory with their prefix or snapshots.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all sub-directories with "subdir" prefix and return 10 results in the account</p>
     *
     * {@codesnippet com.azure.storage.file.directoryAsyncClient.listFilesAndDirectories#string-integer}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/list-directories-and-files">Azure
     * Docs</a>.</p>
     *
     * @param prefix Optional prefix which filters the results to return only files and directories whose name begins
     * with.
     * @param maxResults Optional maximum number of files and/or directories to return per page. If the request does not
     * specify maxresults or specifies a value greater than 5,000, the server will return up to 5,000 items.
     * @return {@link FileReference File info} in this directory with prefix and max number of return results.
     */
    public PagedFlux<FileReference> listFilesAndDirectories(String prefix, Integer maxResults) {
        return listFilesAndDirectoriesWithOptionalTimeout(prefix, maxResults, null, Context.NONE);
    }

    PagedFlux<FileReference> listFilesAndDirectoriesWithOptionalTimeout(String prefix, Integer maxResults,
                                                                        Duration timeout, Context context) {
        Function<String, Mono<PagedResponse<FileReference>>> retriever =
            marker -> postProcessResponse(Utility.applyOptionalTimeout(this.azureFileStorageClient.directorys()
                .listFilesAndDirectoriesSegmentWithRestResponseAsync(shareName, directoryPath, prefix, snapshot,
                    marker, maxResults, null, context), timeout)
                .map(response -> new PagedResponseBase<>(response.getRequest(),
                    response.getStatusCode(),
                    response.getHeaders(),
                    convertResponseAndGetNumOfResults(response),
                    response.getValue().getNextMarker(),
                    response.getDeserializedHeaders())));

        return new PagedFlux<>(() -> retriever.apply(null), retriever);
    }

    /**
     * List of open handles on a directory or a file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Get 10 handles with recursive call.</p>
     *
     * {@codesnippet com.azure.storage.file.directoryAsyncClient.listHandles#integer-boolean}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/list-handles">Azure Docs</a>.</p>
     *
     * @param maxResult Optional maximum number of results will return per page
     * @param recursive Specifies operation should apply to the directory specified in the URI, its files, its
     * subdirectories and their files.
     * @return {@link HandleItem handles} in the directory that satisfy the requirements
     */
    public PagedFlux<HandleItem> listHandles(Integer maxResult, boolean recursive) {
        return listHandlesWithOptionalTimeout(maxResult, recursive, null, Context.NONE);
    }

    PagedFlux<HandleItem> listHandlesWithOptionalTimeout(Integer maxResult, boolean recursive, Duration timeout,
        Context context) {
        Function<String, Mono<PagedResponse<HandleItem>>> retriever =
            marker -> postProcessResponse(Utility.applyOptionalTimeout(this.azureFileStorageClient.directorys()
                .listHandlesWithRestResponseAsync(shareName, directoryPath, marker, maxResult, null, snapshot,
                    recursive, context), timeout)
                .map(response -> new PagedResponseBase<>(response.getRequest(),
                    response.getStatusCode(),
                    response.getHeaders(),
                    response.getValue().getHandleList(),
                    response.getValue().getNextMarker(),
                    response.getDeserializedHeaders())));

        return new PagedFlux<>(() -> retriever.apply(null), retriever);
    }

    /**
     * Closes a handle or handles opened on a directory or a file at the service. It is intended to be used alongside
     * {@link DirectoryAsyncClient#listHandles(Integer, boolean)} .
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Force close handles with handles returned by get handles in recursive.</p>
     *
     * {@codesnippet com.azure.storage.file.directoryAsyncClient.forceCloseHandles}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/force-close-handles">Azure Docs</a>.</p>
     *
     * @param handleId Specifies the handle ID to be closed. Use an asterisk ('*') as a wildcard string to specify all
     * handles.
     * @param recursive A boolean value that specifies if the operation should also apply to the files and
     * subdirectories of the directory specified in the URI.
     * @return The counts of number of handles closed
     */
    public PagedFlux<Integer> forceCloseHandles(String handleId, boolean recursive) {
        return forceCloseHandlesWithOptionalTimeout(handleId, recursive, null, Context.NONE);
    }

    PagedFlux<Integer> forceCloseHandlesWithOptionalTimeout(String handleId, boolean recursive, Duration timeout,
        Context context) {
        Function<String, Mono<PagedResponse<Integer>>> retriever =
            marker -> postProcessResponse(Utility.applyOptionalTimeout(this.azureFileStorageClient.directorys()
                .forceCloseHandlesWithRestResponseAsync(shareName, directoryPath, handleId, null, marker, snapshot,
                    recursive, context), timeout)
                .map(response -> new PagedResponseBase<>(response.getRequest(),
                    response.getStatusCode(),
                    response.getHeaders(),
                    Collections.singletonList(response.getDeserializedHeaders().getNumberOfHandlesClosed()),
                    response.getDeserializedHeaders().getMarker(),
                    response.getDeserializedHeaders())));

        return new PagedFlux<>(() -> retriever.apply(null), retriever);
    }

    /**
     * Creates a subdirectory under current directory with specific name and returns a response of DirectoryAsyncClient
     * to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the sub directory "subdir" </p>
     *
     * {@codesnippet com.azure.storage.file.directoryAsyncClient.createSubDirectory#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @param subDirectoryName Name of the subdirectory
     * @return A subdirectory client.
     * @throws StorageException If the subdirectory has already existed, the parent directory does not exist or
     * directory is an invalid resource name.
     */
    public Mono<DirectoryAsyncClient> createSubDirectory(String subDirectoryName) {
        return createSubDirectoryWithResponse(subDirectoryName, null, null, null)
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a subdirectory under current directory with specific name , metadata and returns a response of
     * DirectoryAsyncClient to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the subdirectory named "subdir", with metadata</p>
     *
     * {@codesnippet com.azure.storage.file.directoryAsyncClient.createSubDirectoryWithResponse#string-filesmbproperties-string-map}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @param subDirectoryName Name of the subdirectory
     * @param smbProperties The SMB properties of the directory.
     * @param filePermission The file permission of the directory.
     * @param metadata Optional metadata to associate with the subdirectory
     * @return A response containing the subdirectory client and the status of creating the directory.
     * @throws StorageException If the directory has already existed, the parent directory does not exist or
     * subdirectory is an invalid resource name.
     */
    public Mono<Response<DirectoryAsyncClient>> createSubDirectoryWithResponse(String subDirectoryName,
        FileSmbProperties smbProperties, String filePermission, Map<String, String> metadata) {
        return withContext(context -> createSubDirectoryWithResponse(subDirectoryName, smbProperties, filePermission,
            metadata, context));
    }

    Mono<Response<DirectoryAsyncClient>> createSubDirectoryWithResponse(String subDirectoryName,
        FileSmbProperties smbProperties, String filePermission, Map<String, String> metadata, Context context) {
        DirectoryAsyncClient createSubClient = getSubDirectoryClient(subDirectoryName);
        return postProcessResponse(createSubClient.createWithResponse(smbProperties, filePermission, metadata, context))
            .map(response -> new SimpleResponse<>(response, createSubClient));
    }

    /**
     * Deletes the subdirectory with specific name in this directory.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the subdirectory named "subdir"</p>
     *
     * {@codesnippet com.azure.storage.file.directoryAsyncClient.deleteSubDirectory#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-directory">Azure Docs</a>.</p>
     *
     * @param subDirectoryName Name of the subdirectory
     * @return An empty response.
     * @throws StorageException If the subdirectory doesn't exist, the parent directory does not exist or subdirectory
     * name is an invalid resource name.
     */
    public Mono<Void> deleteSubDirectory(String subDirectoryName) {
        return deleteSubDirectoryWithResponse(subDirectoryName).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes the subdirectory with specific name in this directory.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the subdirectory named "subdir"</p>
     *
     * {@codesnippet com.azure.storage.file.directoryAsyncClient.deleteSubDirectoryWithResponse#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-directory">Azure Docs</a>.</p>
     *
     * @param subDirectoryName Name of the subdirectory
     * @return A response that only contains headers and response status code
     * @throws StorageException If the subdirectory doesn't exist, the parent directory does not exist or subdirectory
     * name is an invalid resource name.
     */
    public Mono<Response<Void>> deleteSubDirectoryWithResponse(String subDirectoryName) {
        return withContext(context -> deleteSubDirectoryWithResponse(subDirectoryName, context));
    }

    Mono<Response<Void>> deleteSubDirectoryWithResponse(String subDirectoryName, Context context) {
        DirectoryAsyncClient deleteSubClient = getSubDirectoryClient(subDirectoryName);
        return postProcessResponse(deleteSubClient.deleteWithResponse(context));
    }

    /**
     * Creates a file in this directory with specific name, max number of results and returns a response of
     * DirectoryInfo to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create 1k file with named "myFile"</p>
     *
     * {@codesnippet com.azure.storage.file.directoryAsyncClient.createFile#string-long}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-file">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file
     * @param maxSize Size of the file
     * @return The FileAsyncClient.
     * @throws StorageException If the file has already existed, the parent directory does not exist or file name is an
     * invalid resource name.
     */
    public Mono<FileAsyncClient> createFile(String fileName, long maxSize) {
        return createFileWithResponse(fileName, maxSize, null, null, null, null)
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a file in this directory with specific name and returns a response of DirectoryInfo to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the file named "myFile"</p>
     *
     * {@codesnippet com.azure.storage.file.directoryAsyncClient.createFileWithResponse#string-long-filehttpheaders-filesmbproperties-string-map}
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
     * @throws StorageException If the directory has already existed, the parent directory does not exist or file name
     * is an invalid resource name.
     */
    public Mono<Response<FileAsyncClient>> createFileWithResponse(String fileName, long maxSize,
        FileHTTPHeaders httpHeaders, FileSmbProperties smbProperties, String filePermission,
        Map<String, String> metadata) {
        return withContext(context ->
            createFileWithResponse(fileName, maxSize, httpHeaders, smbProperties, filePermission, metadata, context));
    }

    Mono<Response<FileAsyncClient>> createFileWithResponse(String fileName, long maxSize, FileHTTPHeaders httpHeaders,
        FileSmbProperties smbProperties, String filePermission, Map<String, String> metadata, Context context) {
        FileAsyncClient fileAsyncClient = getFileClient(fileName);
        return postProcessResponse(fileAsyncClient
            .createWithResponse(maxSize, httpHeaders, smbProperties, filePermission, metadata, context))
            .map(response -> new SimpleResponse<>(response, fileAsyncClient));
    }

    /**
     * Deletes the file with specific name in this directory.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the file "filetest"</p>
     *
     * {@codesnippet com.azure.storage.file.directoryAsyncClient.deleteFile#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-file2">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file
     * @return An empty response.
     * @throws StorageException If the directory doesn't exist or the file doesn't exist or file name is an invalid
     * resource name.
     */
    public Mono<Void> deleteFile(String fileName) {
        return deleteFileWithResponse(fileName).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes the file with specific name in this directory.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the file "filetest"</p>
     *
     * {@codesnippet com.azure.storage.file.directoryAsyncClient.deleteFileWithResponse#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-file2">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file
     * @return A response that only contains headers and response status code
     * @throws StorageException If the directory doesn't exist or the file doesn't exist or file name is an invalid
     * resource name.
     */
    public Mono<Response<Void>> deleteFileWithResponse(String fileName) {
        return withContext(context -> deleteFileWithResponse(fileName, context));
    }

    Mono<Response<Void>> deleteFileWithResponse(String fileName, Context context) {
        FileAsyncClient fileAsyncClient = getFileClient(fileName);
        return postProcessResponse(fileAsyncClient.deleteWithResponse(context));
    }

    /**
     * Get snapshot id which attached to {@link DirectoryAsyncClient}. Return {@code null} if no snapshot id attached.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Get the share snapshot id. </p>
     *
     * {@codesnippet com.azure.storage.file.directoryAsyncClient.getShareSnapshotId}
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
     * {@codesnippet com.azure.storage.file.directoryAsyncClient.getShareName}
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
     * {@codesnippet com.azure.storage.file.directoryAsyncClient.getDirectoryPath}
     *
     * @return The path of the directory.
     */
    public String getDirectoryPath() {
        return directoryPath;
    }

    private Response<DirectoryInfo> createWithRestResponse(final DirectorysCreateResponse response) {
        String eTag = response.getDeserializedHeaders().getETag();
        OffsetDateTime lastModified = response.getDeserializedHeaders().getLastModified();
        FileSmbProperties smbProperties = new FileSmbProperties(response.getHeaders());
        DirectoryInfo directoryInfo = new DirectoryInfo(eTag, lastModified, smbProperties);
        return new SimpleResponse<>(response, directoryInfo);
    }

    private Response<DirectoryProperties> getPropertiesResponse(DirectorysGetPropertiesResponse response) {
        Map<String, String> metadata = response.getDeserializedHeaders().getMetadata();
        String eTag = response.getDeserializedHeaders().getETag();
        OffsetDateTime offsetDateTime = response.getDeserializedHeaders().getLastModified();
        boolean isServerEncrypted = response.getDeserializedHeaders().isServerEncrypted();
        FileSmbProperties smbProperties = new FileSmbProperties(response.getHeaders());
        DirectoryProperties directoryProperties =
            new DirectoryProperties(metadata, eTag, offsetDateTime, isServerEncrypted, smbProperties);
        return new SimpleResponse<>(response, directoryProperties);
    }

    private Response<DirectoryInfo> setPropertiesResponse(final DirectorysSetPropertiesResponse response) {
        String eTag = response.getDeserializedHeaders().getETag();
        OffsetDateTime lastModified = response.getDeserializedHeaders().getLastModified();
        FileSmbProperties smbProperties = new FileSmbProperties(response.getHeaders());
        DirectoryInfo directoryInfo = new DirectoryInfo(eTag, lastModified, smbProperties);
        return new SimpleResponse<>(response, directoryInfo);
    }

    private Response<DirectorySetMetadataInfo> setMetadataResponse(final DirectorysSetMetadataResponse response) {
        String eTag = response.getDeserializedHeaders().getETag();
        boolean isServerEncrypted = response.getDeserializedHeaders().isServerEncrypted();
        DirectorySetMetadataInfo directorySetMetadataInfo = new DirectorySetMetadataInfo(eTag, isServerEncrypted);
        return new SimpleResponse<>(response, directorySetMetadataInfo);
    }

    private List<FileReference> convertResponseAndGetNumOfResults(
        DirectorysListFilesAndDirectoriesSegmentResponse response) {
        Set<FileReference> fileReferences = new TreeSet<>(Comparator.comparing(FileReference::getName));
        if (response.getValue().getSegment() != null) {
            response.getValue().getSegment().getDirectoryItems()
                .forEach(directoryItem -> fileReferences.add(new FileReference(directoryItem.getName(),
                    true, null)));
            response.getValue().getSegment().getFileItems()
                .forEach(fileItem -> fileReferences.add(new FileReference(fileItem.getName(), false,
                    fileItem.getProperties())));
        }

        return new ArrayList<>(fileReferences);
    }
}
