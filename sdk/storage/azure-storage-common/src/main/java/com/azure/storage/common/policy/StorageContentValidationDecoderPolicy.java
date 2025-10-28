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
import com.azure.storage.common.implementation.structuredmessage.StructuredMessageDecodingStream;
import com.azure.storage.common.implementation.structuredmessage.StatefulStructuredMessageDecoder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * This is a decoding policy in an {@link com.azure.core.http.HttpPipeline} to decode structured messages in
 * storage download requests. The policy checks for a context value to determine when to apply structured message decoding.
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
                // Get or create stateful decoder
                StatefulStructuredMessageDecoder decoder = getOrCreateDecoder(context, contentLength);
                
                // Decode using the stateful decoder
                Flux<ByteBuffer> decodedStream = decoder.decode(httpResponse.getBody());
                
                // Update context with decoder state for potential retries
                context.setData(Constants.STRUCTURED_MESSAGE_DECODER_STATE_CONTEXT_KEY, decoder);
                
                return new DecodedResponse(httpResponse, decodedStream, decoder);
            }

            return httpResponse;
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
     * Gets or creates a stateful decoder from context.
     *
     * @param context The pipeline call context.
     * @param contentLength The content length.
     * @return The stateful decoder.
     */
    private StatefulStructuredMessageDecoder getOrCreateDecoder(HttpPipelineCallContext context, Long contentLength) {
        return context.getData(Constants.STRUCTURED_MESSAGE_DECODER_STATE_CONTEXT_KEY)
            .filter(value -> value instanceof StatefulStructuredMessageDecoder)
            .map(value -> (StatefulStructuredMessageDecoder) value)
            .orElseGet(() -> new StatefulStructuredMessageDecoder(contentLength));
    }

    /**
     * Checks if the response is a download response (GET request with body).
     *
     * @param httpResponse The HTTP response.
     * @return true if it's a download response, false otherwise.
     */
    private boolean isDownloadResponse(HttpResponse httpResponse) {
        return httpResponse.getRequest().getHttpMethod() == HttpMethod.GET && httpResponse.getBody() != null;
    }

    /**
     * HTTP response wrapper that provides a decoded response body.
     */
    static class DecodedResponse extends HttpResponse {
        private final Flux<ByteBuffer> decodedBody;
        private final HttpResponse originalResponse;
        private final StatefulStructuredMessageDecoder decoder;

        DecodedResponse(HttpResponse httpResponse, Flux<ByteBuffer> decodedBody, 
                       StatefulStructuredMessageDecoder decoder) {
            super(httpResponse.getRequest());
            this.originalResponse = httpResponse;
            this.decodedBody = decodedBody;
            this.decoder = decoder;
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
            return getBodyAsByteArray().map(String::new);
        }

        @Override
        public Mono<String> getBodyAsString(Charset charset) {
            return getBodyAsByteArray().map(bytes -> new String(bytes, charset));
        }

        /**
         * Gets the decoder instance for retry state tracking.
         *
         * @return The stateful decoder.
         */
        public StatefulStructuredMessageDecoder getDecoder() {
            return decoder;
        }
    }
}
