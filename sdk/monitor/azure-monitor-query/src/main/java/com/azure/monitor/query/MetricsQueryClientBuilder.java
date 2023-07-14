// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.client.traits.ConfigurationTrait;
import com.azure.core.client.traits.EndpointTrait;
import com.azure.core.client.traits.HttpTrait;
import com.azure.core.client.traits.TokenCredentialTrait;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.AddHeadersFromContextPolicy;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.builder.ClientBuilderUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.query.implementation.metrics.MonitorManagementClient;
import com.azure.monitor.query.implementation.metricsdefinitions.MetricsDefinitionsClient;
import com.azure.monitor.query.implementation.metricsnamespaces.MetricsNamespacesClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.azure.core.util.CoreUtils.getApplicationId;

/**
 * Fluent builder for creating instances of {@link MetricsQueryClient} and {@link MetricsQueryAsyncClient}.
 *
 * <p><strong>Instantiating an asynchronous Metrics query Client</strong></p>
 *
 * <!-- src_embed com.azure.monitor.query.MetricsQueryAsyncClient.instantiation -->
 * <pre>
 * MetricsQueryAsyncClient metricsQueryAsyncClient = new MetricsQueryClientBuilder&#40;&#41;
 *         .credential&#40;tokenCredential&#41;
 *         .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.monitor.query.MetricsQueryAsyncClient.instantiation -->
 *
 * <p><strong>Instantiating a synchronous Metrics query Client</strong></p>
 *
 * <!-- src_embed com.azure.monitor.query.MetricsQueryClient.instantiation -->
 * <pre>
 * MetricsQueryClient metricsQueryClient = new MetricsQueryClientBuilder&#40;&#41;
 *         .credential&#40;tokenCredential&#41;
 *         .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.monitor.query.MetricsQueryClient.instantiation -->
 */
