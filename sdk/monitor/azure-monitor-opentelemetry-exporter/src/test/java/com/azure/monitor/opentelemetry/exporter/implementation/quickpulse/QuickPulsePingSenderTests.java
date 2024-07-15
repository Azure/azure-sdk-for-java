// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.json.implementation.jackson.core.JsonProcessingException;
import com.azure.monitor.opentelemetry.exporter.implementation.MockHttpResponse;
import com.azure.monitor.opentelemetry.exporter.implementation.NoopTracer;
import com.azure.monitor.opentelemetry.exporter.implementation.configuration.ConnectionString;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class QuickPulsePingSenderTests {

    @Test
    void endpointIsFormattedCorrectlyWhenUsingConnectionString() throws URISyntaxException {
        ConnectionString connectionString = ConnectionString.parse("InstrumentationKey=testing-123");
        QuickPulsePingSender quickPulsePingSender =
            new QuickPulsePingSender(
                null,
                connectionString::getLiveEndpoint,
                connectionString::getInstrumentationKey,
                null,
                null,
                null,
                null,
                null);
        String quickPulseEndpoint = quickPulsePingSender.getQuickPulseEndpoint();
        String endpointUrl = quickPulsePingSender.getQuickPulsePingUri(quickPulseEndpoint);
        URI uri = new URI(endpointUrl);
        assertThat(uri).isNotNull();
        assertThat(endpointUrl).endsWith("/ping?ikey=testing-123");
        assertThat(endpointUrl)
            .isEqualTo(
                "https://rt.services.visualstudio.com/QuickPulseService.svc/ping?ikey=testing-123");
        QuickPulseConfiguration.getInstance().reset();
    }

    @Test
    void endpointIsFormattedCorrectlyWhenUsingInstrumentationKey() throws URISyntaxException {
        ConnectionString connectionString =
            ConnectionString.parse("InstrumentationKey=A-test-instrumentation-key");
        QuickPulsePingSender quickPulsePingSender =
            new QuickPulsePingSender(
                null,
                connectionString::getLiveEndpoint,
                connectionString::getInstrumentationKey,
                null,
                null,
                null,
                null,
                null);
        String quickPulseEndpoint = quickPulsePingSender.getQuickPulseEndpoint();
        String endpointUrl = quickPulsePingSender.getQuickPulsePingUri(quickPulseEndpoint);
        URI uri = new URI(endpointUrl);
        assertThat(uri).isNotNull();
        assertThat(endpointUrl)
            .endsWith(
                "/ping?ikey=A-test-instrumentation-key"); // from resources/ApplicationInsights.xml
        assertThat(endpointUrl)
            .isEqualTo(
                "https://rt.services.visualstudio.com/QuickPulseService.svc/ping?ikey=A-test-instrumentation-key");
        QuickPulseConfiguration.getInstance().reset();
    }

    @Test
    void endpointChangesWithRedirectHeaderAndGetNewPingInterval() {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-ms-qps-service-polling-interval-hint", "1000");
        headers.put("x-ms-qps-service-endpoint-redirect-v2", "https://new.endpoint.com");
        headers.put("x-ms-qps-subscribed", "true");
        headers.put("x-ms-qps-configuration-etag", "0::randometag::1::");
        HttpHeaders httpHeaders = new HttpHeaders(headers);
        ConnectionString connectionString = ConnectionString.parse("InstrumentationKey=fake-ikey");
        HttpPipeline httpPipeline =
            new HttpPipelineBuilder()
                .httpClient(request -> Mono.just(new MockHttpResponse(request, 200, httpHeaders)))
                .tracer(new NoopTracer())
                .build();
        QuickPulsePingSender quickPulsePingSender =
            new QuickPulsePingSender(
                httpPipeline,
                connectionString::getLiveEndpoint,
                connectionString::getInstrumentationKey,
                null,
                "instance1",
                "machine1",
                "qpid123",
                "testSdkVersion");
        QuickPulseHeaderInfo quickPulseHeaderInfo = quickPulsePingSender.ping(null);
        assertThat(QuickPulseStatus.QP_IS_ON).isEqualTo(quickPulseHeaderInfo.getQuickPulseStatus());
        assertThat(1000).isEqualTo(quickPulseHeaderInfo.getQpsServicePollingInterval());
        assertThat("https://new.endpoint.com")
            .isEqualTo(quickPulseHeaderInfo.getQpsServiceEndpointRedirect());
        QuickPulseConfiguration.getInstance().reset();
    }

    @Test
    void successfulPingReturnsWithEtagHeader() {
        System.out.println("Etag: " + QuickPulseConfiguration.getInstance().getEtag());
        Map<String, String> headers = new HashMap<>();
        headers.put("x-ms-qps-service-polling-interval-hint", "1000");
        headers.put("x-ms-qps-service-endpoint-redirect-v2", "https://new.endpoint.com");
        headers.put("x-ms-qps-subscribed", "true");
        headers.put("x-ms-qps-configuration-etag", "1::randometag::2::");
        HttpHeaders httpHeaders = new HttpHeaders(headers);
        ConnectionString connectionString = ConnectionString.parse("InstrumentationKey=fake-ikey");
        HttpPipeline httpPipeline =
            new HttpPipelineBuilder()
                .httpClient(request -> Mono.just(new MockHttpResponse(request, 200, httpHeaders)))
                .tracer(new NoopTracer())
                .build();
        QuickPulsePingSender quickPulsePingSender =
            new QuickPulsePingSender(
                httpPipeline,
                connectionString::getLiveEndpoint,
                connectionString::getInstrumentationKey,
                null,
                "instance1",
                "machine1",
                "qpid123",
                "testSdkVersion");
        QuickPulseHeaderInfo quickPulseHeaderInfo = quickPulsePingSender.ping(null);
        assertThat(QuickPulseStatus.QP_IS_ON).isEqualTo(quickPulseHeaderInfo.getQuickPulseStatus());
        assertThat("https://new.endpoint.com")
            .isEqualTo(quickPulseHeaderInfo.getQpsServiceEndpointRedirect());
        assertThat(QuickPulseConfiguration.getInstance().getEtag()).isEqualTo("1::randometag::2::");
        QuickPulseConfiguration.getInstance().reset();
    }

    @Test
    void successfulPingReturnsWithEtagHeaderAndRequestedMetrics() {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-ms-qps-service-polling-interval-hint", "1000");
        headers.put("x-ms-qps-service-endpoint-redirect-v2", "https://new.endpoint.com");
        headers.put("x-ms-qps-subscribed", "true");
        headers.put("x-ms-qps-configuration-etag", "2::randometag::3::");
        HttpHeaders httpHeaders = new HttpHeaders(headers);
        ConnectionString connectionString = ConnectionString.parse("InstrumentationKey=fake-ikey");

        List<Map<String, Object>> metrics = new ArrayList<>();
        Map<String, Object> metric1 = new HashMap<>();
        metric1.put("Id", "my_gauge");
        metric1.put("Aggregation", "Avg");
        metric1.put("TelemetryType", "Metric");
        metric1.put("Projection", "my_gauge");
        metric1.put("BackendAggregation", "Min");
        metric1.put("FilterGroups", new ArrayList<>());
        Map<String, Object> metric2 = new HashMap<>();
        metric2.put("Id", "MyFruitCounter");
        metric2.put("Aggregation", "Sum");
        metric2.put("TelemetryType", "Metric");
        metric2.put("Projection", "MyFruitCounter");
        metric2.put("BackendAggregation", "Max");
        metric2.put("FilterGroups", new ArrayList<>());
        metrics.add(metric1);
        metrics.add(metric2);

        Map<String, Object> metricsMap = new HashMap<>();
        metricsMap.put("Metrics", metrics);
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(metricsMap);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            jsonBody = "{}";
        }
        byte[] bodyBytes = jsonBody.getBytes(StandardCharsets.UTF_8);

        HttpPipeline httpPipeline =
            new HttpPipelineBuilder()
                .httpClient(request -> Mono.just(new MockHttpResponse(request, 200, httpHeaders, bodyBytes)))
                .tracer(new NoopTracer())
                .build();
        QuickPulsePingSender quickPulsePingSender =
            new QuickPulsePingSender(
                httpPipeline,
                connectionString::getLiveEndpoint,
                connectionString::getInstrumentationKey,
                null,
                "instance1",
                "machine1",
                "qpid123",
                "testSdkVersion");

        QuickPulseHeaderInfo quickPulseHeaderInfo = quickPulsePingSender.ping(null);
        assertThat(QuickPulseStatus.QP_IS_ON).isEqualTo(quickPulseHeaderInfo.getQuickPulseStatus());
        assertThat("https://new.endpoint.com")
            .isEqualTo(quickPulseHeaderInfo.getQpsServiceEndpointRedirect());
        assertThat(QuickPulseConfiguration.getInstance().getEtag()).isEqualTo("2::randometag::3::");
        assertThat(QuickPulseConfiguration.getInstance().getMetrics().size()).isEqualTo(2);
        assertThat(QuickPulseConfiguration.getInstance().getMetrics().get("my_gauge").getAggregation())
            .isEqualTo("Avg");
        assertThat(QuickPulseConfiguration.getInstance().getMetrics().get("my_gauge").getTelemetryType())
            .isEqualTo("Metric");
        assertThat(QuickPulseConfiguration.getInstance().getMetrics().get("my_gauge").getProjection()).isEqualTo("my_gauge");
        assertThat(QuickPulseConfiguration.getInstance().getMetrics().get("my_gauge").getId()).isEqualTo("my_gauge");
        System.out.println(QuickPulseConfiguration.getInstance().getMetrics().get("MyFruitCounter").getAggregation());
        assertThat(QuickPulseConfiguration.getInstance().getMetrics().get("MyFruitCounter").getAggregation()).isEqualTo("Sum");
        assertThat(QuickPulseConfiguration.getInstance().getMetrics().get("MyFruitCounter").getTelemetryType()).isEqualTo("Metric");
        assertThat(QuickPulseConfiguration.getInstance().getMetrics().get("MyFruitCounter").getProjection()).isEqualTo("MyFruitCounter");
        assertThat(QuickPulseConfiguration.getInstance().getMetrics().get("MyFruitCounter").getId()).isEqualTo("MyFruitCounter");
        QuickPulseConfiguration.getInstance().reset();
    }

}
