// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed;

import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.implementation.util.ChunkedDownloadUtils;
import com.azure.storage.blob.models.BlobDownloadAsyncResponse;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.ParallelTransferOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.function.Function;

/**
 * FOR INTERNAL USE ONLY.
 * Class to lazily download a blob.
 */
class BlobLazyDownloader {

    private final BlobAsyncClient client; /* Client to download from. */
    private final long blockSize; /* The block size. */
    private final BlobRange range;

    /**
     * Creates a new BlobLazyDownloader to download the rest of a blob at a certain offset.
     */
    BlobLazyDownloader(BlobAsyncClient client, long blockSize, long offset) {
        this.client = client;
        this.blockSize = blockSize;
        this.range = new BlobRange(offset);
    }

    /**
     * Creates a new BlobLazyDownloader to download a partial blob.
     */
    BlobLazyDownloader(BlobAsyncClient client, long totalSize) {
        this.client = client;
        this.blockSize = totalSize;
        this.range = new BlobRange(0, totalSize);
    }

    /* TODO (gapra) : It may be possible to unduplicate the code below as well to share between downloadToFile but
       wasnt immediately obvious to me */
    public Flux<ByteBuffer> download() {
        ParallelTransferOptions options = new ParallelTransferOptions()
            .setBlockSizeLong(blockSize);
        BlobRequestConditions requestConditions = new BlobRequestConditions();

        Function<BlobRange, Mono<BlobDownloadAsyncResponse>> downloadFunc = range
            -> client.downloadWithResponse(range, null, new BlobRequestConditions(), false);

        return ChunkedDownloadUtils.downloadFirstChunk(range, options, requestConditions, downloadFunc)
            .flatMapMany(setupTuple3 -> {
                long newCount = setupTuple3.getT1();
                BlobRequestConditions finalConditions = setupTuple3.getT2();

                int numChunks = ChunkedDownloadUtils.calculateNumBlocks(newCount, options.getBlockSizeLong());

                // In case it is an empty blob, this ensures we still actually perform a download operation.
                numChunks = numChunks == 0 ? 1 : numChunks;

                BlobDownloadAsyncResponse initialResponse = setupTuple3.getT3();
                return Flux.range(0, numChunks)
                    .concatMap(chunkNum -> { /* TODO (gapra) : This was the biggest difference - downloadToFile does
                                                                it in parallel, but we want this to be strictly
                                                                 sequential. */
//                        // The first chunk was retrieved during setup.
//                        if (chunkNum == 0) {
//                            return initialResponse.getValue();
//                        }
//
//                        // Calculate whether we need a full chunk or something smaller because we are at the end.
//                        long modifier = chunkNum.longValue() * options.getBlockSizeLong();
//                        long chunkSizeActual = Math.min(options.getBlockSizeLong(),
//                            newCount - modifier);
//                        BlobRange chunkRange = new BlobRange(range.getOffset() + modifier, chunkSizeActual);
//
//                        // Make the download call.
//                        return client.downloadWithResponse(chunkRange, null, finalConditions, false)
//                            .flatMapMany(BlobDownloadAsyncResponse::getValue);
                        return ChunkedDownloadUtils.downloadChunk(client, chunkNum, initialResponse, range, options, finalConditions, newCount);
                    });
            });
    }

}
