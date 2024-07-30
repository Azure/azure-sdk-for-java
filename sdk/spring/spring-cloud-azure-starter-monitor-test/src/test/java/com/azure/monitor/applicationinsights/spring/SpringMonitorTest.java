// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.monitor.applicationinsights.spring;

import com.azure.Application;
import com.azure.Controller;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.monitor.applicationinsights.spring.selfdiagnostics.SelfDiagnosticsLevel;
import com.azure.monitor.opentelemetry.exporter.implementation.models.*;
import io.opentelemetry.sdk.common.internal.OtelVersion;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import reactor.util.annotation.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    classes = {Application.class, SpringMonitorTest.TestConfig.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "applicationinsights.connection.string=InstrumentationKey=00000000-0000-0000-0000-0FEEDDADBEEF;IngestionEndpoint=https://test.in.applicationinsights.azure.com/;LiveEndpoint=https://test.livediagnostics.monitor.azure.com/"
    })
class SpringMonitorTest {

    private static CountDownLatch countDownLatch;

    private static CustomValidationPolicy customValidationPolicy;

    @Autowired
    private TestRestTemplate restTemplate;

    @TestConfiguration
    static class TestConfig {

        @Bean
        HttpPipeline httpPipeline() {
            countDownLatch = new CountDownLatch(2);
            customValidationPolicy = new CustomValidationPolicy(countDownLatch);
            return getHttpPipeline(customValidationPolicy);
        }

        HttpPipeline getHttpPipeline(@Nullable HttpPipelinePolicy policy) {
            return new HttpPipelineBuilder()
                .httpClient(HttpClient.createDefault())
                .policies(policy)
                .build();
        }

        @Bean
        @Primary
        SelfDiagnosticsLevel testSelfDiagnosticsLevel() {
            return SelfDiagnosticsLevel.DEBUG;
        }
    }

    @Test
    public void shouldMonitor() throws InterruptedException, MalformedURLException {

        // Only required with GraalVM native test execution
        // we aren't sure why this is needed, seems to be a Logback issue with GraalVM native
        SpringMonitorTest.class.getResourceAsStream("/logback.xml");

        String response = restTemplate.getForObject(Controller.URL, String.class);
        assertThat(response).isEqualTo("OK!");

        countDownLatch.await(10, SECONDS);

        assertThat(customValidationPolicy.url)
            .isEqualTo(new URL("https://test.in.applicationinsights.azure.com/v2.1/track"));

        List<TelemetryItem> telemetryItems = getTelemetryItems();

        List<TelemetryItem> logs;
        List<TelemetryItem> remoteDependencies;
        List<TelemetryItem> requests;

        // wait for at least 1 log, 1 dependency, and 1 request
        long start = System.currentTimeMillis();
        boolean found;
        do {
            telemetryItems = getTelemetryItems();
            logs = getItemsForType(telemetryItems, "Message");
            remoteDependencies = getItemsForType(telemetryItems, "RemoteDependency");
            requests = getItemsForType(telemetryItems, "Request");
            found = !logs.isEmpty() && !remoteDependencies.isEmpty() && !requests.isEmpty();
        }
        while (!found && System.currentTimeMillis() - start < SECONDS.toMillis(10));

        // Log telemetry
        TelemetryItem firstLogTelemetry = logs.get(0);
        MonitorDomain logBaseData = firstLogTelemetry.getData().getBaseData();
        MessageData logData = (MessageData) logBaseData;
        assertThat(logData.getMessage()).startsWith("Starting SpringMonitorTest using");
        assertThat(logData.getSeverityLevel()).isEqualTo(SeverityLevel.INFORMATION);

        // SQL telemetry
        TelemetryItem remoteDependency = remoteDependencies.get(0);
        MonitorDomain remoteBaseData = remoteDependency.getData().getBaseData();
        RemoteDependencyData remoteDependencyData = (RemoteDependencyData) remoteBaseData;
        assertThat(remoteDependencyData.getType()).isEqualTo("SQL");
        assertThat(remoteDependencyData.getData())
            .isEqualTo("create table test_table (id bigint not null, primary key (id))");

        // HTTP telemetry
        TelemetryItem request = requests.get(0);
        MonitorDomain requestBaseData = request.getData().getBaseData();
        RequestData requestData = (RequestData) requestBaseData;
        assertThat(requestData.getUrl()).contains(Controller.URL);
        assertThat(requestData.isSuccess()).isTrue();
        assertThat(requestData.getResponseCode()).isEqualTo("200");
        assertThat(requestData.getName()).isEqualTo("GET /controller-url");
    }

    @NotNull
    private static List<TelemetryItem> getTelemetryItems() {
        return customValidationPolicy.actualTelemetryItems.stream()
            .filter(item -> {
                MonitorDomain baseData = item.getData().getBaseData();
                return !(baseData instanceof MetricsData) || isSpecialOtelResourceMetric((MetricsData) baseData);
            })
            .collect(Collectors.toList());
    }

    @NotNull
    private static List<TelemetryItem> getItemsForType(List<TelemetryItem> telemetryItems, String type) {
        return telemetryItems.stream()
            .filter(telemetry -> telemetry.getName().equals(type))
            .collect(Collectors.toList());
    }

    @Test
    void verifyOpenTelemetryVersion() {
        String currentOTelVersion = OtelVersion.VERSION;
        assertThat(OpenTelemetryVersionCheckRunner.STARTER_OTEL_VERSION)
            .as(
                "Dear developer, You may have updated the OpenTelemetry dependencies of spring-cloud-azure-starter-monitor without updating the OTel starter version declared in "
                    + OpenTelemetryVersionCheckRunner.class
                    + ".")
            .isEqualTo(currentOTelVersion);
    }

    private static boolean isSpecialOtelResourceMetric(MetricsData baseData) {
        return baseData.getMetrics().stream().noneMatch(metricDataPoint -> metricDataPoint.getName().equals("_OTELRESOURCE_"));
    }
}
