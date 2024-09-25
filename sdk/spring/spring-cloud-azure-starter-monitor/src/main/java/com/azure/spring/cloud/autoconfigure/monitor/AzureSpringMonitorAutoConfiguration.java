// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.monitor;

import com.azure.core.http.HttpPipeline;
import com.azure.monitor.opentelemetry.AzureMonitor;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Auto-configuration for Azure Monitor OpenTelemetry Distro / Application Insights in Spring Boot native image Java application.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(OpenTelemetryAutoConfiguration.class)
@ConditionalOnProperty(name = "otel.sdk.disabled", havingValue = "false", matchIfMissing = true)
class AzureSpringMonitorAutoConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(AzureSpringMonitorAutoConfiguration.class);

    private static final AutoConfigurationCustomizerProvider DISABLE_OTEL_CUSTOMER_PROVIDER = autoConfigurationCustomizer -> autoConfigurationCustomizer.addPropertiesCustomizer(configProperties -> Collections.singletonMap("otel.sdk.disabled", "true"));

    private static final AutoConfigurationCustomizerProvider NO_EXPORT_CUSTOMER_PROVIDER = autoConfigurationCustomizer -> autoConfigurationCustomizer.addPropertiesCustomizer(configProperties -> new HashMap<String, String>(3) {{
        put("otel.traces.exporter", "none");
        put("otel.metrics.exporter", "none");
        put("otel.logs.exporter", "none");
    }});

    {
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
    AutoConfigurationCustomizerProvider autoConfigurationCustomizerProvider(@Value("${applicationinsights.connection.string:}") String connectionString, ObjectProvider<AzureMonitorExporterBuilder> azureMonitorExporterBuilder) {

        if (!isNativeRuntimeExecution() && applicationInsightsAgentIsAttached()) {
            LOG.warn("The spring-cloud-azure-starter-monitor Spring starter is disabled because the Application Insights Java agent is enabled."
                + " You can remove this message by using the otel.sdk.disabled=true property.");
            return DISABLE_OTEL_CUSTOMER_PROVIDER;
        }

        AzureMonitorExporterBuilder providedAzureMonitorExporterBuilder = azureMonitorExporterBuilder.getIfAvailable();
        if (providedAzureMonitorExporterBuilder != null) {
            if (System.getenv("APPLICATIONINSIGHTS_CONNECTION_STRING") == null && !connectionString.isEmpty()) {
                LOG.warn("You have created an AzureMonitorExporterBuilder bean and set the applicationinsights.connection.string property."
                    + " The applicationinsights.connection.string property is ignored.");
            }
            // The AzureMonitor class (OpenTelemetry exporter library) is able to use the APPLICATIONINSIGHTS_CONNECTION_STRING environment variable
            return autoConfigurationCustomizer -> AzureMonitor.customize(autoConfigurationCustomizer, providedAzureMonitorExporterBuilder);
        }

        if (connectionString.isEmpty()) {
            LOG.warn("Unable to find the Application Insights connection string (applicationinsights.connection.string property system property or APPLICATIONINSIGHTS_CONNECTION_STRING environment variable). The telemetry data won't be sent to Azure."
                + " If you want to disable the spring-cloud-azure-starter-monitor Spring starter and not display this warning, set the otel.sdk.disabled=true property.");
            // If the user does not provide a connection, we disable the export and leave the instrumentation enabled to spot potential failures from
            // the instrumentation, with the customer automatic tests for example.
            return NO_EXPORT_CUSTOMER_PROVIDER;
        }

        if (!connectionString.contains("InstrumentationKey=")) {
            throw new WrongConnectionStringException(); // To fail fast, before the OpenTelemetry exporter
        }

        return autoConfigurationCustomizer -> AzureMonitor.customize(autoConfigurationCustomizer, connectionString);
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
