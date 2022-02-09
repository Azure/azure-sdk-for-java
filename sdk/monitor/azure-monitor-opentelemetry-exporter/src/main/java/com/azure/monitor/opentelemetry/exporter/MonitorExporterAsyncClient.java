// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;

import com.azure.monitor.opentelemetry.exporter.implementation.ApplicationInsightsClientImpl;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;
import com.azure.monitor.opentelemetry.exporter.implementation.models.ExportResult;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * This class contains asynchronous operations to interact with the Azure Monitor Exporter service.
 */
@ServiceClient(builder = AzureMonitorExporterBuilder.class, isAsync = true)
class MonitorExporterAsyncClient {
    private final ApplicationInsightsClientImpl restServiceClient;

    MonitorExporterAsyncClient(ApplicationInsightsClientImpl restServiceClient) {
        this.restServiceClient = restServiceClient;
    }

    /**
     * The list of telemetry items that will be sent to the Azure Monitor Exporter service. The response contains the
     * status of number of telemetry items successfully accepted and the number of items that failed along with the
     * error code for all the failed items.
     *
     * @param telemetryItems The list of telemetry items to send.
     * @return The response containing the number of successfully accepted items and error details of items that were
     * rejected.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    Mono<ExportResult> export(List<TelemetryItem> telemetryItems) {
        return restServiceClient.trackAsync(telemetryItems);
    }

    /**
     * The list of telemetry items that will be sent to the Azure Monitor Exporter service. The response contains the
     * status of number of telemetry items successfully accepted and the number of items that failed along with the
     * error code for all the failed items.
     *
     * @param telemetryItems The list of telemetry items to send.
     * @return The response containing the number of successfully accepted items and error details of items that were
     * rejected.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    Mono<Response<ExportResult>> exportWithResponse(List<TelemetryItem> telemetryItems) {
        return restServiceClient.trackWithResponseAsync(telemetryItems);
    }

    Mono<Response<ExportResult>> exportWithResponse(List<TelemetryItem> telemetryItems, Context context) {
        return restServiceClient.trackWithResponseAsync(telemetryItems, context);
    }
}
