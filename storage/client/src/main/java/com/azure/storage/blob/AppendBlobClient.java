// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.util.Context;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.models.AppendBlobAppendBlockFromUrlHeaders;
import com.azure.storage.blob.models.AppendBlobAppendBlockHeaders;
import com.azure.storage.blob.models.AppendBlobCreateHeaders;
import com.azure.storage.blob.models.BlobHTTPHeaders;
import com.azure.storage.blob.models.SourceModifiedAccessConditions;
import io.netty.buffer.ByteBuf;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.time.Duration;


/**
 * Client to an append blob. It may be obtained through a {@link AppendBlobClientBuilder}, via
 * the method {@link BlobClient#asAppendBlobClient()}, or via the method
 * {@link ContainerClient#createAppendBlobClient(String)}. This class does not hold
 * any state about a particular blob, but is instead a convenient way of sending appropriate
 * requests to the resource on the service.
 *
 * <p>
 * Please refer to the <a href=https://docs.microsoft.com/en-us/rest/api/storageservices/understanding-block-blobs--append-blobs--and-page-blobs>Azure Docs</a>
 * for more information.
 */
public final class AppendBlobClient extends BlobClient {

    AppendBlobAsyncClient appendBlobAsyncClient;

    /**
     * Indicates the maximum number of bytes that can be sent in a call to appendBlock.
     */
    public static final int MAX_APPEND_BLOCK_BYTES = 4 * Constants.MB;

    /**
     * Indicates the maximum number of blocks allowed in an append blob.
     */
    public static final int MAX_BLOCKS = 50000;

    /**
     * Package-private constructor for use by {@link AppendBlobClientBuilder}.
     * @param azureBlobStorage the API client for blob storage API
     */
    AppendBlobClient(AzureBlobStorageImpl azureBlobStorage) {
        super(azureBlobStorage);
        this.appendBlobAsyncClient = new AppendBlobAsyncClient(azureBlobStorage);
    }

    /**
     * Static method for getting a new builder for this class.
     *
     * @return
     *      A new {@link AppendBlobClientBuilder} instance.
     */
    public static AppendBlobClientBuilder appendBlobClientBuilder() {
        return new AppendBlobClientBuilder();
    }

    /**
     * Creates a 0-length append blob. Call appendBlock to append data to an append blob.
     *
     * @return
     *      The information of the created appended blob.
     */
    public AppendBlobCreateHeaders create() {
        return this.create(null, null, null, null, null);
    }

    /**
     * Creates a 0-length append blob. Call appendBlock to append data to an append blob.
     *
     * @param headers
     *         {@link BlobHTTPHeaders}
     * @param metadata
     *         {@link Metadata}
     * @param accessConditions
     *         {@link BlobAccessConditions}
     * @param timeout
     *         An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to
     *         its parent, forming a linked list.
     *
     * @return
     *      The information of the created appended blob.
     */
    public AppendBlobCreateHeaders create(BlobHTTPHeaders headers, Metadata metadata,
                                          BlobAccessConditions accessConditions, Duration timeout, Context context) {
        Mono<AppendBlobCreateHeaders> response = appendBlobAsyncClient.create(headers, metadata, accessConditions, context);
        return timeout == null
            ? response.block()
            : response.block(timeout);
    }

    /**
     * Commits a new block of data to the end of the existing append blob.
     * <p>
     * Note that the data passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flux} must produce the same data each time it is subscribed to.
     *
     * @param data
     *         The data to write to the blob. Note that this {@code Flux} must be replayable if retries are enabled
     *         (the default). In other words, the Flux must produce the same data each time it is subscribed to.
     * @param length
     *         The exact length of the data. It is important that this value match precisely the length of the data
     *         emitted by the {@code Flux}.
     *
     * @return
     *      The information of the append blob operation.
     */
    public AppendBlobAppendBlockHeaders appendBlock(Flux<ByteBuf> data, long length) {
        return this.appendBlock(data, length, null, null, null);
    }

    /**
     * Commits a new block of data to the end of the existing append blob.
     * <p>
     * Note that the data passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flux} must produce the same data each time it is subscribed to.
     *
     * @param data
     *         The data to write to the blob. Note that this {@code Flux} must be replayable if retries are enabled
     *         (the default). In other words, the Flux must produce the same data each time it is subscribed to.
     * @param length
     *         The exact length of the data. It is important that this value match precisely the length of the data
     *         emitted by the {@code Flux}.
     * @param appendBlobAccessConditions
     *         {@link AppendBlobAccessConditions}
     * @param timeout
     *         An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to
     *         its parent, forming a linked list.
     *
     * @return
     *      The information of the append blob operation.
     */
    public AppendBlobAppendBlockHeaders appendBlock(Flux<ByteBuf> data, long length,
                                                           AppendBlobAccessConditions appendBlobAccessConditions, Duration timeout, Context context) {
        Mono<AppendBlobAppendBlockHeaders> response = appendBlobAsyncClient.appendBlock(data, length, appendBlobAccessConditions, context);
        return timeout == null
            ? response.block()
            : response.block(timeout);
    }

    /**
     * Commits a new block of data from another blob to the end of this append blob.
     *
     * @param sourceURL
     *          The url to the blob that will be the source of the copy.  A source blob in the same storage account can
     *          be authenticated via Shared Key. However, if the source is a blob in another account, the source blob
     *          must either be public or must be authenticated via a shared access signature. If the source blob is
     *          public, no authentication is required to perform the operation.
     * @param sourceRange
     *          The source {@link BlobRange} to copy.
     *
     * @return
     *      The information of the append blob operation.
     */
    public AppendBlobAppendBlockFromUrlHeaders appendBlockFromUrl(URL sourceURL, BlobRange sourceRange) {
        return this.appendBlockFromUrl(sourceURL, sourceRange, null, null,
                 null, null, null);
    }

    /**
     * Commits a new block of data from another blob to the end of this append blob.
     *
     * @param sourceURL
     *          The url to the blob that will be the source of the copy.  A source blob in the same storage account can
     *          be authenticated via Shared Key. However, if the source is a blob in another account, the source blob
     *          must either be public or must be authenticated via a shared access signature. If the source blob is
     *          public, no authentication is required to perform the operation.
     * @param sourceRange
     *          {@link BlobRange}
     * @param sourceContentMD5
     *          An MD5 hash of the block content from the source blob. If specified, the service will calculate the MD5
     *          of the received data and fail the request if it does not match the provided MD5.
     * @param destAccessConditions
     *          {@link AppendBlobAccessConditions}
     * @param sourceAccessConditions
     *          {@link SourceModifiedAccessConditions}
     * @param timeout
     *         An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context
     *          {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *          {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *          arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *          immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to
     *          its parent, forming a linked list.
     *
     * @return
     *      The information of the append blob operation.
     */
    public AppendBlobAppendBlockFromUrlHeaders appendBlockFromUrl(URL sourceURL, BlobRange sourceRange,
            byte[] sourceContentMD5, AppendBlobAccessConditions destAccessConditions,
            SourceModifiedAccessConditions sourceAccessConditions, Duration timeout, Context context) {
        Mono<AppendBlobAppendBlockFromUrlHeaders> response = appendBlobAsyncClient.appendBlockFromUrl(sourceURL, sourceRange, sourceContentMD5, destAccessConditions, sourceAccessConditions, context);
        return timeout == null
            ? response.block()
            : response.block(timeout);
    }
}
