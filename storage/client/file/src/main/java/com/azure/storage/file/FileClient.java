// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.file.models.FileCopyInfo;
import com.azure.storage.file.models.FileDownloadInfo;
import com.azure.storage.file.models.FileHTTPHeaders;
import com.azure.storage.file.models.FileInfo;
import com.azure.storage.file.models.FileMetadataInfo;
import com.azure.storage.file.models.FileProperties;
import com.azure.storage.file.models.FileRange;
import com.azure.storage.file.models.FileRangeWriteType;
import com.azure.storage.file.models.FileUploadInfo;
import com.azure.storage.file.models.HandleItem;
import com.azure.storage.file.models.StorageErrorException;
import io.netty.buffer.ByteBuf;
import java.net.URL;
import java.util.Map;
import reactor.core.publisher.Flux;

/**
 * This class provides a client that contains all the operations for interacting files under Azure Storage File Service.
 * Operations allowed by the client are creating, uploading, copying, listing, downloading, and deleting files.
 *
 * <p><strong>Instantiating a synchronous File Client</strong></p>
 *
 * <pre>
 * FileClient client = FileClient.builder()
 *        .connectionString(connectionString)
 *        .endpoint(endpoint)
 *        .buildClient();
 * </pre>
 *
 * <p>View {@link FileClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see FileClientBuilder
 * @see FileAsyncClient
 * @see SharedKeyCredential
 * @see SASTokenCredential
 */
public class FileClient {
    private final FileAsyncClient fileAsyncClient;

    /**
     * Creates a FileClient that wraps a FileAsyncClient and blocks requests.
     *
     * @param fileAsyncClient FileAsyncClient that is used to send requests
     */
    FileClient(FileAsyncClient fileAsyncClient) {
        this.fileAsyncClient = fileAsyncClient;
    }

    /**
     * Get the url of the storage file client.
     * @return the URL of the storage file client.
     * @throws RuntimeException If the file is using a malformed URL.
     */
    public URL getFileUrl() {
        return fileAsyncClient.getFileUrl();
    }

    /**
     * Creates a file in the storage account and returns a response of {@link FileInfo} to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the file with length of 1024 bytes, some headers and metadata.</p>
     *
     * {@codesnippet com.azure.storage.file.fileClient.create}
     *
     * @param maxSize The maximum size in bytes for the file, up to 1 TiB.
     * @return A response containing the file info and the status of creating the file.
     * @throws StorageErrorException If the file has already existed, the parent directory does not exist or fileName is an invalid resource name.
     */
    public Response<FileInfo> create(long maxSize) {
        return fileAsyncClient.create(maxSize).block();
    }

    /**
     * Creates a file in the storage account and returns a response of FileInfo to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the file with length of 1024 bytes, some headers and metadata.</p>
     *
     * <pre>
     * FileHTTPHeaders httpHeaders = new FileHTTPHeaders().fileContentType("text/plain");
     * Response&lt;FileInfo&gt; response = client.create(1024, httpHeaders, Collections.singletonMap("file", "updatedMetadata"));
     * System.out.printf("Creating the file completed with status code %d", response.statusCode());
     * </pre>
     *
     * @param maxSize The maximum size in bytes for the file, up to 1 TiB.
     * @param httpHeaders Additional parameters for the operation.
     * @param metadata Optional. Name-value pairs associated with the file as metadata. Metadata names must adhere to the naming rules.
     *                           @see <a href="https://docs.microsoft.com/en-us/dotnet/csharp/language-reference/">C# identifiers</a>
     * @return A response containing the directory info and the status of creating the directory.
     * @throws StorageErrorException If the directory has already existed, the parent directory does not exist or directory is an invalid resource name.
     */
    public Response<FileInfo> create(long maxSize, FileHTTPHeaders httpHeaders, Map<String, String> metadata) {
        return fileAsyncClient.create(maxSize, httpHeaders, metadata).block();
    }

    /**
     * Copies a blob or file to a destination file within the storage account.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Copy file from source getDirectoryUrl to the {@code filePath} </p>
     *
     * {@codesnippet com.azure.storage.file.fileClient.startCopy#string-map}
     *
     * @param sourceUrl Specifies the URL of the source file or blob, up to 2 KB in length.
     * @param metadata Optional. Name-value pairs associated with the file as metadata. Metadata names must adhere to the naming rules.
     *      *                           @see <a href="https://docs.microsoft.com/en-us/dotnet/csharp/language-reference/">C# identifiers</a>
     * @return A response containing the file copy info and the status of copying the file.
     */
    public Response<FileCopyInfo> startCopy(String sourceUrl, Map<String, String> metadata) {
        return fileAsyncClient.startCopy(sourceUrl, metadata).block();
    }

