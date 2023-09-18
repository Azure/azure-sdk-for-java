// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.opentelemetry.exporter.implementation.configuration.ConnectionString;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryItemExporter;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdkBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This class provides a fluent builder API to configure the OpenTelemetry SDK with Azure Monitor Exporters.
 */
public final class AzureMonitorExporterBuilder {

    private static final ClientLogger LOGGER = new ClientLogger(AzureMonitorExporterBuilder.class);

    private static final String APPLICATIONINSIGHTS_CONNECTION_STRING =
        "APPLICATIONINSIGHTS_CONNECTION_STRING";
    private static final String APPLICATIONINSIGHTS_AUTHENTICATION_SCOPE =
        "https://monitor.azure.com//.default";

    private static final Map<String, String> PROPERTIES =
        CoreUtils.getProperties("azure-monitor-opentelemetry-exporter.properties");

    private ConnectionString connectionString;
    private TokenCredential credential;

    // suppress warnings is needed in ApplicationInsights-Java repo, can be removed when upstreaming
    @SuppressWarnings({"UnusedVariable", "FieldCanBeLocal"})
    private AzureMonitorExporterServiceVersion serviceVersion;

    private HttpPipeline httpPipeline;
    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions;
    private final List<HttpPipelinePolicy> httpPipelinePolicies = new ArrayList<>();
    private ClientOptions clientOptions;

    // these are only populated after the builder is frozen
    private HttpPipeline theHttpPipeline;
    private TelemetryItemExporter theTelemetryItemExporter;
    private ConnectionString theConnectionString;

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
     * responses.
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
     * requests/responses.
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
     * @throws NullPointerException If the connection string is {@code null}.
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
     *
     * @param sdkBuilder the {@link AutoConfiguredOpenTelemetrySdkBuilder} in which to install the azure monitor exporter.
     */
    public void build(AutoConfiguredOpenTelemetrySdkBuilder sdkBuilder) {
        // FREEZE THE BUILDER
        AzureMonitorExporterBuilderHelper helper =
            new AzureMonitorExporterBuilderHelper(connectionStringBuilder, telemetryItemExporterBuilder, httpPipelineBuilder);
        helper.build(sdkBuilder);
    }
}
