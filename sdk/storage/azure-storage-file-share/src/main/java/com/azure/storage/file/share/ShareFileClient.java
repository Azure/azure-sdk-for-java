// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.SyncPoller;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.file.share.models.CloseHandlesInfo;
import com.azure.storage.file.share.models.HandleItem;
import com.azure.storage.file.share.models.PermissionCopyModeType;
import com.azure.storage.file.share.models.ShareFileCopyInfo;
import com.azure.storage.file.share.models.ShareFileDownloadResponse;
import com.azure.storage.file.share.models.ShareFileHttpHeaders;
import com.azure.storage.file.share.models.ShareFileInfo;
import com.azure.storage.file.share.models.ShareFileMetadataInfo;
import com.azure.storage.file.share.models.ShareFileProperties;
import com.azure.storage.file.share.models.ShareFileRange;
import com.azure.storage.file.share.models.ShareFileRangeList;
import com.azure.storage.file.share.models.ShareFileUploadOptions;
import com.azure.storage.file.share.models.ShareFileUploadInfo;
import com.azure.storage.file.share.models.ShareFileUploadRangeFromUrlInfo;
import com.azure.storage.file.share.models.ShareFileUploadRangeOptions;
import com.azure.storage.file.share.models.ShareRequestConditions;
import com.azure.storage.file.share.models.ShareStorageException;
import com.azure.storage.file.share.options.ShareFileDownloadOptions;
import com.azure.storage.file.share.options.ShareFileListRangesDiffOptions;
import com.azure.storage.file.share.options.ShareFileUploadRangeFromUrlOptions;
import com.azure.storage.file.share.sas.ShareServiceSasSignatureValues;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.FileAlreadyExistsException;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

import static com.azure.storage.common.implementation.StorageImplUtils.blockWithOptionalTimeout;

/**
 * This class provides a client that contains all the operations for interacting files under Azure Storage File Service.
 * Operations allowed by the client are creating, uploading, copying, listing, downloading, and deleting files.
 *
 * <p><strong>Instantiating a synchronous File Client</strong></p>
 *
 * {@codesnippet com.azure.storage.file.share.ShareFileClient.instantiation}
 *
 * <p>View {@link ShareFileClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see ShareFileClientBuilder
 * @see ShareFileAsyncClient
 * @see StorageSharedKeyCredential
 */
@ServiceClient(builder = ShareFileClientBuilder.class)
public class ShareFileClient {
    private final ClientLogger logger = new ClientLogger(ShareFileClient.class);

    private final ShareFileAsyncClient shareFileAsyncClient;

    /**
     * Creates a ShareFileClient that wraps a ShareFileAsyncClient and requests.
     *
     * @param shareFileAsyncClient ShareFileAsyncClient that is used to send requests
     */
    ShareFileClient(ShareFileAsyncClient shareFileAsyncClient) {
        this.shareFileAsyncClient = shareFileAsyncClient;
    }

    /**
     * Get the url of the storage account.
     *
     * @return the URL of the storage account
     */
    public String getAccountUrl() {
        return shareFileAsyncClient.getAccountUrl();
    }

    /**
     * Get the url of the storage file client.
     *
     * @return the URL of the storage file client.
     */
    public String getFileUrl() {
        return shareFileAsyncClient.getFileUrl();
    }

    /**
     * Gets the service version the client is using.
     *
     * @return the service version the client is using.
     */
    public ShareServiceVersion getServiceVersion() {
        return shareFileAsyncClient.getServiceVersion();
    }

    /**
     * Opens a file input stream to download the file.
     * <p>
     *
     * @return An <code>InputStream</code> object that represents the stream to use for reading from the file.
     * @throws ShareStorageException If a storage service error occurred.
     */
    public final StorageFileInputStream openInputStream() {
        return openInputStream(new ShareFileRange(0));
    }

    /**
     * Opens a file input stream to download the specified range of the file.
     * <p>
     *
     * @param range {@link ShareFileRange}
     * @return An <code>InputStream</code> object that represents the stream to use for reading from the file.
     * @throws ShareStorageException If a storage service error occurred.
     */
    public final StorageFileInputStream openInputStream(ShareFileRange range) {
        return new StorageFileInputStream(shareFileAsyncClient, range.getStart(), range.getEnd());
    }

    /**
     * Creates and opens an output stream to write data to the file. If the file already exists on the service, it will
     * be overwritten.
     *
     * @return A {@link StorageFileOutputStream} object used to write data to the file.
     * @throws ShareStorageException If a storage service error occurred.
     */
    public final StorageFileOutputStream getFileOutputStream() {
        return getFileOutputStream(0);
    }

    /**
     * Creates and opens an output stream to write data to the file. If the file already exists on the service, it will
     * be overwritten.
     *
     * @param offset Starting point of the upload range, if {@code null} it will start from the beginning.
     * @return A {@link StorageFileOutputStream} object used to write data to the file.
     * @throws ShareStorageException If a storage service error occurred.
     */
    public final StorageFileOutputStream getFileOutputStream(long offset) {
        return new StorageFileOutputStream(shareFileAsyncClient, offset);
    }

