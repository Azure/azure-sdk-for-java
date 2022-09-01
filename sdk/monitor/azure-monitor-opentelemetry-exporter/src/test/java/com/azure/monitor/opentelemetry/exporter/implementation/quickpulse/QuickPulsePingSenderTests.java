/*
 * ApplicationInsights-Java
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.MockHttpResponse;
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
  }

  @Test
  void endpointChangesWithRedirectHeaderAndGetNewPingInterval() {
    Map<String, String> headers = new HashMap<>();
    headers.put("x-ms-qps-service-polling-interval-hint", "1000");
    headers.put("x-ms-qps-service-endpoint-redirect-v2", "https://new.endpoint.com");
    headers.put("x-ms-qps-subscribed", "true");
    HttpHeaders httpHeaders = new HttpHeaders(headers);
    ConnectionString connectionString = ConnectionString.parse("InstrumentationKey=fake-ikey");
    HttpPipeline httpPipeline =
        new HttpPipelineBuilder()
            .httpClient(request -> Mono.just(new MockHttpResponse(request, 200, httpHeaders)))
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
