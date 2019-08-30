// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.util.Context;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.BlobHTTPHeaders;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlockBlobItem;
import com.azure.storage.blob.models.BlockList;
import com.azure.storage.blob.models.BlockListType;
import com.azure.storage.blob.models.LeaseAccessConditions;
import com.azure.storage.blob.models.Metadata;
import com.azure.storage.blob.models.SourceModifiedAccessConditions;
import com.azure.storage.blob.models.StorageException;
import com.azure.storage.common.Utility;
import java.util.Objects;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;

/**
 * Client to a block blob. It may only be instantiated through a {@link BlobClientBuilder}, via
 * the method {@link BlobClient#asBlockBlobClient()}, or via the method
 * {@link ContainerClient#getBlockBlobClient(String)}. This class does not hold
 * any state about a particular blob, but is instead a convenient way of sending appropriate
 * requests to the resource on the service.
 *
 * <p>
 * This client contains operations on a blob. Operations on a container are available on {@link ContainerClient},
 * and operations on the service are available on {@link BlobServiceClient}.
 *
 * <p>
 * Please refer to the <a href=https://docs.microsoft.com/en-us/rest/api/storageservices/understanding-block-blobs--append-blobs--and-page-blobs>Azure Docs</a>
 * for more information.
 */
public final class BlockBlobClient extends BlobClient {
    private final BlockBlobAsyncClient blockBlobAsyncClient;

    /**
     * Indicates the maximum number of bytes that can be sent in a call to upload.
     */
    public static final int MAX_UPLOAD_BLOB_BYTES = BlockBlobAsyncClient.MAX_UPLOAD_BLOB_BYTES;

    /**
     * Indicates the maximum number of bytes that can be sent in a call to stageBlock.
     */
    public static final int MAX_STAGE_BLOCK_BYTES = BlockBlobAsyncClient.MAX_STAGE_BLOCK_BYTES;

    /**
     * Indicates the maximum number of blocks allowed in a block blob.
     */
    public static final int MAX_BLOCKS = BlockBlobAsyncClient.MAX_BLOCKS;

    /**
     * Package-private constructor for use by {@link BlobClientBuilder}.
     * @param blockBlobAsyncClient the async block blob client
     */
    BlockBlobClient(BlockBlobAsyncClient blockBlobAsyncClient) {
        super(blockBlobAsyncClient);
        this.blockBlobAsyncClient = blockBlobAsyncClient;
    }

    /**
     * Creates and opens an output stream to write data to the block blob. If the blob already exists on the service,
     * it will be overwritten.
     * @return A {@link BlobOutputStream} object used to write data to the blob.
     *
     * @throws StorageException If a storage service error occurred.
     */
    public BlobOutputStream getBlobOutputStream() {
        return getBlobOutputStream(null);
    }