    /**
     * Determines if the file this client represents exists in the cloud.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.exists}
     *
     * @return Flag indicating existence of the file.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Boolean exists() {
        return existsWithResponse(null, Context.NONE).getValue();
    }

    /**
     * Determines if the file this client represents exists in the cloud.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.existsWithResponse#Duration-Context}
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return Flag indicating existence of the file.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Boolean> existsWithResponse(Duration timeout, Context context) {
        Mono<Response<Boolean>> response = shareFileAsyncClient.existsWithResponse(context);

        return blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Creates a file in the storage account and returns a response of {@link ShareFileInfo} to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the file with length of 1024 bytes, some headers and metadata.</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.create}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-file">Azure Docs</a>.</p>
     *
     * @param maxSize The maximum size in bytes for the file.
     * @return The {@link ShareFileInfo file info}
     * @throws ShareStorageException If the file has already existed, the parent directory does not exist or fileName
     * is an invalid resource name.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ShareFileInfo create(long maxSize) {
        return createWithResponse(maxSize, null, null, null, null, null, Context.NONE).getValue();
    }

    /**
     * Creates a file in the storage account and returns a response of ShareFileInfo to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the file with length of 1024 bytes, some headers, file smb properties and metadata.</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.createWithResponse#long-ShareFileHttpHeaders-FileSmbProperties-String-Map-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-file">Azure Docs</a>.</p>
     *
     * @param maxSize The maximum size in bytes for the file.
     * @param httpHeaders The user settable file http headers.
     * @param smbProperties The user settable file smb properties.
     * @param filePermission The file permission of the file.
     * @param metadata Optional name-value pairs associated with the file as metadata.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the {@link ShareFileInfo file info} and the status of creating the file.
     * @throws ShareStorageException If the directory has already existed, the parent directory does not exist or
     * directory is an invalid resource name.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     * @see <a href="https://docs.microsoft.com/dotnet/csharp/language-reference/">C# identifiers</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareFileInfo> createWithResponse(long maxSize, ShareFileHttpHeaders httpHeaders,
        FileSmbProperties smbProperties, String filePermission, Map<String, String> metadata, Duration timeout,
        Context context) {
        return this.createWithResponse(maxSize, httpHeaders, smbProperties, filePermission, metadata, null, timeout,
            context);
    }

    /**
     * Creates a file in the storage account and returns a response of ShareFileInfo to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the file with length of 1024 bytes, some headers, file smb properties and metadata.</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.createWithResponse#long-ShareFileHttpHeaders-FileSmbProperties-String-Map-ShareRequestConditions-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-file">Azure Docs</a>.</p>
     *
     * @param maxSize The maximum size in bytes for the file.
     * @param httpHeaders The user settable file http headers.
     * @param smbProperties The user settable file smb properties.
     * @param filePermission The file permission of the file.
     * @param metadata Optional name-value pairs associated with the file as metadata.
     * @param requestConditions {@link ShareRequestConditions}
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the {@link ShareFileInfo file info} and the status of creating the file.
     * @throws ShareStorageException If the directory has already existed, the parent directory does not exist or
     * directory is an invalid resource name.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     * @see <a href="https://docs.microsoft.com/dotnet/csharp/language-reference/">C# identifiers</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareFileInfo> createWithResponse(long maxSize, ShareFileHttpHeaders httpHeaders,
        FileSmbProperties smbProperties, String filePermission, Map<String, String> metadata,
        ShareRequestConditions requestConditions, Duration timeout, Context context) {
        Mono<Response<ShareFileInfo>> response = shareFileAsyncClient
            .createWithResponse(maxSize, httpHeaders, smbProperties, filePermission, metadata, requestConditions,
                context);
        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Copies a blob or file to a destination file within the storage account.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Copy file from source getDirectoryUrl to the {@code resourcePath} </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.beginCopy#string-map-duration}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/copy-file">Azure Docs</a>.</p>
     *
     * @param sourceUrl Specifies the URL of the source file or blob, up to 2 KB in length.
     * @param metadata Optional name-value pairs associated with the file as metadata. Metadata names must adhere to the
     * naming rules.
     * @param pollInterval Duration between each poll for the copy status. If none is specified, a default of one second
     * is used.
     * @return A {@link SyncPoller} to poll the progress of copy operation.
     * @see <a href="https://docs.microsoft.com/dotnet/csharp/language-reference/">C# identifiers</a>
     */
    public SyncPoller<ShareFileCopyInfo, Void> beginCopy(String sourceUrl, Map<String, String> metadata,
        Duration pollInterval) {
        return this.beginCopy(sourceUrl, null, null, null, null, null, metadata, pollInterval, null);
    }

    /**
     * Copies a blob or file to a destination file within the storage account.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Copy file from source getDirectoryUrl to the {@code resourcePath} </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.beginCopy#string-filesmbproperties-string-permissioncopymodetype-boolean-boolean-map-duration-ShareRequestConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/copy-file">Azure Docs</a>.</p>
     *
     * @param sourceUrl Specifies the URL of the source file or blob, up to 2 KB in length.
     * @param smbProperties The user settable file smb properties.
     * @param filePermission The file permission of the file.
     * @param filePermissionCopyMode Mode of file permission acquisition.
     * @param ignoreReadOnly Whether or not to copy despite target being read only. (default is false)
     * @param setArchiveAttribute Whether or not the archive attribute is to be set on the target. (default is true)
     * @param metadata Optional name-value pairs associated with the file as metadata. Metadata names must adhere to the
     * naming rules.
     * @param pollInterval Duration between each poll for the copy status. If none is specified, a default of one second
     * is used.
     * @param destinationRequestConditions {@link ShareRequestConditions}
     * @return A {@link SyncPoller} to poll the progress of copy operation.
     * @see <a href="https://docs.microsoft.com/dotnet/csharp/language-reference/">C# identifiers</a>
     */
    public SyncPoller<ShareFileCopyInfo, Void> beginCopy(String sourceUrl, FileSmbProperties smbProperties,
        String filePermission, PermissionCopyModeType filePermissionCopyMode, Boolean ignoreReadOnly,
        Boolean setArchiveAttribute, Map<String, String> metadata, Duration pollInterval,
        ShareRequestConditions destinationRequestConditions) {
        return shareFileAsyncClient.beginCopy(sourceUrl, smbProperties, filePermission, filePermissionCopyMode,
            ignoreReadOnly, setArchiveAttribute, metadata, pollInterval, destinationRequestConditions)
                .getSyncPoller();
    }

