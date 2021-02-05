package com.azure.opentelemetry.exporter.azuremonitor;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

public class AzureMonitorExporterTestBase extends TestBase {

    Tracer configureAzureMonitorExporter(HttpPipelinePolicy validator) {
        AzureMonitorExporterBuilder builder = new AzureMonitorExporterBuilder()
            // .connectionString(System.getenv("AZURE_MONITOR_CONNECTION_STRING"))
            .connectionString("InstrumentationKey=b4f83947-968d-4e2f-82ad-804be07697ae")
            .addPolicy(validator);

        builder.httpClient(HttpClient.createDefault());
        if (getTestMode() == TestMode.PLAYBACK) {
            builder.httpClient(interceptorManager.getPlaybackClient());
        }
        if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        AzureMonitorExporter exporter = builder.buildExporter();
        OpenTelemetrySdk.getGlobalTracerManagement().addSpanProcessor(SimpleSpanProcessor.create(exporter));
        return OpenTelemetrySdk.get().getTracer("Sample");
    }


}
