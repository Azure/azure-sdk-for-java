// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
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
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.time.Duration;

/**
 * Client to a blob of any type: block, append, or page. It may only be instantiated through a {@link BlobClientBuilder} or via
 * the method {@link ContainerClient#getBlobClient(String)}. This class does not hold any state about a particular
 * blob, but is instead a convenient way of sending appropriate requests to the resource on the service.
 *
 * <p>
 * This client offers the ability to download blobs. Note that uploading data is specific to each type of blob. Please
 * refer to the {@link BlockBlobClient}, {@link PageBlobClient}, or {@link AppendBlobClient} for upload options. This
 * client can be converted into one of these clients easily through the methods {@link #asBlockBlobClient}, {@link #asPageBlobClient},
 * and {@link #asAppendBlobClient}.
 *
 * <p>
 * This client contains operations on a blob. Operations on a container are available on {@link ContainerClient},
 * and operations on the service are available on {@link StorageClient}.
 *
 * <p>
 * Please refer to the <a href=https://docs.microsoft.com/en-us/rest/api/storageservices/understanding-block-blobs--append-blobs--and-page-blobs>Azure Docs</a>
 * for more information.
 */
public class BlobClient {
    private static final int BLOB_DEFAULT_DOWNLOAD_BLOCK_SIZE = 4 * Constants.MB;
    private final BlobAsyncClient blobAsyncClient;

