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
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.contentvalidation.StructuredMessageConstants;
import com.azure.storage.common.implementation.contentvalidation.StructuredMessageDecoder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * HTTP pipeline policy that decodes the storage structured message body returned for downloads when CRC64
 * content validation is active.
 *
 * <p>The policy decides when to opt in (via the context key), tells the service to
 * encode the response (via the request header), constructs the decoder and the wrapper response, and
 * translates decoder-level failures (malformed framing, CRC mismatch, premature end-of-stream) into reactive
 * {@link IOException} errors.</p>
 *
 * <p>This policy uses {@link com.azure.core.http.HttpPipelinePosition#PER_RETRY PER_RETRY} semantics by default, so
 * each retry produces a fresh response that this policy wraps with a fresh decoder. A CRC failure on one attempt
 * cannot pollute another, and the storage download retry logic ({@code BlobAsyncClientBase.downloadStream...}) can
 * resume by reissuing range requests; each new range response is validated end-to-end on its own.</p>
 *
 * <p>Because the wrapped {@link StructuredMessageDecoder} only releases payload bytes after the corresponding
 * segment's CRC has been verified, the {@link DecodedResponse}'s body Flux is guaranteed to contain only validated
 * bytes – callers never see a byte that could later fail validation, even when retries are involved.</p>
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
        // Check if the decoding should be applied.
        if (!shouldApplyDecoding(context)) {
            return next.process();
        }

        // Tell the service we want a structured-message body.
        context.getHttpRequest()
            .getHeaders()
            .set(Constants.HeaderConstants.STRUCTURED_BODY_TYPE_HEADER_NAME,
                StructuredMessageConstants.STRUCTURED_BODY_TYPE_VALUE);

        return next.process().map(httpResponse -> {
            // The HTTP Content-Length is the size of the encoded structured message body. We hand it to the
            // decoder which cross-checks it against the message header.
            Long contentLength = getContentLength(httpResponse.getHeaders());

            // Only 2xx GET responses with a positive content length carry a body that we can decode.
            if (!isEligibleDownload(httpResponse, contentLength)) {
                return httpResponse;
            }

            // Confirm the service actually honored our structured-body request before we hand the body to the decoder.
            validateStructuredMessageHeaders(httpResponse);

            long expectedLength = contentLength;
            // Fresh decoder per response so retries each get a clean state machine.
            StructuredMessageDecoder decoder = new StructuredMessageDecoder(expectedLength);

            Flux<ByteBuffer> decodedStream = decodeStream(httpResponse.getBody(), decoder);
            return new DecodedResponse(httpResponse, decodedStream);
        });
    }

    /**
     * @return true when the request carries the boolean opt-in flag set 
     * by {@code ContentValidationModeResolver.addStructuredMessageDecodingToContext}.
     */
    private boolean shouldApplyDecoding(HttpPipelineCallContext context) {
        return context.getData(StructuredMessageConstants.STRUCTURED_MESSAGE_DECODING_CONTEXT_KEY)
            .map(value -> value instanceof Boolean && (Boolean) value)
            .orElse(false);
    }

    /**
     * Verifies the response acknowledges the structured-body request: presence of the
     * {@code x-ms-structured-body} header and the {@code x-ms-structured-content-length} 
     * header. If either is missing, the service is sending us a normal body and we must not run the decoder over it.
     */
    private void validateStructuredMessageHeaders(HttpResponse httpResponse) {
        String structuredBody
            = httpResponse.getHeaders().getValue(Constants.HeaderConstants.STRUCTURED_BODY_TYPE_HEADER_NAME);
        String structuredContentLength
            = httpResponse.getHeaders().getValue(Constants.HeaderConstants.STRUCTURED_CONTENT_LENGTH_HEADER_NAME);
        if (structuredBody == null || structuredContentLength == null) {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException("Structured message was requested but the response did not acknowledge it."));
        }
    }

    /**
     * Reads {@code Content-Length} as a {@code long}, returning {@code null} when the header is missing or
     * unparseable so callers can simply skip decoding for non-bodied responses.
     */
    private static Long getContentLength(HttpHeaders headers) {
        String value = headers.getValue(HttpHeaderName.CONTENT_LENGTH);
        if (value != null) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                // Header invalid; treat as not eligible.
            }
        }
        return null;
    }

    /**
     * @return true for a 2xx response to a GET request, the only response shape that carries a body we
     * can decode. 206 (Partial Content) on retried range downloads is included.
     */
    private static boolean isDownloadResponse(HttpResponse response) {
        return response.getRequest().getHttpMethod() == HttpMethod.GET && response.getStatusCode() / 100 == 2;
    }

    /**
     * @return true when the response is one we should decode: a 2xx GET with a positive, parseable
     * {@code Content-Length}.
     */
    private static boolean isEligibleDownload(HttpResponse response, Long contentLength) {
        return isDownloadResponse(response) && contentLength != null && contentLength > 0;
    }

    /**
     * Builds the body-decoding Flux: each upstream {@link ByteBuffer} is fed to the decoder in order
     * ({@code concatMap} preserves order and serializes access), and a deferred stream-completion check is
     * appended so a truncated body raises an error instead of completing silently.
     */
    private Flux<ByteBuffer> decodeStream(Flux<ByteBuffer> encodedFlux, StructuredMessageDecoder decoder) {
        return encodedFlux.concatMap(buffer -> decodeBuffer(buffer, decoder))
            .concatWith(Mono.defer(() -> handleStreamCompletion(decoder)));
    }

    /**
     * Feeds a single inbound chunk to the decoder and translates its outputs into reactive emissions:
     * If the decoder reports validated bytes, emit them downstream.
     * If the decoder threw because the input is malformed or a CRC mismatch was detected, surface that as
     * an {@link IOException}.
     * If the decoder is already complete (e.g., extra trailing bytes after the message footer), drop the
     * chunk silently.
     */
    private Flux<ByteBuffer> decodeBuffer(ByteBuffer buffer, StructuredMessageDecoder decoder) {
        if (decoder.isComplete()) {
            // Decoding finished on a previous chunk; ignore any trailing bytes the transport might still emit.
            return Flux.empty();
        }

        if (buffer == null || !buffer.hasRemaining()) {
            return Flux.empty();
        }

        try {
            ByteBuffer validated = decoder.decodeChunk(buffer);
            return emitDecodedPayload(validated);
        } catch (IllegalArgumentException e) {
            return Flux.error(new IOException("Failed to decode structured message: " + e.getMessage(), e));
        } catch (Exception e) {
            // Anything not foreseen by the decoder, log it.
            LOGGER.error("Failed to decode structured message chunk: " + e.getMessage(), e);
            return Flux.error(new IOException("Failed to decode structured message chunk: " + e.getMessage(), e));
        }
    }

    /**
     * Run after the upstream Flux completes. If the decoder is not in a complete state, the response body ended
     * before all expected bytes arrived – surface this as an {@link IOException} so callers don't accept a
     * truncated payload.
     */
    private Mono<ByteBuffer> handleStreamCompletion(StructuredMessageDecoder decoder) {
        if (!decoder.isComplete()) {
            return Mono.error(new IOException("Stream ended prematurely before structured message decoding completed"));
        }
        return Mono.empty();
    }

    /**
     * Wraps the decoder output in a Flux. The decoder hands back a freshly-allocated buffer wrapping its own
     * internal byte array; we make a defensive copy so downstream consumers that aggregate or keep references to
     * the buffer cannot accidentally see the decoder's internal storage if the decoder ever changes to reuse
     * arrays.
     */
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
