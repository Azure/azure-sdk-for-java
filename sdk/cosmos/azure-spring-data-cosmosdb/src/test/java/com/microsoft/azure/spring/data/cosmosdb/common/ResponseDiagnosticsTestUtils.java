// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.data.cosmosdb.common;

import com.azure.data.cosmos.CosmosResponseDiagnostics;
import com.azure.data.cosmos.FeedResponseDiagnostics;
import com.microsoft.azure.spring.data.cosmosdb.core.ResponseDiagnostics;
import com.microsoft.azure.spring.data.cosmosdb.core.ResponseDiagnosticsProcessor;


public class ResponseDiagnosticsTestUtils {

    private final ResponseDiagnosticsProcessor responseDiagnosticsProcessor;
    private ResponseDiagnostics diagnostics;

    public ResponseDiagnosticsTestUtils() {
        responseDiagnosticsProcessor = responseDiagnostics -> {
            diagnostics = responseDiagnostics;
        };
    }

    public CosmosResponseDiagnostics getCosmosResponseDiagnostics() {
        return diagnostics == null ? null : diagnostics.getCosmosResponseDiagnostics();
    }

    public FeedResponseDiagnostics getFeedResponseDiagnostics() {
        return diagnostics == null ? null : diagnostics.getFeedResponseDiagnostics();
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
