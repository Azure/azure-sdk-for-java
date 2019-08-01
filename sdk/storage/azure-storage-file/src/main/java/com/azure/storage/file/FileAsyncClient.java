// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.implementation.util.FluxUtil;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.file.implementation.AzureFileStorageBuilder;
import com.azure.storage.file.implementation.AzureFileStorageImpl;
import com.azure.storage.file.models.CopyStatusType;
import com.azure.storage.file.models.FileCopyInfo;
import com.azure.storage.file.models.FileDownloadInfo;
import com.azure.storage.file.models.FileGetPropertiesHeaders;
import com.azure.storage.file.models.FileHTTPHeaders;
import com.azure.storage.file.models.FileInfo;
import com.azure.storage.file.models.FileMetadataInfo;
import com.azure.storage.file.models.FileProperties;
import com.azure.storage.file.models.FileRange;
import com.azure.storage.file.models.FileRangeWriteType;
import com.azure.storage.file.models.FileUploadInfo;
import com.azure.storage.file.models.FileUploadRangeHeaders;
import com.azure.storage.file.models.FilesCreateResponse;
import com.azure.storage.file.models.FilesDownloadResponse;
import com.azure.storage.file.models.FilesForceCloseHandlesResponse;
import com.azure.storage.file.models.FilesGetPropertiesResponse;
import com.azure.storage.file.models.FilesGetRangeListResponse;
import com.azure.storage.file.models.FilesListHandlesResponse;
import com.azure.storage.file.models.FilesSetHTTPHeadersResponse;
import com.azure.storage.file.models.FilesSetMetadataResponse;
import com.azure.storage.file.models.FilesStartCopyResponse;
import com.azure.storage.file.models.FilesUploadRangeResponse;
import com.azure.storage.file.models.HandleItem;
import com.azure.storage.file.models.StorageErrorException;
import io.netty.buffer.ByteBuf;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * This class provides a client that contains all the operations for interacting with file in Azure Storage File Service.
 * Operations allowed by the client are creating, copying, uploading, downloading, deleting and listing on a file, retrieving properties, setting metadata
 * and list or force close handles of the file.
 *
 * <p><strong>Instantiating an Asynchronous File Client</strong></p>
 *
 * {@codesnippet com.azure.storage.file.fileAsyncClient.instantiation}
 *
 * <p>View {@link FileClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see FileClientBuilder
 * @see FileClient
 * @see SharedKeyCredential
 * @see SASTokenCredential
 */
public class FileAsyncClient {
    private final ClientLogger logger = new ClientLogger(FileAsyncClient.class);
    private static final long FILE_DEFAULT_BLOCK_SIZE = 4 * 1024 * 1024L;
    private static final long DOWNLOAD_UPLOAD_CHUNK_TIMEOUT = 300;

    private final AzureFileStorageImpl azureFileStorageClient;
    private final String shareName;
    private final String filePath;
    private final String snapshot;

    /**
     * Creates a FileAsyncClient that sends requests to the storage file at {@link AzureFileStorageImpl#getUrl() endpoint}.
     * Each service call goes through the {@link HttpPipeline pipeline} in the {@code client}.
     * @param azureFileStorageClient Client that interacts with the service interfaces
     * @param shareName Name of the share
     * @param filePath Path to the file
     * @param snapshot The snapshot of the share
     */
    FileAsyncClient(AzureFileStorageImpl azureFileStorageClient, String shareName, String filePath, String snapshot) {
        this.shareName = shareName;
        this.filePath = filePath;
        this.snapshot = snapshot;
        this.azureFileStorageClient = azureFileStorageClient;
    }

    /**
     * Creates a FileAsyncClient that sends requests to the storage account at {@code endpoint}.
     * Each service call goes through the {@code httpPipeline}.
     * @param endpoint URL for the Storage File service
     * @param httpPipeline HttpPipeline that HTTP requests and response flow through
     * @param shareName Name of the share
     * @param filePath Path to the file
     * @param snapshot Optional snapshot of the share
     */
    FileAsyncClient(URL endpoint, HttpPipeline httpPipeline, String shareName, String filePath, String snapshot) {
        this.shareName = shareName;
        this.filePath = filePath;
        this.snapshot = snapshot;
        this.azureFileStorageClient = new AzureFileStorageBuilder().pipeline(httpPipeline)
                          .url(endpoint.toString())
                          .build();
    }

    /**
     * Get the url of the storage file client.
     * @return the URL of the storage file client
     * @throws RuntimeException If the file is using a malformed URL.
     */
    public URL getFileUrl() {
        try {
            return new URL(azureFileStorageClient.getUrl());
        } catch (MalformedURLException e) {
            throw new RuntimeException(String.format("Invalid URL on %s: %s" + getClass().getSimpleName(),
                azureFileStorageClient.getUrl()), e);
        }
    }

    /**
     * Creates a file in the storage account and returns a response of {@link FileInfo} to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the file with size 1KB.</p>
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
    public Mono<Response<FileInfo>> create(long maxSize) {
        return create(maxSize, null, null);
    }

    /**
     * Creates a file in the storage account and returns a response of FileInfo to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the file with length of 1024 bytes, some headers and metadata.</p>
     *
     * {@codesnippet com.azure.storage.file.fileAsyncClient.create#long-filehttpheaders-map}
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
    public Mono<Response<FileInfo>> create(long maxSize, FileHTTPHeaders httpHeaders, Map<String, String> metadata) {
        return azureFileStorageClient.files().createWithRestResponseAsync(shareName, filePath, maxSize, null, metadata, httpHeaders, Context.NONE)
            .map(this::createFileInfoResponse);
    }

    /**
     * Copies a blob or file to a destination file within the storage account.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Copy file from source url to the {@code filePath} </p>
     *
     * {@codesnippet com.azure.storage.file.fileAsyncClient.startCopy#string-map}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/copy-file">Azure Docs</a>.</p>
     *
     * @param sourceUrl Specifies the URL of the source file or blob, up to 2 KB in length.
     * @param metadata Optional name-value pairs associated with the file as metadata. Metadata names must adhere to the naming rules.
     * @see <a href="https://docs.microsoft.com/en-us/dotnet/csharp/language-reference/">C# identifiers</a>
     * @return A response containing the file copy info and the status of copying the file.
     */
    public Mono<Response<FileCopyInfo>> startCopy(String sourceUrl, Map<String, String> metadata) {
        return azureFileStorageClient.files().startCopyWithRestResponseAsync(shareName, filePath, sourceUrl, null, metadata, Context.NONE)
                    .map(this::startCopyResponse);
    }

    /**
     * Aborts a pending Copy File operation, and leaves a destination file with zero length and full metadata.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Abort copy file from copy id("someCopyId") </p>
     *
     * {@codesnippet com.azure.storage.file.fileAsyncClient.abortCopy#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/abort-copy-file">Azure Docs</a>.</p>
     *
     * @param copyId Specifies the copy id which has copying pending status associate with it.
     * @return A response containing the status of aborting copy the file.
     */
    public Mono<VoidResponse> abortCopy(String copyId) {
        return azureFileStorageClient.files().abortCopyWithRestResponseAsync(shareName, filePath, copyId, Context.NONE)
                    .map(VoidResponse::new);
    }

    /**
     * Downloads a file from the system, including its metadata and properties
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Download the file to current folder. </p>
     *
     * {@codesnippet com.azure.storage.file.fileAsyncClient.downloadToFile#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-file">Azure Docs</a>.</p>
     *
     * @param downloadFilePath The path where store the downloaded file
     * @return An empty response.
     */
    public Mono<Void> downloadToFile(String downloadFilePath) {
        return downloadToFile(downloadFilePath, null);
    }

    /**
     * Downloads a file from the system, including its metadata and properties
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Download the file from 1024 to 2048 bytes to current folder. </p>
     *
     * {@codesnippet com.azure.storage.file.fileAsyncClient.downloadToFile#string-filerange}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-file">Azure Docs</a>.</p>
     *
     * @param downloadFilePath The path where store the downloaded file
     * @param range Optional byte range which returns file data only from the specified range.
     * @return An empty response.
     */
    public Mono<Void> downloadToFile(String downloadFilePath, FileRange range) {
        AsynchronousFileChannel channel = channelSetup(downloadFilePath);
        return sliceFileRange(range)
                   .flatMap(chunk -> downloadWithProperties(chunk, false)
                         .map(dar -> dar.value().body())
                         .subscribeOn(Schedulers.elastic())
                         .flatMap(fbb -> FluxUtil.bytebufStreamToFile(fbb, channel, chunk.start() - (range == null ? 0 : range.start()))
                         .subscribeOn(Schedulers.elastic())
                         .timeout(Duration.ofSeconds(DOWNLOAD_UPLOAD_CHUNK_TIMEOUT))
                         .retry(3, throwable -> throwable instanceof IOException || throwable instanceof TimeoutException)))
                   .then()
                   .doOnTerminate(() -> channelCleanUp(channel));
    }

    private AsynchronousFileChannel channelSetup(String filePath) {
        try {
            return AsynchronousFileChannel.open(Paths.get(filePath), StandardOpenOption.READ, StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void channelCleanUp(AsynchronousFileChannel channel) {
        try {
            channel.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Flux<FileRange> sliceFileRange(FileRange fileRange) {
        long offset = fileRange == null ? 0L : fileRange.start();
        Mono<Long> end;
        if (fileRange != null) {
            end = Mono.just(fileRange.end());
        } else {
            end = Mono.empty();
        }
        end = end.switchIfEmpty(getProperties().map(rb -> rb.value().contentLength()));
        return end
                   .map(e -> {
                       List<FileRange> chunks = new ArrayList<>();
                       for (long pos = offset; pos < e; pos += FILE_DEFAULT_BLOCK_SIZE) {
                           long count = FILE_DEFAULT_BLOCK_SIZE;
                           if (pos + count > e) {
                               count = e - pos;
                           }
                           chunks.add(new FileRange(pos, pos + count - 1));
                       }
                       return chunks;
                   })
                   .flatMapMany(Flux::fromIterable);
    }

    /**
     * Downloads a file from the system, including its metadata and properties
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Download the file with its metadata and properties. </p>
     *
     * {@codesnippet com.azure.storage.file.fileAsyncClient.downloadWithProperties}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-file">Azure Docs</a>.</p>
     *
     * @return A response that only contains headers and response status code
     */
    public Mono<Response<FileDownloadInfo>> downloadWithProperties() {
        return downloadWithProperties(null, null);
    }

    /**
     * Downloads a file from the system, including its metadata and properties
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Download the file from 1024 to 2048 bytes with its metadata and properties and without the contentMD5. </p>
     *
     * {@codesnippet com.azure.storage.file.fileAsyncClient.downloadWithProperties#filerange-boolean}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-file">Azure Docs</a>.</p>
     *
     * @param range Optional byte range which returns file data only from the specified range.
     * @param rangeGetContentMD5 Optional boolean which the service returns the MD5 hash for the range when it sets to true, as long as the range is less than or equal to 4 MB in size.
     * @return A response that only contains headers and response status code
     */
    public Mono<Response<FileDownloadInfo>> downloadWithProperties(FileRange range, Boolean rangeGetContentMD5) {
        String rangeString = range == null ? null : range.toString();
        return azureFileStorageClient.files().downloadWithRestResponseAsync(shareName, filePath, null, rangeString, rangeGetContentMD5, Context.NONE)
                    .map(this::downloadWithPropertiesResponse);
    }

    /**
     * Deletes the file associate with the client.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the file</p>
     *
     * {@codesnippet com.azure.storage.file.fileAsyncClient.delete}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-file2">Azure Docs</a>.</p>
     *
     * @return A response that only contains headers and response status code
     * @throws StorageErrorException If the directory doesn't exist or the file doesn't exist.
     */
    public Mono<VoidResponse> delete() {
        return azureFileStorageClient.files().deleteWithRestResponseAsync(shareName, filePath, Context.NONE)
                    .map(VoidResponse::new);
    }

    /**
     * Retrieves the properties of the storage account's file.
     * The properties includes file metadata, last modified date, is server encrypted, and eTag.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve file properties</p>
     *
     * {@codesnippet com.azure.storage.file.fileAsyncClient.getProperties}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-file-properties">Azure Docs</a>.</p>
     *
     * @return Storage file properties
     */
    public Mono<Response<FileProperties>> getProperties() {
        return azureFileStorageClient.files().getPropertiesWithRestResponseAsync(shareName, filePath, snapshot, null, Context.NONE)
                    .map(this::getPropertiesResponse);
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
     * {@codesnippet com.azure.storage.file.fileAsyncClient.setHttpHeaders#long-filehttpheaders}
     *
     * <p>Clear the metadata of the file</p>
     *
     * {@codesnippet com.azure.storage.file.fileAsyncClient.setHttpHeaders#long-filehttpheaders.clearHttpHeaders}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-file-properties">Azure Docs</a>.</p>
     *
     * @param newFileSize New file size of the file
     * @param httpHeaders Resizes a file to the specified size. If the specified byte value is less than the current size of the file, then all ranges above the specified byte value are cleared.
     * @return Response of the information about the file
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     */
    public Mono<Response<FileInfo>> setHttpHeaders(long newFileSize, FileHTTPHeaders httpHeaders) {
        return azureFileStorageClient.files().setHTTPHeadersWithRestResponseAsync(shareName, filePath, null, newFileSize, httpHeaders, Context.NONE)
                        .map(this::setHttpHeadersResponse);
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
     * {@codesnippet com.azure.storage.file.fileAsyncClient.setMetadata#map}
     *
     * <p>Clear the metadata of the file</p>
     *
     * {@codesnippet com.azure.storage.file.fileAsyncClient.setMetadata#map.clearMetadata}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-file-metadata">Azure Docs</a>.</p>
     *
     * @param metadata Options.Metadata to set on the file, if null is passed the metadata for the file is cleared
     * @return information about the file
     * @throws StorageErrorException If the file doesn't exist or the metadata contains invalid keys
     */
    public Mono<Response<FileMetadataInfo>> setMetadata(Map<String, String> metadata) {
        return azureFileStorageClient.files().setMetadataWithRestResponseAsync(shareName, filePath, null, metadata, Context.NONE)
                    .map(this::setMetadataResponse);
    }

    /**
     * Uploads a range of bytes to the beginning of a file in storage file service. Upload operations performs an in-place write on the specified file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Upload "default" to the file. </p>
     *
     * {@codesnippet com.azure.storage.file.fileAsyncClient.upload#flux-long}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/put-range">Azure Docs</a>.</p>
     *
     * @param data The data which will upload to the storage file.
     * @param length Specifies the number of bytes being transmitted in the request body. When the FileRangeWriteType is set to clear, the value of this header must be set to zero..
     * @return A response that only contains headers and response status code
     * @throws StorageErrorException If you attempt to upload a range that is larger than 4 MB, the service returns status code 413 (Request Entity Too Large)
     */
    public Mono<Response<FileUploadInfo>> upload(Flux<ByteBuf> data, long length) {
        FileRange range = new FileRange(0, length - 1);
        return azureFileStorageClient.files().uploadRangeWithRestResponseAsync(shareName, filePath, range.toString(), FileRangeWriteType.UPDATE, length, data, null, null, Context.NONE)
            .map(this::uploadResponse);
    }

    /**
     * Uploads a range of bytes to specific of a file in storage file service. Upload operations performs an in-place write on the specified file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Upload the file from 1024 to 2048 bytes with its metadata and properties and without the contentMD5. </p>
     *
     * {@codesnippet com.azure.storage.file.fileAsyncClient.upload#bytebuf-long-int-filerangewritetype}
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
    public Mono<Response<FileUploadInfo>> upload(Flux<ByteBuf> data, long length, long offset, FileRangeWriteType type) {
        FileRange range = new FileRange(offset, offset + length - 1);
        return azureFileStorageClient.files().uploadRangeWithRestResponseAsync(shareName, filePath, range.toString(), type, length, data, null, null, Context.NONE)
                   .map(this::uploadResponse);
    }

    /**
     * Uploads file to storage file service.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Upload the file from the source file path. </p>
     *
     * {@codesnippet com.azure.storage.file.fileAsyncClient.uploadFromFile#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-file">Azure Docs Create File</a>
     * and
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/put-range">Azure Docs Upload</a>.</p>
     *
     * @param uploadFilePath The path where store the source file to upload
     * @return An empty response.
     */
    public Mono<Void> uploadFromFile(String uploadFilePath) {
        return uploadFromFile(uploadFilePath, FileRangeWriteType.UPDATE);
    }

    /**
     * Uploads file to storage file service.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p> Upload the file from the source file path. </p>
     *
     * (@codesnippet com.azure.storage.file.fileAsyncClient.uploadFromFile#string-filerangewritetype}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-file">Azure Docs Create File</a>
     * and
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/put-range">Azure Docs Upload</a>.</p>
     *
     * @param uploadFilePath The path where store the source file to upload
     * @param type You may specify one of the following options:
     * <ul>
     *    <li>Update: Writes the bytes specified by the request body into the specified range.</li>
     *    <li>Clear: Clears the specified range and releases the space used in storage for that range. To clear a range, set the Content-Length header to zero.</li>
     * <ul/>
     *
     * @return An empty response.
     * @throws UncheckedIOException If an I/O error occurs.
     */
    public Mono<Void> uploadFromFile(String uploadFilePath, FileRangeWriteType type) {
        AsynchronousFileChannel channel = channelSetup(uploadFilePath);
        return Flux.fromIterable(sliceFile(uploadFilePath))
                   .flatMap(chunk -> {
                       return upload(FluxUtil.byteBufStreamFromFile(channel, chunk.start(), chunk.end() - chunk.start() + 1), chunk.end() - chunk.start() + 1, chunk.start(), type)
                            .timeout(Duration.ofSeconds(DOWNLOAD_UPLOAD_CHUNK_TIMEOUT))
                            .retry(3, throwable -> throwable instanceof IOException || throwable instanceof TimeoutException);
                   })
                   .then()
                   .doOnTerminate(() -> channelCleanUp(channel));
    }

    private List<FileRange> sliceFile(String path) {
        File file = new File(path);
        assert file.exists();
        List<FileRange> ranges = new ArrayList<>();
        for (long pos = 0; pos < file.length(); pos += FILE_DEFAULT_BLOCK_SIZE) {
            long count = FILE_DEFAULT_BLOCK_SIZE;
            if (pos + count > file.length()) {
                count = file.length() - pos;
            }
            ranges.add(new FileRange(pos, pos + count - 1));
        }
        return ranges;
    }

    /**
     * List of valid ranges for a file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all ranges for the file client.</p>
     *
     * {@codesnippet com.azure.storage.file.fileAsyncClient.listRanges}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/list-ranges">Azure Docs</a>.</p>
     *
     * @return {@link FileRange ranges} in the files.
     */
    public Flux<FileRange> listRanges() {
        return azureFileStorageClient.files().getRangeListWithRestResponseAsync(shareName, filePath, snapshot, null, null, Context.NONE)
                   .flatMapMany(this::convertListRangesResponseToFileRangeInfo);
    }

    /**
     * List of valid ranges for a file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all ranges within the file range from 1KB to 2KB.</p>
     *
     * {@codesnippet com.azure.storage.file.fileAsyncClient.listRanges#filerange}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/list-ranges">Azure Docs</a>.</p>
     *
     * @param range Optional byte range which returns file data only from the specified range.
     * @return {@link FileRange ranges} in the files that satisfy the requirements
     */
    public Flux<FileRange> listRanges(FileRange range) {
        String rangeString = range == null ? null : range.toString();
        return azureFileStorageClient.files().getRangeListWithRestResponseAsync(shareName, filePath, snapshot, null, rangeString, Context.NONE)
                    .flatMapMany(this::convertListRangesResponseToFileRangeInfo);
    }

    /**
     * List of open handles on a file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all handles for the file client.</p>
     *
     * {@codesnippet com.azure.storage.file.fileAsyncClient.listHandles}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/list-handles">Azure Docs</a>.</p>
     *
     * @return {@link HandleItem handles} in the files that satisfy the requirements
     */
    public Flux<HandleItem> listHandles() {
        return listHandles(null);
    }

    /**
     * List of open handles on a file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List 10 handles for the file client.</p>
     *
     * {@codesnippet com.azure.storage.file.fileAsyncClient.listHandles#integer}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/list-handles">Azure Docs</a>.</p>
     *
     * @param maxResults Optional maximum number of results will return per page
     * @return {@link HandleItem handles} in the file that satisfy the requirements
     */
    public Flux<HandleItem> listHandles(Integer maxResults) {
        return azureFileStorageClient.files().listHandlesWithRestResponseAsync(shareName, filePath, null, maxResults, null, snapshot, Context.NONE)
                   .flatMapMany(response -> nextPageForHandles(response, maxResults));
    }

    /**
     * Closes a handle or handles opened on a file at the service. It is intended to be used alongside {@link FileAsyncClient#listHandles()} (Integer)} .
     * TODO: Will change the return type to how many handles have been closed. Implement one more API to force close all handles.
     * TODO: @see <a href="https://github.com/Azure/azure-sdk-for-java/issues/4525">Github Issue 4525</a>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Force close handles with handles returned by list handles in recursive.</p>
     *
     * {@codesnippet com.azure.storage.file.fileAsyncClient.forceCloseHandles#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/force-close-handles">Azure Docs</a>.</p>
     *
     * @param handleId Specifies the handle ID to be closed. Use an asterisk ('*') as a wildcard string to specify all handles.
     * @return The counts of number of handles closed
     */
    public Flux<Integer> forceCloseHandles(String handleId) {
        return azureFileStorageClient.files().forceCloseHandlesWithRestResponseAsync(shareName, filePath, handleId, null, null, snapshot, Context.NONE)
                   .flatMapMany(response -> nextPageForForceCloseHandles(response, handleId));
    }

    /**
     * Get snapshot id which attached to {@link FileAsyncClient}.
     * Return {@code null} if no snapshot id attached.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Get the share snapshot id. </p>
     *
     * {@codesnippet com.azure.storage.file.fileAsyncClient.getShareSnapshotId}
     *
     * @return The snapshot id which is a unique {@code DateTime} value that identifies the share snapshot to its base share.
     */
    public String getShareSnapshotId() {
        return this.snapshot;
    }

    private Flux<Integer> nextPageForForceCloseHandles(final FilesForceCloseHandlesResponse response, final String handleId) {
        List<Integer> handleCount = Arrays.asList(response.deserializedHeaders().numberOfHandlesClosed());

        if (response.deserializedHeaders().marker() == null) {
            return Flux.fromIterable(handleCount);
        }
        Mono<FilesForceCloseHandlesResponse> listResponse = azureFileStorageClient.files().forceCloseHandlesWithRestResponseAsync(shareName, filePath, handleId, null, response.deserializedHeaders().marker(), snapshot, Context.NONE);
        Flux<Integer> fileRefPublisher = listResponse.flatMapMany(newResponse -> nextPageForForceCloseHandles(newResponse, handleId));
        return Flux.fromIterable(handleCount).concatWith(fileRefPublisher);
    }

    private Flux<HandleItem> nextPageForHandles(final FilesListHandlesResponse response, final Integer maxResults) {
        List<HandleItem> handleItems = response.value().handleList();

        if (response.value().nextMarker() == null) {
            return Flux.fromIterable(handleItems);
        }

        Mono<FilesListHandlesResponse> listResponse = azureFileStorageClient.files().listHandlesWithRestResponseAsync(shareName, filePath, response.value().nextMarker(), maxResults, null, snapshot,  Context.NONE);
        Flux<HandleItem> fileRefPublisher = listResponse.flatMapMany(newResponse -> nextPageForHandles(newResponse, maxResults));
        return Flux.fromIterable(handleItems).concatWith(fileRefPublisher);
    }

    private Response<FileInfo> createFileInfoResponse(final FilesCreateResponse response) {
        String eTag = response.deserializedHeaders().eTag();
        OffsetDateTime lastModified = response.deserializedHeaders().lastModified();
        boolean isServerEncrypted = response.deserializedHeaders().isServerEncrypted();
        FileInfo fileInfo = new FileInfo(eTag, lastModified, isServerEncrypted);
        return new SimpleResponse<>(response, fileInfo);
    }

    private Response<FileCopyInfo> startCopyResponse(final FilesStartCopyResponse response) {
        String eTag = response.deserializedHeaders().eTag();
        OffsetDateTime lastModified = response.deserializedHeaders().lastModified();
        String copyId = response.deserializedHeaders().copyId();
        CopyStatusType copyStatus = response.deserializedHeaders().copyStatus();
        FileCopyInfo fileCopyInfo = new FileCopyInfo(eTag, lastModified, copyId, copyStatus);
        return new SimpleResponse<>(response, fileCopyInfo);
    }

    private Response<FileInfo> setHttpHeadersResponse(final FilesSetHTTPHeadersResponse response) {
        String eTag = response.deserializedHeaders().eTag();
        OffsetDateTime lastModified = response.deserializedHeaders().lastModified();
        boolean isServerEncrypted = response.deserializedHeaders().isServerEncrypted();
        FileInfo fileInfo = new FileInfo(eTag, lastModified, isServerEncrypted);
        return new SimpleResponse<>(response, fileInfo);
    }
    private Response<FileDownloadInfo> downloadWithPropertiesResponse(final FilesDownloadResponse response) {
        String eTag = response.deserializedHeaders().eTag();
        OffsetDateTime lastModified = response.deserializedHeaders().lastModified();
        Map<String, String> metadata = response.deserializedHeaders().metadata();
        Long contentLength = response.deserializedHeaders().contentLength();
        String contentType = response.deserializedHeaders().contentType();
        String contentRange = response.deserializedHeaders().contentRange();
        Flux<ByteBuf> body = response.value();
        FileDownloadInfo fileDownloadInfo = new FileDownloadInfo(eTag, lastModified, metadata, contentLength, contentType, contentRange, body);
        return new SimpleResponse<>(response, fileDownloadInfo);
    }

    private Response<FileProperties> getPropertiesResponse(final FilesGetPropertiesResponse response) {
        FileGetPropertiesHeaders headers = response.deserializedHeaders();
        String eTag = headers.eTag();
        OffsetDateTime lastModified = headers.lastModified();
        Map<String, String> metadata = headers.metadata();
        String fileType = headers.fileType();
        Long contentLength = headers.contentLength();
        String contentType = headers.contentType();
        byte[] contentMD5;
        try {
            contentMD5 = headers.contentMD5();
        } catch (NullPointerException e) {
            contentMD5 = null;
        }
        String contentEncoding = headers.contentEncoding();
        String cacheControl = headers.cacheControl();
        String contentDisposition = headers.contentDisposition();
        OffsetDateTime copyCompletionTime = headers.copyCompletionTime();
        String copyStatusDescription = headers.copyStatusDescription();
        String copyId = headers.copyId();
        String copyProgress = headers.copyProgress();
        String copySource = headers.copySource();
        CopyStatusType copyStatus = headers.copyStatus();
        Boolean isServerEncrpted = headers.isServerEncrypted();
        FileProperties fileProperties = new FileProperties(eTag, lastModified, metadata, fileType, contentLength,
            contentType, contentMD5, contentEncoding, cacheControl, contentDisposition, copyCompletionTime, copyStatusDescription,
            copyId, copyProgress, copySource, copyStatus, isServerEncrpted);
        return new SimpleResponse<>(response, fileProperties);
    }

    private Response<FileUploadInfo> uploadResponse(final FilesUploadRangeResponse response) {
        FileUploadRangeHeaders headers = response.deserializedHeaders();
        String eTag = headers.eTag();
        OffsetDateTime lastModified = headers.lastModified();
        byte[] contentMD5;
        try {
            contentMD5 = headers.contentMD5();
        } catch (NullPointerException e) {
            contentMD5 = null;
        }
        Boolean isServerEncrypted = headers.isServerEncrypted();
        FileUploadInfo fileUploadInfo = new FileUploadInfo(eTag, lastModified, contentMD5, isServerEncrypted);
        return new SimpleResponse<>(response, fileUploadInfo);
    }

    private Response<FileMetadataInfo> setMetadataResponse(final FilesSetMetadataResponse response) {
        String eTag = response.deserializedHeaders().eTag();
        boolean isServerEncrypted = response.deserializedHeaders().isServerEncrypted();
        FileMetadataInfo fileMetadataInfo = new FileMetadataInfo(eTag, isServerEncrypted);
        return new SimpleResponse<>(response, fileMetadataInfo);
    }

    private Flux<FileRange> convertListRangesResponseToFileRangeInfo(FilesGetRangeListResponse response) {
        List<FileRange> fileRanges = new ArrayList<>();
        response.value().forEach(range -> {
            long start = range.start();
            long end = range.end();
            fileRanges.add(new FileRange(start, end));
        });
        return Flux.fromIterable(fileRanges);
    }
}
