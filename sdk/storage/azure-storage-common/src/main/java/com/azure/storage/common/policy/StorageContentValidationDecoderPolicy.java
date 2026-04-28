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
 * Pipeline policy that decodes structured messages in storage download responses when
 * CRC64-based content validation is active (i.e., when {@link com.azure.storage.common.ContentValidationAlgorithm}
 * is {@code CRC64} or {@code AUTO}).
 *
 * <p>The policy is activated by the presence of a boolean context key
 * ({@link StructuredMessageConstants#STRUCTURED_MESSAGE_DECODING_CONTEXT_KEY}). It validates per-segment
 * CRC64 checksums during decoding.</p>
 *
 * <p>Emission guarantee: the policy only forwards payload bytes that the
 * {@link StructuredMessageDecoder} has already CRC-validated at the segment boundary. Each invocation creates
 * a fresh decoder and the decoder itself withholds bytes until their enclosing segment passes validation.</p>
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
        if (!shouldApplyDecoding(context)) {
            return next.process();
        }

        context.getHttpRequest()
            .getHeaders()
            .set(Constants.HeaderConstants.STRUCTURED_BODY_TYPE_HEADER_NAME,
                StructuredMessageConstants.STRUCTURED_BODY_TYPE_VALUE);

        return next.process().map(httpResponse -> {
            Long contentLength = getContentLength(httpResponse.getHeaders());

            if (!isEligibleDownload(httpResponse, contentLength)) {
                return httpResponse;
            }

            validateStructuredMessageHeaders(httpResponse);

            long expectedLength = contentLength;
            StructuredMessageDecoder decoder = new StructuredMessageDecoder(expectedLength);

            Flux<ByteBuffer> decodedStream = decodeStream(httpResponse.getBody(), decoder);
            return new DecodedResponse(httpResponse, decodedStream);
        });
    }

    private boolean shouldApplyDecoding(HttpPipelineCallContext context) {
        return context.getData(StructuredMessageConstants.STRUCTURED_MESSAGE_DECODING_CONTEXT_KEY)
            .map(value -> value instanceof Boolean && (Boolean) value)
            .orElse(false);
    }

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

    private static boolean isDownloadResponse(HttpResponse response) {
        return response.getRequest().getHttpMethod() == HttpMethod.GET && response.getStatusCode() / 100 == 2;
    }

    /**
     * @return The content length, or null if absent or unparseable.
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

    private static boolean isEligibleDownload(HttpResponse response, Long contentLength) {
        return isDownloadResponse(response) && contentLength != null && contentLength > 0;
    }

    private Flux<ByteBuffer> decodeStream(Flux<ByteBuffer> encodedFlux, StructuredMessageDecoder decoder) {
        return encodedFlux.concatMap(buffer -> decodeBuffer(buffer, decoder))
            .concatWith(Mono.defer(() -> handleStreamCompletion(decoder)));
    }

    private Flux<ByteBuffer> decodeBuffer(ByteBuffer buffer, StructuredMessageDecoder decoder) {
        if (decoder.isComplete()) {
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
            LOGGER.error("Failed to decode structured message chunk: " + e.getMessage(), e);
            return Flux.error(new IOException("Failed to decode structured message chunk: " + e.getMessage(), e));
        }
    }

    private Mono<ByteBuffer> handleStreamCompletion(StructuredMessageDecoder decoder) {
        if (!decoder.isComplete()) {
            return Mono.error(new IOException("Stream ended prematurely before structured message decoding completed"));
        }
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
