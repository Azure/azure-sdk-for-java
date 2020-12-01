// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.synapse.spark;

import com.azure.analytics.synapse.spark.implementation.SparkClientImpl;
import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A builder for creating a new instance of the SparkClient type.
 */
@ServiceClientBuilder(serviceClients = {SparkBatchAsyncClient.class, SparkBatchClient.class})
public final class SparkClientBuilder {
    private static final String SYNAPSE_PROPERTIES = "azure-analytics-synapse-spark.properties";
    private static final String NAME = "name";
    private static final String VERSION = "version";
    private static final RetryPolicy DEFAULT_RETRY_POLICY = new RetryPolicy("retry-after-ms", ChronoUnit.MILLIS);
    static final String DEFAULT_SCOPE = "https://dev.azuresynapse.net/.default";

    private final ClientLogger logger = new ClientLogger(SparkClientBuilder.class);
    private final List<HttpPipelinePolicy> policies;
    private final HttpHeaders headers;
    private final String clientName;
    private final String clientVersion;

    private Configuration configuration;
    private String endpoint;
    private String sparkPoolName;
    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions;
    private HttpPipeline httpPipeline;
    private RetryPolicy retryPolicy;
    private TokenCredential tokenCredential;
    private SparkServiceVersion version;

    /**
     * The constructor with defaults.
     */
    public SparkClientBuilder() {
        policies = new ArrayList<>();
        httpLogOptions = new HttpLogOptions();

        Map<String, String> properties = CoreUtils.getProperties(SYNAPSE_PROPERTIES);
        clientName = properties.getOrDefault(NAME, "UnknownName");
        clientVersion = properties.getOrDefault(VERSION, "UnknownVersion");

        headers = new HttpHeaders();
    }

    /**
     * Builds an instance of SparkClientImpl with the provided parameters.
     *
     * @return an instance of SparkClientImpl.
     */
    private SparkClientImpl buildInnerClient() {
        // Global Env configuration store
        final Configuration buildConfiguration = (configuration == null)
            ? Configuration.getGlobalConfiguration().clone() : configuration;
        // Service Version
        final SparkServiceVersion serviceVersion =
            version != null ? version : SparkServiceVersion.getLatest();

        // Endpoint cannot be null, which is required in request authentication
        Objects.requireNonNull(endpoint, "'Endpoint' is required and can not be null.");

        HttpPipeline pipeline = httpPipeline;
        // Create a default Pipeline if it is not given
        if (pipeline == null) {
            // Closest to API goes first, closest to wire goes last.
            final List<HttpPipelinePolicy> policies = new ArrayList<>();

            policies.add(new UserAgentPolicy(httpLogOptions.getApplicationId(), clientName, clientVersion,
                buildConfiguration));
            policies.add(new RequestIdPolicy());
            policies.add(new AddHeadersPolicy(headers));

            HttpPolicyProviders.addBeforeRetryPolicies(policies);

            policies.add(retryPolicy == null ? DEFAULT_RETRY_POLICY : retryPolicy);

            policies.add(new AddDatePolicy());
            // Authentications
            if (tokenCredential != null) {
                // User token based policy
                policies.add(new BearerTokenAuthenticationPolicy(tokenCredential, DEFAULT_SCOPE));
            } else {
                // Throw exception that credential and tokenCredential cannot be null
                throw logger.logExceptionAsError(
                    new IllegalArgumentException("Missing credential information while building a client."));
            }

            policies.addAll(this.policies);
            HttpPolicyProviders.addAfterRetryPolicies(policies);

            policies.add(new HttpLoggingPolicy(httpLogOptions));

            pipeline = new HttpPipelineBuilder()
                .policies(policies.toArray(new HttpPipelinePolicy[0]))
                .httpClient(httpClient)
                .build();
        }

        return new SparkClientImpl(pipeline, endpoint, serviceVersion.getVersion(), sparkPoolName);
    }

    /**
     * Sets the service endpoint for the Azure Synapse Analytics instance.
     *
     * @param endpoint The URL of the Azure Synapse Analytics instance service requests to and receive responses from.
     * @return The updated {@link SparkClientBuilder} object.
     * @throws NullPointerException if {@code endpoint} is null
     * @throws IllegalArgumentException if {@code endpoint} cannot be parsed into a valid URL.
     */
    public SparkClientBuilder endpoint(String endpoint) {
        Objects.requireNonNull(endpoint, "'endpoint' cannot be null.");

        try {
            new URL(endpoint);
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("'endpoint' must be a valid URL.", ex));
        }

        if (endpoint.endsWith("/")) {
            this.endpoint = endpoint.substring(0, endpoint.length() - 1);
        } else {
            this.endpoint = endpoint;
        }

