// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.core.util.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.EventConstants;
import org.slf4j.event.Level;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * A logger emitting diagnostic information to log4j for operations hitting
 * certain conditions (errors, exceeded latency threshold)
 */
public class CosmosDiagnosticsLogger implements CosmosDiagnosticsHandler {
    private final static Logger logger = LoggerFactory.getLogger(CosmosDiagnosticsLogger.class);

    private final CosmosDiagnosticsLoggerConfig config;

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
     * @param diagnosticsContext the diagnostics context
     * @return a flag indicating whether to log the operation or not
     */
    protected boolean shouldLog(CosmosDiagnosticsContext diagnosticsContext) {

        if (!diagnosticsContext.isCompleted()) {
            return false;
        }

        return diagnosticsContext.isFailure() ||
            diagnosticsContext.isThresholdViolated() ||
            isEnabledForLevel(this.config.getLevelForSuccessfulOperations());
    }

    /**
     * Logs the operation. This method can be overridden for example to emit logs to a different target than log4j
     * @param ctx the diagnostics context
     */
    protected void log(CosmosDiagnosticsContext ctx) {
        if (ctx.isFailure()) {
            Level levelForFailedOps = this.config.getLevelForFailures();
            if (isEnabledForLevel(levelForFailedOps)) {
                logAtLevel(
                    levelForFailedOps,
                    "Account: {} -> DB: {}, Col:{}, StatusCode: {}:{} Diagnostics: {}",
                    ctx.getAccountName(),
                    ctx.getDatabaseName(),
                    ctx.getContainerName(),
                    ctx.getStatusCode(),
                    ctx.getSubStatusCode(),
                    ctx);
            }
        } else if (ctx.isThresholdViolated()) {
            Level levelForThresholdViolations = this.config.getLevelForThresholdViolations();
            logAtLevel(
                levelForThresholdViolations,
                "Account: {} -> DB: {}, Col:{}, StatusCode: {}:{} Diagnostics: {}",
                ctx.getAccountName(),
                ctx.getDatabaseName(),
                ctx.getContainerName(),
                ctx.getStatusCode(),
                ctx.getSubStatusCode(),
                ctx);
        } else if (isEnabledForLevel(this.config.getLevelForSuccessfulOperationsWithRequestDiagnostics())) {
            logAtLevel(
                this.config.getLevelForSuccessfulOperationsWithRequestDiagnostics(),
                "Account: {} -> DB: {}, Col:{}, StatusCode: {}:{} Diagnostics: {}",
                ctx.getAccountName(),
                ctx.getDatabaseName(),
                ctx.getContainerName(),
                ctx.getStatusCode(),
                ctx.getSubStatusCode(),
                ctx);
        } else if (isEnabledForLevel(this.config.getLevelForSuccessfulOperations())) {
            logAtLevel(
                this.config.getLevelForSuccessfulOperations(),
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

    private static boolean isEnabledForLevel(Level level) {
        int levelInt = level.toInt();
        switch (levelInt) {
            case (EventConstants.TRACE_INT):
                return logger.isTraceEnabled();
            case (EventConstants.DEBUG_INT):
                return logger.isDebugEnabled();
            case (EventConstants.INFO_INT):
                return logger.isInfoEnabled();
            case (EventConstants.WARN_INT):
                return logger.isWarnEnabled();
            case (EventConstants.ERROR_INT):
                return logger.isErrorEnabled();
            default:
                throw new IllegalArgumentException("Level [" + level + "] not recognized.");
        }
    }

    private static void logAtLevel(Level level, String template, Object... args) {
        int levelInt = level.toInt();
        switch (levelInt) {
            case (EventConstants.TRACE_INT):
                logger.trace(template, args);
                return;
            case (EventConstants.DEBUG_INT):
                logger.debug(template, args);
                return;
            case (EventConstants.INFO_INT):
                logger.info(template, args);
                return;
            case (EventConstants.WARN_INT):
                logger.warn(template, args);
                return;
            case (EventConstants.ERROR_INT):
                logger.error(template, args);
                return;
            default:
                throw new IllegalArgumentException("Level [" + level + "] not recognized.");
        }
    }
}