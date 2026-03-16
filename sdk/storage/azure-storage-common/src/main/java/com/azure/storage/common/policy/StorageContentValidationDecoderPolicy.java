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
import com.azure.storage.common.implementation.StorageCrc64Calculator;
import com.azure.storage.common.implementation.structuredmessage.StructuredMessageDecoder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
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
    private static final HttpHeaderName X_MS_STRUCTURED_BODY = HttpHeaderName.fromString("x-ms-structured-body");
    private static final HttpHeaderName X_MS_STRUCTURED_CONTENT_LENGTH
        = HttpHeaderName.fromString("x-ms-structured-content-length");

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
            LOGGER.atVerbose()
                .addKeyValue("thread", Thread.currentThread().getName())
                .log("StorageContentValidationDecoderPolicy received response");
            // Only apply decoding to download responses (GET requests with body)
            if (!isDownloadResponse(httpResponse)) {
                LOGGER.atVerbose()
                    .log("StorageContentValidationDecoderPolicy not a download response, passing through");
                return httpResponse;
            }

            DownloadContentValidationOptions validationOptions = getValidationOptions(context);
            // Structured messages are scoped to a single response; always decode per response.
            boolean responseScoped = true;
            // Decoder expected length must match the 8-byte "message length" in the XSM body header, which is the
            // encoded stream size. Use Content-Length (not x-ms-structured-content-length) so validation passes.
            Long contentLength = getContentLength(httpResponse.getHeaders(), responseScoped);

            if (validationOptions != null && validationOptions.isStructuredMessageValidationEnabled()) {
                String structuredBody = httpResponse.getHeaders().getValue(X_MS_STRUCTURED_BODY);
                String structuredContentLength = httpResponse.getHeaders().getValue(X_MS_STRUCTURED_CONTENT_LENGTH);
                if (structuredBody == null || structuredContentLength == null) {
                    throw LOGGER.logExceptionAsError(new IllegalStateException(
                        "Structured message was requested but the response did not acknowledge it."));
                }
            }

            if (contentLength != null && contentLength > 0 && validationOptions != null) {
                long expectedLength = contentLength;
                LOGGER.atVerbose()
                    .addKeyValue("expectedLength", expectedLength)
                    .addKeyValue("thread", Thread.currentThread().getName())
                    .log("StorageContentValidationDecoderPolicy creating decoder");

                AtomicReference<DecoderState> decoderStateHolder = null;
                Object decoderStateHolderObj
                    = context.getData(Constants.STRUCTURED_MESSAGE_DECODER_STATE_REF_CONTEXT_KEY).orElse(null);
                if (decoderStateHolderObj instanceof AtomicReference) {
                    @SuppressWarnings("unchecked")
                    AtomicReference<DecoderState> tmp = (AtomicReference<DecoderState>) decoderStateHolderObj;
                    decoderStateHolder = tmp;
                }

                // Always create a new decoder per response (matches .NET behavior for structured messages).
                AggregateCrcState aggregateCrcState
                    = context.getData(Constants.STRUCTURED_MESSAGE_AGGREGATE_CRC_CONTEXT_KEY)
                        .filter(value -> value instanceof AggregateCrcState)
                        .map(value -> (AggregateCrcState) value)
                        .orElse(null);

                DecoderState decoderState = new DecoderState(expectedLength, aggregateCrcState);

                Object skipBytesObj
                    = context.getData(Constants.STRUCTURED_MESSAGE_DECODER_SKIP_BYTES_CONTEXT_KEY).orElse(null);
                if (skipBytesObj instanceof Number) {
                    decoderState.setDecodedBytesToSkip(((Number) skipBytesObj).longValue());
                }

                if (decoderStateHolder != null) {
                    decoderStateHolder.set(decoderState);
                }

                // Decode using the stateful decoder
                Flux<ByteBuffer> decodedStream = decodeStream(httpResponse.getBody(), decoderState)
                    .doOnSubscribe(s -> LOGGER.atVerbose()
                        .addKeyValue("thread", Thread.currentThread().getName())
                        .log("StorageContentValidationDecoderPolicy decoded flux subscribed"))
                    .doOnComplete(() -> LOGGER.atVerbose()
                        .addKeyValue("thread", Thread.currentThread().getName())
                        .log("StorageContentValidationDecoderPolicy decoded flux completed"))
                    .doOnError(e -> LOGGER.atVerbose()
                        .addKeyValue("thread", Thread.currentThread().getName())
                        .addKeyValue("error", e)
                        .log("StorageContentValidationDecoderPolicy decoded flux error"));

                LOGGER.atVerbose()
                    .addKeyValue("thread", Thread.currentThread().getName())
                    .log("StorageContentValidationDecoderPolicy returning DecodedResponse");
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
            // If decoding already completed, ignore subsequent buffers.
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

                updateProgress(state);

                switch (result.getStatus()) {
                    case SUCCESS:
                    case NEED_MORE_BYTES:
                    case COMPLETED:
                        return emitDecodedPayload(state, result.getDecodedPayload());

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
            // If decoding already completed, suppress late downstream errors (mirror .NET).
            if (state.decoder.isComplete()) {
                LOGGER.atInfo().log("Decoder complete; suppressing downstream error and completing successfully");
                return Flux.empty();
            }
            state.addSegmentsToAggregateIfNeeded();
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
                state.addSegmentsToAggregateIfNeeded();
                if (state.aggregateCrcState != null && state.aggregateCrcState.hasSegments()) {
                    long composed = state.aggregateCrcState.composeCrc();
                    long calculated = state.aggregateCrcState.getRunningCrc();
                    if (composed != calculated) {
                        return Mono.error(LOGGER.logExceptionAsError(new IllegalArgumentException(
                            "CRC64 mismatch detected in composed structured message. Expected: " + composed + ", got: "
                                + calculated)));
                    }
                }
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
            state.decodedBytesAtLastCompleteSegment = state.decoder.getDecodedBytesAtLastCompleteSegment();
            state.lastCompleteSegmentStart = currentLastCompleteSegment;

            LOGGER.atInfo()
                .addKeyValue("newSegmentBoundary", currentLastCompleteSegment)
                .addKeyValue("decodedBytesAtBoundary", state.decodedBytesAtLastCompleteSegment)
                .log("Segment boundary crossed, updated decoded bytes snapshot");
        }

        long encodedProgress = state.decoder.getMessageOffset() + state.decoder.getPendingEncodedByteCount();
        state.totalEncodedBytesProcessed.set(encodedProgress);
    }

    private Flux<ByteBuffer> emitDecodedPayload(DecoderState state, ByteBuffer decodedPayload) {
        if (decodedPayload == null || !decodedPayload.hasRemaining()) {
            return Flux.empty();
        }

        long skip = state.decodedBytesToSkip.get();
        if (skip > 0) {
            if (skip >= decodedPayload.remaining()) {
                state.decodedBytesToSkip.addAndGet(-decodedPayload.remaining());
                return Flux.empty();
            } else {
                int skipCount = (int) skip;
                decodedPayload.position(decodedPayload.position() + skipCount);
                decodedPayload = decodedPayload.slice();
                state.decodedBytesToSkip.addAndGet(-skipCount);
            }
        }

        if (!decodedPayload.hasRemaining()) {
            return Flux.empty();
        }

        // Return a defensive copy to avoid any inadvertent position/limit side effects.
        ByteBuffer copy = ByteBuffer.allocate(decodedPayload.remaining());
        copy.put(decodedPayload.duplicate());
        copy.flip();

        state.totalBytesDecoded.addAndGet(copy.remaining());
        if (state.aggregateCrcState != null) {
            state.aggregateCrcState.appendPayload(copy.asReadOnlyBuffer());
        }
        return Flux.just(copy);
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
        long retryOffset = state.getRetryOffset();
        long decodedSoFar = state.totalBytesDecoded.get();
        long expectedLength = state.decoder.getMessageLength();
        String originalMessage = message != null ? message : "";

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
    private Long getContentLength(HttpHeaders headers, boolean responseScoped) {
        if (!responseScoped) {
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
    private DecoderState getOrCreateDecoderState(HttpPipelineCallContext context, long contentLength,
        boolean responseScoped) {
        if (responseScoped) {
            return new DecoderState(contentLength, null);
        }
        return context.getData(Constants.STRUCTURED_MESSAGE_DECODER_STATE_CONTEXT_KEY)
            .filter(value -> value instanceof DecoderState)
            .map(value -> (DecoderState) value)
            .orElseGet(() -> new DecoderState(contentLength, null));
    }

    /**
     * Aggregates CRC state across retries to match .NET structured message retriable stream behavior.
     */
    public static final class AggregateCrcState {
        private final List<StructuredMessageDecoder.SegmentInfo> segments = new ArrayList<>();
        private long runningCrc = 0;

        void appendPayload(ByteBuffer payload) {
            if (payload == null || !payload.hasRemaining()) {
                return;
            }
            ByteBuffer copy = payload.asReadOnlyBuffer();
            byte[] data = new byte[copy.remaining()];
            copy.get(data);
            runningCrc = StorageCrc64Calculator.compute(data, runningCrc);
        }

        void addSegments(List<StructuredMessageDecoder.SegmentInfo> newSegments) {
            if (newSegments == null || newSegments.isEmpty()) {
                return;
            }
            segments.addAll(newSegments);
        }

        boolean hasSegments() {
            return !segments.isEmpty();
        }

        long getRunningCrc() {
            return runningCrc;
        }

        long composeCrc() {
            if (segments.isEmpty()) {
                return 0;
            }
            long composed = segments.get(0).getCrc64();
            long totalLength = segments.get(0).getLength();
            for (int i = 1; i < segments.size(); i++) {
                StructuredMessageDecoder.SegmentInfo next = segments.get(i);
                composed
                    = StorageCrc64Calculator.concat(0, 0, composed, totalLength, 0, next.getCrc64(), next.getLength());
                totalLength += next.getLength();
            }
            return composed;
        }

        long getTotalLength() {
            long totalLength = 0;
            for (StructuredMessageDecoder.SegmentInfo segment : segments) {
                totalLength += segment.getLength();
            }
            return totalLength;
        }
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

    private boolean isResponseScoped(HttpPipelineCallContext context) {
        return context.getData(Constants.STRUCTURED_MESSAGE_RESPONSE_SCOPED_CONTEXT_KEY)
            .map(value -> value instanceof Boolean && (Boolean) value)
            .orElse(false);
    }

    /**
     * State holder for the structured message decoder that tracks decoding progress
     * across network interruptions.
     */
    public static class DecoderState {
        private final StructuredMessageDecoder decoder;
        private final long expectedContentLength;
        private final AggregateCrcState aggregateCrcState;
        /**
         * Tracks how many decoded bytes have actually been emitted to the caller (excludes bytes skipped during
         * fast-forward on retry).
         */
        private final AtomicLong totalBytesDecoded;
        private final AtomicLong totalEncodedBytesProcessed;
        private final java.io.ByteArrayOutputStream accumulatedDecoded = new java.io.ByteArrayOutputStream();
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
        private boolean segmentsAddedToAggregate;

        /**
         * Creates a new decoder state.
         *
         * @param expectedContentLength For response-scoped structured messages, the decoded payload length
         * (e.g. from x-ms-structured-content-length); for range responses, the encoded length. The decoder
         * validates this against the message header in the body.
         * @param aggregateCrcState Aggregated CRC state shared across retries, or null if not aggregating.
         */
        public DecoderState(long expectedContentLength, AggregateCrcState aggregateCrcState) {
            this.expectedContentLength = expectedContentLength;
            this.decoder = new StructuredMessageDecoder(expectedContentLength);
            this.totalBytesDecoded = new AtomicLong(0);
            this.totalEncodedBytesProcessed = new AtomicLong(0);
            this.decodedBytesAtLastCompleteSegment = 0;
            this.aggregateCrcState = aggregateCrcState;
            this.segmentsAddedToAggregate = false;
        }

        private void addSegmentsToAggregateIfNeeded() {
            if (segmentsAddedToAggregate || aggregateCrcState == null) {
                return;
            }
            aggregateCrcState.addSegments(decoder.getCompletedSegments());
            segmentsAddedToAggregate = true;
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
         * Gets the total decoded payload bytes for this response.
         *
         * @return The decoded payload length.
         */
        public long getDecodedPayloadLength() {
            return decoder.getTotalDecodedPayloadBytes();
        }

        /**
         * Gets the composed CRC64 for the decoded payload in this response.
         *
         * @return The composed CRC64 value.
         */
        public long getComposedCrc64() {
            if (aggregateCrcState != null && aggregateCrcState.hasSegments()) {
                return aggregateCrcState.composeCrc();
            }

            List<StructuredMessageDecoder.SegmentInfo> segments = decoder.getCompletedSegments();
            if (segments.isEmpty()) {
                return 0;
            }

            long composed = segments.get(0).getCrc64();
            long totalLength = segments.get(0).getLength();
            for (int i = 1; i < segments.size(); i++) {
                StructuredMessageDecoder.SegmentInfo next = segments.get(i);
                composed
                    = StorageCrc64Calculator.concat(0, 0, composed, totalLength, 0, next.getCrc64(), next.getLength());
                totalLength += next.getLength();
            }
            return composed;
        }

        /**
         * Gets the composed decoded payload length represented by validated segments.
         *
         * @return The composed payload length.
         */
        public long getComposedLength() {
            if (aggregateCrcState != null && aggregateCrcState.hasSegments()) {
                return aggregateCrcState.getTotalLength();
            }

            List<StructuredMessageDecoder.SegmentInfo> segments = decoder.getCompletedSegments();
            long totalLength = 0;
            for (StructuredMessageDecoder.SegmentInfo segment : segments) {
                totalLength += segment.getLength();
            }
            return totalLength;
        }

        /**
         * Gets the decoded offset to use for retry requests.
         * This uses the last complete segment boundary to ensure retries
         * resume from a valid segment boundary, not mid-segment.
         *
         * Also resets decoder state to align with the segment boundary.
         *
         * @return The offset for retry requests (last complete segment boundary).
         */
        public long getRetryOffset() {
            // Use the decoded byte count at the last complete segment boundary for retry offset.
            long retryOffset = decodedBytesAtLastCompleteSegment;
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
         * @return The retry start offset (decoded byte position) that the next request should use.
         */
        public long prepareForRetry() {
            return resetForRetry();
        }

        /**
         * Resets decoder and counters to the last validated segment boundary and returns the retry offset.
         *
         * @return retry offset (decoded boundary).
         */
        public long resetForRetry() {
            long retryOffset = decodedBytesAtLastCompleteSegment;

            decoder.resetToLastCompleteSegment();
            accumulatedDecoded.reset();

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
         * Resets decoder state while preserving decoded bytes up to the last complete segment boundary.
         * This allows retries to resume from a validated boundary without losing already validated payload
         * when nothing has been emitted downstream yet.
         *
         * @return retry offset (decoded boundary).
         */
        public long resetForRetryPreservingPrefix() {
            long retryOffset = decodedBytesAtLastCompleteSegment;

            decoder.resetToLastCompleteSegment();

            byte[] data = accumulatedDecoded.toByteArray();
            accumulatedDecoded.reset();

            long prefixLength = decodedBytesAtLastCompleteSegment;
            if (prefixLength > 0 && data.length > 0) {
                int keep = (int) Math.min(prefixLength, data.length);
                accumulatedDecoded.write(data, 0, keep);
            }

            // Align encoded counters to the boundary we will resume from so subsequent progress tracking is consistent.
            totalEncodedBytesProcessed.set(decoder.getMessageOffset() + decoder.getPendingEncodedByteCount());
            decodedBytesToSkip.set(0);

            LOGGER.atInfo()
                .addKeyValue("retryOffset", retryOffset)
                .addKeyValue("decoderOffset", decoder.getMessageOffset())
                .addKeyValue("decodedPrefixBytes", decodedBytesAtLastCompleteSegment)
                .log("Prepared decoder state for smart retry preserving validated prefix");

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

        /**
         * @param decoded Append decoded bytes produced so far in the current decode attempt.
         *
         */
        public void appendPartial(ByteBuffer decoded) {
            if (decoded == null || !decoded.hasRemaining()) {
                return;
            }
            ByteBuffer copy = decoded.asReadOnlyBuffer();
            byte[] data = new byte[copy.remaining()];
            copy.get(data);
            accumulatedDecoded.write(data, 0, data.length);
        }

        /**
         * Drains and returns all accumulated decoded bytes for this message.
         *
         * @return ByteBuffer containing all decoded bytes or null if none accumulated.
         */
        public ByteBuffer drainPartial() {
            byte[] data = accumulatedDecoded.toByteArray();
            accumulatedDecoded.reset();
            return data.length == 0 ? null : ByteBuffer.wrap(data);
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
            // Ensure the original response is closed once the decoded stream completes.
            return Flux.using(() -> originalResponse, r -> decodedBody, HttpResponse::close);
        }

        @Override
        public Mono<byte[]> getBodyAsByteArray() {
            return FluxUtil.collectBytesInByteBufferStream(getBody());
        }

        @Override
        public Mono<String> getBodyAsString() {
            return getBodyAsByteArray().map(bytes -> new String(bytes, Charset.defaultCharset()));
        }

        @Override
        public Mono<String> getBodyAsString(Charset charset) {
            return getBodyAsByteArray().map(bytes -> new String(bytes, charset));
        }

        @Override
        public void close() {
            originalResponse.close();
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
