// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.applicationconfig;

import com.azure.applicationconfig.credentials.ConfigurationClientCredentials;
import com.azure.applicationconfig.models.ConfigurationSetting;
import com.azure.common.http.HttpClient;
import com.azure.common.http.HttpPipeline;
import com.azure.common.http.policy.HttpLogDetailLevel;
import com.azure.common.http.policy.HttpPipelinePolicy;

import java.net.MalformedURLException;

/**
 * Builds instances of {@link ConfigurationClient} based on the configuration options stored in the builder when
 * {@link ConfigurationClientBuilder#build() build} is called. Preparing the builder has two primary options, using
 * {@link ConfigurationClientCredentials} or using a {@link HttpPipeline} and the service endpoint.
 *
 * <p>ConfigurationClientCredentials has the service endpoint and authorization information built into it. Use
 * {@link ConfigurationClientBuilder#credentials(ConfigurationClientCredentials) credentials} to configure the builder.</p>
 *
 * <pre>
 * ConfigurationClient client = ConfigurationClient.builder()
 *     .credentials(new ConfigurationClientCredentials(connectionString))
 *     .build();
 * </pre>
 *
 * <p>HttpPipeline is constructed using the authorization information but doesn't have the service endpoint built into
 * it. Use {@link ConfigurationClientBuilder#pipeline(HttpPipeline) pipeline} and {@link ConfigurationClientBuilder#serviceEndpoint(String) serviceEndpoint}
 * to configure the builder. The HttpPipeline requires additional configuration over using ConfigurationClientCredentials
 * but it allows for finer control on how the ConfigurationClient is built.</p>
 *
 * <pre>
 * ConfigurationClient.builder()
 *     .pipeline(new HttpPipeline(policies))
 *     .serviceEndpoint(serviceEndpoint)
 *     .build();
 * </pre>
 *
 * @see ConfigurationClient
 * @see ConfigurationClientCredentials
 */
public final class ConfigurationClientBuilder {
    private final ConfigurationAsyncClientBuilder builder;

    ConfigurationClientBuilder() {
        builder = ConfigurationAsyncClient.builder();
    }

    /**
     * Creates a {@link ConfigurationClient} based on options set in the Builder. Every time {@code build()} is
     * called, a new instance of {@link ConfigurationClient} is created.
     *
     * <p>
     * If {@link ConfigurationClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * {@link ConfigurationClientBuilder#serviceEndpoint(String) serviceEndpoint} are used to create the
     * {@link ConfigurationClient client}. All other builder settings are ignored.
     * </p>
     *
     * @return A ConfigurationClient with the options set from the builder.
     * @throws NullPointerException If {@code serviceEndpoint} has not been set. This setting is automatically set when
     * {@link ConfigurationClientBuilder#credentials(ConfigurationClientCredentials) credentials} are set through
     * the builder. Or can be set explicitly by calling {@link ConfigurationClientBuilder#serviceEndpoint(String)}.
     * @throws IllegalStateException If {@link ConfigurationClientBuilder#credentials(ConfigurationClientCredentials)}
     * has not been set.
     */
    public ConfigurationClient build() {
        return new ConfigurationClient(builder.build());
    }

    /**
     * Sets the service endpoint for the Azure App Configuration instance.
     *
     * @param serviceEndpoint The URL of the Azure App Configuration instance to send {@link ConfigurationSetting}
     * service requests to and receive responses from.
     * @return The updated ConfigurationClientBuilder object.
     * @throws MalformedURLException if {@code serviceEndpoint} is null or it cannot be parsed into a valid URL.
     */
    public ConfigurationClientBuilder serviceEndpoint(String serviceEndpoint) throws MalformedURLException {
        builder.serviceEndpoint(serviceEndpoint);
        return this;
    }

    /**
     * Sets the credentials to use when authenticating HTTP requests. Also, sets the
     * {@link ConfigurationClientBuilder#serviceEndpoint(String) serviceEndpoint} for this ConfigurationClientBuilder.
     *
     * @param credentials The credentials to use for authenticating HTTP requests.
     * @return The updated ConfigurationClientBuilder object.
     * @throws NullPointerException If {@code credentials} is {@code null}.
     */
    public ConfigurationClientBuilder credentials(ConfigurationClientCredentials credentials) {
        builder.credentials(credentials);
        return this;
    }

    /**
     * Sets the logging level for HTTP requests and responses.
     *
     * @param logLevel The amount of logging output when sending and receiving HTTP requests/responses.
     * @return The updated ConfigurationClientBuilder object.
     */
    public ConfigurationClientBuilder httpLogDetailLevel(HttpLogDetailLevel logLevel) {
        builder.httpLogDetailLevel(logLevel);
        return this;
    }

    /**
     * Adds a policy to the set of existing policies that are executed after
     * {@link ConfigurationClient} required policies.
     *
     * @param policy The retry policy for service requests.
     * @return The updated ConfigurationClientBuilder object.
     * @throws NullPointerException If {@code policy} is {@code null}.
     */
    public ConfigurationClientBuilder addPolicy(HttpPipelinePolicy policy) {
        builder.addPolicy(policy);
        return this;
    }

    /**
     * Sets the HTTP client to use for sending and receiving requests to and from the service.
     *
     * @param client The HTTP client to use for requests.
     * @return The updated ConfigurationClientBuilder object.
     * @throws NullPointerException If {@code client} is {@code null}.
     */
    public ConfigurationClientBuilder httpClient(HttpClient client) {
        builder.httpClient(client);
        return this;
    }

    /**
     * Sets the HTTP pipeline to use for the service client.
     *
     * If {@code pipeline} is set, all other settings are ignored, aside from
     * {@link ConfigurationClientBuilder#serviceEndpoint(String) serviceEndpoint} to build {@link ConfigurationClient}.
     *
     * @param pipeline The HTTP pipeline to use for sending service requests and receiving responses.
     * @return The updated ConfigurationClientBuilder object.
     */
    public ConfigurationClientBuilder pipeline(HttpPipeline pipeline) {
        builder.pipeline(pipeline);
        return this;
    }
}