    /**
     * Aborts a pending Copy File operation, and leaves a destination file with zero length and full metadata.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Abort copy file from copy id("someCopyId") </p>
     *
     * <pre>
     * VoidResponse response = client.abortCopy("someCopyId")
     * System.out.printf("Abort copying the file completed with status code %d", response.statusCode());
     * </pre>
     *
     * @param copyId Specifies the copy id which has copying pending status associate with it.
     * @return A response containing the status of aborting copy the file.
     */
    public VoidResponse abortCopy(String copyId) {
        return fileAsyncClient.abortCopy(copyId).block();
    }

    /**
     * Downloads a file from the system, including its metadata and properties
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Download the file to current folder. </p>
     *
     * {@codesnippet com.azure.storage.file.fileClient.downloadToFile}
     *
     * @param downloadFilePath The path where store the downloaded file
     */
    public void downloadToFile(String downloadFilePath) {
        downloadToFile(downloadFilePath, null);
    }

    /**
     * Downloads a file from the system, including its metadata and properties
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Download the file from 1024 to 2048 bytes to current folder. </p>
     *
     * <pre>
     * client.downloadToFile("someFilePath", new FileRange(1024, 2048));
     * if (Files.exist(Paths.get(downloadFilePath))) {
     *      System.out.println("Download the file completed");
     * }
     * </pre>
     *
     * @param downloadFilePath The path where store the downloaded file
     * @param range Optional. Return file data only from the specified byte range.
     */
    public void downloadToFile(String downloadFilePath, FileRange range) {
        fileAsyncClient.downloadToFile(downloadFilePath, range).block();
    }

    /**
     * Downloads a file from the system, including its metadata and properties
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Download the file with its metadata and properties. </p>
     *
     * {@codesnippet com.azure.storage.file.fileClient.downloadWithProperties}
     *
     * @return A response that only contains headers and response status code
     */
    public Response<FileDownloadInfo> downloadWithProperties() {
        return fileAsyncClient.downloadWithProperties(null, null).block();
    }

    /**
     * Downloads a file from the system, including its metadata and properties
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Download the file from 1024 to 2048 bytes with its metadata and properties and without the contentMD5. </p>
     *
     * <pre>
     * Response&lt;FileDownloadInfo&gt; response = client.downloadWithProperties()
     * System.out.printf("Downloading the file completed with status code %d", response.statusCode());
     * </pre>
     *
     * @param range Optional. Return file data only from the specified byte range.
     * @param rangeGetContentMD5 Optional. When this header is set to true and specified together with the Range header, the service returns the MD5 hash for the range, as long as the range is less than or equal to 4 MB in size.
     * @return A response that only contains headers and response status code
     */
    public Response<FileDownloadInfo> downloadWithProperties(FileRange range, Boolean rangeGetContentMD5) {
        return fileAsyncClient.downloadWithProperties(range, rangeGetContentMD5).block();
    }

    /**
     * Deletes the file associate with the client.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the file</p>
     *
     * {@codesnippet com.azure.storage.file.fileClient.delete}
     *
     * @return A response that only contains headers and response status code
     * @throws StorageErrorException If the directory doesn't exist or the file doesn't exist.
     */
    public VoidResponse delete() {
        return fileAsyncClient.delete().block();
    }

    /**
     * Retrieves the properties of the storage account's file.
     * The properties includes file metadata, last modified date, is server encrypted, and eTag.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve file properties</p>
     *
     * <pre>
     * Response&lt;FileProperties&gt; response = client.getProperties()
     * DirectoryProperties properties = response.value();
     * System.out.printf("File latest modified date is %s.", properties.lastModified());
     * </pre>
     *
     * @return Storage file properties
     */
    public Response<FileProperties> getProperties() {
        return fileAsyncClient.getProperties().block();
    }

