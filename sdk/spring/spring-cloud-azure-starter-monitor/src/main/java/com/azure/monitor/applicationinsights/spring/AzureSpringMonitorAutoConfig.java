// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.applicationinsights.spring;

import com.azure.core.http.HttpPipeline;
import com.azure.monitor.opentelemetry.exporter.AzureMonitorExporterBuilder;
import io.opentelemetry.instrumentation.spring.autoconfigure.OpenTelemetryAutoConfiguration;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.logs.ConfigurableLogRecordExporterProvider;
import io.opentelemetry.sdk.autoconfigure.spi.metrics.ConfigurableMetricExporterProvider;
import io.opentelemetry.sdk.autoconfigure.spi.traces.ConfigurableSpanExporterProvider;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOG = LoggerFactory.getLogger(AzureSpringMonitorAutoConfig.class);

    static final String AZURE_EXPORTER_NAME = "azure-exporter";

    private final Optional<AzureMonitorExporterBuilder> azureMonitorExporterBuilderOpt;

    /**
     * Create an instance of AzureSpringMonitorConfig
     *
     * @param connectionStringSysProp connection string system property
     * @param httpPipeline an instance of HttpPipeline
     */
    public AzureSpringMonitorAutoConfig(@Value("${applicationinsights.connection.string:}") String connectionStringSysProp, ObjectProvider<HttpPipeline> httpPipeline) {
        this.azureMonitorExporterBuilderOpt = createAzureMonitorExporterBuilder(connectionStringSysProp, httpPipeline);
        if (!isNativeRuntimeExecution()) {
            LOG.warn("You are using Application Insights for Spring in a non-native GraalVM runtime environment. We recommend using the Application Insights Java agent.");
        }
    }

    private static boolean isNativeRuntimeExecution() {
        String imageCode = System.getProperty("org.graalvm.nativeimage.imagecode");
        return imageCode != null;
    }

    private Optional<AzureMonitorExporterBuilder> createAzureMonitorExporterBuilder(String connectionStringSysProp, ObjectProvider<HttpPipeline> httpPipeline) {
        Optional<String> connectionString = ConnectionStringRetriever.retrieveConnectionString(connectionStringSysProp);
        if (connectionString.isPresent()) {
            AzureMonitorExporterBuilder azureMonitorExporterBuilder = new AzureMonitorExporterBuilder().connectionString(connectionString.get());
            HttpPipeline providedHttpPipeline = httpPipeline.getIfAvailable();
            if (providedHttpPipeline != null) {
                azureMonitorExporterBuilder = azureMonitorExporterBuilder.httpPipeline(providedHttpPipeline);
            }
            return Optional.of(azureMonitorExporterBuilder);
        } else {
            LOG.warn("Unable to find the Application Insights connection string. The telemetry data won't be sent to Azure.");
        }
        return Optional.empty();
    }

    /**
     * Declare a ConfigurableMetricExporterProvider bean
     *
     * @return ConfigurableMetricExporterProvider
     */
    @Bean
    ConfigurableMetricExporterProvider otlpMetricExporterProvider() {
        MetricExporter metricExporter = createMetricExporter();
        return new ConfigurableMetricExporterProvider() {
            @Override
            public MetricExporter createExporter(ConfigProperties configProperties) {
                return metricExporter;
            }
            @Override
            public String getName() {
                return AZURE_EXPORTER_NAME;
            }
        };

    }

    private MetricExporter createMetricExporter() {
        if (!azureMonitorExporterBuilderOpt.isPresent()) {
            return null;
        }
        return azureMonitorExporterBuilderOpt.get().buildMetricExporter();
    }

    /**
     * Declare a ConfigurableSpanExporterProvider bean
     *
     * @return ConfigurableSpanExporterProvider
     */
    @Bean
    ConfigurableSpanExporterProvider otlpSpanExporterProvider() {
        SpanExporter spanExporter = createSpanExporter();
        return new ConfigurableSpanExporterProvider() {
            @Override
            public SpanExporter createExporter(ConfigProperties configProperties) {
                return spanExporter;
            }
            @Override
            public String getName() {
                return AZURE_EXPORTER_NAME;
            }
        };
    }

    private SpanExporter createSpanExporter() {
        if (!azureMonitorExporterBuilderOpt.isPresent()) {
            return null;
        }
        return azureMonitorExporterBuilderOpt.get().buildTraceExporter();
    }


    /**
     * Declare a ConfigurableLogRecordExporterProvider bean
     *
     * @return ConfigurableLogRecordExporterProvider
     */
    @Bean
    ConfigurableLogRecordExporterProvider otlpLogRecordExporterProvider() {
        LogRecordExporter logRecordExporter = createLogRecordExporter();
        return new ConfigurableLogRecordExporterProvider() {
            @Override
            public LogRecordExporter createExporter(ConfigProperties configProperties) {
                return logRecordExporter;
            }
            @Override
            public String getName() {
                return AZURE_EXPORTER_NAME;
            }
        };
    }

    private LogRecordExporter createLogRecordExporter() {
        if (!azureMonitorExporterBuilderOpt.isPresent()) {
            return null;
        }
        return azureMonitorExporterBuilderOpt.get().buildLogRecordExporter();
    }


    /**
     * Declare OpenTelemetryVersionCheckRunner bean to check the OpenTelemetry version
     *
     * @return OpenTelemetryVersionCheckRunner
     */
    @Bean
    public OpenTelemetryVersionCheckRunner openTelemetryVersionCheckRunner() {
        return new OpenTelemetryVersionCheckRunner();
    }
}
