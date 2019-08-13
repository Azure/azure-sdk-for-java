// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.http.rest.VoidResponse;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.file.models.DirectoryInfo;
import com.azure.storage.file.models.DirectoryProperties;
import com.azure.storage.file.models.DirectorySetMetadataInfo;
import com.azure.storage.file.models.FileHTTPHeaders;
import com.azure.storage.file.models.FileRef;
import com.azure.storage.file.models.HandleItem;
import com.azure.storage.file.models.StorageErrorException;
import java.net.URL;
import java.util.Map;

/**
 * This class provides a client that contains all the operations for interacting with directory in Azure Storage File Service.
 * Operations allowed by the client are creating, deleting and listing subdirectory and file, retrieving properties, , setting metadata
 * and list or force close handles of the directory or file.
 *
 * <p><strong>Instantiating an Synchronous Directory Client</strong></p>
 *
 * {@codesnippet com.azure.storage.file.directoryClient.instantiation}
 *
 * <p>View {@link DirectoryClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see DirectoryClientBuilder
 * @see DirectoryClient
 * @see SharedKeyCredential
 * @see SASTokenCredential
 */
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
     * @return A response containing the directory info and the status of creating the directory.
     * @throws StorageErrorException If the directory has already existed, the parent directory does not exist or directory name is an invalid resource name.
     */
    public Response<DirectoryInfo> create() {
        return create(null);
    }

    /**
     * Creates a directory in the file share and returns a response of DirectoryInfo to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the directory</p>
     *
     * {@codesnippet com.azure.storage.file.directoryClient.create#map}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @param metadata Optional metadata to associate with the directory.
     * @return A response containing the directory info and the status of creating the directory.
     * @throws StorageErrorException If the directory has already existed, the parent directory does not exist or directory name is an invalid resource name.
     */
    public Response<DirectoryInfo> create(Map<String, String> metadata) {
        return directoryAsyncClient.create(metadata).block();
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
     * @return A response that only contains headers and response status code
     * @throws StorageErrorException If the share doesn't exist
     */
    public VoidResponse delete() {
        return directoryAsyncClient.delete().block();
    }

    /**
     * Retrieves the properties of this directory.
     * The properties includes directory metadata, last modified date, is server encrypted, and eTag.
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
    public Response<DirectoryProperties> getProperties() {
        return directoryAsyncClient.getProperties().block();
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
     * @param metadata Optional metadata to set on the directory, if null is passed the metadata for the directory is cleared
     * @return information about the directory
     * @throws StorageErrorException If the directory doesn't exist or the metadata contains invalid keys
     */
    public Response<DirectorySetMetadataInfo> setMetadata(Map<String, String> metadata) {
        return directoryAsyncClient.setMetadata(metadata).block();
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
    public Iterable<FileRef> listFilesAndDirectories() {
        return listFilesAndDirectories(null, null);
    }

    /**
     * Lists all sub-directories and files in this directory with their prefix or snapshots.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all sub-directories and files in this directory with "subdir" prefix and return 10 results in the account</p>
     *
     * {@codesnippet com.azure.storage.file.directoryClient.listFilesAndDirectories#string-integer}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/list-directories-and-files">Azure Docs</a>.</p>
     *
     * @param prefix Optional prefix which filters the results to return only files and directories whose name begins with.
     * @param maxResults Optional maximum number of files and/or directories to return per page.
     *                   If the request does not specify maxresults or specifies a value greater than 5,000, the server will return up to 5,000 items.
     * @return {@link FileRef File info} in this directory with prefix and max number of return results.
     */
    public Iterable<FileRef> listFilesAndDirectories(String prefix, Integer maxResults) {
        return directoryAsyncClient.listFilesAndDirectories(prefix, maxResults).toIterable();
    }

    /**
     * List of open handles on a directory or a file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Get 10 handles with recursive call.</p>
     *
     * {@codesnippet com.azure.storage.file.directoryClient.getHandles}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/list-handles">Azure Docs</a>.</p>
     *
     * @param maxResult Optional maximum number of results will return per page
     * @param recursive Specifies operation should apply to the directory specified in the URI, its files, its subdirectories and their files.
     * @return {@link HandleItem handles} in the directory that satisfy the requirements
     */
    public Iterable<HandleItem> getHandles(Integer maxResult, boolean recursive) {
        return directoryAsyncClient.getHandles(maxResult, recursive).collectList().block();
    }

    /**
     * Closes a handle or handles opened on a directory or a file at the service. It is intended to be used alongside {@link DirectoryClient#getHandles(Integer, boolean)} .
     * TODO: Will change the return type to how many handles have been closed. Implement one more API to force close all handles.
     * TODO: @see <a href="https://github.com/Azure/azure-sdk-for-java/issues/4525">Github Issue 4525</a>
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
     * @param handleId Specifies the handle ID to be closed. Use an asterisk ('*') as a wildcard string to specify all handles.
     * @param recursive A boolean value that specifies if the operation should also apply to the files and subdirectories of the directory specified in the URI.
     * @return The counts of number of handles closed.
     */
    public Iterable<Integer> forceCloseHandles(String handleId, boolean recursive) {
        return directoryAsyncClient.forceCloseHandles(handleId, recursive).collectList().block();
    }

    /**
     * Creates a subdirectory under current directory with specific name and returns a response of DirectoryClient to interact with it.
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
     * @return A response containing the subdirectory client and the status of creating the directory.
     * @throws StorageErrorException If the subdirectory has already existed, the parent directory does not exist or directory is an invalid resource name.
     */
    public Response<DirectoryClient> createSubDirectory(String subDirectoryName) {
        return createSubDirectory(subDirectoryName, null);
    }

    /**
     * Creates a subdirectory under current directory with specific name , metadata and returns a response of DirectoryClient to interact with it.
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
    public Response<DirectoryClient> createSubDirectory(String subDirectoryName, Map<String, String> metadata) {
        DirectoryClient directoryClient = getSubDirectoryClient(subDirectoryName);
        return new SimpleResponse<>(directoryClient.create(metadata), directoryClient);
    }

    /**
     * Deletes the subdirectory with specific name in this directory. The directory must be empty before it can be deleted.
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
     * @return A response that only contains headers and response status code
     * @throws StorageErrorException If the subdirectory doesn't exist, the parent directory does not exist or subdirectory name is an invalid resource name.
     */
    public VoidResponse deleteSubDirectory(String subDirectoryName) {
        return directoryAsyncClient.deleteSubDirectory(subDirectoryName).block();
    }

    /**
     * Creates a file in this directory with specific name, max number of results and returns a response of DirectoryInfo to interact with it.
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
     * @return A response containing the FileClient and the status of creating the directory.
     * @throws StorageErrorException If the file has already existed, the parent directory does not exist or file name is an invalid resource name.
     */
    public Response<FileClient> createFile(String fileName, long maxSize) {
        return createFile(fileName, maxSize, null, null);
    }

    /**
     * Creates a file in this directory with specific name and returns a response of DirectoryInfo to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the file named "myFile"</p>
     *
     * {@codesnippet com.azure.storage.file.directoryClient.createFile#string-long-fileHTTPHeaders-map}
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
    public Response<FileClient> createFile(String fileName, long maxSize, FileHTTPHeaders httpHeaders, Map<String, String> metadata) {
        return directoryAsyncClient.createFile(fileName, maxSize, httpHeaders, metadata)
            .map(response -> new SimpleResponse<>(response, new FileClient(response.value()))).block();
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
     * @return A response that only contains headers and response status code
     * @throws StorageErrorException If the directory doesn't exist or the file doesn't exist or file name is an invalid resource name.
     */
    public VoidResponse deleteFile(String fileName) {
        return directoryAsyncClient.deleteFile(fileName).block();
    }

    /**
     * Get snapshot id which attached to {@link DirectoryClient}.
     * Return {@code null} if no snapshot id attached.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Get the share snapshot id. </p>
     *
     * {@codesnippet com.azure.storage.file.directoryClient.getShareSnapshotId}
     *
     * @return The snapshot id which is a unique {@code DateTime} value that identifies the share snapshot to its base share.
     */
    public String getShareSnapshotId() {
        return directoryAsyncClient.getShareSnapshotId();
    }
}
