// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.implementation.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobRange;
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

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static com.azure.core.implementation.util.FluxUtil.monoError;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * This class provides a client that contains generic blob operations for Azure Storage Blobs. Operations allowed by
 * the client are downloading and copying a blob, retrieving and setting metadata, retrieving and setting HTTP headers,
 * and deleting and un-deleting a blob.
 *
 * <p>
 * This client is instantiated through {@link BlobClientBuilder} or retrieved via
 * {@link BlobContainerAsyncClient#getBlobAsyncClient(String) getBlobAsyncClient}.
 *
 * <p>
 * For operations on a specific blob type (i.e append, block, or page) use
 * {@link #getAppendBlobAsyncClient() getAppendBlobAsyncClient}, {@link #getBlockBlobAsyncClient()
 * getBlockBlobAsyncClient}, or {@link #getPageBlobAsyncClient() getPageBlobAsyncClient} to construct a client that
 * allows blob specific operations.
 *
 * <p>
 * Please refer to the <a href=https://docs.microsoft.com/en-us/rest/api/storageservices/understanding-block-blobs--append-blobs--and-page-blobs>Azure
 * Docs</a> for more information.
 */
public class BlobAsyncClient extends BlobAsyncClientBase {
    public static final int BLOB_DEFAULT_UPLOAD_BLOCK_SIZE = 4 * Constants.MB;
    static final int BLOB_MAX_UPLOAD_BLOCK_SIZE = 100 * Constants.MB;

    private final ClientLogger logger = new ClientLogger(BlobAsyncClient.class);

    /**
     * Package-private constructor for use by {@link BlobClientBuilder}.
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
     * @return a {@link BlobAsyncClient} used to interact with the specific snapshot.
     */
    @Override
    public BlobAsyncClient getSnapshotClient(String snapshot) {
        return new BlobAsyncClient(getHttpPipeline(), getBlobUrl(), getServiceVersion(), getAccountName(),
            getContainerName(), getBlobName(), snapshot, getCustomerProvidedKey());
    }

    /**
     * Creates a new {@link AppendBlobAsyncClient} associated to this blob.
     *
     * @return a {@link AppendBlobAsyncClient} associated to this blob.
     */
    public AppendBlobAsyncClient getAppendBlobAsyncClient() {
        return prepareBuilder().buildAppendBlobAsyncClient();
    }

    /**
     * Creates a new {@link BlockBlobAsyncClient} associated to this blob.
     *
     * @return a {@link BlockBlobAsyncClient} associated to this blob.
     */
    public BlockBlobAsyncClient getBlockBlobAsyncClient() {
        return prepareBuilder().buildBlockBlobAsyncClient();
    }

    /**
     * Creates a new {@link PageBlobAsyncClient} associated to this blob.
     *
     * @return a {@link PageBlobAsyncClient} associated to this blob.
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
            return uploadWithResponse(data, parallelTransferOptions, false, null, null, null, null)
                .flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
     * {@codesnippet com.azure.storage.blob.BlobAsyncClient.uploadWithResponse#Flux-ParallelTransferOptions-boolean-BlobHttpHeaders-Map-AccessTier-BlobAccessConditions}
     *
     * <p><strong>Using Progress Reporting</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobAsyncClient.uploadWithResponse#Flux-ParallelTransferOptions-boolean-BlobHttpHeaders-Map-AccessTier-BlobAccessConditions.ProgressReporter}
     *
     * @param data The data to write to the blob. Unlike other upload methods, this method does not require that the
     * {@code Flux} be replayable. In other words, it does not have to support multiple subscribers and is not expected
     * to produce the same values across subscriptions.
     * @param parallelTransferOptions {@link ParallelTransferOptions} used to configure buffered uploading.
     * @param overwrite Whether to overwrite, should data already exist on this blob.
     * @param headers {@link BlobHttpHeaders}
     * @param metadata Metadata to associate with the blob.
     * @param tier {@link AccessTier} for the destination blob.
     * @param accessConditions {@link BlobAccessConditions}
     * @return A reactive response containing the information of the uploaded block blob.
     */
    public Mono<Response<BlockBlobItem>> uploadWithResponse(Flux<ByteBuffer> data,
        ParallelTransferOptions parallelTransferOptions, boolean overwrite, BlobHttpHeaders headers,
        Map<String, String> metadata, AccessTier tier, BlobAccessConditions accessConditions) {
        try {
            // TODO: Parallelism parameter? Or let Reactor handle it?
            // TODO: Sample/api reference
            Objects.requireNonNull(data, "'data' must not be null");
            BlobAccessConditions accessConditionsFinal = accessConditions == null
                ? new BlobAccessConditions() : accessConditions;
            final ParallelTransferOptions finalParallelTransferOptions = parallelTransferOptions == null
                ? new ParallelTransferOptions() : parallelTransferOptions;
            int blockSize = finalParallelTransferOptions.getBlockSize();
            int numBuffers = finalParallelTransferOptions.getNumBuffers();
            ProgressReceiver progressReceiver = finalParallelTransferOptions.getProgressReceiver();

            // See ProgressReporter for an explanation on why this lock is necessary and why we use AtomicLong.
            AtomicLong totalProgress = new AtomicLong(0);
            Lock progressLock = new ReentrantLock();

            // Validation done in the constructor.
            UploadBufferPool pool = new UploadBufferPool(numBuffers, blockSize);

            /*
            Break the source Flux into chunks that are <= chunk size. This makes filling the pooled buffers much easier
            as we can guarantee we only need at most two buffers for any call to write (two in the case of one pool
            buffer filling up with more data to write). We use flatMapSequential because we need to guarantee we
            preserve the ordering of the buffers, but we don't really care if one is split before another.
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
            Mono<Response<BlockBlobItem>> uploadTask = chunkedSource.concatMap(pool::write)
                .concatWith(Flux.defer(pool::flush))
                .flatMapSequential(buffer -> {
                    // Report progress as necessary.
                    Flux<ByteBuffer> progressData = ProgressReporter.addParallelProgressReporting(Flux.just(buffer),
                        progressReceiver, progressLock, totalProgress);


                    final String blockId = Base64.getEncoder().encodeToString(
                        UUID.randomUUID().toString().getBytes(UTF_8));

                    return getBlockBlobAsyncClient().stageBlockWithResponse(blockId, progressData, buffer.remaining(),
                        accessConditionsFinal.getLeaseAccessConditions())
                        // We only care about the stageBlock insofar as it was successful,
                        // but we need to collect the ids.
                        .map(x -> blockId)
                        .doFinally(x -> pool.returnBuffer(buffer))
                        .flux();

                }) // TODO: parallelism?
                .collect(Collectors.toList())
                .flatMap(ids ->
                    getBlockBlobAsyncClient()
                        .commitBlockListWithResponse(ids, overwrite, headers, metadata, tier, accessConditions));

            if (overwrite) {
                return uploadTask;
            } else {
                return exists()
                    .flatMap(exists -> exists
                        ? monoError(logger, new IllegalArgumentException(Constants.BLOB_ALREADY_EXISTS))
                        : uploadTask);
            }
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
     * {@codesnippet com.azure.storage.blob.BlobAsyncClient.uploadFromFile#String}
     *
     * @param filePath Path to the upload file
     * @return An empty response
     */
    public Mono<Void> uploadFromFile(String filePath) {
        try {
            return uploadFromFile(filePath, null, false, null, null, null, null);
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
     * {@codesnippet com.azure.storage.blob.BlobAsyncClient.uploadFromFile#String-ParallelTransferOptions-boolean-BlobHttpHeaders-Map-AccessTier-BlobAccessConditions}
     *
     * @param filePath Path to the upload file
     * @param parallelTransferOptions {@link ParallelTransferOptions} to use to upload from file. Number of parallel
     *        transfers parameter is ignored.
     * @param overwrite Whether to overwrite, should data already exist on this blob.
     * @param headers {@link BlobHttpHeaders}
     * @param metadata Metadata to associate with the blob.
     * @param tier {@link AccessTier} for the destination blob.
     * @param accessConditions {@link BlobAccessConditions}
     * @return An empty response
     * @throws IllegalArgumentException If {@code blockSize} is less than 0 or greater than 100MB
     * @throws UncheckedIOException If an I/O error occurs
     */
    // TODO (gapra) : Investigate if this is can be parallelized, and include the parallelTransfers parameter.
    public Mono<Void> uploadFromFile(String filePath, ParallelTransferOptions parallelTransferOptions,
        boolean overwrite, BlobHttpHeaders headers, Map<String, String> metadata, AccessTier tier,
        BlobAccessConditions accessConditions) {
        try {
            final ParallelTransferOptions finalParallelTransferOptions = parallelTransferOptions == null
                ? new ParallelTransferOptions()
                : parallelTransferOptions;
            ProgressReceiver progressReceiver = finalParallelTransferOptions.getProgressReceiver();

            // See ProgressReporter for an explanation on why this lock is necessary and why we use AtomicLong.
            AtomicLong totalProgress = new AtomicLong(0);
            Lock progressLock = new ReentrantLock();

            Mono<Void> uploadTask = Mono.using(() -> uploadFileResourceSupplier(filePath),
                channel -> {
                    final SortedMap<Long, String> blockIds = new TreeMap<>();
                    return Flux.fromIterable(sliceFile(filePath, finalParallelTransferOptions.getBlockSize()))
                        .doOnNext(chunk -> blockIds.put(chunk.getOffset(), getBlockID()))
                        .flatMap(chunk -> {
                            String blockId = blockIds.get(chunk.getOffset());

                            Flux<ByteBuffer> progressData = ProgressReporter.addParallelProgressReporting(
                                FluxUtil.readFile(channel, chunk.getOffset(), chunk.getCount()),
                                progressReceiver, progressLock, totalProgress);

                            return getBlockBlobAsyncClient()
                                .stageBlockWithResponse(blockId, progressData, chunk.getCount(), null);
                        })
                        .then(Mono.defer(() -> getBlockBlobAsyncClient().commitBlockListWithResponse(
                            new ArrayList<>(blockIds.values()), overwrite, headers, metadata, tier, accessConditions)))
                        .then()
                        .doOnTerminate(() -> {
                            try {
                                channel.close();
                            } catch (IOException e) {
                                throw logger.logExceptionAsError(new UncheckedIOException(e));
                            }
                        });
                }, this::uploadFileCleanup);
            if (overwrite) {
                return uploadTask;
            } else {
                return exists()
                    .flatMap(exists -> exists
                        ? monoError(logger, new IllegalArgumentException(Constants.BLOB_ALREADY_EXISTS))
                        : uploadTask);
            }
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Resource Supplier for UploadFile
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

    private List<BlobRange> sliceFile(String path, int blockSize) {
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
}
