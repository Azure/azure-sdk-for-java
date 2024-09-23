// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import static com.azure.core.util.tracing.Tracer.DISABLE_TRACING_KEY;
import static org.junit.jupiter.api.Assertions.assertTrue;

@LiveOnly
public class AppConfigurationExporterIntegrationTest extends MonitorExporterClientTestBase {
    @Test
    public void setConfigurationTest() throws InterruptedException {
        CountDownLatch exporterCountDown = new CountDownLatch(1);

        ValidationPolicy validationPolicy = new ValidationPolicy(exporterCountDown, "set-config-exporter-testing");
        OpenTelemetry openTelemetry
            = OpenTelemetrySdkTestFeature.createOpenTelemetrySdk(getHttpPipeline(validationPolicy));

        Tracer tracer = openTelemetry.getTracer("Sample");

        ConfigurationClient client = getConfigurationClient();

        Span span = tracer.spanBuilder("set-config-exporter-testing").startSpan();
        Scope scope = span.makeCurrent();
        try {
            // Thread bound (sync) calls will automatically pick up the parent span and you don't need to
            // pass it explicitly.
            client.setConfigurationSetting("hello", "text", "World");
        } finally {
            span.end();
            scope.close();
        }
        assertTrue(exporterCountDown.await(60, TimeUnit.SECONDS));
    }

    @Disabled("Multiple tests fail to trigger end span - https://github.com/Azure/azure-sdk-for-java/issues/23567")
    @Test
    public void testDisableTracing() throws InterruptedException {
        CountDownLatch exporterCountDown = new CountDownLatch(1);

        ValidationPolicy validationPolicy = new ValidationPolicy(exporterCountDown, "disable-config-exporter-testing");
        OpenTelemetry openTelemetry
            = OpenTelemetrySdkTestFeature.createOpenTelemetrySdk(getHttpPipeline(validationPolicy));

        Tracer tracer = openTelemetry.getTracer("Sample");

        ConfigurationClient client = getConfigurationClient();

        Span span = tracer.spanBuilder("disable-config-exporter-testing").startSpan();
        Scope scope = span.makeCurrent();
        try {
            ConfigurationSetting configurationSetting
                = new ConfigurationSetting().setKey("hello").setLabel("text").setValue("World");
            client.setConfigurationSettingWithResponse(configurationSetting, false,
                Context.NONE.addData(DISABLE_TRACING_KEY, true));
        } finally {
            span.end();
            scope.close();
        }
        assertTrue(exporterCountDown.await(60, TimeUnit.SECONDS));
    }

    private static ConfigurationClient getConfigurationClient() {
        return new ConfigurationClientBuilder().connectionString(System.getenv("AZURE_APPCONFIG_CONNECTION_STRING"))
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildClient();
    }

    static class ValidationPolicy implements HttpPipelinePolicy {

        private final CountDownLatch countDown;
        private final String expectedSpanName;

        ValidationPolicy(CountDownLatch countDown, String expectedSpanName) {
            this.countDown = countDown;
            this.expectedSpanName = expectedSpanName;
        }

        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            Mono<String> asyncString = FluxUtil.collectBytesInByteBufferStream(context.getHttpRequest().getBody())
                .map(bytes -> ungzip(bytes))
                .map(bytes -> new String(bytes, StandardCharsets.UTF_8));
            asyncString.subscribe(value -> {
                if (value.contains(expectedSpanName)) {
                    countDown.countDown();
                }
            });
            return next.process();
        }

        private static byte[] ungzip(byte[] bytes) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(bytes))) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = gis.read(buffer)) > 0) {
                    bos.write(buffer, 0, len);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return bos.toByteArray();
        }
    }
}
