// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.implementation.models.BlockBlobsCommitBlockListHeaders;
import com.azure.storage.blob.implementation.models.BlockBlobsPutBlobFromUrlHeaders;
import com.azure.storage.blob.implementation.models.BlockBlobsUploadHeaders;
import com.azure.storage.blob.implementation.models.EncryptionScope;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobImmutabilityPolicy;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlockBlobItem;
import com.azure.storage.blob.models.BlockList;
import com.azure.storage.blob.models.BlockListType;
import com.azure.storage.blob.models.BlockLookupList;
import com.azure.storage.blob.models.CpkInfo;
import com.azure.storage.blob.models.CustomerProvidedKey;
import com.azure.storage.blob.options.BlobUploadFromUrlOptions;
import com.azure.storage.blob.options.BlockBlobCommitBlockListOptions;
import com.azure.storage.blob.options.BlockBlobListBlocksOptions;
import com.azure.storage.blob.options.BlockBlobSimpleUploadOptions;
import com.azure.storage.blob.options.BlockBlobStageBlockFromUrlOptions;
import com.azure.storage.common.Utility;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.StorageImplUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;
import static com.azure.storage.common.Utility.STORAGE_TRACING_NAMESPACE_VALUE;

/**
 * Client to a block blob. It may only be instantiated through a {@link SpecializedBlobClientBuilder} or via the method
 * {@link BlobAsyncClient#getBlockBlobAsyncClient()}. This class does not hold any state about a particular blob, but is
 * instead a convenient way of sending appropriate requests to the resource on the service.
 *
 * <p>
 * Please refer to the
 * <a href=https://docs.microsoft.com/rest/api/storageservices/understanding-block-blobs--append-blobs--and-page-blobs>Azure Docs</a> for more information.
 *
 * <p>
 * Note this client is an async client that returns reactive responses from Spring Reactor Core project
 * (https://projectreactor.io/). Calling the methods in this client will <strong>NOT</strong> start the actual network
 * operation, until {@code .subscribe()} is called on the reactive response. You can simply convert one of these
 * responses to a {@link java.util.concurrent.CompletableFuture} object through {@link Mono#toFuture()}.
 */
@ServiceClient(builder = SpecializedBlobClientBuilder.class, isAsync = true)
public final class BlockBlobAsyncClient extends BlobAsyncClientBase {
    private final ClientLogger logger = new ClientLogger(BlockBlobAsyncClient.class);

    /**
     * Indicates the maximum number of bytes that can be sent in a call to upload.
     * @deprecated Use {@link #MAX_STAGE_BLOCK_BYTES_LONG}
     */
    @Deprecated
    public static final int MAX_UPLOAD_BLOB_BYTES = 256 * Constants.MB;

    /**
     * Indicates the maximum number of bytes that can be sent in a call to upload.
     */
    public static final long MAX_UPLOAD_BLOB_BYTES_LONG = 5000L * Constants.MB;

    /**
     * Indicates the maximum number of bytes that can be sent in a call to stageBlock.
     * @deprecated Use {@link #MAX_STAGE_BLOCK_BYTES_LONG}
     */
    @Deprecated
    public static final int MAX_STAGE_BLOCK_BYTES = 100 * Constants.MB;

    /**
     * Indicates the maximum number of bytes that can be sent in a call to stageBlock.
     */
    public static final long MAX_STAGE_BLOCK_BYTES_LONG = 4000L * Constants.MB;

    /**
     * Indicates the maximum number of blocks allowed in a block blob.
     */
    public static final int MAX_BLOCKS = 50000;

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
     * @param encryptionScope Encryption scope used during encryption of the blob's data on the server, pass
     * {@code null} to allow the service to use its own encryption.
     * @param versionId The version identifier for the blob, pass {@code null} to interact with the latest blob version.
     * @param serializerAdapter serializer adapter
     */
    BlockBlobAsyncClient(HttpPipeline pipeline, String url, BlobServiceVersion serviceVersion,
        String accountName, String containerName, String blobName, String snapshot, CpkInfo customerProvidedKey,
        EncryptionScope encryptionScope, String versionId, SerializerAdapter serializerAdapter) {
        super(pipeline, url, serviceVersion, accountName, containerName, blobName, snapshot, customerProvidedKey,
            encryptionScope, versionId, serializerAdapter);
    }

