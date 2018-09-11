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
import com.microsoft.rest.v2.Context;
import com.microsoft.rest.v2.http.HttpPipeline;
import io.reactivex.Single;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import static com.microsoft.azure.storage.blob.Utility.addErrorWrappingToSingle;

/**
 * Represents a URL to a blob of any type: block, append, or page. It may be obtained by direct construction or via the
 * create method on a {@link ContainerURL} object. This class does not hold any state about a particular blob but is
 * instead a convenient way of sending off appropriate requests to the resource on the service. Please refer to the <a
 * href=https://docs.microsoft.com/en-us/rest/api/storageservices/understanding-block-blobs--append-blobs--and-page-blobs>Azure
 * Docs</a> for more information.
 */
public class BlobURL extends StorageURL {

    /**
     * Creates a {@code BlobURL} object pointing to the account specified by the URL and using the provided pipeline to
     * make HTTP requests.
     *
     * @param url
     *         A {@code URL} to an Azure Storage blob.
     * @param pipeline
     *         A {@code HttpPipeline} which configures the behavior of HTTP exchanges. Please refer to the
     *         createPipeline method on {@link StorageURL} for more information.
     */
    public BlobURL(URL url, HttpPipeline pipeline) {
        super(url, pipeline);
    }

    /**
     * Creates a new {@link BlobURL} with the given pipeline.
     *
     * @param pipeline
     *         An {@link HttpPipeline} object to set.
     * @return A {@link BlobURL} object with the given pipeline.
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
     *         A {@code String} to set.
     * @return A {@link BlobURL} object with the given pipeline.
     * @throws MalformedURLException
     *         Appending the specified snapshot produced an invalid URL.
     * @throws UnknownHostException
     *         If the url contains an improperly formatted ipaddress or unknown host address.
     */
    public BlobURL withSnapshot(String snapshot) throws MalformedURLException, UnknownHostException {
        BlobURLParts blobURLParts = URLParser.parse(new URL(this.storageClient.url()));
        blobURLParts.withSnapshot(snapshot);
        return new BlobURL(blobURLParts.toURL(), super.storageClient.httpPipeline());
    }

