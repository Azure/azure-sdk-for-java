// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.file.share.models.CloseHandlesInfo;
import com.azure.storage.file.share.models.ShareDirectoryInfo;
import com.azure.storage.file.share.models.ShareDirectoryProperties;
import com.azure.storage.file.share.models.ShareDirectorySetMetadataInfo;
import com.azure.storage.file.share.models.ShareFileHttpHeaders;
import com.azure.storage.file.share.models.ShareFileInfo;
import com.azure.storage.file.share.models.HandleItem;
import com.azure.storage.file.share.models.ShareRequestConditions;
import com.azure.storage.file.share.models.ShareStorageException;
import com.azure.storage.file.share.models.ShareFileItem;
import com.azure.storage.file.share.options.ShareListFilesAndDirectoriesOptions;
import com.azure.storage.file.share.sas.ShareServiceSasSignatureValues;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

import static com.azure.storage.common.implementation.StorageImplUtils.blockWithOptionalTimeout;

/**
 * This class provides a client that contains all the operations for interacting with directory in Azure Storage File
 * Service. Operations allowed by the client are creating, deleting and listing subdirectory and file, retrieving
 * properties, setting metadata and list or force close handles of the directory or file.
 *
 * <p><strong>Instantiating an Synchronous Directory Client</strong></p>
 *
 * {@codesnippet com.azure.storage.file.share.ShareDirectoryClient.instantiation}
 *
 * <p>View {@link ShareFileClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see ShareFileClientBuilder
 * @see ShareDirectoryClient
 * @see StorageSharedKeyCredential
 */
@ServiceClient(builder = ShareFileClientBuilder.class)
public class ShareDirectoryClient {

    private final ShareDirectoryAsyncClient shareDirectoryAsyncClient;

    /**
     * Creates a ShareDirectoryClient that wraps a ShareDirectoryAsyncClient and blocks requests.
     *
     * @param shareDirectoryAsyncClient ShareDirectoryAsyncClient that is used to send requests
     */
    ShareDirectoryClient(ShareDirectoryAsyncClient shareDirectoryAsyncClient) {
        this.shareDirectoryAsyncClient = shareDirectoryAsyncClient;
    }

    /**
     * Get the url of the storage directory client.
     *
     * @return the URL of the storage directory client.
     */
    public String getDirectoryUrl() {
        return shareDirectoryAsyncClient.getDirectoryUrl();
    }

    /**
     * Gets the service version the client is using.
     *
     * @return the service version the client is using.
     */
    public ShareServiceVersion getServiceVersion() {
        return shareDirectoryAsyncClient.getServiceVersion();
    }

    /**
     * Constructs a ShareFileClient that interacts with the specified file.
     *
     * <p>If the file doesn't exist in this directory {@link ShareFileClient#create(long)} create} in the client will
     * need to be called before interaction with the file can happen.</p>
     *
     * @param fileName Name of the file
     * @return a ShareFileClient that interacts with the specified share
     */
    public ShareFileClient getFileClient(String fileName) {
        return new ShareFileClient(shareDirectoryAsyncClient.getFileClient(fileName));
    }

    /**
     * Constructs a ShareDirectoryClient that interacts with the specified directory.
     *
     * <p>If the file doesn't exist in this directory {@link ShareDirectoryClient#create()} create} in the client will
     * need to be called before interaction with the directory can happen.</p>
     *
     * @param subdirectoryName Name of the directory
     * @return a ShareDirectoryClient that interacts with the specified directory
     */
    public ShareDirectoryClient getSubdirectoryClient(String subdirectoryName) {
        return new ShareDirectoryClient(shareDirectoryAsyncClient.getSubdirectoryClient(subdirectoryName));
    }

