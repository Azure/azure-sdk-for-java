// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.RequestConditions;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.HttpGetterInfo;
import com.azure.storage.blob.ProgressReporter;
import com.azure.storage.blob.implementation.AzureBlobStorageBuilder;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.implementation.models.BlobGetAccountInfoHeaders;
import com.azure.storage.blob.implementation.models.BlobGetPropertiesHeaders;
import com.azure.storage.blob.implementation.models.BlobStartCopyFromURLHeaders;
import com.azure.storage.blob.implementation.models.EncryptionScope;
import com.azure.storage.blob.implementation.util.BlobSasImplUtil;
import com.azure.storage.blob.implementation.util.ModelHelper;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.ArchiveStatus;
import com.azure.storage.blob.models.BlobCopyInfo;
import com.azure.storage.blob.models.BlobDownloadAsyncResponse;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.CopyStatusType;
import com.azure.storage.blob.models.CpkInfo;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.storage.blob.models.DownloadRetryOptions;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.models.RehydratePriority;
import com.azure.storage.blob.models.StorageAccountInfo;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.Utility;
import com.azure.storage.common.implementation.SasImplUtils;
import com.azure.storage.common.implementation.StorageImplUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple3;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.azure.core.util.FluxUtil.fluxError;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;
import static com.azure.storage.common.Utility.STORAGE_TRACING_NAMESPACE_VALUE;
import static java.lang.StrictMath.toIntExact;

/**
 * This class provides a client that contains all operations that apply to any blob type.
 *
 * <p>
 * This client offers the ability to download blobs. Note that uploading data is specific to each type of blob. Please
 * refer to the {@link BlockBlobClient}, {@link PageBlobClient}, or {@link AppendBlobClient} for upload options.
 */
public class BlobAsyncClientBase {

    private final ClientLogger logger = new ClientLogger(BlobAsyncClientBase.class);

    protected final AzureBlobStorageImpl azureBlobStorage;
    private final String snapshot;
    private final CpkInfo customerProvidedKey;
    protected final EncryptionScope encryptionScope;
    protected final String accountName;
    protected final String containerName;
    protected final String blobName;
    protected final BlobServiceVersion serviceVersion;

    /**
     * Protected constructor for use by {@link SpecializedBlobClientBuilder}.
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
        this(pipeline, url, serviceVersion, accountName, containerName, blobName, snapshot, customerProvidedKey, null);
    }

    /**
     * Protected constructor for use by {@link SpecializedBlobClientBuilder}.
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
     * @param encryptionScope Encryption scope used during encryption of the blob's data on the server, pass
     * {@code null} to allow the service to use its own encryption.
     */
    protected BlobAsyncClientBase(HttpPipeline pipeline, String url, BlobServiceVersion serviceVersion,
        String accountName, String containerName, String blobName, String snapshot, CpkInfo customerProvidedKey,
        EncryptionScope encryptionScope) {
        this.azureBlobStorage = new AzureBlobStorageBuilder()
            .pipeline(pipeline)
            .url(url)
            .version(serviceVersion.getVersion())
            .build();
        this.serviceVersion = serviceVersion;

        this.accountName = accountName;
        this.containerName = containerName;
        this.blobName = Utility.urlEncode(Utility.urlDecode(blobName));
        this.snapshot = snapshot;
        this.customerProvidedKey = customerProvidedKey;
        this.encryptionScope = encryptionScope;
    }

    /**
     * Gets the {@code encryption scope} used to encrypt this blob's content on the server.
     *
     * @return the encryption scope used for encryption.
     */
    protected String getEncryptionScope() {
        if (encryptionScope == null) {
            return null;
        }
        return encryptionScope.getEncryptionScope();
    }

