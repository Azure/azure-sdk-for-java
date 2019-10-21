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
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.implementation.models.AppendBlobAppendBlockFromUrlHeaders;
import com.azure.storage.blob.implementation.models.AppendBlobAppendBlockHeaders;
import com.azure.storage.blob.implementation.models.AppendBlobCreateHeaders;
import com.azure.storage.blob.models.AppendBlobAccessConditions;
import com.azure.storage.blob.models.AppendBlobItem;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.CpkInfo;
import com.azure.storage.blob.models.SourceModifiedAccessConditions;
import com.azure.storage.common.implementation.Constants;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Map;

import static com.azure.core.implementation.util.FluxUtil.monoError;
import static com.azure.core.implementation.util.FluxUtil.withContext;

/**
 * Client to an append blob. It may only be instantiated through a
 * {@link SpecializedBlobClientBuilder#buildAppendBlobAsyncClient()} or via the method
 * {@link BlobAsyncClient#getAppendBlobAsyncClient()}. This class does not hold any state about a
 * particular blob, but is instead a convenient way of sending appropriate requests to the resource on the service.
 *
 * <p>
 * This client contains operations on a blob. Operations on a container are available on {@link
 * BlobContainerAsyncClient}, and operations on the service are available on {@link BlobServiceAsyncClient}.
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
public final class AppendBlobAsyncClient extends BlobAsyncClientBase {
    private final ClientLogger logger = new ClientLogger(AppendBlobAsyncClient.class);

    /**
     * Indicates the maximum number of bytes that can be sent in a call to appendBlock.
     */
    public static final int MAX_APPEND_BLOCK_BYTES = 4 * Constants.MB;

    /**
     * Indicates the maximum number of blocks allowed in an append blob.
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
    AppendBlobAsyncClient(HttpPipeline pipeline, String url, BlobServiceVersion serviceVersion,
        String accountName, String containerName, String blobName, String snapshot, CpkInfo customerProvidedKey) {
        super(pipeline, url, serviceVersion, accountName, containerName, blobName, snapshot, customerProvidedKey);
    }

    /**
     * Creates a 0-length append blob. Call appendBlock to append data to an append blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.AppendBlobAsyncClient.create}
     *
     * @return A {@link Mono} containing the information of the created appended blob.
     */
    public Mono<AppendBlobItem> create() {
        try {
            return createWithResponse(false, null, null, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a 0-length append blob. Call appendBlock to append data to an append blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.AppendBlobAsyncClient.createWithResponse#boolean-BlobHttpHeaders-Map-BlobAccessConditions}
     *
     * @param overwrite Whether to overwrite, should data already exist on this blob.
     * @param headers {@link BlobHttpHeaders}
     * @param metadata Metadata to associate with the blob.
     * @param accessConditions {@link BlobAccessConditions}
     * @return A {@link Mono} containing {@link Response} whose {@link Response#getValue() value} contains the created
     * appended blob.
     */
    public Mono<Response<AppendBlobItem>> createWithResponse(boolean overwrite, BlobHttpHeaders headers,
        Map<String, String> metadata, BlobAccessConditions accessConditions) {
        try {
            return withContext(context -> createWithResponse(overwrite, headers, metadata, accessConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<AppendBlobItem>> createWithResponse(boolean overwrite, BlobHttpHeaders headers,
        Map<String, String> metadata, BlobAccessConditions accessConditions, Context context) {
        accessConditions = (accessConditions == null) ? new BlobAccessConditions() : accessConditions;

        if (!overwrite) {
            accessConditions.getModifiedAccessConditions().setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD);
        }

        return this.azureBlobStorage.appendBlobs().createWithRestResponseAsync(null,
            null, 0, null, metadata, null, headers, accessConditions.getLeaseAccessConditions(),
            getCustomerProvidedKey(), accessConditions.getModifiedAccessConditions(), context)
            .map(rb -> {
                AppendBlobCreateHeaders hd = rb.getDeserializedHeaders();
                AppendBlobItem item = new AppendBlobItem(hd.getETag(), hd.getLastModified(), hd.getContentMD5(),
                    hd.isServerEncrypted(), hd.getEncryptionKeySha256(), null, null);
                return new SimpleResponse<>(rb, item);
            });
    }

    /**
     * Commits a new block of data to the end of the existing append blob.
     * <p>
     * Note that the data passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flux} must produce the same data each time it is subscribed to.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.AppendBlobAsyncClient.appendBlock#Flux-long}
     *
     * @param data The data to write to the blob. Note that this {@code Flux} must be replayable if retries are enabled
     * (the default). In other words, the Flux must produce the same data each time it is subscribed to.
     * @param length The exact length of the data. It is important that this value match precisely the length of the
     * data emitted by the {@code Flux}.
     * @return {@link Mono} containing the information of the append blob operation.
     */
    public Mono<AppendBlobItem> appendBlock(Flux<ByteBuffer> data, long length) {
        try {
            return appendBlockWithResponse(data, length, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Commits a new block of data to the end of the existing append blob.
     * <p>
     * Note that the data passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flux} must produce the same data each time it is subscribed to.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.AppendBlobAsyncClient.appendBlockWithResponse#Flux-long-AppendBlobAccessConditions}
     *
     * @param data The data to write to the blob. Note that this {@code Flux} must be replayable if retries are enabled
     * (the default). In other words, the Flux must produce the same data each time it is subscribed to.
     * @param length The exact length of the data. It is important that this value match precisely the length of the
     * data emitted by the {@code Flux}.
     * @param appendBlobAccessConditions {@link AppendBlobAccessConditions}
     * @return A {@link Mono} containing {@link Response} whose {@link Response#getValue() value} contains the append
     * blob operation.
     */
    public Mono<Response<AppendBlobItem>> appendBlockWithResponse(Flux<ByteBuffer> data, long length,
        AppendBlobAccessConditions appendBlobAccessConditions) {
        try {
            return withContext(context -> appendBlockWithResponse(data, length, appendBlobAccessConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<AppendBlobItem>> appendBlockWithResponse(Flux<ByteBuffer> data, long length,
        AppendBlobAccessConditions appendBlobAccessConditions, Context context) {
        appendBlobAccessConditions = appendBlobAccessConditions == null ? new AppendBlobAccessConditions()
            : appendBlobAccessConditions;

        return this.azureBlobStorage.appendBlobs().appendBlockWithRestResponseAsync(
            null, null, data, length, null, null, null, null,
            appendBlobAccessConditions.getLeaseAccessConditions(),
            appendBlobAccessConditions.getAppendPositionAccessConditions(), getCustomerProvidedKey(),
            appendBlobAccessConditions.getModifiedAccessConditions(), context)
            .map(rb -> {
                AppendBlobAppendBlockHeaders hd = rb.getDeserializedHeaders();
                AppendBlobItem item = new AppendBlobItem(hd.getETag(), hd.getLastModified(), hd.getContentMD5(),
                    hd.isServerEncrypted(), hd.getEncryptionKeySha256(), hd.getBlobAppendOffset(),
                    hd.getBlobCommittedBlockCount());
                return new SimpleResponse<>(rb, item);
            });
    }

    /**
     * Commits a new block of data from another blob to the end of this append blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.AppendBlobAsyncClient.appendBlockFromUrl#String-BlobRange}
     *
     * @param sourceUrl The url to the blob that will be the source of the copy.  A source blob in the same storage
     * account can be authenticated via Shared Key. However, if the source is a blob in another account, the source blob
     * must either be public or must be authenticated via a shared access signature. If the source blob is public, no
     * authentication is required to perform the operation.
     * @param sourceRange The source {@link BlobRange} to copy.
     * @return {@link Mono} containing the information of the append blob operation.
     */
    public Mono<AppendBlobItem> appendBlockFromUrl(String sourceUrl, BlobRange sourceRange) {
        try {
            return appendBlockFromUrlWithResponse(sourceUrl, sourceRange, null, null, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Commits a new block of data from another blob to the end of this append blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.AppendBlobAsyncClient.appendBlockFromUrlWithResponse#String-BlobRange-byte-AppendBlobAccessConditions-SourceModifiedAccessConditions}
     *
     * @param sourceUrl The url to the blob that will be the source of the copy.  A source blob in the same storage
     * account can be authenticated via Shared Key. However, if the source is a blob in another account, the source blob
     * must either be public or must be authenticated via a shared access signature. If the source blob is public, no
     * authentication is required to perform the operation.
     * @param sourceRange {@link BlobRange}
     * @param sourceContentMD5 An MD5 hash of the block content from the source blob. If specified, the service will
     * calculate the MD5 of the received data and fail the request if it does not match the provided MD5.
     * @param destAccessConditions {@link AppendBlobAccessConditions}
     * @param sourceAccessConditions {@link SourceModifiedAccessConditions}
     * @return A {@link Mono} containing {@link Response} whose {@link Response#getValue() value} contains the append
     * blob operation.
     */
    public Mono<Response<AppendBlobItem>> appendBlockFromUrlWithResponse(String sourceUrl, BlobRange sourceRange,
        byte[] sourceContentMD5, AppendBlobAccessConditions destAccessConditions,
        SourceModifiedAccessConditions sourceAccessConditions) {
        try {
            return withContext(context -> appendBlockFromUrlWithResponse(sourceUrl, sourceRange, sourceContentMD5,
                destAccessConditions, sourceAccessConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<AppendBlobItem>> appendBlockFromUrlWithResponse(String sourceUrl, BlobRange sourceRange,
        byte[] sourceContentMD5, AppendBlobAccessConditions destAccessConditions,
        SourceModifiedAccessConditions sourceAccessConditions, Context context) {
        sourceRange = sourceRange == null ? new BlobRange(0) : sourceRange;
        destAccessConditions = destAccessConditions == null
            ? new AppendBlobAccessConditions() : destAccessConditions;

        URL url;
        try {
            url = new URL(sourceUrl);
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'sourceUrl' is not a valid url."));
        }

        return this.azureBlobStorage.appendBlobs().appendBlockFromUrlWithRestResponseAsync(null, null, url, 0,
                sourceRange.toString(), sourceContentMD5, null, null, null, null, getCustomerProvidedKey(),
                destAccessConditions.getLeaseAccessConditions(),
                destAccessConditions.getAppendPositionAccessConditions(),
                destAccessConditions.getModifiedAccessConditions(), sourceAccessConditions, context)
            .map(rb -> {
                AppendBlobAppendBlockFromUrlHeaders hd = rb.getDeserializedHeaders();
                AppendBlobItem item = new AppendBlobItem(hd.getETag(), hd.getLastModified(), hd.getContentMD5(),
                    hd.isServerEncrypted(), hd.getEncryptionKeySha256(), hd.getBlobAppendOffset(),
                    hd.getBlobCommittedBlockCount());
                return new SimpleResponse<>(rb, item);
            });
    }
}
