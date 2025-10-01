// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.binarydata.BinaryData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static io.clientcore.core.http.models.HttpHeaderName.TRACEPARENT;
import static io.clientcore.core.http.pipeline.PipelineTestHelpers.TRACESTATE;
import static io.clientcore.core.http.pipeline.PipelineTestHelpers.sendRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ParameterizedClass(name = "isAsync={0}")
@ValueSource(booleans = { false, true })
public class HttpInstrumentationPolicyFallbackTests {
    private final boolean isAsync;

    public HttpInstrumentationPolicyFallbackTests(boolean isAsync) {
        this.isAsync = isAsync;
    }

    @Test
    public void simpleRequestTracingDisabled() {
        HttpInstrumentationOptions tracingOffLoggingOnOptions
            = new HttpInstrumentationOptions().setTracingEnabled(false)
                .setHttpLogLevel(HttpInstrumentationOptions.HttpLogLevel.HEADERS);

        HttpPipeline pipeline
            = new HttpPipelineBuilder().addPolicy(new HttpInstrumentationPolicy(tracingOffLoggingOnOptions))
                .httpClient(request -> new Response<>(request, 200, new HttpHeaders(), BinaryData.empty()))
                .build();

        // should not throw
        try (Response<BinaryData> response = sendRequest(pipeline, isAsync)) {
            assertEquals(200, response.getStatusCode());
            assertNull(response.getRequest().getHeaders().get(TRACESTATE));
            assertNull(response.getRequest().getHeaders().get(TRACEPARENT));
        }
    }

    @ParameterizedTest
    @ValueSource(ints = { 200, 201, 206, 302, 400, 404, 500, 503 })
    public void simpleRequestTracingEnabled(int statusCode) {
        HttpInstrumentationOptions tracingOnLoggingOnOptions
            = new HttpInstrumentationOptions().setHttpLogLevel(HttpInstrumentationOptions.HttpLogLevel.HEADERS);

        HttpPipeline pipeline
            = new HttpPipelineBuilder().addPolicy(new HttpInstrumentationPolicy(tracingOnLoggingOnOptions))
                .httpClient(request -> new Response<>(request, statusCode, new HttpHeaders(), BinaryData.empty()))
                .build();

        // should not throw
        try (Response<BinaryData> response = sendRequest(pipeline, isAsync)) {
            assertEquals(statusCode, response.getStatusCode());
            assertNull(response.getRequest().getHeaders().get(TRACESTATE));
            assertNotNull(response.getRequest().getHeaders().get(TRACEPARENT));
        }
    }
}