    /**
     * Creates a new {@link BlobAsyncClientBase} linked to the {@code snapshot} of this blob resource.
     *
     * @param snapshot the identifier for a specific snapshot of this blob
     * @return a {@link BlobAsyncClientBase} used to interact with the specific snapshot.
     */
    public BlobAsyncClientBase getSnapshotClient(String snapshot) {
        return new BlobAsyncClientBase(getHttpPipeline(), getBlobUrl(), getServiceVersion(), getAccountName(),
            getContainerName(), getBlobName(), snapshot, getCustomerProvidedKey(), encryptionScope);
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
     * Decodes and gets the blob name.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.getBlobName}
     *
     * @return The decoded name of the blob.
     */
    public final String getBlobName() {
        return (blobName == null) ? null : Utility.urlDecode(blobName);
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
     * Gets the {@link CpkInfo} used to encrypt this blob's content on the server.
     *
     * @return the customer provided key used for encryption.
     */
    public CpkInfo getCustomerProvidedKey() {
        return customerProvidedKey;
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
     * Gets the service version the client is using.
     *
     * @return the service version the client is using.
     */
    public BlobServiceVersion getServiceVersion() {
        return serviceVersion;
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
     * @return A {@link PollerFlux} that polls the blob copy operation until it has completed, has failed, or has been
     * cancelled.
     */
    public PollerFlux<BlobCopyInfo, Void> beginCopy(String sourceUrl, Duration pollInterval) {
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
     * @param sourceModifiedRequestConditions {@link RequestConditions} against the source. Standard HTTP Access
     * conditions related to the modification of data. ETag and LastModifiedTime are used to construct conditions
     * related to when the blob was changed relative to the given request. The request will fail if the specified
     * condition is not satisfied.
     * @param destRequestConditions {@link BlobRequestConditions} against the destination.
     * @param pollInterval Duration between each poll for the copy status. If none is specified, a default of one second
     * is used.
     * @return A {@link PollerFlux} that polls the blob copy operation until it has completed, has failed, or has been
     * cancelled.
     */
    public PollerFlux<BlobCopyInfo, Void> beginCopy(String sourceUrl, Map<String, String> metadata, AccessTier tier,
        RehydratePriority priority, RequestConditions sourceModifiedRequestConditions,
        BlobRequestConditions destRequestConditions, Duration pollInterval) {

        final Duration interval = pollInterval != null ? pollInterval : Duration.ofSeconds(1);
        final RequestConditions sourceModifiedCondition = sourceModifiedRequestConditions == null
            ? new RequestConditions()
            : sourceModifiedRequestConditions;
        final BlobRequestConditions destinationRequestConditions = destRequestConditions == null
            ? new BlobRequestConditions()
            : destRequestConditions;

        // We want to hide the SourceAccessConditions type from the user for consistency's sake, so we convert here.
        final RequestConditions sourceConditions = new RequestConditions()
            .setIfModifiedSince(sourceModifiedCondition.getIfModifiedSince())
            .setIfUnmodifiedSince(sourceModifiedCondition.getIfUnmodifiedSince())
            .setIfMatch(sourceModifiedCondition.getIfMatch())
            .setIfNoneMatch(sourceModifiedCondition.getIfNoneMatch());

        return new PollerFlux<>(interval,
            (pollingContext) -> {
                try {
                    return onStart(sourceUrl, metadata, tier, priority, sourceConditions, destinationRequestConditions);
                } catch (RuntimeException ex) {
                    return monoError(logger, ex);
                }
            },
            (pollingContext) -> {
                try {
                    return onPoll(pollingContext.getLatestResponse());
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

                    return abortCopyFromUrl(copyIdentifier).thenReturn(firstResponse.getValue());
                }

                return Mono.empty();
            },
            (pollingContext) -> Mono.empty());
    }

    private Mono<BlobCopyInfo> onStart(String sourceUrl, Map<String, String> metadata, AccessTier tier,
        RehydratePriority priority, RequestConditions sourceModifiedRequestConditions,
        BlobRequestConditions destinationRequestConditions) {
        URL url;
        try {
            url = new URL(sourceUrl);
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'sourceUrl' is not a valid url.", ex));
        }

        return withContext(
            context -> azureBlobStorage.blobs().startCopyFromURLWithRestResponseAsync(null, null, url, null, metadata,
                tier, priority, sourceModifiedRequestConditions.getIfModifiedSince(),
                sourceModifiedRequestConditions.getIfUnmodifiedSince(), sourceModifiedRequestConditions.getIfMatch(),
                sourceModifiedRequestConditions.getIfNoneMatch(), destinationRequestConditions.getIfModifiedSince(),
                destinationRequestConditions.getIfUnmodifiedSince(), destinationRequestConditions.getIfMatch(),
                destinationRequestConditions.getIfNoneMatch(), destinationRequestConditions.getLeaseId(), null,
                context))
            .map(response -> {
                final BlobStartCopyFromURLHeaders headers = response.getDeserializedHeaders();

                return new BlobCopyInfo(sourceUrl, headers.getCopyId(), headers.getCopyStatus(),
                    headers.getETag(), headers.getLastModified(), headers.getErrorCode());
            });
    }

    private Mono<PollResponse<BlobCopyInfo>> onPoll(PollResponse<BlobCopyInfo> pollResponse) {
        if (pollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED
            || pollResponse.getStatus() == LongRunningOperationStatus.FAILED) {
            return Mono.just(pollResponse);
        }

        final BlobCopyInfo lastInfo = pollResponse.getValue();
        if (lastInfo == null) {
            logger.warning("BlobCopyInfo does not exist. Activation operation failed.");
            return Mono.just(new PollResponse<>(
                LongRunningOperationStatus.fromString("COPY_START_FAILED", true), null));
        }

        return getProperties().map(response -> {
            final CopyStatusType status = response.getCopyStatus();
            final BlobCopyInfo result = new BlobCopyInfo(response.getCopySource(), response.getCopyId(), status,
                response.getETag(), response.getCopyCompletionTime(), response.getCopyStatusDescription());

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
        }).onErrorReturn(
            new PollResponse<>(LongRunningOperationStatus.fromString("POLLING_FAILED", true), lastInfo));
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
     * @param copyId The id of the copy operation to abort.
     * @return A reactive response signalling completion.
     * @see #copyFromUrl(String)
     * @see #beginCopy(String, Duration)
     * @see #beginCopy(String, Map, AccessTier, RehydratePriority, RequestConditions, BlobRequestConditions, Duration)
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
     * @param copyId The id of the copy operation to abort.
     * @param leaseId The lease ID the active lease on the blob must match.
     * @return A reactive response signalling completion.
     * @see #copyFromUrl(String)
     * @see #beginCopy(String, Duration)
     * @see #beginCopy(String, Map, AccessTier, RehydratePriority, RequestConditions, BlobRequestConditions, Duration)
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
     * @param sourceModifiedRequestConditions {@link RequestConditions} against the source. Standard HTTP Access
     * conditions related to the modification of data. ETag and LastModifiedTime are used to construct conditions
     * related to when the blob was changed relative to the given request. The request will fail if the specified
     * condition is not satisfied.
     * @param destRequestConditions {@link BlobRequestConditions} against the destination.
     * @return A reactive response containing the copy ID for the long running operation.
     */
    public Mono<Response<String>> copyFromUrlWithResponse(String copySource, Map<String, String> metadata,
        AccessTier tier, RequestConditions sourceModifiedRequestConditions,
        BlobRequestConditions destRequestConditions) {
        try {
            return withContext(context -> copyFromUrlWithResponse(copySource, metadata, tier,
                sourceModifiedRequestConditions, destRequestConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<String>> copyFromUrlWithResponse(String copySource, Map<String, String> metadata, AccessTier tier,
        RequestConditions sourceModifiedRequestConditions, BlobRequestConditions destRequestConditions,
        Context context) {
        sourceModifiedRequestConditions = sourceModifiedRequestConditions == null
            ? new RequestConditions() : sourceModifiedRequestConditions;
        destRequestConditions = destRequestConditions == null ? new BlobRequestConditions() : destRequestConditions;

        URL url;
        try {
            url = new URL(copySource);
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'copySource' is not a valid url."));
        }

        return this.azureBlobStorage.blobs().copyFromURLWithRestResponseAsync(
            null, null, url, null, metadata, tier, sourceModifiedRequestConditions.getIfModifiedSince(),
            sourceModifiedRequestConditions.getIfUnmodifiedSince(), sourceModifiedRequestConditions.getIfMatch(),
            sourceModifiedRequestConditions.getIfNoneMatch(), destRequestConditions.getIfModifiedSince(),
            destRequestConditions.getIfUnmodifiedSince(), destRequestConditions.getIfMatch(),
            destRequestConditions.getIfNoneMatch(), destRequestConditions.getLeaseId(), null, null, context)
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
                .flatMapMany(BlobDownloadAsyncResponse::getValue);
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
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.downloadWithResponse#BlobRange-DownloadRetryOptions-BlobRequestConditions-boolean}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param range {@link BlobRange}
     * @param options {@link DownloadRetryOptions}
     * @param requestConditions {@link BlobRequestConditions}
     * @param getRangeContentMd5 Whether the contentMD5 for the specified blob range should be returned.
     * @return A reactive response containing the blob data.
     */
    public Mono<BlobDownloadAsyncResponse> downloadWithResponse(BlobRange range, DownloadRetryOptions options,
        BlobRequestConditions requestConditions, boolean getRangeContentMd5) {
        try {
            return withContext(context ->
                downloadWithResponse(range, options, requestConditions, getRangeContentMd5,
                    context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<BlobDownloadAsyncResponse> downloadWithResponse(BlobRange range, DownloadRetryOptions options,
        BlobRequestConditions requestConditions, boolean getRangeContentMd5, Context context) {
        return downloadHelper(range, options, requestConditions, getRangeContentMd5, context)
            .map(response -> new BlobDownloadAsyncResponse(response.getRequest(), response.getStatusCode(),
                response.getHeaders(), response.getValue(), response.getDeserializedHeaders()));
    }

    private Mono<ReliableDownload> downloadHelper(BlobRange range, DownloadRetryOptions options,
        BlobRequestConditions requestConditions, boolean getRangeContentMd5, Context context) {
        range = range == null ? new BlobRange(0) : range;
        Boolean getMD5 = getRangeContentMd5 ? getRangeContentMd5 : null;
        requestConditions = requestConditions == null ? new BlobRequestConditions() : requestConditions;
        HttpGetterInfo info = new HttpGetterInfo()
            .setOffset(range.getOffset())
            .setCount(range.getCount())
            .setETag(requestConditions.getIfMatch());

        return azureBlobStorage.blobs().downloadWithRestResponseAsync(null, null, snapshot, null, range.toHeaderValue(),
            requestConditions.getLeaseId(), getMD5, null, requestConditions.getIfModifiedSince(),
            requestConditions.getIfUnmodifiedSince(), requestConditions.getIfMatch(),
            requestConditions.getIfNoneMatch(), null, customerProvidedKey, context)
            .map(response -> {
                info.setETag(response.getDeserializedHeaders().getETag());
                return new ReliableDownload(response, options, info, updatedInfo ->
                    downloadHelper(new BlobRange(updatedInfo.getOffset(), updatedInfo.getCount()), options,
                        new BlobRequestConditions().setIfMatch(info.getETag()), false, context));
            });
    }

    /**
     * Downloads the entire blob into a file specified by the path.
     *
     * <p>The file will be created and must not exist, if the file already exists a {@link FileAlreadyExistsException}
     * will be thrown.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.downloadToFile#String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param filePath A {@link String} representing the filePath where the downloaded data will be written.
     * @return A reactive response containing the blob properties and metadata.
     */
    public Mono<BlobProperties> downloadToFile(String filePath) {
        return downloadToFile(filePath, false);
    }

    /**
     * Downloads the entire blob into a file specified by the path.
     *
     * <p>If overwrite is set to false, the file will be created and must not exist, if the file already exists a
     * {@link FileAlreadyExistsException} will be thrown.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.downloadToFile#String-boolean}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param filePath A {@link String} representing the filePath where the downloaded data will be written.
     * @param overwrite Whether or not to overwrite the file, should the file exist.
     * @return A reactive response containing the blob properties and metadata.
     */
    public Mono<BlobProperties> downloadToFile(String filePath, boolean overwrite) {
        try {
            Set<OpenOption> openOptions = null;
            if (overwrite) {
                openOptions = new HashSet<>();
                openOptions.add(StandardOpenOption.CREATE);
                openOptions.add(StandardOpenOption.TRUNCATE_EXISTING); // If the file already exists and it is opened
                // for WRITE access, then its length is truncated to 0.
                openOptions.add(StandardOpenOption.READ);
                openOptions.add(StandardOpenOption.WRITE);
            }
            return downloadToFileWithResponse(filePath, null, null, null, null, false, openOptions)
                .flatMap(FluxUtil::toMono);
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
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.downloadToFileWithResponse#String-BlobRange-ParallelTransferOptions-DownloadRetryOptions-BlobRequestConditions-boolean}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param filePath A {@link String} representing the filePath where the downloaded data will be written.
     * @param range {@link BlobRange}
     * @param parallelTransferOptions {@link ParallelTransferOptions} to use to download to file. Number of parallel
     * transfers parameter is ignored.
     * @param options {@link DownloadRetryOptions}
     * @param requestConditions {@link BlobRequestConditions}
     * @param rangeGetContentMd5 Whether the contentMD5 for the specified blob range should be returned.
     * @return A reactive response containing the blob properties and metadata.
     * @throws IllegalArgumentException If {@code blockSize} is less than 0 or greater than 100MB.
     * @throws UncheckedIOException If an I/O error occurs.
     */
    public Mono<Response<BlobProperties>> downloadToFileWithResponse(String filePath, BlobRange range,
        ParallelTransferOptions parallelTransferOptions, DownloadRetryOptions options,
        BlobRequestConditions requestConditions, boolean rangeGetContentMd5) {
        return downloadToFileWithResponse(filePath, range, parallelTransferOptions, options, requestConditions,
            rangeGetContentMd5, null);
    }

    /**
     * Downloads the entire blob into a file specified by the path.
     *
     * <p>By default the file will be created and must not exist, if the file already exists a
     * {@link FileAlreadyExistsException} will be thrown. To override this behavior, provide appropriate
     * {@link OpenOption OpenOptions} </p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.downloadToFileWithResponse#String-BlobRange-ParallelTransferOptions-DownloadRetryOptions-BlobRequestConditions-boolean-Set}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param filePath A {@link String} representing the filePath where the downloaded data will be written.
     * @param range {@link BlobRange}
     * @param parallelTransferOptions {@link ParallelTransferOptions} to use to download to file. Number of parallel
     * transfers parameter is ignored.
     * @param options {@link DownloadRetryOptions}
     * @param requestConditions {@link BlobRequestConditions}
     * @param rangeGetContentMd5 Whether the contentMD5 for the specified blob range should be returned.
     * @param openOptions {@link OpenOption OpenOptions} to use to configure how to open or create the file.
     * @return A reactive response containing the blob properties and metadata.
     * @throws IllegalArgumentException If {@code blockSize} is less than 0 or greater than 100MB.
     * @throws UncheckedIOException If an I/O error occurs.
     */
    public Mono<Response<BlobProperties>> downloadToFileWithResponse(String filePath, BlobRange range,
        ParallelTransferOptions parallelTransferOptions, DownloadRetryOptions options,
        BlobRequestConditions requestConditions, boolean rangeGetContentMd5, Set<OpenOption> openOptions) {
        try {
            return withContext(context ->
                downloadToFileWithResponse(filePath, range, parallelTransferOptions, options,
                    requestConditions, rangeGetContentMd5, openOptions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<BlobProperties>> downloadToFileWithResponse(String filePath, BlobRange range,
        ParallelTransferOptions parallelTransferOptions, DownloadRetryOptions downloadRetryOptions,
        BlobRequestConditions requestConditions, boolean rangeGetContentMd5, Set<OpenOption> openOptions,
        Context context) {
        BlobRange finalRange = range == null ? new BlobRange(0) : range;
        final ParallelTransferOptions finalParallelTransferOptions =
            ModelHelper.populateAndApplyDefaults(parallelTransferOptions);
        BlobRequestConditions finalConditions = requestConditions == null
            ? new BlobRequestConditions() : requestConditions;

        // Default behavior is not to overwrite
        if (openOptions == null) {
            openOptions = new HashSet<>();
            openOptions.add(StandardOpenOption.CREATE_NEW);
            openOptions.add(StandardOpenOption.WRITE);
            openOptions.add(StandardOpenOption.READ);
        }

        AsynchronousFileChannel channel = downloadToFileResourceSupplier(filePath, openOptions);
        return Mono.just(channel)
            .flatMap(c -> this.downloadToFileImpl(c, finalRange, finalParallelTransferOptions,
                downloadRetryOptions, finalConditions, rangeGetContentMd5, context))
            .doFinally(signalType -> this.downloadToFileCleanup(channel, filePath, signalType));
    }

    private AsynchronousFileChannel downloadToFileResourceSupplier(String filePath, Set<OpenOption> openOptions) {
        try {
            return AsynchronousFileChannel.open(Paths.get(filePath), openOptions, null);
        } catch (IOException e) {
            throw logger.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    private Mono<Response<BlobProperties>> downloadToFileImpl(AsynchronousFileChannel file, BlobRange finalRange,
        ParallelTransferOptions finalParallelTransferOptions, DownloadRetryOptions downloadRetryOptions,
        BlobRequestConditions requestConditions, boolean rangeGetContentMd5, Context context) {
        // See ProgressReporter for an explanation on why this lock is necessary and why we use AtomicLong.
        Lock progressLock = new ReentrantLock();
        AtomicLong totalProgress = new AtomicLong(0);

        /*
         * Downloads the first chunk and gets the size of the data and etag if not specified by the user.
         */
        return getSetupMono(finalRange, finalParallelTransferOptions, downloadRetryOptions, requestConditions,
            rangeGetContentMd5, context)
            .flatMap(setupTuple3 -> {
                long newCount = setupTuple3.getT1();
                BlobRequestConditions finalConditions = setupTuple3.getT2();

                int numChunks = calculateNumBlocks(newCount, finalParallelTransferOptions.getBlockSize());

                // In case it is an empty blob, this ensures we still actually perform a download operation.
                numChunks = numChunks == 0 ? 1 : numChunks;

                BlobDownloadAsyncResponse initialResponse = setupTuple3.getT3();
                return Flux.range(0, numChunks)
                    .flatMap(chunkNum -> {
                        // The first chunk was retrieved during setup.
                        if (chunkNum == 0) {
                            return writeBodyToFile(initialResponse, file, 0, finalParallelTransferOptions, progressLock,
                                totalProgress);
                        }

                        // Calculate whether we need a full chunk or something smaller because we are at the end.
                        long modifier = chunkNum.longValue() * finalParallelTransferOptions.getBlockSize();
                        long chunkSizeActual = Math.min(finalParallelTransferOptions.getBlockSize(),
                            newCount - modifier);
                        BlobRange chunkRange = new BlobRange(finalRange.getOffset() + modifier, chunkSizeActual);

                        // Make the download call.
                        return this.downloadWithResponse(chunkRange, downloadRetryOptions, finalConditions,
                            rangeGetContentMd5, null)
                            .subscribeOn(Schedulers.elastic())
                            .flatMap(response ->
                                writeBodyToFile(response, file, chunkNum, finalParallelTransferOptions, progressLock,
                                    totalProgress));
                    })
                    // Only the first download call returns a value.
                    .then(Mono.just(buildBlobPropertiesResponse(initialResponse)));
            });
    }

    private int calculateNumBlocks(long dataSize, long blockLength) {
        // Can successfully cast to an int because MaxBlockSize is an int, which this expression must be less than.
        int numBlocks = toIntExact(dataSize / blockLength);
        // Include an extra block for trailing data.
        if (dataSize % blockLength != 0) {
            numBlocks++;
        }
        return numBlocks;
    }

    /*
    Download the first chunk. Construct a Mono which will emit the total count for calculating the number of chunks,
    access conditions containing the etag to lock on, and the response from downloading the first chunk.
     */
    private Mono<Tuple3<Long, BlobRequestConditions, BlobDownloadAsyncResponse>> getSetupMono(BlobRange range,
        ParallelTransferOptions parallelTransferOptions, DownloadRetryOptions downloadRetryOptions,
        BlobRequestConditions requestConditions, boolean rangeGetContentMd5, Context context) {
        // We will scope our initial download to either be one chunk or the total size.
        long initialChunkSize = range.getCount() != null && range.getCount() < parallelTransferOptions.getBlockSize()
            ? range.getCount() : parallelTransferOptions.getBlockSize();

        return this.downloadWithResponse(new BlobRange(range.getOffset(), initialChunkSize), downloadRetryOptions,
            requestConditions, rangeGetContentMd5, context)
            .subscribeOn(Schedulers.elastic())
            .flatMap(response -> {
                /*
                Either the etag was set and it matches because the download succeeded, so this is a no-op, or there
                was no etag, so we set it here. ETag locking is vital to ensure we download one, consistent view
                of the file.
                 */
                BlobRequestConditions newConditions = setEtag(requestConditions,
                    response.getDeserializedHeaders().getETag());

                // Extract the total length of the blob from the contentRange header. e.g. "bytes 1-6/7"
                long totalLength = extractTotalBlobLength(response.getDeserializedHeaders().getContentRange());

                /*
                If the user either didn't specify a count or they specified a count greater than the size of the
                remaining data, take the size of the remaining data. This is to prevent the case where the count
                is much much larger than the size of the blob and we could try to download at an invalid offset.
                 */
                long newCount = range.getCount() == null || range.getCount() > (totalLength - range.getOffset())
                    ? totalLength - range.getOffset() : range.getCount();

                return Mono.zip(Mono.just(newCount), Mono.just(newConditions), Mono.just(response));
            })
            .onErrorResume(BlobStorageException.class, blobStorageException -> {
                /*
                 * In the case of an empty blob, we still want to report success and give back valid headers.
                 * Attempting a range download on an empty blob will return an InvalidRange error code and a
                 * Content-Range header of the format "bytes * /0". We need to double check that the total size is zero
                 * in the case that the customer has attempted an invalid range on a non-zero length blob.
                 */
                if (blobStorageException.getErrorCode() == BlobErrorCode.INVALID_RANGE
                    && extractTotalBlobLength(blobStorageException.getResponse()
                    .getHeaders().getValue("Content-Range")) == 0) {

                    return this.downloadWithResponse(new BlobRange(0, 0L), downloadRetryOptions, requestConditions,
                        rangeGetContentMd5, context)
                        .subscribeOn(Schedulers.elastic())
                        .flatMap(response -> {
                            /*
                            Ensure the blob is still 0 length by checking our download was the full length.
                            (200 is for full blob; 206 is partial).
                             */
                            if (response.getStatusCode() != 200) {
                                Mono.error(new IllegalStateException("Blob was modified mid download. It was "
                                    + "originally 0 bytes and is now larger."));
                            }
                            return Mono.zip(Mono.just(0L), Mono.just(requestConditions), Mono.just(response));
                        });
                }

                return Mono.error(blobStorageException);
            });
    }

    private static BlobRequestConditions setEtag(BlobRequestConditions requestConditions, String etag) {
        //We don't want to modify the user's object, so we'll create a duplicate and set the retrieved etag.
        return new BlobRequestConditions()
            .setIfModifiedSince(
                requestConditions.getIfModifiedSince())
            .setIfUnmodifiedSince(
                requestConditions.getIfModifiedSince())
            .setIfMatch(etag)
            .setIfNoneMatch(
                requestConditions.getIfNoneMatch())
            .setLeaseId(requestConditions.getLeaseId());
    }

    private static Mono<Void> writeBodyToFile(BlobDownloadAsyncResponse response, AsynchronousFileChannel file,
        long chunkNum, ParallelTransferOptions finalParallelTransferOptions, Lock progressLock,
        AtomicLong totalProgress) {

        // Extract the body.
        Flux<ByteBuffer> data = response.getValue();

        // Report progress as necessary.
        data = ProgressReporter.addParallelProgressReporting(data,
            finalParallelTransferOptions.getProgressReceiver(), progressLock, totalProgress);

        // Write to the file.
        return FluxUtil.writeFile(data, file, chunkNum * finalParallelTransferOptions.getBlockSize());
    }

    private static Response<BlobProperties> buildBlobPropertiesResponse(BlobDownloadAsyncResponse response) {
        // blobSize determination - contentLength only returns blobSize if the download is not chunked.
        long blobSize = response.getDeserializedHeaders().getContentRange() == null
            ? response.getDeserializedHeaders().getContentLength()
            : extractTotalBlobLength(response.getDeserializedHeaders().getContentRange());
        BlobProperties properties = new BlobProperties(null, response.getDeserializedHeaders().getLastModified(),
            response.getDeserializedHeaders().getETag(), blobSize, response.getDeserializedHeaders().getContentType(),
            null, response.getDeserializedHeaders().getContentEncoding(),
            response.getDeserializedHeaders().getContentDisposition(),
            response.getDeserializedHeaders().getContentLanguage(), response.getDeserializedHeaders().getCacheControl(),
            response.getDeserializedHeaders().getBlobSequenceNumber(), response.getDeserializedHeaders().getBlobType(),
            response.getDeserializedHeaders().getLeaseStatus(), response.getDeserializedHeaders().getLeaseState(),
            response.getDeserializedHeaders().getLeaseDuration(), response.getDeserializedHeaders().getCopyId(),
            response.getDeserializedHeaders().getCopyStatus(), response.getDeserializedHeaders().getCopySource(),
            response.getDeserializedHeaders().getCopyProgress(),
            response.getDeserializedHeaders().getCopyCompletionTime(),
            response.getDeserializedHeaders().getCopyStatusDescription(),
            response.getDeserializedHeaders().isServerEncrypted(), null, null, null, null, null,
            response.getDeserializedHeaders().getEncryptionKeySha256(), null,
            response.getDeserializedHeaders().getMetadata(),
            response.getDeserializedHeaders().getBlobCommittedBlockCount());
        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), properties);
    }

    private static long extractTotalBlobLength(String contentRange) {
        return Long.parseLong(contentRange.split("/")[1]);
    }

    private void downloadToFileCleanup(AsynchronousFileChannel channel, String filePath, SignalType signalType) {
        try {
            channel.close();
            if (!signalType.equals(SignalType.ON_COMPLETE)) {
                Files.deleteIfExists(Paths.get(filePath));
                logger.verbose("Downloading to file failed. Cleaning up resources.");
            }
        } catch (IOException e) {
            throw logger.logExceptionAsError(new UncheckedIOException(e));
        }
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
     * @param requestConditions {@link BlobRequestConditions}
     * @return A reactive response signalling completion.
     */
    public Mono<Response<Void>> deleteWithResponse(DeleteSnapshotsOptionType deleteBlobSnapshotOptions,
        BlobRequestConditions requestConditions) {
        try {
            return withContext(context -> deleteWithResponse(deleteBlobSnapshotOptions,
                requestConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> deleteWithResponse(DeleteSnapshotsOptionType deleteBlobSnapshotOptions,
        BlobRequestConditions requestConditions, Context context) {
        requestConditions = requestConditions == null ? new BlobRequestConditions() : requestConditions;

        return this.azureBlobStorage.blobs().deleteWithRestResponseAsync(null, null, snapshot, null,
            requestConditions.getLeaseId(), deleteBlobSnapshotOptions, requestConditions.getIfModifiedSince(),
            requestConditions.getIfUnmodifiedSince(), requestConditions.getIfMatch(),
            requestConditions.getIfNoneMatch(), null, context)
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
     * @param requestConditions {@link BlobRequestConditions}
     * @return A reactive response containing the blob properties and metadata.
     */
    public Mono<Response<BlobProperties>> getPropertiesWithResponse(BlobRequestConditions requestConditions) {
        try {
            return withContext(context -> getPropertiesWithResponse(requestConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<BlobProperties>> getPropertiesWithResponse(BlobRequestConditions requestConditions, Context context) {
        requestConditions = requestConditions == null ? new BlobRequestConditions() : requestConditions;
        context = context == null ? Context.NONE : context;

        return this.azureBlobStorage.blobs().getPropertiesWithRestResponseAsync(
            null, null, snapshot, null, requestConditions.getLeaseId(), requestConditions.getIfModifiedSince(),
            requestConditions.getIfUnmodifiedSince(), requestConditions.getIfMatch(),
            requestConditions.getIfNoneMatch(), null, customerProvidedKey,
            context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
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
     * @param requestConditions {@link BlobRequestConditions}
     * @return A reactive response signalling completion.
     */
    public Mono<Response<Void>> setHttpHeadersWithResponse(BlobHttpHeaders headers,
        BlobRequestConditions requestConditions) {
        try {
            return withContext(context -> setHttpHeadersWithResponse(headers, requestConditions,
                context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> setHttpHeadersWithResponse(BlobHttpHeaders headers, BlobRequestConditions requestConditions,
        Context context) {
        requestConditions = requestConditions == null ? new BlobRequestConditions() : requestConditions;
        context = context == null ? Context.NONE : context;

        return this.azureBlobStorage.blobs().setHTTPHeadersWithRestResponseAsync(
            null, null, null, requestConditions.getLeaseId(), requestConditions.getIfModifiedSince(),
            requestConditions.getIfUnmodifiedSince(), requestConditions.getIfMatch(),
            requestConditions.getIfNoneMatch(), null, headers,
            context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
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
     * @param requestConditions {@link BlobRequestConditions}
     * @return A reactive response signalling completion.
     */
    public Mono<Response<Void>> setMetadataWithResponse(Map<String, String> metadata,
        BlobRequestConditions requestConditions) {
        try {
            return withContext(context -> setMetadataWithResponse(metadata, requestConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> setMetadataWithResponse(Map<String, String> metadata, BlobRequestConditions requestConditions,
        Context context) {
        requestConditions = requestConditions == null ? new BlobRequestConditions() : requestConditions;
        context = context == null ? Context.NONE : context;

        return this.azureBlobStorage.blobs().setMetadataWithRestResponseAsync(
            null, null, null, metadata, requestConditions.getLeaseId(), requestConditions.getIfModifiedSince(),
            requestConditions.getIfUnmodifiedSince(), requestConditions.getIfMatch(),
            requestConditions.getIfNoneMatch(), null, customerProvidedKey, encryptionScope,
            context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
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
     * @param requestConditions {@link BlobRequestConditions}
     * @return A response containing a {@link BlobAsyncClientBase} which is used to interact with the created snapshot,
     * use {@link #getSnapshotId()} to get the identifier for the snapshot.
     */
    public Mono<Response<BlobAsyncClientBase>> createSnapshotWithResponse(Map<String, String> metadata,
        BlobRequestConditions requestConditions) {
        try {
            return withContext(context -> createSnapshotWithResponse(metadata, requestConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<BlobAsyncClientBase>> createSnapshotWithResponse(Map<String, String> metadata,
        BlobRequestConditions requestConditions, Context context) {
        requestConditions = requestConditions == null ? new BlobRequestConditions() : requestConditions;
        context = context == null ? Context.NONE : context;

        return this.azureBlobStorage.blobs().createSnapshotWithRestResponseAsync(
            null, null, null, metadata, requestConditions.getIfModifiedSince(),
            requestConditions.getIfUnmodifiedSince(), requestConditions.getIfMatch(),
            requestConditions.getIfNoneMatch(), requestConditions.getLeaseId(), null, customerProvidedKey,
            encryptionScope, context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
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
        context = context == null ? Context.NONE : context;

        return this.azureBlobStorage.blobs().setTierWithRestResponseAsync(
            null, null, tier, null, priority, null, leaseId,
            context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
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
        context = context == null ? Context.NONE : context;

        return this.azureBlobStorage.blobs().undeleteWithRestResponseAsync(null,
            null, context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(response -> new SimpleResponse<>(response, null));
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
        context = context == null ? Context.NONE : context;
        return this.azureBlobStorage.blobs().getAccountInfoWithRestResponseAsync(null, null,
            context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(rb -> {
                BlobGetAccountInfoHeaders hd = rb.getDeserializedHeaders();
                return new SimpleResponse<>(rb, new StorageAccountInfo(hd.getSkuName(), hd.getAccountKind()));
            });
    }

    /**
     * Generates a user delegation SAS for the blob using the specified {@link BlobServiceSasSignatureValues}.
     * <p>See {@link BlobServiceSasSignatureValues} for more information on how to construct a user delegation SAS.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.generateUserDelegationSas#BlobServiceSasSignatureValues-UserDelegationKey}
     *
     * @param blobServiceSasSignatureValues {@link BlobServiceSasSignatureValues}
     * @param userDelegationKey A {@link UserDelegationKey} object used to sign the SAS values.
     * @see BlobServiceAsyncClient#getUserDelegationKey(OffsetDateTime, OffsetDateTime) for more information on how to
     * get a user delegation key.
     *
     * @return A {@code String} representing all SAS query parameters.
     */
    public String generateUserDelegationSas(BlobServiceSasSignatureValues blobServiceSasSignatureValues,
        UserDelegationKey userDelegationKey) {
        return new BlobSasImplUtil(blobServiceSasSignatureValues, getContainerName(), getBlobName(), getSnapshotId())
            .generateUserDelegationSas(userDelegationKey, getAccountName());
    }

    /**
     * Generates a service SAS for the blob using the specified {@link BlobServiceSasSignatureValues}
     * Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link BlobServiceSasSignatureValues} for more information on how to construct a service SAS.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.generateSas#BlobServiceSasSignatureValues}
     *
     * @param blobServiceSasSignatureValues {@link BlobServiceSasSignatureValues}
     *
     * @return A {@code String} representing all SAS query parameters.
     */
    public String generateSas(BlobServiceSasSignatureValues blobServiceSasSignatureValues) {
        return new BlobSasImplUtil(blobServiceSasSignatureValues, getContainerName(), getBlobName(), getSnapshotId())
            .generateSas(SasImplUtils.extractSharedKeyCredential(getHttpPipeline()));
    }
}
