/**
 * Copyright Microsoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.azure.storage.blob;

import com.microsoft.azure.storage.blob.models.*;
import com.microsoft.rest.v2.RestResponse;
import com.microsoft.rest.v2.http.HttpPipeline;
import com.microsoft.rest.v2.http.UrlBuilder;
import io.reactivex.Flowable;
import io.reactivex.Single;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * Represents a URL to a page blob. It may be obtained by direct construction or via the create method on a
 * {@link ContainerURL} object. This class does not hold any state about a particular blob but is instead a convenient
 * way of sending off appropriate requests to the resource on the service. Please refer to the following for more
 * information on page blobs:
 * https://docs.microsoft.com/en-us/rest/api/storageservices/understanding-block-blobs--append-blobs--and-page-blobs
 */
public final class PageBlobURL extends BlobURL {

    /**
     * Indicates the number of bytes in a page.
     */
    public static final int PAGE_BYTES = 512;

    /**
     * Indicates the maximum number of bytes that may be sent in a call to putPage.
     */
    public static final int MAX_PUT_PAGES_BYTES = 4 * Constants.MB;

    /**
     * Creates a new {@link PageBlobURL} object.
     *
     * @param url
     *      A {@code java.net.URL} to a page blob.
     * @param pipeline
     *      A {@link HttpPipeline} for sending requests.
     */
    public PageBlobURL(URL url, HttpPipeline pipeline) {
        super( url, pipeline);
    }

    /**
     * Creates a new {@link PageBlobURL} with the given pipeline.
     *
     * @param pipeline
     *      A {@link HttpPipeline} object to set.
     * @return
     *      A {@link PageBlobURL} object with the given pipeline.
     */
    public PageBlobURL withPipeline(HttpPipeline pipeline) {
        try {
            return new PageBlobURL(new URL(this.storageClient.url()), pipeline);
        } catch (MalformedURLException e) {
            // TODO: remove
        }
        return null;
    }

    /**
     * Creates a new {@link PageBlobURL} with the given snapshot.
     *
     * @param snapshot
     *      A {@code String} of the snapshot id.
     * @return
     *      A {@link PageBlobURL} object with the given pipeline.
     */
    public PageBlobURL withSnapshot(String snapshot) throws MalformedURLException, UnknownHostException {
        BlobURLParts blobURLParts = URLParser.parse(new URL(this.storageClient.url()));
        blobURLParts.snapshot = snapshot;
        return new PageBlobURL(blobURLParts.toURL(), super.storageClient.httpPipeline());
    }

