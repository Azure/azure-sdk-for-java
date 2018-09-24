/*
 * Copyright Microsoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.azure.storage.blob;

import com.microsoft.azure.storage.blob.models.BlobDownloadHeaders;
import com.microsoft.azure.storage.blob.models.ModifiedAccessConditions;
import com.microsoft.rest.v2.util.FlowableUtil;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import javafx.util.Pair;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.util.ArrayList;
import java.util.Base64;
import java.util.UUID;

import static java.lang.StrictMath.toIntExact;

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
     * @param options
     *         {@link TransferManagerUploadToBlockBlobOptions}
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=tm_file "Sample code for TransferManager.uploadFileToBlockBlob")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public static Single<CommonRestResponse> uploadFileToBlockBlob(
            final AsynchronousFileChannel file, final BlockBlobURL blockBlobURL, final int blockLength,
            final TransferManagerUploadToBlockBlobOptions options) throws IOException {
        Utility.assertNotNull("file", file);
        Utility.assertNotNull("blockBlobURL", blockBlobURL);
        Utility.assertInBounds("blockLength", blockLength, 1, BlockBlobURL.MAX_STAGE_BLOCK_BYTES);
        TransferManagerUploadToBlockBlobOptions optionsReal = options == null ?
                TransferManagerUploadToBlockBlobOptions.DEFAULT : options;


        // If the size of the file can fit in a single upload, do it this way.
        if (file.size() < BlockBlobURL.MAX_UPLOAD_BLOB_BYTES) {

            // Transform the specific RestResponse into a CommonRestResponse.
            return blockBlobURL.upload(FlowableUtil.readFile(file), file.size(), optionsReal.httpHeaders(),
                    optionsReal.metadata(), optionsReal.accessConditions(), null)
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
                    int count = Math.min(blockLength, (int) (file.size() - i * blockLength));
                    Flowable<ByteBuffer> data = FlowableUtil.readFile(file, i * blockLength, count);

                    final String blockId = Base64.getEncoder().encodeToString(
                            UUID.randomUUID().toString().getBytes());

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
        BlobRange r = range == null ? BlobRange.DEFAULT : range;
        TransferManagerDownloadFromBlobOptions o = options == null ?
                TransferManagerDownloadFromBlobOptions.DEFAULT : options;
        Utility.assertNotNull("blobURL", blobURL);
        Utility.assertNotNull("file", file);

        // Get the size of the data and etag if not specified by the user.
        Single<Pair<Long, BlobAccessConditions>> setupSingle = getSetupSingle(blobURL, r, o);

        return setupSingle.flatMap(setupPair -> {
            Long dataSize = setupPair.getKey();
            BlobAccessConditions realConditions = setupPair.getValue();

            int numChunks = calculateNumBlocks(dataSize, o.chunkSize());

            // In case it is an empty blob, this ensures we still actually perform a download operation.
            numChunks = numChunks == 0 ? 1 : numChunks;

            return Observable.range(0, numChunks)
                    .flatMap(i -> {
                        // Calculate whether we need a full chunk or something smaller because we are at the end.
                        long chunkSizeActual = Math.min(o.chunkSize(),
                                dataSize - (i * o.chunkSize()));
                        BlobRange chunkRange = new BlobRange().withOffset(r.offset() + (i * o.chunkSize()))
                                .withCount(chunkSizeActual);


                        // Make the download call.
                        return blobURL.download(chunkRange, realConditions, false, null)
                                // Extract the body.
                                .flatMapObservable(response ->
                                        // Write to the file.
                                        FlowableUtil.writeFile(response.body(o.reliableDownloadOptionsPerBlock()), file,
                                                i * o.chunkSize())
                                                /*
                                                Satisfy the return type. Observable required for flatmap to accept
                                                maxConcurrency. We want to eventually give the user back the headers.
                                                 */
                                                .andThen(Single.just(response.headers()))
                                                .toObservable());
                    }, o.parallelism())
                    // All the headers will be the same, so we just pick the last one.
                    .lastOrError();
        });
    }


    private static Single<Pair<Long, BlobAccessConditions>> getSetupSingle(BlobURL blobURL, BlobRange r,
            TransferManagerDownloadFromBlobOptions o) {
        /*
        Construct a Single which will emit the total count of bytes to be downloaded and retrieve an etag to lock on to
        if one was not specified. We use a single for this because we may have to make a REST call to get the length to
        calculate the count and we need to maintain asynchronicity.
         */
        if (r.count() == null || o.accessConditions().modifiedAccessConditions().ifMatch() == null) {
            return blobURL.getProperties(o.accessConditions(), null)
                    .map(response -> {
                        BlobAccessConditions newConditions;
                        if (o.accessConditions().modifiedAccessConditions().ifMatch() == null) {
                            newConditions = new BlobAccessConditions()
                                    .withModifiedAccessConditions(new ModifiedAccessConditions()
                                            .withIfModifiedSince(
                                                    o.accessConditions().modifiedAccessConditions().ifModifiedSince())
                                            .withIfUnmodifiedSince(
                                                    o.accessConditions().modifiedAccessConditions().ifUnmodifiedSince())
                                            .withIfMatch(response.headers().eTag())
                                            .withIfNoneMatch(
                                                    o.accessConditions().modifiedAccessConditions().ifNoneMatch()))
                                    .withLeaseAccessConditions(o.accessConditions().leaseAccessConditions());
                        } else {
                            newConditions = o.accessConditions();
                        }
                        long newCount;
                        /*
                        If the user either didn't specify a count or they specified a count greater than the size of the
                        remaining data, take the size of the remaining data. This is to prevent the case where the count
                        is much much larger than the size of the blob and we could try to download at an invalid offset.
                         */
                        if (r.count() == null || r.count() > response.headers().contentLength() - r.offset()) {
                            newCount = response.headers().contentLength() - r.offset();
                        } else {
                            newCount = r.count();
                        }
                        return new Pair<>(newCount, newConditions);
                    });
        } else {
            return Single.just(new Pair<>(r.count(), o.accessConditions()));
        }
    }

}
