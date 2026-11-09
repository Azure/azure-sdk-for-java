// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.core.util.Context;

/**
 * And interface that can be implemented to add custom diagnostic processors
 */
@FunctionalInterface
public interface CosmosDiagnosticsHandler {

    /**
     * This method will be invoked when an operation completed (successfully or failed) to allow diagnostic handlers to
     * emit the diagnostics NOTE: Any code in handleDiagnostics should not execute any I/O operations, do thread
     * switches or execute CPU intense work - if needed a diagnostics handler should queue this work asynchronously. The
     * method handleDiagnostics will be invoked on the hot path - so, any inefficient diagnostics handler will increase
     * end-to-end latency perceived by the application
     *
     * @param diagnosticsContext the Cosmos DB diagnostic context with metadata for the operation
     * @param traceContext the Azure trace context
     */
    void handleDiagnostics(CosmosDiagnosticsContext diagnosticsContext, Context traceContext);

    /**
     * A Cosmos diagnostics handler which will log operations to log4j
     * - Failures (contains diagnostics string) --> Error
     * - Threshold violations (contains diagnostics string) --> Info
     * - Successful operations --> Debug
     * If Trace level is enabled also, the diagnostics string will be logged for successful operations.
     */
    CosmosDiagnosticsHandler DEFAULT_LOGGING_HANDLER = new CosmosDiagnosticsLogger();
}