    /**
     * Converts this BlobURL to a {@link BlockBlobURL} object. Note that this does not change the actual type of the
     * blob if it has already been created.
     *
     * @return A {@link BlockBlobURL} object.
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
     * @return An {@link AppendBlobURL} object.
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
     * @return A {@link PageBlobURL} object.
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
     * Copies the data at the source URL to a blob. For more information, see the <a
     * href="https://docs.microsoft.com/rest/api/storageservices/copy-blob">Azure Docs</a>
     *
     * @param sourceURL
     *         The source URL to copy from. URLs outside of Azure may only be copied to block blobs.
     * @param metadata
     *      {@link Metadata}
     * @param sourceModifiedAccessConditions
     *      {@link ModifiedAccessConditions} against the source.
     * @param destAccessConditions
     *      {@link BlobAccessConditions} against the destination.
     * @param context
     *      {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *      {@link com.microsoft.rest.v2.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *      arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *      immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *      parent, forming a linked list.
     * @return Emits the successful response.
     * @apiNote ## Sample Code \n [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=start_copy
     * "Sample code for BlobURL.startCopyFromURL")] \n [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=start_copy_helper
     * "Helper for start_copy sample.")] \n For more samples, please see the [Samples
     * file](%https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Single<BlobStartCopyFromURLResponse> startCopyFromURL(URL sourceURL, Metadata metadata,
            ModifiedAccessConditions sourceModifiedAccessConditions, BlobAccessConditions destAccessConditions,
            Context context) {
        metadata = metadata == null ? Metadata.NONE : metadata;
        sourceModifiedAccessConditions = sourceModifiedAccessConditions == null ?
                new ModifiedAccessConditions() : sourceModifiedAccessConditions;
        destAccessConditions = destAccessConditions == null ? BlobAccessConditions.NONE : destAccessConditions;
        context = context == null ? Context.NONE : context;

        // We want to hide the SourceAccessConditions type from the user for consistency's sake, so we convert here.
        SourceModifiedAccessConditions sourceConditions = new SourceModifiedAccessConditions()
                .withSourceIfModifiedSince(sourceModifiedAccessConditions.ifModifiedSince())
                .withSourceIfUnmodifiedSince(sourceModifiedAccessConditions.ifUnmodifiedSince())
                .withSourceIfMatch(sourceModifiedAccessConditions.ifMatch())
                .withSourceIfNoneMatch(sourceModifiedAccessConditions.ifNoneMatch());

        return addErrorWrappingToSingle(this.storageClient.generatedBlobs().startCopyFromURLWithRestResponseAsync(
               context, sourceURL, null, metadata, null, sourceConditions,
                destAccessConditions.modifiedAccessConditions(), destAccessConditions.leaseAccessConditions()));
    }

    /**
     * Stops a pending copy that was previously started and leaves a destination blob with 0 length and metadata. For
     * more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/abort-copy-blob">Azure
     * Docs</a>.
     *
     * @param copyId
     *      The id of the copy operation to abort. Returned as the {@code copyId} field on the {@link
     *      BlobStartCopyFromURLHeaders} object.
     * @param leaseAccessConditions
     *      {@link LeaseAccessConditions}
     * @param context
     *      {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *      {@link com.microsoft.rest.v2.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *      arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *      immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *      parent, forming a linked list.
     * @return Emits the successful response.
     * @apiNote ## Sample Code \n [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=abort_copy
     * "Sample code for BlobURL.abortCopyFromURL")] \n For more samples, please see the [Samples
     * file](%https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Single<BlobAbortCopyFromURLResponse> abortCopyFromURL(String copyId,
            LeaseAccessConditions leaseAccessConditions, Context context) {
        context = context == null ? Context.NONE : context;

        return addErrorWrappingToSingle(this.storageClient.generatedBlobs().abortCopyFromURLWithRestResponseAsync(
                context, copyId, null, null, leaseAccessConditions));
    }

    /**
     * Reads a range of bytes from a blob. The response also includes the blob's properties and metadata. For more
     * information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a>.
     * <p>
     * Note that the response body has reliable download functionality built in, meaning that a failed download stream
     * will be automatically retried. This behavior may be configured with {@link ReliableDownloadOptions}.
     *
     * @param range
     *         {@link BlobRange}
     * @param accessConditions
     *         {@link BlobAccessConditions}
     * @param rangeGetContentMD5
     *         Whether the contentMD5 for the specified blob range should be returned.
     * @param context
     *      {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *      {@link com.microsoft.rest.v2.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *      arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *      immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *      parent, forming a linked list.
     *
     * @return Emits the successful response.
     * @apiNote ## Sample Code \n [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=upload_download
     * "Sample code for BlobURL.download")] \n For more samples, please see the [Samples
     * file](%https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Single<DownloadResponse> download(BlobRange range, BlobAccessConditions accessConditions,
            boolean rangeGetContentMD5, Context context) {
        Boolean getMD5 = rangeGetContentMD5 ? rangeGetContentMD5 : null;
        range = range == null ? BlobRange.DEFAULT : range;
        accessConditions = accessConditions == null ? BlobAccessConditions.NONE : accessConditions;
        HTTPGetterInfo info = new HTTPGetterInfo()
                .withCount(range.offset())
                .withCount(range.count())
                .withETag(accessConditions.modifiedAccessConditions().ifMatch());

        return addErrorWrappingToSingle(this.storageClient.generatedBlobs().downloadWithRestResponseAsync(
                context, null, null, range.toString(), getMD5, null,
                accessConditions.leaseAccessConditions(),
                accessConditions.modifiedAccessConditions()))
                // Convert the autorest response to a DownloadResponse, which enable reliable download.
                .map(response -> {
                    // If there wasn't an etag originally specified, lock on the one returned.
                    info.withETag(response.headers().eTag());
                    return new DownloadResponse(response, info,
                            // In the event of a stream failure, make a new request to pick up where we left off.
                            newInfo ->
                                    this.download(new BlobRange().withOffset(newInfo.offset())
                                                    .withCount(newInfo.count()),
                                            new BlobAccessConditions().withModifiedAccessConditions(
                                                    new ModifiedAccessConditions().withIfMatch(info.eTag())), false,
                                            context == null ? Context.NONE : context));
                });
    }

    /**
     * Deletes the specified blob or snapshot. Note that deleting a blob also deletes all its snapshots. For more
     * information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/delete-blob">Azure Docs</a>.
     *
     * @param deleteBlobSnapshotOptions
     *         Specifies the behavior for deleting the snapshots on this blob. {@code Include} will delete the base blob
     *         and all snapshots. {@code Only} will delete only the snapshots. If a snapshot is being deleted, you must
     *         pass null.
     * @param accessConditions
     *         {@link BlobAccessConditions}
     * @param context
     *      {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *      {@link com.microsoft.rest.v2.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *      arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *      immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *      parent, forming a linked list.
     * @return Emits the successful response.
     * @apiNote ## Sample Code \n [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=blob_delete
     * "Sample code for BlobURL.delete")] \n For more samples, please see the [Samples
     * file](%https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Single<BlobDeleteResponse> delete(DeleteSnapshotsOptionType deleteBlobSnapshotOptions,
            BlobAccessConditions accessConditions, Context context) {
        accessConditions = accessConditions == null ? BlobAccessConditions.NONE : accessConditions;
        context = context == null ? Context.NONE : context;

        return addErrorWrappingToSingle(this.storageClient.generatedBlobs().deleteWithRestResponseAsync(
                context, null, null, deleteBlobSnapshotOptions, null, accessConditions.leaseAccessConditions(),
                accessConditions.modifiedAccessConditions()));
    }

    /**
     * Returns the blob's metadata and properties. For more information, see the <a
     * href="https://docs.microsoft.com/rest/api/storageservices/get-blob-properties">Azure Docs</a>.
     *
     * @param accessConditions
     *         {@link BlobAccessConditions}
     * @param context
     *      {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *      {@link com.microsoft.rest.v2.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *      arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *      immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *      parent, forming a linked list.
     * @return Emits the successful response.
     * @apiNote ## Sample Code \n [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=properties_metadata
     * "Sample code for BlobURL.getProperties")] \n For more samples, please see the [Samples
     * file](%https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Single<BlobGetPropertiesResponse> getProperties(BlobAccessConditions accessConditions, Context context) {
        accessConditions = accessConditions == null ? BlobAccessConditions.NONE : accessConditions;
        context = context == null ? Context.NONE : context;

        return addErrorWrappingToSingle(this.storageClient.generatedBlobs().getPropertiesWithRestResponseAsync(
                context, null, null, null, accessConditions.leaseAccessConditions(),
                accessConditions.modifiedAccessConditions()));
    }

    /**
     * Changes a blob's HTTP header properties. For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-properties">Azure
     * Docs</a>.
     *
     * @param headers
     *      {@link BlobHTTPHeaders}
     * @param accessConditions
     *         {@link BlobAccessConditions}
     * @param context
     *      {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *      {@link com.microsoft.rest.v2.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *      arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *      immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *      parent, forming a linked list.
     * @return Emits the successful response.
     * @apiNote ## Sample Code \n [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=properties_metadata
     * "Sample code for BlobURL.setHTTPHeaders")] \n For more samples, please see the [Samples
     * file](%https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Single<BlobSetHTTPHeadersResponse> setHTTPHeaders(BlobHTTPHeaders headers,
            BlobAccessConditions accessConditions, Context context) {
        accessConditions = accessConditions == null ? BlobAccessConditions.NONE : accessConditions;
        context = context == null ? Context.NONE : context;

        return addErrorWrappingToSingle(this.storageClient.generatedBlobs().setHTTPHeadersWithRestResponseAsync(
                context, null, null, headers, accessConditions.leaseAccessConditions(),
                accessConditions.modifiedAccessConditions()));
    }

    /**
     * Changes a blob's metadata. For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-metadata">Azure
     * Docs</a>.
     *
     * @param metadata
     *         {@link Metadata}
     * @param accessConditions
     *         {@link BlobAccessConditions}
     * @param context
     *      {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *      {@link com.microsoft.rest.v2.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *      arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *      immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *      parent, forming a linked list.
     * @return Emits the successful response.
     * @apiNote ## Sample Code \n [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=properties_metadata
     * "Sample code for BlobURL.setMetadata")] \n For more samples, please see the [Samples
     * file](%https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Single<BlobSetMetadataResponse> setMetadata(Metadata metadata, BlobAccessConditions accessConditions,
            Context context) {
        metadata = metadata == null ? Metadata.NONE : metadata;
        accessConditions = accessConditions == null ? BlobAccessConditions.NONE : accessConditions;
        context = context == null ? Context.NONE : context;

        return addErrorWrappingToSingle(this.storageClient.generatedBlobs().setMetadataWithRestResponseAsync(
                context, null, metadata, null, accessConditions.leaseAccessConditions(),
                accessConditions.modifiedAccessConditions()));
    }

    /**
     * Creates a read-only snapshot of a blob. For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/snapshot-blob">Azure
     * Docs</a>.
     *
     * @param metadata
     *         {@link Metadata}
     * @param accessConditions
     *         {@link BlobAccessConditions}
     * @param context
     *      {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *      {@link com.microsoft.rest.v2.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *      arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *      immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *      parent, forming a linked list.
     * @return Emits the successful response.
     * @apiNote ## Sample Code \n [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=snapshot
     * "Sample code for BlobURL.createSnapshot")] \n For more samples, please see the [Samples
     * file](%https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Single<BlobCreateSnapshotResponse> createSnapshot(Metadata metadata, BlobAccessConditions accessConditions,
            Context context) {
        metadata = metadata == null ? Metadata.NONE : metadata;
        accessConditions = accessConditions == null ? BlobAccessConditions.NONE : accessConditions;
        context = context == null ? Context.NONE : context;

        return addErrorWrappingToSingle(this.storageClient.generatedBlobs().createSnapshotWithRestResponseAsync(
                context, null, metadata, null, accessConditions.modifiedAccessConditions(),
                accessConditions.leaseAccessConditions()));
    }

    /**
     * Sets the tier on a blob. The operation is allowed on a page blob in a premium storage account or a block blob in
     * a blob storage or GPV2 account. A premium page blob's tier determines the allowed size, IOPS, and bandwidth of
     * the blob. A block blob's tier determines the Hot/Cool/Archive storage type. This does not update the blob's etag.

     * For detailed information about block blob level tiering see the <a href="https://docs.microsoft.com/en-us/azure/storage/blobs/storage-blob-storage-tiers.">Azure Docs</a>.
     *
     * @apiNote
     * ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=tier "Sample code for BlobURL.setTier")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
     *
     * @param tier
     *      The new tier for the blob.
     * @param leaseAccessConditions
     *      {@link LeaseAccessConditions}
     * @param context
     *      {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *      {@link com.microsoft.rest.v2.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *      arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *      immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *      parent, forming a linked list.
     * @return
     *      Emits the successful response.
     */
    public Single<BlobSetTierResponse> setTier(AccessTier tier, LeaseAccessConditions leaseAccessConditions,
            Context context) {
        Utility.assertNotNull("tier", tier);
        context = context == null ? Context.NONE : context;

        return addErrorWrappingToSingle(this.storageClient.generatedBlobs().setTierWithRestResponseAsync(context, tier,
                null, null, leaseAccessConditions));
    }


