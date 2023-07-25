// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.applicationinsights.spring;

import com.azure.core.http.HttpPipeline;
import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.opentelemetry.exporter.AzureMonitorExporterBuilder;
import io.opentelemetry.api.logs.GlobalLoggerProvider;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

/**
 * Config for Azure Telemetry
 */
@Configuration(proxyBeanMethods = false)
public class AzureTelemetryConfig {

    private static final ClientLogger LOGGER = new ClientLogger(AzureTelemetryConfig.class);

    private static final String CONNECTION_STRING_ERROR_MESSAGE = "Unable to find the Application Insights connection string.";

    private final Optional<AzureMonitorExporterBuilder> azureMonitorExporterBuilderOpt;

    /**
     * Create an instance of AzureTelemetryConfig
     * @param connectionStringSysProp connection string system property
     * @param azureTelemetryActivation a instance of AzureTelemetryActivation
     * @param httpPipeline an instance of HttpPipeline
     */
    public AzureTelemetryConfig(@Value("${applicationinsights.connection.string:}") String connectionStringSysProp, AzureTelemetryActivation azureTelemetryActivation, ObjectProvider<HttpPipeline> httpPipeline) {
        if (azureTelemetryActivation.isTrue()) {
            this.azureMonitorExporterBuilderOpt = createAzureMonitorExporterBuilder(connectionStringSysProp, httpPipeline);
            if (!isNativeRuntimeExecution()) {
                LOGGER.warning("You are using Application Insights for Spring in a non-native GraalVM runtime environment. We recommend using the Application Insights Java agent.");
            }
        } else {
            azureMonitorExporterBuilderOpt = Optional.empty();
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
    public MetricExporter metricExporter() {
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
    public SpanExporter spanExporter() {
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
    public LogRecordExporter logRecordExporter() {
        if (!azureMonitorExporterBuilderOpt.isPresent()) {
            return null;
        }
        LogRecordExporter logRecordExporter = azureMonitorExporterBuilderOpt.get().buildLogRecordExporter();
        initOTelLogger(logRecordExporter);
        return logRecordExporter;
    }

    private void initOTelLogger(LogRecordExporter logRecordExporter) {
        if (azureMonitorExporterBuilderOpt.isPresent()) {
            BatchLogRecordProcessor batchLogRecordProcessor = BatchLogRecordProcessor.builder(logRecordExporter).build();
            SdkLoggerProvider loggerProvider =
                SdkLoggerProvider.builder()
                    .addLogRecordProcessor(batchLogRecordProcessor)
                    .build();
            GlobalLoggerProvider.set(loggerProvider);
        }
    }
}