    /**
     * Package-private constructor for use by {@link BlobClientBuilder}.
     * @param blobAsyncClient the async blob client
     */
    BlobClient(BlobAsyncClient blobAsyncClient) {
        this.blobAsyncClient = blobAsyncClient;
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
     * Creates a new {@link BlockBlobClient} to this resource, maintaining configurations. Only do this for blobs
     * that are known to be block blobs.
     *
     * @return
     *      A {@link BlockBlobClient} to this resource.
     */
    public BlockBlobClient asBlockBlobClient() {
        return new BlockBlobClient(blobAsyncClient.asBlockBlobAsyncClient());
    }

    /**
     * Creates a new {@link AppendBlobClient} to this resource, maintaining configurations. Only do this for blobs
     * that are known to be append blobs.
     *
     * @return
     *      A {@link AppendBlobClient} to this resource.
     */
    public AppendBlobClient asAppendBlobClient() {
        return new AppendBlobClient(blobAsyncClient.asAppendBlobAsyncClient());
    }

    /**
     * Creates a new {@link PageBlobClient} to this resource, maintaining configurations. Only do this for blobs
     * that are known to be page blobs.
     *
     * @return
     *      A {@link PageBlobClient} to this resource.
     */
    public PageBlobClient asPageBlobClient() {
        return new PageBlobClient(blobAsyncClient.asPageBlobAsyncClient());
    }

    /**
     * Initializes a {@link ContainerClient} object pointing to the container this blob is in. This method does
     * not create a container. It simply constructs the URL to the container and offers access to methods relevant to
     * containers.
     *
     * @return
     *     A {@link ContainerClient} object pointing to the container containing the blob
     */
    public ContainerClient getContainerClient() {
        return new ContainerClient(blobAsyncClient.getContainerAsyncClient());
    }

    /**
     * Gets the URL of the blob represented by this client.
     * @return the URL.
     */
    public URL getBlobUrl() {
        return blobAsyncClient.getBlobUrl();
    }

    /**
     * Opens a blob input stream to download the blob.
     * <p>
     *
     * @return An <code>InputStream</code> object that represents the stream to use for reading from the blob.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public final BlobInputStream openInputStream() {
        return openInputStream(new BlobRange(0), null);
    }

    /**
     * Opens a blob input stream to download the specified range of the blob.
     * <p>
     *
     * @param range
     *         {@link BlobRange}
     * @param accessConditions
     *            An {@link BlobAccessConditions} object that represents the access conditions for the blob.
     *
     * @return An <code>InputStream</code> object that represents the stream to use for reading from the blob.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public final BlobInputStream openInputStream(BlobRange range, BlobAccessConditions accessConditions) {
        return new BlobInputStream(blobAsyncClient, range.offset(), range.count(), accessConditions);
    }

    /**
     * Gets if the container this client represents exists in the cloud.
     *
     * @return true if the container exists, false if it doesn't
     */
    public Response<Boolean> exists() {
        return this.exists(null);
    }

    /**
     * Gets if the container this client represents exists in the cloud.
     *
     * @param timeout
     *         An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return
     *         true if the container exists, false if it doesn't
     */
    public Response<Boolean> exists(Duration timeout) {
        Mono<Response<Boolean>> response = blobAsyncClient.exists();

        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Copies the data at the source URL to a blob. For more information, see the <a
     *      * href="https://docs.microsoft.com/rest/api/storageservices/copy-blob">Azure Docs</a>
     *
     * @param sourceURL
     *      The source URL to copy from. URLs outside of Azure may only be copied to block blobs.
     *
     * @return
     *      The copy ID for the long running operation.
     */
    public Response<String> startCopyFromURL(URL sourceURL) {
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
     * @param timeout
     *         An optional timeout value beyond which a {@link RuntimeException} will be raised.
     *
     * @return
     *      The copy ID for the long running operation.
     */
    public Response<String> startCopyFromURL(URL sourceURL, Metadata metadata,
            ModifiedAccessConditions sourceModifiedAccessConditions, BlobAccessConditions destAccessConditions,
            Duration timeout) {
        Mono<Response<String>> response = blobAsyncClient
            .startCopyFromURL(sourceURL, metadata, sourceModifiedAccessConditions, destAccessConditions);

        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Stops a pending copy that was previously started and leaves a destination blob with 0 length and metadata.
     *
     * @param copyId
     *         The id of the copy operation to abort. Returned as the {@code copyId} field on the {@link
     *         BlobStartCopyFromURLHeaders} object.
     * @return A response containing status code and HTTP headers.
     */
    public VoidResponse abortCopyFromURL(String copyId) {
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
     * @param timeout
     *         An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return A response containing status code and HTTP headers.
     */
    public VoidResponse abortCopyFromURL(String copyId, LeaseAccessConditions leaseAccessConditions, Duration timeout) {
        Mono<VoidResponse> response = blobAsyncClient
            .abortCopyFromURL(copyId, leaseAccessConditions);

        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Copies the data at the source URL to a blob and waits for the copy to complete before returning a response.
     *
     * @param copySource
     *         The source URL to copy from.
     *
     * @return
     *      The copy ID for the long running operation.
     */
    public Response<String> copyFromURL(URL copySource) {
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
     * @param timeout
     *         An optional timeout value beyond which a {@link RuntimeException} will be raised.
     *
     * @return
     *      The copy ID for the long running operation.
     */
    public Response<String> copyFromURL(URL copySource, Metadata metadata,
                              ModifiedAccessConditions sourceModifiedAccessConditions, BlobAccessConditions destAccessConditions,
                              Duration timeout) {
        Mono<Response<String>> response = blobAsyncClient
            .copyFromURL(copySource, metadata, sourceModifiedAccessConditions, destAccessConditions);

        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Downloads the entire blob into an output stream. Uploading data must be done from the {@link BlockBlobClient},
     * {@link PageBlobClient}, or {@link AppendBlobClient}.
     *
     * @param stream
     *          A non-null {@link OutputStream} instance where the downloaded data will be written.
     * @return A response containing status code and HTTP headers.
     * @throws UncheckedIOException If an I/O error occurs.
     */
    public VoidResponse download(OutputStream stream) {
        return this.download(stream, null, null, null, false, null);
    }

    /**
     * Downloads a range of bytes from a blob into an output stream. Uploading data must be done from the {@link BlockBlobClient},
     * {@link PageBlobClient}, or {@link AppendBlobClient}.
     *
     * @param stream
     *          A non-null {@link OutputStream} instance where the downloaded data will be written.
     * @param range
     *         {@link BlobRange}
     * @param accessConditions
     *         {@link BlobAccessConditions}
     * @param rangeGetContentMD5
     *         Whether the contentMD5 for the specified blob range should be returned.
     * @param timeout
     *         An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return A response containing status code and HTTP headers.
     * @throws UncheckedIOException If an I/O error occurs.
     *
     */
    public VoidResponse download(OutputStream stream, ReliableDownloadOptions options, BlobRange range,
                                 BlobAccessConditions accessConditions, boolean rangeGetContentMD5, Duration timeout) {
        Mono<VoidResponse> download = blobAsyncClient
            .download(range, accessConditions, rangeGetContentMD5, options)
            .flatMapMany(res -> res.value()
                .doOnNext(bf -> {
                    try {
                        stream.write(bf.array());
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }).map(bf -> res))
            .last()
            .map(VoidResponse::new);

        return Utility.blockWithOptionalTimeout(download, timeout);
    }

    /**
     * Downloads the entire blob into a file specified by the path. The file will be created if it doesn't exist.
     * Uploading data must be done from the {@link BlockBlobClient}, {@link PageBlobClient}, or {@link AppendBlobClient}.
     *
     * @param filePath
     *          A non-null {@link OutputStream} instance where the downloaded data will be written.
     */
    public void downloadToFile(String filePath) throws IOException {
        this.downloadToFile(filePath, null, null, BLOB_DEFAULT_DOWNLOAD_BLOCK_SIZE, null, false, null);
    }

    /**
     * Downloads a range of bytes  blob into a file specified by the path. The file will be created if it doesn't exist.
     * Uploading data must be done from the {@link BlockBlobClient}, {@link PageBlobClient}, or {@link AppendBlobClient}.
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
     * @param timeout
     *         An optional timeout value beyond which a {@link RuntimeException} will be raised.
     */
    public void downloadToFile(String filePath, ReliableDownloadOptions options, BlobRange range, Integer blockSize,
            BlobAccessConditions accessConditions, boolean rangeGetContentMD5, Duration timeout) throws IOException {
        Mono<Void> download = blobAsyncClient.downloadToFile(filePath, range, blockSize, accessConditions, rangeGetContentMD5, options);

        try {
            if (timeout == null) {
                download.block();
            } else {
                download.block(timeout);
            }
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    /**
     * Deletes the specified blob or snapshot. Note that deleting a blob also deletes all its snapshots.
     *
     * @return A response containing status code and HTTP headers.
     */
    public VoidResponse delete() {
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
     * @param timeout
     *         An optional timeout value beyond which a {@link RuntimeException} will be raised.
     *
     * @return A response containing status code and HTTP headers.
     */
    public VoidResponse delete(DeleteSnapshotsOptionType deleteBlobSnapshotOptions,
            BlobAccessConditions accessConditions, Duration timeout) {
        Mono<VoidResponse> response = blobAsyncClient
            .delete(deleteBlobSnapshotOptions, accessConditions);

        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Returns the blob's metadata and properties.
     *
     * @return
     *      The blob properties and metadata.
     */
    public Response<BlobProperties> getProperties() {
        return this.getProperties(null, null);
    }

    /**
     * Returns the blob's metadata and properties.
     *
     * @param accessConditions
     *         {@link BlobAccessConditions}
     * @param timeout
     *         An optional timeout value beyond which a {@link RuntimeException} will be raised.
     *
     * @return
     *      The blob properties and metadata.
     */
    public Response<BlobProperties> getProperties(BlobAccessConditions accessConditions, Duration timeout) {
        Mono<Response<BlobProperties>> response = blobAsyncClient
            .getProperties(accessConditions);

        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Changes a blob's HTTP header properties. if only one HTTP header is updated, the
     * others will all be erased. In order to preserve existing values, they must be
     * passed alongside the header being changed. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-properties">Azure Docs</a>.
     *
     * @param headers
     *         {@link BlobHTTPHeaders}
     * @return A response containing status code and HTTP headers.
     */
    public VoidResponse setHTTPHeaders(BlobHTTPHeaders headers) {
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
     * @param timeout
     *         An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return A response containing status code and HTTP headers.
     */
    public VoidResponse setHTTPHeaders(BlobHTTPHeaders headers, BlobAccessConditions accessConditions,
            Duration timeout) {
        Mono<VoidResponse> response = blobAsyncClient
            .setHTTPHeaders(headers, accessConditions);

        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Changes a blob's metadata. The specified metadata in this method will replace existing
     * metadata. If old values must be preserved, they must be downloaded and included in the
     * call to this method. For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-metadata">Azure Docs</a>.
     *
     * @param metadata
     *         {@link Metadata}
     * @return A response containing status code and HTTP headers.
     */
    public VoidResponse setMetadata(Metadata metadata) {
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
     * @param timeout
     *         An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return A response containing status code and HTTP headers.
     */
    public VoidResponse setMetadata(Metadata metadata, BlobAccessConditions accessConditions, Duration timeout) {
        Mono<VoidResponse> response = blobAsyncClient
            .setMetadata(metadata, accessConditions);

        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Creates a read-only snapshot of a blob.
     *
     * @return
     *      The ID of the new snapshot.
     */
    public Response<String> createSnapshot() {
        return this.createSnapshot(null, null, null);
    }

    /**
     * Creates a read-only snapshot of a blob.
     *
     * @param metadata
     *         {@link Metadata}
     * @param accessConditions
     *         {@link BlobAccessConditions}
     * @param timeout
     *         An optional timeout value beyond which a {@link RuntimeException} will be raised.
     *
     * @return
     *      The ID of the new snapshot.
     */
    public Response<String> createSnapshot(Metadata metadata, BlobAccessConditions accessConditions, Duration timeout) {
        Mono<Response<String>> response = blobAsyncClient
            .createSnapshot(metadata, accessConditions);

        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Sets the tier on a blob. The operation is allowed on a page blob in a premium storage account or a block blob in
     * a blob storage or GPV2 account. A premium page blob's tier determines the allowed size, IOPS, and bandwidth of
     * the blob. A block blob's tier determines the Hot/Cool/Archive storage type. This does not update the blob's etag.
     *
     * @param tier
     *         The new tier for the blob.
     * @return A response containing status code and HTTP headers.
     */
    public VoidResponse setTier(AccessTier tier) {
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
     * @param timeout
     *         An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return A response containing status code and HTTP headers.
     */
    public VoidResponse setTier(AccessTier tier, LeaseAccessConditions leaseAccessConditions, Duration timeout) {
        Mono<VoidResponse> response = blobAsyncClient
            .setTier(tier, leaseAccessConditions);

        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Undelete restores the content and metadata of a soft-deleted blob and/or any associated soft-deleted snapshots.
     *
     * @return A response containing status code and HTTP headers.
     */
    public VoidResponse undelete() {
        return this.undelete(null);
    }

    /**
     * Undelete restores the content and metadata of a soft-deleted blob and/or any associated soft-deleted snapshots.
     *
     * @param timeout
     *         An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return A response containing status code and HTTP headers.
     */
    public VoidResponse undelete(Duration timeout) {
        Mono<VoidResponse> response = blobAsyncClient
            .undelete();

        return Utility.blockWithOptionalTimeout(response, timeout);
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
     *      The lease ID.
     */
    public Response<String> acquireLease(String proposedId, int duration) {
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
     * @param timeout
     *         An optional timeout value beyond which a {@link RuntimeException} will be raised.
     *
     * @return
     *      The lease ID.
     */
    public Response<String> acquireLease(String proposedID, int duration,
            ModifiedAccessConditions modifiedAccessConditions, Duration timeout) {
        Mono<Response<String>> response = blobAsyncClient
            .acquireLease(proposedID, duration, modifiedAccessConditions);

        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Renews the blob's previously-acquired lease.
     *
     * @param leaseID
     *         The leaseId of the active lease on the blob.
     *
     * @return
     *      The renewed lease ID.
     */
    public Response<String> renewLease(String leaseID) {
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
     * @param timeout
     *         An optional timeout value beyond which a {@link RuntimeException} will be raised.
     *
     * @return
     *      The renewed lease ID.
     */
    public Response<String> renewLease(String leaseID, ModifiedAccessConditions modifiedAccessConditions,
            Duration timeout) {
        Mono<Response<String>> response = blobAsyncClient
            .renewLease(leaseID, modifiedAccessConditions);

        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Releases the blob's previously-acquired lease.
     *
     * @param leaseID
     *         The leaseId of the active lease on the blob.
     * @return A response containing status code and HTTP headers.
     */
    public VoidResponse releaseLease(String leaseID) {
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
     * @param timeout
     *         An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return A response containing status code and HTTP headers.
     */
    public VoidResponse releaseLease(String leaseID,
            ModifiedAccessConditions modifiedAccessConditions, Duration timeout) {
        Mono<VoidResponse> response = blobAsyncClient
            .releaseLease(leaseID, modifiedAccessConditions);

        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * BreakLease breaks the blob's previously-acquired lease (if it exists). Pass the LeaseBreakDefault (-1) constant
     * to break a fixed-duration lease when it expires or an infinite lease immediately.
     *
     * @return
     *      The remaining time in the broken lease in seconds.
     */
    public Response<Integer> breakLease() {
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
     * @param timeout
     *         An optional timeout value beyond which a {@link RuntimeException} will be raised.
     *
     * @return
     *      The remaining time in the broken lease in seconds.
     */
    public Response<Integer> breakLease(Integer breakPeriodInSeconds,
            ModifiedAccessConditions modifiedAccessConditions, Duration timeout) {
        Mono<Response<Integer>> response = blobAsyncClient
            .breakLease(breakPeriodInSeconds, modifiedAccessConditions);

        return Utility.blockWithOptionalTimeout(response, timeout);
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
     *      The new lease ID.
     */
    public Response<String> changeLease(String leaseId, String proposedID) {
        return this.changeLease(leaseId, proposedID, null, null);
    }

    /**
     * ChangeLease changes the blob's lease ID.  For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/lease-blob">Azure Docs</a>.
     *
     * @param leaseId
     *         The leaseId of the active lease on the blob.
     * @param proposedID
     *         A {@code String} in any valid GUID format.
     * @param modifiedAccessConditions
     *         Standard HTTP Access conditions related to the modification of data. ETag and LastModifiedTime are used
     *         to construct conditions related to when the blob was changed relative to the given request. The request
     *         will fail if the specified condition is not satisfied.
     * @param timeout
     *         An optional timeout value beyond which a {@link RuntimeException} will be raised.
     *
     * @return The new lease ID.
     */
    public Response<String> changeLease(String leaseId, String proposedID,
            ModifiedAccessConditions modifiedAccessConditions, Duration timeout) {
        Mono<Response<String>> response = blobAsyncClient
            .changeLease(leaseId, proposedID, modifiedAccessConditions);

        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Returns the sku name and account kind for the account. For more information, please see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-account-information">Azure Docs</a>.
     *
     * @return The sku name and account kind.
     */
    public Response<StorageAccountInfo> getAccountInfo() {
        return this.getAccountInfo(null);
    }

    /**
     * Returns the sku name and account kind for the account. For more information, please see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-account-information">Azure Docs</a>.
     *
     * @param timeout
     *         An optional timeout value beyond which a {@link RuntimeException} will be raised.
     *
     * @return The sku name and account kind.
     */
    public Response<StorageAccountInfo> getAccountInfo(Duration timeout) {
        Mono<Response<StorageAccountInfo>> response = blobAsyncClient
            .getAccountInfo();

        return Utility.blockWithOptionalTimeout(response, timeout);
    }
}
