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
 * {@codesnippet com.azure.storage.file.fileClient.instantiation}
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
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-file">Azure Docs</a>.</p>
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
     * {@codesnippet com.azure.storage.file.fileClient.create#long-filehttpheaders-map}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-file">Azure Docs</a>.</p>
     *
     * @param maxSize The maximum size in bytes for the file, up to 1 TiB.
     * @param httpHeaders Additional parameters for the operation.
     * @param metadata Optional name-value pairs associated with the file as metadata. Metadata names must adhere to the naming rules.
     * @see <a href="https://docs.microsoft.com/en-us/dotnet/csharp/language-reference/">C# identifiers</a>
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
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/copy-file">Azure Docs</a>.</p>
     *
     * @param sourceUrl Specifies the URL of the source file or blob, up to 2 KB in length.
     * @param metadata Optional name-value pairs associated with the file as metadata. Metadata names must adhere to the naming rules.
     * @see <a href="https://docs.microsoft.com/en-us/dotnet/csharp/language-reference/">C# identifiers</a>
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
     * {@codesnippet com.azure.storage.file.fileClient.abortCopy#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/abort-copy-file">Azure Docs</a>.</p>
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
     * {@codesnippet com.azure.storage.file.fileClient.downloadToFile#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-file">Azure Docs</a>.</p>
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
     * {@codesnippet com.azure.storage.file.fileClient.downloadToFile#string-filerange}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-file">Azure Docs</a>.</p>
     *
     * @param downloadFilePath The path where store the downloaded file
     * @param range Optional byte range which returns file data only from the specified range.
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
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-file">Azure Docs</a>.</p>
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
     * {@codesnippet com.azure.storage.file.fileClient.downloadWithProperties#filerange-boolean}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-file">Azure Docs</a>.</p>
     *
     * @param range Optional byte range which returns file data only from the specified range.
     * @param rangeGetContentMD5 Optional boolean which the service returns the MD5 hash for the range when it sets to true, as long as the range is less than or equal to 4 MB in size.
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
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-file2">Azure Docs</a>.</p>
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
     * {@codesnippet com.azure.storage.file.fileClient.getProperties}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-file-properties">Azure Docs</a>.</p>
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
     * {@codesnippet com.azure.storage.file.fileClient.setHttpHeaders#long-filehttpheaders}
     *
     * <p>Clear the httpHeaders of the file</p>
     *
     * {@codesnippet com.azure.storage.file.fileClient.setHttpHeaders#long-filehttpheaders.clearHttpHeaders}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-file-properties">Azure Docs</a>.</p>
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
     * {@codesnippet com.azure.storage.file.fileClient.setMetadata#map}
     *
     * <p>Clear the metadata of the file</p>
     *
     * {@codesnippet com.azure.storage.file.fileClient.setMetadata#map.clearMetadata}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-file-metadata">Azure Docs</a>.</p>
     *
     * @param metadata Options.Metadata to set on the file, if null is passed the metadata for the file is cleared
     * @return information about the file
     * @throws StorageErrorException If the file doesn't exist or the metadata contains invalid keys
     */
    public Response<FileMetadataInfo> setMetadata(Map<String, String> metadata) {
        return fileAsyncClient.setMetadata(metadata).block();
    }

    /**
     * Uploads a range of bytes to the beginning of a file in storage file service. Upload operations performs an in-place write on the specified file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Upload "default" to the file. </p>
     *
     * {@codesnippet com.azure.storage.file.fileClient.upload#flux-long}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/put-range">Azure Docs</a>.</p>
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
     * {@codesnippet com.azure.storage.file.fileClient.upload#bytebuf-long-int-filerangewritetype}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/put-range">Azure Docs</a>.</p>
     *
     * @param data The data which will upload to the storage file.
     * @param offset Optional starting point of the upload range. It will start from the beginning if it is {@code null}
     * @param length Specifies the number of bytes being transmitted in the request body. When the FileRangeWriteType is set to clear, the value of this header must be set to zero.
     * @param type You may specify one of the following options:
     * <ul>
     *      <li>Update: Writes the bytes specified by the request body into the specified range.</li>
     *      <li>Clear: Clears the specified range and releases the space used in storage for that range. To clear a range, set the Content-Length header to zero.</li>
     * <ul/>
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
     * {@codesnippet com.azure.storage.file.fileClient.uploadFromFile#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-file">Azure Docs Create File</a>
     * and
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/put-range">Azure Docs Upload</a>.</p>
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
     * {@codesnippet com.azure.storage.file.fileClient.uploadFromFile#string-filerangewritetype}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-file">Azure Docs Create File</a>
     * and
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/put-range">Azure Docs Upload</a>.</p>
     *
     * @param uploadFilePath The path where store the source file to upload
     * @param type You may specify one of the following options:
     * <ul>
     *      <li>Update: Writes the bytes specified by the request body into the specified range.</li>
     *      <li>Clear: Clears the specified range and releases the space used in storage for that range. To clear a range, set the Content-Length header to zero.</li>
     * <ul/>
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
     * {@codesnippet com.azure.storage.file.fileClient.listRanges}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/list-ranges">Azure Docs</a>.</p>
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
     * {@codesnippet com.azure.storage.file.fileClient.listRanges#filerange}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/list-ranges">Azure Docs</a>.</p>
     *
     * @param range Optional byte range which returns file data only from the specified range.
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
     * {@codesnippet com.azure.storage.file.fileClient.listHandles}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/list-handles">Azure Docs</a>.</p>
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
     * {@codesnippet com.azure.storage.file.fileClient.listHandles#integer}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/list-handles">Azure Docs</a>.</p>
     *
     * @param maxResults Optional max number of results returned per page
     * @return {@link HandleItem handles} in the file that satisfy the requirements
     */
    public Iterable<HandleItem> listHandles(Integer maxResults) {
        return fileAsyncClient.listHandles(maxResults).toIterable();
    }

    /**
     * Closes a handle or handles opened on a file at the service. It is intended to be used alongside {@link FileClient#listHandles()} (Integer)} .
     * TODO: Will change the return type to how many handles have been closed. Implement one more API to force close all handles.
     * TODO: @see <a href="https://github.com/Azure/azure-sdk-for-java/issues/4525">Github Issue 4525</a>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Force close handles with handles returned by list handles in recursive.</p>
     *
     * {@codesnippet com.azure.storage.file.fileClient.forceCloseHandles#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/force-close-handles">Azure Docs</a>.</p>
     *
     * @param handleId Specifies the handle ID to be closed. Use an asterisk ('*') as a wildcard string to specify all handles.
     * @return The counts of number of handles closed
     */
    public Iterable<Integer> forceCloseHandles(String handleId) {
        return fileAsyncClient.forceCloseHandles(handleId).toIterable();
    }

    /**
     * Get snapshot id which attached to {@link FileClient}.
     * Return {@code null} if no snapshot id attached.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Get the share snapshot id. </p>
     *
     * {@codesnippet com.azure.storage.file.fileClient.getShareSnapshotId}
     *
     * @return The snapshot id which is a unique {@code DateTime} value that identifies the share snapshot to its base share.
     */
    public String getShareSnapshotId() {
        return fileAsyncClient.getShareSnapshotId();
    }
}

