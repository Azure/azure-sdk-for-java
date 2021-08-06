// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opentelemetry;

import com.azure.core.annotation.ExpectedResponses;
import com.azure.core.annotation.Get;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.tracing.opentelemetry.implementation.AmqpPropagationFormatUtil;
import com.azure.core.util.Context;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.azure.core.util.tracing.Tracer.PARENT_SPAN_KEY;
import static com.azure.core.util.tracing.Tracer.SPAN_CONTEXT_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for {@link OpenTelemetryHttpPolicy}.
 */
public class OpenTelemetryHttpPolicyTests {

    @Test
    public void addAfterPolicyTest() {
        // Arrange & Act
        final HttpPipeline pipeline = createHttpPipeline();

        // Assert
        assertEquals(1, pipeline.getPolicyCount());
        assertEquals(OpenTelemetryHttpPolicy.class, pipeline.getPolicy(0).getClass());
    }

    @Host("https://httpbin.org")
    @ServiceInterface(name = "TestService")
    interface TestService {
        @Get("anything")
        @ExpectedResponses({200})
        HttpBinJSON getAnything(Context context);
    }

    @Test
    public void openTelemetryHttpPolicyTest() {
        // Arrange
        // reset the global object before attempting to register
        GlobalOpenTelemetry.resetForTest();
        // Get the global singleton Tracer object.
        Tracer tracer = OpenTelemetrySdk.builder().build().getTracer("TracerSdkTest");
        // Start user parent span.
        Span parentSpan = tracer.spanBuilder(PARENT_SPAN_KEY).startSpan();
        Scope scope = parentSpan.makeCurrent();
        // Add parent span to tracingContext
        Context tracingContext = new Context(PARENT_SPAN_KEY, parentSpan);

        Span expectedSpan = tracer
            .spanBuilder("/anything")
            .setParent(io.opentelemetry.context.Context.current().with(parentSpan))
            .setSpanKind(SpanKind.CLIENT)
            .startSpan();

        // Act
        HttpBinJSON response = RestProxy
            .create(OpenTelemetryHttpPolicyTests.TestService.class, createHttpPipeline()).getAnything(tracingContext);

        // Assert
        String diagnosticId = response.headers().get("Traceparent");
        assertNotNull(diagnosticId);
        SpanContext returnedSpanContext = getNonRemoteSpanContext(diagnosticId);
        verifySpanContextAttributes(expectedSpan.getSpanContext(), returnedSpanContext);
        scope.close();
    }

    private static HttpPipeline createHttpPipeline() {
        final HttpClient httpClient = HttpClient.createDefault();
        final List<HttpPipelinePolicy> policies = new ArrayList<>();
        HttpPolicyProviders.addAfterRetryPolicies(policies);

        final HttpPipeline httpPipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .build();
        return httpPipeline;
    }

    private static SpanContext getNonRemoteSpanContext(String diagnosticId) {
        Context updatedContext = AmqpPropagationFormatUtil.extractContext(diagnosticId, Context.NONE);
        SpanContext spanContext = (SpanContext) updatedContext.getData(SPAN_CONTEXT_KEY).get();
        return SpanContext.create(spanContext.getTraceId(), spanContext.getSpanId(),
            spanContext.getTraceFlags(), spanContext.getTraceState());
    }

    private static void verifySpanContextAttributes(SpanContext expectedSpanContext, SpanContext actualSpanContext) {
        assertEquals(expectedSpanContext.getTraceId(), actualSpanContext.getTraceId());
        assertNotEquals(expectedSpanContext.getSpanId(), actualSpanContext.getSpanId());
        assertEquals(expectedSpanContext.getTraceFlags(), actualSpanContext.getTraceFlags());
        assertEquals(expectedSpanContext.getTraceState(), actualSpanContext.getTraceState());
        assertEquals(expectedSpanContext.isValid(), actualSpanContext.isValid());
        assertEquals(expectedSpanContext.isRemote(), actualSpanContext.isRemote());
    }

    /**
     * Maps to the JSON return values from http://httpbin.org.
     */
    static class HttpBinJSON {

        @JsonProperty()
        private Map<String, String> headers;

        /**
         * Gets the response headers.
         *
         * @return The response headers.
         */
        public Map<String, String> headers() {
            return headers;
        }

        /**
         * Sets the response headers.
         *
         * @param headers The response headers.
         */
        public void headers(Map<String, String> headers) {
            this.headers = headers;
        }
    }

}
