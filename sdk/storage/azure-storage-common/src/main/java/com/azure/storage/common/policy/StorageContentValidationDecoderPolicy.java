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
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.DownloadContentValidationOptions;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.structuredmessage.StructuredMessageDecoder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final String EXPECTED_LENGTH_CONTEXT_KEY = "azStructuredMsgExpectedLength";

    /**
     * Machine-readable token pattern for extracting retry start offset from exception messages.
     * Format: RETRY-START-OFFSET={number}
     */
    private static final String RETRY_OFFSET_TOKEN = "RETRY-START-OFFSET=";
    private static final Pattern RETRY_OFFSET_PATTERN = Pattern.compile("RETRY-START-OFFSET=(\\d+)");

    /**
     * Creates a new instance of {@link StorageContentValidationDecoderPolicy}.
     */
    public StorageContentValidationDecoderPolicy() {
    }

    @Override
    public HttpPipelinePosition getPipelinePosition() {
        // Run on every retry so the state stored in the call context is available across attempts.
        return HttpPipelinePosition.PER_RETRY;
    }

    /**
     * Parses the retry start offset from an exception message containing the RETRY-START-OFFSET token.
     *
     * @param message The exception message to parse.
     * @return The retry start offset, or -1 if not found.
     */
    public static long parseRetryStartOffset(String message) {
        if (message == null) {
            return -1;
        }
        Matcher matcher = RETRY_OFFSET_PATTERN.matcher(message);
        if (matcher.find()) {
            try {
                return Long.parseLong(matcher.group(1));
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }

    /**
     * Parses decoder offset information from enriched exception messages.
     * Format: "[decoderOffset=X,lastCompleteSegment=Y]"
     *
     * @param message The exception message to parse.
     * @return A long array [decoderOffset, lastCompleteSegment], or null if not found.
     */
    public static long[] parseDecoderOffsets(String message) {
        if (message == null) {
            return null;
        }
        // Pattern: [decoderOffset=123,lastCompleteSegment=456]
        Pattern pattern = Pattern.compile("\\[decoderOffset=(\\d+),lastCompleteSegment=(\\d+)\\]");
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            try {
                long decoderOffset = Long.parseLong(matcher.group(1));
                long lastCompleteSegment = Long.parseLong(matcher.group(2));
                return new long[] { decoderOffset, lastCompleteSegment };
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Attempts to extract the decoder state from a decoded response instance.
     *
     * @param response The HTTP response returned from the pipeline.
     * @return The decoder state if present, otherwise null.
     */
    public static DecoderState tryGetDecoderState(HttpResponse response) {
        if (response instanceof DecodedResponse) {
            return ((DecodedResponse) response).getDecoderState();
        }
        return null;
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
                // Preserve the original encoded length across retries; range responses may advertise smaller lengths.
                long expectedLength = context.getData(EXPECTED_LENGTH_CONTEXT_KEY)
                    .filter(Long.class::isInstance)
                    .map(Long.class::cast)
                    .orElse(contentLength);

                // Cache for subsequent retries.
                context.setData(EXPECTED_LENGTH_CONTEXT_KEY, expectedLength);

                AtomicReference<DecoderState> decoderStateHolder = null;
                Object decoderStateHolderObj
                    = context.getData(Constants.STRUCTURED_MESSAGE_DECODER_STATE_REF_CONTEXT_KEY).orElse(null);
                if (decoderStateHolderObj instanceof AtomicReference) {
                    @SuppressWarnings("unchecked")
                    AtomicReference<DecoderState> tmp = (AtomicReference<DecoderState>) decoderStateHolderObj;
                    decoderStateHolder = tmp;
                }

                // Get or create decoder with state tracking
                DecoderState decoderState = getOrCreateDecoderState(context, expectedLength);

                if (decoderStateHolder != null) {
                    decoderStateHolder.set(decoderState);
                }

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
     * <p>When an error occurs or the stream ends prematurely, an IOException is thrown with a
     * machine-readable token RETRY-START-OFFSET=&lt;number&gt; that can be parsed to determine
     * the correct offset for retry requests.</p>
     *
     * @param encodedFlux The flux of encoded byte buffers.
     * @param state The decoder state.
     * @return A flux of decoded byte buffers.
     */
    private Flux<ByteBuffer> decodeStream(Flux<ByteBuffer> encodedFlux, DecoderState state) {
        return encodedFlux.concatMap(encodedBuffer -> {
            // If decoding already completed, drop any subsequent buffers (can happen with late keep-alive frames).
            if (state.decoder.isComplete()) {
                LOGGER.atVerbose()
                    .addKeyValue("bufferLength", encodedBuffer == null ? "null" : encodedBuffer.remaining())
                    .log("Decoder already completed; ignoring extra buffer");
                return Flux.empty();
            }

            // Skip empty buffers that may be emitted by reactor-netty
            if (encodedBuffer == null || !encodedBuffer.hasRemaining()) {
                LOGGER.atVerbose()
                    .addKeyValue("bufferLength", encodedBuffer == null ? "null" : encodedBuffer.remaining())
                    .log("Skipping empty/null buffer in decodeStream");
                return Flux.empty();
            }

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
                        // Update counters but defer emitting payload until we have a fully validated message.
                        updateProgress(state);
                        return Flux.empty();

                    case COMPLETED:
                        updateProgress(state);
                        ByteBuffer decodedPayload = result.getDecodedPayload();
                        if (decodedPayload != null && decodedPayload.hasRemaining()) {
                            long skip = state.decodedBytesToSkip.get();
                            if (skip > 0) {
                                if (skip >= decodedPayload.remaining()) {
                                    state.decodedBytesToSkip.addAndGet(-decodedPayload.remaining());
                                    decodedPayload = null;
                                } else {
                                    int skipCount = (int) skip;
                                    decodedPayload.position(decodedPayload.position() + skipCount);
                                    decodedPayload = decodedPayload.slice();
                                    state.decodedBytesToSkip.addAndGet(-skipCount);
                                }
                            }

                            if (decodedPayload != null && decodedPayload.hasRemaining()) {
                                state.totalBytesDecoded.addAndGet(decodedPayload.remaining());
                                return Flux.just(decodedPayload);
                            }
                        }
                        return Flux.empty();

                    case INVALID:
                        LOGGER.error("Invalid data during decode: {}", result.getMessage());
                        return Flux.error(createRetryableException(state,
                            "Failed to decode structured message: " + result.getMessage()));

                    default:
                        return Flux.error(new IllegalStateException("Unknown decode status: " + result.getStatus()));
                }

            } catch (Exception e) {
                LOGGER.error("Failed to decode structured message chunk: " + e.getMessage(), e);
                return Flux.error(createRetryableException(state, e.getMessage(), e));
            }
        }).onErrorResume(throwable -> {
            // If decoding already completed and we emitted payload, suppress late downstream errors (mirror .NET).
            // If no payload was emitted, surface the error so the retriable download can resume properly.
            if (state.decoder.isComplete()) {
                if (state.totalBytesDecoded.get() > 0) {
                    LOGGER.atInfo().log("Decoder complete; suppressing downstream error and completing successfully");
                    return Flux.empty();
                } else {
                    LOGGER.atInfo().log("Decoder complete with no emitted payload; propagating error to retry");
                }
            }
            // Wrap any error with retry offset information
            if (throwable instanceof IOException) {
                // Check if already has retry offset token
                if (throwable.getMessage() != null && throwable.getMessage().contains(RETRY_OFFSET_TOKEN)) {
                    return Flux.error(throwable);
                }
            }
            // Wrap the error with retry offset
            return Flux.error(createRetryableException(state, throwable.getMessage(), throwable));
        }).concatWith(Mono.defer(() -> {
            // Check on completion if decode is finished - if not, throw with retry offset
            if (!state.decoder.isComplete()) {
                LOGGER.atInfo()
                    .addKeyValue("messageOffset", state.decoder.getMessageOffset())
                    .addKeyValue("messageLength", state.decoder.getMessageLength())
                    .addKeyValue("totalDecodedPayload", state.decoder.getTotalDecodedPayloadBytes())
                    .addKeyValue("lastCompleteSegment", state.decoder.getLastCompleteSegmentStart())
                    .log("Stream ended but decode not finalized - throwing retryable exception");
                return Mono.error(createRetryableException(state,
                    "Stream ended prematurely before structured message decoding completed"));
            } else {
                LOGGER.atInfo()
                    .addKeyValue("messageOffset", state.decoder.getMessageOffset())
                    .addKeyValue("totalDecodedPayload", state.decoder.getTotalDecodedPayloadBytes())
                    .log("Stream complete and decode finalized successfully");
                return Mono.empty();
            }
        }));
    }

    /**
     * Updates progress counters based on the current decoder state and logs when a new validated segment boundary is
     * crossed. This keeps encoded progress in sync with the decoder while deferring payload emission until the entire
     * message is validated.
     */
    private void updateProgress(DecoderState state) {
        long currentLastCompleteSegment = state.decoder.getLastCompleteSegmentStart();

        // Only update decodedBytesAtLastCompleteSegment when the boundary changes (new segment validated).
        if (state.lastCompleteSegmentStart != currentLastCompleteSegment) {
            state.decodedBytesAtLastCompleteSegment = state.decoder.getTotalDecodedPayloadBytes();
            state.lastCompleteSegmentStart = currentLastCompleteSegment;

            LOGGER.atInfo()
                .addKeyValue("newSegmentBoundary", currentLastCompleteSegment)
                .addKeyValue("decodedBytesAtBoundary", state.decodedBytesAtLastCompleteSegment)
                .log("Segment boundary crossed, updated decoded bytes snapshot");
        }

        long encodedProgress = state.decoder.getMessageOffset() + state.decoder.getPendingEncodedByteCount();
        state.totalEncodedBytesProcessed.set(encodedProgress);
    }

    /**
     * Creates an IOException with the retry start offset encoded in the message.
     *
     * @param state The decoder state.
     * @param message The error message.
     * @return An IOException with retry offset information.
     */
    private IOException createRetryableException(DecoderState state, String message) {
        return createRetryableException(state, message, null);
    }

    /**
     * Creates an IOException with the retry start offset encoded in the message.
     *
     * @param state The decoder state.
     * @param message The error message.
     * @param cause The original cause, may be null.
     * @return An IOException with retry offset information.
     */
    private IOException createRetryableException(DecoderState state, String message, Throwable cause) {
        long retryOffset = state.prepareForRetry();
        long decodedSoFar = state.totalBytesDecoded.get();
        long expectedLength = state.decoder.getMessageLength();

        // Check if the exception message already has decoder offset information
        // If so, prefer lastCompleteSegment from the enriched message
        String originalMessage = message != null ? message : "";
        long[] decoderOffsets = parseDecoderOffsets(originalMessage);
        if (decoderOffsets != null) {
            // Use lastCompleteSegment from the enriched exception as the retry offset
            retryOffset = decoderOffsets[1];  // lastCompleteSegment
            LOGGER.atInfo()
                .addKeyValue("decoderOffset", decoderOffsets[0])
                .addKeyValue("lastCompleteSegment", decoderOffsets[1])
                .log("Parsed decoder offsets from enriched exception");
        }

        // Build message components for clarity
        long displayExpected = expectedLength > 0 ? expectedLength : 0;

        String fullMessage = String.format("Incomplete structured message: decoded %d of %d bytes. %s%d. %s",
            decodedSoFar, displayExpected, RETRY_OFFSET_TOKEN, retryOffset, originalMessage);

        LOGGER.atInfo()
            .addKeyValue("retryOffset", retryOffset)
            .addKeyValue("decodedSoFar", decodedSoFar)
            .addKeyValue("expectedLength", expectedLength)
            .log("Creating retryable exception with offset");

        if (cause != null) {
            return new IOException(fullMessage, cause);
        }
        return new IOException(fullMessage);
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
        // Prefer the total length from Content-Range (if present) so retries use the full encoded length
        // even when the current response is partial.
        String contentRange = headers.getValue(HttpHeaderName.CONTENT_RANGE);
        if (contentRange != null) {
            // Format: bytes start-end/total
            int slash = contentRange.indexOf('/');
            if (slash > -1 && slash + 1 < contentRange.length()) {
                String totalPart = contentRange.substring(slash + 1).trim();
                if (!"*".equals(totalPart)) {
                    try {
                        return Long.parseLong(totalPart);
                    } catch (NumberFormatException e) {
                        LOGGER.warning("Invalid content range total length in response headers: " + contentRange);
                    }
                }
            }
        }

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
        /**
         * Tracks how many decoded bytes have actually been emitted to the caller (excludes bytes skipped during
         * fast-forward on retry).
         */
        private final AtomicLong totalBytesDecoded;
        private final AtomicLong totalEncodedBytesProcessed;
        /**
         * Snapshot of decoded bytes emitted at the last fully validated segment boundary. Used to correlate the encoded
         * retry offset with the decoded offset that RetriableDownloadFlux tracks.
         */
        private long decodedBytesAtLastCompleteSegment;
        private long lastCompleteSegmentStart; // Tracks the last value to detect changes
        /**
         * Number of decoded bytes that should be skipped on the next retry to fast-forward to the caller's decoded
         * offset. This mirrors the .NET StructuredMessageDecodingRetriableStream behavior.
         */
        private final AtomicLong decodedBytesToSkip = new AtomicLong(0);

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
         * Gets the expected encoded content length associated with this decoder state.
         *
         * @return The expected encoded content length.
         */
        public long getExpectedContentLength() {
            return expectedContentLength;
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
            long retryOffset = decoder.getRetryStartOffset();
            long lastCompleteSegmentOffset = decoder.getLastCompleteSegmentStart();

            LOGGER.atInfo()
                .addKeyValue("decoderOffset", decoder.getMessageOffset())
                .addKeyValue("pendingBytes", decoder.getPendingEncodedByteCount())
                .addKeyValue("lastCompleteSegment", lastCompleteSegmentOffset)
                .log("Computed smart-retry offset from decoder state");
            return retryOffset;
        }

        /**
         * Prepares the decoder state for a retry by rewinding the decoder to the last complete segment
         * boundary and resetting the accounting counters to that point. This mirrors the behavior of
         * the cryptography smart-retry implementation which always replays from a validated boundary.
         *
         * @return The retry start offset (encoded byte position) that the next request should use.
         */
        public long prepareForRetry() {
            return resetForRetry();
        }

        /**
         * Resets decoder and counters to the last validated segment boundary and returns the retry offset.
         *
         * @return retry offset (encoded boundary).
         */
        public long resetForRetry() {
            long retryOffset = decoder.getRetryStartOffset();

            decoder.resetToLastCompleteSegment();

            // Align encoded counters to the boundary we will resume from so subsequent progress tracking is consistent.
            totalEncodedBytesProcessed.set(decoder.getMessageOffset() + decoder.getPendingEncodedByteCount());
            decodedBytesToSkip.set(0);

            LOGGER.atInfo()
                .addKeyValue("retryOffset", retryOffset)
                .addKeyValue("decoderOffset", decoder.getMessageOffset())
                .addKeyValue("decodedBytesAtBoundary", decodedBytesAtLastCompleteSegment)
                .log("Prepared decoder state for smart retry");

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

        /**
         * Gets the decoded payload bytes accounted for at the last complete segment boundary.
         * This is used to correlate decoder progress with reliable download offsets.
         *
         * @return The decoded byte count at the last segment boundary.
         */
        public long getDecodedBytesAtLastCompleteSegment() {
            return decodedBytesAtLastCompleteSegment;
        }

        /**
         * Sets how many decoded bytes should be skipped when resuming after a retry. This lets the decoder fast-forward
         * within the current segment to align with the decoded offset already emitted to the user before the failure.
         *
         * @param bytesToSkip decoded bytes to drop from the next decoded payloads.
         */
        public void setDecodedBytesToSkip(long bytesToSkip) {
            decodedBytesToSkip.set(Math.max(0, bytesToSkip));
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
