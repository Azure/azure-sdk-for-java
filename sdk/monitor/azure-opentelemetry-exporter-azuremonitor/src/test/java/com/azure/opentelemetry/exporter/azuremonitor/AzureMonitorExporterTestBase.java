package com.azure.opentelemetry.exporter.azuremonitor;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
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

        HttpClient httpClient;
        if (getTestMode() == TestMode.RECORD || getTestMode() == TestMode.LIVE) {
            httpClient = HttpClient.createDefault();
        } else {
            httpClient = interceptorManager.getPlaybackClient();
        }

        HttpPipeline httpPipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(interceptorManager.getRecordPolicy(), validator).build();

        AzureMonitorExporter exporter = new AzureMonitorExporterBuilder()
            .connectionString("{connection-string}")
            .pipeline(httpPipeline)
            .buildExporter();
        OpenTelemetrySdk.getGlobalTracerManagement().addSpanProcessor(SimpleSpanProcessor.create(exporter));
        return OpenTelemetrySdk.get().getTracer("Sample");
    }


}
