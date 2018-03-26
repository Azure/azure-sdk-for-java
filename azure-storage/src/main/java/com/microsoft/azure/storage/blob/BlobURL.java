/*
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
import com.microsoft.rest.v2.http.HttpPipeline;
import io.reactivex.Single;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * Represents a URL to a blob of any type: block, append, or page. It may be obtained by direct construction or via the
 * create method on a {@link ContainerURL} object. This class does not hold any state about a particular blob but is
 * instead a convenient way of sending off appropriate requests to the resource on the service. Please refer to the
 * following for more information on blobs:
 * https://docs.microsoft.com/en-us/rest/api/storageservices/understanding-block-blobs--append-blobs--and-page-blobs
 */
public class BlobURL extends StorageURL {

    /**
     * Creates a new {@link BlobURL} object.
     *
     * @param url
     *      A {@code java.net.URL} to a blob.
     * @param pipeline
     *      An {@link HttpPipeline} for sending requests.
     */
    public BlobURL(URL url, HttpPipeline pipeline) {
        super(url, pipeline);
    }

    /**
     * Creates a new {@link BlobURL} with the given pipeline.
     *
     * @param pipeline
     *      An {@link HttpPipeline} object to set.
     * @return
     *      A {@link BlobURL} object with the given pipeline.
     */
    public BlobURL withPipeline(HttpPipeline pipeline) {
        try {
            return new BlobURL(new URL(this.storageClient.url()), pipeline);
        } catch (MalformedURLException e) {
            // TODO: Remove
        }
        return null;
    }

    /**
     * Creates a new {@link BlobURL} with the given snapshot.
     *
     * @param snapshot
     *      A {@code String} to set.
     * @return
     *      A {@link BlobURL} object with the given pipeline.
     */
    public BlobURL withSnapshot(String snapshot) throws MalformedURLException, UnknownHostException {
        BlobURLParts blobURLParts = URLParser.parse(new URL(this.storageClient.url()));
        blobURLParts.snapshot = snapshot;
        return new BlobURL(blobURLParts.toURL(), super.storageClient.httpPipeline());
    }

    /**
     * Converts this BlobURL to a {@link BlockBlobURL} object. Note that this does not change the actual type of the
     * blob if it has already been created.
     *
     * @return
     *      A {@link BlockBlobURL} object.
     */
    public BlockBlobURL toBlockBlobURL() {
        try {
            return new BlockBlobURL(new URL(this.storageClient.url()), super.storageClient.httpPipeline());
        } catch (MalformedURLException e) {
            // TODO: remove.
        }
        return null;
    }

    /**
     * Converts this BlobURL to an {@link AppendBlobURL} object. Note that this does not change the actual type of the
     * blob if it has already been created.
     *
     * @return
     *      An {@link AppendBlobURL} object.
     */
    public AppendBlobURL toAppendBlobURL() {
        try {
            return new AppendBlobURL(new URL(this.storageClient.url()), super.storageClient.httpPipeline());
        } catch (MalformedURLException e) {
            // TODO: remove
        }
        return null;
    }

    /**
     * Converts this BlobURL to a {@link PageBlobURL} object. Note that this does not change the actual type of the blob
     * if it has already been created.
     *
     * @return
     *      A {@link PageBlobURL} object.
     */
    public PageBlobURL toPageBlobURL() {
        try {
            return new PageBlobURL(new URL(this.storageClient.url()), super.storageClient.httpPipeline());
        } catch (MalformedURLException e) {
            // TODO: remove
        }
        return null;
    }

    /**
     * Copies the data at the source URL to a blob. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/copy-blob">Azure Docs</a>
     *
     *
     * @param sourceURL
     *      The source URL to copy from. URLs outside of Azure may only be copied to block blobs.
     * @param metadata
     *      {@link Metadata}
     * @param sourceAccessConditions
     *      {@link BlobAccessConditions} against the source.
     * @param destAccessConditions
     *      {@link BlobAccessConditions} against the destination.
     * @return
     *      Emits the successful response.
     */
    public Single<BlobsStartCopyFromURLResponse> startCopyFromURL(
            URL sourceURL, Metadata metadata, BlobAccessConditions sourceAccessConditions,
            BlobAccessConditions destAccessConditions) {
        metadata = metadata == null ? Metadata.NONE : metadata;
        sourceAccessConditions = sourceAccessConditions == null ? BlobAccessConditions.NONE : sourceAccessConditions;
        destAccessConditions = destAccessConditions == null ? BlobAccessConditions.NONE : sourceAccessConditions;

        return this.storageClient.generatedBlobs().startCopyFromURLWithRestResponseAsync(
                sourceURL, null, metadata,
                sourceAccessConditions.getHttpAccessConditions().getIfModifiedSince(),
                sourceAccessConditions.getHttpAccessConditions().getIfUnmodifiedSince(),
                sourceAccessConditions.getHttpAccessConditions().getIfMatch().toString(),
                sourceAccessConditions.getHttpAccessConditions().getIfNoneMatch().toString(),
                destAccessConditions.getHttpAccessConditions().getIfModifiedSince(),
                destAccessConditions.getHttpAccessConditions().getIfUnmodifiedSince(),
                destAccessConditions.getHttpAccessConditions().getIfMatch().toString(),
                destAccessConditions.getHttpAccessConditions().getIfNoneMatch().toString(),
                sourceAccessConditions.getLeaseAccessConditions().getLeaseId(),
                destAccessConditions.getLeaseAccessConditions().getLeaseId(),
                null);
    }

