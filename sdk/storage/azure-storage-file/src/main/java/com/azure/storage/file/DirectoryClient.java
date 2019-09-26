// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.storage.common.Utility;
import com.azure.storage.common.credentials.SasTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.file.models.DirectoryInfo;
import com.azure.storage.file.models.DirectoryProperties;
import com.azure.storage.file.models.DirectorySetMetadataInfo;
import com.azure.storage.file.models.FileHTTPHeaders;
import com.azure.storage.file.models.FileInfo;
import com.azure.storage.file.models.FileRef;
import com.azure.storage.file.models.HandleItem;
import com.azure.storage.file.models.StorageException;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.time.Duration;
import java.util.Map;

/**
 * This class provides a client that contains all the operations for interacting with directory in Azure Storage File
 * Service. Operations allowed by the client are creating, deleting and listing subdirectory and file, retrieving
 * properties, setting metadata and list or force close handles of the directory or file.
 *
 * <p><strong>Instantiating an Synchronous Directory Client</strong></p>
 *
 * {@codesnippet com.azure.storage.file.directoryClient.instantiation}
 *
 * <p>View {@link FileClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see FileClientBuilder
 * @see DirectoryClient
 * @see SharedKeyCredential
 * @see SasTokenCredential
 */
@ServiceClient(builder = FileClientBuilder.class)
public class DirectoryClient {

    private final DirectoryAsyncClient directoryAsyncClient;

    /**
     * Creates a DirectoryClient that wraps a DirectoryAsyncClient and blocks requests.
     *
     * @param directoryAsyncClient DirectoryAsyncClient that is used to send requests
     */
    DirectoryClient(DirectoryAsyncClient directoryAsyncClient) {
        this.directoryAsyncClient = directoryAsyncClient;
    }

    /**
     * Get the url of the storage directory client.
     *
     * @return the URL of the storage directory client.
     * @throws RuntimeException If the directory is using a malformed URL.
     */
    public URL getDirectoryUrl() {
        return directoryAsyncClient.getDirectoryUrl();
    }

    /**
     * Constructs a FileClient that interacts with the specified file.
     *
     * <p>If the file doesn't exist in this directory {@link FileClient#create(long)} create} in the client will
     * need to be called before interaction with the file can happen.</p>
     *
     * @param fileName Name of the file
     * @return a FileClient that interacts with the specified share
     */
    public FileClient getFileClient(String fileName) {
        return new FileClient(directoryAsyncClient.getFileClient(fileName));
    }

    /**
     * Constructs a DirectoryClient that interacts with the specified directory.
     *
     * <p>If the file doesn't exist in this directory {@link DirectoryClient#create()} create} in the client will
     * need to be called before interaction with the directory can happen.</p>
     *
     * @param subDirectoryName Name of the directory
     * @return a DirectoryClient that interacts with the specified directory
     */
    public DirectoryClient getSubDirectoryClient(String subDirectoryName) {
        return new DirectoryClient(directoryAsyncClient.getSubDirectoryClient(subDirectoryName));
    }

    /**
     * Creates a directory in the file share and returns a response of {@link DirectoryInfo} to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the directory</p>
     *
     * {@codesnippet com.azure.storage.file.directoryClient.createDirectory}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @return The {@link DirectoryInfo directory info}.
     * @throws StorageException If the directory has already existed, the parent directory does not exist or directory
     * name is an invalid resource name.
     */
    public DirectoryInfo create() {
        return createWithResponse(null, null, null, null, Context.NONE).getValue();
    }