    /**
     * Aborts a pending Copy File operation, and leaves a destination file with zero length and full metadata.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Abort copy file from copy id("someCopyId") </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.abortCopy#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/abort-copy-file">Azure Docs</a>.</p>
     *
     * @param copyId Specifies the copy id which has copying pending status associate with it.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void abortCopy(String copyId) {
        abortCopyWithResponse(copyId, null, Context.NONE);
    }

    /**
     * Aborts a pending Copy File operation, and leaves a destination file with zero length and full metadata.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Abort copy file from copy id("someCopyId") </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.abortCopyWithResponse#string-duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/abort-copy-file">Azure Docs</a>.</p>
     *
     * @param copyId Specifies the copy id which has copying pending status associate with it.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the status of aborting copy the file.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> abortCopyWithResponse(String copyId, Duration timeout, Context context) {
        return this.abortCopyWithResponse(copyId, null, timeout, context);
    }

    /**
     * Aborts a pending Copy File operation, and leaves a destination file with zero length and full metadata.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Abort copy file from copy id("someCopyId") </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.abortCopyWithResponse#string-ShareRequestConditions-duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/abort-copy-file">Azure Docs</a>.</p>
     *
     * @param copyId Specifies the copy id which has copying pending status associate with it.
     * @param requestConditions {@link ShareRequestConditions}
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the status of aborting copy the file.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> abortCopyWithResponse(String copyId, ShareRequestConditions requestConditions,
        Duration timeout, Context context) {
        Mono<Response<Void>> response = shareFileAsyncClient.abortCopyWithResponse(copyId, requestConditions, context);
        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
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
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.downloadToFile#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-file">Azure Docs</a>.</p>
     *
     * @param downloadFilePath The path where store the downloaded file
     * @return The properties of the file.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ShareFileProperties downloadToFile(String downloadFilePath) {
        return downloadToFileWithResponse(downloadFilePath, null, null, Context.NONE).getValue();
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
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.downloadToFileWithResponse#String-ShareFileRange-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-file">Azure Docs</a>.</p>
     *
     * @param downloadFilePath The path where store the downloaded file
     * @param range Optional byte range which returns file data only from the specified range.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The response of the file properties.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareFileProperties> downloadToFileWithResponse(String downloadFilePath, ShareFileRange range,
        Duration timeout, Context context) {
        return this.downloadToFileWithResponse(downloadFilePath, range, null, timeout, context);
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
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.downloadToFileWithResponse#String-ShareFileRange-ShareRequestConditions-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-file">Azure Docs</a>.</p>
     *
     * @param downloadFilePath The path where store the downloaded file
     * @param range Optional byte range which returns file data only from the specified range.
     * @param requestConditions {@link ShareRequestConditions}
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The response of the file properties.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareFileProperties> downloadToFileWithResponse(String downloadFilePath, ShareFileRange range,
        ShareRequestConditions requestConditions, Duration timeout, Context context) {
        Mono<Response<ShareFileProperties>> response = shareFileAsyncClient.downloadToFileWithResponse(downloadFilePath,
            range, requestConditions, context);
        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Downloads a file from the system, including its metadata and properties
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Download the file with its metadata and properties. </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.download#OutputStream}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-file">Azure Docs</a>.</p>
     *
     * @param stream A non-null {@link OutputStream} where the downloaded data will be written.
     * @throws NullPointerException If {@code stream} is {@code null}.
     */
    public void download(OutputStream stream) {
        downloadWithResponse(stream, null, null, null, Context.NONE);
    }

