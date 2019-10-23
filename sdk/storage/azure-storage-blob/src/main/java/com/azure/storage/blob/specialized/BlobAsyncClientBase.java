// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import static com.azure.core.implementation.util.FluxUtil.withContext;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.RequestConditions;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.implementation.util.FluxUtil;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollResponse.OperationStatus;
import com.azure.core.util.polling.Poller;
import com.azure.storage.blob.implementation.util.BlobHelper;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.HttpGetterInfo;
import com.azure.storage.blob.ProgressReceiver;
import com.azure.storage.blob.ProgressReporter;
import com.azure.storage.blob.implementation.AzureBlobStorageBuilder;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.implementation.models.BlobGetAccountInfoHeaders;
import com.azure.storage.blob.implementation.models.BlobGetPropertiesHeaders;
import com.azure.storage.blob.implementation.models.BlobStartCopyFromURLHeaders;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.ArchiveStatus;
import com.azure.storage.blob.models.BlobCopyInfo;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.CopyStatusType;
import com.azure.storage.blob.models.CpkInfo;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.models.RehydratePriority;
import com.azure.storage.blob.models.ReliableDownloadOptions;
import com.azure.storage.blob.models.StorageAccountInfo;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.StorageImplUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.azure.core.implementation.util.FluxUtil.fluxError;
import static com.azure.core.implementation.util.FluxUtil.monoError;

/**
 * This class provides a client that contains all operations that apply to any blob type.
 *
 * <p>
 * This client offers the ability to download blobs. Note that uploading data is specific to each type of blob. Please
 * refer to the {@link BlockBlobClient}, {@link PageBlobClient}, or {@link AppendBlobClient} for upload options.
 */
public class BlobAsyncClientBase {

    private static final int BLOB_DEFAULT_DOWNLOAD_BLOCK_SIZE = 4 * Constants.MB;
    private static final int BLOB_MAX_DOWNLOAD_BLOCK_SIZE = 100 * Constants.MB;

    private final ClientLogger logger = new ClientLogger(BlobAsyncClientBase.class);

    private final AzureBlobStorageImpl azureBlobStorage;
    private final String snapshot;
    private final CpkInfo customerProvidedKey;
    private final String accountName;
    private final String containerName;
    private final String blobName;
    private final BlobServiceVersion serviceVersion;

    /**
     * Package-private constructor for use by {@link SpecializedBlobClientBuilder}.
     *
     * @param pipeline The pipeline used to send and receive service requests.
     * @param url The endpoint where to send service requests.
     * @param serviceVersion The version of the service to receive requests.
     * @param accountName The storage account name.
     * @param containerName The container name.
     * @param blobName The blob name.
     * @param snapshot The snapshot identifier for the blob, pass {@code null} to interact with the blob directly.
     * @param customerProvidedKey Customer provided key used during encryption of the blob's data on the server, pass
     * {@code null} to allow the service to use its own encryption.
     */
    protected BlobAsyncClientBase(HttpPipeline pipeline, String url, BlobServiceVersion serviceVersion,
        String accountName, String containerName, String blobName, String snapshot, CpkInfo customerProvidedKey) {
        this.azureBlobStorage = new AzureBlobStorageBuilder()
            .pipeline(pipeline)
            .url(url)
            .version(serviceVersion.getVersion())
            .build();
        this.serviceVersion = serviceVersion;

        this.accountName = accountName;
        this.containerName = containerName;
        this.blobName = blobName;
        this.snapshot = snapshot;
        this.customerProvidedKey = customerProvidedKey;

        BlobHelper.setAsyncPropertyAccessor(new BlobPropertyAccessor());
    }

    /**
     * Creates a new {@link BlobAsyncClientBase} linked to the {@code snapshot} of this blob resource.
     *
     * @param snapshot the identifier for a specific snapshot of this blob
     * @return a {@link BlobAsyncClientBase} used to interact with the specific snapshot.
     */
    public BlobAsyncClientBase getSnapshotClient(String snapshot) {
        return new BlobAsyncClientBase(getHttpPipeline(), getBlobUrl(), serviceVersion, accountName, containerName,
            blobName, snapshot, customerProvidedKey);
    }

    /**
     * Gets the URL of the blob represented by this client.
     *
     * @return the URL.
     */
    public String getBlobUrl() {
        if (!this.isSnapshot()) {
            return azureBlobStorage.getUrl();
        } else {
            if (azureBlobStorage.getUrl().contains("?")) {
                return String.format("%s&snapshot=%s", azureBlobStorage.getUrl(), snapshot);
            } else {
                return String.format("%s?snapshot=%s", azureBlobStorage.getUrl(), snapshot);
            }
        }
    }

    /**
     * Get the container name.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.getContainerName}
     *
     * @return The name of the container.
     */
    public final String getContainerName() {
        return containerName;
    }

    /**
     * Get the blob name.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.getBlobName}
     *
     * @return The name of the blob.
     */
    public final String getBlobName() {
        return blobName;
    }

    /**
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return The pipeline.
     */
    public HttpPipeline getHttpPipeline() {
        return azureBlobStorage.getHttpPipeline();
    }

    /**
     * Get associated account name.
     *
     * @return account name associated with this storage resource.
     */
    public String getAccountName() {
        return accountName;
    }