    /**
     * Stops a pending copy that was previously started and leaves a destination blob with 0 length and metadata. For
     * more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/abort-copy-blob">Azure Docs</a>.
     *
     * @param copyId
     *      The id of the copy operation to abort. Returned as the {@code copyId} field on the
     *      {@link BlobsStartCopyFromURLHeaders} object.
     * @param leaseAccessConditions
     *      {@link LeaseAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<BlobsAbortCopyFromURLResponse> abortCopyFromURL(
            String copyId, LeaseAccessConditions leaseAccessConditions) {
        leaseAccessConditions = leaseAccessConditions == null ? LeaseAccessConditions.NONE : leaseAccessConditions;

        return this.storageClient.generatedBlobs().abortCopyFromURLWithRestResponseAsync(
                copyId, null, leaseAccessConditions.getLeaseId(), null);
    }

    /**
     * Reads a range of bytes from a blob. The response also includes the blob's properties and metadata. For more
     * information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a>.
     *
     * @param range
     *      {@link BlobRange}
     * @param accessConditions
     *      {@link BlobAccessConditions}
     * @param rangeGetContentMD5
     *      Whether the contentMD5 for the specified blob range should be returned.
     * @return
     *      Emits the successful response.
     */
    public Single<BlobsDownloadResponse> download(
            BlobRange range, BlobAccessConditions accessConditions, boolean rangeGetContentMD5) {

        // TODO: Are there other places for this? Should this be in the swagger?
        Boolean getMD5 = rangeGetContentMD5 ? rangeGetContentMD5 : null;
        range = range == null ? BlobRange.DEFAULT : range;
        accessConditions = accessConditions == null ? BlobAccessConditions.NONE : accessConditions;

        return this.storageClient.generatedBlobs().downloadWithRestResponseAsync(
                null, null, range.toString(),
                accessConditions.getLeaseAccessConditions().getLeaseId(),
                getMD5, accessConditions.getHttpAccessConditions().getIfModifiedSince(),
                accessConditions.getHttpAccessConditions().getIfUnmodifiedSince(),
                accessConditions.getHttpAccessConditions().getIfMatch().toString(),
                accessConditions.getHttpAccessConditions().getIfNoneMatch().toString(),
                null);
    }