    /**
     * Undelete restores the content and metadata of a soft-deleted blob and/or any associated soft-deleted snapshots.
     * For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/undelete-blob">Azure
     * Docs</a>.
     *
     * @apiNote
     * ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=undelete "Sample code for BlobURL.undelete")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
     *
     * @return
     *      Emits the successful response.
     * @param context
     *      {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *      {@link com.microsoft.rest.v2.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *      arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *      immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *      parent, forming a linked list.
     */
    public Single<BlobUndeleteResponse> undelete(Context context) {
        context = context == null ? Context.NONE : context;

        return addErrorWrappingToSingle(this.storageClient.generatedBlobs().undeleteWithRestResponseAsync(context, null,
                null));
    }

    /**
     * Acquires a lease on the blob for write and delete operations. The lease duration must be between 15 to 60
     * seconds, or infinite (-1). For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/lease-blob">Azure
     * Docs</a>.
     *
     * @param proposedID
     *         A {@code String} in any valid GUID format. May be null.
     * @param duration
     *      The  duration of the lease, in seconds, or negative one (-1) for a lease that
     *      never expires. A non-infinite lease can be between 15 and 60 seconds.
     * @param modifiedAccessConditions
     *      {@link ModifiedAccessConditions}
     * @param context
     *      {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *      {@link com.microsoft.rest.v2.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *      arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *      immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *      parent, forming a linked list.
     * @return
     *      Emits the successful response.
     */
    public Single<BlobAcquireLeaseResponse> acquireLease(String proposedID, int duration,
            ModifiedAccessConditions modifiedAccessConditions, Context context) {
        if (!(duration == -1 || (duration >= 15 && duration <=60))) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw new IllegalArgumentException("Duration must be -1 or between 15 and 60.");
        }
        context = context == null ? Context.NONE : context;

