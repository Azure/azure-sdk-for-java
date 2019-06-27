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
import java.util.Map;

/**
 * This class provides a client that contains all the operations for interacting with directory in Azure Storage File Service.
 * Operations allowed by the client are creating, deleting and listing subdirectory and file, retrieving properties, , setting metadata
 * and list or force close handles of the directory or file.
 *
 * <p><strong>Instantiating an Synchronous Directory Client</strong></p>
 *
 * <pre>
 * DirectoryClient client = DirectoryClient.builder()
 *     .connectionString(connectionString)
 *     .endpoint(endpoint)
 *     .build();
 * </pre>
 *
 * <p>View {@link DirectoryClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see DirectoryClientBuilder
 * @see DirectoryClient
 * @see SharedKeyCredential
 * @see SASTokenCredential
 */
public class DirectoryClient {

    public final DirectoryAsyncClient directoryAsyncClient;

    /**
     * Creates a DirectoryClient that wraps a DirectoryAsyncClient and blocks requests.
     *
     * @param directoryAsyncClient DirectoryAsyncClient that is used to send requests
     */
    DirectoryClient(DirectoryAsyncClient directoryAsyncClient) {
        this.directoryAsyncClient = directoryAsyncClient;
    }

    /**
     * Creates a builder that can configure options for the DirectoryClient before creating an instance of it.
     *
     * @return A new {@link DirectoryClientBuilder} used to create DirectoryClient instances.
     */
    public static DirectoryClientBuilder builder() {
        return new DirectoryClientBuilder();
    }

    /**
     * Get the url of the storage directory client.
     * @return the URL of the storage directory client
     */
    public String url() {
        return directoryAsyncClient.url();
    }

    /**
     * Constructs a FileClient that interacts with the specified file.
     *
     * <p>If the file doesn't exist in the storage account {@link FileClient#create(long)} create} in the client will
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
     * <p>If the file doesn't exist in the storage account {@link DirectoryClient#create()} create} in the client will
     * need to be called before interaction with the directory can happen.</p>
     *
     * @param directoryName Name of the directory
     * @return a DirectoryClient that interacts with the specified directory
     */
    public DirectoryClient getSubDirectoryClient(String directoryName) {
        return new DirectoryClient(directoryAsyncClient.getSubDirectoryClient(directoryName));
    }

    /**
     * Creates a directory in the storage account and returns a response of {@link DirectoryInfo} to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the directory</p>
     *
     * <pre>
     * Response&lt;DirectoryInfo&gt; response = client.create();
     * System.out.printf("Creating the directory completed with status code %d", response.statusCode());
     * </pre>
     *
     * @return A response containing the directory info and the status of creating the directory.
     * @throws StorageErrorException If the directory has already existed, the parent directory does not exist or directory name is an invalid resource name.
     */
    public Response<DirectoryInfo> create() {
        return create(null);
    }

    /**
     * Creates a directory in the storage account and returns a response of DirectoryInfo to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the directory</p>
     *
     * <pre>
     * Response&lt;DirectoryInfo&gt; response = client.create(Collections.singletonMap("directory", "metadata"));
     * System.out.printf("Creating the directory completed with status code %d", response.statusCode());
     * </pre>
     *
     * @param metadata Optional. Metadata to associate with the directory
     * @return A response containing the directory info and the status of creating the directory.
     * @throws StorageErrorException If the directory has already existed, the parent directory does not exist or directory name is an invalid resource name.
     */
    public Response<DirectoryInfo> create(Map<String, String> metadata) {
        return directoryAsyncClient.create(metadata).block();
    }

    /**
     * Deletes the directory in the storage account. The directory must be empty before it can be deleted.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the directory</p>
     *
     * <pre>
     * VoidResponse response = client.delete();
     * System.out.printf("Deleting the share completed with status code %d", response.statusCode());
     * </pre>
     *
     * @return A response that only contains headers and response status code
     * @throws StorageErrorException If the share doesn't exist
     */
    public VoidResponse delete() {
        return directoryAsyncClient.delete().block();
    }

