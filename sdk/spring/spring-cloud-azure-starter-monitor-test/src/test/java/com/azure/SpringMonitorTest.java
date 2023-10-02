// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.monitor.applicationinsights.spring.OpenTelemetryVersionCheckRunner;
import com.azure.monitor.applicationinsights.spring.selfdiagnostics.SelfDiagnosticsLevel;
import com.azure.monitor.opentelemetry.exporter.implementation.models.MessageData;
import com.azure.monitor.opentelemetry.exporter.implementation.models.MetricsData;
import com.azure.monitor.opentelemetry.exporter.implementation.models.MonitorDomain;
import com.azure.monitor.opentelemetry.exporter.implementation.models.RemoteDependencyData;
import com.azure.monitor.opentelemetry.exporter.implementation.models.RequestData;
import com.azure.monitor.opentelemetry.exporter.implementation.models.SeverityLevel;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
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

  @Autowired private TestRestTemplate restTemplate;

  // See io.opentelemetry.instrumentation.spring.autoconfigure.OpenTelemetryAutoConfiguration
  @Autowired private ObjectProvider<List<SpanExporter>> otelSpanExportersProvider;

  // See io.opentelemetry.instrumentation.spring.autoconfigure.OpenTelemetryAutoConfiguration
  @Autowired private ObjectProvider<List<LogRecordExporter>> otelLoggerExportersProvider;

  // See io.opentelemetry.instrumentation.spring.autoconfigure.OpenTelemetryAutoConfiguration
  @Autowired private ObjectProvider<List<MetricExporter>> otelMetricExportersProvider;

  @Autowired private Resource otelResource;

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
  void applicationContextShouldOnlyContainTheAzureSpanExporter() {
    List<SpanExporter> spanExporters = otelSpanExportersProvider.getIfAvailable();
    assertThat(spanExporters).hasSize(1);

    SpanExporter spanExporter = spanExporters.get(0);
    String exporterClassName = spanExporter.getClass().getName();
    assertThat(exporterClassName)
        .isEqualTo(
            "com.azure.monitor.opentelemetry.exporter.AzureMonitorTraceExporter"); // AzureMonitorTraceExporter is not public
  }

  @Test
  void applicationContextShouldOnlyContainTheAzureLogRecordExporter() {
    List<LogRecordExporter> logRecordExporters = otelLoggerExportersProvider.getIfAvailable();
    assertThat(logRecordExporters).hasSize(1);

    LogRecordExporter logRecordExporter = logRecordExporters.get(0);
    String exporterClassName = logRecordExporter.getClass().getName();
    assertThat(exporterClassName)
        .isEqualTo(
            "com.azure.monitor.opentelemetry.exporter.AzureMonitorLogRecordExporter"); // AzureMonitorLogRecordExporter is not public
  }

  @Test
  void applicationContextShouldOnlyContainTheAzureMetricExporter() {
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
    // we aren't sure why this is needed, seems to be a Logback issue with GraalVM native
    SpringMonitorTest.class.getResourceAsStream("/logback.xml");

    String response = restTemplate.getForObject(Controller.URL, String.class);
    assertThat(response).isEqualTo("OK!");

    countDownLatch.await(10, SECONDS);

    assertThat(customValidationPolicy.url)
        .isEqualTo(new URL("https://test.in.applicationinsights.azure.com/v2.1/track"));

    List<TelemetryItem> telemetryItems = customValidationPolicy.actualTelemetryItems.stream()
        .filter(item -> {
          MonitorDomain baseData = item.getData().getBaseData();
          return !(baseData instanceof MetricsData) || isSpecialOtelResourceMetric((MetricsData) baseData);
        })
        .collect(Collectors.toList());
    List<String> telemetryTypes = telemetryItems.stream()
        .map(TelemetryItem::getName)
        .collect(Collectors.toList());

    // TODO (alzimmer): In some test runs there ends up being 4 telemetry items, in others 5.
    //  This needs to be investigated on why this is happening, it always ends up being the 'Request' telemetry item.
    assertThat(telemetryItems.size()).as("Telemetry: " + telemetryTypes)
        .is(new Condition<>(size -> size == 4 || size == 5, "size == 4 || size == 5"));

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

    // TODO (alzimmer): In some test runs the 'Request' telemetry item is missing.
    if (requests.size() >= 1) {
        assertThat(requests).hasSize(1);
        TelemetryItem request = requests.get(0);
        MonitorDomain requestBaseData = request.getData().getBaseData();
        RequestData requestData = (RequestData) requestBaseData;
        assertThat(requestData.getUrl()).contains(Controller.URL);
        assertThat(requestData.isSuccess()).isTrue();
        assertThat(requestData.getResponseCode()).isEqualTo("200");
        assertThat(requestData.getName()).isEqualTo("GET /controller-url");
    }
  }

  @Test
  void verifyOpenTelemetryVersion() {
    String currentOTelVersion = otelResource.getAttribute(ResourceAttributes.TELEMETRY_SDK_VERSION);
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