    /**
     * Downloads a file from the system, including its metadata and properties
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Download the file from 1024 to 2048 bytes with its metadata and properties and without the contentMD5. </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.downloadWithResponse#OutputStream-ShareFileRange-Boolean-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-file">Azure Docs</a>.</p>
     *
     * @param stream A non-null {@link OutputStream} where the downloaded data will be written.
     * @param range Optional byte range which returns file data only from the specified range.
     * @param rangeGetContentMD5 Optional boolean which the service returns the MD5 hash for the range when it sets to
     * true, as long as the range is less than or equal to 4 MB in size.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the headers and response status code
     * @throws NullPointerException If {@code stream} is {@code null}.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    public ShareFileDownloadResponse downloadWithResponse(OutputStream stream, ShareFileRange range,
        Boolean rangeGetContentMD5, Duration timeout, Context context) {
        return this.downloadWithResponse(stream, range, rangeGetContentMD5, null, timeout, context);
    }

    /**
     * Downloads a file from the system, including its metadata and properties
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Download the file from 1024 to 2048 bytes with its metadata and properties and without the contentMD5. </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.downloadWithResponse#OutputStream-ShareFileRange-Boolean-ShareRequestConditions-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-file">Azure Docs</a>.</p>
     *
     * @param stream A non-null {@link OutputStream} where the downloaded data will be written.
     * @param range Optional byte range which returns file data only from the specified range.
     * @param rangeGetContentMD5 Optional boolean which the service returns the MD5 hash for the range when it sets to
     * true, as long as the range is less than or equal to 4 MB in size.
     * @param requestConditions {@link ShareRequestConditions}
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the headers and response status code
     * @throws NullPointerException If {@code stream} is {@code null}.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    public ShareFileDownloadResponse downloadWithResponse(OutputStream stream, ShareFileRange range,
        Boolean rangeGetContentMD5, ShareRequestConditions requestConditions, Duration timeout, Context context) {
        return downloadWithResponse(stream, new ShareFileDownloadOptions().setRange(range)
            .setRangeContentMd5Requested(rangeGetContentMD5).setRequestConditions(requestConditions), timeout, context);
    }

    /**
     * Downloads a file from the system, including its metadata and properties
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Download the file from 1024 to 2048 bytes with its metadata and properties and without the contentMD5. </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.downloadWithResponse#OutputStream-ShareFileDownloadOptions-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-file">Azure Docs</a>.</p>
     *
     * @param stream A non-null {@link OutputStream} where the downloaded data will be written.
     * @param options {@link ShareFileDownloadOptions}
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the headers and response status code
     * @throws NullPointerException If {@code stream} is {@code null}.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    public ShareFileDownloadResponse downloadWithResponse(OutputStream stream, ShareFileDownloadOptions options,
        Duration timeout, Context context) {
        Objects.requireNonNull(stream, "'stream' cannot be null.");

        Mono<ShareFileDownloadResponse> download = shareFileAsyncClient.downloadWithResponse(options, context)
            .flatMap(response -> response.getValue().reduce(stream, (outputStream, buffer) -> {
                try {
                    outputStream.write(FluxUtil.byteBufferToArray(buffer));
                    return outputStream;
                } catch (IOException ex) {
                    throw logger.logExceptionAsError(Exceptions.propagate(new UncheckedIOException(ex)));
                }
            }).thenReturn(new ShareFileDownloadResponse(response)));

        return StorageImplUtils.blockWithOptionalTimeout(download, timeout);
    }

    /**
     * Deletes the file associate with the client.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the file</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.delete}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-file2">Azure Docs</a>.</p>
     *
     * @throws ShareStorageException If the directory doesn't exist or the file doesn't exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void delete() {
        deleteWithResponse(null, Context.NONE);
    }


    /**
     * Deletes the file associate with the client.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the file</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.deleteWithResponse#duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-file2">Azure Docs</a>.</p>
     *
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response that only contains headers and response status code
     * @throws ShareStorageException If the directory doesn't exist or the file doesn't exist.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteWithResponse(Duration timeout, Context context) {
        return this.deleteWithResponse(null, timeout, context);
    }

    /**
     * Deletes the file associate with the client.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the file</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.deleteWithResponse#ShareRequestConditions-duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-file2">Azure Docs</a>.</p>
     *
     * @param requestConditions {@link ShareRequestConditions}
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response that only contains headers and response status code
     * @throws ShareStorageException If the directory doesn't exist or the file doesn't exist.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteWithResponse(ShareRequestConditions requestConditions, Duration timeout,
        Context context) {
        Mono<Response<Void>> response = shareFileAsyncClient.deleteWithResponse(requestConditions, context);
        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Retrieves the properties of the storage account's file. The properties includes file metadata, last modified
     * date, is server encrypted, and eTag.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve file properties</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.getProperties}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-file-properties">Azure Docs</a>.</p>
     *
     * @return {@link ShareFileProperties Storage file properties}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ShareFileProperties getProperties() {
        return getPropertiesWithResponse(null, Context.NONE).getValue();
    }

    /**
     * Retrieves the properties of the storage account's file. The properties includes file metadata, last modified
     * date, is server encrypted, and eTag.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve file properties</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.getPropertiesWithResponse#duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-file-properties">Azure Docs</a>.</p>
     *
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the {@link ShareFileProperties Storage file properties} with headers and
     * status code.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareFileProperties> getPropertiesWithResponse(Duration timeout, Context context) {
        return this.getPropertiesWithResponse(null, timeout, context);
    }

    /**
     * Retrieves the properties of the storage account's file. The properties includes file metadata, last modified
     * date, is server encrypted, and eTag.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve file properties</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.getPropertiesWithResponse#ShareRequestConditions-duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-file-properties">Azure Docs</a>.</p>
     *
     * @param requestConditions {@link ShareRequestConditions}
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the {@link ShareFileProperties Storage file properties} with headers and
     * status code.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareFileProperties> getPropertiesWithResponse(ShareRequestConditions requestConditions,
        Duration timeout, Context context) {
        Mono<Response<ShareFileProperties>> response = shareFileAsyncClient.getPropertiesWithResponse(requestConditions,
            context);
        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
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
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.setProperties#long-ShareFileHttpHeaders-FileSmbProperties-String}
     *
     * <p>Clear the httpHeaders of the file and preserve the SMB properties</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.setProperties#long-ShareFileHttpHeaders-FileSmbProperties-String.clearHttpHeaderspreserveSMBProperties}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-file-properties">Azure Docs</a>.</p>
     *
     * @param newFileSize New file size of the file
     * @param httpHeaders The user settable file http headers.
     * @param smbProperties The user settable file smb properties.
     * @param filePermission The file permission of the file
     * @return The {@link ShareFileInfo file info}
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ShareFileInfo setProperties(long newFileSize, ShareFileHttpHeaders httpHeaders,
        FileSmbProperties smbProperties, String filePermission) {
        return setPropertiesWithResponse(newFileSize, httpHeaders, smbProperties, filePermission, null, Context.NONE)
            .getValue();
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
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.setPropertiesWithResponse#long-ShareFileHttpHeaders-FileSmbProperties-String-Duration-Context}
     *
     * <p>Clear the httpHeaders of the file and preserve the SMB properties</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.setPropertiesWithResponse#long-ShareFileHttpHeaders-FileSmbProperties-String-Duration-Context.clearHttpHeaderspreserveSMBProperties}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-file-properties">Azure Docs</a>.</p>
     *
     * @param newFileSize New file size of the file
     * @param httpHeaders The user settable file http headers.
     * @param smbProperties The user settable file smb properties.
     * @param filePermission The file permission of the file
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return Response containing the {@link ShareFileInfo file info} with headers and status code
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareFileInfo> setPropertiesWithResponse(long newFileSize, ShareFileHttpHeaders httpHeaders,
        FileSmbProperties smbProperties, String filePermission, Duration timeout, Context context) {
        return this.setPropertiesWithResponse(newFileSize, httpHeaders, smbProperties, filePermission, null,
            timeout, context);
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
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.setPropertiesWithResponse#long-ShareFileHttpHeaders-FileSmbProperties-String-ShareRequestConditions-Duration-Context}
     *
     * <p>Clear the httpHeaders of the file and preserve the SMB properties</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.setPropertiesWithResponse#long-ShareFileHttpHeaders-FileSmbProperties-String-ShareRequestConditions-Duration-Context.clearHttpHeaderspreserveSMBProperties}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-file-properties">Azure Docs</a>.</p>
     *
     * @param newFileSize New file size of the file
     * @param httpHeaders The user settable file http headers.
     * @param smbProperties The user settable file smb properties.
     * @param filePermission The file permission of the file
     * @param requestConditions {@link ShareRequestConditions}
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return Response containing the {@link ShareFileInfo file info} with headers and status code
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareFileInfo> setPropertiesWithResponse(long newFileSize, ShareFileHttpHeaders httpHeaders,
        FileSmbProperties smbProperties, String filePermission, ShareRequestConditions requestConditions,
        Duration timeout, Context context) {
        Mono<Response<ShareFileInfo>> response = shareFileAsyncClient
            .setPropertiesWithResponse(newFileSize, httpHeaders, smbProperties, filePermission, requestConditions,
                context);
        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
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
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.setMetadata#map}
     *
     * <p>Clear the metadata of the file</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.setMetadata#map.clearMetadata}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-file-metadata">Azure Docs</a>.</p>
     *
     * @param metadata Options.Metadata to set on the file, if null is passed the metadata for the file is cleared
     * @return The {@link ShareFileMetadataInfo file meta info}
     * @throws ShareStorageException If the file doesn't exist or the metadata contains invalid keys
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ShareFileMetadataInfo setMetadata(Map<String, String> metadata) {
        return setMetadataWithResponse(metadata, null, Context.NONE).getValue();
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
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.setMetadataWithResponse#map-duration-context}
     *
     * <p>Clear the metadata of the file</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.setMetadataWithResponse#map-duration-context.clearMetadata}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-file-metadata">Azure Docs</a>.</p>
     *
     * @param metadata Options.Metadata to set on the file, if null is passed the metadata for the file is cleared
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return Response containing the {@link ShareFileMetadataInfo file meta info} with headers and status code
     * @throws ShareStorageException If the file doesn't exist or the metadata contains invalid keys
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareFileMetadataInfo> setMetadataWithResponse(Map<String, String> metadata, Duration timeout,
        Context context) {
        return this.setMetadataWithResponse(metadata, null, timeout, context);
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
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.setMetadataWithResponse#map-ShareRequestConditions-duration-context}
     *
     * <p>Clear the metadata of the file</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.setMetadataWithResponse#map-ShareRequestConditions-duration-context.clearMetadata}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-file-metadata">Azure Docs</a>.</p>
     *
     * @param metadata Options.Metadata to set on the file, if null is passed the metadata for the file is cleared
     * @param requestConditions {@link ShareRequestConditions}
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return Response containing the {@link ShareFileMetadataInfo file meta info} with headers and status code
     * @throws ShareStorageException If the file doesn't exist or the metadata contains invalid keys
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareFileMetadataInfo> setMetadataWithResponse(Map<String, String> metadata,
        ShareRequestConditions requestConditions, Duration timeout, Context context) {
        Mono<Response<ShareFileMetadataInfo>> response = shareFileAsyncClient
            .setMetadataWithResponse(metadata, requestConditions, context);
        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Uploads a range of bytes to the beginning of a file in storage file service. Upload operations performs an
     * in-place write on the specified file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Upload data "default" to the file in Storage File Service. </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.upload#InputStream-long}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-range">Azure Docs</a>.</p>
     *
     * @param data The data which will upload to the storage file.
     * @param length Specifies the number of bytes being transmitted in the request body. When the
     * ShareFileRangeWriteType is set to clear, the value of this header must be set to zero.
     * @return The {@link ShareFileUploadInfo file upload info}
     * @throws ShareStorageException If you attempt to upload a range that is larger than 4 MB, the service returns
     * status code 413 (Request Entity Too Large)
     *
     * @deprecated Use {@link ShareFileClient#uploadRange(InputStream, long)} instead. Or consider
     * {@link ShareFileClient#upload(InputStream, long, ParallelTransferOptions)} for an upload that can handle
     * large amounts of data.
     */
    @Deprecated
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ShareFileUploadInfo upload(InputStream data, long length) {
        return uploadWithResponse(data, length, 0L, null, Context.NONE).getValue();
    }

