// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.storage.blob.models.BlobDownloadAsyncResponse;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.common.ParallelTransferOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple3;

import java.util.function.BiFunction;
import java.util.function.Function;

import static java.lang.StrictMath.toIntExact;

/**
 * This class provides helper methods for lazy/chunked download.
 *
 * RESERVED FOR INTERNAL USE.
 */
public class ChunkedDownloadUtils {

    /*
    Download the first chunk. Construct a Mono which will emit the total count for calculating the number of chunks,
    access conditions containing the etag to lock on, and the response from downloading the first chunk.
     */
    public static Mono<Tuple3<Long, BlobRequestConditions, BlobDownloadAsyncResponse>> downloadFirstChunk(
        BlobRange range, ParallelTransferOptions parallelTransferOptions,
        BlobRequestConditions requestConditions, BiFunction<BlobRange, BlobRequestConditions,
        Mono<BlobDownloadAsyncResponse>> downloader, boolean eTagLock) {
        // We will scope our initial download to either be one chunk or the total size.
        long initialChunkSize = range.getCount() != null
            && range.getCount() < parallelTransferOptions.getBlockSizeLong()
            ? range.getCount() : parallelTransferOptions.getBlockSizeLong();

        return downloader.apply(new BlobRange(range.getOffset(), initialChunkSize), requestConditions)
            .subscribeOn(Schedulers.elastic())
            .flatMap(response -> {
                /*
                Either the etag was set and it matches because the download succeeded, so this is a no-op, or there
                was no etag, so we set it here. ETag locking is vital to ensure we download one, consistent view
                of the file.
                 */
                BlobRequestConditions newConditions = eTagLock ? setEtag(requestConditions,
                    response.getDeserializedHeaders().getETag()) : requestConditions;

                // Extract the total length of the blob from the contentRange header. e.g. "bytes 1-6/7"
                long totalLength = extractTotalBlobLength(response.getDeserializedHeaders().getContentRange());

                /*
                If the user either didn't specify a count or they specified a count greater than the size of the
                remaining data, take the size of the remaining data. This is to prevent the case where the count
                is much much larger than the size of the blob and we could try to download at an invalid offset.
                 */
                long newCount = range.getCount() == null || range.getCount() > (totalLength - range.getOffset())
                    ? totalLength - range.getOffset() : range.getCount();

                return Mono.zip(Mono.just(newCount), Mono.just(newConditions), Mono.just(response));
            })
            .onErrorResume(BlobStorageException.class, blobStorageException -> {
                /*
                 * In the case of an empty blob, we still want to report success and give back valid headers.
                 * Attempting a range download on an empty blob will return an InvalidRange error code and a
                 * Content-Range header of the format "bytes * /0". We need to double check that the total size is zero
                 * in the case that the customer has attempted an invalid range on a non-zero length blob.
                 */
                if (blobStorageException.getErrorCode() == BlobErrorCode.INVALID_RANGE
                    && extractTotalBlobLength(blobStorageException.getResponse()
                    .getHeaders().getValue("Content-Range")) == 0) {

                    return downloader.apply(new BlobRange(0, 0L), requestConditions)
                        .subscribeOn(Schedulers.elastic())
                        .flatMap(response -> {
                            /*
                            Ensure the blob is still 0 length by checking our download was the full length.
                            (200 is for full blob; 206 is partial).
                             */
                            if (response.getStatusCode() != 200) {
                                Mono.error(new IllegalStateException("Blob was modified mid download. It was "
                                    + "originally 0 bytes and is now larger."));
                            }
                            return Mono.zip(Mono.just(0L), Mono.just(requestConditions), Mono.just(response));
                        });
                }

                return Mono.error(blobStorageException);
            });
    }

    public static <T> Flux<T> downloadChunk(Integer chunkNum, BlobDownloadAsyncResponse initialResponse,
        BlobRange finalRange, ParallelTransferOptions finalParallelTransferOptions,
        BlobRequestConditions requestConditions, long newCount,
        BiFunction<BlobRange, BlobRequestConditions, Mono<BlobDownloadAsyncResponse>> downloader,
        Function<BlobDownloadAsyncResponse, Flux<T>> returnTransformer) {
        // The first chunk was retrieved during setup.
        if (chunkNum == 0) {
            return returnTransformer.apply(initialResponse);
        }

        // Calculate whether we need a full chunk or something smaller because we are at the end.
        long modifier = chunkNum.longValue() * finalParallelTransferOptions.getBlockSizeLong();
        long chunkSizeActual = Math.min(finalParallelTransferOptions.getBlockSizeLong(),
            newCount - modifier);
        BlobRange chunkRange = new BlobRange(finalRange.getOffset() + modifier, chunkSizeActual);

        // Make the download call.
        return downloader.apply(chunkRange, requestConditions)
            .subscribeOn(Schedulers.elastic())
            .flatMapMany(returnTransformer);
    }

    private static BlobRequestConditions setEtag(BlobRequestConditions requestConditions, String etag) {
        // We don't want to modify the user's object, so we'll create a duplicate and set the retrieved etag.
        return new BlobRequestConditions()
            .setIfModifiedSince(
                requestConditions.getIfModifiedSince())
            .setIfUnmodifiedSince(
                requestConditions.getIfModifiedSince())
            .setIfMatch(etag)
            .setIfNoneMatch(
                requestConditions.getIfNoneMatch())
            .setLeaseId(requestConditions.getLeaseId());
    }

    public static long extractTotalBlobLength(String contentRange) {
        return Long.parseLong(contentRange.split("/")[1]);
    }

    public static int calculateNumBlocks(long dataSize, long blockLength) {
        // Can successfully cast to an int because MaxBlockSize is an int, which this expression must be less than.
        int numBlocks = toIntExact(dataSize / blockLength);
        // Include an extra block for trailing data.
        if (dataSize % blockLength != 0) {
            numBlocks++;
        }
        return numBlocks;
    }
}
