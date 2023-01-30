// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.core.util.Context;

/**
 * And interface that can be implemented to add custom diagnostic processors
 */
public interface CosmosDiagnosticsHandler {

    /**
     * This method will be invoked when an operation completed (successfully or failed) to allow
     * diagnostic handlers to emit the diagnostics
     * NOTE: Any code in handleDiagnostics should not execute any I/O operations, do thread switches or execute CPU
     * intense work - if needed a diagnostics handler should queue this work asynchronously. The method
     * handleDiagnostics will be invoked on the hot path - so, any inefficient diagnostics handler will increase
     * end-to-end latency perceived by the application
     * @param traceContext the Azure trace context
     * @param diagnosticsContext the Cosmos DB diagnostic context with metadata for the operation
     */
    void handleDiagnostics(Context traceContext, CosmosDiagnosticsContext diagnosticsContext);
}