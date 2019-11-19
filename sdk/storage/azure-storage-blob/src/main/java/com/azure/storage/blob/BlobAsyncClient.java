// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.implementation.util.ModelHelper;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlockBlobItem;
import com.azure.storage.blob.models.CpkInfo;
import com.azure.storage.blob.models.CustomerProvidedKey;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.specialized.AppendBlobAsyncClient;
import com.azure.storage.blob.specialized.BlobAsyncClientBase;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.blob.specialized.PageBlobAsyncClient;
import com.azure.storage.blob.specialized.SpecializedBlobClientBuilder;
import com.azure.storage.common.implementation.Constants;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.monoError;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * This class provides a client that contains generic blob operations for Azure Storage Blobs. Operations allowed by the
 * client are uploading and downloading, copying a blob, retrieving and setting metadata, retrieving and setting HTTP
 * headers, and deleting and un-deleting a blob.
 *
 * <p>
 * This client is instantiated through {@link BlobClientBuilder} or retrieved via {@link
 * BlobContainerAsyncClient#getBlobAsyncClient(String) getBlobAsyncClient}.
 *
 * <p>
 * For operations on a specific blob type (i.e append, block, or page) use {@link #getAppendBlobAsyncClient()
 * getAppendBlobAsyncClient}, {@link #getBlockBlobAsyncClient() getBlockBlobAsyncClient}, or {@link
 * #getPageBlobAsyncClient() getPageBlobAsyncClient} to construct a client that allows blob specific operations.
 *
 * <p>
 * Please refer to the
 * <a href=https://docs.microsoft.com/en-us/rest/api/storageservices/understanding-block-blobs--append-blobs--and-page-blobs>Azure
 * Docs</a> for more information.
 */
public class BlobAsyncClient extends BlobAsyncClientBase {
    private static final int CHUNKED_UPLOAD_REQUIREMENT = 4 * Constants.MB;

    /**
     * The block size to use if none is specified in parallel operations.
     */
    public static final int BLOB_DEFAULT_UPLOAD_BLOCK_SIZE = 4 * Constants.MB;

    /**
     * The number of buffers to use if none is specied on the buffered upload method.
     */
    public static final int BLOB_DEFAULT_NUMBER_OF_BUFFERS = 8;

    /**
     * If a blob is known to be greater than 100MB, using a larger block size will trigger some server-side
     * optimizations. If the block size is not set and the size of the blob is known to be greater than 100MB, this
     * value will be used.
     */
    public static final int BLOB_DEFAULT_HTBB_UPLOAD_BLOCK_SIZE = 8 * Constants.MB;
    static final int BLOB_MAX_UPLOAD_BLOCK_SIZE = 100 * Constants.MB;
    private final ClientLogger logger = new ClientLogger(BlobAsyncClient.class);

    /**
     * Protected constructor for use by {@link BlobClientBuilder}.
     *
     * @param pipeline The pipeline used to send and receive service requests.
     * @param url The endpoint where to send service requests.
     * @param serviceVersion The version of the service to receive requests.
     * @param accountName The storage account name.
     * @param containerName The container name.
     * @param blobName The blob name.
     * @param snapshot The snapshot identifier for the blob, pass {@code null} to interact with the blob directly.
     * @param customerProvidedKey Customer provided key used during encryption of the blob's data on the server, pass
     * {@code null} to allow the service to use its own encryption.
     */
    protected BlobAsyncClient(HttpPipeline pipeline, String url, BlobServiceVersion serviceVersion, String accountName,
        String containerName, String blobName, String snapshot, CpkInfo customerProvidedKey) {
        super(pipeline, url, serviceVersion, accountName, containerName, blobName, snapshot, customerProvidedKey);
    }

    /**
     * Creates a new {@link BlobAsyncClient} linked to the {@code snapshot} of this blob resource.
     *
     * @param snapshot the identifier for a specific snapshot of this blob
     * @return A {@link BlobAsyncClient} used to interact with the specific snapshot.
     */
    @Override
    public BlobAsyncClient getSnapshotClient(String snapshot) {
        return new BlobAsyncClient(getHttpPipeline(), getBlobUrl(), getServiceVersion(), getAccountName(),
            getContainerName(), getBlobName(), snapshot, getCustomerProvidedKey());
    }

    /**
     * Creates a new {@link AppendBlobAsyncClient} associated with this blob.
     *
     * @return A {@link AppendBlobAsyncClient} associated with this blob.
     */
    public AppendBlobAsyncClient getAppendBlobAsyncClient() {
        return prepareBuilder().buildAppendBlobAsyncClient();
    }

    /**
     * Creates a new {@link BlockBlobAsyncClient} associated with this blob.
     *
     * @return A {@link BlockBlobAsyncClient} associated with this blob.
     */
    public BlockBlobAsyncClient getBlockBlobAsyncClient() {
        return prepareBuilder().buildBlockBlobAsyncClient();
    }

    /**
     * Creates a new {@link PageBlobAsyncClient} associated with this blob.
     *
     * @return A {@link PageBlobAsyncClient} associated with this blob.
     */
    public PageBlobAsyncClient getPageBlobAsyncClient() {
        return prepareBuilder().buildPageBlobAsyncClient();
    }

    private SpecializedBlobClientBuilder prepareBuilder() {
        SpecializedBlobClientBuilder builder = new SpecializedBlobClientBuilder()
            .pipeline(getHttpPipeline())
            .endpoint(getBlobUrl())
            .snapshot(getSnapshotId())
            .serviceVersion(getServiceVersion());

        CpkInfo cpk = getCustomerProvidedKey();
        if (cpk != null) {
            builder.customerProvidedKey(new CustomerProvidedKey(cpk.getEncryptionKey()));
        }

        return builder;
    }

    /**
     * Creates a new block blob. By default this method will not overwrite an existing blob.
     * <p>
     * Updating an existing block blob overwrites any existing metadata on the blob. Partial updates are not supported
     * with this method; the content of the existing blob is overwritten with the new content. To perform a partial
     * update of a block blob's, use {@link BlockBlobAsyncClient#stageBlock(String, Flux, long) stageBlock} and {@link
     * BlockBlobAsyncClient#commitBlockList(List) commitBlockList}. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block">Azure Docs for Put Block</a> and the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block-list">Azure Docs for Put Block List</a>.
     * <p>
     * The data passed need not support multiple subscriptions/be replayable as is required in other upload methods when
     * retries are enabled, and the length of the data need not be known in advance. Therefore, this method does
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
     * {@codesnippet com.azure.storage.blob.BlobAsyncClient.upload#Flux-ParallelTransferOptions}
     *
     * @param data The data to write to the blob. Unlike other upload methods, this method does not require that the
     * {@code Flux} be replayable. In other words, it does not have to support multiple subscribers and is not expected
     * to produce the same values across subscriptions.
     * @param parallelTransferOptions {@link ParallelTransferOptions} used to configure buffered uploading.
     * @return A reactive response containing the information of the uploaded block blob.
     */
    public Mono<BlockBlobItem> upload(Flux<ByteBuffer> data, ParallelTransferOptions parallelTransferOptions) {
        try {
            return upload(data, parallelTransferOptions, false);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     * <p>
     * Updating an existing block blob overwrites any existing metadata on the blob. Partial updates are not supported
     * with this method; the content of the existing blob is overwritten with the new content. To perform a partial
     * update of a block blob's, use {@link BlockBlobAsyncClient#stageBlock(String, Flux, long) stageBlock} and {@link
     * BlockBlobAsyncClient#commitBlockList(List) commitBlockList}. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block">Azure Docs for Put Block</a> and the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block-list">Azure Docs for Put Block List</a>.
     * <p>
     * The data passed need not support multiple subscriptions/be replayable as is required in other upload methods when
     * retries are enabled, and the length of the data need not be known in advance. Therefore, this method does
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
     * {@codesnippet com.azure.storage.blob.BlobAsyncClient.upload#Flux-ParallelTransferOptions-boolean}
     *
     * @param data The data to write to the blob. Unlike other upload methods, this method does not require that the
     * {@code Flux} be replayable. In other words, it does not have to support multiple subscribers and is not expected
     * to produce the same values across subscriptions.
     * @param parallelTransferOptions {@link ParallelTransferOptions} used to configure buffered uploading.
     * @param overwrite Whether or not to overwrite, should the blob already exist.
     * @return A reactive response containing the information of the uploaded block blob.
     */
    public Mono<BlockBlobItem> upload(Flux<ByteBuffer> data, ParallelTransferOptions parallelTransferOptions,
        boolean overwrite) {
        try {
            Mono<Void> overwriteCheck;
            BlobRequestConditions requestConditions;

            if (overwrite) {
                overwriteCheck = Mono.empty();
                requestConditions = null;
            } else {
                overwriteCheck = exists().flatMap(exists -> exists
                    ? monoError(logger, new IllegalArgumentException(Constants.BLOB_ALREADY_EXISTS))
                    : Mono.empty());
                requestConditions = new BlobRequestConditions().setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD);
            }

            return overwriteCheck
                .then(uploadWithResponse(data, parallelTransferOptions, null, null, null,
                    requestConditions)).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     * <p>
     * Updating an existing block blob overwrites any existing metadata on the blob. Partial updates are not supported
     * with this method; the content of the existing blob is overwritten with the new content. To perform a partial
     * update of a block blob's, use {@link BlockBlobAsyncClient#stageBlock(String, Flux, long) stageBlock} and {@link
     * BlockBlobAsyncClient#commitBlockList(List) commitBlockList}. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block">Azure Docs for Put Block</a> and the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block-list">Azure Docs for Put Block List</a>.
     * <p>
     * The data passed need not support multiple subscriptions/be replayable as is required in other upload methods when
     * retries are enabled, and the length of the data need not be known in advance. Therefore, this method does
     * support uploading any arbitrary data source, including network streams. This behavior is possible because this
     * method will perform some internal buffering as configured by the blockSize and numBuffers parameters, so while
     * this method may offer additional convenience, it will not be as performant as other options, which should be
     * preferred when possible.
     * <p>
     * Typically, the greater the number of buffers used, the greater the possible parallelism when transferring the
     * data. Larger buffers means we will have to stage fewer blocks and therefore require fewer IO operations. The
     * trade-offs between these values are context-dependent, so some experimentation may be required to optimize inputs
     * for a given scenario.
     * <p>
     * To avoid overwriting, pass "*" to {@link BlobRequestConditions#setIfNoneMatch(String)}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobAsyncClient.uploadWithResponse#Flux-ParallelTransferOptions-BlobHttpHeaders-Map-AccessTier-BlobRequestConditions}
     *
     * <p><strong>Using Progress Reporting</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobAsyncClient.uploadWithResponse#Flux-ParallelTransferOptions-BlobHttpHeaders-Map-AccessTier-BlobRequestConditions.ProgressReporter}
     *
     * @param data The data to write to the blob. Unlike other upload methods, this method does not require that the
     * {@code Flux} be replayable. In other words, it does not have to support multiple subscribers and is not expected
     * to produce the same values across subscriptions.
     * @param parallelTransferOptions {@link ParallelTransferOptions} used to configure buffered uploading.
     * @param headers {@link BlobHttpHeaders}
     * @param metadata Metadata to associate with the blob.
     * @param tier {@link AccessTier} for the destination blob.
     * @param requestConditions {@link BlobRequestConditions}
     * @return A reactive response containing the information of the uploaded block blob.
     */
    public Mono<Response<BlockBlobItem>> uploadWithResponse(Flux<ByteBuffer> data,
        ParallelTransferOptions parallelTransferOptions, BlobHttpHeaders headers, Map<String, String> metadata,
        AccessTier tier, BlobRequestConditions requestConditions) {
        try {
            Objects.requireNonNull(data, "'data' must not be null");
            BlobRequestConditions validatedRequestConditions = requestConditions == null
                ? new BlobRequestConditions() : requestConditions;
            final ParallelTransferOptions validatedParallelTransferOptions =
                ModelHelper.populateAndApplyDefaults(parallelTransferOptions);

            BlockBlobAsyncClient blockBlobAsyncClient = getBlockBlobAsyncClient();

            Function<Flux<ByteBuffer>, Mono<Response<BlockBlobItem>>> uploadInChunksFunction = (stream) ->
                uploadInChunks(blockBlobAsyncClient, stream, validatedParallelTransferOptions, headers, metadata, tier,
                    validatedRequestConditions);

            BiFunction<Flux<ByteBuffer>, Long, Mono<Response<BlockBlobItem>>> uploadFullBlobMethod =
                (stream, length) -> blockBlobAsyncClient.uploadWithResponse(ProgressReporter
                    .addProgressReporting(stream, validatedParallelTransferOptions.getProgressReceiver()),
                    length, headers, metadata, tier, null, validatedRequestConditions);

            return determineUploadFullOrChunked(data, uploadInChunksFunction, uploadFullBlobMethod);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    private Mono<Response<BlockBlobItem>> uploadInChunks(BlockBlobAsyncClient blockBlobAsyncClient,
        Flux<ByteBuffer> data, ParallelTransferOptions parallelTransferOptions, BlobHttpHeaders headers,
        Map<String, String> metadata, AccessTier tier, BlobRequestConditions requestConditions) {
        // TODO: Parallelism parameter? Or let Reactor handle it?
        // TODO: Sample/api reference
        // See ProgressReporter for an explanation on why this lock is necessary and why we use AtomicLong.
        AtomicLong totalProgress = new AtomicLong();
        Lock progressLock = new ReentrantLock();

        // Validation done in the constructor.
        UploadBufferPool pool = new UploadBufferPool(parallelTransferOptions.getNumBuffers(),
            parallelTransferOptions.getBlockSize());

        /*
        Break the source Flux into chunks that are <= chunk size. This makes filling the pooled buffers much easier
        as we can guarantee we only need at most two buffers for any call to write (two in the case of one pool
        buffer filling up with more data to write). We use flatMapSequential because we need to guarantee we
        preserve the ordering of the buffers, but we don't really care if one is split before another.
         */
        Flux<ByteBuffer> chunkedSource = data
            .flatMapSequential(buffer -> {
                if (buffer.remaining() <= parallelTransferOptions.getBlockSize()) {
                    return Flux.just(buffer);
                }
                int numSplits = (int) Math.ceil(buffer.remaining() / (double) parallelTransferOptions.getBlockSize());
                return Flux.range(0, numSplits)
                    .map(i -> {
                        ByteBuffer duplicate = buffer.duplicate().asReadOnlyBuffer();
                        duplicate.position(i * parallelTransferOptions.getBlockSize());
                        duplicate.limit(Math.min(duplicate.limit(), (i + 1) * parallelTransferOptions.getBlockSize()));
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
                Flux<ByteBuffer> progressData = ProgressReporter.addParallelProgressReporting(Flux.just(buffer),
                    parallelTransferOptions.getProgressReceiver(), progressLock, totalProgress);

                final String blockId = Base64.getEncoder().encodeToString(
                    UUID.randomUUID().toString().getBytes(UTF_8));

                return blockBlobAsyncClient.stageBlockWithResponse(blockId, progressData, buffer.remaining(),
                    null, requestConditions.getLeaseId())
                    // We only care about the stageBlock insofar as it was successful,
                    // but we need to collect the ids.
                    .map(x -> blockId)
                    .doFinally(x -> pool.returnBuffer(buffer))
                    .flux();
            }) // TODO: parallelism?
            .collect(Collectors.toList())
            .flatMap(ids ->
                blockBlobAsyncClient.commitBlockListWithResponse(ids, headers, metadata, tier, requestConditions));
    }

    private Mono<Response<BlockBlobItem>> determineUploadFullOrChunked(final Flux<ByteBuffer> data,
        final Function<Flux<ByteBuffer>, Mono<Response<BlockBlobItem>>> uploadInChunks,
        final BiFunction<Flux<ByteBuffer>, Long, Mono<Response<BlockBlobItem>>> uploadFullBlob) {
        final long[] bufferedDataSize = {0};
        final LinkedList<ByteBuffer> cachedBuffers = new LinkedList<>();

        /*
         * Window the reactive stream until either the stream completes or the windowing size is hit. If the window
         * size is hit the next emission will be the pointer to the rest of the reactive steam.
         *
         * Once the windowing has completed buffer the two streams, this should create a maximum overhead of ~4MB plus
         * the next stream emission if the window size was hit. If there are two streams buffered use Stage Blocks and
         * Put Block List as the upload mechanism otherwise use Put Blob.
         */
        return data
            .filter(ByteBuffer::hasRemaining)
            .windowUntil(buffer -> {
                if (bufferedDataSize[0] > CHUNKED_UPLOAD_REQUIREMENT) {
                    return false;
                } else {
                    bufferedDataSize[0] += buffer.remaining();

                    if (bufferedDataSize[0] > CHUNKED_UPLOAD_REQUIREMENT) {
                        return true;
                    } else {
                        /*
                         * Buffer until the first 4MB are emitted from the stream in case Put Blob is used. This is
                         * done to support replayability which is required by the Put Blob code path, it doesn't buffer
                         * the stream in a way that the Stage Blocks and Put Block List code path does, and this API
                         * explicitly states that it supports non-replayable streams.
                         */
                        ByteBuffer cachedBuffer = ByteBuffer.allocate(buffer.remaining()).put(buffer);
                        cachedBuffer.flip();
                        cachedBuffers.add(cachedBuffer);
                        return false;
                    }
                }
                /*
                 * Use cutBefore = true as we want to window all data under 4MB into one window.
                 * Set the prefetch to CHUNKED_UPLOAD_REQUIREMENT in the case that there are numerous tiny buffers,
                 * windowUntil uses a default limit of 256 and once that is hit it will trigger onComplete which causes
                 * downstream issues.
                 */
            }, true, CHUNKED_UPLOAD_REQUIREMENT)
            .buffer(2)
            .next()
            .flatMap(fluxes -> {
                if (fluxes.size() == 1) {
                    return uploadFullBlob.apply(Flux.fromIterable(cachedBuffers), bufferedDataSize[0]);
                } else {
                    return uploadInChunks.apply(dequeuingFlux(cachedBuffers).concatWith(fluxes.get(1)));
                }
            })
            // If nothing was emitted from the stream upload an empty blob.
            .switchIfEmpty(uploadFullBlob.apply(Flux.empty(), 0L));
    }

    private static Flux<ByteBuffer> dequeuingFlux(Queue<ByteBuffer> queue) {
        // Generate is used as opposed to Flux.fromIterable as it allows the buffers to be garbage collected sooner.
        return Flux.generate(sink -> {
            ByteBuffer buffer = queue.poll();
            if (buffer != null) {
                sink.next(buffer);
            } else {
                sink.complete();
            }
        });
    }

    /**
     * Creates a new block blob with the content of the specified file. By default this method will not overwrite an
     * existing blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobAsyncClient.uploadFromFile#String}
     *
     * @param filePath Path to the upload file
     * @return An empty response
     * @throws UncheckedIOException If an I/O error occurs
     */
    public Mono<Void> uploadFromFile(String filePath) {
        try {
            return uploadFromFile(filePath, false);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob, with the content of the specified
     * file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobAsyncClient.uploadFromFile#String-boolean}
     *
     * @param filePath Path to the upload file
     * @param overwrite Whether or not to overwrite, should the blob already exist.
     * @return An empty response
     * @throws UncheckedIOException If an I/O error occurs
     */
    public Mono<Void> uploadFromFile(String filePath, boolean overwrite) {
        try {
            Mono<Void> overwriteCheck = Mono.empty();
            BlobRequestConditions requestConditions = null;

            // Note that if the file will be uploaded using a putBlob, we also can skip the exists check.
            if (!overwrite) {
                if (uploadInBlocks(filePath)) {
                    overwriteCheck = exists().flatMap(exists -> exists
                        ? monoError(logger, new IllegalArgumentException(Constants.BLOB_ALREADY_EXISTS))
                        : Mono.empty());
                }

                requestConditions = new BlobRequestConditions().setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD);
            }

            return overwriteCheck.then(uploadFromFile(filePath, null, null, null, null, requestConditions));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob, with the content of the specified
     * file.
     * <p>
     * To avoid overwriting, pass "*" to {@link BlobRequestConditions#setIfNoneMatch(String)}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobAsyncClient.uploadFromFile#String-ParallelTransferOptions-BlobHttpHeaders-Map-AccessTier-BlobRequestConditions}
     *
     * @param filePath Path to the upload file
     * @param parallelTransferOptions {@link ParallelTransferOptions} to use to upload from file. Number of parallel
     * transfers parameter is ignored.
     * @param headers {@link BlobHttpHeaders}
     * @param metadata Metadata to associate with the blob.
     * @param tier {@link AccessTier} for the destination blob.
     * @param requestConditions {@link BlobRequestConditions}
     * @return An empty response
     * @throws UncheckedIOException If an I/O error occurs
     */
    public Mono<Void> uploadFromFile(String filePath, ParallelTransferOptions parallelTransferOptions,
        BlobHttpHeaders headers, Map<String, String> metadata, AccessTier tier,
        BlobRequestConditions requestConditions) {
        try {
            return Mono.using(() -> uploadFileResourceSupplier(filePath),
                channel -> {
                    try {
                        BlockBlobAsyncClient blockBlobAsyncClient = getBlockBlobAsyncClient();
                        long fileSize = channel.size();

                        // If the file is larger than 256MB chunk it and stage it as blocks.
                        if (uploadInBlocks(filePath)) {
                            return uploadBlocks(fileSize, parallelTransferOptions, headers, metadata, tier,
                                requestConditions, channel, blockBlobAsyncClient);
                        } else {
                            // Otherwise we know it can be sent in a single request reducing network overhead.
                            return blockBlobAsyncClient.uploadWithResponse(FluxUtil.readFile(channel), fileSize,
                                headers, metadata, tier, null, requestConditions)
                                .then();
                        }
                    } catch (IOException ex) {
                        return Mono.error(ex);
                    }
                }, this::uploadFileCleanup);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    boolean uploadInBlocks(String filePath) {
        AsynchronousFileChannel channel = uploadFileResourceSupplier(filePath);
        boolean retVal;
        try {
            retVal = channel.size() > 256 * Constants.MB;
        } catch (IOException e) {
            throw logger.logExceptionAsError(new UncheckedIOException(e));
        } finally {
            uploadFileCleanup(channel);
        }

        return retVal;
    }

    private Mono<Void> uploadBlocks(long fileSize, ParallelTransferOptions parallelTransferOptions,
        BlobHttpHeaders headers, Map<String, String> metadata, AccessTier tier,
        BlobRequestConditions requestConditions, AsynchronousFileChannel channel, BlockBlobAsyncClient client) {
        final ParallelTransferOptions finalParallelTransferOptions =
            ModelHelper.populateAndApplyDefaults(parallelTransferOptions);
        final BlobRequestConditions finalRequestConditions = (requestConditions == null)
            ? new BlobRequestConditions() : requestConditions;

        // See ProgressReporter for an explanation on why this lock is necessary and why we use AtomicLong.
        AtomicLong totalProgress = new AtomicLong();
        Lock progressLock = new ReentrantLock();

        final SortedMap<Long, String> blockIds = new TreeMap<>();
        return Flux.fromIterable(sliceFile(fileSize, finalParallelTransferOptions.getBlockSize(),
            parallelTransferOptions))
            .flatMap(chunk -> {
                String blockId = getBlockID();
                blockIds.put(chunk.getOffset(), blockId);

                Flux<ByteBuffer> progressData = ProgressReporter.addParallelProgressReporting(
                    FluxUtil.readFile(channel, chunk.getOffset(), chunk.getCount()),
                    finalParallelTransferOptions.getProgressReceiver(), progressLock, totalProgress);

                return client.stageBlockWithResponse(blockId, progressData, chunk.getCount(), null,
                    finalRequestConditions.getLeaseId());
            })
            .then(Mono.defer(() -> client.commitBlockListWithResponse(
                new ArrayList<>(blockIds.values()), headers, metadata, tier, finalRequestConditions)))
            .then();
    }

    /**
     * RESERVED FOR INTERNAL USE.
     *
     * Resource Supplier for UploadFile.
     *
     * @param filePath The path for the file
     * @return {@code AsynchronousFileChannel}
     * @throws UncheckedIOException an input output exception.
     */
    protected AsynchronousFileChannel uploadFileResourceSupplier(String filePath) {
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

    private List<BlobRange> sliceFile(long fileSize, int blockSize, ParallelTransferOptions originalOptions) {
        boolean enableHtbbOptimization = originalOptions == null || originalOptions.getBlockSize() == null;
        List<BlobRange> ranges = new ArrayList<>();
        if (fileSize > 100 * Constants.MB && enableHtbbOptimization) {
            blockSize = BLOB_DEFAULT_HTBB_UPLOAD_BLOCK_SIZE;
        }
        for (long pos = 0; pos < fileSize; pos += blockSize) {
            long count = blockSize;
            if (pos + count > fileSize) {
                count = fileSize - pos;
            }
            ranges.add(new BlobRange(pos, count));
        }
        return ranges;
    }
}