    /**
     * Creates a directory in the file share and returns a response of DirectoryInfo to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the directory</p>
     *
     * {@codesnippet com.azure.storage.file.directoryClient.createWithResponse#filesmbproperties-string-map-duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @param smbProperties The SMB properties of the directory.
     * @param filePermission The file permission of the directory.
     * @param metadata Optional metadata to associate with the directory.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the directory info and the status of creating the directory.
     * @throws StorageException If the directory has already existed, the parent directory does not exist or directory
     * name is an invalid resource name.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    public Response<DirectoryInfo> createWithResponse(FileSmbProperties smbProperties, String filePermission,
        Map<String, String> metadata, Duration timeout, Context context) {
        Mono<Response<DirectoryInfo>> response = directoryAsyncClient
            .createWithResponse(smbProperties, filePermission, metadata, context);
        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Deletes the directory in the file share. The directory must be empty before it can be deleted.
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
     * @throws StorageException If the share doesn't exist
     */
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
     * {@codesnippet com.azure.storage.file.DirectoryClient.deleteWithResponse#duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-directory">Azure Docs</a>.</p>
     *
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response that only contains headers and response status code
     * @throws StorageException If the share doesn't exist
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    public Response<Void> deleteWithResponse(Duration timeout, Context context) {
        Mono<Response<Void>> response = directoryAsyncClient.deleteWithResponse(context);
        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Retrieves the properties of this directory. The properties includes directory metadata, last modified date, is
     * server encrypted, and eTag.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve directory properties</p>
     *
     * {@codesnippet com.azure.storage.file.directoryClient.getProperties}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-directory-properties">Azure Docs</a>.</p>
     *
     * @return Storage directory properties
     */
    public DirectoryProperties getProperties() {
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
     * {@codesnippet com.azure.storage.file.DirectoryClient.getPropertiesWithResponse#duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-directory-properties">Azure Docs</a>.</p>
     *
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the storage directory properties with response status code and headers
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    public Response<DirectoryProperties> getPropertiesWithResponse(Duration timeout, Context context) {
        Mono<Response<DirectoryProperties>> response = directoryAsyncClient.getPropertiesWithResponse(context);
        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Sets the properties of this directory. The properties include the file SMB properties and the file permission.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set directory properties</p>
     *
     * {@codesnippet com.azure.storage.file.directoryClient.setProperties#filesmbproperties-string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-directory-properties">Azure Docs</a>.</p>
     *
     * @param smbProperties The SMB properties of the directory.
     * @param filePermission The file permission of the directory.
     * @return The storage directory SMB properties
     */
    public DirectoryInfo setProperties(FileSmbProperties smbProperties, String filePermission) {
        return setPropertiesWithResponse(smbProperties, filePermission, null, Context.NONE).getValue();
    }

    /**
     * Sets the properties of this directory. The properties include the file SMB properties and the file permission.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set directory properties</p>
     *
     * {@codesnippet com.azure.storage.file.directoryClient.setPropertiesWithResponse#filesmbproperties-string-duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-directory-properties">Azure Docs</a>.</p>
     *
     * @param smbProperties The SMB properties of the directory.
     * @param filePermission The file permission of the directory.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the storage directory smb properties with headers and response status code
     */
    public Response<DirectoryInfo> setPropertiesWithResponse(FileSmbProperties smbProperties, String filePermission,
        Duration timeout, Context context) {
        Mono<Response<DirectoryInfo>> response = directoryAsyncClient
            .setPropertiesWithResponse(smbProperties, filePermission, context);
        return Utility.blockWithOptionalTimeout(response, timeout);
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
     * {@codesnippet com.azure.storage.file.directoryClient.setMetadata#map}
     *
     * <p>Clear the metadata of the directory</p>
     *
     * {@codesnippet com.azure.storage.file.directoryClient.setMetadata#map.clearMetadata}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-directory-metadata">Azure Docs</a>.</p>
     *
     * @param metadata Optional metadata to set on the directory, if null is passed the metadata for the directory is
     * cleared
     * @return The information about the directory
     * @throws StorageException If the directory doesn't exist or the metadata contains invalid keys
     */
    public DirectorySetMetadataInfo setMetadata(Map<String, String> metadata) {
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
     * {@codesnippet com.azure.storage.file.directoryClient.setMetadataWithResponse#map-duration-context}
     *
     * <p>Clear the metadata of the directory</p>
     *
     * {@codesnippet com.azure.storage.file.DirectoryClient.setMetadataWithResponse#map-duration-context.clearMetadata}
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-directory-metadata">Azure Docs</a>.</p>
     *
     * @param metadata Optional metadata to set on the directory, if null is passed the metadata for the directory is
     * cleared
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the information about the directory and response status code
     * @throws StorageException If the directory doesn't exist or the metadata contains invalid keys
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    public Response<DirectorySetMetadataInfo> setMetadataWithResponse(Map<String, String> metadata, Duration timeout,
        Context context) {
        Mono<Response<DirectorySetMetadataInfo>> response = directoryAsyncClient
            .setMetadataWithResponse(metadata, context);
        return Utility.blockWithOptionalTimeout(response, timeout);
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
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/list-directories-and-files">Azure
     * Docs</a>.</p>
     *
     * @return {@link FileRef File info} in the storage directory
     */
    public PagedIterable<FileRef> listFilesAndDirectories() {
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
     * {@codesnippet com.azure.storage.file.directoryClient.listFilesAndDirectories#string-integer-duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/list-directories-and-files">Azure
     * Docs</a>.</p>
     *
     * @param prefix Optional prefix which filters the results to return only files and directories whose name begins
     * with.
     * @param maxResults Optional maximum number of files and/or directories to return per page. If the request does not
     * specify maxresults or specifies a value greater than 5,000, the server will return up to 5,000 items.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return {@link FileRef File info} in this directory with prefix and max number of return results.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    public PagedIterable<FileRef> listFilesAndDirectories(String prefix, Integer maxResults, Duration timeout,
        Context context) {
        return new PagedIterable<>(directoryAsyncClient
            .listFilesAndDirectoriesWithOptionalTimeout(prefix, maxResults, timeout, context));
    }

    /**
     * List of open handles on a directory or a file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Get 10 handles with recursive call.</p>
     *
     * {@codesnippet com.azure.storage.file.directoryClient.listHandles#Integer-boolean-duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/list-handles">Azure Docs</a>.</p>
     *
     * @param maxResult Optional maximum number of results will return per page
     * @param recursive Specifies operation should apply to the directory specified in the URI, its files, its
     * subdirectories and their files.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return {@link HandleItem handles} in the directory that satisfy the requirements
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    public PagedIterable<HandleItem> listHandles(Integer maxResult, boolean recursive, Duration timeout,
        Context context) {
        return new PagedIterable<>(directoryAsyncClient
            .listHandlesWithOptionalTimeout(maxResult, recursive, timeout, context));
    }

    /**
     * Closes a handle or handles opened on a directory or a file at the service. It is intended to be used alongside
     * {@link DirectoryClient#listHandles(Integer, boolean, Duration, Context)} .
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Force close handles with handles returned by get handles in recursive.</p>
     *
     * {@codesnippet com.azure.storage.file.directoryClient.forceCloseHandles}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/force-close-handles">Azure Docs</a>.</p>
     *
     * @param handleId Specifies the handle ID to be closed. Use an asterisk ('*') as a wildcard string to specify all
     * handles.
     * @param recursive A boolean value that specifies if the operation should also apply to the files and
     * subdirectories of the directory specified in the URI.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The counts of number of handles closed.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    public PagedIterable<Integer> forceCloseHandles(String handleId, boolean recursive, Duration timeout,
        Context context) {
        return new PagedIterable<>(directoryAsyncClient
            .forceCloseHandlesWithOptionalTimeout(handleId, recursive, timeout, context));
    }

    /**
     * Creates a subdirectory under current directory with specific name and returns a response of DirectoryClient to
     * interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the sub directory "subdir" </p>
     *
     * {@codesnippet com.azure.storage.file.directoryClient.createSubDirectory#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @param subDirectoryName Name of the subdirectory
     * @return The subdirectory client.
     * @throws StorageException If the subdirectory has already existed, the parent directory does not exist or
     * directory is an invalid resource name.
     */
    public DirectoryClient createSubDirectory(String subDirectoryName) {
        return createSubDirectoryWithResponse(subDirectoryName, null, null, null, null, Context.NONE).getValue();
    }

    /**
     * Creates a subdirectory under current directory with specific name , metadata and returns a response of
     * DirectoryClient to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the subdirectory named "subdir", with metadata</p>
     *
     * {@codesnippet com.azure.storage.file.directoryClient.createSubDirectoryWithResponse#string-filesmbproperties-string-map-duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @param subDirectoryName Name of the subdirectory
     * @param smbProperties The SMB properties of the directory.
     * @param filePermission The file permission of the directory.
     * @param metadata Optional metadata to associate with the subdirectory
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the subdirectory client and the status of creating the directory.
     * @throws StorageException If the directory has already existed, the parent directory does not exist or
     * subdirectory is an invalid resource name.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    public Response<DirectoryClient> createSubDirectoryWithResponse(String subDirectoryName,
        FileSmbProperties smbProperties, String filePermission, Map<String, String> metadata, Duration timeout,
        Context context) {
        DirectoryClient directoryClient = getSubDirectoryClient(subDirectoryName);
        return new SimpleResponse<>(directoryClient
            .createWithResponse(smbProperties, filePermission, metadata, timeout, context), directoryClient);
    }

    /**
     * Deletes the subdirectory with specific name in this directory. The directory must be empty before it can be
     * deleted.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the subdirectory named "subdir"</p>
     *
     * {@codesnippet com.azure.storage.file.directoryClient.deleteSubDirectory#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-directory">Azure Docs</a>.</p>
     *
     * @param subDirectoryName Name of the subdirectory
     * @throws StorageException If the subdirectory doesn't exist, the parent directory does not exist or subdirectory
     * name is an invalid resource name.
     */
    public void deleteSubDirectory(String subDirectoryName) {
        deleteSubDirectoryWithResponse(subDirectoryName, null, Context.NONE);
    }

    /**
     * Deletes the subdirectory with specific name in this directory. The directory must be empty before it can be
     * deleted.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the subdirectory named "subdir"</p>
     *
     * {@codesnippet com.azure.storage.file.directoryClient.deleteSubDirectoryWithResponse#string-duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-directory">Azure Docs</a>.</p>
     *
     * @param subDirectoryName Name of the subdirectory
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @return A response that only contains headers and response status code
     * @throws StorageException If the subdirectory doesn't exist, the parent directory does not exist or subdirectory
     * name is an invalid resource name.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    public Response<Void> deleteSubDirectoryWithResponse(String subDirectoryName, Duration timeout, Context context) {
        Mono<Response<Void>> response = directoryAsyncClient.deleteSubDirectoryWithResponse(subDirectoryName, context);
        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Creates a file in this directory with specific name, max number of results and returns a response of
     * DirectoryInfo to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create 1k file with named "myFile"</p>
     *
     * {@codesnippet com.azure.storage.file.directoryClient.createFile#string-long}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-file">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file
     * @param maxSize Size of the file
     * @return The FileClient
     * @throws StorageException If the file has already existed, the parent directory does not exist or file name is an
     * invalid resource name.
     */
    public FileClient createFile(String fileName, long maxSize) {
        return createFileWithResponse(fileName, maxSize, null, null, null, null, null, Context.NONE).getValue();
    }

    /**
     * Creates a file in this directory with specific name and returns a response of DirectoryInfo to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the file named "myFile"</p>
     *
     * {@codesnippet com.azure.storage.file.directoryClient.createFile#string-long-filehttpheaders-filesmbproperties-string-map-duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-file">Azure Docs</a>.</p>
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
     * @throws StorageException If the directory has already existed, the parent directory does not exist or file name
     * is an invalid resource name.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    public Response<FileClient> createFileWithResponse(String fileName, long maxSize, FileHTTPHeaders httpHeaders,
        FileSmbProperties smbProperties, String filePermission, Map<String, String> metadata, Duration timeout,
        Context context) {
        FileClient fileClient = getFileClient(fileName);
        Response<FileInfo> response = fileClient.createWithResponse(maxSize, httpHeaders, smbProperties, filePermission,
            metadata, timeout, context);
        return new SimpleResponse<>(response, fileClient);
    }

    /**
     * Deletes the file with specific name in this directory.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the file "filetest"</p>
     *
     * {@codesnippet com.azure.storage.file.directoryClient.deleteFile#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-file2">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file
     * @throws StorageException If the directory doesn't exist or the file doesn't exist or file name is an invalid
     * resource name.
     */
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
     * {@codesnippet com.azure.storage.file.DirectoryClient.deleteFileWithResponse#string-duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-file2">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response that only contains headers and response status code
     * @throws StorageException If the directory doesn't exist or the file doesn't exist or file name is an invalid
     * resource name.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    public Response<Void> deleteFileWithResponse(String fileName, Duration timeout, Context context) {
        Mono<Response<Void>> response = directoryAsyncClient.deleteFileWithResponse(fileName, context);
        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Get snapshot id which attached to {@link DirectoryClient}. Return {@code null} if no snapshot id attached.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Get the share snapshot id. </p>
     *
     * {@codesnippet com.azure.storage.file.directoryClient.getShareSnapshotId}
     *
     * @return The snapshot id which is a unique {@code DateTime} value that identifies the share snapshot to its base
     * share.
     */
    public String getShareSnapshotId() {
        return directoryAsyncClient.getShareSnapshotId();
    }

    /**
     * Get the share name of directory client.
     *
     * <p>Get the share name. </p>
     *
     * {@codesnippet com.azure.storage.file.directoryClient.getShareName}
     *
     * @return The share name of the directory.
     */
    public String getShareName() {
        return directoryAsyncClient.getShareName();
    }

    /**
     * Get the directory path of the client.
     *
     * <p>Get directory path. </p>
     *
     * {@codesnippet com.azure.storage.file.directoryClient.getDirectoryPath}
     *
     * @return The path of the directory.
     */
    public String getDirectoryPath() {
        return directoryAsyncClient.getDirectoryPath();
    }
}
