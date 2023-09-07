// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.monitor.opentelemetry.exporter.implementation.configuration.ConnectionString;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdkBuilder;
import io.opentelemetry.sdk.trace.export.SpanExporter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class provides a fluent builder API to instantiate {@link AzureMonitorTraceExporter} that
 * implements {@link SpanExporter} interface defined by OpenTelemetry API specification.
 */
public final class AzureMonitorExporterBuilder {

    private ConnectionString connectionString;
    private TokenCredential credential;

    private AzureMonitorExporterServiceVersion serviceVersion;

    private HttpPipeline httpPipeline;
    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions;
    private final List<HttpPipelinePolicy> httpPipelinePolicies = new ArrayList<>();

    private Configuration configuration;
    private ClientOptions clientOptions;

    /**
     * Creates an instance of {@link AzureMonitorExporterBuilder}.
     */
    public AzureMonitorExporterBuilder() {
    }

    /**
     * Sets the HTTP pipeline to use for the service client. If {@code httpPipeline} is set, all other
     * settings are ignored.
     *
     * @param httpPipeline The HTTP pipeline to use for sending service requests and receiving
     *                     responses.
     * @return The updated {@link AzureMonitorExporterBuilder} object.
     */
    public AzureMonitorExporterBuilder httpPipeline(HttpPipeline httpPipeline) {
        this.httpPipeline = httpPipeline;
        return this;
    }

    /**
     * Sets the HTTP client to use for sending and receiving requests to and from the service.
     *
     * @param httpClient The HTTP client to use for requests.
     * @return The updated {@link AzureMonitorExporterBuilder} object.
     */
    public AzureMonitorExporterBuilder httpClient(HttpClient httpClient) {
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
     * @return The updated {@link AzureMonitorExporterBuilder} object.
     */
    public AzureMonitorExporterBuilder httpLogOptions(HttpLogOptions httpLogOptions) {
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
    public AzureMonitorExporterBuilder addHttpPipelinePolicy(HttpPipelinePolicy httpPipelinePolicy) {
        httpPipelinePolicies.add(
            Objects.requireNonNull(httpPipelinePolicy, "'policy' cannot be null."));
        return this;
    }

    /**
     * Sets the configuration store that is used during construction of the service client.
     *
     * <p>The default configuration store is a clone of the {@link
     * Configuration#getGlobalConfiguration() global configuration store}, use {@link
     * Configuration#NONE} to bypass using configuration settings during construction.
     *
     * @param configuration The configuration store used to
     * @return The updated {@link AzureMonitorExporterBuilder} object.
     */
    // TODO (trask) remove in favor of OpenTelemetry ConfigProperties?
    public AzureMonitorExporterBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the client options such as application ID and custom headers to set on a request.
     *
     * @param clientOptions The client options.
     * @return The updated {@link AzureMonitorExporterBuilder} object.
     */
    public AzureMonitorExporterBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
        return this;
    }

    /**
     * Sets the connection string to use for exporting telemetry events to Azure Monitor.
     *
     * @param connectionString The connection string for the Azure Monitor resource.
     * @return The updated {@link AzureMonitorExporterBuilder} object.
     * @throws NullPointerException     If the connection string is {@code null}.
     * @throws IllegalArgumentException If the connection string is invalid.
     */
    public AzureMonitorExporterBuilder connectionString(String connectionString) {
        this.connectionString = ConnectionString.parse(connectionString);
        return this;
    }

    /**
     * Sets the Azure Monitor service version.
     *
     * @param serviceVersion The Azure Monitor service version.
     * @return The update {@link AzureMonitorExporterBuilder} object.
     */
    public AzureMonitorExporterBuilder serviceVersion(
        AzureMonitorExporterServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
        return this;
    }

    /**
     * Sets the token credential required for authentication with the ingestion endpoint service.
     *
     * @param credential The Azure Identity TokenCredential.
     * @return The updated {@link AzureMonitorExporterBuilder} object.
     */
    public AzureMonitorExporterBuilder credential(TokenCredential credential) {
        this.credential = credential;
        return this;
    }

    /**
     * Configures an {@link AutoConfiguredOpenTelemetrySdkBuilder} based on the options set in the builder.
     */
    public void build(AutoConfiguredOpenTelemetrySdkBuilder sdkBuilder) {
        HttpPipelineBuilder httpPipelineBuilder = new HttpPipelineBuilder(credential, httpClient, httpLogOptions,
            httpPipelinePolicies, configuration, clientOptions, httpPipeline);
        AzureMonitorExporterBuilderHelper helper =
            new AzureMonitorExporterBuilderHelper(connectionString, serviceVersion, configuration, clientOptions, httpPipelineBuilder);
        helper.build(sdkBuilder);
    }
}
