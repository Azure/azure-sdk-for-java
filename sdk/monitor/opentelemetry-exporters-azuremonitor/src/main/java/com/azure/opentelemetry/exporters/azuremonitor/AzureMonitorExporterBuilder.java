// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.opentelemetry.exporters.azuremonitor;

import com.azure.opentelemetry.exporters.azuremonitor.implementation.ApplicationInsightsClientImpl;
import com.azure.opentelemetry.exporters.azuremonitor.implementation.ApplicationInsightsClientImplBuilder;
import com.azure.opentelemetry.exporters.azuremonitor.implementation.NdJsonSerializer;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.opentelemetry.sdk.trace.export.SpanExporter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

/**
 * This class provides a fluent builder API to instantiate {@link AzureMonitorExporter} that implements
 * {@link SpanExporter} interface defined by OpenTelemetry API specification.
 */
public final class AzureMonitorExporterBuilder {
    private final ClientLogger logger = new ClientLogger(AzureMonitorExporterBuilder.class);
    private final ApplicationInsightsClientImplBuilder restServiceClientBuilder;
    private String instrumentationKey;

    /**
     * Creates an instance of {@link AzureMonitorExporterBuilder}.
     */
    public AzureMonitorExporterBuilder() {
        restServiceClientBuilder = new ApplicationInsightsClientImplBuilder();
    }

    /**
     * Sets the service endpoint for the Azure Monitor Exporter.
     * @param endpoint The URL of the Azure Monitor Exporter endpoint.
     * @return The updated {@link AzureMonitorExporterBuilder} object.
     * @throws NullPointerException if {@code endpoint} is null.
     * @throws IllegalArgumentException if {@code endpoint} cannot be parsed into a valid URL.
     */
    public AzureMonitorExporterBuilder endpoint(String endpoint) {
        Objects.requireNonNull(endpoint, "'endpoint' cannot be null.");

        try {
            URL url = new URL(endpoint);
            restServiceClientBuilder.host(url.getHost());
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsWarning(
                new IllegalArgumentException("'endpoint' must be a valid URL.", ex));
        }
        return this;
    }

    /**
     * Sets the HTTP pipeline to use for the service client. If {@code pipeline} is set, all other settings are
     * ignored, apart from {@link #endpoint(String) endpoint}.
     *
     * @param httpPipeline The HTTP pipeline to use for sending service requests and receiving responses.
     * @return The updated {@link AzureMonitorExporterBuilder} object.
     */
    public AzureMonitorExporterBuilder pipeline(HttpPipeline httpPipeline) {
        restServiceClientBuilder.pipeline(httpPipeline);
        return this;
    }

    /**
     * Sets the HTTP client to use for sending and receiving requests to and from the service.
     *
     * @param client The HTTP client to use for requests.
     * @return The updated {@link AzureMonitorExporterBuilder} object.
     */
    public AzureMonitorExporterBuilder httpClient(HttpClient client) {
        restServiceClientBuilder.httpClient(client);
        return this;
    }

    /**
     * Sets the logging configuration for HTTP requests and responses.
     *
     * <p> If logLevel is not provided, default value of {@link HttpLogDetailLevel#NONE} is set. </p>
     * @param logOptions The logging configuration to use when sending and receiving HTTP requests/responses.
     *
     * @return The updated {@link AzureMonitorExporterBuilder} object.
     */
    public AzureMonitorExporterBuilder httpLogOptions(HttpLogOptions logOptions) {
        restServiceClientBuilder.httpLogOptions(logOptions);
        return this;
    }

    /**
     * Sets the {@link RetryPolicy} that is used when each request is sent.
     * <p>
     * The default retry policy will be used if not provided to build {@link AzureMonitorExporterBuilder} .
     * @param retryPolicy user's retry policy applied to each request.
     *
     * @return The updated {@link AzureMonitorExporterBuilder} object.
     */
    public AzureMonitorExporterBuilder retryPolicy(RetryPolicy retryPolicy) {
        restServiceClientBuilder.retryPolicy(retryPolicy);
        return this;
    }

