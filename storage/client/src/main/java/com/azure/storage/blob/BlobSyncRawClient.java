// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpPipeline;
import com.azure.core.util.Context;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.models.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import static com.azure.storage.blob.Utility.postProcessResponse;

/**
 * Represents a URL to a blob of any type: block, append, or page. It may be obtained by direct construction or via the
 * create method on a {@link ContainerAsyncClient} object. This class does not hold any state about a particular blob but is
 * instead a convenient way of sending off appropriate requests to the resource on the service. Please refer to the
 * <a href=https://docs.microsoft.com/en-us/rest/api/storageservices/understanding-block-blobs--append-blobs--and-page-blobs>Azure Docs</a> for more information.
 */
public class BlobAsyncClient {

    BlobAsyncRawClient blobAsyncRawClient;

    /**
     * Creates a {@code BlobAsyncRawClient} object pointing to the account specified by the URL and using the provided pipeline to
     * make HTTP requests.
     *
     * @param url
     *         A {@code URL} to an Azure Storage blob.
     * @param pipeline
     *         A {@code HttpPipeline} which configures the behavior of HTTP exchanges. Please refer to
     *         {@link StorageURL#createPipeline(ICredentials, PipelineOptions)} for more information.
     */
    BlobAsyncClient(URL url, HttpPipeline pipeline) {
        super(url, pipeline);
    }

    /**
     * Converts this BlobAsyncRawClient to a {@link BlockBlobAsyncRawClient} object. Note that this does not change the actual type of the
     * blob if it has already been created.
     *
     * @return A {@link BlockBlobAsyncRawClient} object.
     */
    // TODO: figure out what to do with these methods
    // return raw clients or pretty clients?
    public BlockBlobAsyncRawClient toBlockBlobAsyncClient() {
        return blobAsyncRawClient.toBlockBlobAsyncClient();
    }

    /**
     * Converts this BlobAsyncRawClient to an {@link AppendBlobAsyncRawClient} object. Note that this does not change the actual type of the
     * blob if it has already been created.
     *
     * @return An {@link AppendBlobAsyncRawClient} object.
     */
    public AppendBlobAsyncRawClient toAppendBlobAsyncClient() {
        return blobAsyncRawClient.toAppendBlobAsyncClient();
    }

