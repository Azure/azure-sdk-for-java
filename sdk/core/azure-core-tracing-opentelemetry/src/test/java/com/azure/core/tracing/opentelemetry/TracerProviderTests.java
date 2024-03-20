// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opentelemetry;

import com.azure.core.experimental.util.tracing.LoggingTracerProvider;
import com.azure.core.test.utils.TestConfigurationSource;
import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationBuilder;
import com.azure.core.util.TracingOptions;
import com.azure.core.util.tracing.Tracer;
import com.azure.core.util.tracing.TracerProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TracerProviderTests {
    @Test
    public void createTracerCustomProvider() {
        TracingOptions options = new LoggingTracerProvider.LoggingTracingOptions();

        Tracer tracer = TracerProvider.getDefaultProvider().createTracer("test", null, null, options);

        assertEquals("LoggingTracer", tracer.getClass().getSimpleName());
    }

    @Test
    public void createTracerCustomProviderConfiguration() {
        Configuration config
            = new ConfigurationBuilder()
                .putProperty("tracing.provider.implementation",
                    "com.azure.core.experimental.util.tracing.LoggingTracerProvider")
                .build();

        Tracer tracer = TracerProvider.getDefaultProvider()
            .createTracer("test", null, null, TracingOptions.fromConfiguration(config));

        assertEquals("LoggingTracer", tracer.getClass().getSimpleName());
    }

    @Test
    public void createTracerOTelProviderInConfig() {
        Configuration config
            = new ConfigurationBuilder()
                .putProperty("tracing.provider.implementation",
                    "com.azure.core.tracing.opentelemetry.OpenTelemetryTracerProvider")
                .build();

        Tracer tracer = TracerProvider.getDefaultProvider()
            .createTracer("test", null, null, TracingOptions.fromConfiguration(config));

        assertInstanceOf(OpenTelemetryTracer.class, tracer);
    }

    @Test
    public void createTracerOTelProviderInEnvVar() {
        TestConfigurationSource envSource = new TestConfigurationSource();
        envSource.put(Configuration.PROPERTY_AZURE_TRACING_IMPLEMENTATION,
            "com.azure.core.tracing.opentelemetry.OpenTelemetryTracerProvider");
        Configuration config
            = new ConfigurationBuilder(new TestConfigurationSource(), new TestConfigurationSource(), envSource).build();

        Tracer tracer = TracerProvider.getDefaultProvider()
            .createTracer("test", null, null, TracingOptions.fromConfiguration(config));

        assertInstanceOf(OpenTelemetryTracer.class, tracer);
    }

    @Test
    public void createTracerOTelProviderInOptions() {
        TracingOptions options = new OpenTelemetryTracingOptions();
        Tracer tracer = TracerProvider.getDefaultProvider().createTracer("test", null, null, options);

        assertInstanceOf(OpenTelemetryTracer.class, tracer);
    }

    @Test
    public void createTracerOTelProviderOTelOptions() {
        Tracer tracer
            = TracerProvider.getDefaultProvider().createTracer("test", null, null, new OpenTelemetryTracingOptions());

        assertInstanceOf(OpenTelemetryTracer.class, tracer);
    }

    @Test
    public void createTracerCustomProviderEnvVar() {
        TestConfigurationSource envSource = new TestConfigurationSource();
        envSource.put(Configuration.PROPERTY_AZURE_TRACING_IMPLEMENTATION,
            "com.azure.core.experimental.util.tracing.LoggingTracerProvider");
        Configuration config
            = new ConfigurationBuilder(new TestConfigurationSource(), new TestConfigurationSource(), envSource).build();

        TracingOptions options = TracingOptions.fromConfiguration(config);
        Tracer tracer = TracerProvider.getDefaultProvider().createTracer("test", null, null, options);

        assertEquals("LoggingTracer", tracer.getClass().getSimpleName());
    }

    @Test
    public void createTracerCustomProviderDoesNotExistConfiguration() {
        Configuration config = new ConfigurationBuilder()
            .putProperty("tracing.provider.implementation", "com.azure.core.util.tracing.TestTracerProvider")
            .build();

        assertThrows(RuntimeException.class, () -> TracingOptions.fromConfiguration(config));
    }

    @Test
    public void createTracerCustomProviderNoDefaultCtor() {
        Configuration config = new ConfigurationBuilder()
            .putProperty("tracing.provider.implementation", "com.azure.core.util.tracing.InvalidTracerProvider")
            .build();

        assertThrows(RuntimeException.class, () -> TracingOptions.fromConfiguration(config));
    }

    @Test
    public void createTracerProviderIncompatibleType() {
        TestConfigurationSource envSource = new TestConfigurationSource();
        envSource.put(Configuration.PROPERTY_AZURE_TRACING_IMPLEMENTATION,
            "com.azure.core.tracing.opentelemetry.TracerProviderTests");
        Configuration config
            = new ConfigurationBuilder(new TestConfigurationSource(), new TestConfigurationSource(), envSource).build();

        // class exists
        TracingOptions options = TracingOptions.fromConfiguration(config);

        // but it's not a TracerProvider implementation
        assertThrows(IllegalStateException.class,
            () -> TracerProvider.getDefaultProvider().createTracer("test", null, null, options));
    }
}
