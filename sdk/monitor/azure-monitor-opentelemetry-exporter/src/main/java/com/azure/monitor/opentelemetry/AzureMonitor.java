// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry;

import com.azure.core.annotation.Fluent;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.ClientOptions;
import com.azure.monitor.opentelemetry.exporter.AzureMonitorExporterBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.AzureMonitorExporterProviderKeys;
import com.azure.monitor.opentelemetry.exporter.implementation.AzureMonitorLogRecordExporterProvider;
import com.azure.monitor.opentelemetry.exporter.implementation.AzureMonitorMetricExporterProvider;
import com.azure.monitor.opentelemetry.exporter.implementation.AzureMonitorSpanExporterProvider;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdkBuilder;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.View;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to enable Azure Monitor for OpenTelemetry autoconfiguration.
 */
public final class AzureMonitor {

    @Fluent
    /**
     * Options to configure the Azure Monitor export.
     */
    public static class ExportOptions {

        private HttpPipeline httpPipeline;
        private HttpClient httpClient;
        private HttpLogOptions httpLogOptions;
        private HttpPipelinePolicy httpPipelinePolicy;
        private ClientOptions clientOptions;
        private String connectionString;
        private TokenCredential credential;

        private ExportOptions() {}

        /**
         * Sets the HTTP pipeline to use for the service client. If {@code httpPipeline} is set, all other
         * settings are ignored.
         *
         * @param httpPipeline The HTTP pipeline to use for sending service requests and receiving
         *                     responses.
         * @return The updated {@link AzureMonitor} object.
         */
        public ExportOptions httpPipeline(HttpPipeline httpPipeline) {
            this.httpPipeline = httpPipeline;
            return this;
        }

        /**
         * Sets the HTTP client to use for sending and receiving requests to and from the service.
         *
         * @param httpClient The HTTP client to use for requests.
         * @return The updated {@link AzureMonitor} object.
         */
        public ExportOptions httpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        /**
         * Sets the logging configuration for HTTP requests and responses.
         *
         * <p>If logLevel is not provided, default value of {@link HttpLogDetailLevel#NONE} is set.
         *
         * @param httpLogOptions The logging configuration to use when sending and receiving HTTP
         *                       requests/responses.
         * @return The updated {@link AzureMonitor} object.
         */
        public ExportOptions httpLogOptions(HttpLogOptions httpLogOptions) {
            this.httpLogOptions = httpLogOptions;
            return this;
        }

        /**
         * Adds a policy to the set of existing policies that are executed after required policies.
         *
         * @param httpPipelinePolicy a policy to be added to the http pipeline.
         * @return The updated {@link AzureMonitorExporterBuilder} object.
         * @throws NullPointerException If {@code policy} is {@code null}.
         */
        public ExportOptions addHttpPipelinePolicy(HttpPipelinePolicy httpPipelinePolicy) {
            this.httpPipelinePolicy = httpPipelinePolicy;
            return this;
        }

        /**
         * Sets the client options such as application ID and custom headers to set on a request.
         *
         * @param clientOptions The client options.
         * @return The updated {@link AzureMonitorExporterBuilder} object.
         */
        public ExportOptions clientOptions(ClientOptions clientOptions) {
            this.clientOptions = clientOptions;
            return this;
        }

        /**
         * Sets the connection string to use for exporting telemetry events to Azure Monitor.
         *
         * @param connectionString The connection string for the Azure Monitor resource.
         * @return The updated {@link AzureMonitorExporterBuilder} object.
         * @throws NullPointerException If the connection string is {@code null}.
         * @throws IllegalArgumentException If the connection string is invalid.
         */
        public ExportOptions connectionString(String connectionString) {
            this.connectionString = connectionString;
            return this;
        }

        /**
         * Sets the token credential required for authentication with the ingestion endpoint service.
         *
         * @param credential The Azure Identity TokenCredential.
         * @return The updated {@link AzureMonitorExporterBuilder} object.
         */
        public ExportOptions credential(TokenCredential credential) {
            this.credential = credential;
            return this;
        }
    }

    /**
     * Creeate export options.
     * @return the export options.
     */
    public static ExportOptions exportOptions() {
        return new ExportOptions();
    }

    /**
     * Configures an {@link AutoConfiguredOpenTelemetrySdkBuilder} for Azure Monitor based on the options set.
     */
    public static void configure(AutoConfiguredOpenTelemetrySdkBuilder autoConfiguredOpenTelemetrySdkBuilder) {
        configure(autoConfiguredOpenTelemetrySdkBuilder, new ExportOptions());
    }

