// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects;

import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.Context;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BetaTelemetryClientTest {
    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void cachesSuccessfulConnectionString(boolean async) {
        List<HttpRequest> requests = new ArrayList<>();
        Supplier<String> lookup
            = createLookup(async, requests, "{\"value\":[{\"name\":\"insights\",\"type\":\"AppInsights\"}]}",
                "{\"credentials\":{\"type\":\"ApiKey\",\"key\":\"InstrumentationKey=test\"}}");
        assertEquals("InstrumentationKey=test", lookup.get());
        assertEquals("InstrumentationKey=test", lookup.get());
        assertEquals(2, requests.size());
        assertTrue(requests.get(0).getUrl().getQuery().contains("connectionType=AppInsights"));
        assertTrue(requests.get(1).getUrl().getPath().contains("insights"));
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void missingConnectionIsNotCached(boolean async) {
        List<HttpRequest> requests = new ArrayList<>();
        Supplier<String> lookup = createLookup(async, requests, "{\"value\":[]}", "{}");
        assertThrows(ResourceNotFoundException.class, lookup::get);
        assertThrows(ResourceNotFoundException.class, lookup::get);
        assertEquals(2, requests.size());
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void rejectsInvalidCredentials(boolean async) {
        for (String credentials : new String[] {
            "{}",
            "{\"credentials\":{\"type\":\"EntraID\"}}",
            "{\"credentials\":{\"type\":\"ApiKey\",\"key\":\"\"}}" }) {
            List<HttpRequest> requests = new ArrayList<>();
            Supplier<String> lookup
                = createLookup(async, requests, "{\"value\":[{\"name\":\"insights\"}]}", credentials);
            assertThrows(IllegalStateException.class, lookup::get);
            assertThrows(IllegalStateException.class, lookup::get);
            assertEquals(4, requests.size());
        }
    }

    private static Supplier<String> createLookup(boolean async, List<HttpRequest> requests, String listResponse,
        String credentialResponse) {
        HttpClient httpClient = new HttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                assertTrue(async, "Synchronous telemetry must not use the asynchronous transport");
                return Mono.fromSupplier(() -> createResponse(request));
            }

            @Override
            public HttpResponse sendSync(HttpRequest request, Context context) {
                assertFalse(async, "Asynchronous telemetry must not use the synchronous transport");
                return createResponse(request);
            }

            private HttpResponse createResponse(HttpRequest request) {
                requests.add(request);
                String body = request.getUrl().getPath().endsWith("/connections") ? listResponse : credentialResponse;
                return new MockHttpResponse(request, 200,
                    new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json"),
                    body.getBytes(StandardCharsets.UTF_8));
            }
        };
        AIProjectClientBuilder builder
            = new AIProjectClientBuilder().endpoint("https://localhost/api/projects/project").httpClient(httpClient);
        if (async) {
            BetaTelemetryAsyncClient client = builder.buildBetaTelemetryAsyncClient();
            return () -> client.getApplicationInsightsConnectionString().block();
        }
        BetaTelemetryClient client = builder.buildBetaTelemetryClient();
        return client::getApplicationInsightsConnectionString;
    }
}
