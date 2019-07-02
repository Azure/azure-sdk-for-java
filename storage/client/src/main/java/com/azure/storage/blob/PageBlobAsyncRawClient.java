// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.implementation.http.UrlBuilder;
import com.azure.core.util.Context;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.BlobHTTPHeaders;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.Metadata;
import com.azure.storage.blob.models.ModifiedAccessConditions;
import com.azure.storage.blob.models.PageBlobAccessConditions;
import com.azure.storage.blob.models.PageBlobsClearPagesResponse;
import com.azure.storage.blob.models.PageBlobsCopyIncrementalResponse;
import com.azure.storage.blob.models.PageBlobsCreateResponse;
import com.azure.storage.blob.models.PageBlobsGetPageRangesDiffResponse;
import com.azure.storage.blob.models.PageBlobsGetPageRangesResponse;
import com.azure.storage.blob.models.PageBlobsResizeResponse;
import com.azure.storage.blob.models.PageBlobsUpdateSequenceNumberResponse;
import com.azure.storage.blob.models.PageBlobsUploadPagesFromURLResponse;
import com.azure.storage.blob.models.PageBlobsUploadPagesResponse;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.blob.models.SequenceNumberActionType;
import com.azure.storage.blob.models.SourceModifiedAccessConditions;
import io.netty.buffer.ByteBuf;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;

import static com.azure.storage.blob.Utility.postProcessResponse;

/**
 * Represents a URL to a page blob. It may be obtained by direct construction or via the create method on a
 * {@link ContainerAsyncClient} object. This class does not hold any state about a particular blob but is instead a convenient
 * way of sending off appropriate requests to the resource on the service. Please refer to the
 * <a href=https://docs.microsoft.com/en-us/rest/api/storageservices/understanding-block-blobs--append-blobs--and-page-blobs>Azure Docs</a>
 * for more information.
 */
final class PageBlobAsyncRawClient extends BlobAsyncRawClient {

    /**
     * Indicates the number of bytes in a page.
     */
    public static final int PAGE_BYTES = 512;

    /**
     * Indicates the maximum number of bytes that may be sent in a call to putPage.
     */
    public static final int MAX_PUT_PAGES_BYTES = 4 * Constants.MB;

    /**
     * Creates a {@code PageBlobAsyncRawClient} object pointing to the account specified by the URL and using the provided
     * pipeline to make HTTP requests.
     *
     */
    public PageBlobAsyncRawClient(AzureBlobStorageImpl azureBlobStorage, String snapshot) {
        super(azureBlobStorage, snapshot);
    }

