// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.clienttelemetry;

import com.azure.core.util.Context;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosDiagnosticsContext;
import com.azure.cosmos.CosmosDiagnosticsHandler;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;

import java.lang.ref.WeakReference;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public final class ClientMetricsDiagnosticsHandler implements CosmosDiagnosticsHandler {
    private static final ImplementationBridgeHelpers.CosmosClientTelemetryConfigHelper.CosmosClientTelemetryConfigAccessor clientTelemetryConfigAccessor =
        ImplementationBridgeHelpers.CosmosClientTelemetryConfigHelper.getCosmosClientTelemetryConfigAccessor();

    private final WeakReference<CosmosAsyncClient> clientRef;

    public ClientMetricsDiagnosticsHandler(CosmosAsyncClient client) {

        checkNotNull(client, "Argument 'client' must not be null.");
        this.clientRef =  new WeakReference<>(client);
    }

    @Override
    public void handleDiagnostics(CosmosDiagnosticsContext diagnosticsContext, Context traceContext) {
        checkNotNull(traceContext, "Argument 'traceContext' must not be null.");

        CosmosAsyncClient clientSnapshot = this.clientRef.get();
        if (clientSnapshot == null) {
            return;
        }

        ClientTelemetryMetrics.recordOperation(
            clientSnapshot,
            diagnosticsContext);
    }
}