    /**
     * Creates a new {@link BlockBlobAsyncClient} with the specified {@code encryptionScope}.
     *
     * @param encryptionScope the encryption scope for the blob, pass {@code null} to use no encryption scope.
     * @return a {@link BlockBlobAsyncClient} with the specified {@code encryptionScope}.
     */
    @Override
    public BlockBlobAsyncClient getEncryptionScopeAsyncClient(String encryptionScope) {
        EncryptionScope finalEncryptionScope = null;
        if (encryptionScope != null) {
            finalEncryptionScope = new EncryptionScope().setEncryptionScope(encryptionScope);
        }
        return new BlockBlobAsyncClient(getHttpPipeline(), getAccountUrl(), getServiceVersion(), getAccountName(),
            getContainerName(), getBlobName(), getSnapshotId(), getCustomerProvidedKey(), finalEncryptionScope,
            getVersionId(), getSerializerAdapter());
    }

    /**
     * Creates a new {@link BlockBlobAsyncClient} with the specified {@code customerProvidedKey}.
     *
     * @param customerProvidedKey the {@link CustomerProvidedKey} for the blob,
     * pass {@code null} to use no customer provided key.
     * @return a {@link BlockBlobAsyncClient} with the specified {@code customerProvidedKey}.
     */
    @Override
    public BlockBlobAsyncClient getCustomerProvidedKeyAsyncClient(CustomerProvidedKey customerProvidedKey) {
        CpkInfo finalCustomerProvidedKey = null;
        if (customerProvidedKey != null) {
            finalCustomerProvidedKey = new CpkInfo()
                .setEncryptionKey(customerProvidedKey.getKey())
                .setEncryptionKeySha256(customerProvidedKey.getKeySha256())
                .setEncryptionAlgorithm(customerProvidedKey.getEncryptionAlgorithm());
        }
        return new BlockBlobAsyncClient(getHttpPipeline(), getAccountUrl(), getServiceVersion(), getAccountName(),
            getContainerName(), getBlobName(), getSnapshotId(), finalCustomerProvidedKey, encryptionScope,
            getVersionId(), getSerializerAdapter());
    }

