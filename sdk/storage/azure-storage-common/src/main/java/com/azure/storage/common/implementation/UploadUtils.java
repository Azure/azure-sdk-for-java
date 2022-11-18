// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation;

import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.CoreUtils;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.azure.core.util.FluxUtil.monoError;

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
    public static <T> Mono<Response<T>> uploadFullOrChunked(final BinaryData data,
        ParallelTransferOptions parallelTransferOptions,
        final Function<BinaryData, Mono<Response<T>>> uploadInChunks,
        final Function<BinaryData, Mono<Response<T>>> uploadFull) {

        long threshold = parallelTransferOptions.getMaxSingleUploadSizeLong();
        // if data has known length, branch off that. Otherwise, start buffering an initial upload to determine branch
        if (data.getLength() != null) {
            if (data.getLength() > threshold) {
                return uploadInChunks.apply(data);
            } else {
                return uploadFull.apply(data);
            }
        }
        PayloadSizeGate gate = new PayloadSizeGate(parallelTransferOptions.getMaxSingleUploadSizeLong());

        return data.toFluxByteBuffer()
            .filter(ByteBuffer::hasRemaining)
            // The gate buffers data until threshold is breached.
            .concatMap(gate::write, 0)
            // First buffer is emitted after threshold is breached or there's no more data.
            // Therefore we can make a decision how to upload data on first element.
            .switchOnFirst((signal, flux) -> {
                // If there is an error before the threshold is reached, propagate the error
                if (signal.isOnError()) {
                    Throwable t = signal.getThrowable();
                    if (t != null) {
                        return Flux.error(t);
                    } else {
                        return Flux.error(new IllegalStateException("Source flux failed but cause is unretrievable"));
                    }
                }
                if (gate.isThresholdBreached()) {
                    // In this case we can pass a flux that can have just one subscriber because
                    // the chunked upload is going to cache the data downstream before sending chunks over the wire.
                    return BinaryData.fromFlux(flux.concatWith(Flux.defer(gate::flush)))
                        .flatMap(uploadInChunks);
                } else {
                    // In this case gate contains all the data cached.
                    // The flux passed to this lambda allows only one subscriber. Therefore we substitute it
                    // with flux coming from gate which is based of iterable and can be subscribed again.
                    return BinaryData.fromFlux(gate.flush(), gate.size())
                        .flatMap(uploadFull);
                }
            })
            .next()
            // If nothing was emitted from the stream upload an empty blob.
            .switchIfEmpty(Mono.defer(() -> BinaryData.fromFlux(Flux.empty(), 0L).flatMap(uploadFull)));
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
        if (parallelTransferOptions.getBlockSizeLong() <= Integer.MAX_VALUE) {
            int chunkSize = parallelTransferOptions.getBlockSizeLong().intValue();
            return data
                .flatMapSequential(buffer -> {
                    if (buffer.remaining() <= chunkSize) {
                        return Flux.just(buffer);
                    }
                    int numSplits = (int) Math.ceil(buffer.remaining() / (double) chunkSize);
                    return Flux.range(0, numSplits)
                        .map(i -> {
                            ByteBuffer duplicate = buffer.duplicate().asReadOnlyBuffer();
                            duplicate.position(i * chunkSize);
                            duplicate.limit(Math.min(duplicate.limit(), (i + 1) * chunkSize));
                            return duplicate;
                        });
                }, 1, 1);
        } else {
            return data;
        }
    }


    public static boolean shouldUploadInChunks(String filePath, Long maxSingleUploadSize, ClientLogger logger) {
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

    /**
     * Computes the md5 of the data and wraps it with the data.
     *
     * @param data The data.
     * @param computeMd5 Whether to compute the md5.
     * @param logger Logger to log errors.
     * @return The data wrapped with its md5.
     */
    public static Mono<FluxMd5Wrapper> computeMd5(BinaryData data, boolean computeMd5, ClientLogger logger) {
        if (!data.isReplayable()) {
            return Mono.error(logger.logExceptionAsError(new IllegalArgumentException("data was not replayable")));
        }
        if (computeMd5) {
            try {
                return data.toFluxByteBuffer().reduce(MessageDigest.getInstance("MD5"), (digest, buffer) -> {
                    // Use MessageDigest.update(ByteBuffer) as this is able to optimize based on the type of ByteBuffer
                    // that was passed. Also, pass it a ByteBuffer.duplicate view so that the actual ByteBuffer won't
                    // be mutated.
                    digest.update(buffer.duplicate().asReadOnlyBuffer());
                    return digest;
                }).map(messageDigest -> new FluxMd5Wrapper(data, messageDigest.digest()));
            } catch (NoSuchAlgorithmException e) {
                return monoError(logger, new RuntimeException(e));
            }
        } else {
            return Mono.just(new FluxMd5Wrapper(data, null));
        }
    }

    public static class FluxMd5Wrapper {
        private final BinaryData data;
        private final byte[] md5;

        FluxMd5Wrapper(BinaryData data, byte[] md5) {
            this.data = data;
            this.md5 = CoreUtils.clone(md5);
        }

        public BinaryData getData() {
            return data;
        }

        public byte[] getMd5() {
            return CoreUtils.clone(md5);
        }
    }
}