    /**
     * Uploads a range of bytes to specific of a file in storage file service. Upload operations performs an in-place
     * write on the specified file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Upload data "default" starting from 1024. </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.uploadWithResponse#InputStream-long-Long-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-range">Azure Docs</a>.</p>
     *
     * @param data The data which will upload to the storage file.
     * @param length Specifies the number of bytes being transmitted in the request body.
     * @param offset Starting point of the upload range, if {@code null} it will start from the beginning.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the {@link ShareFileUploadInfo file upload info} with headers and response
     * status code.
     * @throws ShareStorageException If you attempt to upload a range that is larger than 4 MB, the service returns
     * status code 413 (Request Entity Too Large)
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     *
     * @deprecated Use {@link ShareFileClient#uploadRangeWithResponse(ShareFileUploadRangeOptions, Duration, Context)}
     * instead. Or consider {@link ShareFileClient#uploadWithResponse(ShareFileUploadOptions, Duration, Context)} for
     * an upload that can handle large amounts of  data.
     */
    @Deprecated
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareFileUploadInfo> uploadWithResponse(InputStream data, long length, Long offset,
        Duration timeout, Context context) {
        return this.uploadWithResponse(data, length, offset, null, timeout, context);
    }

    /**
     * Uploads a range of bytes to specific of a file in storage file service. Upload operations performs an in-place
     * write on the specified file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Upload data "default" starting from 1024. </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.uploadWithResponse#InputStream-long-Long-ShareRequestConditions-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-range">Azure Docs</a>.</p>
     *
     * @param data The data which will upload to the storage file.
     * @param length Specifies the number of bytes being transmitted in the request body.
     * @param offset Starting point of the upload range, if {@code null} it will start from the beginning.
     * @param requestConditions {@link ShareRequestConditions}
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the {@link ShareFileUploadInfo file upload info} with headers and response
     * status code.
     * @throws ShareStorageException If you attempt to upload a range that is larger than 4 MB, the service returns
     * status code 413 (Request Entity Too Large)
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     *
     * @deprecated Use {@link ShareFileClient#uploadRangeWithResponse(ShareFileUploadRangeOptions, Duration, Context)}
     * instead. Or consider {@link ShareFileClient#uploadWithResponse(ShareFileUploadOptions, Duration, Context)} for
     * an upload that can handle large amounts of data.
     */
    @Deprecated
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareFileUploadInfo> uploadWithResponse(InputStream data, long length, Long offset,
        ShareRequestConditions requestConditions, Duration timeout, Context context) {
        return this.uploadRangeWithResponse(
            new ShareFileUploadRangeOptions(data, length).setOffset(offset).setRequestConditions(requestConditions),
            timeout, context);
    }

