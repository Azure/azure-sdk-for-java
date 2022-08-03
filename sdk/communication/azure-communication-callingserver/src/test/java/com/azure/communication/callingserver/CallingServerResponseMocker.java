// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.callingserver;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.AbstractMap.SimpleEntry;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class CallingServerResponseMocker {
    public static final String MOCK_ENDPOINT = "https://REDACTED.communication.azure.com/";
    public static final String MOCK_CONNECTION_STRING = String.format("endpoint=%s;accesskey=eyJhbG", MOCK_ENDPOINT);

    public static String generateDownloadResult(String content) {
        return content;
    }

    public static CallAutomationAsyncClient getCallingServerAsyncClient(ArrayList<SimpleEntry<String, Integer>> responses) {
        HttpClient mockHttpClient = new MockHttpClient(responses);

        return new CallAutomationClientBuilder()
            .httpClient(mockHttpClient)
            .connectionString(MOCK_CONNECTION_STRING)
            .endpoint(MOCK_ENDPOINT)
            .buildAsyncClient();
    }

    public static CallAutomationClient getCallingServerClient(ArrayList<SimpleEntry<String, Integer>> responses) {
        HttpClient mockHttpClient = new MockHttpClient(responses);

        return new CallAutomationClientBuilder()
            .httpClient(mockHttpClient)
            .connectionString(MOCK_CONNECTION_STRING)
            .endpoint(MOCK_ENDPOINT)
            .buildClient();
    }

    public static CallConnection getCallConnection(ArrayList<SimpleEntry<String, Integer>> responses) {
        CallAutomationClient callAutomationClient = getCallingServerClient(responses);
        return callAutomationClient.getCallConnection("callConnectionId");
    }

    public static CallConnectionAsync getCallConnectionAsync(ArrayList<SimpleEntry<String, Integer>> responses) {
        CallAutomationAsyncClient callingServerClient = getCallingServerAsyncClient(responses);
        return callingServerClient.getCallConnectionAsync("callConnectionId");
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
