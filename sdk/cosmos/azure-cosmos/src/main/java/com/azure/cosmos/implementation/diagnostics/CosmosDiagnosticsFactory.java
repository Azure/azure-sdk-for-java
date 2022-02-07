package com.azure.cosmos.implementation.diagnostics;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.implementation.ClientSideRequestStatistics;
import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.QueryMetrics;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

public class CosmosDiagnosticsFactory {

    public static CosmosDiagnostics createCosmosDiagnostics(ICosmosDiagnostics internalCosmosDiagnostics) {
        return BridgeInternal.createCosmosDiagnostics(internalCosmosDiagnostics);
    }

    public static CosmosDiagnostics createSingleRequestDiagnostics(
        DiagnosticsClientContext diagnosticsClientContext,
        GlobalEndpointManager globalEndpointManager) {

        return BridgeInternal.createCosmosDiagnostics(
            new SingleRequestDiagnostics(diagnosticsClientContext, globalEndpointManager));
    }

    public static CosmosDiagnostics createFeedResponseDiagnostics(ConcurrentMap<String, QueryMetrics> queryMetricsMap) {
        return BridgeInternal.createCosmosDiagnostics(new FeedResponseDiagnostics(queryMetricsMap));
    }

    public static CosmosDiagnostics createFeedResponseDiagnostics(
        List<ClientSideRequestStatistics> clientSideRequestStatisticsList,
        ConcurrentMap<String, QueryMetrics> queryMetricsMap) {
        return BridgeInternal.createCosmosDiagnostics(new FeedResponseDiagnostics(clientSideRequestStatisticsList, queryMetricsMap));
    }
}
