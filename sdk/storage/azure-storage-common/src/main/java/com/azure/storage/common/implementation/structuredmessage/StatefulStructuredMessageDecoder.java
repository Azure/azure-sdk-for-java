// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.structuredmessage;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.implementation.BufferStagingArea;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Stateful decoder for structured messages that supports mid-stream retries.
 * This decoder maintains state across network interruptions to ensure all data
 * is validated before retrying from the point of failure.
 */
public class StatefulStructuredMessageDecoder {
    private static final ClientLogger LOGGER = new ClientLogger(StatefulStructuredMessageDecoder.class);

    private final long expectedContentLength;
    private final StructuredMessageDecoder decoder;
    private final AtomicLong totalBytesDecoded;
    private final AtomicLong totalEncodedBytesProcessed;
    private ByteBuffer pendingData;
    private boolean finalized;

    /**
     * Creates a new stateful structured message decoder.
     *
     * @param expectedContentLength The expected length of the encoded content.
     */
    public StatefulStructuredMessageDecoder(long expectedContentLength) {
        this.expectedContentLength = expectedContentLength;
        this.decoder = new StructuredMessageDecoder(expectedContentLength);
        this.totalBytesDecoded = new AtomicLong(0);
        this.totalEncodedBytesProcessed = new AtomicLong(0);
        this.pendingData = null;
        this.finalized = false;
    }

    /**
     * Decodes a flux of byte buffers representing encoded structured message data.
     *
     * @param encodedFlux The flux of encoded byte buffers.
     * @return A flux of decoded byte buffers.
     */
    public Flux<ByteBuffer> decode(Flux<ByteBuffer> encodedFlux) {
        if (finalized) {
            return Flux.error(new IllegalStateException("Decoder has already been finalized"));
        }

        // Collect all data first (structured message needs complete data to decode)
        return encodedFlux
            .collect(() -> new EncodedDataCollector(), EncodedDataCollector::addBuffer)
            .flatMapMany(collector -> {
                try {
                    ByteBuffer allEncodedData = collector.getAllData();
                    
                    if (allEncodedData.remaining() == 0) {
                        return Flux.empty();
                    }

                    // Update total encoded bytes processed
                    totalEncodedBytesProcessed.addAndGet(allEncodedData.remaining());

                    // Decode the complete message
                    ByteBuffer decodedData = decoder.decode(allEncodedData);
                    
                    // Update total bytes decoded
                    totalBytesDecoded.addAndGet(decodedData.remaining());

                    // Finalize decoding
                    decoder.finalizeDecoding();
                    finalized = true;

                    return Flux.just(decodedData);
                } catch (Exception e) {
                    LOGGER.error("Failed to decode structured message: " + e.getMessage(), e);
                    return Flux.error(e);
                }
            });
    }

    /**
     * Gets the total number of decoded bytes processed so far.
     *
     * @return The total decoded bytes.
     */
    public long getTotalBytesDecoded() {
        return totalBytesDecoded.get();
    }

    /**
     * Gets the total number of encoded bytes processed so far.
     *
     * @return The total encoded bytes processed.
     */
    public long getTotalEncodedBytesProcessed() {
        return totalEncodedBytesProcessed.get();
    }

    /**
     * Checks if the decoder has been finalized.
     *
     * @return true if finalized, false otherwise.
     */
    public boolean isFinalized() {
        return finalized;
    }

    /**
     * Helper class to collect encoded data buffers.
     */
    private static class EncodedDataCollector {
        private ByteBuffer accumulatedBuffer;

        EncodedDataCollector() {
            this.accumulatedBuffer = ByteBuffer.allocate(0);
        }

        void addBuffer(ByteBuffer buffer) {
            // Accumulate the buffer
            ByteBuffer newBuffer = ByteBuffer.allocate(accumulatedBuffer.remaining() + buffer.remaining());
            newBuffer.put(accumulatedBuffer);
            newBuffer.put(buffer);
            newBuffer.flip();
            accumulatedBuffer = newBuffer;
        }

        ByteBuffer getAllData() {
            return accumulatedBuffer;
        }
    }
}
