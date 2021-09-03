// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.RequestConditions;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.http.rest.StreamResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.ProgressReporter;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.implementation.AzureBlobStorageImplBuilder;
import com.azure.storage.blob.implementation.models.BlobTag;
import com.azure.storage.blob.implementation.models.BlobTags;
import com.azure.storage.blob.implementation.models.BlobsDownloadHeaders;
import com.azure.storage.blob.implementation.models.BlobsGetAccountInfoHeaders;
import com.azure.storage.blob.implementation.models.BlobsGetPropertiesHeaders;
import com.azure.storage.blob.implementation.models.BlobsSetImmutabilityPolicyHeaders;
import com.azure.storage.blob.implementation.models.BlobsStartCopyFromURLHeaders;
import com.azure.storage.blob.implementation.models.EncryptionScope;
import com.azure.storage.blob.implementation.models.InternalBlobLegalHoldResult;
import com.azure.storage.blob.implementation.models.QueryRequest;
import com.azure.storage.blob.implementation.models.QuerySerialization;
import com.azure.storage.blob.implementation.util.BlobQueryReader;
import com.azure.storage.blob.implementation.util.BlobRequestConditionProperty;
import com.azure.storage.blob.implementation.util.BlobSasImplUtil;
import com.azure.storage.blob.implementation.util.ChunkedDownloadUtils;
import com.azure.storage.blob.implementation.util.ModelHelper;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.ArchiveStatus;
import com.azure.storage.blob.models.BlobBeginCopySourceRequestConditions;
import com.azure.storage.blob.models.BlobCopyInfo;
import com.azure.storage.blob.models.BlobDownloadAsyncResponse;
import com.azure.storage.blob.models.BlobDownloadContentAsyncResponse;
import com.azure.storage.blob.models.BlobDownloadHeaders;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobImmutabilityPolicy;
import com.azure.storage.blob.models.BlobImmutabilityPolicyMode;
import com.azure.storage.blob.models.BlobLegalHoldResult;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobQueryAsyncResponse;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.CopyStatusType;
import com.azure.storage.blob.models.CpkInfo;
import com.azure.storage.blob.models.CustomerProvidedKey;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.storage.blob.models.DownloadRetryOptions;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.models.RehydratePriority;
import com.azure.storage.blob.models.StorageAccountInfo;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.blob.options.BlobBeginCopyOptions;
import com.azure.storage.blob.options.BlobCopyFromUrlOptions;
import com.azure.storage.blob.options.BlobDownloadToFileOptions;
import com.azure.storage.blob.options.BlobGetTagsOptions;
import com.azure.storage.blob.options.BlobQueryOptions;
import com.azure.storage.blob.options.BlobSetAccessTierOptions;
import com.azure.storage.blob.options.BlobSetTagsOptions;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.Utility;
import com.azure.storage.common.implementation.SasImplUtils;
import com.azure.storage.common.implementation.StorageImplUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;

import static com.azure.core.util.FluxUtil.fluxError;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;
import static com.azure.storage.common.Utility.STORAGE_TRACING_NAMESPACE_VALUE;

/**
 * This class provides a client that contains all operations that apply to any blob type.
 *
 * <p>
 * This client offers the ability to download blobs. Note that uploading data is specific to each type of blob. Please
 * refer to the {@link BlockBlobClient}, {@link PageBlobClient}, or {@link AppendBlobClient} for upload options.
 */
public class BlobAsyncClientBase {

    private final ClientLogger logger = new ClientLogger(BlobAsyncClientBase.class);
    private static final Duration TIMEOUT_VALUE = Duration.ofSeconds(60);

