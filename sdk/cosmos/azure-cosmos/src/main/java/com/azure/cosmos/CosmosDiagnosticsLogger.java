// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.models.CosmosDiagnosticsContext;
import com.azure.cosmos.models.CosmosDiagnosticsLoggerConfig;
import com.fasterxml.jackson.dataformat.xml.util.CaseInsensitiveNameSet;

import java.util.HashSet;

public final class CosmosDiagnosticsLogger  implements CosmosDiagnosticsHandler{

    private final CosmosDiagnosticsLoggerConfig config;
    private final CaseInsensitiveNameSet pointOperationTypes = CaseInsensitiveNameSet.construct(
        new HashSet<String>() {{
            add("Read");
            add("Create");
            add("Upsert");
            add("Replace");
            add("Patch");

            // TODO fix / complete
        }}
    );

    public CosmosDiagnosticsLogger(CosmosDiagnosticsLoggerConfig config) {
        this.config = config;

        // TODO add null handling
    }

    @Override
    public void handleDiagnostics(
        CosmosDiagnosticsContext diagnosticsContext,
        CosmosDiagnostics diagnostics,
        CosmosException error,
        int statusCode,
        int subStatusCode) {

        // TODO implement

        if (shouldLog(diagnosticsContext, diagnostics, statusCode, subStatusCode)) {
            this.log(diagnosticsContext, diagnostics, error, statusCode, subStatusCode);
        }
    }

    public boolean shouldLog(
        CosmosDiagnosticsContext diagnosticsContext,
        CosmosDiagnostics diagnostics,
        int statusCode,
        int subStatusCode) {

        if (shouldLog(statusCode, subStatusCode)) {
            return true;
        }

        if (diagnosticsContext.getResourceType() == "Document") {
            if (pointOperationTypes.contains(diagnosticsContext.getOperationType())) {
                if (diagnostics.getDuration().compareTo(this.config.getPointOperationLatencyThreshold()) >= 1) {
                    return true;
                }
            } else {
                if (diagnostics.getDuration().compareTo(this.config.getFeedOperationLatencyThreshold()) >= 1) {
                    return true;
                }
            }
        }

        // TODO add check for request charge (and maybe paylaod)?

        return false;
    }

    public boolean shouldLog(int statusCode, int subStatusCode) {
        return statusCode >= 500 || statusCode == 408 || statusCode == 410;

        // TODO any checks needed whether any of the requests had error, check number of regions etc.?
    }

    public void log(
        CosmosDiagnosticsContext diagnosticsContext,
        CosmosDiagnostics diagnostics,
        CosmosException error,
        int statusCode,
        int subStatusCode) {

        // TODO implement and log to log4j
    }
}
