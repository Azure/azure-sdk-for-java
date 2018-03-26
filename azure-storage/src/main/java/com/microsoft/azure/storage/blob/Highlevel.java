package com.microsoft.azure.storage.blob;

import io.reactivex.*;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Function;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Base64;
import java.util.UUID;

/**
 * This class contains a collection of methods (and structures associated with those methods) which perform higher-level
 * operations. Whereas operations on the URL types guarantee a single REST request and make no assumptions on desired
 * behavior, these methods will often compose several requests to provide a convenient way of performing more complex
 * operations. Further, we will make our own assumptions and optimizations for common cases that may not be ideal for
 * rarer cases.
 */
public class Highlevel {

    public static class UploadToBlockBlobOptions {

        /**
         * An object which represents the default parallel upload options. progressReceiver=null. httpHeaders, metadata,
         * and accessConditions are default values. parallelism=5.
         */
        public static final UploadToBlockBlobOptions DEFAULT = new UploadToBlockBlobOptions(null,
                null, null, null, null);

        private IProgressReceiver progressReceiver;

        private BlobHTTPHeaders httpHeaders;

        private Metadata metadata;

        private BlobAccessConditions accessConditions;

        private int parallelism;

        /**
         * Creates a new object that configures the parallel upload behavior.
         *
         * @param progressReceiver
         *      An object that implements the {@link IProgressReceiver} interface which will be invoked periodically as
         *      bytes are sent in a PutBlock call to the BlockBlobURL.
         * @param httpHeaders
         *      {@link BlobHTTPHeaders}
         * @param metadata
         *      {@link Metadata}
         * @param accessConditions
         *      {@link BlobAccessConditions}
         * @param parallelism
         *      A {@code int} that indicates the maximum number of blocks to upload in parallel. Must be greater than 0.
         *      The default is 5 (null=default).
         */
        public UploadToBlockBlobOptions(IProgressReceiver progressReceiver, BlobHTTPHeaders httpHeaders,
                                        Metadata metadata, BlobAccessConditions accessConditions, Integer parallelism) {
            if (parallelism == null) {
                this.parallelism = 5;
            }
            else if (parallelism <= 0) {
                throw new IllegalArgumentException("Parallelism must be > 0");
            } else {
                this.parallelism = parallelism;
            }

            this.progressReceiver = progressReceiver;
            this.httpHeaders = httpHeaders;
            this.metadata = metadata;
            this.accessConditions = accessConditions == null ? BlobAccessConditions.NONE : accessConditions;
        }
    }

    /**
     * Uploads the contents of a file to a block blob in parallel, breaking it into block-size chunks if necessary.
     *
     * @param file
     *      The file to upload.
     * @param blockBlobURL
     *      Points to the blob to which the data should be uploaded.
     * @param blockLength
     *      If the data must be broken up into blocks, this value determines what size those blocks will be. This will
     *      affect the total number of service requests made. This value will be ignored if the data can be uploaded in
     *      a single put-blob operation.
     * @param options
     *      {@link UploadToBlockBlobOptions}
     * @return
     *      Emits the successful response.
     */
    public static Single<CommonRestResponse> uploadFileToBlockBlob(
            final FileChannel file, final BlockBlobURL blockBlobURL, final int blockLength,
            final UploadToBlockBlobOptions options) {
        Utility.assertNotNull("file", file);
        Utility.assertNotNull("blockBlobURL", blockBlobURL);
        Utility.assertNotNull("options", options);
        Utility.assertInBounds("blockLength", blockLength, 1, BlockBlobURL.MAX_PUT_BLOCK_BYTES);

        try {
            // If the size of the file can fit in a single upload, do it this way.
            if (file.size() < BlockBlobURL.MAX_PUT_BLOB_BYTES) {
                return doSingleShotUpload(
                        Flowable.just(file.map(FileChannel.MapMode.READ_ONLY, 0, file.size())), file.size(),
                        blockBlobURL, options);
            }
            // Can successfully cast to an int because MaxBlockSize is an int, which this expression must be less than.
            int numBlocks = (int)(file.size()/blockLength);
            return Observable.range(0, numBlocks)
                    .map((Function<Integer, ByteBuffer>) i -> {
                        /*
                        The docs say that the result of mapping a region which is not entirely contained by the file
                        is undefined, so we must be precise with the last block size.
                         */
                        int count = Math.min(blockLength, (int)(file.size()-i*blockLength));
                        // Memory map the file to get a ByteBuffer to an in memory portion of the file.
                        return file.map(FileChannel.MapMode.READ_ONLY, i*blockLength, count);
                    })
                    // Gather all of the buffers, in order, into this list, which will become the block list.
                    .collectInto(new ArrayList<>(numBlocks),
                            (BiConsumer<ArrayList<ByteBuffer>, ByteBuffer>) ArrayList::add)
                    // Turn the list into a call to uploadByteBuffersToBlockBlob and return that result.
                    .flatMap((Function<ArrayList<ByteBuffer>, SingleSource<CommonRestResponse>>) blocks ->
                            uploadByteBuffersToBlockBlob(blocks, blockBlobURL, options));
        }
        catch (IOException e) {
            throw new Error(e);
        }
    }