    /**
     * Gets the snapshotId for a blob resource
     *
     * @return A string that represents the snapshotId of the snapshot blob
     */
    public String getSnapshotId() {
        return this.snapshot;
    }

    /**
     * Determines if a blob is a snapshot
     *
     * @return A boolean that indicates if a blob is a snapshot
     */
    public boolean isSnapshot() {
        return this.snapshot != null;
    }

    /**
     * Determines if the blob this client represents exists in the cloud.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.exists}
     *
     * @return true if the blob exists, false if it doesn't
     */
    public Mono<Boolean> exists() {
        try {
            return existsWithResponse().flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Determines if the blob this client represents exists in the cloud.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.existsWithResponse}
     *
     * @return true if the blob exists, false if it doesn't
     */
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
            .onErrorResume(t -> t instanceof BlobStorageException && ((BlobStorageException) t).getStatusCode() == 404,
                t -> {
                    HttpResponse response = ((BlobStorageException) t).getResponse();
                    return Mono.just(new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                        response.getHeaders(), false));
                });
    }

    /**
     * Copies the data at the source URL to a blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.beginCopy#String-Duration}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/copy-blob">Azure Docs</a></p>
     *
     * @param sourceUrl The source URL to copy from. URLs outside of Azure may only be copied to block blobs.
     * @param pollInterval Duration between each poll for the copy status. If none is specified, a default of one second
     * is used.
     * @return A {@link Poller} that polls the blob copy operation until it has completed, has failed, or has been
     * cancelled.
     */
    public Poller<BlobCopyInfo, Void> beginCopy(String sourceUrl, Duration pollInterval) {
        return beginCopy(sourceUrl, null, null, null, null, null, pollInterval);
    }

    /**
     * Copies the data at the source URL to a blob.
     *
     * <p><strong>Starting a copy operation</strong></p>
     * Starting a copy operation and polling on the responses.
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.beginCopy#String-Map-AccessTier-RehydratePriority-RequestConditions-BlobRequestConditions-Duration}
     *
     * <p><strong>Cancelling a copy operation</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.beginCopyFromUrlCancel#String-Map-AccessTier-RehydratePriority-RequestConditions-BlobRequestConditions-Duration}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/copy-blob">Azure Docs</a></p>
     *
     * @param sourceUrl The source URL to copy from. URLs outside of Azure may only be copied to block blobs.
     * @param metadata Metadata to associate with the destination blob.
     * @param tier {@link AccessTier} for the destination blob.
     * @param priority {@link RehydratePriority} for rehydrating the blob.
     * @param sourceModifiedAccessConditions {@link RequestConditions} against the source. Standard HTTP
     * Access conditions related to the modification of data. ETag and LastModifiedTime are used to construct
     * conditions related to when the blob was changed relative to the given request. The request will fail if the
     * specified condition is not satisfied.
     * @param destAccessConditions {@link BlobRequestConditions} against the destination.
     * @param pollInterval Duration between each poll for the copy status. If none is specified, a default of one second
     * is used.
     * @return A {@link Poller} that polls the blob copy operation until it has completed, has failed, or has been
     * cancelled.
     */
    public Poller<BlobCopyInfo, Void> beginCopy(String sourceUrl, Map<String, String> metadata, AccessTier tier,
            RehydratePriority priority, RequestConditions sourceModifiedAccessConditions,
            BlobRequestConditions destAccessConditions, Duration pollInterval) {

        final Duration interval = pollInterval != null ? pollInterval : Duration.ofSeconds(1);
        final RequestConditions sourceModifiedCondition = sourceModifiedAccessConditions == null
            ? new RequestConditions()
            : sourceModifiedAccessConditions;
        final BlobRequestConditions destinationAccessConditions = destAccessConditions == null
            ? new BlobRequestConditions()
            : destAccessConditions;

        // We want to hide the SourceAccessConditions type from the user for consistency's sake, so we convert here.
        final RequestConditions sourceConditions = new RequestConditions()
            .setIfModifiedSince(sourceModifiedCondition.getIfModifiedSince())
            .setIfUnmodifiedSince(sourceModifiedCondition.getIfUnmodifiedSince())
            .setIfMatch(sourceModifiedCondition.getIfMatch())
            .setIfNoneMatch(sourceModifiedCondition.getIfNoneMatch());

        return new Poller<>(interval,
            response -> {
                try {
                    return onPoll(response);
                } catch (RuntimeException ex) {
                    return monoError(logger, ex);
                }
            },
            Mono::empty,
            () -> {
                try {
                    return onStart(sourceUrl, metadata, tier, priority, sourceConditions, destinationAccessConditions);
                } catch (RuntimeException ex) {
                    return monoError(logger, ex);
                }
            },
            poller -> {
                final PollResponse<BlobCopyInfo> response = poller.getLastPollResponse();

                if (response == null || response.getValue() == null) {
                    return Mono.error(logger.logExceptionAsError(
                        new IllegalArgumentException("Cannot cancel a poll response that never started.")));
                }

                final String copyIdentifier = response.getValue().getCopyId();

                if (!ImplUtils.isNullOrEmpty(copyIdentifier)) {
                    logger.info("Cancelling copy operation for copy id: {}", copyIdentifier);

                    return abortCopyFromUrl(copyIdentifier).thenReturn(response.getValue());
                }

                return Mono.empty();
            });
    }

    private Mono<BlobCopyInfo> onStart(String sourceUrl, Map<String, String> metadata, AccessTier tier,
            RehydratePriority priority, RequestConditions sourceModifiedAccessConditions,
            BlobRequestConditions destinationAccessConditions) {
        URL url;
        try {
            url = new URL(sourceUrl);
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'sourceUrl' is not a valid url.", ex));
        }

        return withContext(
            context -> azureBlobStorage.blobs().startCopyFromURLWithRestResponseAsync(null, null, url, null, metadata,
                tier, priority, sourceModifiedAccessConditions.getIfModifiedSince(),
                sourceModifiedAccessConditions.getIfUnmodifiedSince(), sourceModifiedAccessConditions.getIfMatch(),
                sourceModifiedAccessConditions.getIfNoneMatch(), destinationAccessConditions.getIfModifiedSince(),
                destinationAccessConditions.getIfUnmodifiedSince(), destinationAccessConditions.getIfMatch(),
                destinationAccessConditions.getIfNoneMatch(), destinationAccessConditions.getLeaseId(), null, context))
            .map(response -> {
                final BlobStartCopyFromURLHeaders headers = response.getDeserializedHeaders();

                return new BlobCopyInfo(sourceUrl, headers.getCopyId(), headers.getCopyStatus(),
                    headers.getETag(), headers.getLastModified(), headers.getErrorCode());
            });
    }

    private Mono<PollResponse<BlobCopyInfo>> onPoll(PollResponse<BlobCopyInfo> pollResponse) {
        if (pollResponse.getStatus() == OperationStatus.SUCCESSFULLY_COMPLETED
            || pollResponse.getStatus() == OperationStatus.FAILED) {
            return Mono.just(pollResponse);
        }

        final BlobCopyInfo lastInfo = pollResponse.getValue();
        if (lastInfo == null) {
            logger.warning("BlobCopyInfo does not exist. Activation operation failed.");
            return Mono.just(new PollResponse<>(
                OperationStatus.fromString("COPY_START_FAILED", true), null));
        }

        return getProperties().map(response -> {
            final CopyStatusType status = response.getCopyStatus();
            final BlobCopyInfo result = new BlobCopyInfo(response.getCopySource(), response.getCopyId(), status,
                response.getETag(), response.getCopyCompletionTime(), response.getCopyStatusDescription());

            OperationStatus operationStatus;
            switch (status) {
                case SUCCESS:
                    operationStatus = OperationStatus.SUCCESSFULLY_COMPLETED;
                    break;
                case FAILED:
                    operationStatus = OperationStatus.FAILED;
                    break;
                case ABORTED:
                    operationStatus = OperationStatus.USER_CANCELLED;
                    break;
                case PENDING:
                    operationStatus = OperationStatus.IN_PROGRESS;
                    break;
                default:
                    throw logger.logExceptionAsError(new IllegalArgumentException(
                        "CopyStatusType is not supported. Status: " + status));
            }

            return new PollResponse<>(operationStatus, result);
        }).onErrorReturn(
            new PollResponse<>(OperationStatus.fromString("POLLING_FAILED", true), lastInfo));
    }

    /**
     * Stops a pending copy that was previously started and leaves a destination blob with 0 length and metadata.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.abortCopyFromUrl#String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/abort-copy-blob">Azure Docs</a></p>
     *
     * @see #copyFromUrl(String)
     * @see #beginCopy(String, Duration)
     * @see #beginCopy(String, Map, AccessTier, RehydratePriority, RequestConditions, BlobRequestConditions, Duration)
     * @param copyId The id of the copy operation to abort.
     * @return A reactive response signalling completion.
     */
    public Mono<Void> abortCopyFromUrl(String copyId) {
        try {
            return abortCopyFromUrlWithResponse(copyId, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Stops a pending copy that was previously started and leaves a destination blob with 0 length and metadata.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.abortCopyFromUrlWithResponse#String-String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/abort-copy-blob">Azure Docs</a></p>
     *
     * @see #copyFromUrl(String)
     * @see #beginCopy(String, Duration)
     * @see #beginCopy(String, Map, AccessTier, RehydratePriority, RequestConditions, BlobRequestConditions, Duration)
     * @param copyId The id of the copy operation to abort.
     * @param leaseId The lease ID the active lease on the blob must match.
     * @return A reactive response signalling completion.
     */
    public Mono<Response<Void>> abortCopyFromUrlWithResponse(String copyId, String leaseId) {
        try {
            return withContext(context -> abortCopyFromUrlWithResponse(copyId, leaseId, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> abortCopyFromUrlWithResponse(String copyId, String leaseId, Context context) {
        return this.azureBlobStorage.blobs().abortCopyFromURLWithRestResponseAsync(
            null, null, copyId, null, leaseId, null, context)
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Copies the data at the source URL to a blob and waits for the copy to complete before returning a response.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.copyFromUrl#String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/copy-blob">Azure Docs</a></p>
     *
     * @param copySource The source URL to copy from.
     * @return A reactive response containing the copy ID for the long running operation.
     */
    public Mono<String> copyFromUrl(String copySource) {
        try {
            return copyFromUrlWithResponse(copySource, null, null, null, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Copies the data at the source URL to a blob and waits for the copy to complete before returning a response.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.copyFromUrlWithResponse#String-Map-AccessTier-RequestConditions-BlobRequestConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/copy-blob">Azure Docs</a></p>
     *
     * @param copySource The source URL to copy from. URLs outside of Azure may only be copied to block blobs.
     * @param metadata Metadata to associate with the destination blob.
     * @param tier {@link AccessTier} for the destination blob.
     * @param sourceModifiedAccessConditions {@link RequestConditions} against the source. Standard HTTP Access
     * conditions related to the modification of data. ETag and LastModifiedTime are used to construct conditions
     * related to when the blob was changed relative to the given request. The request will fail if the specified
     * condition is not satisfied.
     * @param destAccessConditions {@link BlobRequestConditions} against the destination.
     * @return A reactive response containing the copy ID for the long running operation.
     */
    public Mono<Response<String>> copyFromUrlWithResponse(String copySource, Map<String, String> metadata,
            AccessTier tier, RequestConditions sourceModifiedAccessConditions,
            BlobRequestConditions destAccessConditions) {
        try {
            return withContext(context -> copyFromUrlWithResponse(copySource, metadata, tier,
                sourceModifiedAccessConditions, destAccessConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<String>> copyFromUrlWithResponse(String copySource, Map<String, String> metadata, AccessTier tier,
            RequestConditions sourceModifiedAccessConditions, BlobRequestConditions destAccessConditions,
            Context context) {
        sourceModifiedAccessConditions = sourceModifiedAccessConditions == null
            ? new RequestConditions() : sourceModifiedAccessConditions;
        destAccessConditions = destAccessConditions == null ? new BlobRequestConditions() : destAccessConditions;

        URL url;
        try {
            url = new URL(copySource);
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'copySource' is not a valid url."));
        }

        return this.azureBlobStorage.blobs().copyFromURLWithRestResponseAsync(
            null, null, url, null, metadata, tier, sourceModifiedAccessConditions.getIfModifiedSince(),
            sourceModifiedAccessConditions.getIfUnmodifiedSince(), sourceModifiedAccessConditions.getIfMatch(),
            sourceModifiedAccessConditions.getIfNoneMatch(), destAccessConditions.getIfModifiedSince(),
            destAccessConditions.getIfUnmodifiedSince(), destAccessConditions.getIfMatch(),
            destAccessConditions.getIfNoneMatch(), destAccessConditions.getLeaseId(), null, context)
            .map(rb -> new SimpleResponse<>(rb, rb.getDeserializedHeaders().getCopyId()));
    }

    /**
     * Reads the entire blob. Uploading data must be done from the {@link BlockBlobClient}, {@link PageBlobClient}, or
     * {@link AppendBlobClient}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.download}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @return A reactive response containing the blob data.
     */
    public Flux<ByteBuffer> download() {
        try {
            return downloadWithResponse(null, null, null, false)
                .flatMapMany(Response::getValue);
        } catch (RuntimeException ex) {
            return fluxError(logger, ex);
        }
    }

    /**
     * Reads a range of bytes from a blob. Uploading data must be done from the {@link BlockBlobClient}, {@link
     * PageBlobClient}, or {@link AppendBlobClient}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.downloadWithResponse#BlobRange-ReliableDownloadOptions-BlobAccessConditions-boolean}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param range {@link BlobRange}
     * @param options {@link ReliableDownloadOptions}
     * @param accessConditions {@link BlobRequestConditions}
     * @param rangeGetContentMD5 Whether the contentMD5 for the specified blob range should be returned.
     * @return A reactive response containing the blob data.
     */
    public Mono<Response<Flux<ByteBuffer>>> downloadWithResponse(BlobRange range, ReliableDownloadOptions options,
        BlobRequestConditions accessConditions, boolean rangeGetContentMD5) {
        try {
            return withContext(context -> downloadWithResponse(range, options, accessConditions, rangeGetContentMD5,
                context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Flux<ByteBuffer>>> downloadWithResponse(BlobRange range, ReliableDownloadOptions options,
        BlobRequestConditions accessConditions, boolean rangeGetContentMD5, Context context) {
        return download(range, accessConditions, rangeGetContentMD5, context)
            .map(response -> new SimpleResponse<>(
                response.getRawResponse(),
                response.body(options).switchIfEmpty(Flux.just(ByteBuffer.wrap(new byte[0])))));
    }

    /**
     * Reads a range of bytes from a blob. The response also includes the blob's properties and metadata. For more
     * information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a>.
     * <p>
     * Note that the response body has reliable download functionality built in, meaning that a failed download stream
     * will be automatically retried. This behavior may be configured with {@link ReliableDownloadOptions}.
     *
     * @param range {@link BlobRange}
     * @param accessConditions {@link BlobRequestConditions}
     * @param rangeGetContentMd5 Whether the content MD5 for the specified blob range should be returned.
     * @return Emits the successful response.
     */
    Mono<DownloadAsyncResponse> download(BlobRange range, BlobRequestConditions accessConditions,
        boolean rangeGetContentMd5) {
        return withContext(context -> download(range, accessConditions, rangeGetContentMd5, context));
    }

    Mono<DownloadAsyncResponse> download(BlobRange range, BlobRequestConditions accessConditions,
        boolean rangeGetContentMd5, Context context) {
        range = range == null ? new BlobRange(0) : range;
        Boolean getMD5 = rangeGetContentMd5 ? rangeGetContentMd5 : null;
        accessConditions = accessConditions == null ? new BlobRequestConditions() : accessConditions;
        HttpGetterInfo info = new HttpGetterInfo()
            .setOffset(range.getOffset())
            .setCount(range.getCount())
            .setETag(accessConditions.getIfMatch());

        // TODO: range is BlobRange but expected as String
        // TODO: figure out correct response
        return this.azureBlobStorage.blobs().downloadWithRestResponseAsync(null, null, snapshot, null,
            range.toHeaderValue(), accessConditions.getLeaseId(), getMD5, null, accessConditions.getIfModifiedSince(),
            accessConditions.getIfUnmodifiedSince(), accessConditions.getIfMatch(), accessConditions.getIfNoneMatch(),
            null, customerProvidedKey, context)
            // Convert the autorest response to a DownloadAsyncResponse, which enable reliable download.
            .map(response -> {
                // If there wasn't an etag originally specified, lock on the one returned.
                info.setETag(response.getDeserializedHeaders().getETag());
                return new DownloadAsyncResponse(response, info,
                    // In the event of a stream failure, make a new request to pick up where we left off.
                    newInfo ->
                        this.download(new BlobRange(newInfo.getOffset(), newInfo.getCount()),
                            new BlobRequestConditions().setIfMatch(info.getETag()), false, context));
            });
    }


    /**
     * Downloads the entire blob into a file specified by the path.
     *
     * <p>The file will be created and must not exist, if the file already exists a {@link FileAlreadyExistsException}
     * will be thrown.</p>
     *
     * <p>Uploading data must be done from the {@link BlockBlobClient}, {@link PageBlobClient}, or {@link
     * AppendBlobClient}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.downloadToFile#String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param filePath A non-null {@link OutputStream} instance where the downloaded data will be written.
     * @return An empty response
     */
    public Mono<BlobProperties> downloadToFile(String filePath) {
        try {
            return downloadToFileWithResponse(filePath, null, null,
                null, null, false).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Downloads the entire blob into a file specified by the path.
     *
     * <p>The file will be created and must not exist, if the file already exists a {@link FileAlreadyExistsException}
     * will be thrown.</p>
     *
     * <p>Uploading data must be done from the {@link BlockBlobClient}, {@link PageBlobClient}, or {@link
     * AppendBlobClient}.</p>
     *
     * <p>This method makes an extra HTTP call to get the length of the blob in the beginning. To avoid this extra
     * call, provide the {@link BlobRange} parameter.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.downloadToFileWithResponse#String-BlobRange-ParallelTransferOptions-ReliableDownloadOptions-BlobRequestConditions-boolean}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param filePath A non-null {@link OutputStream} instance where the downloaded data will be written.
     * @param range {@link BlobRange}
     * @param parallelTransferOptions {@link ParallelTransferOptions} to use to download to file. Number of parallel
     * transfers parameter is ignored.
     * @param options {@link ReliableDownloadOptions}
     * @param accessConditions {@link BlobRequestConditions}
     * @param rangeGetContentMD5 Whether the contentMD5 for the specified blob range should be returned.
     * @return An empty response
     * @throws IllegalArgumentException If {@code blockSize} is less than 0 or greater than 100MB.
     * @throws UncheckedIOException If an I/O error occurs.
     */
    public Mono<Response<BlobProperties>> downloadToFileWithResponse(String filePath, BlobRange range,
        ParallelTransferOptions parallelTransferOptions, ReliableDownloadOptions options,
        BlobRequestConditions accessConditions, boolean rangeGetContentMD5) {
        try {
            return withContext(context -> downloadToFileWithResponse(filePath, range, parallelTransferOptions, options,
                accessConditions, rangeGetContentMD5, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    // TODO (gapra) : Investigate if this is can be parallelized, and include the parallelTransfers parameter.
    Mono<Response<BlobProperties>> downloadToFileWithResponse(String filePath, BlobRange range,
        ParallelTransferOptions parallelTransferOptions, ReliableDownloadOptions options,
        BlobRequestConditions accessConditions, boolean rangeGetContentMD5, Context context) {
        ParallelTransferOptions finalParallelTransferOptions = new ParallelTransferOptions();
        finalParallelTransferOptions.populateAndApplyDefaults(parallelTransferOptions);

        // See ProgressReporter for an explanation on why this lock is necessary and why we use AtomicLong.
        AtomicLong totalProgress = new AtomicLong(0);
        Lock progressLock = new ReentrantLock();

        return Mono.using(() -> downloadToFileResourceSupplier(filePath),
            channel -> getPropertiesWithResponse(accessConditions)
                .flatMap(response -> processInRange(channel, response,
                range, finalParallelTransferOptions.getBlockSize(), options, accessConditions, rangeGetContentMD5,
                context, totalProgress, progressLock, finalParallelTransferOptions.getProgressReceiver())),
            this::downloadToFileCleanup);
    }

    private Mono<Response<BlobProperties>> processInRange(AsynchronousFileChannel channel,
        Response<BlobProperties> blobPropertiesResponse, BlobRange range, Integer blockSize,
        ReliableDownloadOptions options, BlobRequestConditions accessConditions, boolean rangeGetContentMd5,
        Context context, AtomicLong totalProgress, Lock progressLock, ProgressReceiver progressReceiver) {
        return Mono.justOrEmpty(range).switchIfEmpty(Mono.just(new BlobRange(0,
            blobPropertiesResponse.getValue().getBlobSize()))).flatMapMany(rg ->
            Flux.fromIterable(sliceBlobRange(rg, blockSize)))
            .flatMap(chunk -> this.download(chunk, accessConditions, rangeGetContentMd5, context)
                .subscribeOn(Schedulers.elastic())
                .flatMap(dar -> {
                    Flux<ByteBuffer> progressData = ProgressReporter.addParallelProgressReporting(
                        dar.body(options), progressReceiver, progressLock, totalProgress);

                    return FluxUtil.writeFile(progressData, channel,
                        chunk.getOffset() - ((range == null) ? 0 : range.getOffset()));
                })).then(Mono.just(blobPropertiesResponse));
    }

    private AsynchronousFileChannel downloadToFileResourceSupplier(String filePath) {
        try {
            return AsynchronousFileChannel.open(Paths.get(filePath), StandardOpenOption.READ, StandardOpenOption.WRITE,
                StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            throw logger.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    private void downloadToFileCleanup(AsynchronousFileChannel channel) {
        try {
            channel.close();
        } catch (IOException e) {
            throw logger.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    private List<BlobRange> sliceBlobRange(BlobRange blobRange, Integer blockSize) {
        if (blockSize == null) {
            blockSize = BLOB_DEFAULT_DOWNLOAD_BLOCK_SIZE;
        }
        long offset = blobRange.getOffset();
        long length = blobRange.getCount();
        List<BlobRange> chunks = new ArrayList<>();
        for (long pos = offset; pos < offset + length; pos += blockSize) {
            long count = blockSize;
            if (pos + count > offset + length) {
                count = offset + length - pos;
            }
            chunks.add(new BlobRange(pos, count));
        }
        return chunks;
    }

    /**
     * Deletes the specified blob or snapshot. Note that deleting a blob also deletes all its snapshots.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.delete}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-blob">Azure Docs</a></p>
     *
     * @return A reactive response signalling completion.
     */
    public Mono<Void> delete() {
        try {
            return deleteWithResponse(null, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes the specified blob or snapshot. Note that deleting a blob also deletes all its snapshots.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.deleteWithResponse#DeleteSnapshotsOptionType-BlobRequestConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-blob">Azure Docs</a></p>
     *
     * @param deleteBlobSnapshotOptions Specifies the behavior for deleting the snapshots on this blob. {@code Include}
     * will delete the base blob and all snapshots. {@code Only} will delete only the snapshots. If a snapshot is being
     * deleted, you must pass null.
     * @param accessConditions {@link BlobRequestConditions}
     * @return A reactive response signalling completion.
     */
    public Mono<Response<Void>> deleteWithResponse(DeleteSnapshotsOptionType deleteBlobSnapshotOptions,
        BlobRequestConditions accessConditions) {
        try {
            return withContext(context -> deleteWithResponse(deleteBlobSnapshotOptions, accessConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> deleteWithResponse(DeleteSnapshotsOptionType deleteBlobSnapshotOptions,
        BlobRequestConditions accessConditions, Context context) {
        accessConditions = accessConditions == null ? new BlobRequestConditions() : accessConditions;

        return this.azureBlobStorage.blobs().deleteWithRestResponseAsync(null, null, snapshot, null,
            accessConditions.getLeaseId(), deleteBlobSnapshotOptions, accessConditions.getIfModifiedSince(),
            accessConditions.getIfUnmodifiedSince(), accessConditions.getIfMatch(), accessConditions.getIfNoneMatch(),
            null, context)
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Returns the blob's metadata and properties.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.getProperties}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-properties">Azure Docs</a></p>
     *
     * @return A reactive response containing the blob properties and metadata.
     */
    public Mono<BlobProperties> getProperties() {
        try {
            return getPropertiesWithResponse(null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns the blob's metadata and properties.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.getPropertiesWithResponse#BlobRequestConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-properties">Azure Docs</a></p>
     *
     * @param accessConditions {@link BlobRequestConditions}
     * @return A reactive response containing the blob properties and metadata.
     */
    public Mono<Response<BlobProperties>> getPropertiesWithResponse(BlobRequestConditions accessConditions) {
        try {
            return withContext(context -> getPropertiesWithResponse(accessConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<BlobProperties>> getPropertiesWithResponse(BlobRequestConditions accessConditions, Context context) {
        accessConditions = accessConditions == null ? new BlobRequestConditions() : accessConditions;

        return this.azureBlobStorage.blobs().getPropertiesWithRestResponseAsync(
            null, null, snapshot, null, accessConditions.getLeaseId(), accessConditions.getIfModifiedSince(),
            accessConditions.getIfUnmodifiedSince(), accessConditions.getIfMatch(), accessConditions.getIfNoneMatch(),
            null, customerProvidedKey, context)
            .map(rb -> {
                BlobGetPropertiesHeaders hd = rb.getDeserializedHeaders();
                BlobProperties properties = new BlobProperties(hd.getCreationTime(), hd.getLastModified(), hd.getETag(),
                    hd.getContentLength() == null ? 0 : hd.getContentLength(), hd.getContentType(), hd.getContentMD5(),
                    hd.getContentEncoding(), hd.getContentDisposition(), hd.getContentLanguage(), hd.getCacheControl(),
                    hd.getBlobSequenceNumber(), hd.getBlobType(), hd.getLeaseStatus(), hd.getLeaseState(),
                    hd.getLeaseDuration(), hd.getCopyId(), hd.getCopyStatus(), hd.getCopySource(), hd.getCopyProgress(),
                    hd.getCopyCompletionTime(), hd.getCopyStatusDescription(), hd.isServerEncrypted(),
                    hd.isIncrementalCopy(), hd.getDestinationSnapshot(), AccessTier.fromString(hd.getAccessTier()),
                    hd.isAccessTierInferred(), ArchiveStatus.fromString(hd.getArchiveStatus()),
                    hd.getEncryptionKeySha256(), hd.getAccessTierChangeTime(), hd.getMetadata(),
                    hd.getBlobCommittedBlockCount());
                return new SimpleResponse<>(rb, properties);
            });
    }

    /**
     * Changes a blob's HTTP header properties. if only one HTTP header is updated, the others will all be erased. In
     * order to preserve existing values, they must be passed alongside the header being changed.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.setHttpHeaders#BlobHttpHeaders}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-properties">Azure Docs</a></p>
     *
     * @param headers {@link BlobHttpHeaders}
     * @return A reactive response signalling completion.
     */
    public Mono<Void> setHttpHeaders(BlobHttpHeaders headers) {
        try {
            return setHttpHeadersWithResponse(headers, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Changes a blob's HTTP header properties. if only one HTTP header is updated, the others will all be erased. In
     * order to preserve existing values, they must be passed alongside the header being changed.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.setHttpHeadersWithResponse#BlobHttpHeaders-BlobRequestConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-properties">Azure Docs</a></p>
     *
     * @param headers {@link BlobHttpHeaders}
     * @param accessConditions {@link BlobRequestConditions}
     * @return A reactive response signalling completion.
     */
    public Mono<Response<Void>> setHttpHeadersWithResponse(BlobHttpHeaders headers,
        BlobRequestConditions accessConditions) {
        try {
            return withContext(context -> setHttpHeadersWithResponse(headers, accessConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> setHttpHeadersWithResponse(BlobHttpHeaders headers, BlobRequestConditions accessConditions,
        Context context) {
        accessConditions = accessConditions == null ? new BlobRequestConditions() : accessConditions;

        return this.azureBlobStorage.blobs().setHTTPHeadersWithRestResponseAsync(
            null, null, null, accessConditions.getLeaseId(), accessConditions.getIfModifiedSince(),
            accessConditions.getIfUnmodifiedSince(), accessConditions.getIfMatch(), accessConditions.getIfNoneMatch(),
            null, headers, context)
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Changes a blob's metadata. The specified metadata in this method will replace existing metadata. If old values
     * must be preserved, they must be downloaded and included in the call to this method.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.setMetadata#Map}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-metadata">Azure Docs</a></p>
     *
     * @param metadata Metadata to associate with the blob.
     * @return A reactive response signalling completion.
     */
    public Mono<Void> setMetadata(Map<String, String> metadata) {
        try {
            return setMetadataWithResponse(metadata, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Changes a blob's metadata. The specified metadata in this method will replace existing metadata. If old values
     * must be preserved, they must be downloaded and included in the call to this method.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.setMetadataWithResponse#Map-BlobRequestConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-metadata">Azure Docs</a></p>
     *
     * @param metadata Metadata to associate with the blob.
     * @param accessConditions {@link BlobRequestConditions}
     * @return A reactive response signalling completion.
     */
    public Mono<Response<Void>> setMetadataWithResponse(Map<String, String> metadata,
        BlobRequestConditions accessConditions) {
        try {
            return withContext(context -> setMetadataWithResponse(metadata, accessConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> setMetadataWithResponse(Map<String, String> metadata, BlobRequestConditions accessConditions,
        Context context) {
        accessConditions = accessConditions == null ? new BlobRequestConditions() : accessConditions;

        return this.azureBlobStorage.blobs().setMetadataWithRestResponseAsync(
            null, null, null, metadata, accessConditions.getLeaseId(), accessConditions.getIfModifiedSince(),
            accessConditions.getIfUnmodifiedSince(), accessConditions.getIfMatch(), accessConditions.getIfNoneMatch(),
            null, customerProvidedKey, context)
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Creates a read-only snapshot of the blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.createSnapshot}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/snapshot-blob">Azure Docs</a></p>
     *
     * @return A response containing a {@link BlobAsyncClientBase} which is used to interact with the created snapshot,
     * use {@link #getSnapshotId()} to get the identifier for the snapshot.
     */
    public Mono<BlobAsyncClientBase> createSnapshot() {
        try {
            return createSnapshotWithResponse(null, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a read-only snapshot of the blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.createSnapshotWithResponse#Map-BlobRequestConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/snapshot-blob">Azure Docs</a></p>
     *
     * @param metadata Metadata to associate with the blob snapshot.
     * @param accessConditions {@link BlobRequestConditions}
     * @return A response containing a {@link BlobAsyncClientBase} which is used to interact with the created snapshot,
     * use {@link #getSnapshotId()} to get the identifier for the snapshot.
     */
    public Mono<Response<BlobAsyncClientBase>> createSnapshotWithResponse(Map<String, String> metadata,
        BlobRequestConditions accessConditions) {
        try {
            return withContext(context -> createSnapshotWithResponse(metadata, accessConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<BlobAsyncClientBase>> createSnapshotWithResponse(Map<String, String> metadata,
        BlobRequestConditions accessConditions, Context context) {
        accessConditions = accessConditions == null ? new BlobRequestConditions() : accessConditions;

        return this.azureBlobStorage.blobs().createSnapshotWithRestResponseAsync(
            null, null, null, metadata, accessConditions.getIfModifiedSince(), accessConditions.getIfUnmodifiedSince(),
            accessConditions.getIfMatch(), accessConditions.getIfNoneMatch(), accessConditions.getLeaseId(), null,
            customerProvidedKey, context)
            .map(rb -> new SimpleResponse<>(rb, this.getSnapshotClient(rb.getDeserializedHeaders().getSnapshot())));
    }

    /**
     * Sets the tier on a blob. The operation is allowed on a page blob in a premium storage account or a block blob in
     * a blob storage or GPV2 account. A premium page blob's tier determines the allowed size, IOPS, and bandwidth of
     * the blob. A block blob's tier determines the Hot/Cool/Archive storage type. This does not update the blob's
     * etag.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.setAccessTier#AccessTier}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-tier">Azure Docs</a></p>
     *
     * @param tier The new tier for the blob.
     * @return A reactive response signalling completion.
     * @throws NullPointerException if {@code tier} is null.
     */
    public Mono<Void> setAccessTier(AccessTier tier) {
        try {
            return setAccessTierWithResponse(tier, null, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Sets the tier on a blob. The operation is allowed on a page blob in a premium storage account or a block blob in
     * a blob storage or GPV2 account. A premium page blob's tier determines the allowed size, IOPS, and bandwidth of
     * the blob. A block blob's tier determines the Hot/Cool/Archive storage type. This does not update the blob's
     * etag.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.setAccessTierWithResponse#AccessTier-RehydratePriority-String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-tier">Azure Docs</a></p>
     *
     * @param tier The new tier for the blob.
     * @param priority Optional priority to set for re-hydrating blobs.
     * @param leaseId The lease ID the active lease on the blob must match.
     * @return A reactive response signalling completion.
     * @throws NullPointerException if {@code tier} is null.
     */
    public Mono<Response<Void>> setAccessTierWithResponse(AccessTier tier, RehydratePriority priority, String leaseId) {
        try {
            return withContext(context -> setTierWithResponse(tier, priority, leaseId, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> setTierWithResponse(AccessTier tier, RehydratePriority priority, String leaseId,
        Context context) {
        StorageImplUtils.assertNotNull("tier", tier);

        return this.azureBlobStorage.blobs().setTierWithRestResponseAsync(
            null, null, tier, null, priority, null, leaseId, context)
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Undelete restores the content and metadata of a soft-deleted blob and/or any associated soft-deleted snapshots.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.undelete}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/undelete-blob">Azure Docs</a></p>
     *
     * @return A reactive response signalling completion.
     */
    public Mono<Void> undelete() {
        try {
            return undeleteWithResponse().flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Undelete restores the content and metadata of a soft-deleted blob and/or any associated soft-deleted snapshots.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.undeleteWithResponse}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/undelete-blob">Azure Docs</a></p>
     *
     * @return A reactive response signalling completion.
     */
    public Mono<Response<Void>> undeleteWithResponse() {
        try {
            return withContext(this::undeleteWithResponse);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> undeleteWithResponse(Context context) {
        return this.azureBlobStorage.blobs().undeleteWithRestResponseAsync(null,
            null, context).map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Returns the sku name and account kind for the account.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.getAccountInfo}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-account-information">Azure Docs</a></p>
     *
     * @return a reactor response containing the sku name and account kind.
     */
    public Mono<StorageAccountInfo> getAccountInfo() {
        try {
            return getAccountInfoWithResponse().flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns the sku name and account kind for the account.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.getAccountInfoWithResponse}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-account-information">Azure Docs</a></p>
     *
     * @return a reactor response containing the sku name and account kind.
     */
    public Mono<Response<StorageAccountInfo>> getAccountInfoWithResponse() {
        try {
            return withContext(this::getAccountInfoWithResponse);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<StorageAccountInfo>> getAccountInfoWithResponse(Context context) {
        return this.azureBlobStorage.blobs().getAccountInfoWithRestResponseAsync(null, null, context)
            .map(rb -> {
                BlobGetAccountInfoHeaders hd = rb.getDeserializedHeaders();
                return new SimpleResponse<>(rb, new StorageAccountInfo(hd.getSkuName(), hd.getAccountKind()));
            });
    }

    private class BlobPropertyAccessor implements BlobHelper.AsyncPropertyAccessor {
        @Override
        public AzureBlobStorageImpl getAzureBlobStorageImpl(final BlobAsyncClientBase client) {
            return client.azureBlobStorage;
        }

        @Override
        public CpkInfo getCustomerProvidedKey(final BlobAsyncClientBase client) {
            return client.customerProvidedKey;
        }

        @Override
        public BlobServiceVersion getServiceVersion(BlobAsyncClientBase client) {
            return client.serviceVersion;
        }
    }
}
