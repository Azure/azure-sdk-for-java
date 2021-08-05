// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;

import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;
import com.azure.monitor.opentelemetry.exporter.implementation.models.ExportResultException;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for Monitor Exporter async client
 */
public class MonitorExporterAsyncClientTest extends MonitorExporterClientTestBase {

    private MonitorExporterAsyncClient getClient() {
        return getClientBuilder().buildAsyncClient();
    }

    @Test
    public void testSendRequestData() {
        List<TelemetryItem> telemetryItems = getValidTelemetryItems();
        StepVerifier.create(getClient().export(telemetryItems))
            .assertNext(exportResult -> {
                assertTrue(exportResult.getErrors().isEmpty(), "Empty error list expected.");
                assertEquals(3, exportResult.getItemsAccepted());
                assertEquals(3, exportResult.getItemsReceived());
            }).verifyComplete();
    }

    @Test
    public void testSendPartialInvalidRequestData() {

        List<TelemetryItem> telemetryItems = getPartiallyInvalidTelemetryItems();

        StepVerifier.create(getClient().export(telemetryItems))
            .assertNext(exportResult -> {
                assertEquals(3, exportResult.getItemsReceived());
                assertEquals(2, exportResult.getItemsAccepted());
                assertEquals(1, exportResult.getErrors().size());
                assertEquals(1, exportResult.getErrors().get(0).getIndex());
            }).verifyComplete();
    }

    @Test
    public void testSendAllInvalidRequestData() {
        List<TelemetryItem> telemetryItems = getAllInvalidTelemetryItems();
        StepVerifier.create(getClient().export(telemetryItems))
            .expectError(ExportResultException.class)
            .verify();
    }
}
