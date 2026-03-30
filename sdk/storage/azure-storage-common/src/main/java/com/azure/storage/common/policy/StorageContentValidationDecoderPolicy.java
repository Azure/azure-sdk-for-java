// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.contentvalidation.StructuredMessageDecoder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Pipeline policy that decodes structured messages in storage download responses when
 * CRC64-based content validation is active (i.e., when {@code StorageChecksumAlgorithm}
 * is {@code CRC64} or {@code AUTO}).
 *
 * <p>The policy is activated by the presence of a boolean context key
 * ({@link Constants#STRUCTURED_MESSAGE_DECODING_CONTEXT_KEY}). It validates per-segment
 * CRC64 checksums, tracks decoder progress in {@link DecoderState}, and embeds
 * machine-readable retry offsets in exception messages so the caller can resume from the
 * last validated segment boundary.</p>
 */
public class StorageContentValidationDecoderPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(StorageContentValidationDecoderPolicy.class);
    private static final HttpHeaderName X_MS_STRUCTURED_BODY = HttpHeaderName.fromString("x-ms-structured-body");
    private static final HttpHeaderName X_MS_STRUCTURED_CONTENT_LENGTH
        = HttpHeaderName.fromString("x-ms-structured-content-length");

    /**
     * Creates a new instance of {@link StorageContentValidationDecoderPolicy}.
     */
    public StorageContentValidationDecoderPolicy() {
    }

    @Override
    public HttpPipelinePosition getPipelinePosition() {
        return HttpPipelinePosition.PER_RETRY;
    }

    /**
     * Parses the retry start offset from an exception message containing the
     * {@code RETRY-START-OFFSET} token.
     *
     * @param message The exception message to parse.
     * @return The retry start offset, or -1 if not found.
     */
    public static long parseRetryStartOffset(String message) {
        return ContentValidationDecoderUtils.parseRetryStartOffset(message);
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        if (!shouldApplyDecoding(context)) {
            return next.process();
        }

        return next.process().map(httpResponse -> {
            Long contentLength = ContentValidationDecoderUtils.getContentLength(httpResponse.getHeaders());

            if (!ContentValidationDecoderUtils.isEligibleDownload(httpResponse, contentLength)) {
                LOGGER.atVerbose().log("Not a download response with content, passing through");
                return httpResponse;
            }

            validateStructuredMessageHeaders(httpResponse);

            long expectedLength = contentLength;
            DecoderState decoderState = createDecoderState(context, expectedLength);

            Flux<ByteBuffer> decodedStream = decodeStream(httpResponse.getBody(), decoderState);

            LOGGER.atVerbose()
                .addKeyValue("expectedLength", expectedLength)
                .log("Returning DecodedResponse with structured message decoding");
            return new DecodedResponse(httpResponse, decodedStream, decoderState);
        });
    }

    private boolean shouldApplyDecoding(HttpPipelineCallContext context) {
        return context.getData(Constants.STRUCTURED_MESSAGE_DECODING_CONTEXT_KEY)
            .map(value -> value instanceof Boolean && (Boolean) value)
            .orElse(false);
    }

    private void validateStructuredMessageHeaders(HttpResponse httpResponse) {
        String structuredBody = httpResponse.getHeaders().getValue(X_MS_STRUCTURED_BODY);
        String structuredContentLength = httpResponse.getHeaders().getValue(X_MS_STRUCTURED_CONTENT_LENGTH);
        if (structuredBody == null || structuredContentLength == null) {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException("Structured message was requested but the response did not acknowledge it."));
        }
    }

    private DecoderState createDecoderState(HttpPipelineCallContext context, long expectedLength) {
        AggregateCrcState aggregateCrcState = context.getData(Constants.STRUCTURED_MESSAGE_AGGREGATE_CRC_CONTEXT_KEY)
            .filter(AggregateCrcState.class::isInstance)
            .map(AggregateCrcState.class::cast)
            .orElse(null);

        DecoderState state = new DecoderState(expectedLength, aggregateCrcState);

        context.getData(Constants.STRUCTURED_MESSAGE_DECODER_SKIP_BYTES_CONTEXT_KEY)
            .filter(Number.class::isInstance)
            .map(Number.class::cast)
            .ifPresent(skip -> state.setDecodedBytesToSkip(skip.longValue()));

        getDecoderStateHolder(context).ifPresent(holder -> holder.set(state));

        return state;
    }

    @SuppressWarnings("unchecked")
    private Optional<AtomicReference<DecoderState>> getDecoderStateHolder(HttpPipelineCallContext context) {
        return context.getData(Constants.STRUCTURED_MESSAGE_DECODER_STATE_REF_CONTEXT_KEY)
            .filter(AtomicReference.class::isInstance)
            .map(obj -> (AtomicReference<DecoderState>) obj);
    }

    private Flux<ByteBuffer> decodeStream(Flux<ByteBuffer> encodedFlux, DecoderState state) {
        StructuredMessageDecoder decoder = state.getDecoder();

        return encodedFlux.concatMap(buffer -> decodeBuffer(buffer, state, decoder))
            .onErrorResume(throwable -> handleStreamError(throwable, state, decoder))
            .concatWith(Mono.defer(() -> handleStreamCompletion(state, decoder)));
    }

    private Flux<ByteBuffer> decodeBuffer(ByteBuffer buffer, DecoderState state, StructuredMessageDecoder decoder) {

        if (decoder.isComplete()) {
            LOGGER.atVerbose()
                .addKeyValue("bufferLength", buffer == null ? "null" : buffer.remaining())
                .log("Decoder already completed; ignoring extra buffer");
            return Flux.empty();
        }

        if (buffer == null || !buffer.hasRemaining()) {
            return Flux.empty();
        }

        LOGGER.atInfo()
            .addKeyValue("newBytes", buffer.remaining())
            .addKeyValue("decoderOffset", decoder.getMessageOffset())
            .addKeyValue("lastCompleteSegment", decoder.getLastCompleteSegmentStart())
            .addKeyValue("totalDecodedPayload", decoder.getTotalDecodedPayloadBytes())
            .log("Received buffer in decodeStream");

        try {
            StructuredMessageDecoder.DecodeResult result = decoder.decodeChunk(buffer);

            LOGGER.atInfo()
                .addKeyValue("status", result.getStatus())
                .addKeyValue("bytesConsumed", result.getBytesConsumed())
                .addKeyValue("decoderOffset", decoder.getMessageOffset())
                .addKeyValue("lastCompleteSegment", decoder.getLastCompleteSegmentStart())
                .log("Decode chunk result");

            state.updateProgress();

            switch (result.getStatus()) {
                case SUCCESS:
                case NEED_MORE_BYTES:
                    return handleSuccessOrNeedMore(state, result);

                case COMPLETED:
                    return handleCompleted(state, result);

                case INVALID:
                    return handleInvalid(state, result);

                default:
                    return Flux.error(new IllegalStateException("Unknown decode status: " + result.getStatus()));
            }
        } catch (Exception e) {
            LOGGER.error("Failed to decode structured message chunk: " + e.getMessage(), e);
            return Flux.error(createRetryableException(state, e.getMessage(), e));
        }
    }

    private Flux<ByteBuffer> handleSuccessOrNeedMore(DecoderState state, StructuredMessageDecoder.DecodeResult result) {
        return emitDecodedPayload(state, result.getDecodedPayload());
    }

    private Flux<ByteBuffer> handleCompleted(DecoderState state, StructuredMessageDecoder.DecodeResult result) {
        return emitDecodedPayload(state, result.getDecodedPayload());
    }

    private Flux<ByteBuffer> handleInvalid(DecoderState state, StructuredMessageDecoder.DecodeResult result) {
        LOGGER.error("Invalid data during decode: {}", result.getMessage());
        return Flux
            .error(createRetryableException(state, "Failed to decode structured message: " + result.getMessage()));
    }

    private Flux<ByteBuffer> handleStreamError(Throwable throwable, DecoderState state,
        StructuredMessageDecoder decoder) {

        if (decoder.isComplete()) {
            LOGGER.atInfo().log("Decoder complete; suppressing downstream error and completing successfully");
            return Flux.empty();
        }
        state.addSegmentsToAggregateIfNeeded();

        if (throwable instanceof IOException
            && throwable.getMessage() != null
            && throwable.getMessage().contains(ContentValidationDecoderUtils.RETRY_OFFSET_TOKEN)) {
            return Flux.error(throwable);
        }

        return Flux.error(createRetryableException(state, throwable.getMessage(), throwable));
    }

    private Mono<ByteBuffer> handleStreamCompletion(DecoderState state, StructuredMessageDecoder decoder) {
        if (!decoder.isComplete()) {
            LOGGER.atInfo()
                .addKeyValue("messageOffset", decoder.getMessageOffset())
                .addKeyValue("messageLength", decoder.getMessageLength())
                .addKeyValue("totalDecodedPayload", decoder.getTotalDecodedPayloadBytes())
                .addKeyValue("lastCompleteSegment", decoder.getLastCompleteSegmentStart())
                .log("Stream ended but decode not finalized - throwing retryable exception");
            return Mono.error(createRetryableException(state,
                "Stream ended prematurely before structured message decoding completed"));
        }

        state.addSegmentsToAggregateIfNeeded();

        if (state.aggregateCrcState != null && state.aggregateCrcState.hasSegments()) {
            long composed = state.aggregateCrcState.composeCrc();
            long calculated = state.aggregateCrcState.getRunningCrc();
            if (composed != calculated) {
                return Mono.error(LOGGER.logExceptionAsError(
                    new IllegalArgumentException("CRC64 mismatch detected in composed structured message. Expected: "
                        + composed + ", got: " + calculated)));
            }
        }

        LOGGER.atInfo()
            .addKeyValue("messageOffset", decoder.getMessageOffset())
            .addKeyValue("totalDecodedPayload", decoder.getTotalDecodedPayloadBytes())
            .log("Stream complete and decode finalized successfully");
        return Mono.empty();
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

        ByteBuffer copy = ByteBuffer.allocate(decodedPayload.remaining());
        copy.put(decodedPayload.duplicate());
        copy.flip();

        state.totalBytesDecoded.addAndGet(copy.remaining());
        if (state.aggregateCrcState != null) {
            state.aggregateCrcState.appendPayload(copy.asReadOnlyBuffer());
        }
        return Flux.just(copy);
    }

    private IOException createRetryableException(DecoderState state, String message) {
        return createRetryableException(state, message, null);
    }

    private IOException createRetryableException(DecoderState state, String message, Throwable cause) {
        StructuredMessageDecoder decoder = state.getDecoder();
        long retryOffset = state.getRetryOffset();
        long decodedSoFar = state.totalBytesDecoded.get();
        long expectedLength = decoder.getMessageLength();
        String originalMessage = message != null ? message : "";
        long displayExpected = expectedLength > 0 ? expectedLength : 0;

        String fullMessage
            = String.format("Incomplete structured message: decoded %d of %d bytes. %s%d. %s", decodedSoFar,
                displayExpected, ContentValidationDecoderUtils.RETRY_OFFSET_TOKEN, retryOffset, originalMessage);

        LOGGER.atInfo()
            .addKeyValue("retryOffset", retryOffset)
            .addKeyValue("decodedSoFar", decodedSoFar)
            .addKeyValue("expectedLength", expectedLength)
            .log("Creating retryable exception with offset");

        return cause != null ? new IOException(fullMessage, cause) : new IOException(fullMessage);
    }
}