    /**
     * Determines if the directory this client represents exists in the cloud.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryClient.exists}
     *
     * @return Flag indicating existence of the directory.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Boolean exists() {
        return existsWithResponse(null, Context.NONE).getValue();
    }

    /**
     * Determines if the directory this client represents exists in the cloud.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryClient.existsWithResponse#Duration-Context}
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return Flag indicating existence of the directory.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Boolean> existsWithResponse(Duration timeout, Context context) {
        Mono<Response<Boolean>> response = shareDirectoryAsyncClient.existsWithResponse(context);

        return blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Creates a directory in the file share and returns a response of {@link ShareDirectoryInfo} to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the directory</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryClient.createDirectory}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @return The {@link ShareDirectoryInfo directory info}.
     * @throws ShareStorageException If the directory has already existed, the parent directory does not exist or
     * directory name is an invalid resource name.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ShareDirectoryInfo create() {
        return createWithResponse(null, null, null, null, Context.NONE).getValue();
    }

    /**
     * Creates a directory in the file share and returns a response of ShareDirectoryInfo to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the directory</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryClient.createWithResponse#FileSmbProperties-String-Map-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @param smbProperties The SMB properties of the directory.
     * @param filePermission The file permission of the directory.
     * @param metadata Optional metadata to associate with the directory.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the directory info and the status of creating the directory.
     * @throws ShareStorageException If the directory has already existed, the parent directory does not exist or
     * directory name is an invalid resource name.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareDirectoryInfo> createWithResponse(FileSmbProperties smbProperties, String filePermission,
        Map<String, String> metadata, Duration timeout, Context context) {
        Mono<Response<ShareDirectoryInfo>> response = shareDirectoryAsyncClient
            .createWithResponse(smbProperties, filePermission, metadata, context);
        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Deletes the directory in the file share. The directory must be empty before it can be deleted.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the directory</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryClient.delete}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-directory">Azure Docs</a>.</p>
     *
     * @throws ShareStorageException If the share doesn't exist
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void delete() {
        deleteWithResponse(null, Context.NONE);
    }

    /**
     * Deletes the directory in the file share. The directory must be empty before it can be deleted.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the directory</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryClient.deleteWithResponse#duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-directory">Azure Docs</a>.</p>
     *
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response that only contains headers and response status code
     * @throws ShareStorageException If the share doesn't exist
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteWithResponse(Duration timeout, Context context) {
        Mono<Response<Void>> response = shareDirectoryAsyncClient.deleteWithResponse(context);
        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Retrieves the properties of this directory. The properties includes directory metadata, last modified date, is
     * server encrypted, and eTag.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve directory properties</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryClient.getProperties}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-directory-properties">Azure Docs</a>.</p>
     *
     * @return Storage directory properties
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ShareDirectoryProperties getProperties() {
        return getPropertiesWithResponse(null, Context.NONE).getValue();
    }

    /**
     * Retrieves the properties of this directory. The properties includes directory metadata, last modified date, is
     * server encrypted, and eTag.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve directory properties</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryClient.getPropertiesWithResponse#duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-directory-properties">Azure Docs</a>.</p>
     *
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the storage directory properties with response status code and headers
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareDirectoryProperties> getPropertiesWithResponse(Duration timeout, Context context) {
        Mono<Response<ShareDirectoryProperties>> response = shareDirectoryAsyncClient
            .getPropertiesWithResponse(context);
        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Sets the properties of this directory. The properties include the file SMB properties and the file permission.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set directory properties</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryClient.setProperties#FileSmbProperties-String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-directory-properties">Azure Docs</a>.</p>
     *
     * @param smbProperties The SMB properties of the directory.
     * @param filePermission The file permission of the directory.
     * @return The storage directory SMB properties
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ShareDirectoryInfo setProperties(FileSmbProperties smbProperties, String filePermission) {
        return setPropertiesWithResponse(smbProperties, filePermission, null, Context.NONE).getValue();
    }

    /**
     * Sets the properties of this directory. The properties include the file SMB properties and the file permission.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set directory properties</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryClient.setPropertiesWithResponse#FileSmbProperties-String-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-directory-properties">Azure Docs</a>.</p>
     *
     * @param smbProperties The SMB properties of the directory.
     * @param filePermission The file permission of the directory.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the storage directory smb properties with headers and response status code
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareDirectoryInfo> setPropertiesWithResponse(FileSmbProperties smbProperties,
                                                                  String filePermission,
                                                                  Duration timeout, Context context) {
        Mono<Response<ShareDirectoryInfo>> response = shareDirectoryAsyncClient
            .setPropertiesWithResponse(smbProperties, filePermission, context);
        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
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
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryClient.setMetadata#map}
     *
     * <p>Clear the metadata of the directory</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryClient.setMetadata#map.clearMetadata}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-directory-metadata">Azure Docs</a>.</p>
     *
     * @param metadata Optional metadata to set on the directory, if null is passed the metadata for the directory is
     * cleared
     * @return The information about the directory
     * @throws ShareStorageException If the directory doesn't exist or the metadata contains invalid keys
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ShareDirectorySetMetadataInfo setMetadata(Map<String, String> metadata) {
        return setMetadataWithResponse(metadata, null, Context.NONE).getValue();
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
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryClient.setMetadataWithResponse#map-duration-context}
     *
     * <p>Clear the metadata of the directory</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryClient.setMetadataWithResponse#map-duration-context.clearMetadata}
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-directory-metadata">Azure Docs</a>.</p>
     *
     * @param metadata Optional metadata to set on the directory, if null is passed the metadata for the directory is
     * cleared
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the information about the directory and response status code
     * @throws ShareStorageException If the directory doesn't exist or the metadata contains invalid keys
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareDirectorySetMetadataInfo> setMetadataWithResponse(Map<String, String> metadata,
        Duration timeout, Context context) {
        Mono<Response<ShareDirectorySetMetadataInfo>> response = shareDirectoryAsyncClient
            .setMetadataWithResponse(metadata, context);
        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Lists all sub-directories and files in this directory without their prefix or maxResult in single page.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all sub-directories and files in the account</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryClient.listFilesAndDirectories}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-directories-and-files">Azure
     * Docs</a>.</p>
     *
     * @return {@link ShareFileItem File info} in the storage directory
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ShareFileItem> listFilesAndDirectories() {
        return listFilesAndDirectories(null, null, null, Context.NONE);
    }

    /**
     * Lists all sub-directories and files in this directory with their prefix or snapshots.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all sub-directories and files in this directory with "subdir" prefix and return 10 results in the
     * account</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryClient.listFilesAndDirectories#string-integer-duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-directories-and-files">Azure
     * Docs</a>.</p>
     *
     * @param prefix Optional prefix which filters the results to return only files and directories whose name begins
     * with.
     * @param maxResultsPerPage Optional maximum number of files and/or directories to return per page.
     * If the request does not specify maxResultsPerPage or specifies a value greater than 5,000,
     * the server will return up to 5,000 items. If iterating by page, the page size passed to byPage methods such as
     * {@link PagedIterable#iterableByPage(int)} will be preferred over this value.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return {@link ShareFileItem File info} in this directory with prefix and max number of return results.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ShareFileItem> listFilesAndDirectories(String prefix, Integer maxResultsPerPage,
                                                                Duration timeout, Context context) {
        return listFilesAndDirectories(new ShareListFilesAndDirectoriesOptions().setPrefix(prefix)
            .setMaxResultsPerPage(maxResultsPerPage), timeout, context);
    }

    /**
     * Lists all sub-directories and files in this directory with their prefix or snapshots.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all sub-directories and files in this directory with "subdir" prefix and return 10 results in the
     * account</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryClient.listFilesAndDirectories#ShareListFilesAndDirectoriesOptions-duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-directories-and-files">Azure
     * Docs</a>.</p>
     *
     * @param options Optional parameters.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return {@link ShareFileItem File info} in this directory with prefix and max number of return results.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ShareFileItem> listFilesAndDirectories(ShareListFilesAndDirectoriesOptions options,
        Duration timeout, Context context) {
        return new PagedIterable<>(shareDirectoryAsyncClient
            .listFilesAndDirectoriesWithOptionalTimeout(options, timeout, context));
    }

    /**
     * List of open handles on a directory or a file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Get 10 handles with recursive call.</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryClient.listHandles#Integer-boolean-duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-handles">Azure Docs</a>.</p>
     *
     * @param maxResultsPerPage Optional maximum number of results will return per page
     * @param recursive Specifies operation should apply to the directory specified in the URI, its files, its
     * subdirectories and their files.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return {@link HandleItem handles} in the directory that satisfy the requirements
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<HandleItem> listHandles(Integer maxResultsPerPage, boolean recursive, Duration timeout,
        Context context) {
        return new PagedIterable<>(shareDirectoryAsyncClient
            .listHandlesWithOptionalTimeout(maxResultsPerPage, recursive, timeout, context));
    }

    /**
     * Closes a handle on the directory at the service. This is intended to be used alongside {@link
     * #listHandles(Integer, boolean, Duration, Context)}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Force close handles returned by list handles.</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryClient.forceCloseHandle#String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/force-close-handles">Azure Docs</a>.</p>
     *
     * @param handleId Handle ID to be closed.
     * @return Information about the closed handles.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CloseHandlesInfo forceCloseHandle(String handleId) {
        return forceCloseHandleWithResponse(handleId, null, Context.NONE).getValue();
    }

    /**
     * Closes a handle on the directory at the service. This is intended to be used alongside {@link
     * #listHandles(Integer, boolean, Duration, Context)}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Force close handles returned by list handles.</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryClient.forceCloseHandleWithResponse#String-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/force-close-handles">Azure Docs</a>.</p>
     *
     * @param handleId Handle ID to be clsoed.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response that contains information about the closed handles, headers and response status code.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CloseHandlesInfo> forceCloseHandleWithResponse(String handleId, Duration timeout, Context context) {
        Mono<Response<CloseHandlesInfo>> response = shareDirectoryAsyncClient
            .forceCloseHandleWithResponse(handleId, context);
        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Closes all handles opened on the directory at the service.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Force close all handles recursively.</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryClient.forceCloseAllHandles#boolean-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/force-close-handles">Azure Docs</a>.</p>
     *
     * @param recursive Flag indicating if the operation should apply to all subdirectories and files contained in the
     * directory.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return Information about the closed handles
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CloseHandlesInfo forceCloseAllHandles(boolean recursive, Duration timeout, Context context) {
        return new PagedIterable<>(shareDirectoryAsyncClient.forceCloseAllHandlesWithTimeout(recursive, timeout,
            context)).stream().reduce(new CloseHandlesInfo(0, 0),
                (accu, next) -> new CloseHandlesInfo(accu.getClosedHandles() + next.getClosedHandles(),
                    accu.getFailedHandles() + next.getFailedHandles()));
    }

    /**
     * Creates a subdirectory under current directory with specific name and returns a response of ShareDirectoryClient
     * to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the sub directory "subdir" </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryClient.createSubdirectory#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @param subdirectoryName Name of the subdirectory
     * @return The subdirectory client.
     * @throws ShareStorageException If the subdirectory has already existed, the parent directory does not exist or
     * directory is an invalid resource name.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ShareDirectoryClient createSubdirectory(String subdirectoryName) {
        return createSubdirectoryWithResponse(subdirectoryName, null, null, null,
            null, Context.NONE).getValue();
    }

    /**
     * Creates a subdirectory under current directory with specific name , metadata and returns a response of
     * ShareDirectoryClient to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the subdirectory named "subdir", with metadata</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryClient.createSubdirectoryWithResponse#String-FileSmbProperties-String-Map-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @param subdirectoryName Name of the subdirectory
     * @param smbProperties The SMB properties of the directory.
     * @param filePermission The file permission of the directory.
     * @param metadata Optional metadata to associate with the subdirectory
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the subdirectory client and the status of creating the directory.
     * @throws ShareStorageException If the directory has already existed, the parent directory does not exist or
     * subdirectory is an invalid resource name.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareDirectoryClient> createSubdirectoryWithResponse(String subdirectoryName,
        FileSmbProperties smbProperties, String filePermission, Map<String, String> metadata, Duration timeout,
        Context context) {
        ShareDirectoryClient shareDirectoryClient = getSubdirectoryClient(subdirectoryName);
        return new SimpleResponse<>(shareDirectoryClient
            .createWithResponse(smbProperties, filePermission, metadata, timeout, context), shareDirectoryClient);
    }

    /**
     * Deletes the subdirectory with specific name in this directory. The directory must be empty before it can be
     * deleted.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the subdirectory named "subdir"</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryClient.deleteSubdirectory#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-directory">Azure Docs</a>.</p>
     *
     * @param subdirectoryName Name of the subdirectory
     * @throws ShareStorageException If the subdirectory doesn't exist, the parent directory does not exist or
     * subdirectory name is an invalid resource name.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteSubdirectory(String subdirectoryName) {
        deleteSubdirectoryWithResponse(subdirectoryName, null, Context.NONE);
    }

    /**
     * Deletes the subdirectory with specific name in this directory. The directory must be empty before it can be
     * deleted.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the subdirectory named "subdir"</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryClient.deleteSubdirectoryWithResponse#string-duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-directory">Azure Docs</a>.</p>
     *
     * @param subdirectoryName Name of the subdirectory
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @return A response that only contains headers and response status code
     * @throws ShareStorageException If the subdirectory doesn't exist, the parent directory does not exist or
     * subdirectory name is an invalid resource name.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteSubdirectoryWithResponse(String subdirectoryName, Duration timeout, Context context) {
        Mono<Response<Void>> response = shareDirectoryAsyncClient.deleteSubdirectoryWithResponse(subdirectoryName,
            context);
        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Creates a file in this directory with specific name, max number of results and returns a response of
     * ShareDirectoryInfo to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create 1k file with named "myFile"</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryClient.createFile#string-long}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-file">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file
     * @param maxSize Size of the file
     * @return The ShareFileClient
     * @throws ShareStorageException If the file has already existed, the parent directory does not exist or file name
     * is an invalid resource name.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ShareFileClient createFile(String fileName, long maxSize) {
        return createFileWithResponse(fileName, maxSize, null, null, null,
            null, null, Context.NONE).getValue();
    }

    /**
     * Creates a file in this directory with specific name and returns a response of ShareDirectoryInfo to
     * interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the file named "myFile"</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryClient.createFile#String-long-ShareFileHttpHeaders-FileSmbProperties-String-Map-duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-file">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file
     * @param maxSize Max size of the file
     * @param httpHeaders The user settable file http headers.
     * @param smbProperties The user settable file smb properties.
     * @param filePermission THe file permission of the file.
     * @param metadata Optional name-value pairs associated with the file as metadata.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the directory info and the status of creating the directory.
     * @throws ShareStorageException If the directory has already existed, the parent directory does not exist or file
     * name is an invalid resource name.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareFileClient> createFileWithResponse(String fileName, long maxSize,
        ShareFileHttpHeaders httpHeaders, FileSmbProperties smbProperties, String filePermission,
        Map<String, String> metadata, Duration timeout, Context context) {
        return this.createFileWithResponse(fileName, maxSize, httpHeaders, smbProperties, filePermission, metadata,
            null, timeout, context);
    }

    /**
     * Creates a file in this directory with specific name and returns a response of ShareDirectoryInfo to
     * interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the file named "myFile"</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryClient.createFile#String-long-ShareFileHttpHeaders-FileSmbProperties-String-Map-ShareRequestConditions-duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-file">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file
     * @param maxSize Max size of the file
     * @param httpHeaders The user settable file http headers.
     * @param smbProperties The user settable file smb properties.
     * @param filePermission THe file permission of the file.
     * @param metadata Optional name-value pairs associated with the file as metadata.
     * @param requestConditions {@link ShareRequestConditions}
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the directory info and the status of creating the directory.
     * @throws ShareStorageException If the directory has already existed, the parent directory does not exist or file
     * name is an invalid resource name.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareFileClient> createFileWithResponse(String fileName, long maxSize,
        ShareFileHttpHeaders httpHeaders, FileSmbProperties smbProperties, String filePermission,
        Map<String, String> metadata, ShareRequestConditions requestConditions, Duration timeout, Context context) {
        ShareFileClient shareFileClient = getFileClient(fileName);
        Response<ShareFileInfo> response = shareFileClient.createWithResponse(maxSize, httpHeaders, smbProperties,
            filePermission, metadata, requestConditions, timeout, context);
        return new SimpleResponse<>(response, shareFileClient);
    }

    /**
     * Deletes the file with specific name in this directory.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the file "filetest"</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryClient.deleteFile#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-file2">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file
     * @throws ShareStorageException If the directory doesn't exist or the file doesn't exist or file name is an invalid
     * resource name.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteFile(String fileName) {
        deleteFileWithResponse(fileName, null, Context.NONE);
    }

    /**
     * Deletes the file with specific name in this directory.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the file "filetest"</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryClient.deleteFileWithResponse#string-duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-file2">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response that only contains headers and response status code
     * @throws ShareStorageException If the directory doesn't exist or the file doesn't exist or file name is an invalid
     * resource name.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteFileWithResponse(String fileName, Duration timeout, Context context) {
        return this.deleteFileWithResponse(fileName, null, timeout, context);
    }

    /**
     * Deletes the file with specific name in this directory.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the file "filetest"</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryClient.deleteFileWithResponse#string-ShareRequestConditions-duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-file2">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file
     * @param requestConditions {@link ShareRequestConditions}
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response that only contains headers and response status code
     * @throws ShareStorageException If the directory doesn't exist or the file doesn't exist or file name is an invalid
     * resource name.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteFileWithResponse(String fileName, ShareRequestConditions requestConditions,
        Duration timeout, Context context) {
        Mono<Response<Void>> response = shareDirectoryAsyncClient.deleteFileWithResponse(fileName, requestConditions,
            context);
        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Get snapshot id which attached to {@link ShareDirectoryClient}. Return {@code null} if no snapshot id attached.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Get the share snapshot id. </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryClient.getShareSnapshotId}
     *
     * @return The snapshot id which is a unique {@code DateTime} value that identifies the share snapshot to its base
     * share.
     */
    public String getShareSnapshotId() {
        return shareDirectoryAsyncClient.getShareSnapshotId();
    }

