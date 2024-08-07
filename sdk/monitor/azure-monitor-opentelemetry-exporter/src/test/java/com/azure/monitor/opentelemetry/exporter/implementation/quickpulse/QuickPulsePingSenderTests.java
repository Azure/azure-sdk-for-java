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

class QuickPulsePingSenderTests {

    @Test
    void endpointIsFormattedCorrectlyWhenUsingConnectionString() throws URISyntaxException {
        ConnectionString connectionString = ConnectionString.parse("InstrumentationKey=testing-123");
        QuickPulsePingSender quickPulsePingSender = new QuickPulsePingSender(null, connectionString::getLiveEndpoint,
            connectionString::getInstrumentationKey, null, null, null, null, null);
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
        QuickPulsePingSender quickPulsePingSender = new QuickPulsePingSender(null, connectionString::getLiveEndpoint,
            connectionString::getInstrumentationKey, null, null, null, null, null);
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
        HttpHeaders httpHeaders = new HttpHeaders(headers);
        ConnectionString connectionString = ConnectionString.parse("InstrumentationKey=fake-ikey");
        HttpPipeline httpPipeline = new HttpPipelineBuilder()
            .httpClient(request -> Mono.just(new MockHttpResponse(request, 200, httpHeaders)))
            .tracer(new NoopTracer())
            .build();
        QuickPulsePingSender quickPulsePingSender
            = new QuickPulsePingSender(httpPipeline, connectionString::getLiveEndpoint,
                connectionString::getInstrumentationKey, null, "instance1", "machine1", "qpid123", "testSdkVersion");
        QuickPulseHeaderInfo quickPulseHeaderInfo = quickPulsePingSender.ping(null);
        assertThat(QuickPulseStatus.QP_IS_ON).isEqualTo(quickPulseHeaderInfo.getQuickPulseStatus());
        assertThat(1000).isEqualTo(quickPulseHeaderInfo.getQpsServicePollingInterval());
        assertThat("https://new.endpoint.com").isEqualTo(quickPulseHeaderInfo.getQpsServiceEndpointRedirect());
    }
}
