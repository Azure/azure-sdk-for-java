// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.implementation.http.PagedResponseBase;
import com.azure.core.implementation.util.FluxUtil;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.Constants;
import com.azure.storage.common.IPRange;
import com.azure.storage.common.SASProtocol;
import com.azure.storage.common.Utility;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
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
import com.azure.storage.file.models.FileUploadRangeFromURLHeaders;
import com.azure.storage.file.models.FileUploadRangeFromURLInfo;
import com.azure.storage.file.models.FileUploadRangeHeaders;
import com.azure.storage.file.models.FilesCreateResponse;
import com.azure.storage.file.models.FilesDownloadResponse;
import com.azure.storage.file.models.FilesGetPropertiesResponse;
import com.azure.storage.file.models.FilesSetHTTPHeadersResponse;
import com.azure.storage.file.models.FilesSetMetadataResponse;
import com.azure.storage.file.models.FilesStartCopyResponse;
import com.azure.storage.file.models.FilesUploadRangeFromURLResponse;
import com.azure.storage.file.models.FilesUploadRangeResponse;
import com.azure.storage.file.models.HandleItem;
import com.azure.storage.file.models.StorageException;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.core.implementation.util.FluxUtil.withContext;
import static com.azure.storage.file.FileExtensions.filePermissionAndKeyHelper;
import static com.azure.storage.file.PostProcessor.postProcessResponse;

