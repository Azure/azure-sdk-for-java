// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.util.Context;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobHTTPHeaders;
import com.azure.storage.blob.models.BlobStartCopyFromURLHeaders;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.storage.blob.models.LeaseAccessConditions;
import com.azure.storage.blob.models.ModifiedAccessConditions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.time.OffsetDateTime;

/**
 * Client to a blob of any type: block, append, or page. It may only be instantiated through a {@link BlobClientBuilder} or via
 * the method {@link ContainerAsyncClient#getBlobAsyncClient(String)}. This class does not hold any state about a particular
 * blob, but is instead a convenient way of sending appropriate requests to the resource on the service.
 *
 * <p>
 * This client offers the ability to download blobs. Note that uploading data is specific to each type of blob. Please
 * refer to the {@link BlockBlobClient}, {@link PageBlobClient}, or {@link AppendBlobClient} for upload options. This
 * client can be converted into one of these clients easily through the methods {@link #asBlockBlobAsyncClient},
 * {@link #asPageBlobAsyncClient}, and {@link #asAppendBlobAsyncClient()}.
 *
 * <p>
 * This client contains operations on a blob. Operations on a container are available on {@link ContainerAsyncClient},
 * and operations on the service are available on {@link StorageAsyncClient}.
 *
 * <p>
 * Please refer to the <a href=https://docs.microsoft.com/en-us/rest/api/storageservices/understanding-block-blobs--append-blobs--and-page-blobs>Azure Docs</a>
 * for more information.
 *
 * <p>
 * Note this client is an async client that returns reactive responses from Spring Reactor Core
 * project (https://projectreactor.io/). Calling the methods in this client will <strong>NOT</strong>
 * start the actual network operation, until {@code .subscribe()} is called on the reactive response.
 * You can simply convert one of these responses to a {@link java.util.concurrent.CompletableFuture}
 * object through {@link Mono#toFuture()}.
 */
public class BlobAsyncClient {

    protected BlobAsyncRawClient blobAsyncRawClient;

    /**
     * Package-private constructor for use by {@link BlobClientBuilder}.
     * @param azureBlobStorage the API client for blob storage API
     */
    BlobAsyncClient(AzureBlobStorageImpl azureBlobStorage) {
        blobAsyncRawClient = new BlobAsyncRawClient(azureBlobStorage);
    }

    /**
     * Static method for getting a new builder for this class.
     *
     * @return
     *      A new {@link BlobClientBuilder} instance.
     */
    public static BlobClientBuilder blobClientBuilder() {
        return new BlobClientBuilder();
    }

    /**
     * Creates a new {@link BlockBlobAsyncClient} to this resource, maintaining configurations. Only do this for blobs
     * that are known to be block blobs.
     *
     * @return
     *      A {@link BlockBlobAsyncClient} to this resource.
     */
    public BlockBlobAsyncClient asBlockBlobAsyncClient() {
        return new BlockBlobAsyncClient(this.blobAsyncRawClient.azureBlobStorage);
    }

    /**
     * Creates a new {@link AppendBlobAsyncClient} to this resource, maintaining configurations. Only do this for blobs
     * that are known to be append blobs.
     *
     * @return
     *      A {@link AppendBlobAsyncClient} to this resource.
     */
    public AppendBlobAsyncClient asAppendBlobAsyncClient() {
        return new AppendBlobAsyncClient(this.blobAsyncRawClient.azureBlobStorage);
    }

    /**
     * Creates a new {@link PageBlobAsyncClient} to this resource, maintaining configurations. Only do this for blobs
     * that are known to be page blobs.
     *
     * @return
     *      A {@link PageBlobAsyncClient} to this resource.
     */
    public PageBlobAsyncClient asPageBlobAsyncClient() {
        return new PageBlobAsyncClient(this.blobAsyncRawClient.azureBlobStorage);
    }


    /**
     * Copies the data at the source URL to a blob. For more information, see the <a
     *      * href="https://docs.microsoft.com/rest/api/storageservices/copy-blob">Azure Docs</a>
     *
     * @param sourceURL
     *      The source URL to copy from. URLs outside of Azure may only be copied to block blobs.
     *
     * @return
     *      A reactive response containing the copy ID for the long running operation.
     */
    public Mono<String> startCopyFromURL(URL sourceURL) {
        return this.startCopyFromURL(sourceURL, null, null, null, null);
    }

