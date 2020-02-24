// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.storage.common.implementation.Constants;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class MockRequestResponse {
    public HttpRequest getMockRequest() throws MalformedURLException {
        HttpHeaders headers = new HttpHeaders();
        headers.put(Constants.HeaderConstants.CONTENT_ENCODING, "en-US");
        URL url = new URL("http://devtest.blob.core.windows.net/test-container/test-blob");
        HttpRequest request = new HttpRequest(HttpMethod.POST, url, headers, null);
        return request;
    }

    public static HttpResponse getStubResponse(final int code, final HttpRequest request) {
        return new HttpResponse(request) {
            @Override
            public int getStatusCode() {
                return code;
            }

            @Override
            public String getHeaderValue(String s) {
                return null;
            }

            @Override
            public HttpHeaders getHeaders() {
                return new HttpHeaders();
            }

            @Override
            public Flux<ByteBuffer> getBody() {
                return Flux.empty();
            }

            @Override
            public Mono<byte[]> getBodyAsByteArray() {
                return Mono.just(new byte[0]);
            }

            @Override
            public Mono<String> getBodyAsString() {
                return Mono.just("");
            }

            @Override
            public Mono<String> getBodyAsString(Charset charset) {
                return Mono.just("");
            }

        };
    }

    public class MockRetryRangeResponsePolicy implements HttpPipelinePolicy {
        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            return next.process().flatMap(response -> {
                if (!response.getRequest().getHeaders().getValue("x-ms-range").equals("bytes=2-6")) {
                    return Mono.error(new IllegalArgumentException("The range header was not set correctly on retry."));
                } else {
                    // ETag can be a dummy value. It's not validated, but DownloadResponse requires one
                    return Mono.just(new MockDownloadHttpResponse(response, 206, Flux.error(new IOException())));
                }
            });
        }

    }

    public class MockDownloadHttpResponse extends HttpResponse {
        public MockDownloadHttpResponse(HttpResponse response, int statusCode, Flux<ByteBuffer> body) {
            super(response.getRequest());
            this.statusCode = statusCode;
            this.headers = response.getHeaders();
            this.body = body;
        }

        @Override
        public int getStatusCode() {
            return statusCode;
        }

        @Override
        public String getHeaderValue(String s) {
            return headers.getValue(s);
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }

        @Override
        public Flux<ByteBuffer> getBody() {
            return body;
        }

        @Override
        public Mono<byte[]> getBodyAsByteArray() {
            return Mono.error(new IOException());
        }

        @Override
        public Mono<String> getBodyAsString() {
            return Mono.error(new IOException());
        }

        @Override
        public Mono<String> getBodyAsString(Charset charset) {
            return Mono.error(new IOException());
        }

        private final int statusCode;
        private final HttpHeaders headers;
        private final Flux<ByteBuffer> body;
    }
}
