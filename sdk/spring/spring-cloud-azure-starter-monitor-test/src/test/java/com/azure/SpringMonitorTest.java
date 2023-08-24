// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

import com.azure.core.http.*;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.monitor.opentelemetry.exporter.implementation.models.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.Test;
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
  public void shouldMonitor() throws InterruptedException, MalformedURLException {
    String response = restTemplate.getForObject(Controller.URL, String.class);
    assertThat(response).isEqualTo("OK!");

    countDownLatch.await(10, SECONDS);

    assertThat(customValidationPolicy.url)
        .isEqualTo(new URL("https://test.in.applicationinsights.azure.com/v2.1/track"));

    List<TelemetryItem> telemetryItems = customValidationPolicy.actualTelemetryItems;
    assertThat(telemetryItems.size()).isEqualTo(5);

    // Log telemetry
    TelemetryItem telemetry1 = telemetryItems.get(0);
    assertThat(telemetry1.getName()).isEqualTo("Message");
    MonitorDomain logBaseData = telemetry1.getData().getBaseData();
    MessageData logData = (MessageData) logBaseData;
    assertThat(logData.getMessage())
        .isEqualTo("Initializing Spring DispatcherServlet 'dispatcherServlet'");
    assertThat(logData.getSeverityLevel()).isEqualTo(SeverityLevel.INFORMATION);

    TelemetryItem telemetry2 = telemetryItems.get(1);
    assertThat(telemetry2.getName()).isEqualTo("Message");

    TelemetryItem telemetry3 = telemetryItems.get(2);
    assertThat(telemetry3.getName()).isEqualTo("Message");

    // SQL telemetry
    TelemetryItem telemetry4 = telemetryItems.get(3);
    assertThat(telemetry4.getName()).isEqualTo("RemoteDependency");
    MonitorDomain remoteBaseData = telemetry4.getData().getBaseData();
    RemoteDependencyData remoteDependencyData = (RemoteDependencyData) remoteBaseData;
    assertThat(remoteDependencyData.getType()).isEqualTo("SQL");
    assertThat(remoteDependencyData.getData())
        .isEqualTo("create table test_table (id bigint not null, primary key (id))");

    // HTTP telemetry
    TelemetryItem telemetry5 = telemetryItems.get(4);
    assertThat(telemetry5.getName()).isEqualTo("Request");
    MonitorDomain requestBaseData = telemetry5.getData().getBaseData();
    RequestData requestData = (RequestData) requestBaseData;
    assertThat(requestData.getUrl()).contains(Controller.URL);
    assertThat(requestData.isSuccess()).isTrue();
    assertThat(requestData.getResponseCode()).isEqualTo("200");
    assertThat(requestData.getName()).isEqualTo("GET /controller-url");
  }
}
