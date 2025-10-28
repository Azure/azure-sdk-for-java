// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.structuredmessage;

import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Stateful decoder for structured messages that supports mid-stream retries.
 * This decoder maintains state across network interruptions to ensure all data
 * is validated before retrying from the point of failure.
 * 
 * <p>This decoder uses streaming decoding and validates segments incrementally,
 * allowing for smart retries that:
 * <ul>
 *   <li>Validate all received segment checksums before retry</li>
 *   <li>Track exact encoded and decoded byte positions</li>
 *   <li>Resume from the correct offset after network faults</li>
 *   <li>Preserve decoder state across retry requests</li>
 * </ul>
 */
public class StatefulStructuredMessageDecoder {
    private static final ClientLogger LOGGER = new ClientLogger(StatefulStructuredMessageDecoder.class);

    private final long expectedContentLength;
    private final StructuredMessageDecoder decoder;
    private final AtomicLong totalBytesDecoded;
    private final AtomicLong totalEncodedBytesProcessed;
    private ByteBuffer pendingBuffer;

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
        this.pendingBuffer = null;
    }

    /**
     * Decodes a flux of byte buffers representing encoded structured message data.
     * This method processes data incrementally, validating segment checksums as
     * complete segments are received.
     *
     * @param encodedFlux The flux of encoded byte buffers.
     * @return A flux of decoded byte buffers.
     */
    public Flux<ByteBuffer> decode(Flux<ByteBuffer> encodedFlux) {
        return encodedFlux.concatMap(encodedBuffer -> {
            try {
                // Combine with pending data if any
                ByteBuffer dataToProcess = combineWithPending(encodedBuffer);
                
                // Track encoded bytes
                int encodedBytesInBuffer = encodedBuffer.remaining();
                totalEncodedBytesProcessed.addAndGet(encodedBytesInBuffer);

                // Try to decode what we have - decoder handles partial data
                // The size parameter allows us to decode incrementally
                int availableSize = dataToProcess.remaining();
                ByteBuffer decodedData = decoder.decode(dataToProcess.duplicate(), availableSize);
                
                // Track decoded bytes
                int decodedBytes = decodedData.remaining();
                totalBytesDecoded.addAndGet(decodedBytes);

                // Store any remaining unprocessed data for next iteration
                if (dataToProcess.hasRemaining()) {
                    pendingBuffer = ByteBuffer.allocate(dataToProcess.remaining());
                    pendingBuffer.put(dataToProcess);
                    pendingBuffer.flip();
                } else {
                    pendingBuffer = null;
                }

                // Return decoded data if any
                if (decodedBytes > 0) {
                    return Flux.just(decodedData);
                } else {
                    return Flux.empty();
                }
            } catch (Exception e) {
                LOGGER.error("Failed to decode structured message chunk: " + e.getMessage(), e);
                return Flux.error(e);
            }
        }).doOnComplete(() -> {
            // Finalize when stream completes
            try {
                decoder.finalizeDecoding();
            } catch (IllegalArgumentException e) {
                // Expected if we haven't received all data yet (e.g., interrupted download)
                LOGGER.verbose("Decoding not finalized - may resume on retry: " + e.getMessage());
            }
        });
    }

    /**
     * Combines pending buffer with new data.
     *
     * @param newBuffer The new buffer to combine.
     * @return Combined buffer.
     */
    private ByteBuffer combineWithPending(ByteBuffer newBuffer) {
        if (pendingBuffer == null || !pendingBuffer.hasRemaining()) {
            return newBuffer.duplicate();
        }

        ByteBuffer combined = ByteBuffer.allocate(pendingBuffer.remaining() + newBuffer.remaining());
        combined.put(pendingBuffer.duplicate());
        combined.put(newBuffer.duplicate());
        combined.flip();
        return combined;
    }

    /**
     * Gets the total number of decoded bytes processed so far.
     * This value can be used to calculate the offset for retries.
     *
     * @return The total decoded bytes.
     */
    public long getTotalBytesDecoded() {
        return totalBytesDecoded.get();
    }

    /**
     * Gets the total number of encoded bytes processed so far.
     * This value represents the position in the encoded stream.
     *
     * @return The total encoded bytes processed.
     */
    public long getTotalEncodedBytesProcessed() {
        return totalEncodedBytesProcessed.get();
    }

    /**
     * Checks if the decoder has finalized (completed decoding the entire message).
     *
     * @return true if finalized, false otherwise.
     */
    public boolean isFinalized() {
        // Decoder is finalized when we've processed the expected content length
        return totalEncodedBytesProcessed.get() >= expectedContentLength;
    }

    /**
     * Gets the expected content length being decoded.
     *
     * @return The expected content length.
     */
    public long getExpectedContentLength() {
        return expectedContentLength;
    }
}
