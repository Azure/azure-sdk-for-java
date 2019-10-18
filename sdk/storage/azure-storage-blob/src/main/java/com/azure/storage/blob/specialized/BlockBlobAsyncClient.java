// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.implementation.util.FluxUtil;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlockBlobItem;
import com.azure.storage.blob.models.BlockList;
import com.azure.storage.blob.models.BlockListType;
import com.azure.storage.blob.models.BlockLookupList;
import com.azure.storage.blob.models.CpkInfo;
import com.azure.storage.blob.models.LeaseAccessConditions;
import com.azure.storage.blob.models.SourceModifiedAccessConditions;
import com.azure.storage.common.implementation.Constants;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import static com.azure.core.implementation.util.FluxUtil.withContext;

/**
 * Client to a block blob. It may only be instantiated through a {@link SpecializedBlobClientBuilder} or via the method
 * {@link BlobAsyncClient#getBlockBlobAsyncClient()}. This class does not hold any state about a particular blob, but is
 * instead a convenient way of sending appropriate requests to the resource on the service.
 *
 * <p>
 * Please refer to the <a href=https://docs.microsoft.com/en-us/rest/api/storageservices/understanding-block-blobs--append-blobs--and-page-blobs>Azure
 * Docs</a> for more information.
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
     * {@codesnippet com.azure.storage.blob.specialized.BlockBlobAsyncClient.upload#Flux-long}
     *
     * @param data The data to write to the blob. Note that this {@code Flux} must be replayable if retries are enabled
     * (the default). In other words, the Flux must produce the same data each time it is subscribed to.
     * @param length The exact length of the data. It is important that this value match precisely the length of the
     * data emitted by the {@code Flux}.
     * @return A reactive response containing the information of the uploaded block blob.
     */
    public Mono<BlockBlobItem> upload(Flux<ByteBuffer> data, long length) {
        return uploadWithResponse(data, length, null, null, null, null).flatMap(FluxUtil::toMono);
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
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlockBlobAsyncClient.uploadWithResponse#Flux-long-BlobHttpHeaders-Map-AccessTier-BlobAccessConditions}
     *
     * @param data The data to write to the blob. Note that this {@code Flux} must be replayable if retries are enabled
     * (the default). In other words, the Flux must produce the same data each time it is subscribed to.
     * @param length The exact length of the data. It is important that this value match precisely the length of the
     * data emitted by the {@code Flux}.
     * @param headers {@link BlobHttpHeaders}
     * @param metadata Metadata to associate with the blob.
     * @param tier {@link AccessTier} for the destination blob.
     * @param accessConditions {@link BlobAccessConditions}
     * @return A reactive response containing the information of the uploaded block blob.
     */
    public Mono<Response<BlockBlobItem>> uploadWithResponse(Flux<ByteBuffer> data, long length, BlobHttpHeaders headers,
        Map<String, String> metadata, AccessTier tier, BlobAccessConditions accessConditions) {
        return withContext(context -> uploadWithResponse(data, length, headers, metadata, tier, accessConditions,
            context));
    }

    Mono<Response<BlockBlobItem>> uploadWithResponse(Flux<ByteBuffer> data, long length, BlobHttpHeaders headers,
        Map<String, String> metadata, AccessTier tier, BlobAccessConditions accessConditions, Context context) {
        accessConditions = accessConditions == null ? new BlobAccessConditions() : accessConditions;

        return this.azureBlobStorage.blockBlobs().uploadWithRestResponseAsync(null,
            null, data, length, null, metadata, tier, null, headers, accessConditions.getLeaseAccessConditions(),
            getCustomerProvidedKey(), accessConditions.getModifiedAccessConditions(), context)
            .map(rb -> new SimpleResponse<>(rb, new BlockBlobItem(rb.getDeserializedHeaders())));
    }

    /**
     * Uploads the specified block to the block blob's "staging area" to be later committed by a call to
     * commitBlockList. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block">Azure Docs</a>.
     * <p>
     * Note that the data passed must be replayable if retries are enabled (the default). In other words, the
     *
     * @param base64BlockID A Base64 encoded {@code String} that specifies the ID for this block. Note that all block
     * ids for a given blob must be the same length.
     * @param data The data to write to the block. Note that this {@code Flux} must be replayable if retries are enabled
     * (the default). In other words, the Flux must produce the same data each time it is subscribed to.
     * @param length The exact length of the data. It is important that this value match precisely the length of the
     * data emitted by the {@code Flux}.
     * @return A reactive response signalling completion.
     * @code Flux} must produce the same data each time it is subscribed to.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlockBlobAsyncClient.stageBlock#String-Flux-long}
     */
    public Mono<Void> stageBlock(String base64BlockID, Flux<ByteBuffer> data, long length) {
        return stageBlockWithResponse(base64BlockID, data, length, null).flatMap(FluxUtil::toMono);
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
     * {@codesnippet com.azure.storage.blob.specialized.BlockBlobAsyncClient.stageBlockWithResponse#String-Flux-long-LeaseAccessConditions}
     *
     * @param base64BlockID A Base64 encoded {@code String} that specifies the ID for this block. Note that all block
     * ids for a given blob must be the same length.
     * @param data The data to write to the block. Note that this {@code Flux} must be replayable if retries are enabled
     * (the default). In other words, the Flux must produce the same data each time it is subscribed to.
     * @param length The exact length of the data. It is important that this value match precisely the length of the
     * data emitted by the {@code Flux}.
     * @param leaseAccessConditions By setting lease access conditions, requests will fail if the provided lease does
     * not match the active lease on the blob.
     * @return A reactive response signalling completion.
     */
    public Mono<Response<Void>> stageBlockWithResponse(String base64BlockID, Flux<ByteBuffer> data, long length,
        LeaseAccessConditions leaseAccessConditions) {
        return withContext(context -> stageBlockWithResponse(base64BlockID, data, length, leaseAccessConditions,
            context));
    }

    Mono<Response<Void>> stageBlockWithResponse(String base64BlockID, Flux<ByteBuffer> data, long length,
        LeaseAccessConditions leaseAccessConditions, Context context) {
        return this.azureBlobStorage.blockBlobs().stageBlockWithRestResponseAsync(null, null,
            base64BlockID, length, data, null, null, null, null, leaseAccessConditions, getCustomerProvidedKey(),
            context).map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Creates a new block to be committed as part of a blob where the contents are read from a URL. For more
     * information, see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/put-block-from-url">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlockBlobAsyncClient.stageBlockFromURL#String-String-BlobRange}
     *
     * @param base64BlockID A Base64 encoded {@code String} that specifies the ID for this block. Note that all block
     * ids for a given blob must be the same length.
     * @param sourceUrl The url to the blob that will be the source of the copy.  A source blob in the same storage
     * account can be authenticated via Shared Key. However, if the source is a blob in another account, the source blob
     * must either be public or must be authenticated via a shared access signature. If the source blob is public, no
     * authentication is required to perform the operation.
     * @param sourceRange {@link BlobRange}
     * @return A reactive response signalling completion.
     */
    public Mono<Void> stageBlockFromURL(String base64BlockID, String sourceUrl, BlobRange sourceRange) {
        return this.stageBlockFromURLWithResponse(base64BlockID, sourceUrl, sourceRange, null, null, null)
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a new block to be committed as part of a blob where the contents are read from a URL. For more
     * information, see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/put-block-from-url">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlockBlobAsyncClient.stageBlockFromURLWithResponse#String-String-BlobRange-byte-LeaseAccessConditions-SourceModifiedAccessConditions}
     *
     * @param base64BlockID A Base64 encoded {@code String} that specifies the ID for this block. Note that all block
     * ids for a given blob must be the same length.
     * @param sourceUrl The url to the blob that will be the source of the copy.  A source blob in the same storage
     * account can be authenticated via Shared Key. However, if the source is a blob in another account, the source blob
     * must either be public or must be authenticated via a shared access signature. If the source blob is public, no
     * authentication is required to perform the operation.
     * @param sourceRange {@link BlobRange}
     * @param sourceContentMD5 An MD5 hash of the block content from the source blob. If specified, the service will
     * calculate the MD5 of the received data and fail the request if it does not match the provided MD5.
     * @param leaseAccessConditions By setting lease access conditions, requests will fail if the provided lease does
     * not match the active lease on the blob.
     * @param sourceModifiedAccessConditions {@link SourceModifiedAccessConditions}
     * @return A reactive response signalling completion.
     */
    public Mono<Response<Void>> stageBlockFromURLWithResponse(String base64BlockID, String sourceUrl,
        BlobRange sourceRange, byte[] sourceContentMD5, LeaseAccessConditions leaseAccessConditions,
        SourceModifiedAccessConditions sourceModifiedAccessConditions) {
        return withContext(context -> stageBlockFromURLWithResponse(base64BlockID, sourceUrl, sourceRange,
            sourceContentMD5, leaseAccessConditions, sourceModifiedAccessConditions));
    }

    Mono<Response<Void>> stageBlockFromURLWithResponse(String base64BlockID, String sourceUrl,
        BlobRange sourceRange, byte[] sourceContentMD5, LeaseAccessConditions leaseAccessConditions,
        SourceModifiedAccessConditions sourceModifiedAccessConditions, Context context) {
        sourceRange = sourceRange == null ? new BlobRange(0) : sourceRange;

        URL url;
        try {
            url = new URL(sourceUrl);
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'sourceUrl' is not a valid url."));
        }

        return this.azureBlobStorage.blockBlobs().stageBlockFromURLWithRestResponseAsync(null, null,
                base64BlockID, 0, url, sourceRange.toHeaderValue(), sourceContentMD5, null, null,
                null, getCustomerProvidedKey(), leaseAccessConditions, sourceModifiedAccessConditions, context)
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
     * @return A reactive response containing the list of blocks.
     */
    public Mono<BlockList> listBlocks(BlockListType listType) {
        return this.listBlocksWithResponse(listType, null).map(Response::getValue);
    }

    /**
     * Returns the list of blocks that have been uploaded as part of a block blob using the specified block list filter.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-block-list">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlockBlobAsyncClient.listBlocksWithResponse#BlockListType-LeaseAccessConditions}
     *
     * @param listType Specifies which type of blocks to return.
     * @param leaseAccessConditions By setting lease access conditions, requests will fail if the provided lease does
     * not match the active lease on the blob.
     * @return A reactive response containing the list of blocks.
     */
    public Mono<Response<BlockList>> listBlocksWithResponse(BlockListType listType,
        LeaseAccessConditions leaseAccessConditions) {

        return withContext(context -> listBlocksWithResponse(listType, leaseAccessConditions, context));
    }

    Mono<Response<BlockList>> listBlocksWithResponse(BlockListType listType,
        LeaseAccessConditions leaseAccessConditions, Context context) {

        return this.azureBlobStorage.blockBlobs().getBlockListWithRestResponseAsync(null,
            null, listType, getSnapshotId(), null, null, leaseAccessConditions, context)
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
     * @param base64BlockIDs A list of base64 encode {@code String}s that specifies the block IDs to be committed.
     * @return A reactive response containing the information of the block blob.
     */
    public Mono<BlockBlobItem> commitBlockList(List<String> base64BlockIDs) {
        return commitBlockListWithResponse(base64BlockIDs, null, null, null, null).flatMap(FluxUtil::toMono);
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
     * {@codesnippet com.azure.storage.blob.specialized.BlockBlobAsyncClient.commitBlockListWithResponse#List-BlobHttpHeaders-Map-AccessTier-BlobAccessConditions}
     *
     * @param base64BlockIDs A list of base64 encode {@code String}s that specifies the block IDs to be committed.
     * @param headers {@link BlobHttpHeaders}
     * @param metadata Metadata to associate with the blob.
     * @param tier {@link AccessTier} for the destination blob.
     * @param accessConditions {@link BlobAccessConditions}
     * @return A reactive response containing the information of the block blob.
     */
    public Mono<Response<BlockBlobItem>> commitBlockListWithResponse(List<String> base64BlockIDs,
        BlobHttpHeaders headers, Map<String, String> metadata, AccessTier tier, BlobAccessConditions accessConditions) {
        return withContext(context -> commitBlockListWithResponse(base64BlockIDs, headers, metadata, tier,
            accessConditions, context));
    }

    Mono<Response<BlockBlobItem>> commitBlockListWithResponse(List<String> base64BlockIDs,
        BlobHttpHeaders headers, Map<String, String> metadata, AccessTier tier, BlobAccessConditions accessConditions,
        Context context) {
        accessConditions = accessConditions == null ? new BlobAccessConditions() : accessConditions;

        return this.azureBlobStorage.blockBlobs().commitBlockListWithRestResponseAsync(
            null, null, new BlockLookupList().setLatest(base64BlockIDs), null, null, null, metadata, tier, null,
            headers, accessConditions.getLeaseAccessConditions(), getCustomerProvidedKey(),
            accessConditions.getModifiedAccessConditions(), context)
            .map(rb -> new SimpleResponse<>(rb, new BlockBlobItem(rb.getDeserializedHeaders())));
    }
}
