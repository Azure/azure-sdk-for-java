// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.RequestConditions;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollingContext;
import com.azure.core.util.polling.SyncPoller;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.implementation.AzureBlobStorageImplBuilder;
import com.azure.storage.blob.implementation.accesshelpers.BlobPropertiesConstructorProxy;
import com.azure.storage.blob.implementation.models.BlobPropertiesInternalGetProperties;
import com.azure.storage.blob.implementation.models.BlobTag;
import com.azure.storage.blob.implementation.models.BlobTags;
import com.azure.storage.blob.implementation.models.BlobsCopyFromURLHeaders;
import com.azure.storage.blob.implementation.models.BlobsCreateSnapshotHeaders;
import com.azure.storage.blob.implementation.models.BlobsGetAccountInfoHeaders;
import com.azure.storage.blob.implementation.models.BlobsGetPropertiesHeaders;
import com.azure.storage.blob.implementation.models.BlobsGetTagsHeaders;
import com.azure.storage.blob.implementation.models.BlobsQueryHeaders;
import com.azure.storage.blob.implementation.models.BlobsSetImmutabilityPolicyHeaders;
import com.azure.storage.blob.implementation.models.BlobsSetLegalHoldHeaders;
import com.azure.storage.blob.implementation.models.BlobsStartCopyFromURLHeaders;
import com.azure.storage.blob.implementation.models.EncryptionScope;
import com.azure.storage.blob.implementation.models.InternalBlobLegalHoldResult;
import com.azure.storage.blob.implementation.models.QueryRequest;
import com.azure.storage.blob.implementation.models.QuerySerialization;
import com.azure.storage.blob.implementation.util.BlobQueryReader;
import com.azure.storage.blob.implementation.util.BlobRequestConditionProperty;
import com.azure.storage.blob.implementation.util.BlobSasImplUtil;
import com.azure.storage.blob.implementation.util.ByteBufferBackedOutputStreamUtil;
import com.azure.storage.blob.implementation.util.ChunkedDownloadUtils;
import com.azure.storage.blob.implementation.util.ModelHelper;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobBeginCopySourceRequestConditions;
import com.azure.storage.blob.models.BlobCopyInfo;
import com.azure.storage.blob.models.BlobDownloadAsyncResponse;
import com.azure.storage.blob.models.BlobDownloadContentAsyncResponse;
import com.azure.storage.blob.models.BlobDownloadContentResponse;
import com.azure.storage.blob.models.BlobDownloadResponse;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobImmutabilityPolicy;
import com.azure.storage.blob.models.BlobImmutabilityPolicyMode;
import com.azure.storage.blob.models.BlobLegalHoldResult;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobQueryAsyncResponse;
import com.azure.storage.blob.models.BlobQueryResponse;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobSeekableByteChannelReadResult;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.ConsistentReadControl;
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
import com.azure.storage.blob.options.BlobInputStreamOptions;
import com.azure.storage.blob.options.BlobQueryOptions;
import com.azure.storage.blob.options.BlobSeekableByteChannelReadOptions;
import com.azure.storage.blob.options.BlobSetAccessTierOptions;
import com.azure.storage.blob.options.BlobSetTagsOptions;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.Utility;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.SasImplUtils;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.common.implementation.StorageSeekableByteChannel;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.azure.storage.common.implementation.StorageImplUtils.blockWithOptionalTimeout;
import static com.azure.storage.common.implementation.StorageImplUtils.sendRequest;

/**
 * This class provides a client that contains all operations that apply to any blob type.
 *
 * <p>
 * This client offers the ability to download blobs. Note that uploading data is specific to each type of blob. Please
 * refer to the {@link BlockBlobClient}, {@link PageBlobClient}, or {@link AppendBlobClient} for upload options.
 */
public class BlobClientBase {
    private static final ClientLogger LOGGER = new ClientLogger(BlobClientBase.class);
    private static final Set<OpenOption> DEFAULT_OPEN_OPTIONS_SET = Collections.unmodifiableSet(new HashSet<>(
        Arrays.asList(StandardOpenOption.CREATE_NEW, StandardOpenOption.READ, StandardOpenOption.WRITE)));

    /**
     * Backing REST client for the blob client.
     */
    protected final AzureBlobStorageImpl azureBlobStorage;

    private final String snapshot;
    private final String versionId;
    private final CpkInfo customerProvidedKey;

    /**
     * Encryption scope of the blob.
     */
    protected final EncryptionScope encryptionScope;

    /**
     * Storage account name that contains the blob.
     */
    protected final String accountName;

    /**
     * Container name that contains the blob.
     */
    protected final String containerName;

    /**
     * Name of the blob.
     */
    protected final String blobName;

    /**
     * Storage REST API version used in requests to the Storage service.
     */
    protected final BlobServiceVersion serviceVersion;

    private final BlobAsyncClientBase client;

