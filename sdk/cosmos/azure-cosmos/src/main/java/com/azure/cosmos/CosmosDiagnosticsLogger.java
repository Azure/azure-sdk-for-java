// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.core.util.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * A logger emitting diagnostic information to log4j for operations hitting
 * certain conditions (errors, exceeded latency threshold)
 */
class CosmosDiagnosticsLogger implements CosmosDiagnosticsHandler {
    private final static Logger logger = LoggerFactory.getLogger(CosmosDiagnosticsLogger.class);

    /**
     * Creates an instance of the CosmosDiagnosticLogger class
     */
    public CosmosDiagnosticsLogger() {
    }

    /**
     * Decides whether to log diagnostics for an operation and emits the logs when needed
     *
     * @param diagnosticsContext the Cosmos DB diagnostic context with metadata for the operation
     * @param traceContext the Azure trace context
     */
    @Override
    public final void handleDiagnostics(CosmosDiagnosticsContext diagnosticsContext, Context traceContext) {
        checkNotNull(diagnosticsContext, "Argument 'diagnosticsContext' must not be null.");

        if (shouldLog(diagnosticsContext)) {
            this.log(diagnosticsContext);
        }
    }

    /**
     * Decides whether to log diagnostics for an operation
     * @param diagnosticsContext the diagnostics context
     * @return a flag indicating whether to log the operation or not
     */
    protected boolean shouldLog(CosmosDiagnosticsContext diagnosticsContext) {

        if (!diagnosticsContext.isCompleted()) {
            return false;
        }

        return diagnosticsContext.isFailure() ||
            diagnosticsContext.isThresholdViolated() ||
            logger.isDebugEnabled();
    }

    /**
     * Logs the operation. This method can be overridden for example to emit logs to a different target than log4j
     * @param ctx the diagnostics context
     */
    protected void log(CosmosDiagnosticsContext ctx) {
        if (ctx.isFailure()) {
            if (logger.isErrorEnabled()) {
                logger.error(
                    "Account: {} -> DB: {}, Col:{}, StatusCode: {}:{} Diagnostics: {}",
                    ctx.getAccountName(),
                    ctx.getDatabaseName(),
                    ctx.getContainerName(),
                    ctx.getStatusCode(),
                    ctx.getSubStatusCode(),
                    ctx);
            }
        } else if (ctx.isThresholdViolated()) {
            if (logger.isInfoEnabled()) {
                logger.info(
                    "Account: {} -> DB: {}, Col:{}, StatusCode: {}:{} Diagnostics: {}",
                    ctx.getAccountName(),
                    ctx.getDatabaseName(),
                    ctx.getContainerName(),
                    ctx.getStatusCode(),
                    ctx.getSubStatusCode(),
                    ctx);
            }
        } else if (logger.isTraceEnabled()) {
            logger.trace(
                "Account: {} -> DB: {}, Col:{}, StatusCode: {}:{} Diagnostics: {}",
                ctx.getAccountName(),
                ctx.getDatabaseName(),
                ctx.getContainerName(),
                ctx.getStatusCode(),
                ctx.getSubStatusCode(),
                ctx);
        } else if (logger.isDebugEnabled()) {
            logger.debug(
                "Account: {} -> DB: {}, Col:{}, StatusCode: {}:{}, Latency: {}, Request charge: {}",
                ctx.getAccountName(),
                ctx.getDatabaseName(),
                ctx.getContainerName(),
                ctx.getStatusCode(),
                ctx.getSubStatusCode(),
                ctx.getDuration(),
                ctx.getTotalRequestCharge());
        }
    }
}