// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.DownloadContentValidationOptions;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.structuredmessage.StructuredMessageDecoder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This is a decoding policy in an {@link com.azure.core.http.HttpPipeline} to decode structured messages in
 * storage download requests. The policy checks for a context value to determine when to apply structured message decoding.
 * 
 * <p>The policy supports smart retries by maintaining decoder state across network interruptions, ensuring:
 * <ul>
 *   <li>All received segment checksums are validated before retry</li>
 *   <li>Exact encoded and decoded byte positions are tracked</li>
 *   <li>Decoder state is preserved across retry requests</li>
 *   <li>Retries continue from the correct offset after network faults</li>
 * </ul>
 */
public class StorageContentValidationDecoderPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(StorageContentValidationDecoderPolicy.class);

    /**
     * Creates a new instance of {@link StorageContentValidationDecoderPolicy}.
     */
    public StorageContentValidationDecoderPolicy() {
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        // Check if structured message decoding is enabled for this request
        if (!shouldApplyDecoding(context)) {
            return next.process();
        }

        return next.process().map(httpResponse -> {
            // Only apply decoding to download responses (GET requests with body)
            if (!isDownloadResponse(httpResponse)) {
                return httpResponse;
            }

            DownloadContentValidationOptions validationOptions = getValidationOptions(context);
            Long contentLength = getContentLength(httpResponse.getHeaders());

            if (contentLength != null && contentLength > 0 && validationOptions != null) {
                // Get or create decoder with state tracking
                DecoderState decoderState = getOrCreateDecoderState(context, contentLength);

                // Decode using the stateful decoder
                Flux<ByteBuffer> decodedStream = decodeStream(httpResponse.getBody(), decoderState);

                // Update context with decoder state for potential retries
                context.setData(Constants.STRUCTURED_MESSAGE_DECODER_STATE_CONTEXT_KEY, decoderState);

                return new DecodedResponse(httpResponse, decodedStream, decoderState);
            }

            return httpResponse;
        });
    }

    /**
     * Decodes a stream of byte buffers using the decoder state.
     *
     * @param encodedFlux The flux of encoded byte buffers.
     * @param state The decoder state.
     * @return A flux of decoded byte buffers.
     */
    private Flux<ByteBuffer> decodeStream(Flux<ByteBuffer> encodedFlux, DecoderState state) {
        return encodedFlux.concatMap(encodedBuffer -> {
            // Track the NEW bytes received from the network (before combining with pending)
            int newBytesReceived = encodedBuffer.remaining();
            state.totalEncodedBytesProcessed.addAndGet(newBytesReceived);

            // Combine with pending data if any
            ByteBuffer dataToProcess = state.combineWithPending(encodedBuffer);

            try {
                // Try to decode what we have - decoder handles partial data
                // Create duplicate for decoder - it will advance the duplicate's position as it reads
                int availableSize = dataToProcess.remaining();
                ByteBuffer duplicateForDecode = dataToProcess.duplicate();
                int initialPosition = duplicateForDecode.position();

                // Decode - this advances duplicateForDecode's position
                ByteBuffer decodedData = state.decoder.decode(duplicateForDecode, availableSize);

                // Calculate how much of the input buffer was consumed by checking the duplicate's position
                int bytesConsumed = duplicateForDecode.position() - initialPosition;
                int bytesRemaining = availableSize - bytesConsumed;

                // Save only unconsumed portion to pending
                if (bytesRemaining > 0) {
                    // Position the original buffer to skip consumed bytes, then slice to get unconsumed
                    dataToProcess.position(bytesConsumed);
                    ByteBuffer unconsumed = dataToProcess.slice();
                    state.updatePendingBuffer(unconsumed);
                } else {
                    // All data was consumed
                    state.pendingBuffer = null;
                }

                // Track decoded bytes
                int decodedBytes = decodedData.remaining();
                state.totalBytesDecoded.addAndGet(decodedBytes);

                // Return decoded data if any
                if (decodedBytes > 0) {
                    return Flux.just(decodedData);
                } else {
                    return Flux.empty();
                }
            } catch (IllegalArgumentException e) {
                // Handle decoder exceptions - check if it's due to incomplete data
                String errorMsg = e.getMessage();
                if (errorMsg != null && (errorMsg.contains("not long enough") || errorMsg.contains("is incomplete"))) {
                    // Not enough data to decode yet - preserve all data in pending buffer
                    state.updatePendingBuffer(dataToProcess);

                    // Don't fail - just return empty and wait for more data
                    return Flux.empty();
                } else {
                    // Other errors should propagate
                    LOGGER.error("Failed to decode structured message chunk: " + e.getMessage(), e);
                    return Flux.error(e);
                }
            } catch (Exception e) {
                LOGGER.error("Failed to decode structured message chunk: " + e.getMessage(), e);
                return Flux.error(e);
            }
        }).doOnComplete(() -> {
            // Finalize when stream completes
            try {
                state.decoder.finalizeDecoding();
            } catch (IllegalArgumentException e) {
                // Expected if we haven't received all data yet (e.g., interrupted download)
                LOGGER.verbose("Decoding not finalized - may resume on retry: " + e.getMessage());
            }
        });
    }

    /**
     * Checks if structured message decoding should be applied based on context.
     *
     * @param context The pipeline call context.
     * @return true if decoding should be applied, false otherwise.
     */
    private boolean shouldApplyDecoding(HttpPipelineCallContext context) {
        return context.getData(Constants.STRUCTURED_MESSAGE_DECODING_CONTEXT_KEY)
            .map(value -> value instanceof Boolean && (Boolean) value)
            .orElse(false);
    }

    /**
     * Gets the validation options from context.
     *
     * @param context The pipeline call context.
     * @return The validation options or null if not present.
     */
    private DownloadContentValidationOptions getValidationOptions(HttpPipelineCallContext context) {
        return context.getData(Constants.STRUCTURED_MESSAGE_VALIDATION_OPTIONS_CONTEXT_KEY)
            .filter(value -> value instanceof DownloadContentValidationOptions)
            .map(value -> (DownloadContentValidationOptions) value)
            .orElse(null);
    }

    /**
     * Gets the content length from response headers.
     *
     * @param headers The response headers.
     * @return The content length or null if not present.
     */
    private Long getContentLength(HttpHeaders headers) {
        String contentLengthStr = headers.getValue(HttpHeaderName.CONTENT_LENGTH);
        if (contentLengthStr != null) {
            try {
                return Long.parseLong(contentLengthStr);
            } catch (NumberFormatException e) {
                LOGGER.warning("Invalid content length in response headers: " + contentLengthStr);
            }
        }
        return null;
    }

    /**
     * Gets or creates a decoder state from context.
     *
     * @param context The pipeline call context.
     * @param contentLength The content length.
     * @return The decoder state.
     */
    private DecoderState getOrCreateDecoderState(HttpPipelineCallContext context, long contentLength) {
        return context.getData(Constants.STRUCTURED_MESSAGE_DECODER_STATE_CONTEXT_KEY)
            .filter(value -> value instanceof DecoderState)
            .map(value -> (DecoderState) value)
            .orElseGet(() -> new DecoderState(contentLength));
    }

    /**
     * Checks if the response is a download response.
     *
     * @param httpResponse The HTTP response.
     * @return true if it's a download response, false otherwise.
     */
    private boolean isDownloadResponse(HttpResponse httpResponse) {
        HttpMethod method = httpResponse.getRequest().getHttpMethod();
        return method == HttpMethod.GET && httpResponse.getStatusCode() / 100 == 2;
    }

    /**
     * State holder for the structured message decoder that tracks decoding progress
     * across network interruptions.
     */
    public static class DecoderState {
        private final StructuredMessageDecoder decoder;
        private final long expectedContentLength;
        private final AtomicLong totalBytesDecoded;
        private final AtomicLong totalEncodedBytesProcessed;
        private ByteBuffer pendingBuffer;

        /**
         * Creates a new decoder state.
         *
         * @param expectedContentLength The expected length of the encoded content.
         */
        public DecoderState(long expectedContentLength) {
            this.expectedContentLength = expectedContentLength;
            this.decoder = new StructuredMessageDecoder(expectedContentLength);
            this.totalBytesDecoded = new AtomicLong(0);
            this.totalEncodedBytesProcessed = new AtomicLong(0);
            this.pendingBuffer = null;
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
         * Updates the pending buffer with remaining data.
         *
         * @param dataToProcess The buffer with remaining data.
         */
        private void updatePendingBuffer(ByteBuffer dataToProcess) {
            pendingBuffer = ByteBuffer.allocate(dataToProcess.remaining());
            pendingBuffer.put(dataToProcess);
            pendingBuffer.flip();
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
         * Gets the offset to use for retry requests.
         * This uses the decoder's last complete segment boundary to ensure retries
         * resume from a valid segment boundary, not mid-segment.
         *
         * @return The offset for retry requests (last complete segment boundary).
         */
        public long getRetryOffset() {
            // Use the decoder's last complete segment start as the retry offset
            // This ensures we resume from a segment boundary, not mid-segment
            long retryOffset = decoder.getLastCompleteSegmentStart();
            LOGGER.verbose("Retry offset calculated: {} (last complete segment boundary)", retryOffset);
            return retryOffset;
        }

        /**
         * Checks if the decoder has finalized.
         *
         * @return true if finalized, false otherwise.
         */
        public boolean isFinalized() {
            return totalEncodedBytesProcessed.get() >= expectedContentLength;
        }
    }

    /**
     * Decoded HTTP response that wraps the original response with a decoded stream.
     */
    private static class DecodedResponse extends HttpResponse {
        private final HttpResponse originalResponse;
        private final Flux<ByteBuffer> decodedBody;
        private final DecoderState decoderState;

        /**
         * Creates a new decoded response.
         *
         * @param originalResponse The original HTTP response.
         * @param decodedBody The decoded body stream.
         * @param decoderState The decoder state.
         */
        DecodedResponse(HttpResponse originalResponse, Flux<ByteBuffer> decodedBody, DecoderState decoderState) {
            super(originalResponse.getRequest());
            this.originalResponse = originalResponse;
            this.decodedBody = decodedBody;
            this.decoderState = decoderState;
        }

        @Override
        public int getStatusCode() {
            return originalResponse.getStatusCode();
        }

        @Override
        public String getHeaderValue(String name) {
            return originalResponse.getHeaderValue(name);
        }

        @Override
        public HttpHeaders getHeaders() {
            return originalResponse.getHeaders();
        }

        @Override
        public Flux<ByteBuffer> getBody() {
            return decodedBody;
        }

        @Override
        public Mono<byte[]> getBodyAsByteArray() {
            return FluxUtil.collectBytesInByteBufferStream(decodedBody);
        }

        @Override
        public Mono<String> getBodyAsString() {
            return getBodyAsByteArray().map(bytes -> new String(bytes, Charset.defaultCharset()));
        }

        @Override
        public Mono<String> getBodyAsString(Charset charset) {
            return getBodyAsByteArray().map(bytes -> new String(bytes, charset));
        }

        /**
         * Gets the decoder state.
         *
         * @return The decoder state.
         */
        public DecoderState getDecoderState() {
            return decoderState;
        }
    }
}