@ServiceClientBuilder(serviceClients = {MetricsQueryClient.class, MetricsQueryAsyncClient.class})
public final class MetricsQueryClientBuilder implements EndpointTrait<MetricsQueryClientBuilder>,
        HttpTrait<MetricsQueryClientBuilder>, ConfigurationTrait<MetricsQueryClientBuilder>, TokenCredentialTrait<MetricsQueryClientBuilder> {
    private static final ClientLogger LOGGER = new ClientLogger(MetricsQueryClientBuilder.class);

    private static final String CLIENT_NAME;
    private static final String CLIENT_VERSION;

    static {
        Map<String, String> properties = CoreUtils.getProperties("azure-monitor-query.properties");
        CLIENT_NAME = properties.getOrDefault("name", "UnknownName");
        CLIENT_VERSION = properties.getOrDefault("version", "UnknownVersion");
    }

    private final List<HttpPipelinePolicy> perCallPolicies = new ArrayList<>();
    private final List<HttpPipelinePolicy> perRetryPolicies = new ArrayList<>();

    private String endpoint;
    private HttpPipeline pipeline;
    private HttpClient httpClient;
    private Configuration configuration;
    private HttpLogOptions httpLogOptions;
    private HttpPipelinePolicy retryPolicy;
    private RetryOptions retryOptions;
    private ClientOptions clientOptions;
    private TokenCredential tokenCredential;
    private MetricsQueryServiceVersion serviceVersion;

    /**
     * Sets the metrics query endpoint.
     * @param endpoint the host value.
     * @return the MetricsClientBuilder.
     */
    @Override
    public MetricsQueryClientBuilder endpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    /**
     * Sets The HTTP pipeline to send requests through.
     * @param pipeline the pipeline value.
     * @return the MetricsClientBuilder.
     */
    @Override
    public MetricsQueryClientBuilder pipeline(HttpPipeline pipeline) {
        if (this.pipeline != null && pipeline == null) {
            LOGGER.info("HttpPipeline is being set to 'null' when it was previously configured.");
        }

        this.pipeline = pipeline;
        return this;
    }

    /**
     * Sets The HTTP client used to send the request.
     * @param httpClient the httpClient value.
     * @return the MetricsClientBuilder.
     */
    @Override
    public MetricsQueryClientBuilder httpClient(HttpClient httpClient) {
        if (this.httpClient != null && httpClient == null) {
            LOGGER.info("HttpClient is being set to 'null' when it was previously configured.");
        }

        this.httpClient = httpClient;
        return this;
    }

    /**
     * Sets The configuration store that is used during construction of the service client.
     * @param configuration the configuration value.
     * @return the MetricsClientBuilder.
     */
    @Override
    public MetricsQueryClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets The logging configuration for HTTP requests and responses.
     * @param httpLogOptions the httpLogOptions value.
     * @return the MetricsClientBuilder.
     */
    @Override
    public MetricsQueryClientBuilder httpLogOptions(HttpLogOptions httpLogOptions) {
        this.httpLogOptions = httpLogOptions;
        return this;
    }

    /**
     * Sets The retry policy that will attempt to retry failed requests, if applicable.
     * @param retryPolicy the retryPolicy value.
     * @return the MetricsClientBuilder.
     */
    public MetricsQueryClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
        return this;
    }

    /**
     * Sets the {@link RetryOptions} used for creating the client.
     * @param retryOptions The {@link RetryOptions}.
     * @return the updated {@link MetricsQueryClientBuilder}.
     */
    @Override
    public MetricsQueryClientBuilder retryOptions(RetryOptions retryOptions) {
        this.retryOptions = retryOptions;
        return this;
    }

    /**
     * Adds a custom Http pipeline policy.
     * @param customPolicy The custom Http pipeline policy to add.
     * @return the MetricsClientBuilder.
     */
    @Override
    public MetricsQueryClientBuilder addPolicy(HttpPipelinePolicy customPolicy) {
        Objects.requireNonNull(customPolicy, "'customPolicy' cannot be null.");
        if (customPolicy.getPipelinePosition() == HttpPipelinePosition.PER_RETRY) {
            perRetryPolicies.add(customPolicy);
        } else {
            perCallPolicies.add(customPolicy);
        }
        return this;
    }

    /**
     * Sets The TokenCredential used for authentication.
     * @param tokenCredential the tokenCredential value.
     * @return the MetricsClientBuilder.
     */
    @Override
    public MetricsQueryClientBuilder credential(TokenCredential tokenCredential) {
        this.tokenCredential = tokenCredential;
        return this;
    }

    /**
     * Set the {@link ClientOptions} used for creating the client.
     * @param clientOptions The {@link ClientOptions}.
     * @return the {@link MetricsQueryClientBuilder}
     */
    @Override
    public MetricsQueryClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
        return this;
    }

    /**
     * The service version to use when creating the client.
     * @param serviceVersion The {@link MetricsQueryServiceVersion}.
     * @return the {@link MetricsQueryClientBuilder}
     */
    public MetricsQueryClientBuilder serviceVersion(MetricsQueryServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
        return this;
    }

    /**
     * Creates a synchronous client with the configured options in this builder.
     * @return A synchronous {@link MetricsQueryClient}.
     */
    public MetricsQueryClient buildClient() {
        String host = getBuildEndpoint();
        MetricsQueryServiceVersion apiVersion = getBuildServiceVersion();
        HttpPipeline httpPipeline = getOrCreateHttpPipeline();

        return new MetricsQueryClient(createMonitorManagementClient(host, httpPipeline, apiVersion),
            createMetricsNamespacesClient(host, httpPipeline),
            createMetricsDefinitionsClient(host, httpPipeline, apiVersion));
    }

    /**
     * Creates an asynchronous client with the configured options in this builder.
     * @return An asynchronous {@link MetricsQueryAsyncClient}.
     */
    public MetricsQueryAsyncClient buildAsyncClient() {
        String host = getBuildEndpoint();
        MetricsQueryServiceVersion apiVersion = getBuildServiceVersion();
        HttpPipeline httpPipeline = getOrCreateHttpPipeline();

        return new MetricsQueryAsyncClient(createMonitorManagementClient(host, httpPipeline, apiVersion),
            createMetricsNamespacesClient(host, httpPipeline),
            createMetricsDefinitionsClient(host, httpPipeline, apiVersion));
    }



    private MonitorManagementClient createMonitorManagementClient(String endpoint, HttpPipeline pipeline,
                                                                  MetricsQueryServiceVersion serviceVersion) {
        return new MonitorManagementClient(pipeline, endpoint, serviceVersion.getVersion());
    }

    private MetricsDefinitionsClient createMetricsDefinitionsClient(String endpoint, HttpPipeline pipeline,
                                                                    MetricsQueryServiceVersion serviceVersion) {
        return new MetricsDefinitionsClient(pipeline, endpoint, serviceVersion.getVersion());
    }

    private MetricsNamespacesClient createMetricsNamespacesClient(String endpoint, HttpPipeline pipeline) {
        // Namespaces uses a different version than the other clients.
        // Only 2017-12-01-preview is supported.
        return new MetricsNamespacesClient(pipeline, endpoint, "2017-12-01-preview");
    }

    private String getBuildEndpoint() {
        return (this.endpoint == null) ? "https://management.azure.com" : this.endpoint;
    }

    private MetricsQueryServiceVersion getBuildServiceVersion() {
        return (this.serviceVersion == null) ? MetricsQueryServiceVersion.getLatest() : this.serviceVersion;
    }

    private HttpPipeline getOrCreateHttpPipeline() {
        if (this.pipeline != null) {
            return this.pipeline;
        }

        Configuration buildConfiguration = (configuration == null)
            ? Configuration.getGlobalConfiguration()
            : configuration;

        final List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new UserAgentPolicy(
            getApplicationId(clientOptions, httpLogOptions), CLIENT_NAME, CLIENT_VERSION, buildConfiguration));
        policies.add(new RequestIdPolicy());
        policies.add(new AddHeadersFromContextPolicy());

        policies.addAll(perCallPolicies);
        HttpPolicyProviders.addBeforeRetryPolicies(policies);

        policies.add(ClientBuilderUtil.validateAndGetRetryPolicy(retryPolicy, retryOptions, new RetryPolicy()));

        policies.add(new AddDatePolicy());

        if (tokenCredential != null) {
            String localHost = (endpoint != null) ? endpoint : "https://management.azure.com";
            policies.add(new BearerTokenAuthenticationPolicy(tokenCredential, String.format("%s/.default", localHost)));
        }

        policies.addAll(perRetryPolicies);

        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(httpLogOptions));

        return new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .clientOptions(clientOptions)
            .build();
    }

}
