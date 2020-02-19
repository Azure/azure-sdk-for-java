// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.http;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class HttpResponseTests {
    @Test
    public void testBufferedResponseSubscribeOnceAndDoDeepCopy() {
        // A source Response that throws if body is subscribed more than once.
        SelfDisposedHttpResponse sourceHttpResponse = new SelfDisposedHttpResponse();
        // A Buffered response based on source response.
        Flux<ByteBuffer> bufferedContentFlux = sourceHttpResponse.buffer().getBody();
        Flux<Tuple2<ByteBuffer, ByteBuffer>> zipped
            = bufferedContentFlux.zipWith(sourceHttpResponse.getInnerContentFlux());
        // Validate that buffered Response is not replaying source Response body.
        StepVerifier.create(zipped)
            .thenConsumeWhile(o -> {
                assertFalse(o.getT1() == o.getT2(),
                    "Buffered response should not cache shallow copy of source.");
                return true;
            })
            .verifyComplete();
    }

    // A Type to mimic Response with body content released/disposed as it consumed
    private static class SelfDisposedHttpResponse extends HttpResponse {
        private final Mono<ByteBuffer> contentMono;
        private final HttpHeaders headers = new HttpHeaders();
        private volatile boolean consumed;

        protected SelfDisposedHttpResponse() {
            super(new HttpRequest(HttpMethod.GET, "http://localhost"));
            this.contentMono = Mono.just(ByteBuffer.wrap("long_long_content".getBytes()));
        }

        Flux<ByteBuffer> getInnerContentFlux() {
            return this.contentMono.flux();
        }

        @Override
        public int getStatusCode() {
            return 200;
        }

        @Override
        public String getHeaderValue(String name) {
            return null;
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }

        @Override
        public Flux<ByteBuffer> getBody() {
            return this.contentMono
                .doOnNext(bb -> {
                    // This ensure BufferedHttpResponse subscribes only once.
                    assertFalse(consumed, "content is already consumed");
                    consumed = true;
                })
                .flux();
        }

        @Override
        public Mono<byte[]> getBodyAsByteArray() {
            return this.getBody()
                .map(bb -> new byte[bb.remaining()])
                .next();
        }

        @Override
        public Mono<String> getBodyAsString() {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public Mono<String> getBodyAsString(Charset charset) {
            throw new RuntimeException("Not implemented");
        }
    }
}