    /**
     * Creates a new block blob. By default this method will not overwrite an existing blob. Updating an existing block
     * blob overwrites any existing metadata on the blob. Partial updates are not supported with PutBlob; the content
     * of the existing blob is overwritten with the new content. To perform a partial update of a block blob's, use
     * PutBlock and PutBlockList. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob">Azure Docs</a>.
     * <p>
     * Note that the data passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flux} must produce the same data each time it is subscribed to.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlockBlobAsyncClient.upload#Flux-long}
     *
     * @param data The data to write to the blob. Note that this {@code Flux} must be replayable if retries are enabled
     * (the default). In other words, the Flux must produce the same data each time it is subscribed to.
     * @param length The exact length of the data. It is important that this value match precisely the length of the
     * data emitted by the {@code Flux}.
     * @return A reactive response containing the information of the uploaded block blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BlockBlobItem> upload(Flux<ByteBuffer> data, long length) {
        try {
            return upload(data, length, false);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob. Updating an existing block blob
     * overwrites any existing metadata on the blob. Partial updates are not supported with PutBlob; the content of the
     * existing blob is overwritten with the new content. To perform a partial update of a block blob's, use PutBlock
     * and PutBlockList. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob">Azure Docs</a>.
     * <p>
     * Note that the data passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flux} must produce the same data each time it is subscribed to.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlockBlobAsyncClient.upload#Flux-long-boolean}
     *
     * @param data The data to write to the blob. Note that this {@code Flux} must be replayable if retries are enabled
     * (the default). In other words, the Flux must produce the same data each time it is subscribed to.
     * @param length The exact length of the data. It is important that this value match precisely the length of the
     * data emitted by the {@code Flux}.
     * @param overwrite Whether or not to overwrite, should data exist on the blob.
     * @return A reactive response containing the information of the uploaded block blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BlockBlobItem> upload(Flux<ByteBuffer> data, long length, boolean overwrite) {
        try {
            BlobRequestConditions blobRequestConditions = new BlobRequestConditions();
            if (!overwrite) {
                blobRequestConditions.setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD);
            }
            return uploadWithResponse(data, length, null, null, null, null, blobRequestConditions)
                .flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     * <p>
     * Updating an existing block blob overwrites any existing metadata on the blob. Partial updates are not supported
     * with PutBlob; the content of the existing blob is overwritten with the new content. To perform a partial update
     * of a block blob's, use PutBlock and PutBlockList. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob">Azure Docs</a>.
     * <p>
     * Note that the data passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flux} must produce the same data each time it is subscribed to.
     * <p>
     * To avoid overwriting, pass "*" to {@link BlobRequestConditions#setIfNoneMatch(String)}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlockBlobAsyncClient.uploadWithResponse#Flux-long-BlobHttpHeaders-Map-AccessTier-byte-BlobRequestConditions}
     *
     * @param data The data to write to the blob. Note that this {@code Flux} must be replayable if retries are enabled
     * (the default). In other words, the Flux must produce the same data each time it is subscribed to.
     * @param length The exact length of the data. It is important that this value match precisely the length of the
     * data emitted by the {@code Flux}.
     * @param headers {@link BlobHttpHeaders}
     * @param metadata Metadata to associate with the blob. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param tier {@link AccessTier} for the destination blob.
     * @param contentMd5 An MD5 hash of the blob content. This hash is used to verify the integrity of the blob during
     * transport. When this header is specified, the storage service compares the hash of the content that has arrived
     * with this header value. Note that this MD5 hash is not stored with the blob. If the two hashes do not match, the
     * operation will fail.
     * @param requestConditions {@link BlobRequestConditions}
     * @return A reactive response containing the information of the uploaded block blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BlockBlobItem>> uploadWithResponse(Flux<ByteBuffer> data, long length, BlobHttpHeaders headers,
        Map<String, String> metadata, AccessTier tier, byte[] contentMd5, BlobRequestConditions requestConditions) {
        return this.uploadWithResponse(new BlockBlobSimpleUploadOptions(data, length).setHeaders(headers)
            .setMetadata(metadata).setTier(tier).setContentMd5(contentMd5).setRequestConditions(requestConditions));
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     * <p>
     * Updating an existing block blob overwrites any existing metadata on the blob. Partial updates are not supported
     * with PutBlob; the content of the existing blob is overwritten with the new content. To perform a partial update
     * of a block blob's, use PutBlock and PutBlockList. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob">Azure Docs</a>.
     * <p>
     * Note that the data passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flux} must produce the same data each time it is subscribed to.
     * <p>
     * To avoid overwriting, pass "*" to {@link BlobRequestConditions#setIfNoneMatch(String)}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlockBlobAsyncClient.uploadWithResponse#BlockBlobSimpleUploadOptions}
     *
     * @param options {@link BlockBlobSimpleUploadOptions}
     * @return A reactive response containing the information of the uploaded block blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BlockBlobItem>> uploadWithResponse(BlockBlobSimpleUploadOptions options) {
        try {
            return withContext(context -> uploadWithResponse(options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<BlockBlobItem>> uploadWithResponse(BlockBlobSimpleUploadOptions options, Context context) {
        StorageImplUtils.assertNotNull("options", options);
        Flux<ByteBuffer> data = options.getDataFlux() == null ? Utility.convertStreamToByteBuffer(
            options.getDataStream(), options.getLength(), BlobAsyncClient.BLOB_DEFAULT_UPLOAD_BLOCK_SIZE, true)
            .subscribeOn(Schedulers.boundedElastic())
            : options.getDataFlux();
        BlobRequestConditions requestConditions = options.getRequestConditions() == null ? new BlobRequestConditions()
            : options.getRequestConditions();
        context = context == null ? Context.NONE : context;
        BlobImmutabilityPolicy immutabilityPolicy = options.getImmutabilityPolicy() == null
            ? new BlobImmutabilityPolicy() : options.getImmutabilityPolicy();

        return this.azureBlobStorage.getBlockBlobs().uploadWithResponseAsync(containerName, blobName,
            options.getLength(), data, null, options.getContentMd5(), options.getMetadata(),
            requestConditions.getLeaseId(), options.getTier(), requestConditions.getIfModifiedSince(),
            requestConditions.getIfUnmodifiedSince(), requestConditions.getIfMatch(),
            requestConditions.getIfNoneMatch(), requestConditions.getTagsConditions(), null,
            tagsToString(options.getTags()), immutabilityPolicy.getExpiryTime(), immutabilityPolicy.getPolicyMode(),
            options.isLegalHold(), options.getHeaders(), getCustomerProvidedKey(),
            encryptionScope, context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(rb -> {
                BlockBlobsUploadHeaders hd = rb.getDeserializedHeaders();
                BlockBlobItem item = new BlockBlobItem(hd.getETag(), hd.getLastModified(), hd.getContentMD5(),
                    hd.isXMsRequestServerEncrypted(), hd.getXMsEncryptionKeySha256(), hd.getXMsEncryptionScope(),
                    hd.getXMsVersionId());
                return new SimpleResponse<>(rb, item);
            });
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     * <p>
     * Updating an existing block blob overwrites any existing metadata on the blob. Partial updates are not supported
     * with PutBlobFromUrl; the content of the existing blob is overwritten with the new content.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob-from-url">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlockBlobAsyncClient.uploadFromUrl#String}
     *
     * @param sourceUrl The source URL to upload from.
     * @return A reactive response containing the information of the uploaded block blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BlockBlobItem> uploadFromUrl(String sourceUrl) {
        try {
            return uploadFromUrl(sourceUrl, false);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     * <p>
     * Updating an existing block blob overwrites any existing metadata on the blob. Partial updates are not supported
     * with PutBlobFromUrl; the content of the existing blob is overwritten with the new content.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob-from-url">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlockBlobAsyncClient.uploadFromUrl#String-boolean}
     *
     * @param sourceUrl The source URL to upload from.
     * @param overwrite Whether or not to overwrite, should data exist on the blob.
     * @return A reactive response containing the information of the uploaded block blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BlockBlobItem> uploadFromUrl(String sourceUrl, boolean overwrite) {
        try {
            BlobRequestConditions blobRequestConditions = new BlobRequestConditions();
            if (!overwrite) {
                blobRequestConditions.setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD);
            }
            return uploadFromUrlWithResponse(new BlobUploadFromUrlOptions(sourceUrl)
                .setDestinationRequestConditions(blobRequestConditions))
                .flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     * <p>
     * Updating an existing block blob overwrites any existing metadata on the blob. Partial updates are not supported
     * with PutBlobFromUrl; the content of the existing blob is overwritten with the new content.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob-from-url">Azure Docs</a>.
     * <p>
     * To avoid overwriting, pass "*" to {@link BlobRequestConditions#setIfNoneMatch(String)}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlockBlobAsyncClient.uploadFromUrlWithResponse#BlobUploadFromUrlOptions}
     *
     * @param options {@link BlobUploadFromUrlOptions}
     * @return A reactive response containing the information of the uploaded block blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BlockBlobItem>> uploadFromUrlWithResponse(BlobUploadFromUrlOptions options) {
        try {
            return withContext(context -> uploadFromUrlWithResponse(options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<BlockBlobItem>> uploadFromUrlWithResponse(BlobUploadFromUrlOptions options, Context context) {
        StorageImplUtils.assertNotNull("options", options);
        BlobRequestConditions destinationRequestConditions =
            options.getDestinationRequestConditions() == null ? new BlobRequestConditions()
            : options.getDestinationRequestConditions();
        BlobRequestConditions sourceRequestConditions =
            options.getSourceRequestConditions() == null ? new BlobRequestConditions()
            : options.getSourceRequestConditions();
        context = context == null ? Context.NONE : context;
        String sourceAuth = options.getSourceAuthorization() == null
            ? null : options.getSourceAuthorization().toString();

        try {
            new URL(options.getSourceUrl());
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'sourceUrl' is not a valid url."));
        }

        // TODO (kasobol-msft) add metadata back (https://github.com/Azure/azure-sdk-for-net/issues/15969)
        return this.azureBlobStorage.getBlockBlobs().putBlobFromUrlWithResponseAsync(
            containerName, blobName, 0, options.getSourceUrl(), null, null, null,
            destinationRequestConditions.getLeaseId(), options.getTier(),
            destinationRequestConditions.getIfModifiedSince(), destinationRequestConditions.getIfUnmodifiedSince(),
            destinationRequestConditions.getIfMatch(), destinationRequestConditions.getIfNoneMatch(),
            destinationRequestConditions.getTagsConditions(),
            sourceRequestConditions.getIfModifiedSince(), sourceRequestConditions.getIfUnmodifiedSince(),
            sourceRequestConditions.getIfMatch(), sourceRequestConditions.getIfNoneMatch(),
            sourceRequestConditions.getTagsConditions(),
            null, options.getContentMd5(), tagsToString(options.getTags()),
            options.isCopySourceBlobProperties(), sourceAuth, options.getHeaders(),
            getCustomerProvidedKey(), encryptionScope,
            context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(rb -> {
                BlockBlobsPutBlobFromUrlHeaders hd = rb.getDeserializedHeaders();
                BlockBlobItem item = new BlockBlobItem(hd.getETag(), hd.getLastModified(), hd.getContentMD5(),
                    hd.isXMsRequestServerEncrypted(), hd.getXMsEncryptionKeySha256(), hd.getXMsEncryptionScope(),
                    hd.getXMsVersionId());
                return new SimpleResponse<>(rb, item);
            });
    }

    /**
     * Uploads the specified block to the block blob's "staging area" to be later committed by a call to
     * commitBlockList. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block">Azure Docs</a>.
     * <p>
     * Note that the data passed must be replayable if retries are enabled (the default). In other words, the
     *
     * @param base64BlockId A Base64 encoded {@code String} that specifies the ID for this block. Note that all block
     * ids for a given blob must be the same length.
     * @param data The data to write to the block. Note that this {@code Flux} must be replayable if retries are enabled
     * (the default). In other words, the {@code Flux} must produce the same data each time it is subscribed to.
     * @param length The exact length of the data. It is important that this value match precisely the length of the
     * data emitted by the {@code Flux}.
     *
     * @return A reactive response signalling completion.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlockBlobAsyncClient.stageBlock#String-Flux-long}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> stageBlock(String base64BlockId, Flux<ByteBuffer> data, long length) {
        try {
            return stageBlockWithResponse(base64BlockId, data, length, null, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Uploads the specified block to the block blob's "staging area" to be later committed by a call to
     * commitBlockList. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block">Azure Docs</a>.
     * <p>
     * Note that the data passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flux} must produce the same data each time it is subscribed to.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlockBlobAsyncClient.stageBlockWithResponse#String-Flux-long-byte-String}
     *
     * @param base64BlockId A Base64 encoded {@code String} that specifies the ID for this block. Note that all block
     * ids for a given blob must be the same length.
     * @param data The data to write to the block. Note that this {@code Flux} must be replayable if retries are enabled
     * (the default). In other words, the Flux must produce the same data each time it is subscribed to.
     * @param length The exact length of the data. It is important that this value match precisely the length of the
     * data emitted by the {@code Flux}.
     * @param contentMd5 An MD5 hash of the block content. This hash is used to verify the integrity of the block during
     * transport. When this header is specified, the storage service compares the hash of the content that has arrived
     * with this header value. Note that this MD5 hash is not stored with the blob. If the two hashes do not match, the
     * operation will fail.
     * @param leaseId The lease ID the active lease on the blob must match.
     *
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> stageBlockWithResponse(String base64BlockId, Flux<ByteBuffer> data, long length,
        byte[] contentMd5, String leaseId) {
        try {
            return withContext(context -> stageBlockWithResponse(base64BlockId, data, length,
                contentMd5, leaseId, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> stageBlockWithResponse(String base64BlockId, Flux<ByteBuffer> data, long length,
        byte[] contentMd5, String leaseId, Context context) {
        context = context == null ? Context.NONE : context;
        return this.azureBlobStorage.getBlockBlobs().stageBlockWithResponseAsync(containerName, blobName,
            base64BlockId, length, data, contentMd5, null, null, leaseId, null, getCustomerProvidedKey(),
            encryptionScope, context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Creates a new block to be committed as part of a blob where the contents are read from a URL. For more
     * information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/put-block-from-url">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlockBlobAsyncClient.stageBlockFromUrl#String-String-BlobRange}
     *
     * @param base64BlockId A Base64 encoded {@code String} that specifies the ID for this block. Note that all block
     * ids for a given blob must be the same length.
     * @param sourceUrl The url to the blob that will be the source of the copy.  A source blob in the same storage
     * account can be authenticated via Shared Key. However, if the source is a blob in another account, the source blob
     * must either be public or must be authenticated via a shared access signature. If the source blob is public, no
     * authentication is required to perform the operation.
     * @param sourceRange {@link BlobRange}
     *
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> stageBlockFromUrl(String base64BlockId, String sourceUrl, BlobRange sourceRange) {
        try {
            return this.stageBlockFromUrlWithResponse(base64BlockId, sourceUrl, sourceRange, null, null, null)
                .flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new block to be committed as part of a blob where the contents are read from a URL. For more
     * information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/put-block-from-url">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlockBlobAsyncClient.stageBlockFromUrlWithResponse#String-String-BlobRange-byte-String-BlobRequestConditions}
     *
     * @param base64BlockId A Base64 encoded {@code String} that specifies the ID for this block. Note that all block
     * ids for a given blob must be the same length.
     * @param sourceUrl The url to the blob that will be the source of the copy.  A source blob in the same storage
     * account can be authenticated via Shared Key. However, if the source is a blob in another account, the source blob
     * must either be public or must be authenticated via a shared access signature. If the source blob is public, no
     * authentication is required to perform the operation.
     * @param sourceRange {@link BlobRange}
     * @param sourceContentMd5 An MD5 hash of the block content. This hash is used to verify the integrity of the block
     * during transport. When this header is specified, the storage service compares the hash of the content that has
     * arrived with this header value. Note that this MD5 hash is not stored with the blob. If the two hashes do not
     * match, the operation will fail.
     * @param leaseId The lease ID that the active lease on the blob must match.
     * @param sourceRequestConditions {@link BlobRequestConditions}
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> stageBlockFromUrlWithResponse(String base64BlockId, String sourceUrl,
        BlobRange sourceRange, byte[] sourceContentMd5, String leaseId, BlobRequestConditions sourceRequestConditions) {
        try {
            return this.stageBlockFromUrlWithResponse(new BlockBlobStageBlockFromUrlOptions(base64BlockId, sourceUrl)
                .setSourceRange(sourceRange).setSourceContentMd5(sourceContentMd5).setLeaseId(leaseId)
                .setSourceRequestConditions(sourceRequestConditions));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new block to be committed as part of a blob where the contents are read from a URL. For more
     * information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/put-block-from-url">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlockBlobAsyncClient.stageBlockFromUrlWithResponse#BlockBlobStageBlockFromUrlOptions}
     *
     * @param options parameters for the operation.
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> stageBlockFromUrlWithResponse(BlockBlobStageBlockFromUrlOptions options) {
        try {
            return withContext(context -> stageBlockFromUrlWithResponse(options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> stageBlockFromUrlWithResponse(BlockBlobStageBlockFromUrlOptions options, Context context) {
        BlobRange sourceRange = (options.getSourceRange() == null) ? new BlobRange(0) : options.getSourceRange();
        BlobRequestConditions sourceRequestConditions = (options.getSourceRequestConditions() == null)
            ? new BlobRequestConditions() : options.getSourceRequestConditions();

        try {
            new URL(options.getSourceUrl());
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'sourceUrl' is not a valid url."));
        }
        context = context == null ? Context.NONE : context;
        String sourceAuth = options.getSourceAuthorization() == null
            ? null : options.getSourceAuthorization().toString();

        return this.azureBlobStorage.getBlockBlobs().stageBlockFromURLWithResponseAsync(containerName, blobName,
            options.getBase64BlockId(), 0, options.getSourceUrl(), sourceRange.toHeaderValue(), options.getSourceContentMd5(), null, null,
            options.getLeaseId(), sourceRequestConditions.getIfModifiedSince(),
            sourceRequestConditions.getIfUnmodifiedSince(), sourceRequestConditions.getIfMatch(),
            sourceRequestConditions.getIfNoneMatch(), null, sourceAuth, getCustomerProvidedKey(),
            encryptionScope, context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Returns the list of blocks that have been uploaded as part of a block blob using the specified block list filter.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-block-list">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlockBlobAsyncClient.listBlocks#BlockListType}
     *
     * @param listType Specifies which type of blocks to return.
     *
     * @return A reactive response containing the list of blocks.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BlockList> listBlocks(BlockListType listType) {
        try {
            return this.listBlocksWithResponse(listType, null).map(Response::getValue);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns the list of blocks that have been uploaded as part of a block blob using the specified block list
     * filter.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-block-list">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlockBlobAsyncClient.listBlocksWithResponse#BlockListType-String}
     *
     * @param listType Specifies which type of blocks to return.
     * @param leaseId The lease ID the active lease on the blob must match.
     * @return A reactive response containing the list of blocks.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BlockList>> listBlocksWithResponse(BlockListType listType, String leaseId) {
        try {
            return this.listBlocksWithResponse(new BlockBlobListBlocksOptions(listType).setLeaseId(leaseId));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns the list of blocks that have been uploaded as part of a block blob using the specified block list
     * filter.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-block-list">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlockBlobAsyncClient.listBlocksWithResponse#BlockBlobListBlocksOptions}
     *
     * @param options {@link BlockBlobListBlocksOptions}
     * @return A reactive response containing the list of blocks.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BlockList>> listBlocksWithResponse(BlockBlobListBlocksOptions options) {
        try {
            return withContext(context -> listBlocksWithResponse(options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<BlockList>> listBlocksWithResponse(BlockBlobListBlocksOptions options, Context context) {
        StorageImplUtils.assertNotNull("options", options);

        return this.azureBlobStorage.getBlockBlobs().getBlockListWithResponseAsync(
            containerName, blobName, options.getType(), getSnapshotId(), null, options.getLeaseId(),
            options.getIfTagsMatch(), null, context)
            .map(response -> new SimpleResponse<>(response, response.getValue()));
    }

    /**
     * Writes a blob by specifying the list of block IDs that are to make up the blob. In order to be written as part of
     * a blob, a block must have been successfully written to the server in a prior stageBlock operation. You can call
     * commitBlockList to update a blob by uploading only those blocks that have changed, then committing the new and
     * existing blocks together. Any blocks not specified in the block list and permanently deleted. For more
     * information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block-list">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlockBlobAsyncClient.commitBlockList#List}
     *
     * @param base64BlockIds A list of base64 encode {@code String}s that specifies the block IDs to be committed.
     * @return A reactive response containing the information of the block blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BlockBlobItem> commitBlockList(List<String> base64BlockIds) {
        try {
            return commitBlockList(base64BlockIds, false);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Writes a blob by specifying the list of block IDs that are to make up the blob. In order to be written as part of
     * a blob, a block must have been successfully written to the server in a prior stageBlock operation. You can call
     * commitBlockList to update a blob by uploading only those blocks that have changed, then committing the new and
     * existing blocks together. Any blocks not specified in the block list and permanently deleted. For more
     * information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block-list">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlockBlobAsyncClient.commitBlockList#List-boolean}
     *
     * @param base64BlockIds A list of base64 encode {@code String}s that specifies the block IDs to be committed.
     * @param overwrite Whether or not to overwrite, should data exist on the blob.
     * @return A reactive response containing the information of the block blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BlockBlobItem> commitBlockList(List<String> base64BlockIds, boolean overwrite) {
        try {
            BlobRequestConditions requestConditions = null;
            if (!overwrite) {
                requestConditions = new BlobRequestConditions().setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD);
            }
            return commitBlockListWithResponse(base64BlockIds, null, null, null, requestConditions)
                .flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Writes a blob by specifying the list of block IDs that are to make up the blob. In order to be written as part
     * of a blob, a block must have been successfully written to the server in a prior stageBlock operation. You can
     * call commitBlockList to update a blob by uploading only those blocks that have changed, then committing the new
     * and existing blocks together. Any blocks not specified in the block list and permanently deleted. For more
     * information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block-list">Azure Docs</a>.
     * <p>
     * To avoid overwriting, pass "*" to {@link BlobRequestConditions#setIfNoneMatch(String)}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlockBlobAsyncClient.commitBlockListWithResponse#List-BlobHttpHeaders-Map-AccessTier-BlobRequestConditions}
     *
     * @param base64BlockIds A list of base64 encode {@code String}s that specifies the block IDs to be committed.
     * @param headers {@link BlobHttpHeaders}
     * @param metadata Metadata to associate with the blob. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param tier {@link AccessTier} for the destination blob.
     * @param requestConditions {@link BlobRequestConditions}
     * @return A reactive response containing the information of the block blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BlockBlobItem>> commitBlockListWithResponse(List<String> base64BlockIds,
            BlobHttpHeaders headers, Map<String, String> metadata, AccessTier tier,
            BlobRequestConditions requestConditions) {
        return this.commitBlockListWithResponse(new BlockBlobCommitBlockListOptions(base64BlockIds)
            .setHeaders(headers).setMetadata(metadata).setTier(tier).setRequestConditions(requestConditions));
    }

    /**
     * Writes a blob by specifying the list of block IDs that are to make up the blob. In order to be written as part
     * of a blob, a block must have been successfully written to the server in a prior stageBlock operation. You can
     * call commitBlockList to update a blob by uploading only those blocks that have changed, then committing the new
     * and existing blocks together. Any blocks not specified in the block list and permanently deleted. For more
     * information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block-list">Azure Docs</a>.
     * <p>
     * To avoid overwriting, pass "*" to {@link BlobRequestConditions#setIfNoneMatch(String)}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlockBlobAsyncClient.commitBlockListWithResponse#BlockBlobCommitBlockListOptions}
     *
     * @param options {@link BlockBlobCommitBlockListOptions}
     * @return A reactive response containing the information of the block blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BlockBlobItem>> commitBlockListWithResponse(BlockBlobCommitBlockListOptions options) {
        try {
            return withContext(context -> commitBlockListWithResponse(options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<BlockBlobItem>> commitBlockListWithResponse(BlockBlobCommitBlockListOptions options,
        Context context) {
        StorageImplUtils.assertNotNull("options", options);
        BlobRequestConditions requestConditions = options.getRequestConditions() == null ? new BlobRequestConditions()
            : options.getRequestConditions();
        context = context == null ? Context.NONE : context;
        BlobImmutabilityPolicy immutabilityPolicy = options.getImmutabilityPolicy() == null
            ? new BlobImmutabilityPolicy() : options.getImmutabilityPolicy();

        return this.azureBlobStorage.getBlockBlobs().commitBlockListWithResponseAsync(containerName, blobName,
            new BlockLookupList().setLatest(options.getBase64BlockIds()), null, null, null, options.getMetadata(),
            requestConditions.getLeaseId(), options.getTier(), requestConditions.getIfModifiedSince(),
            requestConditions.getIfUnmodifiedSince(), requestConditions.getIfMatch(),
            requestConditions.getIfNoneMatch(), requestConditions.getTagsConditions(), null,
            tagsToString(options.getTags()), immutabilityPolicy.getExpiryTime(), immutabilityPolicy.getPolicyMode(),
            options.isLegalHold(), options.getHeaders(), getCustomerProvidedKey(),
            encryptionScope, context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(rb -> {
                BlockBlobsCommitBlockListHeaders hd = rb.getDeserializedHeaders();
                BlockBlobItem item = new BlockBlobItem(hd.getETag(), hd.getLastModified(), hd.getContentMD5(),
                    hd.isXMsRequestServerEncrypted(), hd.getXMsEncryptionKeySha256(), hd.getXMsEncryptionScope(),
                    hd.getXMsVersionId());
                return new SimpleResponse<>(rb, item);
            });
    }
}
