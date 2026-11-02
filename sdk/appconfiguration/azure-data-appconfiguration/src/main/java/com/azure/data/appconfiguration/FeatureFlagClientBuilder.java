// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.client.traits.ConfigurationTrait;
import com.azure.core.client.traits.ConnectionStringTrait;
import com.azure.core.client.traits.EndpointTrait;
import com.azure.core.client.traits.HttpTrait;
import com.azure.core.client.traits.TokenCredentialTrait;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.data.appconfiguration.implementation.SyncTokenPolicy;
import com.azure.data.appconfiguration.models.ConfigurationAudience;

/**
 * Builds clients for the dedicated feature flag resource. These clients do not manage key-value-backed
 * {@link com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting feature flag settings}.
 *
 * <p>For example, authenticate with a connection string:</p>
 * <pre>
 * FeatureFlagClient client = new FeatureFlagClientBuilder()
 *     .connectionString(connectionString)
 *     .buildClient();
 * </pre>
 *
 * <p>A custom pipeline takes precedence over HTTP, authentication, logging, retry, and policy settings.
 * The endpoint and service version still apply. Custom pipelines must supply authentication and any desired policies.</p>
 */
@ServiceClientBuilder(serviceClients = { FeatureFlagClient.class, FeatureFlagAsyncClient.class })
public final class FeatureFlagClientBuilder implements TokenCredentialTrait<FeatureFlagClientBuilder>,
    ConnectionStringTrait<FeatureFlagClientBuilder>, HttpTrait<FeatureFlagClientBuilder>,
    ConfigurationTrait<FeatureFlagClientBuilder>, EndpointTrait<FeatureFlagClientBuilder> {
    private final ConfigurationClientBuilder builder = new ConfigurationClientBuilder();

    /**
     * Creates a builder for dedicated feature flag clients.
     */
    public FeatureFlagClientBuilder() {
    }

    /**
     * Builds a synchronous client.
     *
     * @return a new feature flag client.
     * @throws IllegalArgumentException if the service version does not support feature flag resources.
     * @throws NullPointerException if required connection information is missing.
     */
    public FeatureFlagClient buildClient() {
        SyncTokenPolicy syncTokenPolicy = new SyncTokenPolicy();
        return new FeatureFlagClient(builder.buildFeatureFlagInnerClient(syncTokenPolicy), syncTokenPolicy);
    }

    /**
     * Builds an asynchronous client.
     *
     * @return a new asynchronous feature flag client.
     * @throws IllegalArgumentException if the service version does not support feature flag resources.
     * @throws NullPointerException if required connection information is missing.
     */
    public FeatureFlagAsyncClient buildAsyncClient() {
        SyncTokenPolicy syncTokenPolicy = new SyncTokenPolicy();
        return new FeatureFlagAsyncClient(builder.buildFeatureFlagInnerClient(syncTokenPolicy), syncTokenPolicy);
    }

    /**
     * Sets the service endpoint.
     * @param endpoint the service endpoint.
     * @return this builder.
     */
    @Override
    public FeatureFlagClientBuilder endpoint(String endpoint) {
        builder.endpoint(endpoint);
        return this;
    }

    /**
     * Sets the connection string used for shared-key authentication.
     * @param connectionString the App Configuration connection string.
     * @return this builder.
     */
    @Override
    public FeatureFlagClientBuilder connectionString(String connectionString) {
        builder.connectionString(connectionString);
        return this;
    }

    /**
     * Sets the Microsoft Entra credential.
     * @param credential the token credential.
     * @return this builder.
     */
    @Override
    public FeatureFlagClientBuilder credential(TokenCredential credential) {
        builder.credential(credential);
        return this;
    }

    /**
     * Sets the HTTP transport.
     * @param httpClient the transport.
     * @return this builder.
     */
    @Override
    public FeatureFlagClientBuilder httpClient(HttpClient httpClient) {
        builder.httpClient(httpClient);
        return this;
    }

    /**
     * Sets a custom HTTP pipeline.
     * @param pipeline the pipeline, which must provide authentication and any desired policies.
     * @return this builder.
     */
    @Override
    public FeatureFlagClientBuilder pipeline(HttpPipeline pipeline) {
        builder.pipeline(pipeline);
        return this;
    }

    /**
     * Sets HTTP logging options.
     * @param logOptions the logging options.
     * @return this builder.
     */
    @Override
    public FeatureFlagClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        builder.httpLogOptions(logOptions);
        return this;
    }

    /**
     * Adds an HTTP pipeline policy.
     * @param policy the policy.
     * @return this builder.
     */
    @Override
    public FeatureFlagClientBuilder addPolicy(HttpPipelinePolicy policy) {
        builder.addPolicy(policy);
        return this;
    }

    /**
     * Sets retry options. Mutually exclusive with {@link #retryPolicy(HttpPipelinePolicy)}.
     * @param retryOptions the retry options.
     * @return this builder.
     */
    @Override
    public FeatureFlagClientBuilder retryOptions(RetryOptions retryOptions) {
        builder.retryOptions(retryOptions);
        return this;
    }

    /**
     * Sets the retry policy. Mutually exclusive with {@link #retryOptions(RetryOptions)}.
     * @param retryPolicy the retry policy.
     * @return this builder.
     */
    public FeatureFlagClientBuilder retryPolicy(HttpPipelinePolicy retryPolicy) {
        builder.retryPolicy(retryPolicy);
        return this;
    }

    /**
     * Sets common client options.
     * @param clientOptions the client options.
     * @return this builder.
     */
    @Override
    public FeatureFlagClientBuilder clientOptions(ClientOptions clientOptions) {
        builder.clientOptions(clientOptions);
        return this;
    }

    /**
     * Sets the configuration used when building the client.
     * @param configuration the configuration.
     * @return this builder.
     */
    @Override
    public FeatureFlagClientBuilder configuration(Configuration configuration) {
        builder.configuration(configuration);
        return this;
    }

    /**
     * Sets the service version. Feature flag resources require {@code 2026-05-01-preview} or later.
     * @param version the version, or null to use the latest supported version.
     * @return this builder.
     */
    public FeatureFlagClientBuilder serviceVersion(ConfigurationServiceVersion version) {
        builder.serviceVersion(version);
        return this;
    }

    /**
     * Sets the Microsoft Entra audience. This does not affect shared-key authentication.
     * @param audience the audience.
     * @return this builder.
     */
    public FeatureFlagClientBuilder audience(ConfigurationAudience audience) {
        builder.audience(audience);
        return this;
    }
}
