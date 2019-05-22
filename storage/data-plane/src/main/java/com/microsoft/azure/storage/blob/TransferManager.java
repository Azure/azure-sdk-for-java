// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage.blob;

import com.microsoft.azure.storage.blob.models.BlobDownloadHeaders;
import com.microsoft.azure.storage.blob.models.BlockBlobCommitBlockListResponse;
import com.microsoft.azure.storage.blob.models.ModifiedAccessConditions;
import com.microsoft.azure.storage.blob.models.StorageErrorCode;
import com.microsoft.rest.v2.util.FlowableUtil;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.StrictMath.toIntExact;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * This class contains a collection of methods (and structures associated with those methods) which perform higher-level
 * operations. Whereas operations on the URL types guarantee a single REST request and make no assumptions on desired
 * behavior, these methods will often compose several requests to provide a convenient way of performing more complex
 * operations. Further, we will make our own assumptions and optimizations for common cases that may not be ideal for
 * rarer cases.
 */
public final class TransferManager {

    /**
     * The default size of a download chunk for download large blobs.
     */
    public static final int BLOB_DEFAULT_DOWNLOAD_BLOCK_SIZE = 4 * Constants.MB;

    /**
     * Uploads the contents of a file to a block blob in parallel, breaking it into block-size chunks if necessary.
     *
     * @param file
     *         The file to upload.
     * @param blockBlobURL
     *         Points to the blob to which the data should be uploaded.
     * @param blockLength
     *         If the data must be broken up into blocks, this value determines what size those blocks will be. This
     *         will affect the total number of service requests made as each REST request uploads exactly one block in
     *         full. This value will be ignored if the data can be uploaded in a single put-blob operation. Must be
     *         between 1 and {@link BlockBlobURL#MAX_STAGE_BLOCK_BYTES}. Note as well that
     *         {@code fileLength/blockLength} must be less than or equal to {@link BlockBlobURL#MAX_BLOCKS}.
     * @param maxSingleShotSize
     *         If the size of the data is less than or equal to this value, it will be uploaded in a single put
     *         rather than broken up into chunks. If the data is uploaded in a single shot, the block size will be
     *         ignored. Some constraints to consider are that more requests cost more, but several small or mid-sized
     *         requests may sometimes perform better. Must be greater than 0. May be null to accept default behavior.
     * @param options
     *         {@link TransferManagerUploadToBlockBlobOptions}
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=tm_file "Sample code for TransferManager.uploadFileToBlockBlob")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public static Single<CommonRestResponse> uploadFileToBlockBlob(final AsynchronousFileChannel file,
            final BlockBlobURL blockBlobURL, final int blockLength, Integer maxSingleShotSize,
            final TransferManagerUploadToBlockBlobOptions options) throws IOException {
        Utility.assertNotNull("file", file);
        Utility.assertNotNull("blockBlobURL", blockBlobURL);
        Utility.assertInBounds("blockLength", blockLength, 1, BlockBlobURL.MAX_STAGE_BLOCK_BYTES);
        if (maxSingleShotSize != null) {
            Utility.assertInBounds("maxSingleShotSize", maxSingleShotSize, 0, BlockBlobURL.MAX_UPLOAD_BLOB_BYTES);
        } else {
            maxSingleShotSize = BlockBlobURL.MAX_UPLOAD_BLOB_BYTES;
        }
        TransferManagerUploadToBlockBlobOptions optionsReal = options == null
                ? new TransferManagerUploadToBlockBlobOptions() : options;

        // See ProgressReporter for an explanation on why this lock is necessary and why we use AtomicLong.
        AtomicLong totalProgress = new AtomicLong(0);
        Lock progressLock = new ReentrantLock();

        // If the size of the file can fit in a single upload, do it this way.
        if (file.size() < maxSingleShotSize) {
            Flowable<ByteBuffer> data = FlowableUtil.readFile(file);

            data = ProgressReporter.addProgressReporting(data, optionsReal.progressReceiver());

            return blockBlobURL.upload(data, file.size(), optionsReal.httpHeaders(),
                    optionsReal.metadata(), optionsReal.accessConditions(), null)
                    // Transform the specific RestResponse into a CommonRestResponse.
                    .map(CommonRestResponse::createFromPutBlobResponse);
        }

        // Calculate and validate the number of blocks.
        int numBlocks = calculateNumBlocks(file.size(), blockLength);
        if (numBlocks > BlockBlobURL.MAX_BLOCKS) {
            throw new IllegalArgumentException(SR.BLOB_OVER_MAX_BLOCK_LIMIT);
        }

        return Observable.range(0, numBlocks)
                /*
                For each block, make a call to stageBlock as follows. concatMap ensures that the items emitted
                by this Observable are in the same sequence as they are begun, which will be important for composing
                the list of Ids later. Eager ensures parallelism but may require some internal buffering.
                 */
                .concatMapEager(i -> {
                    // The max number of bytes for a block is currently 100MB, so the final result must be an int.
                    int count = (int) Math.min((long) blockLength, (file.size() - i * (long) blockLength));
                    // i * blockLength could be a long, so we need a cast to prevent overflow.
                    Flowable<ByteBuffer> data = FlowableUtil.readFile(file, i * (long) blockLength, count);

                    // Report progress as necessary.
                    data = ProgressReporter.addParallelProgressReporting(data, optionsReal.progressReceiver(),
                                progressLock, totalProgress);

                    final String blockId = Base64.getEncoder().encodeToString(
                            UUID.randomUUID().toString().getBytes(UTF_8));

                    /*
                    Make a call to stageBlock. Instead of emitting the response, which we don't care about other
                    than that it was successful, emit the blockId for this request. These will be collected below.
                    Turn that into an Observable which emits one item to comply with the signature of
                    concatMapEager.
                     */
                    return blockBlobURL.stageBlock(blockId, data,
                            count, optionsReal.accessConditions().leaseAccessConditions(), null)
                            .map(x -> blockId).toObservable();

                    /*
                    Specify the number of concurrent subscribers to this map. This determines how many concurrent
                    rest calls are made. This is so because maxConcurrency is the number of internal subscribers
                    available to subscribe to the Observables emitted by the source. A subscriber is not released
                    for a new subscription until its Observable calls onComplete, which here means that the call to
                    stageBlock is finished. Prefetch is a hint that each of the Observables emitted by the source
                    will emit only one value, which is true here because we have converted from a Single.
                     */
                }, optionsReal.parallelism(), 1)
                /*
                collectInto will gather each of the emitted blockIds into a list. Because we used concatMap, the Ids
                will be emitted according to their block number, which means the list generated here will be
                properly ordered. This also converts into a Single.
                 */
                .collectInto(new ArrayList<String>(numBlocks), ArrayList::add)
                /*
                collectInto will not emit the list until its source calls onComplete. This means that by the time we
                call stageBlock list, all of the stageBlock calls will have finished. By flatMapping the list, we
                can "map" it into a call to commitBlockList.
                */
                .flatMap(ids ->
                        blockBlobURL.commitBlockList(ids, optionsReal.httpHeaders(), optionsReal.metadata(),
                                optionsReal.accessConditions(), null))

