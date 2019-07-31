// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.implementation.http.UrlBuilder;
import com.azure.core.implementation.util.FluxUtil;
import com.azure.core.util.Context;
import com.azure.storage.blob.implementation.AzureBlobStorageBuilder;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
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
import com.azure.storage.blob.models.SourceModifiedAccessConditions;
import com.azure.storage.blob.models.StorageAccountInfo;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.common.credentials.SharedKeyCredential;
import io.netty.buffer.ByteBuf;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.azure.storage.blob.Utility.postProcessResponse;

/**
 * Client to a blob of any type: block, append, or page. It may only be instantiated through a {@link BlobClientBuilder}
 * or via the method {@link ContainerAsyncClient#getBlobAsyncClient(String)}. This class does not hold any state about a
 * particular blob, but is instead a convenient way of sending appropriate requests to the resource on the service.
 *
 * <p>
 * This client offers the ability to download blobs. Note that uploading data is specific to each type of blob. Please
 * refer to the {@link BlockBlobClient}, {@link PageBlobClient}, or {@link AppendBlobClient} for upload options. This
 * client can be converted into one of these clients easily through the methods {@link #asBlockBlobAsyncClient}, {@link
 * #asPageBlobAsyncClient}, and {@link #asAppendBlobAsyncClient()}.
 *
 * <p>
 * This client contains operations on a blob. Operations on a container are available on {@link ContainerAsyncClient},
 * and operations on the service are available on {@link BlobServiceAsyncClient}.
 *
 * <p>
 * Please refer to the <a href=https://docs.microsoft.com/en-us/rest/api/storageservices/understanding-block-blobs--append-blobs--and-page-blobs>Azure
 * Docs</a> for more information.
 *
 * <p>
 * Note this client is an async client that returns reactive responses from Spring Reactor Core project
 * (https://projectreactor.io/). Calling the methods in this client will <strong>NOT</strong> start the actual network
 * operation, until {@code .subscribe()} is called on the reactive response. You can simply convert one of these
 * responses to a {@link java.util.concurrent.CompletableFuture} object through {@link Mono#toFuture()}.
 */
public class BlobAsyncClient {
    private static final int BLOB_DEFAULT_DOWNLOAD_BLOCK_SIZE = 4 * Constants.MB;
    private static final int BLOB_MAX_DOWNLOAD_BLOCK_SIZE = 100 * Constants.MB;

    final AzureBlobStorageImpl azureBlobStorage;
    protected final String snapshot;

    /**
     * Package-private constructor for use by {@link BlobClientBuilder}.
     *
     * @param azureBlobStorage the API client for blob storage
     */
    BlobAsyncClient(AzureBlobStorageImpl azureBlobStorage, String snapshot) {
        this.azureBlobStorage = azureBlobStorage;
        this.snapshot = snapshot;
    }

    /**
     * Creates a new {@link BlockBlobAsyncClient} to this resource, maintaining configurations. Only do this for blobs
     * that are known to be block blobs.
     *
     * @return A {@link BlockBlobAsyncClient} to this resource.
     */
    public BlockBlobAsyncClient asBlockBlobAsyncClient() {
        return new BlockBlobAsyncClient(new AzureBlobStorageBuilder()
            .url(getBlobUrl().toString())
            .pipeline(azureBlobStorage.getHttpPipeline())
            .build(), snapshot);
    }

    /**
     * Creates a new {@link AppendBlobAsyncClient} to this resource, maintaining configurations. Only do this for blobs
     * that are known to be append blobs.
     *
     * @return A {@link AppendBlobAsyncClient} to this resource.
     */
    public AppendBlobAsyncClient asAppendBlobAsyncClient() {
        return new AppendBlobAsyncClient(new AzureBlobStorageBuilder()
            .url(getBlobUrl().toString())
            .pipeline(azureBlobStorage.getHttpPipeline())
            .build(), snapshot);
    }

    /**
     * Creates a new {@link PageBlobAsyncClient} to this resource, maintaining configurations. Only do this for blobs
     * that are known to be page blobs.
     *
     * @return A {@link PageBlobAsyncClient} to this resource.
     */
    public PageBlobAsyncClient asPageBlobAsyncClient() {
        return new PageBlobAsyncClient(new AzureBlobStorageBuilder()
            .url(getBlobUrl().toString())
            .pipeline(azureBlobStorage.getHttpPipeline())
            .build(), snapshot);
    }

    /**
     * Creates a new {@link BlobAsyncClient} linked to the {@code snapshot} of this blob resource.
     *
     * @param snapshot the identifier for a specific snapshot of this blob
     * @return a {@link BlobAsyncClient} used to interact with the specific snapshot.
     */
    public BlobAsyncClient getSnapshotClient(String snapshot) {
        return new BlobAsyncClient(new AzureBlobStorageBuilder()
            .url(getBlobUrl().toString())
            .pipeline(azureBlobStorage.getHttpPipeline())
            .build(), snapshot);
    }

    /**
     * Initializes a {@link ContainerAsyncClient} object pointing to the container this blob is in. This method does not
     * create a container. It simply constructs the client to the container and offers access to methods relevant to
     * containers.
     *
     * @return A {@link ContainerAsyncClient} object pointing to the container containing the blob
     */
    public ContainerAsyncClient getContainerAsyncClient() {
        BlobURLParts parts = URLParser.parse(getBlobUrl());
        return new ContainerAsyncClient(new AzureBlobStorageBuilder()
            .url(String.format("%s://%s/%s", parts.scheme(), parts.host(), parts.containerName()))
            .pipeline(azureBlobStorage.getHttpPipeline())
            .build());
    }

