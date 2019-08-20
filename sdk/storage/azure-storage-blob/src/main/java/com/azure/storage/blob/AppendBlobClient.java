// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.storage.blob.models.AppendBlobAccessConditions;
import com.azure.storage.blob.models.AppendBlobItem;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.BlobHTTPHeaders;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.Metadata;
import com.azure.storage.blob.models.SourceModifiedAccessConditions;
import com.azure.storage.common.Utility;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.InputStream;
import java.net.URL;
import java.time.Duration;


/**
 * Client to an append blob. It may only be instantiated through a {@link BlobClientBuilder}, via
 * the method {@link BlobClient#asAppendBlobClient()}, or via the method
 * {@link ContainerClient#getAppendBlobClient(String)}. This class does not hold
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
     *
     * @throws StorageException If a storage service error occurred.
     */
    public BlobOutputStream getBlobOutputStream() {
        return getBlobOutputStream(null);
    }

    /**
     * Creates and opens an output stream to write data to the append blob. If the blob already exists on the service,
     * it will be overwritten.
     *
     * @param accessConditions A {@link BlobAccessConditions} object that represents the access conditions for the blob.
     *
     * @return A {@link BlobOutputStream} object used to write data to the blob.
     *
     * @throws StorageException If a storage service error occurred.
     */
    public BlobOutputStream getBlobOutputStream(AppendBlobAccessConditions accessConditions) {
        return new BlobOutputStream(appendBlobAsyncClient, accessConditions);
    }

    /**
     * Creates a 0-length append blob. Call appendBlock to append data to an append blob.
     *
     * @return The information of the created appended blob.
     */
    public AppendBlobItem create() {
        return create(null, null, null, null);
    }

    /**
     * Creates a 0-length append blob. Call appendBlock to append data to an append blob.
     *
     * @param headers {@link BlobHTTPHeaders}
     * @param metadata {@link Metadata}
     * @param accessConditions {@link BlobAccessConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     *
     * @return The information of the created appended blob.
     */
    public AppendBlobItem create(BlobHTTPHeaders headers, Metadata metadata,
                                          BlobAccessConditions accessConditions, Duration timeout) {
        return createWithResponse(headers, metadata, accessConditions, timeout, Context.NONE).value();
    }

    /**
     * Creates a 0-length append blob. Call appendBlock to append data to an append blob.
     *
     * @param headers {@link BlobHTTPHeaders}
     * @param metadata {@link Metadata}
     * @param accessConditions {@link BlobAccessConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#value() value} contains the created appended blob.
     */
    public Response<AppendBlobItem> createWithResponse(BlobHTTPHeaders headers, Metadata metadata,
                                           BlobAccessConditions accessConditions, Duration timeout, Context context) {
        Mono<Response<AppendBlobItem>> response = appendBlobAsyncClient.createWithResponse(headers, metadata, accessConditions, context);
        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Commits a new block of data to the end of the existing append blob.
     * <p>
     * Note that the data passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flux} must produce the same data each time it is subscribed to.
     *
     * @param data The data to write to the blob.
     * @param length The exact length of the data. It is important that this value match precisely the length of the data
     *         emitted by the {@code Flux}.
     *
     * @return The information of the append blob operation.
     */
    public AppendBlobItem appendBlock(InputStream data, long length) {
        return appendBlockWithResponse(data, length, null, null, Context.NONE).value();
    }

    /**
     * Commits a new block of data to the end of the existing append blob.
     * <p>
     * Note that the data passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flux} must produce the same data each time it is subscribed to.
     *
     * @param data The data to write to the blob. Note that this {@code Flux} must be replayable if retries are enabled
     *         (the default). In other words, the Flux must produce the same data each time it is subscribed to.
     * @param length The exact length of the data. It is important that this value match precisely the length of the data
     *         emitted by the {@code Flux}.
     * @param appendBlobAccessConditions {@link AppendBlobAccessConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#value() value} contains the append blob operation.
     */
    public Response<AppendBlobItem> appendBlockWithResponse(InputStream data, long length,
                                                AppendBlobAccessConditions appendBlobAccessConditions, Duration timeout, Context context) {
        Flux<ByteBuf> fbb = Flux.range(0, (int) Math.ceil((double) length / (double) MAX_APPEND_BLOCK_BYTES))
            .map(i -> i * MAX_APPEND_BLOCK_BYTES)
            .concatMap(pos -> Mono.fromCallable(() -> {
                long count = pos + MAX_APPEND_BLOCK_BYTES > length ? length - pos : MAX_APPEND_BLOCK_BYTES;
                byte[] cache = new byte[(int) count];
                int read = 0;
                while (read < count) {
                    read += data.read(cache, read, (int) count - read);
                }

                return ByteBufAllocator.DEFAULT.buffer((int) count).writeBytes(cache);
            }));

        Mono<Response<AppendBlobItem>> response = appendBlobAsyncClient.appendBlockWithResponse(fbb.subscribeOn(Schedulers.elastic()), length, appendBlobAccessConditions, context);
        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Commits a new block of data from another blob to the end of this append blob.
     *
     * @param sourceURL The url to the blob that will be the source of the copy.  A source blob in the same storage account can
     *          be authenticated via Shared Key. However, if the source is a blob in another account, the source blob
     *          must either be public or must be authenticated via a shared access signature. If the source blob is
     *          public, no authentication is required to perform the operation.
     * @param sourceRange The source {@link BlobRange} to copy.
     *
     * @return The information of the append blob operation.
     */
    public AppendBlobItem appendBlockFromUrl(URL sourceURL, BlobRange sourceRange) {
        return appendBlockFromUrl(sourceURL, sourceRange, null, null, null, null);
    }

    /**
     * Commits a new block of data from another blob to the end of this append blob.
     *
     * @param sourceURL The url to the blob that will be the source of the copy.  A source blob in the same storage account can
     *          be authenticated via Shared Key. However, if the source is a blob in another account, the source blob
     *          must either be public or must be authenticated via a shared access signature. If the source blob is
     *          public, no authentication is required to perform the operation.
     * @param sourceRange {@link BlobRange}
     * @param sourceContentMD5 An MD5 hash of the block content from the source blob. If specified, the service will calculate the MD5
     *          of the received data and fail the request if it does not match the provided MD5.
     * @param destAccessConditions {@link AppendBlobAccessConditions}
     * @param sourceAccessConditions {@link SourceModifiedAccessConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     *
     * @return The information of the append blob operation.
     */
    public AppendBlobItem appendBlockFromUrl(URL sourceURL, BlobRange sourceRange,
            byte[] sourceContentMD5, AppendBlobAccessConditions destAccessConditions,
            SourceModifiedAccessConditions sourceAccessConditions, Duration timeout) {
        return this.appendBlockFromUrlWithResponse(sourceURL, sourceRange, sourceContentMD5, destAccessConditions, sourceAccessConditions, timeout, Context.NONE).value();
    }

    /**
     * Commits a new block of data from another blob to the end of this append blob.
     *
     * @param sourceURL The url to the blob that will be the source of the copy.  A source blob in the same storage account can
     *          be authenticated via Shared Key. However, if the source is a blob in another account, the source blob
     *          must either be public or must be authenticated via a shared access signature. If the source blob is
     *          public, no authentication is required to perform the operation.
     * @param sourceRange {@link BlobRange}
     * @param sourceContentMD5 An MD5 hash of the block content from the source blob. If specified, the service will calculate the MD5
     *          of the received data and fail the request if it does not match the provided MD5.
     * @param destAccessConditions {@link AppendBlobAccessConditions}
     * @param sourceAccessConditions {@link SourceModifiedAccessConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return The information of the append blob operation.
     */
    public Response<AppendBlobItem> appendBlockFromUrlWithResponse(URL sourceURL, BlobRange sourceRange,
                                                       byte[] sourceContentMD5, AppendBlobAccessConditions destAccessConditions,
                                                       SourceModifiedAccessConditions sourceAccessConditions, Duration timeout, Context context) {
        Mono<Response<AppendBlobItem>> response = appendBlobAsyncClient.appendBlockFromUrlWithResponse(sourceURL, sourceRange, sourceContentMD5, destAccessConditions, sourceAccessConditions, context);
        return Utility.blockWithOptionalTimeout(response, timeout);
    }
}