    /**
     * Buffers a range of bytes and uploads sub-ranges in parallel to a file in storage file service. Upload operations
     * perform an in-place write on the specified file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Upload data "default" to the file in Storage File Service. </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.upload#InputStream-long-ParallelTransferOptions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-range">Azure Docs</a>.</p>
     *
     * @param data The data which will upload to the storage file.
     * @param length Specifies the number of bytes being transmitted in the request body.
     * @param transferOptions {@link ParallelTransferOptions} for file transfer.
     * @return The {@link ShareFileUploadInfo file upload info}
     */
    public ShareFileUploadInfo upload(InputStream data, long length, ParallelTransferOptions transferOptions) {
        return uploadWithResponse(new ShareFileUploadOptions(data, length).setParallelTransferOptions(transferOptions),
            null, Context.NONE).getValue();
    }

    /**
     * Buffers a range of bytes and uploads sub-ranges in parallel to a file in storage file service. Upload operations
     * perform an in-place write on the specified file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Upload data "default" to the file in Storage File Service. </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.uploadWithResponse#ShareFileUploadOptions-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-range">Azure Docs</a>.</p>
     *
     * @param options Argument collection for the upload operation.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The {@link ShareFileUploadInfo file upload info}
     */
    public Response<ShareFileUploadInfo> uploadWithResponse(ShareFileUploadOptions options,
        Duration timeout, Context context) {
        return StorageImplUtils.blockWithOptionalTimeout(
            shareFileAsyncClient.uploadWithResponse(options, context), timeout);
    }

    /**
     * Uploads a range of bytes to the specified offset of a file in storage file service. Upload operations perform an
     * in-place write on the specified file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Upload data "default" to the file in Storage File Service. </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.uploadRange#InputStream-long}
     *
     * <p>This method does a single Put Range operation. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-range">Azure Docs</a>.</p>
     *
     * @param data The data which will upload to the storage file.
     * @param length Specifies the number of bytes being transmitted in the request body.
     * @return The {@link ShareFileUploadInfo file upload info}
     * @throws ShareStorageException If you attempt to upload a range that is larger than 4 MB, the service returns
     * status code 413 (Request Entity Too Large)
     */
    public ShareFileUploadInfo uploadRange(InputStream data, long length) {
        return this.uploadRangeWithResponse(new ShareFileUploadRangeOptions(data, length), null, Context.NONE).getValue();
    }

    /**
     * Uploads a range of bytes to the specified offset of a file in storage file service. Upload operations perform an
     * in-place write on the specified file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Upload data "default" to the file in Storage File Service. </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.uploadRangeWithResponse#ShareFileUploadRangeOptions-Duration-Context}
     *
     * <p>This method does a single Put Range operation. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-range">Azure Docs</a>.</p>
     *
     * @param options Argument collection for the upload operation.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The {@link ShareFileUploadInfo file upload info}
     * @throws ShareStorageException If you attempt to upload a range that is larger than 4 MB, the service returns
     * status code 413 (Request Entity Too Large)
     */
    public Response<ShareFileUploadInfo> uploadRangeWithResponse(ShareFileUploadRangeOptions options,
        Duration timeout, Context context) {
        return StorageImplUtils.blockWithOptionalTimeout(
            shareFileAsyncClient.uploadRangeWithResponse(options, context), timeout);
    }

    /**
     * Uploads a range of bytes from one file to another file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Upload a number of bytes from a file at defined source and destination offsets </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.uploadRangeFromUrl#long-long-long-String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-range">Azure Docs</a>.</p>
     *
     * @param length Specifies the number of bytes being transmitted in the request body.
     * @param destinationOffset Starting point of the upload range on the destination.
     * @param sourceOffset Starting point of the upload range on the source.
     * @param sourceUrl Specifies the URL of the source file.
     * @return The {@link ShareFileUploadRangeFromUrlInfo file upload range from url info}
     */
    // TODO: (gapra) Fix put range from URL link. Service docs have not been updated to show this API
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ShareFileUploadRangeFromUrlInfo uploadRangeFromUrl(long length, long destinationOffset, long sourceOffset,
                                                              String sourceUrl) {
        return uploadRangeFromUrlWithResponse(length, destinationOffset, sourceOffset, sourceUrl, null, Context.NONE)
            .getValue();
    }