    /**
     * Uploads a large ByteBuffer to a block blob in parallel, breaking it up into block-size chunks if necessary.
     *
     * @param data
     *      The buffer to upload.
     * @param blockBlobURL
     *      A {@link BlockBlobURL} that points to the blob to which the data should be uploaded.
     * @param blockLength
     *      If the data must be broken up into blocks, this value determines what size those blocks will be. This will
     *      affect the total number of service requests made. This value will be ignored if the data can be uploaded in
     *      a single put-blob operation.
     * @param options
     *      {@link UploadToBlockBlobOptions}
     * @return
     *      Emits the successful response.
     */
    public static Single<CommonRestResponse> uploadByteBufferToBlockBlob(
            final ByteBuffer data, final BlockBlobURL blockBlobURL, final int blockLength,
            final UploadToBlockBlobOptions options) {
        Utility.assertNotNull("data", data);
        Utility.assertNotNull("blockBlobURL", blockBlobURL);
        Utility.assertNotNull("options", options);
        Utility.assertInBounds("blockLength", blockLength, 1, BlockBlobURL.MAX_PUT_BLOCK_BYTES);

        // If the size of the buffer can fit in a single upload, do it this way.
        if (data.remaining() < BlockBlobURL.MAX_PUT_BLOB_BYTES) {
            return doSingleShotUpload(Flowable.just(data), data.remaining(), blockBlobURL, options);
        }

        int numBlocks = data.remaining()/blockLength;
        return Observable.range(0, numBlocks)
                .map(i -> {
                    int count = Math.min(blockLength, data.remaining()-i*blockLength);
                    ByteBuffer block = data.duplicate();
                    block.position(i*blockLength);
                    block.limit(i*blockLength+count);
                    return block;
                })
                .collectInto(new ArrayList<>(numBlocks),
                        (BiConsumer<ArrayList<ByteBuffer>, ByteBuffer>) ArrayList::add)
                .flatMap(blocks -> uploadByteBuffersToBlockBlob(blocks, blockBlobURL, options));
    }

