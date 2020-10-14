// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed;

import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.implementation.util.ChunkedDownloadUtils;
import com.azure.storage.blob.models.BlobDownloadAsyncResponse;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.common.ParallelTransferOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.function.BiFunction;

/**
 * FOR INTERNAL USE ONLY.
 * Class to lazily download a blob.
 */
class BlobChunkedDownloader {

    private final BlobAsyncClient client; /* Client to download from. */
    private final long blockSize; /* The block size. */
    private final BlobRange range;

    /**
     * Creates a new BlobLazyDownloader to download the rest of a blob at a certain offset.
     */
    BlobChunkedDownloader(BlobAsyncClient client, long blockSize, long offset) {
        this.client = client;
        this.blockSize = blockSize;
        this.range = new BlobRange(offset);
    }

    /**
     * Creates a new BlobLazyDownloader to download a partial blob.
     */
    BlobChunkedDownloader(BlobAsyncClient client, long totalSize) {
        this.client = client;
        this.blockSize = totalSize;
        this.range = new BlobRange(0, totalSize);
    }

    public Flux<ByteBuffer> download() {
        ParallelTransferOptions options = new ParallelTransferOptions()
            .setBlockSizeLong(blockSize);
        BlobRequestConditions requestConditions = new BlobRequestConditions();

        BiFunction<BlobRange, BlobRequestConditions, Mono<BlobDownloadAsyncResponse>> downloadFunc = (range, conditions)
            -> client.downloadWithResponse(range, null, conditions, false);

        /* We don't etag lock since the Changefeed can append to the blob while we are reading it. */
        return ChunkedDownloadUtils.downloadFirstChunk(range, options, requestConditions, downloadFunc, false)
            .flatMapMany(setupTuple3 -> {
                long newCount = setupTuple3.getT1();
                BlobRequestConditions finalConditions = setupTuple3.getT2();

                int numChunks = ChunkedDownloadUtils.calculateNumBlocks(newCount, options.getBlockSizeLong());

                // In case it is an empty blob, this ensures we still actually perform a download operation.
                numChunks = numChunks == 0 ? 1 : numChunks;

                BlobDownloadAsyncResponse initialResponse = setupTuple3.getT3();
                return Flux.range(0, numChunks)
                    .concatMap(chunkNum -> ChunkedDownloadUtils.downloadChunk(chunkNum, initialResponse,
                        range, options, finalConditions, newCount, downloadFunc, BlobDownloadAsyncResponse::getValue));
            });
    }

}
