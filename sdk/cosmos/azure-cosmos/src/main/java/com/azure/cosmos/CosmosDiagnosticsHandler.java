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
     * @param traceContext the Azure trace context
     * @param diagnosticsContext the Cosmos DB diagnostic context with metadata for the operation
     */
    void handleDiagnostics(Context traceContext, CosmosDiagnosticsContext diagnosticsContext);
}