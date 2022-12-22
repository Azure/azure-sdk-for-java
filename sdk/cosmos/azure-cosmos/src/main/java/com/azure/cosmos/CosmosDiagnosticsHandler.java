// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.models.CosmosDiagnosticsContext;

// TODO - I bet there will be discussions on how to integrate this extension
// with open telemetry - worthwhile taking some time to line-upa  story
// that at least allows same outcome with OpenTelemetry in addition
// or maybe drop this extension in favor of OpenTelemetry wiring
// Need more time to investigate
public interface CosmosDiagnosticsHandler {
    void handleDiagnostics(
        CosmosDiagnosticsContext diagnosticsContext,
        CosmosDiagnostics diagnostics,
        CosmosException error,
        int statusCode,
        int subStatusCode);
}
