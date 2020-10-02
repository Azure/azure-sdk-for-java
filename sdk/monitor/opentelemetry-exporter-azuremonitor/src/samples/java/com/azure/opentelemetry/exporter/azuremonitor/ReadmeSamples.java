// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.opentelemetry.exporter.azuremonitor;

import com.azure.opentelemetry.exporter.azuremonitor.models.ExportResult;
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
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 *
 * Code samples for the README.md
 */
public class ReadmeSamples {

    /**
     * Sample for creating async client.
     */
    public void createAsyncClient() {
        MonitorExporterAsyncClient monitorExporterAsyncClient = new MonitorExporterClientBuilder()
            .buildAsyncClient();
    }

    /**
     * Sample for creating sync client.
     */
    public void createClient() {
        MonitorExporterClient monitorExporterClient = new MonitorExporterClientBuilder().buildClient();
    }

    /**
     * Create a telemetry item of type {@link RequestData}.
     *
     * @param responseCode The response code.
     * @param requestName The name of the request.
     * @param success The completion status of the request.
     * @param duration The duration for completing the request.
     * @return The telemetry event representing the provided request data.
     */
    private TelemetryItem createRequestData(String responseCode, String requestName, boolean success,
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

    private void exportTelemetryData() {
        MonitorExporterClient monitorExporterClient = new MonitorExporterClientBuilder().buildClient();

        List<TelemetryItem> telemetryItems = new ArrayList<>();
        telemetryItems.add(createRequestData("200", "GET /service/resource-name", true, Duration.ofMillis(100)));
        telemetryItems.add(createRequestData("400", "GET /service/resource-name", false, Duration.ofMillis(50)));
        telemetryItems.add(createRequestData("202", "GET /service/resource-name", true, Duration.ofMillis(125)));

        ExportResult result = monitorExporterClient.export(telemetryItems);
        System.out.println("Items received " + result.getItemsReceived());
        System.out.println("Items accepted " + result.getItemsAccepted());
        System.out.println("Errors " + result.getErrors().size());
        result.getErrors()
            .forEach(
                error -> System.out.println(error.getStatusCode() + " " + error.getMessage()
                    + " " + error.getIndex()));
    }
}
