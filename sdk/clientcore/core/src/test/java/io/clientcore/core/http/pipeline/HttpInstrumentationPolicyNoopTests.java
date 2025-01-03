// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.MockHttpResponse;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.instrumentation.InstrumentationOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.SocketException;

import static io.clientcore.core.http.models.HttpHeaderName.TRACEPARENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HttpInstrumentationPolicyNoopTests {
    private static final InstrumentationOptions<?> OPTIONS = new InstrumentationOptions<>();
    private static final HttpHeaderName TRACESTATE = HttpHeaderName.fromString("tracestate");

    @ParameterizedTest
    @ValueSource(ints = { 200, 201, 206, 302, 400, 404, 500, 503 })
    public void simpleRequestTracingDisabled(int statusCode) throws IOException {
        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new HttpInstrumentationPolicy(OPTIONS, null))
            .httpClient(request -> new MockHttpResponse(request, statusCode))
            .build();

        // should not throw
        try (Response<?> response = pipeline.send(new HttpRequest(HttpMethod.GET, "https://localhost/"))) {
            assertEquals(statusCode, response.getStatusCode());
            assertNull(response.getRequest().getHeaders().get(TRACESTATE));
            assertNull(response.getRequest().getHeaders().get(TRACEPARENT));
        }
    }

    @Test
    public void exceptionTracingDisabled() {
        SocketException exception = new SocketException("test exception");
        HttpPipeline pipeline
            = new HttpPipelineBuilder().policies(new HttpInstrumentationPolicy(OPTIONS, null)).httpClient(request -> {
                throw exception;
            }).build();

        assertThrows(UncheckedIOException.class,
            () -> pipeline.send(new HttpRequest(HttpMethod.GET, "https://localhost/")).close());
    }
}