    /**
     * Gets the URL of the blob represented by this client.
     *
     * @return the URL.
     * @throws RuntimeException If the blob is using a malformed URL.
     */
    public URL getBlobUrl() {
        try {
            UrlBuilder urlBuilder = UrlBuilder.parse(azureBlobStorage.getUrl());
            if (snapshot != null) {
                urlBuilder.query("snapshot=" + snapshot);
            }
            return urlBuilder.toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(String.format("Invalid URL on %s: %s" + getClass().getSimpleName(), azureBlobStorage.getUrl()), e);
        }
    }

    /**
     * Determines if the blob this client represents exists in the cloud.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobAsyncClient.exists}
     *
     * @return true if the blob exists, false if it doesn't
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
     * Copies the data at the source URL to a blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobAsyncClient.startCopyFromURL#URL}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/copy-blob">Azure Docs</a></p>
     *
     * @param sourceURL The source URL to copy from. URLs outside of Azure may only be copied to block blobs.
     * @return A reactive response containing the copy ID for the long running operation.
     */
    public Mono<Response<String>> startCopyFromURL(URL sourceURL) {
        return this.startCopyFromURL(sourceURL, null, null, null);
    }

    /**
     * Copies the data at the source URL to a blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobAsyncClient.startCopyFromURL#URL-Metadata-ModifiedAccessConditions-BlobAccessConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/copy-blob">Azure Docs</a></p>
     *
     * @param sourceURL The source URL to copy from. URLs outside of Azure may only be copied to block blobs.
     * @param metadata {@link Metadata}
     * @param sourceModifiedAccessConditions {@link ModifiedAccessConditions} against the source. Standard HTTP Access
     * conditions related to the modification of data. ETag and LastModifiedTime are used to construct conditions
     * related to when the blob was changed relative to the given request. The request will fail if the specified
     * condition is not satisfied.
     * @param destAccessConditions {@link BlobAccessConditions} against the destination.
     * @return A reactive response containing the copy ID for the long running operation.
     */
    public Mono<Response<String>> startCopyFromURL(URL sourceURL, Metadata metadata, ModifiedAccessConditions sourceModifiedAccessConditions, BlobAccessConditions destAccessConditions) {
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
            null, null, sourceURL, null, metadata, null, sourceConditions,
            destAccessConditions.modifiedAccessConditions(), destAccessConditions.leaseAccessConditions(), Context.NONE))
            .map(rb -> new SimpleResponse<>(rb, rb.deserializedHeaders().copyId()));
    }

    /**
     * Stops a pending copy that was previously started and leaves a destination blob with 0 length and metadata.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobAsyncClient.abortCopyFromURL#String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/abort-copy-blob">Azure Docs</a></p>
     *
     * @param copyId The id of the copy operation to abort. Returned as the {@code copyId} field on the {@link
     * BlobStartCopyFromURLHeaders} object.
     * @return A reactive response signalling completion.
     */
    public Mono<VoidResponse> abortCopyFromURL(String copyId) {
        return this.abortCopyFromURL(copyId, null);
    }

    /**
     * Stops a pending copy that was previously started and leaves a destination blob with 0 length and metadata.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobAsyncClient.abortCopyFromURL#String-LeaseAccessConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/abort-copy-blob">Azure Docs</a></p>
     *
     * @param copyId The id of the copy operation to abort. Returned as the {@code copyId} field on the {@link
     * BlobStartCopyFromURLHeaders} object.
     * @param leaseAccessConditions By setting lease access conditions, requests will fail if the provided lease does
     * not match the active lease on the blob.
     * @return A reactive response signalling completion.
     */
    public Mono<VoidResponse> abortCopyFromURL(String copyId, LeaseAccessConditions leaseAccessConditions) {
        return postProcessResponse(this.azureBlobStorage.blobs().abortCopyFromURLWithRestResponseAsync(
            null, null, copyId, null, null, leaseAccessConditions, Context.NONE))
            .map(VoidResponse::new);
    }

    /**
     * Copies the data at the source URL to a blob and waits for the copy to complete before returning a response.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobAsyncClient.copyFromURL#URL}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/copy-blob">Azure Docs</a></p>
     *
     * @param copySource The source URL to copy from.
     * @return A reactive response containing the copy ID for the long running operation.
     */
    public Mono<Response<String>> copyFromURL(URL copySource) {
        return this.copyFromURL(copySource, null, null, null);
    }

    /**
     * Copies the data at the source URL to a blob and waits for the copy to complete before returning a response.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobAsyncClient.copyFromURL#URL-Metadata-ModifiedAccessConditions-BlobAccessConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/copy-blob">Azure Docs</a></p>
     *
     * @param copySource The source URL to copy from. URLs outside of Azure may only be copied to block blobs.
     * @param metadata {@link Metadata}
     * @param sourceModifiedAccessConditions {@link ModifiedAccessConditions} against the source. Standard HTTP Access
     * conditions related to the modification of data. ETag and LastModifiedTime are used to construct conditions
     * related to when the blob was changed relative to the given request. The request will fail if the specified
     * condition is not satisfied.
     * @param destAccessConditions {@link BlobAccessConditions} against the destination.
     * @return A reactive response containing the copy ID for the long running operation.
     */
    public Mono<Response<String>> copyFromURL(URL copySource, Metadata metadata, ModifiedAccessConditions sourceModifiedAccessConditions, BlobAccessConditions destAccessConditions) {
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
            destAccessConditions.modifiedAccessConditions(), destAccessConditions.leaseAccessConditions(), Context.NONE))
            .map(rb -> new SimpleResponse<>(rb, rb.deserializedHeaders().copyId()));
    }

    /**
     * Reads the entire blob. Uploading data must be done from the {@link BlockBlobClient}, {@link PageBlobClient}, or
     * {@link AppendBlobClient}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobAsyncClient.download}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @return A reactive response containing the blob data.
     */
    public Mono<Response<Flux<ByteBuffer>>> download() {
        return this.download(null, null, null, false);
    }

    /**
     * Reads a range of bytes from a blob. Uploading data must be done from the {@link BlockBlobClient}, {@link
     * PageBlobClient}, or {@link AppendBlobClient}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobAsyncClient.download#BlobRange-ReliableDownloadOptions-BlobAccessConditions-boolean}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param range {@link BlobRange}
     * @param options {@link ReliableDownloadOptions}
     * @param accessConditions {@link BlobAccessConditions}
     * @param rangeGetContentMD5 Whether the contentMD5 for the specified blob range should be returned.
     * @return A reactive response containing the blob data.
     */
    public Mono<Response<Flux<ByteBuffer>>> download(BlobRange range, ReliableDownloadOptions options, BlobAccessConditions accessConditions, boolean rangeGetContentMD5) {
        return this.download(range, accessConditions, rangeGetContentMD5)
            .map(response -> new SimpleResponse<>(
                response.rawResponse(),
                response.body(options).map(ByteBuf::nioBuffer).switchIfEmpty(Flux.just(ByteBuffer.allocate(0)))));
    }

    /**
     * Reads a range of bytes from a blob. The response also includes the blob's properties and metadata. For more
     * information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a>.
     * <p>
     * Note that the response body has reliable download functionality built in, meaning that a failed download stream
     * will be automatically retried. This behavior may be configured with {@link ReliableDownloadOptions}.
     *
     * @param range {@link BlobRange}
     * @param accessConditions {@link BlobAccessConditions}
     * @param rangeGetContentMD5 Whether the contentMD5 for the specified blob range should be returned.
     * @return Emits the successful response.
     * @apiNote ## Sample Code \n [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=upload_download
     * "Sample code for BlobAsyncClient.download")] \n For more samples, please see the [Samples
     * file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    Mono<DownloadAsyncResponse> download(BlobRange range, BlobAccessConditions accessConditions, boolean rangeGetContentMD5) {
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
            accessConditions.leaseAccessConditions(), accessConditions.modifiedAccessConditions(), Context.NONE))
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
     * Downloads the entire blob into a file specified by the path. The file will be created if it doesn't exist.
     * Uploading data must be done from the {@link BlockBlobClient}, {@link PageBlobClient}, or {@link
     * AppendBlobClient}.
     * <p>
     * This method makes an extra HTTP call to get the length of the blob in the beginning. To avoid this extra call,
     * use the other overload providing the {@link BlobRange} parameter.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobAsyncClient.downloadToFile#String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param filePath A non-null {@link OutputStream} instance where the downloaded data will be written.
     * @return An empty response
     */
    public Mono<Void> downloadToFile(String filePath) {
        return this.downloadToFile(filePath, null, BLOB_DEFAULT_DOWNLOAD_BLOCK_SIZE, null, null, false);
    }

    /**
     * Downloads a range of bytes  blob into a file specified by the path. The file will be created if it doesn't exist.
     * Uploading data must be done from the {@link BlockBlobClient}, {@link PageBlobClient}, or {@link
     * AppendBlobClient}.
     * <p>
     * This method makes an extra HTTP call to get the length of the blob in the beginning. To avoid this extra call,
     * provide the {@link BlobRange} parameter.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobAsyncClient.downloadToFile#String-BlobRange-Integer-ReliableDownloadOptions-BlobAccessConditions-boolean}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param filePath A non-null {@link OutputStream} instance where the downloaded data will be written.
     * @param range {@link BlobRange}
     * @param blockSize the size of a chunk to download at a time, in bytes
     * @param options {@link ReliableDownloadOptions}
     * @param accessConditions {@link BlobAccessConditions}
     * @param rangeGetContentMD5 Whether the contentMD5 for the specified blob range should be returned.
     * @return An empty response
     * @throws IllegalArgumentException If {@code blockSize} is less than 0 or greater than 100MB.
     * @throws UncheckedIOException If an I/O error occurs.
     */
    public Mono<Void> downloadToFile(String filePath, BlobRange range, Integer blockSize, ReliableDownloadOptions options,
                                     BlobAccessConditions accessConditions, boolean rangeGetContentMD5) {
        if (blockSize < 0 || blockSize > BLOB_MAX_DOWNLOAD_BLOCK_SIZE) {
            throw new IllegalArgumentException("Block size should not exceed 100MB");
        }

        return Mono.using(() -> downloadToFileResourceSupplier(filePath),
            channel -> Mono.justOrEmpty(range)
                .switchIfEmpty(getFullBlobRange(accessConditions))
                .flatMapMany(rg -> Flux.fromIterable(sliceBlobRange(rg, blockSize)))
                .flatMap(chunk -> this.download(chunk, accessConditions, rangeGetContentMD5)
                    .subscribeOn(Schedulers.elastic())
                    .flatMap(dar -> FluxUtil.bytebufStreamToFile(dar.body(options), channel, chunk.offset() - (range == null ? 0 : range.offset()))))
                .then(), this::downloadToFileCleanup);
    }

    private AsynchronousFileChannel downloadToFileResourceSupplier(String filePath) {
        try {
            return AsynchronousFileChannel.open(Paths.get(filePath), StandardOpenOption.READ, StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void downloadToFileCleanup(AsynchronousFileChannel channel) {
        try {
            channel.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobAsyncClient.delete}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-blob">Azure Docs</a></p>
     *
     * @return A reactive response signalling completion.
     */
    public Mono<VoidResponse> delete() {
        return this.delete(null, null);
    }

    /**
     * Deletes the specified blob or snapshot. Note that deleting a blob also deletes all its snapshots.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobAsyncClient.delete#DeleteSnapshotsOptionType-BlobAccessConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-blob">Azure Docs</a></p>
     *
     * @param deleteBlobSnapshotOptions Specifies the behavior for deleting the snapshots on this blob. {@code Include}
     * will delete the base blob and all snapshots. {@code Only} will delete only the snapshots. If a snapshot is being
     * deleted, you must pass null.
     * @param accessConditions {@link BlobAccessConditions}
     * @return A reactive response signalling completion.
     */
    public Mono<VoidResponse> delete(DeleteSnapshotsOptionType deleteBlobSnapshotOptions, BlobAccessConditions accessConditions) {
        accessConditions = accessConditions == null ? new BlobAccessConditions() : accessConditions;

        return postProcessResponse(this.azureBlobStorage.blobs().deleteWithRestResponseAsync(
            null, null, snapshot, null, null, deleteBlobSnapshotOptions,
            null, accessConditions.leaseAccessConditions(), accessConditions.modifiedAccessConditions(),
            Context.NONE))
            .map(VoidResponse::new);
    }

    /**
     * Returns the blob's metadata and properties.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobAsyncClient.getProperties}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-properties">Azure Docs</a></p>
     *
     * @return A reactive response containing the blob properties and metadata.
     */
    public Mono<Response<BlobProperties>> getProperties() {
        return this.getProperties(null);
    }

    /**
     * Returns the blob's metadata and properties.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobAsyncClient.getProperties#BlobAccessConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-properties">Azure Docs</a></p>
     *
     * @param accessConditions {@link BlobAccessConditions}
     * @return A reactive response containing the blob properties and metadata.
     */
    public Mono<Response<BlobProperties>> getProperties(BlobAccessConditions accessConditions) {
        accessConditions = accessConditions == null ? new BlobAccessConditions() : accessConditions;

        return postProcessResponse(this.azureBlobStorage.blobs().getPropertiesWithRestResponseAsync(
            null, null, snapshot, null, null, null,
            null, null, null, accessConditions.leaseAccessConditions(),
            accessConditions.modifiedAccessConditions(), Context.NONE))
            .map(rb -> new SimpleResponse<>(rb, new BlobProperties(rb.deserializedHeaders())));
    }

    /**
     * Changes a blob's HTTP header properties. if only one HTTP header is updated, the others will all be erased. In
     * order to preserve existing values, they must be passed alongside the header being changed.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobAsyncClient.setHTTPHeaders#BlobHTTPHeaders}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-properties">Azure Docs</a></p>
     *
     * @param headers {@link BlobHTTPHeaders}
     * @return A reactive response signalling completion.
     */
    public Mono<VoidResponse> setHTTPHeaders(BlobHTTPHeaders headers) {
        return this.setHTTPHeaders(headers, null);
    }

    /**
     * Changes a blob's HTTP header properties. if only one HTTP header is updated, the others will all be erased. In
     * order to preserve existing values, they must be passed alongside the header being changed.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobAsyncClient.setHTTPHeaders#BlobHTTPHeaders-BlobAccessConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-properties">Azure Docs</a></p>
     *
     * @param headers {@link BlobHTTPHeaders}
     * @param accessConditions {@link BlobAccessConditions}
     * @return A reactive response signalling completion.
     */
    public Mono<VoidResponse> setHTTPHeaders(BlobHTTPHeaders headers, BlobAccessConditions accessConditions) {
        accessConditions = accessConditions == null ? new BlobAccessConditions() : accessConditions;

        return postProcessResponse(this.azureBlobStorage.blobs().setHTTPHeadersWithRestResponseAsync(
            null, null, null, null, headers,
            accessConditions.leaseAccessConditions(), accessConditions.modifiedAccessConditions(), Context.NONE))
            .map(VoidResponse::new);
    }

    /**
     * Changes a blob's metadata. The specified metadata in this method will replace existing metadata. If old values
     * must be preserved, they must be downloaded and included in the call to this method.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobAsyncClient.setMetadata#Metadata}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-metadata">Azure Docs</a></p>
     *
     * @param metadata {@link Metadata}
     * @return A reactive response signalling completion.
     */
    public Mono<VoidResponse> setMetadata(Metadata metadata) {
        return this.setMetadata(metadata, null);
    }

    /**
     * Changes a blob's metadata. The specified metadata in this method will replace existing metadata. If old values
     * must be preserved, they must be downloaded and included in the call to this method.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobAsyncClient.setMetadata#Metadata-BlobAccessConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-metadata">Azure Docs</a></p>
     *
     * @param metadata {@link Metadata}
     * @param accessConditions {@link BlobAccessConditions}
     * @return A reactive response signalling completion.
     */
    public Mono<VoidResponse> setMetadata(Metadata metadata, BlobAccessConditions accessConditions) {
        metadata = metadata == null ? new Metadata() : metadata;
        accessConditions = accessConditions == null ? new BlobAccessConditions() : accessConditions;

        return postProcessResponse(this.azureBlobStorage.blobs().setMetadataWithRestResponseAsync(
            null, null, null, metadata, null, null,
            null, null, accessConditions.leaseAccessConditions(),
            accessConditions.modifiedAccessConditions(), Context.NONE))
            .map(VoidResponse::new);
    }

    /**
     * Creates a read-only snapshot of the blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobAsyncClient.createSnapshot}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/snapshot-blob">Azure Docs</a></p>
     *
     * @return A response containing a {@link BlobAsyncClient} which is used to interact with the created snapshot, use
     * {@link BlobAsyncClient#getSnapshotId()} to get the identifier for the snapshot.
     */
    public Mono<Response<BlobAsyncClient>> createSnapshot() {
        return this.createSnapshot(null, null);
    }

    /**
     * Creates a read-only snapshot of the blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobAsyncClient.createSnapshot#Metadata-BlobAccessConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/snapshot-blob">Azure Docs</a></p>
     *
     * @param metadata {@link Metadata}
     * @param accessConditions {@link BlobAccessConditions}
     * @return A response containing a {@link BlobAsyncClient} which is used to interact with the created snapshot, use
     * {@link BlobAsyncClient#getSnapshotId()} to get the identifier for the snapshot.
     */
    public Mono<Response<BlobAsyncClient>> createSnapshot(Metadata metadata, BlobAccessConditions accessConditions) {
        metadata = metadata == null ? new Metadata() : metadata;
        accessConditions = accessConditions == null ? new BlobAccessConditions() : accessConditions;

        return postProcessResponse(this.azureBlobStorage.blobs().createSnapshotWithRestResponseAsync(
            null, null, null, metadata, null, null,
            null, null, accessConditions.modifiedAccessConditions(),
            accessConditions.leaseAccessConditions(), Context.NONE))
            .map(rb -> new SimpleResponse<>(rb, this.getSnapshotClient(rb.deserializedHeaders().snapshot())));
    }

    /**
     * Sets the tier on a blob. The operation is allowed on a page blob in a premium storage account or a block blob in
     * a blob storage or GPV2 account. A premium page blob's tier determines the allowed size, IOPS, and bandwidth of
     * the blob. A block blob's tier determines the Hot/Cool/Archive storage type. This does not update the blob's
     * etag.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobAsyncClient.setTier#AccessTier}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-tier">Azure Docs</a></p>
     *
     * @param tier The new tier for the blob.
     * @return A reactive response signalling completion.
     */
    public Mono<VoidResponse> setTier(AccessTier tier) {
        return this.setTier(tier, null);
    }

    /**
     * Sets the tier on a blob. The operation is allowed on a page blob in a premium storage account or a block blob in
     * a blob storage or GPV2 account. A premium page blob's tier determines the allowed size, IOPS, and bandwidth of
     * the blob. A block blob's tier determines the Hot/Cool/Archive storage type. This does not update the blob's
     * etag.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobAsyncClient.setTier#AccessTier-LeaseAccessConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-tier">Azure Docs</a></p>
     *
     * @param tier The new tier for the blob.
     * @param leaseAccessConditions By setting lease access conditions, requests will fail if the provided lease does
     * not match the active lease on the blob.
     * @return A reactive response signalling completion.
     */
    public Mono<VoidResponse> setTier(AccessTier tier, LeaseAccessConditions leaseAccessConditions) {
        Utility.assertNotNull("tier", tier);

        return postProcessResponse(this.azureBlobStorage.blobs().setTierWithRestResponseAsync(
            null, null, tier, null, null, leaseAccessConditions, Context.NONE))
            .map(VoidResponse::new);
    }

    /**
     * Undelete restores the content and metadata of a soft-deleted blob and/or any associated soft-deleted snapshots.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobAsyncClient.undelete}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/undelete-blob">Azure Docs</a></p>
     *
     * @return A reactive response signalling completion.
     */
    public Mono<VoidResponse> undelete() {
        return postProcessResponse(this.azureBlobStorage.blobs().undeleteWithRestResponseAsync(null,
            null, Context.NONE))
            .map(VoidResponse::new);
    }

    /**
     * Acquires a lease on the blob for write and delete operations. The lease duration must be between 15 to 60
     * seconds, or infinite (-1).
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobAsyncClient.acquireLease#String-int}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/lease-blob">Azure Docs</a></p>
     *
     * @param proposedId A {@code String} in any valid GUID format. May be null.
     * @param duration The  duration of the lease, in seconds, or negative one (-1) for a lease that never expires. A
     * non-infinite lease can be between 15 and 60 seconds.
     * @return A reactive response containing the lease ID.
     */
    public Mono<Response<String>> acquireLease(String proposedId, int duration) {
        return this.acquireLease(proposedId, duration, null);
    }

    /**
     * Acquires a lease on the blob for write and delete operations. The lease duration must be between 15 to 60
     * seconds, or infinite (-1).
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobAsyncClient.acquireLease#String-int-ModifiedAccessConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/lease-blob">Azure Docs</a></p>
     *
     * @param proposedId A {@code String} in any valid GUID format. May be null.
     * @param duration The  duration of the lease, in seconds, or negative one (-1) for a lease that never expires. A
     * non-infinite lease can be between 15 and 60 seconds.
     * @param modifiedAccessConditions Standard HTTP Access conditions related to the modification of data. ETag and
     * LastModifiedTime are used to construct conditions related to when the blob was changed relative to the given
     * request. The request will fail if the specified condition is not satisfied.
     * @return A reactive response containing the lease ID.
     * @throws IllegalArgumentException If {@code duration} is outside the bounds of 15 to 60 or isn't -1.
     */
    public Mono<Response<String>> acquireLease(String proposedId, int duration, ModifiedAccessConditions modifiedAccessConditions) {
        if (!(duration == -1 || (duration >= 15 && duration <= 60))) {
            // Throwing is preferred to Mono.error because this will error out immediately instead of waiting until
            // subscription.
            throw new IllegalArgumentException("Duration must be -1 or between 15 and 60.");
        }

        return postProcessResponse(this.azureBlobStorage.blobs().acquireLeaseWithRestResponseAsync(
            null, null, null, duration, proposedId, null,
            modifiedAccessConditions, Context.NONE))
            .map(rb -> new SimpleResponse<>(rb, rb.deserializedHeaders().leaseId()));
    }

    /**
     * Renews the blob's previously-acquired lease.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobAsyncClient.renewLease#String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/lease-blob">Azure Docs</a></p>
     *
     * @param leaseId The leaseId of the active lease on the blob.
     * @return A reactive response containing the renewed lease ID.
     */
    public Mono<Response<String>> renewLease(String leaseId) {
        return this.renewLease(leaseId, null);
    }

    /**
     * Renews the blob's previously-acquired lease.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobAsyncClient.renewLease#String-ModifiedAccessConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/lease-blob">Azure Docs</a></p>
     *
     * @param leaseId The leaseId of the active lease on the blob.
     * @param modifiedAccessConditions Standard HTTP Access conditions related to the modification of data. ETag and
     * LastModifiedTime are used to construct conditions related to when the blob was changed relative to the given
     * request. The request will fail if the specified condition is not satisfied.
     * @return A reactive response containing the renewed lease ID.
     */
    public Mono<Response<String>> renewLease(String leaseId, ModifiedAccessConditions modifiedAccessConditions) {
        return postProcessResponse(this.azureBlobStorage.blobs().renewLeaseWithRestResponseAsync(null,
            null, leaseId, null, null, modifiedAccessConditions, Context.NONE))
            .map(rb -> new SimpleResponse<>(rb, rb.deserializedHeaders().leaseId()));
    }

    /**
     * Releases the blob's previously-acquired lease.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobAsyncClient.releaseLease#String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/lease-blob">Azure Docs</a></p>
     *
     * @param leaseId The leaseId of the active lease on the blob.
     * @return A reactive response signalling completion.
     */
    public Mono<VoidResponse> releaseLease(String leaseId) {
        return this.releaseLease(leaseId, null);
    }

    /**
     * Releases the blob's previously-acquired lease.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobAsyncClient.releaseLease#String-ModifiedAccessConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/lease-blob">Azure Docs</a></p>
     *
     * @param leaseId The leaseId of the active lease on the blob.
     * @param modifiedAccessConditions Standard HTTP Access conditions related to the modification of data. ETag and
     * LastModifiedTime are used to construct conditions related to when the blob was changed relative to the given
     * request. The request will fail if the specified condition is not satisfied.
     * @return A reactive response signalling completion.
     */
    public Mono<VoidResponse> releaseLease(String leaseId, ModifiedAccessConditions modifiedAccessConditions) {
        return postProcessResponse(this.azureBlobStorage.blobs().releaseLeaseWithRestResponseAsync(null,
            null, leaseId, null, null, modifiedAccessConditions, Context.NONE))
            .map(VoidResponse::new);
    }

    /**
     * BreakLease breaks the blob's previously-acquired lease (if it exists). Pass the LeaseBreakDefault (-1) constant
     * to break a fixed-duration lease when it expires or an infinite lease immediately.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobAsyncClient.breakLease}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/lease-blob">Azure Docs</a></p>
     *
     * @return A reactive response containing the remaining time in the broken lease in seconds.
     */
    public Mono<Response<Integer>> breakLease() {
        return this.breakLease(null, null);
    }

    /**
     * BreakLease breaks the blob's previously-acquired lease (if it exists). Pass the LeaseBreakDefault (-1) constant
     * to break a fixed-duration lease when it expires or an infinite lease immediately.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobAsyncClient.breakLease#Integer-ModifiedAccessConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/lease-blob">Azure Docs</a></p>
     *
     * @param breakPeriodInSeconds An optional {@code Integer} representing the proposed duration of seconds that the
     * lease should continue before it is broken, between 0 and 60 seconds. This break period is only used if it is
     * shorter than the time remaining on the lease. If longer, the time remaining on the lease is used. A new lease
     * will not be available before the break period has expired, but the lease may be held for longer than the break
     * period.
     * @param modifiedAccessConditions Standard HTTP Access conditions related to the modification of data. ETag and
     * LastModifiedTime are used to construct conditions related to when the blob was changed relative to the given
     * request. The request will fail if the specified condition is not satisfied.
     * @return A reactive response containing the remaining time in the broken lease in seconds.
     */
    public Mono<Response<Integer>> breakLease(Integer breakPeriodInSeconds, ModifiedAccessConditions modifiedAccessConditions) {
        return postProcessResponse(this.azureBlobStorage.blobs().breakLeaseWithRestResponseAsync(null,
            null, null, breakPeriodInSeconds, null, modifiedAccessConditions, Context.NONE))
            .map(rb -> new SimpleResponse<>(rb, rb.deserializedHeaders().leaseTime()));
    }

    /**
     * ChangeLease changes the blob's lease ID.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobAsyncClient.changeLease#String-String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/lease-blob">Azure Docs</a></p>
     *
     * @param leaseId The leaseId of the active lease on the blob.
     * @param proposedId A {@code String} in any valid GUID format.
     * @return A reactive response containing the new lease ID.
     */
    public Mono<Response<String>> changeLease(String leaseId, String proposedId) {
        return this.changeLease(leaseId, proposedId, null);
    }

    /**
     * ChangeLease changes the blob's lease ID.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobAsyncClient.changeLease#String-String-ModifiedAccessConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/lease-blob">Azure Docs</a></p>
     *
     * @param leaseId The leaseId of the active lease on the blob.
     * @param proposedId A {@code String} in any valid GUID format.
     * @param modifiedAccessConditions Standard HTTP Access conditions related to the modification of data. ETag and
     * LastModifiedTime are used to construct conditions related to when the blob was changed relative to the given
     * request. The request will fail if the specified condition is not satisfied.
     * @return A reactive response containing the new lease ID.
     */
    public Mono<Response<String>> changeLease(String leaseId, String proposedId, ModifiedAccessConditions modifiedAccessConditions) {
        return postProcessResponse(this.azureBlobStorage.blobs().changeLeaseWithRestResponseAsync(null,
            null, leaseId, proposedId, null, null, modifiedAccessConditions, Context.NONE))
            .map(rb -> new SimpleResponse<>(rb, rb.deserializedHeaders().leaseId()));
    }

    /**
     * Returns the sku name and account kind for the account.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobAsyncClient.getAccountInfo}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-account-information">Azure Docs</a></p>
     *
     * @return a reactor response containing the sku name and account kind.
     */
    public Mono<Response<StorageAccountInfo>> getAccountInfo() {
        return postProcessResponse(
            this.azureBlobStorage.blobs().getAccountInfoWithRestResponseAsync(null, null, Context.NONE))
            .map(rb -> new SimpleResponse<>(rb, new StorageAccountInfo(rb.deserializedHeaders())));
    }

    /**
     * Generates a user delegation SAS with the specified parameters
     *
     * @param userDelegationKey The {@code UserDelegationKey} user delegation key for the SAS
     * @param accountName The {@code String} account name for the SAS
     * @param permissions The {@code ContainerSASPermissions} permission for the SAS
     * @param expiryTime The {@code OffsetDateTime} expiry time for the SAS
     * @return A string that represents the SAS token
     */
    public String generateUserDelegationSAS(UserDelegationKey userDelegationKey, String accountName,
                                            BlobSASPermission permissions, OffsetDateTime expiryTime) {
        return this.generateUserDelegationSAS(userDelegationKey, accountName, permissions, expiryTime, null /*
        startTime */, null /* version */, null /*sasProtocol */, null /* ipRange */, null /* cacheControl */, null
            /*contentDisposition */, null /* contentEncoding */, null /* contentLanguage */, null /* contentType */);
    }

    /**
     * Generates a user delegation SAS token with the specified parameters
     *
     * @param userDelegationKey The {@code UserDelegationKey} user delegation key for the SAS
     * @param accountName The {@code String} account name for the SAS
     * @param permissions The {@code ContainerSASPermissions} permission for the SAS
     * @param expiryTime The {@code OffsetDateTime} expiry time for the SAS
     * @param startTime An optional {@code OffsetDateTime} start time for the SAS
     * @param version An optional {@code String} version for the SAS
     * @param sasProtocol An optional {@code SASProtocol} protocol for the SAS
     * @param ipRange An optional {@code IPRange} ip address range for the SAS
     * @return A string that represents the SAS token
     */
    public String generateUserDelegationSAS(UserDelegationKey userDelegationKey, String accountName,
                                            BlobSASPermission permissions, OffsetDateTime expiryTime, OffsetDateTime startTime, String version,
                                            SASProtocol sasProtocol, IPRange ipRange) {
        return this.generateUserDelegationSAS(userDelegationKey, accountName, permissions, expiryTime, startTime,
            version, sasProtocol, ipRange, null /* cacheControl */, null /* contentDisposition */, null /*
            contentEncoding */, null /* contentLanguage */, null /* contentType */);
    }

    /**
     * Generates a user delegation SAS token with the specified parameters
     *
     * @param userDelegationKey The {@code UserDelegationKey} user delegation key for the SAS
     * @param accountName The {@code String} account name for the SAS
     * @param permissions The {@code ContainerSASPermissions} permission for the SAS
     * @param expiryTime The {@code OffsetDateTime} expiry time for the SAS
     * @param startTime An optional {@code OffsetDateTime} start time for the SAS
     * @param version An optional {@code String} version for the SAS
     * @param sasProtocol An optional {@code SASProtocol} protocol for the SAS
     * @param ipRange An optional {@code IPRange} ip address range for the SAS
     * @param cacheControl An optional {@code String} cache-control header for the SAS.
     * @param contentDisposition An optional {@code String} content-disposition header for the SAS.
     * @param contentEncoding An optional {@code String} content-encoding header for the SAS.
     * @param contentLanguage An optional {@code String} content-language header for the SAS.
     * @param contentType An optional {@code String} content-type header for the SAS.
     * @return A string that represents the SAS token
     */
    public String generateUserDelegationSAS(UserDelegationKey userDelegationKey, String accountName,
                                            BlobSASPermission permissions, OffsetDateTime expiryTime, OffsetDateTime startTime, String version,
                                            SASProtocol sasProtocol, IPRange ipRange, String cacheControl, String contentDisposition,
                                            String contentEncoding, String contentLanguage, String contentType) {

        ServiceSASSignatureValues serviceSASSignatureValues = new ServiceSASSignatureValues(version, sasProtocol,
            startTime, expiryTime, permissions == null ? null : permissions.toString(), ipRange, null /* identifier*/,
            cacheControl, contentDisposition, contentEncoding, contentLanguage, contentType);

        ServiceSASSignatureValues values = configureServiceSASSignatureValues(serviceSASSignatureValues, accountName);

        SASQueryParameters sasQueryParameters = values.generateSASQueryParameters(userDelegationKey);

        return sasQueryParameters.encode();
    }

    /**
     * Generates a SAS token with the specified parameters
     *
     * @param permissions The {@code ContainerSASPermissions} permission for the SAS
     * @param expiryTime The {@code OffsetDateTime} expiry time for the SAS
     * @return A string that represents the SAS token
     */
    public String generateSAS(BlobSASPermission permissions, OffsetDateTime expiryTime) {
        return this.generateSAS(null, permissions, expiryTime, null /* startTime */,   /* identifier */ null /*
        version */, null /* sasProtocol */, null /* ipRange */, null /* cacheControl */, null /* contentLanguage*/,
            null /* contentEncoding */, null /* contentLanguage */, null /* contentType */);
    }

    /**
     * Generates a SAS token with the specified parameters
     *
     * @param identifier The {@code String} name of the access policy on the container this SAS references if any
     * @return A string that represents the SAS token
     */
    public String generateSAS(String identifier) {
        return this.generateSAS(identifier, null  /* permissions */, null /* expiryTime */, null /* startTime */,
            null /* version */, null /* sasProtocol */, null /* ipRange */, null /* cacheControl */, null /*
            contentLanguage*/, null /* contentEncoding */, null /* contentLanguage */, null /* contentType */);
    }

    /**
     * Generates a SAS token with the specified parameters
     *
     * @param identifier The {@code String} name of the access policy on the container this SAS references if any
     * @param permissions The {@code ContainerSASPermissions} permission for the SAS
     * @param expiryTime The {@code OffsetDateTime} expiry time for the SAS
     * @param startTime An optional {@code OffsetDateTime} start time for the SAS
     * @param version An optional {@code String} version for the SAS
     * @param sasProtocol An optional {@code SASProtocol} protocol for the SAS
     * @param ipRange An optional {@code IPRange} ip address range for the SAS
     * @return A string that represents the SAS token
     */
    public String generateSAS(String identifier, BlobSASPermission permissions, OffsetDateTime expiryTime,
                              OffsetDateTime startTime, String version, SASProtocol sasProtocol, IPRange ipRange) {
        return this.generateSAS(identifier, permissions, expiryTime, startTime, version, sasProtocol, ipRange, null
            /* cacheControl */, null /* contentLanguage*/, null /* contentEncoding */, null /* contentLanguage */,
            null /* contentType */);
    }

    /**
     * Generates a SAS token with the specified parameters
     *
     * @param identifier The {@code String} name of the access policy on the container this SAS references if any
     * @param permissions The {@code ContainerSASPermissions} permission for the SAS
     * @param expiryTime The {@code OffsetDateTime} expiry time for the SAS
     * @param startTime An optional {@code OffsetDateTime} start time for the SAS
     * @param version An optional {@code String} version for the SAS
     * @param sasProtocol An optional {@code SASProtocol} protocol for the SAS
     * @param ipRange An optional {@code IPRange} ip address range for the SAS
     * @param cacheControl An optional {@code String} cache-control header for the SAS.
     * @param contentDisposition An optional {@code String} content-disposition header for the SAS.
     * @param contentEncoding An optional {@code String} content-encoding header for the SAS.
     * @param contentLanguage An optional {@code String} content-language header for the SAS.
     * @param contentType An optional {@code String} content-type header for the SAS.
     * @return A string that represents the SAS token
     */
    public String generateSAS(String identifier, BlobSASPermission permissions, OffsetDateTime expiryTime,
                              OffsetDateTime startTime, String version, SASProtocol sasProtocol, IPRange ipRange, String cacheControl,
                              String contentDisposition, String contentEncoding, String contentLanguage, String contentType) {

        ServiceSASSignatureValues serviceSASSignatureValues = new ServiceSASSignatureValues(version, sasProtocol,
            startTime, expiryTime, permissions == null ? null : permissions.toString(), ipRange, identifier,
            cacheControl, contentDisposition, contentEncoding, contentLanguage, contentType);

        SharedKeyCredential sharedKeyCredential =
            Utility.getSharedKeyCredential(this.azureBlobStorage.getHttpPipeline());

        Utility.assertNotNull("sharedKeyCredential", sharedKeyCredential);

        ServiceSASSignatureValues values = configureServiceSASSignatureValues(serviceSASSignatureValues,
            sharedKeyCredential.accountName());

        SASQueryParameters sasQueryParameters = values.generateSASQueryParameters(sharedKeyCredential);

        return sasQueryParameters.encode();
    }

    /**
     * Sets serviceSASSignatureValues parameters dependent on the current blob type
     */
    ServiceSASSignatureValues configureServiceSASSignatureValues(ServiceSASSignatureValues serviceSASSignatureValues,
                                                                 String accountName) {

        // Set canonical name
        serviceSASSignatureValues.canonicalName(this.azureBlobStorage.getUrl(), accountName);

        // Set snapshotId
        serviceSASSignatureValues.snapshotId(getSnapshotId());

        // Set resource
        if (isSnapshot()) {
            serviceSASSignatureValues.resource(Constants.UrlConstants.SAS_BLOB_SNAPSHOT_CONSTANT);
        } else {
            serviceSASSignatureValues.resource(Constants.UrlConstants.SAS_BLOB_CONSTANT);
        }

        return serviceSASSignatureValues;
    }

    /**
     * Gets the snapshotId for a blob resource
     *
     * @return A string that represents the snapshotId of the snapshot blob
     */
    public String getSnapshotId() {
        return this.snapshot;
    }

    /**
     * Determines if a blob is a snapshot
     *
     * @return A boolean that indicates if a blob is a snapshot
     */
    public boolean isSnapshot() {
        return this.snapshot != null;
    }
}