    /**
     * Retrieves the properties of the storage account's directory.
     * The properties includes directory metadata, last modified date, is server encrypted, and eTag.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve directory properties</p>
     *
     * <pre>
     * Response&lt;DirectoryProperties&gt; response = client.getProperties();
     * System.out.printf("Directory latest modified date is %s.", properties.value().lastModified());
     * </pre>
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
     * <pre>
     * Response&ltDirectorySetMetadataInfo&gt response = client.setMetadata(Collections.singletonMap("directory", "updatedMetadata"));
     * System.out.printf("Setting the directory metadata completed with status code %d", response.statusCode());
     * </pre>
     *
     * <p>Clear the metadata of the directory</p>
     *
     * <pre>
     * client.setMetadata(null)
     *     .subscribe(response -&gt; System.out.printf("Clearing the directory metadata completed with status code %d", response.statusCode()));
     * </pre>
     *
     * @param metadata Optional. Metadata to set on the directory, if null is passed the metadata for the directory is cleared
     * @return information about the directory
     * @throws StorageErrorException If the directory doesn't exist or the metadata contains invalid keys
     */
    public Response<DirectorySetMetadataInfo> setMetadata(Map<String, String> metadata) {
        return directoryAsyncClient.setMetadata(metadata).block();
    }

    /**
     * Lists all directories and files in the storage account without their prefix or maxResult.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all directories and files in the account</p>
     *
     * <pre>
     * Iterable&lt;FileRef&gt; result = client.listFilesAndDirectories()
     * System.out.printf("The file or directory %s exists in the account", result.iterator().next().name());
     * </pre>
     *
     * @return {@link FileRef File info} in the storage directory
     */
    public Iterable<FileRef> listFilesAndDirectories() {
        return listFilesAndDirectories(null, null);
    }

    /**
     * Lists all shares in the storage account with their prefix or snapshots.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all directories with "subdir" prefix and return 10 results in the account</p>
     *
     * <pre>
     * Iterable&lt;FileRef&gt; result = client.listFilesAndDirectories("subdir", 10)
     * System.out.printf("The file or directory %s exists in the account", result.iterator().next().name());
     * </pre>
     *
     * @param prefix Optional. Filters the results to return only files and directories whose name begins with the specified prefix.
     * @param maxResults Optional. Specifies the maximum number of files and/or directories to return per page.
     *                   If the request does not specify maxresults or specifies a value greater than 5,000, the server will return up to 5,000 items.
     * @return {@link FileRef File info} in the storage account with prefix and max number of return results.
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
     * <pre>
     * Iterable&lt;HandleItem&gt; result = client.getHandles(10, true)
     * System.out.printf("Get handles completed with handle id %s", result.iterator().next().handleId());
     * </pre>
     * @param maxResult Optional. The number of results will return per page
     * @param recursive Specifies operation should apply to the directory specified in the URI, its files, its subdirectories and their files.
     * @return {@link HandleItem handles} in the directory that satisfy the requirements
     */
    public Iterable<HandleItem> getHandles(Integer maxResult, boolean recursive) {
        return directoryAsyncClient.getHandles(maxResult, recursive).collectList().block();
    }

