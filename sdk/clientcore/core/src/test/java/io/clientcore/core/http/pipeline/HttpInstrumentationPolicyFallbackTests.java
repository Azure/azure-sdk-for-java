// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.MockHttpResponse;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpLogOptions;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.instrumentation.InstrumentationOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;

import static io.clientcore.core.http.models.HttpHeaderName.TRACEPARENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class HttpInstrumentationPolicyFallbackTests {
    private static final InstrumentationOptions<?> OPTIONS = new InstrumentationOptions<>();
    private static final InstrumentationOptions<?> DISABLED_TRACING_OPTIONS
        = new InstrumentationOptions<>().setTracingEnabled(false);
    private static final HttpLogOptions ENABLED_HTTP_LOG_OPTIONS
        = new HttpLogOptions().setLogLevel(HttpLogOptions.HttpLogDetailLevel.HEADERS);
    private static final HttpHeaderName TRACESTATE = HttpHeaderName.fromString("tracestate");

    @Test
    public void simpleRequestTracingDisabled() throws IOException {
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new HttpInstrumentationPolicy(DISABLED_TRACING_OPTIONS, ENABLED_HTTP_LOG_OPTIONS))
            .httpClient(request -> new MockHttpResponse(request, 200))
            .build();

        // should not throw
        try (Response<?> response = pipeline.send(new HttpRequest(HttpMethod.GET, "https://localhost/"))) {
            assertEquals(200, response.getStatusCode());
            assertNull(response.getRequest().getHeaders().get(TRACESTATE));
            assertNull(response.getRequest().getHeaders().get(TRACEPARENT));
        }
    }

    @ParameterizedTest
    @ValueSource(ints = { 200, 201, 206, 302, 400, 404, 500, 503 })
    public void simpleRequestTracingEnabled(int statusCode) throws IOException {
        HttpPipeline pipeline
            = new HttpPipelineBuilder().policies(new HttpInstrumentationPolicy(OPTIONS, ENABLED_HTTP_LOG_OPTIONS))
                .httpClient(request -> new MockHttpResponse(request, statusCode))
                .build();

        // should not throw
        try (Response<?> response = pipeline.send(new HttpRequest(HttpMethod.GET, "https://localhost/"))) {
            assertEquals(statusCode, response.getStatusCode());
            assertNull(response.getRequest().getHeaders().get(TRACESTATE));
            assertNotNull(response.getRequest().getHeaders().get(TRACEPARENT));
        }
    }
}
