// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.MockHttpResponse;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.NoopTracer;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.configuration.ConnectionString;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.LiveMetricsRestAPIsForClientSDKs;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.LiveMetricsRestAPIsForClientSDKsBuilder;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.IsSubscribedHeaders;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class QuickPulsePingSenderTests {

    @Test
    void endpointIsFormattedCorrectlyWhenUsingConnectionString() {
        ConnectionString connectionString = ConnectionString.parse("InstrumentationKey=testing-123");
        QuickPulsePingSender quickPulsePingSender = new QuickPulsePingSender(null, connectionString::getLiveEndpoint,
            connectionString::getInstrumentationKey, null, null, null, null, null);
        String quickPulseEndpoint = quickPulsePingSender.getQuickPulseEndpoint();
        String instrumentationKey = quickPulsePingSender.getInstrumentationKey();
        assertThat(quickPulseEndpoint).isEqualTo("https://rt.services.visualstudio.com/");
        assertThat(instrumentationKey).isEqualTo("testing-123");
    }

    @Test
    void endpointIsFormattedCorrectlyWhenUsingInstrumentationKey() {
        ConnectionString connectionString = ConnectionString.parse("InstrumentationKey=A-test-instrumentation-key");
        QuickPulsePingSender quickPulsePingSender = new QuickPulsePingSender(null, connectionString::getLiveEndpoint,
            connectionString::getInstrumentationKey, null, null, null, null, null);
        String quickPulseEndpoint = quickPulsePingSender.getQuickPulseEndpoint();
        String instrumentationKey = quickPulsePingSender.getInstrumentationKey();
        assertThat(quickPulseEndpoint).isEqualTo("https://rt.services.visualstudio.com/");
        assertThat(instrumentationKey).isEqualTo("A-test-instrumentation-key");
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
        LiveMetricsRestAPIsForClientSDKsBuilder builder = new LiveMetricsRestAPIsForClientSDKsBuilder();
        LiveMetricsRestAPIsForClientSDKs liveMetricsRestAPIsForClientSDKs
            = builder.pipeline(httpPipeline).buildClient();
        QuickPulsePingSender quickPulsePingSender
            = new QuickPulsePingSender(liveMetricsRestAPIsForClientSDKs, connectionString::getLiveEndpoint,
                connectionString::getInstrumentationKey, null, "instance1", "machine1", "qpid123", "testSdkVersion");
        IsSubscribedHeaders pingHeaders = quickPulsePingSender.ping(null);
        assertThat("true").isEqualTo(pingHeaders.getXMsQpsSubscribed());
        assertThat("1000").isEqualTo(pingHeaders.getXMsQpsServicePollingIntervalHint());
        assertThat("https://new.endpoint.com").isEqualTo(pingHeaders.getXMsQpsServiceEndpointRedirectV2());
    }
}