    /**
     * Sets the user-defined httpHeaders to associate to the file.
     *
     * <p>If {@code null} is passed for the httpHeaders it will clear the httpHeaders associated to the file.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set the httpHeaders of contentType of "text/plain"</p>
     *
     * <pre>
     * FileHTTPHeaders httpHeaders = new FileHTTPHeaders().fileContentType("text/plain");
     * Response&lt;FileInfo&gt; response = client.setHttpHeaders(1024, httpHeaders);
     * System.out.printf("Setting the file httpHeaders completed with status code %d", response.statusCode());
     * </pre>
     *
     * <p>Clear the metadata of the file</p>
     *
     * <pre>
     * Response&lt;FileInfo&gt; response = client.setHttpHeaders(1024, null)
     * System.out.printf("Clearing the file httpHeaders completed with status code %d", response.statusCode());
     * </pre>
     *
     * @param newFileSize New file size of the file
     * @param httpHeaders Resizes a file to the specified size. If the specified byte value is less than the current size of the file, then all ranges above the specified byte value are cleared.
     * @return Response of the information about the file
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     */
    public Response<FileInfo> setHttpHeaders(long newFileSize, FileHTTPHeaders httpHeaders) {
        return fileAsyncClient.setHttpHeaders(newFileSize, httpHeaders).block();
    }

    /**
     * Sets the user-defined metadata to associate to the file.
     *
     * <p>If {@code null} is passed for the metadata it will clear the metadata associated to the file.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set the metadata to "file:updatedMetadata"</p>
     *
     * <pre>
     * Response&lt;FileMetadataInfo&gt; response = client.setMetadata(Collections.singletonMap("file", "updatedMetadata"));
     * System.out.printf("Setting the file metadata completed with status code %d", response.statusCode());
     * </pre>
     *
     * <p>Clear the metadata of the file</p>
     *
     * <pre>
     * client.setMetadata(null)
     *     .subscribe(response -&gt; System.out.printf("Clearing the file metadata completed with status code %d", response.statusCode()));
     * </pre>
     *
     * @param metadata Options.Metadata to set on the file, if null is passed the metadata for the file is cleared
     * @return information about the file
     * @throws StorageErrorException If the file doesn't exist or the metadata contains invalid keys
     */
    public Response<FileMetadataInfo> setMeatadata(Map<String, String> metadata) {
        return fileAsyncClient.setMetadata(metadata).block();
    }

    /**
     * Uploads a range of bytes to the beginning of a file in storage file service. Upload operations performs an in-place write on the specified file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Upload "default" to the file. </p>
     *
     * {@codesnippet com.azure.storage.file.fileClient.upload}
     *
     * @param data The data which will upload to the storage file.
     * @param length Specifies the number of bytes being transmitted in the request body. When the FileRangeWriteType is set to clear, the value of this header must be set to zero..
     * @return A response that only contains headers and response status code
     * @throws StorageErrorException If you attempt to upload a range that is larger than 4 MB, the service returns status code 413 (Request Entity Too Large)
     */
    public Response<FileUploadInfo> upload(ByteBuf data, long length) {
        return fileAsyncClient.upload(Flux.just(data), length).block();
    }

    /**
     * Uploads a range of bytes to specific of a file in storage file service. Upload operations performs an in-place write on the specified file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Upload the file from 1024 to 2048 bytes with its metadata and properties and without the contentMD5. </p>
     *
     * <pre>
     * ByteBuf defaultData = Unpooled.wrappedBuffer("default".getBytes(StandardCharsets.UTF_8));
     * Response&lt;FileUploadInfo&gt; response = client.upload(defaultData, defaultData.readableBytes());
     * System.out.printf("Upload the bytes to file range completed with status code %d", response.statusCode());
     * </pre>
     *
     * @param data The data which will upload to the storage file.
     * @param offset Optional. The starting point of the upload range. It will start from the beginning if it is {@code null}
     * @param length Specifies the number of bytes being transmitted in the request body. When the FileRangeWriteType is set to clear, the value of this header must be set to zero.
     * @param type You may specify one of the following options:
     *              - Update: Writes the bytes specified by the request body into the specified range.
     *              - Clear: Clears the specified range and releases the space used in storage for that range. To clear a range, set the Content-Length header to zero.
     * @return A response that only contains headers and response status code
     * @throws StorageErrorException If you attempt to upload a range that is larger than 4 MB, the service returns status code 413 (Request Entity Too Large)
     */
    public Response<FileUploadInfo> upload(ByteBuf data, long length, int offset, FileRangeWriteType type) {
        return fileAsyncClient.upload(Flux.just(data), length, offset, type).block();
    }

