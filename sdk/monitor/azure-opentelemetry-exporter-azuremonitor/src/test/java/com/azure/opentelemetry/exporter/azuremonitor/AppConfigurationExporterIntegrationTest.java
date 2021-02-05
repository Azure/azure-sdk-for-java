package com.azure.opentelemetry.exporter.azuremonitor;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.TestMode;
import com.azure.core.util.FluxUtil;
import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integration")
public class AppConfigurationExporterIntegrationTest extends AzureMonitorExporterTestBase {

    @Test
    public void testSetConfiguration() throws InterruptedException {
        CountDownLatch appConfigCountDown = new CountDownLatch(1);
        CountDownLatch exporterCountDown = new CountDownLatch(2);

        HttpPipelinePolicy validatorPolicy = (context, next) -> {
            Mono<String> asyncString = FluxUtil.collectBytesInByteBufferStream(context.getHttpRequest().getBody())
                .map(bytes -> new String(bytes, StandardCharsets.UTF_8));
            asyncString.subscribe(value -> {
                if (value.contains("app-config-exporter-testing") && value.contains("\"responseCode\":\"200\"")) {
                    exporterCountDown.countDown();
                }
                if (value.contains("AppConfig.setKey")) {
                    exporterCountDown.countDown();
                }
            });
            return next.process();
        };

        ConfigurationClient client = getConfigurationClient(appConfigCountDown);
        Tracer tracer = configureAzureMonitorExporter(validatorPolicy);

        Span span = tracer.spanBuilder("app-config-exporter-testing").startSpan();
        final Scope scope = span.makeCurrent();
        try {
            // Thread bound (sync) calls will automatically pick up the parent span and you don't need to pass it explicitly.
            client.setConfigurationSetting("hello", "text", "World");
        } finally {
            span.end();
            scope.close();
        }
        assertTrue(appConfigCountDown.await(1, TimeUnit.SECONDS));
        assertTrue(exporterCountDown.await(1, TimeUnit.SECONDS));

        TimeUnit.SECONDS.sleep(2);
    }

    private ConfigurationClient getConfigurationClient(CountDownLatch appConfigCountDown) {
        HttpPipelinePolicy validator = (context, next) -> {
            Optional<Object> data = context.getData(com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY);
            if (data.isPresent() && data.get().equals("Microsoft.AppConfiguration")) {
                appConfigCountDown.countDown();
            }
            return next.process();
        };
        ConfigurationClientBuilder builder = new ConfigurationClientBuilder()
            // .connectionString(System.getenv("AZURE_APPCONFIG_CONNECTION_STRING"))
            .connectionString("Endpoint=https://srnagarappconfig.azconfig.io;Id=pFzC-l1-s0:ZN+D59S5KBG72bSsc2ly;Secret=A8kWyww+wZkiQH3RGTEd76oBBMicIQJpJ+aVqSC9rt0=")
            .addPolicy(validator);

        builder.httpClient(HttpClient.createDefault());
        if (getTestMode() == TestMode.PLAYBACK) {
            builder.httpClient(interceptorManager.getPlaybackClient());
        }
        if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }
        return builder.buildClient();
    }
}
