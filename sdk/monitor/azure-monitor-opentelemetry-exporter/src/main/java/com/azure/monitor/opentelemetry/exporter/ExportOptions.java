package com.azure.monitor.opentelemetry.exporter;

import com.azure.core.annotation.Fluent;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.opentelemetry.AzureMonitor;
import com.azure.monitor.opentelemetry.exporter.implementation.configuration.ConnectionString;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Options to configure the Azure Monitor export.
 */
@Fluent
public class ExportOptions {

    private static final ClientLogger LOGGER = new ClientLogger(ExportOptions.class);

    HttpPipeline httpPipeline;
    HttpClient httpClient;
    HttpLogOptions httpLogOptions;
    final List<HttpPipelinePolicy> httpPipelinePolicies = new ArrayList<>();
    ClientOptions clientOptions;
    ConnectionString connectionString;
    TokenCredential credential;

    boolean frozen;

    /**
     * Sets the HTTP pipeline to use for the service client. If {@code httpPipeline} is set, all other
     * settings are ignored.
     *
     * @param httpPipeline The HTTP pipeline to use for sending service requests and receiving
     *                     responses.
     * @return The updated {@link AzureMonitorExporterBuilder} object.
     */
    public ExportOptions httpPipeline(HttpPipeline httpPipeline) {
        if (frozen) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                "httpPipeline cannot be changed after any of the build methods have been called"));
        }
        this.httpPipeline = httpPipeline;
        return this;
    }

    /**
     * Sets the HTTP client to use for sending and receiving requests to and from the service.
     *
     * @param httpClient The HTTP client to use for requests.
     * @return The updated {@link AzureMonitorExporterBuilder} object.
     */
    public ExportOptions httpClient(HttpClient httpClient) {
        if (frozen) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                "httpClient cannot be changed after any of the build methods have been called"));
        }
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
    public ExportOptions httpLogOptions(HttpLogOptions httpLogOptions) {
        if (frozen) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                "httpLogOptions cannot be changed after any of the build methods have been called"));
        }
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
        if (frozen) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                "httpPipelinePolicies cannot be added after any of the build methods have been called"));
        }
        httpPipelinePolicies.add(Objects.requireNonNull(httpPipelinePolicy, "'policy' cannot be null."));
        return this;
    }

    /**
     * Sets the client options such as application ID and custom headers to set on a request.
     *
     * @param clientOptions The client options.
     * @return The updated {@link AzureMonitorExporterBuilder} object.
     */
    public ExportOptions clientOptions(ClientOptions clientOptions) {
        if (frozen) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                "clientOptions cannot be changed after any of the build methods have been called"));
        }
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
        if (frozen) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                "connectionString cannot be changed after any of the build methods have been called"));
        }
        this.connectionString = ConnectionString.parse(connectionString);
        return this;
    }

    /**
     * Sets the token credential required for authentication with the ingestion endpoint service.
     *
     * @param credential The Azure Identity TokenCredential.
     * @return The updated {@link AzureMonitorExporterBuilder} object.
     */
    public ExportOptions credential(TokenCredential credential) {
        if (frozen) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                "credential cannot be changed after any of the build methods have been called"));
        }
        this.credential = credential;
        return this;
    }
}