    /**
     * Converts this BlobAsyncRawClient to a {@link PageBlobAsyncRawClient} object. Note that this does not change the actual type of the blob
     * if it has already been created.
     *
     * @return A {@link PageBlobAsyncRawClient} object.
     */
    public PageBlobAsyncRawClient toPageBlobAsyncClient() {
        return blobAsyncRawClient.toPageBlobAsyncClient();
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
        return blobAsyncRawClient.startCopyFromURL(sourceURL, null, null, null, null);
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
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to
     *         its parent, forming a linked list.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=start_copy "Sample code for BlobAsyncRawClient.startCopyFromURL")] \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=start_copy_helper "Helper for start_copy sample.")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<BlobsStartCopyFromURLResponse> startCopyFromURL(URL sourceURL, Metadata metadata,
                                                                 ModifiedAccessConditions sourceModifiedAccessConditions, BlobAccessConditions destAccessConditions,
                                                                 Context context) {
        return blobAsyncRawClient.startCopyFromURL(sourceURL, metadata, sourceModifiedAccessConditions, destAccessConditions, context);
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
    public Mono<Void> abortCopyFromURL(String copyId) {
        return blobAsyncRawClient.abortCopyFromURL(copyId, null, null);
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
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to
     *         its parent, forming a linked list.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=abort_copy "Sample code for BlobAsyncRawClient.abortCopyFromURL")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<Void> abortCopyFromURL(String copyId,
                                                                 LeaseAccessConditions leaseAccessConditions, Context context) {
        return blobAsyncRawClient.abortCopyFromURL(copyId, leaseAccessConditions, context);
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
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=sync_copy "Sample code for BlobAsyncRawClient.syncCopyFromURL")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<BlobsCopyFromURLResponse> syncCopyFromURL(URL copySource) {
        return blobAsyncRawClient.syncCopyFromURL(copySource, null, null, null, null);
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
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to
     *         its parent, forming a linked list.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=sync_copy "Sample code for BlobAsyncRawClient.syncCopyFromURL")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<BlobsCopyFromURLResponse> syncCopyFromURL(URL copySource, Metadata metadata,
                                                           ModifiedAccessConditions sourceModifiedAccessConditions, BlobAccessConditions destAccessConditions,
                                                           Context context) {
        return blobAsyncRawClient.syncCopyFromURL(copySource, metadata, sourceModifiedAccessConditions, destAccessConditions, context);
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
    public Flux<DownloadResponse> download() {
        return blobAsyncRawClient.download(null, null, false, null);
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
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to
     *         its parent, forming a linked list.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=upload_download "Sample code for BlobAsyncRawClient.download")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Flux<DownloadResponse> download(BlobRange range, BlobAccessConditions accessConditions,
                                             boolean rangeGetContentMD5, Context context) {
        return blobAsyncRawClient.download(range, accessConditions, rangeGetContentMD5, context);
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
    public Mono<Void> delete() {
        return blobAsyncRawClient.delete(null, null, null);
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
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to
     *         its parent, forming a linked list.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=blob_delete "Sample code for BlobAsyncRawClient.delete")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<Void> delete(DeleteSnapshotsOptionType deleteBlobSnapshotOptions,
                                             BlobAccessConditions accessConditions, Context context) {
        return blobAsyncRawClient.delete(deleteBlobSnapshotOptions, accessConditions, context);
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
        return blobAsyncRawClient.getProperties(null, null);
    }

    /**
     * Returns the blob's metadata and properties. For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob-properties">Azure Docs</a>.
     *
     * @param accessConditions
     *         {@link BlobAccessConditions}
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to
     *         its parent, forming a linked list.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=properties_metadata "Sample code for BlobAsyncRawClient.getProperties")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<BlobsGetPropertiesResponse> getProperties(BlobAccessConditions accessConditions, Context context) {
        return blobAsyncRawClient.getProperties(accessConditions, context);
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
    public Mono<Void> setHTTPHeaders(BlobHTTPHeaders headers) {
        return blobAsyncRawClient.setHTTPHeaders(headers, null, null);
    }

    /**
     * Changes a blob's HTTP header properties. For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-properties">Azure Docs</a>.
     *
     * @param headers
     *         {@link BlobHTTPHeaders}
     * @param accessConditions
     *         {@link BlobAccessConditions}
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to
     *         its parent, forming a linked list.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=properties_metadata "Sample code for BlobAsyncRawClient.setHTTPHeaders")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<Void> setHTTPHeaders(BlobHTTPHeaders headers,
                                                             BlobAccessConditions accessConditions, Context context) {
        return blobAsyncRawClient.setHTTPHeaders(headers, accessConditions, context);
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
    public Mono<Void> setMetadata(Metadata metadata) {
        return blobAsyncRawClient.setMetadata(metadata, null, null);
    }

    /**
     * Changes a blob's metadata. For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-metadata">Azure Docs</a>.
     *
     * @param metadata
     *         {@link Metadata}
     * @param accessConditions
     *         {@link BlobAccessConditions}
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to
     *         its parent, forming a linked list.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=properties_metadata "Sample code for BlobAsyncRawClient.setMetadata")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<Void> setMetadata(Metadata metadata, BlobAccessConditions accessConditions,
                                                       Context context) {
        return blobAsyncRawClient.setMetadata(metadata, accessConditions, context);
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
    public Mono<String> createSnapshot() {
        return blobAsyncRawClient.createSnapshot(null, null, null);
    }

    /**
     * Creates a read-only snapshot of a blob. For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/snapshot-blob">Azure Docs</a>.
     *
     * @param metadata
     *         {@link Metadata}
     * @param accessConditions
     *         {@link BlobAccessConditions}
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to
     *         its parent, forming a linked list.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=snapshot "Sample code for BlobAsyncRawClient.createSnapshot")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<String> createSnapshot(Metadata metadata, BlobAccessConditions accessConditions,
                                                             Context context) {
        return blobAsyncRawClient.createSnapshot(metadata, accessConditions, context);
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
    public Mono<Void> setTier(AccessTier tier) {
        return blobAsyncRawClient.setTier(tier, null, null);
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
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to
     *         its parent, forming a linked list.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=tier "Sample code for BlobAsyncRawClient.setTier")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<Void> setTier(AccessTier tier, LeaseAccessConditions leaseAccessConditions,
                                               Context context) {
        return blobAsyncRawClient.setTier(tier, leaseAccessConditions, context);
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
    public Mono<Void> undelete() {
        return this.undelete(null);
    }

    /**
     * Undelete restores the content and metadata of a soft-deleted blob and/or any associated soft-deleted snapshots.
     * For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/undelete-blob">Azure Docs</a>.
     *
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *         parent, forming a linked list.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=undelete "Sample code for BlobAsyncRawClient.undelete")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<Void> undelete(Context context) {
        return blobAsyncRawClient.undelete(context);
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
        return blobAsyncRawClient.acquireLease(proposedId, duration, null, null);
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
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to
     *         its parent, forming a linked list.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=blob_lease "Sample code for BlobAsyncRawClient.acquireLease")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<BlobsAcquireLeaseResponse> acquireLease(String proposedID, int duration,
                                                         ModifiedAccessConditions modifiedAccessConditions, Context context) {
        return blobAsyncRawClient.acquireLease(proposedID, duration, modifiedAccessConditions, context);
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
        return blobAsyncRawClient.renewLease(leaseID, null, null);
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
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to
     *         its parent, forming a linked list.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=blob_lease "Sample code for BlobAsyncRawClient.renewLease")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<BlobsRenewLeaseResponse> renewLease(String leaseID, ModifiedAccessConditions modifiedAccessConditions,
                                                     Context context) {
        return blobAsyncRawClient.renewLease(leaseID, modifiedAccessConditions, context);
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
        return blobAsyncRawClient.releaseLease(leaseID, null, null);
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
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to
     *         its parent, forming a linked list.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=blob_lease "Sample code for BlobAsyncRawClient.releaseLease")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<BlobsReleaseLeaseResponse> releaseLease(String leaseID,
                                                         ModifiedAccessConditions modifiedAccessConditions, Context context) {
        return blobAsyncRawClient.releaseLease(leaseID, modifiedAccessConditions, context);
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
        return blobAsyncRawClient.breakLease(null, null, null);
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
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to
     *         its parent, forming a linked list.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=blob_lease "Sample code for BlobAsyncRawClient.breakLease")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<BlobsBreakLeaseResponse> breakLease(Integer breakPeriodInSeconds,
                                                     ModifiedAccessConditions modifiedAccessConditions, Context context) {
        return blobAsyncRawClient.breakLease(breakPeriodInSeconds, modifiedAccessConditions, context);
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
        return blobAsyncRawClient.changeLease(leaseId, proposedID, null, null);
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
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to
     *         its parent, forming a linked list.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=blob_lease "Sample code for BlobAsyncRawClient.changeLease")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<BlobsChangeLeaseResponse> changeLease(String leaseId, String proposedID,
                                                       ModifiedAccessConditions modifiedAccessConditions, Context context) {
        return blobAsyncRawClient.changeLease(leaseId, proposedID, modifiedAccessConditions, context);
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
        return blobAsyncRawClient.getAccountInfo(null);
    }

    /**
     * Returns the sku name and account kind for the account. For more information, please see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-account-information">Azure Docs</a>.
     *
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to
     *         its parent, forming a linked list.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=account_info "Sample code for BlobAsyncRawClient.getAccountInfo")] \n
     * For more samples, please see the [Samples file](https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<BlobsGetAccountInfoResponse> getAccountInfo(Context context) {
        return blobAsyncRawClient.getAccountInfo(context);
    }
}
