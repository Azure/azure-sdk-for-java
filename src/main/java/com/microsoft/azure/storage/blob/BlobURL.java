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
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import static com.microsoft.azure.storage.blob.Utility.*;

/**
 * Represents a URL to a blob of any type: block, append, or page. It may be obtained by direct construction or via the
 * create method on a {@link ContainerURL} object. This class does not hold any state about a particular blob but is
 * instead a convenient way of sending off appropriate requests to the resource on the service. Please refer to the
 * <a href=https://docs.microsoft.com/en-us/rest/api/storageservices/understanding-block-blobs--append-blobs--and-page-blobs>Azure Docs</a>
 * for more information.
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
     * @throws MalformedURLException
     *      Appending the specified snapshot produced an invalid URL.
     * @throws UnknownHostException
     *      If the url contains an improperly formatted ipaddress or unknown host address.
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
     * @apiNote
     * ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=start_copy "Sample code for BlobURL.startCopyFromURL")] \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=start_copy_helper "Helper for start_copy sample.")] \n
     * For more samples, please see the [Samples file] (https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
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
    public Single<BlobStartCopyFromURLResponse> startCopyFromURL(
            URL sourceURL, Metadata metadata, BlobAccessConditions sourceAccessConditions,
            BlobAccessConditions destAccessConditions) {
        metadata = metadata == null ? Metadata.NONE : metadata;
        sourceAccessConditions = sourceAccessConditions == null ? BlobAccessConditions.NONE : sourceAccessConditions;
        destAccessConditions = destAccessConditions == null ? BlobAccessConditions.NONE : destAccessConditions;

        return addErrorWrappingToSingle(this.storageClient.generatedBlobs().startCopyFromURLWithRestResponseAsync(
                sourceURL, null, metadata,
                sourceAccessConditions.getHttpAccessConditions().getIfModifiedSince(),
                sourceAccessConditions.getHttpAccessConditions().getIfUnmodifiedSince(),
                sourceAccessConditions.getHttpAccessConditions().getIfMatch().toString(),
                sourceAccessConditions.getHttpAccessConditions().getIfNoneMatch().toString(),
                destAccessConditions.getHttpAccessConditions().getIfModifiedSince(),
                destAccessConditions.getHttpAccessConditions().getIfUnmodifiedSince(),
                destAccessConditions.getHttpAccessConditions().getIfMatch().toString(),
                destAccessConditions.getHttpAccessConditions().getIfNoneMatch().toString(),
                destAccessConditions.getLeaseAccessConditions().getLeaseId(),
                sourceAccessConditions.getLeaseAccessConditions().getLeaseId(),
                null));
    }

    /**
     * Stops a pending copy that was previously started and leaves a destination blob with 0 length and metadata. For
     * more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/abort-copy-blob">Azure Docs</a>.
     *
     * @apiNote
     * ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=abort_copy "Sample code for BlobURL.abortCopyFromURL")] \n
     * For more samples, please see the [Samples file] (https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
     *
     * @param copyId
     *      The id of the copy operation to abort. Returned as the {@code copyId} field on the
     *      {@link BlobStartCopyFromURLHeaders} object.
     * @param leaseAccessConditions
     *      {@link LeaseAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<BlobAbortCopyFromURLResponse> abortCopyFromURL(
            String copyId, LeaseAccessConditions leaseAccessConditions) {
        leaseAccessConditions = leaseAccessConditions == null ? LeaseAccessConditions.NONE : leaseAccessConditions;

        return addErrorWrappingToSingle(this.storageClient.generatedBlobs().abortCopyFromURLWithRestResponseAsync(
                copyId, null, leaseAccessConditions.getLeaseId(), null));
    }

    /**
     * Reads a range of bytes from a blob. The response also includes the blob's properties and metadata. For more
     * information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a>.
     *
     * Please consider using the {@link RetryReader} in conjunction with this method to achieve more reliable downloads
     * that are resilient to transient network failures. A convenient way to do this is to simply pass non-null options
     * to {@link DownloadResponse#body(RetryReaderOptions)}, and a RetryReader will be applied to the body
     * automatically.
     *
     * @apiNote
     * ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=upload_download "Sample code for BlobURL.download")] \n
     * For more samples, please see the [Samples file] (https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
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
    public Single<DownloadResponse> download(
            BlobRange range, BlobAccessConditions accessConditions, boolean rangeGetContentMD5) {

        Boolean getMD5 = rangeGetContentMD5 ? rangeGetContentMD5 : null;
        range = range == null ? BlobRange.DEFAULT : range;
        accessConditions = accessConditions == null ? BlobAccessConditions.NONE : accessConditions;

        return addErrorWrappingToSingle(this.storageClient.generatedBlobs().downloadWithRestResponseAsync(
                null, null, range.toString(),
                accessConditions.getLeaseAccessConditions().getLeaseId(),
                getMD5,
                accessConditions.getHttpAccessConditions().getIfModifiedSince(),
                accessConditions.getHttpAccessConditions().getIfUnmodifiedSince(),
                accessConditions.getHttpAccessConditions().getIfMatch().toString(),
                accessConditions.getHttpAccessConditions().getIfNoneMatch().toString(),
                null))
                .map(response ->
                        new DownloadResponse(response.statusCode(), response.headers(),
                                response.rawHeaders(), response.body()));

    }

    /**
     * Deletes the specified blob or snapshot. Note that deleting a blob also deletes all its snapshots. For more
     * information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-blob">Azure Docs</a>.
     *
     * @apiNote
     * ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=blob_delete "Sample code for BlobURL.delete")] \n
     * For more samples, please see the [Samples file] (https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
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
    public Single<BlobDeleteResponse> delete(
            DeleteSnapshotsOptionType deleteBlobSnapshotOptions, BlobAccessConditions accessConditions) {
        accessConditions = accessConditions == null ? BlobAccessConditions.NONE : accessConditions;

        return addErrorWrappingToSingle(this.storageClient.generatedBlobs().deleteWithRestResponseAsync(
                null, null,
                accessConditions.getLeaseAccessConditions().getLeaseId(),
                deleteBlobSnapshotOptions,
                accessConditions.getHttpAccessConditions().getIfModifiedSince(),
                accessConditions.getHttpAccessConditions().getIfUnmodifiedSince(),
                accessConditions.getHttpAccessConditions().getIfMatch().toString(),
                accessConditions.getHttpAccessConditions().getIfNoneMatch().toString(),
                null));
    }

    /**
     * Returns the blob's metadata and properties. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob-properties">Azure Docs</a>.
     *
     * @apiNote
     * ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=properties_metadata "Sample code for BlobURL.getProperties")] \n
     * For more samples, please see the [Samples file] (https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
     *
     * @param accessConditions
     *      {@link BlobAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<BlobGetPropertiesResponse> getProperties(
            BlobAccessConditions accessConditions) {
        accessConditions = accessConditions == null ? BlobAccessConditions.NONE : accessConditions;

        return addErrorWrappingToSingle(this.storageClient.generatedBlobs().getPropertiesWithRestResponseAsync(
                null, null,
                accessConditions.getLeaseAccessConditions().getLeaseId(),
                accessConditions.getHttpAccessConditions().getIfModifiedSince(),
                accessConditions.getHttpAccessConditions().getIfUnmodifiedSince(),
                accessConditions.getHttpAccessConditions().getIfMatch().toString(),
                accessConditions.getHttpAccessConditions().getIfNoneMatch().toString(),
                null));
    }

    /**
     * Changes a blob's HTTP header properties. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-properties">Azure Docs</a>.
     *
     * @apiNote
     * ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=properties_metadata "Sample code for BlobURL.setHTTPHeaders")] \n
     * For more samples, please see the [Samples file] (https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
     *
     * @param headers
     *      {@link BlobHTTPHeaders}
     * @param accessConditions
     *      {@link BlobAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<BlobSetHTTPHeadersResponse> setHTTPHeaders(
            BlobHTTPHeaders headers, BlobAccessConditions accessConditions) {
        headers = headers == null ? BlobHTTPHeaders.NONE : headers;
        accessConditions = accessConditions == null ? BlobAccessConditions.NONE : accessConditions;

        return addErrorWrappingToSingle(this.storageClient.generatedBlobs().setHTTPHeadersWithRestResponseAsync(
                null,
                headers.getCacheControl(),
                headers.getContentType(),
                headers.getContentMD5(),
                headers.getContentEncoding(),
                headers.getContentLanguage(),
                accessConditions.getLeaseAccessConditions().getLeaseId(),
                accessConditions.getHttpAccessConditions().getIfModifiedSince(),
                accessConditions.getHttpAccessConditions().getIfUnmodifiedSince(),
                accessConditions.getHttpAccessConditions().getIfMatch().toString(),
                accessConditions.getHttpAccessConditions().getIfNoneMatch().toString(),
                headers.getContentDisposition(),
                null));
    }

    /**
     * Changes a blob's metadata. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-metadata">Azure Docs</a>.
     *
     * @apiNote
     * ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=properties_metadata "Sample code for BlobURL.setMetadata")] \n
     * For more samples, please see the [Samples file] (https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
     *
     * @param metadata
     *      {@link Metadata}
     * @param accessConditions
     *      {@link BlobAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<BlobSetMetadataResponse> setMetadata(
            Metadata metadata, BlobAccessConditions accessConditions) {
        metadata = metadata == null ? Metadata.NONE : metadata;
        accessConditions = accessConditions == null ? BlobAccessConditions.NONE : accessConditions;

        return addErrorWrappingToSingle(this.storageClient.generatedBlobs().setMetadataWithRestResponseAsync(
                null, metadata,
                accessConditions.getLeaseAccessConditions().getLeaseId(),
                accessConditions.getHttpAccessConditions().getIfModifiedSince(),
                accessConditions.getHttpAccessConditions().getIfUnmodifiedSince(),
                accessConditions.getHttpAccessConditions().getIfMatch().toString(),
                accessConditions.getHttpAccessConditions().getIfNoneMatch().toString(),
                null));
    }

    /**
     * Creates a read-only snapshot of a blob. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/snapshot-blob">Azure Docs</a>.
     *
     * @apiNote
     * ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=snapshot "Sample code for BlobURL.createSnapshot")] \n
     * For more samples, please see the [Samples file] (https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
     *
     * @param metadata
     *      {@link Metadata}
     * @param accessConditions
     *      {@link BlobAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<BlobCreateSnapshotResponse> createSnapshot(
            Metadata metadata, BlobAccessConditions accessConditions) {
        metadata = metadata == null ? Metadata.NONE : metadata;
        accessConditions = accessConditions == null ? BlobAccessConditions.NONE : accessConditions;

        return addErrorWrappingToSingle(this.storageClient.generatedBlobs().createSnapshotWithRestResponseAsync(
                null, metadata,
                accessConditions.getHttpAccessConditions().getIfModifiedSince(),
                accessConditions.getHttpAccessConditions().getIfUnmodifiedSince(),
                accessConditions.getHttpAccessConditions().getIfMatch().toString(),
                accessConditions.getHttpAccessConditions().getIfNoneMatch().toString(),
                accessConditions.getLeaseAccessConditions().getLeaseId(),
                null));
    }

    /**
     * Sets the tier on a blob. The operation is allowed on a page blob in a premium storage account or a block blob in
     * a blob storage or GPV2 account. A premium page blob's tier determines the allowed size, IOPS, and bandwidth of
     * the blob. A block blob's tier determines the Hot/Cool/Archive storage type. This does not update the blob's etag.
     * For detailed information about block blob level tiering see the <a href="https://docs.microsoft.com/en-us/azure/storage/blobs/storage-blob-storage-tiers.">Azure Docs</a>.
     *
     * @apiNote
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=tier "Sample code for BlobURL.setTier")]
     *
     * @param tier
     *      The new tier for the blob.
     * @return
     *      Emits the successful response.
     */
    public Single<BlobSetTierResponse> setTier(AccessTier tier) {
        Utility.assertNotNull("tier", tier);

        return addErrorWrappingToSingle(this.storageClient.generatedBlobs().setTierWithRestResponseAsync(tier,
                null,null));
    }


    /**
     * Undelete restores the content and metadata of a soft-deleted blob and/or any associated soft-deleted snapshots.
     * For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/undelete-blob">Azure Docs</a>.
     *
     * @apiNote
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=undelete "Sample code for BlobURL.undelete")]
     *
     * @return
     *      Emits the successful response.
     */
    public Single<BlobUndeleteResponse> undelete() {
        return addErrorWrappingToSingle(this.storageClient.generatedBlobs().undeleteWithRestResponseAsync(null,
                null));
    }

    /**
     * Acquires a lease on the blob for write and delete operations. The lease duration must be between
     * 15 to 60 seconds, or infinite (-1). For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/lease-blob">Azure Docs</a>.
     *
     * @apiNote
     * ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=blob_lease "Sample code for BlobURL.acquireLease")]\n
     * For more samples, please see the [Samples file] (https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
     *
     * @param proposedID
     *      A {@code String} in any valid GUID format. May be null.
     * @param duration
     *      The  duration of the lease, in seconds, or negative one (-1) for a lease that
     *      never expires. A non-infinite lease can be between 15 and 60 seconds.
     * @param httpAccessConditions
     *      {@link HTTPAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<BlobAcquireLeaseResponse> acquireLease(
            String proposedID, int duration, HTTPAccessConditions httpAccessConditions) {
        httpAccessConditions = httpAccessConditions == null ? HTTPAccessConditions.NONE : httpAccessConditions;
        if (!(duration == -1 || (duration >= 15 && duration <=60))) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw new IllegalArgumentException("Duration must be -1 or between 15 and 60.");
        }

        return addErrorWrappingToSingle(this.storageClient.generatedBlobs().acquireLeaseWithRestResponseAsync(
                null, duration, proposedID,
                httpAccessConditions.getIfModifiedSince(),
                httpAccessConditions.getIfUnmodifiedSince(),
                httpAccessConditions.getIfMatch().toString(),
                httpAccessConditions.getIfNoneMatch().toString(),
                null));
    }

    /**
     * Renews the blob's previously-acquired lease. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/lease-blob">Azure Docs</a>.
     *
     * @apiNote
     * ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=blob_lease "Sample code for BlobURL.renewLease")] \n
     * For more samples, please see the [Samples file] (https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
     *
     * @param leaseID
     *      The leaseId of the active lease on the blob.
     * @param httpAccessConditions
     *      {@link HTTPAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<BlobRenewLeaseResponse> renewLease(
            String leaseID, HTTPAccessConditions httpAccessConditions) {
        httpAccessConditions = httpAccessConditions == null ? HTTPAccessConditions.NONE : httpAccessConditions;

        return addErrorWrappingToSingle(this.storageClient.generatedBlobs().renewLeaseWithRestResponseAsync(
                leaseID, null,
                httpAccessConditions.getIfModifiedSince(),
                httpAccessConditions.getIfUnmodifiedSince(),
                httpAccessConditions.getIfMatch().toString(),
                httpAccessConditions.getIfNoneMatch().toString(),
                null));
    }

    /**
     * Releases the blob's previously-acquired lease. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/lease-blob">Azure Docs</a>.
     *
     * @apiNote
     * ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=blob_lease "Sample code for BlobURL.releaseLease")] \n
     * For more samples, please see the [Samples file] (https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
     *
     * @param leaseID
     *      The leaseId of the active lease on the blob.
     * @param httpAccessConditions
     *      {@link HTTPAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<BlobReleaseLeaseResponse> releaseLease(
            String leaseID, HTTPAccessConditions httpAccessConditions) {
        httpAccessConditions = httpAccessConditions == null ? HTTPAccessConditions.NONE : httpAccessConditions;

        return addErrorWrappingToSingle(this.storageClient.generatedBlobs().releaseLeaseWithRestResponseAsync(
                leaseID, null,
                httpAccessConditions.getIfModifiedSince(),
                httpAccessConditions.getIfUnmodifiedSince(),
                httpAccessConditions.getIfMatch().toString(),
                httpAccessConditions.getIfNoneMatch().toString(),
                null));
    }

    /**
     * BreakLease breaks the blob's previously-acquired lease (if it exists). Pass the LeaseBreakDefault (-1) constant
     * to break a fixed-duration lease when it expires or an infinite lease immediately. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/lease-blob">Azure Docs</a>.
     *
     * @apiNote
     * ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=blob_lease "Sample code for BlobURL.breakLease")] \n
     * For more samples, please see the [Samples file] (https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
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
    public Single<BlobBreakLeaseResponse> breakLease(
            Integer breakPeriodInSeconds, HTTPAccessConditions httpAccessConditions) {
        httpAccessConditions = httpAccessConditions == null ? HTTPAccessConditions.NONE : httpAccessConditions;

        return addErrorWrappingToSingle(this.storageClient.generatedBlobs().breakLeaseWithRestResponseAsync(
                null, breakPeriodInSeconds,
                httpAccessConditions.getIfModifiedSince(),
                httpAccessConditions.getIfUnmodifiedSince(),
                httpAccessConditions.getIfMatch().toString(),
                httpAccessConditions.getIfNoneMatch().toString(),
                null));
    }

    /**
     * ChangeLease changes the blob's lease ID. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/lease-blob">Azure Docs</a>.
     *
     * @apiNote
     * ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=blob_lease "Sample code for BlobURL.changeLease")] \n
     * For more samples, please see the [Samples file] (https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
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
    public Single<BlobChangeLeaseResponse> changeLease(
            String leaseId, String proposedID, HTTPAccessConditions httpAccessConditions) {
        httpAccessConditions = httpAccessConditions == null ? HTTPAccessConditions.NONE : httpAccessConditions;

        return addErrorWrappingToSingle(this.storageClient.generatedBlobs().changeLeaseWithRestResponseAsync(
                leaseId, proposedID, null,
                httpAccessConditions.getIfModifiedSince(),
                httpAccessConditions.getIfUnmodifiedSince(),
                httpAccessConditions.getIfMatch().toString(), httpAccessConditions.getIfNoneMatch().toString(),
                null));
    }

    // TODO: Update links
}
