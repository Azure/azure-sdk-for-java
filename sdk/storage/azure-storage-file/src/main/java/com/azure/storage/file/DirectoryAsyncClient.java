// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.util.Context;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.file.implementation.AzureFileStorageBuilder;
import com.azure.storage.file.implementation.AzureFileStorageImpl;
import com.azure.storage.file.models.DirectoryInfo;
import com.azure.storage.file.models.DirectoryProperties;
import com.azure.storage.file.models.DirectorySetMetadataInfo;
import com.azure.storage.file.models.DirectorysCreateResponse;
import com.azure.storage.file.models.DirectorysForceCloseHandlesResponse;
import com.azure.storage.file.models.DirectorysGetPropertiesResponse;
import com.azure.storage.file.models.DirectorysListFilesAndDirectoriesSegmentResponse;
import com.azure.storage.file.models.DirectorysListHandlesResponse;
import com.azure.storage.file.models.DirectorysSetMetadataResponse;
import com.azure.storage.file.models.FileHTTPHeaders;
import com.azure.storage.file.models.FileRef;
import com.azure.storage.file.models.HandleItem;
import com.azure.storage.file.models.StorageErrorException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * This class provides a client that contains all the operations for interacting with directory in Azure Storage File Service.
 * Operations allowed by the client are creating, deleting and listing subdirectory and file, retrieving properties, setting metadata
 * and list or force close handles of the directory or file.
 *
 * <p><strong>Instantiating an Asynchronous Directory Client</strong></p>
 *
 * {@codesnippet com.azure.storage.file.directoryAsyncClient.instantiation}
 *
 * <p>View {@link DirectoryClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see DirectoryClientBuilder
 * @see DirectoryClient
 * @see SharedKeyCredential
 * @see SASTokenCredential
 */
public class DirectoryAsyncClient {
    private final AzureFileStorageImpl azureFileStorageClient;
    private final String shareName;
    private final String directoryPath;
    private final String snapshot;

    /**
     * Creates a DirectoryAsyncClient that sends requests to the storage directory at {@link AzureFileStorageImpl#getUrl() endpoint}.
     * Each service call goes through the {@link HttpPipeline pipeline} in the {@code client}.
     * @param azureFileStorageClient Client that interacts with the service interfaces
     * @param shareName Name of the share
     * @param directoryPath Name of the directory
     * @param snapshot The snapshot of the share
     */
    DirectoryAsyncClient(AzureFileStorageImpl azureFileStorageClient, String shareName, String directoryPath, String snapshot) {
        this.shareName = shareName;
        this.directoryPath = directoryPath;
        this.snapshot = snapshot;
        this.azureFileStorageClient = azureFileStorageClient;
    }

    /**
     * Creates a DirectoryAsyncClient that sends requests to this directory at {@code endpoint}.
     * Each service call goes through the {@code httpPipeline}.
     * @param endpoint URL for the Storage File service
     * @param httpPipeline HttpPipeline that HTTP requests and response flow through
     * @param shareName Name of the share
     * @param directoryPath Name of the directory
     * @param snapshot Optional snapshot of the share
     */
    DirectoryAsyncClient(URL endpoint, HttpPipeline httpPipeline, String shareName, String directoryPath, String snapshot) {
        this.shareName = shareName;
        this.directoryPath = directoryPath;
        this.snapshot = snapshot;
        this.azureFileStorageClient = new AzureFileStorageBuilder().pipeline(httpPipeline)
                                          .url(endpoint.toString())
                                          .build();
    }

    /**
     * Get the url of the storage directory client.
     * @return the URL of the storage directory client
     * @throws RuntimeException If the directory is using a malformed URL.
     */
    public URL getDirectoryUrl() {
        try {
            return new URL(azureFileStorageClient.getUrl());
        } catch (MalformedURLException e) {
            throw new RuntimeException(String.format("Invalid URL on %s: %s" + getClass().getSimpleName(),
                azureFileStorageClient.getUrl()), e);
        }
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
     * @return A response containing the directory info and the status of creating the directory.
     * @throws StorageErrorException If the directory has already existed, the parent directory does not exist or directory name is an invalid resource name.
     */
    public Mono<Response<DirectoryInfo>> create() {
        return create(null);
    }

    /**
     * Creates a directory in the file share and returns a response of DirectoryInfo to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the directory</p>
     *
     * {@codesnippet com.azure.storage.file.directoryAsyncClient.create#map}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @param metadata Optional metadata to associate with the directory
     * @return A response containing the directory info and the status of creating the directory.
     * @throws StorageErrorException If the directory has already existed, the parent directory does not exist or directory name is an invalid resource name.
     */
    public Mono<Response<DirectoryInfo>> create(Map<String, String> metadata) {
        return azureFileStorageClient.directorys().createWithRestResponseAsync(shareName, directoryPath, null, metadata, Context.NONE)
            .map(this::createWithRestResponse);
    }

    /**
     * Deletes the directory in the file share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the directory</p>
     *
     * {@codesnippet com.azure.storage.file.directoryClient.delete}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-directory">Azure Docs</a>.</p>
     *
     * @return A response that only contains headers and response status code
     * @throws StorageErrorException If the share doesn't exist
     */
    public Mono<VoidResponse> delete() {
        return azureFileStorageClient.directorys().deleteWithRestResponseAsync(shareName, directoryPath, Context.NONE).map(VoidResponse::new)
            .map(VoidResponse::new);
    }

    /**
     * Retrieves the properties of this directory.
     * The properties includes directory metadata, last modified date, is server encrypted, and eTag.
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
    public Mono<Response<DirectoryProperties>> getProperties() {
        return azureFileStorageClient.directorys().getPropertiesWithRestResponseAsync(shareName, directoryPath, snapshot, null, Context.NONE)
            .map(this::getPropertiesResponse);
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
     * @param metadata Optional metadata to set on the directory, if null is passed the metadata for the directory is cleared
     * @return information about the directory
     * @throws StorageErrorException If the directory doesn't exist or the metadata contains invalid keys
     */
    public Mono<Response<DirectorySetMetadataInfo>> setMetadata(Map<String, String> metadata) {
        return azureFileStorageClient.directorys().setMetadataWithRestResponseAsync(shareName, directoryPath, null, metadata, Context.NONE)
            .map(this::setMetadataResponse);
    }

    /**
     * Lists all sub-directories and files in this directory without their prefix or maxResult.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all sub-directories and files in the account</p>
     *
     * {@codesnippet com.azure.storage.file.directoryClient.listFilesAndDirectories}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/list-directories-and-files">Azure Docs</a>.</p>
     *
     * @return {@link FileRef File info} in the storage directory
     */
    public Flux<FileRef> listFilesAndDirectories() {
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
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/list-directories-and-files">Azure Docs</a>.</p>
     *
     * @param prefix Optional prefix which filters the results to return only files and directories whose name begins with.
     * @param maxResults Optional maximum number of files and/or directories to return per page.
     *                   If the request does not specify maxresults or specifies a value greater than 5,000, the server will return up to 5,000 items.
     * @return {@link FileRef File info} in this directory with prefix and max number of return results.
     */
    public Flux<FileRef> listFilesAndDirectories(String prefix, Integer maxResults) {
        return azureFileStorageClient.directorys().listFilesAndDirectoriesSegmentWithRestResponseAsync(shareName, directoryPath, prefix, snapshot, null, maxResults, null, Context.NONE)
                  .flatMapMany(response -> nextPageForFileAndDirecotries(response, prefix, maxResults));
    }

    /**
     * List of open handles on a directory or a file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Get 10 handles with recursive call.</p>
     *
     * {@codesnippet com.azure.storage.file.directoryAsyncClient.getHandles}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/list-handles">Azure Docs</a>.</p>
     *
     * @param maxResult Optional maximum number of results will return per page
     * @param recursive Specifies operation should apply to the directory specified in the URI, its files, its subdirectories and their files.
     * @return {@link HandleItem handles} in the directory that satisfy the requirements
     */
    public Flux<HandleItem> getHandles(Integer maxResult, boolean recursive) {
        return azureFileStorageClient.directorys().listHandlesWithRestResponseAsync(shareName, directoryPath, null, maxResult, null, snapshot, recursive, Context.NONE)
                   .flatMapMany(response -> nextPageForHandles(response, maxResult, recursive));
    }

    /**
     * Closes a handle or handles opened on a directory or a file at the service. It is intended to be used alongside {@link DirectoryAsyncClient#getHandles(Integer, boolean)} .
     * TODO: Will change the return type to how many handles have been closed. Implement one more API to force close all handles.
     * TODO: @see <a href="https://github.com/Azure/azure-sdk-for-java/issues/4525">Github Issue 4525</a>
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
     * @param handleId Specifies the handle ID to be closed. Use an asterisk ('*') as a wildcard string to specify all handles.
     * @param recursive A boolean value that specifies if the operation should also apply to the files and subdirectories of the directory specified in the URI.
     * @return The counts of number of handles closed
     */
    public Flux<Integer> forceCloseHandles(String handleId, boolean recursive) {
        return azureFileStorageClient.directorys().forceCloseHandlesWithRestResponseAsync(shareName, directoryPath, handleId, null, null, snapshot, recursive, Context.NONE)
            .flatMapMany(response -> nextPageForForceCloseHandles(response, handleId, recursive));
    }

    /**
     * Creates a subdirectory under current directory with specific name and returns a response of DirectoryAsyncClient to interact with it.
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
     * @return A response containing the subdirectory client and the status of creating the directory.
     * @throws StorageErrorException If the subdirectory has already existed, the parent directory does not exist or directory is an invalid resource name.
     */
    public Mono<Response<DirectoryAsyncClient>> createSubDirectory(String subDirectoryName) {
        return createSubDirectory(subDirectoryName, null);
    }

    /**
     * Creates a subdirectory under current directory with specific name , metadata and returns a response of DirectoryAsyncClient to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the subdirectory named "subdir", with metadata</p>
     *
     * {@codesnippet com.azure.storage.file.directoryClient.createSubDirectory#string-map}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @param subDirectoryName Name of the subdirectory
     * @param metadata Optional metadata to associate with the subdirectory
     * @return A response containing the subdirectory client and the status of creating the directory.
     * @throws StorageErrorException If the directory has already existed, the parent directory does not exist or subdirectory is an invalid resource name.
     */
    public Mono<Response<DirectoryAsyncClient>> createSubDirectory(String subDirectoryName, Map<String, String> metadata) {
        DirectoryAsyncClient createSubClient = getSubDirectoryClient(subDirectoryName);
        return createSubClient.create(metadata)
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
     * @return A response that only contains headers and response status code
     * @throws StorageErrorException If the subdirectory doesn't exist, the parent directory does not exist or subdirectory name is an invalid resource name.
     */
    public Mono<VoidResponse> deleteSubDirectory(String subDirectoryName) {
        DirectoryAsyncClient deleteSubClient = getSubDirectoryClient(subDirectoryName);
        return deleteSubClient.delete().map(VoidResponse::new);
    }

    /**
     * Creates a file in this directory with specific name, max number of results and returns a response of DirectoryInfo to interact with it.
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
     * @return A response containing the FileAsyncClient and the status of creating the directory.
     * @throws StorageErrorException If the file has already existed, the parent directory does not exist or file name is an invalid resource name.
     */
    public Mono<Response<FileAsyncClient>> createFile(String fileName, long maxSize) {
        return createFile(fileName, maxSize, null, null);
    }

    /**
     * Creates a file in this directory with specific name and returns a response of DirectoryInfo to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the file named "myFile"</p>
     *
     * {@codesnippet com.azure.storage.file.directoryAsyncClient.createFile#string-long-fileHTTPHeaders-map}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-file">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file
     * @param maxSize Max size of the file
     * @param httpHeaders the Http headers set to the file
     * @param metadata Optional name-value pairs associated with the file as metadata. Metadata names must adhere to the naming rules.
     * @return A response containing the directory info and the status of creating the directory.
     * @throws StorageErrorException If the directory has already existed, the parent directory does not exist or file name is an invalid resource name.
     */
    public Mono<Response<FileAsyncClient>> createFile(String fileName, long maxSize, FileHTTPHeaders httpHeaders, Map<String, String> metadata) {
        FileAsyncClient fileAsyncClient = getFileClient(fileName);
        return fileAsyncClient.create(maxSize, httpHeaders, metadata).map(response -> new SimpleResponse<>(response, fileAsyncClient));
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
     * @return A response that only contains headers and response status code
     * @throws StorageErrorException If the directory doesn't exist or the file doesn't exist or file name is an invalid resource name.
     */
    public Mono<VoidResponse> deleteFile(String fileName) {
        FileAsyncClient fileAsyncClient = getFileClient(fileName);
        return fileAsyncClient.delete().map(VoidResponse::new);
    }

    /**
     * Get snapshot id which attached to {@link DirectoryAsyncClient}.
     * Return {@code null} if no snapshot id attached.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Get the share snapshot id. </p>
     *
     * {@codesnippet com.azure.storage.file.directoryAsyncClient.getShareSnapshotId}
     *
     * @return The snapshot id which is a unique {@code DateTime} value that identifies the share snapshot to its base share.
     */
    public String getShareSnapshotId() {
        return this.snapshot;
    }

    private Response<DirectoryInfo> createWithRestResponse(final DirectorysCreateResponse response) {
        String eTag = response.deserializedHeaders().eTag();
        OffsetDateTime lastModified = response.deserializedHeaders().lastModified();
        DirectoryInfo directoryInfo = new DirectoryInfo(eTag, lastModified);
        return new SimpleResponse<>(response, directoryInfo);

    }

    private Response<DirectoryProperties> getPropertiesResponse(DirectorysGetPropertiesResponse response) {
        Map<String, String> metadata = response.deserializedHeaders().metadata();
        String eTag = response.deserializedHeaders().eTag();
        OffsetDateTime offsetDateTime = response.deserializedHeaders().lastModified();
        boolean isServerEncrypted = response.deserializedHeaders().isServerEncrypted();

        DirectoryProperties directoryProperties = new DirectoryProperties(metadata, eTag, offsetDateTime, isServerEncrypted);
        return new SimpleResponse<>(response, directoryProperties);
    }

    private Response<DirectorySetMetadataInfo> setMetadataResponse(final DirectorysSetMetadataResponse response) {
        String eTag = response.deserializedHeaders().eTag();
        boolean isServerEncrypted = response.deserializedHeaders().isServerEncrypted();

        DirectorySetMetadataInfo directorySetMetadataInfo = new DirectorySetMetadataInfo(eTag, isServerEncrypted);
        return new SimpleResponse<>(response, directorySetMetadataInfo);
    }

    private Flux<FileRef> nextPageForFileAndDirecotries(final DirectorysListFilesAndDirectoriesSegmentResponse response, final String prefix, final Integer maxResult) {
        List<FileRef> fileRefs = convertResponseAndGetNumOfResults(response);

        if (response.value().nextMarker() == null) {
            return Flux.fromIterable(fileRefs);
        }
        Mono<DirectorysListFilesAndDirectoriesSegmentResponse> listResponse = azureFileStorageClient.directorys().listFilesAndDirectoriesSegmentWithRestResponseAsync(shareName, directoryPath, prefix, snapshot, response.value().nextMarker(), maxResult, null, Context.NONE);
        Flux<FileRef> fileRefPublisher = listResponse.flatMapMany(newResponse -> nextPageForFileAndDirecotries(newResponse, prefix, maxResult));
        return Flux.fromIterable(fileRefs).concatWith(fileRefPublisher);
    }

    private Flux<HandleItem> nextPageForHandles(DirectorysListHandlesResponse response, Integer maxResult, boolean recursive) {
        List<HandleItem> handleItems = response.value().handleList();

        if (response.value().nextMarker() == null) {
            return Flux.fromIterable(handleItems);
        }
        Mono<DirectorysListHandlesResponse> listResponse = azureFileStorageClient.directorys().listHandlesWithRestResponseAsync(shareName, directoryPath, response.value().nextMarker(), maxResult, null, snapshot,  recursive, Context.NONE);
        Flux<HandleItem> fileRefPublisher = listResponse.flatMapMany(newResponse -> nextPageForHandles(newResponse, maxResult, recursive));
        return Flux.fromIterable(handleItems).concatWith(fileRefPublisher);
    }

    private Flux<Integer> nextPageForForceCloseHandles(DirectorysForceCloseHandlesResponse response, String handleId, boolean recursive) {
        List<Integer> handleCount = Arrays.asList(response.deserializedHeaders().numberOfHandlesClosed());

        if (response.deserializedHeaders().marker() == null) {
            return Flux.fromIterable(handleCount);
        }
        Mono<DirectorysForceCloseHandlesResponse> listResponse = azureFileStorageClient.directorys().forceCloseHandlesWithRestResponseAsync(shareName, directoryPath, handleId, null, response.deserializedHeaders().marker(), snapshot, recursive, Context.NONE);
        Flux<Integer> fileRefPublisher = listResponse.flatMapMany(newResponse -> nextPageForForceCloseHandles(newResponse, handleId, recursive));
        return Flux.fromIterable(handleCount).concatWith(fileRefPublisher);
    }

    private List<FileRef> convertResponseAndGetNumOfResults(DirectorysListFilesAndDirectoriesSegmentResponse response) {
        List<FileRef> fileRefs = new ArrayList<>();
        response.value().segment().directoryItems().forEach(directoryItem -> fileRefs.add(new FileRef(directoryItem.name(), true, null)));
        response.value().segment().fileItems().forEach(fileItem -> fileRefs.add(new FileRef(fileItem.name(), false, fileItem.properties())));
        return fileRefs;
    }
}
