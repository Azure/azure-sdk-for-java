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
 * <pre>
 * FileAsyncClient client = FileAsyncClient.builder()
 *     .connectionString(connectionString)
 *     .endpoint(endpoint)
 *     .buildAsyncClient();
 * </pre>
 *
 * <p>View {@link FileClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see FileClientBuilder
 * @see FileClient
 * @see SharedKeyCredential
 * @see SASTokenCredential
 */
public class FileAsyncClient {
    private static final ClientLogger LOGGER = new ClientLogger(FileAsyncClient.class);
    private static final long FILE_DEFAULT_BLOCK_SIZE = 4 * 1024 * 1024L;

    private final AzureFileStorageImpl azureFileStorageClient;
    private final String shareName;
    private final String filePath;
    private final String shareSnapshot;

    /**
     * Creates a FileAsyncClient that sends requests to the storage file at {@link AzureFileStorageImpl#url() endpoint}.
     * Each service call goes through the {@link HttpPipeline pipeline} in the {@code client}.
     * @param azureFileStorageClient Client that interacts with the service interfaces
     * @param shareName Name of the share
     * @param filePath Path to the file
     * @param shareSnapshot The snapshot of the share
     */
    FileAsyncClient(AzureFileStorageImpl azureFileStorageClient, String shareName, String filePath, String shareSnapshot) {
        this.shareName = shareName;
        this.filePath = filePath;
        this.shareSnapshot = shareSnapshot;
        this.azureFileStorageClient = new AzureFileStorageBuilder().pipeline(azureFileStorageClient.httpPipeline())
                            .url(azureFileStorageClient.url())
                            .version(azureFileStorageClient.version())
                            .build();
    }

    /**
     * Creates a FileAsyncClient that sends requests to the storage account at {@code endpoint}.
     * Each service call goes through the {@code httpPipeline}.
     * @param endpoint URL for the Storage File service
     * @param httpPipeline HttpPipeline that HTTP requests and response flow through
     * @param shareName Name of the share
     * @param filePath Path to the file
     * @param shareSnapshot Optional. The snapshot of the share
     */
    FileAsyncClient(URL endpoint, HttpPipeline httpPipeline, String shareName, String filePath, String shareSnapshot) {
        this.shareName = shareName;
        this.filePath = filePath;
        this.shareSnapshot = shareSnapshot;
        this.azureFileStorageClient = new AzureFileStorageBuilder().pipeline(httpPipeline)
                          .url(endpoint.toString())
                          .build();
    }

    /**
     * Get the getFileUrl of the storage file client.
     * @return the URL of the storage file client
     * @throws MalformedURLException if no protocol is specified, or an
     *         unknown protocol is found, or {@code spec} is {@code null}.
     */
    public URL getFileUrl() throws MalformedURLException {
        return new URL(azureFileStorageClient.url());
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
     * <pre>
     * FileHTTPHeaders httpHeaders = new FileHTTPHeaders().fileContentType("text/plain");
     * client.create(1024, httpHeaders, Collections.singletonMap("file", "updatedMetadata"))
     *     .subscribe(response -&gt; System.out.printf("Creating the file completed with status code %d", response.statusCode()));
     * </pre>
     *
     * @param maxSize The maximum size in bytes for the file, up to 1 TiB.
     * @param httpHeaders Additional parameters for the operation.
     * @param metadata Optional. Name-value pairs associated with the file as metadata. Metadata names must adhere to the naming rules.
     *                           @see <a href="https://docs.microsoft.com/en-us/dotnet/csharp/language-reference/">C# identifiers</a>
     * @return A response containing the directory info and the status of creating the directory.
     * @throws StorageErrorException If the directory has already existed, the parent directory does not exist or directory is an invalid resource name.
     */
    public Mono<Response<FileInfo>> create(long maxSize, FileHTTPHeaders httpHeaders, Map<String, String> metadata) {
        return azureFileStorageClient.files().createWithRestResponseAsync(shareName, filePath, maxSize, null, metadata, httpHeaders, Context.NONE)
            .map(this::createResponse);
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
     * @param sourceUrl Specifies the URL of the source file or blob, up to 2 KB in length.
     * @param metadata Optional. Name-value pairs associated with the file as metadata. Metadata names must adhere to the naming rules.
     *      *                           @see <a href="https://docs.microsoft.com/en-us/dotnet/csharp/language-reference/">C# identifiers</a>
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
     * <pre>
     * client.abortCopy("someCopyId")
     *     .subscribe(response -&gt; System.out.printf("Abort copying the file completed with status code %d", response.statusCode()));
     * </pre>
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
     * <pre>
     * client.downloadToFile("someFilePath")
     *     .doOnTerminate(() -> if (Files.exist(Paths.get("someFilePath"))) {
     *          System.out.println("Download the file completed");
     *     });
     * </pre>
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
     * {@codesnippet com.azure.storage.file.fileAsyncClient.downloadToFile}
     *
     * @param downloadFilePath The path where store the downloaded file
     * @param range Optional. Return file data only from the specified byte range.
     * @return An empty response.
     * @throws UncheckedIOException If an I/O error occurs.
     */
    public Mono<Void> downloadToFile(String downloadFilePath, FileRange range) {
        AsynchronousFileChannel channel = channelSetup(downloadFilePath);
        return sliceFileRange(range)
                   .flatMap(chunk -> downloadWithProperties(chunk, false)
                         .map(dar -> dar.value().body())
                         .subscribeOn(Schedulers.elastic())
                         .flatMap(fbb -> FluxUtil.bytebufStreamToFile(fbb, channel, chunk.start() - (range == null ? 0 : range.start()))
                         .subscribeOn(Schedulers.elastic())
                         .timeout(Duration.ofSeconds(300))
                         .retry(3, throwable -> throwable instanceof IOException || throwable instanceof TimeoutException))
                   .doOnTerminate(() ->
                                    LOGGER.asInfo().log("Saved " + chunk.toString() + " on thread " + Thread.currentThread().getName())))
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
     * <pre>
     * client.downloadWithProperties(new Range(1024, 2048), false)
     *     .subscribe(response -&gt; System.out.printf("Downloading the file range completed with status code %d", response.statusCode()));
     * </pre>
     *
     * @param range Optional. Return file data only from the specified byte range.
     * @param rangeGetContentMD5 Optional. When this header is set to true and specified together with the Range header, the service returns the MD5 hash for the range, as long as the range is less than or equal to 4 MB in size.
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
     * <pre>
     * client.getProperties()
     *    .subscribe(response -&gt; {
     *        DirectoryProperties properties = response.value();
     *        System.out.printf("File latest modified date is %s.", properties.lastModified());
     *    });
     * </pre>
     *
     * @return Storage file properties
     */
    public Mono<Response<FileProperties>> getProperties() {
        return azureFileStorageClient.files().getPropertiesWithRestResponseAsync(shareName, filePath, shareSnapshot, null, Context.NONE)
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
     * <pre>
     * FileHTTPHeaders httpHeaders = new FileHTTPHeaders().fileContentType("text/plain");
     * client.setHttpHeaders(1024, httpHeaders)
     *     .subscribe(response -&gt; System.out.printf("Setting the file httpHeaders completed with status code %d", response.statusCode()));
     * </pre>
     *
     * <p>Clear the metadata of the file</p>
     *
     * <pre>
     * client.setHttpHeaders(1024, null)
     *     .subscribe(response -&gt; System.out.printf("Clearing the file httpHeaders completed with status code %d", response.statusCode()));
     * </pre>
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
     * <pre>
     * client.setMetadata(Collections.singletonMap("file", "updatedMetadata"))
     *     .subscribe(response -&gt; System.out.printf("Setting the file metadata completed with status code %d", response.statusCode()));
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
    public Mono<Response<FileMetadataInfo>> setMetadata(Map<String, String> metadata) {
        return azureFileStorageClient.files().setMetadataWithRestResponseAsync(shareName, filePath, null, metadata, Context.NONE)
                    .map(this::setMeatadataResponse);
    }

    /**
     * Uploads a range of bytes to the beginning of a file in storage file service. Upload operations performs an in-place write on the specified file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Upload "default" to the file. </p>
     *
     * {@codesnippet com.azure.storage.file.fileAsyncClient.upload}
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
     * <pre>
     * ByteBuf defaultData = Unpooled.wrappedBuffer(defaultText.getBytes(StandardCharsets.UTF_8));
     * client.upload(defaultData, defaultData.readableBytes())
     *     .subscribe(response -&gt; System.out.printf("Upload the bytes to file range completed with status code %d", response.statusCode()));
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
     * {@codesnippet com.azure.storage.file.fileAsyncClient.uploadFromFile}
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
     * <pre>
     * client.uploadFromFile("someFilePath", FileRangeWriteType.UPDATE)
     *     .doOnTerminate(() -> if (client.getProperties() != null) {
     *          System.out.printf("Upload the file with length of %d completed", client.getProperties().block().value().contentLength());
     *     });
     * </pre>
     *
     * @param uploadFilePath The path where store the source file to upload
     * @param type You may specify one of the following options:
     *              - Update: Writes the bytes specified by the request body into the specified range.
     *              - Clear: Clears the specified range and releases the space used in storage for that range. To clear a range, set the Content-Length header to zero.
     * @return An empty response.
     * @throws UncheckedIOException If an I/O error occurs.
     */
    public Mono<Void> uploadFromFile(String uploadFilePath, FileRangeWriteType type) {
        AsynchronousFileChannel channel = channelSetup(uploadFilePath);
        return Flux.fromIterable(sliceFile(uploadFilePath))
                   .flatMap(chunk -> {
                       return upload(FluxUtil.byteBufStreamFromFile(channel, chunk.start(), chunk.end() - chunk.start() + 1), chunk.end() - chunk.start() + 1, chunk.start(), type)
                            .timeout(Duration.ofSeconds(300))
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
     * <pre>
     * client.listRanges()
     *     .subscribe(range -&gt; System.out.printf("List ranges completed with start: %d, end: %d", range.start(), range.end()));
     * </pre>
     *
     * @return {@link FileRange ranges} in the files.
     */
    public Flux<FileRange> listRanges() {
        return azureFileStorageClient.files().getRangeListWithRestResponseAsync(shareName, filePath, shareSnapshot, null, null, Context.NONE)
                   .flatMapMany(this::convertListRangesResponseToFileRangeInfo);
    }

    /**
     * List of valid ranges for a file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all ranges within the file range from 1KB to 2KB.</p>
     *
     * <pre>
     * client.listRanges(new FileRange(1024, 2048)
     *     .subscribe(result -&gt; System.out.printf("List ranges completed with start: %d, end: %d", result.start(), result.end()));
     * </pre>
     *
     * @param range Optional. Return file data only from the specified byte range.
     * @return {@link FileRange ranges} in the files that satisfy the requirements
     */
    public Flux<FileRange> listRanges(FileRange range) {
        String rangeString = range == null ? null : range.toString();
        return azureFileStorageClient.files().getRangeListWithRestResponseAsync(shareName, filePath, shareSnapshot, null, rangeString, Context.NONE)
                    .flatMapMany(this::convertListRangesResponseToFileRangeInfo);
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
     *     .subscribe(result -&gt; System.out.printf("List handles completed with handle id %s", result.handleId()));
     * </pre>
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
     * <pre>
     * client.listHandles(10)
     *     .subscribe(result -&gt; System.out.printf("List handles completed with handle id %s", result.handleId()));     * </pre>
     * @param maxResults Optional. The number of results will return per page
     * @return {@link HandleItem handles} in the file that satisfy the requirements
     */
    public Flux<HandleItem> listHandles(Integer maxResults) {
        return azureFileStorageClient.files().listHandlesWithRestResponseAsync(shareName, filePath, null, maxResults, null, shareSnapshot, Context.NONE)
                   .flatMapMany(response -> nextPageForHandles(response, maxResults));
    }

    /**
     * Closes a handle or handles opened on a file at the service. It is intended to be used alongside {@link FileAsyncClient#listHandles()} (Integer)} .
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Force close handles with handles returned by list handles in recursive.</p>
     *
     * <pre>
     * client.listHandles(10)
     *     .subscribe(result -&gt; {
     *         client.forceCloseHandles(result.handleId(), true).subscribe(numOfClosedHandles -&gt
     *              System.out.printf("Close %d handles.", numOfClosedHandles)
     *     )});
     * </pre>
     * @param handleId Specifies the handle ID to be closed. Use an asterisk ('*') as a wildcard string to specify all handles.
     * @return The counts of number of handles closed
     */
    public Flux<Integer> forceCloseHandles(String handleId) {
        return azureFileStorageClient.files().forceCloseHandlesWithRestResponseAsync(shareName, filePath, handleId, null, null, shareSnapshot, Context.NONE)
                   .flatMapMany(response -> nextPageForForceCloseHandles(response, handleId));
    }

    private Flux<Integer> nextPageForForceCloseHandles(final FilesForceCloseHandlesResponse response, final String handleId) {
        List<Integer> handleCount = Arrays.asList(response.deserializedHeaders().numberOfHandlesClosed());

        if (response.deserializedHeaders().marker() == null) {
            return Flux.fromIterable(handleCount);
        }
        Mono<FilesForceCloseHandlesResponse> listResponse = azureFileStorageClient.files().forceCloseHandlesWithRestResponseAsync(shareName, filePath, handleId, null, response.deserializedHeaders().marker(), shareSnapshot, Context.NONE);
        Flux<Integer> fileRefPublisher = listResponse.flatMapMany(newResponse -> nextPageForForceCloseHandles(newResponse, handleId));
        return Flux.fromIterable(handleCount).concatWith(fileRefPublisher);
    }

    private Flux<HandleItem> nextPageForHandles(final FilesListHandlesResponse response, final Integer maxResults) {
        List<HandleItem> handleItems = response.value().handleList();

        if (response.value().nextMarker() == null) {
            return Flux.fromIterable(handleItems);
        }

        Mono<FilesListHandlesResponse> listResponse = azureFileStorageClient.files().listHandlesWithRestResponseAsync(shareName, filePath, response.value().nextMarker(), maxResults, null, shareSnapshot,  Context.NONE);
        Flux<HandleItem> fileRefPublisher = listResponse.flatMapMany(newResponse -> nextPageForHandles(newResponse, maxResults));
        return Flux.fromIterable(handleItems).concatWith(fileRefPublisher);
    }

    private Response<FileInfo> createResponse(final FilesCreateResponse response) {
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

    private Response<FileMetadataInfo> setMeatadataResponse(final FilesSetMetadataResponse response) {
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
