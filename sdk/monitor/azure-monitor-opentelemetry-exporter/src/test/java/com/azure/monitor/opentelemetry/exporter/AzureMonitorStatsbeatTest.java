// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.monitor.opentelemetry.exporter.implementation.MockHttpResponse;
import com.azure.monitor.opentelemetry.exporter.implementation.NoopTracer;
import com.azure.monitor.opentelemetry.exporter.implementation.models.MetricsData;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.TestUtils;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

@Execution(ExecutionMode.SAME_THREAD)
public class AzureMonitorStatsbeatTest {
    private static final String STATSBEAT_CONNECTION_STRING =
        "InstrumentationKey=00000000-0000-0000-0000-000000000000;"
            + "IngestionEndpoint=https://westus-0.in.applicationinsights.azure.com/;"
            + "LiveEndpoint=https://westus.livediagnostics.monitor.azure.com/";
    private static final String INSTRUMENTATION_KEY = "00000000-0000-0000-0000-000000000000";

    @Test
    public void testStatsbeat() throws Exception {
        // create the OpenTelemetry SDK
        CountDownLatch countDownLatch = new CountDownLatch(1);
        CustomValidationPolicy customValidationPolicy = new CustomValidationPolicy(countDownLatch);
        OpenTelemetrySdk openTelemetry =
            TestUtils.createOpenTelemetrySdk(
                getHttpPipeline(customValidationPolicy, HttpClient.createDefault()), getStatsbeatConfiguration(), STATSBEAT_CONNECTION_STRING);

        // generate a metric
        generateMetric(openTelemetry);

        // close to flush
        openTelemetry.close();

        Thread.sleep(2000);

        // wait for export
        countDownLatch.await(10, SECONDS);
        assertThat(customValidationPolicy.getUrl())
            .isEqualTo(new URL("https://westus-0.in.applicationinsights.azure.com/v2.1/track"));

        verifyStatsbeatTelemetry(customValidationPolicy);
    }

    @Test
    @DisabledOnOs(value = {OS.MAC}, disabledReason = "Unstable")
    public void testStatsbeatShutdownWhen400InvalidIKeyReturned() throws Exception {
        String fakeBody = "{\"itemsReceived\":4,\"itemsAccepted\":0,\"errors\":[{\"index\":0,\"statusCode\":400,\"message\":\"Invalid instrumentation key\"},{\"index\":1,\"statusCode\":400,\"message\":\"Invalid instrumentation key\"},{\"index\":2,\"statusCode\":400,\"message\":\"Invalid instrumentation key\"},{\"index\":3,\"statusCode\":400,\"message\":\"Invalid instrumentation key\"}]}";
        verifyStatsbeatShutdownOrnNot(fakeBody, true);
    }

    @Test
    public void testStatsbeatNotShutDownWhen400InvalidDataReturned() throws Exception {
        String fakeBody = "{\"itemsReceived\":1,\"itemsAccepted\":0,\"errors\":[{\"index\":0,\"statusCode\":400,\"message\":\"102: Field 'time' on type 'Envelope' is not a valid time string. Expected: date, Actual: fake\"}]}";
        verifyStatsbeatShutdownOrnNot(fakeBody, false);
    }

    private void verifyStatsbeatShutdownOrnNot(String fakeBody, boolean shutdown) throws Exception {
        MockedHttpClient mockedHttpClient =
            new MockedHttpClient(
                request -> {
                    return Mono.just(new MockHttpResponse(request, 400, new HttpHeaders(), fakeBody.getBytes()));
                });

        // create OpenTelemetrySdk
        CountDownLatch countDownLatch = new CountDownLatch(1);
        CustomValidationPolicy customValidationPolicy = new CustomValidationPolicy(countDownLatch);
        OpenTelemetrySdk openTelemetrySdk =
            TestUtils.createOpenTelemetrySdk(
                getHttpPipeline(customValidationPolicy, mockedHttpClient), getStatsbeatConfiguration(), STATSBEAT_CONNECTION_STRING);

        generateMetric(openTelemetrySdk);

        // close to flush
        openTelemetrySdk.close();

        Thread.sleep(2000);

        // wait for export
        countDownLatch.await(10, SECONDS);
        assertThat(customValidationPolicy.getUrl())
            .isEqualTo(new URL("https://westus-0.in.applicationinsights.azure.com/v2.1/track"));

        if (shutdown) {
            assertThat(customValidationPolicy.getActualTelemetryItems().stream().filter(item -> item.getName().equals("Statsbeat")).count()).isEqualTo(0);
        } else {
            verifyStatsbeatTelemetry(customValidationPolicy);
        }
    }

