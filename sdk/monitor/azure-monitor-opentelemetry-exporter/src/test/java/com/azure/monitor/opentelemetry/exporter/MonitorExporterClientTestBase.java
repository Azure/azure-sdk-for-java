// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.models.MonitorBase;
import com.azure.monitor.opentelemetry.exporter.implementation.models.MonitorDomain;
import com.azure.monitor.opentelemetry.exporter.implementation.models.RequestData;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Base test class for Monitor Exporter client tests
 */
public class MonitorExporterClientTestBase extends TestBase {

    AzureMonitorExporterBuilder getClientBuilder() {
        HttpClient httpClient;
        if (getTestMode() == TestMode.RECORD || getTestMode() == TestMode.LIVE) {
            httpClient = HttpClient.createDefault();
        } else {
            httpClient = interceptorManager.getPlaybackClient();
        }

        HttpPipeline httpPipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(interceptorManager.getRecordPolicy()).build();

        return new AzureMonitorExporterBuilder().pipeline(httpPipeline);
    }

    AzureMonitorExporterBuilder getClientBuilderWithAuthentication() {
        TokenCredential credential = null;
        HttpClient httpClient;
        if (getTestMode() == TestMode.RECORD || getTestMode() == TestMode.LIVE) {
            httpClient = HttpClient.createDefault();
            credential =
                new ClientSecretCredentialBuilder()
                    .tenantId(System.getenv("AZURE_TENANT_ID"))
                    .clientSecret(System.getenv("AZURE_CLIENT_SECRET"))
                    .clientId(System.getenv("AZURE_CLIENT_ID"))
                    .build();
        } else {
            httpClient = interceptorManager.getPlaybackClient();
        }

        if (credential != null) {
            return new AzureMonitorExporterBuilder()
                .credential(credential)
                .httpClient(httpClient)
                .addPolicy(interceptorManager.getRecordPolicy());
        } else {
            return new AzureMonitorExporterBuilder()
                .httpClient(httpClient)
                .addPolicy(interceptorManager.getRecordPolicy());
        }
    }

    List<TelemetryItem> getAllInvalidTelemetryItems() {
        List<TelemetryItem> telemetryItems = new ArrayList<>();
        telemetryItems.add(createRequestData("200", "GET /service/resource-name", true, Duration.ofMillis(100),
            OffsetDateTime.now().minusDays(10)));
        telemetryItems.add(createRequestData("400", "GET /service/resource-name", false, Duration.ofMillis(50),
            OffsetDateTime.now().minusDays(10)));
        telemetryItems.add(createRequestData("202", "GET /service/resource-name", true, Duration.ofMillis(125),
            OffsetDateTime.now().minusDays(10)));
        return telemetryItems;
    }

    TelemetryItem createRequestData(String responseCode, String requestName, boolean success,
                                                   Duration duration, OffsetDateTime time) {
        MonitorDomain requestData = new RequestData()
            .setId(UUID.randomUUID().toString())
            .setDuration(getFormattedDuration(duration))
            .setResponseCode(responseCode)
            .setSuccess(success)
            .setUrl("http://localhost:8080/")
            .setName(requestName)
            .setVersion(2);

        MonitorBase monitorBase = new MonitorBase()
            .setBaseType("RequestData")
            .setBaseData(requestData);

        TelemetryItem telemetryItem = new TelemetryItem()
            .setVersion(1)
            .setInstrumentationKey("{instrumentation-key}")
            .setName("test-event-name")
            .setSampleRate(100.0f)
            .setTime(time)
            .setData(monitorBase);
        return telemetryItem;
    }

    String getFormattedDuration(Duration duration) {
        return duration.toDays() + "." + duration.toHours() + ":" + duration.toMinutes() + ":" + duration.getSeconds()
            + "." + duration.toMillis();
    }

    List<TelemetryItem> getPartiallyInvalidTelemetryItems() {
        List<TelemetryItem> telemetryItems = new ArrayList<>();
        telemetryItems.add(createRequestData("200", "GET /service/resource-name", true, Duration.ofMillis(100),
            OffsetDateTime.now()));
        telemetryItems.add(createRequestData("400", "GET /service/resource-name", false, Duration.ofMillis(50),
            OffsetDateTime.now().minusDays(2)));
        telemetryItems.add(createRequestData("202", "GET /service/resource-name", true, Duration.ofMillis(125),
            OffsetDateTime.now()));
        return telemetryItems;
    }

    List<TelemetryItem> getValidTelemetryItems() {
        List<TelemetryItem> telemetryItems = new ArrayList<>();
        telemetryItems.add(createRequestData("200", "GET /service/resource-name", true, Duration.ofMillis(100),
            OffsetDateTime.now()));
        telemetryItems.add(createRequestData("400", "GET /service/resource-name", false, Duration.ofMillis(50),
            OffsetDateTime.now()));
        telemetryItems.add(createRequestData("202", "GET /service/resource-name", true, Duration.ofMillis(125),
            OffsetDateTime.now()));
        return telemetryItems;
    }

}
