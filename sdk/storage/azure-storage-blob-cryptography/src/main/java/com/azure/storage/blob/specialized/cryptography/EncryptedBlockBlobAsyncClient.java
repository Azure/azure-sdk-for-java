// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.implementation.util.FluxUtil;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.BlobHTTPHeaders;
import com.azure.storage.blob.models.BlockBlobItem;
import com.azure.storage.blob.models.BlockLookupList;
import com.azure.storage.blob.models.LeaseAccessConditions;
import com.azure.storage.blob.models.Metadata;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.specialized.BlobAsyncClientBase;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.blob.specialized.UploadBufferPool;
import com.azure.storage.common.Constants;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;


import static com.azure.storage.blob.implementation.PostProcessor.postProcessResponse;
import static com.azure.core.implementation.util.FluxUtil.withContext;
import static java.nio.charset.StandardCharsets.UTF_8;

public class EncryptedBlockBlobAsyncClient extends BlobAsyncClientBase {

    static final int BLOB_DEFAULT_UPLOAD_BLOCK_SIZE = 4 * Constants.MB;
    private static final int BLOB_MAX_UPLOAD_BLOCK_SIZE = 100 * Constants.MB;
    private final ClientLogger logger = new ClientLogger(EncryptedBlockBlobAsyncClient.class);

    private final BlobEncryptionPolicy encryptionPolicy;

    /**
     * Package-private constructor for use by {@link EncryptedBlobClientBuilder}.
     */
    EncryptedBlockBlobAsyncClient(AzureBlobStorageImpl constructImpl, String snapshot,
        BlobEncryptionPolicy encryptionPolicy) {
        super(constructImpl, snapshot, null);
        this.encryptionPolicy = encryptionPolicy;
    }

    public BlockBlobAsyncClient getBlockBlobAsyncClient() {
        return new BlobClientBuilder()
            .pipeline(removeDecryptionPolicy(getHttpPipeline(),
                getHttpPipeline().getHttpClient()))
            .endpoint(getBlobUrl().toString())
            .buildBlobAsyncClient()
            .asBlockBlobAsyncClient();
    }

