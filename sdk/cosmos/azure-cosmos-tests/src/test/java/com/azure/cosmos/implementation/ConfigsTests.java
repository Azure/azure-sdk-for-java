// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.clienttelemetry.MetricCategory;
import com.azure.cosmos.implementation.clienttelemetry.TagName;
import com.azure.cosmos.implementation.directconnectivity.Protocol;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.EnumSet;

import static com.azure.cosmos.implementation.Configs.isThinClientEnabled;
import static org.assertj.core.api.Assertions.assertThat;

public class ConfigsTests {

    @Test(groups = { "unit" })
    public void maxHttpHeaderSize() {
        Configs config = new Configs();
        assertThat(config.getMaxHttpHeaderSize()).isEqualTo(32 * 1024);
    }

    @Test(groups = { "unit" })
    public void maxHttpBodyLength() {
        Configs config = new Configs();
        assertThat(config.getMaxHttpBodyLength()).isEqualTo(6 * 1024 * 1024);
    }

    @Test(groups = { "unit" })
    public void getProtocol() {
        Configs config = new Configs();
        assertThat(config.getProtocol()).isEqualTo(Protocol.valueOf(System.getProperty("azure.cosmos.directModeProtocol", "TCP").toUpperCase()));
    }

    @Test(groups = { "unit" })
    public void getDirectHttpsMaxConnectionLimit() {
        Configs config = new Configs();
        assertThat(config.getDirectHttpsMaxConnectionLimit()).isEqualTo(Runtime.getRuntime().availableProcessors() * 500);
    }

    @Test(groups = { "unit" })
    public void getMetricsConfig() {
        System.clearProperty("COSMOS.METRICS_CONFIG");
        CosmosMicrometerMetricsConfig metricsConfig = Configs.getMetricsConfig();
        assertThat(metricsConfig.getMetricCategories()).isEqualTo(MetricCategory.DEFAULT_CATEGORIES);
        assertThat(metricsConfig.getTagNames()).isEqualTo(TagName.DEFAULT_TAGS);
        assertThat(metricsConfig.getPercentiles()).contains(0.95, 0.99);
        assertThat(metricsConfig.getEnableHistograms()).isTrue();
        assertThat(metricsConfig.getApplyDiagnosticThresholdsForTransportLevelMeters()).isFalse();
        assertThat(metricsConfig.getSampleRate()).isEqualTo(1.0);

        System.setProperty(
            "COSMOS.METRICS_CONFIG",
           "{\"metricCategories\":\"[OperationSummary, RequestSummary]\","
               + "\"tagNames\":\"[Container, Operation]\","
               + "\"sampleRate\":0.5,"
               + "\"percentiles\":[0.90,0.99],"
               + "\"enableHistograms\":false,"
               + "\"applyDiagnosticThresholdsForTransportLevelMeters\":true}");
        try {
            metricsConfig = Configs.getMetricsConfig();
            assertThat(metricsConfig.getMetricCategories()).isEqualTo(EnumSet.of(MetricCategory.OperationSummary, MetricCategory.RequestSummary));
            assertThat(metricsConfig.getTagNames()).isEqualTo(EnumSet.of(TagName.Container, TagName.Operation));
            assertThat(metricsConfig.getPercentiles()).contains(0.90, 0.99);
            assertThat(metricsConfig.getEnableHistograms()).isFalse();
            assertThat(metricsConfig.getApplyDiagnosticThresholdsForTransportLevelMeters()).isTrue();
            assertThat(metricsConfig.getSampleRate()).isEqualTo(0.5);
        } finally {
            System.clearProperty("COSMOS.METRICS_CONFIG");
        }
    }

    @Test(groups = { "emulator" })
    public void httpConnectionWithoutTLSAllowed() {
        Configs config = new Configs();
        assertThat(config.isHttpConnectionWithoutTLSAllowed()).isFalse();

        System.clearProperty("COSMOS.HTTP_CONNECTION_WITHOUT_TLS_ALLOWED");
        System.setProperty("COSMOS.HTTP_CONNECTION_WITHOUT_TLS_ALLOWED", "true");
        try {
            assertThat(config.isHttpConnectionWithoutTLSAllowed()).isTrue();
        } finally {
            System.clearProperty("COSMOS.HTTP_CONNECTION_WITHOUT_TLS_ALLOWED");
        }
    }

