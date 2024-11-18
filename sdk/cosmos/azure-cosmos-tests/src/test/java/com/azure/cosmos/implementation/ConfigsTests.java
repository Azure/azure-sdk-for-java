// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.clienttelemetry.MetricCategory;
import com.azure.cosmos.implementation.clienttelemetry.TagName;
import com.azure.cosmos.implementation.directconnectivity.Protocol;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import io.netty.handler.ssl.SslContext;
import org.testng.annotations.Test;

import java.util.EnumSet;

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

        System.setProperty("COSMOS.HTTP_CONNECTION_WITHOUT_TLS_ALLOWED", "true");
        assertThat(config.isHttpConnectionWithoutTLSAllowed()).isTrue();

        System.clearProperty("COSMOS.HTTP_CONNECTION_WITHOUT_TLS_ALLOWED");
    }

    @Test(groups = { "emulator" })
    public void emulatorCertValidationDisabled() {
        Configs config = new Configs();
        assertThat(config.isEmulatorServerCertValidationDisabled()).isFalse();

        System.setProperty("COSMOS.EMULATOR_SERVER_CERTIFICATE_VALIDATION_DISABLED", "true");
        assertThat(config.isEmulatorServerCertValidationDisabled()).isTrue();

        System.clearProperty("COSMOS.EMULATOR_SERVER_CERTIFICATE_VALIDATION_DISABLED");
    }

    @Test(groups = { "emulator" })
    public void emulatorHost() {
        Configs config = new Configs();
        assertThat(config.getEmulatorHost()).isEmpty();

        System.setProperty("COSMOS.EMULATOR_HOST", "randomHost");
        assertThat(config.getEmulatorHost()).isEqualTo("randomHost");

        System.clearProperty("COSMOS.EMULATOR_HOST");
    }

    @Test(groups = { "emulator" })
    public void sslContextTest() {
        Configs config = new Configs();
        SslContext sslContext = config.getSslContext(false);
        assertThat(sslContext).isEqualTo(ReflectionUtils.getSslContext(config));

        sslContext = config.getSslContext(true);
        assertThat(sslContext).isEqualTo(ReflectionUtils.getSslContextWithCertValidationDisabled(config));
    }
}
