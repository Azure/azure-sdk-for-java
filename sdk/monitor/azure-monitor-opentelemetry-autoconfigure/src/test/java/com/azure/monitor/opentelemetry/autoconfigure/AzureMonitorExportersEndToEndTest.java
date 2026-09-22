// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.NoopTracer;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.RequestData;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.MessageData;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.MetricsData;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.RemoteDependencyData;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.TelemetryEventData;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.TelemetryExceptionData;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.TelemetryItem;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.semconv.ExceptionAttributes;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.utils.TestUtils;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import org.junit.jupiter.api.Test;
import reactor.util.annotation.Nullable;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class AzureMonitorExportersEndToEndTest {

    private static final String CONNECTION_STRING_ENV = "InstrumentationKey=00000000-0000-0000-0000-0FEEDDADBEEF;"
        + "IngestionEndpoint=https://test.in.applicationinsights.azure.com/;"
        + "LiveEndpoint=https://test.livediagnostics.monitor.azure.com/";

    private static final String INSTRUMENTATION_KEY = "00000000-0000-0000-0000-000000000000";
    private static final AttributeKey<Value<?>> CUSTOM_MEASUREMENTS
        = AttributeKey.valueKey("microsoft.custom_measurements");

    @Test
    public void testBuildTraceExporter() throws Exception {
        // create the OpenTelemetry SDK
        final int numberOfSpans = 10;
        CountDownLatch countDownLatch = new CountDownLatch(numberOfSpans);
        CustomValidationPolicy customValidationPolicy = new CustomValidationPolicy(countDownLatch);
        HttpPipeline httpPipeline = getHttpPipeline(customValidationPolicy);
        OpenTelemetry openTelemetry = TestUtils.createOpenTelemetrySdk(httpPipeline, getConfiguration());

        // generate spans
        for (int i = 0; i < numberOfSpans; i++) {
            generateSpan(openTelemetry, i % 2 == 0 ? SpanKind.CLIENT : SpanKind.SERVER);
        }

        // wait for export
        countDownLatch.await(numberOfSpans, SECONDS);
        Thread.sleep(1000); // wait for the last span to be processed. macOS test kept failing on CI due to URL is null.
        assertThat(customValidationPolicy.getUrl())
            .isEqualTo(new URL("https://test.in.applicationinsights.azure.com/v2.1/track"));
        assertThat(customValidationPolicy.getActualTelemetryItems().size()).isEqualTo(numberOfSpans);

        // validate span
        TelemetryItem spanTelemetryItem = customValidationPolicy.getActualTelemetryItems()
            .stream()
            .filter(item -> item.getName().equals("RemoteDependency"))
            .findFirst()
            .get();
        validateSpan(spanTelemetryItem);

        TelemetryItem requestTelemetryItem = customValidationPolicy.getActualTelemetryItems()
            .stream()
            .filter(item -> item.getName().equals("Request"))
            .findFirst()
            .get();
        validateRequest(requestTelemetryItem);
    }

    HttpPipeline getHttpPipeline(@Nullable HttpPipelinePolicy policy, HttpClient httpClient) {
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        if (policy != null) {
            policies.add(policy);
        }
        return new HttpPipelineBuilder().httpClient(httpClient)
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .tracer(new NoopTracer())
            .build();
    }

    HttpPipeline getHttpPipeline(@Nullable HttpPipelinePolicy policy) {
        return getHttpPipeline(policy, HttpClient.createDefault());
    }

    @Test
    public void testBuildMetricExporter() throws Exception {
        // create the OpenTelemetry SDK
        CountDownLatch countDownLatch = new CountDownLatch(1);
        CustomValidationPolicy customValidationPolicy = new CustomValidationPolicy(countDownLatch);
        OpenTelemetrySdk openTelemetry
            = TestUtils.createOpenTelemetrySdk(getHttpPipeline(customValidationPolicy), getConfiguration());

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
        OpenTelemetry openTelemetry
            = TestUtils.createOpenTelemetrySdk(getHttpPipeline(customValidationPolicy), getConfiguration());

        // generate a log
        generateLog(openTelemetry);

        // wait for export
        countDownLatch.await(10, SECONDS);
        assertThat(customValidationPolicy.getUrl())
            .isEqualTo(new URL("https://test.in.applicationinsights.azure.com/v2.1/track"));
        assertThat(customValidationPolicy.getActualTelemetryItems().size()).isEqualTo(1);

        // validate log
        TelemetryItem logTelemetryItem = customValidationPolicy.getActualTelemetryItems()
            .stream()
            .filter(item -> item.getName().equals("Message"))
            .findFirst()
            .get();

        validateLog(logTelemetryItem);
    }

    @Test
    public void testBuildLogExporterWithCustomEvent() throws Exception {
        // create the OpenTelemetry SDK
        CountDownLatch countDownLatch = new CountDownLatch(1);
        CustomValidationPolicy customValidationPolicy = new CustomValidationPolicy(countDownLatch);
        OpenTelemetry openTelemetry
            = TestUtils.createOpenTelemetrySdk(getHttpPipeline(customValidationPolicy), getConfiguration());

        // generate a log
        generateEvent(openTelemetry);

        // wait for export
        countDownLatch.await(10, SECONDS);
        assertThat(customValidationPolicy.getUrl())
            .isEqualTo(new URL("https://test.in.applicationinsights.azure.com/v2.1/track"));
        assertThat(customValidationPolicy.getActualTelemetryItems().size()).isEqualTo(1);

        // validate log
        TelemetryItem eventTelemetryItem = customValidationPolicy.getActualTelemetryItems()
            .stream()
            .filter(item -> item.getName().equals("Event"))
            .findFirst()
            .get();

        validateEvent(eventTelemetryItem);
    }

    @Test
    public void testBuildLogExporterWithException() throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        CustomValidationPolicy customValidationPolicy = new CustomValidationPolicy(countDownLatch);
        OpenTelemetry openTelemetry
            = TestUtils.createOpenTelemetrySdk(getHttpPipeline(customValidationPolicy), getConfiguration());

        generateExceptionLog(openTelemetry);

        countDownLatch.await(10, SECONDS);
        assertThat(customValidationPolicy.getActualTelemetryItems()).hasSize(1);
        validateException(customValidationPolicy.getActualTelemetryItems().get(0));
    }

    @Test
    public void testBuildTraceMetricLogExportersConsecutively() throws Exception {
        // create the OpenTelemetry SDK
        CountDownLatch countDownLatch = new CountDownLatch(3);
        CustomValidationPolicy customValidationPolicy = new CustomValidationPolicy(countDownLatch);
        OpenTelemetrySdk openTelemetry
            = TestUtils.createOpenTelemetrySdk(getHttpPipeline(customValidationPolicy), getConfiguration());

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
        TelemetryItem spanTelemetryItem = customValidationPolicy.getActualTelemetryItems()
            .stream()
            .filter(item -> item.getName().equals("RemoteDependency"))
            .findFirst()
            .get();
        TelemetryItem metricTelemetryItem = customValidationPolicy.getActualTelemetryItems()
            .stream()
            .filter(item -> item.getName().equals("Metric"))
            .findFirst()
            .get();
        TelemetryItem logTelemetryItem = customValidationPolicy.getActualTelemetryItems()
            .stream()
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
        generateSpan(openTelemetry, SpanKind.INTERNAL);
    }

    @SuppressWarnings("try")
    private static void generateSpan(OpenTelemetry openTelemetry, SpanKind spanKind) {
        Tracer tracer = openTelemetry.getTracer("Sample");
        Span span = tracer.spanBuilder("test").setSpanKind(spanKind).startSpan();
        try (Scope ignored = span.makeCurrent()) {
            span.setAttribute("name", "apple");
            span.setAttribute("color", "red");
            span.setAttribute(CUSTOM_MEASUREMENTS, customMeasurements());
        } finally {
            span.end();
        }
    }

    private static void generateMetric(OpenTelemetry openTelemetry) {
        Meter meter = openTelemetry.getMeter("Sample");
        LongCounter counter = meter.counterBuilder("test").build();
        counter.add(1L, Attributes.of(AttributeKey.stringKey("name"), "apple", AttributeKey.stringKey("color"), "red"));
    }

    private static void generateLog(OpenTelemetry openTelemetry) {
        Logger logger = openTelemetry.getLogsBridge().get("Sample");
        logger.logRecordBuilder()
            .setBody("test body")
            .setAttribute(AttributeKey.stringKey("name"), "apple")
            .setAttribute(AttributeKey.stringKey("color"), "red")
            .setAttribute(CUSTOM_MEASUREMENTS, customMeasurements())
            .emit();
    }

    private static void generateEvent(OpenTelemetry openTelemetry) {
        Logger logger = openTelemetry.getLogsBridge().get("Sample");
        logger.logRecordBuilder()
            .setBody("TestEventBody")
            .setAttribute(AttributeKey.stringKey("microsoft.custom_event.name"), "TestEvent")
            .setAttribute(AttributeKey.stringKey("name"), "apple")
            .setAttribute(CUSTOM_MEASUREMENTS, customMeasurements())
            .emit();
    }

    private static void generateExceptionLog(OpenTelemetry openTelemetry) {
        Logger logger = openTelemetry.getLogsBridge().get("Sample");
        logger.logRecordBuilder()
            .setBody("exception body")
            .setAttribute(ExceptionAttributes.EXCEPTION_STACKTRACE, "java.lang.IllegalStateException: test")
            .setAttribute(CUSTOM_MEASUREMENTS, customMeasurements())
            .emit();
    }

    private static Value<?> customMeasurements() {
        Map<String, Value<?>> measurements = new LinkedHashMap<>();
        measurements.put("itemsProcessed", Value.of(42.0));
        measurements.put("queueDepth", Value.of(7.0));
        return Value.of(measurements);
    }

    private static void validateSpan(TelemetryItem telemetryItem) {
        assertThat(telemetryItem.getName()).isEqualTo("RemoteDependency");
        assertThat(telemetryItem.getInstrumentationKey()).isEqualTo(INSTRUMENTATION_KEY);
        assertThat(telemetryItem.getTags()).containsEntry("ai.cloud.role", "unknown_service:java");
        assertThat(telemetryItem.getTags()).hasEntrySatisfying("ai.internal.sdkVersion",
            v -> assertThat(v).contains("otel"));
        assertThat(telemetryItem.getData().getBaseType()).isEqualTo("RemoteDependencyData");

        RemoteDependencyData actualData = TestUtils.toRemoteDependencyData(telemetryItem.getData().getBaseData());
        assertThat(actualData.getName()).isEqualTo("test");
        assertThat(actualData.getProperties()).containsExactly(entry("color", "red"), entry("name", "apple"));
        assertCustomMeasurements(actualData.getMeasurements(), actualData.getProperties());
    }

    private static void validateRequest(TelemetryItem telemetryItem) {
        assertThat(telemetryItem.getData().getBaseType()).isEqualTo("RequestData");
        RequestData requestData = TestUtils.toRequestData(telemetryItem.getData().getBaseData());
        assertCustomMeasurements(requestData.getMeasurements(), requestData.getProperties());
    }

    private static void validateMetric(TelemetryItem telemetryItem) {
        assertThat(telemetryItem.getInstrumentationKey()).isEqualTo(INSTRUMENTATION_KEY);
        assertThat(telemetryItem.getTags()).containsEntry("ai.cloud.role", "unknown_service:java");
        assertThat(telemetryItem.getTags()).hasEntrySatisfying("ai.internal.sdkVersion",
            v -> assertThat(v).contains("otel"));
        assertThat(telemetryItem.getData().getBaseType()).isEqualTo("MetricData");

        MetricsData metricsData = TestUtils.toMetricsData(telemetryItem.getData().getBaseData());
        assertThat(metricsData.getMetrics().size()).isEqualTo(1);
        assertThat(metricsData.getMetrics().get(0).getName()).isEqualTo("test");
        assertThat(metricsData.getMetrics().get(0).getValue()).isEqualTo(1.0);
        assertThat(metricsData.getProperties()).containsExactly(entry("color", "red"), entry("name", "apple"));
    }

    private static void validateLog(TelemetryItem telemetryItem) {
        assertThat(telemetryItem.getName()).isEqualTo("Message");
        assertThat(telemetryItem.getInstrumentationKey()).isEqualTo(INSTRUMENTATION_KEY);
        assertThat(telemetryItem.getTags()).containsEntry("ai.cloud.role", "unknown_service:java");
        assertThat(telemetryItem.getTags()).hasEntrySatisfying("ai.internal.sdkVersion",
            v -> assertThat(v).contains("otel"));
        assertThat(telemetryItem.getData().getBaseType()).isEqualTo("MessageData");

        MessageData messageData = TestUtils.toMessageData(telemetryItem.getData().getBaseData());
        assertThat(messageData.getMessage()).isEqualTo("test body");
        assertThat(messageData.getProperties()).containsOnly(entry("LoggerName", "Sample"),
            entry("SourceType", "Logger"), entry("color", "red"), entry("name", "apple"));
        assertCustomMeasurements(messageData.getMeasurements(), messageData.getProperties());
    }

    private static void validateEvent(TelemetryItem telemetryItem) {
        assertThat(telemetryItem.getName()).isEqualTo("Event");
        assertThat(telemetryItem.getInstrumentationKey()).isEqualTo(INSTRUMENTATION_KEY);
        assertThat(telemetryItem.getTags()).containsEntry("ai.cloud.role", "unknown_service:java");
        assertThat(telemetryItem.getTags()).hasEntrySatisfying("ai.internal.sdkVersion",
            v -> assertThat(v).contains("otel"));
        assertThat(telemetryItem.getData().getBaseType()).isEqualTo("EventData");

        TelemetryEventData eventData = TestUtils.toTelemetryEventData(telemetryItem.getData().getBaseData());
        System.out.println("Actual Event Properties: " + eventData.getProperties());
        assertThat(eventData.getName()).isEqualTo("TestEvent");
        assertThat(eventData.getProperties()).containsOnly(entry("name", "apple"),
            entry("microsoft.custom_event.name", "TestEvent"));
        assertCustomMeasurements(eventData.getMeasurements(), eventData.getProperties());
    }

    private static void validateException(TelemetryItem telemetryItem) {
        assertThat(telemetryItem.getData().getBaseType()).isEqualTo("ExceptionData");
        TelemetryExceptionData exceptionData
            = TestUtils.toTelemetryExceptionData(telemetryItem.getData().getBaseData());
        assertCustomMeasurements(exceptionData.getMeasurements(), exceptionData.getProperties());
    }

    private static void assertCustomMeasurements(Map<String, Double> measurements, Map<String, String> properties) {
        assertThat(measurements).containsOnly(entry("itemsProcessed", 42.0), entry("queueDepth", 7.0));
        assertThat(properties).doesNotContainKey("microsoft.custom_measurements");
    }

    private static Map<String, String> getConfiguration() {
        return Collections.singletonMap("APPLICATIONINSIGHTS_CONNECTION_STRING", CONNECTION_STRING_ENV);
    }
}
