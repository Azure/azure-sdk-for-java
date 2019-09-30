// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.cryptography;

import com.azure.core.exception.UnexpectedLengthException;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.BlockBlobClient;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.BlobHTTPHeaders;
import com.azure.storage.blob.models.BlockBlobItem;
import com.azure.storage.blob.models.Metadata;
import com.azure.storage.common.Utility;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Objects;

public class EncryptedBlockBlobClient extends BlobClient {

    private final EncryptedBlockBlobAsyncClient encryptedBlockBlobAsyncClient;

    /**
     * Package-private constructor for use by {@link BlobClientBuilder}.
     */
    EncryptedBlockBlobClient(EncryptedBlockBlobAsyncClient encryptedBlockBlobAsyncClient) {
        super(encryptedBlockBlobAsyncClient);
        this.encryptedBlockBlobAsyncClient = encryptedBlockBlobAsyncClient;
    }

    public BlockBlobClient getBlockBlobClient() {
        return new BlobClientBuilder()
            .pipeline(EncryptedBlockBlobAsyncClient.removeDecryptionPolicy(getHttpPipeline(),
                getHttpPipeline().getHttpClient()))
            .endpoint(getBlobUrl().toString())
            .buildBlockBlobClient();
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob. Updating an existing block blob
     * overwrites any existing metadata on the blob. Partial updates are not supported with PutBlob; the content of the
     * existing blob is overwritten with the new content. To perform a partial update of a block blob's, use PutBlock
     * and PutBlockList on a regular BlockBlob client. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.cryptography.EncryptedBlockBlobClient.upload#InputStream-long}
     *
     * @param data The data to write to the blob.
     * @param length The exact length of the data. It is important that this value match precisely the length of the
     * data provided in the {@link InputStream}.
     * @return The information of the uploaded block blob.
     * @throws IOException If an I/O error occurs
     */
    public BlockBlobItem upload(InputStream data, long length) throws IOException {
        return uploadWithResponse(data, length, null, null, null, null, null, Context.NONE).getValue();
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob. Updating an existing block blob
     * overwrites any existing metadata on the blob. Partial updates are not supported with PutBlob; the content of the
     * existing blob is overwritten with the new content. To perform a partial update of a block blob's, use PutBlock
     * and PutBlockList. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.cryptography.EncryptedBlockBlobClient.uploadWithResponse#InputStream-long-BlobHTTPHeaders-Metadata-AccessTier-BlobAccessConditions-Duration-Context}
     *
     * @param data The data to write to the blob.
     * @param length The exact length of the data. It is important that this value match precisely the length of the
     * data provided in the {@link InputStream}.
     * @param headers {@link BlobHTTPHeaders}
     * @param metadata {@link Metadata}
     * @param tier {@link AccessTier} for the destination blob.
     * @param accessConditions {@link BlobAccessConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The information of the uploaded block blob.
     * @throws UnexpectedLengthException when the length of data does not match the input {@code length}.
     * @throws NullPointerException if the input data is null.
     * @throws IOException If an I/O error occurs
     */
    public Response<BlockBlobItem> uploadWithResponse(InputStream data, long length, BlobHTTPHeaders headers,
        Metadata metadata, AccessTier tier, BlobAccessConditions accessConditions, Duration timeout,
        Context context) throws IOException {
        Objects.requireNonNull(data);
        Flux<ByteBuffer> fbb = Utility.convertStreamToByteBuffer(data, length,
            EncryptedBlockBlobAsyncClient.BLOB_DEFAULT_UPLOAD_BLOCK_SIZE);
        Mono<Response<BlockBlobItem>> upload = encryptedBlockBlobAsyncClient
            .uploadWithResponse(fbb.subscribeOn(Schedulers.elastic()), length, headers, metadata, tier,
                accessConditions, context);

        try {
            return Utility.blockWithOptionalTimeout(upload, timeout);
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.cryptography.EncryptedBlockBlobClient.uploadFromFile#String}
     *
     * @param filePath Path of the file to upload
     * @throws IOException If an I/O error occurs
     */
    public void uploadFromFile(String filePath) throws IOException {
        uploadFromFile(filePath, EncryptedBlockBlobAsyncClient.BLOB_DEFAULT_UPLOAD_BLOCK_SIZE, 2, null, null, null,
            null, null);
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.cryptography.EncryptedBlockBlobClient.uploadFromFile#String-Integer-BlobHTTPHeaders-Metadata-AccessTier-BlobAccessConditions-Duration}
     *
     * @param filePath Path of the file to upload
     * @param blockSize Size of the blocks to upload
     * @param numBuffers The maximum number of buffers this method should allocate. Must be at least two. Typically, the
     * larger the number of buffers, the more parallel, and thus faster, the upload portion of this operation will be.
     * The amount of memory consumed by this method may be up to blockSize * numBuffers.
     * @param headers {@link BlobHTTPHeaders}
     * @param metadata {@link Metadata}
     * @param tier {@link AccessTier} for the uploaded blob
     * @param accessConditions {@link BlobAccessConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @throws IOException If an I/O error occurs
     */
    public void uploadFromFile(String filePath, Integer blockSize, int numBuffers, BlobHTTPHeaders headers,
        Metadata metadata, AccessTier tier, BlobAccessConditions accessConditions, Duration timeout)
        throws IOException {
        Mono<Void> upload = this.encryptedBlockBlobAsyncClient.uploadFromFile(filePath, blockSize, numBuffers, headers,
            metadata, tier, accessConditions);

        try {
            Utility.blockWithOptionalTimeout(upload, timeout);
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

}
