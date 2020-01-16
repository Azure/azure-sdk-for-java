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
import com.azure.core.test.implementation.entities.HttpBinJSON;
import com.azure.core.tracing.opentelemetry.implementation.AmqpPropagationFormatUtil;
import com.azure.core.util.Context;
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.Tracer;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

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

        final HttpPipeline pipeline = createHttpPipeline();

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
        // Get the global singleton Tracer object.
        Tracer tracer = OpenTelemetry.getTracerFactory().get("TracerSdkTest");
        // Start user parent span.
        Span parentSpan = tracer.spanBuilder(PARENT_SPAN_KEY).startSpan();
        tracer.withSpan(parentSpan);
        // Add parent span to tracingContext
        Context tracingContext = new Context(PARENT_SPAN_KEY, parentSpan);

        Span expectedSpan = tracer
            .spanBuilder("/anything")
            .setParent(parentSpan)
            .setSpanKind(Span.Kind.CLIENT)
            .startSpan();

        // Act
        HttpBinJSON response = RestProxy
            .create(OpenTelemetryHttpPolicyTests.TestService.class, createHttpPipeline()).getAnything(tracingContext);

        // Assert
        String diagnosticId = response.headers().get("Traceparent");
        assertNotNull(diagnosticId);
        Context updatedContext = AmqpPropagationFormatUtil.extractContext(diagnosticId, Context.NONE);
        SpanContext returnedSpanContext = (SpanContext) updatedContext.getData(SPAN_CONTEXT_KEY).get();
        verifySpanContextAttributes(expectedSpan.getContext(), returnedSpanContext);
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

    private static void verifySpanContextAttributes(SpanContext expectedSpanContext, SpanContext actualSpanContext) {
        assertEquals(expectedSpanContext.getTraceId(), actualSpanContext.getTraceId());
        assertNotEquals(expectedSpanContext.getSpanId(), actualSpanContext.getSpanId());
        assertEquals(expectedSpanContext.getTraceFlags(), actualSpanContext.getTraceFlags());
        assertEquals(expectedSpanContext.getTracestate(), actualSpanContext.getTracestate());
        assertEquals(expectedSpanContext.isValid(), actualSpanContext.isValid());
        assertEquals(expectedSpanContext.isRemote(), actualSpanContext.isRemote());
    }
}
