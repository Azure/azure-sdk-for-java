// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.implementation.http.UrlBuilder;
import com.azure.core.implementation.util.FluxUtil;
import com.azure.storage.blob.implementation.AzureBlobStorageBuilder;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.BlobHTTPHeaders;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobStartCopyFromURLHeaders;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.storage.blob.models.LeaseAccessConditions;
import com.azure.storage.blob.models.Metadata;
import com.azure.storage.blob.models.ModifiedAccessConditions;
import com.azure.storage.blob.models.ReliableDownloadOptions;
import com.azure.storage.blob.models.StorageAccountInfo;
import io.netty.buffer.ByteBuf;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

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
    private static final int BLOB_DEFAULT_DOWNLOAD_BLOCK_SIZE = 4 * Constants.MB;
    private static final int BLOB_MAX_DOWNLOAD_BLOCK_SIZE = 100 * Constants.MB;

    final BlobAsyncRawClient blobAsyncRawClient;

    /**
     * Package-private constructor for use by {@link BlobClientBuilder}.
     * @param azureBlobStorageBuilder the API client builder for blob storage API
     */
    BlobAsyncClient(AzureBlobStorageBuilder azureBlobStorageBuilder, String snapshot) {
        this.blobAsyncRawClient = new BlobAsyncRawClient(azureBlobStorageBuilder.build(), snapshot);
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
        return new BlockBlobAsyncClient(new AzureBlobStorageBuilder().url(getBlobUrl().toString()).pipeline(blobAsyncRawClient.azureBlobStorage.httpPipeline()), blobAsyncRawClient.snapshot);
    }

    /**
     * Creates a new {@link AppendBlobAsyncClient} to this resource, maintaining configurations. Only do this for blobs
     * that are known to be append blobs.
     *
     * @return
     *      A {@link AppendBlobAsyncClient} to this resource.
     */
    public AppendBlobAsyncClient asAppendBlobAsyncClient() {
        return new AppendBlobAsyncClient(new AzureBlobStorageBuilder().url(getBlobUrl().toString()).pipeline(blobAsyncRawClient.azureBlobStorage.httpPipeline()), blobAsyncRawClient.snapshot);
    }

    /**
     * Creates a new {@link PageBlobAsyncClient} to this resource, maintaining configurations. Only do this for blobs
     * that are known to be page blobs.
     *
     * @return
     *      A {@link PageBlobAsyncClient} to this resource.
     */
    public PageBlobAsyncClient asPageBlobAsyncClient() {
        return new PageBlobAsyncClient(new AzureBlobStorageBuilder().url(getBlobUrl().toString()).pipeline(blobAsyncRawClient.azureBlobStorage.httpPipeline()), blobAsyncRawClient.snapshot);
    }

    /**
     * Initializes a {@link ContainerAsyncClient} object pointing to the container this blob is in. This method does
     * not create a container. It simply constructs the URL to the container and offers access to methods relevant to
     * containers.
     *
     * @return
     *     A {@link ContainerAsyncClient} object pointing to the container containing the blob
     */
    public ContainerAsyncClient getContainerAsyncClient() {
        try {
            BlobURLParts parts = URLParser.parse(getBlobUrl());
            return new ContainerAsyncClient(new AzureBlobStorageBuilder()
                .url(String.format("%s://%s/%s", parts.scheme(), parts.host(), parts.containerName()))
                .pipeline(blobAsyncRawClient.azureBlobStorage.httpPipeline()));
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the URL of the blob represented by this client.
     * @return the URL.
     */
    public URL getBlobUrl() {
        try {
            UrlBuilder urlBuilder = UrlBuilder.parse(blobAsyncRawClient.azureBlobStorage.url());
            if (blobAsyncRawClient.snapshot != null) {
                urlBuilder.query("snapshot=" + blobAsyncRawClient.snapshot);
            }
            return urlBuilder.toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(String.format("Invalid URL on %s: %s" + getClass().getSimpleName(), blobAsyncRawClient.azureBlobStorage.url()), e);
        }
    }

    /**
     * Gets if the blob this client represents exists in the cloud.
     *
     * @return
     *         true if the blob exists, false if it doesn't
     */
    public Mono<Response<Boolean>> exists() {
        return this.getProperties()
            .map(cp -> (Response<Boolean>) new SimpleResponse<>(cp, true))
            .onErrorResume(t -> t instanceof StorageException && ((StorageException) t).statusCode() == 404, t -> {
                HttpResponse response = ((StorageException) t).response();
                return Mono.just(new SimpleResponse<>(response.request(), response.statusCode(), response.headers(), false));
            });
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
    public Mono<Response<String>> startCopyFromURL(URL sourceURL) {
        return this.startCopyFromURL(sourceURL, null, null, null);
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
     *
     * @return
     *      A reactive response containing the copy ID for the long running operation.
     */
    public Mono<Response<String>> startCopyFromURL(URL sourceURL, Metadata metadata,
            ModifiedAccessConditions sourceModifiedAccessConditions, BlobAccessConditions destAccessConditions) {
        return blobAsyncRawClient
            .startCopyFromURL(sourceURL, metadata, sourceModifiedAccessConditions, destAccessConditions)
            .map(rb -> new SimpleResponse<>(rb, rb.deserializedHeaders().copyId()));
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
    public Mono<VoidResponse> abortCopyFromURL(String copyId) {
        return this.abortCopyFromURL(copyId, null);
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
     *
     * @return
     *      A reactive response signalling completion.
     */
    public Mono<VoidResponse> abortCopyFromURL(String copyId, LeaseAccessConditions leaseAccessConditions) {
        return blobAsyncRawClient
            .abortCopyFromURL(copyId, leaseAccessConditions)
            .map(VoidResponse::new);
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
    public Mono<Response<String>> copyFromURL(URL copySource) {
        return this.copyFromURL(copySource, null, null, null);
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
     *
     * @return
     *      A reactive response containing the copy ID for the long running operation.
     */
    public Mono<Response<String>> copyFromURL(URL copySource, Metadata metadata,
                                    ModifiedAccessConditions sourceModifiedAccessConditions, BlobAccessConditions destAccessConditions) {
        return blobAsyncRawClient
            .syncCopyFromURL(copySource, metadata, sourceModifiedAccessConditions, destAccessConditions)
            .map(rb -> new SimpleResponse<>(rb, rb.deserializedHeaders().copyId()));
    }

    /**
     * Reads the entire blob. Uploading data must be done from the {@link BlockBlobClient}, {@link PageBlobClient}, or {@link AppendBlobClient}.
     *
     * @return
     *      A reactive response containing the blob data.
     */
    public Mono<Response<Flux<ByteBuffer>>> download() {
        return this.download(null, null, false, null);
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
     *
     * @return
     *      A reactive response containing the blob data.
     */
    public Mono<Response<Flux<ByteBuffer>>> download(BlobRange range, BlobAccessConditions accessConditions,
            boolean rangeGetContentMD5, ReliableDownloadOptions options) {
        return blobAsyncRawClient
            .download(range, accessConditions, rangeGetContentMD5)
            .map(response -> new SimpleResponse<>(
                response.rawResponse(),
                response.body(options).map(ByteBuf::nioBuffer).switchIfEmpty(Flux.just(ByteBuffer.allocate(0)))));
    }

    /**
     * Downloads the entire blob into a file specified by the path. The file will be created if it doesn't exist.
     * Uploading data must be done from the {@link BlockBlobClient}, {@link PageBlobClient}, or {@link AppendBlobClient}.
     * <p>
     * This method makes an extra HTTP call to get the length of the blob in the beginning. To avoid this extra call,
     * use the other overload providing the {@link BlobRange} parameter.
     *
     * @param filePath
     *          A non-null {@link OutputStream} instance where the downloaded data will be written.
     */
    public Mono<Void> downloadToFile(String filePath) {
        return this.downloadToFile(filePath, null, BLOB_DEFAULT_DOWNLOAD_BLOCK_SIZE, null, false, null);
    }

    /**
     * Downloads a range of bytes  blob into a file specified by the path. The file will be created if it doesn't exist.
     * Uploading data must be done from the {@link BlockBlobClient}, {@link PageBlobClient}, or {@link AppendBlobClient}.
     * <p>
     * This method makes an extra HTTP call to get the length of the blob in the beginning. To avoid this extra call,
     * provide the {@link BlobRange} parameter.
     *
     * @param filePath
     *          A non-null {@link OutputStream} instance where the downloaded data will be written.
     * @param range
     *         {@link BlobRange}
     * @param blockSize
     *         the size of a chunk to download at a time, in bytes
     * @param accessConditions
     *         {@link BlobAccessConditions}
     * @param rangeGetContentMD5
     *         Whether the contentMD5 for the specified blob range should be returned.
     */
    public Mono<Void> downloadToFile(String filePath, BlobRange range, Integer blockSize, BlobAccessConditions accessConditions,
                                     boolean rangeGetContentMD5, ReliableDownloadOptions options) {
        if (blockSize < 0 || blockSize > BLOB_MAX_DOWNLOAD_BLOCK_SIZE) {
            throw new IllegalArgumentException("Block size should not exceed 100MB");
        }
        return Mono.using(() -> {
                try {
                    return AsynchronousFileChannel.open(Paths.get(filePath), StandardOpenOption.READ, StandardOpenOption.WRITE);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            },
            channel -> Mono.justOrEmpty(range)
                .switchIfEmpty(getFullBlobRange(accessConditions))
                .flatMapMany(rg -> Flux.fromIterable(sliceBlobRange(rg, blockSize)))
                .flatMap(chunk -> blobAsyncRawClient
                    .download(chunk, accessConditions, rangeGetContentMD5)
                    .subscribeOn(Schedulers.elastic())
                    .flatMap(dar -> FluxUtil.bytebufStreamToFile(dar.body(options), channel, chunk.offset() - (range == null ? 0 : range.offset()))))
                .then(),
            channel -> {
                try {
                    channel.close();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
    }

    private Mono<BlobRange> getFullBlobRange(BlobAccessConditions accessConditions) {
        return getProperties(accessConditions).map(rb -> new BlobRange(0, rb.value().blobSize()));
    }

    private List<BlobRange> sliceBlobRange(BlobRange blobRange, Integer blockSize) {
        if (blockSize == null) {
            blockSize = BLOB_DEFAULT_DOWNLOAD_BLOCK_SIZE;
        }
        long offset = blobRange.offset();
        long length = blobRange.count();
        List<BlobRange> chunks = new ArrayList<>();
        for (long pos = offset; pos < offset + length; pos += blockSize) {
            long count = blockSize;
            if (pos + count > offset + length) {
                count = offset + length - pos;
            }
            chunks.add(new BlobRange(pos, count));
        }
        return chunks;
    }

    /**
     * Deletes the specified blob or snapshot. Note that deleting a blob also deletes all its snapshots.
     *
     * @return
     *      A reactive response signalling completion.
     */
    public Mono<VoidResponse> delete() {
        return this.delete(null, null);
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
     *
     * @return
     *      A reactive response signalling completion.
     */
    public Mono<VoidResponse> delete(DeleteSnapshotsOptionType deleteBlobSnapshotOptions,
            BlobAccessConditions accessConditions) {
        return blobAsyncRawClient
            .delete(deleteBlobSnapshotOptions, accessConditions)
            .map(VoidResponse::new);
    }

    /**
     * Returns the blob's metadata and properties.
     *
     * @return
     *      A reactive response containing the blob properties and metadata.
     */
    public Mono<Response<BlobProperties>> getProperties() {
        return this.getProperties(null);
    }

    /**
     * Returns the blob's metadata and properties.
     *
     * @param accessConditions
     *         {@link BlobAccessConditions}
     *
     * @return
     *      A reactive response containing the blob properties and metadata.
     */
    public Mono<Response<BlobProperties>> getProperties(BlobAccessConditions accessConditions) {
        return blobAsyncRawClient
            .getProperties(accessConditions)
            .map(rb -> new SimpleResponse<>(rb, new BlobProperties(rb.deserializedHeaders())));
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
    public Mono<VoidResponse> setHTTPHeaders(BlobHTTPHeaders headers) {
        return this.setHTTPHeaders(headers, null);
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
     *
     * @return
     *      A reactive response signalling completion.
     */
    public Mono<VoidResponse> setHTTPHeaders(BlobHTTPHeaders headers, BlobAccessConditions accessConditions) {
        return blobAsyncRawClient
            .setHTTPHeaders(headers, accessConditions)
            .map(VoidResponse::new);
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
    public Mono<VoidResponse> setMetadata(Metadata metadata) {
        return this.setMetadata(metadata, null);
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
     *
     * @return
     *      A reactive response signalling completion.
     */
    public Mono<VoidResponse> setMetadata(Metadata metadata, BlobAccessConditions accessConditions) {
        return blobAsyncRawClient
            .setMetadata(metadata, accessConditions)
            .map(VoidResponse::new);
    }

    /**
     * Creates a read-only snapshot of a blob.
     *
     * @return
     *      A reactive response containing the ID of the new snapshot.
     */
    public Mono<Response<String>> createSnapshot() {
        return this.createSnapshot(null, null);
    }

    /**
     * Creates a read-only snapshot of a blob.
     *
     * @param metadata
     *         {@link Metadata}
     * @param accessConditions
     *         {@link BlobAccessConditions}
     *
     * @return
     *      A reactive response containing the ID of the new snapshot.
     */
    public Mono<Response<String>> createSnapshot(Metadata metadata, BlobAccessConditions accessConditions) {
        return blobAsyncRawClient
            .createSnapshot(metadata, accessConditions)
            .map(rb -> new SimpleResponse<>(rb, rb.deserializedHeaders().snapshot()));
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
    public Mono<VoidResponse> setTier(AccessTier tier) {
        return this.setTier(tier, null);
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
     *
     * @return
     *      A reactive response signalling completion.
     */
    public Mono<VoidResponse> setTier(AccessTier tier, LeaseAccessConditions leaseAccessConditions) {
        return blobAsyncRawClient
            .setTier(tier, leaseAccessConditions)
            .map(VoidResponse::new);
    }

    /**
     * Undelete restores the content and metadata of a soft-deleted blob and/or any associated soft-deleted snapshots.
     *
     * @return
     *      A reactive response signalling completion.
     */
    public Mono<VoidResponse> undelete() {
        return blobAsyncRawClient
            .undelete()
            .map(VoidResponse::new);
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
    public Mono<Response<String>> acquireLease(String proposedId, int duration) {
        return this.acquireLease(proposedId, duration, null);
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
     *
     * @return
     *      A reactive response containing the lease ID.
     */
    public Mono<Response<String>> acquireLease(String proposedID, int duration, ModifiedAccessConditions modifiedAccessConditions) {
        return blobAsyncRawClient
            .acquireLease(proposedID, duration, modifiedAccessConditions)
            .map(rb -> new SimpleResponse<>(rb, rb.deserializedHeaders().leaseId()));
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
    public Mono<Response<String>> renewLease(String leaseID) {
        return this.renewLease(leaseID, null);
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
     *
     * @return
     *      A reactive response containing the renewed lease ID.
     */
    public Mono<Response<String>> renewLease(String leaseID, ModifiedAccessConditions modifiedAccessConditions) {
        return blobAsyncRawClient
            .renewLease(leaseID, modifiedAccessConditions)
            .map(rb -> new SimpleResponse<>(rb, rb.deserializedHeaders().leaseId()));
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
    public Mono<VoidResponse> releaseLease(String leaseID) {
        return this.releaseLease(leaseID, null);
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
     *
     * @return
     *      A reactive response signalling completion.
     */
    public Mono<VoidResponse> releaseLease(String leaseID, ModifiedAccessConditions modifiedAccessConditions) {
        return blobAsyncRawClient
            .releaseLease(leaseID, modifiedAccessConditions)
            .map(VoidResponse::new);
    }

    /**
     * BreakLease breaks the blob's previously-acquired lease (if it exists). Pass the LeaseBreakDefault (-1) constant
     * to break a fixed-duration lease when it expires or an infinite lease immediately.
     *
     * @return
     *      A reactive response containing the remaining time in the broken lease in seconds.
     */
    public Mono<Response<Integer>> breakLease() {
        return this.breakLease(null, null);
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
     *
     * @return
     *      A reactive response containing the remaining time in the broken lease in seconds.
     */
    public Mono<Response<Integer>> breakLease(Integer breakPeriodInSeconds, ModifiedAccessConditions modifiedAccessConditions) {
        return blobAsyncRawClient
            .breakLease(breakPeriodInSeconds, modifiedAccessConditions)
            .map(rb -> new SimpleResponse<>(rb, rb.deserializedHeaders().leaseTime()));
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
    public Mono<Response<String>> changeLease(String leaseId, String proposedID) {
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
     * @return A reactive response containing the new lease ID.
     */
    public Mono<Response<String>> changeLease(String leaseId, String proposedID, ModifiedAccessConditions modifiedAccessConditions) {
        return blobAsyncRawClient
            .changeLease(leaseId, proposedID, modifiedAccessConditions)
            .map(rb -> new SimpleResponse<>(rb, rb.deserializedHeaders().leaseId()));
    }

    /**
     * Returns the sku name and account kind for the account. For more information, please see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-account-information">Azure Docs</a>.
     *
     * @return a reactor response containing the sku name and account kind.
     */
    // TODO determine this return type
    public Mono<Response<StorageAccountInfo>> getAccountInfo() {
        return blobAsyncRawClient
            .getAccountInfo()
            .map(rb -> new SimpleResponse<>(rb, new StorageAccountInfo(rb.deserializedHeaders())));
    }
}
