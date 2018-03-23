package com.microsoft.azure.storage.blob;

import com.microsoft.azure.storage.models.BlobPutHeaders;
import com.microsoft.azure.storage.models.BlockBlobPutBlockHeaders;
import com.microsoft.azure.storage.models.BlockBlobPutBlockListHeaders;
import com.microsoft.rest.v2.RestResponse;
import io.reactivex.*;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Function;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.UUID;

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
         *      A {@link BlobHTTPHeaders} to be associated with the blob when PutBlockList is called.
         * @param metadata
         *      A {@link Metadata} object to be associated with the blob when PutBlockList is called.
         * @param accessConditions
         *      A {@link BlobAccessConditions} object that indicate the access conditions for the block blob.
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
     *      A {@link BlockBlobURL} that points to the blob to which the data should be uploaded.
     * @param blockLength
     *      If the data must be broken up into blocks, this value determines what size those blocks will be. This will
     *      affect the total number of service requests made. This value will be ignored if the data can be uploaded in
     *      a single put-blob operation.
     * @param options
     *      A {@link UploadToBlockBlobOptions} object to configure the upload behavior.
     * @return
     *      A {@link Single} that will return a {@link CommonRestResponse} if successful.
     */
    public static Single<CommonRestResponse> uploadFileToBlockBlob(
            final FileChannel file, final BlockBlobURL blockBlobURL, final int blockLength,
            final UploadToBlockBlobOptions options) {
        Utility.assertNotNull("file", file);
        Utility.assertNotNull("blockBlobURL", blockBlobURL);
        Utility.assertNotNull("options", options);
        Utility.assertInBounds("blockLength", blockLength, 1, BlockBlobURL.MAX_PUT_BLOCK_BYTES);

        try {
            // If the size of the file can fit in a single putBlob, do it this way.
            if (file.size() < BlockBlobURL.MAX_PUT_BLOB_BYTES) {
                return doSingleShotUpload(file.map(FileChannel.MapMode.READ_ONLY, 0, file.size()), blockBlobURL,
                        options);
            }
            // Can successfully cast to an int because MaxBlockSize is an int, which this expression must be less than.
            int numBlocks = (int)(file.size()/blockLength);
            return Observable.range(0, numBlocks)
                    .map(new Function<Integer, ByteBuffer>() {
                        @Override
                        public ByteBuffer apply(Integer i) throws Exception {
                            /*
                            The docs say that the result of mapping a region which is not entirely contained by the file
                            is undefined, so we must be precise with the last block size.
                             */
                            int count = Math.min(blockLength, (int)(file.size()-i*blockLength));
                            // Memory map the file to get a ByteBuffer to an in memory portion of the file.
                            return file.map(FileChannel.MapMode.READ_ONLY, i*blockLength, count);
                        }
                    })
                    // Gather all of the buffers, in order, into this list, which will become the block list.
                    .collectInto(new ArrayList<ByteBuffer>(numBlocks),
                            new BiConsumer<ArrayList<ByteBuffer>, ByteBuffer>() {
                        @Override
                        public void accept(ArrayList<ByteBuffer> blocks, ByteBuffer block) throws Exception {
                            blocks.add(block);
                        }
                    })
                    // Turn the list into a call to uploadByteBuffersToBlockBlob and return that result.
                    .flatMap(new Function<ArrayList<ByteBuffer>, SingleSource<CommonRestResponse>>() {
                        @Override
                        public SingleSource<CommonRestResponse> apply(ArrayList<ByteBuffer> blocks) throws Exception {
                            return uploadByteBuffersToBlockBlob(blocks, blockBlobURL, options);
                        }
                    });
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
     *      A {@link UploadToBlockBlobOptions} object to configure the upload behavior.
     * @return
     *      A {@link Single} that will return a {@link CommonRestResponse} if successful.
     */
    public static Single<CommonRestResponse> uploadByteBufferToBlockBlob(
            final ByteBuffer data, final BlockBlobURL blockBlobURL, final int blockLength,
            final UploadToBlockBlobOptions options) {
        Utility.assertNotNull("data", data);
        Utility.assertNotNull("blockBlobURL", blockBlobURL);
        Utility.assertNotNull("options", options);
        Utility.assertInBounds("blockLength", blockLength, 1, BlockBlobURL.MAX_PUT_BLOCK_BYTES);

        // If the size of the buffer can fit in a single putBlob, do it this way.
        if (data.remaining() < BlockBlobURL.MAX_PUT_BLOB_BYTES) {
            //return doSingleShotUpload(data, blockBlobURL, options);
        }

        int numBlocks = data.remaining()/blockLength;
        return Observable.range(0, numBlocks)
                .map(new Function<Integer, ByteBuffer>() {
                    @Override
                    public ByteBuffer apply(Integer i) throws Exception {
                        int count = Math.min(blockLength, data.remaining()-i*blockLength);
                        ByteBuffer block = data.duplicate();
                        block.position(i*blockLength);
                        block.limit(i*blockLength+count);
                        return block;
                    }
                })
                .collectInto(new ArrayList<ByteBuffer>(numBlocks),
                        new BiConsumer<ArrayList<ByteBuffer>, ByteBuffer>() {
                            @Override
                            public void accept(ArrayList<ByteBuffer> blocks, ByteBuffer block)
                                    throws Exception {
                                blocks.add(block);
                            }
                        })
                .flatMap(new Function<ArrayList<ByteBuffer>, SingleSource<? extends CommonRestResponse>>() {
                    @Override
                    public SingleSource<CommonRestResponse> apply(ArrayList<ByteBuffer> blocks) throws Exception {
                        return uploadByteBuffersToBlockBlob(blocks, blockBlobURL, options);
                    }
                });
    }

    /**
     * Uploads an iterable of ByteBuffers to a block blob.
     *
     * @param data
     *      A {@code Iterable} of {@link ByteBuffer} that contains the data to upload.
     * @param blockBlobURL
     *      A {@link BlockBlobURL} that points to the blob to which the data should be uploaded.
     * @param options
     *      A {@link UploadToBlockBlobOptions} object to configure the upload behavior.
     * @return
     *      A {@link Single} that will return a {@link CommonRestResponse} if successful.
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

        // If the size can fit in 1 putBlob call, do it this way.
        if (numBlocks == 1 && size <= BlockBlobURL.MAX_PUT_BLOB_BYTES) {
            return doSingleShotUpload(data.iterator().next(), blockBlobURL, options);
        }

        if (numBlocks > BlockBlobURL.MAX_BLOCKS) {
            throw new IllegalArgumentException(SR.BLOB_OVER_MAX_BLOCK_LIMIT);
        }

        // TODO: context with cancel?

        // Generate a flowable that emits items which are the ByteBuffers in the provided Iterable.
        return Observable.fromIterable(data)
                /*
                 For each ByteBuffer, make a call to putBlock as follows. concatMap ensures that the items
                 emitted by this Observable are in the same sequence as they are begun, which will be important for
                 composing the list of Ids later.
                 */
                .concatMapEager(new Function<ByteBuffer, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(final ByteBuffer blockData) throws Exception {
                        if (blockData.remaining() > Constants.MAX_BLOCK_SIZE) {
                            throw new IllegalArgumentException(SR.INVALID_BLOCK_SIZE);
                        }

                        // TODO: progress

                        final String blockId = DatatypeConverter.printBase64Binary(
                                UUID.randomUUID().toString().getBytes());

                        // TODO: What happens if one of the calls fails? It seems like this single/observable
                        // will emit an error, which will halt the collecting into a list. Will the list still
                        // be emitted or will it emit an error? In the latter, it'll just propogate. In the former,
                        // we should check the size of the blockList equals numBlocks before sending it up.

                        /*
                         Make a call to putBlock. Instead of emitting the RestResponse, which we don't care about,
                         emit the blockId for this request. These will be collected below. Turn that into an Observable
                         which emits one item to comply with the signature of concatMapEager.
                         */
                        return blockBlobURL.putBlock(blockId, Flowable.just(blockData), blockData.remaining(),
                                options.accessConditions.getLeaseAccessConditions())
                                .map(new Function<RestResponse<BlockBlobPutBlockHeaders,Void>, String>() {
                                    @Override
                                    public String apply(RestResponse<BlockBlobPutBlockHeaders, Void> x) throws Exception {
                                        return blockId;
                                    }
                                }).toObservable();

                /*
                 Specify the number of concurrent subscribers to this map. This determines how many concurrent rest
                 calls are made. This is so because maxConcurrency is the number of internal subscribers available to
                 subscribe to the Observables emitted by the source. A subscriber is not released for a new subscription
                 until its Observable calls onComplete, which here means that the call to putBlock is finished. Prefetch
                 is a hint that each of the Observables emitted by the source will emit only one value, which is true
                 here because we have converted from a Single.
                 */

                    }
                }, options.parallelism, 1)
                /*
                collectInto will gather each of the emitted blockIds into a list. Because we used concatMap, the Ids
                will be emitted according to their block number, which means the list generated here will be properly
                ordered. This also converts into a Single.
                */
                .collectInto(new ArrayList<String>(numBlocks), new BiConsumer<ArrayList<String>, String>() {
                    @Override
                    public void accept(ArrayList<String> ids, String id) throws Exception {
                        ids.add(id);
                    }
                })
                /*
                collectInto will not emit the list until its source calls onComplete. This means that by the time we
                call putBlock list, all of the putBlock calls will have finished. By flatMapping the list, we can
                "map" it into a call to putBlockList.
                 */
                .flatMap(new Function<ArrayList<String>, SingleSource<RestResponse<BlockBlobPutBlockListHeaders, Void>>>() {
                    public SingleSource<RestResponse<BlockBlobPutBlockListHeaders, Void>> apply(ArrayList<String> ids) throws Exception {
                        return blockBlobURL.putBlockList(ids, options.httpHeaders, options.metadata,
                                options.accessConditions);
                    }
                })
                /*
                Finally, we must turn the specific response type into a CommonRestResponse by mapping.
                 */
                .map(new Function<RestResponse<BlockBlobPutBlockListHeaders, Void>, CommonRestResponse>() {
                    @Override
                    public CommonRestResponse apply(RestResponse<BlockBlobPutBlockListHeaders, Void> response) throws Exception {
                        return CommonRestResponse.createFromPutBlockListResponse(response);
                    }
                });


        /*
         * Should take in a ByteBuffer.
         * Get FileChannel from FileInputStream and call map to get MappedByteBuffer.
         * Duplicate/slice the buffer for each network call (backed by the same data)
         * Set the position and limit on the new buffer (independent per buffer object).
         * Can convert to flowable by get()-ing some relative section of the array or the whole thing. This will read
         * those bytes into memory. Create using Flowable.just(byte[]).
         *
         */
    }

    private static Single<CommonRestResponse> doSingleShotUpload(
            ByteBuffer data, BlockBlobURL blockBlobURL, UploadToBlockBlobOptions options) {
        if (options.progressReceiver != null) {
            // TODO: Wrap in a progress stream once progress is written.
        }

        return blockBlobURL.putBlob(Flowable.just(data), data.remaining(), options.httpHeaders,
                options.metadata, options.accessConditions)
                .map(new Function<RestResponse<BlobPutHeaders, Void>, CommonRestResponse>() {
                    // Transform the specific RestResponse into a CommonRestResponse.
                    @Override
                    public CommonRestResponse apply(
                            RestResponse<BlobPutHeaders, Void> response) throws Exception {
                        return CommonRestResponse.createFromPutBlobResponse(response);
                    }
                });
    }
}
