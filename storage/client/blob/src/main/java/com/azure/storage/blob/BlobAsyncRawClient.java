// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.util.Context;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.BlobHTTPHeaders;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobStartCopyFromURLHeaders;
import com.azure.storage.blob.models.BlobsAbortCopyFromURLResponse;
import com.azure.storage.blob.models.BlobsAcquireLeaseResponse;
import com.azure.storage.blob.models.BlobsBreakLeaseResponse;
import com.azure.storage.blob.models.BlobsChangeLeaseResponse;
import com.azure.storage.blob.models.BlobsCopyFromURLResponse;
import com.azure.storage.blob.models.BlobsCreateSnapshotResponse;
import com.azure.storage.blob.models.BlobsDeleteResponse;
import com.azure.storage.blob.models.BlobsGetAccountInfoResponse;
import com.azure.storage.blob.models.BlobsGetPropertiesResponse;
import com.azure.storage.blob.models.BlobsReleaseLeaseResponse;
import com.azure.storage.blob.models.BlobsRenewLeaseResponse;
import com.azure.storage.blob.models.BlobsSetHTTPHeadersResponse;
import com.azure.storage.blob.models.BlobsSetMetadataResponse;
import com.azure.storage.blob.models.BlobsSetTierResponse;
import com.azure.storage.blob.models.BlobsStartCopyFromURLResponse;
import com.azure.storage.blob.models.BlobsUndeleteResponse;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.storage.blob.models.LeaseAccessConditions;
import com.azure.storage.blob.models.Metadata;
import com.azure.storage.blob.models.ModifiedAccessConditions;
import com.azure.storage.blob.models.ReliableDownloadOptions;
import com.azure.storage.blob.models.SourceModifiedAccessConditions;
import reactor.core.publisher.Mono;

import java.net.URL;

import static com.azure.storage.blob.Utility.postProcessResponse;

/**
 * Represents a URL to a blob of any type: block, append, or page. It may be obtained by direct construction or via the
 * create method on a {@link ContainerAsyncClient} object. This class does not hold any state about a particular blob but is
 * instead a convenient way of sending off appropriate requests to the resource on the service. Please refer to the
 * <a href=https://docs.microsoft.com/en-us/rest/api/storageservices/understanding-block-blobs--append-blobs--and-page-blobs>Azure Docs</a> for more information.
 */
class BlobAsyncRawClient {

    protected AzureBlobStorageImpl azureBlobStorage;

    final String snapshot;

    /**
     * Creates a {@code BlobAsyncRawClient} object pointing to the account specified by the URL and using the provided pipeline to
     * make HTTP requests..
     */
    BlobAsyncRawClient(AzureBlobStorageImpl azureBlobStorage, String snapshot) {
        this.azureBlobStorage = azureBlobStorage;
        this.snapshot = snapshot;
    }

