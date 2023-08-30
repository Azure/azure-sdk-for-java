// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.azure.core.http.*;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.monitor.opentelemetry.exporter.implementation.models.*;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.trace.export.SpanExporter;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.util.annotation.Nullable;

@SpringBootTest(
    classes = {Application.class, SpringMonitorTest.TestConfiguration.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "applicationinsights.connection.string=InstrumentationKey=00000000-0000-0000-0000-0FEEDDADBEEF;IngestionEndpoint=https://test.in.applicationinsights.azure.com/;LiveEndpoint=https://test.livediagnostics.monitor.azure.com/"
    })
public class SpringMonitorTest {

  private static CountDownLatch countDownLatch;

  private static CustomValidationPolicy customValidationPolicy;

  @Autowired private TestRestTemplate restTemplate;

  // See io.opentelemetry.instrumentation.spring.autoconfigure.OpenTelemetryAutoConfiguration
  @Autowired ObjectProvider<List<SpanExporter>> otelSpanExportersProvider;

  // See io.opentelemetry.instrumentation.spring.autoconfigure.OpenTelemetryAutoConfiguration
  @Autowired ObjectProvider<List<LogRecordExporter>> otelLoggerExportersProvider;

  // See io.opentelemetry.instrumentation.spring.autoconfigure.OpenTelemetryAutoConfiguration
  @Autowired ObjectProvider<List<MetricExporter>> otelMetricExportersProvider;

  @Configuration(proxyBeanMethods = false)
  static class TestConfiguration {

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
  }

  @Test
  public void applicationContextShouldOnlyContainTheAzureSpanExporter() {
    List<SpanExporter> spanExporters = otelSpanExportersProvider.getIfAvailable();
    assertThat(spanExporters).hasSize(1);

    SpanExporter spanExporter = spanExporters.get(0);
    String exporterClassName = spanExporter.getClass().getName();
    assertThat(exporterClassName)
        .isEqualTo(
            "com.azure.monitor.opentelemetry.exporter.AzureMonitorTraceExporter"); // AzureMonitorTraceExporter is not public
  }

  @Test
  public void applicationContextShouldOnlyContainTheAzureLogRecordExporter() {
    List<LogRecordExporter> logRecordExporters = otelLoggerExportersProvider.getIfAvailable();
    assertThat(logRecordExporters).hasSize(1);

    LogRecordExporter logRecordExporter = logRecordExporters.get(0);
    String exporterClassName = logRecordExporter.getClass().getName();
    assertThat(exporterClassName)
        .isEqualTo(
            "com.azure.monitor.opentelemetry.exporter.AzureMonitorLogRecordExporter"); // AzureMonitorLogRecordExporter is not public
  }

  @Test
  public void applicationContextShouldOnlyContainTheAzureMetricExporter() {
    List<MetricExporter> metricExporters = otelMetricExportersProvider.getIfAvailable();
    assertThat(metricExporters).hasSize(1);

    MetricExporter metricExporter = metricExporters.get(0);
    String exporterClassName = metricExporter.getClass().getName();
    assertThat(exporterClassName)
        .isEqualTo(
            "com.azure.monitor.opentelemetry.exporter.AzureMonitorMetricExporter"); // AzureMonitorMetricExporter is not public
  }

  @Test
  public void shouldMonitor() throws InterruptedException, MalformedURLException {

    // Only required with GraalVM native test execution
    SpringMonitorTest.class.getResourceAsStream("/logback.xml");

    String response = restTemplate.getForObject(Controller.URL, String.class);
    assertThat(response).isEqualTo("OK!");

    countDownLatch.await(10, SECONDS);

    assertThat(customValidationPolicy.url)
        .isEqualTo(new URL("https://test.in.applicationinsights.azure.com/v2.1/track"));

    List<TelemetryItem> telemetryItems = customValidationPolicy.actualTelemetryItems;
    List<String> telemetryTypes =
        telemetryItems.stream().map(telemetry -> telemetry.getName()).collect(Collectors.toList());
    assertThat(telemetryItems.size()).as("Telemetry: " + telemetryTypes).isEqualTo(5);

    // Log telemetry
    List<TelemetryItem> logs =
        telemetryItems.stream()
            .filter(telemetry -> telemetry.getName().equals("Message"))
            .collect(Collectors.toList());
    assertThat(logs).hasSize(3);

    TelemetryItem firstLogTelemetry = logs.get(0);
    MonitorDomain logBaseData = firstLogTelemetry.getData().getBaseData();
    MessageData logData = (MessageData) logBaseData;
    assertThat(logData.getMessage())
        .isEqualTo("Initializing Spring DispatcherServlet 'dispatcherServlet'");
    assertThat(logData.getSeverityLevel()).isEqualTo(SeverityLevel.INFORMATION);

    // SQL telemetry
    List<TelemetryItem> remoteDependencies =
        telemetryItems.stream()
            .filter(telemetry -> telemetry.getName().equals("RemoteDependency"))
            .collect(Collectors.toList());
    assertThat(remoteDependencies).hasSize(1);

    TelemetryItem remoteDependency = remoteDependencies.get(0);
    MonitorDomain remoteBaseData = remoteDependency.getData().getBaseData();
    RemoteDependencyData remoteDependencyData = (RemoteDependencyData) remoteBaseData;
    assertThat(remoteDependencyData.getType()).isEqualTo("SQL");
    assertThat(remoteDependencyData.getData())
        .isEqualTo("create table test_table (id bigint not null, primary key (id))");

    // HTTP telemetry
    List<TelemetryItem> requests =
        telemetryItems.stream()
            .filter(telemetry -> telemetry.getName().equals("Request"))
            .collect(Collectors.toList());
    TelemetryItem request = requests.get(0);
    MonitorDomain requestBaseData = request.getData().getBaseData();
    RequestData requestData = (RequestData) requestBaseData;
    assertThat(requestData.getUrl()).contains(Controller.URL);
    assertThat(requestData.isSuccess()).isTrue();
    assertThat(requestData.getResponseCode()).isEqualTo("200");
    assertThat(requestData.getName()).isEqualTo("GET /controller-url");
  }
}
