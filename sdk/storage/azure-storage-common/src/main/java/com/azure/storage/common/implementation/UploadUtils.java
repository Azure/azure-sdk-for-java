// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation;

import com.azure.core.http.rest.Response;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.common.Utility;
import com.azure.storage.common.implementation.structuredmessage.StorageChecksumAlgorithm;
import com.azure.storage.common.implementation.structuredmessage.StructuredMessageEncoder;
import com.azure.storage.common.implementation.structuredmessage.StructuredMessageFlags;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.io.InputStream;
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
import static com.azure.storage.common.implementation.structuredmessage.StructuredMessageConstants.STATIC_MAXIMUM_ENCODED_DATA_LENGTH;
import static com.azure.storage.common.implementation.structuredmessage.StructuredMessageConstants.STRUCTURED_BODY_TYPE_VALUE;
import static com.azure.storage.common.implementation.structuredmessage.StructuredMessageConstants.V1_DEFAULT_SEGMENT_CONTENT_LENGTH;

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

        PayloadSizeGate gate = new PayloadSizeGate(parallelTransferOptions.getMaxSingleUploadSizeLong());

        return data.filter(ByteBuffer::hasRemaining)
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
                    return uploadInChunks.apply(flux.concatWith(Flux.defer(gate::flush)));
                } else {
                    // In this case gate contains all the data cached.
                    // The flux passed to this lambda allows only one subscriber. Therefore we substitute it
                    // with flux coming from gate which is based of iterable and can be subscribed again.
                    return uploadFull.apply(gate.flush(), gate.size());
                }
            })
            .next()
            // If nothing was emitted from the stream upload an empty blob.
            .switchIfEmpty(Mono.defer(() -> uploadFull.apply(Flux.empty(), 0L)));
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
            return data.flatMapSequential(buffer -> {
                if (buffer.remaining() <= chunkSize) {
                    return Flux.just(buffer);
                }
                int numSplits = (int) Math.ceil(buffer.remaining() / (double) chunkSize);
                return Flux.range(0, numSplits).map(i -> {
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

    public static Mono<FluxContentValidationWrapper> computeChecksum(Flux<ByteBuffer> data, boolean computeMd5,
        StorageChecksumAlgorithm storageChecksumAlgorithm, long length, ClientLogger logger) {
        if (computeMd5) {
            return computeMd5(data, true, length, logger);
        }
        if (storageChecksumAlgorithm.resolveAuto() == StorageChecksumAlgorithm.CRC64) {
            if (length < STATIC_MAXIMUM_ENCODED_DATA_LENGTH) {
                return computeCRC64(data, length, logger);
            } else {
                return applyStructuredMessage(data, length);
            }
        }
        return Mono.just(new FluxContentValidationWrapper(data, new ContentValidationInfo(), length));
    }

    public static Mono<FluxContentValidationWrapper> applyStructuredMessage(Flux<ByteBuffer> data, long length) {
        StructuredMessageEncoder structuredMessageEncoder = new StructuredMessageEncoder((int) length,
            V1_DEFAULT_SEGMENT_CONTENT_LENGTH, StructuredMessageFlags.STORAGE_CRC64);

        // Create BufferStagingArea with 4MB chunks
        BufferStagingArea stagingArea
            = new BufferStagingArea(STATIC_MAXIMUM_ENCODED_DATA_LENGTH, STATIC_MAXIMUM_ENCODED_DATA_LENGTH);

        Flux<ByteBuffer> encodedBody = data.flatMapSequential(stagingArea::write, 1, 1)
            .concatWith(Flux.defer(stagingArea::flush))
            .flatMap(bufferAggregator -> bufferAggregator.asFlux().map(structuredMessageEncoder::encode));

        return Mono.just(new FluxContentValidationWrapper(encodedBody,
            new ContentValidationInfo().setStructuredBodyType(STRUCTURED_BODY_TYPE_VALUE)
                .setOriginalContentLength(length),
            structuredMessageEncoder.getEncodedMessageLength()));
    }

    public static Mono<FluxContentValidationWrapper> computeCRC64(Flux<ByteBuffer> data, long length,
        ClientLogger logger) {
        try {
            return data.reduce(0L, (crc, buffer) -> {
                // Use ByteBuffer.duplicate to create a read-only view that won't mutate the original buffer
                ByteBuffer copyData = buffer.duplicate().asReadOnlyBuffer();
                // Convert ByteBuffer to byte array for CRC calculation
                byte[] bufferArray = new byte[copyData.remaining()];
                copyData.get(bufferArray);
                // Update the cumulative CRC with this buffer's data
                return StorageCrc64Calculator.compute(bufferArray, crc);
            }).map(finalCrc -> {
                // Convert the final CRC64 value to byte array (little-endian)
                byte[] crc64Bytes = new byte[8];
                for (int i = 0; i < 8; i++) {
                    crc64Bytes[i] = (byte) (finalCrc >>> (i * 8));
                }
                return new FluxContentValidationWrapper(data, new ContentValidationInfo().setCRC64checksum(crc64Bytes),
                    length);
            });
        } catch (Exception e) {
            return monoError(logger, new RuntimeException(e));
        }
    }

    public static Mono<FluxContentValidationWrapper> computeMd5(Flux<ByteBuffer> data, boolean computeMd5, long length,
        ClientLogger logger) {
        if (computeMd5) {
            try {
                return data.reduce(MessageDigest.getInstance("MD5"), (digest, buffer) -> {
                    // Use MessageDigest.update(ByteBuffer) as this is able to optimize based on the type of ByteBuffer
                    // that was passed. Also, pass it a ByteBuffer.duplicate view so that the actual ByteBuffer won't
                    // be mutated.
                    digest.update(buffer.duplicate().asReadOnlyBuffer());
                    return digest;
                })
                    .map(messageDigest -> new FluxContentValidationWrapper(data,
                        new ContentValidationInfo().setMD5checksum(messageDigest.digest()), length));
            } catch (NoSuchAlgorithmException e) {
                return monoError(logger, new RuntimeException(e));
            }
        } else {
            return Mono.just(new FluxContentValidationWrapper(data, new ContentValidationInfo(), length));
        }
    }

    /**
     * comment
     */
    public static class ContentValidationInfo {
        private byte[] MD5checksum;
        private byte[] CRC64checksum;
        private Long originalContentLength;
        private String structuredBodyType;

        public ContentValidationInfo() {
            this.MD5checksum = null;
            this.originalContentLength = null;
        }

        public ContentValidationInfo setOriginalContentLength(Long originalContentLength) {
            this.originalContentLength = originalContentLength;
            return this;
        }

        public Long getOriginalContentLength() {
            return originalContentLength;
        }

        public byte[] getMD5checksum() {
            return MD5checksum;
        }

        public ContentValidationInfo setMD5checksum(byte[] MD5checksum) {
            this.MD5checksum = CoreUtils.clone(MD5checksum);
            return this;
        }

        public byte[] getCRC64checksum() {
            return CRC64checksum;
        }

        public ContentValidationInfo setCRC64checksum(byte[] CRC64checksum) {
            this.CRC64checksum = CoreUtils.clone(CRC64checksum);
            return this;
        }

        public String getStructuredBodyType() {
            return structuredBodyType;
        }

        public ContentValidationInfo setStructuredBodyType(String structuredBodyType) {
            this.structuredBodyType = structuredBodyType;
            return this;
        }
    }

    public static class FluxContentValidationWrapper {
        private final Flux<ByteBuffer> data;
        private final ContentValidationInfo contentValidationInfo;
        private final long dataLength;

        public FluxContentValidationWrapper(Flux<ByteBuffer> data, ContentValidationInfo contentValidationInfo,
            long dataLength) {
            this.data = data;
            this.contentValidationInfo = contentValidationInfo;
            this.dataLength = dataLength;
        }

        public final Flux<ByteBuffer> getData() {
            return data;
        }

        public final ContentValidationInfo getContentValidationInfo() {
            return contentValidationInfo;
        }

        public final long getDataLength() {
            return dataLength;
        }
    }

    /**
     * Extracts the byte buffer for upload operations.
     *
     * @param data the {@link Flux} of {@link ByteBuffer}, if specified.
     * @param optionalLength length of data.
     * @param blockSize the block size (chunk size) to transfer at a time.
     * @param dataStream the {@link InputStream}, if specified.
     * @return the updated {@link Flux} of {@link ByteBuffer}.
     */
    public static Flux<ByteBuffer> extractByteBuffer(Flux<ByteBuffer> data, Long optionalLength, Long blockSize,
        InputStream dataStream) {
        // no specified length: use azure.core's converter
        if (data == null && optionalLength == null) {
            // We can only buffer up to max int due to restrictions in ByteBuffer.
            int chunkSize = (int) Math.min(Constants.MAX_INPUT_STREAM_CONVERTER_BUFFER_LENGTH, blockSize);
            data = FluxUtil.toFluxByteBuffer(dataStream, chunkSize).subscribeOn(Schedulers.boundedElastic());
            // specified length (legacy requirement): use custom converter. no marking because we buffer anyway.
        } else if (data == null) {
            // We can only buffer up to max int due to restrictions in ByteBuffer.
            int chunkSize = (int) Math.min(Constants.MAX_INPUT_STREAM_CONVERTER_BUFFER_LENGTH, blockSize);
            data = Utility.convertStreamToByteBuffer(dataStream, optionalLength, chunkSize, false);
        }
        return data;
    }
}
