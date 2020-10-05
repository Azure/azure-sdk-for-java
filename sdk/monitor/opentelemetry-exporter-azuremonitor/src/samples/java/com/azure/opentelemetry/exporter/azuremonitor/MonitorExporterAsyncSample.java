// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.opentelemetry.exporter.azuremonitor;

import com.azure.opentelemetry.exporter.azuremonitor.models.MonitorBase;
import com.azure.opentelemetry.exporter.azuremonitor.models.MonitorDomain;
import com.azure.opentelemetry.exporter.azuremonitor.models.RequestData;
import com.azure.opentelemetry.exporter.azuremonitor.models.TelemetryItem;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Sample program for creating an async client and exporting telemetry data to Azure Monitor.
 */
public class MonitorExporterAsyncSample {
    /**
     * The main method to run the sample.
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) throws InterruptedException {
        MonitorExporterAsyncClient monitorExporterAsyncClient = new MonitorExporterClientBuilder()
            .buildAsyncClient();

        List<TelemetryItem> telemetryItems = new ArrayList<>();
        telemetryItems.add(createRequestData("200", "GET /service/resource-name", true, Duration.ofMillis(100)));
        telemetryItems.add(createRequestData("400", "GET /service/resource-name", false, Duration.ofMillis(50)));
        telemetryItems.add(createRequestData("202", "GET /service/resource-name", true, Duration.ofMillis(125)));

        monitorExporterAsyncClient.export(telemetryItems)
            .subscribe(result -> {
                System.out.println("Items received " + result.getItemsReceived());
                System.out.println("Items accepted " + result.getItemsAccepted());
                System.out.println("Errors " + result.getErrors().size());
                result.getErrors()
                    .forEach(
                        error -> System.out.println(error.getStatusCode() + " " + error.getMessage()
                            + " " + error.getIndex()));
            }, ex -> System.out.println("Error occured exporting telemetry data " + ex.getMessage()),
                () -> System.out.println("Successfully completed exporting telemetry data"));

        Thread.sleep(5000);
    }

    private static TelemetryItem createRequestData(String responseCode, String requestName, boolean success,
                                            Duration duration) {
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
            .setTime(OffsetDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
            .setData(monitorBase);
        return telemetryItem;
    }

    private static String getFormattedDuration(Duration duration) {
        return duration.toDays() + "." + duration.toHours() + ":" + duration.toMinutes() + ":" + duration.getSeconds()
            + "." + duration.toMillis();
    }
}
