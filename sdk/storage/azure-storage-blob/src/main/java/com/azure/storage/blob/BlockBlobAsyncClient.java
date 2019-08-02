// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.implementation.util.FluxUtil;
import com.azure.core.util.Context;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.BlobHTTPHeaders;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlockBlobItem;
import com.azure.storage.blob.models.BlockItem;
import com.azure.storage.blob.models.BlockListType;
import com.azure.storage.blob.models.BlockLookupList;
import com.azure.storage.blob.models.LeaseAccessConditions;
import com.azure.storage.blob.models.Metadata;
import com.azure.storage.blob.models.SourceModifiedAccessConditions;
import io.netty.buffer.ByteBuf;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import static com.azure.storage.blob.Utility.postProcessResponse;

/**
 * Client to a block blob. It may only be instantiated through a {@link BlobClientBuilder}, via
 * the method {@link BlobAsyncClient#asBlockBlobAsyncClient()}, or via the method
 * {@link ContainerAsyncClient#getBlockBlobAsyncClient(String)}. This class does not hold
 * any state about a particular blob, but is instead a convenient way of sending appropriate
 * requests to the resource on the service.
 *
 * <p>
 * This client contains operations on a blob. Operations on a container are available on {@link ContainerAsyncClient},
 * and operations on the service are available on {@link BlobServiceAsyncClient}.
 *
 * <p>
 * Please refer
 * to the <a href=https://docs.microsoft.com/en-us/rest/api/storageservices/understanding-block-blobs--append-blobs--and-page-blobs>Azure Docs</a>
 * for more information.
 *
 * <p>
 * Note this client is an async client that returns reactive responses from Spring Reactor Core
 * project (https://projectreactor.io/). Calling the methods in this client will <strong>NOT</strong>
 * start the actual network operation, until {@code .subscribe()} is called on the reactive response.
 * You can simply convert one of these responses to a {@link java.util.concurrent.CompletableFuture}
 * object through {@link Mono#toFuture()}.
 */
