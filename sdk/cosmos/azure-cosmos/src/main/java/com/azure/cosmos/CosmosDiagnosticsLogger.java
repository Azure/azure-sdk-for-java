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

/**
 * A logger emitting diagnostic information to log4j for operations hitting
 * certain conditions (errors, exceeded latency threshold)
 */
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

    /**
     * Creates an instance of the CosmosDiagnosticLogger class
     * @param config the configuration determining the conditions when to log an operation
     */
    public CosmosDiagnosticsLogger(CosmosDiagnosticsLoggerConfig config) {
        checkNotNull(config, "Argument 'config' must not be null.");
        this.config = config;
    }

    /**
     * Decides whether to log diagnostics for an operation and emits the logs when needed
     * @param traceContext the Azure trace context
     * @param diagnosticsContext the Cosmos DB diagnostic context with metadata for the operation
     */
    @Override
    public final void handleDiagnostics(Context traceContext, CosmosDiagnosticsContext diagnosticsContext) {
        checkNotNull(diagnosticsContext, "Argument 'diagnosticsContext' must not be null.");

        if (shouldLog(diagnosticsContext)) {
            this.log(diagnosticsContext);
        }
    }

    /**
     * Decides whether to log diagnostics for an operation
     * @param diagnosticsContext
     * @return a flag inidcating whether to log the operation or not
     */
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

    /**
     * Logs the operation. This method can be overridden for example to emit logs to a different target than log4j
     * @param ctx
     */
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