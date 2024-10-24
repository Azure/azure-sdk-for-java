// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.configuration;

class DefaultEndpoints {

    static final String INGESTION_ENDPOINT = System.getProperty(
        // this property is needed for testing 2.x SDK which only sets instrumentationKey (without
        // endpoint) and also needed for testing the (deprecated) instrumentationKeyOverrides
        "applicationinsights.testing.global-ingestion-endpoint", "https://dc.services.visualstudio.com/");

    static final String LIVE_ENDPOINT = "https://rt.services.visualstudio.com/";

    static final String PROFILER_ENDPOINT = "https://agent.azureserviceprofiler.net/";

    private DefaultEndpoints() {
    }
}