    static HttpPipeline removeDecryptionPolicy(HttpPipeline originalPipeline, HttpClient client) {
        HttpPipelinePolicy[] policies = new HttpPipelinePolicy[originalPipeline.getPolicyCount() - 1];
        int index = 0;
        for (int i = 0; i < originalPipeline.getPolicyCount(); i++) {
            if (!(originalPipeline.getPolicy(i) instanceof BlobDecryptionPolicy)) {
                policies[index] = originalPipeline.getPolicy(i);
            } else {
                index--;
            }
            index++;
        }
        return new HttpPipelineBuilder()
            .httpClient(client)
            .policies(policies)
            .build();
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob. Updating an existing block blob
     * overwrites any existing metadata on the blob. Partial updates are not supported with PutBlob; the content of the
     * existing blob is overwritten with the new content. To perform a partial update of a block blob's, use PutBlock
     * and PutBlockList in a regular blob client. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob">Azure Docs</a>.
     * <p>
     * Note that the data passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flux} must produce the same data each time it is subscribed to.
     * <p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.cryptography.EncryptedBlockBlobAsyncClient.upload#Flux-long}
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
     * of a block blob's, use PutBlock and PutBlockList on a regular blob client. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob">Azure Docs</a>.
     * <p>
     * Note that the data passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flux} must produce the same data each time it is subscribed to.
     * <p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.cryptography.EncryptedBlockBlobAsyncClient.uploadWithResponse#Flux-long-BlobHTTPHeaders-Metadata-AccessTier-BlobAccessConditions}
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
        final Metadata metadataFinal = metadata == null ? new Metadata() : metadata;
        final BlobAccessConditions accessConditionsFinal = accessConditions == null ? new BlobAccessConditions()
            : accessConditions;
        Mono<Flux<ByteBuffer>> dataFinal = encryptionPolicy.prepareToSendEncryptedRequest(data, metadataFinal);
        // Readjust the length to account for padding.
        long lengthFinal =  length + (16 - length % 16);

        return dataFinal.flatMap(df ->
            postProcessResponse(this.azureBlobStorage.blockBlobs().uploadWithRestResponseAsync(null, null, df,
                lengthFinal, null, metadataFinal, tier, null, headers, accessConditionsFinal.getLeaseAccessConditions(),
            null /* customer provided key */, accessConditionsFinal.getModifiedAccessConditions(), context))
            .map(rb -> new SimpleResponse<>(rb, new BlockBlobItem(rb.getDeserializedHeaders()))));
    }


    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     * <p>
     * Updating an existing block blob overwrites any existing metadata on the blob. Partial updates are not supported
     * with this method; the content of the existing blob is overwritten with the new content. To perform a partial
     * update of block blob's, use {@link BlockBlobAsyncClient#stageBlock(String, Flux, long) stageBlock} and {@link
     * BlockBlobAsyncClient#commitBlockList(List)} on a regular blob client. For more information, see the
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
     * {@codesnippet com.azure.storage.blob.specialized.cryptography.EncryptedBlockBlobAsyncClient.upload#Flux-ParallelTransferOptions}
     *
     * @param data The data to write to the blob. Unlike other upload methods, this method does not require that the
     * {@code Flux} be replayable. In other words, it does not have to support multiple subscribers and is not expected
     * to produce the same values across subscriptions.
     * @param parallelTransferOptions {@link ParallelTransferOptions} used to configure buffered uploading.
     * @return A reactive response containing the information of the uploaded block blob.
     */
    public Mono<BlockBlobItem> upload(Flux<ByteBuffer> data, ParallelTransferOptions parallelTransferOptions) {
        return this.uploadWithResponse(data, parallelTransferOptions, null, null, null, null).flatMap(FluxUtil::toMono);
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
     * {@codesnippet com.azure.storage.blob.specialized.cryptography.EncryptedBlockBlobAsyncClient.uploadWithResponse#Flux-ParallelTransferOptions-BlobHTTPHeaders-Metadata-AccessTier-BlobAccessConditions}
     *
     * @param data The data to write to the blob. Unlike other upload methods, this method does not require that the
     * {@code Flux} be replayable. In other words, it does not have to support multiple subscribers and is not expected
     * to produce the same values across subscriptions.
     * @param parallelTransferOptions {@link ParallelTransferOptions} used to configure buffered uploading.
     * @param headers {@link BlobHTTPHeaders}
     * @param metadata {@link Metadata}
     * @param tier {@link AccessTier} for the destination blob.
     * @param accessConditions {@link BlobAccessConditions}
     * @return A reactive response containing the information of the uploaded block blob.
     */
    // TODO (gapra) : Investigate best way to reuse all the code in the RegularBlobClient.
    public Mono<Response<BlockBlobItem>> uploadWithResponse(Flux<ByteBuffer> data,
        ParallelTransferOptions parallelTransferOptions, BlobHTTPHeaders headers, Metadata metadata, AccessTier tier,
        BlobAccessConditions accessConditions) {
        // TODO: Parallelism parameter? Or let Reactor handle it?
        // TODO: Sample/api reference
        Objects.requireNonNull(data, "data must not be null");
        final BlobAccessConditions accessConditionsFinal = accessConditions == null
            ? new BlobAccessConditions() : accessConditions;
        final Metadata metadataFinal = metadata == null ? new Metadata() : metadata;
        // TODO: Progress reporting.
        // See ProgressReporter for an explanation on why this lock is necessary and why we use AtomicLong.
        /*AtomicLong totalProgress = new AtomicLong(0);
        Lock progressLock = new ReentrantLock();*/
        Mono<Flux<ByteBuffer>> dataFinal = encryptionPolicy.prepareToSendEncryptedRequest(data, metadataFinal);
        final ParallelTransferOptions finalParallelTransferOptions = parallelTransferOptions == null
            ? new ParallelTransferOptions() : parallelTransferOptions;
        int blockSize = finalParallelTransferOptions.getBlockSize();
        int numBuffers = finalParallelTransferOptions.getNumBuffers();
        UploadBufferPool pool = new UploadBufferPool(numBuffers, blockSize);


         /*
        Break the source Flux into chunks that are <= chunk size. This makes filling the pooled buffers much easier
        as we can guarantee we only need at most two buffers for any call to write (two in the case of one pool buffer
        filling up with more data to write). We use flatMapSequential because we need to guarantee we preserve the
        ordering of the buffers, but we don't really care if one is split before another.
         */
        Flux<ByteBuffer> chunkedSource = dataFinal.flatMapMany(df -> df.flatMapSequential(buffer -> {
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
        }));

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
                this.commitBlockListWithResponse(ids, headers, metadataFinal, tier, accessConditions));
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob, with the content of the specified
     * file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.cryptography.EncryptedBlockBlobAsyncClient.uploadFromFile#String}
     *
     * @param filePath Path to the upload file
     * @return An empty response
     */
    public Mono<Void> uploadFromFile(String filePath) {
        return uploadFromFile(filePath, null, null, null, null, null);
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob, with the content of the specified
     * file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.cryptography.EncryptedBlockBlobAsyncClient.uploadFromFile#String-ParallelTransferOptions-BlobHTTPHeaders-Metadata-AccessTier-BlobAccessConditions}
     *
     * @param filePath Path to the upload file
     * @param parallelTransferOptions {@link ParallelTransferOptions} to use to upload from file. Number of parallel
     *        transfers parameter is ignored.
     * @param headers {@link BlobHTTPHeaders}
     * @param metadata {@link Metadata}
     * @param tier {@link AccessTier} for the destination blob.
     * @param accessConditions {@link BlobAccessConditions}
     * @return An empty response
     * @throws IllegalArgumentException If {@code blockSize} is less than 0 or greater than 100MB
     * @throws UncheckedIOException If an I/O error occurs
     */
    // TODO (gapra) : Investigate if this is can be parallelized, and include the parallelTransfers parameter.
    public Mono<Void> uploadFromFile(String filePath, ParallelTransferOptions parallelTransferOptions,
        BlobHTTPHeaders headers, Metadata metadata, AccessTier tier, BlobAccessConditions accessConditions) {
        Metadata metadataFinal = metadata == null ? new Metadata() : metadata;
        final ParallelTransferOptions finalParallelTransferOptions = parallelTransferOptions == null
            ? new ParallelTransferOptions()
            : parallelTransferOptions;

        return Mono.using(() -> uploadFileResourceSupplier(filePath),
            channel -> this.uploadWithResponse(FluxUtil.readFile(channel), finalParallelTransferOptions, headers,
                metadataFinal, tier, accessConditions)
                .then()
                .doOnTerminate(() -> {
                    try {
                        channel.close();
                    } catch (IOException e) {
                        throw logger.logExceptionAsError(new UncheckedIOException(e));
                    }
                }), this::uploadFileCleanup);
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

    private Mono<Response<Void>> stageBlockWithResponse(String base64BlockID, Flux<ByteBuffer> data, long length,
        LeaseAccessConditions leaseAccessConditions) {
        return withContext(context -> stageBlockWithResponse(base64BlockID, data, length, leaseAccessConditions,
            context));
    }

    Mono<Response<Void>> stageBlockWithResponse(String base64BlockID, Flux<ByteBuffer> data, long length,
        LeaseAccessConditions leaseAccessConditions, Context context) {
        return postProcessResponse(this.azureBlobStorage.blockBlobs().stageBlockWithRestResponseAsync(null,
            null, base64BlockID, length, data, null, null, null, null, leaseAccessConditions, null, context))
            .map(response -> new SimpleResponse<>(response, null));
    }

    private Mono<Response<BlockBlobItem>> commitBlockListWithResponse(List<String> base64BlockIDs,
        BlobHTTPHeaders headers, Metadata metadata, AccessTier tier, BlobAccessConditions accessConditions) {
        return withContext(context -> commitBlockListWithResponse(base64BlockIDs, headers, metadata, tier,
            accessConditions, context));
    }

    Mono<Response<BlockBlobItem>> commitBlockListWithResponse(List<String> base64BlockIDs,
        BlobHTTPHeaders headers, Metadata metadata, AccessTier tier, BlobAccessConditions accessConditions,
        Context context) {
        metadata = metadata == null ? new Metadata() : metadata;
        accessConditions = accessConditions == null ? new BlobAccessConditions() : accessConditions;

        return postProcessResponse(this.azureBlobStorage.blockBlobs().commitBlockListWithRestResponseAsync(
            null, null, new BlockLookupList().setLatest(base64BlockIDs), null, null, null, metadata, tier, null,
            headers, accessConditions.getLeaseAccessConditions(), null /*cpk*/,
            accessConditions.getModifiedAccessConditions(), context))
            .map(rb -> new SimpleResponse<>(rb, new BlockBlobItem(rb.getDeserializedHeaders())));
    }
}
