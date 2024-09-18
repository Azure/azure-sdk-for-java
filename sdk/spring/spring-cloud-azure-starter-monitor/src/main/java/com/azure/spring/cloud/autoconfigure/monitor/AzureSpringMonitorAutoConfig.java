// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.monitor;

import com.azure.core.http.HttpPipeline;
import com.azure.monitor.opentelemetry.exporter.AzureMonitorExporterBuilder;
import io.opentelemetry.instrumentation.spring.autoconfigure.OpenTelemetryAutoConfiguration;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdkBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Auto-configuration for Azure Monitor OpenTelemetry Distro / Application Insights in Spring Boot native image Java application.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(OpenTelemetryAutoConfiguration.class)
@ConditionalOnProperty(name = "otel.sdk.disabled", havingValue = "false", matchIfMissing = true)
public class AzureSpringMonitorAutoConfig {

    private static final Logger LOG = LoggerFactory.getLogger(AzureSpringMonitorAutoConfig.class);

    private static final Function<ConfigProperties, Map<String, String>> OTEL_DISABLE_CONFIG = configProperties -> {
        Map<String, String> properties = new HashMap<>();
        properties.put("otel.sdk.disabled", "true");
        return properties;
    };

    private static final Function<ConfigProperties, Map<String, String>> NO_EXPORT_CONFIG = configProperties -> {
        Map<String, String> properties = new HashMap<>(3);
        properties.put("otel.traces.exporter", "none");
        properties.put("otel.metrics.exporter", "none");
        properties.put("otel.logs.exporter", "none");
        return properties;
    };

    private final String connectionString;
    private final ObjectProvider<HttpPipeline> httpPipeline;


    /**
     * Create an instance of AzureSpringMonitorConfig
     *
     * @param connectionString connection string system property
     * @param httpPipeline an instance of HttpPipeline
     */
    AzureSpringMonitorAutoConfig(@Value("${applicationinsights.connection.string:}") String connectionString, ObjectProvider<HttpPipeline> httpPipeline) {
        this.connectionString = connectionString;
        this.httpPipeline = httpPipeline;
        if (!isNativeRuntimeExecution()) {
            LOG.warn("You are using Application Insights for Spring in a non-native GraalVM runtime environment. We recommend using the Application Insights Java agent.");
        }
    }

    private static boolean isNativeRuntimeExecution() {
        String imageCode = System.getProperty("org.graalvm.nativeimage.imagecode");
        return imageCode != null;
    }

    private static boolean applicationInsightsAgentIsAttached() {
        try {
            Class.forName("com.microsoft.applicationinsights.agent.Agent", false, null);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Bean
    AutoConfigurationCustomizerProvider autoConfigurationCustomizerProvider() {
        return new AutoConfigurationCustomizerProvider() {
            @Override
            public void customize(AutoConfigurationCustomizer autoConfigurationCustomizer) {

                if (!isNativeRuntimeExecution() && applicationInsightsAgentIsAttached()) {
                    LOG.warn("The spring-cloud-azure-starter-monitor Spring starter is disabled because the Application Insights Java agent is enabled."
                        + " You can remove this message by adding the otel.sdk.disabled=true property.");
                    autoConfigurationCustomizer.addPropertiesCustomizer(OTEL_DISABLE_CONFIG);
                    return;
                }

                if (connectionString == null || connectionString.isEmpty()) {
                    LOG.warn("Unable to find the Application Insights connection string. The telemetry data won't be sent to Azure.");
                    // If the user does not provide a connection, we disable the export and leave the instrumentation enabled to spot potential failures from
                    // the instrumentation, in the customer automatic tests for example.
                    autoConfigurationCustomizer.addPropertiesCustomizer(NO_EXPORT_CONFIG);
                    return;
                }

                if (!connectionString.contains("InstrumentationKey=")) {
                    throw new WrongConnectionStringException();
                }

                if (autoConfigurationCustomizer instanceof AutoConfiguredOpenTelemetrySdkBuilder) {
                    AutoConfiguredOpenTelemetrySdkBuilder sdkBuilder = (AutoConfiguredOpenTelemetrySdkBuilder) autoConfigurationCustomizer;

                    AzureMonitorExporterBuilder azureMonitorExporterBuilder = new AzureMonitorExporterBuilder().connectionString(connectionString);
                    HttpPipeline providedHttpPipeline = httpPipeline.getIfAvailable();
                    if (providedHttpPipeline != null) {
                        azureMonitorExporterBuilder = azureMonitorExporterBuilder.httpPipeline(providedHttpPipeline);
                    }
                    azureMonitorExporterBuilder.install(sdkBuilder);
                }
            }

        };
    }

    /**
     * Declare OpenTelemetryVersionCheckRunner bean to check the OpenTelemetry version
     *
     * @return OpenTelemetryVersionCheckRunner
     */
    @Bean
    OpenTelemetryVersionCheckRunner openTelemetryVersionCheckRunner() {
        return new OpenTelemetryVersionCheckRunner();
    }
}
