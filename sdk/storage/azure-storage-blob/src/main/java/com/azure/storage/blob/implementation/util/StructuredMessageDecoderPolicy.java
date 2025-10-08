// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.implementation.structuredmessage.StructuredMessageDecoder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

/**
 * This policy decodes structured messages in HTTP responses using the StructuredMessageDecoder.
 * It intercepts the response body and decodes it on-the-fly.
 */
public class StructuredMessageDecoderPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(StructuredMessageDecoderPolicy.class);
    private final long expectedContentLength;

    /**
     * Creates a new StructuredMessageDecoderPolicy.
     *
     * @param expectedContentLength The expected content length after decoding.
     */
    public StructuredMessageDecoderPolicy(long expectedContentLength) {
        this.expectedContentLength = expectedContentLength;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return next.process().flatMap(response -> {
            // Get the Content-Length from the response headers
            String contentLengthHeader = response.getHeaderValue(HttpHeaderName.CONTENT_LENGTH);

            if (contentLengthHeader == null) {
                LOGGER.warning("Content-Length header not found in response, skipping structured message decoding.");
                return Mono.just(response);
            }

            // Create a decoder for this response
            StructuredMessageDecoder decoder = new StructuredMessageDecoder(expectedContentLength);

            // Wrap the response body to decode it
            Flux<ByteBuffer> decodedBody = response.getBody().map(buffer -> {
                try {
                    return decoder.decode(buffer);
                } catch (Exception e) {
                    LOGGER.error("Error decoding structured message", e);
                    throw new RuntimeException("Failed to decode structured message", e);
                }
            }).doOnComplete(() -> {
                try {
                    decoder.finalizeDecoding();
                } catch (Exception e) {
                    LOGGER.error("Error finalizing structured message decoding", e);
                    throw new RuntimeException("Failed to finalize structured message decoding", e);
                }
            });

            // Create a new response with the decoded body
            return Mono.just(new HttpResponseWrapper(response, decodedBody));
        });
    }

    @Override
    public HttpPipelinePosition getPipelinePosition() {
        return HttpPipelinePosition.PER_CALL;
    }

    /**
     * Wrapper class to replace the response body with decoded content.
     */
    private static class HttpResponseWrapper extends HttpResponse {
        private final HttpResponse originalResponse;
        private final Flux<ByteBuffer> decodedBody;

        HttpResponseWrapper(HttpResponse originalResponse, Flux<ByteBuffer> decodedBody) {
            super(originalResponse.getRequest());
            this.originalResponse = originalResponse;
            this.decodedBody = decodedBody;
        }

        @Override
        public int getStatusCode() {
            return originalResponse.getStatusCode();
        }

        @Override
        @Deprecated
        public String getHeaderValue(String name) {
            return originalResponse.getHeaderValue(name);
        }

        @Override
        public String getHeaderValue(HttpHeaderName headerName) {
            return originalResponse.getHeaderValue(headerName);
        }

        @Override
        public com.azure.core.http.HttpHeaders getHeaders() {
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
            return getBodyAsByteArray().map(String::new);
        }

        @Override
        public Mono<String> getBodyAsString(java.nio.charset.Charset charset) {
            return getBodyAsByteArray().map(bytes -> new String(bytes, charset));
        }

        @Override
        public void close() {
            originalResponse.close();
        }
    }
}