    /**
     * Copies the data at the source URL to a blob. For more information, see the <a
     * href="https://docs.microsoft.com/rest/api/storageservices/copy-blob">Azure Docs</a>
     *
     * @param sourceURL
     *         The source URL to copy from. URLs outside of Azure may only be copied to block blobs.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=start_copy "Sample code for BlobAsyncRawClient.startCopyFromURL")] \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=start_copy_helper "Helper for start_copy sample.")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<BlobsStartCopyFromURLResponse> startCopyFromURL(URL sourceURL) {
        return this.startCopyFromURL(sourceURL, null, null, null);
    }

    /**
     * Copies the data at the source URL to a blob. For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/copy-blob">Azure Docs</a>
     *
     * @param sourceURL
     *         The source URL to copy from. URLs outside of Azure may only be copied to block blobs.
     * @param metadata
     *         {@link Metadata}
     * @param sourceModifiedAccessConditions
     *         {@link ModifiedAccessConditions} against the source. Standard HTTP Access conditions related to the
     *         modification of data. ETag and LastModifiedTime are used to construct conditions related to when the blob
     *         was changed relative to the given request. The request will fail if the specified condition is not
     *         satisfied.
     * @param destAccessConditions
     *         {@link BlobAccessConditions} against the destination.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=start_copy "Sample code for BlobAsyncRawClient.startCopyFromURL")] \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=start_copy_helper "Helper for start_copy sample.")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<BlobsStartCopyFromURLResponse> startCopyFromURL(URL sourceURL, Metadata metadata,
                                                                 ModifiedAccessConditions sourceModifiedAccessConditions, BlobAccessConditions destAccessConditions) {
        metadata = metadata == null ? new Metadata() : metadata;
        sourceModifiedAccessConditions = sourceModifiedAccessConditions == null
            ? new ModifiedAccessConditions() : sourceModifiedAccessConditions;
        destAccessConditions = destAccessConditions == null ? new BlobAccessConditions() : destAccessConditions;

        // We want to hide the SourceAccessConditions type from the user for consistency's sake, so we convert here.
        SourceModifiedAccessConditions sourceConditions = new SourceModifiedAccessConditions()
            .sourceIfModifiedSince(sourceModifiedAccessConditions.ifModifiedSince())
            .sourceIfUnmodifiedSince(sourceModifiedAccessConditions.ifUnmodifiedSince())
            .sourceIfMatch(sourceModifiedAccessConditions.ifMatch())
            .sourceIfNoneMatch(sourceModifiedAccessConditions.ifNoneMatch());

        return postProcessResponse(this.azureBlobStorage.blobs().startCopyFromURLWithRestResponseAsync(
            null, null,  sourceURL, null, metadata, null, sourceConditions,
            destAccessConditions.modifiedAccessConditions(), destAccessConditions.leaseAccessConditions(), Context.NONE));
    }

    /**
     * Stops a pending copy that was previously started and leaves a destination blob with 0 length and metadata. For
     * more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/abort-copy-blob">Azure Docs</a>.
     *
     * @param copyId
     *         The id of the copy operation to abort. Returned as the {@code copyId} field on the {@link
     *         BlobStartCopyFromURLHeaders} object.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=abort_copy "Sample code for BlobAsyncRawClient.abortCopyFromURL")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<BlobsAbortCopyFromURLResponse> abortCopyFromURL(String copyId) {
        return this.abortCopyFromURL(copyId, null);
    }

    /**
     * Stops a pending copy that was previously started and leaves a destination blob with 0 length and metadata. For
     * more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/abort-copy-blob">Azure Docs</a>.
     *
     * @param copyId
     *         The id of the copy operation to abort. Returned as the {@code copyId} field on the {@link
     *         BlobStartCopyFromURLHeaders} object.
     * @param leaseAccessConditions
     *         By setting lease access conditions, requests will fail if the provided lease does not match the active
     *         lease on the blob.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=abort_copy "Sample code for BlobAsyncRawClient.abortCopyFromURL")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<BlobsAbortCopyFromURLResponse> abortCopyFromURL(String copyId,
                                                                LeaseAccessConditions leaseAccessConditions) {
        return postProcessResponse(this.azureBlobStorage.blobs().abortCopyFromURLWithRestResponseAsync(
            null, null, copyId, null, null, leaseAccessConditions, Context.NONE));
    }

    /**
     * Copies the data at the source URL to a blob and waits for the copy to complete before returning a response.
     * For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/copy-blob">Azure Docs</a>
     *
     * @param copySource
     *         The source URL to copy from.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=sync_copy "Sample code for BlobAsyncRawClient.copyFromURL")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<BlobsCopyFromURLResponse> syncCopyFromURL(URL copySource) {
        return this.syncCopyFromURL(copySource, null, null, null);
    }

    /**
     * Copies the data at the source URL to a blob and waits for the copy to complete before returning a response.
     * For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/copy-blob">Azure Docs</a>
     *
     * @param copySource
     *         The source URL to copy from. URLs outside of Azure may only be copied to block blobs.
     * @param metadata
     *         {@link Metadata}
     * @param sourceModifiedAccessConditions
     *         {@link ModifiedAccessConditions} against the source. Standard HTTP Access conditions related to the
     *         modification of data. ETag and LastModifiedTime are used to construct conditions related to when the blob
     *         was changed relative to the given request. The request will fail if the specified condition is not
     *         satisfied.
     * @param destAccessConditions
     *         {@link BlobAccessConditions} against the destination.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=sync_copy "Sample code for BlobAsyncRawClient.copyFromURL")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<BlobsCopyFromURLResponse> syncCopyFromURL(URL copySource, Metadata metadata,
                                                           ModifiedAccessConditions sourceModifiedAccessConditions, BlobAccessConditions destAccessConditions) {
        metadata = metadata == null ? new Metadata() : metadata;
        sourceModifiedAccessConditions = sourceModifiedAccessConditions == null
            ? new ModifiedAccessConditions() : sourceModifiedAccessConditions;
        destAccessConditions = destAccessConditions == null ? new BlobAccessConditions() : destAccessConditions;

        // We want to hide the SourceAccessConditions type from the user for consistency's sake, so we convert here.
        SourceModifiedAccessConditions sourceConditions = new SourceModifiedAccessConditions()
            .sourceIfModifiedSince(sourceModifiedAccessConditions.ifModifiedSince())
            .sourceIfUnmodifiedSince(sourceModifiedAccessConditions.ifUnmodifiedSince())
            .sourceIfMatch(sourceModifiedAccessConditions.ifMatch())
            .sourceIfNoneMatch(sourceModifiedAccessConditions.ifNoneMatch());

        return postProcessResponse(this.azureBlobStorage.blobs().copyFromURLWithRestResponseAsync(
            null, null, copySource, null, metadata, null, sourceConditions,
            destAccessConditions.modifiedAccessConditions(), destAccessConditions.leaseAccessConditions(), Context.NONE));
    }

    /**
     * Reads a range of bytes from a blob. The response also includes the blob's properties and metadata. For more
     * information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a>.
     * <p>
     * Note that the response body has reliable download functionality built in, meaning that a failed download stream
     * will be automatically retried. This behavior may be configured with {@link ReliableDownloadOptions}.
     *
     * @return Emits the successful response.
     * @apiNote ## Sample Code \n [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=upload_download
     * "Sample code for BlobAsyncRawClient.download")] \n For more samples, please see the [Samples
     * file](%https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<DownloadAsyncResponse> download() {
        return this.download(null, null, false);
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
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=upload_download "Sample code for BlobAsyncRawClient.download")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<DownloadAsyncResponse> download(BlobRange range, BlobAccessConditions accessConditions,
                                             boolean rangeGetContentMD5) {
        range = range == null ? new BlobRange(0) : range;
        Boolean getMD5 = rangeGetContentMD5 ? rangeGetContentMD5 : null;
        accessConditions = accessConditions == null ? new BlobAccessConditions() : accessConditions;
        HTTPGetterInfo info = new HTTPGetterInfo()
            .offset(range.offset())
            .count(range.count())
            .eTag(accessConditions.modifiedAccessConditions().ifMatch());

        // TODO: range is BlobRange but expected as String
        // TODO: figure out correct response
        return postProcessResponse(this.azureBlobStorage.blobs().downloadWithRestResponseAsync(
            null, null, snapshot, null, null, range.toHeaderValue(), getMD5,
            null, null, null, null,
            accessConditions.leaseAccessConditions(),  accessConditions.modifiedAccessConditions(), Context.NONE))
            // Convert the autorest response to a DownloadAsyncResponse, which enable reliable download.
            .map(response -> {
                // If there wasn't an etag originally specified, lock on the one returned.
                info.eTag(response.deserializedHeaders().eTag());
                return new DownloadAsyncResponse(response, info,
                    // In the event of a stream failure, make a new request to pick up where we left off.
                    newInfo ->
                        this.download(new BlobRange(newInfo.offset(), newInfo.count()),
                            new BlobAccessConditions().modifiedAccessConditions(
                                new ModifiedAccessConditions().ifMatch(info.eTag())), false));
            });
    }

    /**
     * Deletes the specified blob or snapshot. Note that deleting a blob also deletes all its snapshots. For more
     * information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/delete-blob">Azure Docs</a>.
     *
     * @return Emits the successful response.
     * @apiNote ## Sample Code \n [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=blob_delete
     * "Sample code for BlobAsyncRawClient.delete")] \n For more samples, please see the [Samples
     * file](%https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<BlobsDeleteResponse> delete() {
        return this.delete(null, null);
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
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=blob_delete "Sample code for BlobAsyncRawClient.delete")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<BlobsDeleteResponse> delete(DeleteSnapshotsOptionType deleteBlobSnapshotOptions,
                                            BlobAccessConditions accessConditions) {
        accessConditions = accessConditions == null ? new BlobAccessConditions() : accessConditions;

        return postProcessResponse(this.azureBlobStorage.blobs().deleteWithRestResponseAsync(
            null, null, snapshot, null, null, deleteBlobSnapshotOptions,
            null, accessConditions.leaseAccessConditions(), accessConditions.modifiedAccessConditions(),
            Context.NONE));
    }

    /**
     * Returns the blob's metadata and properties. For more information, see the <a
     * href="https://docs.microsoft.com/rest/api/storageservices/get-blob-properties">Azure Docs</a>.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=properties_metadata "Sample code for BlobAsyncRawClient.getProperties")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<BlobsGetPropertiesResponse> getProperties() {
        return this.getProperties(null);
    }

    /**
     * Returns the blob's metadata and properties. For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob-properties">Azure Docs</a>.
     *
     * @param accessConditions
     *         {@link BlobAccessConditions}
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=properties_metadata "Sample code for BlobAsyncRawClient.getProperties")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<BlobsGetPropertiesResponse> getProperties(BlobAccessConditions accessConditions) {
        accessConditions = accessConditions == null ? new BlobAccessConditions() : accessConditions;

        return postProcessResponse(this.azureBlobStorage.blobs().getPropertiesWithRestResponseAsync(
            null, null, snapshot, null, null, null,
            null, null, null, accessConditions.leaseAccessConditions(),
            accessConditions.modifiedAccessConditions(), Context.NONE));
    }

    /**
     * Changes a blob's HTTP header properties. For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-properties">Azure
     * Docs</a>.
     *
     * @param headers
     *         {@link BlobHTTPHeaders}
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=properties_metadata "Sample code for BlobAsyncRawClient.setHTTPHeaders")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<BlobsSetHTTPHeadersResponse> setHTTPHeaders(BlobHTTPHeaders headers) {
        return this.setHTTPHeaders(headers, null);
    }

    /**
     * Changes a blob's HTTP header properties. For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-properties">Azure Docs</a>.
     *
     * @param headers
     *         {@link BlobHTTPHeaders}
     * @param accessConditions
     *         {@link BlobAccessConditions}
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=properties_metadata "Sample code for BlobAsyncRawClient.setHTTPHeaders")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<BlobsSetHTTPHeadersResponse> setHTTPHeaders(BlobHTTPHeaders headers,
                                                            BlobAccessConditions accessConditions) {
        accessConditions = accessConditions == null ? new BlobAccessConditions() : accessConditions;

        return postProcessResponse(this.azureBlobStorage.blobs().setHTTPHeadersWithRestResponseAsync(
            null, null, null, null, headers,
            accessConditions.leaseAccessConditions(), accessConditions.modifiedAccessConditions(), Context.NONE));
    }

    /**
     * Changes a blob's metadata. For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-metadata">Azure Docs</a>.
     *
     * @param metadata
     *         {@link Metadata}
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=properties_metadata "Sample code for BlobAsyncRawClient.setMetadata")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<BlobsSetMetadataResponse> setMetadata(Metadata metadata) {
        return this.setMetadata(metadata, null);
    }

    /**
     * Changes a blob's metadata. For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-metadata">Azure Docs</a>.
     *
     * @param metadata
     *         {@link Metadata}
     * @param accessConditions
     *         {@link BlobAccessConditions}
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=properties_metadata "Sample code for BlobAsyncRawClient.setMetadata")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<BlobsSetMetadataResponse> setMetadata(Metadata metadata, BlobAccessConditions accessConditions) {
        metadata = metadata == null ? new Metadata() : metadata;
        accessConditions = accessConditions == null ? new BlobAccessConditions() : accessConditions;

        return postProcessResponse(this.azureBlobStorage.blobs().setMetadataWithRestResponseAsync(
            null, null, null, metadata, null, null,
            null, null, accessConditions.leaseAccessConditions(),
            accessConditions.modifiedAccessConditions(), Context.NONE));
    }

    /**
     * Creates a read-only snapshot of a blob. For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/snapshot-blob">Azure Docs</a>.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=snapshot "Sample code for BlobAsyncRawClient.createSnapshot")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<BlobsCreateSnapshotResponse> createSnapshot() {
        return this.createSnapshot(null, null);
    }

    /**
     * Creates a read-only snapshot of a blob. For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/snapshot-blob">Azure Docs</a>.
     *
     * @param metadata
     *         {@link Metadata}
     * @param accessConditions
     *         {@link BlobAccessConditions}
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=snapshot "Sample code for BlobAsyncRawClient.createSnapshot")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<BlobsCreateSnapshotResponse> createSnapshot(Metadata metadata, BlobAccessConditions accessConditions) {
        metadata = metadata == null ? new Metadata() : metadata;
        accessConditions = accessConditions == null ? new BlobAccessConditions() : accessConditions;

        return postProcessResponse(this.azureBlobStorage.blobs().createSnapshotWithRestResponseAsync(
            null, null, null, metadata, null, null,
            null, null, accessConditions.modifiedAccessConditions(),
            accessConditions.leaseAccessConditions(), Context.NONE));
    }

    /**
     * Sets the tier on a blob. The operation is allowed on a page blob in a premium storage account or a block blob in
     * a blob storage or GPV2 account. A premium page blob's tier determines the allowed size, IOPS, and bandwidth of
     * the blob. A block blob's tier determines the Hot/Cool/Archive storage type. This does not update the blob's etag.
     * <p>
     * For detailed information about block blob level tiering see the <a href="https://docs.microsoft.com/en-us/azure/storage/blobs/storage-blob-storage-tiers.">Azure Docs</a>.
     *
     * @param tier
     *         The new tier for the blob.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=tier "Sample code for BlobAsyncRawClient.setTier")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<BlobsSetTierResponse> setTier(AccessTier tier) {
        return this.setTier(tier, null);
    }

    /**
     * Sets the tier on a blob. The operation is allowed on a page blob in a premium storage account or a block blob in
     * a blob storage or GPV2 account. A premium page blob's tier determines the allowed size, IOPS, and bandwidth of
     * the blob. A block blob's tier determines the Hot/Cool/Archive storage type. This does not update the blob's etag.
     * <p>
     * For detailed information about block blob level tiering see the <a href="https://docs.microsoft.com/en-us/azure/storage/blobs/storage-blob-storage-tiers.">Azure Docs</a>.
     *
     * @param tier
     *         The new tier for the blob.
     * @param leaseAccessConditions
     *         By setting lease access conditions, requests will fail if the provided lease does not match the active
     *         lease on the blob.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=tier "Sample code for BlobAsyncRawClient.setTier")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<BlobsSetTierResponse> setTier(AccessTier tier, LeaseAccessConditions leaseAccessConditions) {
        Utility.assertNotNull("tier", tier);

        return postProcessResponse(this.azureBlobStorage.blobs().setTierWithRestResponseAsync(
            null, null, tier, null, null, leaseAccessConditions, Context.NONE));
    }

    /**
     * Undelete restores the content and metadata of a soft-deleted blob and/or any associated soft-deleted snapshots.
     * For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/undelete-blob">Azure Docs</a>.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=undelete "Sample code for BlobAsyncRawClient.undelete")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<BlobsUndeleteResponse> undelete() {
        return postProcessResponse(this.azureBlobStorage.blobs().undeleteWithRestResponseAsync(null,
            null, Context.NONE));
    }

    /**
     * Acquires a lease on the blob for write and delete operations. The lease duration must be between 15 to 60
     * seconds, or infinite (-1). For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/lease-blob">Azure Docs</a>.
     *
     * @param proposedId
     *      A {@code String} in any valid GUID format. May be null.
     * @param duration
     *         The  duration of the lease, in seconds, or negative one (-1) for a lease that
     *         never expires. A non-infinite lease can be between 15 and 60 seconds.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=blob_lease "Sample code for BlobAsyncRawClient.acquireLease")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<BlobsAcquireLeaseResponse> acquireLease(String proposedId, int duration) {
        return this.acquireLease(proposedId, duration, null);
    }

    /**
     * Acquires a lease on the blob for write and delete operations. The lease duration must be between 15 to 60
     * seconds, or infinite (-1). For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/lease-blob">Azure Docs</a>.
     *
     * @param proposedID
     *         A {@code String} in any valid GUID format. May be null.
     * @param duration
     *         The  duration of the lease, in seconds, or negative one (-1) for a lease that
     *         never expires. A non-infinite lease can be between 15 and 60 seconds.
     * @param modifiedAccessConditions
     *         Standard HTTP Access conditions related to the modification of data. ETag and LastModifiedTime are used
     *         to construct conditions related to when the blob was changed relative to the given request. The request
     *         will fail if the specified condition is not satisfied.
     *
     * @return Emits the successful response.
     * @throws IllegalArgumentException If {@code duration} is outside the bounds of 15 to 60 or isn't -1.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=blob_lease "Sample code for BlobAsyncRawClient.acquireLease")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<BlobsAcquireLeaseResponse> acquireLease(String proposedID, int duration,
                                                         ModifiedAccessConditions modifiedAccessConditions) {
        if (!(duration == -1 || (duration >= 15 && duration <= 60))) {
            // Throwing is preferred to Mono.error because this will error out immediately instead of waiting until
            // subscription.
            throw new IllegalArgumentException("Duration must be -1 or between 15 and 60.");
        }

        return postProcessResponse(this.azureBlobStorage.blobs().acquireLeaseWithRestResponseAsync(
            null, null, null, duration, proposedID, null,
            modifiedAccessConditions, Context.NONE));
    }

    /**
     * Renews the blob's previously-acquired lease. For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/lease-blob">Azure Docs</a>.
     *
     * @param leaseID
     *         The leaseId of the active lease on the blob.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=blob_lease "Sample code for BlobAsyncRawClient.renewLease")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<BlobsRenewLeaseResponse> renewLease(String leaseID) {
        return this.renewLease(leaseID, null);
    }

    /**
     * Renews the blob's previously-acquired lease. For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/lease-blob">Azure Docs</a>.
     *
     * @param leaseID
     *         The leaseId of the active lease on the blob.
     * @param modifiedAccessConditions
     *         Standard HTTP Access conditions related to the modification of data. ETag and LastModifiedTime are used
     *         to construct conditions related to when the blob was changed relative to the given request. The request
     *         will fail if the specified condition is not satisfied.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=blob_lease "Sample code for BlobAsyncRawClient.renewLease")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<BlobsRenewLeaseResponse> renewLease(String leaseID, ModifiedAccessConditions modifiedAccessConditions) {
        return postProcessResponse(this.azureBlobStorage.blobs().renewLeaseWithRestResponseAsync(null,
            null, leaseID, null, null, modifiedAccessConditions, Context.NONE));
    }

    /**
     * Releases the blob's previously-acquired lease. For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/lease-blob">Azure Docs</a>.
     *
     * @param leaseID
     *         The leaseId of the active lease on the blob.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=blob_lease "Sample code for BlobAsyncRawClient.releaseLease")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<BlobsReleaseLeaseResponse> releaseLease(String leaseID) {
        return this.releaseLease(leaseID, null);
    }

    /**
     * Releases the blob's previously-acquired lease. For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/lease-blob">Azure Docs</a>.
     *
     * @param leaseID
     *         The leaseId of the active lease on the blob.
     * @param modifiedAccessConditions
     *         Standard HTTP Access conditions related to the modification of data. ETag and LastModifiedTime are used
     *         to construct conditions related to when the blob was changed relative to the given request. The request
     *         will fail if the specified condition is not satisfied.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=blob_lease "Sample code for BlobAsyncRawClient.releaseLease")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<BlobsReleaseLeaseResponse> releaseLease(String leaseID,
                                                         ModifiedAccessConditions modifiedAccessConditions) {
        return postProcessResponse(this.azureBlobStorage.blobs().releaseLeaseWithRestResponseAsync(null,
            null, leaseID, null, null, modifiedAccessConditions, Context.NONE));
    }

    /**
     * BreakLease breaks the blob's previously-acquired lease (if it exists). Pass the LeaseBreakDefault (-1) constant
     * to break a fixed-duration lease when it expires or an infinite lease immediately. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/lease-blob">Azure Docs</a>.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=blob_lease "Sample code for BlobAsyncRawClient.breakLease")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
     *
     * @return
     *      Emits the successful response.
     */
    public Mono<BlobsBreakLeaseResponse> breakLease() {
        return this.breakLease(null, null);
    }