    /**
     * Adds a policy to the set of existing policies that are executed after required policies.
     * @param policy The retry policy for service requests.
     *
     * @return The updated {@link AzureMonitorExporterBuilder} object.
     * @throws NullPointerException If {@code policy} is {@code null}.
     */
    public AzureMonitorExporterBuilder addPolicy(HttpPipelinePolicy policy) {
        restServiceClientBuilder.addPolicy(Objects.requireNonNull(policy, "'policy' cannot be null."));
        return this;
    }

    /**
     * Sets the configuration store that is used during construction of the service client.
     * <p>
     * The default configuration store is a clone of the {@link Configuration#getGlobalConfiguration() global
     * configuration store}, use {@link Configuration#NONE} to bypass using configuration settings during construction.
     *
     * @param configuration The configuration store used to
     * @return The updated {@link AzureMonitorExporterBuilder} object.
     */
    public AzureMonitorExporterBuilder configuration(Configuration configuration) {
        restServiceClientBuilder.configuration(configuration);
        return this;
    }

    /**
     * Sets the instrumentation key to use for exporting telemetry events to Azure Monitor.
     * @param instrumentationKey The instrumentation key of the Azure Monitor resource.
     * @return The updated {@link AzureMonitorExporterBuilder} object.
     */
    public AzureMonitorExporterBuilder instrumentationKey(String instrumentationKey) {
        this.instrumentationKey = instrumentationKey;
        return this;
    }

    /**
     * Creates a {@link MonitorExporterClient} based on options set in the builder. Every time {@code
     * buildAsyncClient()} is called a new instance of {@link MonitorExporterClient} is created.
     *
     * <p>
     * If {@link #pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and {@link #endpoint(String)
     * endpoint} are used to create the {@link MonitorExporterAsyncClient client}. All other builder settings are
     * ignored.
     * </p>
     * @return A {@link MonitorExporterClient} with the options set from the builder.
     * @throws NullPointerException if {@link #endpoint(String) endpoint} has not been set.
     */
    MonitorExporterClient buildClient() {
        return new MonitorExporterClient(buildAsyncClient());
    }

    /**
     * Creates a {@link MonitorExporterAsyncClient} based on options set in the builder. Every time {@code
     * buildAsyncClient()} is called a new instance of {@link MonitorExporterAsyncClient} is created.
     *
     * <p>
     * If {@link #pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and {@link #endpoint(String)
     * endpoint} are used to create the {@link MonitorExporterAsyncClient client}. All other builder settings are
     * ignored.
     * </p>
     * @return A {@link MonitorExporterAsyncClient} with the options set from the builder.
     */
    MonitorExporterAsyncClient buildAsyncClient() {
        // Customize serializer to use NDJSON
        final SimpleModule ndjsonModule = new SimpleModule("Ndjson List Serializer");
        JacksonAdapter jacksonAdapter = new JacksonAdapter();
        jacksonAdapter.serializer().registerModule(ndjsonModule);
        ndjsonModule.addSerializer(new NdJsonSerializer());
        restServiceClientBuilder.serializerAdapter(jacksonAdapter);
        ApplicationInsightsClientImpl restServiceClient = restServiceClientBuilder.buildClient();

        return new MonitorExporterAsyncClient(restServiceClient);
    }

    /**
     * Creates an {@link AzureMonitorExporter} based on the options set in the builder. This exporter is an
     * implementation of OpenTelemetry {@link SpanExporter}.
     *
     * @return An instance of {@link AzureMonitorExporter}.
     * @throws NullPointerException if the instrumentation key is not set.
     */
    public AzureMonitorExporter buildExporter() {
        Objects.requireNonNull(instrumentationKey, "'instrumentationKey' cannot be null");
        return new AzureMonitorExporter(buildClient(), instrumentationKey);
    }

}