                // Finally, we must turn the specific response type into a CommonRestResponse by mapping.
                .map(CommonRestResponse::createFromPutBlockListResponse);
    }

    private static int calculateNumBlocks(long dataSize, long blockLength) {
        // Can successfully cast to an int because MaxBlockSize is an int, which this expression must be less than.
        int numBlocks = toIntExact(dataSize / blockLength);
        // Include an extra block for trailing data.
        if (dataSize % blockLength != 0) {
            numBlocks++;
        }
        return numBlocks;
    }

    /**
     * Downloads a file directly into a file, splitting the download into chunks and parallelizing as necessary.
     *
     * @param file
     *         The destination file to which the blob will be written.
     * @param blobURL
     *         The URL to the blob to download.
     * @param range
     *         {@link BlobRange}
     * @param options
     *         {@link TransferManagerDownloadFromBlobOptions}
     *
     * @return A {@code Completable} that will signal when the download is complete.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=tm_file "Sample code for TransferManager.downloadBlobToFile")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public static Single<BlobDownloadHeaders> downloadBlobToFile(AsynchronousFileChannel file, BlobURL blobURL,
            BlobRange range, TransferManagerDownloadFromBlobOptions options) {
        BlobRange rangeReal = range == null ? new BlobRange() : range;
        TransferManagerDownloadFromBlobOptions optionsReal = options == null ? new TransferManagerDownloadFromBlobOptions() : options;
        Utility.assertNotNull("blobURL", blobURL);
        Utility.assertNotNull("file", file);

        // See ProgressReporter for an explanation on why this lock is necessary and why we use AtomicLong.
        Lock progressLock = new ReentrantLock();
        AtomicLong totalProgress = new AtomicLong(0);

        // Get the size of the data and etag if not specified by the user.
        Single<DownloadHelper> setupSingle = getSetupSingle(blobURL, rangeReal, optionsReal);

        return setupSingle.flatMap(helper -> {
            long newCount = helper.newCount;
            BlobAccessConditions realConditions = helper.realConditions;

            int numChunks = calculateNumBlocks(newCount, optionsReal.chunkSize());

            // In case it is an empty blob, this ensures we still actually perform a download operation.
            numChunks = numChunks == 0 ? 1 : numChunks;

            DownloadResponse initialResponse = helper.initialResponse;
            return Flowable.range(0, numChunks)
                    .flatMapSingle(chunkNum -> {
                        // The first chunk was retrieved during setup.
                        if (chunkNum == 0) {
                            return writeBodyToFile(initialResponse, file, 0, optionsReal, progressLock, totalProgress);
                        }

                        // Calculate whether we need a full chunk or something smaller because we are at the end.
                        long chunkSizeActual = Math.min(optionsReal.chunkSize(),
                                newCount - (chunkNum * optionsReal.chunkSize()));
                        BlobRange chunkRange = new BlobRange().withOffset(
                                rangeReal.offset() + (chunkNum * optionsReal.chunkSize()))
                                .withCount(chunkSizeActual);

                        // Make the download call.
                        return blobURL.download(chunkRange, realConditions, false, null)
                                .flatMap(response ->
                                    writeBodyToFile(response, file, chunkNum, optionsReal, progressLock,
                                            totalProgress));
                    }, false, optionsReal.parallelism())
                    // All the headers will be the same, so we just pick the last one.
                    .lastOrError();
        });
    }

    /*
    Construct a Single which will emit the total count for calculating the number of chunks, access conditions
    containing the etag to lock on, and the response from downloading the first chunk.
     */
    private static Single<DownloadHelper> getSetupSingle(BlobURL blobURL, BlobRange r,
            TransferManagerDownloadFromBlobOptions o) {
        // We will scope our initial download to either be one chunk or the total size.
        long initialChunkSize = r.count() != null && r.count() < o.chunkSize() ? r.count() : o.chunkSize();

        return blobURL.download(new BlobRange().withOffset(r.offset()).withCount(initialChunkSize),
                o.accessConditions(), false, null)
                .map(response -> {
                    /*
                    Either the etag was set and it matches because the download succeed, so this is a no-op, or there
                    was no etag, so we set it here.
                     */
                    BlobAccessConditions newConditions = setEtag(o.accessConditions(), response.headers().eTag());

                    /*
                    If the user either didn't specify a count or they specified a count greater than the size of the
                    remaining data, take the size of the remaining data. This is to prevent the case where the count
                    is much much larger than the size of the blob and we could try to download at an invalid offset.
                     */
                    long newCount;
                    // Extract the total length of the blob from the contentRange header. e.g. "bytes 1-6/7"
                    long totalLength = extractTotalBlobLength(response.headers().contentRange());
                    if (r.count() == null || r.count() > (totalLength - r.offset())) {
                        newCount = totalLength - r.offset();
                    } else {
                        newCount = r.count();
                    }
                    return new DownloadHelper(newCount, newConditions, response);
                })
                .onErrorResumeNext(throwable -> {
                    /*
                    In the case of an empty blob, we still want to report successful download to file and give back
                    valid DownloadResponseHeaders. Attempting a range download on an empty blob will return an
                    InvalidRange error code and a Content-Range header of the format "bytes * /0".
                    We need to double check that the total size is zero in the case that the customer has attempted an
                    invalid range on a non-zero length blob.
                     */
                    if (throwable instanceof StorageException
                            && ((StorageException) throwable).errorCode() == StorageErrorCode.INVALID_RANGE
                            && extractTotalBlobLength(((StorageException) throwable).response()
                                    .headers().value("Content-Range")) == 0) {
                        return blobURL.download(new BlobRange().withOffset(0).withCount(0L), o.accessConditions(),
                                false, null)
                                .map(response -> {
                                    /*
                                    Ensure the blob is still 0 length by checking our download was the full length.
                                    (200 is for full blob; 206 is partial).
                                     */
                                    if (response.statusCode() != 200) {
                                        throw new IllegalStateException("Blob was modified mid download. It was "
                                            + "originally 0 bytes and is now larger.");
                                    }
                                    return new DownloadHelper(0L, o.accessConditions(), response);
                                });
                    }
                    return Single.error(throwable);
                });
    }

    private static BlobAccessConditions setEtag(BlobAccessConditions accessConditions, String etag) {
        /*
        We don't want to modify the user's object, so we'll create a duplicate and set the
        retrieved etag.
         */
        return new BlobAccessConditions()
                .withModifiedAccessConditions(new ModifiedAccessConditions()
                        .withIfModifiedSince(
                                accessConditions.modifiedAccessConditions().ifModifiedSince())
                        .withIfUnmodifiedSince(
                                accessConditions.modifiedAccessConditions().ifUnmodifiedSince())
                        .withIfMatch(etag)
                        .withIfNoneMatch(
                                accessConditions.modifiedAccessConditions().ifNoneMatch()))
                .withLeaseAccessConditions(accessConditions.leaseAccessConditions());
    }

    private static Single<BlobDownloadHeaders> writeBodyToFile(DownloadResponse response,
            AsynchronousFileChannel file, long chunkNum, TransferManagerDownloadFromBlobOptions optionsReal,
            Lock progressLock, AtomicLong totalProgress) {

        // Extract the body.
        Flowable<ByteBuffer> data = response.body(
                optionsReal.reliableDownloadOptionsPerBlock());

        // Report progress as necessary.
        data = ProgressReporter.addParallelProgressReporting(data,
                optionsReal.progressReceiver(), progressLock, totalProgress);

        // Write to the file.
        return FlowableUtil.writeFile(data, file,
                chunkNum * optionsReal.chunkSize())
                /*
                Satisfy the return type. Observable required for flatmap to accept
                maxConcurrency. We want to eventually give the user back the headers.
                 */
                .andThen(Single.just(response.headers()));
    }

    private static long extractTotalBlobLength(String contentRange) {
        return Long.parseLong(contentRange.split("/")[1]);
    }

    private static final class DownloadHelper {
        final long newCount;

        final BlobAccessConditions realConditions;

        final DownloadResponse initialResponse;

        DownloadHelper(long newCount, BlobAccessConditions realConditions, DownloadResponse initialResponse) {
            this.newCount = newCount;
            this.realConditions = realConditions;
            this.initialResponse = initialResponse;
        }
    }

    /**
     * Uploads the contents of an arbitrary {@code Flowable} to a block blob. This Flowable need not be replayable and
     * therefore it may have as its source a network stream or any other data for which the replay behavior is unknown
     * (non-replayable meaning the Flowable may not return the exact same data on each subscription).
     *
     * To eliminate the need for replayability on the source, the client must perform some buffering in order to ensure
     * the actual data passed to the network is replayable. This is important in order to support retries, which are
     * crucial for reliable data transfer. Typically, the greater the number of buffers used, the greater the possible
     * parallelism. Larger buffers means we will have to stage fewer blocks. The tradeoffs between these values are
     * context-dependent, so some experimentation may be required to optimize inputs for a given scenario.
     *
     * Note that buffering must be strictly sequential. Only the upload portion of this operation may be parallelized;
     * the reads cannot be. Therefore, this method is not as optimal as
     * {@link #uploadFileToBlockBlob(AsynchronousFileChannel, BlockBlobURL, int, Integer, TransferManagerUploadToBlockBlobOptions)}
     * and if the source is known to be a file, that method should be preferred.
     *
     * @param source
     *         Contains the data to upload. Unlike other upload methods in this library, this method does not require
     *         that the Flowable be replayable.
     * @param blockBlobURL
     *         Points to the blob to which the data should be uploaded.
     * @param blockSize
     *         The size of each block that will be staged. This value also determines the size that each buffer used by
     *         this method will be and determines the number of requests that need to be made. The amount of memory
     *         consumed by this method may be up to blockSize * numBuffers. If block size is large, this method will
     *         make fewer network calls, but each individual call will send more data and will therefore take longer.
     * @param numBuffers
     *         The maximum number of buffers this method should allocate. Must be at least two. Generally this value
     *         should have some relationship to the value for parallelism passed via the options. If the number of
     *         available buffers is smaller than the level of parallelism, then this method will not be able to make
     *         full use of the available parallelism. It is unlikely that the value need be more than two times the
     *         level of parallelism as such a value means that (assuming buffering is fast enough) there are enough
     *         available buffers to have both one occupied for each worker and one ready for all workers should they
     *         all complete the current request at approximately the same time. The amount of memory consumed by this
     *         method may be up to blockSize * numBuffers.
     * @param options
     *         {@link TransferManagerUploadToBlockBlobOptions}
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=tm_nrf "Sample code for TransferManager.uploadFromNonReplayableFlowable")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public static Single<BlockBlobCommitBlockListResponse> uploadFromNonReplayableFlowable(
            final Flowable<ByteBuffer> source, final BlockBlobURL blockBlobURL, final int blockSize,
            final int numBuffers, final TransferManagerUploadToBlockBlobOptions options) {
        Utility.assertNotNull("source", source);
        Utility.assertNotNull("blockBlobURL", blockBlobURL);

        TransferManagerUploadToBlockBlobOptions optionsReal = options == null
                ? new TransferManagerUploadToBlockBlobOptions() : options;

        // See ProgressReporter for an explanation on why this lock is necessary and why we use AtomicLong.
        AtomicLong totalProgress = new AtomicLong(0);
        Lock progressLock = new ReentrantLock();

        // Validation done in the constructor.
        UploadFromNRFBufferPool pool = new UploadFromNRFBufferPool(numBuffers, blockSize);

        /*
        Break the source flowable into chunks that are <= chunk size. This makes filling the pooled buffers much easier
        as we can guarantee we only need at most two buffers for any call to write (two in the case of one pool buffer
        filling up with more data to write)
         */
        Flowable<ByteBuffer> chunkedSource = source.flatMap(buffer -> {
            if (buffer.remaining() <= blockSize) {
                return Flowable.just(buffer);
            }
            List<ByteBuffer> smallerChunks = new ArrayList<>();
            for (int i = 0; i < Math.ceil(buffer.remaining() / (double) blockSize); i++) {
                // Note that duplicate does not duplicate data. It simply creates a duplicate view of the data.
                ByteBuffer duplicate = buffer.duplicate();
                duplicate.position(i * blockSize);
                duplicate.limit(Math.min(duplicate.limit(), (i + 1) * blockSize));
                smallerChunks.add(duplicate);
            }
            return Flowable.fromIterable(smallerChunks);
        }, false, 1);

        /*
        Write each buffer from the chunkedSource to the pool and call flush at the end to get the last bits.
         */
        return chunkedSource.flatMap(pool::write, false, 1)
                .concatWith(Flowable.defer(pool::flush))
                .concatMapEager(buffer -> {
                    // Report progress as necessary.
                    Flowable<ByteBuffer> data = ProgressReporter.addParallelProgressReporting(Flowable.just(buffer),
                            optionsReal.progressReceiver(), progressLock, totalProgress);

                    final String blockId = Base64.getEncoder().encodeToString(
                            UUID.randomUUID().toString().getBytes(UTF_8));

                    /*
                    Make a call to stageBlock. Instead of emitting the response, which we don't care about other
                    than that it was successful, emit the blockId for this request. These will be collected below.
                    Turn that into an Observable which emits one item to comply with the signature of
                    concatMapEager.
                     */
                    return blockBlobURL.stageBlock(blockId, data,
                            buffer.remaining(), optionsReal.accessConditions().leaseAccessConditions(), null)
                            .map(x -> {
                                pool.returnBuffer(buffer);
                                return blockId;
                            }).toFlowable();

                    /*
                    Specify the number of concurrent subscribers to this map. This determines how many concurrent
                    rest calls are made. This is so because maxConcurrency is the number of internal subscribers
                    available to subscribe to the Observables emitted by the source. A subscriber is not released
                    for a new subscription until its Observable calls onComplete, which here means that the call to
                    stageBlock is finished. Prefetch is a hint that each of the Observables emitted by the source
                    will emit only one value, which is true here because we have converted from a Single.
                     */
                }, optionsReal.parallelism(), 1)
                /*
                collectInto will gather each of the emitted blockIds into a list. Because we used concatMap, the Ids
                will be emitted according to their block number, which means the list generated here will be
                properly ordered. This also converts into a Single.
                 */
                .collectInto(new ArrayList<String>(), ArrayList::add)
                /*
                collectInto will not emit the list until its source calls onComplete. This means that by the time we
                call stageBlock list, all of the stageBlock calls will have finished. By flatMapping the list, we
                can "map" it into a call to commitBlockList.
                */
                .flatMap(ids ->
                        blockBlobURL.commitBlockList(ids, optionsReal.httpHeaders(), optionsReal.metadata(),
                                optionsReal.accessConditions(), null));

    }
}