    @Test(groups = { "emulator" })
    public void emulatorCertValidationDisabled() {
        Configs config = new Configs();
        assertThat(config.isEmulatorServerCertValidationDisabled()).isFalse();

        System.clearProperty("COSMOS.EMULATOR_SERVER_CERTIFICATE_VALIDATION_DISABLED");
        System.setProperty("COSMOS.EMULATOR_SERVER_CERTIFICATE_VALIDATION_DISABLED", "true");
        try {
            assertThat(config.isEmulatorServerCertValidationDisabled()).isTrue();
        } finally {
            System.clearProperty("COSMOS.EMULATOR_SERVER_CERTIFICATE_VALIDATION_DISABLED");
        }
    }

    @Test(groups = { "emulator" })
    public void emulatorHost() {
        Configs config = new Configs();
        assertThat(config.getEmulatorHost()).isEmpty();

        System.clearProperty("COSMOS.EMULATOR_HOST");
        System.setProperty("COSMOS.EMULATOR_HOST", "randomHost");
        try {
            assertThat(config.getEmulatorHost()).isEqualTo("randomHost");
        } finally {
            System.clearProperty("COSMOS.EMULATOR_HOST");
        }
    }

    @Test(groups = { "emulator" })
    public void http2Enabled() {
        assertThat(Configs.isHttp2Enabled()).isFalse();

        System.clearProperty("COSMOS.HTTP2_ENABLED");
        System.setProperty("COSMOS.HTTP2_ENABLED", "true");
        try {
            assertThat(Configs.isHttp2Enabled()).isTrue();
        } finally {
            System.clearProperty("COSMOS.HTTP2_ENABLED");
        }
    }

    @Test(groups = { "unit" })
    public void http2MaxConnectionPoolSize() {
        assertThat(Configs.getHttp2MaxConnectionPoolSize()).isEqualTo(1000);

        System.clearProperty("COSMOS.HTTP2_MAX_CONNECTION_POOL_SIZE");
        System.setProperty("COSMOS.HTTP2_MAX_CONNECTION_POOL_SIZE", "10");
        try {
            assertThat(Configs.getHttp2MaxConnectionPoolSize()).isEqualTo(10);
        } finally {
            System.clearProperty("COSMOS.HTTP2_MAX_CONNECTION_POOL_SIZE");
        }
    }

    @Test(groups = { "unit" })
    public void http2MinConnectionPoolSize() {
        assertThat(Configs.getHttp2MinConnectionPoolSize()).isEqualTo(
            Math.max(8, Runtime.getRuntime().availableProcessors())
        );

        System.clearProperty("COSMOS.HTTP2_MIN_CONNECTION_POOL_SIZE");
        System.setProperty("COSMOS.HTTP2_MIN_CONNECTION_POOL_SIZE", "10");
        try {
            assertThat(Configs.getHttp2MinConnectionPoolSize()).isEqualTo(10);
        } finally {
            System.clearProperty("COSMOS.HTTP2_MIN_CONNECTION_POOL_SIZE");
        }
    }

    @Test(groups = { "unit" })
    public void http2MaxConcurrentStreams() {
        assertThat(Configs.getHttp2MaxConcurrentStreams()).isEqualTo(30);

        System.clearProperty("COSMOS.HTTP2_MAX_CONCURRENT_STREAMS");
        System.setProperty("COSMOS.HTTP2_MAX_CONCURRENT_STREAMS", "10");
        try {
            assertThat(Configs.getHttp2MaxConcurrentStreams()).isEqualTo(10);
        } finally {
            System.clearProperty("COSMOS.HTTP2_MAX_CONCURRENT_STREAMS");
        }
    }

    @Test(groups = { "emulator" })
    public void thinClientEnabledTest() {
        assertThat(isThinClientEnabled()).isFalse();
        System.clearProperty("COSMOS.THINCLIENT_ENABLED");
        System.setProperty("COSMOS.THINCLIENT_ENABLED", "true");
        try {
            assertThat(isThinClientEnabled()).isTrue();
        } finally {
            System.clearProperty("COSMOS.THINCLIENT_ENABLED");
        }
    }

    @Test(groups = { "emulator" })
    public void thinClientEndpointTest() {
        Configs config = new Configs();
        assertThat(config.getThinclientEndpoint()).isEqualTo(URI.create(""));

        System.clearProperty("COSMOS.THINCLIENT_ENDPOINT");
        System.setProperty("COSMOS.THINCLIENT_ENDPOINT", "testThinClientEndpoint");
        try {
            assertThat(config.getThinclientEndpoint()).isEqualTo(URI.create("testThinClientEndpoint"));
        } finally {
            System.clearProperty("COSMOS.THINCLIENT_ENDPOINT");
        }
    }
}
