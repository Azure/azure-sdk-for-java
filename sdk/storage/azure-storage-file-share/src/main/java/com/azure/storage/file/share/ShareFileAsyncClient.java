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
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.http.rest.StreamResponse;
import com.azure.core.util.Context;
import com.azure.core.util.Contexts;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.ProgressListener;
import com.azure.core.util.ProgressReporter;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.storage.common.ParallelTransferOptions;
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
import com.azure.storage.file.share.implementation.models.DestinationLeaseAccessConditions;
import com.azure.storage.file.share.implementation.models.FilesCreateHeaders;
import com.azure.storage.file.share.implementation.models.FilesGetPropertiesHeaders;
import com.azure.storage.file.share.implementation.models.FilesSetHttpHeadersHeaders;
import com.azure.storage.file.share.implementation.models.FilesSetMetadataHeaders;
import com.azure.storage.file.share.implementation.models.FilesStartCopyHeaders;
import com.azure.storage.file.share.implementation.models.FilesUploadRangeFromURLHeaders;
import com.azure.storage.file.share.implementation.models.FilesUploadRangeHeaders;
import com.azure.storage.file.share.implementation.models.ShareFileRangeWriteType;
import com.azure.storage.file.share.implementation.models.SourceLeaseAccessConditions;
import com.azure.storage.file.share.implementation.util.ModelHelper;
import com.azure.storage.file.share.implementation.util.ShareSasImplUtil;
import com.azure.storage.file.share.models.CloseHandlesInfo;
import com.azure.storage.file.share.models.CopyStatusType;
import com.azure.storage.file.share.models.CopyableFileSmbPropertiesList;
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
import com.azure.storage.file.share.models.ShareFileUploadInfo;
import com.azure.storage.file.share.models.ShareFileUploadOptions;
import com.azure.storage.file.share.models.ShareFileUploadRangeFromUrlInfo;
import com.azure.storage.file.share.models.ShareFileUploadRangeOptions;
import com.azure.storage.file.share.models.ShareRequestConditions;
import com.azure.storage.file.share.models.ShareStorageException;
import com.azure.storage.file.share.options.ShareFileCopyOptions;
import com.azure.storage.file.share.options.ShareFileDownloadOptions;
import com.azure.storage.file.share.options.ShareFileListRangesDiffOptions;
import com.azure.storage.file.share.options.ShareFileRenameOptions;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

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
 * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.instantiation -->
 * <pre>
 * ShareFileAsyncClient client = new ShareFileClientBuilder&#40;&#41;
 *     .connectionString&#40;&quot;$&#123;connectionString&#125;&quot;&#41;
 *     .endpoint&#40;&quot;$&#123;endpoint&#125;&quot;&#41;
 *     .buildFileAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.instantiation -->
 *
 * <p>View {@link ShareFileClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see ShareFileClientBuilder
 * @see ShareFileClient
 * @see StorageSharedKeyCredential
 */
@ServiceClient(builder = ShareFileClientBuilder.class, isAsync = true)
public class ShareFileAsyncClient {
    private static final ClientLogger LOGGER = new ClientLogger(ShareFileAsyncClient.class);
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

    ShareFileAsyncClient(ShareFileAsyncClient fileAsyncClient) {
        this(fileAsyncClient.azureFileStorageClient, fileAsyncClient.shareName,
            Utility.urlEncode(fileAsyncClient.filePath), fileAsyncClient.snapshot, fileAsyncClient.accountName,
            fileAsyncClient.serviceVersion);
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
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.exists -->
     * <pre>
     * client.exists&#40;&#41;.subscribe&#40;response -&gt; System.out.printf&#40;&quot;Exists? %b%n&quot;, response&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.exists -->
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
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.existsWithResponse -->
     * <pre>
     * client.existsWithResponse&#40;&#41;.subscribe&#40;response -&gt; System.out.printf&#40;&quot;Exists? %b%n&quot;, response.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.existsWithResponse -->
     *
     * @return Flag indicating existence of the file.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Boolean>> existsWithResponse() {
        try {
            return withContext(this::existsWithResponse);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
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
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.create -->
     * <pre>
     * shareFileAsyncClient.create&#40;1024&#41;.subscribe&#40;
     *     response -&gt; &#123; &#125;,
     *     error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Complete creating the file!&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.create -->
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
        return createWithResponse(maxSize, null, null, null, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a file in the storage account and returns a response of ShareFileInfo to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the file with length of 1024 bytes, some headers, file smb properties and metadata.</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.createWithResponse#long-ShareFileHttpHeaders-FileSmbProperties-String-Map -->
     * <pre>
     * ShareFileHttpHeaders httpHeaders = new ShareFileHttpHeaders&#40;&#41;
     *     .setContentType&#40;&quot;text&#47;html&quot;&#41;
     *     .setContentEncoding&#40;&quot;gzip&quot;&#41;
     *     .setContentLanguage&#40;&quot;en&quot;&#41;
     *     .setCacheControl&#40;&quot;no-transform&quot;&#41;
     *     .setContentDisposition&#40;&quot;attachment&quot;&#41;;
     * FileSmbProperties smbProperties = new FileSmbProperties&#40;&#41;
     *     .setNtfsFileAttributes&#40;EnumSet.of&#40;NtfsFileAttributes.READ_ONLY&#41;&#41;
     *     .setFileCreationTime&#40;OffsetDateTime.now&#40;&#41;&#41;
     *     .setFileLastWriteTime&#40;OffsetDateTime.now&#40;&#41;&#41;
     *     .setFilePermissionKey&#40;&quot;filePermissionKey&quot;&#41;;
     * String filePermission = &quot;filePermission&quot;;
     * &#47;&#47; NOTE: filePermission and filePermissionKey should never be both set
     * shareFileAsyncClient.createWithResponse&#40;1024, httpHeaders, smbProperties, filePermission,
     *     Collections.singletonMap&#40;&quot;directory&quot;, &quot;metadata&quot;&#41;&#41;
     *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Creating the file completed with status code %d&quot;,
     *         response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.createWithResponse#long-ShareFileHttpHeaders-FileSmbProperties-String-Map -->
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
        return createWithResponse(maxSize, httpHeaders, smbProperties, filePermission, metadata, null);
    }

    /**
     * Creates a file in the storage account and returns a response of ShareFileInfo to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the file with length of 1024 bytes, some headers, file smb properties and metadata.</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.createWithResponse#long-ShareFileHttpHeaders-FileSmbProperties-String-Map-ShareRequestConditions -->
     * <pre>
     * ShareFileHttpHeaders httpHeaders = new ShareFileHttpHeaders&#40;&#41;
     *     .setContentType&#40;&quot;text&#47;html&quot;&#41;
     *     .setContentEncoding&#40;&quot;gzip&quot;&#41;
     *     .setContentLanguage&#40;&quot;en&quot;&#41;
     *     .setCacheControl&#40;&quot;no-transform&quot;&#41;
     *     .setContentDisposition&#40;&quot;attachment&quot;&#41;;
     * FileSmbProperties smbProperties = new FileSmbProperties&#40;&#41;
     *     .setNtfsFileAttributes&#40;EnumSet.of&#40;NtfsFileAttributes.READ_ONLY&#41;&#41;
     *     .setFileCreationTime&#40;OffsetDateTime.now&#40;&#41;&#41;
     *     .setFileLastWriteTime&#40;OffsetDateTime.now&#40;&#41;&#41;
     *     .setFilePermissionKey&#40;&quot;filePermissionKey&quot;&#41;;
     * String filePermission = &quot;filePermission&quot;;
     * &#47;&#47; NOTE: filePermission and filePermissionKey should never be both set
     *
     * ShareRequestConditions requestConditions = new ShareRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     *
     * shareFileAsyncClient.createWithResponse&#40;1024, httpHeaders, smbProperties, filePermission,
     *     Collections.singletonMap&#40;&quot;directory&quot;, &quot;metadata&quot;&#41;, requestConditions&#41;
     *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Creating the file completed with status code %d&quot;,
     *         response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.createWithResponse#long-ShareFileHttpHeaders-FileSmbProperties-String-Map-ShareRequestConditions -->
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
            return monoError(LOGGER, ex);
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
        String fileChangeTime = smbProperties.getFileChangeTimeString();

        return azureFileStorageClient.getFiles()
            .createWithResponseAsync(shareName, filePath, maxSize, fileAttributes, null, metadata, filePermission,
                filePermissionKey, fileCreationTime, fileLastWriteTime, fileChangeTime, requestConditions.getLeaseId(),
                httpHeaders, context)
            .map(ShareFileAsyncClient::createFileInfoResponse);
    }

    /**
     * Copies a blob or file to a destination file within the storage account.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Copy file from source url to the {@code resourcePath} </p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.beginCopy#string-map-duration -->
     * <pre>
     * PollerFlux&lt;ShareFileCopyInfo, Void&gt; poller = shareFileAsyncClient.beginCopy&#40;
     *     &quot;https:&#47;&#47;&#123;accountName&#125;.file.core.windows.net?&#123;SASToken&#125;&quot;,
     *     Collections.singletonMap&#40;&quot;file&quot;, &quot;metadata&quot;&#41;, Duration.ofSeconds&#40;2&#41;&#41;;
     *
     * poller.subscribe&#40;response -&gt; &#123;
     *     final ShareFileCopyInfo value = response.getValue&#40;&#41;;
     *     System.out.printf&#40;&quot;Copy source: %s. Status: %s.%n&quot;, value.getCopySourceUrl&#40;&#41;, value.getCopyStatus&#40;&#41;&#41;;
     * &#125;, error -&gt; System.err.println&#40;&quot;Error: &quot; + error&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Complete copying the file.&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.beginCopy#string-map-duration -->
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
        ShareFileCopyOptions options = new ShareFileCopyOptions().setMetadata(metadata);
        return beginCopy(sourceUrl, options, pollInterval);
    }

    /**
     * Copies a blob or file to a destination file within the storage account.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Copy file from source url to the {@code resourcePath} </p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.beginCopy#string-filesmbproperties-string-permissioncopymodetype-boolean-boolean-map-duration-ShareRequestConditions -->
     * <pre>
     * FileSmbProperties smbProperties = new FileSmbProperties&#40;&#41;
     *     .setNtfsFileAttributes&#40;EnumSet.of&#40;NtfsFileAttributes.READ_ONLY&#41;&#41;
     *     .setFileCreationTime&#40;OffsetDateTime.now&#40;&#41;&#41;
     *     .setFileLastWriteTime&#40;OffsetDateTime.now&#40;&#41;&#41;
     *     .setFilePermissionKey&#40;&quot;filePermissionKey&quot;&#41;;
     * String filePermission = &quot;filePermission&quot;;
     * &#47;&#47; NOTE: filePermission and filePermissionKey should never be both set
     * boolean ignoreReadOnly = false; &#47;&#47; Default value
     * boolean setArchiveAttribute = true; &#47;&#47; Default value
     * ShareRequestConditions requestConditions = new ShareRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     *
     * PollerFlux&lt;ShareFileCopyInfo, Void&gt; poller = shareFileAsyncClient.beginCopy&#40;
     *     &quot;https:&#47;&#47;&#123;accountName&#125;.file.core.windows.net?&#123;SASToken&#125;&quot;,
     *     smbProperties, filePermission, PermissionCopyModeType.SOURCE, ignoreReadOnly, setArchiveAttribute,
     *     Collections.singletonMap&#40;&quot;file&quot;, &quot;metadata&quot;&#41;, Duration.ofSeconds&#40;2&#41;, requestConditions&#41;;
     *
     * poller.subscribe&#40;response -&gt; &#123;
     *     final ShareFileCopyInfo value = response.getValue&#40;&#41;;
     *     System.out.printf&#40;&quot;Copy source: %s. Status: %s.%n&quot;, value.getCopySourceUrl&#40;&#41;, value.getCopyStatus&#40;&#41;&#41;;
     * &#125;, error -&gt; System.err.println&#40;&quot;Error: &quot; + error&#41;, &#40;&#41; -&gt; System.out.println&#40;&quot;Complete copying the file.&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.beginCopy#string-filesmbproperties-string-permissioncopymodetype-boolean-boolean-map-duration-ShareRequestConditions -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/copy-file">Azure Docs</a>.</p>
     *
     * @param sourceUrl Specifies the URL of the source file or blob, up to 2 KB in length.
     * @param smbProperties The user settable file smb properties.
     * @param filePermission The file permission of the file.
     * @param filePermissionCopyMode Mode of file permission acquisition.
     * @param ignoreReadOnly Whether to copy despite target being read only. (default is false)
     * @param setArchiveAttribute Whether the archive attribute is to be set on the target. (default is true)
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
        ShareFileCopyOptions options = new ShareFileCopyOptions()
            .setSmbProperties(smbProperties)
            .setFilePermission(filePermission)
            .setPermissionCopyModeType(filePermissionCopyMode)
            .setIgnoreReadOnly(ignoreReadOnly)
            .setArchiveAttribute(setArchiveAttribute)
            .setMetadata(metadata)
            .setDestinationRequestConditions(destinationRequestConditions);

        return beginCopy(sourceUrl, options, pollInterval);
    }

    /**
     * Copies a blob or file to a destination file within the storage account.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Copy file from source url to the {@code resourcePath} </p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.beginCopy#String-Duration-ShareFileCopyOptions -->
     * <pre>
     * FileSmbProperties smbProperties = new FileSmbProperties&#40;&#41;
     *     .setNtfsFileAttributes&#40;EnumSet.of&#40;NtfsFileAttributes.READ_ONLY&#41;&#41;
     *     .setFileCreationTime&#40;OffsetDateTime.now&#40;&#41;&#41;
     *     .setFileLastWriteTime&#40;OffsetDateTime.now&#40;&#41;&#41;
     *     .setFilePermissionKey&#40;&quot;filePermissionKey&quot;&#41;;
     * String filePermission = &quot;filePermission&quot;;
     * &#47;&#47; NOTE: filePermission and filePermissionKey should never be both set
     * boolean ignoreReadOnly = false; &#47;&#47; Default value
     * boolean setArchiveAttribute = true; &#47;&#47; Default value
     * ShareRequestConditions requestConditions = new ShareRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     * CopyableFileSmbPropertiesList list = new CopyableFileSmbPropertiesList&#40;&#41;.setCreatedOn&#40;true&#41;.setLastWrittenOn&#40;true&#41;;
     * &#47;&#47; NOTE: FileSmbProperties and CopyableFileSmbPropertiesList should never be both set
     *
     * ShareFileCopyOptions options = new ShareFileCopyOptions&#40;&#41;
     *     .setSmbProperties&#40;smbProperties&#41;
     *     .setFilePermission&#40;filePermission&#41;
     *     .setIgnoreReadOnly&#40;ignoreReadOnly&#41;
     *     .setArchiveAttribute&#40;setArchiveAttribute&#41;
     *     .setDestinationRequestConditions&#40;requestConditions&#41;
     *     .setSmbPropertiesToCopy&#40;list&#41;
     *     .setPermissionCopyModeType&#40;PermissionCopyModeType.SOURCE&#41;
     *     .setMetadata&#40;Collections.singletonMap&#40;&quot;file&quot;, &quot;metadata&quot;&#41;&#41;;
     *
     * PollerFlux&lt;ShareFileCopyInfo, Void&gt; poller = shareFileAsyncClient.beginCopy&#40;
     *     &quot;https:&#47;&#47;&#123;accountName&#125;.file.core.windows.net?&#123;SASToken&#125;&quot;, options, Duration.ofSeconds&#40;2&#41;&#41;;
     *
     * poller.subscribe&#40;response -&gt; &#123;
     *     final ShareFileCopyInfo value = response.getValue&#40;&#41;;
     *     System.out.printf&#40;&quot;Copy source: %s. Status: %s.%n&quot;, value.getCopySourceUrl&#40;&#41;, value.getCopyStatus&#40;&#41;&#41;;
     * &#125;, error -&gt; System.err.println&#40;&quot;Error: &quot; + error&#41;, &#40;&#41; -&gt; System.out.println&#40;&quot;Complete copying the file.&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.beginCopy#String-Duration-ShareFileCopyOptions -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/copy-file">Azure Docs</a>.</p>
     *
     * @param sourceUrl Specifies the URL of the source file or blob, up to 2 KB in length.
     * @param pollInterval Duration between each poll for the copy status. If none is specified, a default of one second
     * is used.
     * @param options {@link ShareFileCopyOptions}
     * @return A {@link PollerFlux} that polls the file copy operation until it has completed or has been cancelled.
     * @see <a href="https://docs.microsoft.com/dotnet/csharp/language-reference/">C# identifiers</a>
     */
    public PollerFlux<ShareFileCopyInfo, Void> beginCopy(String sourceUrl, ShareFileCopyOptions options, Duration pollInterval) {

        final ShareRequestConditions finalRequestConditions =
            options.getDestinationRequestConditions() == null ? new ShareRequestConditions() : options.getDestinationRequestConditions();
        final AtomicReference<String> copyId = new AtomicReference<>();
        final Duration interval = pollInterval == null ? Duration.ofSeconds(1) : pollInterval;

        FileSmbProperties tempSmbProperties = options.getSmbProperties() == null ? new FileSmbProperties() : options.getSmbProperties();

        String filePermissionKey = tempSmbProperties.getFilePermissionKey();

        if (options.getFilePermission() == null || options.getPermissionCopyModeType() == PermissionCopyModeType.SOURCE) {
            if ((options.getFilePermission() != null || filePermissionKey != null) && options.getPermissionCopyModeType() != PermissionCopyModeType.OVERRIDE) {
                return PollerFlux.error(LOGGER.logExceptionAsError(new IllegalArgumentException(
                    "File permission and file permission key can not be set when PermissionCopyModeType is source or "
                        + "null")));
            }
        } else if (options.getPermissionCopyModeType() == PermissionCopyModeType.OVERRIDE) {
            // Checks that file permission and file permission key are valid
            try {
                validateFilePermissionAndKey(options.getFilePermission(), tempSmbProperties.getFilePermissionKey());
            } catch (RuntimeException ex) {
                return PollerFlux.error(LOGGER.logExceptionAsError(ex));
            }
        }

        // check if only copy flag or smb properties are set (not both)
        CopyableFileSmbPropertiesList list = options.getSmbPropertiesToCopy()  == null ? new CopyableFileSmbPropertiesList() : options.getSmbPropertiesToCopy();
        if (list.isFileAttributes() && tempSmbProperties.getNtfsFileAttributes() != null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Both CopyableFileSmbPropertiesList.isSetFileAttributes and smbProperties.ntfsFileAttributes cannot be set."));
        }
        if (list.isCreatedOn() && tempSmbProperties.getFileCreationTime() != null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Both CopyableFileSmbPropertiesList.isSetCreatedOn and smbProperties.fileCreationTime cannot be set."));
        }
        if (list.isLastWrittenOn() && tempSmbProperties.getFileLastWriteTime() != null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Both CopyableFileSmbPropertiesList.isSetLastWrittenOn and smbProperties.fileLastWriteTime cannot be set."));
        }
        if (list.isChangedOn() && tempSmbProperties.getFileChangeTime() != null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Both CopyableFileSmbPropertiesList.isSetChangedOn and smbProperties.fileChangeTime cannot be set."));
        }

        String fileAttributes = list.isFileAttributes() ? FileConstants.COPY_SOURCE : NtfsFileAttributes.toString(tempSmbProperties.getNtfsFileAttributes());
        String fileCreationTime = list.isCreatedOn()  ? FileConstants.COPY_SOURCE : FileSmbProperties.parseFileSMBDate(tempSmbProperties.getFileCreationTime());
        String fileLastWriteTime = list.isLastWrittenOn() ? FileConstants.COPY_SOURCE : FileSmbProperties.parseFileSMBDate(tempSmbProperties.getFileLastWriteTime());
        String fileChangedOnTime = list.isChangedOn() ? FileConstants.COPY_SOURCE : FileSmbProperties.parseFileSMBDate(tempSmbProperties.getFileChangeTime());

        final CopyFileSmbInfo copyFileSmbInfo = new CopyFileSmbInfo()
            .setFilePermissionCopyMode(options.getPermissionCopyModeType())
            .setFileAttributes(fileAttributes)
            .setFileCreationTime(fileCreationTime)
            .setFileLastWriteTime(fileLastWriteTime)
            .setFileChangeTime(fileChangedOnTime)
            .setIgnoreReadOnly(options.isIgnoreReadOnly())
            .setSetArchiveAttribute(options.isArchiveAttributeSet());

        final String copySource = Utility.encodeUrlPath(sourceUrl);

        return new PollerFlux<>(interval,
            (pollingContext) -> {
                try {
                    return withContext(context -> azureFileStorageClient.getFiles()
                        .startCopyWithResponseAsync(shareName, filePath, copySource, null,
                            options.getMetadata(), options.getFilePermission(), tempSmbProperties.getFilePermissionKey(),
                            finalRequestConditions.getLeaseId(), copyFileSmbInfo, context))
                        .map(response -> {
                            final FilesStartCopyHeaders headers = response.getDeserializedHeaders();
                            copyId.set(headers.getXMsCopyId());

                            return new ShareFileCopyInfo(sourceUrl, headers.getXMsCopyId(), headers.getXMsCopyStatus(),
                                headers.getETag(), headers.getLastModified(),
                                response.getHeaders().getValue("x-ms-error-code"));
                        });
                } catch (RuntimeException ex) {
                    return monoError(LOGGER, ex);
                }
            },
            (pollingContext) -> {
                try {
                    return onPoll(pollingContext.getLatestResponse(), finalRequestConditions);
                } catch (RuntimeException ex) {
                    return monoError(LOGGER, ex);
                }
            },
            (pollingContext, firstResponse) -> {
                if (firstResponse == null || firstResponse.getValue() == null) {
                    return Mono.error(LOGGER.logExceptionAsError(
                        new IllegalArgumentException("Cannot cancel a poll response that never started.")));
                }
                final String copyIdentifier = firstResponse.getValue().getCopyId();
                if (!CoreUtils.isNullOrEmpty(copyIdentifier)) {
                    LOGGER.info("Cancelling copy operation for copy id: {}", copyIdentifier);
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
            LOGGER.warning("ShareFileCopyInfo does not exist. Activation operation failed.");
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
                        throw LOGGER.logExceptionAsError(new IllegalArgumentException(
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
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.abortCopy#string -->
     * <pre>
     * shareFileAsyncClient.abortCopy&#40;&quot;someCopyId&quot;&#41;
     *     .doOnSuccess&#40;response -&gt; System.out.println&#40;&quot;Abort copying the file completed.&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.abortCopy#string -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/abort-copy-file">Azure Docs</a>.</p>
     *
     * @param copyId Specifies the copy id which has copying pending status associate with it.
     * @return An empty response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
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
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.abortCopyWithResponse#string -->
     * <pre>
     * shareFileAsyncClient.abortCopyWithResponse&#40;&quot;someCopyId&quot;&#41;
     *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Abort copying the file completed with status code %d&quot;,
     *         response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.abortCopyWithResponse#string -->
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
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.abortCopyWithResponse#string-ShareRequestConditions -->
     * <pre>
     * ShareRequestConditions requestConditions = new ShareRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     * shareFileAsyncClient.abortCopyWithResponse&#40;&quot;someCopyId&quot;, requestConditions&#41;
     *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Abort copying the file completed with status code %d&quot;,
     *         response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.abortCopyWithResponse#string-ShareRequestConditions -->
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
            return withContext(context -> abortCopyWithResponse(copyId, requestConditions, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
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
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.downloadToFile#string -->
     * <pre>
     * shareFileAsyncClient.downloadToFile&#40;&quot;somelocalfilepath&quot;&#41;.subscribe&#40;
     *     response -&gt; &#123;
     *         if &#40;Files.exists&#40;Paths.get&#40;&quot;somelocalfilepath&quot;&#41;&#41;&#41; &#123;
     *             System.out.println&#40;&quot;Successfully downloaded the file.&quot;&#41;;
     *         &#125;
     *     &#125;,
     *     error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Complete downloading the file!&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.downloadToFile#string -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-file">Azure Docs</a>.</p>
     *
     * @param downloadFilePath The path where store the downloaded file
     * @return An empty response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ShareFileProperties> downloadToFile(String downloadFilePath) {
        return downloadToFileWithResponse(downloadFilePath, null).flatMap(FluxUtil::toMono);
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
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.downloadToFileWithResponse#string-ShareFileRange -->
     * <pre>
     * shareFileAsyncClient.downloadToFileWithResponse&#40;&quot;somelocalfilepath&quot;, new ShareFileRange&#40;1024, 2047L&#41;&#41;
     *     .subscribe&#40;
     *         response -&gt; &#123;
     *             if &#40;Files.exists&#40;Paths.get&#40;&quot;somelocalfilepath&quot;&#41;&#41;&#41; &#123;
     *                 System.out.println&#40;&quot;Successfully downloaded the file with status code &quot;
     *                     + response.getStatusCode&#40;&#41;&#41;;
     *             &#125;
     *         &#125;,
     *         error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *         &#40;&#41; -&gt; System.out.println&#40;&quot;Complete downloading the file!&quot;&#41;
     *     &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.downloadToFileWithResponse#string-ShareFileRange -->
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
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.downloadToFileWithResponse#string-ShareFileRange-ShareRequestConditions -->
     * <pre>
     * ShareRequestConditions requestConditions = new ShareRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     * shareFileAsyncClient.downloadToFileWithResponse&#40;&quot;somelocalfilepath&quot;, new ShareFileRange&#40;1024, 2047L&#41;,
     *     requestConditions&#41;
     *     .subscribe&#40;
     *         response -&gt; &#123;
     *             if &#40;Files.exists&#40;Paths.get&#40;&quot;somelocalfilepath&quot;&#41;&#41;&#41; &#123;
     *                 System.out.println&#40;&quot;Successfully downloaded the file with status code &quot;
     *                     + response.getStatusCode&#40;&#41;&#41;;
     *             &#125;
     *         &#125;,
     *         error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *         &#40;&#41; -&gt; System.out.println&#40;&quot;Complete downloading the file!&quot;&#41;
     *     &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.downloadToFileWithResponse#string-ShareFileRange-ShareRequestConditions -->
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
            return monoError(LOGGER, ex);
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
        return Mono.justOrEmpty(range).switchIfEmpty(Mono.defer(() -> Mono.just(new ShareFileRange(0, response.getValue()
            .getContentLength()))))
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
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(fbb -> FluxUtil
                    .writeFile(fbb, channel, chunk.getStart() - (range == null ? 0 : range.getStart()))
                    .subscribeOn(Schedulers.boundedElastic())
                    .timeout(Duration.ofSeconds(DOWNLOAD_UPLOAD_CHUNK_TIMEOUT))
                    .retryWhen(Retry.max(3).filter(throwable -> throwable instanceof IOException
                        || throwable instanceof TimeoutException))))
            .then(Mono.just(response));
    }

    private AsynchronousFileChannel channelSetup(String filePath, OpenOption... options) {
        try {
            return AsynchronousFileChannel.open(Paths.get(filePath), options);
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    private void channelCleanUp(AsynchronousFileChannel channel) {
        try {
            channel.close();
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(Exceptions.propagate(new UncheckedIOException(e)));
        }
    }

    /**
     * Downloads a file from the system, including its metadata and properties
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Download the file with its metadata and properties. </p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.download -->
     * <pre>
     * shareFileAsyncClient.download&#40;&#41;.subscribe&#40;
     *     response -&gt; &#123; &#125;,
     *     error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Complete downloading the data!&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.download -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-file">Azure Docs</a>.</p>
     *
     * @return A reactive response containing the file data.
     */
    public Flux<ByteBuffer> download() {
        return downloadWithResponse(null).flatMapMany(ShareFileDownloadAsyncResponse::getValue);
    }

    /**
     * Downloads a file from the system, including its metadata and properties
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Download the file from 1024 to 2048 bytes with its metadata and properties and without the contentMD5. </p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.downloadWithResponse#ShareFileRange-Boolean -->
     * <pre>
     * shareFileAsyncClient.downloadWithResponse&#40;new ShareFileRange&#40;1024, 2047L&#41;, false&#41;
     *     .subscribe&#40;response -&gt;
     *             System.out.printf&#40;&quot;Complete downloading the data with status code %d%n&quot;, response.getStatusCode&#40;&#41;&#41;,
     *         error -&gt; System.err.println&#40;error.getMessage&#40;&#41;&#41;
     *     &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.downloadWithResponse#ShareFileRange-Boolean -->
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
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.downloadWithResponse#ShareFileRange-Boolean-ShareRequestConditions -->
     * <pre>
     * ShareRequestConditions requestConditions = new ShareRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     * shareFileAsyncClient.downloadWithResponse&#40;new ShareFileRange&#40;1024, 2047L&#41;, false, requestConditions&#41;
     *     .subscribe&#40;response -&gt;
     *             System.out.printf&#40;&quot;Complete downloading the data with status code %d%n&quot;, response.getStatusCode&#40;&#41;&#41;,
     *         error -&gt; System.err.println&#40;error.getMessage&#40;&#41;&#41;
     *     &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.downloadWithResponse#ShareFileRange-Boolean-ShareRequestConditions -->
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
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.downloadWithResponse#ShareFileDownloadOptions -->
     * <pre>
     * ShareRequestConditions requestConditions = new ShareRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     * ShareFileRange range = new ShareFileRange&#40;1024, 2047L&#41;;
     * DownloadRetryOptions retryOptions = new DownloadRetryOptions&#40;&#41;.setMaxRetryRequests&#40;3&#41;;
     * ShareFileDownloadOptions options = new ShareFileDownloadOptions&#40;&#41;.setRange&#40;range&#41;
     *     .setRequestConditions&#40;requestConditions&#41;
     *     .setRangeContentMd5Requested&#40;false&#41;
     *     .setRetryOptions&#40;retryOptions&#41;;
     * shareFileAsyncClient.downloadWithResponse&#40;options&#41;
     *     .subscribe&#40;response -&gt;
     *             System.out.printf&#40;&quot;Complete downloading the data with status code %d%n&quot;, response.getStatusCode&#40;&#41;&#41;,
     *         error -&gt; System.err.println&#40;error.getMessage&#40;&#41;&#41;
     *     &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.downloadWithResponse#ShareFileDownloadOptions -->
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
            return monoError(LOGGER, ex);
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
                            LOGGER.warning("Exception encountered in ReliableDownload after all data read from the network but "
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
                ).switchIfEmpty(Flux.defer(() -> Flux.just(ByteBuffer.wrap(new byte[0]))));

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
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.delete -->
     * <pre>
     * shareFileAsyncClient.delete&#40;&#41;.subscribe&#40;
     *     response -&gt; &#123; &#125;,
     *     error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Complete deleting the file!&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.delete -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-file2">Azure Docs</a>.</p>
     *
     * @return An empty response
     * @throws ShareStorageException If the directory doesn't exist or the file doesn't exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
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
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.deleteWithResponse -->
     * <pre>
     * shareFileAsyncClient.deleteWithResponse&#40;&#41;.subscribe&#40;
     *     response -&gt; System.out.println&#40;&quot;Complete deleting the file with status code:&quot; + response.getStatusCode&#40;&#41;&#41;,
     *     error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.deleteWithResponse -->
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
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.deleteWithResponse#ShareRequestConditions -->
     * <pre>
     * ShareRequestConditions requestConditions = new ShareRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     * shareFileAsyncClient.deleteWithResponse&#40;requestConditions&#41;.subscribe&#40;
     *     response -&gt; System.out.println&#40;&quot;Complete deleting the file with status code:&quot; + response.getStatusCode&#40;&#41;&#41;,
     *     error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.deleteWithResponse#ShareRequestConditions -->
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
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Void>> deleteWithResponse(ShareRequestConditions requestConditions, Context context) {
        requestConditions = requestConditions == null ? new ShareRequestConditions() : requestConditions;
        return azureFileStorageClient.getFiles().deleteWithResponseAsync(shareName, filePath, null,
            requestConditions.getLeaseId(), context).map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Deletes the file associate with the client if it exists.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the file</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.deleteIfExists -->
     * <pre>
     * shareFileAsyncClient.deleteIfExists&#40;&#41;.subscribe&#40;deleted -&gt; &#123;
     *     if &#40;deleted&#41; &#123;
     *         System.out.println&#40;&quot;Successfully deleted.&quot;&#41;;
     *     &#125; else &#123;
     *         System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.deleteIfExists -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-file2">Azure Docs</a>.</p>
     *
     * @return a reactive response signaling completion. {@code true} indicates that the file was successfully
     * deleted, {@code false} indicates that the file did not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> deleteIfExists() {
        return deleteIfExistsWithResponse(null).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes the file associate with the client if it does not exist.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the file</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.deleteIfExistsWithResponse#ShareRequestConditions -->
     * <pre>
     * ShareRequestConditions requestConditions = new ShareRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     * shareFileAsyncClient.deleteIfExistsWithResponse&#40;requestConditions&#41;.subscribe&#40;response -&gt; &#123;
     *     if &#40;response.getStatusCode&#40;&#41; == 404&#41; &#123;
     *         System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     *     &#125; else &#123;
     *         System.out.println&#40;&quot;successfully deleted.&quot;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.deleteIfExistsWithResponse#ShareRequestConditions -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-file2">Azure Docs</a>.</p>
     *
     * @param requestConditions {@link ShareRequestConditions}
     * @return A reactive response signaling completion. If {@link Response}'s status code is 202, the file was
     * successfully deleted. If status code is 404, the file does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Boolean>> deleteIfExistsWithResponse(ShareRequestConditions requestConditions) {
        try {
            return withContext(context -> this.deleteIfExistsWithResponse(requestConditions, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Boolean>> deleteIfExistsWithResponse(ShareRequestConditions requestConditions, Context context) {
        try {
            requestConditions = requestConditions == null ? new ShareRequestConditions() : requestConditions;
            return deleteWithResponse(requestConditions, context)
                .map(response -> (Response<Boolean>) new SimpleResponse<>(response, true))
                .onErrorResume(t -> t instanceof ShareStorageException && ((ShareStorageException) t).getStatusCode() == 404,
                    t -> {
                        HttpResponse response = ((ShareStorageException) t).getResponse();
                        return Mono.just(new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                            response.getHeaders(), false));
                    });
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Retrieves the properties of the storage account's file. The properties include file metadata, last modified
     * date, is server encrypted, and eTag.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve file properties</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.getProperties -->
     * <pre>
     * shareFileAsyncClient.getProperties&#40;&#41;
     *     .subscribe&#40;properties -&gt; &#123;
     *         System.out.printf&#40;&quot;File latest modified date is %s.&quot;, properties.getLastModified&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.getProperties -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-file-properties">Azure Docs</a>.</p>
     *
     * @return {@link ShareFileProperties Storage file properties}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ShareFileProperties> getProperties() {
        return getPropertiesWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Retrieves the properties of the storage account's file. The properties include file metadata, last modified
     * date, is server encrypted, and eTag.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve file properties</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.getPropertiesWithResponse -->
     * <pre>
     * shareFileAsyncClient.getPropertiesWithResponse&#40;&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         ShareFileProperties properties = response.getValue&#40;&#41;;
     *         System.out.printf&#40;&quot;File latest modified date is %s.&quot;, properties.getLastModified&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.getPropertiesWithResponse -->
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
     * Retrieves the properties of the storage account's file. The properties include file metadata, last modified
     * date, is server encrypted, and eTag.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve file properties</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.getPropertiesWithResponse#ShareRequestConditions -->
     * <pre>
     * ShareRequestConditions requestConditions = new ShareRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     * shareFileAsyncClient.getPropertiesWithResponse&#40;requestConditions&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         ShareFileProperties properties = response.getValue&#40;&#41;;
     *         System.out.printf&#40;&quot;File latest modified date is %s.&quot;, properties.getLastModified&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.getPropertiesWithResponse#ShareRequestConditions -->
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
            return withContext(context -> this.getPropertiesWithResponse(requestConditions, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<ShareFileProperties>> getPropertiesWithResponse(ShareRequestConditions requestConditions,
        Context context) {
        requestConditions = requestConditions == null ? new ShareRequestConditions() : requestConditions;
        context = context == null ? Context.NONE : context;
        return azureFileStorageClient.getFiles()
            .getPropertiesWithResponseAsync(shareName, filePath, snapshot, null, requestConditions.getLeaseId(),
                context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(ShareFileAsyncClient::getPropertiesResponse);
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
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.setProperties#long-ShareFileHttpHeaders-FileSmbProperties-String -->
     * <pre>
     * ShareFileHttpHeaders httpHeaders = new ShareFileHttpHeaders&#40;&#41;
     *     .setContentType&#40;&quot;text&#47;html&quot;&#41;
     *     .setContentEncoding&#40;&quot;gzip&quot;&#41;
     *     .setContentLanguage&#40;&quot;en&quot;&#41;
     *     .setCacheControl&#40;&quot;no-transform&quot;&#41;
     *     .setContentDisposition&#40;&quot;attachment&quot;&#41;;
     * FileSmbProperties smbProperties = new FileSmbProperties&#40;&#41;
     *     .setNtfsFileAttributes&#40;EnumSet.of&#40;NtfsFileAttributes.READ_ONLY&#41;&#41;
     *     .setFileCreationTime&#40;OffsetDateTime.now&#40;&#41;&#41;
     *     .setFileLastWriteTime&#40;OffsetDateTime.now&#40;&#41;&#41;
     *     .setFilePermissionKey&#40;&quot;filePermissionKey&quot;&#41;;
     * String filePermission = &quot;filePermission&quot;;
     * &#47;&#47; NOTE: filePermission and filePermissionKey should never be both set
     * shareFileAsyncClient.setProperties&#40;1024, httpHeaders, smbProperties, filePermission&#41;
     *     .doOnSuccess&#40;response -&gt; System.out.println&#40;&quot;Setting the file properties completed.&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.setProperties#long-ShareFileHttpHeaders-FileSmbProperties-String -->
     *
     * <p>Clear the metadata of the file and preserve the SMB properties</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.setProperties#long-ShareFileHttpHeaders-FileSmbProperties-String.clearHttpHeaderspreserveSMBProperties -->
     * <pre>
     * shareFileAsyncClient.setProperties&#40;1024, null, null, null&#41;
     *     .subscribe&#40;response -&gt; System.out.println&#40;&quot;Setting the file httpHeaders completed.&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.setProperties#long-ShareFileHttpHeaders-FileSmbProperties-String.clearHttpHeaderspreserveSMBProperties -->
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
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.setPropertiesWithResponse#long-ShareFileHttpHeaders-FileSmbProperties-String -->
     * <pre>
     * ShareFileHttpHeaders httpHeaders = new ShareFileHttpHeaders&#40;&#41;
     *     .setContentType&#40;&quot;text&#47;html&quot;&#41;
     *     .setContentEncoding&#40;&quot;gzip&quot;&#41;
     *     .setContentLanguage&#40;&quot;en&quot;&#41;
     *     .setCacheControl&#40;&quot;no-transform&quot;&#41;
     *     .setContentDisposition&#40;&quot;attachment&quot;&#41;;
     * FileSmbProperties smbProperties = new FileSmbProperties&#40;&#41;
     *     .setNtfsFileAttributes&#40;EnumSet.of&#40;NtfsFileAttributes.READ_ONLY&#41;&#41;
     *     .setFileCreationTime&#40;OffsetDateTime.now&#40;&#41;&#41;
     *     .setFileLastWriteTime&#40;OffsetDateTime.now&#40;&#41;&#41;
     *     .setFilePermissionKey&#40;&quot;filePermissionKey&quot;&#41;;
     * String filePermission = &quot;filePermission&quot;;
     * &#47;&#47; NOTE: filePermission and filePermissionKey should never be both set
     * shareFileAsyncClient.setPropertiesWithResponse&#40;1024, httpHeaders, smbProperties, filePermission&#41;
     *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Setting the file properties completed with status code %d&quot;,
     *         response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.setPropertiesWithResponse#long-ShareFileHttpHeaders-FileSmbProperties-String -->
     *
     * <p>Clear the metadata of the file and preserve the SMB properties</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.setPropertiesWithResponse#long-ShareFileHttpHeaders-FileSmbProperties-String.clearHttpHeaderspreserveSMBProperties -->
     * <pre>
     * shareFileAsyncClient.setPropertiesWithResponse&#40;1024, null, null, null&#41;
     *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Setting the file httpHeaders completed with status code %d&quot;,
     *         response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.setPropertiesWithResponse#long-ShareFileHttpHeaders-FileSmbProperties-String.clearHttpHeaderspreserveSMBProperties -->
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
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.setPropertiesWithResponse#long-ShareFileHttpHeaders-FileSmbProperties-String-ShareRequestConditions -->
     * <pre>
     * ShareFileHttpHeaders httpHeaders = new ShareFileHttpHeaders&#40;&#41;
     *     .setContentType&#40;&quot;text&#47;html&quot;&#41;
     *     .setContentEncoding&#40;&quot;gzip&quot;&#41;
     *     .setContentLanguage&#40;&quot;en&quot;&#41;
     *     .setCacheControl&#40;&quot;no-transform&quot;&#41;
     *     .setContentDisposition&#40;&quot;attachment&quot;&#41;;
     * FileSmbProperties smbProperties = new FileSmbProperties&#40;&#41;
     *     .setNtfsFileAttributes&#40;EnumSet.of&#40;NtfsFileAttributes.READ_ONLY&#41;&#41;
     *     .setFileCreationTime&#40;OffsetDateTime.now&#40;&#41;&#41;
     *     .setFileLastWriteTime&#40;OffsetDateTime.now&#40;&#41;&#41;
     *     .setFilePermissionKey&#40;&quot;filePermissionKey&quot;&#41;;
     * String filePermission = &quot;filePermission&quot;;
     * &#47;&#47; NOTE: filePermission and filePermissionKey should never be both set
     * ShareRequestConditions requestConditions = new ShareRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     * shareFileAsyncClient.setPropertiesWithResponse&#40;1024, httpHeaders, smbProperties, filePermission, requestConditions&#41;
     *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Setting the file properties completed with status code %d&quot;,
     *         response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.setPropertiesWithResponse#long-ShareFileHttpHeaders-FileSmbProperties-String-ShareRequestConditions -->
     *
     * <p>Clear the metadata of the file and preserve the SMB properties</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.setPropertiesWithResponse#long-ShareFileHttpHeaders-FileSmbProperties-String-ShareRequestConditions.clearHttpHeaderspreserveSMBProperties -->
     * <pre>
     * ShareRequestConditions requestConditions = new ShareRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     * shareFileAsyncClient.setPropertiesWithResponse&#40;1024, null, null, null, requestConditions&#41;
     *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Setting the file httpHeaders completed with status code %d&quot;,
     *         response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.setPropertiesWithResponse#long-ShareFileHttpHeaders-FileSmbProperties-String-ShareRequestConditions.clearHttpHeaderspreserveSMBProperties -->
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
            return monoError(LOGGER, ex);
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
        String fileChangeTime = smbProperties.getFileChangeTimeString();
        context = context == null ? Context.NONE : context;

        return azureFileStorageClient.getFiles()
            .setHttpHeadersWithResponseAsync(shareName, filePath, fileAttributes, null, newFileSize, filePermission,
                filePermissionKey, fileCreationTime, fileLastWriteTime, fileChangeTime, requestConditions.getLeaseId(),
                httpHeaders, context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(ShareFileAsyncClient::setPropertiesResponse);
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
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.setMetadata#map -->
     * <pre>
     * shareFileAsyncClient.setMetadata&#40;Collections.singletonMap&#40;&quot;file&quot;, &quot;updatedMetadata&quot;&#41;&#41;
     *     .doOnSuccess&#40;response -&gt; System.out.println&#40;&quot;Setting the file metadata completed.&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.setMetadata#map -->
     *
     * <p>Clear the metadata of the file</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.setMetadataWithResponse#map.clearMetadata -->
     * <pre>
     * shareFileAsyncClient.setMetadataWithResponse&#40;null&#41;.subscribe&#40;
     *     response -&gt; System.out.printf&#40;&quot;Setting the file metadata completed with status code %d&quot;,
     *         response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.setMetadataWithResponse#map.clearMetadata -->
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
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.setMetadataWithResponse#map -->
     * <pre>
     * shareFileAsyncClient.setMetadataWithResponse&#40;Collections.singletonMap&#40;&quot;file&quot;, &quot;updatedMetadata&quot;&#41;&#41;
     *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Setting the file metadata completed with status code %d&quot;,
     *         response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.setMetadataWithResponse#map -->
     *
     * <p>Clear the metadata of the file</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.setMetadataWithResponse#map.clearMetadata -->
     * <pre>
     * shareFileAsyncClient.setMetadataWithResponse&#40;null&#41;.subscribe&#40;
     *     response -&gt; System.out.printf&#40;&quot;Setting the file metadata completed with status code %d&quot;,
     *         response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.setMetadataWithResponse#map.clearMetadata -->
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
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.setMetadataWithResponse#map-ShareRequestConditions -->
     * <pre>
     * ShareRequestConditions requestConditions = new ShareRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     * shareFileAsyncClient.setMetadataWithResponse&#40;Collections.singletonMap&#40;&quot;file&quot;, &quot;updatedMetadata&quot;&#41;, requestConditions&#41;
     *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Setting the file metadata completed with status code %d&quot;,
     *         response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.setMetadataWithResponse#map-ShareRequestConditions -->
     *
     * <p>Clear the metadata of the file</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.setMetadataWithResponse#map-ShareRequestConditions.clearMetadata -->
     * <pre>
     * ShareRequestConditions requestConditions = new ShareRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     * shareFileAsyncClient.setMetadataWithResponse&#40;null, requestConditions&#41;.subscribe&#40;
     *     response -&gt; System.out.printf&#40;&quot;Setting the file metadata completed with status code %d&quot;,
     *         response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.setMetadataWithResponse#map-ShareRequestConditions.clearMetadata -->
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
            return monoError(LOGGER, ex);
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
                .map(ShareFileAsyncClient::setMetadataResponse);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
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
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.upload#flux-long -->
     * <pre>
     * ByteBuffer defaultData = ByteBuffer.wrap&#40;&quot;default&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;&#41;;
     * shareFileAsyncClient.upload&#40;Flux.just&#40;defaultData&#41;, defaultData.remaining&#40;&#41;&#41;.subscribe&#40;
     *     response -&gt; &#123; &#125;,
     *     error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Complete deleting the file!&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.upload#flux-long -->
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
        return uploadWithResponse(data, length, 0L).flatMap(FluxUtil::toMono);
    }

    /**
     * Uploads a range of bytes to specific of a file in storage file service. Upload operations performs an in-place
     * write on the specified file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Upload the file from 1024 to 2048 bytes with its metadata and properties and without the contentMD5. </p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.uploadWithResponse#flux-long-long -->
     * <pre>
     * ByteBuffer defaultData = ByteBuffer.wrap&#40;&quot;default&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;&#41;;
     * shareFileAsyncClient.uploadWithResponse&#40;Flux.just&#40;defaultData&#41;, defaultData.remaining&#40;&#41;, 0L&#41;.subscribe&#40;
     *     response -&gt; &#123; &#125;,
     *     error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Complete deleting the file!&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.uploadWithResponse#flux-long-long -->
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
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.uploadWithResponse#flux-long-long-ShareRequestConditions -->
     * <pre>
     * ShareRequestConditions requestConditions = new ShareRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     * ByteBuffer defaultData = ByteBuffer.wrap&#40;&quot;default&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;&#41;;
     * shareFileAsyncClient.uploadWithResponse&#40;Flux.just&#40;defaultData&#41;, defaultData.remaining&#40;&#41;, 0L, requestConditions&#41;
     *     .subscribe&#40;
     *         response -&gt; &#123; &#125;,
     *         error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *         &#40;&#41; -&gt; System.out.println&#40;&quot;Complete deleting the file!&quot;&#41;
     *     &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.uploadWithResponse#flux-long-long-ShareRequestConditions -->
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
            return uploadRangeWithResponse(new ShareFileUploadRangeOptions(data, length).setOffset(offset)
                .setRequestConditions(requestConditions));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
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
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.upload#Flux-ParallelTransferOptions -->
     * <pre>
     * ByteBuffer defaultData = ByteBuffer.wrap&#40;&quot;default&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;&#41;;
     * shareFileAsyncClient.upload&#40;Flux.just&#40;defaultData&#41;, null&#41;.subscribe&#40;
     *         response -&gt; &#123; &#125;,
     *         error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *         &#40;&#41; -&gt; System.out.println&#40;&quot;Complete deleting the file!&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.upload#Flux-ParallelTransferOptions -->
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
            return monoError(LOGGER, ex);
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
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.uploadWithResponse#ShareFileUploadOptions -->
     * <pre>
     * ByteBuffer defaultData = ByteBuffer.wrap&#40;&quot;default&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;&#41;;
     * shareFileAsyncClient.uploadWithResponse&#40;new ShareFileUploadOptions&#40;
     *     Flux.just&#40;defaultData&#41;&#41;&#41;.subscribe&#40;
     *         response -&gt; &#123; &#125;,
     *         error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *         &#40;&#41; -&gt; System.out.println&#40;&quot;Complete deleting the file!&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.uploadWithResponse#ShareFileUploadOptions -->
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
            return monoError(LOGGER, ex);
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

            BiFunction<Flux<ByteBuffer>, Long, Mono<Response<ShareFileUploadInfo>>> uploadFull = (stream, length) -> {
                ProgressListener progressListener = validatedParallelTransferOptions.getProgressListener();
                Context uploadContext = context;
                if (progressListener != null) {
                    uploadContext = Contexts.with(context).setHttpRequestProgressReporter(
                        ProgressReporter.withProgressListener(progressListener)
                    ).getContext();
                }
                return uploadRangeWithResponse(new ShareFileUploadRangeOptions(stream, length)
                    .setOffset(options.getOffset()).setRequestConditions(validatedRequestConditions), uploadContext);
            };

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
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<ShareFileUploadInfo>> uploadInChunks(Flux<ByteBuffer> data, long offset,
        ParallelTransferOptions parallelTransferOptions, ShareRequestConditions requestConditions, Context context) {

        // Validation done in the constructor.
        BufferStagingArea stagingArea = new BufferStagingArea(parallelTransferOptions.getBlockSizeLong(), FILE_MAX_PUT_RANGE_SIZE);

        Flux<ByteBuffer> chunkedSource = UploadUtils.chunkSource(data, parallelTransferOptions);

        ProgressListener progressListener = parallelTransferOptions.getProgressListener();
        ProgressReporter progressReporter = progressListener == null ? null : ProgressReporter.withProgressListener(
            progressListener);

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
                Context uploadContext = context;
                if (progressReporter != null) {
                    uploadContext = Contexts.with(context)
                        .setHttpRequestProgressReporter(progressReporter.createChild()).getContext();
                }
                return uploadRangeWithResponse(
                    new ShareFileUploadRangeOptions(bufferAggregator.asFlux(), currentBufferLength)
                        .setOffset(currentOffset).setRequestConditions(requestConditions), uploadContext)
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
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.uploadRange#Flux-long -->
     * <pre>
     * ByteBuffer defaultData = ByteBuffer.wrap&#40;&quot;default&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;&#41;;
     * shareFileAsyncClient.uploadRange&#40;Flux.just&#40;defaultData&#41;, defaultData.remaining&#40;&#41;&#41;.subscribe&#40;
     *         response -&gt; &#123; &#125;,
     *         error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *         &#40;&#41; -&gt; System.out.println&#40;&quot;Complete deleting the file!&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.uploadRange#Flux-long -->
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
            return monoError(LOGGER, ex);
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
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.uploadRangeWithResponse#ShareFileUploadRangeOptions -->
     * <pre>
     * ByteBuffer defaultData = ByteBuffer.wrap&#40;&quot;default&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;&#41;;
     * shareFileAsyncClient.uploadRangeWithResponse&#40;new ShareFileUploadRangeOptions&#40;
     *     Flux.just&#40;defaultData&#41;, defaultData.remaining&#40;&#41;&#41;&#41;.subscribe&#40;
     *         response -&gt; &#123; &#125;,
     *         error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *         &#40;&#41; -&gt; System.out.println&#40;&quot;Complete deleting the file!&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.uploadRangeWithResponse#ShareFileUploadRangeOptions -->
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
            return monoError(LOGGER, ex);
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
                options.getLength(), null, null, requestConditions.getLeaseId(), options.getLastWrittenMode(), data,
                context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(ShareFileAsyncClient::uploadResponse);
    }

    /**
     * Uploads a range of bytes from one file to another file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Upload a number of bytes from a file at defined source and destination offsets </p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.uploadRangeFromUrl#long-long-long-String -->
     * <pre>
     * shareFileAsyncClient.uploadRangeFromUrl&#40;6, 8, 0, &quot;sourceUrl&quot;&#41;.subscribe&#40;
     *     response -&gt; &#123; &#125;,
     *     error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Completed upload range from url!&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.uploadRangeFromUrl#long-long-long-String -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-range-from-url">Azure Docs</a>.</p>
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
        return uploadRangeFromUrlWithResponse(length, destinationOffset, sourceOffset, sourceUrl)
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Uploads a range of bytes from one file to another file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Upload a number of bytes from a file at defined source and destination offsets </p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.uploadRangeFromUrlWithResponse#long-long-long-String -->
     * <pre>
     * shareFileAsyncClient.uploadRangeFromUrlWithResponse&#40;6, 8, 0, &quot;sourceUrl&quot;&#41;.subscribe&#40;
     *     response -&gt; &#123; &#125;,
     *     error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Completed upload range from url!&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.uploadRangeFromUrlWithResponse#long-long-long-String -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-range-from-url">Azure Docs</a>.</p>
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

    /**
     * Uploads a range of bytes from one file to another file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Upload a number of bytes from a file at defined source and destination offsets </p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.uploadRangeFromUrlWithResponse#long-long-long-String-ShareRequestConditions -->
     * <pre>
     * ShareRequestConditions requestConditions = new ShareRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     * shareFileAsyncClient.uploadRangeFromUrlWithResponse&#40;6, 8, 0, &quot;sourceUrl&quot;, requestConditions&#41;.subscribe&#40;
     *     response -&gt; &#123; &#125;,
     *     error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Completed upload range from url!&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.uploadRangeFromUrlWithResponse#long-long-long-String-ShareRequestConditions -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-range-from-url">Azure Docs</a>.</p>
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
        try {
            return this.uploadRangeFromUrlWithResponse(new ShareFileUploadRangeFromUrlOptions(length, sourceUrl)
                .setDestinationOffset(destinationOffset).setSourceOffset(sourceOffset)
                .setDestinationRequestConditions(destinationRequestConditions));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Uploads a range of bytes from one file to another file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Upload a number of bytes from a file at defined source and destination offsets </p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.uploadRangeFromUrlWithResponse#ShareFileUploadRangeFromUrlOptions -->
     * <pre>
     * shareFileAsyncClient.uploadRangeFromUrlWithResponse&#40;
     *     new ShareFileUploadRangeFromUrlOptions&#40;6, &quot;sourceUrl&quot;&#41;.setDestinationOffset&#40;8&#41;&#41;
     *     .subscribe&#40;
     *         response -&gt; &#123; &#125;,
     *         error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *         &#40;&#41; -&gt; System.out.println&#40;&quot;Completed upload range from url!&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.uploadRangeFromUrlWithResponse#ShareFileUploadRangeFromUrlOptions -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-range-from-url">Azure Docs</a>.</p>
     *
     * @param options argument collection
     * @return A response containing the {@link ShareFileUploadRangeFromUrlInfo file upload range from url info} with
     * headers and response status code.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareFileUploadRangeFromUrlInfo>> uploadRangeFromUrlWithResponse(
        ShareFileUploadRangeFromUrlOptions options) {
        try {
            return withContext(context -> uploadRangeFromUrlWithResponse(options, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
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
                null, sourceRange.toString(), null, modifiedRequestConditions.getLeaseId(), sourceAuth,
                options.getLastWrittenMode(), null,
                context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(ShareFileAsyncClient::uploadRangeFromUrlResponse);
    }

    /**
     * Clear a range of bytes to specific of a file in storage file service. Clear operations performs an in-place write
     * on the specified file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Clears the first 1024 bytes. </p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.clearRange#long -->
     * <pre>
     * shareFileAsyncClient.clearRange&#40;1024&#41;.subscribe&#40;
     *     response -&gt; &#123; &#125;,
     *     error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Complete clearing the range!&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.clearRange#long -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-range">Azure Docs</a>.</p>
     *
     * @param length Specifies the number of bytes being cleared.
     * @return The {@link ShareFileUploadInfo file upload info}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ShareFileUploadInfo> clearRange(long length) {
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
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.clearRange#long-long -->
     * <pre>
     * shareFileAsyncClient.clearRangeWithResponse&#40;1024, 1024&#41;.subscribe&#40;
     *     response -&gt; &#123; &#125;,
     *     error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Complete clearing the range!&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.clearRange#long-long -->
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
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.clearRange#long-long-ShareRequestConditions -->
     * <pre>
     * ShareRequestConditions requestConditions = new ShareRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     * shareFileAsyncClient.clearRangeWithResponse&#40;1024, 1024, requestConditions&#41;.subscribe&#40;
     *     response -&gt; &#123; &#125;,
     *     error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Complete clearing the range!&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.clearRange#long-long-ShareRequestConditions -->
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
            return withContext(context -> clearRangeWithResponse(length, offset, requestConditions, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<ShareFileUploadInfo>> clearRangeWithResponse(long length, long offset,
        ShareRequestConditions requestConditions, Context context) {
        requestConditions = requestConditions == null ? new ShareRequestConditions() : requestConditions;
        ShareFileRange range = new ShareFileRange(offset, offset + length - 1);
        context = context == null ? Context.NONE : context;
        return azureFileStorageClient.getFiles()
            .uploadRangeWithResponseAsync(shareName, filePath, range.toString(), ShareFileRangeWriteType.CLEAR,
                0L, null, null, requestConditions.getLeaseId(), null, (Flux<ByteBuffer>) null,
                context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(ShareFileAsyncClient::uploadResponse);
    }

    /**
     * Uploads file to storage file service.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p> Upload the file from the source file path. </p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.uploadFromFile#string -->
     * <pre>
     * shareFileAsyncClient.uploadFromFile&#40;&quot;someFilePath&quot;&#41;.subscribe&#40;
     *     response -&gt; &#123; &#125;,
     *     error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Complete deleting the file!&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.uploadFromFile#string -->
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
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.uploadFromFile#string-ShareRequestConditions -->
     * <pre>
     * ShareRequestConditions requestConditions = new ShareRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     * shareFileAsyncClient.uploadFromFile&#40;&quot;someFilePath&quot;, requestConditions&#41;.subscribe&#40;
     *     response -&gt; &#123; &#125;,
     *     error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Complete deleting the file!&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.uploadFromFile#string-ShareRequestConditions -->
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
            return monoError(LOGGER, ex);
        }
    }

    private static List<ShareFileRange> sliceFile(String path) {
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
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.listRanges -->
     * <pre>
     * shareFileAsyncClient.listRanges&#40;&#41;.subscribe&#40;range -&gt;
     *     System.out.printf&#40;&quot;List ranges completed with start: %d, end: %d&quot;, range.getStart&#40;&#41;, range.getEnd&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.listRanges -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-ranges">Azure Docs</a>.</p>
     *
     * @return {@link ShareFileRange ranges} in the files.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ShareFileRange> listRanges() {
        return listRanges(null);
    }

    /**
     * List of valid ranges for a file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all ranges within the file range from 1KB to 2KB.</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.listRanges#ShareFileRange -->
     * <pre>
     * shareFileAsyncClient.listRanges&#40;new ShareFileRange&#40;1024, 2048L&#41;&#41;
     *     .subscribe&#40;result -&gt; System.out.printf&#40;&quot;List ranges completed with start: %d, end: %d&quot;,
     *         result.getStart&#40;&#41;, result.getEnd&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.listRanges#ShareFileRange -->
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
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.listRanges#ShareFileRange-ShareRequestConditions -->
     * <pre>
     * ShareRequestConditions requestConditions = new ShareRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     * shareFileAsyncClient.listRanges&#40;new ShareFileRange&#40;1024, 2048L&#41;, requestConditions&#41;
     *     .subscribe&#40;result -&gt; System.out.printf&#40;&quot;List ranges completed with start: %d, end: %d&quot;,
     *         result.getStart&#40;&#41;, result.getEnd&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.listRanges#ShareFileRange-ShareRequestConditions -->
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
            return pagedFluxError(LOGGER, ex);
        }
    }

    /**
     * List of valid ranges for a file between the file and the specified snapshot.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.listRangesDiff#String -->
     * <pre>
     * final String prevSnapshot = &quot;previoussnapshot&quot;;
     * shareFileAsyncClient.listRangesDiff&#40;prevSnapshot&#41;.subscribe&#40;response -&gt; &#123;
     *     System.out.println&#40;&quot;Valid Share File Ranges are:&quot;&#41;;
     *     for &#40;FileRange range : response.getRanges&#40;&#41;&#41; &#123;
     *         System.out.printf&#40;&quot;Start: %s, End: %s%n&quot;, range.getStart&#40;&#41;, range.getEnd&#40;&#41;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.listRangesDiff#String -->
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
            return monoError(LOGGER, ex);
        }
    }

    /**
     * List of valid ranges for a file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all ranges within the file range from 1KB to 2KB.</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.listRangesDiffWithResponse#ShareFileListRangesDiffOptions -->
     * <pre>
     * shareFileAsyncClient.listRangesDiffWithResponse&#40;new ShareFileListRangesDiffOptions&#40;&quot;previoussnapshot&quot;&#41;
     *     .setRange&#40;new ShareFileRange&#40;1024, 2048L&#41;&#41;&#41;.subscribe&#40;response -&gt; &#123;
     *         System.out.println&#40;&quot;Valid Share File Ranges are:&quot;&#41;;
     *         for &#40;FileRange range : response.getValue&#40;&#41;.getRanges&#40;&#41;&#41; &#123;
     *             System.out.printf&#40;&quot;Start: %s, End: %s%n&quot;, range.getStart&#40;&#41;, range.getEnd&#40;&#41;&#41;;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.listRangesDiffWithResponse#ShareFileListRangesDiffOptions -->
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
            return monoError(LOGGER, ex);
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
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.listHandles -->
     * <pre>
     * shareFileAsyncClient.listHandles&#40;&#41;
     *     .subscribe&#40;result -&gt; System.out.printf&#40;&quot;List handles completed with handle id %s&quot;, result.getHandleId&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.listHandles -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-handles">Azure Docs</a>.</p>
     *
     * @return {@link HandleItem handles} in the files that satisfy the requirements
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
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
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.listHandles#integer -->
     * <pre>
     * shareFileAsyncClient.listHandles&#40;10&#41;
     *     .subscribe&#40;result -&gt; System.out.printf&#40;&quot;List handles completed with handle id %s&quot;, result.getHandleId&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.listHandles#integer -->
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
            return pagedFluxError(LOGGER, ex);
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
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.forceCloseHandle#String -->
     * <pre>
     * shareFileAsyncClient.listHandles&#40;&#41;.subscribe&#40;handleItem -&gt;
     *     shareFileAsyncClient.forceCloseHandle&#40;handleItem.getHandleId&#40;&#41;&#41;.subscribe&#40;ignored -&gt;
     *         System.out.printf&#40;&quot;Closed handle %s on resource %s%n&quot;,
     *             handleItem.getHandleId&#40;&#41;, handleItem.getPath&#40;&#41;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.forceCloseHandle#String -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/force-close-handles">Azure Docs</a>.</p>
     *
     * @param handleId Handle ID to be closed.
     * @return A response that contains information about the closed handles.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CloseHandlesInfo> forceCloseHandle(String handleId) {
        return forceCloseHandleWithResponse(handleId).flatMap(FluxUtil::toMono);
    }

    /**
     * Closes a handle on the file. This is intended to be used alongside {@link #listHandles()}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Force close handles returned by list handles.</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.forceCloseHandleWithResponse#String -->
     * <pre>
     * shareFileAsyncClient.listHandles&#40;&#41;.subscribe&#40;handleItem -&gt;
     *     shareFileAsyncClient.forceCloseHandleWithResponse&#40;handleItem.getHandleId&#40;&#41;&#41;.subscribe&#40;response -&gt;
     *         System.out.printf&#40;&quot;Closing handle %s on resource %s completed with status code %d%n&quot;,
     *             handleItem.getHandleId&#40;&#41;, handleItem.getPath&#40;&#41;, response.getStatusCode&#40;&#41;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.forceCloseHandleWithResponse#String -->
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
            return monoError(LOGGER, ex);
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
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.forceCloseAllHandles -->
     * <pre>
     * shareFileAsyncClient.forceCloseAllHandles&#40;&#41;.subscribe&#40;handlesClosedInfo -&gt;
     *     System.out.printf&#40;&quot;Closed %d open handles on the file.%nFailed to close %d open handles on the file%n&quot;,
     *         handlesClosedInfo.getClosedHandles&#40;&#41;, handlesClosedInfo.getFailedHandles&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.forceCloseAllHandles -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/force-close-handles">Azure Docs</a>.</p>
     *
     * @return A response that contains information about the closed handles.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CloseHandlesInfo> forceCloseAllHandles() {
        try {
            return withContext(context -> forceCloseAllHandlesWithOptionalTimeout(null, context)
                .reduce(new CloseHandlesInfo(0, 0),
                    (accu, next) -> new CloseHandlesInfo(accu.getClosedHandles() + next.getClosedHandles(),
                        accu.getFailedHandles() + next.getFailedHandles())));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
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
     * Moves the file to another location within the share.
     * For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/rename-file">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.rename#String -->
     * <pre>
     * ShareFileAsyncClient renamedClient = client.rename&#40;destinationPath&#41;.block&#40;&#41;;
     * System.out.println&#40;&quot;File Client has been renamed&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.rename#String -->
     *
     * @param destinationPath Relative path from the share to rename the file to.
     * @return A {@link Mono} containing a {@link ShareFileAsyncClient} used to interact with the new file created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ShareFileAsyncClient> rename(String destinationPath) {
        try {
            return renameWithResponse(new ShareFileRenameOptions(destinationPath)).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Moves the file to another location within the share.
     * For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/rename-file">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.renameWithResponse#ShareFileRenameOptions -->
     * <pre>
     * FileSmbProperties smbProperties = new FileSmbProperties&#40;&#41;
     *     .setNtfsFileAttributes&#40;EnumSet.of&#40;NtfsFileAttributes.READ_ONLY&#41;&#41;
     *     .setFileCreationTime&#40;OffsetDateTime.now&#40;&#41;&#41;
     *     .setFileLastWriteTime&#40;OffsetDateTime.now&#40;&#41;&#41;
     *     .setFilePermissionKey&#40;&quot;filePermissionKey&quot;&#41;;
     * ShareFileRenameOptions options = new ShareFileRenameOptions&#40;destinationPath&#41;
     *     .setDestinationRequestConditions&#40;new ShareRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;&#41;
     *     .setSourceRequestConditions&#40;new ShareRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;&#41;
     *     .setIgnoreReadOnly&#40;false&#41;
     *     .setReplaceIfExists&#40;false&#41;
     *     .setFilePermission&#40;&quot;filePermission&quot;&#41;
     *     .setSmbProperties&#40;smbProperties&#41;;
     *
     * ShareFileAsyncClient newRenamedClient = client.renameWithResponse&#40;options&#41;.block&#40;&#41;.getValue&#40;&#41;;
     * System.out.println&#40;&quot;File Client has been renamed&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.renameWithResponse#ShareFileRenameOptions -->
     *
     * @param options {@link ShareFileRenameOptions}
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a {@link
     * ShareFileAsyncClient} used to interact with the file created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareFileAsyncClient>> renameWithResponse(ShareFileRenameOptions options) {
        try {
            return withContext(context -> renameWithResponse(options, context))
                .map(response -> new SimpleResponse<>(response, new ShareFileAsyncClient(response.getValue())));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<ShareFileAsyncClient>> renameWithResponse(ShareFileRenameOptions options, Context context) {
        StorageImplUtils.assertNotNull("options", options);
        context = context == null ? Context.NONE : context;

        ShareRequestConditions sourceRequestConditions = options.getSourceRequestConditions() == null
            ? new ShareRequestConditions() : options.getSourceRequestConditions();
        ShareRequestConditions destinationRequestConditions = options.getDestinationRequestConditions() == null
            ? new ShareRequestConditions() : options.getDestinationRequestConditions();

        // We want to hide the SourceAccessConditions type from the user for consistency's sake, so we convert here.
        SourceLeaseAccessConditions sourceConditions = new SourceLeaseAccessConditions()
            .setSourceLeaseId(sourceRequestConditions.getLeaseId());
        DestinationLeaseAccessConditions destinationConditions = new DestinationLeaseAccessConditions()
            .setDestinationLeaseId(destinationRequestConditions.getLeaseId());

        CopyFileSmbInfo smbInfo = null;
        String filePermissionKey = null;
        if (options.getSmbProperties() != null) {
            FileSmbProperties tempSmbProperties = options.getSmbProperties();
            filePermissionKey = tempSmbProperties.getFilePermissionKey();

            String fileAttributes = NtfsFileAttributes.toString(tempSmbProperties.getNtfsFileAttributes());
            String fileCreationTime = FileSmbProperties.parseFileSMBDate(tempSmbProperties.getFileCreationTime());
            String fileLastWriteTime = FileSmbProperties.parseFileSMBDate(tempSmbProperties.getFileLastWriteTime());
            String fileChangeTime = FileSmbProperties.parseFileSMBDate(tempSmbProperties.getFileChangeTime());
            smbInfo = new CopyFileSmbInfo()
                .setFileAttributes(fileAttributes)
                .setFileCreationTime(fileCreationTime)
                .setFileLastWriteTime(fileLastWriteTime)
                .setFileChangeTime(fileChangeTime)
                .setIgnoreReadOnly(options.isIgnoreReadOnly());
        }

        ShareFileAsyncClient destinationFileClient = getFileAsyncClient(options.getDestinationPath());

        ShareFileHttpHeaders headers = options.getContentType() == null ? null
            : new ShareFileHttpHeaders().setContentType(options.getContentType());

        String renameSource = this.getFileUrl();
        // TODO (rickle-msft): when support added to core
//        String sasToken = this.extractSasToken();
//        renameSource = sasToken == null ? renameSource : renameSource + sasToken;

        return destinationFileClient.azureFileStorageClient.getFiles().renameWithResponseAsync(
            destinationFileClient.getShareName(), destinationFileClient.getFilePath(), renameSource,
            null /* timeout */, options.getReplaceIfExists(), options.isIgnoreReadOnly(),
            options.getFilePermission(), filePermissionKey, options.getMetadata(), sourceConditions,
            destinationConditions, smbInfo, headers,
            context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(response -> new SimpleResponse<>(response, destinationFileClient));
    }

    /**
     * Takes in a destination and creates a ShareFileAsyncClient with a new path
     * @param destinationPath The destination path
     * @return A DataLakePathAsyncClient
     */
    ShareFileAsyncClient getFileAsyncClient(String destinationPath) {
        if (CoreUtils.isNullOrEmpty(destinationPath)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'destinationPath' can not be set to null"));
        }

        return new ShareFileAsyncClient(this.azureFileStorageClient, getShareName(), destinationPath, null,
            this.getAccountName(), this.getServiceVersion());
    }

//    private String extractSasToken() {
//        for (int i = 0; i < this.getHttpPipeline().getPolicyCount(); i++) {
//            if (this.getHttpPipeline().getPolicy(i) instanceof AzureSasCredentialPolicy) {
//                AzureSasCredentialPolicy policy = (AzureSasCredentialPolicy) this.getHttpPipeline().getPolicy(i);
//                return policy.getCredential().getSignature();
//            }
//        }
//        return null;
//    }

    /**
     * Get snapshot id which attached to {@link ShareFileAsyncClient}. Return {@code null} if no snapshot id attached.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Get the share snapshot id. </p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.getShareSnapshotId -->
     * <pre>
     * OffsetDateTime currentTime = OffsetDateTime.of&#40;LocalDateTime.now&#40;&#41;, ZoneOffset.UTC&#41;;
     * ShareFileAsyncClient shareFileAsyncClient = new ShareFileClientBuilder&#40;&#41;
     *     .endpoint&#40;&quot;https:&#47;&#47;$&#123;accountName&#125;.file.core.windows.net&quot;&#41;
     *     .sasToken&#40;&quot;$&#123;SASToken&#125;&quot;&#41;
     *     .shareName&#40;&quot;myshare&quot;&#41;
     *     .resourcePath&#40;&quot;myfiile&quot;&#41;
     *     .snapshot&#40;currentTime.toString&#40;&#41;&#41;
     *     .buildFileAsyncClient&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Snapshot ID: %s%n&quot;, shareFileAsyncClient.getShareSnapshotId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.getShareSnapshotId -->
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
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.getShareName -->
     * <pre>
     * String shareName = directoryAsyncClient.getShareName&#40;&#41;;
     * System.out.println&#40;&quot;The share name of the directory is &quot; + shareName&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.getShareName -->
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
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.getFilePath -->
     * <pre>
     * String filePath = shareFileAsyncClient.getFilePath&#40;&#41;;
     * System.out.println&#40;&quot;The name of the file is &quot; + filePath&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.getFilePath -->
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
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.generateSas#ShareServiceSasSignatureValues -->
     * <pre>
     * OffsetDateTime expiryTime = OffsetDateTime.now&#40;&#41;.plusDays&#40;1&#41;;
     * ShareFileSasPermission permission = new ShareFileSasPermission&#40;&#41;.setReadPermission&#40;true&#41;;
     *
     * ShareServiceSasSignatureValues values = new ShareServiceSasSignatureValues&#40;expiryTime, permission&#41;
     *     .setStartTime&#40;OffsetDateTime.now&#40;&#41;&#41;;
     *
     * shareFileAsyncClient.generateSas&#40;values&#41;; &#47;&#47; Client must be authenticated via StorageSharedKeyCredential
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.generateSas#ShareServiceSasSignatureValues -->
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
     * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.generateSas#ShareServiceSasSignatureValues-Context -->
     * <pre>
     * OffsetDateTime expiryTime = OffsetDateTime.now&#40;&#41;.plusDays&#40;1&#41;;
     * ShareFileSasPermission permission = new ShareFileSasPermission&#40;&#41;.setReadPermission&#40;true&#41;;
     *
     * ShareServiceSasSignatureValues values = new ShareServiceSasSignatureValues&#40;expiryTime, permission&#41;
     *     .setStartTime&#40;OffsetDateTime.now&#40;&#41;&#41;;
     *
     * &#47;&#47; Client must be authenticated via StorageSharedKeyCredential
     * shareFileAsyncClient.generateSas&#40;values, new Context&#40;&quot;key&quot;, &quot;value&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.generateSas#ShareServiceSasSignatureValues-Context -->
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

    private static Response<ShareFileInfo> createFileInfoResponse(ResponseBase<FilesCreateHeaders, Void> response) {
        String eTag = response.getDeserializedHeaders().getETag();
        OffsetDateTime lastModified = response.getDeserializedHeaders().getLastModified();
        boolean isServerEncrypted = response.getDeserializedHeaders().isXMsRequestServerEncrypted();
        FileSmbProperties smbProperties = new FileSmbProperties(response.getHeaders());
        ShareFileInfo shareFileInfo = new ShareFileInfo(eTag, lastModified, isServerEncrypted, smbProperties);
        return new SimpleResponse<>(response, shareFileInfo);
    }

    private static Response<ShareFileInfo> setPropertiesResponse(
        final ResponseBase<FilesSetHttpHeadersHeaders, Void> response) {
        String eTag = response.getDeserializedHeaders().getETag();
        OffsetDateTime lastModified = response.getDeserializedHeaders().getLastModified();
        boolean isServerEncrypted = response.getDeserializedHeaders().isXMsRequestServerEncrypted();
        FileSmbProperties smbProperties = new FileSmbProperties(response.getHeaders());
        ShareFileInfo shareFileInfo = new ShareFileInfo(eTag, lastModified, isServerEncrypted, smbProperties);
        return new SimpleResponse<>(response, shareFileInfo);
    }

    private static Response<ShareFileProperties> getPropertiesResponse(
        final ResponseBase<FilesGetPropertiesHeaders, Void> response) {
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
        Boolean isServerEncrypted = headers.isXMsServerEncrypted();
        FileSmbProperties smbProperties = new FileSmbProperties(response.getHeaders());
        ShareFileProperties shareFileProperties = new ShareFileProperties(eTag, lastModified, metadata, fileType,
            contentLength, contentType, contentMD5, contentEncoding, cacheControl, contentDisposition,
            leaseStatusType, leaseStateType, leaseDurationType, copyCompletionTime, copyStatusDescription, copyId,
            copyProgress, copySource, copyStatus, isServerEncrypted, smbProperties);
        return new SimpleResponse<>(response, shareFileProperties);
    }

    private static Response<ShareFileUploadInfo> uploadResponse(ResponseBase<FilesUploadRangeHeaders, Void> response) {
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

    private static Response<ShareFileUploadRangeFromUrlInfo> uploadRangeFromUrlResponse(
        final ResponseBase<FilesUploadRangeFromURLHeaders, Void> response) {
        FilesUploadRangeFromURLHeaders headers = response.getDeserializedHeaders();
        String eTag = headers.getETag();
        OffsetDateTime lastModified = headers.getLastModified();
        Boolean isServerEncrypted = headers.isXMsRequestServerEncrypted();
        ShareFileUploadRangeFromUrlInfo shareFileUploadRangeFromUrlInfo =
            new ShareFileUploadRangeFromUrlInfo(eTag, lastModified, isServerEncrypted);
        return new SimpleResponse<>(response, shareFileUploadRangeFromUrlInfo);
    }

    private static Response<ShareFileMetadataInfo> setMetadataResponse(
        final ResponseBase<FilesSetMetadataHeaders, Void> response) {
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
    private static void validateFilePermissionAndKey(String filePermission, String  filePermissionKey) {
        if (filePermission != null && filePermissionKey != null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                FileConstants.MessageConstants.FILE_PERMISSION_FILE_PERMISSION_KEY_INVALID));
        }

        if (filePermission != null) {
            StorageImplUtils.assertInBounds("filePermission",
                filePermission.getBytes(StandardCharsets.UTF_8).length, 0, 8 * Constants.KB);
        }
    }
}