    private HttpPipeline getHttpPipeline(@Nullable HttpPipelinePolicy policy, HttpClient httpClient) {
        return new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(policy == null ? new HttpPipelinePolicy[0] : new HttpPipelinePolicy[]{policy})
            .tracer(new NoopTracer())
            .build();
    }

    private static void generateMetric(OpenTelemetry openTelemetry) {
        Meter meter = openTelemetry.getMeter("Sample");
        LongCounter counter = meter.counterBuilder("test").build();
        counter.add(
            1L,
            Attributes.of(
                AttributeKey.stringKey("name"), "apple", AttributeKey.stringKey("color"), "red"));
    }

    private void verifyStatsbeatTelemetry(CustomValidationPolicy customValidationPolicy) {
        TelemetryItem attachStatsbeat =
            customValidationPolicy.getActualTelemetryItems().stream()
                .filter(item -> item.getName().equals("Statsbeat"))
                .filter(item -> {
                    return getMetricName(item).equals("Attach");
                })
                .findFirst()
                .get();
        validateAttachStatsbeat(attachStatsbeat);

        TelemetryItem featureStatsbeat =
            customValidationPolicy.getActualTelemetryItems().stream()
                .filter(item -> item.getName().equals("Statsbeat"))
                .filter(item -> {
                    return getMetricName(item).equals("Feature");
                })
                .findFirst()
                .get();
        validateFeatureStatsbeat(featureStatsbeat);
    }

    private static Map<String, String> getStatsbeatConfiguration() {
        Map<String, String> map = new HashMap<>(3);
        map.put("APPLICATIONINSIGHTS_CONNECTION_STRING", STATSBEAT_CONNECTION_STRING);
        map.put("STATSBEAT_LONG_INTERVAL_SECONDS_PROPERTY_NAME", "1");
        map.put("STATSBEAT_SHORT_INTERVAL_SECONDS_PROPERTY_NAME", "1");
        return map;
    }

    private static void validateAttachStatsbeat(TelemetryItem telemetryItem) {
        assertThat(telemetryItem.getData().getBaseType()).isEqualTo("MetricData");
        assertThat(getMetricName(telemetryItem)).isEqualTo("Attach");
        Map<String, String> properties = getMetricProperties(telemetryItem);
        assertThat(properties).contains(entry("rp", "unknown"), entry("attach", "Manual"), entry("language", "java"));
        assertThat(properties).containsKeys("attach", "cikey", "language", "os", "rp", "runtimeVersion", "version");
    }

    private static void validateFeatureStatsbeat(TelemetryItem telemetryItem) {
        assertThat(telemetryItem.getData().getBaseType()).isEqualTo("MetricData");
        assertThat(getMetricName(telemetryItem)).isEqualTo("Feature");
        Map<String, String> properties = getMetricProperties(telemetryItem);
        assertThat(properties).contains(entry("type", "0"), entry("language", "java"));
        assertThat(properties).containsKeys("feature", "cikey", "language", "os", "rp", "runtimeVersion", "version");
    }

    @SuppressWarnings("unchecked")
    private static String getMetricName(TelemetryItem telemetryItem) {
        return (String) (((List<Map<String, Object>>) telemetryItem.getData().getBaseData().getAdditionalProperties().get("metrics")).get(0)).get("name");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> getMetricProperties(TelemetryItem telemetryItem) {
        return (Map<String, String>) telemetryItem.getData().getBaseData().getAdditionalProperties().get("properties");
    }

    private static class MockedHttpClient implements HttpClient {

        private final AtomicInteger count = new AtomicInteger();
        private final Function<HttpRequest, Mono<HttpResponse>> handler;

        MockedHttpClient(Function<HttpRequest, Mono<HttpResponse>> handler) {
            this.handler = handler;
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest httpRequest) {
            count.getAndIncrement();
            return handler.apply(httpRequest);
        }

        int getCount() {
            return count.get();
        }
    }
}
