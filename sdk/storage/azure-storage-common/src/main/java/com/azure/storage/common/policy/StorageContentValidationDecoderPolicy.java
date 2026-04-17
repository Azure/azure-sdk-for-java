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
import com.azure.storage.common.implementation.contentvalidation.StructuredMessageConstants;
import com.azure.storage.common.implementation.contentvalidation.StructuredMessageDecoder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Pipeline policy that decodes structured messages in storage download responses when
 * CRC64-based content validation is active (i.e., when {@link com.azure.storage.common.ContentValidationAlgorithm}
 * is {@code CRC64} or {@code AUTO}).
 *
 * <p>The policy is activated by the presence of a boolean context key
 * ({@link Constants#STRUCTURED_MESSAGE_DECODING_CONTEXT_KEY}). It validates per-segment
 * CRC64 checksums during decoding. Since this policy is placed at the front of the pipeline
 * (before the retry policy), each retry re-enters the policy with a fresh response body,
 * so no cross-retry state management is needed.</p>
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

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        if (!shouldApplyDecoding(context)) {
            return next.process();
        }

        context.getHttpRequest()
            .getHeaders()
            .set(X_MS_STRUCTURED_BODY, StructuredMessageConstants.STRUCTURED_BODY_TYPE_VALUE);

        return next.process().map(httpResponse -> {
            Long contentLength = ContentValidationDecoderUtils.getContentLength(httpResponse.getHeaders());

            if (!ContentValidationDecoderUtils.isEligibleDownload(httpResponse, contentLength)) {
                LOGGER.atVerbose().log("Not a download response with content, passing through");
                return httpResponse;
            }

            validateStructuredMessageHeaders(httpResponse);

            long expectedLength = contentLength;
            StructuredMessageDecoder decoder = new StructuredMessageDecoder(expectedLength);

            Flux<ByteBuffer> decodedStream = decodeStream(httpResponse.getBody(), decoder);

            LOGGER.atVerbose()
                .addKeyValue("expectedLength", expectedLength)
                .log("Returning DecodedResponse with structured message decoding");
            return new DecodedResponse(httpResponse, decodedStream);
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

    private Flux<ByteBuffer> decodeStream(Flux<ByteBuffer> encodedFlux, StructuredMessageDecoder decoder) {
        return encodedFlux.concatMap(buffer -> decodeBuffer(buffer, decoder))
            .onErrorResume(throwable -> handleStreamError(throwable, decoder))
            .concatWith(Mono.defer(() -> handleStreamCompletion(decoder)));
    }

    private Flux<ByteBuffer> decodeBuffer(ByteBuffer buffer, StructuredMessageDecoder decoder) {
        if (decoder.isComplete()) {
            LOGGER.atVerbose()
                .addKeyValue("bufferLength", buffer == null ? "null" : buffer.remaining())
                .log("Decoder already completed; ignoring extra buffer");
            return Flux.empty();
        }

        if (buffer == null || !buffer.hasRemaining()) {
            return Flux.empty();
        }

        LOGGER.atVerbose()
            .addKeyValue("newBytes", buffer.remaining())
            .addKeyValue("decoderOffset", decoder.getMessageOffset())
            .addKeyValue("lastCompleteSegment", decoder.getLastCompleteSegmentStart())
            .addKeyValue("totalDecodedPayload", decoder.getTotalDecodedPayloadBytes())
            .log("Received buffer in decodeStream");

        try {
            StructuredMessageDecoder.DecodeResult result = decoder.decodeChunk(buffer);

            LOGGER.atVerbose()
                .addKeyValue("status", result.getStatus())
                .addKeyValue("bytesConsumed", result.getBytesConsumed())
                .addKeyValue("decoderOffset", decoder.getMessageOffset())
                .addKeyValue("lastCompleteSegment", decoder.getLastCompleteSegmentStart())
                .log("Decode chunk result");

            switch (result.getStatus()) {
                case NEED_MORE_BYTES:
                    return emitDecodedPayload(result.getDecodedPayload());

                case COMPLETED:
                    return emitDecodedPayload(result.getDecodedPayload());

                case INVALID:
                    LOGGER.error("Invalid data during decode: {}", result.getMessage());
                    return Flux.error(new IOException("Failed to decode structured message: " + result.getMessage()));

                default:
                    return Flux.error(new IllegalStateException("Unknown decode status: " + result.getStatus()));
            }
        } catch (Exception e) {
            LOGGER.error("Failed to decode structured message chunk: " + e.getMessage(), e);
            return Flux.error(new IOException("Failed to decode structured message chunk: " + e.getMessage(), e));
        }
    }

    private Flux<ByteBuffer> handleStreamError(Throwable throwable, StructuredMessageDecoder decoder) {
        if (decoder.isComplete()) {
            LOGGER.atVerbose().log("Decoder complete; suppressing downstream error and completing successfully");
            return Flux.empty();
        }

        return Flux.error(throwable);
    }

    private Mono<ByteBuffer> handleStreamCompletion(StructuredMessageDecoder decoder) {
        if (!decoder.isComplete()) {
            LOGGER.atVerbose()
                .addKeyValue("messageOffset", decoder.getMessageOffset())
                .addKeyValue("messageLength", decoder.getMessageLength())
                .addKeyValue("totalDecodedPayload", decoder.getTotalDecodedPayloadBytes())
                .addKeyValue("lastCompleteSegment", decoder.getLastCompleteSegmentStart())
                .log("Stream ended but decode not finalized");
            return Mono.error(new IOException("Stream ended prematurely before structured message decoding completed"));
        }

        LOGGER.atVerbose()
            .addKeyValue("messageOffset", decoder.getMessageOffset())
            .addKeyValue("totalDecodedPayload", decoder.getTotalDecodedPayloadBytes())
            .log("Stream complete and decode finalized successfully");
        return Mono.empty();
    }

    private static Flux<ByteBuffer> emitDecodedPayload(ByteBuffer decodedPayload) {
        if (decodedPayload == null || !decodedPayload.hasRemaining()) {
            return Flux.empty();
        }

        ByteBuffer copy = ByteBuffer.allocate(decodedPayload.remaining());
        copy.put(decodedPayload.duplicate());
        copy.flip();

        return Flux.just(copy);
    }
}
