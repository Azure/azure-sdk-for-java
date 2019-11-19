// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.implementation.models.BlockBlobCommitBlockListHeaders;
import com.azure.storage.blob.implementation.models.BlockBlobUploadHeaders;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlockBlobItem;
import com.azure.storage.blob.models.BlockList;
import com.azure.storage.blob.models.BlockListType;
import com.azure.storage.blob.models.BlockLookupList;
import com.azure.storage.blob.models.CpkInfo;
import com.azure.storage.common.implementation.Constants;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * Client to a block blob. It may only be instantiated through a {@link SpecializedBlobClientBuilder} or via the method
 * {@link BlobAsyncClient#getBlockBlobAsyncClient()}. This class does not hold any state about a particular blob, but is
 * instead a convenient way of sending appropriate requests to the resource on the service.
 *
 * <p>
 * Please refer to the
 * <a href=https://docs.microsoft.com/en-us/rest/api/storageservices/understanding-block-blobs--append-blobs--and-page-blobs>Azure Docs</a> for more information.
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
     */
    public static final int MAX_UPLOAD_BLOB_BYTES = 256 * Constants.MB;

    /**
     * Indicates the maximum number of bytes that can be sent in a call to stageBlock.
     */
    public static final int MAX_STAGE_BLOCK_BYTES = 100 * Constants.MB;

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
     */
    BlockBlobAsyncClient(HttpPipeline pipeline, String url, BlobServiceVersion serviceVersion,
        String accountName, String containerName, String blobName, String snapshot, CpkInfo customerProvidedKey) {
        super(pipeline, url, serviceVersion, accountName, containerName, blobName, snapshot, customerProvidedKey);
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
     * <p>
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
     * <p>
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
     * @param metadata Metadata to associate with the blob.
     * @param tier {@link AccessTier} for the destination blob.
     * @param contentMd5 An MD5 hash of the blob content. This hash is used to verify the integrity of the blob during
     * transport. When this header is specified, the storage service compares the hash of the content that has arrived
     * with this header value. Note that this MD5 hash is not stored with the blob. If the two hashes do not match, the
     * operation will fail.
     * @param requestConditions {@link BlobRequestConditions}
     * @return A reactive response containing the information of the uploaded block blob.
     */
    public Mono<Response<BlockBlobItem>> uploadWithResponse(Flux<ByteBuffer> data, long length, BlobHttpHeaders headers,
        Map<String, String> metadata, AccessTier tier, byte[] contentMd5, BlobRequestConditions requestConditions) {
        try {
            return withContext(context -> uploadWithResponse(data, length, headers, metadata, tier, contentMd5,
                requestConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<BlockBlobItem>> uploadWithResponse(Flux<ByteBuffer> data, long length, BlobHttpHeaders headers,
        Map<String, String> metadata, AccessTier tier, byte[] contentMd5, BlobRequestConditions requestConditions,
        Context context) {
        requestConditions = requestConditions == null ? new BlobRequestConditions() : requestConditions;

        return this.azureBlobStorage.blockBlobs().uploadWithRestResponseAsync(null,
            null, data, length, null, contentMd5, metadata, requestConditions.getLeaseId(), tier,
            requestConditions.getIfModifiedSince(), requestConditions.getIfUnmodifiedSince(),
            requestConditions.getIfMatch(), requestConditions.getIfNoneMatch(), null, headers, getCustomerProvidedKey(),
            context)
            .map(rb -> {
                BlockBlobUploadHeaders hd = rb.getDeserializedHeaders();
                BlockBlobItem item = new BlockBlobItem(hd.getETag(), hd.getLastModified(), hd.getContentMD5(),
                    hd.isServerEncrypted(), hd.getEncryptionKeySha256());
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
    public Mono<Response<Void>> stageBlockWithResponse(String base64BlockId, Flux<ByteBuffer> data, long length,
        byte[] contentMd5, String leaseId) {
        try {
            return withContext(context -> stageBlockWithResponse(base64BlockId, data, length, contentMd5,
                leaseId, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> stageBlockWithResponse(String base64BlockId, Flux<ByteBuffer> data, long length,
        byte[] contentMd5, String leaseId, Context context) {
        return this.azureBlobStorage.blockBlobs().stageBlockWithRestResponseAsync(null, null,
            base64BlockId, length, data, contentMd5, null, null, leaseId, null, getCustomerProvidedKey(), context)
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Creates a new block to be committed as part of a blob where the contents are read from a URL. For more
     * information, see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/put-block-from-url">Azure
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
    public Mono<Response<Void>> stageBlockFromUrlWithResponse(String base64BlockId, String sourceUrl,
        BlobRange sourceRange, byte[] sourceContentMd5, String leaseId, BlobRequestConditions sourceRequestConditions) {
        try {
            return withContext(context -> stageBlockFromUrlWithResponse(base64BlockId, sourceUrl, sourceRange,
                sourceContentMd5, leaseId, sourceRequestConditions));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> stageBlockFromUrlWithResponse(String base64BlockId, String sourceUrl, BlobRange sourceRange,
        byte[] sourceContentMd5, String leaseId, BlobRequestConditions sourceRequestConditions, Context context) {
        sourceRange = (sourceRange == null) ? new BlobRange(0) : sourceRange;
        sourceRequestConditions = (sourceRequestConditions == null)
            ? new BlobRequestConditions() : sourceRequestConditions;

        URL url;
        try {
            url = new URL(sourceUrl);
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'sourceUrl' is not a valid url."));
        }

        return this.azureBlobStorage.blockBlobs().stageBlockFromURLWithRestResponseAsync(null, null, base64BlockId, 0,
            url, sourceRange.toHeaderValue(), sourceContentMd5, null, null, leaseId,
            sourceRequestConditions.getIfModifiedSince(), sourceRequestConditions.getIfUnmodifiedSince(),
            sourceRequestConditions.getIfMatch(), sourceRequestConditions.getIfNoneMatch(), null,
            getCustomerProvidedKey(), context)
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
    public Mono<Response<BlockList>> listBlocksWithResponse(BlockListType listType, String leaseId) {
        try {
            return withContext(context -> listBlocksWithResponse(listType, leaseId, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<BlockList>> listBlocksWithResponse(BlockListType listType, String leaseId, Context context) {

        return this.azureBlobStorage.blockBlobs().getBlockListWithRestResponseAsync(
            null, null, listType, getSnapshotId(), null, leaseId, null, context)
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
     * @param metadata Metadata to associate with the blob.
     * @param tier {@link AccessTier} for the destination blob.
     * @param requestConditions {@link BlobRequestConditions}
     * @return A reactive response containing the information of the block blob.
     */
    public Mono<Response<BlockBlobItem>> commitBlockListWithResponse(List<String> base64BlockIds,
            BlobHttpHeaders headers, Map<String, String> metadata, AccessTier tier,
            BlobRequestConditions requestConditions) {
        try {
            return withContext(context -> commitBlockListWithResponse(base64BlockIds, headers, metadata, tier,
                requestConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<BlockBlobItem>> commitBlockListWithResponse(List<String> base64BlockIds,
            BlobHttpHeaders headers, Map<String, String> metadata, AccessTier tier,
            BlobRequestConditions requestConditions, Context context) {
        requestConditions = requestConditions == null ? new BlobRequestConditions() : requestConditions;

        return this.azureBlobStorage.blockBlobs().commitBlockListWithRestResponseAsync(null, null,
            new BlockLookupList().setLatest(base64BlockIds), null, null, null, metadata, requestConditions.getLeaseId(),
            tier, requestConditions.getIfModifiedSince(), requestConditions.getIfUnmodifiedSince(),
            requestConditions.getIfMatch(), requestConditions.getIfNoneMatch(), null, headers, getCustomerProvidedKey(),
            context)
            .map(rb -> {
                BlockBlobCommitBlockListHeaders hd = rb.getDeserializedHeaders();
                BlockBlobItem item = new BlockBlobItem(hd.getETag(), hd.getLastModified(), hd.getContentMD5(),
                    hd.isServerEncrypted(), hd.getEncryptionKeySha256());
                return new SimpleResponse<>(rb, item);
            });
    }
}
