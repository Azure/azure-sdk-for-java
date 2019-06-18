// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.util.Context;
import com.azure.storage.blob.implementation.AzureBlobStorageBuilder;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.models.BlobHTTPHeaders;
import com.azure.storage.blob.models.CopyStatusType;
import com.azure.storage.blob.models.ModifiedAccessConditions;
import com.azure.storage.blob.models.PageBlobClearPagesHeaders;
import com.azure.storage.blob.models.PageBlobCreateHeaders;
import com.azure.storage.blob.models.PageBlobResizeHeaders;
import com.azure.storage.blob.models.PageBlobUpdateSequenceNumberHeaders;
import com.azure.storage.blob.models.PageBlobUploadPagesFromURLHeaders;
import com.azure.storage.blob.models.PageBlobUploadPagesHeaders;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.blob.models.SequenceNumberActionType;
import com.azure.storage.blob.models.SourceModifiedAccessConditions;
import io.netty.buffer.ByteBuf;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URL;

/**
 * Client to a page blob. It may only be instantiated through a {@link PageBlobClientBuilder}, via
 * the method {@link BlobAsyncClient#asPageBlobAsyncClient()}, or via the method
 * {@link ContainerAsyncClient#getPageBlobAsyncClient(String)}. This class does not hold
 * any state about a particular blob, but is instead a convenient way of sending appropriate
 * requests to the resource on the service.
 *
 * <p>
 * This client contains operations on a blob. Operations on a container are available on {@link ContainerAsyncClient},
 * and operations on the service are available on {@link StorageAsyncClient}.
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
public final class PageBlobAsyncClient extends BlobAsyncClient {

    private PageBlobAsyncRawClient pageBlobAsyncRawClient;

    /**
     * Indicates the number of bytes in a page.
     */
    public static final int PAGE_BYTES = 512;

    /**
     * Indicates the maximum number of bytes that may be sent in a call to putPage.
     */
    public static final int MAX_PUT_PAGES_BYTES = 4 * Constants.MB;

    /**
     * Package-private constructor for use by {@link PageBlobClientBuilder}.
     * @param azureBlobStorageBuilder the API client builder for blob storage API
     */
    PageBlobAsyncClient(AzureBlobStorageBuilder azureBlobStorageBuilder) {
        super(azureBlobStorageBuilder);
        this.pageBlobAsyncRawClient = new PageBlobAsyncRawClient(azureBlobStorageBuilder.build());
    }

    /**
     * @return a new client {@link PageBlobClientBuilder} instance.
     */
    public static PageBlobClientBuilder builder() {
        return new PageBlobClientBuilder();
    }


    /**
     * Creates a page blob of the specified length. Call PutPage to upload data data to a page blob.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob">Azure Docs</a>.
     *
     * @param size
     *         Specifies the maximum size for the page blob, up to 8 TB. The page blob size must be aligned to a
     *         512-byte boundary.
     *
     * @return
     *      A reactive response containing the information of the created page blob.
     */
    public Mono<PageBlobCreateHeaders> create(long size) {
        return this.create(size, null, null, null, null, null);
    }

    /**
     * Creates a page blob of the specified length. Call PutPage to upload data data to a page blob.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob">Azure Docs</a>.
     *
     * @param size
     *         Specifies the maximum size for the page blob, up to 8 TB. The page blob size must be aligned to a
     *         512-byte boundary.
     * @param sequenceNumber
     *         A user-controlled value that you can use to track requests. The value of the sequence number must be
     *         between 0 and 2^63 - 1.The default value is 0.
     * @param headers
     *         {@link BlobHTTPHeaders}
     * @param metadata
     *         {@link Metadata}
     * @param accessConditions
     *         {@link BlobAccessConditions}
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to
     *         its parent, forming a linked list.
     *
     * @return
     *      A reactive response containing the information of the created page blob.
     */
    public Mono<PageBlobCreateHeaders> create(long size, Long sequenceNumber, BlobHTTPHeaders headers,
            Metadata metadata, BlobAccessConditions accessConditions, Context context) {
        return pageBlobAsyncRawClient
            .create(size, sequenceNumber, headers, metadata, accessConditions, context)
            .map(ResponseBase::deserializedHeaders);
    }

    /**
     * Writes 1 or more pages to the page blob. The start and end offsets must be a multiple of 512.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-page">Azure Docs</a>.
     * <p>
     * Note that the data passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flux} must produce the same data each time it is subscribed to.
     *
     * @param pageRange
     *         A {@link PageRange} object. Given that pages must be aligned with 512-byte boundaries, the start offset must
     *         be a modulus of 512 and the end offset must be a modulus of 512 - 1. Examples of valid byte ranges are
     *         0-511, 512-1023, etc.
     * @param body
     *         The data to upload. Note that this {@code Flux} must be replayable if retries are enabled
     *         (the default). In other words, the Flowable must produce the same data each time it is subscribed to.
     *
     * @return
     *      A reactive response containing the information of the uploaded pages.
     */
    public Mono<PageBlobUploadPagesHeaders> uploadPages(PageRange pageRange, Flux<ByteBuf> body) {
        return this.uploadPages(pageRange, body, null, null);
    }

    /**
     * Writes 1 or more pages to the page blob. The start and end offsets must be a multiple of 512.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-page">Azure Docs</a>.
     * <p>
     * Note that the data passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flux} must produce the same data each time it is subscribed to.
     *
     * @param pageRange
     *         A {@link PageRange} object. Given that pages must be aligned with 512-byte boundaries, the start offset
     *         must be a modulus of 512 and the end offset must be a modulus of 512 - 1. Examples of valid byte ranges
     *         are 0-511, 512-1023, etc.
     * @param body
     *         The data to upload. Note that this {@code Flux} must be replayable if retries are enabled
     *         (the default). In other words, the Flowable must produce the same data each time it is subscribed to.
     * @param pageBlobAccessConditions
     *         {@link PageBlobAccessConditions}
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to
     *         its parent, forming a linked list.
     *
     * @return
     *      A reactive response containing the information of the uploaded pages.
     */
    public Mono<PageBlobUploadPagesHeaders> uploadPages(PageRange pageRange, Flux<ByteBuf> body,
            PageBlobAccessConditions pageBlobAccessConditions, Context context) {
        return pageBlobAsyncRawClient
            .uploadPages(pageRange, body, pageBlobAccessConditions, context)
            .map(ResponseBase::deserializedHeaders);
    }

    /**
     * Writes 1 or more pages from the source page blob to this page blob. The start and end offsets must be a multiple
     * of 512.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-page">Azure Docs</a>.
     * <p>
     *
     * @param range
     *          A {@link PageRange} object. Given that pages must be aligned with 512-byte boundaries, the start offset
     *          must be a modulus of 512 and the end offset must be a modulus of 512 - 1. Examples of valid byte ranges
     *          are 0-511, 512-1023, etc.
     * @param sourceURL
     *          The url to the blob that will be the source of the copy.  A source blob in the same storage account can be
     *          authenticated via Shared Key. However, if the source is a blob in another account, the source blob must
     *          either be public or must be authenticated via a shared access signature. If the source blob is public, no
     *          authentication is required to perform the operation.
     * @param sourceOffset
     *          The source offset to copy from.  Pass null or 0 to copy from the beginning of source page blob.
     *
     * @return
     *      A reactive response containing the information of the uploaded pages.
     */
    public Mono<PageBlobUploadPagesFromURLHeaders> uploadPagesFromURL(PageRange range, URL sourceURL, Long sourceOffset) {
        return this.uploadPagesFromURL(range, sourceURL, sourceOffset, null, null,
                null, null);
    }

    /**
     * Writes 1 or more pages from the source page blob to this page blob. The start and end offsets must be a multiple
     * of 512.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-page">Azure Docs</a>.
     * <p>
     *
     * @param range
     *          The destination {@link PageRange} range. Given that pages must be aligned with 512-byte boundaries, the start offset
     *          must be a modulus of 512 and the end offset must be a modulus of 512 - 1. Examples of valid byte ranges
     *          are 0-511, 512-1023, etc.
     * @param sourceURL
     *          The url to the blob that will be the source of the copy.  A source blob in the same storage account can be
     *          authenticated via Shared Key. However, if the source is a blob in another account, the source blob must
     *          either be public or must be authenticated via a shared access signature. If the source blob is public, no
     *          authentication is required to perform the operation.
     * @param sourceOffset
     *          The source offset to copy from.  Pass null or 0 to copy from the beginning of source blob.
     * @param sourceContentMD5
     *          An MD5 hash of the block content from the source blob. If specified, the service will calculate the MD5
     *          of the received data and fail the request if it does not match the provided MD5.
     * @param destAccessConditions
     *          {@link PageBlobAccessConditions}
     * @param sourceAccessConditions
     *          {@link SourceModifiedAccessConditions}
     * @param context
     *          {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *          {@link HttpPipeline}'s policy objects. Most applications do not need to pass
     *          arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *          immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to
     *          its parent, forming a linked list.
     *
     * @return
     *      A reactive response containing the information of the uploaded pages.
     */
    public Mono<PageBlobUploadPagesFromURLHeaders> uploadPagesFromURL(PageRange range, URL sourceURL, Long sourceOffset,
            byte[] sourceContentMD5, PageBlobAccessConditions destAccessConditions,
            SourceModifiedAccessConditions sourceAccessConditions, Context context) {

        return pageBlobAsyncRawClient
            .uploadPagesFromURL(range, sourceURL, sourceOffset, sourceContentMD5, destAccessConditions, sourceAccessConditions, context)
            .map(ResponseBase::deserializedHeaders);
    }

    /**
     * Frees the specified pages from the page blob.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-page">Azure Docs</a>.
     *
     * @param pageRange
     *         A {@link PageRange} object. Given that pages must be aligned with 512-byte boundaries, the start offset
     *         must be a modulus of 512 and the end offset must be a modulus of 512 - 1. Examples of valid byte ranges
     *         are 0-511, 512-1023, etc.
     *
     * @return
     *      A reactive response containing the information of the cleared pages.
     */
    public Mono<PageBlobClearPagesHeaders> clearPages(PageRange pageRange) {
        return this.clearPages(pageRange, null, null);
    }

    /**
     * Frees the specified pages from the page blob.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-page">Azure Docs</a>.
     *
     * @param pageRange
     *         A {@link PageRange} object. Given that pages must be aligned with 512-byte boundaries, the start offset
     *         must be a modulus of 512 and the end offset must be a modulus of 512 - 1. Examples of valid byte ranges
     *         are 0-511, 512-1023, etc.
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to
     *         its parent, forming a linked list.
     * @param pageBlobAccessConditions
     *         {@link PageBlobAccessConditions}
     *
     * @return
     *      A reactive response containing the information of the cleared pages.
     */
    public Mono<PageBlobClearPagesHeaders> clearPages(PageRange pageRange,
            PageBlobAccessConditions pageBlobAccessConditions, Context context) {
        return pageBlobAsyncRawClient
            .clearPages(pageRange, pageBlobAccessConditions, context)
            .map(ResponseBase::deserializedHeaders);
    }

    /**
     * Returns the list of valid page ranges for a page blob or snapshot of a page blob.
     * For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/get-page-ranges">Azure Docs</a>.
     *
     * @param blobRange
     *         {@link BlobRange}
     *
     * @return
     *      A reactive response containing the information of the cleared pages.
     */
    public Flux<PageRange> getPageRanges(BlobRange blobRange) {
        return this.getPageRanges(blobRange, null, null);
    }

    /**
     * Returns the list of valid page ranges for a page blob or snapshot of a page blob.
     * For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/get-page-ranges">Azure Docs</a>.
     *
     * @param blobRange
     *         {@link BlobRange}
     * @param accessConditions
     *         {@link BlobAccessConditions}
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to
     *         its parent, forming a linked list.
     *
     * @return
     *      A reactive response emitting all the page ranges.
     */
    public Flux<PageRange> getPageRanges(BlobRange blobRange,
                                                            BlobAccessConditions accessConditions, Context context) {
        return pageBlobAsyncRawClient
            .getPageRanges(blobRange, accessConditions, context)
            .flatMapMany(response -> Flux.fromIterable(response.value().pageRange()));
    }

    /**
     * Gets the collection of page ranges that differ between a specified snapshot and this page blob.
     * For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/get-page-ranges">Azure Docs</a>.
     *
     * @param blobRange
     *         {@link BlobRange}
     * @param prevSnapshot
     *         Specifies that the response will contain only pages that were changed between target blob and previous
     *         snapshot. Changed pages include both updated and cleared pages. The target
     *         blob may be a snapshot, as long as the snapshot specified by prevsnapshot is the older of the two.
     *
     * @return
     *      A reactive response emitting all the different page ranges.
     */
    public Flux<PageRange> getPageRangesDiff(BlobRange blobRange, String prevSnapshot) {
        return this.getPageRangesDiff(blobRange, prevSnapshot, null, null);
    }

    /**
     * Gets the collection of page ranges that differ between a specified snapshot and this page blob.
     * For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/get-page-ranges">Azure Docs</a>.
     *
     * @param blobRange
     *         {@link BlobRange}
     * @param prevSnapshot
     *         Specifies that the response will contain only pages that were changed between target blob and previous
     *         snapshot. Changed pages include both updated and cleared pages. The target
     *         blob may be a snapshot, as long as the snapshot specified by prevsnapshot is the older of the two.
     * @param accessConditions
     *         {@link BlobAccessConditions}
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to
     *         its parent, forming a linked list.
     *
     * @return
     *      A reactive response emitting all the different page ranges.
     */
    public Flux<PageRange> getPageRangesDiff(BlobRange blobRange, String prevSnapshot,
            BlobAccessConditions accessConditions, Context context) {
        return pageBlobAsyncRawClient
            .getPageRangesDiff(blobRange, prevSnapshot, accessConditions, context)
            .flatMapMany(response -> Flux.fromIterable(response.value().pageRange()));
    }

    /**
     * Resizes the page blob to the specified size (which must be a multiple of 512).
     * For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-properties">Azure Docs</a>.
     *
     * @param size
     *         Resizes a page blob to the specified size. If the specified value is less than the current size of the
     *         blob, then all pages above the specified value are cleared.
     *
     * @return
     *      A reactive response emitting the resized page blob.
     */
    public Mono<PageBlobResizeHeaders> resize(long size) {
        return this.resize(size, null, null);
    }

    /**
     * Resizes the page blob to the specified size (which must be a multiple of 512).
     * For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-properties">Azure Docs</a>.
     *
     * @param size
     *         Resizes a page blob to the specified size. If the specified value is less than the current size of the
     *         blob, then all pages above the specified value are cleared.
     * @param accessConditions
     *         {@link BlobAccessConditions}
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to
     *         its parent, forming a linked list.
     *
     * @return
     *      A reactive response emitting the resized page blob.
     */
    public Mono<PageBlobResizeHeaders> resize(long size, BlobAccessConditions accessConditions, Context context) {
        return pageBlobAsyncRawClient
            .resize(size, accessConditions, context)
            .map(ResponseBase::deserializedHeaders);
    }

    /**
     * Sets the page blob's sequence number.
     * For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-properties">Azure Docs</a>.
     *
     * @param action
     *         Indicates how the service should modify the blob's sequence number.
     * @param sequenceNumber
     *         The blob's sequence number. The sequence number is a user-controlled property that you can use to track
     *         requests and manage concurrency issues.
     *
     * @return
     *      A reactive response emitting the updated page blob.
     */
    public Mono<PageBlobUpdateSequenceNumberHeaders> updateSequenceNumber(SequenceNumberActionType action,
            Long sequenceNumber) {
        return this.updateSequenceNumber(action, sequenceNumber, null, null);
    }

    /**
     * Sets the page blob's sequence number.
     * For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-properties">Azure Docs</a>.
     *
     * @param action
     *         Indicates how the service should modify the blob's sequence number.
     * @param sequenceNumber
     *         The blob's sequence number. The sequence number is a user-controlled property that you can use to track
     *         requests and manage concurrency issues.
     * @param accessConditions
     *         {@link BlobAccessConditions}
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to
     *         its parent, forming a linked list.
     *
     * @return
     *      A reactive response emitting the updated page blob.
     */
    public Mono<PageBlobUpdateSequenceNumberHeaders> updateSequenceNumber(SequenceNumberActionType action,
            Long sequenceNumber, BlobAccessConditions accessConditions, Context context) {
        return pageBlobAsyncRawClient
            .updateSequenceNumber(action, sequenceNumber, accessConditions, context)
            .map(ResponseBase::deserializedHeaders);
    }

    /**
     * Begins an operation to start an incremental copy from one page blob's snapshot to this page
     * blob. The snapshot is copied such that only the differential changes between the previously copied snapshot are
     * transferred to the destination. The copied snapshots are complete copies of the original snapshot and can be read
     * or copied from as usual. For more information, see
     * the Azure Docs <a href="https://docs.microsoft.com/rest/api/storageservices/incremental-copy-blob">here</a> and
     * <a href="https://docs.microsoft.com/en-us/azure/virtual-machines/windows/incremental-snapshots">here</a>.
     *
     * @param source
     *         The source page blob.
     * @param snapshot
     *         The snapshot on the copy source.
     *
     * @return
     *      A reactive response emitting the copy status.
     */
    public Mono<CopyStatusType> copyIncremental(URL source, String snapshot) {
        return this.copyIncremental(source, snapshot, null, null);
    }

    /**
     * Begins an operation to start an incremental copy from one page blob's snapshot to this page
     * blob. The snapshot is copied such that only the differential changes between the previously copied snapshot are
     * transferred to the destination. The copied snapshots are complete copies of the original snapshot and can be read
     * or copied from as usual. For more information, see
     * the Azure Docs <a href="https://docs.microsoft.com/rest/api/storageservices/incremental-copy-blob">here</a> and
     * <a href="https://docs.microsoft.com/en-us/azure/virtual-machines/windows/incremental-snapshots">here</a>.
     *
     * @param source
     *         The source page blob.
     * @param snapshot
     *         The snapshot on the copy source.
     * @param modifiedAccessConditions
     *         Standard HTTP Access conditions related to the modification of data. ETag and LastModifiedTime are used
     *         to construct conditions related to when the blob was changed relative to the given request. The request
     *         will fail if the specified condition is not satisfied.
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *         parent, forming a linked list.
     *
     * @return
     *      A reactive response emitting the copy status.
     */
    public Mono<CopyStatusType> copyIncremental(URL source, String snapshot,
            ModifiedAccessConditions modifiedAccessConditions, Context context) {
        return pageBlobAsyncRawClient
            .copyIncremental(source, snapshot, modifiedAccessConditions, context)
            .map(response -> response.deserializedHeaders().copyStatus());
    }
}