    /**
     * BreakLease breaks the blob's previously-acquired lease (if it exists). Pass the LeaseBreakDefault (-1) constant
     * to break a fixed-duration lease when it expires or an infinite lease immediately. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/lease-blob">Azure Docs</a>.
     *
     * @param breakPeriodInSeconds
     *         An optional {@code Integer} representing the proposed duration of seconds that the lease should continue
     *         before it is broken, between 0 and 60 seconds. This break period is only used if it is shorter than the
     *         time remaining on the lease. If longer, the time remaining on the lease is used. A new lease will not be
     *         available before the break period has expired, but the lease may be held for longer than the break
     *         period.
     * @param modifiedAccessConditions
     *         Standard HTTP Access conditions related to the modification of data. ETag and LastModifiedTime are used
     *         to construct conditions related to when the blob was changed relative to the given request. The request
     *         will fail if the specified condition is not satisfied.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=blob_lease "Sample code for BlobAsyncRawClient.breakLease")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<BlobsBreakLeaseResponse> breakLease(Integer breakPeriodInSeconds,
                                                     ModifiedAccessConditions modifiedAccessConditions) {
        return postProcessResponse(this.azureBlobStorage.blobs().breakLeaseWithRestResponseAsync(null,
            null, null, breakPeriodInSeconds, null, modifiedAccessConditions, Context.NONE));
    }

    /**
     * ChangeLease changes the blob's lease ID. For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/lease-blob">Azure Docs</a>.
     *
     * @param leaseId
     *         The leaseId of the active lease on the blob.
     * @param proposedID
     *         A {@code String} in any valid GUID format.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=blob_lease "Sample code for BlobAsyncRawClient.changeLease")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<BlobsChangeLeaseResponse> changeLease(String leaseId, String proposedID) {
        return this.changeLease(leaseId, proposedID, null);
    }

    /**
     * ChangeLease changes the blob's lease ID. For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/lease-blob">Azure Docs</a>.
     *
     * @param leaseId
     *         The leaseId of the active lease on the blob.
     * @param proposedID
     *         A {@code String} in any valid GUID format.
     * @param modifiedAccessConditions
     *         Standard HTTP Access conditions related to the modification of data. ETag and LastModifiedTime are used
     *         to construct conditions related to when the blob was changed relative to the given request. The request
     *         will fail if the specified condition is not satisfied.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=blob_lease "Sample code for BlobAsyncRawClient.changeLease")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<BlobsChangeLeaseResponse> changeLease(String leaseId, String proposedID,
                                                       ModifiedAccessConditions modifiedAccessConditions) {
        return postProcessResponse(this.azureBlobStorage.blobs().changeLeaseWithRestResponseAsync(null,
            null, leaseId, proposedID, null, null, modifiedAccessConditions, Context.NONE));
    }

    /**
     * Returns the sku name and account kind for the account. For more information, please see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-account-information">Azure Docs</a>.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=account_info "Sample code for BlobAsyncRawClient.getAccountInfo")] \n
     * For more samples, please see the [Samples file](https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<BlobsGetAccountInfoResponse> getAccountInfo() {
        return postProcessResponse(
            this.azureBlobStorage.blobs().getAccountInfoWithRestResponseAsync(null, null, Context.NONE));
    }
}