    /**
     * Uploads an iterable of {@code ByteBuffers} to a block blob. The data will first data will first be examined to
     * check the size and validate the number of blocks. If the total amount of data in all the buffers is small enough,
     * this method will perform a single upload operation. Otherwise, each {@code ByteBuffer} in the iterable is
     * assumed to be its own discreet block of data for the block blob and will be uploaded as such.
     *
     * @param data
     *      The data to upload.
     * @param blockBlobURL
     *      A {@link BlockBlobURL} that points to the blob to which the data should be uploaded.
     * @param options
     *      {@link UploadToBlockBlobOptions}
     * @return
     *      Emits the successful response.
     */
    public static Single<CommonRestResponse> uploadByteBuffersToBlockBlob(
            final Iterable<ByteBuffer> data, final BlockBlobURL blockBlobURL,
            final UploadToBlockBlobOptions options) {
        Utility.assertNotNull("data", data);
        Utility.assertNotNull("blockBlobURL", blockBlobURL);
        Utility.assertNotNull("options", options);

        // Determine the size of the blob and the number of blocks
        long size = 0;
        int numBlocks = 0;
        for (ByteBuffer b : data) {
            size += b.remaining();
            numBlocks++;
        }

        // If the size can fit in 1 upload call, do it this way.
        if (size <= BlockBlobURL.MAX_PUT_BLOB_BYTES) {
            return doSingleShotUpload(Flowable.fromIterable(data), size, blockBlobURL, options);
        }

        if (numBlocks > BlockBlobURL.MAX_BLOCKS) {
            throw new IllegalArgumentException(SR.BLOB_OVER_MAX_BLOCK_LIMIT);
        }

        // TODO: context with cancel?

        // Generate an observable that emits items which are the ByteBuffers in the provided Iterable.
        return Observable.fromIterable(data)
                /*
                 For each ByteBuffer, make a call to stageBlock as follows. concatMap ensures that the items
                 emitted by this Observable are in the same sequence as they are begun, which will be important for
                 composing the list of Ids later.
                 */
                .concatMapEager(blockData -> {
                    if (blockData.remaining() > Constants.MAX_BLOCK_SIZE) {
                        throw new IllegalArgumentException(SR.INVALID_BLOCK_SIZE);
                    }

                    // TODO: progress

                    final String blockId = Base64.getEncoder().encodeToString(
                            UUID.randomUUID().toString().getBytes());


                    // TODO: What happens if one of the calls fails? It seems like this single/observable
                    // will emit an error, which will halt the collecting into a list. Will the list still
                    // be emitted or will it emit an error? In the latter, it'll just propagate. In the former,
                    // we should check the size of the blockList equals numBlocks before sending it up.

                    /*
                     Make a call to stageBlock. Instead of emitting the response, which we don't care about other than
                     that it was successful, emit the blockId for this request. These will be collected below. Turn that
                     into an Observable which emits one item to comply with the signature of concatMapEager.
                     */
                    return blockBlobURL.stageBlock(blockId, Flowable.just(blockData), blockData.remaining(),
                            options.accessConditions.getLeaseAccessConditions())
                            .map(x -> blockId).toObservable();

                /*
                 Specify the number of concurrent subscribers to this map. This determines how many concurrent rest
                 calls are made. This is so because maxConcurrency is the number of internal subscribers available to
                 subscribe to the Observables emitted by the source. A subscriber is not released for a new subscription
                 until its Observable calls onComplete, which here means that the call to stageBlock is finished. Prefetch
                 is a hint that each of the Observables emitted by the source will emit only one value, which is true
                 here because we have converted from a Single.
                 */

                }, options.parallelism, 1)
                /*
                collectInto will gather each of the emitted blockIds into a list. Because we used concatMap, the Ids
                will be emitted according to their block number, which means the list generated here will be properly
                ordered. This also converts into a Single.
                */
                .collectInto(new ArrayList<>(numBlocks), (BiConsumer<ArrayList<String>, String>) ArrayList::add)
                /*
                collectInto will not emit the list until its source calls onComplete. This means that by the time we
                call stageBlock list, all of the stageBlock calls will have finished. By flatMapping the list, we can
                "map" it into a call to commitBlockList.
                 */
                .flatMap( ids ->
                        blockBlobURL.commitBlockList(ids, options.httpHeaders, options.metadata, options.accessConditions))
                /*
                Finally, we must turn the specific response type into a CommonRestResponse by mapping.
                 */
                .map(CommonRestResponse::createFromPutBlockListResponse);

    }

    private static Single<CommonRestResponse> doSingleShotUpload(
            Flowable<ByteBuffer> data, long size, BlockBlobURL blockBlobURL, UploadToBlockBlobOptions options) {
        if (options.progressReceiver != null) {
            // TODO: Wrap in a progress stream once progress is written.
        }

        // Transform the specific RestResponse into a CommonRestResponse.
        return blockBlobURL.upload(data, size, options.httpHeaders,
                options.metadata, options.accessConditions)
                .map(CommonRestResponse::createFromPutBlobResponse);
    }
}
