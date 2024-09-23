// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;

import com.azure.core.http.HttpPipeline;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdkBuilder;

import java.util.Collections;
import java.util.Map;

class OpenTelemetrySdkTestFeature {

    private static final String TRACE_CONNECTION_STRING = "InstrumentationKey=00000000-0000-0000-0000-000000000000;"
        + "IngestionEndpoint=https://test.in.applicationinsights.azure.com/;"
        + "LiveEndpoint=https://test.livediagnostics.monitor.azure.com/";

    static OpenTelemetrySdk createOpenTelemetrySdk(HttpPipeline httpPipeline, Map<String, String> configuration,
        String connectionString) {
        AutoConfiguredOpenTelemetrySdkBuilder sdkBuilder = AutoConfiguredOpenTelemetrySdk.builder();

        AzureMonitorExporterBuilder azureMonitorExporterBuilder
            = new AzureMonitorExporterBuilder(configuration).connectionString(connectionString).pipeline(httpPipeline);
        AzureMonitorCustomizer.customize(sdkBuilder, azureMonitorExporterBuilder);

        return sdkBuilder.build().getOpenTelemetrySdk();
    }

    static OpenTelemetrySdk createOpenTelemetrySdk(HttpPipeline httpPipeline) {
        return createOpenTelemetrySdk(httpPipeline, Collections.emptyMap());
    }

    static OpenTelemetrySdk createOpenTelemetrySdk(HttpPipeline httpPipeline, Map<String, String> configuration) {
        return OpenTelemetrySdkTestFeature.createOpenTelemetrySdk(httpPipeline, configuration, TRACE_CONNECTION_STRING);
    }

}
