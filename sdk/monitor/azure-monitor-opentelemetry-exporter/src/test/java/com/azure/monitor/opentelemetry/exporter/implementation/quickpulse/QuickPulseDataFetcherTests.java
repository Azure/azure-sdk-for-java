// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.MockHttpResponse;
import com.azure.monitor.opentelemetry.exporter.implementation.NoopTracer;
import com.azure.monitor.opentelemetry.exporter.implementation.configuration.ConnectionString;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class QuickPulseDataFetcherTests {

    @Test
    void testGetCurrentSdkVersion() {
        ConnectionString connectionString = ConnectionString.parse("InstrumentationKey=testing-123");
        QuickPulseDataFetcher dataFetcher =
            new QuickPulseDataFetcher(
                new QuickPulseDataCollector(true),
                null,
                connectionString::getLiveEndpoint,
                connectionString::getInstrumentationKey,
                null,
                null,
                null,
                null);
        String sdkVersion = dataFetcher.getCurrentSdkVersion();
        assertThat(sdkVersion).isNotNull();
        assertThat(sdkVersion).isNotEqualTo("java:unknown");
    }

    @Test
    void endpointIsFormattedCorrectlyWhenUsingConfig() throws URISyntaxException {
        ConnectionString connectionString = ConnectionString.parse("InstrumentationKey=testing-123");
        QuickPulseDataFetcher quickPulseDataFetcher =
            new QuickPulseDataFetcher(
                new QuickPulseDataCollector(true),
                null,
                connectionString::getLiveEndpoint,
                connectionString::getInstrumentationKey,
                null,
                null,
                null,
                null);
        String quickPulseEndpoint = quickPulseDataFetcher.getQuickPulseEndpoint();
        String endpointUrl = quickPulseDataFetcher.getEndpointUrl(quickPulseEndpoint);
        URI uri = new URI(endpointUrl);
        assertThat(uri).isNotNull();
        assertThat(endpointUrl)
            .isEqualTo(
                "https://rt.services.visualstudio.com/QuickPulseService.svc/post?ikey=testing-123");
    }

    @Test
    void endpointIsFormattedCorrectlyWhenConfigIsNull() throws URISyntaxException {
        ConnectionString connectionString = ConnectionString.parse("InstrumentationKey=testing-123");
        QuickPulseDataFetcher quickPulseDataFetcher =
            new QuickPulseDataFetcher(
                new QuickPulseDataCollector(true),
                null,
                connectionString::getLiveEndpoint,
                connectionString::getInstrumentationKey,
                null,
                null,
                null,
                null);
        String quickPulseEndpoint = quickPulseDataFetcher.getQuickPulseEndpoint();
        String endpointUrl = quickPulseDataFetcher.getEndpointUrl(quickPulseEndpoint);
        URI uri = new URI(endpointUrl);
        assertThat(uri).isNotNull();
        assertThat(endpointUrl)
            .isEqualTo(
                "https://rt.services.visualstudio.com/QuickPulseService.svc/post?ikey=testing-123");
    }

    @Test
    void endpointChangesWithRedirectHeaderAndGetNewPingInterval() {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-ms-qps-service-polling-interval-hint", "1000");
        headers.put("x-ms-qps-service-endpoint-redirect-v2", "https://new.endpoint.com");
        headers.put("x-ms-qps-subscribed", "true");
        headers.put("x-ms-qps-configuration-etag", "0::randometag::2::");
        HttpHeaders httpHeaders = new HttpHeaders(headers);
        ConnectionString connectionString = ConnectionString.parse("InstrumentationKey=testing-123");
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
    }
}
