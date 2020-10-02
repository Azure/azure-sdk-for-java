// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.opentelemetry.exporter.azuremonitor;

import com.azure.opentelemetry.exporter.azuremonitor.implementation.ApplicationInsightsClientImpl;
import com.azure.opentelemetry.exporter.azuremonitor.implementation.ApplicationInsightsClientImplBuilder;
import com.azure.opentelemetry.exporter.azuremonitor.implementation.NdJsonSerializer;
import com.azure.core.annotation.ServiceClientBuilder;
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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help instantiation of {@link MonitorExporterClient
 * MonitorExporterClients} and {@link MonitorExporterAsyncClient MonitorExporterAsyncClients}, call
 * {@link #buildClient()} buildClient} and {@link #buildAsyncClient() buildAsyncClient} respectively to construct an
 * instance of the desired client.
 */
@ServiceClientBuilder(serviceClients = {MonitorExporterClient.class, MonitorExporterAsyncClient.class})
public final class MonitorExporterClientBuilder {
    private final ClientLogger logger = new ClientLogger(MonitorExporterClientBuilder.class);

    private ApplicationInsightsClientImplBuilder restServiceClientBuilder;

    /**
     * Creates an instance of {@link MonitorExporterClientBuilder}.
     */
    public MonitorExporterClientBuilder() {
        restServiceClientBuilder = new ApplicationInsightsClientImplBuilder();
    }

    /**
     * Sets the service endpoint for the Azure Monitor Exporter.
     * @param endpoint The URL of the Azure Monitor Exporter endpoint.
     * @return The updated {@link MonitorExporterClientBuilder} object.
     * @throws NullPointerException if {@code endpoint} is null.
     * @throws IllegalArgumentException if {@code endpoint} cannot be parsed into a valid URL.
     */
    public MonitorExporterClientBuilder endpoint(String endpoint) {
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
     * @return The updated {@link MonitorExporterClientBuilder} object.
     */
    public MonitorExporterClientBuilder pipeline(HttpPipeline httpPipeline) {
        restServiceClientBuilder.pipeline(httpPipeline);
        return this;
    }

    /**
     * Sets the HTTP client to use for sending and receiving requests to and from the service.
     *
     * @param client The HTTP client to use for requests.
     * @return The updated {@link MonitorExporterClientBuilder} object.
     */
    public MonitorExporterClientBuilder httpClient(HttpClient client) {
        restServiceClientBuilder.httpClient(client);
        return this;
    }

    /**
     * Sets the logging configuration for HTTP requests and responses.
     *
     * <p> If logLevel is not provided, default value of {@link HttpLogDetailLevel#NONE} is set. </p>
     * @param logOptions The logging configuration to use when sending and receiving HTTP requests/responses.
     *
     * @return The updated {@link MonitorExporterClientBuilder} object.
     */
    public MonitorExporterClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        restServiceClientBuilder.httpLogOptions(logOptions);
        return this;
    }

    /**
     * Sets the {@link RetryPolicy} that is used when each request is sent.
     * <p>
     * The default retry policy will be used if not provided to build {@link MonitorExporterClientBuilder} .
     * @param retryPolicy user's retry policy applied to each request.
     *
     * @return The updated {@link MonitorExporterClientBuilder} object.
     */
    public MonitorExporterClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        restServiceClientBuilder.retryPolicy(retryPolicy);
        return this;
    }

    /**
     * Adds a policy to the set of existing policies that are executed after required policies.
     * @param policy The retry policy for service requests.
     *
     * @return The updated {@link MonitorExporterClientBuilder} object.
     * @throws NullPointerException If {@code policy} is {@code null}.
     */
    public MonitorExporterClientBuilder addPolicy(HttpPipelinePolicy policy) {
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
     * @return The updated {@link MonitorExporterClientBuilder} object.
     */
    public MonitorExporterClientBuilder configuration(Configuration configuration) {
        restServiceClientBuilder.configuration(configuration);
        return this;
    }

    /**
     * Sets the {@link MonitorExporterServiceVersion} that is used when making API requests.
     * <p>
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version the client library will have the result of potentially moving to a newer service version.
     * @param version {@link MonitorExporterServiceVersion} of the service to be used when making requests.
     *
     * @return The updated {@link MonitorExporterClientBuilder} object.
     */
    public MonitorExporterClientBuilder serviceVersion(MonitorExporterServiceVersion version) {
        // no-op as the rest client doesn't support setting service version yet and there's only one version
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
    public MonitorExporterClient buildClient() {
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
    public MonitorExporterAsyncClient buildAsyncClient() {

        // Customize serializer to use NDJSON
        final SimpleModule ndjsonModule = new SimpleModule("Ndjson List Serializer");
        JacksonAdapter jacksonAdapter = new JacksonAdapter();
        jacksonAdapter.serializer().registerModule(ndjsonModule);
        ndjsonModule.addSerializer(new NdJsonSerializer());
        restServiceClientBuilder.serializerAdapter(jacksonAdapter);
        ApplicationInsightsClientImpl restServiceClient = restServiceClientBuilder.buildClient();

        return new MonitorExporterAsyncClient(restServiceClient);
    }

}