    /**
     * Creates a page blob of the specified length. Call PutPage to upload data data to a page blob.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob">Azure Docs</a>.
     *
     * @param size
     *      Specifies the maximum size for the page blob, up to 8 TB. The page blob size must be aligned to a
     *      512-byte boundary.
     * @param sequenceNumber
     *      A user-controlled value that you can use to track requests. The value of the sequence number must be
     *      between 0 and 2^63 - 1.The default value is 0.
     * @param headers
     *      {@link BlobHTTPHeaders}
     * @param metadata
     *      {@link Metadata}
     * @param accessConditions
     *      {@link BlobAccessConditions}
     * @return
     *       Emits the successful response.
     */
    public Single<PageBlobCreateResponse> create(
            long size, Long sequenceNumber, BlobHTTPHeaders headers, Metadata metadata,
            BlobAccessConditions accessConditions) {
        if (size%PageBlobURL.PAGE_BYTES != 0) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw new IllegalArgumentException("size must be a multiple of PageBlobURL.PAGE_BYTES.");
        }
        if (sequenceNumber != null && sequenceNumber < 0) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw new IllegalArgumentException("SequenceNumber must be greater than or equal to 0.");
        }
        headers = headers == null ? BlobHTTPHeaders.NONE : headers;
        metadata = metadata == null ? Metadata.NONE : metadata;
        accessConditions = accessConditions == null ? BlobAccessConditions.NONE : accessConditions;

        // TODO: What if you pass 0 for pageblob size? Validate?
        return this.storageClient.generatedPageBlobs().createWithRestResponseAsync(
                0, null, headers.getContentType(), headers.getContentEncoding(),
                headers.getContentLanguage(), headers.getContentMD5(), headers.getCacheControl(),
                metadata, accessConditions.getLeaseAccessConditions().getLeaseId(),
                headers.getContentDisposition(),
                accessConditions.getHttpAccessConditions().getIfModifiedSince(),
                accessConditions.getHttpAccessConditions().getIfUnmodifiedSince(),
                accessConditions.getHttpAccessConditions().getIfMatch().toString(),
                accessConditions.getHttpAccessConditions().getIfNoneMatch().toString(),
                size, sequenceNumber, null);
    }

    /**
     * Writes 1 or more pages to the page blob. The start and end offsets must be a multiple of 512.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-page">Azure Docs</a>.
     *
     * @param pageRange
     *      A {@link PageRange} object. Given that pages must be aligned with 512-byte boundaries, the start offset must
     *      be a modulus of 512 and the end offset must be a modulus of 512 - 1. Examples of valid byte ranges are
     *      0-511, 512-1023, etc.
     * @param body
     *      The data to upload.
     * @param accessConditions
     *      {@link BlobAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<PageBlobUploadPagesResponse> uploadPages(
            PageRange pageRange, Flowable<ByteBuffer> body, BlobAccessConditions accessConditions) {
        accessConditions = accessConditions == null ? BlobAccessConditions.NONE : accessConditions;
        if (pageRange == null) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            new IllegalArgumentException("pageRange cannot be null.");
        }
        String pageRangeStr = this.pageRangeToString(pageRange);

        return this.storageClient.generatedPageBlobs().uploadPagesWithRestResponseAsync(
                 body, pageRange.end()-pageRange.start()+1,null, pageRangeStr,
                accessConditions.getLeaseAccessConditions().getLeaseId(),
                accessConditions.getPageBlobAccessConditions().getIfSequenceNumberLessThanOrEqual(),
                accessConditions.getPageBlobAccessConditions().getIfSequenceNumberLessThan(),
                accessConditions.getPageBlobAccessConditions().getIfSequenceNumberEqual(),
                accessConditions.getHttpAccessConditions().getIfModifiedSince(),
                accessConditions.getHttpAccessConditions().getIfUnmodifiedSince(),
                accessConditions.getHttpAccessConditions().getIfMatch().toString(),
                accessConditions.getHttpAccessConditions().getIfNoneMatch().toString(), null);
    }

    /**
     * Frees the specified pages from the page blob.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-page">Azure Docs</a>.
     *
     * @param pageRange
     *      A {@link PageRange} object. Given that pages must be aligned with 512-byte boundaries, the start offset must
     *      be a modulus of 512 and the end offset must be a modulus of 512 - 1. Examples of valid byte ranges are
     *      0-511, 512-1023, etc.
     * @param accessConditions
     *      {@link BlobAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<PageBlobClearPagesResponse> clearPages(
            PageRange pageRange, BlobAccessConditions accessConditions) {
        accessConditions = accessConditions == null ? BlobAccessConditions.NONE : accessConditions;
        if (pageRange == null) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw new IllegalArgumentException("pageRange cannot be null.");
        }
        String pageRangeStr = this.pageRangeToString(pageRange);

         return this.storageClient.generatedPageBlobs().clearPagesWithRestResponseAsync(
                 0,null, pageRangeStr,
                 accessConditions.getLeaseAccessConditions().getLeaseId(),
                 accessConditions.getPageBlobAccessConditions().getIfSequenceNumberLessThanOrEqual(),
                 accessConditions.getPageBlobAccessConditions().getIfSequenceNumberLessThan(),
                 accessConditions.getPageBlobAccessConditions().getIfSequenceNumberEqual(),
                 accessConditions.getHttpAccessConditions().getIfModifiedSince(),
                 accessConditions.getHttpAccessConditions().getIfUnmodifiedSince(),
                 accessConditions.getHttpAccessConditions().getIfMatch().toString(),
                 accessConditions.getHttpAccessConditions().getIfNoneMatch().toString(), null);
    }

    /**
     * Returns the list of valid page ranges for a page blob or snapshot of a page blob.
     * For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/get-page-ranges">Azure Docs</a>.
     *
     * @param blobRange
     *      {@link BlobRange}
     * @param accessConditions
     *      {@link BlobAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<PageBlobGetPageRangesResponse> getPageRanges(
            BlobRange blobRange, BlobAccessConditions accessConditions) {
        blobRange = blobRange == null ? BlobRange.DEFAULT : blobRange;
        accessConditions = accessConditions == null ? BlobAccessConditions.NONE : accessConditions;

        return this.storageClient.generatedPageBlobs().getPageRangesWithRestResponseAsync(
                null, null, blobRange.toString(),
                accessConditions.getLeaseAccessConditions().getLeaseId(),
                accessConditions.getHttpAccessConditions().getIfModifiedSince(),
                accessConditions.getHttpAccessConditions().getIfUnmodifiedSince(),
                accessConditions.getHttpAccessConditions().getIfMatch().toString(),
                accessConditions.getHttpAccessConditions().getIfNoneMatch().toString(),
                null);
    }

    /**
     * Gets the collection of page ranges that differ between a specified snapshot and this page blob.
     * For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/get-page-ranges">Azure Docs</a>.
     *
     * @param blobRange
     *     {@link BlobRange}
     * @param prevSnapshot
     *     Specifies that the response will contain only pages that were changed between target blob and previous
     *     snapshot. Changed pages include both updated and cleared pages. The target
     *     blob may be a snapshot, as long as the snapshot specified by prevsnapshot is the older of the two.
     * @param accessConditions
     *     {@link BlobAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<PageBlobGetPageRangesDiffResponse> getPageRangesDiff(
            BlobRange blobRange, String prevSnapshot, BlobAccessConditions accessConditions) {
        blobRange = blobRange == null ? BlobRange.DEFAULT : blobRange;
        accessConditions = accessConditions == null ? BlobAccessConditions.NONE : accessConditions;

        if (prevSnapshot == null) {
            throw new IllegalArgumentException("prevSnapshot cannot be null");
        }

        return this.storageClient.generatedPageBlobs().getPageRangesDiffWithRestResponseAsync(
                null,null, prevSnapshot, blobRange.toString(),
                accessConditions.getLeaseAccessConditions().getLeaseId(),
                accessConditions.getHttpAccessConditions().getIfModifiedSince(),
                accessConditions.getHttpAccessConditions().getIfUnmodifiedSince(),
                accessConditions.getHttpAccessConditions().getIfMatch().toString(),
                accessConditions.getHttpAccessConditions().getIfNoneMatch().toString(),
                null);
    }

    /**
     * Resizes the page blob to the specified size (which must be a multiple of 512).
     * For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-properties">Azure Docs</a>.
     *
     * @param size
     *      Resizes a page blob to the specified size. If the specified value is less than the current size of the
     *      blob, then all pages above the specified value are cleared.
     * @param accessConditions
     *      {@link BlobAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<PageBlobResizeResponse> resize(
            long size, BlobAccessConditions accessConditions) {
        if (size%PageBlobURL.PAGE_BYTES != 0) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw new IllegalArgumentException("size must be a multiple of PageBlobURL.PAGE_BYTES.");
        }
        accessConditions = accessConditions == null ? BlobAccessConditions.NONE : accessConditions;

        return this.storageClient.generatedPageBlobs().resizeWithRestResponseAsync(
                size,null,
                accessConditions.getLeaseAccessConditions().getLeaseId(),
                accessConditions.getHttpAccessConditions().getIfModifiedSince(),
                accessConditions.getHttpAccessConditions().getIfUnmodifiedSince(),
                accessConditions.getHttpAccessConditions().getIfMatch().toString(),
                accessConditions.getHttpAccessConditions().getIfNoneMatch().toString(),
                null);
    }

    /**
     * Sets the page blob's sequence number.
     *
     * @param action
     *      Indicates how the service should modify the blob's sequence number.
     * @param sequenceNumber
     *      The blob's sequence number. The sequence number is a user-controlled property that you can use to track
     *      requests and manage concurrency issues.
     * @param accessConditions
     *      {@link BlobAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<PageBlobUpdateSequenceNumberResponse> updateSequenceNumber(
            SequenceNumberActionType action, Long sequenceNumber, BlobAccessConditions accessConditions) {
        if (sequenceNumber != null && sequenceNumber < 0) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw new IllegalArgumentException("SequenceNumber must be greater than or equal to 0.");
        }
        accessConditions = accessConditions == null ? BlobAccessConditions.NONE : accessConditions;
        if(action == SequenceNumberActionType.INCREMENT) {
           sequenceNumber = null;
        }

        return this.storageClient.generatedPageBlobs().updateSequenceNumberWithRestResponseAsync(
                action, null,
                accessConditions.getLeaseAccessConditions().getLeaseId(),
                accessConditions.getHttpAccessConditions().getIfModifiedSince(),
                accessConditions.getHttpAccessConditions().getIfUnmodifiedSince(),
                accessConditions.getHttpAccessConditions().getIfMatch().toString(),
                accessConditions.getHttpAccessConditions().getIfNoneMatch().toString(),
                 sequenceNumber,null);
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
     *      The source page blob.
     * @param snapshot
     *      The snapshot on the copy source.
     * @param accessConditions
     *      {@link BlobAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<PageBlobCopyIncrementalResponse> copyIncremental(
            URL source, String snapshot, BlobAccessConditions accessConditions) {
        accessConditions = accessConditions == null ? BlobAccessConditions.NONE : accessConditions;

        UrlBuilder builder = UrlBuilder.parse(source);
        builder.setQueryParameter(Constants.SNAPSHOT_QUERY_PARAMETER, snapshot);
        try {
            source = builder.toURL();
        } catch (MalformedURLException e) {
            // We are parsing a valid url and adding a query parameter. If this fails, we can't recover.
            throw new Error(e);
        }
        return this.storageClient.generatedPageBlobs().copyIncrementalWithRestResponseAsync(source,
                null, null,
                accessConditions.getHttpAccessConditions().getIfModifiedSince(),
                accessConditions.getHttpAccessConditions().getIfUnmodifiedSince(),
                accessConditions.getHttpAccessConditions().getIfMatch().toString(),
                accessConditions.getHttpAccessConditions().getIfNoneMatch().toString(), null);
    }

    private String pageRangeToString(PageRange pageRange) {
        if (pageRange.start() < 0 || pageRange.end() <= 0) {
            throw new IllegalArgumentException("PageRange's start and end values must be greater than or equal to " +
                    "0 if specified.");
        }
        if (pageRange.start()%PageBlobURL.PAGE_BYTES != 0 ) {
            throw new IllegalArgumentException("PageRange's start value must be a multiple of 512.");
        }
        if (pageRange.end()%PageBlobURL.PAGE_BYTES != PageBlobURL.PAGE_BYTES-1) {
            throw new IllegalArgumentException("PageRange's end value must be 1 less than a multiple of 512.");
        }
        if (pageRange.end() <= pageRange.start()) {
            throw new IllegalArgumentException("PageRange's End value must be after the start.");
        }

        StringBuilder range = new StringBuilder("bytes=").append(pageRange.start()).append('-').append(pageRange.end());
        return range.toString();
    }
}
