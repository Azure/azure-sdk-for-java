// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.implementation.http.UrlBuilder;
import com.azure.core.implementation.util.FluxUtil;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.BlobHTTPHeaders;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.CopyStatusType;
import com.azure.storage.blob.models.CpkInfo;
import com.azure.storage.blob.models.Metadata;
import com.azure.storage.blob.models.ModifiedAccessConditions;
import com.azure.storage.blob.models.PageBlobAccessConditions;
import com.azure.storage.blob.models.PageBlobItem;
import com.azure.storage.blob.models.PageList;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.blob.models.SequenceNumberActionType;
import com.azure.storage.blob.models.SourceModifiedAccessConditions;
import com.azure.storage.common.Constants;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;

import static com.azure.core.implementation.util.FluxUtil.withContext;
import static com.azure.storage.blob.PostProcessor.postProcessResponse;

/**
 * Client to a page blob. It may only be instantiated through a {@link BlobClientBuilder}, via the method {@link
 * BlobAsyncClient#asPageBlobAsyncClient()}, or via the method
 * {@link ContainerAsyncClient#getPageBlobAsyncClient(String)}. This class does not hold any state about a particular
 * blob, but is instead a convenient way of sending appropriate requests to the resource on the service.
 *
 * <p>
 * This client contains operations on a blob. Operations on a container are available on {@link ContainerAsyncClient},
 * and operations on the service are available on {@link BlobServiceAsyncClient}.
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
public final class PageBlobAsyncClient extends BlobAsyncClient {
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
     * Package-private constructor for use by {@link BlobClientBuilder}.
     *
     * @param azureBlobStorage the API client for blob storage
     */
    PageBlobAsyncClient(AzureBlobStorageImpl azureBlobStorage, String snapshot, CpkInfo cpk) {
        super(azureBlobStorage, snapshot, cpk);
    }

    /**
     * Creates a page blob of the specified length. Call PutPage to upload data data to a page blob. For more
     * information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob">Azure Docs</a>.
     *
     * @param size Specifies the maximum size for the page blob, up to 8 TB. The page blob size must be aligned to a
     * 512-byte boundary.
     * @return A reactive response containing the information of the created page blob.
     */
    public Mono<PageBlobItem> setCreate(long size) {
        return createWithResponse(size, null, null, null, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a page blob of the specified length. Call PutPage to upload data data to a page blob. For more
     * information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob">Azure Docs</a>.
     *
     * @param size Specifies the maximum size for the page blob, up to 8 TB. The page blob size must be aligned to a
     * 512-byte boundary.
     * @param sequenceNumber A user-controlled value that you can use to track requests. The value of the sequence
     * number must be between 0 and 2^63 - 1.The default value is 0.
     * @param headers {@link BlobHTTPHeaders}
     * @param metadata {@link Metadata}
     * @param accessConditions {@link BlobAccessConditions}
     * @return A reactive response containing the information of the created page blob.
     * @throws IllegalArgumentException If {@code size} isn't a multiple of {@link PageBlobAsyncClient#PAGE_BYTES} or
     * {@code sequenceNumber} isn't null and is less than 0.
     */
    public Mono<Response<PageBlobItem>> createWithResponse(long size, Long sequenceNumber, BlobHTTPHeaders headers,
        Metadata metadata, BlobAccessConditions accessConditions) {
        return withContext(context ->
            createWithResponse(size, sequenceNumber, headers, metadata, accessConditions, context));
    }

    Mono<Response<PageBlobItem>> createWithResponse(long size, Long sequenceNumber, BlobHTTPHeaders headers,
        Metadata metadata, BlobAccessConditions accessConditions, Context context) {
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
        metadata = metadata == null ? new Metadata() : metadata;

        return postProcessResponse(this.azureBlobStorage.pageBlobs().createWithRestResponseAsync(null,
            null, 0, size, null, metadata, sequenceNumber, null, headers, accessConditions.getLeaseAccessConditions(),
            cpk, accessConditions.getModifiedAccessConditions(), context))
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
     * @param pageRange A {@link PageRange} object. Given that pages must be aligned with 512-byte boundaries, the start
     * offset must be a modulus of 512 and the end offset must be a modulus of 512 - 1. Examples of valid byte ranges
     * are 0-511, 512-1023, etc.
     * @param body The data to upload. Note that this {@code Flux} must be replayable if retries are enabled (the
     * default). In other words, the Flowable must produce the same data each time it is subscribed to.
     * @return A reactive response containing the information of the uploaded pages.
     */
    public Mono<PageBlobItem> uploadPages(PageRange pageRange, Flux<ByteBuffer> body) {
        return uploadPagesWithResponse(pageRange, body, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Writes 1 or more pages to the page blob. The start and end offsets must be a multiple of 512. For more
     * information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-page">Azure Docs</a>.
     * <p>
     * Note that the data passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flux} must produce the same data each time it is subscribed to.
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
        return withContext(context -> uploadPagesWithResponse(pageRange, body, pageBlobAccessConditions, context));
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

        return postProcessResponse(this.azureBlobStorage.pageBlobs().uploadPagesWithRestResponseAsync(null,
            null, body, pageRange.getEnd() - pageRange.getStart() + 1, null, null, null, pageRangeStr, null,
            pageBlobAccessConditions.getLeaseAccessConditions(), cpk,
            pageBlobAccessConditions.getSequenceNumberAccessConditions(),
            pageBlobAccessConditions.getModifiedAccessConditions(), context))
            .map(rb -> new SimpleResponse<>(rb, new PageBlobItem(rb.getDeserializedHeaders())));
    }

    /**
     * Writes 1 or more pages from the source page blob to this page blob. The start and end offsets must be a multiple
     * of 512. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-page">Azure Docs</a>.
     * <p>
     *
     * @param range A {@link PageRange} object. Given that pages must be aligned with 512-byte boundaries, the start
     * offset must be a modulus of 512 and the end offset must be a modulus of 512 - 1. Examples of valid byte ranges
     * are 0-511, 512-1023, etc.
     * @param sourceURL The url to the blob that will be the source of the copy.  A source blob in the same storage
     * account can be authenticated via Shared Key. However, if the source is a blob in another account, the source blob
     * must either be public or must be authenticated via a shared access signature. If the source blob is public, no
     * authentication is required to perform the operation.
     * @param sourceOffset The source offset to copy from.  Pass null or 0 to copy from the beginning of source page
     * blob.
     * @return A reactive response containing the information of the uploaded pages.
     */
    public Mono<PageBlobItem> uploadPagesFromURL(PageRange range, URL sourceURL, Long sourceOffset) {
        return uploadPagesFromURLWithResponse(range, sourceURL, sourceOffset, null, null, null)
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Writes 1 or more pages from the source page blob to this page blob. The start and end offsets must be a multiple
     * of 512. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-page">Azure Docs</a>.
     * <p>
     *
     * @param range The destination {@link PageRange} range. Given that pages must be aligned with 512-byte boundaries,
     * the start offset must be a modulus of 512 and the end offset must be a modulus of 512 - 1. Examples of valid byte
     * ranges are 0-511, 512-1023, etc.
     * @param sourceURL The url to the blob that will be the source of the copy.  A source blob in the same storage
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
    public Mono<Response<PageBlobItem>> uploadPagesFromURLWithResponse(PageRange range, URL sourceURL,
        Long sourceOffset, byte[] sourceContentMD5, PageBlobAccessConditions destAccessConditions,
        SourceModifiedAccessConditions sourceAccessConditions) {
        return withContext(context -> uploadPagesFromURLWithResponse(range, sourceURL, sourceOffset, sourceContentMD5,
            destAccessConditions, sourceAccessConditions, context));
    }

    Mono<Response<PageBlobItem>> uploadPagesFromURLWithResponse(PageRange range, URL sourceURL, Long sourceOffset,
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

        return postProcessResponse(this.azureBlobStorage.pageBlobs().uploadPagesFromURLWithRestResponseAsync(
            null, null, sourceURL, sourceRangeString, 0, rangeString, sourceContentMD5, null, null, null, cpk,
            destAccessConditions.getLeaseAccessConditions(), destAccessConditions.getSequenceNumberAccessConditions(),
            destAccessConditions.getModifiedAccessConditions(), sourceAccessConditions, context))
            .map(rb -> new SimpleResponse<>(rb, new PageBlobItem(rb.getDeserializedHeaders(),
                rb.getHeaders().value("x-ms-encryption-key-sha256"))));
    }

    /**
     * Frees the specified pages from the page blob. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-page">Azure Docs</a>.
     *
     * @param pageRange A {@link PageRange} object. Given that pages must be aligned with 512-byte boundaries, the start
     * offset must be a modulus of 512 and the end offset must be a modulus of 512 - 1. Examples of valid byte ranges
     * are 0-511, 512-1023, etc.
     * @return A reactive response containing the information of the cleared pages.
     */
    public Mono<PageBlobItem> clearPages(PageRange pageRange) {
        return clearPagesWithResponse(pageRange, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Frees the specified pages from the page blob. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-page">Azure Docs</a>.
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
        return withContext(context -> clearPagesWithResponse(pageRange, pageBlobAccessConditions, context));
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

        return postProcessResponse(this.azureBlobStorage.pageBlobs().clearPagesWithRestResponseAsync(null,
            null, 0, null, pageRangeStr, null,
            pageBlobAccessConditions.getLeaseAccessConditions(), cpk,
            pageBlobAccessConditions.getSequenceNumberAccessConditions(),
            pageBlobAccessConditions.getModifiedAccessConditions(), context))
            .map(rb -> new SimpleResponse<>(rb, new PageBlobItem(rb.getDeserializedHeaders(),
                rb.getHeaders().value("x-ms-request-server-encrypted"),
                rb.getHeaders().value("x-ms-encryption-key-sha256"))));
    }

    /**
     * Returns the list of valid page ranges for a page blob or snapshot of a page blob. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-page-ranges">Azure Docs</a>.
     *
     * @param blobRange {@link BlobRange}
     * @return A reactive response containing the information of the cleared pages.
     */
    public Mono<PageList> getPageRanges(BlobRange blobRange) {
        return getPageRangesWithResponse(blobRange, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Returns the list of valid page ranges for a page blob or snapshot of a page blob. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-page-ranges">Azure Docs</a>.
     *
     * @param blobRange {@link BlobRange}
     * @param accessConditions {@link BlobAccessConditions}
     * @return A reactive response emitting all the page ranges.
     */
    public Mono<Response<PageList>> getPageRangesWithResponse(BlobRange blobRange,
        BlobAccessConditions accessConditions) {
        return withContext(context -> getPageRangesWithResponse(blobRange, accessConditions, context));
    }

    Mono<Response<PageList>> getPageRangesWithResponse(BlobRange blobRange, BlobAccessConditions accessConditions,
        Context context) {
        blobRange = blobRange == null ? new BlobRange(0) : blobRange;
        accessConditions = accessConditions == null ? new BlobAccessConditions() : accessConditions;

        return postProcessResponse(this.azureBlobStorage.pageBlobs().getPageRangesWithRestResponseAsync(
            null, null, snapshot, null, blobRange.toHeaderValue(),
            null, accessConditions.getLeaseAccessConditions(), accessConditions.getModifiedAccessConditions(),
            context)).map(response -> new SimpleResponse<>(response, response.getValue()));
    }

    /**
     * Gets the collection of page ranges that differ between a specified snapshot and this page blob. For more
     * information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/get-page-ranges">Azure
     * Docs</a>.
     *
     * @param blobRange {@link BlobRange}
     * @param prevSnapshot Specifies that the response will contain only pages that were changed between target blob and
     * previous snapshot. Changed pages include both updated and cleared pages. The target blob may be a snapshot, as
     * long as the snapshot specified by prevsnapshot is the older of the two.
     * @return A reactive response emitting all the different page ranges.
     */
    public Mono<PageList> getPageRangesDiff(BlobRange blobRange, String prevSnapshot) {
        return getPageRangesDiffWithResponse(blobRange, prevSnapshot, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Gets the collection of page ranges that differ between a specified snapshot and this page blob. For more
     * information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/get-page-ranges">Azure
     * Docs</a>.
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
        return withContext(context ->
            getPageRangesDiffWithResponse(blobRange, prevSnapshot, accessConditions, context));
    }

    Mono<Response<PageList>> getPageRangesDiffWithResponse(BlobRange blobRange, String prevSnapshot,
        BlobAccessConditions accessConditions, Context context) {
        blobRange = blobRange == null ? new BlobRange(0) : blobRange;
        accessConditions = accessConditions == null ? new BlobAccessConditions() : accessConditions;

        if (prevSnapshot == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("prevSnapshot cannot be null"));
        }

        return postProcessResponse(this.azureBlobStorage.pageBlobs().getPageRangesDiffWithRestResponseAsync(
            null, null, snapshot, null, prevSnapshot,
            blobRange.toHeaderValue(), null, accessConditions.getLeaseAccessConditions(),
            accessConditions.getModifiedAccessConditions(), context))
            .map(response -> new SimpleResponse<>(response, response.getValue()));
    }

    /**
     * Resizes the page blob to the specified size (which must be a multiple of 512). For more information, see the <a
     * href="https://docs.microsoft.com/rest/api/storageservices/set-blob-properties">Azure Docs</a>.
     *
     * @param size Resizes a page blob to the specified size. If the specified value is less than the current size of
     * the blob, then all pages above the specified value are cleared.
     * @return A reactive response emitting the resized page blob.
     */
    public Mono<PageBlobItem> resize(long size) {
        return resizeWithResponse(size, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Resizes the page blob to the specified size (which must be a multiple of 512). For more information, see the <a
     * href="https://docs.microsoft.com/rest/api/storageservices/set-blob-properties">Azure Docs</a>.
     *
     * @param size Resizes a page blob to the specified size. If the specified value is less than the current size of
     * the blob, then all pages above the specified value are cleared.
     * @param accessConditions {@link BlobAccessConditions}
     * @return A reactive response emitting the resized page blob.
     * @throws IllegalArgumentException If {@code size} isn't a multiple of {@link PageBlobAsyncClient#PAGE_BYTES}
     */
    public Mono<Response<PageBlobItem>> resizeWithResponse(long size, BlobAccessConditions accessConditions) {
        return withContext(context -> resizeWithResponse(size, accessConditions, context));
    }

    Mono<Response<PageBlobItem>> resizeWithResponse(long size, BlobAccessConditions accessConditions, Context context) {
        if (size % PAGE_BYTES != 0) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw logger.logExceptionAsError(
                new IllegalArgumentException("size must be a multiple of PageBlobAsyncClient.PAGE_BYTES."));
        }
        accessConditions = accessConditions == null ? new BlobAccessConditions() : accessConditions;

        return postProcessResponse(this.azureBlobStorage.pageBlobs().resizeWithRestResponseAsync(null,
            null, size, null, null, accessConditions.getLeaseAccessConditions(), cpk,
            accessConditions.getModifiedAccessConditions(), context))
            .map(rb -> new SimpleResponse<>(rb, new PageBlobItem(rb.getDeserializedHeaders())));
    }

    /**
     * Sets the page blob's sequence number. For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-properties">Azure
     * Docs</a>.
     *
     * @param action Indicates how the service should modify the blob's sequence number.
     * @param sequenceNumber The blob's sequence number. The sequence number is a user-controlled property that you can
     * use to track requests and manage concurrency issues.
     * @return A reactive response emitting the updated page blob.
     */
    public Mono<PageBlobItem> updateSequenceNumber(SequenceNumberActionType action, Long sequenceNumber) {
        return updateSequenceNumberWithResponse(action, sequenceNumber, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Sets the page blob's sequence number. For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-properties">Azure
     * Docs</a>.
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
        return withContext(context ->
            updateSequenceNumberWithResponse(action, sequenceNumber, accessConditions, context));
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

        return postProcessResponse(
            this.azureBlobStorage.pageBlobs().updateSequenceNumberWithRestResponseAsync(null,
                null, action, null, sequenceNumber, null,
                accessConditions.getLeaseAccessConditions(), accessConditions.getModifiedAccessConditions(), context))
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
     * @param source The source page blob.
     * @param snapshot The snapshot on the copy source.
     * @return A reactive response emitting the copy status.
     */
    public Mono<CopyStatusType> copyIncremental(URL source, String snapshot) {
        return copyIncrementalWithResponse(source, snapshot, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Begins an operation to start an incremental copy from one page blob's snapshot to this page blob. The snapshot is
     * copied such that only the differential changes between the previously copied snapshot are transferred to the
     * destination. The copied snapshots are complete copies of the original snapshot and can be read or copied from as
     * usual. For more information, see the Azure Docs <a href="https://docs.microsoft.com/rest/api/storageservices/incremental-copy-blob">here</a>
     * and
     * <a href="https://docs.microsoft.com/en-us/azure/virtual-machines/windows/incremental-snapshots">here</a>.
     *
     * @param source The source page blob.
     * @param snapshot The snapshot on the copy source.
     * @param modifiedAccessConditions Standard HTTP Access conditions related to the modification of data. ETag and
     * LastModifiedTime are used to construct conditions related to when the blob was changed relative to the given
     * request. The request will fail if the specified condition is not satisfied.
     * @return A reactive response emitting the copy status.
     * @throws Error If {@code source} and {@code snapshot} form a malformed URL.
     */
    public Mono<Response<CopyStatusType>> copyIncrementalWithResponse(URL source, String snapshot,
        ModifiedAccessConditions modifiedAccessConditions) {
        return withContext(context -> copyIncrementalWithResponse(source, snapshot, modifiedAccessConditions, context));
    }

    Mono<Response<CopyStatusType>> copyIncrementalWithResponse(URL source, String snapshot,
        ModifiedAccessConditions modifiedAccessConditions, Context context) {
        UrlBuilder builder = UrlBuilder.parse(source);
        builder.setQueryParameter(Constants.SNAPSHOT_QUERY_PARAMETER, snapshot);
        try {
            source = builder.toURL();
        } catch (MalformedURLException e) {
            // We are parsing a valid url and adding a query parameter. If this fails, we can't recover.
            throw new Error(e);
        }
        return postProcessResponse(this.azureBlobStorage.pageBlobs().copyIncrementalWithRestResponseAsync(
            null, null, source, null, null, modifiedAccessConditions, context))
            .map(rb -> new SimpleResponse<>(rb, rb.getDeserializedHeaders().getCopyStatus()));
    }

    /**
     * Get the page blob name.
     *
     * @return The name of the page blob.
     */
    public String getName() {
        try {
            return URLParser.parse(new URL(this.azureBlobStorage.getUrl())).getBlobName();
        } catch (MalformedURLException e) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Please double check the URL format. URL: "
                + this.azureBlobStorage.getUrl()));
        }
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
