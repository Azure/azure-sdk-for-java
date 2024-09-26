// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.clienttelemetry;

import com.azure.core.util.Context;
import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosDiagnosticsContext;
import com.azure.cosmos.CosmosDiagnosticsHandler;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.models.CosmosClientTelemetryConfig;
import org.HdrHistogram.ConcurrentDoubleHistogram;

import java.lang.ref.WeakReference;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public final class ClientTelemetryDiagnosticsHandler implements CosmosDiagnosticsHandler {
    private static final ImplementationBridgeHelpers.CosmosDiagnosticsContextHelper.CosmosDiagnosticsContextAccessor ctxAccessor =
        ImplementationBridgeHelpers.CosmosDiagnosticsContextHelper.getCosmosDiagnosticsContextAccessor();
    private static final ImplementationBridgeHelpers.CosmosClientTelemetryConfigHelper.CosmosClientTelemetryConfigAccessor clientTelemetryConfigAccessor =
        ImplementationBridgeHelpers.CosmosClientTelemetryConfigHelper.getCosmosClientTelemetryConfigAccessor();

    private final CosmosClientTelemetryConfig config;
    private WeakReference<ClientTelemetry> telemetryRef;
    private boolean isInitialized;

    public ClientTelemetryDiagnosticsHandler(CosmosClientTelemetryConfig config) {
        checkNotNull(config, "Argument 'config' must not be null.");
        this.config = config;
        this.isInitialized = false;
    }

    private void ensureInitialized() {
        if (this.isInitialized) {
            return;
        }

        ClientTelemetry telemetryFromConfig = clientTelemetryConfigAccessor.getClientTelemetry(config);
        checkNotNull(telemetryFromConfig, "Argument 'telemetryFromConfig' must not be null.");
        this.telemetryRef = new WeakReference<>(telemetryFromConfig);
        this.isInitialized = true;
    }

    @Override
    public void handleDiagnostics(CosmosDiagnosticsContext diagnosticsContext, Context traceContext) {
        checkNotNull(traceContext, "Argument 'traceContext' must not be null.");

        this.ensureInitialized();

        ClientTelemetry clientTelemetrySnapshot = this.telemetryRef.get();
        if (clientTelemetrySnapshot == null) {
            return;
        }

        OperationType operationType = ctxAccessor.getOperationType(diagnosticsContext);
        ResourceType resourceType = ctxAccessor.getResourceType(diagnosticsContext);

        for (CosmosDiagnostics diagnostics: diagnosticsContext.getDiagnostics()) {
            fillClientTelemetry(
                clientTelemetrySnapshot,
                diagnostics,
                diagnosticsContext.getStatusCode(),
                diagnosticsContext.getMaxResponsePayloadSizeInBytes(),
                diagnosticsContext.getContainerName(),
                diagnosticsContext.getDatabaseName(),
                operationType,
                resourceType,
                diagnosticsContext.getEffectiveConsistencyLevel(),
                diagnosticsContext.getTotalRequestCharge()
            );
        }
    }

    private static void fillClientTelemetry(ClientTelemetry clientTelemetrySnapshot,
                                     CosmosDiagnostics cosmosDiagnostics,
                                     int statusCode,
                                     Integer objectSize,
                                     String containerId,
                                     String databaseId,
                                     OperationType operationType,
                                     ResourceType resourceType,
                                     ConsistencyLevel consistencyLevel,
                                     float requestCharge) {
        checkNotNull(clientTelemetrySnapshot, "Argument 'clientTelemetrySnapshot' must not be NULL.");
        ReportPayload reportPayloadLatency = createReportPayload(cosmosDiagnostics,
            statusCode, objectSize, containerId, databaseId
            , operationType, resourceType, consistencyLevel, ClientTelemetry.REQUEST_LATENCY_NAME,
            ClientTelemetry.REQUEST_LATENCY_UNIT);


        ConcurrentDoubleHistogram latencyHistogram = clientTelemetrySnapshot
            .getClientTelemetryInfo()
            .getOperationInfoMap()
            .get(reportPayloadLatency);

        if (cosmosDiagnostics.getDuration() != null) {
            if (latencyHistogram != null) {
                ClientTelemetry.recordValue(latencyHistogram, cosmosDiagnostics.getDuration().toMillis());
            } else {
                if (statusCode >= HttpConstants.StatusCodes.MINIMUM_SUCCESS_STATUSCODE && statusCode <= HttpConstants.StatusCodes.MAXIMUM_SUCCESS_STATUSCODE) {
                    latencyHistogram = new ConcurrentDoubleHistogram(ClientTelemetry.REQUEST_LATENCY_MAX_MILLI_SEC, ClientTelemetry.REQUEST_LATENCY_SUCCESS_PRECISION);
                } else {
                    latencyHistogram = new ConcurrentDoubleHistogram(ClientTelemetry.REQUEST_LATENCY_MAX_MILLI_SEC, ClientTelemetry.REQUEST_LATENCY_FAILURE_PRECISION);
                }

                latencyHistogram.setAutoResize(true);
                ClientTelemetry.recordValue(latencyHistogram, cosmosDiagnostics.getDuration().toMillis());
                clientTelemetrySnapshot.getClientTelemetryInfo().getOperationInfoMap().put(reportPayloadLatency, latencyHistogram);
            }
        }

        ReportPayload reportPayloadRequestCharge = createReportPayload(cosmosDiagnostics,
            statusCode, objectSize, containerId, databaseId
            , operationType, resourceType, consistencyLevel, ClientTelemetry.REQUEST_CHARGE_NAME, ClientTelemetry.REQUEST_CHARGE_UNIT);
        ConcurrentDoubleHistogram requestChargeHistogram = clientTelemetrySnapshot.getClientTelemetryInfo().getOperationInfoMap().get(reportPayloadRequestCharge);
        if (requestChargeHistogram != null) {
            ClientTelemetry.recordValue(requestChargeHistogram, requestCharge);
        } else {
            requestChargeHistogram = new ConcurrentDoubleHistogram(ClientTelemetry.REQUEST_CHARGE_MAX, ClientTelemetry.REQUEST_CHARGE_PRECISION);
            requestChargeHistogram.setAutoResize(true);
            ClientTelemetry.recordValue(requestChargeHistogram, requestCharge);
            clientTelemetrySnapshot.getClientTelemetryInfo().getOperationInfoMap().put(reportPayloadRequestCharge,
                requestChargeHistogram);
        }
    }

    private static ReportPayload createReportPayload(CosmosDiagnostics cosmosDiagnostics,
                                              int statusCode,
                                              Integer objectSize,
                                              String containerId,
                                              String databaseId,
                                              OperationType operationType,
                                              ResourceType resourceType,
                                              ConsistencyLevel consistencyLevel,
                                              String metricsName,
                                              String unitName) {
        ReportPayload reportPayload = new ReportPayload(metricsName, unitName);
        reportPayload.setRegionsContacted(BridgeInternal.getRegionsContacted(cosmosDiagnostics).toString());
        assert consistencyLevel != null : "Effective consistency model must not be null here.";
        reportPayload.setConsistency(consistencyLevel);
        if (objectSize != null) {
            reportPayload.setGreaterThan1Kb(objectSize > ClientTelemetry.ONE_KB_TO_BYTES);
        }

        reportPayload.setDatabaseName(databaseId);
        reportPayload.setContainerName(containerId);
        reportPayload.setOperation(operationType);
        reportPayload.setResource(resourceType);
        reportPayload.setStatusCode(statusCode);
        return reportPayload;
    }
}
