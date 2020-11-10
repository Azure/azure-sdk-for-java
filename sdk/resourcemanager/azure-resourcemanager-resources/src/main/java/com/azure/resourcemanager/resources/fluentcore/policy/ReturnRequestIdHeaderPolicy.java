// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.policy;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RequestIdPolicy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * An interceptor for requesting server return client-request-id in response headers.
 * Optionally, fill-in the client-request-id, if server does not return it in response headers.
 * <p>
 * ReturnRequestIdHeaderInterceptor should be added after {@link RequestIdPolicy}.
 *
 * @see RequestIdPolicy
 */
public class ReturnRequestIdHeaderPolicy implements HttpPipelinePolicy {

    private static final String NAME_RETURN_CLIENT_REQUEST_ID = "x-ms-return-client-request-id";
    private static final String NAME_CLIENT_REQUEST_ID = "x-ms-client-request-id";

    private final Option option;

    /**
     * Additional client handling, if server does not return client-request-id in response headers.
     */
    public enum Option {
        /**
         * Default.
         */
        NONE,

        /**
         * Fill-in the client-request-id from request headers.
         */
        COPY_CLIENT_REQUEST_ID
    }

    /**
     * Creates a new instance of ReturnRequestIdHeaderPolicy.
     * Sets "x-ms-return-client-request-id: true" in requests headers.
     */
    public ReturnRequestIdHeaderPolicy() {
        this(Option.NONE);
    }

    /**
     * Creates a new instance of ReturnRequestIdHeaderPolicy.
     * Sets "x-ms-return-client-request-id: true" in requests headers.
     * <p>
     * Optionally fill-in the client-request-id if server does not return it in response headers.
     *
     * @param option the option of additional client handling,
     *               if server does not return client-request-id in response headers.
     */
    public ReturnRequestIdHeaderPolicy(Option option) {
        this.option = option;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        final HttpRequest request = context.getHttpRequest();
        final String clientRequestId = request.getHeaders().getValue(NAME_CLIENT_REQUEST_ID);

        if (request.getHeaders().getValue(NAME_RETURN_CLIENT_REQUEST_ID) == null) {
            request.getHeaders().put(NAME_RETURN_CLIENT_REQUEST_ID, "true");
        }

        Mono<HttpResponse> responseMono = next.process();
        if (option == Option.COPY_CLIENT_REQUEST_ID && clientRequestId != null) {
            responseMono = responseMono.map(response -> {
                if (response.getHeaderValue(NAME_CLIENT_REQUEST_ID) == null) {
                    response = new BufferedHttpHeaderResponse(response);
                    response.getHeaders().put(NAME_CLIENT_REQUEST_ID, clientRequestId);
                }
                return response;
            });
        }
        return responseMono;
    }

    private static class BufferedHttpHeaderResponse extends HttpResponse {

        private final HttpResponse innerHttpResponse;
        private final HttpHeaders cachedHeaders;

        BufferedHttpHeaderResponse(HttpResponse innerHttpResponse) {
            super(innerHttpResponse.getRequest());
            this.innerHttpResponse = innerHttpResponse;
            this.cachedHeaders = new HttpHeaders(innerHttpResponse.getHeaders());
        }

        @Override
        public int getStatusCode() {
            return innerHttpResponse.getStatusCode();
        }

        @Override
        public String getHeaderValue(String name) {
            return cachedHeaders.getValue(name);
        }

        @Override
        public HttpHeaders getHeaders() {
            return cachedHeaders;
        }

        @Override
        public Flux<ByteBuffer> getBody() {
            return innerHttpResponse.getBody();
        }

        @Override
        public Mono<byte[]> getBodyAsByteArray() {
            return innerHttpResponse.getBodyAsByteArray();
        }

        @Override
        public Mono<String> getBodyAsString() {
            return innerHttpResponse.getBodyAsString();
        }

        @Override
        public Mono<String> getBodyAsString(Charset charset) {
            return innerHttpResponse.getBodyAsString();
        }
    }
}