    /**
     * Creates and opens an output stream to write data to the block blob. If the blob already exists on the service,
     * it will be overwritten.
     *
     * @param accessConditions A {@link BlobAccessConditions} object that represents the access conditions for the blob.
     *
     * @return A {@link BlobOutputStream} object used to write data to the blob.
     *
     * @throws StorageException If a storage service error occurred.
     */
    public BlobOutputStream getBlobOutputStream(BlobAccessConditions accessConditions) {
        return BlobOutputStream.blockBlobOutputStream(blockBlobAsyncClient, accessConditions);
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     * Updating an existing block blob overwrites any existing metadata on the blob. Partial updates are not
     * supported with PutBlob; the content of the existing blob is overwritten with the new content. To
     * perform a partial update of a block blob's, use PutBlock and PutBlockList.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob">Azure Docs</a>.
     *
     * @param data The data to write to the blob.
     * @param length The exact length of the data. It is important that this value match precisely the length of the data
     *         provided in the {@link InputStream}.
     *
     * @return The information of the uploaded block blob.
     * @throws IOException If an I/O error occurs
     */
    public BlockBlobItem upload(InputStream data, long length) throws IOException {
        return uploadWithResponse(data, length, null, null, null, null, Context.NONE).value();
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     * Updating an existing block blob overwrites any existing metadata on the blob. Partial updates are not
     * supported with PutBlob; the content of the existing blob is overwritten with the new content. To
     * perform a partial update of a block blob's, use PutBlock and PutBlockList.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob">Azure Docs</a>.
     *
     * @param data The data to write to the blob.
     * @param length The exact length of the data. It is important that this value match precisely the length of the data
     *         provided in the {@link InputStream}.
     * @param headers {@link BlobHTTPHeaders}
     * @param metadata {@link Metadata}
     * @param accessConditions {@link BlobAccessConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return The information of the uploaded block blob.
     * @throws IOException If an I/O error occurs
     */
    public Response<BlockBlobItem> uploadWithResponse(InputStream data, long length, BlobHTTPHeaders headers,
        Metadata metadata, BlobAccessConditions accessConditions, Duration timeout, Context context) throws IOException {
        Flux<ByteBuffer> fbb = Utility.convertStreamToByteBuffer(data, length, BlockBlobAsyncClient.BLOB_DEFAULT_UPLOAD_BLOCK_SIZE);
        Mono<Response<BlockBlobItem>> upload = blockBlobAsyncClient
            .uploadWithResponse(fbb.subscribeOn(Schedulers.elastic()), length, headers, metadata, accessConditions, context);

        try {
            return Utility.blockWithOptionalTimeout(upload, timeout);
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     * @param filePath Path of the file to upload
     *
     * @throws IOException If an I/O error occurs
     */
    public void uploadFromFile(String filePath) throws IOException {
        uploadFromFile(filePath, null, null, null, null);
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     * @param filePath Path of the file to upload
     * @param headers {@link BlobHTTPHeaders}
     * @param metadata {@link Metadata}
     * @param accessConditions {@link BlobAccessConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     *
     * @throws IOException If an I/O error occurs
     */
    public void uploadFromFile(String filePath, BlobHTTPHeaders headers, Metadata metadata,
                               BlobAccessConditions accessConditions, Duration timeout) throws IOException {
        Mono<Void> upload = this.blockBlobAsyncClient.uploadFromFile(filePath, BlockBlobAsyncClient.BLOB_DEFAULT_UPLOAD_BLOCK_SIZE, headers, metadata, accessConditions);

        try {
            Utility.blockWithOptionalTimeout(upload, timeout);
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    /**
     * Uploads the specified block to the block blob's "staging area" to be later committed by a call to
     * commitBlockList. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block">Azure Docs</a>.
     *
     * @param base64BlockID A Base64 encoded {@code String} that specifies the ID for this block. Note that all block ids for a given
     *         blob must be the same length.
     * @param data The data to write to the block.
     * @param length The exact length of the data. It is important that this value match precisely the length of the data
     *         provided in the {@link InputStream}.
     */
    public void stageBlock(String base64BlockID, InputStream data, long length) {
        stageBlockWithResponse(base64BlockID, data, length, null, null, Context.NONE);
    }

    /**
     * Uploads the specified block to the block blob's "staging area" to be later committed by a call to
     * commitBlockList. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block">Azure Docs</a>.
     *
     * @param base64BlockID A Base64 encoded {@code String} that specifies the ID for this block. Note that all block ids for a given
     *         blob must be the same length.
     * @param data The data to write to the block.
     * @param length The exact length of the data. It is important that this value match precisely the length of the data
     *         provided in the {@link InputStream}.
     * @param leaseAccessConditions By setting lease access conditions, requests will fail if the provided lease does not match the active
     *         lease on the blob.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A response containing status code and HTTP headers
     */
    public VoidResponse stageBlockWithResponse(String base64BlockID, InputStream data, long length,
        LeaseAccessConditions leaseAccessConditions, Duration timeout, Context context) {
        Objects.requireNonNull(data);
        Flux<ByteBuffer> fbb = Utility.convertStreamToByteBuffer(data, length, BlockBlobAsyncClient.BLOB_DEFAULT_UPLOAD_BLOCK_SIZE);

        Mono<VoidResponse> response = blockBlobAsyncClient.stageBlockWithResponse(base64BlockID,
            fbb.subscribeOn(Schedulers.elastic()), length, leaseAccessConditions, context);
        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Creates a new block to be committed as part of a blob where the contents are read from a URL. For more
     * information, see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/put-block-from-url">Azure Docs</a>.
     *
     * @param base64BlockID A Base64 encoded {@code String} that specifies the ID for this block. Note that all block ids for a given
     *         blob must be the same length.
     * @param sourceURL The url to the blob that will be the source of the copy.  A source blob in the same storage account can be
     *         authenticated via Shared Key. However, if the source is a blob in another account, the source blob must
     *         either be public or must be authenticated via a shared access signature. If the source blob is public, no
     *         authentication is required to perform the operation.
     * @param sourceRange {@link BlobRange}
     */
    public void stageBlockFromURL(String base64BlockID, URL sourceURL, BlobRange sourceRange) {
        stageBlockFromURLWithResponse(base64BlockID, sourceURL, sourceRange, null, null, null, null, Context.NONE);
    }

    /**
     * Creates a new block to be committed as part of a blob where the contents are read from a URL. For more
     * information, see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/put-block-from-url">Azure Docs</a>.
     *
     * @param base64BlockID A Base64 encoded {@code String} that specifies the ID for this block. Note that all block ids for a given
     *         blob must be the same length.
     * @param sourceURL The url to the blob that will be the source of the copy.  A source blob in the same storage account can
     *         be authenticated via Shared Key. However, if the source is a blob in another account, the source blob
     *         must either be public or must be authenticated via a shared access signature. If the source blob is
     *         public, no authentication is required to perform the operation.
     * @param sourceRange {@link BlobRange}
     * @param sourceContentMD5 An MD5 hash of the block content from the source blob. If specified, the service will calculate the MD5
     *         of the received data and fail the request if it does not match the provided MD5.
     * @param leaseAccessConditions By setting lease access conditions, requests will fail if the provided lease does not match the active
     *         lease on the blob.
     * @param sourceModifiedAccessConditions {@link SourceModifiedAccessConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A response containing status code and HTTP headers
     */
    public VoidResponse stageBlockFromURLWithResponse(String base64BlockID, URL sourceURL, BlobRange sourceRange,
        byte[] sourceContentMD5, LeaseAccessConditions leaseAccessConditions,
        SourceModifiedAccessConditions sourceModifiedAccessConditions, Duration timeout, Context context) {
        Mono<VoidResponse> response = blockBlobAsyncClient.stageBlockFromURLWithResponse(base64BlockID, sourceURL,
            sourceRange, sourceContentMD5, leaseAccessConditions, sourceModifiedAccessConditions, context);
        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Returns the list of blocks that have been uploaded as part of a block blob using the specified block list filter.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-block-list">Azure Docs</a>.
     *
     * @param listType Specifies which type of blocks to return.
     *
     * @return The list of blocks.
     */
    public BlockList listBlocks(BlockListType listType) {
        return this.listBlocksWithResponse(listType, null, null).value();
    }

    /**
     * Returns the list of blocks that have been uploaded as part of a block blob using the specified block list filter.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-block-list">Azure Docs</a>.
     *
     * @param listType Specifies which type of blocks to return.
     * @param leaseAccessConditions By setting lease access conditions, requests will fail if the provided lease does not match the active
     *         lease on the blob.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     *
     * @return The list of blocks.
     */
    public Response<BlockList> listBlocksWithResponse(BlockListType listType,
                                          LeaseAccessConditions leaseAccessConditions, Duration timeout) {
        Mono<Response<BlockList>> response = blockBlobAsyncClient.listBlocks(listType, leaseAccessConditions);

        return Utility.blockWithOptionalTimeout(response, timeout);
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
     * @param base64BlockIDs A list of base64 encode {@code String}s that specifies the block IDs to be committed.
     *
     * @return The information of the block blob.
     */
    public BlockBlobItem commitBlockList(List<String> base64BlockIDs) {
        return commitBlockListWithResponse(base64BlockIDs, null, null, null, null, Context.NONE).value();
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
     * @param base64BlockIDs A list of base64 encode {@code String}s that specifies the block IDs to be committed.
     * @param headers {@link BlobHTTPHeaders}
     * @param metadata {@link Metadata}
     * @param accessConditions {@link BlobAccessConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return The information of the block blob.
     */
    public Response<BlockBlobItem> commitBlockListWithResponse(List<String> base64BlockIDs,
            BlobHTTPHeaders headers, Metadata metadata, BlobAccessConditions accessConditions, Duration timeout,
            Context context) {
        Mono<Response<BlockBlobItem>> response = blockBlobAsyncClient.commitBlockListWithResponse(
            base64BlockIDs, headers, metadata, accessConditions, context);

        return Utility.blockWithOptionalTimeout(response, timeout);
    }
}
