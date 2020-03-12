// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation;

import com.azure.core.http.rest.Response;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.ParallelTransferOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * This class provides helper methods for buffered upload.
 *
 * RESERVED FOR INTERNAL USE.
 */
public class UploadUtils {

    /**
     * Determines whether the upload should happen in full or chunked.
     * @param data The data to write
     * @param parallelTransferOptions {@link ParallelTransferOptions}
     * @param uploadInChunks {@link Function} to upload in chunks.
     * @param uploadFull {@link BiFunction} to upload in full.
     * @return A reactive response containing the information of the uploaded data.
     */
    public static <T> Mono<Response<T>> uploadFullOrChunked(final Flux<ByteBuffer> data,
        ParallelTransferOptions parallelTransferOptions,
        final Function<Flux<ByteBuffer>, Mono<Response<T>>> uploadInChunks,
        final BiFunction<Flux<ByteBuffer>, Long, Mono<Response<T>>> uploadFull) {
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
                if (bufferedDataSize[0] > parallelTransferOptions.getMaxSingleUploadSize()) {
                    return false;
                } else {
                    bufferedDataSize[0] += buffer.remaining();

                    if (bufferedDataSize[0] > parallelTransferOptions.getMaxSingleUploadSize()) {
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
                 * Set the prefetch to 'Integer.MAX_VALUE' to leverage an unbounded fetch limit in case there are numerous
                 * tiny buffers, windowUntil uses a default limit of 256 and once that is hit it will trigger onComplete
                 * which causes downstream issues.
                 */
            }, true, Integer.MAX_VALUE)
            .buffer(2)
            .next()
            .flatMap(fluxes -> {
                if (fluxes.size() == 1) {
                    return uploadFull.apply(Flux.fromIterable(cachedBuffers), bufferedDataSize[0]);
                } else {
                    return uploadInChunks.apply(dequeuingFlux(cachedBuffers).concatWith(fluxes.get(1)));
                }
            })
            // If nothing was emitted from the stream upload an empty blob.
            .switchIfEmpty(uploadFull.apply(Flux.empty(), 0L));
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
     * Break the source Flux into chunks that are <= chunk size. This makes filling the pooled buffers much easier
     * as we can guarantee we only need at most two buffers for any call to write (two in the case of one pool buffer
     * filling up with more data to write). We use flatMapSequential because we need to guarantee we preserve the
     * ordering of the buffers, but we don't really care if one is split before another.
     * @param data Data to chunk
     * @param parallelTransferOptions {@link ParallelTransferOptions}
     * @return Chunked data
     */
    public static Flux<ByteBuffer> chunkSource(Flux<ByteBuffer> data, ParallelTransferOptions parallelTransferOptions) {
        return data
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
    }


    public static boolean shouldUploadInChunks(String filePath, Integer maxSingleUploadSize, ClientLogger logger) {
        AsynchronousFileChannel channel = uploadFileResourceSupplier(filePath, logger);
        boolean retVal;
        try {
            retVal = channel.size() > maxSingleUploadSize;
        } catch (IOException e) {
            throw logger.logExceptionAsError(new UncheckedIOException(e));
        } finally {
            uploadFileCleanup(channel, logger);
        }

        return retVal;
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
    public static AsynchronousFileChannel uploadFileResourceSupplier(String filePath, ClientLogger logger) {
        try {
            return AsynchronousFileChannel.open(Paths.get(filePath), StandardOpenOption.READ);
        } catch (IOException e) {
            throw logger.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    public static void uploadFileCleanup(AsynchronousFileChannel channel, ClientLogger logger) {
        try {
            channel.close();
        } catch (IOException e) {
            throw logger.logExceptionAsError(new UncheckedIOException(e));
        }
    }
}