public final class BlockBlobAsyncClient extends BlobAsyncClient {
    static final int BLOB_DEFAULT_UPLOAD_BLOCK_SIZE = 4 * Constants.MB;
    static final int BLOB_MAX_UPLOAD_BLOCK_SIZE = 100 * Constants.MB;

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
     * Package-private constructor for use by {@link BlobClientBuilder}.
     * @param azureBlobStorage the API client for blob storage
     */
    BlockBlobAsyncClient(AzureBlobStorageImpl azureBlobStorage, String snapshot) {
        super(azureBlobStorage, snapshot);
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     * Updating an existing block blob overwrites any existing metadata on the blob. Partial updates are not
     * supported with PutBlob; the content of the existing blob is overwritten with the new content. To
     * perform a partial update of a block blob's, use PutBlock and PutBlockList.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob">Azure Docs</a>.
     * <p>
     * Note that the data passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flux} must produce the same data each time it is subscribed to.
     * <p>
     *
     * @param data
     *         The data to write to the blob. Note that this {@code Flux} must be replayable if retries are enabled
     *         (the default). In other words, the Flux must produce the same data each time it is subscribed to.
     * @param length
     *         The exact length of the data. It is important that this value match precisely the length of the data
     *         emitted by the {@code Flux}.
     *
     * @return
     *      A reactive response containing the information of the uploaded block blob.
     */
    public Mono<Response<BlockBlobItem>> upload(Flux<ByteBuf> data, long length) {
        return this.upload(data, length, null, null, null);
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     * Updating an existing block blob overwrites any existing metadata on the blob. Partial updates are not
     * supported with PutBlob; the content of the existing blob is overwritten with the new content. To
     * perform a partial update of a block blob's, use PutBlock and PutBlockList.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob">Azure Docs</a>.
     * <p>
     * Note that the data passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flux} must produce the same data each time it is subscribed to.
     * <p>
     *
     * @param data
     *         The data to write to the blob. Note that this {@code Flux} must be replayable if retries are enabled
     *         (the default). In other words, the Flux must produce the same data each time it is subscribed to.
     * @param length
     *         The exact length of the data. It is important that this value match precisely the length of the data
     *         emitted by the {@code Flux}.
     * @param headers
     *         {@link BlobHTTPHeaders}
     * @param metadata
     *         {@link Metadata}
     * @param accessConditions
     *         {@link BlobAccessConditions}
     *
     * @return
     *      A reactive response containing the information of the uploaded block blob.
     */
    public Mono<Response<BlockBlobItem>> upload(Flux<ByteBuf> data, long length, BlobHTTPHeaders headers,
            Metadata metadata, BlobAccessConditions accessConditions) {
        metadata = metadata == null ? new Metadata() : metadata;
        accessConditions = accessConditions == null ? new BlobAccessConditions() : accessConditions;

        return postProcessResponse(this.azureBlobStorage.blockBlobs().uploadWithRestResponseAsync(null,
            null, data, length, null, metadata, null, null,
            null, null, headers, accessConditions.leaseAccessConditions(),
            accessConditions.modifiedAccessConditions(), Context.NONE))
            .map(rb -> new SimpleResponse<>(rb, new BlockBlobItem(rb.deserializedHeaders())));
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob, with the content of the specified file.
     *
     * @param filePath Path to the upload file
     * @return An empty response
     */
    public Mono<Void> uploadFromFile(String filePath) {
        return this.uploadFromFile(filePath, BLOB_DEFAULT_UPLOAD_BLOCK_SIZE, null, null, null);
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob, with the content of the specified file.
     *
     * @param filePath Path to the upload file
     * @param blockSize Size of the blocks to upload
     * @param headers {@link BlobHTTPHeaders}
     * @param metadata {@link Metadata}
     * @param accessConditions {@link BlobAccessConditions}
     * @return An empty response
     * @throws IllegalArgumentException If {@code blockSize} is less than 0 or greater than 100MB
     * @throws UncheckedIOException If an I/O error occurs
     */
    public Mono<Void> uploadFromFile(String filePath, Integer blockSize, BlobHTTPHeaders headers, Metadata metadata,
                                     BlobAccessConditions accessConditions) {
        if (blockSize < 0 || blockSize > BLOB_MAX_UPLOAD_BLOCK_SIZE) {
            throw new IllegalArgumentException("Block size should not exceed 100MB");
        }

        return Mono.using(() -> uploadFileResourceSupplier(filePath),
            channel -> {
                final SortedMap<Long, String> blockIds = new TreeMap<>();
                return Flux.fromIterable(sliceFile(filePath, blockSize))
                    .doOnNext(chunk -> blockIds.put(chunk.offset(), getBlockID()))
                    .flatMap(chunk -> {
                        String blockId = blockIds.get(chunk.offset());
                        return stageBlock(blockId, FluxUtil.byteBufStreamFromFile(channel, chunk.offset(), chunk.count()), chunk.count(), null);
                    })
                    .then(Mono.defer(() -> commitBlockList(new ArrayList<>(blockIds.values()), headers, metadata, accessConditions)))
                    .then()
                    .doOnTerminate(() -> {
                        try {
                            channel.close();
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
            }, this::uploadFileCleanup);
    }

    private AsynchronousFileChannel uploadFileResourceSupplier(String filePath) {
        try {
            return AsynchronousFileChannel.open(Paths.get(filePath), StandardOpenOption.READ);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void uploadFileCleanup(AsynchronousFileChannel channel) {
        try {
            channel.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String getBlockID() {
        return Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
    }

    private List<BlobRange> sliceFile(String path, Integer blockSize) {
        if (blockSize == null) {
            blockSize = BLOB_DEFAULT_UPLOAD_BLOCK_SIZE;
        }
        File file = new File(path);
        assert file.exists();
        List<BlobRange> ranges = new ArrayList<>();
        for (long pos = 0; pos < file.length(); pos += blockSize) {
            long count = blockSize;
            if (pos + count > file.length()) {
                count = file.length() - pos;
            }
            ranges.add(new BlobRange(pos, count));
        }
        return ranges;
    }

    /**
     * Uploads the specified block to the block blob's "staging area" to be later committed by a call to
     * commitBlockList. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block">Azure Docs</a>.
     * <p>
     * Note that the data passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flux} must produce the same data each time it is subscribed to.
     *
     * @param base64BlockID
     *         A Base64 encoded {@code String} that specifies the ID for this block. Note that all block ids for a given
     *         blob must be the same length.
     * @param data
     *         The data to write to the block. Note that this {@code Flux} must be replayable if retries are enabled
     *         (the default). In other words, the Flux must produce the same data each time it is subscribed to.
     * @param length
     *         The exact length of the data. It is important that this value match precisely the length of the data
     *         emitted by the {@code Flux}.
     *
     * @return
     *      A reactive response signalling completion.
     */
    public Mono<VoidResponse> stageBlock(String base64BlockID, Flux<ByteBuf> data,
                                                         long length) {
        return this.stageBlock(base64BlockID, data, length, null);
    }

    /**
     * Uploads the specified block to the block blob's "staging area" to be later committed by a call to
     * commitBlockList. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block">Azure Docs</a>.
     * <p>
     * Note that the data passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flux} must produce the same data each time it is subscribed to.
     *
     * @param base64BlockID
     *         A Base64 encoded {@code String} that specifies the ID for this block. Note that all block ids for a given
     *         blob must be the same length.
     * @param data
     *         The data to write to the block. Note that this {@code Flux} must be replayable if retries are enabled
     *         (the default). In other words, the Flux must produce the same data each time it is subscribed to.
     * @param length
     *         The exact length of the data. It is important that this value match precisely the length of the data
     *         emitted by the {@code Flux}.
     * @param leaseAccessConditions
     *         By setting lease access conditions, requests will fail if the provided lease does not match the active
     *         lease on the blob.
     *
     * @return
     *      A reactive response signalling completion.
     */
    public Mono<VoidResponse> stageBlock(String base64BlockID, Flux<ByteBuf> data, long length,
                 LeaseAccessConditions leaseAccessConditions) {
        return postProcessResponse(this.azureBlobStorage.blockBlobs().stageBlockWithRestResponseAsync(null,
            null, base64BlockID, length, data, null, null, null,
            null, null, null, leaseAccessConditions, Context.NONE))
            .map(VoidResponse::new);
    }

    /**
     * Creates a new block to be committed as part of a blob where the contents are read from a URL. For more
     * information, see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/put-block-from-url">Azure Docs</a>.
     *
     * @param base64BlockID
     *         A Base64 encoded {@code String} that specifies the ID for this block. Note that all block ids for a given
     *         blob must be the same length.
     * @param sourceURL
     *         The url to the blob that will be the source of the copy.  A source blob in the same storage account can be
     *         authenticated via Shared Key. However, if the source is a blob in another account, the source blob must
     *         either be public or must be authenticated via a shared access signature. If the source blob is public, no
     *         authentication is required to perform the operation.
     * @param sourceRange
     *         {@link BlobRange}
     *
     * @return
     *      A reactive response signalling completion.
     */
    public Mono<VoidResponse> stageBlockFromURL(String base64BlockID, URL sourceURL,
            BlobRange sourceRange) {
        return this.stageBlockFromURL(base64BlockID, sourceURL, sourceRange, null,
                null, null);
    }

    /**
     * Creates a new block to be committed as part of a blob where the contents are read from a URL. For more
     * information, see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/put-block-from-url">Azure Docs</a>.
     *
     * @param base64BlockID
     *         A Base64 encoded {@code String} that specifies the ID for this block. Note that all block ids for a given
     *         blob must be the same length.
     * @param sourceURL
     *         The url to the blob that will be the source of the copy.  A source blob in the same storage account can
     *         be authenticated via Shared Key. However, if the source is a blob in another account, the source blob
     *         must either be public or must be authenticated via a shared access signature. If the source blob is
     *         public, no authentication is required to perform the operation.
     * @param sourceRange
     *         {@link BlobRange}
     * @param sourceContentMD5
     *         An MD5 hash of the block content from the source blob. If specified, the service will calculate the MD5
     *         of the received data and fail the request if it does not match the provided MD5.
     * @param leaseAccessConditions
     *         By setting lease access conditions, requests will fail if the provided lease does not match the active
     *         lease on the blob.
     * @param sourceModifiedAccessConditions
     *         {@link SourceModifiedAccessConditions}
     *
     * @return
     *      A reactive response signalling completion.
     */
    public Mono<VoidResponse> stageBlockFromURL(String base64BlockID, URL sourceURL,
            BlobRange sourceRange, byte[] sourceContentMD5, LeaseAccessConditions leaseAccessConditions,
            SourceModifiedAccessConditions sourceModifiedAccessConditions) {
        sourceRange = sourceRange == null ? new BlobRange(0) : sourceRange;

        return postProcessResponse(
            this.azureBlobStorage.blockBlobs().stageBlockFromURLWithRestResponseAsync(null, null,
                base64BlockID, 0, sourceURL, sourceRange.toHeaderValue(), sourceContentMD5, null,
                null, null, null, null,
                leaseAccessConditions, sourceModifiedAccessConditions, Context.NONE))
            .map(VoidResponse::new);
    }

    /**
     * Returns the list of blocks that have been uploaded as part of a block blob using the specified block list filter.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-block-list">Azure Docs</a>.
     *
     * @param listType
     *         Specifies which type of blocks to return.
     *
     * @return
     *      A reactive response containing the list of blocks.
     */
    public Flux<BlockItem> listBlocks(BlockListType listType) {
        return this.listBlocks(listType, null);
    }

    /**
     *
     * Returns the list of blocks that have been uploaded as part of a block blob using the specified block list filter.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-block-list">Azure Docs</a>.
     *
     * @param listType
     *         Specifies which type of blocks to return.
     * @param leaseAccessConditions
     *         By setting lease access conditions, requests will fail if the provided lease does not match the active
     *         lease on the blob.
     *
     * @return
     *      A reactive response containing the list of blocks.
     */
    public Flux<BlockItem> listBlocks(BlockListType listType,
                                      LeaseAccessConditions leaseAccessConditions) {
        return postProcessResponse(this.azureBlobStorage.blockBlobs().getBlockListWithRestResponseAsync(
            null, null, listType, snapshot, null, null, null,
            leaseAccessConditions, Context.NONE))
            .map(ResponseBase::value)
            .flatMapMany(bl -> {
                Flux<BlockItem> committed = Flux.fromIterable(bl.committedBlocks())
                    .map(block -> new BlockItem(block, true));
                Flux<BlockItem> uncommitted = Flux.fromIterable(bl.uncommittedBlocks())
                    .map(block -> new BlockItem(block, false));
                return Flux.concat(committed, uncommitted);
            });
    }

    /**
     * Writes a blob by specifying the list of block IDs that are to make up the blob.
     * In order to be written as part of a blob, a block must have been successfully written
     * to the server in a prior stageBlock operation. You can call commitBlockList to update a blob
     * by uploading only those blocks that have changed, then committing the new and existing
     * blocks together. Any blocks not specified in the block list and permanently deleted.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block-list">Azure Docs</a>.
     *
     * @param base64BlockIDs
     *         A list of base64 encode {@code String}s that specifies the block IDs to be committed.
     *
     * @return
     *      A reactive response containing the information of the block blob.
     */
    public Mono<Response<BlockBlobItem>> commitBlockList(List<String> base64BlockIDs) {
        return this.commitBlockList(base64BlockIDs, null, null, null);
    }

    /**
     * Writes a blob by specifying the list of block IDs that are to make up the blob.
     * In order to be written as part of a blob, a block must have been successfully written
     * to the server in a prior stageBlock operation. You can call commitBlockList to update a blob
     * by uploading only those blocks that have changed, then committing the new and existing
     * blocks together. Any blocks not specified in the block list and permanently deleted.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block-list">Azure Docs</a>.
     *
     * @param base64BlockIDs
     *         A list of base64 encode {@code String}s that specifies the block IDs to be committed.
     * @param headers
     *         {@link BlobHTTPHeaders}
     * @param metadata
     *         {@link Metadata}
     * @param accessConditions
     *         {@link BlobAccessConditions}
     *
     * @return
     *      A reactive response containing the information of the block blob.
     */
    public Mono<Response<BlockBlobItem>> commitBlockList(List<String> base64BlockIDs,
                                              BlobHTTPHeaders headers, Metadata metadata, BlobAccessConditions accessConditions) {
        metadata = metadata == null ? new Metadata() : metadata;
        accessConditions = accessConditions == null ? new BlobAccessConditions() : accessConditions;

        return postProcessResponse(this.azureBlobStorage.blockBlobs().commitBlockListWithRestResponseAsync(
            null, null, new BlockLookupList().latest(base64BlockIDs), null, metadata,
            null, null, null, null, headers,
            accessConditions.leaseAccessConditions(), accessConditions.modifiedAccessConditions(), Context.NONE))
            .map(rb -> new SimpleResponse<>(rb, new BlockBlobItem(rb.deserializedHeaders())));
    }
}