    /**
     * Copies the data at the source URL to a blob. For more information, see the <a
     *      * href="https://docs.microsoft.com/rest/api/storageservices/copy-blob">Azure Docs</a>
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
     * @return
     *      A reactive response containing the copy ID for the long running operation.
     */
    public Mono<String> startCopyFromURL(URL sourceURL, Metadata metadata,
            ModifiedAccessConditions sourceModifiedAccessConditions, BlobAccessConditions destAccessConditions,
            Context context) {
        return blobAsyncRawClient
            .startCopyFromURL(sourceURL, metadata, sourceModifiedAccessConditions, destAccessConditions, context)
            .map(response -> response.deserializedHeaders().copyId());
    }

    /**
     * Stops a pending copy that was previously started and leaves a destination blob with 0 length and metadata.
     *
     * @param copyId
     *         The id of the copy operation to abort. Returned as the {@code copyId} field on the {@link
     *         BlobStartCopyFromURLHeaders} object.
     *
     * @return
     *      A reactive response signalling completion.
     */
    public Mono<Void> abortCopyFromURL(String copyId) {
        return this.abortCopyFromURL(copyId, null, null);
    }

    /**
     * Stops a pending copy that was previously started and leaves a destination blob with 0 length and metadata.
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
     * @return
     *      A reactive response signalling completion.
     */
    public Mono<Void> abortCopyFromURL(String copyId, LeaseAccessConditions leaseAccessConditions, Context context) {
        return blobAsyncRawClient
            .abortCopyFromURL(copyId, leaseAccessConditions, context)
            .then();
    }

    /**
     * Copies the data at the source URL to a blob and waits for the copy to complete before returning a response.
     *
     * @param copySource
     *         The source URL to copy from.
     *
     * @return
     *      A reactive response containing the copy ID for the long running operation.
     */
    public Mono<String> copyFromURL(URL copySource) {
        return this.copyFromURL(copySource, null, null, null, null);
    }

    /**
     * Copies the data at the source URL to a blob and waits for the copy to complete before returning a response.
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
     * @return
     *      A reactive response containing the copy ID for the long running operation.
     */
    public Mono<String> copyFromURL(URL copySource, Metadata metadata,
                                    ModifiedAccessConditions sourceModifiedAccessConditions, BlobAccessConditions destAccessConditions,
                                    Context context) {
        return blobAsyncRawClient
            .syncCopyFromURL(copySource, metadata, sourceModifiedAccessConditions, destAccessConditions, context)
            .map(response -> response.deserializedHeaders().copyId());
    }

    /**
     * Reads the entire blob. Uploading data must be done from the {@link BlockBlobClient}, {@link PageBlobClient}, or {@link AppendBlobClient}.
     *
     * @return
     *      A reactive response containing the blob data.
     */
    public Flux<ByteBuffer> download() {
        return this.download(null, null, false, null, null);
    }

    /**
     * Reads a range of bytes from a blob. Uploading data must be done from the {@link BlockBlobClient}, {@link PageBlobClient}, or {@link AppendBlobClient}.
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
     * @return
     *      A reactive response containing the blob data.
     */
    public Flux<ByteBuffer> download(BlobRange range, BlobAccessConditions accessConditions,
            boolean rangeGetContentMD5, ReliableDownloadOptions options, Context context) {
        return blobAsyncRawClient
            .download(range, accessConditions, rangeGetContentMD5, context)
            .flatMapMany(response -> ByteBufFlux.fromInbound(response.body(options)).asByteBuffer());
    }

    /**
     * Downloads the entire blob into a file specified by the path. The file will be created if it doesn't exist.
     * Uploading data must be done from the {@link BlockBlobClient}, {@link PageBlobClient}, or {@link AppendBlobClient}.
     *
     * @param filePath
     *          A non-null {@link OutputStream} instance where the downloaded data will be written.
     */
    public Mono<Void> downloadToFile(String filePath) {
        return this.downloadToFile(filePath, null, null, false, null, null);
    }

