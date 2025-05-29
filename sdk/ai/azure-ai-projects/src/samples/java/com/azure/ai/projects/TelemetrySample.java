// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class TelemetrySample {

    private static TelemetryClient telemetryClient
        = new AIProjectClientBuilder().endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildTelemetryClient();

    public static void main(String[] args) {
        getConnectionString();
    }

    public static void getConnectionString() {
        // BEGIN:com.azure.ai.projects.TelemetrySample.getConnectionString

        String connectionString = telemetryClient.getConnectionString();
        System.out.println("Connection string: " + connectionString);

        // END:com.azure.ai.projects.TelemetrySample.getConnectionString
    }
}
