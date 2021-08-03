// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.http;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.serializer.JsonSerializerProviders;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link DynamicRequest}.
 */
public class DynamicRequestTest {

    @Test
    public void testGetRequest() {
        HttpClient httpClient = new LocalHttpClient(request -> {
            assertEquals("/testGetRequest", request.getUrl().getPath());
            assertEquals(HttpMethod.GET, request.getHttpMethod());
            assertNull(request.getBody());
            assertEquals(0, request.getHeaders().getSize());
        });
        DynamicRequest dynamicRequest = new DynamicRequest(JsonSerializerProviders.createInstance(true),
            new HttpPipelineBuilder().httpClient(httpClient).build());
        dynamicRequest.setUrl("https://example.com/testGetRequest")
            .setHttpMethod(HttpMethod.GET)
            .send();
    }

    @Test
    public void testPostRequest() {
        String jsonRequestBody = "{\"hello\": \"world\"}";
        HttpClient httpClient = new LocalHttpClient(request -> {
            assertEquals("/testPostRequest", request.getUrl().getPath());
            assertEquals(HttpMethod.POST, request.getHttpMethod());
            assertEquals(2, request.getHeaders().getSize());
            assertEquals(String.valueOf(jsonRequestBody.getBytes().length),
                request.getHeaders().get("Content-Length").getValue());
            assertEquals("application/json", request.getHeaders().get("Content-Type").getValue());
            assertEquals(jsonRequestBody,
                new String(FluxUtil.collectBytesInByteBufferStream(request.getBody()).block()));
        });
        DynamicRequest dynamicRequest = new DynamicRequest(JsonSerializerProviders.createInstance(true),
            new HttpPipelineBuilder().httpClient(httpClient).build());
        dynamicRequest.setUrl("https://example.com/testPostRequest")
            .setHttpMethod(HttpMethod.POST)
            .setHeaders(new HttpHeaders().set("Content-Type", "application/json"))
            .setBody(jsonRequestBody)
            .send();
    }

    private static final class LocalHttpClient implements HttpClient {
        private final Consumer<HttpRequest> requestValidator;

        LocalHttpClient(Consumer<HttpRequest> requestValidator) {
            this.requestValidator = requestValidator;
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            requestValidator.accept(request);
            return Mono.empty();
        }
    }

}