/**
 * This class provides a client that contains all the operations for interacting with file in Azure Storage File
 * Service. Operations allowed by the client are creating, copying, uploading, downloading, deleting and listing on a
 * file, retrieving properties, setting metadata and list or force close handles of the file.
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
     * Creates a FileAsyncClient that sends requests to the storage file at {@link AzureFileStorageImpl#getUrl()
     * endpoint}. Each service call goes through the {@link HttpPipeline pipeline} in the {@code client}.
     *
     * @param azureFileStorageClient Client that interacts with the service interfaces
     * @param shareName Name of the share
     * @param filePath Path to the file
     * @param snapshot The snapshot of the share
     */
    FileAsyncClient(AzureFileStorageImpl azureFileStorageClient, String shareName, String filePath, String snapshot) {
        Objects.requireNonNull(shareName);
        Objects.requireNonNull(filePath);
        this.shareName = shareName;
        this.filePath = filePath;
        this.snapshot = snapshot;
        this.azureFileStorageClient = azureFileStorageClient;
    }

    /**
     * Get the url of the storage file client.
     *
     * @return the URL of the storage file client
     * @throws RuntimeException If the file is using a malformed URL.
     */
    public URL getFileUrl() {
        String fileURLString = String.format("%s/%s/%s", azureFileStorageClient.getUrl(), shareName, filePath);
        if (snapshot != null) {
            fileURLString = String.format("%s?snapshot=%s", fileURLString, snapshot);
        }
        try {
            return new URL(fileURLString);
        } catch (MalformedURLException e) {
            throw logger.logExceptionAsError(new RuntimeException(
                String.format("Invalid URL on %s: %s" + getClass().getSimpleName(), fileURLString), e));
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
     * @throws StorageException If the file has already existed, the parent directory does not exist or fileName is an
     * invalid resource name.
     */
    public Mono<FileInfo> create(long maxSize) {
        return createWithResponse(maxSize, null, null, null, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a file in the storage account and returns a response of FileInfo to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the file with length of 1024 bytes, some headers, file smb properties and metadata.</p>
     *
     * {@codesnippet com.azure.storage.file.fileAsyncClient.createWithResponse#long-filehttpheaders-filesmbproperties-string-map}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-file">Azure Docs</a>.</p>
     *
     * @param maxSize The maximum size in bytes for the file, up to 1 TiB.
     * @param httpHeaders The user settable file http headers.
     * @param smbProperties The user settable file smb properties.
     * @param filePermission The file permission of the file.
     * @param metadata Optional name-value pairs associated with the file as metadata.
     * @return A response containing the {@link FileInfo file info} and the status of creating the file.
     * @throws StorageException If the directory has already existed, the parent directory does not exist or directory
     * is an invalid resource name.
     */
    public Mono<Response<FileInfo>> createWithResponse(long maxSize, FileHTTPHeaders httpHeaders,
        FileSmbProperties smbProperties, String filePermission, Map<String, String> metadata) {
        return withContext(context ->
            createWithResponse(maxSize, httpHeaders, smbProperties, filePermission, metadata, context));
    }

    Mono<Response<FileInfo>> createWithResponse(long maxSize, FileHTTPHeaders httpHeaders,
        FileSmbProperties smbProperties, String filePermission, Map<String, String> metadata, Context context) {
        smbProperties = smbProperties == null ? new FileSmbProperties() : smbProperties;

        // Checks that file permission and file permission key are valid
        filePermissionAndKeyHelper(filePermission, smbProperties.getFilePermissionKey());

        // If file permission and file permission key are both not set then set default value
        filePermission = smbProperties.setFilePermission(filePermission, FileConstants.FILE_PERMISSION_INHERIT);
        String filePermissionKey = smbProperties.getFilePermissionKey();

        String fileAttributes = smbProperties.setNtfsFileAttributes(FileConstants.FILE_ATTRIBUTES_NONE);
        String fileCreationTime = smbProperties.setFileCreationTime(FileConstants.FILE_TIME_NOW);
        String fileLastWriteTime = smbProperties.setFileLastWriteTime(FileConstants.FILE_TIME_NOW);

        return postProcessResponse(azureFileStorageClient.files()
            .createWithRestResponseAsync(shareName, filePath, maxSize, fileAttributes, fileCreationTime,
                fileLastWriteTime, null, metadata, filePermission, filePermissionKey, httpHeaders, context))
            .map(this::createFileInfoResponse);
    }

    /**
     * Copies a blob or file to a destination file within the storage account.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Copy file from source url to the {@code resourcePath} </p>
     *
     * {@codesnippet com.azure.storage.file.fileAsyncClient.startCopy#string-map}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/copy-file">Azure Docs</a>.</p>
     *
     * @param sourceUrl Specifies the URL of the source file or blob, up to 2 KB in length.
     * @param metadata Optional name-value pairs associated with the file as metadata. Metadata names must adhere to the
     * naming rules.
     * @return The {@link FileCopyInfo file copy info}.
     * @see <a href="https://docs.microsoft.com/en-us/dotnet/csharp/language-reference/">C# identifiers</a>
     */
    public Mono<FileCopyInfo> startCopy(String sourceUrl, Map<String, String> metadata) {
        return startCopyWithResponse(sourceUrl, metadata).flatMap(FluxUtil::toMono);
    }

    /**
     * Copies a blob or file to a destination file within the storage account.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Copy file from source url to the {@code resourcePath} </p>
     *
     * {@codesnippet com.azure.storage.file.fileAsyncClient.startCopyWithResponse#string-map}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/copy-file">Azure Docs</a>.</p>
     *
     * @param sourceUrl Specifies the URL of the source file or blob, up to 2 KB in length.
     * @param metadata Optional name-value pairs associated with the file as metadata. Metadata names must adhere to the
     * naming rules.
     * @return A response containing the {@link FileCopyInfo file copy info} and the status of copying the file.
     * @see <a href="https://docs.microsoft.com/en-us/dotnet/csharp/language-reference/">C# identifiers</a>
     */
    public Mono<Response<FileCopyInfo>> startCopyWithResponse(String sourceUrl, Map<String, String> metadata) {
        return withContext(context -> startCopyWithResponse(sourceUrl, metadata, context));
    }

    Mono<Response<FileCopyInfo>> startCopyWithResponse(String sourceUrl, Map<String, String> metadata,
        Context context) {
        return postProcessResponse(azureFileStorageClient.files()
            .startCopyWithRestResponseAsync(shareName, filePath, sourceUrl, null, metadata, context))
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
     * @return An empty response.
     */
    public Mono<Void> abortCopy(String copyId) {
        return abortCopyWithResponse(copyId).flatMap(FluxUtil::toMono);
    }

    /**
     * Aborts a pending Copy File operation, and leaves a destination file with zero length and full metadata.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Abort copy file from copy id("someCopyId") </p>
     *
     * {@codesnippet com.azure.storage.file.fileAsyncClient.abortCopyWithResponse#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/abort-copy-file">Azure Docs</a>.</p>
     *
     * @param copyId Specifies the copy id which has copying pending status associate with it.
     * @return A response containing the status of aborting copy the file.
     */
    public Mono<VoidResponse> abortCopyWithResponse(String copyId) {
        return withContext(context -> abortCopyWithResponse(copyId, context));
    }

    Mono<VoidResponse> abortCopyWithResponse(String copyId, Context context) {
        return postProcessResponse(azureFileStorageClient.files()
            .abortCopyWithRestResponseAsync(shareName, filePath, copyId, context))
            .map(VoidResponse::new);
    }

    /**
     * Downloads a file from the system, including its metadata and properties into a file specified by the path.
     *
     * <p>The file will be created and must not exist, if the file already exists a {@link FileAlreadyExistsException}
     * will be thrown.</p>
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
     * Downloads a file from the system, including its metadata and properties into a file specified by the path.
     *
     * <p>The file will be created and must not exist, if the file already exists a {@link FileAlreadyExistsException}
     * will be thrown.</p>
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
        return Mono.using(() -> channelSetup(downloadFilePath, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW),
            channel -> sliceFileRange(range)
                .flatMap(chunk -> downloadWithPropertiesWithResponse(chunk, false)
                    .map(dar -> dar.getValue().getBody())
                    .subscribeOn(Schedulers.elastic())
                    .flatMap(fbb -> FluxUtil
                        .writeFile(fbb, channel, chunk.getStart() - (range == null ? 0 : range.getStart()))
                        .subscribeOn(Schedulers.elastic())
                        .timeout(Duration.ofSeconds(DOWNLOAD_UPLOAD_CHUNK_TIMEOUT))
                        .retry(3, throwable -> throwable instanceof IOException
                            || throwable instanceof TimeoutException)))
                .then(), this::channelCleanUp);
    }

    private AsynchronousFileChannel channelSetup(String filePath, OpenOption... options) {
        try {
            return AsynchronousFileChannel.open(Paths.get(filePath), options);
        } catch (IOException e) {
            throw logger.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    private void channelCleanUp(AsynchronousFileChannel channel) {
        try {
            channel.close();
        } catch (IOException e) {
            throw logger.logExceptionAsError(Exceptions.propagate(new UncheckedIOException(e)));
        }
    }

    private Flux<FileRange> sliceFileRange(FileRange fileRange) {
        long offset = fileRange == null ? 0L : fileRange.getStart();
        Mono<Long> end;
        if (fileRange != null) {
            end = Mono.just(fileRange.getEnd());
        } else {
            end = Mono.empty();
        }
        end = end.switchIfEmpty(getProperties().map(FileProperties::getContentLength));
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
     * @return The {@link FileDownloadInfo file download Info}
     */
    public Mono<FileDownloadInfo> downloadWithProperties() {
        return downloadWithPropertiesWithResponse(null, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Downloads a file from the system, including its metadata and properties
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Download the file from 1024 to 2048 bytes with its metadata and properties and without the contentMD5. </p>
     *
     * {@codesnippet com.azure.storage.file.fileAsyncClient.downloadWithPropertiesWithResponse#filerange-boolean}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-file">Azure Docs</a>.</p>
     *
     * @param range Optional byte range which returns file data only from the specified range.
     * @param rangeGetContentMD5 Optional boolean which the service returns the MD5 hash for the range when it sets to
     * true, as long as the range is less than or equal to 4 MB in size.
     * @return A response containing the {@link FileDownloadInfo file download Info} with headers and response status
     * code
     */
    public Mono<Response<FileDownloadInfo>> downloadWithPropertiesWithResponse(FileRange range,
        Boolean rangeGetContentMD5) {
        return withContext(context -> downloadWithPropertiesWithResponse(range, rangeGetContentMD5, context));
    }

    Mono<Response<FileDownloadInfo>> downloadWithPropertiesWithResponse(FileRange range, Boolean rangeGetContentMD5,
        Context context) {
        String rangeString = range == null ? null : range.toString();
        return postProcessResponse(azureFileStorageClient.files()
            .downloadWithRestResponseAsync(shareName, filePath, null, rangeString, rangeGetContentMD5, context))
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
     * @return An empty response
     * @throws StorageException If the directory doesn't exist or the file doesn't exist.
     */
    public Mono<Void> delete() {
        return deleteWithResponse(null).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes the file associate with the client.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the file</p>
     *
     * {@codesnippet com.azure.storage.file.fileAsyncClient.deleteWithResponse}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-file2">Azure Docs</a>.</p>
     *
     * @return A response that only contains headers and response status code
     * @throws StorageException If the directory doesn't exist or the file doesn't exist.
     */
    public Mono<VoidResponse> deleteWithResponse() {
        return withContext(this::deleteWithResponse);
    }

    Mono<VoidResponse> deleteWithResponse(Context context) {
        return postProcessResponse(azureFileStorageClient.files()
            .deleteWithRestResponseAsync(shareName, filePath, context))
            .map(VoidResponse::new);
    }

    /**
     * Retrieves the properties of the storage account's file. The properties includes file metadata, last modified
     * date, is server encrypted, and eTag.
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
     * @return {@link FileProperties Storage file properties}
     */
    public Mono<FileProperties> getProperties() {
        return getPropertiesWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Retrieves the properties of the storage account's file. The properties includes file metadata, last modified
     * date, is server encrypted, and eTag.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve file properties</p>
     *
     * {@codesnippet com.azure.storage.file.fileAsyncClient.getPropertiesWithResponse}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-file-properties">Azure Docs</a>.</p>
     *
     * @return A response containing the {@link FileProperties storage file properties} and response status code
     */
    public Mono<Response<FileProperties>> getPropertiesWithResponse() {
        return withContext(this::getPropertiesWithResponse);
    }

    Mono<Response<FileProperties>> getPropertiesWithResponse(Context context) {
        return postProcessResponse(azureFileStorageClient.files()
            .getPropertiesWithRestResponseAsync(shareName, filePath, snapshot, null, context))
            .map(this::getPropertiesResponse);
    }

    /**
     * Sets the user-defined file properties to associate to the file.
     *
     * <p>If {@code null} is passed for the fileProperties.httpHeaders it will clear the httpHeaders associated to the
     * file.
     * If {@code null} is passed for the fileProperties.filesmbproperties it will preserve the filesmb properties
     * associated with the file.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set the httpHeaders of contentType of "text/plain"</p>
     *
     * {@codesnippet com.azure.storage.file.fileAsyncClient.setProperties#long-filehttpheaders-filesmbproperties-string}
     *
     * <p>Clear the metadata of the file and preserve the SMB properties</p>
     *
     * {@codesnippet com.azure.storage.file.fileAsyncClient.setProperties#long-filehttpheaders-filesmbproperties-string.clearHttpHeaderspreserveSMBProperties}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-file-properties">Azure Docs</a>.</p>
     *
     * @param newFileSize New file size of the file
     * @param httpHeaders The user settable file http headers.
     * @param smbProperties The user settable file smb properties.
     * @param filePermission The file permission of the file
     * @return The {@link FileInfo file info}
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     */
    public Mono<FileInfo> setProperties(long newFileSize, FileHTTPHeaders httpHeaders, FileSmbProperties smbProperties,
        String filePermission) {
        return setPropertiesWithResponse(newFileSize, httpHeaders, smbProperties, filePermission)
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Sets the user-defined file properties to associate to the file.
     *
     * <p>If {@code null} is passed for the httpHeaders it will clear the httpHeaders associated to the file.
     * If {@code null} is passed for the filesmbproperties it will preserve the filesmbproperties associated with the
     * file.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set the httpHeaders of contentType of "text/plain"</p>
     *
     * {@codesnippet com.azure.storage.file.fileAsyncClient.setPropertiesWithResponse#long-filehttpheaders-filesmbproperties-string}
     *
     * <p>Clear the metadata of the file and preserve the SMB properties</p>
     *
     * {@codesnippet com.azure.storage.file.fileAsyncClient.setPropertiesWithResponse#long-filehttpheaders-filesmbproperties-string.clearHttpHeaderspreserveSMBProperties}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-file-properties">Azure Docs</a>.</p>
     *
     * @param newFileSize New file size of the file.
     * @param httpHeaders The user settable file http headers.
     * @param smbProperties The user settable file smb properties.
     * @param filePermission The file permission of the file.
     * @return Response containing the {@link FileInfo file info} and response status code.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     */
    public Mono<Response<FileInfo>> setPropertiesWithResponse(long newFileSize, FileHTTPHeaders httpHeaders,
        FileSmbProperties smbProperties, String filePermission) {
        return withContext(context ->
            setPropertiesWithResponse(newFileSize, httpHeaders, smbProperties, filePermission, context));
    }

    Mono<Response<FileInfo>> setPropertiesWithResponse(long newFileSize, FileHTTPHeaders httpHeaders,
        FileSmbProperties smbProperties, String filePermission, Context context) {
        smbProperties = smbProperties == null ? new FileSmbProperties() : smbProperties;

        // Checks that file permission and file permission key are valid
        filePermissionAndKeyHelper(filePermission, smbProperties.getFilePermissionKey());

        // If file permission and file permission key are both not set then set default value
        filePermission = smbProperties.setFilePermission(filePermission, FileConstants.PRESERVE);
        String filePermissionKey = smbProperties.getFilePermissionKey();

        String fileAttributes = smbProperties.setNtfsFileAttributes(FileConstants.PRESERVE);
        String fileCreationTime = smbProperties.setFileCreationTime(FileConstants.PRESERVE);
        String fileLastWriteTime = smbProperties.setFileLastWriteTime(FileConstants.PRESERVE);

        return postProcessResponse(azureFileStorageClient.files()
            .setHTTPHeadersWithRestResponseAsync(shareName, filePath, fileAttributes, fileCreationTime,
                fileLastWriteTime, null, newFileSize, filePermission, filePermissionKey, httpHeaders, context))
            .map(this::setPropertiesResponse);
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
     * {@codesnippet com.azure.storage.file.fileAsyncClient.setMetadataWithResponse#map.clearMetadata}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-file-metadata">Azure Docs</a>.</p>
     *
     * @param metadata Options.Metadata to set on the file, if null is passed the metadata for the file is cleared
     * @return {@link FileMetadataInfo file meta info}
     * @throws StorageException If the file doesn't exist or the metadata contains invalid keys
     */
    public Mono<FileMetadataInfo> setMetadata(Map<String, String> metadata) {
        return setMetadataWithResponse(metadata).flatMap(FluxUtil::toMono);
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
     * {@codesnippet com.azure.storage.file.fileAsyncClient.setMetadataWithResponse#map}
     *
     * <p>Clear the metadata of the file</p>
     *
     * {@codesnippet com.azure.storage.file.fileAsyncClient.setMetadataWithResponse#map.clearMetadata}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-file-metadata">Azure Docs</a>.</p>
     *
     * @param metadata Options.Metadata to set on the file, if null is passed the metadata for the file is cleared
     * @return A response containing the {@link FileMetadataInfo file meta info} and status code
     * @throws StorageException If the file doesn't exist or the metadata contains invalid keys
     */
    public Mono<Response<FileMetadataInfo>> setMetadataWithResponse(Map<String, String> metadata) {
        return withContext(context -> setMetadataWithResponse(metadata, context));
    }

    Mono<Response<FileMetadataInfo>> setMetadataWithResponse(Map<String, String> metadata, Context context) {
        return postProcessResponse(azureFileStorageClient.files()
            .setMetadataWithRestResponseAsync(shareName, filePath, null, metadata, context))
            .map(this::setMetadataResponse);
    }

    /**
     * Uploads a range of bytes to the beginning of a file in storage file service. Upload operations performs an
     * in-place write on the specified file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Upload data "default" to the file in Storage File Service. </p>
     *
     * {@codesnippet com.azure.storage.file.fileAsyncClient.upload#flux-long}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/put-range">Azure Docs</a>.</p>
     *
     * @param data The data which will upload to the storage file.
     * @param length Specifies the number of bytes being transmitted in the request body.
     * @return A response that only contains headers and response status code
     */
    public Mono<FileUploadInfo> upload(Flux<ByteBuffer> data, long length) {
        return uploadWithResponse(data, length).flatMap(FluxUtil::toMono);
    }

    /**
     * Uploads a range of bytes to the beginning of a file in storage file service. Upload operations performs an
     * in-place write on the specified file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Upload "default" to the file. </p>
     *
     * {@codesnippet com.azure.storage.file.fileAsyncClient.uploadWithResponse#flux-long}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/put-range">Azure Docs</a>.</p>
     *
     * @param data The data which will upload to the storage file.
     * @param length Specifies the number of bytes being transmitted in the request body. When the FileRangeWriteType is
     * set to clear, the value of this header must be set to zero..
     * @return A response containing the {@link FileUploadInfo file upload info} with headers and response status code
     * @throws StorageException If you attempt to upload a range that is larger than 4 MB, the service returns status
     * code 413 (Request Entity Too Large)
     */
    public Mono<Response<FileUploadInfo>> uploadWithResponse(Flux<ByteBuffer> data, long length) {
        return withContext(context -> uploadWithResponse(data, length, context));
    }

    Mono<Response<FileUploadInfo>> uploadWithResponse(Flux<ByteBuffer> data, long length, Context context) {
        FileRange range = new FileRange(0, length - 1);
        return postProcessResponse(azureFileStorageClient.files()
            .uploadRangeWithRestResponseAsync(shareName, filePath, range.toString(), FileRangeWriteType.UPDATE,
                length, data, null, null, context))
            .map(this::uploadResponse);
    }

    /**
     * Uploads a range of bytes to specific of a file in storage file service. Upload operations performs an in-place
     * write on the specified file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Upload data "default" starting from 1024 bytes. </p>
     *
     * {@codesnippet com.azure.storage.file.fileAsyncClient.upload#flux-long-long}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/put-range">Azure Docs</a>.</p>
     *
     * @param data The data which will upload to the storage file.
     * @param length Specifies the number of bytes being transmitted in the request body.
     * @param offset Optional starting point of the upload range. It will start from the beginning if it is
     * {@code null}
     * @return The {@link FileUploadInfo file upload info}
     * @throws StorageException If you attempt to upload a range that is larger than 4 MB, the service returns status
     * code 413 (Request Entity Too Large)
     */
    public Mono<FileUploadInfo> upload(Flux<ByteBuffer> data, long length, long offset) {
        return uploadWithResponse(data, length, offset).flatMap(FluxUtil::toMono);
    }

    /**
     * Uploads a range of bytes to specific of a file in storage file service. Upload operations performs an in-place
     * write on the specified file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Upload the file from 1024 to 2048 bytes with its metadata and properties and without the contentMD5. </p>
     *
     * {@codesnippet com.azure.storage.file.fileAsyncClient.uploadWithResponse#flux-long-long}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/put-range">Azure Docs</a>.</p>
     *
     * @param data The data which will upload to the storage file.
     * @param offset Optional starting point of the upload range. It will start from the beginning if it is
     * {@code null}
     * @param length Specifies the number of bytes being transmitted in the request body. When the FileRangeWriteType is
     * set to clear, the value of this header must be set to zero.
     * @return A response containing the {@link FileUploadInfo file upload info} with headers and response status code
     * @throws StorageException If you attempt to upload a range that is larger than 4 MB, the service returns status
     * code 413 (Request Entity Too Large)
     */
    public Mono<Response<FileUploadInfo>> uploadWithResponse(Flux<ByteBuffer> data, long length, long offset) {
        return withContext(context -> uploadWithResponse(data, length, offset, context));
    }

    Mono<Response<FileUploadInfo>> uploadWithResponse(Flux<ByteBuffer> data, long length, long offset,
        Context context) {
        FileRange range = new FileRange(offset, offset + length - 1);
        return postProcessResponse(azureFileStorageClient.files()
            .uploadRangeWithRestResponseAsync(shareName, filePath, range.toString(), FileRangeWriteType.UPDATE,
                length, data, null, null, context))
            .map(this::uploadResponse);
    }

    /**
     * Uploads a range of bytes from one file to another file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Upload a number of bytes from a file at defined source and destination offsets </p>
     *
     * {@codesnippet com.azure.storage.file.fileAsyncClient.uploadRangeFromURL#long-long-long-uri}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/put-range">Azure Docs</a>.</p>
     *
     * @param length Specifies the number of bytes being transmitted in the request body.
     * @param destinationOffset Starting point of the upload range on the destination.
     * @param sourceOffset Starting point of the upload range on the source.
     * @param sourceURI Specifies the URL of the source file.
     * @return The {@link FileUploadRangeFromURLInfo file upload range from url info}
     */
    // TODO: (gapra) Fix put range from URL link. Service docs have not been updated to show this API
    public Mono<FileUploadRangeFromURLInfo> uploadRangeFromURL(long length, long destinationOffset, long sourceOffset,
        URI sourceURI) {
        return uploadRangeFromURLWithResponse(length, destinationOffset, sourceOffset, sourceURI)
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Uploads a range of bytes from one file to another file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Upload a number of bytes from a file at defined source and destination offsets </p>
     *
     * {@codesnippet com.azure.storage.file.fileAsyncClient.uploadRangeFromURLWithResponse#long-long-long-uri}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/put-range">Azure Docs</a>.</p>
     *
     * @param length Specifies the number of bytes being transmitted in the request body.
     * @param destinationOffset Starting point of the upload range on the destination.
     * @param sourceOffset Starting point of the upload range on the source.
     * @param sourceURI Specifies the URL of the source file.
     * @return A response containing the {@link FileUploadRangeFromURLInfo file upload range from url info} with headers
     * and response status code.
     */
    // TODO: (gapra) Fix put range from URL link. Service docs have not been updated to show this API
    public Mono<Response<FileUploadRangeFromURLInfo>> uploadRangeFromURLWithResponse(long length,
        long destinationOffset, long sourceOffset, URI sourceURI) {
        return withContext(context ->
            uploadRangeFromURLWithResponse(length, destinationOffset, sourceOffset, sourceURI, context));
    }

    Mono<Response<FileUploadRangeFromURLInfo>> uploadRangeFromURLWithResponse(long length, long destinationOffset,
        long sourceOffset, URI sourceURI, Context context) {
        FileRange destinationRange = new FileRange(destinationOffset, destinationOffset + length - 1);
        FileRange sourceRange = new FileRange(sourceOffset, sourceOffset + length - 1);

        return postProcessResponse(azureFileStorageClient.files()
            .uploadRangeFromURLWithRestResponseAsync(shareName, filePath, destinationRange.toString(),
                sourceURI.toString(), 0, null, sourceRange.toString(), null, null, context))
            .map(this::uploadRangeFromURLResponse);
    }

    /**
     * Clear a range of bytes to specific of a file in storage file service. Clear operations performs an in-place write
     * on the specified file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Clears the first 1024 bytes. </p>
     *
     * {@codesnippet com.azure.storage.file.fileAsyncClient.clearRange#long}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/put-range">Azure Docs</a>.</p>
     *
     * @param length Specifies the number of bytes being cleared.
     * @return The {@link FileUploadInfo file upload info}
     */
    public Mono<FileUploadInfo> clearRange(long length) {
        return clearRangeWithResponse(length, 0).flatMap(FluxUtil::toMono);
    }

    /**
     * Clear a range of bytes to specific of a file in storage file service. Clear operations performs an in-place write
     * on the specified file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Clear the range starting from 1024 with length of 1024. </p>
     *
     * {@codesnippet com.azure.storage.file.fileAsyncClient.clearRange#long-long}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/put-range">Azure Docs</a>.</p>
     *
     * @param length Specifies the number of bytes being cleared in the request body.
     * @param offset Optional starting point of the upload range. It will start from the beginning if it is
     * {@code null}
     * @return A response of {@link FileUploadInfo file upload info} that only contains headers and response status code
     */
    public Mono<Response<FileUploadInfo>> clearRangeWithResponse(long length, long offset) {
        return withContext(context -> clearRangeWithResponse(length, offset, context));
    }

    Mono<Response<FileUploadInfo>> clearRangeWithResponse(long length, long offset, Context context) {
        FileRange range = new FileRange(offset, offset + length - 1);
        return postProcessResponse(azureFileStorageClient.files()
            .uploadRangeWithRestResponseAsync(shareName, filePath, range.toString(), FileRangeWriteType.CLEAR, 0L,
                null, null, null, context))
            .map(this::uploadResponse);
    }

    /**
     * Uploads file to storage file service.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p> Upload the file from the source file path. </p>
     *
     * (@codesnippet com.azure.storage.file.fileAsyncClient.uploadFromFile#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-file">Azure Docs Create File</a>
     * and
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/put-range">Azure Docs Upload</a>.</p>
     *
     * @param uploadFilePath The path where store the source file to upload
     * @return An empty response.
     * @throws UncheckedIOException If an I/O error occurs.
     */
    public Mono<Void> uploadFromFile(String uploadFilePath) {
        return Mono.using(() -> channelSetup(uploadFilePath, StandardOpenOption.READ),
            channel -> Flux.fromIterable(sliceFile(uploadFilePath))
                .flatMap(chunk -> upload(FluxUtil.readFile(channel, chunk.getStart(),
                    chunk.getEnd() - chunk.getStart() + 1), chunk.getEnd() - chunk.getStart() + 1, chunk.getStart())
                .timeout(Duration.ofSeconds(DOWNLOAD_UPLOAD_CHUNK_TIMEOUT))
                .retry(3, throwable -> throwable instanceof IOException || throwable instanceof TimeoutException))
                .then(), this::channelCleanUp);
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
    public PagedFlux<FileRange> listRanges() {
        return listRanges(null);
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
    public PagedFlux<FileRange> listRanges(FileRange range) {
        return listRangesWithOptionalTimeout(range, null, Context.NONE);
    }

    PagedFlux<FileRange> listRangesWithOptionalTimeout(FileRange range, Duration timeout, Context context) {
        String rangeString = range == null ? null : range.toString();
        Function<String, Mono<PagedResponse<FileRange>>> retriever =
            marker -> postProcessResponse(Utility.applyOptionalTimeout(this.azureFileStorageClient.files()
                .getRangeListWithRestResponseAsync(shareName, filePath, snapshot, null, rangeString, context), timeout)
                .map(response -> new PagedResponseBase<>(response.getRequest(),
                    response.getStatusCode(),
                    response.getHeaders(),
                    response.getValue().stream().map(FileRange::new).collect(Collectors.toList()),
                    null,
                    response.getDeserializedHeaders())));

        return new PagedFlux<>(() -> retriever.apply(null), retriever);
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
    public PagedFlux<HandleItem> listHandles() {
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
    public PagedFlux<HandleItem> listHandles(Integer maxResults) {
        return listHandlesWithOptionalTimeout(maxResults, null, Context.NONE);
    }

    PagedFlux<HandleItem> listHandlesWithOptionalTimeout(Integer maxResults, Duration timeout, Context context) {
        Function<String, Mono<PagedResponse<HandleItem>>> retriever =
            marker -> postProcessResponse(Utility.applyOptionalTimeout(this.azureFileStorageClient.files()
                .listHandlesWithRestResponseAsync(shareName, filePath, marker, maxResults, null, snapshot,
                    context), timeout)
                .map(response -> new PagedResponseBase<>(response.getRequest(),
                    response.getStatusCode(),
                    response.getHeaders(),
                    response.getValue().getHandleList(),
                    response.getValue().getNextMarker(),
                    response.getDeserializedHeaders())));

        return new PagedFlux<>(() -> retriever.apply(null), retriever);
    }

    /**
     * Closes a handle or handles opened on a file at the service. It is intended to be used alongside {@link
     * FileAsyncClient#listHandles()} (Integer)} .
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
     * @param handleId Specifies the handle ID to be closed. Use an asterisk ('*') as a wildcard string to specify all
     * handles.
     * @return The counts of number of handles closed
     */
    public PagedFlux<Integer> forceCloseHandles(String handleId) {
        return forceCloseHandlesWithOptionalTimeout(handleId, null, Context.NONE);
    }

    PagedFlux<Integer> forceCloseHandlesWithOptionalTimeout(String handleId, Duration timeout, Context context) {
        // TODO: Will change the return type to how many handles have been closed.
        // Implement one more API to force close all handles.
        // TODO: @see <a href="https://github.com/Azure/azure-sdk-for-java/issues/4525">Github
        Function<String, Mono<PagedResponse<Integer>>> retriever =
            marker -> postProcessResponse(Utility.applyOptionalTimeout(this.azureFileStorageClient.files()
                .forceCloseHandlesWithRestResponseAsync(shareName, filePath, handleId, null, marker,
                    snapshot, context), timeout)
                .map(response -> new PagedResponseBase<>(response.getRequest(),
                    response.getStatusCode(),
                    response.getHeaders(),
                    Collections.singletonList(response.getDeserializedHeaders().getNumberOfHandlesClosed()),
                    response.getDeserializedHeaders().getMarker(),
                    response.getDeserializedHeaders())));

        return new PagedFlux<>(() -> retriever.apply(null), retriever);
    }

    /**
     * Get snapshot id which attached to {@link FileAsyncClient}. Return {@code null} if no snapshot id attached.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Get the share snapshot id. </p>
     *
     * {@codesnippet com.azure.storage.file.fileAsyncClient.getShareSnapshotId}
     *
     * @return The snapshot id which is a unique {@code DateTime} value that identifies the share snapshot to its base
     * share.
     */
    public String getShareSnapshotId() {
        return this.snapshot;
    }

    /**
     * Generates a SAS token with the specified parameters
     *
     * @param permissions The {@code FileSASPermission} permission for the SAS
     * @param expiryTime The {@code OffsetDateTime} expiry time for the SAS
     * @return A string that represents the SAS token
     */
    public String generateSAS(FileSASPermission permissions, OffsetDateTime expiryTime) {
        return this.generateSAS(null, permissions, expiryTime, null /* startTime */,   /* identifier */ null /*
        version */, null /* sasProtocol */, null /* ipRange */, null /* cacheControl */, null /* contentLanguage*/,
            null /* contentEncoding */, null /* contentLanguage */, null /* contentType */);
    }

    /**
     * Generates a SAS token with the specified parameters
     *
     * @param identifier The {@code String} name of the access policy on the share this SAS references if any
     * @return A string that represents the SAS token
     */
    public String generateSAS(String identifier) {
        return this.generateSAS(identifier, null  /* permissions */, null /* expiryTime */, null /* startTime */,
            null /* version */, null /* sasProtocol */, null /* ipRange */, null /* cacheControl */, null /*
            contentLanguage*/, null /* contentEncoding */, null /* contentLanguage */, null /* contentType */);
    }

    /**
     * Generates a SAS token with the specified parameters
     *
     * @param identifier The {@code String} name of the access policy on the share this SAS references if any
     * @param permissions The {@code FileSASPermission} permission for the SAS
     * @param expiryTime The {@code OffsetDateTime} expiry time for the SAS
     * @param startTime An optional {@code OffsetDateTime} start time for the SAS
     * @param version An optional {@code String} version for the SAS
     * @param sasProtocol An optional {@code SASProtocol} protocol for the SAS
     * @param ipRange An optional {@code IPRange} ip address range for the SAS
     * @return A string that represents the SAS token
     */
    public String generateSAS(String identifier, FileSASPermission permissions, OffsetDateTime expiryTime,
        OffsetDateTime startTime, String version, SASProtocol sasProtocol, IPRange ipRange) {
        return this.generateSAS(identifier, permissions, expiryTime, startTime, version, sasProtocol, ipRange, null
            /* cacheControl */, null /* contentLanguage*/, null /* contentEncoding */, null /* contentLanguage */,
            null /* contentType */);
    }

    /**
     * Generates a SAS token with the specified parameters
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.fileAsyncClient.generateSAS#String-FileSASPermission-OffsetDateTime-OffsetDateTime-String-SASProtocol-IPRange-String-String-String-String-String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-service-sas">Azure Docs</a>.</p>
     *
     * @param identifier The {@code String} name of the access policy on the share this SAS references if any
     * @param permissions The {@code FileSASPermission} permission for the SAS
     * @param expiryTime The {@code OffsetDateTime} expiry time for the SAS
     * @param startTime An optional {@code OffsetDateTime} start time for the SAS
     * @param version An optional {@code String} version for the SAS
     * @param sasProtocol An optional {@code SASProtocol} protocol for the SAS
     * @param ipRange An optional {@code IPRange} ip address range for the SAS
     * @param cacheControl An optional {@code String} cache-control header for the SAS.
     * @param contentDisposition An optional {@code String} content-disposition header for the SAS.
     * @param contentEncoding An optional {@code String} content-encoding header for the SAS.
     * @param contentLanguage An optional {@code String} content-language header for the SAS.
     * @param contentType An optional {@code String} content-type header for the SAS.
     * @return A string that represents the SAS token
     */
    public String generateSAS(String identifier, FileSASPermission permissions, OffsetDateTime expiryTime,
        OffsetDateTime startTime, String version, SASProtocol sasProtocol, IPRange ipRange, String cacheControl,
        String contentDisposition, String contentEncoding, String contentLanguage, String contentType) {

        FileServiceSASSignatureValues fileServiceSASSignatureValues = new FileServiceSASSignatureValues(version,
            sasProtocol, startTime, expiryTime, permissions == null ? null : permissions.toString(), ipRange,
            identifier, cacheControl, contentDisposition, contentEncoding, contentLanguage, contentType);

        SharedKeyCredential sharedKeyCredential =
            Utility.getSharedKeyCredential(this.azureFileStorageClient.getHttpPipeline());

        Utility.assertNotNull("sharedKeyCredential", sharedKeyCredential);

        FileServiceSASSignatureValues values = configureServiceSASSignatureValues(fileServiceSASSignatureValues,
            sharedKeyCredential.getAccountName());

        FileServiceSASQueryParameters fileServiceSasQueryParameters =
            values.generateSASQueryParameters(sharedKeyCredential);

        return fileServiceSasQueryParameters.encode();
    }

    /**
     * Sets fileServiceSASSignatureValues parameters dependent on the current file type
     */
    FileServiceSASSignatureValues configureServiceSASSignatureValues(
        FileServiceSASSignatureValues fileServiceSASSignatureValues, String accountName) {

        // Set canonical name
        fileServiceSASSignatureValues.setCanonicalName(this.shareName, this.filePath, accountName);

        // Set resource
        fileServiceSASSignatureValues.setResource(Constants.UrlConstants.SAS_FILE_CONSTANT);

        return fileServiceSASSignatureValues;
    }

    /**
     * Get the share name of file client.
     *
     * <p>Get the share name. </p>
     *
     * {@codesnippet com.azure.storage.file.fileAsyncClient.getShareName}
     *
     * @return The share name of the file.
     */
    public String getShareName() {
        return shareName;
    }

    /**
     * Get file path of the client.
     *
     * <p>Get the file path. </p>
     *
     * {@codesnippet com.azure.storage.file.fileAsyncClient.getFilePath}
     *
     * @return The path of the file.
     */
    public String getFilePath() {
        return filePath;
    }

    private Response<FileInfo> createFileInfoResponse(final FilesCreateResponse response) {
        String eTag = response.getDeserializedHeaders().getETag();
        OffsetDateTime lastModified = response.getDeserializedHeaders().getLastModified();
        boolean isServerEncrypted = response.getDeserializedHeaders().isServerEncrypted();
        FileSmbProperties smbProperties = new FileSmbProperties(response.getHeaders());
        FileInfo fileInfo = new FileInfo(eTag, lastModified, isServerEncrypted, smbProperties);
        return new SimpleResponse<>(response, fileInfo);
    }

    private Response<FileCopyInfo> startCopyResponse(final FilesStartCopyResponse response) {
        String eTag = response.getDeserializedHeaders().getETag();
        OffsetDateTime lastModified = response.getDeserializedHeaders().getLastModified();
        String copyId = response.getDeserializedHeaders().getCopyId();
        CopyStatusType copyStatus = response.getDeserializedHeaders().getCopyStatus();
        FileCopyInfo fileCopyInfo = new FileCopyInfo(eTag, lastModified, copyId, copyStatus);
        return new SimpleResponse<>(response, fileCopyInfo);
    }

    private Response<FileInfo> setPropertiesResponse(final FilesSetHTTPHeadersResponse response) {
        String eTag = response.getDeserializedHeaders().getETag();
        OffsetDateTime lastModified = response.getDeserializedHeaders().getLastModified();
        boolean isServerEncrypted = response.getDeserializedHeaders().isServerEncrypted();
        FileSmbProperties smbProperties = new FileSmbProperties(response.getHeaders());
        FileInfo fileInfo = new FileInfo(eTag, lastModified, isServerEncrypted, smbProperties);
        return new SimpleResponse<>(response, fileInfo);
    }

    private Response<FileDownloadInfo> downloadWithPropertiesResponse(final FilesDownloadResponse response) {
        String eTag = response.getDeserializedHeaders().getETag();
        OffsetDateTime lastModified = response.getDeserializedHeaders().getLastModified();
        Map<String, String> metadata = response.getDeserializedHeaders().getMetadata();
        Long contentLength = response.getDeserializedHeaders().getContentLength();
        String contentType = response.getDeserializedHeaders().getContentType();
        String contentRange = response.getDeserializedHeaders().getContentRange();
        Flux<ByteBuffer> body = response.getValue();
        FileSmbProperties smbProperties = new FileSmbProperties(response.getHeaders());
        FileDownloadInfo fileDownloadInfo = new FileDownloadInfo(eTag, lastModified, metadata, contentLength,
            contentType, contentRange, body, smbProperties);
        return new SimpleResponse<>(response, fileDownloadInfo);
    }

    private Response<FileProperties> getPropertiesResponse(final FilesGetPropertiesResponse response) {
        FileGetPropertiesHeaders headers = response.getDeserializedHeaders();
        String eTag = headers.getETag();
        OffsetDateTime lastModified = headers.getLastModified();
        Map<String, String> metadata = headers.getMetadata();
        String fileType = headers.getFileType();
        Long contentLength = headers.getContentLength();
        String contentType = headers.getContentType();
        byte[] contentMD5;
        try {
            contentMD5 = headers.getContentMD5();
        } catch (NullPointerException e) {
            contentMD5 = null;
        }
        String contentEncoding = headers.getContentEncoding();
        String cacheControl = headers.getCacheControl();
        String contentDisposition = headers.getContentDisposition();
        OffsetDateTime copyCompletionTime = headers.getCopyCompletionTime();
        String copyStatusDescription = headers.getCopyStatusDescription();
        String copyId = headers.getCopyId();
        String copyProgress = headers.getCopyProgress();
        String copySource = headers.getCopySource();
        CopyStatusType copyStatus = headers.getCopyStatus();
        Boolean isServerEncrpted = headers.isServerEncrypted();
        FileSmbProperties smbProperties = new FileSmbProperties(response.getHeaders());
        FileProperties fileProperties = new FileProperties(eTag, lastModified, metadata, fileType, contentLength,
            contentType, contentMD5, contentEncoding, cacheControl, contentDisposition, copyCompletionTime,
            copyStatusDescription, copyId, copyProgress, copySource, copyStatus, isServerEncrpted, smbProperties);
        return new SimpleResponse<>(response, fileProperties);
    }

    private Response<FileUploadInfo> uploadResponse(final FilesUploadRangeResponse response) {
        FileUploadRangeHeaders headers = response.getDeserializedHeaders();
        String eTag = headers.getETag();
        OffsetDateTime lastModified = headers.getLastModified();
        byte[] contentMD5;
        try {
            contentMD5 = headers.getContentMD5();
        } catch (NullPointerException e) {
            contentMD5 = null;
        }
        Boolean isServerEncrypted = headers.isServerEncrypted();
        FileUploadInfo fileUploadInfo = new FileUploadInfo(eTag, lastModified, contentMD5, isServerEncrypted);
        return new SimpleResponse<>(response, fileUploadInfo);
    }

    private Response<FileUploadRangeFromURLInfo> uploadRangeFromURLResponse(
        final FilesUploadRangeFromURLResponse response) {
        FileUploadRangeFromURLHeaders headers = response.getDeserializedHeaders();
        String eTag = headers.getETag();
        OffsetDateTime lastModified = headers.getLastModified();
        Boolean isServerEncrypted = headers.isServerEncrypted();
        FileUploadRangeFromURLInfo fileUploadRangeFromURLInfo =
            new FileUploadRangeFromURLInfo(eTag, lastModified, isServerEncrypted);
        return new SimpleResponse<>(response, fileUploadRangeFromURLInfo);
    }

    private Response<FileMetadataInfo> setMetadataResponse(final FilesSetMetadataResponse response) {
        String eTag = response.getDeserializedHeaders().getETag();
        boolean isServerEncrypted = response.getDeserializedHeaders().isServerEncrypted();
        FileMetadataInfo fileMetadataInfo = new FileMetadataInfo(eTag, isServerEncrypted);
        return new SimpleResponse<>(response, fileMetadataInfo);
    }
}
