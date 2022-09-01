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
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

/** Base test class for Monitor Exporter client tests */
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
