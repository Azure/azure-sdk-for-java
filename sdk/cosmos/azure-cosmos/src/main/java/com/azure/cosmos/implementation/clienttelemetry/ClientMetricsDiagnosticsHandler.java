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
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;

import java.util.EnumSet;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public final class ClientMetricsDiagnosticsHandler implements CosmosDiagnosticsHandler {
    private static final ImplementationBridgeHelpers.CosmosClientTelemetryConfigHelper.CosmosClientTelemetryConfigAccessor clientTelemetryConfigAccessor =
        ImplementationBridgeHelpers.CosmosClientTelemetryConfigHelper.getCosmosClientTelemetryConfigAccessor();

    private final MeterRegistry registry;
    private final CosmosClientTelemetryConfig config;
    private boolean isInitialized;
    private EnumSet<TagName> metricTagNames;
    private Tag clientCorrelationTag;
    private String accountTageValue;

    public ClientMetricsDiagnosticsHandler(
        MeterRegistry registry,
        CosmosClientTelemetryConfig config) {

        checkNotNull(registry, "Argument 'registry' must not be null.");
        checkNotNull(config, "Argument 'config' must not be null.");
        this.registry = registry;
        this.config = config;
        this.isInitialized = false;
    }

    private void ensureInitialized() {
        if (this.isInitialized) {
            return;
        }

        this.metricTagNames = clientTelemetryConfigAccessor.getMetricTagNames(config);
        this.clientCorrelationTag = clientTelemetryConfigAccessor.getClientCorrelationTag(this.config);
        this.accountTageValue = clientTelemetryConfigAccessor.getAccountName(this.config);
        this.isInitialized = true;
    }

    @Override
    public void handleDiagnostics(Context traceContext, CosmosDiagnosticsContext diagnosticsContext) {
        checkNotNull(traceContext, "Argument 'traceContext' must not be null.");

        this.ensureInitialized();

        for (CosmosDiagnostics diagnostics: diagnosticsContext.getDiagnostics()) {
            ClientTelemetryMetrics.recordOperation(
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
                diagnostics.getDuration(),
                true,
                this.metricTagNames,
                this.clientCorrelationTag,
                this.accountTageValue);
        }
    }
}

