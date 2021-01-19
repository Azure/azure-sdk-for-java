// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ChatResponseMocker {

    public static HttpResponse createReadReceiptsResponse(HttpRequest request) {
        String body = String.format("{\"value\":["
            + "{\"senderId\":\"8:acs:9b665d53-8164-4923-ad5d-5e983b07d2e7_00000005-334f-e4af-b274-5a3a0d0002f9\",\"chatMessageId\":\"1600201311647\",\"readOn\":\"2020-09-15T20:21:51Z\"},"
            + "{\"senderId\":\"8:acs:9b665d53-8164-4923-ad5d-5e983b07d2e7_00000005-334f-e4af-b274-5a3a0d0002f9\",\"chatMessageId\":\"1600201311648\",\"readOn\":\"2020-09-15T20:21:53Z\"}"
            + "]}");
        return generateMockResponse(body, request, 200);
    }

    public static HttpResponse createSendReceiptsResponse(HttpRequest request) {
        return generateMockResponse("testBody", request, 200);
    }

    public static HttpResponse generateMockResponse(String body, HttpRequest request, int statusCode) {
        return new HttpResponse(request) {
            @Override
            public int getStatusCode() {
                return statusCode;
            }

            @Override
            public String getHeaderValue(String name) {
                return null;
            }

            @Override
            public HttpHeaders getHeaders() {
                return new HttpHeaders();
            }

            @Override
            public Flux<ByteBuffer> getBody() {
                return Flux.just(ByteBuffer.wrap(body.getBytes(StandardCharsets.UTF_8)));
            }

            @Override
            public Mono<byte[]> getBodyAsByteArray() {
                return Mono.just(body.getBytes(StandardCharsets.UTF_8));
            }

            @Override
            public Mono<String> getBodyAsString() {
                return Mono.just(body);
            }

            @Override
            public Mono<String> getBodyAsString(Charset charset) {
                return Mono.just(body);
            }
        };
    }
}
