// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.opentelemetry.exporter.azuremonitor;

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
    public void setConfigurationTest() throws InterruptedException {
        CountDownLatch appConfigCountDown = new CountDownLatch(1);
        CountDownLatch exporterCountDown = new CountDownLatch(2);

        ConfigurationClient client = getConfigurationClient(appConfigCountDown);
        Tracer tracer = configureAzureMonitorExporter((context, next) -> {
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
        });

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
    }

    private ConfigurationClient getConfigurationClient(CountDownLatch appConfigCountDown) {
        ConfigurationClient client = new ConfigurationClientBuilder()
            .connectionString(System.getenv("APP_CONFIG_CONNECTION_STRING"))
            .addPolicy((context, next) -> {
                Optional<Object> data = context.getData(com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY);
                if (data.isPresent() && data.get().equals("Microsoft.AppConfiguration")) {
                    appConfigCountDown.countDown();
                }
                return next.process();
            })
            .buildClient();
        return client;
    }
}
