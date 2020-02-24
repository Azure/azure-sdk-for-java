// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common

import com.azure.core.http.HttpHeaders
import com.azure.core.http.HttpMethod
import com.azure.core.http.HttpPipelineCallContext
import com.azure.core.http.HttpPipelineNextPolicy
import com.azure.core.http.HttpRequest
import com.azure.core.http.HttpResponse
import com.azure.core.http.policy.HttpPipelinePolicy
import com.azure.storage.common.implementation.Constants
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

import java.nio.ByteBuffer
import java.nio.charset.Charset

class MockRequestResponse {

    static def getMockRequest() {
        HttpHeaders headers = new HttpHeaders()
        headers.put(Constants.HeaderConstants.CONTENT_ENCODING, "en-US")
        URL url = new URL("http://devtest.blob.core.windows.net/test-container/test-blob")
        HttpRequest request = new HttpRequest(HttpMethod.POST, url, headers, null)
        return request
    }

    /*
    This is for stubbing responses that will actually go through the pipeline and autorest code. Autorest does not seem
    to play too nicely with mocked objects and the complex reflection stuff on both ends made it more difficult to work
    with than was worth it.
     */

    static def getStubResponse(int code, HttpRequest request) {
        return new HttpResponse(request) {

            @Override
            int getStatusCode() {
                return code
            }

            @Override
            String getHeaderValue(String s) {
                return null
            }

            @Override
            HttpHeaders getHeaders() {
                return new HttpHeaders()
            }

            @Override
            Flux<ByteBuffer> getBody() {
                return Flux.empty()
            }

            @Override
            Mono<byte[]> getBodyAsByteArray() {
                return Mono.just(new byte[0])
            }

            @Override
            Mono<String> getBodyAsString() {
                return Mono.just("")
            }

            @Override
            Mono<String> getBodyAsString(Charset charset) {
                return Mono.just("")
            }
        }
    }

    class MockRetryRangeResponsePolicy implements HttpPipelinePolicy {
        @Override
        Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            return next.process().flatMap { HttpResponse response ->
                if (response.getRequest().getHeaders().getValue("x-ms-range") != "bytes=2-6") {
                    return Mono.<HttpResponse> error(new IllegalArgumentException("The range header was not set correctly on retry."))
                } else {
                    // ETag can be a dummy value. It's not validated, but DownloadResponse requires one
                    return Mono.<HttpResponse> just(new MockDownloadHttpResponse(response, 206, Flux.error(new IOException())))
                }
            }
        }
    }

    /*
    This is for stubbing responses that will actually go through the pipeline and autorest code. Autorest does not seem
    to play too nicely with mocked objects and the complex reflection stuff on both ends made it more difficult to work
    with than was worth it. Because this type is just for BlobDownload, we don't need to accept a header type.
     */

    class MockDownloadHttpResponse extends HttpResponse {
        private final int statusCode
        private final HttpHeaders headers
        private final Flux<ByteBuffer> body

        MockDownloadHttpResponse(HttpResponse response, int statusCode, Flux<ByteBuffer> body) {
            super(response.getRequest())
            this.statusCode = statusCode
            this.headers = response.getHeaders()
            this.body = body
        }

        @Override
        int getStatusCode() {
            return statusCode
        }

        @Override
        String getHeaderValue(String s) {
            return headers.getValue(s)
        }

        @Override
        HttpHeaders getHeaders() {
            return headers
        }

        @Override
        Flux<ByteBuffer> getBody() {
            return body
        }

        @Override
        Mono<byte[]> getBodyAsByteArray() {
            return Mono.error(new IOException())
        }

        @Override
        Mono<String> getBodyAsString() {
            return Mono.error(new IOException())
        }

        @Override
        Mono<String> getBodyAsString(Charset charset) {
            return Mono.error(new IOException())
        }
    }
}