    /**
     * Uploads file to storage file service.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Upload the file from the source file path. </p>
     *
     * {@codesnippet com.azure.storage.file.fileClient.uploadFromFile}
     *
     * @param uploadFilePath The path where store the source file to upload
     */
    public void uploadFromFile(String uploadFilePath) {
        uploadFromFile(uploadFilePath, FileRangeWriteType.UPDATE);
    }

    /**
     * Uploads file to storage file service.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p> Upload the file from the source file path. </p>
     *
     * <pre>
     * client.uploadFromFile("someFilePath", FileRangeWriteType.UPDATE);
     * if (client.getProperties() != null) {
     *     System.out.printf("Upload the file with length of %d completed", client.getProperties().block().value().contentLength());
     * };
     * </pre>
     *
     * @param uploadFilePath The path where store the source file to upload
     * @param type You may specify one of the following options:
     *              - Update: Writes the bytes specified by the request body into the specified range.
     *              - Clear: Clears the specified range and releases the space used in storage for that range. To clear a range, set the Content-Length header to zero.
     */
    public void uploadFromFile(String uploadFilePath, FileRangeWriteType type) {
        fileAsyncClient.uploadFromFile(uploadFilePath, type).block();
    }

    /**
     * List of valid ranges for a file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all ranges for the file client.</p>
     *
     * <pre>
     * Iterable&lt;FileRange&gt; ranges = client.listRanges();
     * ranges.forEach(range -&gt
     *      System.out.printf("List ranges completed with start: %d, end: %d", range.start(), range.end()));
     * </pre>
     *
     * @return {@link FileRange ranges} in the files.
     */
    public Iterable<FileRange> listRanges() {
        return fileAsyncClient.listRanges(null).toIterable();
    }

    /**
     * List of valid ranges for a file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all ranges within the file range from 1KB to 2KB.</p>
     *
     * <pre>
     * Iterable%lt;FileRange&gt; ranges = client.listRanges(new FileRange(1024, 2048));
     * ranges.forEach(range -&gt
     *      System.out.printf("List ranges completed with start: %d, end: %d", range.start(), range.end()));
     * </pre>
     *
     * @param range Optional. Return file data only from the specified byte range.
     * @return {@link FileRange ranges} in the files that satisfy the requirements
     */
    public Iterable<FileRange> listRanges(FileRange range) {
        return fileAsyncClient.listRanges(range).toIterable();
    }

    /**
     * List of open handles on a file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all handles for the file client.</p>
     *
     * <pre>
     * client.listHandles()
     *     .forEach(handleItem -&gt; System.out.printf("List handles completed with handleId %d", handleItem.handleId()));
     * </pre>
     *
     * @return {@link HandleItem handles} in the files that satisfy the requirements
     */
    public Iterable<HandleItem> listHandles() {
        return listHandles(null);
    }

    /**
     * List of open handles on a file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List 10 handles for the file client.</p>
     *
     * <pre>
     * client.listHandles(10)
     *     .forEach(handleItem -&gt; System.out.printf("List handles completed with handleId %d", handleItem.handleId()));
     * </pre>
     * @param maxResults Optional. The number of results will return per page
     * @return {@link HandleItem handles} in the file that satisfy the requirements
     */
    public Iterable<HandleItem> listHandles(Integer maxResults) {
        return fileAsyncClient.listHandles(maxResults).toIterable();
    }

    /**
     * Closes a handle or handles opened on a file at the service. It is intended to be used alongside {@link FileClient#listHandles()} (Integer)} .
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Force close handles with handles returned by list handles in recursive.</p>
     *
     * <pre>
     * client.listHandles(10)
     *     .forEach(result -&gt; {
     *         client.forceCloseHandles(result.handleId(), true).subscribe(numOfClosedHandles -&gt
     *              System.out.printf("Close %d handles.", numOfClosedHandles)
     *     )});
     * </pre>
     * @param handleId Specifies the handle ID to be closed. Use an asterisk ('*') as a wildcard string to specify all handles.
     * @return The counts of number of handles closed
     */
    public Iterable<Integer> forceCloseHandles(String handleId) {
        return fileAsyncClient.forceCloseHandles(handleId).toIterable();
    }
}

