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
     * The decoder properly handles partial headers and segments split across chunks.
     *
     * @param encodedFlux The flux of encoded byte buffers.
     * @param state The decoder state.
     * @return A flux of decoded byte buffers.
     */
    private Flux<ByteBuffer> decodeStream(Flux<ByteBuffer> encodedFlux, DecoderState state) {
        return encodedFlux.concatMap(encodedBuffer -> {
            LOGGER.atInfo()
                .addKeyValue("newBytes", encodedBuffer.remaining())
                .addKeyValue("decoderOffset", state.decoder.getMessageOffset())
                .addKeyValue("lastCompleteSegment", state.decoder.getLastCompleteSegmentStart())
                .addKeyValue("totalDecodedPayload", state.decoder.getTotalDecodedPayloadBytes())
                .log("Received buffer in decodeStream");

            try {
                // Use the new decodeChunk API which properly handles partial headers
                StructuredMessageDecoder.DecodeResult result = state.decoder.decodeChunk(encodedBuffer);

                LOGGER.atInfo()
                    .addKeyValue("status", result.getStatus())
                    .addKeyValue("bytesConsumed", result.getBytesConsumed())
                    .addKeyValue("decoderOffset", state.decoder.getMessageOffset())
                    .addKeyValue("lastCompleteSegment", state.decoder.getLastCompleteSegmentStart())
                    .log("Decode chunk result");

                switch (result.getStatus()) {
                    case SUCCESS:
                    case NEED_MORE_BYTES:
                    case COMPLETED:
                        // All three cases update counters and return any decoded payload
                        // SUCCESS and NEED_MORE_BYTES: partial decode, more data expected
                        // COMPLETED: decode finished successfully
                        state.totalEncodedBytesProcessed.set(state.decoder.getMessageOffset());
                        state.totalBytesDecoded.set(state.decoder.getTotalDecodedPayloadBytes());
                        state.decodedBytesAtLastCompleteSegment = state.decoder.getTotalDecodedPayloadBytes();

                        if (result.getDecodedPayload() != null && result.getDecodedPayload().hasRemaining()) {
                            return Flux.just(result.getDecodedPayload());
                        }
                        return Flux.empty();

                    case INVALID:
                        LOGGER.error("Invalid data during decode: {}", result.getMessage());
                        return Flux.error(new IllegalArgumentException(
                            "Failed to decode structured message: " + result.getMessage()));

                    default:
                        return Flux.error(new IllegalStateException("Unknown decode status: " + result.getStatus()));
                }

            } catch (Exception e) {
                LOGGER.error("Failed to decode structured message chunk: " + e.getMessage(), e);
                return Flux.error(e);
            }
        }).doOnComplete(() -> {
            // Finalize when stream completes
            if (!state.decoder.isComplete()) {
                LOGGER.atInfo()
                    .addKeyValue("messageOffset", state.decoder.getMessageOffset())
                    .addKeyValue("messageLength", state.decoder.getMessageLength())
                    .addKeyValue("totalDecodedPayload", state.decoder.getTotalDecodedPayloadBytes())
                    .addKeyValue("lastCompleteSegment", state.decoder.getLastCompleteSegmentStart())
                    .log("Stream complete but decode not finalized - may retry from lastCompleteSegment");
            } else {
                LOGGER.atInfo()
                    .addKeyValue("messageOffset", state.decoder.getMessageOffset())
                    .addKeyValue("totalDecodedPayload", state.decoder.getTotalDecodedPayloadBytes())
                    .log("Stream complete and decode finalized successfully");
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
        private long decodedBytesAtLastCompleteSegment;

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
            this.decodedBytesAtLastCompleteSegment = 0;
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
         * Also resets decoder state to align with the segment boundary.
         *
         * @return The offset for retry requests (last complete segment boundary).
         */
        public long getRetryOffset() {
            // Use the decoder's last complete segment start as the retry offset
            // This ensures we resume from a segment boundary, not mid-segment
            long retryOffset = decoder.getLastCompleteSegmentStart();
            long decoderOffsetBefore = decoder.getMessageOffset();
            long totalProcessedBefore = totalEncodedBytesProcessed.get();

            LOGGER.atInfo()
                .addKeyValue("retryOffset", retryOffset)
                .addKeyValue("decoderOffsetBefore", decoderOffsetBefore)
                .addKeyValue("totalProcessedBefore", totalProcessedBefore)
                .log("Computing retry offset");

            // Reset decoder to the last complete segment boundary
            // This ensures messageOffset and segment state match the retry offset
            decoder.resetToLastCompleteSegment();

            // Reset totalEncodedBytesProcessed to match the retry offset
            // This ensures absoluteStartOfCombined calculation is correct for retry data
            totalEncodedBytesProcessed.set(retryOffset);

            // Reset totalBytesDecoded to the snapshot at last complete segment
            // This ensures decoded byte counting is correct for retry
            totalBytesDecoded.set(decodedBytesAtLastCompleteSegment);

            LOGGER.atInfo()
                .addKeyValue("retryOffset", retryOffset)
                .addKeyValue("totalProcessedAfter", totalEncodedBytesProcessed.get())
                .addKeyValue("totalDecodedAfter", totalBytesDecoded.get())
                .log("Retry offset calculated (last complete segment boundary)");
            return retryOffset;
        }

        /**
         * Checks if the decoder has finalized.
         *
         * @return true if finalized, false otherwise.
         */
        public boolean isFinalized() {
            return decoder.isComplete();
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
