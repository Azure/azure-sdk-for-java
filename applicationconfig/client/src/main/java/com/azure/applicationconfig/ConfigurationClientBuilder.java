// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.applicationconfig;

import com.azure.applicationconfig.models.ConfigurationSetting;
import com.azure.common.http.HttpClient;
import com.azure.common.http.HttpPipeline;
import com.azure.common.http.policy.AsyncCredentialsPolicy;
import com.azure.common.http.policy.HttpLogDetailLevel;
import com.azure.common.http.policy.HttpLoggingPolicy;
import com.azure.common.http.policy.HttpPipelinePolicy;
import com.azure.common.http.policy.RetryPolicy;
import com.azure.common.http.policy.UserAgentPolicy;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Provides configuration options for instances of {@link ConfigurationClient}.
 *
 * @see ConfigurationClient
 * @see ConfigurationClientCredentials
 */
public final class ConfigurationClientBuilder {
    private final List<HttpPipelinePolicy> policies;
    private ConfigurationClientCredentials credentials;
    private URL serviceEndpoint;
    private HttpClient httpClient;
    private HttpLogDetailLevel httpLogDetailLevel;
    private HttpPipeline pipeline;
    private RetryPolicy retryPolicy;
    private String userAgent;

    ConfigurationClientBuilder() {
        userAgent = AzureConfiguration.getUserAgentHeader(AzureConfiguration.NAME, AzureConfiguration.VERSION);
        retryPolicy = new RetryPolicy();
        httpLogDetailLevel = HttpLogDetailLevel.NONE;
        policies = new ArrayList<>();
    }

    /**
     * Creates a {@link ConfigurationClient} based on options set in the Builder. Every time {@code build()} is called,
     * a new instance of {@link ConfigurationClient} is created.
     *
     * <p>
     * If {@link ConfigurationClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * {@link ConfigurationClientBuilder#serviceEndpoint(String) serviceEndpoint} are used to create the
     * {@link ConfigurationClient client}. All other builder settings are ignored.
     * </p>
     *
     * @return A ConfigurationClient with the options set from the builder.
     * @throws NullPointerException If {@code serviceEndpoint} has not been set. This setting is automatically set when
     * {@link ConfigurationClientBuilder#credentials(ConfigurationClientCredentials) credentials} are set through the
     * builder. Or can be set explicitly by calling {@link ConfigurationClientBuilder#serviceEndpoint(String)}.
     * @throws IllegalStateException If {@link ConfigurationClientBuilder#credentials(ConfigurationClientCredentials)}
     * has not been set.
     */
    public ConfigurationClient build() {
        Objects.requireNonNull(serviceEndpoint);

        if (pipeline != null) {
            return new ConfigurationClient(serviceEndpoint, pipeline);
        }

        if (credentials == null) {
            throw new IllegalStateException("'credentials' is required.");
        }

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        policies.add(new UserAgentPolicy(userAgent));
        policies.add(new RequestIdPolicy());
        policies.add(retryPolicy);
        policies.add(new ConfigurationCredentialsPolicy());
        policies.add(new AsyncCredentialsPolicy(credentials));

        policies.addAll(this.policies);

        policies.add(new HttpLoggingPolicy(httpLogDetailLevel));

        HttpPipeline pipeline = httpClient == null
            ? new HttpPipeline(policies)
            : new HttpPipeline(httpClient, policies);

        return new ConfigurationClient(serviceEndpoint, pipeline);
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
        this.serviceEndpoint = new URL(serviceEndpoint);
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
        Objects.requireNonNull(credentials);
        this.credentials = credentials;
        this.serviceEndpoint = credentials.baseUri();
        return this;
    }

    /**
     * Sets the logging level for HTTP requests and responses.
     *
     * @param logLevel The amount of logging output when sending and receiving HTTP requests/responses.
     * @return The updated ConfigurationClientBuilder object.
     */
    public ConfigurationClientBuilder httpLogDetailLevel(HttpLogDetailLevel logLevel) {
        httpLogDetailLevel = logLevel;
        return this;
    }

    /**
     * Adds a policy to the set of existing policies that are executed after
     * {@link com.azure.applicationconfig.ConfigurationClient} required policies.
     *
     * @param policy The retry policy for service requests.
     * @return The updated ConfigurationClientBuilder object.
     * @throws NullPointerException If {@code policy} is {@code null}.
     */
    public ConfigurationClientBuilder addPolicy(HttpPipelinePolicy policy) {
        Objects.requireNonNull(policy);
        policies.add(policy);
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
        Objects.requireNonNull(client);
        this.httpClient = client;
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
        Objects.requireNonNull(pipeline);
        this.pipeline = pipeline;
        return this;
    }
}