    private static String pageRangeToString(PageRange pageRange) {
        if (pageRange.start() < 0 || pageRange.end() <= 0) {
            throw new IllegalArgumentException("PageRange's start and end values must be greater than or equal to "
                    + "0 if specified.");
        }
        if (pageRange.start() % PageBlobAsyncRawClient.PAGE_BYTES != 0) {
            throw new IllegalArgumentException("PageRange's start value must be a multiple of 512.");
        }
        if (pageRange.end() % PageBlobAsyncRawClient.PAGE_BYTES != PageBlobAsyncRawClient.PAGE_BYTES - 1) {
            throw new IllegalArgumentException("PageRange's end value must be 1 less than a multiple of 512.");
        }
        if (pageRange.end() <= pageRange.start()) {
            throw new IllegalArgumentException("PageRange's End value must be after the start.");
        }
        return new StringBuilder("bytes=").append(pageRange.start()).append('-').append(pageRange.end()).toString();
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
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=page_blob_basic "Sample code for PageBlobAsyncRawClient.create")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<PageBlobsCreateResponse> create(long size, Long sequenceNumber, BlobHTTPHeaders headers,
            Metadata metadata, BlobAccessConditions accessConditions) {
        accessConditions = accessConditions == null ? new BlobAccessConditions() : accessConditions;

        if (size % PageBlobAsyncRawClient.PAGE_BYTES != 0) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw new IllegalArgumentException("size must be a multiple of PageBlobAsyncRawClient.PAGE_BYTES.");
        }
        if (sequenceNumber != null && sequenceNumber < 0) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw new IllegalArgumentException("SequenceNumber must be greater than or equal to 0.");
        }
        metadata = metadata == null ? new Metadata() : metadata;

        return postProcessResponse(this.azureBlobStorage.pageBlobs().createWithRestResponseAsync(null,
            null, 0, size, null, metadata, null, null,
            null, sequenceNumber, null, headers, accessConditions.leaseAccessConditions(),
            accessConditions.modifiedAccessConditions(), Context.NONE));
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
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=page_blob_basic "Sample code for PageBlobAsyncRawClient.uploadPages")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<PageBlobsUploadPagesResponse> uploadPages(PageRange pageRange, Flux<ByteBuf> body,
            PageBlobAccessConditions pageBlobAccessConditions) {
        pageBlobAccessConditions = pageBlobAccessConditions == null ? new PageBlobAccessConditions()
                : pageBlobAccessConditions;

        if (pageRange == null) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw new IllegalArgumentException("pageRange cannot be null.");
        }
        String pageRangeStr = pageRangeToString(pageRange);

        return postProcessResponse(this.azureBlobStorage.pageBlobs().uploadPagesWithRestResponseAsync(null,
            null, body, pageRange.end() - pageRange.start() + 1, null,
            null, pageRangeStr, null, null, null, null,
            pageBlobAccessConditions.leaseAccessConditions(), pageBlobAccessConditions.sequenceNumberAccessConditions(),
            pageBlobAccessConditions.modifiedAccessConditions(), Context.NONE));
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
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=page_from_url "Sample code for PageBlobAsyncRawClient.uploadPagesFromURL")]
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<PageBlobsUploadPagesFromURLResponse> uploadPagesFromURL(PageRange range, URL sourceURL, Long sourceOffset,
            byte[] sourceContentMD5, PageBlobAccessConditions destAccessConditions,
            SourceModifiedAccessConditions sourceAccessConditions) {

        if (range == null) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw new IllegalArgumentException("range cannot be null.");
        }

        String rangeString = pageRangeToString(range);

        if (sourceOffset == null) {
            sourceOffset = 0L;
        }

        String sourceRangeString = pageRangeToString(new PageRange().start(sourceOffset).end(sourceOffset + (range.end() - range.start())));

        destAccessConditions = destAccessConditions == null ? new PageBlobAccessConditions() : destAccessConditions;

        return postProcessResponse(this.azureBlobStorage.pageBlobs().uploadPagesFromURLWithRestResponseAsync(
            null, null, sourceURL, sourceRangeString, 0, rangeString, sourceContentMD5,
            null, null, destAccessConditions.leaseAccessConditions(),
            destAccessConditions.sequenceNumberAccessConditions(), destAccessConditions.modifiedAccessConditions(),
            sourceAccessConditions, Context.NONE));
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
     * @param pageBlobAccessConditions
     *         {@link PageBlobAccessConditions}
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=page_blob_basic "Sample code for PageBlobAsyncRawClient.clearPages")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<PageBlobsClearPagesResponse> clearPages(PageRange pageRange,
            PageBlobAccessConditions pageBlobAccessConditions) {
        pageBlobAccessConditions = pageBlobAccessConditions == null ? new PageBlobAccessConditions()
                : pageBlobAccessConditions;
        if (pageRange == null) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw new IllegalArgumentException("pageRange cannot be null.");
        }
        String pageRangeStr = pageRangeToString(pageRange);

        return postProcessResponse(this.azureBlobStorage.pageBlobs().clearPagesWithRestResponseAsync(null,
            null, 0, null, pageRangeStr, null,
            pageBlobAccessConditions.leaseAccessConditions(), pageBlobAccessConditions.sequenceNumberAccessConditions(),
            pageBlobAccessConditions.modifiedAccessConditions(), Context.NONE));
    }

    /**
     * Returns the list of valid page ranges for a page blob or snapshot of a page blob.
     * For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/get-page-ranges">Azure Docs</a>.
     *
     * @param blobRange
     *         {@link BlobRange}
     * @param accessConditions
     *         {@link BlobAccessConditions}
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=page_blob_basic "Sample code for PageBlobAsyncRawClient.getPageRanges")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<PageBlobsGetPageRangesResponse> getPageRanges(BlobRange blobRange,
            BlobAccessConditions accessConditions) {
        blobRange = blobRange == null ? new BlobRange(0) : blobRange;
        accessConditions = accessConditions == null ? new BlobAccessConditions() : accessConditions;

        return postProcessResponse(this.azureBlobStorage.pageBlobs().getPageRangesWithRestResponseAsync(
            null, null, snapshot, null, null, blobRange.toHeaderValue(),
            null, accessConditions.leaseAccessConditions(), accessConditions.modifiedAccessConditions(),
            Context.NONE));
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
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=page_diff "Sample code for PageBlobAsyncRawClient.getPageRangesDiff")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<PageBlobsGetPageRangesDiffResponse> getPageRangesDiff(BlobRange blobRange, String prevSnapshot,
            BlobAccessConditions accessConditions) {
        blobRange = blobRange == null ? new BlobRange(0) : blobRange;
        accessConditions = accessConditions == null ? new BlobAccessConditions() : accessConditions;

        if (prevSnapshot == null) {
            throw new IllegalArgumentException("prevSnapshot cannot be null");
        }

        return postProcessResponse(this.azureBlobStorage.pageBlobs().getPageRangesDiffWithRestResponseAsync(
            null, null, snapshot, null, null, prevSnapshot,
            blobRange.toHeaderValue(), null, accessConditions.leaseAccessConditions(),
            accessConditions.modifiedAccessConditions(), Context.NONE));
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
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=page_blob_basic "Sample code for PageBlobAsyncRawClient.resize")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<PageBlobsResizeResponse> resize(long size, BlobAccessConditions accessConditions) {
        if (size % PageBlobAsyncRawClient.PAGE_BYTES != 0) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw new IllegalArgumentException("size must be a multiple of PageBlobAsyncRawClient.PAGE_BYTES.");
        }
        accessConditions = accessConditions == null ? new BlobAccessConditions() : accessConditions;

        return postProcessResponse(this.azureBlobStorage.pageBlobs().resizeWithRestResponseAsync(null,
            null, size, null, null, accessConditions.leaseAccessConditions(),
            accessConditions.modifiedAccessConditions(), Context.NONE));
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
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=page_blob_basic "Sample code for PageBlobAsyncRawClient.updateSequenceNumber")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<PageBlobsUpdateSequenceNumberResponse> updateSequenceNumber(SequenceNumberActionType action,
            Long sequenceNumber, BlobAccessConditions accessConditions) {
        if (sequenceNumber != null && sequenceNumber < 0) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw new IllegalArgumentException("SequenceNumber must be greater than or equal to 0.");
        }
        accessConditions = accessConditions == null ? new BlobAccessConditions() : accessConditions;
        sequenceNumber = action == SequenceNumberActionType.INCREMENT ? null : sequenceNumber;

        return postProcessResponse(
                this.azureBlobStorage.pageBlobs().updateSequenceNumberWithRestResponseAsync(null,
                    null, action, null, sequenceNumber, null,
                    accessConditions.leaseAccessConditions(), accessConditions.modifiedAccessConditions(), Context.NONE));
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
     *
     * @return Emits the successful response.
     */
    public Mono<PageBlobsCopyIncrementalResponse> copyIncremental(URL source, String snapshot,
            ModifiedAccessConditions modifiedAccessConditions) {

        UrlBuilder builder = UrlBuilder.parse(source);
        builder.setQueryParameter(Constants.SNAPSHOT_QUERY_PARAMETER, snapshot);
        try {
            source = builder.toURL();
        } catch (MalformedURLException e) {
            // We are parsing a valid url and adding a query parameter. If this fails, we can't recover.
            throw new Error(e);
        }
        return postProcessResponse(this.azureBlobStorage.pageBlobs().copyIncrementalWithRestResponseAsync(
            null, null, source, null, null, modifiedAccessConditions, Context.NONE));
    }
}