        return addErrorWrappingToSingle(this.storageClient.generatedBlobs().acquireLeaseWithRestResponseAsync(context,
                null, duration, proposedID, null, modifiedAccessConditions));
    }

    /**
     * Renews the blob's previously-acquired lease. For more information, see the <a
     * href="https://docs.microsoft.com/rest/api/storageservices/lease-blob">Azure Docs</a>.
     *
     * @param leaseID
     *      The leaseId of the active lease on the blob.
     * @param modifiedAccessConditions
     *      {@link ModifiedAccessConditions}
     * @param context
     *      {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *      {@link com.microsoft.rest.v2.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *      arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *      immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *      parent, forming a linked list.
     * @return
     *      Emits the successful response.
     */
    public Single<BlobRenewLeaseResponse> renewLease(String leaseID, ModifiedAccessConditions modifiedAccessConditions,
            Context context) {
        context = context == null ? Context.NONE : context;

        return addErrorWrappingToSingle(this.storageClient.generatedBlobs().renewLeaseWithRestResponseAsync(context,
                leaseID, null, null, modifiedAccessConditions));
    }

    /**
     * Releases the blob's previously-acquired lease. For more information, see the <a
     * href="https://docs.microsoft.com/rest/api/storageservices/lease-blob">Azure Docs</a>.
     *
     * @param leaseID
     *         The leaseId of the active lease on the blob.
     * @param modifiedAccessConditions
     *      {@link ModifiedAccessConditions}
     *@param context
     *      {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *      {@link com.microsoft.rest.v2.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *      arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *      immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *      parent, forming a linked list.
     * @apiNote ## Sample Code \n [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=blob_lease
     * "Sample code for BlobURL.releaseLease")] \n For more samples, please see the [Samples
     * file](%https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
     * @return
     *      Emits the successful response.
     */
    public Single<BlobReleaseLeaseResponse> releaseLease(String leaseID,
            ModifiedAccessConditions modifiedAccessConditions, Context context) {
        context = context == null ? Context.NONE : context;

        return addErrorWrappingToSingle(this.storageClient.generatedBlobs().releaseLeaseWithRestResponseAsync(context,
                leaseID, null, null, modifiedAccessConditions));
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
     * @param modifiedAccessConditions
     *      {@link ModifiedAccessConditions}
     *@param context
     *      {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *      {@link com.microsoft.rest.v2.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *      arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *      immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *      parent, forming a linked list.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=blob_lease "Sample code for BlobURL.breakLease")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
     *
     * @return
     *      Emits the successful response.
     */
    public Single<BlobBreakLeaseResponse> breakLease(Integer breakPeriodInSeconds,
            ModifiedAccessConditions modifiedAccessConditions, Context context) {
        context = context == null ? Context.NONE : context;

        return addErrorWrappingToSingle(this.storageClient.generatedBlobs().breakLeaseWithRestResponseAsync(context,
                null, breakPeriodInSeconds, null, modifiedAccessConditions));
    }

    /**
     * ChangeLease changes the blob's lease ID. For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/lease-blob">Azure
     * Docs</a>.
     *
     * @param leaseId
     *         The leaseId of the active lease on the blob.
     * @param proposedID
     *         A {@code String} in any valid GUID format.
     * @param modifiedAccessConditions
     *      {@link ModifiedAccessConditions}
     *@param context
     *      {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *      {@link com.microsoft.rest.v2.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *      arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *      immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *      parent, forming a linked list.
     *
     * @apiNote ## Sample Code \n [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=blob_lease
     * "Sample code for BlobURL.changeLease")] \n For more samples, please see the [Samples
     * file](%https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
     * @return
     *      Emits the successful response.
     */
    public Single<BlobChangeLeaseResponse> changeLease(String leaseId, String proposedID,
            ModifiedAccessConditions modifiedAccessConditions, Context context) {
        context = context == null ? Context.NONE : context;

        return addErrorWrappingToSingle(this.storageClient.generatedBlobs().changeLeaseWithRestResponseAsync(context,
                leaseId, proposedID, null, null, modifiedAccessConditions));
    }

    /**
     * Returns the sku name and account kind for the account. For more information, please see the <a
     * href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-account-information">Azure Docs</a>.
     *
     * @param context
     *      {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *      {@link com.microsoft.rest.v2.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *      arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *      immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *      parent, forming a linked list.
     *
     * @return Emits the successful response.
     * @apiNote ## Sample code \n [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=account_info
     * "Sample code for BlobURL.getAccountInfo")] \n For more samples, please see the [Samples file]
     * (https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Single<BlobGetAccountInfoResponse> getAccountInfo(Context context) {
        context = context == null ? Context.NONE : context;

        return addErrorWrappingToSingle(
                this.storageClient.generatedBlobs().getAccountInfoWithRestResponseAsync(context));
    }

    // TODO: Update links
}
