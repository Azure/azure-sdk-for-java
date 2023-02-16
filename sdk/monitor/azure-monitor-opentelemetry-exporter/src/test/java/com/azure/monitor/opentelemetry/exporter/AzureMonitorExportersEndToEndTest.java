// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.utils.TestConfigurationSource;
import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationBuilder;
import com.azure.core.util.FluxUtil;
import com.azure.monitor.opentelemetry.exporter.implementation.models.MessageData;
import com.azure.monitor.opentelemetry.exporter.implementation.models.MetricsData;
import com.azure.monitor.opentelemetry.exporter.implementation.models.RemoteDependencyData;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.TestUtils;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.zip.GZIPInputStream;

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
        assertThat(customValidationPolicy.url)
            .isEqualTo(new URL("https://test.in.applicationinsights.azure.com/v2.1/track"));
        assertThat(customValidationPolicy.actualTelemetryItems.size()).isEqualTo(1);

        // validate span
        validateSpan(customValidationPolicy.actualTelemetryItems.get(0));
    }

    @Test
    public void testBuildMetricExporter() throws Exception {
        // create the OpenTelemetry SDK
        CountDownLatch countDownLatch = new CountDownLatch(1);
        CustomValidationPolicy customValidationPolicy = new CustomValidationPolicy(countDownLatch);
        OpenTelemetry openTelemetry =
            TestUtils.createOpenTelemetrySdk(
                getHttpPipeline(customValidationPolicy), getConfiguration());

        // generate a metric
        generateMetric(openTelemetry);

        // wait for export
        countDownLatch.await(10, SECONDS);
        assertThat(customValidationPolicy.url)
            .isEqualTo(new URL("https://test.in.applicationinsights.azure.com/v2.1/track"));
        assertThat(customValidationPolicy.actualTelemetryItems.size()).isEqualTo(1);

        // validate metric
        validateMetric(customValidationPolicy.actualTelemetryItems.get(0));
    }

    @Test
    public void testBuildLogExporter() throws Exception {
        // create the OpenTelemetry SDK
        CountDownLatch countDownLatch = new CountDownLatch(1);
        CustomValidationPolicy customValidationPolicy = new CustomValidationPolicy(countDownLatch);
        OpenTelemetrySdk openTelemetry =
            TestUtils.createOpenTelemetrySdkDeprecated(
                getHttpPipeline(customValidationPolicy), getConfiguration());

        // generate a log
        generateLog(openTelemetry);

        // wait for export
        countDownLatch.await(10, SECONDS);
        assertThat(customValidationPolicy.url)
            .isEqualTo(new URL("https://test.in.applicationinsights.azure.com/v2.1/track"));
        assertThat(customValidationPolicy.actualTelemetryItems.size()).isEqualTo(1);

        // validate log
        validateLog(customValidationPolicy.actualTelemetryItems.get(0));
    }

    @Test
    public void testBuildTraceMetricLogExportersConsecutively() throws Exception {
        // create the OpenTelemetry SDK
        CountDownLatch countDownLatch = new CountDownLatch(3);
        CustomValidationPolicy customValidationPolicy = new CustomValidationPolicy(countDownLatch);
        OpenTelemetrySdk openTelemetry =
            TestUtils.createOpenTelemetrySdkDeprecated(
                getHttpPipeline(customValidationPolicy), getConfiguration());

        // generate telemetry
        generateSpan(openTelemetry);
        generateMetric(openTelemetry);
        generateLog(openTelemetry);

        // wait for export
        countDownLatch.await(10, SECONDS);
        assertThat(customValidationPolicy.url)
            .isEqualTo(new URL("https://test.in.applicationinsights.azure.com/v2.1/track"));
        assertThat(customValidationPolicy.actualTelemetryItems.size()).isEqualTo(3);

        // validate telemetry
        TelemetryItem spanTelemetryItem =
            customValidationPolicy.actualTelemetryItems.stream()
                .filter(item -> item.getName().equals("RemoteDependency"))
                .findFirst()
                .get();
        TelemetryItem metricTelemetryItem =
            customValidationPolicy.actualTelemetryItems.stream()
                .filter(item -> item.getName().equals("Metric"))
                .findFirst()
                .get();
        TelemetryItem logTelemetryItem =
            customValidationPolicy.actualTelemetryItems.stream()
                .filter(item -> item.getName().equals("Message"))
                .findFirst()
                .get();
        validateSpan(spanTelemetryItem);
        validateMetric(metricTelemetryItem);
        validateLog(logTelemetryItem);

        // TODO (trask) also export and validate logs in this test
    }

    private static Configuration getConfiguration() {
        return new ConfigurationBuilder(
            new TestConfigurationSource(),
            new TestConfigurationSource(),
            new TestConfigurationSource()
                .put("APPLICATIONINSIGHTS_CONNECTION_STRING", CONNECTION_STRING_ENV))
            .build();
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

    private static void generateLog(OpenTelemetrySdk openTelemetry) {
        Logger logger = openTelemetry.getSdkLoggerProvider().get("Sample");
        logger
            .logRecordBuilder()
            .setBody("test body")
            .setAttribute(AttributeKey.stringKey("name"), "apple")
            .setAttribute(AttributeKey.stringKey("color"), "red")
            .emit();
    }

    private static void validateSpan(TelemetryItem telemetryItem) {
        assertThat(telemetryItem.getName()).isEqualTo("RemoteDependency");
        assertThat(telemetryItem.getInstrumentationKey()).isEqualTo(INSTRUMENTATION_KEY);
        assertThat(telemetryItem.getTags()).containsEntry("ai.cloud.role", "unknown_service:java");
        assertThat(telemetryItem.getTags())
            .hasEntrySatisfying("ai.internal.sdkVersion", v -> assertThat(v).contains("otel"));
        assertThat(telemetryItem.getData().getBaseType()).isEqualTo("RemoteDependencyData");
        RemoteDependencyData actualData = (RemoteDependencyData) telemetryItem.getData().getBaseData();
        assertThat(actualData.getName()).isEqualTo("test");
        assertThat(actualData.getProperties())
            .containsExactly(entry("color", "red"), entry("name", "apple"));
    }

    private static void validateMetric(TelemetryItem telemetryItem) {
        assertThat(telemetryItem.getName()).isEqualTo("Metric");
        assertThat(telemetryItem.getInstrumentationKey()).isEqualTo(INSTRUMENTATION_KEY);
        assertThat(telemetryItem.getTags()).containsEntry("ai.cloud.role", "unknown_service:java");
        assertThat(telemetryItem.getTags())
            .hasEntrySatisfying("ai.internal.sdkVersion", v -> assertThat(v).contains("otel"));
        assertThat(telemetryItem.getData().getBaseType()).isEqualTo("MetricData");
        MetricsData actualMetricsData = (MetricsData) telemetryItem.getData().getBaseData();
        assertThat(actualMetricsData.getMetrics().get(0).getValue()).isEqualTo(1);
        assertThat(actualMetricsData.getMetrics().get(0).getName()).isEqualTo("test");
        assertThat(actualMetricsData.getProperties())
            .containsExactly(entry("color", "red"), entry("name", "apple"));
    }

    private static void validateLog(TelemetryItem telemetryItem) {
        assertThat(telemetryItem.getName()).isEqualTo("Message");
        assertThat(telemetryItem.getInstrumentationKey()).isEqualTo(INSTRUMENTATION_KEY);
        assertThat(telemetryItem.getTags()).containsEntry("ai.cloud.role", "unknown_service:java");
        assertThat(telemetryItem.getTags())
            .hasEntrySatisfying("ai.internal.sdkVersion", v -> assertThat(v).contains("otel"));
        assertThat(telemetryItem.getData().getBaseType()).isEqualTo("MessageData");
        MessageData actualData = (MessageData) telemetryItem.getData().getBaseData();
        assertThat(actualData.getMessage()).isEqualTo("test body");
        assertThat(actualData.getProperties())
            .containsOnly(
                entry("LoggerName", "Sample"),
                entry("SourceType", "Logger"),
                entry("color", "red"),
                entry("name", "apple"));
    }

    private static class CustomValidationPolicy implements HttpPipelinePolicy {

        private final CountDownLatch countDown;
        private volatile URL url;
        private final List<TelemetryItem> actualTelemetryItems = new CopyOnWriteArrayList<>();

        CustomValidationPolicy(CountDownLatch countDown) {
            this.countDown = countDown;
        }

        @Override
        public Mono<HttpResponse> process(
            HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            url = context.getHttpRequest().getUrl();
            Mono<String> asyncBytes =
                FluxUtil.collectBytesInByteBufferStream(context.getHttpRequest().getBody())
                    .map(CustomValidationPolicy::ungzip);
            asyncBytes.subscribe(
                value -> {
                    ObjectMapper objectMapper = createObjectMapper();
                    try (MappingIterator<TelemetryItem> i =
                             objectMapper.readerFor(TelemetryItem.class).readValues(value)) {
                        while (i.hasNext()) {
                            actualTelemetryItems.add(i.next());
                        }
                        countDown.countDown();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            return next.process();
        }

        // decode gzipped request raw bytes back to original request body
        private static String ungzip(byte[] rawBytes) {
            if (rawBytes.length == 0) {
                return "";
            }
            try (GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(rawBytes))) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] data = new byte[1024];
                int read;
                while ((read = in.read(data, 0, data.length)) != -1) {
                    baos.write(data, 0, read);
                }
                return new String(baos.toByteArray(), StandardCharsets.UTF_8);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private static ObjectMapper createObjectMapper() {
            ObjectMapper objectMapper = new ObjectMapper();
            // handle JSR-310 (java 8) dates with Jackson by configuring ObjectMapper to use this
            // dependency and not (de)serialize Instant as timestamps that it does by default
            objectMapper.findAndRegisterModules().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            return objectMapper;
        }
    }
}