    /**
     * Deletes the specified blob or snapshot. Note that deleting a blob also deletes all its snapshots. For more
     * information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-blob">Azure Docs</a>.
     *
     * @param deleteBlobSnapshotOptions
     *      Specifies the behavior for deleting the snapshots on this blob. {@code Include} will delete the base blob
     *      and all snapshots. {@code Only} will delete only the snapshots. If a snapshot is being deleted, you must
     *      pass null.
     * @param accessConditions
     *      {@link BlobAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<BlobsDeleteResponse> delete(
            DeleteSnapshotsOptionType deleteBlobSnapshotOptions, BlobAccessConditions accessConditions) {
        accessConditions = accessConditions == null ? BlobAccessConditions.NONE : accessConditions;

        return this.storageClient.generatedBlobs().deleteWithRestResponseAsync(
                null, null,
                accessConditions.getLeaseAccessConditions().getLeaseId(),
                deleteBlobSnapshotOptions,
                accessConditions.getHttpAccessConditions().getIfModifiedSince(),
                accessConditions.getHttpAccessConditions().getIfUnmodifiedSince(),
                accessConditions.getHttpAccessConditions().getIfMatch().toString(),
                accessConditions.getHttpAccessConditions().getIfNoneMatch().toString(),
                null);
    }

    /**
     * Returns the blob's metadata and properties. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob-properties">Azure Docs</a>.
     *
     * @param accessConditions
     *      {@link BlobAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<BlobsGetPropertiesResponse> getProperties(
            BlobAccessConditions accessConditions) {
        accessConditions = accessConditions == null ? BlobAccessConditions.NONE : accessConditions;

        return this.storageClient.generatedBlobs().getPropertiesWithRestResponseAsync(
                null, null,
                accessConditions.getLeaseAccessConditions().getLeaseId(),
                accessConditions.getHttpAccessConditions().getIfModifiedSince(),
                accessConditions.getHttpAccessConditions().getIfUnmodifiedSince(),
                accessConditions.getHttpAccessConditions().getIfMatch().toString(),
                accessConditions.getHttpAccessConditions().getIfNoneMatch().toString(),
                null);
    }

    /**
     * Changes a blob's HTTP header properties. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-properties">Azure Docs</a>.
     *
     * @param headers
     *      {@link BlobHTTPHeaders}
     * @param accessConditions
     *      {@link BlobAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<BlobsSetHTTPHeadersResponse> setHTTPHeaders(
            BlobHTTPHeaders headers, BlobAccessConditions accessConditions) {
        headers = headers == null ? BlobHTTPHeaders.NONE : headers;
        accessConditions = accessConditions == null ? BlobAccessConditions.NONE : accessConditions;

        return this.storageClient.generatedBlobs().setHTTPHeadersWithRestResponseAsync(
                null, headers.getCacheControl(), headers.getContentType(), headers.getContentMD5(),
                headers.getContentEncoding(), headers.getContentLanguage(),
                accessConditions.getLeaseAccessConditions().getLeaseId(),
                accessConditions.getHttpAccessConditions().getIfModifiedSince(),
                accessConditions.getHttpAccessConditions().getIfUnmodifiedSince(),
                accessConditions.getHttpAccessConditions().getIfMatch().toString(),
                accessConditions.getHttpAccessConditions().getIfNoneMatch().toString(),
                headers.getContentDisposition(),
                null);
    }

    /**
     * Changes a blob's metadata. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-metadata">Azure Docs</a>.
     *
     * @param metadata
     *      {@link Metadata}
     * @param accessConditions
     *      {@link BlobAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<BlobsSetMetadataResponse> setMetadata(
            Metadata metadata, BlobAccessConditions accessConditions) {
        metadata = metadata == null ? Metadata.NONE : metadata;
        accessConditions = accessConditions == null ? BlobAccessConditions.NONE : accessConditions;

        return this.storageClient.generatedBlobs().setMetadataWithRestResponseAsync(
                null, metadata,
                accessConditions.getLeaseAccessConditions().getLeaseId(),
                accessConditions.getHttpAccessConditions().getIfModifiedSince(),
                accessConditions.getHttpAccessConditions().getIfUnmodifiedSince(),
                accessConditions.getHttpAccessConditions().getIfMatch().toString(),
                accessConditions.getHttpAccessConditions().getIfNoneMatch().toString(),
                null);
    }

    /**
     * Creates a read-only snapshot of a blob. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/snapshot-blob">Azure Docs</a>.
     *
     * @param metadata
     *      {@link Metadata}
     * @param accessConditions
     *      {@link BlobAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<BlobsCreateSnapshotResponse> createSnapshot(
            Metadata metadata, BlobAccessConditions accessConditions) {
        metadata = metadata == null ? Metadata.NONE : metadata;
        accessConditions = accessConditions == null ? BlobAccessConditions.NONE : accessConditions;

        return this.storageClient.generatedBlobs().createSnapshotWithRestResponseAsync(
                null, metadata,
                accessConditions.getHttpAccessConditions().getIfModifiedSince(),
                accessConditions.getHttpAccessConditions().getIfUnmodifiedSince(),
                accessConditions.getHttpAccessConditions().getIfMatch().toString(),
                accessConditions.getHttpAccessConditions().getIfNoneMatch().toString(),
                accessConditions.getLeaseAccessConditions().getLeaseId(),
                null);
    }

    /**
     * Acquires a lease on the blob for write and delete operations. The lease duration must be between
     * 15 to 60 seconds, or infinite (-1). For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/lease-blob">Azure Docs</a>.
     *
     * @param proposedID
     *      A {@code String} in any valid GUID format.
     * @param duration
     *      The  duration of the lease, in seconds, or negative one (-1) for a lease that
     *      never expires. A non-infinite lease can be between 15 and 60 seconds.
     * @param httpAccessConditions
     *      {@link HTTPAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<BlobsAcquireLeaseResponse> acquireLease(
            String proposedID, int duration, HTTPAccessConditions httpAccessConditions) {
        httpAccessConditions = httpAccessConditions == null ? HTTPAccessConditions.NONE : httpAccessConditions;
        if (!(duration == -1 || (duration >= 15 && duration <=60))) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw new IllegalArgumentException("Duration must be -1 or between 15 and 60.");
        }

        return this.storageClient.generatedBlobs().acquireLeaseWithRestResponseAsync(
                null, duration, proposedID,
                httpAccessConditions.getIfModifiedSince(),
                httpAccessConditions.getIfUnmodifiedSince(),
                httpAccessConditions.getIfMatch().toString(),
                httpAccessConditions.getIfNoneMatch().toString(),
                null);
    }

    /**
     * Renews the blob's previously-acquired lease. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/lease-blob">Azure Docs</a>.
     *
     * @param leaseID
     *      The leaseId of the active lease on the blob.
     * @param httpAccessConditions
     *      {@link HTTPAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<BlobsRenewLeaseResponse> renewLease(
            String leaseID, HTTPAccessConditions httpAccessConditions) {
        httpAccessConditions = httpAccessConditions == null ? HTTPAccessConditions.NONE : httpAccessConditions;

        return this.storageClient.generatedBlobs().renewLeaseWithRestResponseAsync(
                leaseID, null,
                httpAccessConditions.getIfModifiedSince(),
                httpAccessConditions.getIfUnmodifiedSince(),
                httpAccessConditions.getIfMatch().toString(),
                httpAccessConditions.getIfNoneMatch().toString(),
                null);
    }

    /**
     * Releases the blob's previously-acquired lease. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/lease-blob">Azure Docs</a>.
     *
     * @param leaseID
     *      The leaseId of the active lease on the blob.
     * @param httpAccessConditions
     *      {@link HTTPAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<BlobsReleaseLeaseResponse> releaseLease(
            String leaseID, HTTPAccessConditions httpAccessConditions) {
        httpAccessConditions = httpAccessConditions == null ? HTTPAccessConditions.NONE : httpAccessConditions;

        return this.storageClient.generatedBlobs().releaseLeaseWithRestResponseAsync(
                leaseID, null,
                httpAccessConditions.getIfModifiedSince(),
                httpAccessConditions.getIfUnmodifiedSince(),
                httpAccessConditions.getIfMatch().toString(),
                httpAccessConditions.getIfNoneMatch().toString(),
                null);
    }

    /**
     * BreakLease breaks the blob's previously-acquired lease (if it exists). Pass the LeaseBreakDefault (-1) constant
     * to break a fixed-duration lease when it expires or an infinite lease immediately. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/lease-blob">Azure Docs</a>.
     *
     * @param breakPeriodInSeconds
     *      An optional {@code Integer} representing the proposed duration of seconds that the lease should continue
     *      before it is broken, between 0 and 60 seconds. This break period is only used if it is shorter than the time
     *      remaining on the lease. If longer, the time remaining on the lease is used. A new lease will not be
     *      available before the break period has expired, but the lease may be held for longer than the break period.
     * @param httpAccessConditions
     *      {@link HTTPAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<BlobsBreakLeaseResponse> breakLease(
            Integer breakPeriodInSeconds, HTTPAccessConditions httpAccessConditions) {
        httpAccessConditions = httpAccessConditions == null ? HTTPAccessConditions.NONE : httpAccessConditions;

        return this.storageClient.generatedBlobs().breakLeaseWithRestResponseAsync(
                null, breakPeriodInSeconds,
                httpAccessConditions.getIfModifiedSince(),
                httpAccessConditions.getIfUnmodifiedSince(),
                httpAccessConditions.getIfMatch().toString(),
                httpAccessConditions.getIfNoneMatch().toString(),
                null);
    }

    /**
     * ChangeLease changes the blob's lease ID. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/lease-blob">Azure Docs</a>.
     *
     * @param leaseId
     *      The leaseId of the active lease on the blob.
     * @param proposedID
     *      A {@code String} in any valid GUID format.
     * @param httpAccessConditions
     *      {@link HTTPAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<BlobsChangeLeaseResponse> changeLease(
            String leaseId, String proposedID, HTTPAccessConditions httpAccessConditions) {
        httpAccessConditions = httpAccessConditions == null ? HTTPAccessConditions.NONE : httpAccessConditions;

        return this.storageClient.generatedBlobs().changeLeaseWithRestResponseAsync(
                leaseId, proposedID, null,
                httpAccessConditions.getIfModifiedSince(),
                httpAccessConditions.getIfUnmodifiedSince(),
                httpAccessConditions.getIfMatch().toString(), httpAccessConditions.getIfNoneMatch().toString(),
                null);
    }

    //TODO: Set Tier
    //TODO: Undelete
    // TODO: Update links
}
