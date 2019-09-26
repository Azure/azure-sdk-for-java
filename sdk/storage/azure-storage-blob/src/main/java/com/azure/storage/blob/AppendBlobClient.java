// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.exception.UnexpectedLengthException;
import com.azure.core.http.rest.Response;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.Context;
import com.azure.storage.blob.models.AppendBlobAccessConditions;
import com.azure.storage.blob.models.AppendBlobItem;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.BlobHTTPHeaders;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.Metadata;
import com.azure.storage.blob.models.SourceModifiedAccessConditions;
import com.azure.storage.blob.models.StorageException;
import com.azure.storage.common.Utility;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Objects;

/**
 * Client to an append blob. It may only be instantiated through a {@link BlobClientBuilder}, via the method {@link
 * BlobClient#asAppendBlobClient()}, or via the method {@link ContainerClient#getAppendBlobClient(String)}. This class
 * does not hold any state about a particular blob, but is instead a convenient way of sending appropriate requests to
 * the resource on the service.
 *
 * <p>
 * This client contains operations on a blob. Operations on a container are available on {@link ContainerClient}, and
 * operations on the service are available on {@link BlobServiceClient}.
 *
 * <p>
 * Please refer to the <a href=https://docs.microsoft.com/en-us/rest/api/storageservices/understanding-block-blobs--append-blobs--and-page-blobs>Azure
 * Docs</a> for more information.
 */
@ServiceClient(builder = BlobClientBuilder.class)
public final class AppendBlobClient extends BlobClient {
    private final AppendBlobAsyncClient appendBlobAsyncClient;

    /**
     * Indicates the maximum number of bytes that can be sent in a call to appendBlock.
     */
    public static final int MAX_APPEND_BLOCK_BYTES = AppendBlobAsyncClient.MAX_APPEND_BLOCK_BYTES;

    /**
     * Indicates the maximum number of blocks allowed in an append blob.
     */
    public static final int MAX_BLOCKS = AppendBlobAsyncClient.MAX_BLOCKS;

    /**
     * Package-private constructor for use by {@link BlobClientBuilder}.
     *
     * @param appendBlobAsyncClient the async append blob client
     */
    AppendBlobClient(AppendBlobAsyncClient appendBlobAsyncClient) {
        super(appendBlobAsyncClient);
        this.appendBlobAsyncClient = appendBlobAsyncClient;
    }

    /**
     * Creates and opens an output stream to write data to the append blob. If the blob already exists on the service,
     * it will be overwritten.
     *
     * @return A {@link BlobOutputStream} object used to write data to the blob.
     * @throws StorageException If a storage service error occurred.
     */
    public BlobOutputStream getBlobOutputStream() {
        return getBlobOutputStream(null);
    }

    /**
     * Creates and opens an output stream to write data to the append blob. If the blob already exists on the service,
     * it will be overwritten.
     *
     * @param accessConditions A {@link BlobAccessConditions} object that represents the access conditions for the
     * blob.
     * @return A {@link BlobOutputStream} object used to write data to the blob.
     * @throws StorageException If a storage service error occurred.
     */
    public BlobOutputStream getBlobOutputStream(AppendBlobAccessConditions accessConditions) {
        return BlobOutputStream.appendBlobOutputStream(appendBlobAsyncClient, accessConditions);
    }

    /**
     * Creates a 0-length append blob. Call appendBlock to append data to an append blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.AppendBlobClient.create}
     *
     * @return The information of the created appended blob.
     */
    public AppendBlobItem create() {
        return createWithResponse(null, null, null, null, Context.NONE).getValue();
    }

    /**
     * Creates a 0-length append blob. Call appendBlock to append data to an append blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.AppendBlobClient.createWithResponse#BlobHTTPHeaders-Metadata-BlobAccessConditions-Duration-Context}
     *
     * @param headers {@link BlobHTTPHeaders}
     * @param metadata {@link Metadata}
     * @param accessConditions {@link BlobAccessConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the created appended blob.
     */
    public Response<AppendBlobItem> createWithResponse(BlobHTTPHeaders headers, Metadata metadata,
        BlobAccessConditions accessConditions, Duration timeout, Context context) {
        return Utility.blockWithOptionalTimeout(appendBlobAsyncClient.
            createWithResponse(headers, metadata, accessConditions, context), timeout);
    }

