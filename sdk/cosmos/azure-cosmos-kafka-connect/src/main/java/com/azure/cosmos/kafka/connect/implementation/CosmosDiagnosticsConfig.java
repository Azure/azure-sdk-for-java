// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation;

public class CosmosDiagnosticsConfig {
    private final boolean useClientTelemetry;
    private final String clientTelemetryEndpoint;

    public CosmosDiagnosticsConfig(boolean useClientTelemetry, String clientTelemetryEndpoint) {
        this.useClientTelemetry = useClientTelemetry;
        this.clientTelemetryEndpoint = clientTelemetryEndpoint;
    }

    public boolean isUseClientTelemetry() {
        return useClientTelemetry;
    }

    public String getClientTelemetryEndpoint() {
        return clientTelemetryEndpoint;
    }
}
