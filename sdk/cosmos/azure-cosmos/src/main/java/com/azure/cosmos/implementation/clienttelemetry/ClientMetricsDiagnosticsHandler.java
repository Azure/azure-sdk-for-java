// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.clienttelemetry;

import com.azure.core.util.Context;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosDiagnosticsContext;
import com.azure.cosmos.CosmosDiagnosticsHandler;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.models.CosmosClientTelemetryConfig;
import com.azure.cosmos.models.CosmosMicrometerMeterOptions;
import com.azure.cosmos.models.CosmosMicrometerMetricsOptions;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;

import java.util.EnumSet;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public final class ClientMetricsDiagnosticsHandler implements CosmosDiagnosticsHandler {
    private static final ImplementationBridgeHelpers.CosmosClientTelemetryConfigHelper.CosmosClientTelemetryConfigAccessor clientTelemetryConfigAccessor =
        ImplementationBridgeHelpers.CosmosClientTelemetryConfigHelper.getCosmosClientTelemetryConfigAccessor();

    private final CosmosClientTelemetryConfig config;
    private final CosmosAsyncClient client;
    private boolean isInitialized;
    private Tag clientCorrelationTag;
    private String accountTagValue;

    public ClientMetricsDiagnosticsHandler(
        CosmosAsyncClient client,
        CosmosClientTelemetryConfig config) {

        checkNotNull(config, "Argument 'config' must not be null.");
        checkNotNull(client, "Argument 'client' must not be null.");
        this.config = config;
        this.client = client;
        this.isInitialized = false;
    }

    private void ensureInitialized() {
        if (this.isInitialized) {
            return;
        }

        this.clientCorrelationTag = clientTelemetryConfigAccessor.getClientCorrelationTag(this.config);
        this.accountTagValue = clientTelemetryConfigAccessor.getAccountName(this.config);
        this.isInitialized = true;
    }

    @Override
    public void handleDiagnostics(Context traceContext, CosmosDiagnosticsContext diagnosticsContext) {
        checkNotNull(traceContext, "Argument 'traceContext' must not be null.");

        this.ensureInitialized();

        for (CosmosDiagnostics diagnostics: diagnosticsContext.getDiagnostics()) {
            ClientTelemetryMetrics.recordOperation(
                this.client,
                diagnostics,
                diagnosticsContext.getStatusCode(),
                diagnosticsContext.getMaxItemCount(),
                diagnosticsContext.getActualItemCount(),
                diagnosticsContext.getCollectionName(),
                diagnosticsContext.getDatabaseName(),
                diagnosticsContext.getOperationType(),
                diagnosticsContext.getResourceType(),
                diagnosticsContext.getConsistencyLevel(),
                (String)null,
                diagnosticsContext.getTotalRequestCharge(),
                diagnostics.getDuration());
        }
    }
}

