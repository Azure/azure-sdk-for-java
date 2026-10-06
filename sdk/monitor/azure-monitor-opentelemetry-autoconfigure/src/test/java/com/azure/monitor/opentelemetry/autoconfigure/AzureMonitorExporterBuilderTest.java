// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.util.Configuration;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.MockHttpResponse;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.RemoteDependencyData;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.utils.TestUtils;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@Isolated("Changes global SDKStats configuration and the temporary directory")
public class AzureMonitorExporterBuilderTest {

    private static final String DISABLED_ALL = "APPLICATIONINSIGHTS_SDKStats_DISABLED_ALL";
    private static final String CONNECTION_STRING = "InstrumentationKey=00000000-0000-0000-0000-000000000000;"
        + "IngestionEndpoint=https://westus-0.in.applicationinsights.azure.com/";

    @TempDir
    File tempDir;

    private String previousDisabledAll;
    private String previousTempDir;

    @BeforeEach
    public void setUp() {
        previousDisabledAll = Configuration.getGlobalConfiguration().get(DISABLED_ALL);
        Configuration.getGlobalConfiguration().put(DISABLED_ALL, "false");
        previousTempDir = System.getProperty("java.io.tmpdir");
        System.setProperty("java.io.tmpdir", tempDir.getAbsolutePath());
    }

    @AfterEach
    public void tearDown() {
        if (previousDisabledAll == null) {
            Configuration.getGlobalConfiguration().remove(DISABLED_ALL);
        } else {
            Configuration.getGlobalConfiguration().put(DISABLED_ALL, previousDisabledAll);
        }
        System.setProperty("java.io.tmpdir", previousTempDir);
    }

