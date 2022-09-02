// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.test.InterceptorManager;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestContextManager;
import com.azure.core.test.TestMode;
import com.azure.core.test.utils.TestResourceNamer;
import com.azure.core.util.Configuration;
import com.azure.monitor.opentelemetry.exporter.implementation.configuration.ConnectionString;
import com.azure.monitor.opentelemetry.exporter.implementation.models.MonitorBase;
import com.azure.monitor.opentelemetry.exporter.implementation.models.MonitorDomain;
import com.azure.monitor.opentelemetry.exporter.implementation.models.RequestData;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.FormattedDuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Base test class for Monitor Exporter client tests
 */
public class MonitorExporterClientTestBase extends TestBase {

    @Override
    @BeforeEach
    public void setupTest(TestInfo testInfo) {
        this.testContextManager =
            new TestContextManager(testInfo.getTestMethod().get(), TestMode.PLAYBACK);
        interceptorManager =
            new InterceptorManager(
                testContextManager.getTestName(),
                new HashMap<>(),
                testContextManager.doNotRecordTest(),
                "regularTelemetryPlayback");
        testResourceNamer =
            new TestResourceNamer(testContextManager, interceptorManager.getRecordedData());
        beforeTest();
    }

    AzureMonitorExporterBuilder getClientBuilder() {
        HttpClient httpClient;
        if (getTestMode() == TestMode.RECORD || getTestMode() == TestMode.LIVE) {
            httpClient = HttpClient.createDefault();
        } else {
            httpClient = interceptorManager.getPlaybackClient();
        }

        HttpPipeline httpPipeline =
            new HttpPipelineBuilder()
                .httpClient(httpClient)
                .policies(interceptorManager.getRecordPolicy())
                .build();

        return new AzureMonitorExporterBuilder().httpPipeline(httpPipeline);
    }

    List<TelemetryItem> getAllInvalidTelemetryItems() {
        List<TelemetryItem> telemetryItems = new ArrayList<>();
        telemetryItems.add(
            createRequestData(
                "200",
                "GET /service/resource-name",
                true,
                Duration.ofMillis(100),
                Instant.now().minus(10, ChronoUnit.DAYS)));
        telemetryItems.add(
            createRequestData(
                "400",
                "GET /service/resource-name",
                false,
                Duration.ofMillis(50),
                Instant.now().minus(10, ChronoUnit.DAYS)));
        telemetryItems.add(
            createRequestData(
                "202",
                "GET /service/resource-name",
                true,
                Duration.ofMillis(125),
                Instant.now().minus(10, ChronoUnit.DAYS)));
        return telemetryItems;
    }

    TelemetryItem createRequestData(
        String responseCode, String requestName, boolean success, Duration duration, Instant time) {
        MonitorDomain requestData =
            new RequestData()
                .setId(UUID.randomUUID().toString())
                .setDuration(FormattedDuration.fromNanos(duration.toNanos()))
                .setResponseCode(responseCode)
                .setSuccess(success)
                .setUrl("http://localhost:8080/")
                .setName(requestName)
                .setVersion(2);

        MonitorBase monitorBase = new MonitorBase().setBaseType("RequestData").setBaseData(requestData);

        String connectionString =
            Configuration.getGlobalConfiguration().get("APPLICATIONINSIGHTS_CONNECTION_STRING", "");

        return new TelemetryItem()
            .setVersion(1)
            .setConnectionString(ConnectionString.parse(connectionString))
            .setName("test-event-name")
            .setSampleRate(100.0f)
            .setTime(time.atOffset(ZoneOffset.UTC))
            .setData(monitorBase);
    }
}
