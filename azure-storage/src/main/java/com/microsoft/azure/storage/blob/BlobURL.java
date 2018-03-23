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

import com.microsoft.azure.storage.models.*;
import com.microsoft.rest.v2.RestResponse;
import com.microsoft.rest.v2.http.HttpPipeline;
import io.reactivex.Flowable;
import io.reactivex.Single;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * Represents a URL to an Azure Storage blob; the blob may be a block blob, append blob, or page blob.
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
     * Copies the data at the source URL to a blob.
     * For more information, see https://docs.microsoft.com/rest/api/storageservices/copy-blob.
     *
     * @param sourceURL
     *      A {@code java.net.URL} representing the source URL to copy from.
     *      URLs outside of Azure may only be copied to block blobs.
     * @param metadata
     *      A {@link Metadata} object that specifies key value pairs to set on the blob.
     * @param sourceAccessConditions
     *      {@link BlobAccessConditions} object to check against the source
     * @param destAccessConditions
     *      {@link BlobAccessConditions} object to check against the destination
     * @return
     *      The {@link Single} which emits a {@link RestResponse} object containing the {@link BlobCopyHeaders} and
     *      {@code Void} body if successful.
     */
    public Single<RestResponse<BlobCopyHeaders, Void>> startCopy(
            URL sourceURL, Metadata metadata, BlobAccessConditions sourceAccessConditions,
            BlobAccessConditions destAccessConditions) {
        metadata = metadata == null ? Metadata.NONE : metadata;
        sourceAccessConditions = sourceAccessConditions == null ? BlobAccessConditions.NONE : sourceAccessConditions;
        destAccessConditions = destAccessConditions == null ? BlobAccessConditions.NONE : sourceAccessConditions;

        return this.storageClient.blobs().copyWithRestResponseAsync(
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
                destAccessConditions.getLeaseAccessConditions().getLeaseId(), null);
    }

    /**
     * Stops a pending copy that was previously started and leaves a destination blob with 0 length and
     * metadata. For more information, see https://docs.microsoft.com/rest/api/storageservices/abort-copy-blob.
     *
     * @param copyId
     *      A {@code String} representing the copy identifier provided in the x-ms-copy-id header of
     *      the original Copy Blob operation.
     * @param leaseAccessConditions
     *      {@link LeaseAccessConditions} object representing lease access conditions
     * @return
     *      The {@link Single} which emits a {@link RestResponse} object containing the {@link BlobAbortCopyHeaders} and
     *      {@code Void} body if successful.
     */
    public Single<RestResponse<BlobAbortCopyHeaders, Void>> abortCopy(
            String copyId, LeaseAccessConditions leaseAccessConditions) {
        leaseAccessConditions = leaseAccessConditions == null ? LeaseAccessConditions.NONE : leaseAccessConditions;

        return this.storageClient.blobs().abortCopyWithRestResponseAsync(
                copyId, null, leaseAccessConditions.getLeaseId(), null);
    }

    /**
     * Reads a range of bytes from a blob. The response also includes the blob's properties and metadata.
     * For more information, see https://docs.microsoft.com/rest/api/storageservices/get-blob.
     *
     * @param range
     *      A {@link BlobRange} which bytes to read.
     * @param accessConditions
     *      A {@link BlobAccessConditions} object that represents the access conditions for the blob.
     * @param rangeGetContentMD5
     *      A {@code boolean} indicating if the contentMD5 for the specified blob range should be returned.
     * @return
     *      The {@link Single} which emits a {@link RestResponse} object containing the {@link BlobGetHeaders} and a
     *      {@link Flowable} which emits {@link ByteBuffer} as the body if successful.
     */
    public Single<RestResponse<BlobGetHeaders, Flowable<ByteBuffer>>> getBlob(
            BlobRange range, BlobAccessConditions accessConditions, boolean rangeGetContentMD5) {

        // TODO: Are there other places for this? Should this be in the swagger?
        Boolean getMD5 = rangeGetContentMD5 ? rangeGetContentMD5 : null;
        range = range == null ? BlobRange.DEFAULT : range;
        accessConditions = accessConditions == null ? BlobAccessConditions.NONE : accessConditions;

        return this.storageClient.blobs().getWithRestResponseAsync(null, null,
                range.toString(), accessConditions.getLeaseAccessConditions().getLeaseId(),
                getMD5, accessConditions.getHttpAccessConditions().getIfModifiedSince(),
                accessConditions.getHttpAccessConditions().getIfUnmodifiedSince(),
                accessConditions.getHttpAccessConditions().getIfMatch().toString(),
                accessConditions.getHttpAccessConditions().getIfNoneMatch().toString(),
                null);
    }

    /**
     * Deletes the specified blob or snapshot. Note that deleting a blob also deletes all its snapshots.
     * For more information, see https://docs.microsoft.com/rest/api/storageservices/delete-blob.
     *
     * @param deleteBlobSnapshotOptions
     *      A {@link DeleteSnapshotsOptionType} which represents delete snapshot options.
     * @param accessConditions
     *      A {@link BlobAccessConditions} object that represents the access conditions for the blob.
     * @return
     *      A {@link Single} which emits a {@link RestResponse} object containing the {@link BlobDeleteHeaders} and a
     *      {@code Void} body if successful.
     */
    public Single<RestResponse<BlobDeleteHeaders, Void>> delete(
            DeleteSnapshotsOptionType deleteBlobSnapshotOptions, BlobAccessConditions accessConditions) {
        accessConditions = accessConditions == null ? BlobAccessConditions.NONE : accessConditions;

        return this.storageClient.blobs().deleteWithRestResponseAsync(null, null,
                accessConditions.getLeaseAccessConditions().getLeaseId(),
                deleteBlobSnapshotOptions,
                accessConditions.getHttpAccessConditions().getIfModifiedSince(),
                accessConditions.getHttpAccessConditions().getIfUnmodifiedSince(),
                accessConditions.getHttpAccessConditions().getIfMatch().toString(),
                accessConditions.getHttpAccessConditions().getIfNoneMatch().toString(), null);
    }

    /**
     * Returns the blob's metadata and properties.
     * For more information, see https://docs.microsoft.com/rest/api/storageservices/get-blob-properties.
     *
     * @param accessConditions
     *      A {@link BlobAccessConditions} object that represents the access conditions for the blob.
     * @return
     *      The {@link Single} which emits a {@link RestResponse} containing the {@link BlobGetPropertiesHeaders} and a
     *      {@code Void} body if successful.
     */
    public Single<RestResponse<BlobGetPropertiesHeaders, Void>> getPropertiesAndMetadata(
            BlobAccessConditions accessConditions) {
        accessConditions = accessConditions == null ? BlobAccessConditions.NONE : accessConditions;

        return this.storageClient.blobs().getPropertiesWithRestResponseAsync(null, null,
                accessConditions.getLeaseAccessConditions().getLeaseId(),
                accessConditions.getHttpAccessConditions().getIfModifiedSince(),
                accessConditions.getHttpAccessConditions().getIfUnmodifiedSince(),
                accessConditions.getHttpAccessConditions().getIfMatch().toString(),
                accessConditions.getHttpAccessConditions().getIfNoneMatch().toString(), null);
    }

    /**
     * Changes a blob's HTTP header properties.
     * For more information, see https://docs.microsoft.com/rest/api/storageservices/set-blob-properties.
     *
     * @param headers
     *      A {@link BlobHTTPHeaders} object that specifies which properties to set on the blob.
     * @param accessConditions
     *      A {@link BlobAccessConditions} object that specifies under which conditions the operation should
     *      complete.
     * @return
     *      The {@link Single} which emits a {@link RestResponse} object containing the {@link BlobSetPropertiesHeaders}
     *      and a {@code Void} body if successful.
     */
    public Single<RestResponse<BlobSetPropertiesHeaders, Void>> setProperties(
            BlobHTTPHeaders headers, BlobAccessConditions accessConditions) {
        headers = headers == null ? BlobHTTPHeaders.NONE : headers;
        accessConditions = accessConditions == null ? BlobAccessConditions.NONE : accessConditions;

        return this.storageClient.blobs().setPropertiesWithRestResponseAsync(null,
                headers.getCacheControl(), headers.getContentType(), headers.getContentMD5(),
                headers.getContentEncoding(),
                headers.getContentLanguage(), accessConditions.getLeaseAccessConditions().getLeaseId(),
                accessConditions.getHttpAccessConditions().getIfModifiedSince(),
                accessConditions.getHttpAccessConditions().getIfUnmodifiedSince(),
                accessConditions.getHttpAccessConditions().getIfMatch().toString(),
                accessConditions.getHttpAccessConditions().getIfNoneMatch().toString(),
                headers.getContentDisposition(),
                null, null, null, null);
    }

    /**
     * Changes a blob's metadata.
     * https://docs.microsoft.com/rest/api/storageservices/set-blob-metadata.
     *
     * @param metadata
     *      A {@link Metadata} object that specifies key value pairs to set on the blob.
     * @param accessConditions
     *      A {@link BlobAccessConditions} object that specifies under which conditions the operation should
     *      complete.
     * @return
     *      The {@link Single} which emits a {@link RestResponse} object containing the {@link BlobSetMetadataHeaders}
     *      and a {@code Void} body if successful.
     */
    public Single<RestResponse<BlobSetMetadataHeaders, Void>> setMetadata(
            Metadata metadata, BlobAccessConditions accessConditions) {
        metadata = metadata == null ? Metadata.NONE : metadata;
        accessConditions = accessConditions == null ? BlobAccessConditions.NONE : accessConditions;

        return this.storageClient.blobs().setMetadataWithRestResponseAsync(null, metadata,
                accessConditions.getLeaseAccessConditions().getLeaseId(),
                accessConditions.getHttpAccessConditions().getIfModifiedSince(),
                accessConditions.getHttpAccessConditions().getIfUnmodifiedSince(),
                accessConditions.getHttpAccessConditions().getIfMatch().toString(),
                accessConditions.getHttpAccessConditions().getIfNoneMatch().toString(),
                null);
    }

    /**
     * Creates a read-only snapshot of a blob.
     * For more information, see https://docs.microsoft.com/rest/api/storageservices/snapshot-blob.
     *
     * @param metadata
     *      A {@link Metadata} object that specifies key value pairs to set on the blob.
     * @param accessConditions
     *      A {@link BlobAccessConditions} object that represents the access conditions for the blob.
     * @return
     *      The {@link Single} which emits a {@link RestResponse} object containing the {@link BlobTakeSnapshotHeaders}
     *      and a {@code Void} body if successful.
     */
    public Single<RestResponse<BlobTakeSnapshotHeaders, Void>> createSnapshot(
            Metadata metadata, BlobAccessConditions accessConditions) {
        metadata = metadata == null ? Metadata.NONE : metadata;
        accessConditions = accessConditions == null ? BlobAccessConditions.NONE : accessConditions;

        return this.storageClient.blobs().takeSnapshotWithRestResponseAsync(null,
                metadata,
                accessConditions.getHttpAccessConditions().getIfModifiedSince(),
                accessConditions.getHttpAccessConditions().getIfUnmodifiedSince(),
                accessConditions.getHttpAccessConditions().getIfMatch().toString(),
                accessConditions.getHttpAccessConditions().getIfNoneMatch().toString(),
                accessConditions.getLeaseAccessConditions().getLeaseId(), null);
    }

    /**
     * Acquires a lease on the blob for write and delete operations. The lease duration must be between
     * 15 to 60 seconds, or infinite (-1).
     * For more information, see https://docs.microsoft.com/rest/api/storageservices/lease-blob.
     *
     * @param proposedID
     *      A {@code String} in any valid GUID format.
     * @param duration
     *      A {@code Integer} specifies the duration of the lease, in seconds, or negative one (-1) for a lease that
     *      never expires. A non-infinite lease can be between 15 and 60 seconds.
     * @param httpAccessConditions
     *      A {@link HTTPAccessConditions} object that represents HTTP access conditions.
     * @return
     *      The {@link Single} which emits a {@link RestResponse} object containing the {@link BlobLeaseHeaders} and a
     *      {@code Void} body if successful.
     */
    public Single<RestResponse<BlobLeaseHeaders, Void>> acquireLease(
            String proposedID, int duration, HTTPAccessConditions httpAccessConditions) {
        httpAccessConditions = httpAccessConditions == null ? HTTPAccessConditions.NONE : httpAccessConditions;
        if (!(duration == -1 || (duration >= 15 && duration <=60))) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw new IllegalArgumentException("Duration must be -1 or between 15 and 60.");
        }

        return this.storageClient.blobs().leaseWithRestResponseAsync(LeaseActionType.ACQUIRE, null,
                null, null, duration, proposedID,
                httpAccessConditions.getIfModifiedSince(),
                httpAccessConditions.getIfUnmodifiedSince(),
                httpAccessConditions.getIfMatch().toString(),
                httpAccessConditions.getIfNoneMatch().toString(),
                null);
    }

    /**
     * Renews the blob's previously-acquired lease.
     * For more information, see https://docs.microsoft.com/rest/api/storageservices/lease-blob.
     *
     * @param leaseID
     *      A {@code String} representing the lease on the blob.
     * @param httpAccessConditions
     *      A {@link HTTPAccessConditions} object that represents HTTP access conditions.
     * @return
     *      The {@link Single} which emits a {@link RestResponse} object containing the {@link BlobLeaseHeaders} and a
     *      {@code Void} body if successful.
     */
    public Single<RestResponse<BlobLeaseHeaders, Void>> renewLease(
            String leaseID, HTTPAccessConditions httpAccessConditions) {
        httpAccessConditions = httpAccessConditions == null ? HTTPAccessConditions.NONE : httpAccessConditions;

        return this.storageClient.blobs().leaseWithRestResponseAsync(LeaseActionType.RENEW, null,
                leaseID, null, null, null,
                httpAccessConditions.getIfModifiedSince(),
                httpAccessConditions.getIfUnmodifiedSince(),
                httpAccessConditions.getIfMatch().toString(), httpAccessConditions.getIfNoneMatch().toString(),
                null);
    }

    /**
     * Releases the blob's previously-acquired lease. For more information, see
     * https://docs.microsoft.com/rest/api/storageservices/lease-blob.
     *
     * @param leaseID
     *      A {@code String} representing the lease on the blob.
     * @param httpAccessConditions
     *      A {@link HTTPAccessConditions} object that represents HTTP access conditions.
     * @return
     *      The {@link Single} which emits a {@link RestResponse} object containing the {@link BlobLeaseHeaders} and a
     *      {@code Void} body if successful.
     */
    public Single<RestResponse<BlobLeaseHeaders, Void>> releaseLease(
            String leaseID, HTTPAccessConditions httpAccessConditions) {
        httpAccessConditions = httpAccessConditions == null ? HTTPAccessConditions.NONE : httpAccessConditions;

        return this.storageClient.blobs().leaseWithRestResponseAsync(LeaseActionType.RELEASE, null,
                leaseID, null, null, null,
                httpAccessConditions.getIfModifiedSince(),
                httpAccessConditions.getIfUnmodifiedSince(),
                httpAccessConditions.getIfMatch().toString(), httpAccessConditions.getIfNoneMatch().toString(),
                null);
    }

    /**
     * BreakLease breaks the blob's previously-acquired lease (if it exists). Pass the LeaseBreakDefault (-1) constant
     * to break a fixed-duration lease when it expires or an infinite lease immediately.
     * For more information, see https://docs.microsoft.com/rest/api/storageservices/lease-blob.
     *
     * @param breakPeriodInSeconds
     *      An optional {@code Integer} representing the proposed duration of seconds that the lease should continue
     *      before it is broken, between 0 and 60 seconds. This break period is only used if it is shorter than the time
     *      remaining on the lease. If longer, the time remaining on the lease is used. A new lease will not be
     *      available before the break period has expired, but the lease may be held for longer than the break period
     * @param httpAccessConditions
     *      A {@link HTTPAccessConditions} object that represents HTTP access conditions.
     * @return
     *      The {@link Single} which emits a {@link RestResponse} object containing the {@link BlobLeaseHeaders} and a
     *      {@code Void} body if successful.
     */
    public Single<RestResponse<BlobLeaseHeaders, Void>> breakLease(
            Integer breakPeriodInSeconds, HTTPAccessConditions httpAccessConditions) {
        httpAccessConditions = httpAccessConditions == null ? HTTPAccessConditions.NONE : httpAccessConditions;

        return this.storageClient.blobs().leaseWithRestResponseAsync(LeaseActionType.RENEW, null,
                null, breakPeriodInSeconds, null, null,
                httpAccessConditions.getIfModifiedSince(),
                httpAccessConditions.getIfUnmodifiedSince(),
                httpAccessConditions.getIfMatch().toString(), httpAccessConditions.getIfNoneMatch().toString(),
                null);
    }

    /**
     * ChangeLease changes the blob's lease ID.
     * For more information, see https://docs.microsoft.com/rest/api/storageservices/lease-blob.
     *
     * @param leaseId
     *      A {@code String} representing the lease ID to change.
     * @param proposedID
     *      A {@code String} in any valid GUID format.
     * @param httpAccessConditions
     *      A {@link HTTPAccessConditions} object that represents HTTP access conditions.
     * @return
     *      The {@link Single} which emits a {@link RestResponse} object containing the {@link BlobLeaseHeaders} and a
     *      {@code Void} body if successful.
     */
    public Single<RestResponse<BlobLeaseHeaders, Void>> changeLease(
            String leaseId, String proposedID, HTTPAccessConditions httpAccessConditions) {
        httpAccessConditions = httpAccessConditions == null ? HTTPAccessConditions.NONE : httpAccessConditions;

        return this.storageClient.blobs().leaseWithRestResponseAsync(LeaseActionType.RENEW, null,
                leaseId, null, null, proposedID,
                httpAccessConditions.getIfModifiedSince(),
                httpAccessConditions.getIfUnmodifiedSince(),
                httpAccessConditions.getIfMatch().toString(), httpAccessConditions.getIfNoneMatch().toString(),
                null);
    }
}
