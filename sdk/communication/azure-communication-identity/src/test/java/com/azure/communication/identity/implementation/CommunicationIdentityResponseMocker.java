// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.identity.implementation;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class CommunicationIdentityResponseMocker {
    
    public static HttpResponse createUserResult(HttpRequest request) {
        String body = String.format("{\"id\": \"Sanitized\"}");
        return generateMockResponse(body, request, 200);
    }

    public static HttpResponse deleteUserResult(HttpRequest request) {
        return generateMockResponse("", request, 200);
    }

    public static HttpResponse revokeTokenResult(HttpRequest request) {
        return generateMockResponse("", request, 200);
    }

    public static HttpResponse getTokenResult(HttpRequest request) {
        String body = String.format("{\"id\": \"Sanitized\",\n"
            + "\"token\": \"Sanitized\",\n"
            + "\"expiresOn\": \"2020-08-14T17:37:34.4564877-07:00\"}");

        return generateMockResponse(body, request, 200);
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
