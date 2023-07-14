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
import com.azure.monitor.query.implementation.logs.AzureLogAnalytics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.azure.core.util.CoreUtils.getApplicationId;

/**
 * Fluent builder for creating instances of {@link LogsQueryClient} and {@link LogsQueryAsyncClient}.
 *
 * <p><strong>Instantiating an asynchronous Logs query Client</strong></p>
 * <!-- src_embed com.azure.monitor.query.LogsQueryAsyncClient.instantiation -->
 * <pre>
 * LogsQueryAsyncClient logsQueryAsyncClient = new LogsQueryClientBuilder&#40;&#41;
 *         .credential&#40;tokenCredential&#41;
 *         .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.monitor.query.LogsQueryAsyncClient.instantiation -->
 *
 * <p><strong>Instantiating a synchronous Logs query Client</strong></p>
 * <!-- src_embed com.azure.monitor.query.LogsQueryClient.instantiation -->
 * <pre>
 * LogsQueryClient logsQueryClient = new LogsQueryClientBuilder&#40;&#41;
 *         .credential&#40;tokenCredential&#41;
 *         .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.monitor.query.LogsQueryClient.instantiation -->
 */
@ServiceClientBuilder(serviceClients = {LogsQueryClient.class, LogsQueryAsyncClient.class})
public final class LogsQueryClientBuilder implements EndpointTrait<LogsQueryClientBuilder>,
    HttpTrait<LogsQueryClientBuilder>, ConfigurationTrait<LogsQueryClientBuilder>, TokenCredentialTrait<LogsQueryClientBuilder> {
    private static final ClientLogger LOGGER = new ClientLogger(LogsQueryClientBuilder.class);

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
    private LogsQueryServiceVersion serviceVersion;

    /**
     * Sets the log query endpoint.
     *
     * @param endpoint the host value.
     * @return the {@link LogsQueryClientBuilder}.
     */
    @Override
    public LogsQueryClientBuilder endpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    /**
     * Sets The HTTP pipeline to send requests through.
     *
     * @param pipeline the pipeline value.
     * @return the {@link LogsQueryClientBuilder}.
     */
    @Override
    public LogsQueryClientBuilder pipeline(HttpPipeline pipeline) {
        if (this.pipeline != null && pipeline == null) {
            LOGGER.info("HttpPipeline is being set to 'null' when it was previously configured.");
        }

        this.pipeline = pipeline;
        return this;
    }

    /**
     * Sets The HTTP client used to send the request.
     *
     * @param httpClient the httpClient value.
     * @return the {@link LogsQueryClientBuilder}.
     */
    @Override
    public LogsQueryClientBuilder httpClient(HttpClient httpClient) {
        if (this.httpClient != null && httpClient == null) {
            LOGGER.info("HttpClient is being set to 'null' when it was previously configured.");
        }

        this.httpClient = httpClient;
        return this;
    }

    /**
     * Sets The configuration store that is used during construction of the service client.
     *
     * @param configuration the configuration value.
     * @return the {@link LogsQueryClientBuilder}.
     */
    @Override
    public LogsQueryClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets The logging configuration for HTTP requests and responses.
     *
     * @param httpLogOptions the httpLogOptions value.
     * @return the {@link LogsQueryClientBuilder}.
     */
    @Override
    public LogsQueryClientBuilder httpLogOptions(HttpLogOptions httpLogOptions) {
        this.httpLogOptions = httpLogOptions;
        return this;
    }

    /**
     * Sets The retry policy that will attempt to retry failed requests, if applicable.
     *
     * @param retryPolicy the retryPolicy value.
     * @return the {@link LogsQueryClientBuilder}.
     */
    public LogsQueryClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
        return this;
    }

    /**
     * Sets the {@link RetryOptions} used for creating the client.
     *
     * @param retryOptions The {@link RetryOptions}.
     * @return the updated {@link LogsQueryClientBuilder}.
     */
    @Override
    public LogsQueryClientBuilder retryOptions(RetryOptions retryOptions) {
        this.retryOptions = retryOptions;
        return this;
    }

    /**
     * Adds a custom Http pipeline policy.
     *
     * @param customPolicy The custom Http pipeline policy to add.
     * @return the {@link LogsQueryClientBuilder}.
     */
    @Override
    public LogsQueryClientBuilder addPolicy(HttpPipelinePolicy customPolicy) {
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
     *
     * @param tokenCredential the tokenCredential value.
     * @return the {@link LogsQueryClientBuilder}.
     */
    @Override
    public LogsQueryClientBuilder credential(TokenCredential tokenCredential) {
        this.tokenCredential = tokenCredential;
        return this;
    }

    /**
     * Set the {@link ClientOptions} used for creating the client.
     *
     * @param clientOptions The {@link ClientOptions}.
     * @return the {@link LogsQueryClientBuilder}.
     */
    @Override
    public LogsQueryClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
        return this;
    }

    /**
     * The service version to use when creating the client.
     *
     * @param serviceVersion The {@link LogsQueryServiceVersion}.
     * @return the {@link LogsQueryClientBuilder}.
     */
    public LogsQueryClientBuilder serviceVersion(LogsQueryServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
        return this;
    }

    /**
     * Creates a synchronous client with the configured options in this builder.
     *
     * @return A synchronous {@link LogsQueryClient}.
     */
    public LogsQueryClient buildClient() {
        return new LogsQueryClient(createImplClient());
    }

    /**
     * Creates an asynchronous client with the configured options in this builder.
     *
     * @return An asynchronous {@link LogsQueryAsyncClient}.
     */
    public LogsQueryAsyncClient buildAsyncClient() {
        LOGGER.info("Using service version " + this.serviceVersion);
        return new LogsQueryAsyncClient(createImplClient());
    }

    private AzureLogAnalytics createImplClient() {
        LogsQueryServiceVersion buildServiceVersion = (this.serviceVersion == null)
            ? LogsQueryServiceVersion.getLatest()
            : this.serviceVersion;
        String buildEndpoint = (this.endpoint == null)
            ? "https://api.loganalytics.io/" + buildServiceVersion.getVersion()
            : this.endpoint;

        if (pipeline != null) {
            return new AzureLogAnalytics(pipeline, buildEndpoint);
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
            String localHost = (endpoint != null) ? endpoint : "https://api.loganalytics.io";
            policies.add(new BearerTokenAuthenticationPolicy(tokenCredential, String.format("%s/.default", localHost)));
        }

        policies.addAll(perRetryPolicies);

        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(httpLogOptions));

        HttpPipeline buildPipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .clientOptions(clientOptions)
            .build();

        return new AzureLogAnalytics(buildPipeline, buildEndpoint);
    }
}