    /**
     * Commits a new block of data to the end of the existing append blob.
     * <p>
     * Note that the data passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flux} must produce the same data each time it is subscribed to.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.AppendBlobClient.appendBlock#InputStream-long}
     *
     * @param data The data to write to the blob.
     * @param length The exact length of the data. It is important that this value match precisely the length of the
     * data emitted by the {@code Flux}.
     * @return The information of the append blob operation.
     */
    public AppendBlobItem appendBlock(InputStream data, long length) {
        return appendBlockWithResponse(data, length, null, null, Context.NONE).getValue();
    }

    /**
     * Commits a new block of data to the end of the existing append blob.
     * <p>
     * Note that the data passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flux} must produce the same data each time it is subscribed to.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.AppendBlobClient.appendBlockWithResponse#InputStream-long-AppendBlobAccessConditions-Duration-Context}
     *
     * @param data The data to write to the blob. Note that this {@code Flux} must be replayable if retries are enabled
     * (the default). In other words, the Flux must produce the same data each time it is subscribed to.
     * @param length The exact length of the data. It is important that this value match precisely the length of the
     * data emitted by the {@code Flux}.
     * @param appendBlobAccessConditions {@link AppendBlobAccessConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the append blob operation.
     * @throws UnexpectedLengthException when the length of data does not match the input {@code length}.
     * @throws NullPointerException if the input data is null.
     */
    public Response<AppendBlobItem> appendBlockWithResponse(InputStream data, long length,
        AppendBlobAccessConditions appendBlobAccessConditions, Duration timeout, Context context) {
        Objects.requireNonNull(data);
        Flux<ByteBuffer> fbb = Utility.convertStreamToByteBuffer(data, length, MAX_APPEND_BLOCK_BYTES);
        Mono<Response<AppendBlobItem>> response = appendBlobAsyncClient.appendBlockWithResponse(
            fbb.subscribeOn(Schedulers.elastic()), length, appendBlobAccessConditions, context);
        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Commits a new block of data from another blob to the end of this append blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.AppendBlobClient.appendBlockFromUrl#URL-BlobRange}
     *
     * @param sourceURL The url to the blob that will be the source of the copy.  A source blob in the same storage
     * account can be authenticated via Shared Key. However, if the source is a blob in another account, the source blob
     * must either be public or must be authenticated via a shared access signature. If the source blob is public, no
     * authentication is required to perform the operation.
     * @param sourceRange The source {@link BlobRange} to copy.
     * @return The information of the append blob operation.
     */
    public AppendBlobItem appendBlockFromUrl(URL sourceURL, BlobRange sourceRange) {
        return appendBlockFromUrlWithResponse(sourceURL, sourceRange, null, null, null, null, Context.NONE).getValue();
    }

    /**
     * Commits a new block of data from another blob to the end of this append blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.AppendBlobClient.appendBlockFromUrlWithResponse#URL-BlobRange-byte-AppendBlobAccessConditions-SourceModifiedAccessConditions-Duration-Context}
     *
     * @param sourceURL The url to the blob that will be the source of the copy.  A source blob in the same storage
     * account can be authenticated via Shared Key. However, if the source is a blob in another account, the source blob
     * must either be public or must be authenticated via a shared access signature. If the source blob is public, no
     * authentication is required to perform the operation.
     * @param sourceRange {@link BlobRange}
     * @param sourceContentMD5 An MD5 hash of the block content from the source blob. If specified, the service will
     * calculate the MD5 of the received data and fail the request if it does not match the provided MD5.
     * @param destAccessConditions {@link AppendBlobAccessConditions}
     * @param sourceAccessConditions {@link SourceModifiedAccessConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The information of the append blob operation.
     */
    public Response<AppendBlobItem> appendBlockFromUrlWithResponse(URL sourceURL, BlobRange sourceRange,
        byte[] sourceContentMD5, AppendBlobAccessConditions destAccessConditions,
        SourceModifiedAccessConditions sourceAccessConditions, Duration timeout, Context context) {
        Mono<Response<AppendBlobItem>> response = appendBlobAsyncClient.appendBlockFromUrlWithResponse(sourceURL,
            sourceRange, sourceContentMD5, destAccessConditions, sourceAccessConditions, context);
        return Utility.blockWithOptionalTimeout(response, timeout);
    }
}
