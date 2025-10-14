// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.azure.core.util.Context;
import com.azure.cosmos.CosmosDiagnosticsContext;
import com.azure.cosmos.CosmosDiagnosticsHandler;
import com.azure.cosmos.implementation.CosmosDaemonThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class CosmosSamplingDiagnosticsLogger implements CosmosDiagnosticsHandler {
    private final static Logger logger = LoggerFactory.getLogger(CosmosSamplingDiagnosticsLogger.class);
    private final int maxLogCount;
    private final AtomicInteger logCountInSamplingInterval;
    private final ScheduledExecutorService executor;

    public CosmosSamplingDiagnosticsLogger(int maxLogCount, int samplingIntervalInMs) {
        checkArgument(maxLogCount > 0, "Argument 'maxLogCount must be a positive integer.");

        this.logCountInSamplingInterval = new AtomicInteger(0);
        this.maxLogCount = maxLogCount;
        logger.info("MaxLogCount: {}, samplingIntervalInMs: {}", this.maxLogCount, samplingIntervalInMs);
        executor = Executors.newSingleThreadScheduledExecutor(new CosmosDaemonThreadFactory("AsyncBenchmark_logSampling"));
        executor.scheduleAtFixedRate(() -> {
                int snapshot = this.logCountInSamplingInterval.getAndSet(0);
                if (snapshot != 0) {
                    logger.info("Resetting number of logs ({}-0)...", snapshot);
                }
            },
            samplingIntervalInMs,
            samplingIntervalInMs,
            TimeUnit.MILLISECONDS);
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

        boolean shouldLog = shouldLog(diagnosticsContext);
        if (shouldLog) {
            int previousLogCount = this.logCountInSamplingInterval.getAndIncrement();

            if (previousLogCount <= this.maxLogCount) {
                logger.info(
                    "Account: {} -> DB: {}, Col:{}, StatusCode: {}:{} Diagnostics: {}",
                    diagnosticsContext.getAccountName(),
                    diagnosticsContext.getDatabaseName(),
                    diagnosticsContext.getContainerName(),
                    diagnosticsContext.getStatusCode(),
                    diagnosticsContext.getSubStatusCode(),
                    diagnosticsContext.toJson());
            } else if (previousLogCount == this.maxLogCount + 1) {
                logger.info("Already logged {} diagnostics - stopping until sampling interval is reset.", this.maxLogCount);
            }
        }
    }

    /**
     * Decides whether to log diagnostics for an operation
     *
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
     *
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
                    ctx.toJson());
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
                    ctx.toJson());
            }
        } else if (logger.isTraceEnabled()) {
            logger.trace(
                "Account: {} -> DB: {}, Col:{}, StatusCode: {}:{} Diagnostics: {}",
                ctx.getAccountName(),
                ctx.getDatabaseName(),
                ctx.getContainerName(),
                ctx.getStatusCode(),
                ctx.getSubStatusCode(),
                ctx.toJson());
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