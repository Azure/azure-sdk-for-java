// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.http.rest.StreamResponse;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.common.ProgressReporter;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.Utility;
import com.azure.storage.common.implementation.BufferAggregator;
import com.azure.storage.common.implementation.BufferStagingArea;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.SasImplUtils;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.common.implementation.UploadUtils;
import com.azure.storage.file.share.implementation.AzureFileStorageImpl;
import com.azure.storage.file.share.implementation.models.CopyFileSmbInfo;
import com.azure.storage.file.share.implementation.models.FilesCreateResponse;
import com.azure.storage.file.share.implementation.models.FilesGetPropertiesHeaders;
import com.azure.storage.file.share.implementation.models.FilesGetPropertiesResponse;
import com.azure.storage.file.share.implementation.models.FilesSetHttpHeadersResponse;
import com.azure.storage.file.share.implementation.models.FilesSetMetadataResponse;
import com.azure.storage.file.share.implementation.models.FilesStartCopyHeaders;
import com.azure.storage.file.share.implementation.models.FilesUploadRangeFromURLHeaders;
import com.azure.storage.file.share.implementation.models.FilesUploadRangeFromURLResponse;
import com.azure.storage.file.share.implementation.models.FilesUploadRangeHeaders;
import com.azure.storage.file.share.implementation.models.FilesUploadRangeResponse;
import com.azure.storage.file.share.implementation.models.ShareFileRangeWriteType;
import com.azure.storage.file.share.implementation.util.ModelHelper;
import com.azure.storage.file.share.implementation.util.ShareSasImplUtil;
import com.azure.storage.file.share.models.CloseHandlesInfo;
import com.azure.storage.file.share.models.CopyStatusType;
import com.azure.storage.file.share.models.DownloadRetryOptions;
import com.azure.storage.file.share.models.HandleItem;
import com.azure.storage.file.share.models.LeaseDurationType;
import com.azure.storage.file.share.models.LeaseStateType;
import com.azure.storage.file.share.models.LeaseStatusType;
import com.azure.storage.file.share.models.NtfsFileAttributes;
import com.azure.storage.file.share.models.PermissionCopyModeType;
import com.azure.storage.file.share.models.Range;
import com.azure.storage.file.share.models.ShareErrorCode;
import com.azure.storage.file.share.models.ShareFileCopyInfo;
import com.azure.storage.file.share.models.ShareFileDownloadAsyncResponse;
import com.azure.storage.file.share.models.ShareFileDownloadHeaders;
import com.azure.storage.file.share.models.ShareFileHttpHeaders;
import com.azure.storage.file.share.models.ShareFileInfo;
import com.azure.storage.file.share.models.ShareFileMetadataInfo;
import com.azure.storage.file.share.models.ShareFileProperties;
import com.azure.storage.file.share.models.ShareFileRange;
import com.azure.storage.file.share.models.ShareFileRangeList;
import com.azure.storage.file.share.models.ShareFileUploadOptions;
import com.azure.storage.file.share.models.ShareFileUploadInfo;
import com.azure.storage.file.share.models.ShareFileUploadRangeOptions;
import com.azure.storage.file.share.models.ShareFileUploadRangeFromUrlInfo;
import com.azure.storage.file.share.models.ShareRequestConditions;
import com.azure.storage.file.share.models.ShareStorageException;
import com.azure.storage.file.share.options.ShareFileDownloadOptions;
import com.azure.storage.file.share.options.ShareFileListRangesDiffOptions;
import com.azure.storage.file.share.options.ShareFileUploadRangeFromUrlOptions;
import com.azure.storage.file.share.sas.ShareServiceSasSignatureValues;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuples;
import reactor.util.retry.Retry;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.fluxError;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.pagedFluxError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;
import static com.azure.storage.common.Utility.STORAGE_TRACING_NAMESPACE_VALUE;


/**
 * This class provides a client that contains all the operations for interacting with file in Azure Storage File
 * Service. Operations allowed by the client are creating, copying, uploading, downloading, deleting and listing on a
 * file, retrieving properties, setting metadata and list or force close handles of the file.
 *
 * <p><strong>Instantiating an Asynchronous File Client</strong></p>
 *
 * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.instantiation}
 *
 * <p>View {@link ShareFileClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see ShareFileClientBuilder
 * @see ShareFileClient
 * @see StorageSharedKeyCredential
 */
@ServiceClient(builder = ShareFileClientBuilder.class, isAsync = true)
public class ShareFileAsyncClient {
    private final ClientLogger logger = new ClientLogger(ShareFileAsyncClient.class);
    static final long FILE_DEFAULT_BLOCK_SIZE = 4 * 1024 * 1024L;
    static final long FILE_MAX_PUT_RANGE_SIZE = 4 * Constants.MB;
    private static final long DOWNLOAD_UPLOAD_CHUNK_TIMEOUT = 300;
    private static final Duration TIMEOUT_VALUE = Duration.ofSeconds(60);

    private final AzureFileStorageImpl azureFileStorageClient;
    private final String shareName;
    private final String filePath;
    private final String snapshot;
    private final String accountName;
    private final ShareServiceVersion serviceVersion;

    /**
     * Creates a ShareFileAsyncClient that sends requests to the storage file at {@link AzureFileStorageImpl#getUrl()
     * endpoint}. Each service call goes through the {@link HttpPipeline pipeline} in the {@code client}.
     *
     * @param azureFileStorageClient Client that interacts with the service interfaces
     * @param shareName Name of the share
     * @param filePath Path to the file
     * @param snapshot The snapshot of the share
     */
    ShareFileAsyncClient(AzureFileStorageImpl azureFileStorageClient, String shareName, String filePath,
                         String snapshot, String accountName, ShareServiceVersion serviceVersion) {
        Objects.requireNonNull(shareName, "'shareName' cannot be null.");
        Objects.requireNonNull(filePath, "'filePath' cannot be null.");
        this.shareName = shareName;
        this.filePath = filePath;
        this.snapshot = snapshot;
        this.azureFileStorageClient = azureFileStorageClient;
        this.accountName = accountName;
        this.serviceVersion = serviceVersion;
    }

    /**
     * Get the url of the storage account.
     *
     * @return the URL of the storage account
     */
    public String getAccountUrl() {
        return azureFileStorageClient.getUrl();
    }

    /**
     * Get the url of the storage file client.
     *
     * @return the URL of the storage file client
     */
    public String getFileUrl() {
        StringBuilder fileUrlstring = new StringBuilder(azureFileStorageClient.getUrl()).append("/")
            .append(shareName).append("/").append(filePath);
        if (snapshot != null) {
            fileUrlstring.append("?sharesnapshot=").append(snapshot);
        }
        return fileUrlstring.toString();
    }

    /**
     * Gets the service version the client is using.
     *
     * @return the service version the client is using.
     */
    public ShareServiceVersion getServiceVersion() {
        return serviceVersion;
    }