    /**
     * Uploads a range of bytes from one file to another file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Upload a number of bytes from a file at defined source and destination offsets </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.uploadRangeFromUrlWithResponse#long-long-long-String-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-range">Azure Docs</a>.</p>
     *
     * @param length Specifies the number of bytes being transmitted in the request body.
     * @param destinationOffset Starting point of the upload range on the destination.
     * @param sourceOffset Starting point of the upload range on the source.
     * @param sourceUrl Specifies the URL of the source file.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the {@link ShareFileUploadRangeFromUrlInfo file upload range from url info} with
     * headers and response status code.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    // TODO: (gapra) Fix put range from URL link. Service docs have not been updated to show this API
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareFileUploadRangeFromUrlInfo> uploadRangeFromUrlWithResponse(long length, long destinationOffset,
        long sourceOffset, String sourceUrl, Duration timeout, Context context) {
        return this.uploadRangeFromUrlWithResponse(length, destinationOffset, sourceOffset, sourceUrl, null, timeout,
            context);
    }

    /**
     * Uploads a range of bytes from one file to another file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Upload a number of bytes from a file at defined source and destination offsets </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.uploadRangeFromUrlWithResponse#long-long-long-String-ShareRequestConditions-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-range">Azure Docs</a>.</p>
     *
     * @param length Specifies the number of bytes being transmitted in the request body.
     * @param destinationOffset Starting point of the upload range on the destination.
     * @param sourceOffset Starting point of the upload range on the source.
     * @param sourceUrl Specifies the URL of the source file.
     * @param requestConditions {@link ShareRequestConditions}
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the {@link ShareFileUploadRangeFromUrlInfo file upload range from url info} with
     * headers and response status code.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    // TODO: (gapra) Fix put range from URL link. Service docs have not been updated to show this API
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareFileUploadRangeFromUrlInfo> uploadRangeFromUrlWithResponse(long length, long destinationOffset,
        long sourceOffset, String sourceUrl, ShareRequestConditions requestConditions, Duration timeout,
        Context context) {
        return this.uploadRangeFromUrlWithResponse(new ShareFileUploadRangeFromUrlOptions(length, sourceUrl)
            .setDestinationOffset(destinationOffset).setSourceOffset(sourceOffset)
            .setDestinationRequestConditions(requestConditions), timeout, context);
    }

    /**
     * Uploads a range of bytes from one file to another file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Upload a number of bytes from a file at defined source and destination offsets </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.uploadRangeFromUrlWithResponse#ShareFileUploadRangeFromUrlOptions-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-range">Azure Docs</a>.</p>
     *
     * @param options argument collection
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the {@link ShareFileUploadRangeFromUrlInfo file upload range from url info} with
     * headers and response status code.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    // TODO: (gapra) Fix put range from URL link. Service docs have not been updated to show this API
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareFileUploadRangeFromUrlInfo> uploadRangeFromUrlWithResponse(
        ShareFileUploadRangeFromUrlOptions options, Duration timeout, Context context) {
        Mono<Response<ShareFileUploadRangeFromUrlInfo>> response = shareFileAsyncClient.uploadRangeFromUrlWithResponse(
            options, context);
        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Clears a range of bytes to specific of a file in storage file service. Clear operations performs an in-place
     * write on the specified file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Clears the first 1024 bytes. </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.clearRange#long}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-range">Azure Docs</a>.</p>
     *
     * @param length Specifies the number of bytes being cleared.
     * @return The {@link ShareFileUploadInfo file upload info}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ShareFileUploadInfo clearRange(long length) {
        return clearRangeWithResponse(length, 0, null, Context.NONE).getValue();
    }

    /**
     * Clears a range of bytes to specific of a file in storage file service. Upload operations performs an in-place
     * write on the specified file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Clear the range starting from 1024 with length of 1024. </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.clearRangeWithResponse#long-long-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-range">Azure Docs</a>.</p>
     *
     * @param length Specifies the number of bytes being transmitted in the request body.
     * @param offset Starting point of the upload range, if {@code null} it will start from the beginning.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the {@link ShareFileUploadInfo file upload info} with headers and response
     * status code.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareFileUploadInfo> clearRangeWithResponse(long length, long offset, Duration timeout,
        Context context) {
        return this.clearRangeWithResponse(length, offset, null, timeout, context);
    }

    /**
     * Clears a range of bytes to specific of a file in storage file service. Upload operations performs an in-place
     * write on the specified file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Clear the range starting from 1024 with length of 1024. </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.clearRangeWithResponse#long-long-ShareRequestConditions-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-range">Azure Docs</a>.</p>
     *
     * @param length Specifies the number of bytes being transmitted in the request body.
     * @param offset Starting point of the upload range, if {@code null} it will start from the beginning.
     * @param requestConditions {@link ShareRequestConditions}
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the {@link ShareFileUploadInfo file upload info} with headers and response
     * status code.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareFileUploadInfo> clearRangeWithResponse(long length, long offset,
        ShareRequestConditions requestConditions, Duration timeout, Context context) {
        Mono<Response<ShareFileUploadInfo>> response = shareFileAsyncClient
            .clearRangeWithResponse(length, offset, requestConditions, context);
        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Uploads file to storage file service.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Upload the file from the source file path. </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.uploadFromFile#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-file">Azure Docs Create File</a>
     * and
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-range">Azure Docs Upload</a>.</p>
     *
     * @param uploadFilePath The path where store the source file to upload
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void uploadFromFile(String uploadFilePath) {
        this.uploadFromFile(uploadFilePath, null);
    }

    /**
     * Uploads file to storage file service.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Upload the file from the source file path. </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.uploadFromFile#string-ShareRequestConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-file">Azure Docs Create File</a>
     * and
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-range">Azure Docs Upload</a>.</p>
     *
     * @param uploadFilePath The path where store the source file to upload
     * @param requestConditions {@link ShareRequestConditions}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void uploadFromFile(String uploadFilePath, ShareRequestConditions requestConditions) {
        shareFileAsyncClient.uploadFromFile(uploadFilePath, requestConditions).block();
    }

    /**
     * List of valid ranges for a file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all ranges for the file client.</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.listRanges}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-ranges">Azure Docs</a>.</p>
     *
     * @return {@link ShareFileRange ranges} in the files.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ShareFileRange> listRanges() {
        return listRanges((ShareFileRange) null, null, null);
    }

    /**
     * List of valid ranges for a file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all ranges within the file range from 1KB to 2KB.</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.listRanges#ShareFileRange-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-ranges">Azure Docs</a>.</p>
     *
     * @param range Optional byte range which returns file data only from the specified range.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return {@link ShareFileRange ranges} in the files that satisfy the requirements
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ShareFileRange> listRanges(ShareFileRange range, Duration timeout, Context context) {
        return this.listRanges(range, null, timeout, context);
    }

    /**
     * List of valid ranges for a file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all ranges within the file range from 1KB to 2KB.</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.listRanges#ShareFileRange-ShareRequestConditions-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-ranges">Azure Docs</a>.</p>
     *
     * @param range Optional byte range which returns file data only from the specified range.
     * @param requestConditions {@link ShareRequestConditions}
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return {@link ShareFileRange ranges} in the files that satisfy the requirements
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ShareFileRange> listRanges(ShareFileRange range, ShareRequestConditions requestConditions,
        Duration timeout, Context context) {
        return new PagedIterable<>(shareFileAsyncClient.listRangesWithOptionalTimeout(range, requestConditions,
            timeout, context));
    }

    /**
     * List of valid ranges for a file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all ranges within the file range from 1KB to 2KB.</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.listRangesDiff#String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-ranges">Azure Docs</a>.</p>
     *
     * @param previousSnapshot Specifies that the response will contain only ranges that were changed between target
     * file and previous snapshot. Changed ranges include both updated and cleared ranges. The target file may be a
     * snapshot, as long as the snapshot specified by previousSnapshot is the older of the two.
     * @return {@link ShareFileRange ranges} in the files that satisfy the requirements
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ShareFileRangeList listRangesDiff(String previousSnapshot) {
        return this.listRangesDiffWithResponse(new ShareFileListRangesDiffOptions(previousSnapshot), null, Context.NONE)
            .getValue();
    }

    /**
     * List of valid ranges for a file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all ranges within the file range from 1KB to 2KB.</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.listRangesDiffWithResponse#ShareFileListRangesDiffOptions-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-ranges">Azure Docs</a>.</p>
     *
     * @param options {@link ShareFileListRangesDiffOptions}
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return {@link ShareFileRange ranges} in the files that satisfy the requirements
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareFileRangeList> listRangesDiffWithResponse(ShareFileListRangesDiffOptions options,
        Duration timeout, Context context) {
        StorageImplUtils.assertNotNull("options", options);
        Mono<Response<ShareFileRangeList>> response = shareFileAsyncClient.listRangesWithResponse(options.getRange(),
            options.getRequestConditions(), options.getPreviousSnapshot(), context);

        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * List of open handles on a file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all handles for the file client.</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.listHandles}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-handles">Azure Docs</a>.</p>
     *
     * @return {@link HandleItem handles} in the files that satisfy the requirements
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<HandleItem> listHandles() {
        return listHandles(null, null, Context.NONE);
    }

    /**
     * List of open handles on a file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List 10 handles for the file client.</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.listHandles#integer-duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-handles">Azure Docs</a>.</p>
     *
     * @param maxResultsPerPage Optional max number of results returned per page
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return {@link HandleItem handles} in the file that satisfy the requirements
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<HandleItem> listHandles(Integer maxResultsPerPage, Duration timeout, Context context) {
        return new PagedIterable<>(shareFileAsyncClient.listHandlesWithOptionalTimeout(maxResultsPerPage, timeout,
            context));
    }

    /**
     * Closes a handle on the file at the service. This is intended to be used alongside {@link #listHandles()}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Force close handles returned by list handles.</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.forceCloseHandle#String}
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
     * Closes a handle on the file at the service. This is intended to be used alongside {@link #listHandles()}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Force close handles returned by list handles.</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.forceCloseHandleWithResponse#String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/force-close-handles">Azure Docs</a>.</p>
     *
     * @param handleId Handle ID to be closed.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response that contains information about the closed handles, headers and response status code.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CloseHandlesInfo> forceCloseHandleWithResponse(String handleId, Duration timeout, Context context) {
        Mono<Response<CloseHandlesInfo>> response = shareFileAsyncClient
            .forceCloseHandleWithResponse(handleId, context);
        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Closes all handles opened on the file at the service.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Force close all handles.</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.forceCloseAllHandles#Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/force-close-handles">Azure Docs</a>.</p>
     *
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return Information about the closed handles
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CloseHandlesInfo forceCloseAllHandles(Duration timeout, Context context) {
        return new PagedIterable<>(shareFileAsyncClient.forceCloseAllHandlesWithOptionalTimeout(timeout, context))
            .stream().reduce(new CloseHandlesInfo(0, 0),
                (accu, next) -> new CloseHandlesInfo(accu.getClosedHandles() + next.getClosedHandles(),
                    accu.getFailedHandles() + next.getFailedHandles()));
    }

    /**
     * Get snapshot id which attached to {@link ShareFileClient}. Return {@code null} if no snapshot id attached.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Get the share snapshot id. </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.getShareSnapshotId}
     *
     * @return The snapshot id which is a unique {@code DateTime} value that identifies the share snapshot to its base
     * share.
     */
    public String getShareSnapshotId() {
        return shareFileAsyncClient.getShareSnapshotId();
    }

