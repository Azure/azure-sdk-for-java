// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.opentelemetry.exporter.azuremonitor;

import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

public class AzureMonitorExporterTestBase extends TestBase {

    @BeforeEach
    public void setupTest(TestInfo testInfo) {
        Assumptions.assumeFalse(getTestMode() == TestMode.PLAYBACK, "Skipping playback tests");
    }

    @Override
    @AfterEach
    public void teardownTest(TestInfo testInfo) {
    }

    Tracer configureAzureMonitorExporter(HttpPipelinePolicy validator) {
        AzureMonitorExporter exporter = new AzureMonitorExporterBuilder()
            .connectionString(System.getenv("AZURE_MONITOR_CONNECTION_STRING"))
            .addPolicy(validator)
            .buildExporter();
        OpenTelemetrySdk.getGlobalTracerManagement().addSpanProcessor(SimpleSpanProcessor.create(exporter));
        return OpenTelemetrySdk.get().getTracer("Sample");
    }


}