    protected final AzureBlobStorageImpl azureBlobStorage;
    private final String snapshot;
    private final String versionId;
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
        this(pipeline, url, serviceVersion, accountName, containerName, blobName, snapshot, customerProvidedKey,
            encryptionScope, null);
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
     * @param versionId The version identifier for the blob, pass {@code null} to interact with the latest blob version.
     */
    protected BlobAsyncClientBase(HttpPipeline pipeline, String url, BlobServiceVersion serviceVersion,
        String accountName, String containerName, String blobName, String snapshot, CpkInfo customerProvidedKey,
        EncryptionScope encryptionScope, String versionId) {
        if (snapshot != null && versionId != null) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("'snapshot' and 'versionId' cannot be used at the same time."));
        }
        this.azureBlobStorage = new AzureBlobStorageImplBuilder()
            .pipeline(pipeline)
            .url(url)
            .version(serviceVersion.getVersion())
            .buildClient();
        this.serviceVersion = serviceVersion;

        this.accountName = accountName;
        this.containerName = containerName;
        this.blobName = Utility.urlDecode(blobName);
        this.snapshot = snapshot;
        this.customerProvidedKey = customerProvidedKey;
        this.encryptionScope = encryptionScope;
        this.versionId = versionId;
        /* Check to make sure the uri is valid. We don't want the error to occur later in the generated layer
           when the sas token has already been applied. */
        try {
            URI.create(getBlobUrl());
        } catch (IllegalArgumentException ex) {
            throw logger.logExceptionAsError(ex);
        }
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
        return new BlobAsyncClientBase(getHttpPipeline(), getAccountUrl(), getServiceVersion(), getAccountName(),
            getContainerName(), getBlobName(), snapshot, getCustomerProvidedKey(), encryptionScope, getVersionId());
    }

    /**
     * Creates a new {@link BlobAsyncClientBase} linked to the {@code versionId} of this blob resource.
     *
     * @param versionId the identifier for a specific version of this blob,
     * pass {@code null} to interact with the latest blob version.
     * @return a {@link BlobAsyncClientBase} used to interact with the specific version.
     */
    public BlobAsyncClientBase getVersionClient(String versionId) {
        return new BlobAsyncClientBase(getHttpPipeline(), getAccountUrl(), getServiceVersion(), getAccountName(),
            getContainerName(), getBlobName(), getSnapshotId(), getCustomerProvidedKey(), encryptionScope, versionId);
    }

    /**
     * Creates a new {@link BlobAsyncClientBase} with the specified {@code encryptionScope}.
     *
     * @param encryptionScope the encryption scope for the blob, pass {@code null} to use no encryption scope.
     * @return a {@link BlobAsyncClientBase} with the specified {@code encryptionScope}.
     */
    public BlobAsyncClientBase getEncryptionScopeAsyncClient(String encryptionScope) {
        EncryptionScope finalEncryptionScope = null;
        if (encryptionScope != null) {
            finalEncryptionScope = new EncryptionScope().setEncryptionScope(encryptionScope);
        }
        return new BlobAsyncClientBase(getHttpPipeline(), getAccountUrl(), getServiceVersion(), getAccountName(),
            getContainerName(), getBlobName(), snapshot, getCustomerProvidedKey(), finalEncryptionScope,
            getVersionId());
    }

    /**
     * Creates a new {@link BlobAsyncClientBase} with the specified {@code customerProvidedKey}.
     *
     * @param customerProvidedKey the {@link CustomerProvidedKey} for the blob,
     * pass {@code null} to use no customer provided key.
     * @return a {@link BlobAsyncClientBase} with the specified {@code customerProvidedKey}.
     */
    public BlobAsyncClientBase getCustomerProvidedKeyAsyncClient(CustomerProvidedKey customerProvidedKey) {
        CpkInfo finalCustomerProvidedKey = null;
        if (customerProvidedKey != null) {
            finalCustomerProvidedKey = new CpkInfo()
                .setEncryptionKey(customerProvidedKey.getKey())
                .setEncryptionKeySha256(customerProvidedKey.getKeySha256())
                .setEncryptionAlgorithm(customerProvidedKey.getEncryptionAlgorithm());
        }
        return new BlobAsyncClientBase(getHttpPipeline(), getAccountUrl(), getServiceVersion(), getAccountName(),
            getContainerName(), getBlobName(), snapshot, finalCustomerProvidedKey, encryptionScope,
            getVersionId());
    }

    /**
     * Get the url of the storage account.
     *
     * @return the URL of the storage account
     */
    public String getAccountUrl() {
        return azureBlobStorage.getUrl();
    }

    /**
     * Gets the URL of the blob represented by this client.
     *
     * @return the URL.
     */
    public String getBlobUrl() {
        String blobUrl = azureBlobStorage.getUrl() + "/" + containerName + "/" + Utility.urlEncode(blobName);
        if (this.isSnapshot()) {
            blobUrl = Utility.appendQueryParameter(blobUrl, "snapshot", getSnapshotId());
        }
        if (this.getVersionId() != null) {
            blobUrl = Utility.appendQueryParameter(blobUrl, "versionid", getVersionId());
        }
        return blobUrl;
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
     * Get an async client pointing to the parent container.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.getContainerAsyncClient}
     *
     * @return {@link BlobContainerAsyncClient}
     */
    public BlobContainerAsyncClient getContainerAsyncClient() {
        return getContainerClientBuilder().buildAsyncClient();
    }

    final BlobContainerClientBuilder getContainerClientBuilder() {
        CustomerProvidedKey encryptionKey = this.customerProvidedKey == null ? null
            : new CustomerProvidedKey(this.customerProvidedKey.getEncryptionKey());
        return new BlobContainerClientBuilder()
            .endpoint(this.getBlobUrl())
            .pipeline(this.getHttpPipeline())
            .serviceVersion(this.serviceVersion)
            .customerProvidedKey(encryptionKey)
            .encryptionScope(this.getEncryptionScope());
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
        return blobName; // The blob name is decoded when the client is constructor
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
     * Gets the versionId for a blob resource
     *
     * @return A string that represents the versionId of the snapshot blob
     */
    public String getVersionId() {
        return this.versionId;
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
    @ServiceMethod(returns = ReturnType.SINGLE)
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
            .onErrorResume(t -> t instanceof BlobStorageException
                    && BlobErrorCode.BLOB_USES_CUSTOMER_SPECIFIED_ENCRYPTION
                    .equals(((BlobStorageException) t).getErrorCode()),
                t -> {
                    HttpResponse response = ((BlobStorageException) t).getResponse();
                    return Mono.just(new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                        response.getHeaders(), true));
                })
            .onErrorResume(t -> t instanceof BlobStorageException && ((BlobStorageException) t).getStatusCode() == 404,
                t -> {
                    HttpResponse response = ((BlobStorageException) t).getResponse();
                    return Mono.just(new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                        response.getHeaders(), false));
                });
    }

    /**
     * Copies the data at the source URL to a blob.
     * <p>
     * This method triggers a long-running, asynchronous operations. The source may be another blob or an Azure File. If
     * the source is in another account, the source must either be public or authenticated with a SAS token. If the
     * source is in the same account, the Shared Key authorization on the destination will also be applied to the
     * source. The source URL must be URL encoded.
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
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<BlobCopyInfo, Void> beginCopy(String sourceUrl, Duration pollInterval) {
        return beginCopy(sourceUrl, null, null, null, null, null, pollInterval);
    }

    /**
     * Copies the data at the source URL to a blob.
     * <p>
     * This method triggers a long-running, asynchronous operations. The source may be another blob or an Azure File. If
     * the source is in another account, the source must either be public or authenticated with a SAS token. If the
     * source is in the same account, the Shared Key authorization on the destination will also be applied to the
     * source. The source URL must be URL encoded.
     *
     * <p><strong>Starting a copy operation</strong></p>
     * Starting a copy operation and polling on the responses.
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.beginCopy#String-Map-AccessTier-RehydratePriority-RequestConditions-BlobRequestConditions-Duration}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/copy-blob">Azure Docs</a></p>
     *
     * @param sourceUrl The source URL to copy from. URLs outside of Azure may only be copied to block blobs.
     * @param metadata Metadata to associate with the destination blob. If there is leading or trailing whitespace in
     * any metadata key or value, it must be removed or encoded.
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
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<BlobCopyInfo, Void> beginCopy(String sourceUrl, Map<String, String> metadata, AccessTier tier,
        RehydratePriority priority, RequestConditions sourceModifiedRequestConditions,
        BlobRequestConditions destRequestConditions, Duration pollInterval) {
        return this.beginCopy(new BlobBeginCopyOptions(sourceUrl).setMetadata(metadata).setTier(tier)
            .setRehydratePriority(priority).setSourceRequestConditions(
                ModelHelper.populateBlobSourceRequestConditions(sourceModifiedRequestConditions))
            .setDestinationRequestConditions(destRequestConditions).setPollInterval(pollInterval));
    }

    /**
     * Copies the data at the source URL to a blob.
     * <p>
     * This method triggers a long-running, asynchronous operations. The source may be another blob or an Azure File. If
     * the source is in another account, the source must either be public or authenticated with a SAS token. If the
     * source is in the same account, the Shared Key authorization on the destination will also be applied to the
     * source. The source URL must be URL encoded.
     *
     * <p><strong>Starting a copy operation</strong></p>
     * Starting a copy operation and polling on the responses.
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.beginCopy#BlobBeginCopyOptions}
     *
     * <p><strong>Cancelling a copy operation</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.beginCopyFromUrlCancel#BlobBeginCopyOptions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/copy-blob">Azure Docs</a></p>
     *
     * @param options {@link BlobBeginCopyOptions}
     * @return A {@link PollerFlux} that polls the blob copy operation until it has completed, has failed, or has been
     * cancelled.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<BlobCopyInfo, Void> beginCopy(BlobBeginCopyOptions options) {
        StorageImplUtils.assertNotNull("options", options);
        final Duration interval = options.getPollInterval() != null
            ? options.getPollInterval() : Duration.ofSeconds(1);
        final BlobBeginCopySourceRequestConditions sourceModifiedCondition =
            options.getSourceRequestConditions() == null
            ? new BlobBeginCopySourceRequestConditions()
            : options.getSourceRequestConditions();
        final BlobRequestConditions destinationRequestConditions =
            options.getDestinationRequestConditions() == null
            ? new BlobRequestConditions()
            : options.getDestinationRequestConditions();
        final BlobImmutabilityPolicy immutabilityPolicy = options.getImmutabilityPolicy() == null
            ? new BlobImmutabilityPolicy() : options.getImmutabilityPolicy();

        return new PollerFlux<>(interval,
            (pollingContext) -> {
                try {
                    return onStart(options.getSourceUrl(), options.getMetadata(), options.getTags(),
                        options.getTier(), options.getRehydratePriority(), options.isSealDestination(),
                        sourceModifiedCondition, destinationRequestConditions, immutabilityPolicy,
                        options.isLegalHold());
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

    private Mono<BlobCopyInfo> onStart(String sourceUrl, Map<String, String> metadata, Map<String, String> tags,
        AccessTier tier, RehydratePriority priority, Boolean sealBlob,
        BlobBeginCopySourceRequestConditions sourceModifiedRequestConditions,
        BlobRequestConditions destinationRequestConditions, BlobImmutabilityPolicy immutabilityPolicy,
        Boolean legalHold) {
        try {
            new URL(sourceUrl);
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'sourceUrl' is not a valid url.", ex));
        }

        return withContext(
            context -> azureBlobStorage.getBlobs().startCopyFromURLWithResponseAsync(containerName, blobName, sourceUrl, null, metadata,
                tier, priority, sourceModifiedRequestConditions.getIfModifiedSince(),
                sourceModifiedRequestConditions.getIfUnmodifiedSince(), sourceModifiedRequestConditions.getIfMatch(),
                sourceModifiedRequestConditions.getIfNoneMatch(), sourceModifiedRequestConditions.getTagsConditions(),
                destinationRequestConditions.getIfModifiedSince(), destinationRequestConditions.getIfUnmodifiedSince(),
                destinationRequestConditions.getIfMatch(), destinationRequestConditions.getIfNoneMatch(),
                destinationRequestConditions.getTagsConditions(), destinationRequestConditions.getLeaseId(), null,
                tagsToString(tags), sealBlob, immutabilityPolicy.getExpiryTime(), immutabilityPolicy.getPolicyMode(),
                legalHold, context))
            .map(response -> {
                final BlobsStartCopyFromURLHeaders headers = response.getDeserializedHeaders();

                return new BlobCopyInfo(sourceUrl, headers.getXMsCopyId(), headers.getXMsCopyStatus(),
                    headers.getETag(), headers.getLastModified(), ModelHelper.getErrorCode(response.getHeaders()),
                    headers.getXMsVersionId());
            });
    }

    String tagsToString(Map<String, String> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            try {
                sb.append(URLEncoder.encode(entry.getKey(), Charset.defaultCharset().toString()));
                sb.append("=");
                sb.append(URLEncoder.encode(entry.getValue(), Charset.defaultCharset().toString()));
                sb.append("&");
            } catch (UnsupportedEncodingException e) {
                throw logger.logExceptionAsError(new IllegalStateException(e));
            }
        }

        sb.deleteCharAt(sb.length() - 1); // Remove the last '&'
        return sb.toString();
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
                response.getETag(), response.getCopyCompletionTime(), response.getCopyStatusDescription(),
                response.getVersionId());

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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/abort-copy-blob">Azure Docs</a></p>
     *
     * @param copyId The id of the copy operation to abort.
     * @return A reactive response signalling completion.
     * @see #copyFromUrl(String)
     * @see #beginCopy(String, Duration)
     * @see #beginCopy(String, Map, AccessTier, RehydratePriority, RequestConditions, BlobRequestConditions, Duration)
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/abort-copy-blob">Azure Docs</a></p>
     *
     * @param copyId The id of the copy operation to abort.
     * @param leaseId The lease ID the active lease on the blob must match.
     * @return A reactive response signalling completion.
     * @see #copyFromUrl(String)
     * @see #beginCopy(String, Duration)
     * @see #beginCopy(String, Map, AccessTier, RehydratePriority, RequestConditions, BlobRequestConditions, Duration)
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> abortCopyFromUrlWithResponse(String copyId, String leaseId) {
        try {
            return withContext(context -> abortCopyFromUrlWithResponse(copyId, leaseId, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> abortCopyFromUrlWithResponse(String copyId, String leaseId, Context context) {
        return this.azureBlobStorage.getBlobs().abortCopyFromURLWithResponseAsync(
            containerName, blobName, copyId, null, leaseId, null, context)
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Copies the data at the source URL to a blob and waits for the copy to complete before returning a response.
     * <p>
     * The source must be a block blob no larger than 256MB. The source must also be either public or have a sas token
     * attached. The URL must be URL encoded.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.copyFromUrl#String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/copy-blob-from-url">Azure Docs</a></p>
     *
     * @param copySource The source URL to copy from.
     * @return A reactive response containing the copy ID for the long running operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<String> copyFromUrl(String copySource) {
        try {
            return copyFromUrlWithResponse(copySource, null, null, null, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Copies the data at the source URL to a blob and waits for the copy to complete before returning a response.
     * <p>
     * The source must be a block blob no larger than 256MB. The source must also be either public or have a sas token
     * attached. The URL must be URL encoded.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.copyFromUrlWithResponse#String-Map-AccessTier-RequestConditions-BlobRequestConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/copy-blob-from-url">Azure Docs</a></p>
     *
     * @param copySource The source URL to copy from. URLs outside of Azure may only be copied to block blobs.
     * @param metadata Metadata to associate with the destination blob. If there is leading or trailing whitespace in
     * any metadata key or value, it must be removed or encoded.
     * @param tier {@link AccessTier} for the destination blob.
     * @param sourceModifiedRequestConditions {@link RequestConditions} against the source. Standard HTTP Access
     * conditions related to the modification of data. ETag and LastModifiedTime are used to construct conditions
     * related to when the blob was changed relative to the given request. The request will fail if the specified
     * condition is not satisfied.
     * @param destRequestConditions {@link BlobRequestConditions} against the destination.
     * @return A reactive response containing the copy ID for the long running operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<String>> copyFromUrlWithResponse(String copySource, Map<String, String> metadata,
        AccessTier tier, RequestConditions sourceModifiedRequestConditions,
        BlobRequestConditions destRequestConditions) {
        return this.copyFromUrlWithResponse(new BlobCopyFromUrlOptions(copySource).setMetadata(metadata)
            .setTier(tier).setSourceRequestConditions(sourceModifiedRequestConditions)
            .setDestinationRequestConditions(destRequestConditions));
    }

    /**
     * Copies the data at the source URL to a blob and waits for the copy to complete before returning a response.
     * <p>
     * The source must be a block blob no larger than 256MB. The source must also be either public or have a sas token
     * attached. The URL must be URL encoded.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.copyFromUrlWithResponse#BlobCopyFromUrlOptions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/copy-blob-from-url">Azure Docs</a></p>
     *
     * @param options {@link BlobCopyFromUrlOptions}
     * @return A reactive response containing the copy ID for the long running operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<String>> copyFromUrlWithResponse(BlobCopyFromUrlOptions options) {
        try {
            return withContext(context -> copyFromUrlWithResponse(options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<String>> copyFromUrlWithResponse(BlobCopyFromUrlOptions options, Context context) {
        StorageImplUtils.assertNotNull("options", options);
        RequestConditions sourceModifiedRequestConditions = options.getSourceRequestConditions() == null
            ? new RequestConditions() : options.getSourceRequestConditions();
        BlobRequestConditions destRequestConditions = options.getDestinationRequestConditions() == null
            ? new BlobRequestConditions() : options.getDestinationRequestConditions();
        BlobImmutabilityPolicy immutabilityPolicy = options.getImmutabilityPolicy() == null
            ? new BlobImmutabilityPolicy() : options.getImmutabilityPolicy();

        try {
            new URL(options.getCopySource());
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'copySource' is not a valid url."));
        }
        String sourceAuth = options.getSourceAuthorization() == null
            ? null : options.getSourceAuthorization().toString();

        return this.azureBlobStorage.getBlobs().copyFromURLWithResponseAsync(
            containerName, blobName, options.getCopySource(), null, options.getMetadata(), options.getTier(),
            sourceModifiedRequestConditions.getIfModifiedSince(),
            sourceModifiedRequestConditions.getIfUnmodifiedSince(), sourceModifiedRequestConditions.getIfMatch(),
            sourceModifiedRequestConditions.getIfNoneMatch(), destRequestConditions.getIfModifiedSince(),
            destRequestConditions.getIfUnmodifiedSince(), destRequestConditions.getIfMatch(),
            destRequestConditions.getIfNoneMatch(), destRequestConditions.getTagsConditions(),
            destRequestConditions.getLeaseId(), null, null,
            tagsToString(options.getTags()), immutabilityPolicy.getExpiryTime(), immutabilityPolicy.getPolicyMode(),
            options.isLegalHold(), sourceAuth, context)
            .map(rb -> new SimpleResponse<>(rb, rb.getDeserializedHeaders().getXMsCopyId()));
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * <p>This method will be deprecated in the future. Use {@link #downloadStream()} instead.
     *
     * @return A reactive response containing the blob data.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Flux<ByteBuffer> download() {
        return downloadStream();
    }

    /**
     * Reads the entire blob. Uploading data must be done from the {@link BlockBlobClient}, {@link PageBlobClient}, or
     * {@link AppendBlobClient}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.downloadStream}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @return A reactive response containing the blob data.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Flux<ByteBuffer> downloadStream() {
        try {
            return downloadWithResponse(null, null, null, false)
                .flatMapMany(BlobDownloadAsyncResponse::getValue);
        } catch (RuntimeException ex) {
            return fluxError(logger, ex);
        }
    }

    /**
     * Reads the entire blob. Uploading data must be done from the {@link BlockBlobClient}, {@link PageBlobClient}, or
     * {@link AppendBlobClient}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobAsyncClient.downloadContent}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * <p>This method supports downloads up to 2GB of data.
     * Use {@link #downloadStream()} to download larger blobs.</p>
     *
     * @return A reactive response containing the blob data.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> downloadContent() {
        try {
            return downloadWithResponse(null, null, null, false).flatMap(response ->
                FluxUtil.collectBytesFromNetworkResponse(response.getValue(), response.getHeaders()))
                .map(BinaryData::fromBytes);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * <p>This method will be deprecated in the future.
     * Use {@link #downloadStreamWithResponse(BlobRange, DownloadRetryOptions, BlobRequestConditions, boolean)}  instead.
     *
     * @param range {@link BlobRange}
     * @param options {@link DownloadRetryOptions}
     * @param requestConditions {@link BlobRequestConditions}
     * @param getRangeContentMd5 Whether the contentMD5 for the specified blob range should be returned.
     * @return A reactive response containing the blob data.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BlobDownloadAsyncResponse> downloadWithResponse(BlobRange range, DownloadRetryOptions options,
        BlobRequestConditions requestConditions, boolean getRangeContentMd5) {
        return downloadStreamWithResponse(range, options, requestConditions, getRangeContentMd5);
    }

    /**
     * Reads a range of bytes from a blob. Uploading data must be done from the {@link BlockBlobClient}, {@link
     * PageBlobClient}, or {@link AppendBlobClient}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.downloadStreamWithResponse#BlobRange-DownloadRetryOptions-BlobRequestConditions-boolean}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param range {@link BlobRange}
     * @param options {@link DownloadRetryOptions}
     * @param requestConditions {@link BlobRequestConditions}
     * @param getRangeContentMd5 Whether the contentMD5 for the specified blob range should be returned.
     * @return A reactive response containing the blob data.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BlobDownloadAsyncResponse> downloadStreamWithResponse(BlobRange range, DownloadRetryOptions options,
        BlobRequestConditions requestConditions, boolean getRangeContentMd5) {
        try {
            return withContext(context ->
                downloadStreamWithResponse(range, options, requestConditions, getRangeContentMd5,
                    context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Reads a range of bytes from a blob. Uploading data must be done from the {@link BlockBlobClient}, {@link
     * PageBlobClient}, or {@link AppendBlobClient}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.downloadContentWithResponse#DownloadRetryOptions-BlobRequestConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * <p>This method supports downloads up to 2GB of data.
     * Use {@link #downloadStreamWithResponse(BlobRange, DownloadRetryOptions, BlobRequestConditions, boolean)}
     * to download larger blobs.</p>
     *
     * @param options {@link DownloadRetryOptions}
     * @param requestConditions {@link BlobRequestConditions}
     * @return A reactive response containing the blob data.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BlobDownloadContentAsyncResponse> downloadContentWithResponse(
        DownloadRetryOptions options,
        BlobRequestConditions requestConditions) {
        try {
            return withContext(context -> downloadStreamWithResponse(null, options, requestConditions, false, context)
                .flatMap(r -> FluxUtil.collectBytesFromNetworkResponse(r.getValue(), r.getHeaders())
                    .map(bytes -> new BlobDownloadContentAsyncResponse(r.getRequest(), r.getStatusCode(),
                        r.getHeaders(), BinaryData.fromBytes(bytes), r.getDeserializedHeaders()))));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<BlobDownloadAsyncResponse> downloadStreamWithResponse(BlobRange range, DownloadRetryOptions options,
        BlobRequestConditions requestConditions, boolean getRangeContentMd5, Context context) {
        BlobRange finalRange = range == null ? new BlobRange(0) : range;
        Boolean getMD5 = getRangeContentMd5 ? getRangeContentMd5 : null;
        BlobRequestConditions finalRequestConditions =
            requestConditions == null ? new BlobRequestConditions() : requestConditions;
        DownloadRetryOptions finalOptions = (options == null) ? new DownloadRetryOptions() : options;

        return downloadRange(finalRange, finalRequestConditions, finalRequestConditions.getIfMatch(), getMD5, context)
            .map(response -> {
                String eTag = ModelHelper.getETag(response.getHeaders());
                BlobsDownloadHeaders blobsDownloadHeaders =
                    ModelHelper.transformBlobDownloadHeaders(response.getHeaders());
                BlobDownloadHeaders blobDownloadHeaders = ModelHelper.populateBlobDownloadHeaders(
                    blobsDownloadHeaders, ModelHelper.getErrorCode(response.getHeaders()));

                /*
                    If the customer did not specify a count, they are reading to the end of the blob. Extract this value
                    from the response for better book keeping towards the end.
                */
                long finalCount;
                if (finalRange.getCount() == null) {
                    long blobLength = BlobAsyncClientBase.getBlobLength(blobDownloadHeaders);
                    finalCount = blobLength - finalRange.getOffset();
                } else {
                    finalCount = finalRange.getCount();
                }

                Flux<ByteBuffer> bufferFlux  = FluxUtil.createRetriableDownloadFlux(
                    () -> response.getValue().timeout(TIMEOUT_VALUE),
                    (throwable, offset) -> {
                        if (!(throwable instanceof IOException || throwable instanceof TimeoutException)) {
                            return Flux.error(throwable);
                        }

                        long newCount = finalCount - (offset - finalRange.getOffset());

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
                                new BlobRange(offset, newCount), finalRequestConditions, eTag, getMD5, context)
                                .flatMapMany(r -> r.getValue().timeout(TIMEOUT_VALUE));
                        } catch (Exception e) {
                            return Flux.error(e);
                        }
                    },
                    finalOptions.getMaxRetryRequests(),
                    finalRange.getOffset()
                ).switchIfEmpty(Flux.just(ByteBuffer.wrap(new byte[0])));

                return new BlobDownloadAsyncResponse(response.getRequest(), response.getStatusCode(),
                    response.getHeaders(), bufferFlux, blobDownloadHeaders);
            });
    }

    private Mono<StreamResponse> downloadRange(BlobRange range, BlobRequestConditions requestConditions,
        String eTag, Boolean getMD5, Context context) {
        return azureBlobStorage.getBlobs().downloadWithResponseAsync(containerName, blobName, snapshot, versionId, null,
            range.toHeaderValue(), requestConditions.getLeaseId(), getMD5, null, requestConditions.getIfModifiedSince(),
            requestConditions.getIfUnmodifiedSince(), eTag,
            requestConditions.getIfNoneMatch(), requestConditions.getTagsConditions(), null,
            customerProvidedKey, context);
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param filePath A {@link String} representing the filePath where the downloaded data will be written.
     * @return A reactive response containing the blob properties and metadata.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param filePath A {@link String} representing the filePath where the downloaded data will be written.
     * @param overwrite Whether or not to overwrite the file, should the file exist.
     * @return A reactive response containing the blob properties and metadata.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param filePath A {@link String} representing the filePath where the downloaded data will be written.
     * @param range {@link BlobRange}
     * @param parallelTransferOptions {@link ParallelTransferOptions} to use to download to file. Number of parallel
     * transfers parameter is ignored.
     * @param options {@link DownloadRetryOptions}
     * @param requestConditions {@link BlobRequestConditions}
     * @param rangeGetContentMd5 Whether the contentMD5 for the specified blob range should be returned.
     * @return A reactive response containing the blob properties and metadata.
     * @throws IllegalArgumentException If {@code blockSize} is less than 0 or greater than 4000MB.
     * @throws UncheckedIOException If an I/O error occurs.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a></p>
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
     * @throws IllegalArgumentException If {@code blockSize} is less than 0 or greater than 4000MB.
     * @throws UncheckedIOException If an I/O error occurs.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BlobProperties>> downloadToFileWithResponse(String filePath, BlobRange range,
        ParallelTransferOptions parallelTransferOptions, DownloadRetryOptions options,
        BlobRequestConditions requestConditions, boolean rangeGetContentMd5, Set<OpenOption> openOptions) {
        try {
            final com.azure.storage.common.ParallelTransferOptions finalParallelTransferOptions =
                ModelHelper.wrapBlobOptions(ModelHelper.populateAndApplyDefaults(parallelTransferOptions));
            return withContext(context ->
                downloadToFileWithResponse(new BlobDownloadToFileOptions(filePath).setRange(range)
                        .setParallelTransferOptions(finalParallelTransferOptions)
                        .setDownloadRetryOptions(options).setRequestConditions(requestConditions)
                        .setRetrieveContentRangeMd5(rangeGetContentMd5).setOpenOptions(openOptions), context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.downloadToFileWithResponse#BlobDownloadToFileOptions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param options {@link BlobDownloadToFileOptions}
     * @return A reactive response containing the blob properties and metadata.
     * @throws IllegalArgumentException If {@code blockSize} is less than 0 or greater than 4000MB.
     * @throws UncheckedIOException If an I/O error occurs.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BlobProperties>> downloadToFileWithResponse(BlobDownloadToFileOptions options) {
        try {
            return withContext(context -> downloadToFileWithResponse(options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<BlobProperties>> downloadToFileWithResponse(BlobDownloadToFileOptions options, Context context) {
        StorageImplUtils.assertNotNull("options", options);

        BlobRange finalRange = options.getRange() == null ? new BlobRange(0) : options.getRange();
        final com.azure.storage.common.ParallelTransferOptions finalParallelTransferOptions =
            ModelHelper.populateAndApplyDefaults(options.getParallelTransferOptions());
        BlobRequestConditions finalConditions = options.getRequestConditions() == null
            ? new BlobRequestConditions() : options.getRequestConditions();

        // Default behavior is not to overwrite
        Set<OpenOption> openOptions = options.getOpenOptions();
        if (openOptions == null) {
            openOptions = new HashSet<>();
            openOptions.add(StandardOpenOption.CREATE_NEW);
            openOptions.add(StandardOpenOption.WRITE);
            openOptions.add(StandardOpenOption.READ);
        }

        AsynchronousFileChannel channel = downloadToFileResourceSupplier(options.getFilePath(), openOptions);
        return Mono.just(channel)
            .flatMap(c -> this.downloadToFileImpl(c, finalRange, finalParallelTransferOptions,
                options.getDownloadRetryOptions(), finalConditions, options.isRetrieveContentRangeMd5(), context))
            .doFinally(signalType -> this.downloadToFileCleanup(channel, options.getFilePath(), signalType));
    }

    private AsynchronousFileChannel downloadToFileResourceSupplier(String filePath, Set<OpenOption> openOptions) {
        try {
            return AsynchronousFileChannel.open(Paths.get(filePath), openOptions, null);
        } catch (IOException e) {
            throw logger.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    private Mono<Response<BlobProperties>> downloadToFileImpl(AsynchronousFileChannel file, BlobRange finalRange,
        com.azure.storage.common.ParallelTransferOptions finalParallelTransferOptions,
        DownloadRetryOptions downloadRetryOptions, BlobRequestConditions requestConditions, boolean rangeGetContentMd5,
        Context context) {
        // See ProgressReporter for an explanation on why this lock is necessary and why we use AtomicLong.
        Lock progressLock = new ReentrantLock();
        AtomicLong totalProgress = new AtomicLong(0);

        /*
         * Downloads the first chunk and gets the size of the data and etag if not specified by the user.
         */
        BiFunction<BlobRange, BlobRequestConditions, Mono<BlobDownloadAsyncResponse>> downloadFunc =
            (range, conditions) -> this.downloadStreamWithResponse(range, downloadRetryOptions, conditions,
                rangeGetContentMd5, context);

        return ChunkedDownloadUtils.downloadFirstChunk(finalRange, finalParallelTransferOptions, requestConditions,
            downloadFunc, true)
            .flatMap(setupTuple3 -> {
                long newCount = setupTuple3.getT1();
                BlobRequestConditions finalConditions = setupTuple3.getT2();

                int numChunks = ChunkedDownloadUtils.calculateNumBlocks(newCount,
                    finalParallelTransferOptions.getBlockSizeLong());

                // In case it is an empty blob, this ensures we still actually perform a download operation.
                numChunks = numChunks == 0 ? 1 : numChunks;

                BlobDownloadAsyncResponse initialResponse = setupTuple3.getT3();
                return Flux.range(0, numChunks)
                    .flatMap(chunkNum -> ChunkedDownloadUtils.downloadChunk(chunkNum, initialResponse,
                        finalRange, finalParallelTransferOptions, finalConditions, newCount, downloadFunc,
                        response -> writeBodyToFile(response, file, chunkNum, finalParallelTransferOptions,
                            progressLock, totalProgress).flux()), finalParallelTransferOptions.getMaxConcurrency())

                    // Only the first download call returns a value.
                    .then(Mono.just(buildBlobPropertiesResponse(initialResponse)));
            });
    }

    private static Mono<Void> writeBodyToFile(BlobDownloadAsyncResponse response, AsynchronousFileChannel file,
        long chunkNum, com.azure.storage.common.ParallelTransferOptions finalParallelTransferOptions, Lock progressLock,
        AtomicLong totalProgress) {

        // Extract the body.
        Flux<ByteBuffer> data = response.getValue();

        // Report progress as necessary.
        data = ProgressReporter.addParallelProgressReporting(data,
            ModelHelper.wrapCommonReceiver(finalParallelTransferOptions.getProgressReceiver()), progressLock,
            totalProgress);

        // Write to the file.
        return FluxUtil.writeFile(data, file, chunkNum * finalParallelTransferOptions.getBlockSizeLong());
    }

    private static Response<BlobProperties> buildBlobPropertiesResponse(BlobDownloadAsyncResponse response) {
        // blobSize determination - contentLength only returns blobSize if the download is not chunked.
        BlobDownloadHeaders hd = response.getDeserializedHeaders();
        long blobSize = getBlobLength(hd);
        BlobProperties properties = new BlobProperties(null, hd.getLastModified(), hd.getETag(), blobSize,
            hd.getContentType(), hd.getContentMd5(), hd.getContentEncoding(), hd.getContentDisposition(),
            hd.getContentLanguage(), hd.getCacheControl(), hd.getBlobSequenceNumber(), hd.getBlobType(),
            hd.getLeaseStatus(), hd.getLeaseState(), hd.getLeaseDuration(), hd.getCopyId(), hd.getCopyStatus(),
            hd.getCopySource(), hd.getCopyProgress(), hd.getCopyCompletionTime(), hd.getCopyStatusDescription(),
            hd.isServerEncrypted(), null, null, null, null, null,
            hd.getEncryptionKeySha256(), hd.getEncryptionScope(), null, hd.getMetadata(),
            hd.getBlobCommittedBlockCount(), hd.getTagCount(), hd.getVersionId(), null,
            hd.getObjectReplicationSourcePolicies(), hd.getObjectReplicationDestinationPolicyId(), null,
            hd.isSealed(), hd.getLastAccessedTime(), null, hd.getImmutabilityPolicy(), hd.hasLegalHold());
        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), properties);
    }

    static long getBlobLength(BlobDownloadHeaders headers) {
        return headers.getContentRange() == null ? headers.getContentLength()
            : ChunkedDownloadUtils.extractTotalBlobLength(headers.getContentRange());
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
     * Deletes the specified blob or snapshot. To delete a blob with its snapshots use
     * {@link #deleteWithResponse(DeleteSnapshotsOptionType, BlobRequestConditions)} and set
     * {@code DeleteSnapshotsOptionType} to INCLUDE.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.delete}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-blob">Azure Docs</a></p>
     *
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> delete() {
        try {
            return deleteWithResponse(null, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes the specified blob or snapshot. To delete a blob with its snapshots use
     * {@link #deleteWithResponse(DeleteSnapshotsOptionType, BlobRequestConditions)} and set
     * {@code DeleteSnapshotsOptionType} to INCLUDE.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.deleteWithResponse#DeleteSnapshotsOptionType-BlobRequestConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-blob">Azure Docs</a></p>
     *
     * @param deleteBlobSnapshotOptions Specifies the behavior for deleting the snapshots on this blob. {@code Include}
     * will delete the base blob and all snapshots. {@code Only} will delete only the snapshots. If a snapshot is being
     * deleted, you must pass null.
     * @param requestConditions {@link BlobRequestConditions}
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
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

        return this.azureBlobStorage.getBlobs().deleteWithResponseAsync(containerName, blobName, snapshot, versionId,
            null, requestConditions.getLeaseId(), deleteBlobSnapshotOptions, requestConditions.getIfModifiedSince(),
            requestConditions.getIfUnmodifiedSince(), requestConditions.getIfMatch(),
            requestConditions.getIfNoneMatch(), requestConditions.getTagsConditions(), null, null, context)
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob-properties">Azure Docs</a></p>
     *
     * @return A reactive response containing the blob properties and metadata.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob-properties">Azure Docs</a></p>
     *
     * @param requestConditions {@link BlobRequestConditions}
     * @return A reactive response containing the blob properties and metadata.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
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

        return this.azureBlobStorage.getBlobs().getPropertiesWithResponseAsync(
            containerName, blobName, snapshot, versionId, null, requestConditions.getLeaseId(),
            requestConditions.getIfModifiedSince(),
            requestConditions.getIfUnmodifiedSince(), requestConditions.getIfMatch(),
            requestConditions.getIfNoneMatch(), requestConditions.getTagsConditions(), null, customerProvidedKey,
            context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(rb -> {
                BlobsGetPropertiesHeaders hd = rb.getDeserializedHeaders();
                BlobProperties properties = new BlobProperties(hd.getXMsCreationTime(), hd.getLastModified(),
                    hd.getETag(), hd.getContentLength() == null ? 0 : hd.getContentLength(), hd.getContentType(),
                    hd.getContentMD5(), hd.getContentEncoding(), hd.getContentDisposition(), hd.getContentLanguage(),
                    hd.getCacheControl(), hd.getXMsBlobSequenceNumber(), hd.getXMsBlobType(), hd.getXMsLeaseStatus(),
                    hd.getXMsLeaseState(), hd.getXMsLeaseDuration(), hd.getXMsCopyId(), hd.getXMsCopyStatus(),
                    hd.getXMsCopySource(), hd.getXMsCopyProgress(), hd.getXMsCopyCompletionTime(),
                    hd.getXMsCopyStatusDescription(), hd.isXMsServerEncrypted(), hd.isXMsIncrementalCopy(),
                    hd.getXMsCopyDestinationSnapshot(), AccessTier.fromString(hd.getXMsAccessTier()),
                    hd.isXMsAccessTierInferred(), ArchiveStatus.fromString(hd.getXMsArchiveStatus()),
                    hd.getXMsEncryptionKeySha256(), hd.getXMsEncryptionScope(), hd.getXMsAccessTierChangeTime(),
                    hd.getXMsMeta(), hd.getXMsBlobCommittedBlockCount(), hd.getXMsTagCount(), hd.getXMsVersionId(),
                    hd.isXMsIsCurrentVersion(),
                    ModelHelper.getObjectReplicationSourcePolicies(hd.getXMsOr()),
                    ModelHelper.getObjectReplicationDestinationPolicyId(hd.getXMsOr()),
                    RehydratePriority.fromString(hd.getXMsRehydratePriority()), hd.isXMsBlobSealed(),
                    hd.getXMsLastAccessTime(), hd.getXMsExpiryTime(), new BlobImmutabilityPolicy()
                    .setExpiryTime(hd.getXMsImmutabilityPolicyUntilDate())
                    .setPolicyMode(hd.getXMsImmutabilityPolicyMode()), hd.isXMsLegalHold());
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-properties">Azure Docs</a></p>
     *
     * @param headers {@link BlobHttpHeaders}
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-properties">Azure Docs</a></p>
     *
     * @param headers {@link BlobHttpHeaders}
     * @param requestConditions {@link BlobRequestConditions}
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> setHttpHeadersWithResponse(BlobHttpHeaders headers,
        BlobRequestConditions requestConditions) {
        try {
            return withContext(context -> setHttpHeadersWithResponse(headers, requestConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> setHttpHeadersWithResponse(BlobHttpHeaders headers, BlobRequestConditions requestConditions,
        Context context) {
        requestConditions = requestConditions == null ? new BlobRequestConditions() : requestConditions;

        return this.azureBlobStorage.getBlobs().setHttpHeadersWithResponseAsync(
            containerName, blobName, null, requestConditions.getLeaseId(), requestConditions.getIfModifiedSince(),
            requestConditions.getIfUnmodifiedSince(), requestConditions.getIfMatch(),
            requestConditions.getIfNoneMatch(), requestConditions.getTagsConditions(), null, headers, context)
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-metadata">Azure Docs</a></p>
     *
     * @param metadata Metadata to associate with the blob. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-metadata">Azure Docs</a></p>
     *
     * @param metadata Metadata to associate with the blob. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param requestConditions {@link BlobRequestConditions}
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
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

        return this.azureBlobStorage.getBlobs().setMetadataWithResponseAsync(
            containerName, blobName, null, metadata, requestConditions.getLeaseId(), requestConditions.getIfModifiedSince(),
            requestConditions.getIfUnmodifiedSince(), requestConditions.getIfMatch(),
            requestConditions.getIfNoneMatch(), requestConditions.getTagsConditions(), null, customerProvidedKey,
            encryptionScope, context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Returns the blob's tags.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.getTags}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob-tags">Azure Docs</a></p>
     *
     * @return A reactive response containing the blob's tags.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Map<String, String>> getTags() {
        return this.getTagsWithResponse(new BlobGetTagsOptions()).map(Response::getValue);
    }

    /**
     * Returns the blob's tags.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.getTagsWithResponse#BlobGetTagsOptions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob-tags">Azure Docs</a></p>
     *
     * @param options {@link BlobGetTagsOptions}
     * @return A reactive response containing the blob's tags.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Map<String, String>>> getTagsWithResponse(BlobGetTagsOptions options) {
        try {
            return withContext(context -> getTagsWithResponse(options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Map<String, String>>> getTagsWithResponse(BlobGetTagsOptions options, Context context) {
        options = (options == null) ? new BlobGetTagsOptions() : options;
        BlobRequestConditions requestConditions = (options.getRequestConditions() == null)
            ? new BlobRequestConditions() : options.getRequestConditions();
        return this.azureBlobStorage.getBlobs().getTagsWithResponseAsync(containerName, blobName, null, null, snapshot,
            versionId, requestConditions.getTagsConditions(), requestConditions.getLeaseId(), context)
            .map(response -> {
                Map<String, String> tags = new HashMap<>();
                for (BlobTag tag : response.getValue().getBlobTagSet()) {
                    tags.put(tag.getKey(), tag.getValue());
                }
                return new SimpleResponse<>(response, tags);
            });
    }

    /**
     * Sets user defined tags. The specified tags in this method will replace existing tags. If old values must be
     * preserved, they must be downloaded and included in the call to this method.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.setTags#Map}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-tags">Azure Docs</a></p>
     *
     * @param tags Tags to associate with the blob.
     * @return A reactive response signaling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> setTags(Map<String, String> tags) {
        return this.setTagsWithResponse(new BlobSetTagsOptions(tags)).flatMap(FluxUtil::toMono);
    }

    /**
     * Sets user defined tags. The specified tags in this method will replace existing tags. If old values must be
     * preserved, they must be downloaded and included in the call to this method.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.setTagsWithResponse#BlobSetTagsOptions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-tags">Azure Docs</a></p>
     *
     * @param options {@link BlobSetTagsOptions}
     * @return A reactive response signaling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> setTagsWithResponse(BlobSetTagsOptions options) {
        try {
            return withContext(context -> setTagsWithResponse(options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> setTagsWithResponse(BlobSetTagsOptions options, Context context) {
        StorageImplUtils.assertNotNull("options", options);
        BlobRequestConditions requestConditions = (options.getRequestConditions() == null)
            ? new BlobRequestConditions() : options.getRequestConditions();
        List<BlobTag> tagList = null;
        if (options.getTags() != null) {
            tagList = new ArrayList<>();
            for (Map.Entry<String, String> entry : options.getTags().entrySet()) {
                tagList.add(new BlobTag().setKey(entry.getKey()).setValue(entry.getValue()));
            }
        }
        BlobTags t = new BlobTags().setBlobTagSet(tagList);
        return this.azureBlobStorage.getBlobs().setTagsWithResponseAsync(containerName, blobName, null, versionId, null, null, null,
            requestConditions.getTagsConditions(), requestConditions.getLeaseId(), t, context)
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/snapshot-blob">Azure Docs</a></p>
     *
     * @return A response containing a {@link BlobAsyncClientBase} which is used to interact with the created snapshot,
     * use {@link #getSnapshotId()} to get the identifier for the snapshot.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/snapshot-blob">Azure Docs</a></p>
     *
     * @param metadata Metadata to associate with the resource. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param requestConditions {@link BlobRequestConditions}
     * @return A response containing a {@link BlobAsyncClientBase} which is used to interact with the created snapshot,
     * use {@link #getSnapshotId()} to get the identifier for the snapshot.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
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

        return this.azureBlobStorage.getBlobs().createSnapshotWithResponseAsync(
            containerName, blobName, null, metadata, requestConditions.getIfModifiedSince(),
            requestConditions.getIfUnmodifiedSince(), requestConditions.getIfMatch(),
            requestConditions.getIfNoneMatch(), requestConditions.getTagsConditions(), requestConditions.getLeaseId(),
            null, customerProvidedKey, encryptionScope, context)
            .map(rb -> new SimpleResponse<>(rb, this.getSnapshotClient(rb.getDeserializedHeaders().getXMsSnapshot())));
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-tier">Azure Docs</a></p>
     *
     * @param tier The new tier for the blob.
     * @return A reactive response signalling completion.
     * @throws NullPointerException if {@code tier} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-tier">Azure Docs</a></p>
     *
     * @param tier The new tier for the blob.
     * @param priority Optional priority to set for re-hydrating blobs.
     * @param leaseId The lease ID the active lease on the blob must match.
     * @return A reactive response signalling completion.
     * @throws NullPointerException if {@code tier} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> setAccessTierWithResponse(AccessTier tier, RehydratePriority priority, String leaseId) {
        try {
            return withContext(context -> setTierWithResponse(new BlobSetAccessTierOptions(tier).setPriority(priority)
                .setLeaseId(leaseId), context));
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
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.setAccessTierWithResponse#BlobSetAccessTierOptions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-tier">Azure Docs</a></p>
     *
     * @param options {@link BlobSetAccessTierOptions}
     * @return A reactive response signalling completion.
     * @throws NullPointerException if {@code tier} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> setAccessTierWithResponse(BlobSetAccessTierOptions options) {
        try {
            return withContext(context -> setTierWithResponse(options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> setTierWithResponse(BlobSetAccessTierOptions options, Context context) {
        StorageImplUtils.assertNotNull("options", options);

        return this.azureBlobStorage.getBlobs().setTierWithResponseAsync(
            containerName, blobName, options.getTier(), snapshot, versionId, null,
            options.getPriority(), null, options.getLeaseId(), options.getTagsConditions(), context)
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/undelete-blob">Azure Docs</a></p>
     *
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/undelete-blob">Azure Docs</a></p>
     *
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> undeleteWithResponse() {
        try {
            return withContext(this::undeleteWithResponse);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> undeleteWithResponse(Context context) {
        return this.azureBlobStorage.getBlobs().undeleteWithResponseAsync(containerName, blobName, null,
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-account-information">Azure Docs</a></p>
     *
     * @return a reactor response containing the sku name and account kind.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-account-information">Azure Docs</a></p>
     *
     * @return a reactor response containing the sku name and account kind.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<StorageAccountInfo>> getAccountInfoWithResponse() {
        try {
            return withContext(this::getAccountInfoWithResponse);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<StorageAccountInfo>> getAccountInfoWithResponse(Context context) {
        return this.azureBlobStorage.getBlobs().getAccountInfoWithResponseAsync(containerName, blobName, context)
            .map(rb -> {
                BlobsGetAccountInfoHeaders hd = rb.getDeserializedHeaders();
                return new SimpleResponse<>(rb, new StorageAccountInfo(hd.getXMsSkuName(), hd.getXMsAccountKind()));
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
     * See {@link BlobServiceAsyncClient#getUserDelegationKey(OffsetDateTime, OffsetDateTime)} for more information on
     * how to get a user delegation key.
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateUserDelegationSas(BlobServiceSasSignatureValues blobServiceSasSignatureValues,
        UserDelegationKey userDelegationKey) {
        return generateUserDelegationSas(blobServiceSasSignatureValues, userDelegationKey, getAccountName(),
            Context.NONE);
    }

    /**
     * Generates a user delegation SAS for the blob using the specified {@link BlobServiceSasSignatureValues}.
     * <p>See {@link BlobServiceSasSignatureValues} for more information on how to construct a user delegation SAS.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.generateUserDelegationSas#BlobServiceSasSignatureValues-UserDelegationKey-String-Context}
     *
     * @param blobServiceSasSignatureValues {@link BlobServiceSasSignatureValues}
     * @param userDelegationKey A {@link UserDelegationKey} object used to sign the SAS values.
     * See {@link BlobServiceAsyncClient#getUserDelegationKey(OffsetDateTime, OffsetDateTime)} for more information on
     * how to get a user delegation key.
     * @param accountName The account name.
     * @param context Additional context that is passed through the code when generating a SAS.
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateUserDelegationSas(BlobServiceSasSignatureValues blobServiceSasSignatureValues,
        UserDelegationKey userDelegationKey, String accountName, Context context) {
        return new BlobSasImplUtil(blobServiceSasSignatureValues, getContainerName(), getBlobName(),
            getSnapshotId(), getVersionId())
            .generateUserDelegationSas(userDelegationKey, accountName, context);
    }

    /**
     * Generates a service SAS for the blob using the specified {@link BlobServiceSasSignatureValues}
     * <p>Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link BlobServiceSasSignatureValues} for more information on how to construct a service SAS.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.generateSas#BlobServiceSasSignatureValues}
     *
     * @param blobServiceSasSignatureValues {@link BlobServiceSasSignatureValues}
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateSas(BlobServiceSasSignatureValues blobServiceSasSignatureValues) {
        return generateSas(blobServiceSasSignatureValues, Context.NONE);
    }

    /**
     * Generates a service SAS for the blob using the specified {@link BlobServiceSasSignatureValues}
     * <p>Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link BlobServiceSasSignatureValues} for more information on how to construct a service SAS.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.generateSas#BlobServiceSasSignatureValues-Context}
     *
     * @param blobServiceSasSignatureValues {@link BlobServiceSasSignatureValues}
     * @param context Additional context that is passed through the code when generating a SAS.
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateSas(BlobServiceSasSignatureValues blobServiceSasSignatureValues, Context context) {
        return new BlobSasImplUtil(blobServiceSasSignatureValues, getContainerName(), getBlobName(),
            getSnapshotId(), getVersionId())
            .generateSas(SasImplUtils.extractSharedKeyCredential(getHttpPipeline()), context);
    }

    /**
     * Queries the entire blob.
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/query-blob-contents">Azure Docs</a></p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.query#String}
     *
     * @param expression The query expression.
     * @return A reactive response containing the queried data.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Flux<ByteBuffer> query(String expression) {
        return queryWithResponse(new BlobQueryOptions(expression))
            .flatMapMany(BlobQueryAsyncResponse::getValue);
    }

    /**
     * Queries the entire blob.
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/query-blob-contents">Azure Docs</a></p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.queryWithResponse#BlobQueryOptions}
     *
     * @param queryOptions {@link BlobQueryOptions The query options}.
     * @return A reactive response containing the queried data.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BlobQueryAsyncResponse> queryWithResponse(BlobQueryOptions queryOptions) {
        try {
            return withContext(context -> queryWithResponse(queryOptions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<BlobQueryAsyncResponse> queryWithResponse(BlobQueryOptions queryOptions, Context context) {
        StorageImplUtils.assertNotNull("queryOptions", queryOptions);
        BlobRequestConditions requestConditions = queryOptions.getRequestConditions() == null
            ? new BlobRequestConditions() : queryOptions.getRequestConditions();

        QuerySerialization in = BlobQueryReader.transformInputSerialization(queryOptions.getInputSerialization(),
            logger);
        QuerySerialization out = BlobQueryReader.transformOutputSerialization(queryOptions.getOutputSerialization(),
            logger);

        QueryRequest qr = new QueryRequest()
            .setExpression(queryOptions.getExpression())
            .setInputSerialization(in)
            .setOutputSerialization(out);

        return this.azureBlobStorage.getBlobs().queryWithResponseAsync(containerName, blobName,
            getSnapshotId(), null, requestConditions.getLeaseId(), requestConditions.getIfModifiedSince(),
            requestConditions.getIfUnmodifiedSince(), requestConditions.getIfMatch(),
            requestConditions.getIfNoneMatch(), requestConditions.getTagsConditions(), null,
            qr, getCustomerProvidedKey(), context)
            .map(response -> new BlobQueryAsyncResponse(response.getRequest(), response.getStatusCode(),
                response.getHeaders(),
                /* Parse the avro reactive stream. */
                new BlobQueryReader(response.getValue(), queryOptions.getProgressConsumer(),
                    queryOptions.getErrorConsumer())
                    .read(),
                ModelHelper.transformQueryHeaders(response.getHeaders())));
    }

    /**
     * Sets the immutability policy on a blob, blob snapshot or blob version.
     * <p> NOTE: Blob Versioning must be enabled on your storage account and the blob must be in a container with
     * object level immutable policy enabled to call this API.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.setImmutabilityPolicy#BlobImmutabilityPolicy}
     *
     * @param immutabilityPolicy {@link BlobImmutabilityPolicy The immutability policy}.
     * @return A reactive response containing the immutability policy.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BlobImmutabilityPolicy> setImmutabilityPolicy(BlobImmutabilityPolicy immutabilityPolicy) {
        try {
            return setImmutabilityPolicyWithResponse(immutabilityPolicy, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Sets the immutability policy on a blob, blob snapshot or blob version.
     * <p> NOTE: Blob Versioning must be enabled on your storage account and the blob must be in a container with
     * immutable storage with versioning enabled to call this API.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.setImmutabilityPolicyWithResponse#BlobImmutabilityPolicy-BlobRequestConditions}
     *
     * @param immutabilityPolicy {@link BlobImmutabilityPolicy The immutability policy}.
     * @param requestConditions {@link BlobRequestConditions}
     * @return A reactive response containing the immutability policy.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BlobImmutabilityPolicy>> setImmutabilityPolicyWithResponse(
        BlobImmutabilityPolicy immutabilityPolicy, BlobRequestConditions requestConditions) {
        try {
            return withContext(context -> setImmutabilityPolicyWithResponse(immutabilityPolicy, requestConditions,
                context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<BlobImmutabilityPolicy>> setImmutabilityPolicyWithResponse(
        BlobImmutabilityPolicy immutabilityPolicy, BlobRequestConditions requestConditions, Context context) {
        context = context == null ? Context.NONE : context;
        BlobImmutabilityPolicy finalImmutabilityPolicy = immutabilityPolicy == null ? new BlobImmutabilityPolicy()
            : immutabilityPolicy;
        if (BlobImmutabilityPolicyMode.MUTABLE.equals(finalImmutabilityPolicy.getPolicyMode())) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                String.format("immutabilityPolicy.policyMode must be %s or %s",
                    BlobImmutabilityPolicyMode.LOCKED.toString(), BlobImmutabilityPolicyMode.UNLOCKED.toString())));
        }

        BlobRequestConditions finalRequestConditions = requestConditions == null
            ? new BlobRequestConditions() : requestConditions;

        ModelHelper.validateConditionsNotPresent(finalRequestConditions,
            EnumSet.of(BlobRequestConditionProperty.LEASE_ID, BlobRequestConditionProperty.TAGS_CONDITIONS,
                BlobRequestConditionProperty.IF_MATCH, BlobRequestConditionProperty.IF_NONE_MATCH,
                BlobRequestConditionProperty.IF_MODIFIED_SINCE), "setImmutabilityPolicy(WithResponse)",
            "requestConditions");

        return this.azureBlobStorage.getBlobs().setImmutabilityPolicyWithResponseAsync(containerName, blobName, null,
            null, finalRequestConditions.getIfUnmodifiedSince(), finalImmutabilityPolicy.getExpiryTime(),
            finalImmutabilityPolicy.getPolicyMode(),
            context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(response -> {
                BlobsSetImmutabilityPolicyHeaders headers = response.getDeserializedHeaders();
                BlobImmutabilityPolicy responsePolicy = new BlobImmutabilityPolicy()
                    .setPolicyMode(headers.getXMsImmutabilityPolicyMode())
                    .setExpiryTime(headers.getXMsImmutabilityPolicyUntilDate());
                return new SimpleResponse<>(response, responsePolicy);
            });
    }

    /**
     * Deletes the immutability policy on a blob, blob snapshot or blob version.
     * <p> NOTE: Blob Versioning must be enabled on your storage account and the blob must be in a container with
     * object level immutable policy enabled to call this API.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.deleteImmutabilityPolicy}
     *
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteImmutabilityPolicy() {
        try {
            return deleteImmutabilityPolicyWithResponse().flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes the immutability policy on a blob, blob snapshot or blob version.
     * <p> NOTE: Blob Versioning must be enabled on your storage account and the blob must be in a container with
     * immutable storage with versioning enabled to call this API.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.deleteImmutabilityPolicyWithResponse}
     *
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteImmutabilityPolicyWithResponse() {
        try {
            return withContext(this::deleteImmutabilityPolicyWithResponse);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> deleteImmutabilityPolicyWithResponse(Context context) {
        context = context == null ? Context.NONE : context;
        return this.azureBlobStorage.getBlobs().deleteImmutabilityPolicyWithResponseAsync(containerName, blobName,
            null, null, context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Sets a legal hold on the blob.
     * <p> NOTE: Blob Versioning must be enabled on your storage account and the blob must be in a container with
     * object level immutable policy enabled to call this API.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.setLegalHold#boolean}
     *
     * @param legalHold Whether or not you want a legal hold on the blob.
     * @return A reactive response containing the legal hold result.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BlobLegalHoldResult> setLegalHold(boolean legalHold) {
        try {
            return setLegalHoldWithResponse(legalHold).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Sets a legal hold on the blob.
     * <p> NOTE: Blob Versioning must be enabled on your storage account and the blob must be in a container with
     * immutable storage with versioning enabled to call this API.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.setLegalHoldWithResponse#boolean}
     *
     * @param legalHold Whether or not you want a legal hold on the blob.
     * @return A reactive response containing the legal hold result.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BlobLegalHoldResult>> setLegalHoldWithResponse(boolean legalHold) {
        try {
            return withContext(context -> setLegalHoldWithResponse(legalHold, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<BlobLegalHoldResult>> setLegalHoldWithResponse(boolean legalHold, Context context) {
        context = context == null ? Context.NONE : context;
        return this.azureBlobStorage.getBlobs().setLegalHoldWithResponseAsync(containerName, blobName,
            legalHold, null, null,
            context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(response -> new SimpleResponse<>(response,
                new InternalBlobLegalHoldResult(response.getDeserializedHeaders().isXMsLegalHold())));
    }
}