    @Test
    public void customerSdkStatsEnabledByDefault() {
        ConfigProperties config = DefaultConfigProperties.createFromMap(Collections.emptyMap());
        assertThat(AzureMonitorExporterBuilder.isCustomerSdkStatsEnabled(config)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = { "true", "TrUe" })
    public void customerSdkStatsDisabledByPublicProperty(String value) {
        ConfigProperties config = DefaultConfigProperties
            .createFromMap(Collections.singletonMap("applicationinsights.sdkstats.disabled", value));
        assertThat(AzureMonitorExporterBuilder.isCustomerSdkStatsEnabled(config)).isFalse();
    }

    @Test
    public void customerSdkStatsDisabledByAllProperty() {
        ConfigProperties config = DefaultConfigProperties
            .createFromMap(Collections.singletonMap("APPLICATIONINSIGHTS_SDKStats_DISABLED_ALL", "true"));
        assertThat(AzureMonitorExporterBuilder.isCustomerSdkStatsEnabled(config)).isFalse();
    }

    @Test
    public void customerSdkStatsDisabledAllTakesPrecedence() {
        Map<String, String> props = new HashMap<>();
        props.put("applicationinsights.sdkstats.disabled", "false");
        props.put("APPLICATIONINSIGHTS_SDKStats_DISABLED_ALL", "true");
        ConfigProperties config = DefaultConfigProperties.createFromMap(props);
        assertThat(AzureMonitorExporterBuilder.isCustomerSdkStatsEnabled(config)).isFalse();
    }

    @Test
    public void customerSdkStatsDisabledAllFalseLeavesEnabled() {
        Map<String, String> props = new HashMap<>();
        props.put("APPLICATIONINSIGHTS_SDKStats_DISABLED_ALL", "false");
        ConfigProperties config = DefaultConfigProperties.createFromMap(props);
        assertThat(AzureMonitorExporterBuilder.isCustomerSdkStatsEnabled(config)).isTrue();
    }

    @Test
    public void customerSdkStatsDisabledFalseLeavesEnabled() {
        ConfigProperties config = DefaultConfigProperties
            .createFromMap(Collections.singletonMap("applicationinsights.sdkstats.disabled", "false"));
        assertThat(AzureMonitorExporterBuilder.isCustomerSdkStatsEnabled(config)).isTrue();
    }

    @ParameterizedTest
    @CsvSource({
        ", true, false",
        ", false, true",
        "false, true, true",
        "true, false, false",
        "FaLsE, true, true",
        "TrUe, false, false" })
    public void customerSdkStatsConfigurationPrecedence(String property, String global, boolean expectedEnabled) {
        Configuration.getGlobalConfiguration().put(DISABLED_ALL, global);
        ConfigProperties config = DefaultConfigProperties.createFromMap(
            property == null ? Collections.emptyMap() : Collections.singletonMap(DISABLED_ALL, property));
        assertThat(AzureMonitorExporterBuilder.isCustomerSdkStatsEnabled(config)).isEqualTo(expectedEnabled);
    }

    @Test
    public void disabledAllFalseDoesNotOverridePublicDisable() {
        Map<String, String> props = new HashMap<>();
        props.put(DISABLED_ALL, "false");
        props.put("applicationinsights.sdkstats.disabled", "true");
        assertThat(AzureMonitorExporterBuilder.isCustomerSdkStatsEnabled(DefaultConfigProperties.createFromMap(props)))
            .isFalse();
    }

    @ParameterizedTest
    @CsvSource({ "true, false, false", "TrUe, false, true", ", True, false" })
    public void disabledAllSkipsStatsbeatStartup(String property, String global, String publicDisable) {
        Configuration.getGlobalConfiguration().put(DISABLED_ALL, global);
        Map<String, String> props = new HashMap<>();
        if (property != null) {
            props.put(DISABLED_ALL, property);
        }
        props.put("applicationinsights.sdkstats.disabled", publicDisable);
        props.put("applicationinsights.live.metrics.enabled", "false");
        // Any regular Statsbeat scheduling would reject these periods.
        props.put("STATSBEAT_SHORT_INTERVAL_SECONDS_PROPERTY_NAME", "-1");
        props.put("STATSBEAT_LONG_INTERVAL_SECONDS_PROPERTY_NAME", "-1");
        ConfigProperties config
            = mock(ConfigProperties.class, delegatesTo(DefaultConfigProperties.createFromMap(props)));
        CustomValidationPolicy validationPolicy = new CustomValidationPolicy(new CountDownLatch(1));
        HttpClient httpClient = request -> Mono.just(new MockHttpResponse(request, 200, new HttpHeaders()));
        AzureMonitorAutoConfigureOptions options
            = new AzureMonitorAutoConfigureOptions().connectionString(CONNECTION_STRING)
                .pipeline(new HttpPipelineBuilder().httpClient(httpClient).policies(validationPolicy).build());

        Set<Thread> existingThreads = Thread.getAllStackTraces().keySet();
        AzureMonitorExporterBuilder builder = new AzureMonitorExporterBuilder();
        SpanExporter spanExporter = null;
        SdkTracerProvider tracerProvider = null;
        try {
            builder.initializeIfNot(options, config, Resource.empty());

            File telemetryDir = new File(tempDir, "applicationinsights");
            assertThat(new File(telemetryDir, "telemetry")).isDirectory();
            assertThat(new File(telemetryDir, "statsbeat")).doesNotExist();
            assertThat(Thread.getAllStackTraces().keySet()).filteredOn(thread -> !existingThreads.contains(thread))
                .extracting(Thread::getName)
                .noneMatch(name -> name.startsWith("BaseStatsbeat-")
                    || name.startsWith("AzureMetadataService-")
                    || name.startsWith("CustomerSdkStats-"));
            verify(config).getString(DISABLED_ALL);

            spanExporter = builder.buildSpanExporter();
            tracerProvider
                = SdkTracerProvider.builder().addSpanProcessor(SimpleSpanProcessor.create(spanExporter)).build();
            tracerProvider.get("test").spanBuilder("application-span").startSpan().end();
            assertThat(tracerProvider.forceFlush().join(10, SECONDS).isSuccess()).isTrue();
            assertThat(validationPolicy.getActualTelemetryItems()).hasSize(1);
            RemoteDependencyData dependency = TestUtils
                .toRemoteDependencyData(validationPolicy.getActualTelemetryItems().get(0).getData().getBaseData());
            assertThat(dependency.getName()).isEqualTo("application-span");
        } finally {
            if (tracerProvider != null) {
                tracerProvider.close();
            } else if (spanExporter != null) {
                spanExporter.shutdown().join(10, SECONDS);
            }
        }
    }
}
