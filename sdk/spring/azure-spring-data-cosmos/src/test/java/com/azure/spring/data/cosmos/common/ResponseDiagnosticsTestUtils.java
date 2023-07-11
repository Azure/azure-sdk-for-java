// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.common;

import com.azure.cosmos.CosmosDiagnostics;
import com.azure.spring.data.cosmos.core.ResponseDiagnostics;
import com.azure.spring.data.cosmos.core.ResponseDiagnosticsProcessor;


public class ResponseDiagnosticsTestUtils {

    private final ResponseDiagnosticsProcessor responseDiagnosticsProcessor;
    private ResponseDiagnostics diagnostics;

    public ResponseDiagnosticsTestUtils() {
        responseDiagnosticsProcessor = responseDiagnostics -> {
            diagnostics = responseDiagnostics;
        };
    }

    public CosmosDiagnostics getCosmosDiagnostics() {
        return diagnostics == null ? null : diagnostics.getCosmosDiagnostics();
    }

    public ResponseDiagnostics.CosmosResponseStatistics getCosmosResponseStatistics() {
        return diagnostics == null ? null : diagnostics.getCosmosResponseStatistics();
    }

    public ResponseDiagnosticsProcessor getResponseDiagnosticsProcessor() {
        return responseDiagnosticsProcessor;
    }

    public ResponseDiagnostics getDiagnostics() {
        return diagnostics;
    }
}
