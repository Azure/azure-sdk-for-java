// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.implementation.http.UrlBuilder;
import com.azure.core.implementation.util.FluxUtil;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.CopyStatusType;
import com.azure.storage.blob.models.CpkInfo;
import com.azure.storage.blob.models.ModifiedAccessConditions;
import com.azure.storage.blob.models.PageBlobAccessConditions;
import com.azure.storage.blob.models.PageBlobItem;
import com.azure.storage.blob.models.PageList;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.blob.models.SequenceNumberActionType;
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
 * Client to a page blob. It may only be instantiated through a {@link SpecializedBlobClientBuilder} or via the method
 * {@link BlobAsyncClient#getPageBlobAsyncClient()}. This class does not hold any state about a particular blob, but is
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
public final class PageBlobAsyncClient extends BlobAsyncClientBase {
    /**
     * Indicates the number of bytes in a page.
     */
    public static final int PAGE_BYTES = 512;

    /**
     * Indicates the maximum number of bytes that may be sent in a call to putPage.
     */
    public static final int MAX_PUT_PAGES_BYTES = 4 * Constants.MB;

    private final ClientLogger logger = new ClientLogger(PageBlobAsyncClient.class);

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
    PageBlobAsyncClient(HttpPipeline pipeline, String url, BlobServiceVersion serviceVersion,
        String accountName, String containerName, String blobName, String snapshot, CpkInfo customerProvidedKey) {
        super(pipeline, url, serviceVersion, accountName, containerName, blobName, snapshot, customerProvidedKey);
    }

    /**
     * Creates a page blob of the specified length. Call PutPage to upload data data to a page blob. For more
     * information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.PageBlobAsyncClient.create#long}
     *
     * @param size Specifies the maximum size for the page blob, up to 8 TB. The page blob size must be aligned to a
     * 512-byte boundary.
     * @return A reactive response containing the information of the created page blob.
     */
    public Mono<PageBlobItem> create(long size) {
        try {
            return createWithResponse(size, null, null, null, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a page blob of the specified length. Call PutPage to upload data data to a page blob. For more
     * information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.PageBlobAsyncClient.createWithResponse#long-Long-BlobHttpHeaders-Map-BlobAccessConditions}
     *
     * @param size Specifies the maximum size for the page blob, up to 8 TB. The page blob size must be aligned to a
     * 512-byte boundary.
     * @param sequenceNumber A user-controlled value that you can use to track requests. The value of the sequence
     * number must be between 0 and 2^63 - 1.The default value is 0.
     * @param headers {@link BlobHttpHeaders}
     * @param metadata Metadata to associate with the blob.
     * @param accessConditions {@link BlobAccessConditions}
     * @return A reactive response containing the information of the created page blob.
     * @throws IllegalArgumentException If {@code size} isn't a multiple of {@link PageBlobAsyncClient#PAGE_BYTES} or
     * {@code sequenceNumber} isn't null and is less than 0.
     */
    public Mono<Response<PageBlobItem>> createWithResponse(long size, Long sequenceNumber, BlobHttpHeaders headers,
        Map<String, String> metadata, BlobAccessConditions accessConditions) {
        try {
            return withContext(context ->
                createWithResponse(size, sequenceNumber, headers, metadata, accessConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<PageBlobItem>> createWithResponse(long size, Long sequenceNumber, BlobHttpHeaders headers,
        Map<String, String> metadata, BlobAccessConditions accessConditions, Context context) {
        accessConditions = accessConditions == null ? new BlobAccessConditions() : accessConditions;

        if (size % PAGE_BYTES != 0) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw logger.logExceptionAsError(
                new IllegalArgumentException("size must be a multiple of PageBlobAsyncClient.PAGE_BYTES."));
        }
        if (sequenceNumber != null && sequenceNumber < 0) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw logger.logExceptionAsError(
                new IllegalArgumentException("SequenceNumber must be greater than or equal to 0."));
        }

        return this.azureBlobStorage.pageBlobs().createWithRestResponseAsync(null, null, 0, size,
            null, null, metadata, sequenceNumber, null, headers, accessConditions.getLeaseAccessConditions(),
            getCustomerProvidedKey(), accessConditions.getModifiedAccessConditions(), context)
            .map(rb -> new SimpleResponse<>(rb, new PageBlobItem(rb.getDeserializedHeaders())));
    }

    /**
     * Writes 1 or more pages to the page blob. The start and end offsets must be a multiple of 512. For more
     * information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-page">Azure Docs</a>.
     * <p>
     * Note that the data passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flux} must produce the same data each time it is subscribed to.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.PageBlobAsyncClient.uploadPages#PageRange-Flux}
     *
     * @param pageRange A {@link PageRange} object. Given that pages must be aligned with 512-byte boundaries, the start
     * offset must be a modulus of 512 and the end offset must be a modulus of 512 - 1. Examples of valid byte ranges
     * are 0-511, 512-1023, etc.
     * @param body The data to upload. Note that this {@code Flux} must be replayable if retries are enabled (the
     * default). In other words, the Flowable must produce the same data each time it is subscribed to.
     * @return A reactive response containing the information of the uploaded pages.
     */
    public Mono<PageBlobItem> uploadPages(PageRange pageRange, Flux<ByteBuffer> body) {
        try {
            return uploadPagesWithResponse(pageRange, body, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Writes 1 or more pages to the page blob. The start and end offsets must be a multiple of 512. For more
     * information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-page">Azure Docs</a>.
     * <p>
     * Note that the data passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flux} must produce the same data each time it is subscribed to.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.PageBlobAsyncClient.uploadPagesWithResponse#PageRange-Flux-PageBlobAccessConditions}
     *
     * @param pageRange A {@link PageRange} object. Given that pages must be aligned with 512-byte boundaries, the start
     * offset must be a modulus of 512 and the end offset must be a modulus of 512 - 1. Examples of valid byte ranges
     * are 0-511, 512-1023, etc.
     * @param body The data to upload. Note that this {@code Flux} must be replayable if retries are enabled (the
     * default). In other words, the Flowable must produce the same data each time it is subscribed to.
     * @param pageBlobAccessConditions {@link PageBlobAccessConditions}
     * @return A reactive response containing the information of the uploaded pages.
     * @throws IllegalArgumentException If {@code pageRange} is {@code null}
     */
    public Mono<Response<PageBlobItem>> uploadPagesWithResponse(PageRange pageRange, Flux<ByteBuffer> body,
        PageBlobAccessConditions pageBlobAccessConditions) {
        try {
            return withContext(context -> uploadPagesWithResponse(pageRange, body, pageBlobAccessConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<PageBlobItem>> uploadPagesWithResponse(PageRange pageRange, Flux<ByteBuffer> body,
        PageBlobAccessConditions pageBlobAccessConditions, Context context) {
        pageBlobAccessConditions = pageBlobAccessConditions == null
            ? new PageBlobAccessConditions()
            : pageBlobAccessConditions;

        if (pageRange == null) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw logger.logExceptionAsError(new IllegalArgumentException("pageRange cannot be null."));
        }
        String pageRangeStr = pageRangeToString(pageRange);

        return this.azureBlobStorage.pageBlobs().uploadPagesWithRestResponseAsync(null,
            null, body, pageRange.getEnd() - pageRange.getStart() + 1, null, null, null, pageRangeStr, null,
            pageBlobAccessConditions.getLeaseAccessConditions(), getCustomerProvidedKey(),
            pageBlobAccessConditions.getSequenceNumberAccessConditions(),
            pageBlobAccessConditions.getModifiedAccessConditions(), context)
            .map(rb -> new SimpleResponse<>(rb, new PageBlobItem(rb.getDeserializedHeaders())));
    }

    /**
     * Writes 1 or more pages from the source page blob to this page blob. The start and end offsets must be a multiple
     * of 512. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-page">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.PageBlobAsyncClient.uploadPagesFromURL#PageRange-String-Long}
     *
     * @param range A {@link PageRange} object. Given that pages must be aligned with 512-byte boundaries, the start
     * offset must be a modulus of 512 and the end offset must be a modulus of 512 - 1. Examples of valid byte ranges
     * are 0-511, 512-1023, etc.
     * @param sourceUrl The url to the blob that will be the source of the copy.  A source blob in the same storage
     * account can be authenticated via Shared Key. However, if the source is a blob in another account, the source blob
     * must either be public or must be authenticated via a shared access signature. If the source blob is public, no
     * authentication is required to perform the operation.
     * @param sourceOffset The source offset to copy from.  Pass null or 0 to copy from the beginning of source page
     * blob.
     * @return A reactive response containing the information of the uploaded pages.
     */
    public Mono<PageBlobItem> uploadPagesFromURL(PageRange range, String sourceUrl, Long sourceOffset) {
        try {
            return uploadPagesFromURLWithResponse(range, sourceUrl, sourceOffset, null, null, null)
                .flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Writes 1 or more pages from the source page blob to this page blob. The start and end offsets must be a multiple
     * of 512. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-page">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.PageBlobAsyncClient.uploadPagesFromURLWithResponse#PageRange-String-Long-byte-PageBlobAccessConditions-SourceModifiedAccessConditions}
     *
     * @param range The destination {@link PageRange} range. Given that pages must be aligned with 512-byte boundaries,
     * the start offset must be a modulus of 512 and the end offset must be a modulus of 512 - 1. Examples of valid byte
     * ranges are 0-511, 512-1023, etc.
     * @param sourceUrl The url to the blob that will be the source of the copy.  A source blob in the same storage
     * account can be authenticated via Shared Key. However, if the source is a blob in another account, the source blob
     * must either be public or must be authenticated via a shared access signature. If the source blob is public, no
     * authentication is required to perform the operation.
     * @param sourceOffset The source offset to copy from.  Pass null or 0 to copy from the beginning of source blob.
     * @param sourceContentMD5 An MD5 hash of the block content from the source blob. If specified, the service will
     * calculate the MD5 of the received data and fail the request if it does not match the provided MD5.
     * @param destAccessConditions {@link PageBlobAccessConditions}
     * @param sourceAccessConditions {@link SourceModifiedAccessConditions}
     * @return A reactive response containing the information of the uploaded pages.
     * @throws IllegalArgumentException If {@code range} is {@code null}
     */
    public Mono<Response<PageBlobItem>> uploadPagesFromURLWithResponse(PageRange range, String sourceUrl,
        Long sourceOffset, byte[] sourceContentMD5, PageBlobAccessConditions destAccessConditions,
        SourceModifiedAccessConditions sourceAccessConditions) {
        try {
            return withContext(
                context -> uploadPagesFromURLWithResponse(range, sourceUrl, sourceOffset, sourceContentMD5,
                    destAccessConditions, sourceAccessConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<PageBlobItem>> uploadPagesFromURLWithResponse(PageRange range, String sourceUrl, Long sourceOffset,
        byte[] sourceContentMD5, PageBlobAccessConditions destAccessConditions,
        SourceModifiedAccessConditions sourceAccessConditions, Context context) {
        if (range == null) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw logger.logExceptionAsError(new IllegalArgumentException("range cannot be null."));
        }

        String rangeString = pageRangeToString(range);

        if (sourceOffset == null) {
            sourceOffset = 0L;
        }

        String sourceRangeString = pageRangeToString(new PageRange()
            .setStart(sourceOffset)
            .setEnd(sourceOffset + (range.getEnd() - range.getStart())));

        destAccessConditions = destAccessConditions == null ? new PageBlobAccessConditions() : destAccessConditions;

        URL url;
        try {
            url = new URL(sourceUrl);
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'sourceUrl' is not a valid url."));
        }

        return this.azureBlobStorage.pageBlobs().uploadPagesFromURLWithRestResponseAsync(null, null,
            url, sourceRangeString, 0, rangeString, sourceContentMD5, null, null, null, getCustomerProvidedKey(),
            destAccessConditions.getLeaseAccessConditions(), destAccessConditions.getSequenceNumberAccessConditions(),
            destAccessConditions.getModifiedAccessConditions(), sourceAccessConditions, context)
            .map(rb -> new SimpleResponse<>(rb, new PageBlobItem(rb.getDeserializedHeaders())));
    }

    /**
     * Frees the specified pages from the page blob. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-page">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.PageBlobAsyncClient.clearPages#PageRange}
     *
     * @param pageRange A {@link PageRange} object. Given that pages must be aligned with 512-byte boundaries, the start
     * offset must be a modulus of 512 and the end offset must be a modulus of 512 - 1. Examples of valid byte ranges
     * are 0-511, 512-1023, etc.
     * @return A reactive response containing the information of the cleared pages.
     */
    public Mono<PageBlobItem> clearPages(PageRange pageRange) {
        try {
            return clearPagesWithResponse(pageRange, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Frees the specified pages from the page blob. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-page">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.PageBlobAsyncClient.clearPagesWithResponse#PageRange-PageBlobAccessConditions}
     *
     * @param pageRange A {@link PageRange} object. Given that pages must be aligned with 512-byte boundaries, the start
     * offset must be a modulus of 512 and the end offset must be a modulus of 512 - 1. Examples of valid byte ranges
     * are 0-511, 512-1023, etc.
     * @param pageBlobAccessConditions {@link PageBlobAccessConditions}
     * @return A reactive response containing the information of the cleared pages.
     * @throws IllegalArgumentException If {@code pageRange} is {@code null}
     */
    public Mono<Response<PageBlobItem>> clearPagesWithResponse(PageRange pageRange,
        PageBlobAccessConditions pageBlobAccessConditions) {
        try {
            return withContext(context -> clearPagesWithResponse(pageRange, pageBlobAccessConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<PageBlobItem>> clearPagesWithResponse(PageRange pageRange,
        PageBlobAccessConditions pageBlobAccessConditions, Context context) {
        pageBlobAccessConditions = pageBlobAccessConditions == null
            ? new PageBlobAccessConditions()
            : pageBlobAccessConditions;
        if (pageRange == null) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw logger.logExceptionAsError(new IllegalArgumentException("pageRange cannot be null."));
        }
        String pageRangeStr = pageRangeToString(pageRange);

        return this.azureBlobStorage.pageBlobs().clearPagesWithRestResponseAsync(null,
            null, 0, null, pageRangeStr, null,
            pageBlobAccessConditions.getLeaseAccessConditions(), getCustomerProvidedKey(),
            pageBlobAccessConditions.getSequenceNumberAccessConditions(),
            pageBlobAccessConditions.getModifiedAccessConditions(), context)
            .map(rb -> new SimpleResponse<>(rb, new PageBlobItem(rb.getDeserializedHeaders())));
    }

    /**
     * Returns the list of valid page ranges for a page blob or snapshot of a page blob. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-page-ranges">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.PageBlobAsyncClient.getPageRanges#BlobRange}
     *
     * @param blobRange {@link BlobRange}
     * @return A reactive response containing the information of the cleared pages.
     */
    public Mono<PageList> getPageRanges(BlobRange blobRange) {
        try {
            return getPageRangesWithResponse(blobRange, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns the list of valid page ranges for a page blob or snapshot of a page blob. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-page-ranges">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.PageBlobAsyncClient.getPageRangesWithResponse#BlobRange-BlobAccessConditions}
     *
     * @param blobRange {@link BlobRange}
     * @param accessConditions {@link BlobAccessConditions}
     * @return A reactive response emitting all the page ranges.
     */
    public Mono<Response<PageList>> getPageRangesWithResponse(BlobRange blobRange,
        BlobAccessConditions accessConditions) {
        try {
            return withContext(context -> getPageRangesWithResponse(blobRange, accessConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<PageList>> getPageRangesWithResponse(BlobRange blobRange, BlobAccessConditions accessConditions,
        Context context) {
        blobRange = blobRange == null ? new BlobRange(0) : blobRange;
        accessConditions = accessConditions == null ? new BlobAccessConditions() : accessConditions;

        return this.azureBlobStorage.pageBlobs().getPageRangesWithRestResponseAsync(
            null, null, getSnapshotId(), null, blobRange.toHeaderValue(),
            null, accessConditions.getLeaseAccessConditions(), accessConditions.getModifiedAccessConditions(),
            context).map(response -> new SimpleResponse<>(response, response.getValue()));
    }

    /**
     * Gets the collection of page ranges that differ between a specified snapshot and this page blob. For more
     * information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/get-page-ranges">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.PageBlobAsyncClient.getPageRangesDiff#BlobRange-String}
     *
     * @param blobRange {@link BlobRange}
     * @param prevSnapshot Specifies that the response will contain only pages that were changed between target blob and
     * previous snapshot. Changed pages include both updated and cleared pages. The target blob may be a snapshot, as
     * long as the snapshot specified by prevsnapshot is the older of the two.
     * @return A reactive response emitting all the different page ranges.
     */
    public Mono<PageList> getPageRangesDiff(BlobRange blobRange, String prevSnapshot) {
        try {
            return getPageRangesDiffWithResponse(blobRange, prevSnapshot, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Gets the collection of page ranges that differ between a specified snapshot and this page blob. For more
     * information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/get-page-ranges">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.PageBlobAsyncClient.getPageRangesDiffWithResponse#BlobRange-String-BlobAccessConditions}
     *
     * @param blobRange {@link BlobRange}
     * @param prevSnapshot Specifies that the response will contain only pages that were changed between target blob and
     * previous snapshot. Changed pages include both updated and cleared pages. The target blob may be a snapshot, as
     * long as the snapshot specified by prevsnapshot is the older of the two.
     * @param accessConditions {@link BlobAccessConditions}
     * @return A reactive response emitting all the different page ranges.
     * @throws IllegalArgumentException If {@code prevSnapshot} is {@code null}
     */
    public Mono<Response<PageList>> getPageRangesDiffWithResponse(BlobRange blobRange, String prevSnapshot,
        BlobAccessConditions accessConditions) {
        try {
            return withContext(context ->
                getPageRangesDiffWithResponse(blobRange, prevSnapshot, accessConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<PageList>> getPageRangesDiffWithResponse(BlobRange blobRange, String prevSnapshot,
        BlobAccessConditions accessConditions, Context context) {
        blobRange = blobRange == null ? new BlobRange(0) : blobRange;
        accessConditions = accessConditions == null ? new BlobAccessConditions() : accessConditions;

        if (prevSnapshot == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("prevSnapshot cannot be null"));
        }

        return this.azureBlobStorage.pageBlobs().getPageRangesDiffWithRestResponseAsync(
            null, null, getSnapshotId(), null, prevSnapshot,
            blobRange.toHeaderValue(), null, accessConditions.getLeaseAccessConditions(),
            accessConditions.getModifiedAccessConditions(), context)
            .map(response -> new SimpleResponse<>(response, response.getValue()));
    }

    /**
     * Resizes the page blob to the specified size (which must be a multiple of 512). For more information, see the <a
     * href="https://docs.microsoft.com/rest/api/storageservices/set-blob-properties">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.PageBlobAsyncClient.resize#long}
     *
     * @param size Resizes a page blob to the specified size. If the specified value is less than the current size of
     * the blob, then all pages above the specified value are cleared.
     * @return A reactive response emitting the resized page blob.
     */
    public Mono<PageBlobItem> resize(long size) {
        try {
            return resizeWithResponse(size, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Resizes the page blob to the specified size (which must be a multiple of 512). For more information, see the <a
     * href="https://docs.microsoft.com/rest/api/storageservices/set-blob-properties">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.PageBlobAsyncClient.resizeWithResponse#long-BlobAccessConditions}
     *
     * @param size Resizes a page blob to the specified size. If the specified value is less than the current size of
     * the blob, then all pages above the specified value are cleared.
     * @param accessConditions {@link BlobAccessConditions}
     * @return A reactive response emitting the resized page blob.
     * @throws IllegalArgumentException If {@code size} isn't a multiple of {@link PageBlobAsyncClient#PAGE_BYTES}
     */
    public Mono<Response<PageBlobItem>> resizeWithResponse(long size, BlobAccessConditions accessConditions) {
        try {
            return withContext(context -> resizeWithResponse(size, accessConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<PageBlobItem>> resizeWithResponse(long size, BlobAccessConditions accessConditions, Context context) {
        if (size % PAGE_BYTES != 0) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw logger.logExceptionAsError(
                new IllegalArgumentException("size must be a multiple of PageBlobAsyncClient.PAGE_BYTES."));
        }
        accessConditions = accessConditions == null ? new BlobAccessConditions() : accessConditions;

        return this.azureBlobStorage.pageBlobs().resizeWithRestResponseAsync(null,
            null, size, null, null, accessConditions.getLeaseAccessConditions(), getCustomerProvidedKey(),
            accessConditions.getModifiedAccessConditions(), context)
            .map(rb -> new SimpleResponse<>(rb, new PageBlobItem(rb.getDeserializedHeaders())));
    }

    /**
     * Sets the page blob's sequence number. For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-properties">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.PageBlobAsyncClient.updateSequenceNumber#SequenceNumberActionType-Long}
     *
     * @param action Indicates how the service should modify the blob's sequence number.
     * @param sequenceNumber The blob's sequence number. The sequence number is a user-controlled property that you can
     * use to track requests and manage concurrency issues.
     * @return A reactive response emitting the updated page blob.
     */
    public Mono<PageBlobItem> updateSequenceNumber(SequenceNumberActionType action, Long sequenceNumber) {
        try {
            return updateSequenceNumberWithResponse(action, sequenceNumber, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Sets the page blob's sequence number. For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-properties">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.PageBlobAsyncClient.updateSequenceNumberWithResponse#SequenceNumberActionType-Long-BlobAccessConditions}
     *
     * @param action Indicates how the service should modify the blob's sequence number.
     * @param sequenceNumber The blob's sequence number. The sequence number is a user-controlled property that you can
     * use to track requests and manage concurrency issues.
     * @param accessConditions {@link BlobAccessConditions}
     * @return A reactive response emitting the updated page blob.
     * @throws IllegalArgumentException If {@code sequenceNumber} isn't null and is less than 0
     */
    public Mono<Response<PageBlobItem>> updateSequenceNumberWithResponse(SequenceNumberActionType action,
        Long sequenceNumber, BlobAccessConditions accessConditions) {
        try {
            return withContext(context ->
                updateSequenceNumberWithResponse(action, sequenceNumber, accessConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<PageBlobItem>> updateSequenceNumberWithResponse(SequenceNumberActionType action, Long sequenceNumber,
        BlobAccessConditions accessConditions, Context context) {
        if (sequenceNumber != null && sequenceNumber < 0) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw logger.logExceptionAsError(
                new IllegalArgumentException("SequenceNumber must be greater than or equal to 0."));
        }
        accessConditions = accessConditions == null ? new BlobAccessConditions() : accessConditions;
        sequenceNumber = action == SequenceNumberActionType.INCREMENT ? null : sequenceNumber;

        return this.azureBlobStorage.pageBlobs().updateSequenceNumberWithRestResponseAsync(null,
                null, action, null, sequenceNumber, null,
                accessConditions.getLeaseAccessConditions(), accessConditions.getModifiedAccessConditions(), context)
            .map(rb -> new SimpleResponse<>(rb, new PageBlobItem(rb.getDeserializedHeaders())));
    }

    /**
     * Begins an operation to start an incremental copy from one page blob's snapshot to this page blob. The snapshot is
     * copied such that only the differential changes between the previously copied snapshot are transferred to the
     * destination. The copied snapshots are complete copies of the original snapshot and can be read or copied from as
     * usual. For more information, see the Azure Docs <a href="https://docs.microsoft.com/rest/api/storageservices/incremental-copy-blob">here</a>
     * and
     * <a href="https://docs.microsoft.com/en-us/azure/virtual-machines/windows/incremental-snapshots">here</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.PageBlobAsyncClient.copyIncremental#String-String}
     *
     * @param source The source page blob.
     * @param snapshot The snapshot on the copy source.
     * @return A reactive response emitting the copy status.
     */
    public Mono<CopyStatusType> copyIncremental(String source, String snapshot) {
        try {
            return copyIncrementalWithResponse(source, snapshot, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Begins an operation to start an incremental copy from one page blob's snapshot to this page blob. The snapshot is
     * copied such that only the differential changes between the previously copied snapshot are transferred to the
     * destination. The copied snapshots are complete copies of the original snapshot and can be read or copied from as
     * usual. For more information, see the Azure Docs <a href="https://docs.microsoft.com/rest/api/storageservices/incremental-copy-blob">here</a>
     * and
     * <a href="https://docs.microsoft.com/en-us/azure/virtual-machines/windows/incremental-snapshots">here</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.PageBlobAsyncClient.copyIncrementalWithResponse#String-String-ModifiedAccessConditions}
     *
     * @param source The source page blob.
     * @param snapshot The snapshot on the copy source.
     * @param modifiedAccessConditions Standard HTTP Access conditions related to the modification of data. ETag and
     * LastModifiedTime are used to construct conditions related to when the blob was changed relative to the given
     * request. The request will fail if the specified condition is not satisfied.
     * @return A reactive response emitting the copy status.
     * @throws IllegalStateException If {@code source} and {@code snapshot} form a malformed URL.
     */
    public Mono<Response<CopyStatusType>> copyIncrementalWithResponse(String source, String snapshot,
        ModifiedAccessConditions modifiedAccessConditions) {
        try {
            return withContext(
                context -> copyIncrementalWithResponse(source, snapshot, modifiedAccessConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<CopyStatusType>> copyIncrementalWithResponse(String source, String snapshot,
        ModifiedAccessConditions modifiedAccessConditions, Context context) {
        UrlBuilder builder = UrlBuilder.parse(source);
        builder.setQueryParameter(Constants.UrlConstants.SNAPSHOT_QUERY_PARAMETER, snapshot);
        URL url;
        try {
            url = builder.toURL();
        } catch (MalformedURLException e) {
            // We are parsing a valid url and adding a query parameter. If this fails, we can't recover.
            throw logger.logExceptionAsError(new IllegalArgumentException(e));
        }
        return this.azureBlobStorage.pageBlobs().copyIncrementalWithRestResponseAsync(
            null, null, url, null, null, modifiedAccessConditions, context)
            .map(rb -> new SimpleResponse<>(rb, rb.getDeserializedHeaders().getCopyStatus()));
    }

    private static String pageRangeToString(PageRange pageRange) {
        if (pageRange.getStart() < 0 || pageRange.getEnd() <= 0) {
            throw new IllegalArgumentException("PageRange's start and end values must be greater than or equal to "
                + "0 if specified.");
        }
        if (pageRange.getStart() % PAGE_BYTES != 0) {
            throw new IllegalArgumentException("PageRange's start value must be a multiple of 512.");
        }
        if (pageRange.getEnd() % PAGE_BYTES != PAGE_BYTES - 1) {
            throw new IllegalArgumentException("PageRange's end value must be 1 less than a multiple of 512.");
        }
        if (pageRange.getEnd() <= pageRange.getStart()) {
            throw new IllegalArgumentException("PageRange's End value must be after the start.");
        }
        return "bytes=" + pageRange.getStart() + '-' + pageRange.getEnd();
    }
}