    /**
     * Downloads a range of bytes  blob into a file specified by the path. The file will be created if it doesn't exist.
     * Uploading data must be done from the {@link BlockBlobClient}, {@link PageBlobClient}, or {@link AppendBlobClient}.
     *
     * @param filePath
     *          A non-null {@link OutputStream} instance where the downloaded data will be written.
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
     */
    public Mono<Void> downloadToFile(String filePath, BlobRange range, BlobAccessConditions accessConditions,
            boolean rangeGetContentMD5, ReliableDownloadOptions options, Context context) {
        //todo make this method smart
        return Mono.using(
            () -> new FileOutputStream(new File(filePath)),
            fstream -> this.download(range, accessConditions, rangeGetContentMD5, options, context)
                .doOnNext(byteBuffer -> {
                    try {
                        fstream.write(byteBuffer.array());
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                })
                .then(),
            fstream -> {
                try {
                    fstream.close();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        );
    }

    /**
     * Deletes the specified blob or snapshot. Note that deleting a blob also deletes all its snapshots.
     *
     * @return
     *      A reactive response signalling completion.
     */
    public Mono<Void> delete() {
        return this.delete(null, null, null);
    }

    /**
     * Deletes the specified blob or snapshot. Note that deleting a blob also deletes all its snapshots.
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
     * @return
     *      A reactive response signalling completion.
     */
    public Mono<Void> delete(DeleteSnapshotsOptionType deleteBlobSnapshotOptions,
            BlobAccessConditions accessConditions, Context context) {
        return blobAsyncRawClient
            .delete(deleteBlobSnapshotOptions, accessConditions, context)
            .then();
    }

    /**
     * Returns the blob's metadata and properties.
     *
     * @return
     *      A reactive response containing the blob properties and metadata.
     */
    public Mono<BlobProperties> getProperties() {
        return this.getProperties(null, null);
    }

    /**
     * Returns the blob's metadata and properties.
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
     * @return
     *      A reactive response containing the blob properties and metadata.
     */
    public Mono<BlobProperties> getProperties(BlobAccessConditions accessConditions, Context context) {
        return blobAsyncRawClient
            .getProperties(accessConditions, context)
            .map(ResponseBase::deserializedHeaders)
            .map(BlobProperties::new);
    }

    /**
     * Changes a blob's HTTP header properties. if only one HTTP header is updated, the
     * others will all be erased. In order to preserve existing values, they must be
     * passed alongside the header being changed. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-properties">Azure Docs</a>.
     *
     * @param headers
     *         {@link BlobHTTPHeaders}
     *
     * @return
     *      A reactive response signalling completion.
     */
    public Mono<Void> setHTTPHeaders(BlobHTTPHeaders headers) {
        return this.setHTTPHeaders(headers, null, null);
    }

    /**
     * Changes a blob's HTTP header properties. if only one HTTP header is updated, the
     * others will all be erased. In order to preserve existing values, they must be
     * passed alongside the header being changed. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-properties">Azure Docs</a>.
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
     * @return
     *      A reactive response signalling completion.
     */
    public Mono<Void> setHTTPHeaders(BlobHTTPHeaders headers, BlobAccessConditions accessConditions, Context context) {
        return blobAsyncRawClient
            .setHTTPHeaders(headers, accessConditions, context)
            .then();
    }

    /**
     * Changes a blob's metadata. The specified metadata in this method will replace existing
     * metadata. If old values must be preserved, they must be downloaded and included in the
     * call to this method. For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-metadata">Azure Docs</a>.
     *
     * @param metadata
     *         {@link Metadata}
     *
     * @return
     *      A reactive response signalling completion.
     */
    public Mono<Void> setMetadata(Metadata metadata) {
        return this.setMetadata(metadata, null, null);
    }

    /**
     * Changes a blob's metadata. The specified metadata in this method will replace existing
     * metadata. If old values must be preserved, they must be downloaded and included in the
     * call to this method. For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-metadata">Azure Docs</a>.
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
     * @return
     *      A reactive response signalling completion.
     */
    public Mono<Void> setMetadata(Metadata metadata, BlobAccessConditions accessConditions, Context context) {
        return blobAsyncRawClient
            .setMetadata(metadata, accessConditions, context)
            .then();
    }

    /**
     * Creates a read-only snapshot of a blob.
     *
     * @return
     *      A reactive response containing the ID of the new snapshot.
     */
    public Mono<String> createSnapshot() {
        return this.createSnapshot(null, null, null);
    }

    /**
     * Creates a read-only snapshot of a blob.
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
     * @return
     *      A reactive response containing the ID of the new snapshot.
     */
    public Mono<String> createSnapshot(Metadata metadata, BlobAccessConditions accessConditions, Context context) {
        return blobAsyncRawClient
            .createSnapshot(metadata, accessConditions, context)
            .map(response -> response.deserializedHeaders().snapshot());
    }

    /**
     * Sets the tier on a blob. The operation is allowed on a page blob in a premium storage account or a block blob in
     * a blob storage or GPV2 account. A premium page blob's tier determines the allowed size, IOPS, and bandwidth of
     * the blob. A block blob's tier determines the Hot/Cool/Archive storage type. This does not update the blob's etag.
     *
     * @param tier
     *         The new tier for the blob.
     *
     * @return
     *      A reactive response signalling completion.
     */
    public Mono<Void> setTier(AccessTier tier) {
        return this.setTier(tier, null, null);
    }

    /**
     * Sets the tier on a blob. The operation is allowed on a page blob in a premium storage account or a block blob in
     * a blob storage or GPV2 account. A premium page blob's tier determines the allowed size, IOPS, and bandwidth of
     * the blob. A block blob's tier determines the Hot/Cool/Archive storage type. This does not update the blob's etag.
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
     * @return
     *      A reactive response signalling completion.
     */
    public Mono<Void> setTier(AccessTier tier, LeaseAccessConditions leaseAccessConditions, Context context) {
        return blobAsyncRawClient
            .setTier(tier, leaseAccessConditions, context)
            .then();
    }

    /**
     * Undelete restores the content and metadata of a soft-deleted blob and/or any associated soft-deleted snapshots.
     *
     * @return
     *      A reactive response signalling completion.
     */
    public Mono<Void> undelete() {
        return this.undelete(null);
    }

    /**
     * Undelete restores the content and metadata of a soft-deleted blob and/or any associated soft-deleted snapshots.
     *
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *         parent, forming a linked list.
     *
     * @return
     *      A reactive response signalling completion.
     */
    public Mono<Void> undelete(Context context) {
        return blobAsyncRawClient
            .undelete(context)
            .then();
    }

    /**
     * Acquires a lease on the blob for write and delete operations. The lease duration must be between 15 to 60
     * seconds, or infinite (-1).
     *
     * @param proposedId
     *      A {@code String} in any valid GUID format. May be null.
     * @param duration
     *         The  duration of the lease, in seconds, or negative one (-1) for a lease that
     *         never expires. A non-infinite lease can be between 15 and 60 seconds.
     *
     * @return
     *      A reactive response containing the lease ID.
     */
    public Mono<String> acquireLease(String proposedId, int duration) {
        return this.acquireLease(proposedId, duration, null, null);
    }

    /**
     * Acquires a lease on the blob for write and delete operations. The lease duration must be between 15 to 60
     * seconds, or infinite (-1).
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
     * @return
     *      A reactive response containing the lease ID.
     */
    public Mono<String> acquireLease(String proposedID, int duration, ModifiedAccessConditions modifiedAccessConditions,
            Context context) {
        return blobAsyncRawClient
            .acquireLease(proposedID, duration, modifiedAccessConditions, context)
            .map(response -> response.deserializedHeaders().leaseId());
    }

    /**
     * Renews the blob's previously-acquired lease.
     *
     * @param leaseID
     *         The leaseId of the active lease on the blob.
     *
     * @return
     *      A reactive response containing the renewed lease ID.
     */
    public Mono<String> renewLease(String leaseID) {
        return this.renewLease(leaseID, null, null);
    }

    /**
     * Renews the blob's previously-acquired lease.
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
     * @return
     *      A reactive response containing the renewed lease ID.
     */
    public Mono<String> renewLease(String leaseID, ModifiedAccessConditions modifiedAccessConditions, Context context) {
        return blobAsyncRawClient
            .renewLease(leaseID, modifiedAccessConditions, context)
            .map(response -> response.deserializedHeaders().leaseId());
    }

    /**
     * Releases the blob's previously-acquired lease.
     *
     * @param leaseID
     *         The leaseId of the active lease on the blob.
     *
     * @return
     *      A reactive response signalling completion.
     */
    public Mono<Void> releaseLease(String leaseID) {
        return this.releaseLease(leaseID, null, null);
    }

    /**
     * Releases the blob's previously-acquired lease.
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
     * @return
     *      A reactive response signalling completion.
     */
    public Mono<Void> releaseLease(String leaseID, ModifiedAccessConditions modifiedAccessConditions, Context context) {
        return blobAsyncRawClient
            .releaseLease(leaseID, modifiedAccessConditions, context)
            .then();
    }

    /**
     * BreakLease breaks the blob's previously-acquired lease (if it exists). Pass the LeaseBreakDefault (-1) constant
     * to break a fixed-duration lease when it expires or an infinite lease immediately.
     *
     * @return
     *      A reactive response containing the remaining time in the broken lease in seconds.
     */
    public Mono<Integer> breakLease() {
        return this.breakLease(null, null, null);
    }

    /**
     * BreakLease breaks the blob's previously-acquired lease (if it exists). Pass the LeaseBreakDefault (-1) constant
     * to break a fixed-duration lease when it expires or an infinite lease immediately.
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
     * @return
     *      A reactive response containing the remaining time in the broken lease in seconds.
     */
    public Mono<Integer> breakLease(Integer breakPeriodInSeconds, ModifiedAccessConditions modifiedAccessConditions,
            Context context) {
        return blobAsyncRawClient
            .breakLease(breakPeriodInSeconds, modifiedAccessConditions, context)
            .map(response -> response.deserializedHeaders().leaseTime());
    }

    /**
     * ChangeLease changes the blob's lease ID.
     *
     * @param leaseId
     *         The leaseId of the active lease on the blob.
     * @param proposedID
     *         A {@code String} in any valid GUID format.
     *
     * @return
     *      A reactive response containing the new lease ID.
     */
    public Mono<String> changeLease(String leaseId, String proposedID) {
        return this.changeLease(leaseId, proposedID, null, null);
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
     * @return A reactive response containing the new lease ID.
     */
    public Mono<String> changeLease(String leaseId, String proposedID, ModifiedAccessConditions modifiedAccessConditions,
            Context context) {
        return blobAsyncRawClient
            .changeLease(leaseId, proposedID, modifiedAccessConditions, context)
            .map(response -> response.deserializedHeaders().leaseId());
    }

    /**
     * Returns the sku name and account kind for the account. For more information, please see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-account-information">Azure Docs</a>.
     *
     * @return a reactor response containing the sku name and account kind.
     */
    public Mono<StorageAccountInfo> getAccountInfo() {
        return this.getAccountInfo(null);
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
     * @return a reactor response containing the sku name and account kind.
     */
    // TODO determine this return type
    public Mono<StorageAccountInfo> getAccountInfo(Context context) {
        return blobAsyncRawClient
            .getAccountInfo(context)
            .map(ResponseBase::deserializedHeaders)
            .map(StorageAccountInfo::new);
    }


    /**
     * Generates a SAS token with the specified expiryTime and permissions
     */
    public String generateSAS(OffsetDateTime expiryTime, String permissions) {
        return generateSAS(null /* version */, null /* sasProtocol */, null /* startTime */,
            expiryTime, permissions, null /* ipRange */, null /* identifier */,
            null /* cacheControl */, null /* contentDisposition */,
            null /* contentEncoding */, null /* contentLanguage */, null /* contentType */);
    }

    /**
     * Generates a SAS token with the specified identifier
     */
    public String generateSAS (String identifier) {
        return generateSAS(null /* version */, null /* sasProtocol */, null /* startTime */,
            null /* expiryTime */, null /* permissions */, null /* ipRange */, identifier,
            null /* cacheControl */, null /* contentDisposition */,
            null /* contentEncoding */, null /* contentLanguage */, null /* contentType */);
    }

    /**
     * Generates a SAS token with the specified version, sasProtocol, startTime, expiryTime, permissions, ipRange, and identifier
     */
    public String generateSAS(String version, SASProtocol sasProtocol, OffsetDateTime startTime, OffsetDateTime expiryTime,
                              String permissions, IPRange ipRange, String identifier) {
        return generateSAS(version, sasProtocol, startTime, expiryTime, permissions, ipRange, identifier,
            null /* cacheControl */, null /* contentDisposition */,
            null /* contentEncoding */, null /* contentLanguage */, null /* contentType */);
    }

    /**
     * Generates a SAS token with the specified version, sasProtocol, startTime, expiryTime, permissions, ipRange, identifier,
     * cacheControl, contentDisposition, contentEncoding, contentLanguage and contentType
     */
    public String generateSAS(String version, SASProtocol sasProtocol, OffsetDateTime startTime, OffsetDateTime expiryTime,
                              String permissions, IPRange ipRange, String identifier, String cacheControl, String contentDisposition,
                              String contentEncoding, String contentLanguage, String contentType) {

        ServiceSASSignatureValues serviceSASSignatureValues = new ServiceSASSignatureValues();

        if(version!=null) {
            serviceSASSignatureValues.version(version);
        }

        serviceSASSignatureValues.protocol(sasProtocol);
        serviceSASSignatureValues.startTime(startTime);
        serviceSASSignatureValues.expiryTime(expiryTime);
        serviceSASSignatureValues.permissions(permissions);
        serviceSASSignatureValues.ipRange(ipRange);
        serviceSASSignatureValues.identifier(identifier);
        serviceSASSignatureValues.cacheControl(cacheControl);
        serviceSASSignatureValues.contentDisposition(contentDisposition);
        serviceSASSignatureValues.contentEncoding(contentEncoding);
        serviceSASSignatureValues.contentLanguage(contentLanguage);
        serviceSASSignatureValues.contentType(contentType);

        SharedKeyCredentials sharedKeyCredentials = getSharedKeyCredentials();

        ServiceSASSignatureValues values = configureServiceSASSignatureValues(serviceSASSignatureValues, sharedKeyCredentials);

        SASQueryParameters sasQueryParameters = values.generateSASQueryParameters(sharedKeyCredentials);

        return sasQueryParameters.encode();
    }

    /**
     * Sets serviceSASSignatureValues parameters dependent on the current blob type
     */
    protected ServiceSASSignatureValues configureServiceSASSignatureValues(ServiceSASSignatureValues serviceSASSignatureValues,
                                                                           SharedKeyCredentials sharedKeyCredentials) {

        String urlString = this.blobAsyncRawClient.azureBlobStorage.url();

        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        // Set snapshotId
        serviceSASSignatureValues.snapshotId(getSnapshotId());

        // Set resource
        if(isSnapshot()) {
            serviceSASSignatureValues.resource("bs");
        } else {
            serviceSASSignatureValues.resource("b");
        }

        // Validate permissions
        String permissions = serviceSASSignatureValues.permissions();
        serviceSASSignatureValues.permissions(BlobSASPermission.parse(permissions).toString());

        // Set canonicalName
        String accountName = sharedKeyCredentials.getAccountName();

        StringBuilder canonicalName = new StringBuilder("/blob");
        canonicalName.append('/').append(accountName).append('/').append(url.getPath());
        serviceSASSignatureValues.canonicalName(canonicalName.toString());

        return serviceSASSignatureValues;
    }

    /**
     * Gets the sharedKeyCredentials for a blob resource
     */

    protected SharedKeyCredentials getSharedKeyCredentials() {
        HttpPipeline httpPipeline = this.blobAsyncRawClient.azureBlobStorage.httpPipeline();
        int numPolicies = httpPipeline.getPolicyCount();
        for(int i = 0; i < numPolicies; i++){
            HttpPipelinePolicy httpPipelinePolicy = httpPipeline.getPolicy(i);
            if(httpPipelinePolicy instanceof SharedKeyCredentials){
                SharedKeyCredentials sharedKeyCredentials = (SharedKeyCredentials) httpPipelinePolicy;
                return sharedKeyCredentials;
            }
        }
        return null;
    }

    /**
     * Gets the snapshotId for a blob resource
     */
    protected String getSnapshotId() {
        String urlString = this.blobAsyncRawClient.azureBlobStorage.url();

        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        if(url!=null) {
            String query = url.getQuery();
            if(query!=null) {
                String[] parameters = query.split("&");
                for(String param: parameters) {
                    String key = param.split("=")[0];
                    String value = param.split("=")[1];
                    if(key.equalsIgnoreCase("snapshot")){
                        return value;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Determines if a blob is a snapshot
     */
    protected boolean isSnapshot() {
        String snapshotId = getSnapshotId();
        return snapshotId!=null;
    }
}
