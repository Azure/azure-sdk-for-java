// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;

import com.azure.monitor.opentelemetry.exporter.implementation.models.ExportResult;
import com.azure.monitor.opentelemetry.exporter.implementation.models.ExportResultException;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for synchronous monitor exporter client.
 */
public class MonitorExporterClientTest extends MonitorExporterClientTestBase {

    private MonitorExporterClient getClient() {
        return getClientBuilder().buildClient();
    }

    @Test
    public void testSendRequestData() {
        List<TelemetryItem> telemetryItems = getValidTelemetryItems();
        ExportResult exportResult = getClient().export(telemetryItems);

        assertTrue(exportResult.getErrors().isEmpty(), "Empty error list expected.");
        assertEquals(3, exportResult.getItemsAccepted());
        assertEquals(3, exportResult.getItemsReceived());
    }

    @Test
    public void testSendPartialInvalidRequestData() {

        List<TelemetryItem> telemetryItems = getPartiallyInvalidTelemetryItems();

        ExportResult exportResult = getClient().export(telemetryItems);
        assertEquals(3, exportResult.getItemsReceived());
        assertEquals(2, exportResult.getItemsAccepted());
        assertEquals(1, exportResult.getErrors().size());
        assertEquals(1, exportResult.getErrors().get(0).getIndex());
    }

    @Test
    public void testSendAllInvalidRequestData() {
        List<TelemetryItem> telemetryItems = getAllInvalidTelemetryItems();
        Assertions.assertThrows(ExportResultException.class, () -> getClient().export(telemetryItems));
    }
}
