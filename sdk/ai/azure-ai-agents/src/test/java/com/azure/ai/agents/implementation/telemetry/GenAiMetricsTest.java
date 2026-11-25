// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation.telemetry;

import com.azure.core.metrics.opentelemetry.OpenTelemetryMetricsOptions;
import com.azure.core.util.ConfigurationBuilder;
import com.azure.core.util.metrics.Meter;
import com.azure.core.util.metrics.MeterProvider;
import com.azure.core.util.tracing.Tracer;
import com.azure.core.util.tracing.TracerProvider;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for GenAI metric values and dimensions.
 */
public final class GenAiMetricsTest {
    private static final AttributeKey<String> OPERATION_NAME = AttributeKey.stringKey("gen_ai.operation.name");
    private static final AttributeKey<String> PROVIDER_NAME = AttributeKey.stringKey("gen_ai.provider.name");
    private static final AttributeKey<String> REQUEST_MODEL = AttributeKey.stringKey("gen_ai.request.model");
    private static final AttributeKey<String> TOKEN_TYPE = AttributeKey.stringKey("gen_ai.token.type");
    private static final AttributeKey<String> SERVER_ADDRESS = AttributeKey.stringKey("server.address");
    private static final AttributeKey<String> ERROR_TYPE = AttributeKey.stringKey("error.type");

    private InMemoryMetricReader metricReader;
    private SdkMeterProvider sdkMeterProvider;
    private GenAiInstrumentation instrumentation;

    @BeforeEach
    public void setup() {
        metricReader = InMemoryMetricReader.create();
        sdkMeterProvider = SdkMeterProvider.builder().registerMetricReader(metricReader).build();
        OpenTelemetry openTelemetry = OpenTelemetrySdk.builder().setMeterProvider(sdkMeterProvider).build();
        Meter meter = MeterProvider.getDefaultProvider()
            .createMeter("test", "1.0.0", new OpenTelemetryMetricsOptions().setOpenTelemetry(openTelemetry));
        Tracer tracer = TracerProvider.getDefaultProvider().createTracer("test", null, null, null);
        instrumentation = new GenAiInstrumentation("https://contoso.services.ai.azure.com",
            new ConfigurationBuilder().putProperty("experimental.enable_genai_tracing", "true").build(), tracer, meter);
    }

    @AfterEach
    public void cleanup() {
        sdkMeterProvider.close();
    }

    @Test
    public void recordsResponseDurationAndTokenMetrics() {
        GenAiTracingScope scope = instrumentation.startChat("gpt-4o");
        assertNotNull(scope);
        scope.setRequestModelAttributes("gpt-4o", null, null);
        scope.setResponseAttributes("response-1", "gpt-4.1", 12L, 7L, "stop");
        scope.close();

        Collection<MetricData> metrics = metricReader.collectAllMetrics();
        MetricData duration = metric(metrics, "gen_ai.client.operation.duration");
        assertEquals("s", duration.getUnit());
        assertTrue(duration.getHistogramData().getPoints().stream().findFirst().get().getSum() >= 0);
        assertCommonAttributes(duration.getHistogramData().getPoints().stream().findFirst().get().getAttributes(),
            "gpt-4.1");

        MetricData tokenUsage = metric(metrics, "gen_ai.client.token.usage");
        assertEquals("token", tokenUsage.getUnit());
        List<io.opentelemetry.sdk.metrics.data.HistogramPointData> tokenPoints
            = tokenUsage.getHistogramData().getPoints().stream().collect(Collectors.toList());
        assertEquals(2, tokenPoints.size());
        assertEquals(12.0,
            tokenPoints.stream()
                .filter(point -> "input".equals(point.getAttributes().get(TOKEN_TYPE)))
                .findFirst()
                .get()
                .getSum());
        assertEquals(7.0,
            tokenPoints.stream()
                .filter(point -> "completion".equals(point.getAttributes().get(TOKEN_TYPE)))
                .findFirst()
                .get()
                .getSum());
        tokenPoints.forEach(point -> assertCommonAttributes(point.getAttributes(), "gpt-4.1"));
    }

    @Test
    public void recordsRequestModelFallbackAndErrorDimension() {
        GenAiTracingScope scope = instrumentation.startInvokeAgent("weather-agent");
        assertNotNull(scope);
        scope.setRequestModelAttributes("gpt-4o", null, null);
        scope.recordError(new IllegalStateException("boom"));
        scope.close();

        MetricData duration = metric(metricReader.collectAllMetrics(), "gen_ai.client.operation.duration");
        io.opentelemetry.sdk.metrics.data.HistogramPointData point
            = duration.getHistogramData().getPoints().stream().findFirst().get();
        assertCommonAttributes(point.getAttributes(), "gpt-4o");
        assertEquals(IllegalStateException.class.getName(), point.getAttributes().get(ERROR_TYPE));
    }

    private static MetricData metric(Collection<MetricData> metrics, String name) {
        MetricData result = metrics.stream().filter(metric -> name.equals(metric.getName())).findFirst().orElse(null);
        assertNotNull(result, "Metric not found: " + name);
        return result;
    }

    private static void assertCommonAttributes(io.opentelemetry.api.common.Attributes attributes, String model) {
        assertEquals("responses", attributes.get(OPERATION_NAME));
        assertEquals("microsoft.foundry", attributes.get(PROVIDER_NAME));
        assertEquals("contoso.services.ai.azure.com", attributes.get(SERVER_ADDRESS));
        assertEquals(model, attributes.get(REQUEST_MODEL));
    }
}
