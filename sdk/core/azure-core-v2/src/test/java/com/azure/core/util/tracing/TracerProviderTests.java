// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.tracing;

import io.clientcore.core.util.configuration.Configuration;
import com.azure.core.v2.util.ConfigurationBuilder;
import com.azure.core.v2.util.TestConfigurationSource;
import com.azure.core.v2.util.TracingOptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TracerProviderTests {
    @Test
    public void noopTracer() {
        Tracer tracer = TracerProvider.getDefaultProvider().createTracer("foo", null, null, null);
        assertNotNull(tracer);
        assertFalse(tracer.isEnabled());
    }

    @Test
    public void invalidParams() {
        assertThrows(NullPointerException.class,
            () -> TracerProvider.getDefaultProvider().createTracer(null, null, null, null));
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
    public void createTracerCustomProviderDoesNotExistEnvVar() {
        TestConfigurationSource envSource = new TestConfigurationSource();
        envSource.put(Configuration.PROPERTY_AZURE_TRACING_IMPLEMENTATION,
            "com.azure.core.util.tracing.TracerProviderTests");
        Configuration config
            = new ConfigurationBuilder(new TestConfigurationSource(), new TestConfigurationSource(), envSource).build();

        // class exists, so no exception here
        TracingOptions options = TracingOptions.fromConfiguration(config);

        // but it should fail attempting create a provider, since TracerProviderTests don't implement TracerProvider
        assertThrows(RuntimeException.class,
            () -> TracerProvider.getDefaultProvider().createTracer("test", null, null, options));
    }

    @Test
    public void createTracerCustomProviderNotInMetaInf() {
        TestConfigurationSource envSource = new TestConfigurationSource();
        envSource.put(Configuration.PROPERTY_AZURE_TRACING_IMPLEMENTATION,
            "com.azure.core.util.tracing.TracerProviderTests$TestTracerProvider");
        Configuration config
            = new ConfigurationBuilder(new TestConfigurationSource(), new TestConfigurationSource(), envSource).build();

        // class exists, so no exception here
        TracingOptions options = TracingOptions.fromConfiguration(config);

        // but it should fail attempting to find it through SPI
        assertThrows(RuntimeException.class,
            () -> TracerProvider.getDefaultProvider().createTracer("test", null, null, options));
    }

    @Test
    public void createTracerDisabledConfiguration() {
        Configuration config = new ConfigurationBuilder().putProperty("tracing.disabled", "true").build();

        TracingOptions options = TracingOptions.fromConfiguration(config);
        Tracer tracer = TracerProvider.getDefaultProvider().createTracer("test", null, null, options);
        assertFalse(tracer.isEnabled());
        assertInstanceOf(NoopTracer.class, tracer);
    }

    public static class TestTracerProvider implements TracerProvider {
        @Override
        public Tracer createTracer(String libraryName, String libraryVersion, String azNamespace,
            TracingOptions options) {
            return null;
        }
    }
}