    /**
     * Closes a handle or handles opened on a directory or a file at the service. It is intended to be used alongside {@link DirectoryClient#getHandles(Integer, boolean)} .
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Force close handles with handles returned by get handles in recursive.</p>
     *
     * <pre>
     * Iterable&lt;HandleItem&gt; result = client.getHandles(10, true)
     * result.forEach(handleItem -&gt;  {
     *    client.forceCloseHandles(handleItem.handleId, true).forEach(numOfClosedHandles -&gt
     *    System.out.printf("Get handles completed with handle id %s", handleItem.handleId()));
     * });
     * </pre>
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
     * <pre>
     * Response&lt;DirectoryClient&gt; response = client.createSubDirectory("subdir")
     * System.out.printf("Creating the sub directory completed with status code %d", response.statusCode());
     * </pre>
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
     * <pre>
     * Response&lt;DirectoryClient&gt; response = client.createSubDirectory("subdir", Collections.singletonMap("directory", "metadata"))
     * System.out.printf("Creating the subdirectory completed with status code %d", response.statusCode());
     * </pre>
     *
     * @param subDirectoryName Name of the subdirectory
     * @param metadata Optional. Metadata to associate with the subdirectory
     * @return A response containing the subdirectory client and the status of creating the directory.
     * @throws StorageErrorException If the directory has already existed, the parent directory does not exist or subdirectory is an invalid resource name.
     */
    public Response<DirectoryClient> createSubDirectory(String subDirectoryName, Map<String, String> metadata) {
        return directoryAsyncClient.createSubDirectory(subDirectoryName, metadata).map(this::mapDirectoryAsyncResponse).block();
    }

    /**
     * Deletes the subdirectory with specific name in the storage account. The directory must be empty before it can be deleted.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the subdirectory named "subdir"</p>
     *
     * <pre>
     * VoidResponse response = client.deleteSubDirectory("subdir")
     * System.out.printf("Deleting the subdirectory completed with status code %d", response.statusCode());
     * </pre>
     *
     * @param subDirectoryName Name of the subdirectory
     * @return A response that only contains headers and response status code
     * @throws StorageErrorException If the subdirectory doesn't exist, the parent directory does not exist or subdirectory name is an invalid resource name.
     */
    public VoidResponse deleteSubDirectory(String subDirectoryName) {
        return directoryAsyncClient.deleteSubDirectory(subDirectoryName).block();
    }

    /**
     * Creates a file in the storage account with specific name, max number of results and returns a response of DirectoryInfo to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create 1k file with named "myFile"</p>
     *
     * <pre>
     * Response&lt;FileClient&gt; response =  client.createFile("myFile", 1024)
     * System.out.printf("Creating the file completed with status code %d", response.statusCode());
     * </pre>
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
     * Creates a file in the storage account with specific name and returns a response of DirectoryInfo to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the file named "myFile"</p>
     *
     * <pre>
     * Response&lt;FileClient&gt; response = client.createFile("myFile", Collections.singletonMap("directory", "metadata"))
     * System.out.printf("Creating the file completed with status code %d", response.statusCode());
     * </pre>
     *
     * @param fileName Name of the file
     * @param maxSize Max size of the file
     * @param httpHeaders the Http headers set to the file
     * @param metadata Optional. Name-value pairs associated with the file as metadata. Metadata names must adhere to the naming rules.
     * @return A response containing the directory info and the status of creating the directory.
     * @throws StorageErrorException If the directory has already existed, the parent directory does not exist or file name is an invalid resource name.
     */
    public Response<FileClient> createFile(String fileName, long maxSize, FileHTTPHeaders httpHeaders, Map<String, String> metadata) {
        return directoryAsyncClient.createFile(fileName, maxSize, httpHeaders, metadata)
            .map(response -> FileAsyncClient.mapResponse(response, new FileClient(response.value()))).block();
    }

    /**
     * Deletes the file with specific name in the storage account.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the file "filetest"</p>
     *
     * <pre>
     * VoidResponse response = client.deleteFile("filetest")
     * System.out.printf("Deleting the file completed with status code %d", response.statusCode());
     * </pre>
     *
     * @param fileName Name of the file
     * @return A response that only contains headers and response status code
     * @throws StorageErrorException If the directory doesn't exist or the file doesn't exist or file name is an invalid resource name.
     */
    public VoidResponse deleteFile(String fileName) {
        return directoryAsyncClient.deleteFile(fileName).block();
    }

    private Response<DirectoryClient> mapDirectoryAsyncResponse(Response response) {
        DirectoryClient directoryClient = new DirectoryClient((DirectoryAsyncClient) response.value());
        return new SimpleResponse<DirectoryClient>(response.request(), response.statusCode(), response.headers(), directoryClient);
    }

}