        return this;
    }

    /**
     * Sets the Spark pool name used for Spark job operations.
     *
     * @param sparkPoolName Spark pool name
     * @return The updated {@link SparkClientBuilder} object.
     */
    public SparkClientBuilder sparkPoolName(String sparkPoolName) {
        this.sparkPoolName = sparkPoolName;
        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authenticate HTTP requests.
     *
     * @param tokenCredential {@link TokenCredential} used to authenticate HTTP requests.
     * @return The updated {@link SparkClientBuilder} object.
     * @throws NullPointerException If {@code tokenCredential} is {@code null}.
     */
    public SparkClientBuilder credential(TokenCredential tokenCredential) {
        Objects.requireNonNull(tokenCredential, "'tokenCredential' cannot be null.");
        this.tokenCredential = tokenCredential;
        return this;
    }

    /**
     * Sets the logging configuration for HTTP requests and responses.
     *
     * <p> If logLevel is not provided, default value of {@link HttpLogDetailLevel#NONE} is set. </p>
     *
     * @param logOptions The logging configuration to use when sending and receiving HTTP requests/responses.
     * @return The updated {@link SparkClientBuilder} object.
     */
    public SparkClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        this.httpLogOptions = logOptions;
        return this;
    }

    /**
     * Adds a policy to the set of existing policies that are executed after required policies.
     *
     * @param policy The retry policy for service requests.
     * @return The updated {@link SparkClientBuilder} object.
     * @throws NullPointerException If {@code policy} is {@code null}.
     */
    public SparkClientBuilder addPolicy(HttpPipelinePolicy policy) {
        policies.add(Objects.requireNonNull(policy, "'policy' cannot be null."));
        return this;
    }

    /**
     * Sets the HTTP client to use for sending and receiving requests to and from the service.
     *
     * @param client The HTTP client to use for requests.
     * @return The updated {@link SparkClientBuilder} object.
     */
    public SparkClientBuilder httpClient(HttpClient client) {
        if (this.httpClient != null && client == null) {
            logger.info("HttpClient is being set to 'null' when it was previously configured.");
        }

        this.httpClient = client;
        return this;
    }

    /**
     * Sets the HTTP pipeline to use for the service client.
     * <p>
     * If {@code pipeline} is set, all other settings are ignored, aside from {@link
     * SparkClientBuilder#endpoint(String) endpoint}
     *
     * @param httpPipeline The HTTP pipeline to use for sending service requests and receiving responses.
     * @return The updated {@link SparkClientBuilder} object.
     */
    public SparkClientBuilder pipeline(HttpPipeline httpPipeline) {
        if (this.httpPipeline != null && httpPipeline == null) {
            logger.info("HttpPipeline is being set to 'null' when it was previously configured.");
        }

        this.httpPipeline = httpPipeline;
        return this;
    }

    /**
     * Sets the configuration store that is used during construction of the service client.
     * <p>
     * The default configuration store is a clone of the {@link Configuration#getGlobalConfiguration() global
     * configuration store}, use {@link Configuration#NONE} to bypass using configuration settings during construction.
     *
     * @param configuration The configuration store used to
     * @return The updated {@link SparkClientBuilder} object.
     */
    public SparkClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the {@link RetryPolicy} that is used when each request is sent.
     *
     * @param retryPolicy user's retry policy applied to each request.
     * @return The updated {@link SparkClientBuilder} object.
     */
    public SparkClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
        return this;
    }

    /**
     * Sets the {@link SparkServiceVersion} that is used when making API requests.
     * <p>
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version the client library will have the result of potentially moving to a newer service version.
     *
     * @param version {@link SparkServiceVersion} of the service to be used when making requests.
     * @return The updated {@link SparkClientBuilder} object.
     */
    public SparkClientBuilder serviceVersion(SparkServiceVersion version) {
        this.version = version;
        return this;
    }

    /**
     * Builds an instance of SparkBatchAsyncClient async client.
     *
     * @return an instance of SparkBatchAsyncClient.
     */
    public SparkBatchAsyncClient buildSparkBatchAsyncClient() {
        return new SparkBatchAsyncClient(buildInnerClient().getSparkBatches());
    }

    /**
     * Builds an instance of SparkSessionAsyncClient async client.
     *
     * @return an instance of SparkSessionAsyncClient.
     */
    public SparkSessionAsyncClient buildSparkSessionAsyncClient() {
        return new SparkSessionAsyncClient(buildInnerClient().getSparkSessions());
    }

    /**
     * Builds an instance of SparkBatchClient sync client.
     *
     * @return an instance of SparkBatchClient.
     */
    public SparkBatchClient buildSparkBatchClient() {
        return new SparkBatchClient(buildInnerClient().getSparkBatches());
    }

    /**
     * Builds an instance of SparkSessionClient sync client.
     *
     * @return an instance of SparkSessionClient.
     */
    public SparkSessionClient buildSparkSessionClient() {
        return new SparkSessionClient(buildInnerClient().getSparkSessions());
    }
}
