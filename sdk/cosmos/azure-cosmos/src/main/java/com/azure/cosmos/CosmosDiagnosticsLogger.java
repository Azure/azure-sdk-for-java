// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.core.util.Context;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class CosmosDiagnosticsLogger implements CosmosDiagnosticsHandler {
    private final static Logger logger = LoggerFactory.getLogger(CosmosDiagnosticsLogger.class);
    private final static ImplementationBridgeHelpers.CosmosDiagnosticsContextHelper.CosmosDiagnosticsContextAccessor ctxAccessor =
        ImplementationBridgeHelpers.CosmosDiagnosticsContextHelper.getCosmosDiagnosticsContextAccessor();

    private final CosmosDiagnosticsLoggerConfig config;
    private final Set<OperationType> pointOperationTypes = new HashSet<OperationType>() {{
        add(OperationType.Create);
        add(OperationType.Delete);
        add(OperationType.Patch);
        add(OperationType.Read);
        add(OperationType.Replace);
        add(OperationType.Upsert);
    }};

    public CosmosDiagnosticsLogger(CosmosDiagnosticsLoggerConfig config) {
        checkNotNull(config, "Argument 'config' must not be null.");
        this.config = config;
    }

    @Override
    public void handleDiagnostics(Context traceContext, CosmosDiagnosticsContext diagnosticsContext) {
        checkNotNull(diagnosticsContext, "Argument 'diagnosticsContext' must not be null.");

        if (shouldLog(diagnosticsContext)) {
            this.log(diagnosticsContext);
        }
    }

    protected boolean shouldLog(CosmosDiagnosticsContext diagnosticsContext) {

        if (!diagnosticsContext.hasCompleted()) {
            return false;
        }

        if (shouldLogDueToStatusCode(diagnosticsContext.getStatusCode(), diagnosticsContext.getSubStatusCode())) {
            return true;
        }

        ResourceType resourceType = ctxAccessor.getResourceType(diagnosticsContext);
        OperationType operationType = ctxAccessor.getOperationType(diagnosticsContext);

        if (resourceType == ResourceType.Document) {
            if (pointOperationTypes.contains(operationType)) {
                if (diagnosticsContext.getDuration().compareTo(this.config.getPointOperationLatencyThreshold()) >= 1) {
                    return true;
                }
            } else {
                if (diagnosticsContext.getDuration().compareTo(this.config.getFeedOperationLatencyThreshold()) >= 1) {
                    return true;
                }
            }
        }

        if (diagnosticsContext.getTotalRequestCharge() > this.config.getRequestChargeThreshold()) {
            return true;
        }

        return false;
    }

    private boolean shouldLogDueToStatusCode(int statusCode, int subStatusCode) {
        return statusCode >= 500 || statusCode == 408 || statusCode == 410;
    }

    protected void log(CosmosDiagnosticsContext ctx) {
        if (this.shouldLogDueToStatusCode(ctx.getStatusCode(), ctx.getSubStatusCode())) {
            logger.warn(
                "Account: {} -> DB: {}, Col:{}, StatusCode: {}:{} Diagnostics: {}",
                ctx.getAccountName(),
                ctx.getDatabaseName(),
                ctx.getCollectionName(),
                ctx.getStatusCode(),
                ctx.getSubStatusCode(),
                ctx.toString());
        } else {
            logger.info(
                "Account: {} -> DB: {}, Col:{}, StatusCode: {}:{} Diagnostics: {}",
                ctx.getAccountName(),
                ctx.getDatabaseName(),
                ctx.getCollectionName(),
                ctx.getStatusCode(),
                ctx.getSubStatusCode(),
                ctx.toString());
        }
    }
}