// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;

import com.azure.core.http.HttpPipeline;
import com.azure.monitor.opentelemetry.exporter.implementation.models.MessageData;
import com.azure.monitor.opentelemetry.exporter.implementation.models.MetricsData;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.TestUtils;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class AzureMonitorExportersEndToEndTest extends MonitorExporterClientTestBase {

    private static final String CONNECTION_STRING_ENV =
        "InstrumentationKey=00000000-0000-0000-0000-0FEEDDADBEEF;"
            + "IngestionEndpoint=https://test.in.applicationinsights.azure.com/;"
            + "LiveEndpoint=https://test.livediagnostics.monitor.azure.com/";

    private static final String INSTRUMENTATION_KEY = "00000000-0000-0000-0000-000000000000";

    @Test
    public void testBuildTraceExporter() throws Exception {
        // create the OpenTelemetry SDK
        CountDownLatch countDownLatch = new CountDownLatch(1);
        CustomValidationPolicy customValidationPolicy = new CustomValidationPolicy(countDownLatch);
        HttpPipeline httpPipeline = getHttpPipeline(customValidationPolicy);
        OpenTelemetry openTelemetry =
            TestUtils.createOpenTelemetrySdk(httpPipeline, getConfiguration());

        // generate a span
        generateSpan(openTelemetry);

        // wait for export
        countDownLatch.await(10, SECONDS);
        assertThat(customValidationPolicy.getUrl())
            .isEqualTo(new URL("https://test.in.applicationinsights.azure.com/v2.1/track"));
        assertThat(customValidationPolicy.getActualTelemetryItems().size()).isEqualTo(1);

        // validate span
        TelemetryItem spanTelemetryItem =
            customValidationPolicy.getActualTelemetryItems().stream()
                .filter(item -> item.getName().equals("RemoteDependency"))
                .findFirst()
                .get();
        validateSpan(spanTelemetryItem);
    }

    @Test
    public void testBuildMetricExporter() throws Exception {
        // create the OpenTelemetry SDK
        CountDownLatch countDownLatch = new CountDownLatch(1);
        CustomValidationPolicy customValidationPolicy = new CustomValidationPolicy(countDownLatch);
        OpenTelemetrySdk openTelemetry =
            TestUtils.createOpenTelemetrySdk(
                getHttpPipeline(customValidationPolicy), getConfiguration());

        // generate a metric
        generateMetric(openTelemetry);

        // close to flush
        openTelemetry.close();

        // wait for export
        countDownLatch.await(10, SECONDS);
        assertThat(customValidationPolicy.getUrl())
            .isEqualTo(new URL("https://test.in.applicationinsights.azure.com/v2.1/track"));
        assertThat(customValidationPolicy.getActualTelemetryItems().size()).isEqualTo(1);
        validateMetric(customValidationPolicy.getActualTelemetryItems().get(0));
    }

    @Test
    public void testBuildLogExporter() throws Exception {
        // create the OpenTelemetry SDK
        CountDownLatch countDownLatch = new CountDownLatch(1);
        CustomValidationPolicy customValidationPolicy = new CustomValidationPolicy(countDownLatch);
        OpenTelemetry openTelemetry =
            TestUtils.createOpenTelemetrySdk(
                getHttpPipeline(customValidationPolicy), getConfiguration());

        // generate a log
        generateLog(openTelemetry);

        // wait for export
        countDownLatch.await(10, SECONDS);
        assertThat(customValidationPolicy.getUrl())
            .isEqualTo(new URL("https://test.in.applicationinsights.azure.com/v2.1/track"));
        assertThat(customValidationPolicy.getActualTelemetryItems().size()).isEqualTo(1);

        // validate log
        TelemetryItem logTelemetryItem =
            customValidationPolicy.getActualTelemetryItems().stream()
                .filter(item -> item.getName().equals("Message"))
                .findFirst()
                .get();

        validateLog(logTelemetryItem);
    }

    @Test
    public void testBuildTraceMetricLogExportersConsecutively() throws Exception {
        // create the OpenTelemetry SDK
        CountDownLatch countDownLatch = new CountDownLatch(3);
        CustomValidationPolicy customValidationPolicy = new CustomValidationPolicy(countDownLatch);
        OpenTelemetrySdk openTelemetry =
            TestUtils.createOpenTelemetrySdk(
                getHttpPipeline(customValidationPolicy), getConfiguration());

        // generate telemetry
        generateSpan(openTelemetry);
        generateMetric(openTelemetry);
        generateLog(openTelemetry);

        // close to flush
        openTelemetry.close();

        // wait for export
        countDownLatch.await(10, SECONDS);
        assertThat(customValidationPolicy.getUrl())
            .isEqualTo(new URL("https://test.in.applicationinsights.azure.com/v2.1/track"));
        assertThat(customValidationPolicy.getActualTelemetryItems().size()).isEqualTo(3);

        // validate telemetry
        TelemetryItem spanTelemetryItem =
            customValidationPolicy.getActualTelemetryItems().stream()
                .filter(item -> item.getName().equals("RemoteDependency"))
                .findFirst()
                .get();
        TelemetryItem metricTelemetryItem =
            customValidationPolicy.getActualTelemetryItems().stream()
                .filter(item -> item.getName().equals("Metric"))
                .findFirst()
                .get();
        TelemetryItem logTelemetryItem =
            customValidationPolicy.getActualTelemetryItems().stream()
                .filter(item -> item.getName().equals("Message"))
                .findFirst()
                .get();
        validateSpan(spanTelemetryItem);
        validateMetric(metricTelemetryItem);
        validateLog(logTelemetryItem);

        // TODO (trask) also export and validate logs in this test
    }

    @SuppressWarnings("try")
    private static void generateSpan(OpenTelemetry openTelemetry) {
        Tracer tracer = openTelemetry.getTracer("Sample");
        Span span = tracer.spanBuilder("test").startSpan();
        try (Scope ignored = span.makeCurrent()) {
            span.setAttribute("name", "apple");
            span.setAttribute("color", "red");
        } finally {
            span.end();
        }
    }

    private static void generateMetric(OpenTelemetry openTelemetry) {
        Meter meter = openTelemetry.getMeter("Sample");
        LongCounter counter = meter.counterBuilder("test").build();
        counter.add(
            1L,
            Attributes.of(
                AttributeKey.stringKey("name"), "apple", AttributeKey.stringKey("color"), "red"));
    }

    private static void generateLog(OpenTelemetry openTelemetry) {
        Logger logger = openTelemetry.getLogsBridge().get("Sample");
        logger
            .logRecordBuilder()
            .setBody("test body")
            .setAttribute(AttributeKey.stringKey("name"), "apple")
            .setAttribute(AttributeKey.stringKey("color"), "red")
            .emit();
    }

    @SuppressWarnings("unchecked")
    private static void validateSpan(TelemetryItem telemetryItem) {
        assertThat(telemetryItem.getName()).isEqualTo("RemoteDependency");
        assertThat(telemetryItem.getInstrumentationKey()).isEqualTo(INSTRUMENTATION_KEY);
        assertThat(telemetryItem.getTags()).containsEntry("ai.cloud.role", "unknown_service:java");
        assertThat(telemetryItem.getTags())
            .hasEntrySatisfying("ai.internal.sdkVersion", v -> assertThat(v).contains("otel"));
        assertThat(telemetryItem.getData().getBaseType()).isEqualTo("RemoteDependencyData");
        Map<String, Object> actualData = telemetryItem.getData().getBaseData().getAdditionalProperties();
        assertThat(actualData.get("name").toString()).isEqualTo("test");
        assertThat((Map<String, String>) actualData.get("properties")).containsExactly(entry("color", "red"), entry("name", "apple"));
    }

    @SuppressWarnings("unchecked")
    private static void validateMetric(TelemetryItem telemetryItem) {
        assertThat(telemetryItem.getInstrumentationKey()).isEqualTo(INSTRUMENTATION_KEY);
        assertThat(telemetryItem.getTags()).containsEntry("ai.cloud.role", "unknown_service:java");
        assertThat(telemetryItem.getTags())
            .hasEntrySatisfying("ai.internal.sdkVersion", v -> assertThat(v).contains("otel"));
        assertThat(telemetryItem.getData().getBaseType()).isEqualTo("MetricData");
        Map<String, Object> metricData = ((List<Map<String, Object>>) telemetryItem.getData().getBaseData().getAdditionalProperties().get("metrics")).get(0);
        assertThat((Double) metricData.get("value")).isEqualTo(1.0);
        assertThat((String) metricData.get("name")).isEqualTo("test");
        assertThat((Map<String, String>) telemetryItem.getData().getBaseData().getAdditionalProperties().get("properties"))
            .containsExactly(entry("color", "red"), entry("name", "apple"));
    }

    @SuppressWarnings("unchecked")
    private static void validateLog(TelemetryItem telemetryItem) {
        assertThat(telemetryItem.getName()).isEqualTo("Message");
        assertThat(telemetryItem.getInstrumentationKey()).isEqualTo(INSTRUMENTATION_KEY);
        assertThat(telemetryItem.getTags()).containsEntry("ai.cloud.role", "unknown_service:java");
        assertThat(telemetryItem.getTags())
            .hasEntrySatisfying("ai.internal.sdkVersion", v -> assertThat(v).contains("otel"));
        assertThat(telemetryItem.getData().getBaseType()).isEqualTo("MessageData");
        Map<String, Object> messageProperties = telemetryItem.getData().getBaseData().getAdditionalProperties();
        assertThat(messageProperties.get("message")).isEqualTo("test body");
        assertThat((Map<String, String>)messageProperties.get("properties"))
            .containsOnly(
                entry("LoggerName", "Sample"),
                entry("SourceType", "Logger"),
                entry("color", "red"),
                entry("name", "apple"));
    }

    private static Map<String, String> getConfiguration() {
        return Collections.singletonMap("APPLICATIONINSIGHTS_CONNECTION_STRING", CONNECTION_STRING_ENV);
    }
}