    /**
     * Get the share name of file client.
     *
     * <p>Get the share name. </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.getShareName}
     *
     * @return The share name of the file.
     */
    public String getShareName() {
        return this.shareFileAsyncClient.getShareName();
    }

    /**
     * Get file path of the client.
     *
     * <p>Get the file path. </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.getFilePath}
     *
     * @return The path of the file.
     */
    public String getFilePath() {
        return this.shareFileAsyncClient.getFilePath();
    }


    /**
     * Get associated account name.
     *
     * @return account name associated with this storage resource.
     */
    public String getAccountName() {
        return this.shareFileAsyncClient.getAccountName();
    }

    /**
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return The pipeline.
     */
    public HttpPipeline getHttpPipeline() {
        return this.shareFileAsyncClient.getHttpPipeline();
    }

    /**
     * Generates a service SAS for the file using the specified {@link ShareServiceSasSignatureValues}
     * <p>Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link ShareServiceSasSignatureValues} for more information on how to construct a service SAS.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.generateSas#ShareServiceSasSignatureValues}
     *
     * @param shareServiceSasSignatureValues {@link ShareServiceSasSignatureValues}
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateSas(ShareServiceSasSignatureValues shareServiceSasSignatureValues) {
        return this.shareFileAsyncClient.generateSas(shareServiceSasSignatureValues);
    }

    /**
     * Generates a service SAS for the file using the specified {@link ShareServiceSasSignatureValues}
     * <p>Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link ShareServiceSasSignatureValues} for more information on how to construct a service SAS.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileClient.generateSas#ShareServiceSasSignatureValues-Context}
     *
     * @param shareServiceSasSignatureValues {@link ShareServiceSasSignatureValues}
     * @param context Additional context that is passed through the code when generating a SAS.
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateSas(ShareServiceSasSignatureValues shareServiceSasSignatureValues, Context context) {
        return this.shareFileAsyncClient.generateSas(shareServiceSasSignatureValues, context);
    }
}

