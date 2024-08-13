// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.json.JsonProviders;
import com.azure.json.JsonWriter;
import com.azure.monitor.opentelemetry.exporter.implementation.MockHttpResponse;
import com.azure.monitor.opentelemetry.exporter.implementation.NoopTracer;
import com.azure.monitor.opentelemetry.exporter.implementation.configuration.ConnectionString;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class QuickPulsePingSenderTests {

    @Test
    void endpointIsFormattedCorrectlyWhenUsingConnectionString() throws URISyntaxException {
        ConnectionString connectionString = ConnectionString.parse("InstrumentationKey=testing-123");
        QuickPulseConfiguration quickPulseConfiguration = new QuickPulseConfiguration();
        QuickPulsePingSender quickPulsePingSender = new QuickPulsePingSender(null, connectionString::getLiveEndpoint,
            connectionString::getInstrumentationKey, null, null, null, null, null, quickPulseConfiguration);
        String quickPulseEndpoint = quickPulsePingSender.getQuickPulseEndpoint();
        String endpointUrl = quickPulsePingSender.getQuickPulsePingUri(quickPulseEndpoint);
        URI uri = new URI(endpointUrl);
        assertThat(uri).isNotNull();
        assertThat(endpointUrl).endsWith("/ping?ikey=testing-123");
        assertThat(endpointUrl)
            .isEqualTo("https://rt.services.visualstudio.com/QuickPulseService.svc/ping?ikey=testing-123");
    }

    @Test
    void endpointIsFormattedCorrectlyWhenUsingInstrumentationKey() throws URISyntaxException {
        ConnectionString connectionString = ConnectionString.parse("InstrumentationKey=A-test-instrumentation-key");
        QuickPulseConfiguration quickPulseConfiguration = new QuickPulseConfiguration();
        QuickPulsePingSender quickPulsePingSender = new QuickPulsePingSender(null, connectionString::getLiveEndpoint,
            connectionString::getInstrumentationKey, null, null, null, null, null, quickPulseConfiguration);
        String quickPulseEndpoint = quickPulsePingSender.getQuickPulseEndpoint();
        String endpointUrl = quickPulsePingSender.getQuickPulsePingUri(quickPulseEndpoint);
        URI uri = new URI(endpointUrl);
        assertThat(uri).isNotNull();
        assertThat(endpointUrl).endsWith("/ping?ikey=A-test-instrumentation-key"); // from resources/ApplicationInsights.xml
        assertThat(endpointUrl).isEqualTo(
            "https://rt.services.visualstudio.com/QuickPulseService.svc/ping?ikey=A-test-instrumentation-key");
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
        QuickPulseConfiguration quickPulseConfiguration = new QuickPulseConfiguration();
        HttpPipeline httpPipeline = new HttpPipelineBuilder()
            .httpClient(request -> Mono.just(new MockHttpResponse(request, 200, httpHeaders)))
            .tracer(new NoopTracer())
            .build();
        QuickPulsePingSender quickPulsePingSender = new QuickPulsePingSender(httpPipeline,
            connectionString::getLiveEndpoint, connectionString::getInstrumentationKey, null, "instance1", "machine1",
            "qpid123", "testSdkVersion", quickPulseConfiguration);
        QuickPulseHeaderInfo quickPulseHeaderInfo = quickPulsePingSender.ping(null);
        assertThat(QuickPulseStatus.QP_IS_ON).isEqualTo(quickPulseHeaderInfo.getQuickPulseStatus());
        assertThat(1000).isEqualTo(quickPulseHeaderInfo.getQpsServicePollingInterval());
        assertThat("https://new.endpoint.com").isEqualTo(quickPulseHeaderInfo.getQpsServiceEndpointRedirect());
        assertThat(quickPulseConfiguration.getEtag()).isEqualTo("0::randometag::1::");
    }

    @Test
    void successfulPingReturnsWithEtagHeaderAndRequestedMetrics() throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-ms-qps-service-polling-interval-hint", "1000");
        headers.put("x-ms-qps-service-endpoint-redirect-v2", "https://new.endpoint.com");
        headers.put("x-ms-qps-subscribed", "true");
        headers.put("x-ms-qps-configuration-etag", "2::randometag::3::");
        HttpHeaders httpHeaders = new HttpHeaders(headers);
        ConnectionString connectionString = ConnectionString.parse("InstrumentationKey=fake-ikey");
        QuickPulseConfiguration quickPulseConfiguration = new QuickPulseConfiguration();

        List<Map<String, Object>> metrics = new ArrayList<>();
        Map<String, Object> metric1 = new HashMap<>();
        metric1.put("Id", "my_gauge");
        metric1.put("Aggregation", "Avg");
        metric1.put("TelemetryType", "Metric");
        metric1.put("Projection", "my_gauge");
        metric1.put("BackendAggregation", "Min");

        ArrayList<HashMap<String, ArrayList<HashMap<String, String>>>> filterGroups = new ArrayList<>();
        HashMap<String, ArrayList<HashMap<String, String>>> filterGroup = new HashMap<>();
        ArrayList<HashMap<String, String>> filters = new ArrayList<>();
        HashMap<String, String> filterOne = new HashMap<>();
        filterOne.put("FieldName", "Test");
        filterOne.put("Predicate", "Equals");
        filterOne.put("Comparand", "Value");
        filters.add(filterOne);
        filterGroup.put("Filters", filters);
        filterGroups.add(filterGroup);
        metric1.put("FilterGroups", filterGroups);

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
        metricsMap.put("DocumentStreams", null);
        metricsMap.put("ETag", "2::randometag::3::");
        metricsMap.put("Metrics", metrics);
        metricsMap.put("QuotaInfo", null);
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonBody;

        try {
            jsonBody = objectMapper.writeValueAsString(metricsMap);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            jsonBody = "{}";
        }
        byte[] bodyBytes = jsonBody.getBytes(StandardCharsets.UTF_8);

        HttpPipeline httpPipeline = new HttpPipelineBuilder()
            .httpClient(request -> Mono.just(new MockHttpResponse(request, 200, httpHeaders, bodyBytes)))
            .tracer(new NoopTracer())
            .build();
        QuickPulsePingSender quickPulsePingSender = new QuickPulsePingSender(httpPipeline,
            connectionString::getLiveEndpoint, connectionString::getInstrumentationKey, null, "instance1", "machine1",
            "qpid123", "testSdkVersion", quickPulseConfiguration);

        QuickPulseHeaderInfo quickPulseHeaderInfo = quickPulsePingSender.ping(null);
        assertThat(QuickPulseStatus.QP_IS_ON).isEqualTo(quickPulseHeaderInfo.getQuickPulseStatus());
        assertThat("https://new.endpoint.com").isEqualTo(quickPulseHeaderInfo.getQpsServiceEndpointRedirect());
        assertThat(quickPulseConfiguration.getEtag()).isEqualTo("2::randometag::3::");
        assertThat(quickPulseConfiguration.getDerivedMetrics().size()).isEqualTo(1);
        assertThat(quickPulseConfiguration.getDerivedMetrics().get("Metric").size()).isEqualTo(2);
        ArrayList<QuickPulseConfiguration.DerivedMetricInfo> metricCategory
            = quickPulseConfiguration.getDerivedMetrics().get("Metric");
        assertThat(metricCategory.get(0).getAggregation()).isEqualTo("Avg");
        assertThat(metricCategory.get(0).getTelemetryType()).isEqualTo("Metric");
        assertThat(metricCategory.get(0).getFilterGroups().size() == 1);
        assertThat(metricCategory.get(0).getFilterGroups().get(0).getFieldName()).isEqualTo("Test");
        assertThat(metricCategory.get(0).getFilterGroups().get(0).getOperator()).isEqualTo("Equals");
        assertThat(metricCategory.get(0).getFilterGroups().get(0).getComparand()).isEqualTo("Value");
        assertThat(metricCategory.get(0).getProjection()).isEqualTo("my_gauge");
        assertThat(metricCategory.get(0).getId()).isEqualTo("my_gauge");
        assertThat(metricCategory.get(1).getAggregation()).isEqualTo("Sum");
        assertThat(metricCategory.get(1).getTelemetryType()).isEqualTo("Metric");
        assertThat(metricCategory.get(1).getProjection()).isEqualTo("MyFruitCounter");
        assertThat(metricCategory.get(1).getId()).isEqualTo("MyFruitCounter");
    }

}