    /**
     * Configures an {@link AutoConfiguredOpenTelemetrySdkBuilder} for Azure Monitor based on the options set.
     * @param autoConfiguredOpenTelemetrySdkBuilder the {@link AutoConfiguredOpenTelemetrySdkBuilder} object.
     */
    public static void configure(AutoConfiguredOpenTelemetrySdkBuilder autoConfiguredOpenTelemetrySdkBuilder, ExportOptions exportOptions) {

        AzureMonitorExporterBuilder azureMonitorExporterBuilder = createExporterBuilder(exportOptions);

        autoConfiguredOpenTelemetrySdkBuilder.addPropertiesSupplier(() -> {
            Map<String, String> props = new HashMap<>();
            props.put("otel.traces.exporter", AzureMonitorExporterProviderKeys.EXPORTER_NAME);
            props.put("otel.metrics.exporter", AzureMonitorExporterProviderKeys.EXPORTER_NAME);
            props.put("otel.logs.exporter", AzureMonitorExporterProviderKeys.EXPORTER_NAME);
            props.put(AzureMonitorExporterProviderKeys.INTERNAL_USING_AZURE_MONITOR_EXPORTER_BUILDER, "true");
            return props;
        });
        autoConfiguredOpenTelemetrySdkBuilder.addSpanExporterCustomizer((spanExporter, configProperties) -> {
            if (spanExporter instanceof AzureMonitorSpanExporterProvider.MarkerSpanExporter) {
                spanExporter = azureMonitorExporterBuilder.buildTraceExporter(configProperties);
            }
            return spanExporter;
        });
        autoConfiguredOpenTelemetrySdkBuilder.addMetricExporterCustomizer((metricExporter, configProperties) -> {
            if (metricExporter instanceof AzureMonitorMetricExporterProvider.MarkerMetricExporter) {
                metricExporter = azureMonitorExporterBuilder.buildMetricExporter(configProperties);
            }
            return metricExporter;
        });
        autoConfiguredOpenTelemetrySdkBuilder.addLogRecordExporterCustomizer((logRecordExporter, configProperties) -> {
            if (logRecordExporter instanceof AzureMonitorLogRecordExporterProvider.MarkerLogRecordExporter) {
                logRecordExporter = azureMonitorExporterBuilder.buildLogRecordExporter(configProperties);
            }
            return logRecordExporter;
        });
        // TODO (trask)
        //        sdkBuilder.addTracerProviderCustomizer((sdkTracerProviderBuilder, configProperties) -> {
        //            QuickPulse quickPulse = QuickPulse.create(getHttpPipeline());
        //            return sdkTracerProviderBuilder.addSpanProcessor(
        //                ne
        autoConfiguredOpenTelemetrySdkBuilder
            .addMeterProviderCustomizer((sdkMeterProviderBuilder, config) -> sdkMeterProviderBuilder
                .registerView(InstrumentSelector.builder().setMeterName("io.opentelemetry.sdk.trace").build(),
                    View.builder().setAggregation(Aggregation.drop()).build())
                .registerView(InstrumentSelector.builder().setMeterName("io.opentelemetry.sdk.logs").build(),
                    View.builder().setAggregation(Aggregation.drop()).build()));
    }

    private static AzureMonitorExporterBuilder createExporterBuilder(ExportOptions exportOptions) {
        AzureMonitorExporterBuilder azureMonitorExporterBuilder = new AzureMonitorExporterBuilder();
        if(exportOptions.httpPipeline != null) {
            azureMonitorExporterBuilder.httpPipeline(exportOptions.httpPipeline);
        }
        if(exportOptions.httpClient != null) {
            azureMonitorExporterBuilder.httpClient(exportOptions.httpClient);
        }
        if(exportOptions.httpLogOptions != null) {
            azureMonitorExporterBuilder.httpLogOptions(exportOptions.httpLogOptions);
        }
        if(exportOptions.httpPipelinePolicy != null) {
            azureMonitorExporterBuilder.addHttpPipelinePolicy(exportOptions.httpPipelinePolicy);
        }
        if(exportOptions.clientOptions != null) {
            azureMonitorExporterBuilder.clientOptions(exportOptions.clientOptions);
        }
        if(exportOptions.connectionString != null) {
            azureMonitorExporterBuilder.connectionString(exportOptions.connectionString);
        }
        if(exportOptions.credential != null) {
            azureMonitorExporterBuilder.credential(exportOptions.credential);
        }
        return azureMonitorExporterBuilder;
    }
}
