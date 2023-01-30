// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.core.util.Context;

public interface CosmosDiagnosticsHandler {
    void handleDiagnostics(Context traceContext, CosmosDiagnosticsContext diagnosticsContext);
}