    /**
     * Get the share name of directory client.
     *
     * <p>Get the share name. </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryClient.getShareName}
     *
     * @return The share name of the directory.
     */
    public String getShareName() {
        return shareDirectoryAsyncClient.getShareName();
    }

    /**
     * Get the directory path of the client.
     *
     * <p>Get directory path. </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryClient.getDirectoryPath}
     *
     * @return The path of the directory.
     */
    public String getDirectoryPath() {
        return shareDirectoryAsyncClient.getDirectoryPath();
    }

    /**
     * Get associated account name.
     *
     * @return account name associated with this storage resource.
     */
    public String getAccountName() {
        return this.shareDirectoryAsyncClient.getAccountName();
    }

    /**
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return The pipeline.
     */
    public HttpPipeline getHttpPipeline() {
        return this.shareDirectoryAsyncClient.getHttpPipeline();
    }

    /**
     * Generates a service SAS for the directory using the specified {@link ShareServiceSasSignatureValues}
     * <p>Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link ShareServiceSasSignatureValues} for more information on how to construct a service SAS.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryClient.generateSas#ShareServiceSasSignatureValues}
     *
     * @param shareServiceSasSignatureValues {@link ShareServiceSasSignatureValues}
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateSas(ShareServiceSasSignatureValues shareServiceSasSignatureValues) {
        return this.shareDirectoryAsyncClient.generateSas(shareServiceSasSignatureValues);
    }

    /**
     * Generates a service SAS for the directory using the specified {@link ShareServiceSasSignatureValues}
     * <p>Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link ShareServiceSasSignatureValues} for more information on how to construct a service SAS.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareDirectoryClient.generateSas#ShareServiceSasSignatureValues-Context}
     *
     * @param shareServiceSasSignatureValues {@link ShareServiceSasSignatureValues}
     * @param context Additional context that is passed through the code when generating a SAS.
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateSas(ShareServiceSasSignatureValues shareServiceSasSignatureValues, Context context) {
        return this.shareDirectoryAsyncClient.generateSas(shareServiceSasSignatureValues, context);
    }
}
