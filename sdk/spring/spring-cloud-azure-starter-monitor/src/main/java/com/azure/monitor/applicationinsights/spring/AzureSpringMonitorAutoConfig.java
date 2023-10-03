// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.applicationinsights.spring;

import com.azure.core.http.HttpPipeline;
import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.opentelemetry.exporter.AzureMonitorExporterBuilder;
import io.opentelemetry.instrumentation.spring.autoconfigure.OpenTelemetryAutoConfiguration;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

/**
 * Auto config for Azure Spring Monitor
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(OpenTelemetryAutoConfiguration.class)
@ConditionalOnProperty(name = "otel.sdk.disabled", havingValue = "false", matchIfMissing = true)
public class AzureSpringMonitorAutoConfig {

    private static final ClientLogger LOGGER = new ClientLogger(AzureSpringMonitorAutoConfig.class);

    private static final String CONNECTION_STRING_ERROR_MESSAGE = "Unable to find the Application Insights connection string.";

    private final Optional<AzureMonitorExporterBuilder> azureMonitorExporterBuilderOpt;

    /**
     * Create an instance of AzureSpringMonitorConfig
     * @param connectionStringSysProp connection string system property
     * @param httpPipeline an instance of HttpPipeline
     */
    public AzureSpringMonitorAutoConfig(@Value("${applicationinsights.connection.string:}") String connectionStringSysProp, ObjectProvider<HttpPipeline> httpPipeline) {
        this.azureMonitorExporterBuilderOpt = createAzureMonitorExporterBuilder(connectionStringSysProp, httpPipeline);
        if (!isNativeRuntimeExecution()) {
            LOGGER.warning("You are using Application Insights for Spring in a non-native GraalVM runtime environment. We recommend using the Application Insights Java agent.");
        }
    }

    private static boolean isNativeRuntimeExecution() {
        String imageCode = System.getProperty("org.graalvm.nativeimage.imagecode");
        return imageCode != null;
    }

    private Optional<AzureMonitorExporterBuilder> createAzureMonitorExporterBuilder(String connectionStringSysProp, ObjectProvider<HttpPipeline> httpPipeline) {
        Optional<String> connectionString = ConnectionStringRetriever.retrieveConnectionString(connectionStringSysProp);
        if (connectionString.isPresent()) {
            try {
                AzureMonitorExporterBuilder azureMonitorExporterBuilder = new AzureMonitorExporterBuilder().connectionString(connectionString.get());
                HttpPipeline providedHttpPipeline = httpPipeline.getIfAvailable();
                if (providedHttpPipeline != null) {
                    azureMonitorExporterBuilder = azureMonitorExporterBuilder.httpPipeline(providedHttpPipeline);
                }
                return Optional.of(azureMonitorExporterBuilder);
            } catch (IllegalArgumentException illegalArgumentException) {
                String errorMessage = illegalArgumentException.getMessage();
                if (errorMessage.contains("InstrumentationKey")) {
                    LOGGER.warning(CONNECTION_STRING_ERROR_MESSAGE + " Please check you have not used an instrumentation key instead of a connection string");
                }
            }
        } else {
            LOGGER.warning(CONNECTION_STRING_ERROR_MESSAGE);
        }
        return Optional.empty();
    }

    /**
     * Declare a MetricExporter bean
     * @return MetricExporter
     */
    @Bean
    public MetricExporter azureSpringMonitorMetricExporter() {
        if (!azureMonitorExporterBuilderOpt.isPresent()) {
            return null;
        }
        return azureMonitorExporterBuilderOpt.get().buildMetricExporter();
    }

    /**
     * Declare a SpanExporter bean
     * @return SpanExporter
     */
    @Bean
    public SpanExporter azureSpringMonitorSpanExporter() {
        if (!azureMonitorExporterBuilderOpt.isPresent()) {
            return null;
        }
        return azureMonitorExporterBuilderOpt.get().buildTraceExporter();
    }

    /**
     * Declare a LogRecordExporter bean
     * @return LogRecordExporter
     */
    @Bean
    public LogRecordExporter azureSpringMonitorLogRecordExporter() {
        if (!azureMonitorExporterBuilderOpt.isPresent()) {
            return null;
        }
        return azureMonitorExporterBuilderOpt.get().buildLogRecordExporter();
    }

    /**
     * Declare OpenTelemetryVersionCheckRunner bean to check the OpenTelemetry version
     * @param resource An OpenTelemetry resource
     * @return OpenTelemetryVersionCheckRunner
     */
    @Bean
    public OpenTelemetryVersionCheckRunner openTelemetryVersionCheckRunner(Resource resource) {
        return new OpenTelemetryVersionCheckRunner(resource);
    }
}