    /**
     * Constructor used by {@link SpecializedBlobClientBuilder}.
     *
     * @param client the async blob client
     */
    protected BlobClientBase(BlobAsyncClientBase client) {
        if (client.getSnapshotId() != null && client.getVersionId() != null) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("'snapshot' and 'versionId' cannot be used at the same time."));
        }
        this.client = client;
        this.azureBlobStorage = new AzureBlobStorageImplBuilder()
            .pipeline(client.getHttpPipeline())
            .url(client.getAccountUrl())
            .version(client.getServiceVersion().getVersion())
            .buildClient();
        this.serviceVersion = client.getServiceVersion();

        this.accountName = client.getAccountName();
        this.containerName = client.getContainerName();
        this.blobName = client.getBlobName();
        this.snapshot = client.getSnapshotId();
        this.customerProvidedKey = client.getCustomerProvidedKey();
        this.encryptionScope = new EncryptionScope().setEncryptionScope(client.getEncryptionScope());
        this.versionId = client.getVersionId();
        /* Check to make sure the uri is valid. We don't want the error to occur later in the generated layer
           when the sas token has already been applied. */
        try {
            URI.create(getBlobUrl());
        } catch (IllegalArgumentException ex) {
            throw LOGGER.logExceptionAsError(ex);
        }
    }

    /**
     * Protected constructor for use by {@link SpecializedBlobClientBuilder}.
     *
     * @param client the async blob client
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
    protected BlobClientBase(BlobAsyncClientBase client, HttpPipeline pipeline, String url,
        BlobServiceVersion serviceVersion, String accountName, String containerName, String blobName, String snapshot,
        CpkInfo customerProvidedKey, EncryptionScope encryptionScope, String versionId) {
        if (snapshot != null && versionId != null) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("'snapshot' and 'versionId' cannot be used at the same time."));
        }
        this.client = client;
        this.azureBlobStorage = new AzureBlobStorageImplBuilder()
            .pipeline(pipeline)
            .url(url)
            .version(serviceVersion.getVersion())
            .buildClient();
        this.serviceVersion = serviceVersion;

        this.accountName = accountName;
        this.containerName = containerName;
        this.blobName = blobName;
        this.snapshot = snapshot;
        this.customerProvidedKey = customerProvidedKey;
        this.encryptionScope = encryptionScope;
        this.versionId = versionId;
        /* Check to make sure the uri is valid. We don't want the error to occur later in the generated layer
           when the sas token has already been applied. */
        try {
            URI.create(getBlobUrl());
        } catch (IllegalArgumentException ex) {
            throw LOGGER.logExceptionAsError(ex);
        }
    }

    /**
     * Creates a new {@link BlobClientBase} linked to the {@code snapshot} of this blob resource.
     *
     * @param snapshot the identifier for a specific snapshot of this blob
     * @return a {@link BlobClientBase} used to interact with the specific snapshot.
     */
    public BlobClientBase getSnapshotClient(String snapshot) {
        return new BlobClientBase(this.client.getSnapshotClient(snapshot), getHttpPipeline(), getAccountUrl(),
            getServiceVersion(), getAccountName(), getContainerName(), getBlobName(), snapshot,
            getCustomerProvidedKey(), encryptionScope, getVersionId());
    }

    /**
     * Creates a new {@link BlobClientBase} linked to the {@code version} of this blob resource.
     *
     * @param versionId the identifier for a specific version of this blob,
     * pass {@code null} to interact with the latest blob version.
     * @return a {@link BlobClientBase} used to interact with the specific version.
     */
    public BlobClientBase getVersionClient(String versionId) {
        return new BlobClientBase(this.client.getVersionClient(versionId), getHttpPipeline(), getAccountUrl(),
            getServiceVersion(), getAccountName(), getContainerName(), getBlobName(), getSnapshotId(),
            getCustomerProvidedKey(), encryptionScope, versionId);
    }

    /**
     * Creates a new {@link BlobClientBase} with the specified {@code encryptionScope}.
     *
     * @param encryptionScope the encryption scope for the blob, pass {@code null} to use no encryption scope.
     * @return a {@link BlobClientBase} with the specified {@code encryptionScope}.
     */
    public BlobClientBase getEncryptionScopeClient(String encryptionScope) {
        EncryptionScope finalEncryptionScope = null;
        if (encryptionScope != null) {
            finalEncryptionScope = new EncryptionScope().setEncryptionScope(encryptionScope);
        }
        return new BlobClientBase(this.client.getEncryptionScopeAsyncClient(encryptionScope), getHttpPipeline(),
            getAccountUrl(), getServiceVersion(), getAccountName(), getContainerName(), getBlobName(), snapshot,
            getCustomerProvidedKey(), finalEncryptionScope, getVersionId());
    }

    /**
     * Creates a new {@link BlobClientBase} with the specified {@code customerProvidedKey}.
     *
     * @param customerProvidedKey the {@link CustomerProvidedKey} for the blob,
     * pass {@code null} to use no customer provided key.
     * @return a {@link BlobClientBase} with the specified {@code customerProvidedKey}.
     */
    public BlobClientBase getCustomerProvidedKeyClient(CustomerProvidedKey customerProvidedKey) {
        CpkInfo finalCustomerProvidedKey = null;
        if (customerProvidedKey != null) {
            finalCustomerProvidedKey = new CpkInfo()
                .setEncryptionKey(customerProvidedKey.getKey())
                .setEncryptionKeySha256(customerProvidedKey.getKeySha256())
                .setEncryptionAlgorithm(customerProvidedKey.getEncryptionAlgorithm());
        }
        return new BlobClientBase(this.client.getCustomerProvidedKeyAsyncClient(customerProvidedKey), getHttpPipeline(),
            getAccountUrl(), getServiceVersion(), getAccountName(), getContainerName(), getBlobName(), snapshot,
            finalCustomerProvidedKey, encryptionScope, getVersionId());
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
     * Get associated account name.
     *
     * @return account name associated with this storage resource.
     */
    public String getAccountName() {
        return this.accountName;
    }

    /**
     * Get the container name.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.getContainerName -->
     * <pre>
     * String containerName = client.getContainerName&#40;&#41;;
     * System.out.println&#40;&quot;The name of the container is &quot; + containerName&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.getContainerName -->
     *
     * @return The name of the container.
     */
    public final String getContainerName() {
        return this.containerName;
    }

    /**
     * Gets a client pointing to the parent container.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.getContainerClient -->
     * <pre>
     * BlobContainerClient containerClient = client.getContainerClient&#40;&#41;;
     * System.out.println&#40;&quot;The name of the container is &quot; + containerClient.getBlobContainerName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.getContainerClient -->
     *
     * @return {@link BlobContainerClient}
     */
    public BlobContainerClient getContainerClient() {
        CustomerProvidedKey encryptionKey = this.customerProvidedKey == null ? null
            : new CustomerProvidedKey(this.customerProvidedKey.getEncryptionKey());
        return new BlobContainerClientBuilder()
            .endpoint(this.getBlobUrl())
            .pipeline(this.getHttpPipeline())
            .serviceVersion(this.serviceVersion)
            .customerProvidedKey(encryptionKey)
            .encryptionScope(this.getEncryptionScope()).buildClient();
        //return client.getContainerClientBuilder().buildClient();
    }

    /**
     * Decodes and gets the blob name.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.getBlobName -->
     * <pre>
     * String blobName = client.getBlobName&#40;&#41;;
     * System.out.println&#40;&quot;The name of the blob is &quot; + blobName&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.getBlobName -->
     *
     * @return The decoded name of the blob.
     */
    public final String getBlobName() {
        return this.blobName;
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
        return this.customerProvidedKey;
    }

    /**
     * Gets the {@code encryption scope} used to encrypt this blob's content on the server.
     *
     * @return the encryption scope used for encryption.
     */
    public String getEncryptionScope() {
        if (encryptionScope == null) {
            return null;
        }
        return encryptionScope.getEncryptionScope();
        //return client.getEncryptionScope();
    }

    /**
     * Gets the service version the client is using.
     *
     * @return the service version the client is using.
     */
    public BlobServiceVersion getServiceVersion() {
        return this.serviceVersion;
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
     * Gets the versionId for a blob resource
     *
     * @return A string that represents the versionId of the snapshot blob
     */
    public String getVersionId() {
        return this.versionId;
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
     * Opens a blob input stream to download the blob.
     *
     * @return An <code>InputStream</code> object that represents the stream to use for reading from the blob.
     * @throws BlobStorageException If a storage service error occurred.
     */
    public BlobInputStream openInputStream() {
        return openInputStream((BlobRange) null, null);
    }

    /**
     * Opens a blob input stream to download the specified range of the blob.
     *
     * @param range {@link BlobRange}
     * @param requestConditions An {@link BlobRequestConditions} object that represents the access conditions for the
     * blob.
     * @return An <code>InputStream</code> object that represents the stream to use for reading from the blob.
     * @throws BlobStorageException If a storage service error occurred.
     */
    public BlobInputStream openInputStream(BlobRange range, BlobRequestConditions requestConditions) {
        return openInputStream(new BlobInputStreamOptions().setRange(range).setRequestConditions(requestConditions));
    }

    /**
     * Opens a blob input stream to download the specified range of the blob.
     *
     * @param options {@link BlobInputStreamOptions}
     * @return An <code>InputStream</code> object that represents the stream to use for reading from the blob.
     * @throws BlobStorageException If a storage service error occurred.
     */
    public BlobInputStream openInputStream(BlobInputStreamOptions options) {
        return openInputStream(options, null);
    }

    /**
     * Opens a blob input stream to download the specified range of the blob.
     *
     * @param options {@link BlobInputStreamOptions}
     * @param context {@link Context}
     * @return An <code>InputStream</code> object that represents the stream to use for reading from the blob.
     * @throws BlobStorageException If a storage service error occurred.
     */
    public BlobInputStream openInputStream(BlobInputStreamOptions options, Context context) {
        Context contextFinal = context == null ? Context.NONE : context;
        options = options == null ? new BlobInputStreamOptions() : options;
        ConsistentReadControl consistentReadControl = options.getConsistentReadControl() == null
            ? ConsistentReadControl.ETAG : options.getConsistentReadControl();
        BlobRequestConditions requestConditions = options.getRequestConditions() == null
            ? new BlobRequestConditions() : options.getRequestConditions();

        BlobRange range = options.getRange() == null ? new BlobRange(0) : options.getRange();
        int chunkSize = options.getBlockSize() == null ? 4 * Constants.MB : options.getBlockSize();

        com.azure.storage.common.ParallelTransferOptions parallelTransferOptions =
            new com.azure.storage.common.ParallelTransferOptions().setBlockSizeLong((long) chunkSize);
        BiFunction<BlobRange, BlobRequestConditions, Mono<BlobDownloadAsyncResponse>> downloadFunc =
            (chunkRange, conditions) -> client.downloadStreamWithResponse(chunkRange, null, conditions, false, contextFinal);
        return ChunkedDownloadUtils.downloadFirstChunk(range, parallelTransferOptions, requestConditions, downloadFunc, true)
            .flatMap(tuple3 -> {
                BlobDownloadAsyncResponse downloadResponse = tuple3.getT3();
                return FluxUtil.collectBytesInByteBufferStream(downloadResponse.getValue())
                    .map(ByteBuffer::wrap)
                    .zipWith(Mono.just(downloadResponse));
            })
            .flatMap(tuple2 -> {
                ByteBuffer initialBuffer = tuple2.getT1();
                BlobDownloadAsyncResponse downloadResponse = tuple2.getT2();

                BlobProperties properties = ModelHelper.buildBlobPropertiesResponse(downloadResponse).getValue();

                String eTag = properties.getETag();
                String versionId = properties.getVersionId();
                BlobClientBase client = this;

                switch (consistentReadControl) {
                    case NONE:
                        break;
                    case ETAG:
                        // Target the user specified eTag by default. If not provided, target the latest eTag.
                        if (requestConditions.getIfMatch() == null) {
                            requestConditions.setIfMatch(eTag);
                        }
                        break;
                    case VERSION_ID:
                        if (versionId == null) {
                            return FluxUtil.monoError(LOGGER,
                                new UnsupportedOperationException("Versioning is not supported on this account."));
                        } else {
                            // Target the user specified version by default. If not provided, target the latest version.
                            if (getVersionId() == null) {
                                client = getVersionClient(versionId);
                            }
                        }
                        break;
                    default:
                        return FluxUtil.monoError(LOGGER, new IllegalArgumentException("Concurrency control type not "
                            + "supported."));
                }

                return Mono.just(new BlobInputStream(client, range.getOffset(), range.getCount(), chunkSize,
                    initialBuffer, requestConditions, properties, contextFinal));
            }).block();
    }

    /**
     * Opens a seekable byte channel in read-only mode to download the blob.
     *
     * @param options {@link BlobSeekableByteChannelReadOptions}
     * @param context {@link Context}
     * @return A <code>SeekableByteChannel</code> that represents the channel to use for reading from the blob.
     * @throws BlobStorageException If a storage service error occurred.
     */
    public BlobSeekableByteChannelReadResult openSeekableByteChannelRead(
        BlobSeekableByteChannelReadOptions options, Context context) {
        context = context == null ? Context.NONE : context;
        options = options == null ? new BlobSeekableByteChannelReadOptions() : options;
        ConsistentReadControl consistentReadControl = options.getConsistentReadControl() == null
            ? ConsistentReadControl.ETAG : options.getConsistentReadControl();
        int chunkSize = options.getReadSizeInBytes() == null ? 4 * Constants.MB : options.getReadSizeInBytes();
        long initialPosition = options.getInitialPosition() == null ? 0 : options.getInitialPosition();

        ByteBuffer initialRange = ByteBuffer.allocate(chunkSize);
        BlobProperties properties;
        BlobDownloadResponse response;
        try (ByteBufferBackedOutputStreamUtil dstStream = new ByteBufferBackedOutputStreamUtil(initialRange)) {
            response = this.downloadStreamWithResponse(dstStream,
                new BlobRange(initialPosition, (long) initialRange.remaining()), null /*downloadRetryOptions*/,
                options.getRequestConditions(), false, null, context);
            properties = ModelHelper.buildBlobPropertiesResponse(response).getValue();
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
        }

        initialRange.limit(initialRange.position());
        initialRange.rewind();

        BlobClientBase behaviorClient = this;
        BlobRequestConditions requestConditions = options.getRequestConditions();
        switch (consistentReadControl) {
            case NONE:
                break;
            case ETAG:
                requestConditions = requestConditions != null ? requestConditions : new BlobRequestConditions();
                // If etag locking but no explicitly specified etag, use the etag from prefetch
                if (requestConditions.getIfMatch() == null) {
                    requestConditions.setIfMatch(properties.getETag());
                }
                break;
            case VERSION_ID:
                if (properties.getVersionId() == null) {
                    throw LOGGER.logExceptionAsError(
                        new UnsupportedOperationException(
                            "Version ID locking unsupported. Versioning is not supported on this account."));
                } else {
                    // If version locking but no explicitly specified version, use the latest version from prefetch
                    if (getVersionId() == null) {
                        behaviorClient = this.getVersionClient(properties.getVersionId());
                    }
                }
                break;
            default:
                throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                    "Concurrency control type " + consistentReadControl + " not supported."));
        }

        StorageSeekableByteChannelBlobReadBehavior behavior = new StorageSeekableByteChannelBlobReadBehavior(
            behaviorClient, initialRange, initialPosition, properties.getBlobSize(), requestConditions);

        SeekableByteChannel channel = new StorageSeekableByteChannel(chunkSize, behavior, initialPosition);
        return new BlobSeekableByteChannelReadResult(channel, properties);
    }

    /**
     * Gets if the blob this client represents exists in the cloud.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.exists -->
     * <pre>
     * System.out.printf&#40;&quot;Exists? %b%n&quot;, client.exists&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.exists -->
     *
     * @return true if the blob exists, false if it doesn't
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Boolean exists() {
        return existsWithResponse(null, Context.NONE).getValue();
    }

    /**
     * Gets if the blob this client represents exists in the cloud.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.existsWithResponse#Duration-Context -->
     * <pre>
     * System.out.printf&#40;&quot;Exists? %b%n&quot;, client.existsWithResponse&#40;timeout, new Context&#40;key2, value2&#41;&#41;.getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.existsWithResponse#Duration-Context -->
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return true if the blob exists, false if it doesn't
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Boolean> existsWithResponse(Duration timeout, Context context) {
        try {
            Callable<Response<Void>> operation = () -> this.azureBlobStorage.getBlobs()
                .getPropertiesNoCustomHeadersWithResponse(containerName, blobName, snapshot, versionId, null, null,
                    null, null, null, null, null, null, customerProvidedKey, context);
            return new SimpleResponse<>(sendRequest(operation, timeout, BlobStorageException.class), true);
        } catch (RuntimeException e) {
            HttpResponse response = ((HttpResponseException) e).getResponse();
            if (e instanceof BlobStorageException
                && BlobErrorCode.BLOB_USES_CUSTOMER_SPECIFIED_ENCRYPTION.equals(((BlobStorageException) e).getErrorCode())) {
                return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), true);
            } else if (ModelHelper.checkBlobDoesNotExistStatusCode(e)) {
                return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), false);
            } else {
                throw LOGGER.logExceptionAsError(e);
            }
        }
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
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.beginCopy#String-Duration -->
     * <pre>
     * final SyncPoller&lt;BlobCopyInfo, Void&gt; poller = client.beginCopy&#40;url, Duration.ofSeconds&#40;2&#41;&#41;;
     * PollResponse&lt;BlobCopyInfo&gt; pollResponse = poller.poll&#40;&#41;;
     * System.out.printf&#40;&quot;Copy identifier: %s%n&quot;, pollResponse.getValue&#40;&#41;.getCopyId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.beginCopy#String-Duration -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/copy-blob">Azure Docs</a></p>
     *
     * @param sourceUrl The source URL to copy from. URLs outside of Azure may only be copied to block blobs.
     * @param pollInterval Duration between each poll for the copy status. If none is specified, a default of one second
     * is used.
     * @return A {@link SyncPoller} to poll the progress of blob copy operation.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<BlobCopyInfo, Void> beginCopy(String sourceUrl, Duration pollInterval) {
        return beginCopy(sourceUrl,
                null,
                null,
                null,
                null,
                null, pollInterval);
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
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.beginCopy#String-Map-AccessTier-RehydratePriority-RequestConditions-BlobRequestConditions-Duration -->
     * <pre>
     * Map&lt;String, String&gt; metadata = Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;;
     * RequestConditions modifiedRequestConditions = new RequestConditions&#40;&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;7&#41;&#41;;
     * BlobRequestConditions blobRequestConditions = new BlobRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     * SyncPoller&lt;BlobCopyInfo, Void&gt; poller = client.beginCopy&#40;url, metadata, AccessTier.HOT,
     *     RehydratePriority.STANDARD, modifiedRequestConditions, blobRequestConditions, Duration.ofSeconds&#40;2&#41;&#41;;
     *
     * PollResponse&lt;BlobCopyInfo&gt; response = poller.waitUntil&#40;LongRunningOperationStatus.SUCCESSFULLY_COMPLETED&#41;;
     * System.out.printf&#40;&quot;Copy identifier: %s%n&quot;, response.getValue&#40;&#41;.getCopyId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.beginCopy#String-Map-AccessTier-RehydratePriority-RequestConditions-BlobRequestConditions-Duration -->
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
     * @return A {@link SyncPoller} to poll the progress of blob copy operation.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<BlobCopyInfo, Void> beginCopy(String sourceUrl, Map<String, String> metadata, AccessTier tier,
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
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.beginCopy#BlobBeginCopyOptions -->
     * <pre>
     * Map&lt;String, String&gt; metadata = Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;;
     * Map&lt;String, String&gt; tags = Collections.singletonMap&#40;&quot;tag&quot;, &quot;value&quot;&#41;;
     * BlobBeginCopySourceRequestConditions modifiedRequestConditions = new BlobBeginCopySourceRequestConditions&#40;&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;7&#41;&#41;;
     * BlobRequestConditions blobRequestConditions = new BlobRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     * SyncPoller&lt;BlobCopyInfo, Void&gt; poller = client.beginCopy&#40;new BlobBeginCopyOptions&#40;url&#41;.setMetadata&#40;metadata&#41;
     *     .setTags&#40;tags&#41;.setTier&#40;AccessTier.HOT&#41;.setRehydratePriority&#40;RehydratePriority.STANDARD&#41;
     *     .setSourceRequestConditions&#40;modifiedRequestConditions&#41;
     *     .setDestinationRequestConditions&#40;blobRequestConditions&#41;.setPollInterval&#40;Duration.ofSeconds&#40;2&#41;&#41;&#41;;
     *
     * PollResponse&lt;BlobCopyInfo&gt; response = poller.waitUntil&#40;LongRunningOperationStatus.SUCCESSFULLY_COMPLETED&#41;;
     * System.out.printf&#40;&quot;Copy identifier: %s%n&quot;, response.getValue&#40;&#41;.getCopyId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.beginCopy#BlobBeginCopyOptions -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/copy-blob">Azure Docs</a></p>
     *
     * @param options {@link BlobBeginCopyOptions}
     * @return A {@link SyncPoller} to poll the progress of blob copy operation.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<BlobCopyInfo, Void> beginCopy(BlobBeginCopyOptions options) {
        StorageImplUtils.assertNotNull("options", options);
        final AtomicReference<String> copyId = new AtomicReference<>();
        final Duration interval = options.getPollInterval() != null ? options.getPollInterval() : Duration.ofSeconds(1);
        final BlobBeginCopySourceRequestConditions sourceModifiedConditions =
            options.getSourceRequestConditions() == null
                ? new BlobBeginCopySourceRequestConditions()
                : options.getSourceRequestConditions();
        final BlobRequestConditions destinationRequestConditions =
            options.getDestinationRequestConditions() == null
                ? new BlobRequestConditions()
                : options.getDestinationRequestConditions();
        final BlobImmutabilityPolicy immutabilityPolicy = options.getImmutabilityPolicy() == null
            ? new BlobImmutabilityPolicy() : options.getImmutabilityPolicy();

        Function<PollingContext<BlobCopyInfo>, PollResponse<BlobCopyInfo>> syncActivationOperation = (pollingContext) -> {
            try {
                new URL(options.getSourceUrl());
            } catch (MalformedURLException ex) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException("'sourceUrl' is not a valid url.", ex));
            }
            ResponseBase<BlobsStartCopyFromURLHeaders, Void> response =
                azureBlobStorage.getBlobs().startCopyFromURLWithResponse(containerName, blobName,
                    options.getSourceUrl(), null, options.getMetadata(), options.getTier(),
                    options.getRehydratePriority(), sourceModifiedConditions.getIfModifiedSince(),
                    sourceModifiedConditions.getIfUnmodifiedSince(), sourceModifiedConditions.getIfMatch(),
                    sourceModifiedConditions.getIfNoneMatch(), sourceModifiedConditions.getTagsConditions(),
                    destinationRequestConditions.getIfModifiedSince(),
                    destinationRequestConditions.getIfUnmodifiedSince(), destinationRequestConditions.getIfMatch(),
                    destinationRequestConditions.getIfNoneMatch(), destinationRequestConditions.getTagsConditions(),
                    destinationRequestConditions.getLeaseId(), null, ModelHelper.tagsToString(options.getTags()),
                    options.isSealDestination(), immutabilityPolicy.getExpiryTime(),
                    immutabilityPolicy.getPolicyMode(), options.isLegalHold(), Context.NONE);

            BlobsStartCopyFromURLHeaders headers = response.getDeserializedHeaders();
            copyId.set(headers.getXMsCopyId());

            return new PollResponse<>(
                LongRunningOperationStatus.IN_PROGRESS,
                new BlobCopyInfo(options.getSourceUrl(), headers.getXMsCopyId(), headers.getXMsCopyStatus(),
                    headers.getETag(), headers.getLastModified(), ModelHelper.getErrorCode(response.getHeaders()),
                    headers.getXMsVersionId())
            );
        };

        Function<PollingContext<BlobCopyInfo>, PollResponse<BlobCopyInfo>> pollOperation = (pollingContext) ->
            onPoll(pollingContext.getLatestResponse(), destinationRequestConditions);

        BiFunction<PollingContext<BlobCopyInfo>, PollResponse<BlobCopyInfo>, BlobCopyInfo> cancelOperation =
            (pollingContext, firstResponse) -> {
                if (firstResponse == null || firstResponse.getValue() == null) {
                    throw LOGGER.logExceptionAsError(
                        new IllegalArgumentException("Cannot cancel a poll response that never started."));
                }
                final String copyIdentifier = firstResponse.getValue().getCopyId();
                if (!CoreUtils.isNullOrEmpty(copyIdentifier)) {
                    LOGGER.info("Cancelling copy operation for copy id: {}", copyIdentifier);
                    abortCopyFromUrl(copyIdentifier);
                    return firstResponse.getValue();
                }
                return null;
            };

        Function<PollingContext<BlobCopyInfo>, Void> fetchResultOperation = (pollingContext) -> null;
        return SyncPoller.createPoller(interval, syncActivationOperation, pollOperation, cancelOperation,
            fetchResultOperation);
    }


    private PollResponse<BlobCopyInfo> onPoll(PollResponse<BlobCopyInfo> pollResponse,
        BlobRequestConditions requestConditions) {
        if (pollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED
            || pollResponse.getStatus() == LongRunningOperationStatus.FAILED) {
            return pollResponse;
        }

        final BlobCopyInfo lastInfo = pollResponse.getValue();
        if (lastInfo == null) {
            LOGGER.warning("BlobCopyInfo does not exist. Activation operation failed.");
            return new PollResponse<>(LongRunningOperationStatus.fromString("COPY_START_FAILED", true), null);
        }

        try {
            Response<BlobProperties> response = getPropertiesWithResponse(requestConditions, null, null);
            BlobProperties value = response.getValue();
            final CopyStatusType status = value.getCopyStatus();
            final BlobCopyInfo result = new BlobCopyInfo(value.getCopySource(), value.getCopyId(), status,
                value.getETag(), value.getCopyCompletionTime(), value.getCopyStatusDescription(), value.getVersionId());

            LongRunningOperationStatus operationStatus = ModelHelper.mapStatusToLongRunningOperationStatus(status);
            return new PollResponse<>(operationStatus, result);
        } catch (RuntimeException e) {
            return new PollResponse<>(LongRunningOperationStatus.fromString("POLLING_FAILED", true), lastInfo);
        }
    }



    /**
     * Stops a pending copy that was previously started and leaves a destination blob with 0 length and metadata.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.abortCopyFromUrl#String -->
     * <pre>
     * client.abortCopyFromUrl&#40;copyId&#41;;
     * System.out.println&#40;&quot;Aborted copy completed.&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.abortCopyFromUrl#String -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/abort-copy-blob">Azure Docs</a></p>
     *
     * @param copyId The id of the copy operation to abort.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void abortCopyFromUrl(String copyId) {
        abortCopyFromUrlWithResponse(copyId, null, null, Context.NONE);
    }

    /**
     * Stops a pending copy that was previously started and leaves a destination blob with 0 length and metadata.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.abortCopyFromUrlWithResponse#String-String-Duration-Context -->
     * <pre>
     * System.out.printf&#40;&quot;Aborted copy completed with status %d%n&quot;,
     *     client.abortCopyFromUrlWithResponse&#40;copyId, leaseId, timeout,
     *         new Context&#40;key2, value2&#41;&#41;.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.abortCopyFromUrlWithResponse#String-String-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/abort-copy-blob">Azure Docs</a></p>
     *
     * @param copyId The id of the copy operation to abort.
     * @param leaseId The lease ID the active lease on the blob must match.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> abortCopyFromUrlWithResponse(String copyId, String leaseId, Duration timeout,
        Context context) {
        Context finalContext = context == null ? Context.NONE : context;
        Callable<Response<Void>> operation = () ->
            this.azureBlobStorage.getBlobs().abortCopyFromURLNoCustomHeadersWithResponse(containerName, blobName,
                copyId, null, leaseId, null, finalContext);

        return sendRequest(operation, timeout, BlobStorageException.class);
    }

    /**
     * Copies the data at the source URL to a blob and waits for the copy to complete before returning a response.
     * <p>
     * The source must be a block blob no larger than 256MB. The source must also be either public or have a sas token
     * attached. The URL must be URL encoded.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.copyFromUrl#String -->
     * <pre>
     * System.out.printf&#40;&quot;Copy identifier: %s%n&quot;, client.copyFromUrl&#40;url&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.copyFromUrl#String -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/copy-blob-from-url">Azure Docs</a></p>
     *
     * @param copySource The source URL to copy from.
     * @return The copy ID for the long running operation.
     * @throws IllegalArgumentException If {@code copySource} is a malformed {@link URL}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public String copyFromUrl(String copySource) {
        return copyFromUrlWithResponse(copySource, null, null, null, null, null, Context.NONE).getValue();
    }

    /**
     * Copies the data at the source URL to a blob and waits for the copy to complete before returning a response.
     * <p>
     * The source must be a block blob no larger than 256MB. The source must also be either public or have a sas token
     * attached. The URL must be URL encoded.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.copyFromUrlWithResponse#String-Map-AccessTier-RequestConditions-BlobRequestConditions-Duration-Context -->
     * <pre>
     * Map&lt;String, String&gt; metadata = Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;;
     * RequestConditions modifiedRequestConditions = new RequestConditions&#40;&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;7&#41;&#41;;
     * BlobRequestConditions blobRequestConditions = new BlobRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     *
     * System.out.printf&#40;&quot;Copy identifier: %s%n&quot;,
     *     client.copyFromUrlWithResponse&#40;url, metadata, AccessTier.HOT, modifiedRequestConditions,
     *         blobRequestConditions, timeout,
     *         new Context&#40;key1, value1&#41;&#41;.getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.copyFromUrlWithResponse#String-Map-AccessTier-RequestConditions-BlobRequestConditions-Duration-Context -->
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
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The copy ID for the long running operation.
     * @throws IllegalArgumentException If {@code copySource} is a malformed {@link URL}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<String> copyFromUrlWithResponse(String copySource, Map<String, String> metadata, AccessTier tier,
            RequestConditions sourceModifiedRequestConditions, BlobRequestConditions destRequestConditions,
            Duration timeout, Context context) {
        return this.copyFromUrlWithResponse(new BlobCopyFromUrlOptions(copySource).setMetadata(metadata)
            .setTier(tier).setSourceRequestConditions(sourceModifiedRequestConditions)
            .setDestinationRequestConditions(destRequestConditions), timeout, context);
    }

    /**
     * Copies the data at the source URL to a blob and waits for the copy to complete before returning a response.
     * <p>
     * The source must be a block blob no larger than 256MB. The source must also be either public or have a sas token
     * attached. The URL must be URL encoded.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.copyFromUrlWithResponse#BlobCopyFromUrlOptions-Duration-Context -->
     * <pre>
     * Map&lt;String, String&gt; metadata = Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;;
     * Map&lt;String, String&gt; tags = Collections.singletonMap&#40;&quot;tag&quot;, &quot;value&quot;&#41;;
     * RequestConditions modifiedRequestConditions = new RequestConditions&#40;&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;7&#41;&#41;;
     * BlobRequestConditions blobRequestConditions = new BlobRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     *
     * System.out.printf&#40;&quot;Copy identifier: %s%n&quot;,
     *     client.copyFromUrlWithResponse&#40;new BlobCopyFromUrlOptions&#40;url&#41;.setMetadata&#40;metadata&#41;.setTags&#40;tags&#41;
     *         .setTier&#40;AccessTier.HOT&#41;.setSourceRequestConditions&#40;modifiedRequestConditions&#41;
     *         .setDestinationRequestConditions&#40;blobRequestConditions&#41;, timeout,
     *         new Context&#40;key1, value1&#41;&#41;.getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.copyFromUrlWithResponse#BlobCopyFromUrlOptions-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/copy-blob-from-url">Azure Docs</a></p>
     *
     * @param options {@link BlobCopyFromUrlOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The copy ID for the long running operation.
     * @throws IllegalArgumentException If {@code copySource} is a malformed {@link URL}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<String> copyFromUrlWithResponse(BlobCopyFromUrlOptions options, Duration timeout,
        Context context) {
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
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'copySource' is not a valid url.", ex));
        }
        String sourceAuth = options.getSourceAuthorization() == null
            ? null : options.getSourceAuthorization().toString();

        Callable<ResponseBase<BlobsCopyFromURLHeaders, Void>> operation = () ->
            this.azureBlobStorage.getBlobs().copyFromURLWithResponse(containerName, blobName, options.getCopySource(),
                    null, options.getMetadata(), options.getTier(), sourceModifiedRequestConditions.getIfModifiedSince(),
                    sourceModifiedRequestConditions.getIfUnmodifiedSince(), sourceModifiedRequestConditions.getIfMatch(),
                    sourceModifiedRequestConditions.getIfNoneMatch(), destRequestConditions.getIfModifiedSince(),
                    destRequestConditions.getIfUnmodifiedSince(), destRequestConditions.getIfMatch(),
                    destRequestConditions.getIfNoneMatch(), destRequestConditions.getTagsConditions(),
                    destRequestConditions.getLeaseId(), null, null, ModelHelper.tagsToString(options.getTags()),
                    immutabilityPolicy.getExpiryTime(), immutabilityPolicy.getPolicyMode(), options.hasLegalHold(),
                    sourceAuth, options.getCopySourceTagsMode(), this.encryptionScope, context);

        ResponseBase<BlobsCopyFromURLHeaders, Void> response = sendRequest(operation, timeout, BlobStorageException.class);
        return new SimpleResponse<>(response, response.getDeserializedHeaders().getXMsCopyId());
    }

    /**
     * Downloads the entire blob into an output stream. Uploading data must be done from the {@link BlockBlobClient},
     * {@link PageBlobClient}, or {@link AppendBlobClient}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.download#OutputStream -->
     * <pre>
     * client.download&#40;new ByteArrayOutputStream&#40;&#41;&#41;;
     * System.out.println&#40;&quot;Download completed.&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.download#OutputStream -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * <p>This method will be deprecated in the future. Use {@link #downloadStream(OutputStream)} instead.
     *
     * @param stream A non-null {@link OutputStream} instance where the downloaded data will be written.
     * @throws UncheckedIOException If an I/O error occurs.
     * @throws NullPointerException if {@code stream} is null
     * @deprecated use {@link #downloadStream(OutputStream)} instead.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    @Deprecated
    public void download(OutputStream stream) {
        downloadStream(stream);
    }

    /**
     * Downloads the entire blob into an output stream. Uploading data must be done from the {@link BlockBlobClient},
     * {@link PageBlobClient}, or {@link AppendBlobClient}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.downloadStream#OutputStream -->
     * <pre>
     * client.downloadStream&#40;new ByteArrayOutputStream&#40;&#41;&#41;;
     * System.out.println&#40;&quot;Download completed.&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.downloadStream#OutputStream -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param stream A non-null {@link OutputStream} instance where the downloaded data will be written.
     * @throws UncheckedIOException If an I/O error occurs.
     * @throws NullPointerException if {@code stream} is null
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void downloadStream(OutputStream stream) {
        downloadWithResponse(stream, null, null, null, false, null, Context.NONE);
    }

    /**
     * Downloads the entire blob. Uploading data must be done from the {@link BlockBlobClient},
     * {@link PageBlobClient}, or {@link AppendBlobClient}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobClient.downloadContent -->
     * <pre>
     * BinaryData data = client.downloadContent&#40;&#41;;
     * System.out.printf&#40;&quot;Downloaded %s&quot;, data.toString&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobClient.downloadContent -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * <p>This method supports downloads up to 2GB of data. Content will be buffered in memory. If the blob is larger,
     * use {@link #downloadStream(OutputStream)} to download larger blobs.</p>
     *
     * @return The content of the blob.
     * @throws UncheckedIOException If an I/O error occurs.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData downloadContent() {
        return blockWithOptionalTimeout(client.downloadContent(), null);
    }

    /**
     * Downloads a range of bytes from a blob into an output stream. Uploading data must be done from the {@link
     * BlockBlobClient}, {@link PageBlobClient}, or {@link AppendBlobClient}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.downloadWithResponse#OutputStream-BlobRange-DownloadRetryOptions-BlobRequestConditions-boolean-Duration-Context -->
     * <pre>
     * BlobRange range = new BlobRange&#40;1024, 2048L&#41;;
     * DownloadRetryOptions options = new DownloadRetryOptions&#40;&#41;.setMaxRetryRequests&#40;5&#41;;
     *
     * System.out.printf&#40;&quot;Download completed with status %d%n&quot;,
     *     client.downloadWithResponse&#40;new ByteArrayOutputStream&#40;&#41;, range, options, null, false,
     *         timeout, new Context&#40;key2, value2&#41;&#41;.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.downloadWithResponse#OutputStream-BlobRange-DownloadRetryOptions-BlobRequestConditions-boolean-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * <p>This method will be deprecated in the future.
     * Use {@link #downloadStreamWithResponse(OutputStream, BlobRange, DownloadRetryOptions,
     * BlobRequestConditions, boolean, Duration, Context)} instead.
     *
     * @param stream A non-null {@link OutputStream} instance where the downloaded data will be written.
     * @param range {@link BlobRange}
     * @param options {@link DownloadRetryOptions}
     * @param requestConditions {@link BlobRequestConditions}
     * @param getRangeContentMd5 Whether the contentMD5 for the specified blob range should be returned.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers.
     * @throws UncheckedIOException If an I/O error occurs.
     * @throws NullPointerException if {@code stream} is null
     * @deprecated use {@link #downloadStreamWithResponse(OutputStream, BlobRange, DownloadRetryOptions, BlobRequestConditions, boolean, Duration, Context)} instead.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    @Deprecated
    public BlobDownloadResponse downloadWithResponse(OutputStream stream, BlobRange range,
        DownloadRetryOptions options, BlobRequestConditions requestConditions, boolean getRangeContentMd5,
        Duration timeout, Context context) {
        return downloadStreamWithResponse(stream, range,
            options, requestConditions, getRangeContentMd5, timeout, context);
    }

    /**
     * Downloads a range of bytes from a blob into an output stream. Uploading data must be done from the {@link
     * BlockBlobClient}, {@link PageBlobClient}, or {@link AppendBlobClient}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.downloadStreamWithResponse#OutputStream-BlobRange-DownloadRetryOptions-BlobRequestConditions-boolean-Duration-Context -->
     * <pre>
     * BlobRange range = new BlobRange&#40;1024, 2048L&#41;;
     * DownloadRetryOptions options = new DownloadRetryOptions&#40;&#41;.setMaxRetryRequests&#40;5&#41;;
     *
     * System.out.printf&#40;&quot;Download completed with status %d%n&quot;,
     *     client.downloadStreamWithResponse&#40;new ByteArrayOutputStream&#40;&#41;, range, options, null, false,
     *         timeout, new Context&#40;key2, value2&#41;&#41;.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.downloadStreamWithResponse#OutputStream-BlobRange-DownloadRetryOptions-BlobRequestConditions-boolean-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param stream A non-null {@link OutputStream} instance where the downloaded data will be written.
     * @param range {@link BlobRange}
     * @param options {@link DownloadRetryOptions}
     * @param requestConditions {@link BlobRequestConditions}
     * @param getRangeContentMd5 Whether the contentMD5 for the specified blob range should be returned.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers.
     * @throws UncheckedIOException If an I/O error occurs.
     * @throws NullPointerException if {@code stream} is null
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BlobDownloadResponse downloadStreamWithResponse(OutputStream stream, BlobRange range,
        DownloadRetryOptions options, BlobRequestConditions requestConditions, boolean getRangeContentMd5,
        Duration timeout, Context context) {
        StorageImplUtils.assertNotNull("stream", stream);
        Mono<BlobDownloadResponse> download = client
            .downloadStreamWithResponse(range, options, requestConditions, getRangeContentMd5, context)
            .flatMap(response -> FluxUtil.writeToOutputStream(response.getValue(), stream)
                .thenReturn(new BlobDownloadResponse(response)));

        return blockWithOptionalTimeout(download, timeout);
    }

    /**
     * Downloads a range of bytes from a blob into an output stream. Uploading data must be done from the {@link
     * BlockBlobClient}, {@link PageBlobClient}, or {@link AppendBlobClient}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.downloadContentWithResponse#DownloadRetryOptions-BlobRequestConditions-Duration-Context -->
     * <pre>
     * DownloadRetryOptions options = new DownloadRetryOptions&#40;&#41;.setMaxRetryRequests&#40;5&#41;;
     *
     * BlobDownloadContentResponse contentResponse = client.downloadContentWithResponse&#40;options, null,
     *     timeout, new Context&#40;key2, value2&#41;&#41;;
     * BinaryData content = contentResponse.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Download completed with status %d and content%s%n&quot;,
     *     contentResponse.getStatusCode&#40;&#41;, content.toString&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.downloadContentWithResponse#DownloadRetryOptions-BlobRequestConditions-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * <p>This method supports downloads up to 2GB of data. Content will be buffered in memory. If the blob is larger,
     * use {@link #downloadStreamWithResponse(OutputStream, BlobRange,
     * DownloadRetryOptions, BlobRequestConditions, boolean, Duration, Context)}  to download larger blobs.</p>
     *
     * @param options {@link DownloadRetryOptions}
     * @param requestConditions {@link BlobRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BlobDownloadContentResponse downloadContentWithResponse(
        DownloadRetryOptions options, BlobRequestConditions requestConditions, Duration timeout, Context context) {
        Mono<BlobDownloadContentResponse> download = client
            .downloadStreamWithResponse(null, options, requestConditions, false, context)
            .flatMap(r ->
                BinaryData.fromFlux(r.getValue())
                    .map(data ->
                        new BlobDownloadContentAsyncResponse(
                            r.getRequest(), r.getStatusCode(),
                            r.getHeaders(), data,
                            r.getDeserializedHeaders())
                    ))
            .map(BlobDownloadContentResponse::new);

        return blockWithOptionalTimeout(download, timeout);
    }

    /**
     * Downloads a range of bytes from a blob into an output stream. Uploading data must be done from the {@link
     * BlockBlobClient}, {@link PageBlobClient}, or {@link AppendBlobClient}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.downloadContentWithResponse#DownloadRetryOptions-BlobRequestConditions-BlobRange-boolean-Duration-Context -->
     * <pre>
     * DownloadRetryOptions options = new DownloadRetryOptions&#40;&#41;.setMaxRetryRequests&#40;5&#41;;
     * BlobRange range = new BlobRange&#40;1024, 2048L&#41;;
     *
     * BlobDownloadContentResponse contentResponse = client.downloadContentWithResponse&#40;options, null,
     *     range, false, timeout, new Context&#40;key2, value2&#41;&#41;;
     * BinaryData content = contentResponse.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Download completed with status %d and content%s%n&quot;,
     *     contentResponse.getStatusCode&#40;&#41;, content.toString&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.downloadContentWithResponse#DownloadRetryOptions-BlobRequestConditions-BlobRange-boolean-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * <p>This method supports downloads up to 2GB of data. Content will be buffered in memory. If the blob is larger,
     * use {@link #downloadStreamWithResponse(OutputStream, BlobRange,
     * DownloadRetryOptions, BlobRequestConditions, boolean, Duration, Context)}  to download larger blobs.</p>
     *
     * @param options {@link DownloadRetryOptions}
     * @param requestConditions {@link BlobRequestConditions}
     * @param range {@link BlobRange}
     * @param getRangeContentMd5 Whether the contentMD5 for the specified blob range should be returned.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BlobDownloadContentResponse downloadContentWithResponse(DownloadRetryOptions options,
        BlobRequestConditions requestConditions, BlobRange range,  boolean getRangeContentMd5, Duration timeout,
        Context context) {
        Mono<BlobDownloadContentResponse> download = client
            .downloadStreamWithResponse(range, options, requestConditions, getRangeContentMd5, context)
            .flatMap(r ->
                BinaryData.fromFlux(r.getValue())
                    .map(data ->
                        new BlobDownloadContentAsyncResponse(
                            r.getRequest(), r.getStatusCode(),
                            r.getHeaders(), data,
                            r.getDeserializedHeaders())
                    ))
            .map(BlobDownloadContentResponse::new);

        return blockWithOptionalTimeout(download, timeout);
    }

    /**
     * Downloads the entire blob into a file specified by the path.
     *
     * <p>The file will be created and must not exist, if the file already exists a {@link FileAlreadyExistsException}
     * will be thrown.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.downloadToFile#String -->
     * <pre>
     * client.downloadToFile&#40;file&#41;;
     * System.out.println&#40;&quot;Completed download to file&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.downloadToFile#String -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param filePath A {@link String} representing the filePath where the downloaded data will be written.
     * @return The blob properties and metadata.
     * @throws UncheckedIOException If an I/O error occurs
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BlobProperties downloadToFile(String filePath) {
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
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.downloadToFile#String-boolean -->
     * <pre>
     * boolean overwrite = false; &#47;&#47; Default value
     * client.downloadToFile&#40;file, overwrite&#41;;
     * System.out.println&#40;&quot;Completed download to file&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.downloadToFile#String-boolean -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param filePath A {@link String} representing the filePath where the downloaded data will be written.
     * @param overwrite Whether to overwrite the file, should the file exist.
     * @return The blob properties and metadata.
     * @throws UncheckedIOException If an I/O error occurs
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BlobProperties downloadToFile(String filePath, boolean overwrite) {
        Set<OpenOption> openOptions = null;
        if (overwrite) {
            openOptions = new HashSet<>();
            openOptions.add(StandardOpenOption.CREATE);
            openOptions.add(StandardOpenOption.TRUNCATE_EXISTING); // If the file already exists and it is opened
            // for WRITE access, then its length is truncated to 0.
            openOptions.add(StandardOpenOption.READ);
            openOptions.add(StandardOpenOption.WRITE);
        }
        return downloadToFileWithResponse(filePath, null, null, null, null, false, openOptions, null, Context.NONE)
            .getValue();
    }

    /**
     * Downloads the entire blob into a file specified by the path.
     *
     * <p>The file will be created and must not exist, if the file already exists a {@link FileAlreadyExistsException}
     * will be thrown.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.downloadToFileWithResponse#String-BlobRange-ParallelTransferOptions-DownloadRetryOptions-BlobRequestConditions-boolean-Duration-Context -->
     * <pre>
     * BlobRange range = new BlobRange&#40;1024, 2048L&#41;;
     * DownloadRetryOptions options = new DownloadRetryOptions&#40;&#41;.setMaxRetryRequests&#40;5&#41;;
     *
     * client.downloadToFileWithResponse&#40;file, range, new ParallelTransferOptions&#40;&#41;.setBlockSizeLong&#40;4L * Constants.MB&#41;,
     *     options, null, false, timeout, new Context&#40;key2, value2&#41;&#41;;
     * System.out.println&#40;&quot;Completed download to file&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.downloadToFileWithResponse#String-BlobRange-ParallelTransferOptions-DownloadRetryOptions-BlobRequestConditions-boolean-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param filePath A {@link String} representing the filePath where the downloaded data will be written.
     * @param range {@link BlobRange}
     * @param parallelTransferOptions {@link ParallelTransferOptions} to use to download to file. Number of parallel
     *        transfers parameter is ignored.
     * @param downloadRetryOptions {@link DownloadRetryOptions}
     * @param requestConditions {@link BlobRequestConditions}
     * @param rangeGetContentMd5 Whether the contentMD5 for the specified blob range should be returned.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the blob properties and metadata.
     * @throws UncheckedIOException If an I/O error occurs.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BlobProperties> downloadToFileWithResponse(String filePath, BlobRange range,
        ParallelTransferOptions parallelTransferOptions, DownloadRetryOptions downloadRetryOptions,
        BlobRequestConditions requestConditions, boolean rangeGetContentMd5, Duration timeout, Context context) {
        return downloadToFileWithResponse(filePath, range, parallelTransferOptions, downloadRetryOptions,
            requestConditions, rangeGetContentMd5, null, timeout, context);
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
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.downloadToFileWithResponse#String-BlobRange-ParallelTransferOptions-DownloadRetryOptions-BlobRequestConditions-boolean-Set-Duration-Context -->
     * <pre>
     * BlobRange blobRange = new BlobRange&#40;1024, 2048L&#41;;
     * DownloadRetryOptions downloadRetryOptions = new DownloadRetryOptions&#40;&#41;.setMaxRetryRequests&#40;5&#41;;
     * Set&lt;OpenOption&gt; openOptions = new HashSet&lt;&gt;&#40;Arrays.asList&#40;StandardOpenOption.CREATE_NEW,
     *     StandardOpenOption.WRITE, StandardOpenOption.READ&#41;&#41;; &#47;&#47; Default options
     *
     * client.downloadToFileWithResponse&#40;file, blobRange, new ParallelTransferOptions&#40;&#41;.setBlockSizeLong&#40;4L * Constants.MB&#41;,
     *     downloadRetryOptions, null, false, openOptions, timeout, new Context&#40;key2, value2&#41;&#41;;
     * System.out.println&#40;&quot;Completed download to file&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.downloadToFileWithResponse#String-BlobRange-ParallelTransferOptions-DownloadRetryOptions-BlobRequestConditions-boolean-Set-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param filePath A {@link String} representing the filePath where the downloaded data will be written.
     * @param range {@link BlobRange}
     * @param parallelTransferOptions {@link ParallelTransferOptions} to use to download to file. Number of parallel
     *        transfers parameter is ignored.
     * @param downloadRetryOptions {@link DownloadRetryOptions}
     * @param requestConditions {@link BlobRequestConditions}
     * @param rangeGetContentMd5 Whether the contentMD5 for the specified blob range should be returned.
     * @param openOptions {@link OpenOption OpenOptions} to use to configure how to open or create the file.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the blob properties and metadata.
     * @throws UncheckedIOException If an I/O error occurs.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BlobProperties> downloadToFileWithResponse(String filePath, BlobRange range,
        ParallelTransferOptions parallelTransferOptions, DownloadRetryOptions downloadRetryOptions,
        BlobRequestConditions requestConditions, boolean rangeGetContentMd5, Set<OpenOption> openOptions,
        Duration timeout, Context context) {
        final com.azure.storage.common.ParallelTransferOptions finalParallelTransferOptions =
            ModelHelper.wrapBlobOptions(ModelHelper.populateAndApplyDefaults(parallelTransferOptions));
        return downloadToFileWithResponse(new BlobDownloadToFileOptions(filePath).setRange(range)
            .setParallelTransferOptions(finalParallelTransferOptions)
            .setDownloadRetryOptions(downloadRetryOptions).setRequestConditions(requestConditions)
            .setRetrieveContentRangeMd5(rangeGetContentMd5).setOpenOptions(openOptions), timeout, context);
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
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.downloadToFileWithResponse#BlobDownloadToFileOptions-Duration-Context -->
     * <pre>
     * client.downloadToFileWithResponse&#40;new BlobDownloadToFileOptions&#40;file&#41;
     *     .setRange&#40;new BlobRange&#40;1024, 2018L&#41;&#41;
     *     .setDownloadRetryOptions&#40;new DownloadRetryOptions&#40;&#41;.setMaxRetryRequests&#40;5&#41;&#41;
     *     .setOpenOptions&#40;new HashSet&lt;&gt;&#40;Arrays.asList&#40;StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE,
     *         StandardOpenOption.READ&#41;&#41;&#41;, timeout, new Context&#40;key2, value2&#41;&#41;;
     * System.out.println&#40;&quot;Completed download to file&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.downloadToFileWithResponse#BlobDownloadToFileOptions-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param options {@link BlobDownloadToFileOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the blob properties and metadata.
     * @throws UncheckedIOException If an I/O error occurs.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BlobProperties> downloadToFileWithResponse(BlobDownloadToFileOptions options, Duration timeout,
        Context context) {
        Mono<Response<BlobProperties>> download = client.downloadToFileWithResponse(options, context);
        return blockWithOptionalTimeout(download, timeout);
    }

    /**
     * Deletes the specified blob or snapshot. To delete a blob with its snapshots use
     * {@link #deleteWithResponse(DeleteSnapshotsOptionType, BlobRequestConditions, Duration, Context)} and set
     * {@code DeleteSnapshotsOptionType} to INCLUDE.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.delete -->
     * <pre>
     * client.delete&#40;&#41;;
     * System.out.println&#40;&quot;Delete completed.&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.delete -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-blob">Azure Docs</a></p>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void delete() {
        deleteWithResponse(null, null, null, Context.NONE);
    }

    /**
     * Deletes the specified blob or snapshot. To delete a blob with its snapshots use
     * {@link #deleteWithResponse(DeleteSnapshotsOptionType, BlobRequestConditions, Duration, Context)} and set
     * {@code DeleteSnapshotsOptionType} to INCLUDE.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.deleteWithResponse#DeleteSnapshotsOptionType-BlobRequestConditions-Duration-Context -->
     * <pre>
     * System.out.printf&#40;&quot;Delete completed with status %d%n&quot;,
     *     client.deleteWithResponse&#40;DeleteSnapshotsOptionType.INCLUDE, null, timeout,
     *         new Context&#40;key1, value1&#41;&#41;.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.deleteWithResponse#DeleteSnapshotsOptionType-BlobRequestConditions-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-blob">Azure Docs</a></p>
     *
     * @param deleteBlobSnapshotOptions Specifies the behavior for deleting the snapshots on this blob. {@code Include}
     * will delete the base blob and all snapshots. {@code Only} will delete only the snapshots. If a snapshot is being
     * deleted, you must pass null.
     * @param requestConditions {@link BlobRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteWithResponse(DeleteSnapshotsOptionType deleteBlobSnapshotOptions,
        BlobRequestConditions requestConditions, Duration timeout, Context context) {
        Context finalContext = context == null ? Context.NONE : context;
        BlobRequestConditions finalRequestConditions = requestConditions == null ? new BlobRequestConditions() : requestConditions;
        Callable<Response<Void>> operation = () -> azureBlobStorage.getBlobs().deleteNoCustomHeadersWithResponse(
            containerName, blobName, snapshot, versionId, null, finalRequestConditions.getLeaseId(),
            deleteBlobSnapshotOptions, finalRequestConditions.getIfModifiedSince(),
            finalRequestConditions.getIfUnmodifiedSince(), finalRequestConditions.getIfMatch(),
            finalRequestConditions.getIfNoneMatch(), finalRequestConditions.getTagsConditions(), null, null,
            finalContext);

        return sendRequest(operation, timeout, BlobStorageException.class);
    }

    /**
     * Deletes the specified blob or snapshot if it exists. To delete a blob with its snapshots use
     * {@link #deleteIfExistsWithResponse(DeleteSnapshotsOptionType, BlobRequestConditions, Duration, Context)} and set
     * {@code DeleteSnapshotsOptionType} to INCLUDE.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.deleteIfExists -->
     * <pre>
     * boolean result = client.deleteIfExists&#40;&#41;;
     * System.out.println&#40;&quot;Delete completed: &quot; + result&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.deleteIfExists -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-blob">Azure Docs</a></p>
     * @return {@code true} if delete succeeds, or {@code false} if blob does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public boolean deleteIfExists() {
        return deleteIfExistsWithResponse(null, null, null, Context.NONE).getValue();
    }

    /**
     * Deletes the specified blob or snapshot if it exists. To delete a blob with its snapshots use
     * {@link #deleteIfExistsWithResponse(DeleteSnapshotsOptionType, BlobRequestConditions, Duration, Context)} and set
     * {@code DeleteSnapshotsOptionType} to INCLUDE.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.deleteIfExistsWithResponse#DeleteSnapshotsOptionType-BlobRequestConditions-Duration-Context -->
     * <pre>
     * Response&lt;Boolean&gt; response = client.deleteIfExistsWithResponse&#40;DeleteSnapshotsOptionType.INCLUDE, null, timeout,
     *     new Context&#40;key1, value1&#41;&#41;;
     * if &#40;response.getStatusCode&#40;&#41; == 404&#41; &#123;
     *     System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     * &#125; else &#123;
     *     System.out.printf&#40;&quot;Delete completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.deleteIfExistsWithResponse#DeleteSnapshotsOptionType-BlobRequestConditions-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-blob">Azure Docs</a></p>
     *
     * @param deleteBlobSnapshotOptions Specifies the behavior for deleting the snapshots on this blob. {@code Include}
     * will delete the base blob and all snapshots. {@code Only} will delete only the snapshots. If a snapshot is being
     * deleted, you must pass null.
     * @param requestConditions {@link BlobRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers. If {@link Response}'s status code is 202, the base
     * blob was successfully deleted. If status code is 404, the base blob does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Boolean> deleteIfExistsWithResponse(DeleteSnapshotsOptionType deleteBlobSnapshotOptions,
        BlobRequestConditions requestConditions, Duration timeout, Context context) {
        try {
            Response<Void> response = this.deleteWithResponse(deleteBlobSnapshotOptions, requestConditions, timeout,
                context);
            return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), true);
        } catch (RuntimeException e) {
            if (ModelHelper.checkBlobDoesNotExistStatusCode(e) && e instanceof HttpResponseException) {
                HttpResponse response = ((HttpResponseException) e).getResponse();
                return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                    false);
            } else {
                throw LOGGER.logExceptionAsError(e);
            }
        }
    }

    /**
     * Returns the blob's metadata and properties.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.getProperties -->
     * <pre>
     * BlobProperties properties = client.getProperties&#40;&#41;;
     * System.out.printf&#40;&quot;Type: %s, Size: %d%n&quot;, properties.getBlobType&#40;&#41;, properties.getBlobSize&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.getProperties -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob-properties">Azure Docs</a></p>
     *
     * @return The blob properties and metadata.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BlobProperties getProperties() {
        return getPropertiesWithResponse(null, null, Context.NONE).getValue();
    }

    /**
     * Returns the blob's metadata and properties.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.getPropertiesWithResponse#BlobRequestConditions-Duration-Context -->
     * <pre>
     * BlobRequestConditions requestConditions = new BlobRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     *
     * BlobProperties properties = client.getPropertiesWithResponse&#40;requestConditions, timeout,
     *     new Context&#40;key2, value2&#41;&#41;.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Type: %s, Size: %d%n&quot;, properties.getBlobType&#40;&#41;, properties.getBlobSize&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.getPropertiesWithResponse#BlobRequestConditions-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob-properties">Azure Docs</a></p>
     *
     * @param requestConditions {@link BlobRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The blob properties and metadata.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BlobProperties> getPropertiesWithResponse(BlobRequestConditions requestConditions, Duration timeout,
        Context context) {
        BlobRequestConditions finalRequestConditions = requestConditions == null ? new BlobRequestConditions() : requestConditions;
        Context finalContext = context == null ? Context.NONE : context;
        Callable<ResponseBase<BlobsGetPropertiesHeaders, Void>> operation = () ->
            this.azureBlobStorage.getBlobs().getPropertiesWithResponse(containerName, blobName, snapshot, versionId,
                null, finalRequestConditions.getLeaseId(), finalRequestConditions.getIfModifiedSince(),
                finalRequestConditions.getIfUnmodifiedSince(), finalRequestConditions.getIfMatch(),
                finalRequestConditions.getIfNoneMatch(), finalRequestConditions.getTagsConditions(), null,
                customerProvidedKey, finalContext);
        ResponseBase<BlobsGetPropertiesHeaders, Void> response = sendRequest(operation, timeout,
            BlobStorageException.class);
        return new SimpleResponse<>(response, BlobPropertiesConstructorProxy
            .create(new BlobPropertiesInternalGetProperties(response.getDeserializedHeaders())));
    }

    /**
     * Changes a blob's HTTP header properties. if only one HTTP header is updated, the others will all be erased. In
     * order to preserve existing values, they must be passed alongside the header being changed.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.setHttpHeaders#BlobHttpHeaders -->
     * <pre>
     * client.setHttpHeaders&#40;new BlobHttpHeaders&#40;&#41;
     *     .setContentLanguage&#40;&quot;en-US&quot;&#41;
     *     .setContentType&#40;&quot;binary&quot;&#41;&#41;;
     * System.out.println&#40;&quot;Set HTTP headers completed&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.setHttpHeaders#BlobHttpHeaders -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-properties">Azure Docs</a></p>
     *
     * @param headers {@link BlobHttpHeaders}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void setHttpHeaders(BlobHttpHeaders headers) {
        setHttpHeadersWithResponse(headers, null, null, Context.NONE);
    }

    /**
     * Changes a blob's HTTP header properties. if only one HTTP header is updated, the others will all be erased. In
     * order to preserve existing values, they must be passed alongside the header being changed.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.setHttpHeadersWithResponse#BlobHttpHeaders-BlobRequestConditions-Duration-Context -->
     * <pre>
     * BlobRequestConditions requestConditions = new BlobRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     *
     * System.out.printf&#40;&quot;Set HTTP headers completed with status %d%n&quot;,
     *     client.setHttpHeadersWithResponse&#40;new BlobHttpHeaders&#40;&#41;
     *         .setContentLanguage&#40;&quot;en-US&quot;&#41;
     *         .setContentType&#40;&quot;binary&quot;&#41;, requestConditions, timeout, new Context&#40;key1, value1&#41;&#41;
     *         .getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.setHttpHeadersWithResponse#BlobHttpHeaders-BlobRequestConditions-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-properties">Azure Docs</a></p>
     *
     * @param headers {@link BlobHttpHeaders}
     * @param requestConditions {@link BlobRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> setHttpHeadersWithResponse(BlobHttpHeaders headers, BlobRequestConditions requestConditions,
        Duration timeout, Context context) {
        BlobRequestConditions finalRequestConditions = requestConditions == null ? new BlobRequestConditions()
            : requestConditions;
        Context finalContext = context == null ? Context.NONE : context;

        Callable<Response<Void>> operation = () ->
            this.azureBlobStorage.getBlobs().setHttpHeadersNoCustomHeadersWithResponse(containerName, blobName, null,
                finalRequestConditions.getLeaseId(), finalRequestConditions.getIfModifiedSince(),
                finalRequestConditions.getIfUnmodifiedSince(), finalRequestConditions.getIfMatch(),
                finalRequestConditions.getIfNoneMatch(), finalRequestConditions.getTagsConditions(), null, headers,
                finalContext);

        return sendRequest(operation, timeout, BlobStorageException.class);
    }

    /**
     * Changes a blob's metadata. The specified metadata in this method will replace existing metadata. If old values
     * must be preserved, they must be downloaded and included in the call to this method.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.setMetadata#Map -->
     * <pre>
     * client.setMetadata&#40;Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;&#41;;
     * System.out.println&#40;&quot;Set metadata completed&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.setMetadata#Map -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-metadata">Azure Docs</a></p>
     *
     * @param metadata Metadata to associate with the blob. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void setMetadata(Map<String, String> metadata) {
        setMetadataWithResponse(metadata, null, null, Context.NONE);
    }

    /**
     * Changes a blob's metadata. The specified metadata in this method will replace existing metadata. If old values
     * must be preserved, they must be downloaded and included in the call to this method.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.setMetadataWithResponse#Map-BlobRequestConditions-Duration-Context -->
     * <pre>
     * BlobRequestConditions requestConditions = new BlobRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     *
     * System.out.printf&#40;&quot;Set metadata completed with status %d%n&quot;,
     *     client.setMetadataWithResponse&#40;Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;, requestConditions, timeout,
     *         new Context&#40;key1, value1&#41;&#41;.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.setMetadataWithResponse#Map-BlobRequestConditions-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-metadata">Azure Docs</a></p>
     *
     * @param metadata Metadata to associate with the blob. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param requestConditions {@link BlobRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> setMetadataWithResponse(Map<String, String> metadata, BlobRequestConditions requestConditions,
        Duration timeout, Context context) {
        BlobRequestConditions finalRequestConditions = requestConditions == null ? new BlobRequestConditions()
            : requestConditions;
        Context finalContext = context == null ? Context.NONE : context;

        Callable<Response<Void>> operation = () ->
            this.azureBlobStorage.getBlobs().setMetadataNoCustomHeadersWithResponse(containerName, blobName, null,
                metadata, finalRequestConditions.getLeaseId(), finalRequestConditions.getIfModifiedSince(),
                finalRequestConditions.getIfUnmodifiedSince(), finalRequestConditions.getIfMatch(),
                finalRequestConditions.getIfNoneMatch(), finalRequestConditions.getTagsConditions(), null,
                customerProvidedKey, encryptionScope, finalContext);

        return sendRequest(operation, timeout, BlobStorageException.class);
    }

    /**
     * Returns the blob's tags.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.getTags -->
     * <pre>
     * Map&lt;String, String&gt; tags = client.getTags&#40;&#41;;
     * System.out.printf&#40;&quot;Number of tags: %d%n&quot;, tags.size&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.getTags -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob-tags">Azure Docs</a></p>
     *
     * @return The blob's tags.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Map<String, String> getTags() {
        return this.getTagsWithResponse(new BlobGetTagsOptions(), null, Context.NONE).getValue();
    }

    /**
     * Returns the blob's tags.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.getTagsWithResponse#BlobGetTagsOptions-Duration-Context -->
     * <pre>
     * Map&lt;String, String&gt; tags = client.getTagsWithResponse&#40;new BlobGetTagsOptions&#40;&#41;, timeout,
     *     new Context&#40;key1, value1&#41;&#41;.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Number of tags: %d%n&quot;, tags.size&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.getTagsWithResponse#BlobGetTagsOptions-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob-tags">Azure Docs</a></p>
     *
     * @param options {@link BlobGetTagsOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The blob's tags.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Map<String, String>> getTagsWithResponse(BlobGetTagsOptions options, Duration timeout,
        Context context) {
        BlobGetTagsOptions finalTagOptions = (options == null) ? new BlobGetTagsOptions() : options;
        BlobRequestConditions requestConditions = (finalTagOptions.getRequestConditions() == null)
            ? new BlobRequestConditions() : finalTagOptions.getRequestConditions();
        Context finalContext = context == null ? Context.NONE : context;

        Callable<ResponseBase<BlobsGetTagsHeaders, BlobTags>> operation = () ->
            this.azureBlobStorage.getBlobs().getTagsWithResponse(containerName, blobName, null, null, snapshot,
            versionId, requestConditions.getTagsConditions(), requestConditions.getLeaseId(), finalContext);

        ResponseBase<BlobsGetTagsHeaders, BlobTags> response = sendRequest(operation, timeout, BlobStorageException.class);
        Map<String, String> tags = new HashMap<>();
        for (BlobTag tag : response.getValue().getBlobTagSet()) {
            tags.put(tag.getKey(), tag.getValue());
        }
        return new SimpleResponse<>(response, tags);
    }

    /**
     * Sets user defined tags. The specified tags in this method will replace existing tags. If old values
     * must be preserved, they must be downloaded and included in the call to this method.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.setTags#Map -->
     * <pre>
     * client.setTags&#40;Collections.singletonMap&#40;&quot;tag&quot;, &quot;value&quot;&#41;&#41;;
     * System.out.println&#40;&quot;Set tag completed&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.setTags#Map -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-tags">Azure Docs</a></p>
     *
     * @param tags Tags to associate with the blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void setTags(Map<String, String> tags) {
        this.setTagsWithResponse(new BlobSetTagsOptions(tags), null, Context.NONE);
    }

    /**
     * Sets user defined tags. The specified tags in this method will replace existing tags. If old values
     * must be preserved, they must be downloaded and included in the call to this method.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.setTagsWithResponse#BlobSetTagsOptions-Duration-Context -->
     * <pre>
     * System.out.printf&#40;&quot;Set metadata completed with status %d%n&quot;,
     *     client.setTagsWithResponse&#40;new BlobSetTagsOptions&#40;Collections.singletonMap&#40;&quot;tag&quot;, &quot;value&quot;&#41;&#41;, timeout,
     *         new Context&#40;key1, value1&#41;&#41;
     *         .getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.setTagsWithResponse#BlobSetTagsOptions-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-tags">Azure Docs</a></p>
     *
     * @param options {@link BlobSetTagsOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> setTagsWithResponse(BlobSetTagsOptions options, Duration timeout, Context context) {
        StorageImplUtils.assertNotNull("options", options);
        Context finalContext = context == null ? Context.NONE : context;
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
        Callable<Response<Void>> operation = () ->
            this.azureBlobStorage.getBlobs().setTagsNoCustomHeadersWithResponse(containerName, blobName, null, versionId,
                null, null, null, requestConditions.getTagsConditions(), requestConditions.getLeaseId(), t, finalContext);
        return sendRequest(operation, timeout, BlobStorageException.class);
    }

    /**
     * Creates a read-only snapshot of the blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.createSnapshot -->
     * <pre>
     * System.out.printf&#40;&quot;Identifier for the snapshot is %s%n&quot;, client.createSnapshot&#40;&#41;.getSnapshotId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.createSnapshot -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/snapshot-blob">Azure Docs</a></p>
     *
     * @return A response containing a {@link BlobClientBase} which is used to interact with the created snapshot, use
     * {@link BlobClientBase#getSnapshotId()} to get the identifier for the snapshot.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BlobClientBase createSnapshot() {
        return createSnapshotWithResponse(null, null, null, Context.NONE).getValue();
    }


    /**
     * Creates a read-only snapshot of the blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.createSnapshotWithResponse#Map-BlobRequestConditions-Duration-Context -->
     * <pre>
     * Map&lt;String, String&gt; snapshotMetadata = Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;;
     * BlobRequestConditions requestConditions = new BlobRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     *
     * System.out.printf&#40;&quot;Identifier for the snapshot is %s%n&quot;,
     *     client.createSnapshotWithResponse&#40;snapshotMetadata, requestConditions, timeout,
     *         new Context&#40;key1, value1&#41;&#41;.getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.createSnapshotWithResponse#Map-BlobRequestConditions-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/snapshot-blob">Azure Docs</a></p>
     *
     * @param metadata Metadata to associate with the resource. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param requestConditions {@link BlobRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing a {@link BlobClientBase} which is used to interact with the created snapshot, use
     * {@link BlobClientBase#getSnapshotId()} to get the identifier for the snapshot.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BlobClientBase> createSnapshotWithResponse(Map<String, String> metadata,
        BlobRequestConditions requestConditions, Duration timeout, Context context) {
        Context finalContext = context == null ? Context.NONE : context;
        BlobRequestConditions finalRequestConditions = requestConditions == null ? new BlobRequestConditions() : requestConditions;

        Callable<ResponseBase<BlobsCreateSnapshotHeaders, Void>> operation = () -> this.azureBlobStorage.getBlobs()
            .createSnapshotWithResponse(containerName, blobName, null, metadata,
                finalRequestConditions.getIfModifiedSince(), finalRequestConditions.getIfUnmodifiedSince(),
                finalRequestConditions.getIfMatch(), finalRequestConditions.getIfNoneMatch(),
                finalRequestConditions.getTagsConditions(), finalRequestConditions.getLeaseId(), null,
                customerProvidedKey, encryptionScope, finalContext);

        ResponseBase<BlobsCreateSnapshotHeaders, Void> response = sendRequest(operation, timeout, BlobStorageException.class);
        return new SimpleResponse<>(response, this.getSnapshotClient(response.getDeserializedHeaders().getXMsSnapshot()));
    }

    /**
     * Sets the tier on a blob. The operation is allowed on a page blob in a premium storage account or a block blob in
     * a blob storage or GPV2 account. A premium page blob's tier determines the allowed size, IOPS, and bandwidth of
     * the blob. A block blob's tier determines the Hot/Cool/Archive storage type. This does not update the blob's
     * etag.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.setAccessTier#AccessTier -->
     * <pre>
     * client.setAccessTier&#40;AccessTier.HOT&#41;;
     * System.out.println&#40;&quot;Set tier completed.&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.setAccessTier#AccessTier -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-tier">Azure Docs</a></p>
     *
     * @param tier The new tier for the blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void setAccessTier(AccessTier tier) {
        setAccessTierWithResponse(tier, null, null, null, Context.NONE);
    }

    /**
     * Sets the tier on a blob. The operation is allowed on a page blob in a premium storage account or a block blob in
     * a blob storage or GPV2 account. A premium page blob's tier determines the allowed size, IOPS, and bandwidth of
     * the blob. A block blob's tier determines the Hot/Cool/Archive storage type. This does not update the blob's
     * etag.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.setAccessTierWithResponse#AccessTier-RehydratePriority-String-Duration-Context -->
     * <pre>
     * System.out.printf&#40;&quot;Set tier completed with status code %d%n&quot;,
     *     client.setAccessTierWithResponse&#40;AccessTier.HOT, RehydratePriority.STANDARD, leaseId, timeout,
     *         new Context&#40;key2, value2&#41;&#41;.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.setAccessTierWithResponse#AccessTier-RehydratePriority-String-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-tier">Azure Docs</a></p>
     *
     * @param tier The new tier for the blob.
     * @param priority Optional priority to set for re-hydrating blobs.
     * @param leaseId The lease ID the active lease on the blob must match.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> setAccessTierWithResponse(AccessTier tier, RehydratePriority priority, String leaseId,
        Duration timeout, Context context) {
        return setAccessTierWithResponse(new BlobSetAccessTierOptions(tier).setPriority(priority).setLeaseId(leaseId),
            timeout, context);
    }

    /**
     * Sets the tier on a blob. The operation is allowed on a page blob in a premium storage account or a block blob in
     * a blob storage or GPV2 account. A premium page blob's tier determines the allowed size, IOPS, and bandwidth of
     * the blob. A block blob's tier determines the Hot/Cool/Archive storage type. This does not update the blob's
     * etag.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.setAccessTierWithResponse#BlobSetAccessTierOptions-Duration-Context -->
     * <pre>
     * System.out.printf&#40;&quot;Set tier completed with status code %d%n&quot;,
     *     client.setAccessTierWithResponse&#40;new BlobSetAccessTierOptions&#40;AccessTier.HOT&#41;
     *         .setPriority&#40;RehydratePriority.STANDARD&#41;
     *         .setLeaseId&#40;leaseId&#41;
     *         .setTagsConditions&#40;tags&#41;,
     *         timeout, new Context&#40;key2, value2&#41;&#41;.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.setAccessTierWithResponse#BlobSetAccessTierOptions-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-tier">Azure Docs</a></p>
     *
     * @param options {@link BlobSetAccessTierOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> setAccessTierWithResponse(BlobSetAccessTierOptions options,
        Duration timeout, Context context) {
        StorageImplUtils.assertNotNull("options", options);
        Context finalContext = context == null ? Context.NONE : context;

        Callable<Response<Void>> operation = () -> this.azureBlobStorage.getBlobs().setTierNoCustomHeadersWithResponse(
            containerName, blobName, options.getTier(), snapshot, versionId, null, options.getPriority(), null,
            options.getLeaseId(), options.getTagsConditions(), finalContext);
        return sendRequest(operation, timeout, BlobStorageException.class);
    }

    /**
     * Undelete restores the content and metadata of a soft-deleted blob and/or any associated soft-deleted snapshots.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.undelete -->
     * <pre>
     * client.undelete&#40;&#41;;
     * System.out.println&#40;&quot;Undelete completed&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.undelete -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/undelete-blob">Azure Docs</a></p>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void undelete() {
        undeleteWithResponse(null, Context.NONE);
    }

    /**
     * Undelete restores the content and metadata of a soft-deleted blob and/or any associated soft-deleted snapshots.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.undeleteWithResponse#Duration-Context -->
     * <pre>
     * System.out.printf&#40;&quot;Undelete completed with status %d%n&quot;, client.undeleteWithResponse&#40;timeout,
     *     new Context&#40;key1, value1&#41;&#41;.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.undeleteWithResponse#Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/undelete-blob">Azure Docs</a></p>
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> undeleteWithResponse(Duration timeout, Context context) {
        Context finalContext = context == null ? Context.NONE : context;
        Callable<Response<Void>> operation = () -> this.azureBlobStorage.getBlobs().undeleteNoCustomHeadersWithResponse(
            containerName, blobName, null, null, finalContext);
        return sendRequest(operation, timeout, BlobStorageException.class);
    }

    /**
     * Returns the sku name and account kind for the account.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.getAccountInfo -->
     * <pre>
     * StorageAccountInfo accountInfo = client.getAccountInfo&#40;&#41;;
     * System.out.printf&#40;&quot;Account Kind: %s, SKU: %s%n&quot;, accountInfo.getAccountKind&#40;&#41;, accountInfo.getSkuName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.getAccountInfo -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-account-information">Azure Docs</a></p>
     *
     * @return The sku name and account kind.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public StorageAccountInfo getAccountInfo() {
        return getAccountInfoWithResponse(null, Context.NONE).getValue();
    }

    /**
     * Returns the sku name and account kind for the account.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.getAccountInfoWithResponse#Duration-Context -->
     * <pre>
     * StorageAccountInfo accountInfo = client.getAccountInfoWithResponse&#40;timeout, new Context&#40;key1, value1&#41;&#41;.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Account Kind: %s, SKU: %s%n&quot;, accountInfo.getAccountKind&#40;&#41;, accountInfo.getSkuName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.getAccountInfoWithResponse#Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-account-information">Azure Docs</a></p>
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The sku name and account kind.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<StorageAccountInfo> getAccountInfoWithResponse(Duration timeout, Context context) {
        Context finalContext = context == null ? Context.NONE : context;
        Callable<ResponseBase<BlobsGetAccountInfoHeaders, Void>> operation = () -> this.azureBlobStorage.getBlobs()
            .getAccountInfoWithResponse(containerName, blobName, finalContext);

        ResponseBase<BlobsGetAccountInfoHeaders, Void> response = sendRequest(operation, timeout,
            BlobStorageException.class);
        BlobsGetAccountInfoHeaders hd = response.getDeserializedHeaders();
        return new SimpleResponse<>(response, new StorageAccountInfo(hd.getXMsSkuName(), hd.getXMsAccountKind()));
    }

    /**
     * Generates a user delegation SAS for the blob using the specified {@link BlobServiceSasSignatureValues}.
     * <p>See {@link BlobServiceSasSignatureValues} for more information on how to construct a user delegation SAS.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.generateUserDelegationSas#BlobServiceSasSignatureValues-UserDelegationKey -->
     * <pre>
     * OffsetDateTime myExpiryTime = OffsetDateTime.now&#40;&#41;.plusDays&#40;1&#41;;
     * BlobSasPermission myPermission = new BlobSasPermission&#40;&#41;.setReadPermission&#40;true&#41;;
     *
     * BlobServiceSasSignatureValues myValues = new BlobServiceSasSignatureValues&#40;expiryTime, permission&#41;
     *     .setStartTime&#40;OffsetDateTime.now&#40;&#41;&#41;;
     *
     * client.generateUserDelegationSas&#40;values, userDelegationKey&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.generateUserDelegationSas#BlobServiceSasSignatureValues-UserDelegationKey -->
     *
     * @param blobServiceSasSignatureValues {@link BlobServiceSasSignatureValues}
     * @param userDelegationKey A {@link UserDelegationKey} object used to sign the SAS values.
     * See {@link BlobServiceClient#getUserDelegationKey(OffsetDateTime, OffsetDateTime)} for more information on
     * how to get a user delegation key.
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
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.generateUserDelegationSas#BlobServiceSasSignatureValues-UserDelegationKey-String-Context -->
     * <pre>
     * OffsetDateTime myExpiryTime = OffsetDateTime.now&#40;&#41;.plusDays&#40;1&#41;;
     * BlobSasPermission myPermission = new BlobSasPermission&#40;&#41;.setReadPermission&#40;true&#41;;
     *
     * BlobServiceSasSignatureValues myValues = new BlobServiceSasSignatureValues&#40;expiryTime, permission&#41;
     *     .setStartTime&#40;OffsetDateTime.now&#40;&#41;&#41;;
     *
     * client.generateUserDelegationSas&#40;values, userDelegationKey, accountName, new Context&#40;key1, value1&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.generateUserDelegationSas#BlobServiceSasSignatureValues-UserDelegationKey-String-Context -->
     *
     * @param blobServiceSasSignatureValues {@link BlobServiceSasSignatureValues}
     * @param userDelegationKey A {@link UserDelegationKey} object used to sign the SAS values.
     * See {@link BlobServiceClient#getUserDelegationKey(OffsetDateTime, OffsetDateTime)} for more information on
     * how to get a user delegation key.
     * @param accountName The account name.
     * @param context Additional context that is passed through the code when generating a SAS.
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateUserDelegationSas(BlobServiceSasSignatureValues blobServiceSasSignatureValues,
        UserDelegationKey userDelegationKey, String accountName, Context context) {
        return new BlobSasImplUtil(blobServiceSasSignatureValues, getContainerName(), getBlobName(), getSnapshotId(),
            getVersionId(), getEncryptionScope()).generateUserDelegationSas(userDelegationKey, accountName, context);
    }

    /**
     * Generates a service SAS for the blob using the specified {@link BlobServiceSasSignatureValues}
     * <p>Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link BlobServiceSasSignatureValues} for more information on how to construct a service SAS.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.generateSas#BlobServiceSasSignatureValues -->
     * <pre>
     * OffsetDateTime expiryTime = OffsetDateTime.now&#40;&#41;.plusDays&#40;1&#41;;
     * BlobSasPermission permission = new BlobSasPermission&#40;&#41;.setReadPermission&#40;true&#41;;
     *
     * BlobServiceSasSignatureValues values = new BlobServiceSasSignatureValues&#40;expiryTime, permission&#41;
     *     .setStartTime&#40;OffsetDateTime.now&#40;&#41;&#41;;
     *
     * client.generateSas&#40;values&#41;; &#47;&#47; Client must be authenticated via StorageSharedKeyCredential
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.generateSas#BlobServiceSasSignatureValues -->
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
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.generateSas#BlobServiceSasSignatureValues-Context -->
     * <pre>
     * OffsetDateTime expiryTime = OffsetDateTime.now&#40;&#41;.plusDays&#40;1&#41;;
     * BlobSasPermission permission = new BlobSasPermission&#40;&#41;.setReadPermission&#40;true&#41;;
     *
     * BlobServiceSasSignatureValues values = new BlobServiceSasSignatureValues&#40;expiryTime, permission&#41;
     *     .setStartTime&#40;OffsetDateTime.now&#40;&#41;&#41;;
     *
     * &#47;&#47; Client must be authenticated via StorageSharedKeyCredential
     * client.generateSas&#40;values, new Context&#40;key1, value1&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.generateSas#BlobServiceSasSignatureValues-Context -->
     *
     * @param blobServiceSasSignatureValues {@link BlobServiceSasSignatureValues}
     * @param context Additional context that is passed through the code when generating a SAS.
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateSas(BlobServiceSasSignatureValues blobServiceSasSignatureValues, Context context) {
        return new BlobSasImplUtil(blobServiceSasSignatureValues, getContainerName(), getBlobName(), getSnapshotId(),
            getVersionId(), getEncryptionScope()).generateSas(SasImplUtils.extractSharedKeyCredential(getHttpPipeline()), context);
    }

    /**
     * Opens a blob input stream to query the blob.
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/query-blob-contents">Azure Docs</a></p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.openQueryInputStream#String -->
     * <pre>
     * String expression = &quot;SELECT * from BlobStorage&quot;;
     * InputStream inputStream = client.openQueryInputStream&#40;expression&#41;;
     * &#47;&#47; Now you can read from the input stream like you would normally.
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.openQueryInputStream#String -->
     *
     * @param expression The query expression.
     * @return An <code>InputStream</code> object that represents the stream to use for reading the query response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public InputStream openQueryInputStream(String expression) {
        return openQueryInputStreamWithResponse(new BlobQueryOptions(expression)).getValue();
    }

    /**
     * Opens a blob input stream to query the blob.
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/query-blob-contents">Azure Docs</a></p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.openQueryInputStream#BlobQueryOptions -->
     * <pre>
     * String expression = &quot;SELECT * from BlobStorage&quot;;
     * BlobQuerySerialization input = new BlobQueryDelimitedSerialization&#40;&#41;
     *     .setColumnSeparator&#40;','&#41;
     *     .setEscapeChar&#40;'&#92;n'&#41;
     *     .setRecordSeparator&#40;'&#92;n'&#41;
     *     .setHeadersPresent&#40;true&#41;
     *     .setFieldQuote&#40;'&quot;'&#41;;
     * BlobQuerySerialization output = new BlobQueryJsonSerialization&#40;&#41;
     *     .setRecordSeparator&#40;'&#92;n'&#41;;
     * BlobRequestConditions requestConditions = new BlobRequestConditions&#40;&#41;
     *     .setLeaseId&#40;&quot;leaseId&quot;&#41;;
     * Consumer&lt;BlobQueryError&gt; errorConsumer = System.out::println;
     * Consumer&lt;BlobQueryProgress&gt; progressConsumer = progress -&gt; System.out.println&#40;&quot;total blob bytes read: &quot;
     *     + progress.getBytesScanned&#40;&#41;&#41;;
     * BlobQueryOptions queryOptions = new BlobQueryOptions&#40;expression&#41;
     *     .setInputSerialization&#40;input&#41;
     *     .setOutputSerialization&#40;output&#41;
     *     .setRequestConditions&#40;requestConditions&#41;
     *     .setErrorConsumer&#40;errorConsumer&#41;
     *     .setProgressConsumer&#40;progressConsumer&#41;;
     *
     * InputStream inputStream = client.openQueryInputStreamWithResponse&#40;queryOptions&#41;.getValue&#40;&#41;;
     * &#47;&#47; Now you can read from the input stream like you would normally.
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.openQueryInputStream#BlobQueryOptions -->
     *
     * @param queryOptions {@link BlobQueryOptions The query options}.
     * @return A response containing status code and HTTP headers including an <code>InputStream</code> object
     * that represents the stream to use for reading the query response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<InputStream> openQueryInputStreamWithResponse(BlobQueryOptions queryOptions) {
        StorageImplUtils.assertNotNull("options", queryOptions);
        BlobRequestConditions requestConditions = queryOptions.getRequestConditions() == null
            ? new BlobRequestConditions() : queryOptions.getRequestConditions();
        QuerySerialization in = BlobQueryReader.transformInputSerialization(queryOptions.getInputSerialization(),
            LOGGER);
        QuerySerialization out = BlobQueryReader.transformOutputSerialization(queryOptions.getOutputSerialization(),
            LOGGER);

        QueryRequest qr = new QueryRequest()
            .setExpression(queryOptions.getExpression())
            .setInputSerialization(in)
            .setOutputSerialization(out);

        ResponseBase<BlobsQueryHeaders, InputStream> response = this.azureBlobStorage.getBlobs().queryWithResponse(
            containerName, blobName, getSnapshotId(), null, requestConditions.getLeaseId(),
            requestConditions.getIfModifiedSince(), requestConditions.getIfUnmodifiedSince(),
            requestConditions.getIfMatch(), requestConditions.getIfNoneMatch(), requestConditions.getTagsConditions(),
            null, qr, getCustomerProvidedKey(), Context.NONE);
        InputStream avroInputStream = response.getValue();
        BlobQueryReader reader = new BlobQueryReader(null, queryOptions.getProgressConsumer(),
            queryOptions.getErrorConsumer());
        try {
            InputStream resultStream = reader.readInputStream(avroInputStream);
            return new SimpleResponse<>(response, resultStream);
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    /**
     * Queries an entire blob into an output stream.
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/query-blob-contents">Azure Docs</a></p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.query#OutputStream-String -->
     * <pre>
     * ByteArrayOutputStream queryData = new ByteArrayOutputStream&#40;&#41;;
     * String expression = &quot;SELECT * from BlobStorage&quot;;
     * client.query&#40;queryData, expression&#41;;
     * System.out.println&#40;&quot;Query completed.&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.query#OutputStream-String -->
     *
     * @param stream A non-null {@link OutputStream} instance where the downloaded data will be written.
     * @param expression The query expression.
     * @throws UncheckedIOException If an I/O error occurs.
     * @throws NullPointerException if {@code stream} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void query(OutputStream stream, String expression) {
        queryWithResponse(new BlobQueryOptions(expression, stream), null, Context.NONE);
    }

    /**
     * Queries an entire blob into an output stream.
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/query-blob-contents">Azure Docs</a></p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.queryWithResponse#BlobQueryOptions-Duration-Context -->
     * <pre>
     * ByteArrayOutputStream queryData = new ByteArrayOutputStream&#40;&#41;;
     * String expression = &quot;SELECT * from BlobStorage&quot;;
     * BlobQueryJsonSerialization input = new BlobQueryJsonSerialization&#40;&#41;
     *     .setRecordSeparator&#40;'&#92;n'&#41;;
     * BlobQueryDelimitedSerialization output = new BlobQueryDelimitedSerialization&#40;&#41;
     *     .setEscapeChar&#40;'&#92;0'&#41;
     *     .setColumnSeparator&#40;','&#41;
     *     .setRecordSeparator&#40;'&#92;n'&#41;
     *     .setFieldQuote&#40;'&#92;''&#41;
     *     .setHeadersPresent&#40;true&#41;;
     * BlobRequestConditions requestConditions = new BlobRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     * Consumer&lt;BlobQueryError&gt; errorConsumer = System.out::println;
     * Consumer&lt;BlobQueryProgress&gt; progressConsumer = progress -&gt; System.out.println&#40;&quot;total blob bytes read: &quot;
     *     + progress.getBytesScanned&#40;&#41;&#41;;
     * BlobQueryOptions queryOptions = new BlobQueryOptions&#40;expression, queryData&#41;
     *     .setInputSerialization&#40;input&#41;
     *     .setOutputSerialization&#40;output&#41;
     *     .setRequestConditions&#40;requestConditions&#41;
     *     .setErrorConsumer&#40;errorConsumer&#41;
     *     .setProgressConsumer&#40;progressConsumer&#41;;
     * System.out.printf&#40;&quot;Query completed with status %d%n&quot;,
     *     client.queryWithResponse&#40;queryOptions, timeout, new Context&#40;key1, value1&#41;&#41;
     *         .getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.queryWithResponse#BlobQueryOptions-Duration-Context -->
     *
     * @param queryOptions {@link BlobQueryOptions The query options}.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers.
     * @throws UncheckedIOException If an I/O error occurs.
     * @throws NullPointerException if {@code stream} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BlobQueryResponse queryWithResponse(BlobQueryOptions queryOptions, Duration timeout, Context context) {
        StorageImplUtils.assertNotNull("options", queryOptions);
        StorageImplUtils.assertNotNull("outputStream", queryOptions.getOutputStream());
        Context finalContext = context == null ? Context.NONE : context;
        BlobRequestConditions requestConditions = queryOptions.getRequestConditions() == null
            ? new BlobRequestConditions() : queryOptions.getRequestConditions();
        QuerySerialization in = BlobQueryReader.transformInputSerialization(queryOptions.getInputSerialization(),
            LOGGER);
        QuerySerialization out = BlobQueryReader.transformOutputSerialization(queryOptions.getOutputSerialization(),
            LOGGER);

        QueryRequest qr = new QueryRequest()
            .setExpression(queryOptions.getExpression())
            .setInputSerialization(in)
            .setOutputSerialization(out);

        Callable<ResponseBase<BlobsQueryHeaders, InputStream>> operation = () -> {
            ResponseBase<BlobsQueryHeaders, InputStream> response = this.azureBlobStorage.getBlobs()
                .queryWithResponse(containerName, blobName, getSnapshotId(), null, requestConditions.getLeaseId(),
                    requestConditions.getIfModifiedSince(), requestConditions.getIfUnmodifiedSince(),
                    requestConditions.getIfMatch(), requestConditions.getIfNoneMatch(), requestConditions.getTagsConditions(),
                    null, qr, getCustomerProvidedKey(), finalContext);

            InputStream avroInputStream = response.getValue();
            BlobQueryReader reader = new BlobQueryReader(null, queryOptions.getProgressConsumer(),
                queryOptions.getErrorConsumer());
            InputStream resultStream = reader.readInputStream(avroInputStream);
            OutputStream outputStream = queryOptions.getOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = resultStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            return response;
        };

        ResponseBase<BlobsQueryHeaders, InputStream> response = sendRequest(operation, timeout,
            BlobStorageException.class);

        BlobQueryAsyncResponse asyncResponse = new BlobQueryAsyncResponse(response.getRequest(),
            response.getStatusCode(), response.getHeaders(), null,
            ModelHelper.transformQueryHeaders(response.getDeserializedHeaders(), response.getHeaders()));

        return new BlobQueryResponse(asyncResponse);
    }

    /**
     * Sets the immutability policy on a blob, blob snapshot or blob version.
     * <p> NOTE: Blob Versioning must be enabled on your storage account and the blob must be in a container with
     * immutable storage with versioning enabled to call this API.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.setImmutabilityPolicy#BlobImmutabilityPolicy -->
     * <pre>
     * BlobImmutabilityPolicy policy = new BlobImmutabilityPolicy&#40;&#41;
     *     .setPolicyMode&#40;BlobImmutabilityPolicyMode.LOCKED&#41;
     *     .setExpiryTime&#40;OffsetDateTime.now&#40;&#41;.plusDays&#40;1&#41;&#41;;
     * BlobImmutabilityPolicy setPolicy = client.setImmutabilityPolicy&#40;policy&#41;;
     * System.out.println&#40;&quot;Successfully completed setting the immutability policy&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.setImmutabilityPolicy#BlobImmutabilityPolicy -->
     *
     * @param immutabilityPolicy {@link BlobImmutabilityPolicy The immutability policy}.
     * @return The immutability policy.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BlobImmutabilityPolicy setImmutabilityPolicy(BlobImmutabilityPolicy immutabilityPolicy) {
        return setImmutabilityPolicyWithResponse(immutabilityPolicy, null, null, Context.NONE).getValue();
    }

    /**
     * Sets the immutability policy on a blob, blob snapshot or blob version.
     * <p> NOTE: Blob Versioning must be enabled on your storage account and the blob must be in a container with
     * immutable storage with versioning enabled to call this API.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.setImmutabilityPolicyWithResponse#BlobImmutabilityPolicy-BlobRequestConditions-Duration-Context -->
     * <pre>
     * BlobImmutabilityPolicy immutabilityPolicy = new BlobImmutabilityPolicy&#40;&#41;
     *     .setPolicyMode&#40;BlobImmutabilityPolicyMode.LOCKED&#41;
     *     .setExpiryTime&#40;OffsetDateTime.now&#40;&#41;.plusDays&#40;1&#41;&#41;;
     * BlobRequestConditions requestConditions = new BlobRequestConditions&#40;&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;1&#41;&#41;;
     * Response&lt;BlobImmutabilityPolicy&gt; response = client.setImmutabilityPolicyWithResponse&#40;immutabilityPolicy,
     *     requestConditions, timeout, new Context&#40;key1, value1&#41;&#41;;
     * System.out.println&#40;&quot;Successfully completed setting the immutability policy&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.setImmutabilityPolicyWithResponse#BlobImmutabilityPolicy-BlobRequestConditions-Duration-Context -->
     *
     * @param immutabilityPolicy {@link BlobImmutabilityPolicy The immutability policy}.
     * @param requestConditions {@link BlobRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the immutability policy.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BlobImmutabilityPolicy> setImmutabilityPolicyWithResponse(BlobImmutabilityPolicy immutabilityPolicy,
        BlobRequestConditions requestConditions, Duration timeout, Context context) {
        Context finalContext = context == null ? Context.NONE : context;
        BlobImmutabilityPolicy finalImmutabilityPolicy = immutabilityPolicy == null ? new BlobImmutabilityPolicy()
            : immutabilityPolicy;
        if (BlobImmutabilityPolicyMode.MUTABLE.equals(finalImmutabilityPolicy.getPolicyMode())) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
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

        Callable<ResponseBase<BlobsSetImmutabilityPolicyHeaders, Void>> operation = () ->
            this.azureBlobStorage.getBlobs().setImmutabilityPolicyWithResponse(containerName, blobName, null, null,
                finalRequestConditions.getIfUnmodifiedSince(), finalImmutabilityPolicy.getExpiryTime(),
                finalImmutabilityPolicy.getPolicyMode(), finalContext);
        ResponseBase<BlobsSetImmutabilityPolicyHeaders, Void> response = sendRequest(operation, timeout,
            BlobStorageException.class);

        BlobsSetImmutabilityPolicyHeaders headers = response.getDeserializedHeaders();
        BlobImmutabilityPolicy responsePolicy = new BlobImmutabilityPolicy()
            .setPolicyMode(headers.getXMsImmutabilityPolicyMode())
            .setExpiryTime(headers.getXMsImmutabilityPolicyUntilDate());
        return new SimpleResponse<>(response, responsePolicy);
    }

    /**
     * Delete the immutability policy on a blob, blob snapshot or blob version.
     * <p> NOTE: Blob Versioning must be enabled on your storage account and the blob must be in a container with
     * immutable storage with versioning enabled to call this API.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.deleteImmutabilityPolicy -->
     * <pre>
     * client.deleteImmutabilityPolicy&#40;&#41;;
     * System.out.println&#40;&quot;Completed immutability policy deletion.&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.deleteImmutabilityPolicy -->
     *
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteImmutabilityPolicy() {
        deleteImmutabilityPolicyWithResponse(null, Context.NONE).getValue();
    }

    /**
     * Delete the immutability policy on a blob, blob snapshot or blob version.
     * <p> NOTE: Blob Versioning must be enabled on your storage account and the blob must be in a container with
     * immutable storage with versioning enabled to call this API.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.deleteImmutabilityPolicyWithResponse#Duration-Context -->
     * <pre>
     * System.out.println&#40;&quot;Delete immutability policy completed with status: &quot;
     *     + client.deleteImmutabilityPolicyWithResponse&#40;timeout, new Context&#40;key1, value1&#41;&#41;.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.deleteImmutabilityPolicyWithResponse#Duration-Context -->
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the immutability policy.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteImmutabilityPolicyWithResponse(Duration timeout, Context context) {
        Context finalContext = context == null ? Context.NONE : context;
        Callable<Response<Void>> operation = () -> this.azureBlobStorage.getBlobs()
            .deleteImmutabilityPolicyNoCustomHeadersWithResponse(containerName, blobName, null, null, finalContext);
        return sendRequest(operation, timeout, BlobStorageException.class);
    }

    /**
     * Sets a legal hold on the blob.
     * <p> NOTE: Blob Versioning must be enabled on your storage account and the blob must be in a container with
     * immutable storage with versioning enabled to call this API.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.setLegalHold#boolean -->
     * <pre>
     * System.out.println&#40;&quot;Legal hold status: &quot; + client.setLegalHold&#40;true&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.setLegalHold#boolean -->
     *
     * @param legalHold Whether you want a legal hold on the blob.
     * @return The legal hold result.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BlobLegalHoldResult setLegalHold(boolean legalHold) {
        return setLegalHoldWithResponse(legalHold, null, Context.NONE).getValue();
    }

    /**
     * Sets a legal hold on the blob.
     * <p> NOTE: Blob Versioning must be enabled on your storage account and the blob must be in a container with
     * immutable storage with versioning enabled to call this API.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.setLegalHoldWithResponse#boolean-Duration-Context -->
     * <pre>
     * System.out.println&#40;&quot;Legal hold status: &quot; + client.setLegalHoldWithResponse&#40;true, timeout,
     *     new Context&#40;key1, value1&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.setLegalHoldWithResponse#boolean-Duration-Context -->
     *
     * @param legalHold Whether you want a legal hold on the blob.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the legal hold result.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BlobLegalHoldResult> setLegalHoldWithResponse(boolean legalHold, Duration timeout, Context context) {
        Context finalContext = context == null ? Context.NONE : context;
        Callable<ResponseBase<BlobsSetLegalHoldHeaders, Void>> operation = () ->
            this.azureBlobStorage.getBlobs().setLegalHoldWithResponse(containerName, blobName, legalHold, null, null,
                finalContext);
        ResponseBase<BlobsSetLegalHoldHeaders, Void> response = sendRequest(operation, timeout,
            BlobStorageException.class);
        return new SimpleResponse<>(response,
            new InternalBlobLegalHoldResult(response.getDeserializedHeaders().isXMsLegalHold()));

    }
}
