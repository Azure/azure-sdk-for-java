// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.implementation.util.FluxUtil;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.AccessTierOptional;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.BlobHTTPHeaders;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlockBlobItem;
import com.azure.storage.blob.models.BlockList;
import com.azure.storage.blob.models.BlockListType;
import com.azure.storage.blob.models.BlockLookupList;
import com.azure.storage.blob.models.CpkInfo;
import com.azure.storage.blob.models.LeaseAccessConditions;
import com.azure.storage.blob.models.Metadata;
import com.azure.storage.blob.models.SourceModifiedAccessConditions;
import com.azure.storage.common.Constants;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.azure.core.implementation.util.FluxUtil.withContext;
import static com.azure.storage.blob.implementation.PostProcessor.postProcessResponse;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Client to a block blob. It may only be instantiated through a {@link SpecializedBlobClientBuilder} or via the method
 * {@link BlobAsyncClient#asBlockBlobAsyncClient()}. This class does not hold any state about a particular blob, but is
 * instead a convenient way of sending appropriate requests to the resource on the service.
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
@ServiceClient(builder = SpecializedBlobClientBuilder.class, isAsync = true)
public final class BlockBlobAsyncClient extends BlobAsyncClientBase {
    private final ClientLogger logger = new ClientLogger(BlockBlobAsyncClient.class);

    static final int BLOB_DEFAULT_UPLOAD_BLOCK_SIZE = 4 * Constants.MB;
    static final int BLOB_MAX_UPLOAD_BLOCK_SIZE = 100 * Constants.MB;

    /**
     * Indicates the maximum number of bytes that can be sent in a call to upload.
     */
    public static final int MAX_UPLOAD_BLOB_BYTES = 256 * Constants.MB;

    /**
     * Indicates the maximum number of bytes that can be sent in a call to stageBlock.
     */
    public static final int MAX_STAGE_BLOCK_BYTES = 100 * Constants.MB;

    /**
     * Indicates the maximum number of blocks allowed in a block blob.
     */
    public static final int MAX_BLOCKS = 50000;

    /**
     * Package-private constructor for use by {@link SpecializedBlobClientBuilder}.
     *
     * @param azureBlobStorage the API client for blob storage
     */
    BlockBlobAsyncClient(AzureBlobStorageImpl azureBlobStorage, String snapshot, CpkInfo cpk) {
        super(azureBlobStorage, snapshot, cpk);
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob. Updating an existing block blob
     * overwrites any existing metadata on the blob. Partial updates are not supported with PutBlob; the content of the
     * existing blob is overwritten with the new content. To perform a partial update of a block blob's, use PutBlock
     * and PutBlockList. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob">Azure Docs</a>.
     * <p>
     * Note that the data passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flux} must produce the same data each time it is subscribed to.
     * <p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlockBlobAsyncClient.upload#Flux-long}
     *
     * @param data The data to write to the blob. Note that this {@code Flux} must be replayable if retries are enabled
     * (the default). In other words, the Flux must produce the same data each time it is subscribed to.
     * @param length The exact length of the data. It is important that this value match precisely the length of the
     * data emitted by the {@code Flux}.
     * @return A reactive response containing the information of the uploaded block blob.
     */
    public Mono<BlockBlobItem> upload(Flux<ByteBuffer> data, long length) {
        return uploadWithResponse(data, length, null, null, null, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     * <p>
     * Updating an existing block blob overwrites any existing metadata on the blob. Partial updates are not supported
     * with PutBlob; the content of the existing blob is overwritten with the new content. To perform a partial update
     * of a block blob's, use PutBlock and PutBlockList. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob">Azure Docs</a>.
     * <p>
     * Note that the data passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flux} must produce the same data each time it is subscribed to.
     * <p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlockBlobAsyncClient.uploadWithResponse#Flux-long-BlobHTTPHeaders-Metadata-AccessTier-BlobAccessConditions}
     *
     * @param data The data to write to the blob. Note that this {@code Flux} must be replayable if retries are enabled
     * (the default). In other words, the Flux must produce the same data each time it is subscribed to.
     * @param length The exact length of the data. It is important that this value match precisely the length of the
     * data emitted by the {@code Flux}.
     * @param headers {@link BlobHTTPHeaders}
     * @param metadata {@link Metadata}
     * @param tier {@link AccessTier} for the destination blob.
     * @param accessConditions {@link BlobAccessConditions}
     * @return A reactive response containing the information of the uploaded block blob.
     */
    public Mono<Response<BlockBlobItem>> uploadWithResponse(Flux<ByteBuffer> data, long length, BlobHTTPHeaders headers,
        Metadata metadata, AccessTier tier, BlobAccessConditions accessConditions) {
        return withContext(context -> uploadWithResponse(data, length, headers, metadata, tier, accessConditions,
            context));
    }

    Mono<Response<BlockBlobItem>> uploadWithResponse(Flux<ByteBuffer> data, long length, BlobHTTPHeaders headers,
        Metadata metadata, AccessTier tier, BlobAccessConditions accessConditions, Context context) {
        metadata = metadata == null ? new Metadata() : metadata;
        accessConditions = accessConditions == null ? new BlobAccessConditions() : accessConditions;
        AccessTierOptional opTier = tier == null ? null : AccessTierOptional.fromString(tier.toString());

        return postProcessResponse(this.azureBlobStorage.blockBlobs().uploadWithRestResponseAsync(null,
            null, data, length, null, metadata, opTier, null, headers, accessConditions.getLeaseAccessConditions(), cpk,
            accessConditions.getModifiedAccessConditions(), context))
            .map(rb -> new SimpleResponse<>(rb, new BlockBlobItem(rb.getDeserializedHeaders())));
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     * <p>
     * Updating an existing block blob overwrites any existing metadata on the blob. Partial updates are not supported
     * with this method; the content of the existing blob is overwritten with the new content. To perform a partial
     * update of a block blob's, use {@link BlockBlobAsyncClient#stageBlock(String, Flux, long) stageBlock} and {@link
     * BlockBlobAsyncClient#commitBlockList(List)}. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block">Azure Docs for Put Block</a> and the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block-list">Azure Docs for Put Block List</a>.
     * <p>
     * The data passed need not support multiple subscriptions/be replayable as is required in other upload methods when
     * retries are enabled, and the length of the data need not be known in advance. Therefore, this method should
     * support uploading any arbitrary data source, including network streams. This behavior is possible because this
     * method will perform some internal buffering as configured by the blockSize and numBuffers parameters, so while
     * this method may offer additional convenience, it will not be as performant as other options, which should be
     * preferred when possible.
     * <p>
     * Typically, the greater the number of buffers used, the greater the possible parallelism when transferring the
     * data. Larger buffers means we will have to stage fewer blocks and therefore require fewer IO operations. The
     * trade-offs between these values are context-dependent, so some experimentation may be required to optimize inputs
     * for a given scenario.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlockBlobAsyncClient.upload#Flux-int-int}
     *
     * @param data The data to write to the blob. Unlike other upload methods, this method does not require that the
     * {@code Flux} be replayable. In other words, it does not have to support multiple subscribers and is not expected
     * to produce the same values across subscriptions.
     * @param blockSize The size of each block that will be staged. This value also determines the size that each buffer
     * used by this method will be and determines the number of requests that need to be made. The amount of memory
     * consumed by this method may be up to blockSize * numBuffers. If block size is large, this method will make fewer
     * network calls, but each individual call will send more data and will therefore take longer.
     * @param numBuffers The maximum number of buffers this method should allocate. Must be at least two. Typically, the
     * larger the number of buffers, the more parallel, and thus faster, the upload portion of this operation will be.
     * The amount of memory consumed by this method may be up to blockSize * numBuffers.
     * @return A reactive response containing the information of the uploaded block blob.
     */
    public Mono<BlockBlobItem> upload(Flux<ByteBuffer> data, int blockSize, int numBuffers) {
        return this.uploadWithResponse(data, blockSize, numBuffers, null, null, null, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob. Updating an existing block blob
     * overwrites any existing metadata on the blob. Partial updates are not supported with this method; the content of
     * the existing blob is overwritten with the new content. To perform a partial update of a block blob's, use {@link
     * BlockBlobAsyncClient#stageBlock(String, Flux, long) stageBlock} and
     * {@link BlockBlobAsyncClient#commitBlockList(List)}, which this method uses internally. For more information,
     * see the <a href="https://docs.microsoft.com/rest/api/storageservices/put-block">Azure
     * Docs for Put Block</a> and the <a href="https://docs.microsoft.com/rest/api/storageservices/put-block-list">Azure
     * Docs for Put Block List</a>.
     * <p>
     * The data passed need not support multiple subscriptions/be replayable as is required in other upload methods when
     * retries are enabled, and the length of the data need not be known in advance. Therefore, this method should
     * support uploading any arbitrary data source, including network streams. This behavior is possible because this
     * method will perform some internal buffering as configured by the blockSize and numBuffers parameters, so while
     * this method may offer additional convenience, it will not be as performant as other options, which should be
     * preferred when possible.
     * <p>
     * Typically, the greater the number of buffers used, the greater the possible parallelism when transferring the
     * data. Larger buffers means we will have to stage fewer blocks and therefore require fewer IO operations. The
     * trade-offs between these values are context-dependent, so some experimentation may be required to optimize inputs
     * for a given scenario.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlockBlobAsyncClient.uploadWithResponse#Flux-int-int-BlobHTTPHeaders-Metadata-AccessTier-BlobAccessConditions}
     *
     * @param data The data to write to the blob. Unlike other upload methods, this method does not require that the
     * {@code Flux} be replayable. In other words, it does not have to support multiple subscribers and is not expected
     * to produce the same values across subscriptions.
     * @param blockSize The size of each block that will be staged. This value also determines the size that each buffer
     * used by this method will be and determines the number of requests that need to be made. The amount of memory
     * consumed by this method may be up to blockSize * numBuffers. If block size is large, this method will make fewer
     * network calls, but each individual call will send more data and will therefore take longer.
     * @param numBuffers The maximum number of buffers this method should allocate. Must be at least two. Typically, the
     * larger the number of buffers, the more parallel, and thus faster, the upload portion of this operation will be.
     * The amount of memory consumed by this method may be up to blockSize * numBuffers.
     * @param headers {@link BlobHTTPHeaders}
     * @param metadata {@link Metadata}
     * @param tier {@link AccessTier} for the destination blob.
     * @param accessConditions {@link BlobAccessConditions}
     * @return A reactive response containing the information of the uploaded block blob.
     */
    public Mono<Response<BlockBlobItem>> uploadWithResponse(Flux<ByteBuffer> data, int blockSize, int numBuffers,
        BlobHTTPHeaders headers, Metadata metadata, AccessTier tier, BlobAccessConditions accessConditions) {
        // TODO: Parallelism parameter? Or let Reactor handle it?
        // TODO: Sample/api reference
        Objects.requireNonNull(data, "data must not be null");
        BlobAccessConditions accessConditionsFinal = accessConditions == null
            ? new BlobAccessConditions() : accessConditions;

        // TODO: Progress reporting.
        // See ProgressReporter for an explanation on why this lock is necessary and why we use AtomicLong.
        /*AtomicLong totalProgress = new AtomicLong(0);
        Lock progressLock = new ReentrantLock();*/

        // Validation done in the constructor.
        UploadBufferPool pool = new UploadBufferPool(numBuffers, blockSize);

        /*
        Break the source Flux into chunks that are <= chunk size. This makes filling the pooled buffers much easier
        as we can guarantee we only need at most two buffers for any call to write (two in the case of one pool buffer
        filling up with more data to write). We use flatMapSequential because we need to guarantee we preserve the
        ordering of the buffers, but we don't really care if one is split before another.
         */
        Flux<ByteBuffer> chunkedSource = data.flatMapSequential(buffer -> {
            if (buffer.remaining() <= blockSize) {
                return Flux.just(buffer);
            }
            int numSplits = (int) Math.ceil(buffer.remaining() / (double) blockSize);
            return Flux.range(0, numSplits)
                .map(i -> {
                    ByteBuffer duplicate = buffer.duplicate().asReadOnlyBuffer();
                    duplicate.position(i * blockSize);
                    duplicate.limit(Math.min(duplicate.limit(), (i + 1) * blockSize));
                    return duplicate;
                });
        });

        /*
         Write to the pool and upload the output.
         */
        return chunkedSource.concatMap(pool::write)
            .concatWith(Flux.defer(pool::flush))
            .flatMapSequential(buffer -> {
                // Report progress as necessary.
                /*Flux<ByteBuffer> progressData = ProgressReporter.addParallelProgressReporting(Flux.just(buffer),
                    optionsReal.progressReceiver(), progressLock, totalProgress);*/

                final String blockId = Base64.getEncoder().encodeToString(
                    UUID.randomUUID().toString().getBytes(UTF_8));

                return this.stageBlockWithResponse(blockId, Flux.just(buffer), buffer.remaining(),
                    accessConditionsFinal.getLeaseAccessConditions())
                    // We only care about the stageBlock insofar as it was successful, but we need to collect the ids.
                    .map(x -> {
                        pool.returnBuffer(buffer);
                        return blockId;
                    }).flux();

            }) // TODO: parallelism?
            .collect(Collectors.toList())
            .flatMap(ids ->
                this.commitBlockListWithResponse(ids, headers, metadata, tier, accessConditions));

    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob, with the content of the specified
     * file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlockBlobAsyncClient.uploadFromFile#String}
     *
     * @param filePath Path to the upload file
     * @return An empty response
     */
    public Mono<Void> uploadFromFile(String filePath) {
        return uploadFromFile(filePath, BLOB_DEFAULT_UPLOAD_BLOCK_SIZE, null, null, null, null);
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob, with the content of the specified
     * file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlockBlobAsyncClient.uploadFromFile#String-Integer-BlobHTTPHeaders-Metadata-AccessTier-BlobAccessConditions}
     *
     * @param filePath Path to the upload file
     * @param blockSize Size of the blocks to upload
     * @param headers {@link BlobHTTPHeaders}
     * @param metadata {@link Metadata}
     * @param tier {@link AccessTier} for the destination blob.
     * @param accessConditions {@link BlobAccessConditions}
     * @return An empty response
     * @throws IllegalArgumentException If {@code blockSize} is less than 0 or greater than 100MB
     * @throws UncheckedIOException If an I/O error occurs
     */
    public Mono<Void> uploadFromFile(String filePath, Integer blockSize, BlobHTTPHeaders headers, Metadata metadata,
        AccessTier tier, BlobAccessConditions accessConditions) {
        if (blockSize < 0 || blockSize > BLOB_MAX_UPLOAD_BLOCK_SIZE) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Block size should not exceed 100MB"));
        }

        return Mono.using(() -> uploadFileResourceSupplier(filePath),
            channel -> {
                final SortedMap<Long, String> blockIds = new TreeMap<>();
                return Flux.fromIterable(sliceFile(filePath, blockSize))
                    .doOnNext(chunk -> blockIds.put(chunk.getOffset(), getBlockID()))
                    .flatMap(chunk -> {
                        String blockId = blockIds.get(chunk.getOffset());
                        return stageBlockWithResponse(blockId, FluxUtil.readFile(channel, chunk.getOffset(),
                            chunk.getCount()), chunk.getCount(), null);
                    })
                    .then(Mono.defer(() -> commitBlockListWithResponse(
                        new ArrayList<>(blockIds.values()), headers, metadata, tier, accessConditions)))
                    .then()
                    .doOnTerminate(() -> {
                        try {
                            channel.close();
                        } catch (IOException e) {
                            throw logger.logExceptionAsError(new UncheckedIOException(e));
                        }
                    });
            }, this::uploadFileCleanup);
    }


    private AsynchronousFileChannel uploadFileResourceSupplier(String filePath) {
        try {
            return AsynchronousFileChannel.open(Paths.get(filePath), StandardOpenOption.READ);
        } catch (IOException e) {
            throw logger.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    private void uploadFileCleanup(AsynchronousFileChannel channel) {
        try {
            channel.close();
        } catch (IOException e) {
            throw logger.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    private String getBlockID() {
        return Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
    }

    private List<BlobRange> sliceFile(String path, Integer blockSize) {
        if (blockSize == null) {
            blockSize = BLOB_DEFAULT_UPLOAD_BLOCK_SIZE;
        }
        File file = new File(path);
        assert file.exists();
        List<BlobRange> ranges = new ArrayList<>();
        for (long pos = 0; pos < file.length(); pos += blockSize) {
            long count = blockSize;
            if (pos + count > file.length()) {
                count = file.length() - pos;
            }
            ranges.add(new BlobRange(pos, count));
        }
        return ranges;
    }

    /**
     * Uploads the specified block to the block blob's "staging area" to be later committed by a call to
     * commitBlockList. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block">Azure Docs</a>.
     * <p>
     * Note that the data passed must be replayable if retries are enabled (the default). In other words, the
     *
     * @param base64BlockID A Base64 encoded {@code String} that specifies the ID for this block. Note that all block
     * ids for a given blob must be the same length.
     * @param data The data to write to the block. Note that this {@code Flux} must be replayable if retries are enabled
     * (the default). In other words, the Flux must produce the same data each time it is subscribed to.
     * @param length The exact length of the data. It is important that this value match precisely the length of the
     * data emitted by the {@code Flux}.
     * @return A reactive response signalling completion.
     * @code Flux} must produce the same data each time it is subscribed to.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlockBlobAsyncClient.stageBlock#String-Flux-long}
     */
    public Mono<Void> stageBlock(String base64BlockID, Flux<ByteBuffer> data, long length) {
        return stageBlockWithResponse(base64BlockID, data, length, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Uploads the specified block to the block blob's "staging area" to be later committed by a call to
     * commitBlockList. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block">Azure Docs</a>.
     * <p>
     * Note that the data passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flux} must produce the same data each time it is subscribed to.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlockBlobAsyncClient.stageBlockWithResponse#String-Flux-long-LeaseAccessConditions}
     *
     * @param base64BlockID A Base64 encoded {@code String} that specifies the ID for this block. Note that all block
     * ids for a given blob must be the same length.
     * @param data The data to write to the block. Note that this {@code Flux} must be replayable if retries are enabled
     * (the default). In other words, the Flux must produce the same data each time it is subscribed to.
     * @param length The exact length of the data. It is important that this value match precisely the length of the
     * data emitted by the {@code Flux}.
     * @param leaseAccessConditions By setting lease access conditions, requests will fail if the provided lease does
     * not match the active lease on the blob.
     * @return A reactive response signalling completion.
     */
    public Mono<VoidResponse> stageBlockWithResponse(String base64BlockID, Flux<ByteBuffer> data, long length,
        LeaseAccessConditions leaseAccessConditions) {
        return withContext(context -> stageBlockWithResponse(base64BlockID, data, length, leaseAccessConditions,
            context));
    }

    Mono<VoidResponse> stageBlockWithResponse(String base64BlockID, Flux<ByteBuffer> data, long length,
        LeaseAccessConditions leaseAccessConditions, Context context) {
        return postProcessResponse(this.azureBlobStorage.blockBlobs().stageBlockWithRestResponseAsync(null,
            null, base64BlockID, length, data, null, null, null, null, leaseAccessConditions, cpk, context))
            .map(VoidResponse::new);
    }

    /**
     * Creates a new block to be committed as part of a blob where the contents are read from a URL. For more
     * information, see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/put-block-from-url">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlockBlobAsyncClient.stageBlockFromURL#String-URL-BlobRange}
     *
     * @param base64BlockID A Base64 encoded {@code String} that specifies the ID for this block. Note that all block
     * ids for a given blob must be the same length.
     * @param sourceURL The url to the blob that will be the source of the copy.  A source blob in the same storage
     * account can be authenticated via Shared Key. However, if the source is a blob in another account, the source blob
     * must either be public or must be authenticated via a shared access signature. If the source blob is public, no
     * authentication is required to perform the operation.
     * @param sourceRange {@link BlobRange}
     * @return A reactive response signalling completion.
     */
    public Mono<Void> stageBlockFromURL(String base64BlockID, URL sourceURL, BlobRange sourceRange) {
        return this.stageBlockFromURLWithResponse(base64BlockID, sourceURL, sourceRange, null, null, null)
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a new block to be committed as part of a blob where the contents are read from a URL. For more
     * information, see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/put-block-from-url">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlockBlobAsyncClient.stageBlockFromURLWithResponse#String-URL-BlobRange-byte-LeaseAccessConditions-SourceModifiedAccessConditions}
     *
     * @param base64BlockID A Base64 encoded {@code String} that specifies the ID for this block. Note that all block
     * ids for a given blob must be the same length.
     * @param sourceURL The url to the blob that will be the source of the copy.  A source blob in the same storage
     * account can be authenticated via Shared Key. However, if the source is a blob in another account, the source blob
     * must either be public or must be authenticated via a shared access signature. If the source blob is public, no
     * authentication is required to perform the operation.
     * @param sourceRange {@link BlobRange}
     * @param sourceContentMD5 An MD5 hash of the block content from the source blob. If specified, the service will
     * calculate the MD5 of the received data and fail the request if it does not match the provided MD5.
     * @param leaseAccessConditions By setting lease access conditions, requests will fail if the provided lease does
     * not match the active lease on the blob.
     * @param sourceModifiedAccessConditions {@link SourceModifiedAccessConditions}
     * @return A reactive response signalling completion.
     */
    public Mono<VoidResponse> stageBlockFromURLWithResponse(String base64BlockID, URL sourceURL,
        BlobRange sourceRange, byte[] sourceContentMD5, LeaseAccessConditions leaseAccessConditions,
        SourceModifiedAccessConditions sourceModifiedAccessConditions) {
        return withContext(context -> stageBlockFromURLWithResponse(base64BlockID, sourceURL, sourceRange,
            sourceContentMD5, leaseAccessConditions, sourceModifiedAccessConditions));
    }

    Mono<VoidResponse> stageBlockFromURLWithResponse(String base64BlockID, URL sourceURL,
        BlobRange sourceRange, byte[] sourceContentMD5, LeaseAccessConditions leaseAccessConditions,
        SourceModifiedAccessConditions sourceModifiedAccessConditions, Context context) {
        sourceRange = sourceRange == null ? new BlobRange(0) : sourceRange;

        return postProcessResponse(
            this.azureBlobStorage.blockBlobs().stageBlockFromURLWithRestResponseAsync(null, null,
                base64BlockID, 0, sourceURL, sourceRange.toHeaderValue(), sourceContentMD5, null, null,
                null, cpk, leaseAccessConditions, sourceModifiedAccessConditions, context))
            .map(VoidResponse::new);
    }

    /**
     * Returns the list of blocks that have been uploaded as part of a block blob using the specified block list filter.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-block-list">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlockBlobAsyncClient.listBlocks#BlockListType}
     *
     * @param listType Specifies which type of blocks to return.
     * @return A reactive response containing the list of blocks.
     */
    public Mono<BlockList> listBlocks(BlockListType listType) {
        return this.listBlocksWithResponse(listType, null).map(Response::getValue);
    }

    /**
     * Returns the list of blocks that have been uploaded as part of a block blob using the specified block list filter.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-block-list">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlockBlobAsyncClient.listBlocksWithResponse#BlockListType-LeaseAccessConditions}
     *
     * @param listType Specifies which type of blocks to return.
     * @param leaseAccessConditions By setting lease access conditions, requests will fail if the provided lease does
     * not match the active lease on the blob.
     * @return A reactive response containing the list of blocks.
     */
    public Mono<Response<BlockList>> listBlocksWithResponse(BlockListType listType,
        LeaseAccessConditions leaseAccessConditions) {

        return withContext(context -> listBlocksWithResponse(listType, leaseAccessConditions, context));
    }

    Mono<Response<BlockList>> listBlocksWithResponse(BlockListType listType,
        LeaseAccessConditions leaseAccessConditions, Context context) {

        return postProcessResponse(this.azureBlobStorage.blockBlobs().getBlockListWithRestResponseAsync(null,
            null, listType, snapshot, null, null, leaseAccessConditions, context))
            .map(response -> new SimpleResponse<>(response, response.getValue()));
    }

    /**
     * Writes a blob by specifying the list of block IDs that are to make up the blob. In order to be written as part of
     * a blob, a block must have been successfully written to the server in a prior stageBlock operation. You can call
     * commitBlockList to update a blob by uploading only those blocks that have changed, then committing the new and
     * existing blocks together. Any blocks not specified in the block list and permanently deleted. For more
     * information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block-list">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlockBlobAsyncClient.commitBlockList#List}
     *
     * @param base64BlockIDs A list of base64 encode {@code String}s that specifies the block IDs to be committed.
     * @return A reactive response containing the information of the block blob.
     */
    public Mono<BlockBlobItem> commitBlockList(List<String> base64BlockIDs) {
        return commitBlockListWithResponse(base64BlockIDs, null, null, null, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Writes a blob by specifying the list of block IDs that are to make up the blob. In order to be written as part of
     * a blob, a block must have been successfully written to the server in a prior stageBlock operation. You can call
     * commitBlockList to update a blob by uploading only those blocks that have changed, then committing the new and
     * existing blocks together. Any blocks not specified in the block list and permanently deleted. For more
     * information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block-list">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlockBlobAsyncClient.commitBlockListWithResponse#List-BlobHTTPHeaders-Metadata-AccessTier-BlobAccessConditions}
     *
     * @param base64BlockIDs A list of base64 encode {@code String}s that specifies the block IDs to be committed.
     * @param headers {@link BlobHTTPHeaders}
     * @param metadata {@link Metadata}
     * @param tier {@link AccessTier} for the destination blob.
     * @param accessConditions {@link BlobAccessConditions}
     * @return A reactive response containing the information of the block blob.
     */
    public Mono<Response<BlockBlobItem>> commitBlockListWithResponse(List<String> base64BlockIDs,
        BlobHTTPHeaders headers, Metadata metadata, AccessTier tier, BlobAccessConditions accessConditions) {
        return withContext(context -> commitBlockListWithResponse(base64BlockIDs, headers, metadata, tier,
            accessConditions, context));
    }

    Mono<Response<BlockBlobItem>> commitBlockListWithResponse(List<String> base64BlockIDs,
        BlobHTTPHeaders headers, Metadata metadata, AccessTier tier, BlobAccessConditions accessConditions,
        Context context) {
        metadata = metadata == null ? new Metadata() : metadata;
        accessConditions = accessConditions == null ? new BlobAccessConditions() : accessConditions;
        AccessTierOptional tierOp = tier == null ? null : AccessTierOptional.fromString(tier.toString());

        return postProcessResponse(this.azureBlobStorage.blockBlobs().commitBlockListWithRestResponseAsync(
            null, null, new BlockLookupList().setLatest(base64BlockIDs), null, null, null, metadata, tierOp, null,
            headers, accessConditions.getLeaseAccessConditions(), cpk, accessConditions.getModifiedAccessConditions(),
            context))
            .map(rb -> new SimpleResponse<>(rb, new BlockBlobItem(rb.getDeserializedHeaders())));
    }
}