    /**
     * Determines if the file this client represents exists in the cloud.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.exists}
     *
     * @return Flag indicating existence of the file.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> exists() {
        return existsWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Determines if the file this client represents exists in the cloud.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.existsWithResponse}
     *
     * @return Flag indicating existence of the file.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Boolean>> existsWithResponse() {
        try {
            return withContext(this::existsWithResponse);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Boolean>> existsWithResponse(Context context) {
        return this.getPropertiesWithResponse(null, context)
            .map(cp -> (Response<Boolean>) new SimpleResponse<>(cp, true))
            .onErrorResume(this::checkDoesNotExistStatusCode,
                t -> {
                    HttpResponse response = t instanceof ShareStorageException
                        ? ((ShareStorageException) t).getResponse()
                        : ((HttpResponseException) t).getResponse();
                    return Mono.just(new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                        response.getHeaders(), false));
                });
    }

    private boolean checkDoesNotExistStatusCode(Throwable t) {
            // ShareStorageException
        return (t instanceof ShareStorageException
            && ((ShareStorageException) t).getStatusCode() == 404
            && (((ShareStorageException) t).getErrorCode() == ShareErrorCode.RESOURCE_NOT_FOUND
            || ((ShareStorageException) t).getErrorCode() == ShareErrorCode.SHARE_NOT_FOUND))

            /* HttpResponseException - file get properties is a head request so a body is not returned. Error
             conversion logic does not properly handle errors that don't return XML. */
            || (t instanceof HttpResponseException
            && ((HttpResponseException) t).getResponse().getStatusCode() == 404
            && (((HttpResponseException) t).getResponse().getHeaderValue("x-ms-error-code")
            .equals(ShareErrorCode.RESOURCE_NOT_FOUND.toString())
            || (((HttpResponseException) t).getResponse().getHeaderValue("x-ms-error-code")
            .equals(ShareErrorCode.SHARE_NOT_FOUND.toString()))));
    }

    /**
     * Creates a file in the storage account and returns a response of {@link ShareFileInfo} to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the file with size 1KB.</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.create}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-file">Azure Docs</a>.</p>
     *
     * @param maxSize The maximum size in bytes for the file.
     * @return A response containing the file info and the status of creating the file.
     * @throws ShareStorageException If the file has already existed, the parent directory does not exist or fileName
     * is an invalid resource name.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ShareFileInfo> create(long maxSize) {
        try {
            return createWithResponse(maxSize, null, null, null, null)
                .flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a file in the storage account and returns a response of ShareFileInfo to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the file with length of 1024 bytes, some headers, file smb properties and metadata.</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.createWithResponse#long-ShareFileHttpHeaders-FileSmbProperties-String-Map}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-file">Azure Docs</a>.</p>
     *
     * @param maxSize The maximum size in bytes for the file.
     * @param httpHeaders The user settable file http headers.
     * @param smbProperties The user settable file smb properties.
     * @param filePermission The file permission of the file.
     * @param metadata Optional name-value pairs associated with the file as metadata.
     * @return A response containing the {@link ShareFileInfo file info} and the status of creating the file.
     * @throws ShareStorageException If the directory has already existed, the parent directory does not exist or
     * directory is an invalid resource name.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareFileInfo>> createWithResponse(long maxSize, ShareFileHttpHeaders httpHeaders,
        FileSmbProperties smbProperties, String filePermission, Map<String, String> metadata) {
        try {
            return createWithResponse(maxSize, httpHeaders, smbProperties, filePermission, metadata, null);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a file in the storage account and returns a response of ShareFileInfo to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the file with length of 1024 bytes, some headers, file smb properties and metadata.</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.createWithResponse#long-ShareFileHttpHeaders-FileSmbProperties-String-Map-ShareRequestConditions}
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
     * @return A response containing the {@link ShareFileInfo file info} and the status of creating the file.
     * @throws ShareStorageException If the directory has already existed, the parent directory does not exist or
     * directory is an invalid resource name.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareFileInfo>> createWithResponse(long maxSize, ShareFileHttpHeaders httpHeaders,
        FileSmbProperties smbProperties, String filePermission, Map<String, String> metadata,
        ShareRequestConditions requestConditions) {
        try {
            return withContext(context ->
                createWithResponse(maxSize, httpHeaders, smbProperties, filePermission, metadata,
                    requestConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<ShareFileInfo>> createWithResponse(long maxSize, ShareFileHttpHeaders httpHeaders,
        FileSmbProperties smbProperties, String filePermission, Map<String, String> metadata,
        ShareRequestConditions requestConditions, Context context) {
        requestConditions = requestConditions == null ? new ShareRequestConditions() : requestConditions;
        smbProperties = smbProperties == null ? new FileSmbProperties() : smbProperties;

        // Checks that file permission and file permission key are valid
        validateFilePermissionAndKey(filePermission, smbProperties.getFilePermissionKey());

        // If file permission and file permission key are both not set then set default value
        filePermission = smbProperties.setFilePermission(filePermission, FileConstants.FILE_PERMISSION_INHERIT);
        String filePermissionKey = smbProperties.getFilePermissionKey();

        String fileAttributes = smbProperties.setNtfsFileAttributes(FileConstants.FILE_ATTRIBUTES_NONE);
        String fileCreationTime = smbProperties.setFileCreationTime(FileConstants.FILE_TIME_NOW);
        String fileLastWriteTime = smbProperties.setFileLastWriteTime(FileConstants.FILE_TIME_NOW);

        return azureFileStorageClient.getFiles()
            .createWithResponseAsync(shareName, filePath, maxSize, fileAttributes, fileCreationTime,
                fileLastWriteTime, null, metadata, filePermission, filePermissionKey, requestConditions.getLeaseId(),
                httpHeaders, context)
            .map(this::createFileInfoResponse);
    }

    /**
     * Copies a blob or file to a destination file within the storage account.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Copy file from source url to the {@code resourcePath} </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.beginCopy#string-map-duration}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/copy-file">Azure Docs</a>.</p>
     *
     * @param sourceUrl Specifies the URL of the source file or blob, up to 2 KB in length.
     * @param metadata Optional name-value pairs associated with the file as metadata. Metadata names must adhere to the
     * naming rules.
     * @param pollInterval Duration between each poll for the copy status. If none is specified, a default of one second
     * is used.
     * @return A {@link PollerFlux} that polls the file copy operation until it has completed or has been cancelled.
     * @see <a href="https://docs.microsoft.com/dotnet/csharp/language-reference/">C# identifiers</a>
     */
    public PollerFlux<ShareFileCopyInfo, Void> beginCopy(String sourceUrl, Map<String, String> metadata,
        Duration pollInterval) {
        return beginCopy(sourceUrl, null, null, null, null, null, metadata, pollInterval, null);
    }

    /**
     * Copies a blob or file to a destination file within the storage account.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Copy file from source url to the {@code resourcePath} </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.beginCopy#string-filesmbproperties-string-permissioncopymodetype-boolean-boolean-map-duration-ShareRequestConditions}
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
     * @return A {@link PollerFlux} that polls the file copy operation until it has completed or has been cancelled.
     * @see <a href="https://docs.microsoft.com/dotnet/csharp/language-reference/">C# identifiers</a>
     */
    public PollerFlux<ShareFileCopyInfo, Void> beginCopy(String sourceUrl, FileSmbProperties smbProperties,
        String filePermission, PermissionCopyModeType filePermissionCopyMode, Boolean ignoreReadOnly,
        Boolean setArchiveAttribute, Map<String, String> metadata, Duration pollInterval,
        ShareRequestConditions destinationRequestConditions) {

        final ShareRequestConditions finalRequestConditions =
            destinationRequestConditions == null ? new ShareRequestConditions() : destinationRequestConditions;
        final AtomicReference<String> copyId = new AtomicReference<>();
        final Duration interval = pollInterval != null ? pollInterval : Duration.ofSeconds(1);

        FileSmbProperties tempSmbProperties = smbProperties == null ? new FileSmbProperties() : smbProperties;

        String filePermissionKey = tempSmbProperties.getFilePermissionKey();

        String fileAttributes = NtfsFileAttributes.toString(tempSmbProperties.getNtfsFileAttributes());
        String fileCreationTime = FileSmbProperties.parseFileSMBDate(tempSmbProperties.getFileCreationTime());
        String fileLastWriteTime = FileSmbProperties.parseFileSMBDate(tempSmbProperties.getFileLastWriteTime());

        if (filePermissionCopyMode == null || filePermissionCopyMode == PermissionCopyModeType.SOURCE) {
            if (filePermission != null || filePermissionKey != null) {
                throw logger.logExceptionAsError(new IllegalArgumentException(
                    "File permission and file permission key can not be set when PermissionCopyModeType is source or "
                        + "null"));
            }
        } else if (filePermissionCopyMode == PermissionCopyModeType.OVERRIDE) {
            // Checks that file permission and file permission key are valid
            validateFilePermissionAndKey(filePermission, tempSmbProperties.getFilePermissionKey());
        }

        final CopyFileSmbInfo copyFileSmbInfo = new CopyFileSmbInfo()
            .setFilePermissionCopyMode(filePermissionCopyMode)
            .setFileAttributes(fileAttributes)
            .setFileCreationTime(fileCreationTime)
            .setFileLastWriteTime(fileLastWriteTime)
            .setIgnoreReadOnly(ignoreReadOnly)
            .setSetArchiveAttribute(setArchiveAttribute);

        final String copySource = Utility.encodeUrlPath(sourceUrl);

        return new PollerFlux<>(interval,
            (pollingContext) -> {
                try {
                    return withContext(context -> azureFileStorageClient.getFiles()
                            .startCopyWithResponseAsync(shareName, filePath, copySource, null,
                                metadata, filePermission, tempSmbProperties.getFilePermissionKey(),
                                finalRequestConditions.getLeaseId(), copyFileSmbInfo, context))
                            .map(response -> {
                                final FilesStartCopyHeaders headers = response.getDeserializedHeaders();
                                copyId.set(headers.getXMsCopyId());

                                return new ShareFileCopyInfo(sourceUrl, headers.getXMsCopyId(), headers.getXMsCopyStatus(),
                                        headers.getETag(), headers.getLastModified(),
                                    response.getHeaders().getValue("x-ms-error-code"));
                            });
                } catch (RuntimeException ex) {
                    return monoError(logger, ex);
                }
            },
            (pollingContext) -> {
                try {
                    return onPoll(pollingContext.getLatestResponse(), finalRequestConditions);
                } catch (RuntimeException ex) {
                    return monoError(logger, ex);
                }
            },
            (pollingContext, firstResponse) -> {
                if (firstResponse == null || firstResponse.getValue() == null) {
                    return Mono.error(logger.logExceptionAsError(
                            new IllegalArgumentException("Cannot cancel a poll response that never started.")));
                }
                final String copyIdentifier = firstResponse.getValue().getCopyId();
                if (!CoreUtils.isNullOrEmpty(copyIdentifier)) {
                    logger.info("Cancelling copy operation for copy id: {}", copyIdentifier);
                    return abortCopyWithResponse(copyIdentifier, finalRequestConditions)
                        .thenReturn(firstResponse.getValue());
                }
                return Mono.empty();
            },
            (pollingContext) -> Mono.empty());
    }

    private Mono<PollResponse<ShareFileCopyInfo>> onPoll(PollResponse<ShareFileCopyInfo> pollResponse,
        ShareRequestConditions requestConditions) {
        if (pollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED
            || pollResponse.getStatus() == LongRunningOperationStatus.FAILED) {
            return Mono.just(pollResponse);
        }

        final ShareFileCopyInfo lastInfo = pollResponse.getValue();
        if (lastInfo == null) {
            logger.warning("ShareFileCopyInfo does not exist. Activation operation failed.");
            return Mono.just(new PollResponse<>(LongRunningOperationStatus.fromString("COPY_START_FAILED",
                    true), null));
        }

        return getPropertiesWithResponse(requestConditions)
            .map(response -> {
                ShareFileProperties value = response.getValue();
                final CopyStatusType status = value.getCopyStatus();
                final ShareFileCopyInfo result = new ShareFileCopyInfo(value.getCopySource(), value.getCopyId(),
                    status, value.getETag(), value.getCopyCompletionTime(), value.getCopyStatusDescription());

                LongRunningOperationStatus operationStatus;
                switch (status) {
                    case SUCCESS:
                        operationStatus = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
                        break;
                    case FAILED:
                        operationStatus = LongRunningOperationStatus.FAILED;
                        break;
                    case ABORTED:
                        operationStatus = LongRunningOperationStatus.USER_CANCELLED;
                        break;
                    case PENDING:
                        operationStatus = LongRunningOperationStatus.IN_PROGRESS;
                        break;
                    default:
                        throw logger.logExceptionAsError(new IllegalArgumentException(
                            "CopyStatusType is not supported. Status: " + status));
                }

                return new PollResponse<>(operationStatus, result);
            }).onErrorReturn(new PollResponse<>(LongRunningOperationStatus.fromString("POLLING_FAILED",
                        true), lastInfo));
    }

    /**
     * Aborts a pending Copy File operation, and leaves a destination file with zero length and full metadata.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Abort copy file from copy id("someCopyId") </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.abortCopy#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/abort-copy-file">Azure Docs</a>.</p>
     *
     * @param copyId Specifies the copy id which has copying pending status associate with it.
     * @return An empty response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> abortCopy(String copyId) {
        try {
            return abortCopyWithResponse(copyId).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Aborts a pending Copy File operation, and leaves a destination file with zero length and full metadata.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Abort copy file from copy id("someCopyId") </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.abortCopyWithResponse#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/abort-copy-file">Azure Docs</a>.</p>
     *
     * @param copyId Specifies the copy id which has copying pending status associate with it.
     * @return A response containing the status of aborting copy the file.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> abortCopyWithResponse(String copyId) {
        return this.abortCopyWithResponse(copyId, null);
    }

    /**
     * Aborts a pending Copy File operation, and leaves a destination file with zero length and full metadata.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Abort copy file from copy id("someCopyId") </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.abortCopyWithResponse#string-ShareRequestConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/abort-copy-file">Azure Docs</a>.</p>
     *
     * @param copyId Specifies the copy id which has copying pending status associate with it.
     * @param requestConditions {@link ShareRequestConditions}
     * @return A response containing the status of aborting copy the file.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> abortCopyWithResponse(String copyId, ShareRequestConditions requestConditions) {
        try {
            return withContext(context -> abortCopyWithResponse(copyId, requestConditions,
                context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> abortCopyWithResponse(String copyId, ShareRequestConditions requestConditions,
        Context context) {
        requestConditions = requestConditions == null ? new ShareRequestConditions() : requestConditions;
        return azureFileStorageClient.getFiles().abortCopyWithResponseAsync(shareName, filePath, copyId, null,
            requestConditions.getLeaseId(), context).map(response -> new SimpleResponse<>(response, null));
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
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.downloadToFile#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-file">Azure Docs</a>.</p>
     *
     * @param downloadFilePath The path where store the downloaded file
     * @return An empty response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ShareFileProperties> downloadToFile(String downloadFilePath) {
        try {
            return downloadToFileWithResponse(downloadFilePath, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.downloadToFileWithResponse#string-ShareFileRange}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-file">Azure Docs</a>.</p>
     *
     * @param downloadFilePath The path where store the downloaded file
     * @param range Optional byte range which returns file data only from the specified range.
     * @return An empty response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareFileProperties>> downloadToFileWithResponse(String downloadFilePath,
        ShareFileRange range) {
        return this.downloadToFileWithResponse(downloadFilePath, range, null);
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
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.downloadToFileWithResponse#string-ShareFileRange-ShareRequestConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-file">Azure Docs</a>.</p>
     *
     * @param downloadFilePath The path where store the downloaded file
     * @param range Optional byte range which returns file data only from the specified range.
     * @param requestConditions {@link ShareRequestConditions}
     * @return An empty response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareFileProperties>> downloadToFileWithResponse(String downloadFilePath,
        ShareFileRange range, ShareRequestConditions requestConditions) {
        try {
            return withContext(context -> downloadToFileWithResponse(downloadFilePath, range,
                requestConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<ShareFileProperties>> downloadToFileWithResponse(String downloadFilePath, ShareFileRange range,
        ShareRequestConditions requestConditions, Context context) {
        return Mono.using(() -> channelSetup(downloadFilePath, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW),
            channel -> getPropertiesWithResponse(requestConditions, context).flatMap(response ->
                downloadResponseInChunk(response, channel, range, requestConditions, context)), this::channelCleanUp);
    }

    private Mono<Response<ShareFileProperties>> downloadResponseInChunk(Response<ShareFileProperties> response,
        AsynchronousFileChannel channel, ShareFileRange range, ShareRequestConditions requestConditions,
        Context context) {
        return Mono.justOrEmpty(range).switchIfEmpty(Mono.just(new ShareFileRange(0, response.getValue()
            .getContentLength())))
            .map(currentRange -> {
                List<ShareFileRange> chunks = new ArrayList<>();
                for (long pos = currentRange.getStart(); pos < currentRange.getEnd(); pos += FILE_DEFAULT_BLOCK_SIZE) {
                    long count = FILE_DEFAULT_BLOCK_SIZE;
                    if (pos + count > currentRange.getEnd()) {
                        count = currentRange.getEnd() - pos;
                    }
                    chunks.add(new ShareFileRange(pos, pos + count - 1));
                }
                return chunks;
            }).flatMapMany(Flux::fromIterable).flatMap(chunk ->
                downloadWithResponse(new ShareFileDownloadOptions().setRange(chunk).setRangeContentMd5Requested(false)
                    .setRequestConditions(requestConditions), context)
                .map(ShareFileDownloadAsyncResponse::getValue)
                .subscribeOn(Schedulers.elastic())
                .flatMap(fbb -> FluxUtil
                    .writeFile(fbb, channel, chunk.getStart() - (range == null ? 0 : range.getStart()))
                    .subscribeOn(Schedulers.elastic())
                    .timeout(Duration.ofSeconds(DOWNLOAD_UPLOAD_CHUNK_TIMEOUT))
                    .retryWhen(Retry.max(3).filter(throwable -> throwable instanceof IOException
                        || throwable instanceof TimeoutException))))
            .then(Mono.just(response));
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

    /**
     * Downloads a file from the system, including its metadata and properties
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Download the file with its metadata and properties. </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.download}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-file">Azure Docs</a>.</p>
     *
     * @return A reactive response containing the file data.
     */
    public Flux<ByteBuffer> download() {
        try {
            return downloadWithResponse(null).flatMapMany(ShareFileDownloadAsyncResponse::getValue);
        } catch (RuntimeException ex) {
            return fluxError(logger, ex);
        }
    }

    /**
     * Downloads a file from the system, including its metadata and properties
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Download the file from 1024 to 2048 bytes with its metadata and properties and without the contentMD5. </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.downloadWithResponse#ShareFileRange-Boolean}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-file">Azure Docs</a>.</p>
     *
     * @param range Optional byte range which returns file data only from the specified range.
     * @param rangeGetContentMD5 Optional boolean which the service returns the MD5 hash for the range when it sets to
     * true, as long as the range is less than or equal to 4 MB in size.
     * @return A reactive response containing response data and the file data.
     */
    public Mono<ShareFileDownloadAsyncResponse> downloadWithResponse(ShareFileRange range, Boolean rangeGetContentMD5) {
        return this.downloadWithResponse(range, rangeGetContentMD5, null);
    }

    /**
     * Downloads a file from the system, including its metadata and properties
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Download the file from 1024 to 2048 bytes with its metadata and properties and without the contentMD5. </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.downloadWithResponse#ShareFileRange-Boolean-ShareRequestConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-file">Azure Docs</a>.</p>
     *
     * @param range Optional byte range which returns file data only from the specified range.
     * @param rangeGetContentMD5 Optional boolean which the service returns the MD5 hash for the range when it sets to
     * @param requestConditions {@link ShareRequestConditions}
     * true, as long as the range is less than or equal to 4 MB in size.
     * @return A reactive response containing response data and the file data.
     */
    public Mono<ShareFileDownloadAsyncResponse> downloadWithResponse(ShareFileRange range, Boolean rangeGetContentMD5,
        ShareRequestConditions requestConditions) {
        return downloadWithResponse(new ShareFileDownloadOptions().setRange(range)
            .setRangeContentMd5Requested(rangeGetContentMD5).setRequestConditions(requestConditions));
    }

    /**
     * Downloads a file from the system, including its metadata and properties
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Download the file from 1024 to 2048 bytes with its metadata and properties and without the contentMD5. </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.downloadWithResponse#ShareFileDownloadOptions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-file">Azure Docs</a>.</p>
     *
     * @param options {@link ShareFileDownloadOptions}
     * true, as long as the range is less than or equal to 4 MB in size.
     * @return A reactive response containing response data and the file data.
     */
    public Mono<ShareFileDownloadAsyncResponse> downloadWithResponse(ShareFileDownloadOptions options) {
        try {
            return withContext(context -> downloadWithResponse(options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<ShareFileDownloadAsyncResponse> downloadWithResponse(ShareFileDownloadOptions options, Context context) {
        options = options == null ? new ShareFileDownloadOptions() : options;
        ShareFileRange range = options.getRange() == null ? new ShareFileRange(0) : options.getRange();
        ShareRequestConditions requestConditions = options.getRequestConditions() == null
            ? new ShareRequestConditions() : options.getRequestConditions();
        DownloadRetryOptions retryOptions = options.getRetryOptions() == null ? new DownloadRetryOptions()
            : options.getRetryOptions();
        Boolean getRangeContentMd5 = options.isRangeContentMd5Requested();

        return downloadRange(range, getRangeContentMd5, requestConditions, context)
            .map(response -> {
                String eTag = ModelHelper.getETag(response.getHeaders());
                ShareFileDownloadHeaders headers = ModelHelper.transformFileDownloadHeaders(response.getHeaders());

                long finalEnd;
                if (range.getEnd() == null) {
                    finalEnd = headers.getContentRange() == null ? headers.getContentLength()
                        : Long.parseLong(headers.getContentRange().split("/")[1]);
                } else {
                    finalEnd = range.getEnd();
                }

                Flux<ByteBuffer> bufferFlux  = FluxUtil.createRetriableDownloadFlux(
                    () -> response.getValue().timeout(TIMEOUT_VALUE),
                    (throwable, offset) -> {
                        if (!(throwable instanceof IOException || throwable instanceof TimeoutException)) {
                            return Flux.error(throwable);
                        }

                        long newCount = finalEnd - (offset - range.getStart());

                        /*
                         It is possible that the network stream will throw an error after emitting all data but before
                         completing. Issuing a retry at this stage would leave the download in a bad state with incorrect count
                         and offset values. Because we have read the intended amount of data, we can ignore the error at the end
                         of the stream.
                         */
                        if (newCount == 0) {
                            logger.warning("Exception encountered in ReliableDownload after all data read from the network but "
                                + "but before stream signaled completion. Returning success as all data was downloaded. "
                                + "Exception message: " + throwable.getMessage());
                            return Flux.empty();
                        }

                        try {
                            return downloadRange(
                                new ShareFileRange(offset, range.getEnd()), getRangeContentMd5,
                                requestConditions, context).flatMapMany(r -> {
                                    String receivedETag = ModelHelper.getETag(r.getHeaders());
                                    if (eTag != null && eTag.equals(receivedETag)) {
                                        return r.getValue().timeout(TIMEOUT_VALUE);
                                    } else {
                                        return Flux.<ByteBuffer>error(
                                            new ConcurrentModificationException(String.format("File has been modified "
                                                + "concurrently. Expected eTag: %s, Received eTag: %s", eTag,
                                                receivedETag)));
                                    }
                                });
                        } catch (Exception e) {
                            return Flux.error(e);
                        }
                    },
                    retryOptions.getMaxRetryRequests(),
                    range.getStart()
                ).switchIfEmpty(Flux.just(ByteBuffer.wrap(new byte[0])));

                return new ShareFileDownloadAsyncResponse(response.getRequest(), response.getStatusCode(),
                    response.getHeaders(), bufferFlux, headers);
            });
    }

    private Mono<StreamResponse> downloadRange(ShareFileRange range, Boolean rangeGetContentMD5,
        ShareRequestConditions requestConditions, Context context) {
        String rangeString = range == null ? null : range.toHeaderValue();
        return azureFileStorageClient.getFiles().downloadWithResponseAsync(shareName, filePath, null,
            rangeString, rangeGetContentMD5, requestConditions.getLeaseId(),  context);
    }

    /**
     * Deletes the file associate with the client.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the file</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.delete}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-file2">Azure Docs</a>.</p>
     *
     * @return An empty response
     * @throws ShareStorageException If the directory doesn't exist or the file doesn't exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> delete() {
        try {
            return deleteWithResponse(null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes the file associate with the client.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the file</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.deleteWithResponse}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-file2">Azure Docs</a>.</p>
     *
     * @return A response that only contains headers and response status code
     * @throws ShareStorageException If the directory doesn't exist or the file doesn't exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteWithResponse() {
        return deleteWithResponse(null);
    }

    /**
     * Deletes the file associate with the client.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the file</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.deleteWithResponse#ShareRequestConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-file2">Azure Docs</a>.</p>
     *
     * @param requestConditions {@link ShareRequestConditions}
     * @return A response that only contains headers and response status code
     * @throws ShareStorageException If the directory doesn't exist or the file doesn't exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteWithResponse(ShareRequestConditions requestConditions) {
        try {
            return withContext(context -> this.deleteWithResponse(requestConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> deleteWithResponse(ShareRequestConditions requestConditions, Context context) {
        requestConditions = requestConditions == null ? new ShareRequestConditions() : requestConditions;
        return azureFileStorageClient.getFiles().deleteWithResponseAsync(shareName, filePath, null,
            requestConditions.getLeaseId(), context).map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Retrieves the properties of the storage account's file. The properties includes file metadata, last modified
     * date, is server encrypted, and eTag.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve file properties</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.getProperties}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-file-properties">Azure Docs</a>.</p>
     *
     * @return {@link ShareFileProperties Storage file properties}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ShareFileProperties> getProperties() {
        try {
            return getPropertiesWithResponse().flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Retrieves the properties of the storage account's file. The properties includes file metadata, last modified
     * date, is server encrypted, and eTag.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve file properties</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.getPropertiesWithResponse}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-file-properties">Azure Docs</a>.</p>
     *
     * @return A response containing the {@link ShareFileProperties storage file properties} and response status code
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareFileProperties>> getPropertiesWithResponse() {
        return this.getPropertiesWithResponse(null);
    }

    /**
     * Retrieves the properties of the storage account's file. The properties includes file metadata, last modified
     * date, is server encrypted, and eTag.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve file properties</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.getPropertiesWithResponse#ShareRequestConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-file-properties">Azure Docs</a>.</p>
     *
     * @param requestConditions {@link ShareRequestConditions}
     * @return A response containing the {@link ShareFileProperties storage file properties} and response status code
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareFileProperties>> getPropertiesWithResponse(ShareRequestConditions requestConditions) {
        try {
            return withContext(context -> this.getPropertiesWithResponse(requestConditions,
                context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<ShareFileProperties>> getPropertiesWithResponse(ShareRequestConditions requestConditions,
        Context context) {
        requestConditions = requestConditions == null ? new ShareRequestConditions() : requestConditions;
        context = context == null ? Context.NONE : context;
        return azureFileStorageClient.getFiles()
            .getPropertiesWithResponseAsync(shareName, filePath, snapshot, null, requestConditions.getLeaseId(),
                context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
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
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.setProperties#long-ShareFileHttpHeaders-FileSmbProperties-String}
     *
     * <p>Clear the metadata of the file and preserve the SMB properties</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.setProperties#long-ShareFileHttpHeaders-FileSmbProperties-String.clearHttpHeaderspreserveSMBProperties}
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
    public Mono<ShareFileInfo> setProperties(long newFileSize, ShareFileHttpHeaders httpHeaders,
                                        FileSmbProperties smbProperties, String filePermission) {
        try {
            return setPropertiesWithResponse(newFileSize, httpHeaders, smbProperties, filePermission)
                .flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.setPropertiesWithResponse#long-ShareFileHttpHeaders-FileSmbProperties-String}
     *
     * <p>Clear the metadata of the file and preserve the SMB properties</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.setPropertiesWithResponse#long-ShareFileHttpHeaders-FileSmbProperties-String.clearHttpHeaderspreserveSMBProperties}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-file-properties">Azure Docs</a>.</p>
     *
     * @param newFileSize New file size of the file.
     * @param httpHeaders The user settable file http headers.
     * @param smbProperties The user settable file smb properties.
     * @param filePermission The file permission of the file.
     * @return Response containing the {@link ShareFileInfo file info} and response status code.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareFileInfo>> setPropertiesWithResponse(long newFileSize, ShareFileHttpHeaders httpHeaders,
                                                              FileSmbProperties smbProperties, String filePermission) {
        return this.setPropertiesWithResponse(newFileSize, httpHeaders, smbProperties, filePermission, null);
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
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.setPropertiesWithResponse#long-ShareFileHttpHeaders-FileSmbProperties-String-ShareRequestConditions}
     *
     * <p>Clear the metadata of the file and preserve the SMB properties</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.setPropertiesWithResponse#long-ShareFileHttpHeaders-FileSmbProperties-String-ShareRequestConditions.clearHttpHeaderspreserveSMBProperties}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-file-properties">Azure Docs</a>.</p>
     *
     * @param newFileSize New file size of the file.
     * @param httpHeaders The user settable file http headers.
     * @param smbProperties The user settable file smb properties.
     * @param filePermission The file permission of the file.
     * @param requestConditions {@link ShareRequestConditions}
     * @return Response containing the {@link ShareFileInfo file info} and response status code.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareFileInfo>> setPropertiesWithResponse(long newFileSize, ShareFileHttpHeaders httpHeaders,
        FileSmbProperties smbProperties, String filePermission, ShareRequestConditions requestConditions) {
        try {
            return withContext(context ->
                setPropertiesWithResponse(newFileSize, httpHeaders, smbProperties, filePermission, requestConditions,
                    context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<ShareFileInfo>> setPropertiesWithResponse(long newFileSize, ShareFileHttpHeaders httpHeaders,
        FileSmbProperties smbProperties, String filePermission, ShareRequestConditions requestConditions,
        Context context) {
        requestConditions = requestConditions == null ? new ShareRequestConditions() : requestConditions;
        smbProperties = smbProperties == null ? new FileSmbProperties() : smbProperties;

        // Checks that file permission and file permission key are valid
        validateFilePermissionAndKey(filePermission, smbProperties.getFilePermissionKey());

        // If file permission and file permission key are both not set then set default value
        filePermission = smbProperties.setFilePermission(filePermission, FileConstants.PRESERVE);
        String filePermissionKey = smbProperties.getFilePermissionKey();

        String fileAttributes = smbProperties.setNtfsFileAttributes(FileConstants.PRESERVE);
        String fileCreationTime = smbProperties.setFileCreationTime(FileConstants.PRESERVE);
        String fileLastWriteTime = smbProperties.setFileLastWriteTime(FileConstants.PRESERVE);
        context = context == null ? Context.NONE : context;

        return azureFileStorageClient.getFiles()
            .setHttpHeadersWithResponseAsync(shareName, filePath, fileAttributes, fileCreationTime,
                fileLastWriteTime, null, newFileSize, filePermission, filePermissionKey, requestConditions.getLeaseId(),
                httpHeaders, context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
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
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.setMetadata#map}
     *
     * <p>Clear the metadata of the file</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.setMetadataWithResponse#map.clearMetadata}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-file-metadata">Azure Docs</a>.</p>
     *
     * @param metadata Options.Metadata to set on the file, if null is passed the metadata for the file is cleared
     * @return {@link ShareFileMetadataInfo file meta info}
     * @throws ShareStorageException If the file doesn't exist or the metadata contains invalid keys
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ShareFileMetadataInfo> setMetadata(Map<String, String> metadata) {
        try {
            return setMetadataWithResponse(metadata).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.setMetadataWithResponse#map}
     *
     * <p>Clear the metadata of the file</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.setMetadataWithResponse#map.clearMetadata}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-file-metadata">Azure Docs</a>.</p>
     *
     * @param metadata Options.Metadata to set on the file, if null is passed the metadata for the file is cleared
     * @return A response containing the {@link ShareFileMetadataInfo file meta info} and status code
     * @throws ShareStorageException If the file doesn't exist or the metadata contains invalid keys
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareFileMetadataInfo>> setMetadataWithResponse(Map<String, String> metadata) {
        return setMetadataWithResponse(metadata, null);
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
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.setMetadataWithResponse#map-ShareRequestConditions}
     *
     * <p>Clear the metadata of the file</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.setMetadataWithResponse#map-ShareRequestConditions.clearMetadata}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-file-metadata">Azure Docs</a>.</p>
     *
     * @param metadata Options.Metadata to set on the file, if null is passed the metadata for the file is cleared
     * @param requestConditions {@link ShareRequestConditions}
     * @return A response containing the {@link ShareFileMetadataInfo file meta info} and status code
     * @throws ShareStorageException If the file doesn't exist or the metadata contains invalid keys
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareFileMetadataInfo>> setMetadataWithResponse(Map<String, String> metadata,
        ShareRequestConditions requestConditions) {
        try {
            return withContext(context -> setMetadataWithResponse(metadata, requestConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<ShareFileMetadataInfo>> setMetadataWithResponse(Map<String, String> metadata,
        ShareRequestConditions requestConditions, Context context) {
        requestConditions = requestConditions == null ? new ShareRequestConditions() : requestConditions;
        context = context == null ? Context.NONE : context;
        try {
            return azureFileStorageClient.getFiles()
                .setMetadataWithResponseAsync(shareName, filePath, null, metadata,
                    requestConditions.getLeaseId(),
                    context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
                .map(this::setMetadataResponse);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Uploads a range of bytes to the beginning of a file in storage file service. Upload operations performs an
     * in-place write on the specified file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Upload data "default" to the file in Storage File Service. </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.upload#flux-long}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-range">Azure Docs</a>.</p>
     *
     * @param data The data which will upload to the storage file.
     * @param length Specifies the number of bytes being transmitted in the request body.
     * @return A response that only contains headers and response status code
     *
     * @deprecated Use {@link ShareFileAsyncClient#uploadRange(Flux, long)} instead. Or consider
     * {@link ShareFileAsyncClient#upload(Flux, ParallelTransferOptions)} for an upload that can handle large amounts of
     * data.
     */
    @Deprecated
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ShareFileUploadInfo> upload(Flux<ByteBuffer> data, long length) {
        try {
            return uploadWithResponse(data, length, 0L).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Uploads a range of bytes to specific of a file in storage file service. Upload operations performs an in-place
     * write on the specified file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Upload the file from 1024 to 2048 bytes with its metadata and properties and without the contentMD5. </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.uploadWithResponse#flux-long-long}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-range">Azure Docs</a>.</p>
     *
     * @param data The data which will upload to the storage file.
     * @param length Specifies the number of bytes being transmitted in the request body. When the
     * ShareFileRangeWriteType is set to clear, the value of this header must be set to zero.
     * @param offset Optional starting point of the upload range. It will start from the beginning if it is
     * {@code null}.
     * @return A response containing the {@link ShareFileUploadInfo file upload info} with headers and response
     * status code.
     * @throws ShareStorageException If you attempt to upload a range that is larger than 4 MB, the service returns
     * status code 413 (Request Entity Too Large)
     *
     * @deprecated Use {@link ShareFileAsyncClient#uploadRangeWithResponse(ShareFileUploadRangeOptions)} instead. Or
     * consider {@link ShareFileAsyncClient#uploadWithResponse(ShareFileUploadOptions)} for an upload that can handle
     * large amounts of data.
     */
    @Deprecated
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareFileUploadInfo>> uploadWithResponse(Flux<ByteBuffer> data, long length, Long offset) {
        return this.uploadWithResponse(data, length, offset, null);
    }

    /**
     * Uploads a range of bytes to specific offset of a file in storage file service. Upload operations performs an
     * in-place write on the specified file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Upload the file from 1024 to 2048 bytes with its metadata and properties and without the contentMD5. </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.uploadWithResponse#flux-long-long-ShareRequestConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-range">Azure Docs</a>.</p>
     *
     * @param data The data which will upload to the storage file.
     * @param length Specifies the number of bytes being transmitted in the request body. When the
     * ShareFileRangeWriteType is set to clear, the value of this header must be set to zero.
     * @param offset Optional starting point of the upload range. It will start from the beginning if it is
     * {@code null}.
     * @param requestConditions {@link ShareRequestConditions}
     * @return A response containing the {@link ShareFileUploadInfo file upload info} with headers and response
     * status code.
     * @throws ShareStorageException If you attempt to upload a range that is larger than 4 MB, the service returns
     * status code 413 (Request Entity Too Large)
     *
     * @deprecated Use {@link ShareFileAsyncClient#uploadRangeWithResponse(ShareFileUploadRangeOptions)} instead. Or
     * consider {@link ShareFileAsyncClient#uploadWithResponse(ShareFileUploadOptions)} for an upload that can handle
     * large amounts of data.
     */
    @Deprecated
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareFileUploadInfo>> uploadWithResponse(Flux<ByteBuffer> data, long length, Long offset,
        ShareRequestConditions requestConditions) {
        try {
            return withContext(context -> uploadRangeWithResponse(new ShareFileUploadRangeOptions(data, length)
                .setOffset(offset).setRequestConditions(requestConditions), context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Buffers a range of bytes and uploads sub-ranges in parallel to a file in storage file service. Upload operations
     * perform an in-place write on the specified file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Upload data "default" to the file in Storage File Service. </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.upload#Flux-ParallelTransferOptions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-range">Azure Docs</a>.</p>
     *
     * @param data The data which will upload to the storage file.
     * @param transferOptions {@link ParallelTransferOptions} to use to upload data.
     * @return The {@link ShareFileUploadInfo file upload info}
     */
    public Mono<ShareFileUploadInfo> upload(Flux<ByteBuffer> data, ParallelTransferOptions transferOptions) {
        try {
            return uploadWithResponse(new ShareFileUploadOptions(data).setParallelTransferOptions(transferOptions))
                .flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Buffers a range of bytes and uploads sub-ranges in parallel to a file in storage file service. Upload operations
     * perform an in-place write on the specified file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Upload data "default" to the file in Storage File Service. </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.uploadWithResponse#ShareFileUploadOptions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-range">Azure Docs</a>.</p>
     *
     * @param options Argument collection for the upload operation.
     * @return The {@link ShareFileUploadInfo file upload info}
     */
    public Mono<Response<ShareFileUploadInfo>> uploadWithResponse(ShareFileUploadOptions options) {
        try {
            return withContext(context -> uploadWithResponse(options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<ShareFileUploadInfo>> uploadWithResponse(ShareFileUploadOptions options, Context context) {
        try {
            StorageImplUtils.assertNotNull("options", options);
            ShareRequestConditions validatedRequestConditions = options.getRequestConditions() == null
                ? new ShareRequestConditions()
                : options.getRequestConditions();
            final ParallelTransferOptions validatedParallelTransferOptions =
                ModelHelper.populateAndApplyDefaults(options.getParallelTransferOptions());
            long validatedOffset = options.getOffset() == null ? 0 : options.getOffset();

            Function<Flux<ByteBuffer>, Mono<Response<ShareFileUploadInfo>>> uploadInChunks = (stream) ->
                uploadInChunks(stream, validatedOffset, validatedParallelTransferOptions, validatedRequestConditions, context);

            BiFunction<Flux<ByteBuffer>, Long, Mono<Response<ShareFileUploadInfo>>> uploadFull = (stream, length) ->
                uploadRangeWithResponse(new ShareFileUploadRangeOptions(ProgressReporter.addProgressReporting(
                    stream, validatedParallelTransferOptions.getProgressReceiver()), length)
                    .setOffset(options.getOffset()).setRequestConditions(validatedRequestConditions), context);

            Flux<ByteBuffer> data = options.getDataFlux();
            // no specified length: use azure.core's converter
            if (data == null && options.getLength() == null) {
                // We can only buffer up to max int due to restrictions in ByteBuffer.
                int chunkSize = (int) Math.min(Constants.MAX_INPUT_STREAM_CONVERTER_BUFFER_LENGTH,
                    validatedParallelTransferOptions.getBlockSizeLong());
                data = FluxUtil.toFluxByteBuffer(options.getDataStream(), chunkSize);
            // specified length (legacy requirement): use custom converter. no marking because we buffer anyway.
            } else if (data == null) {
                // We can only buffer up to max int due to restrictions in ByteBuffer.
                int chunkSize = (int) Math.min(Constants.MAX_INPUT_STREAM_CONVERTER_BUFFER_LENGTH,
                    validatedParallelTransferOptions.getBlockSizeLong());
                data = Utility.convertStreamToByteBuffer(
                    options.getDataStream(), options.getLength(), chunkSize, false);
            }

            return UploadUtils.uploadFullOrChunked(data, validatedParallelTransferOptions, uploadInChunks, uploadFull);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<ShareFileUploadInfo>> uploadInChunks(Flux<ByteBuffer> data, long offset,
        ParallelTransferOptions parallelTransferOptions, ShareRequestConditions requestConditions, Context context) {
        // See ProgressReporter for an explanation on why this lock is necessary and why we use AtomicLong.
        AtomicLong totalProgress = new AtomicLong();
        Lock progressLock = new ReentrantLock();

        // Validation done in the constructor.
        BufferStagingArea stagingArea = new BufferStagingArea(parallelTransferOptions.getBlockSizeLong(), FILE_MAX_PUT_RANGE_SIZE);

        Flux<ByteBuffer> chunkedSource = UploadUtils.chunkSource(data, parallelTransferOptions);

        /*
         Write to the staging area and upload the output.
         maxConcurrency = 1 when writing means only 1 BufferAggregator will be accumulating at a time.
         parallelTransferOptions.getMaxConcurrency() appends will be happening at once, so we guarantee buffering of
         only concurrency + 1 chunks at a time.
         */
        return chunkedSource.flatMapSequential(stagingArea::write, 1, 1)
            .concatWith(Flux.defer(stagingArea::flush))
            .map(bufferAggregator -> Tuples.of(bufferAggregator, bufferAggregator.length(), 0L))
            /* Scan reduces a flux with an accumulator while emitting the intermediate results. */
            /* As an example, data consists of ByteBuffers of length 10-10-5.
               In the map above we transform the initial ByteBuffer to a tuple3 of buff, 10, 0.
               Scan will emit that as is, then accumulate the tuple for the next emission.
               On the second iteration, the middle ByteBuffer gets transformed to buff, 10, 10+0
               (from previous emission). Scan emits that, and on the last iteration, the last ByteBuffer gets
               transformed to buff, 5, 10+10 (from previous emission). */
            .scan((result, source) -> {
                BufferAggregator bufferAggregator = source.getT1();
                long currentBufferLength = bufferAggregator.length();
                long lastBytesWritten = result.getT2();
                long lastOffset = result.getT3();

                return Tuples.of(bufferAggregator, currentBufferLength, lastBytesWritten + lastOffset);
            })
            .flatMapSequential(tuple3 -> {
                BufferAggregator bufferAggregator = tuple3.getT1();
                long currentBufferLength = bufferAggregator.length();
                long currentOffset = tuple3.getT3() + offset;
                // Report progress as necessary.
                Flux<ByteBuffer> progressData = ProgressReporter.addParallelProgressReporting(
                    bufferAggregator.asFlux(), parallelTransferOptions.getProgressReceiver(),
                    progressLock, totalProgress);
                return uploadRangeWithResponse(new ShareFileUploadRangeOptions(progressData, currentBufferLength)
                    .setOffset(currentOffset).setRequestConditions(requestConditions), context)
                    .flux();
            }, parallelTransferOptions.getMaxConcurrency(), 1)
            .last();
    }

    /**
     * Uploads a range of bytes to the specified offset of a file in storage file service. Upload operations perform an
     * in-place write on the specified file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Upload data "default" to the file in Storage File Service. </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.uploadRange#Flux-long}
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
    public Mono<ShareFileUploadInfo> uploadRange(Flux<ByteBuffer> data, long length) {
        try {
            return uploadRangeWithResponse(new ShareFileUploadRangeOptions(data, length)).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Uploads a range of bytes to the specified offset of a file in storage file service. Upload operations perform an
     * in-place write on the specified file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Upload data "default" to the file in Storage File Service. </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.uploadRangeWithResponse#ShareFileUploadRangeOptions}
     *
     * <p>This method does a single Put Range operation. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-range">Azure Docs</a>.</p>
     *
     * @param options Argument collection for the upload operation.
     * @return The {@link ShareFileUploadInfo file upload info}
     * @throws ShareStorageException If you attempt to upload a range that is larger than 4 MB, the service returns
     * status code 413 (Request Entity Too Large)
     */
    public Mono<Response<ShareFileUploadInfo>> uploadRangeWithResponse(ShareFileUploadRangeOptions options) {
        try {
            return withContext(context -> uploadRangeWithResponse(options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * One-shot upload range.
     */
    Mono<Response<ShareFileUploadInfo>> uploadRangeWithResponse(ShareFileUploadRangeOptions options, Context context) {
        ShareRequestConditions requestConditions = options.getRequestConditions() == null
            ? new ShareRequestConditions() : options.getRequestConditions();
        long rangeOffset = (options.getOffset() == null) ? 0L : options.getOffset();
        ShareFileRange range = new ShareFileRange(rangeOffset, rangeOffset + options.getLength() - 1);
        context = context == null ? Context.NONE : context;

        Flux<ByteBuffer> data = options.getDataFlux() == null
            ? Utility.convertStreamToByteBuffer(
                options.getDataStream(), options.getLength(), (int) FILE_DEFAULT_BLOCK_SIZE, true)
            : options.getDataFlux();

        return azureFileStorageClient.getFiles()
            .uploadRangeWithResponseAsync(shareName, filePath, range.toString(), ShareFileRangeWriteType.UPDATE,
                options.getLength(), null, null, requestConditions.getLeaseId(), data,
                context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(this::uploadResponse);
    }

    // TODO: (gapra) Fix put range from URL link. Service docs have not been updated to show this API
    /**
     * Uploads a range of bytes from one file to another file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Upload a number of bytes from a file at defined source and destination offsets </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.uploadRangeFromUrl#long-long-long-String}
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
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ShareFileUploadRangeFromUrlInfo> uploadRangeFromUrl(long length, long destinationOffset,
        long sourceOffset, String sourceUrl) {
        try {
            return uploadRangeFromUrlWithResponse(length, destinationOffset, sourceOffset, sourceUrl)
                .flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    // TODO: (gapra) Fix put range from URL link. Service docs have not been updated to show this API
    /**
     * Uploads a range of bytes from one file to another file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Upload a number of bytes from a file at defined source and destination offsets </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.uploadRangeFromUrlWithResponse#long-long-long-String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-range">Azure Docs</a>.</p>
     *
     * @param length Specifies the number of bytes being transmitted in the request body.
     * @param destinationOffset Starting point of the upload range on the destination.
     * @param sourceOffset Starting point of the upload range on the source.
     * @param sourceUrl Specifies the URL of the source file.
     * @return A response containing the {@link ShareFileUploadRangeFromUrlInfo file upload range from url info} with
     * headers and response status code.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareFileUploadRangeFromUrlInfo>> uploadRangeFromUrlWithResponse(long length,
        long destinationOffset, long sourceOffset, String sourceUrl) {
        return this.uploadRangeFromUrlWithResponse(length, destinationOffset, sourceOffset, sourceUrl, null);
    }

    // TODO: (gapra) Fix put range from URL link. Service docs have not been updated to show this API
    /**
     * Uploads a range of bytes from one file to another file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Upload a number of bytes from a file at defined source and destination offsets </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.uploadRangeFromUrlWithResponse#long-long-long-String-ShareRequestConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-range">Azure Docs</a>.</p>
     *
     * @param length Specifies the number of bytes being transmitted in the request body.
     * @param destinationOffset Starting point of the upload range on the destination.
     * @param sourceOffset Starting point of the upload range on the source.
     * @param sourceUrl Specifies the URL of the source file.
     * @param destinationRequestConditions {@link ShareRequestConditions}
     * @return A response containing the {@link ShareFileUploadRangeFromUrlInfo file upload range from url info} with
     * headers and response status code.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareFileUploadRangeFromUrlInfo>> uploadRangeFromUrlWithResponse(long length,
        long destinationOffset, long sourceOffset, String sourceUrl,
        ShareRequestConditions destinationRequestConditions) {
        return this.uploadRangeFromUrlWithResponse(new ShareFileUploadRangeFromUrlOptions(length, sourceUrl)
            .setDestinationOffset(destinationOffset).setSourceOffset(sourceOffset)
            .setDestinationRequestConditions(destinationRequestConditions));
    }

    // TODO: (gapra) Fix put range from URL link. Service docs have not been updated to show this API
    /**
     * Uploads a range of bytes from one file to another file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Upload a number of bytes from a file at defined source and destination offsets </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.uploadRangeFromUrlWithResponse#ShareFileUploadRangeFromUrlOptions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-range">Azure Docs</a>.</p>
     *
     * @param options argument collection
     * @return A response containing the {@link ShareFileUploadRangeFromUrlInfo file upload range from url info} with
     * headers and response status code.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareFileUploadRangeFromUrlInfo>> uploadRangeFromUrlWithResponse(
        ShareFileUploadRangeFromUrlOptions options) {
        try {
            return withContext(context ->
                uploadRangeFromUrlWithResponse(options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<ShareFileUploadRangeFromUrlInfo>> uploadRangeFromUrlWithResponse(
        ShareFileUploadRangeFromUrlOptions options, Context context) {
        ShareRequestConditions modifiedRequestConditions = options.getDestinationRequestConditions() == null
            ? new ShareRequestConditions() : options.getDestinationRequestConditions();
        ShareFileRange destinationRange = new ShareFileRange(options.getDestinationOffset(),
            options.getDestinationOffset() + options.getLength() - 1);
        ShareFileRange sourceRange = new ShareFileRange(options.getSourceOffset(),
            options.getSourceOffset() + options.getLength() - 1);
        context = context == null ? Context.NONE : context;

        String sourceAuth = options.getSourceAuthorization() == null
            ? null : options.getSourceAuthorization().toString();

        final String copySource = Utility.encodeUrlPath(options.getSourceUrl());

        return azureFileStorageClient.getFiles()
            .uploadRangeFromURLWithResponseAsync(shareName, filePath, destinationRange.toString(), copySource, 0,
                null, sourceRange.toString(), null, modifiedRequestConditions.getLeaseId(), sourceAuth, null,
                context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(this::uploadRangeFromUrlResponse);
    }

    /**
     * Clear a range of bytes to specific of a file in storage file service. Clear operations performs an in-place write
     * on the specified file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Clears the first 1024 bytes. </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.clearRange#long}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-range">Azure Docs</a>.</p>
     *
     * @param length Specifies the number of bytes being cleared.
     * @return The {@link ShareFileUploadInfo file upload info}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ShareFileUploadInfo> clearRange(long length) {
        try {
            return clearRangeWithResponse(length, 0).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Clear a range of bytes to specific of a file in storage file service. Clear operations performs an in-place write
     * on the specified file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Clear the range starting from 1024 with length of 1024. </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.clearRange#long-long}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-range">Azure Docs</a>.</p>
     *
     * @param length Specifies the number of bytes being cleared in the request body.
     * @param offset Optional starting point of the upload range. It will start from the beginning if it is
     * {@code null}
     * @return A response of {@link ShareFileUploadInfo file upload info} that only contains headers and response
     * status code.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareFileUploadInfo>> clearRangeWithResponse(long length, long offset) {
        return this.clearRangeWithResponse(length, offset, null);
    }

    /**
     * Clear a range of bytes to specific of a file in storage file service. Clear operations performs an in-place write
     * on the specified file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Clear the range starting from 1024 with length of 1024. </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.clearRange#long-long-ShareRequestConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-range">Azure Docs</a>.</p>
     *
     * @param length Specifies the number of bytes being cleared in the request body.
     * @param offset Optional starting point of the upload range. It will start from the beginning if it is
     * {@code null}
     * @param requestConditions {@link ShareRequestConditions}
     * @return A response of {@link ShareFileUploadInfo file upload info} that only contains headers and response
     * status code.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareFileUploadInfo>> clearRangeWithResponse(long length, long offset,
        ShareRequestConditions requestConditions) {
        try {
            return withContext(context -> clearRangeWithResponse(length, offset, requestConditions,
                context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<ShareFileUploadInfo>> clearRangeWithResponse(long length, long offset,
        ShareRequestConditions requestConditions, Context context) {
        requestConditions = requestConditions == null ? new ShareRequestConditions() : requestConditions;
        ShareFileRange range = new ShareFileRange(offset, offset + length - 1);
        context = context == null ? Context.NONE : context;
        return azureFileStorageClient.getFiles()
            .uploadRangeWithResponseAsync(shareName, filePath, range.toString(), ShareFileRangeWriteType.CLEAR,
                0L, null, null, requestConditions.getLeaseId(), null,
                context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(this::uploadResponse);
    }

    /**
     * Uploads file to storage file service.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p> Upload the file from the source file path. </p>
     *
     * (@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.uploadFromFile#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-file">Azure Docs Create File</a>
     * and
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-range">Azure Docs Upload</a>.</p>
     *
     * @param uploadFilePath The path where store the source file to upload
     * @return An empty response.
     * @throws UncheckedIOException If an I/O error occurs.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> uploadFromFile(String uploadFilePath) {
        return this.uploadFromFile(uploadFilePath, null);
    }

    /**
     * Uploads file to storage file service.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p> Upload the file from the source file path. </p>
     *
     * (@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.uploadFromFile#string-ShareRequestConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-file">Azure Docs Create File</a>
     * and
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-range">Azure Docs Upload</a>.</p>
     *
     * @param uploadFilePath The path where store the source file to upload
     * @param requestConditions {@link ShareRequestConditions}
     * @return An empty response.
     * @throws UncheckedIOException If an I/O error occurs.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> uploadFromFile(String uploadFilePath, ShareRequestConditions requestConditions) {
        try {
            return Mono.using(() -> channelSetup(uploadFilePath, StandardOpenOption.READ),
                channel -> Flux.fromIterable(sliceFile(uploadFilePath))
                    .flatMap(chunk -> uploadWithResponse(FluxUtil.readFile(channel, chunk.getStart(),
                        chunk.getEnd() - chunk.getStart() + 1), chunk.getEnd() - chunk.getStart() + 1,
                        chunk.getStart(), requestConditions)
                        .timeout(Duration.ofSeconds(DOWNLOAD_UPLOAD_CHUNK_TIMEOUT))
                        .retryWhen(Retry.max(3).filter(throwable -> throwable instanceof IOException
                            || throwable instanceof TimeoutException)))
                    .then(), this::channelCleanUp);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    private List<ShareFileRange> sliceFile(String path) {
        File file = new File(path);
        assert file.exists();
        List<ShareFileRange> ranges = new ArrayList<>();
        for (long pos = 0; pos < file.length(); pos += FILE_DEFAULT_BLOCK_SIZE) {
            long count = FILE_DEFAULT_BLOCK_SIZE;
            if (pos + count > file.length()) {
                count = file.length() - pos;
            }
            ranges.add(new ShareFileRange(pos, pos + count - 1));
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
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.listRanges}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-ranges">Azure Docs</a>.</p>
     *
     * @return {@link ShareFileRange ranges} in the files.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ShareFileRange> listRanges() {
        try {
            return listRanges(null);
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    /**
     * List of valid ranges for a file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all ranges within the file range from 1KB to 2KB.</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.listRanges#ShareFileRange}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-ranges">Azure Docs</a>.</p>
     *
     * @param range Optional byte range which returns file data only from the specified range.
     * @return {@link ShareFileRange ranges} in the files that satisfy the requirements
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ShareFileRange> listRanges(ShareFileRange range) {
        return this.listRanges(range, null);
    }

    /**
     * List of valid ranges for a file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all ranges within the file range from 1KB to 2KB.</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.listRanges#ShareFileRange-ShareRequestConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-ranges">Azure Docs</a>.</p>
     *
     * @param range Optional byte range which returns file data only from the specified range.
     * @param requestConditions {@link ShareRequestConditions}
     * @return {@link ShareFileRange ranges} in the files that satisfy the requirements
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ShareFileRange> listRanges(ShareFileRange range, ShareRequestConditions requestConditions) {
        try {
            return listRangesWithOptionalTimeout(range, requestConditions, null, Context.NONE);
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    /**
     * List of valid ranges for a file between the file and the specified snapshot.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.listRangesDiff#String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-ranges">Azure Docs</a>.</p>
     *
     * @param previousSnapshot Specifies that the response will contain only ranges that were changed between target
     * file and previous snapshot. Changed ranges include both updated and cleared ranges. The target file may be a
     * snapshot, as long as the snapshot specified by previousSnapshot is the older of the two.
     * @return {@link ShareFileRange ranges} in the files that satisfy the requirements
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ShareFileRangeList> listRangesDiff(String previousSnapshot) {
        try {
            return listRangesDiffWithResponse(new ShareFileListRangesDiffOptions(previousSnapshot))
                .map(Response::getValue);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * List of valid ranges for a file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all ranges within the file range from 1KB to 2KB.</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.listRangesDiffWithResponse#ShareFileListRangesDiffOptions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-ranges">Azure Docs</a>.</p>
     *
     * @param options {@link ShareFileListRangesDiffOptions}.
     * @return {@link ShareFileRange ranges} in the files that satisfy the requirements
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareFileRangeList>> listRangesDiffWithResponse(ShareFileListRangesDiffOptions options) {
        try {
            StorageImplUtils.assertNotNull("options", options);
            return listRangesWithResponse(options.getRange(), options.getRequestConditions(),
                options.getPreviousSnapshot(), Context.NONE);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    PagedFlux<ShareFileRange> listRangesWithOptionalTimeout(ShareFileRange range,
        ShareRequestConditions requestConditions, Duration timeout,
        Context context) {

        Function<String, Mono<PagedResponse<ShareFileRange>>> retriever =
            marker -> StorageImplUtils.applyOptionalTimeout(
                this.listRangesWithResponse(range, requestConditions, null, context), timeout)
                .map(response -> new PagedResponseBase<>(response.getRequest(),
                    response.getStatusCode(),
                    response.getHeaders(),
                    response.getValue().getRanges().stream()
                        .map(r -> new Range().setStart(r.getStart()).setEnd(r.getEnd()))
                        .map(ShareFileRange::new).collect(Collectors.toList()),
                    null,
                    response.getHeaders()));

        return new PagedFlux<>(() -> retriever.apply(null), retriever);
    }

    Mono<Response<ShareFileRangeList>> listRangesWithResponse(ShareFileRange range,
        ShareRequestConditions requestConditions, String previousSnapshot, Context context) {

        ShareRequestConditions finalRequestConditions = requestConditions == null
            ? new ShareRequestConditions() : requestConditions;
        String rangeString = range == null ? null : range.toString();
        context = context == null ? Context.NONE : context;

        return this.azureFileStorageClient.getFiles().getRangeListWithResponseAsync(shareName, filePath, snapshot,
            previousSnapshot, null, rangeString, finalRequestConditions.getLeaseId(),
            context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(response -> new SimpleResponse<>(response, response.getValue()));
    }

    /**
     * List of open handles on a file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all handles for the file client.</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.listHandles}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-handles">Azure Docs</a>.</p>
     *
     * @return {@link HandleItem handles} in the files that satisfy the requirements
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<HandleItem> listHandles() {
        try {
            return listHandles(null);
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    /**
     * List of open handles on a file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List 10 handles for the file client.</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.listHandles#integer}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-handles">Azure Docs</a>.</p>
     *
     * @param maxResultsPerPage Optional maximum number of results will return per page
     * @return {@link HandleItem handles} in the file that satisfy the requirements
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<HandleItem> listHandles(Integer maxResultsPerPage) {
        try {
            return listHandlesWithOptionalTimeout(maxResultsPerPage, null, Context.NONE);
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    PagedFlux<HandleItem> listHandlesWithOptionalTimeout(Integer maxResultsPerPage, Duration timeout, Context context) {
        Function<String, Mono<PagedResponse<HandleItem>>> retriever =
            marker -> StorageImplUtils.applyOptionalTimeout(this.azureFileStorageClient.getFiles()
                .listHandlesWithResponseAsync(shareName, filePath, marker, maxResultsPerPage, null, snapshot,
                    context), timeout)
                .map(response -> new PagedResponseBase<>(response.getRequest(),
                    response.getStatusCode(),
                    response.getHeaders(),
                    response.getValue().getHandleList(),
                    response.getValue().getNextMarker(),
                    response.getDeserializedHeaders()));

        return new PagedFlux<>(() -> retriever.apply(null), retriever);
    }

    /**
     * Closes a handle on the file. This is intended to be used alongside {@link #listHandles()}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Force close handles returned by list handles.</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.forceCloseHandle#String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/force-close-handles">Azure Docs</a>.</p>
     *
     * @param handleId Handle ID to be closed.
     * @return A response that contains information about the closed handles.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CloseHandlesInfo> forceCloseHandle(String handleId) {
        try {
            return withContext(context -> forceCloseHandleWithResponse(handleId,
                context)).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Closes a handle on the file. This is intended to be used alongside {@link #listHandles()}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Force close handles returned by list handles.</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.forceCloseHandleWithResponse#String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/force-close-handles">Azure Docs</a>.</p>
     *
     * @param handleId Handle ID to be closed.
     * @return A response that contains information about the closed handles along with headers and response status
     * code.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CloseHandlesInfo>> forceCloseHandleWithResponse(String handleId) {
        try {
            return withContext(context -> forceCloseHandleWithResponse(handleId, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<CloseHandlesInfo>> forceCloseHandleWithResponse(String handleId, Context context) {
        context = context == null ? Context.NONE : context;
        return azureFileStorageClient.getFiles()
            .forceCloseHandlesWithResponseAsync(shareName, filePath, handleId, null, null, snapshot,
                context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(response -> new SimpleResponse<>(response,
                new CloseHandlesInfo(response.getDeserializedHeaders().getXMsNumberOfHandlesClosed(),
                    response.getDeserializedHeaders().getXMsNumberOfHandlesFailed())));
    }

    /**
     * Closes all handles opened on the file at the service.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Force close all handles.</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.forceCloseAllHandles}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/force-close-handles">Azure Docs</a>.</p>
     *
     * @return A response that contains information about the closed handles.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CloseHandlesInfo> forceCloseAllHandles() {
        try {
            return withContext(context -> forceCloseAllHandlesWithOptionalTimeout(null,
                context).reduce(new CloseHandlesInfo(0, 0),
                    (accu, next) -> new CloseHandlesInfo(accu.getClosedHandles() + next.getClosedHandles(),
                        accu.getFailedHandles() + next.getFailedHandles())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    PagedFlux<CloseHandlesInfo> forceCloseAllHandlesWithOptionalTimeout(Duration timeout, Context context) {
        Function<String, Mono<PagedResponse<CloseHandlesInfo>>> retriever =
            marker -> StorageImplUtils.applyOptionalTimeout(this.azureFileStorageClient.getFiles()
                .forceCloseHandlesWithResponseAsync(shareName, filePath, "*", null, marker,
                    snapshot, context), timeout)
                .map(response -> new PagedResponseBase<>(response.getRequest(),
                    response.getStatusCode(),
                    response.getHeaders(),
                    Collections.singletonList(
                        new CloseHandlesInfo(response.getDeserializedHeaders().getXMsNumberOfHandlesClosed(),
                            response.getDeserializedHeaders().getXMsNumberOfHandlesFailed())),
                    response.getDeserializedHeaders().getXMsMarker(),
                    response.getDeserializedHeaders()));

        return new PagedFlux<>(() -> retriever.apply(null), retriever);
    }

    /**
     * Get snapshot id which attached to {@link ShareFileAsyncClient}. Return {@code null} if no snapshot id attached.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Get the share snapshot id. </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.getShareSnapshotId}
     *
     * @return The snapshot id which is a unique {@code DateTime} value that identifies the share snapshot to its base
     * share.
     */
    public String getShareSnapshotId() {
        return this.snapshot;
    }

    /**
     * Get the share name of file client.
     *
     * <p>Get the share name. </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.getShareName}
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
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.getFilePath}
     *
     * @return The path of the file.
     */
    public String getFilePath() {
        return filePath;
    }


    /**
     * Get associated account name.
     *
     * @return account name associated with this storage resource.
     */
    public String getAccountName() {
        return this.accountName;
    }

    /**
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return The pipeline.
     */
    public HttpPipeline getHttpPipeline() {
        return azureFileStorageClient.getHttpPipeline();
    }

    /**
     * Generates a service SAS for the file using the specified {@link ShareServiceSasSignatureValues}
     * <p>Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link ShareServiceSasSignatureValues} for more information on how to construct a service SAS.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.generateSas#ShareServiceSasSignatureValues}
     *
     * @param shareServiceSasSignatureValues {@link ShareServiceSasSignatureValues}
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateSas(ShareServiceSasSignatureValues shareServiceSasSignatureValues) {
        return generateSas(shareServiceSasSignatureValues, Context.NONE);
    }

    /**
     * Generates a service SAS for the file using the specified {@link ShareServiceSasSignatureValues}
     * <p>Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link ShareServiceSasSignatureValues} for more information on how to construct a service SAS.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.generateSas#ShareServiceSasSignatureValues-Context}
     *
     * @param shareServiceSasSignatureValues {@link ShareServiceSasSignatureValues}
     * @param context Additional context that is passed through the code when generating a SAS.
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateSas(ShareServiceSasSignatureValues shareServiceSasSignatureValues, Context context) {
        return new ShareSasImplUtil(shareServiceSasSignatureValues, getShareName(), getFilePath())
            .generateSas(SasImplUtils.extractSharedKeyCredential(getHttpPipeline()), context);
    }

    private Response<ShareFileInfo> createFileInfoResponse(final FilesCreateResponse response) {
        String eTag = response.getDeserializedHeaders().getETag();
        OffsetDateTime lastModified = response.getDeserializedHeaders().getLastModified();
        boolean isServerEncrypted = response.getDeserializedHeaders().isXMsRequestServerEncrypted();
        FileSmbProperties smbProperties = new FileSmbProperties(response.getHeaders());
        ShareFileInfo shareFileInfo = new ShareFileInfo(eTag, lastModified, isServerEncrypted, smbProperties);
        return new SimpleResponse<>(response, shareFileInfo);
    }

    private Response<ShareFileInfo> setPropertiesResponse(final FilesSetHttpHeadersResponse response) {
        String eTag = response.getDeserializedHeaders().getETag();
        OffsetDateTime lastModified = response.getDeserializedHeaders().getLastModified();
        boolean isServerEncrypted = response.getDeserializedHeaders().isXMsRequestServerEncrypted();
        FileSmbProperties smbProperties = new FileSmbProperties(response.getHeaders());
        ShareFileInfo shareFileInfo = new ShareFileInfo(eTag, lastModified, isServerEncrypted, smbProperties);
        return new SimpleResponse<>(response, shareFileInfo);
    }

    private Response<ShareFileProperties> getPropertiesResponse(final FilesGetPropertiesResponse response) {
        FilesGetPropertiesHeaders headers = response.getDeserializedHeaders();
        String eTag = headers.getETag();
        OffsetDateTime lastModified = headers.getLastModified();
        Map<String, String> metadata = headers.getXMsMeta();
        String fileType = headers.getXMsType();
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
        LeaseStatusType leaseStatusType = headers.getXMsLeaseStatus();
        LeaseStateType leaseStateType = headers.getXMsLeaseState();
        LeaseDurationType leaseDurationType = headers.getXMsLeaseDuration();
        OffsetDateTime copyCompletionTime = headers.getXMsCopyCompletionTime();
        String copyStatusDescription = headers.getXMsCopyStatusDescription();
        String copyId = headers.getXMsCopyId();
        String copyProgress = headers.getXMsCopyProgress();
        String copySource = headers.getXMsCopySource();
        CopyStatusType copyStatus = headers.getXMsCopyStatus();
        Boolean isServerEncrpted = headers.isXMsServerEncrypted();
        FileSmbProperties smbProperties = new FileSmbProperties(response.getHeaders());
        ShareFileProperties shareFileProperties = new ShareFileProperties(eTag, lastModified, metadata, fileType,
            contentLength, contentType, contentMD5, contentEncoding, cacheControl, contentDisposition,
            leaseStatusType, leaseStateType, leaseDurationType, copyCompletionTime, copyStatusDescription, copyId,
            copyProgress, copySource, copyStatus, isServerEncrpted, smbProperties);
        return new SimpleResponse<>(response, shareFileProperties);
    }

    private Response<ShareFileUploadInfo> uploadResponse(final FilesUploadRangeResponse response) {
        FilesUploadRangeHeaders headers = response.getDeserializedHeaders();
        String eTag = headers.getETag();
        OffsetDateTime lastModified = headers.getLastModified();
        byte[] contentMD5;
        try {
            contentMD5 = headers.getContentMD5();
        } catch (NullPointerException e) {
            contentMD5 = null;
        }
        Boolean isServerEncrypted = headers.isXMsRequestServerEncrypted();
        ShareFileUploadInfo shareFileUploadInfo = new ShareFileUploadInfo(eTag, lastModified, contentMD5,
            isServerEncrypted);
        return new SimpleResponse<>(response, shareFileUploadInfo);
    }

    private Response<ShareFileUploadRangeFromUrlInfo> uploadRangeFromUrlResponse(
        final FilesUploadRangeFromURLResponse response) {
        FilesUploadRangeFromURLHeaders headers = response.getDeserializedHeaders();
        String eTag = headers.getETag();
        OffsetDateTime lastModified = headers.getLastModified();
        Boolean isServerEncrypted = headers.isXMsRequestServerEncrypted();
        ShareFileUploadRangeFromUrlInfo shareFileUploadRangeFromUrlInfo =
            new ShareFileUploadRangeFromUrlInfo(eTag, lastModified, isServerEncrypted);
        return new SimpleResponse<>(response, shareFileUploadRangeFromUrlInfo);
    }

    private Response<ShareFileMetadataInfo> setMetadataResponse(final FilesSetMetadataResponse response) {
        String eTag = response.getDeserializedHeaders().getETag();
        Boolean isServerEncrypted = response.getDeserializedHeaders().isXMsRequestServerEncrypted();
        ShareFileMetadataInfo shareFileMetadataInfo = new ShareFileMetadataInfo(eTag, isServerEncrypted);
        return new SimpleResponse<>(response, shareFileMetadataInfo);
    }

    /**
     * Verifies that the file permission and file permission key are not both set and if the file permission is set,
     * the file permission is of valid length.
     * @param filePermission The file permission.
     * @param filePermissionKey The file permission key.
     * @throws IllegalArgumentException for invalid file permission or file permission keys.
     */
    private void validateFilePermissionAndKey(String filePermission, String  filePermissionKey) {
        if (filePermission != null && filePermissionKey != null) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                FileConstants.MessageConstants.FILE_PERMISSION_FILE_PERMISSION_KEY_INVALID));
        }

        if (filePermission != null) {
            StorageImplUtils.assertInBounds("filePermission",
                filePermission.getBytes(StandardCharsets.UTF_8).length, 0, 8 * Constants.KB);
        }
    }
}
