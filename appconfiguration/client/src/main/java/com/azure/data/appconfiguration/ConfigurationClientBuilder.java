// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.data.appconfiguration.credentials.ConfigurationClientCredentials;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.policy.ConfigurationCredentialsPolicy;
import com.azure.core.util.configuration.Configuration;
import com.azure.core.util.configuration.ConfigurationManager;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.implementation.http.policy.spi.HttpPolicyProviders;
import com.azure.core.implementation.util.ImplUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of {@link ConfigurationClient}
 * and {@link ConfigurationAsyncClient}, calling {@link ConfigurationClientBuilder#buildSync() buildSync} constructs an
 * instance of ConfigurationClient and calling {@link ConfigurationClientBuilder#buildAsync() buildSync} constructs
 * an instance of ConfigurationAsyncClient.
 *
 * <p>The client needs the service endpoint of the Azure App Configuration store and access credential.
 * {@link ConfigurationClientCredentials} gives the builder the service endpoint and access credential it requires to
 * construct a client, set the ConfigurationClientCredentials with {@link ConfigurationClientBuilder#credential(ConfigurationClientCredentials) this}.</p>
 *
 * <pre>
 * ConfigurationClient client = ConfigurationClient.builder()
 *     .credential(new ConfigurationClientCredentials(connectionString))
 *     .buildSync();
 * </pre>
 *
 * <pre>
 * ConfigurationAsyncClient client = ConfigurationAsyncClient.builder()
 *     .credential(new ConfigurationClientCredentials(connectionString))
 *     .buildAsync();
 * </pre>
 *
 * <p>Another way to construct the client is using a {@link HttpPipeline}. The pipeline gives the client an authenticated
 * way to communicate with the service but it doesn't contain the service endpoint. Set the pipeline with
 * {@link ConfigurationClientBuilder#pipeline(HttpPipeline) this}, additionally set the service endpoint with
 * {@link ConfigurationClientBuilder#endpoint(String) this}. Using a pipeline requires additional setup but
 * allows for finer control on how the ConfigurationAsyncClient it built.</p>
 *
 * <pre>
 * ConfigurationClient.builder()
 *     .pipeline(new HttpPipeline(policies))
 *     .endpoint(endpoint)
 *     .buildSync();
 * </pre>
 *
 * <pre>
 * ConfigurationAsyncClient.builder()
 *     .pipeline(new HttpPipeline(policies))
 *     .endpoint(endpoint)
 *     .buildAsync();
 * </pre>
 *
 * @see ConfigurationClient
 * @see ConfigurationAsyncClient
 * @see ConfigurationClientCredentials
 */
public final class ConfigurationClientBuilder {
    // This header tells the server to return the request id in the HTTP response. Useful for correlation with what
    // request was sent.
    private static final String ECHO_REQUEST_ID_HEADER = "x-ms-return-client-request-id";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String CONTENT_TYPE_HEADER_VALUE = "application/json";
    private static final String ACCEPT_HEADER = "Accept";
    private static final String ACCEPT_HEADER_VALUE = "application/vnd.microsoft.azconfig.kv+json";

    private final List<HttpPipelinePolicy> policies;
    private final HttpHeaders headers;

    private ConfigurationClientCredentials credential;
    private URL endpoint;
    private HttpClient httpClient;
    private HttpLogDetailLevel httpLogDetailLevel;
    private HttpPipeline pipeline;
    private RetryPolicy retryPolicy;
    private Configuration configuration;

    ConfigurationClientBuilder() {
        retryPolicy = new RetryPolicy();
        httpLogDetailLevel = HttpLogDetailLevel.NONE;
        policies = new ArrayList<>();

        headers = new HttpHeaders()
            .put(ECHO_REQUEST_ID_HEADER, "true")
            .put(CONTENT_TYPE_HEADER, CONTENT_TYPE_HEADER_VALUE)
            .put(ACCEPT_HEADER, ACCEPT_HEADER_VALUE);
    }

    /**
     * Creates a {@link ConfigurationClient} based on options set in the Builder. Every time {@code build()} is
     * called, a new instance of {@link ConfigurationClient} is created.
     *
     * <p>
     * If {@link ConfigurationClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * {@link ConfigurationClientBuilder#endpoint(String) endpoint} are used to create the
     * {@link ConfigurationClient client}. All other builder settings are ignored.</p>
     *
     * @return A ConfigurationClient with the options set from the builder.
     * @throws NullPointerException If {@code endpoint} has not been set. This setting is automatically set when
     * {@link ConfigurationClientBuilder#credential(ConfigurationClientCredentials) credential} is set through
     * the builder. Or can be set explicitly by calling {@link ConfigurationClientBuilder#endpoint(String)}.
     * @throws IllegalStateException If {@link ConfigurationClientBuilder#credential(ConfigurationClientCredentials)}
     * has not been set.
     */
    public ConfigurationClient buildSync() {
        return new ConfigurationClient(build());
    }

    /**
     * Creates a {@link ConfigurationAsyncClient} based on options set in the Builder. Every time {@code build()} is
     * called, a new instance of {@link ConfigurationAsyncClient} is created.
     *
     * <p>
     * If {@link ConfigurationClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * {@link ConfigurationClientBuilder#endpoint(String) endpoint} are used to create the
     * {@link ConfigurationAsyncClient client}. All other builder settings are ignored.
     * </p>
     *
     * @return A ConfigurationAsyncClient with the options set from the builder.
     * @throws NullPointerException If {@code endpoint} has not been set. This setting is automatically set when
     * {@link ConfigurationClientBuilder#credential(ConfigurationClientCredentials) credential} is set through
     * the builder. Or can be set explicitly by calling {@link ConfigurationClientBuilder#endpoint(String)}.
     * @throws IllegalStateException If {@link ConfigurationClientBuilder#credential(ConfigurationClientCredentials)}
     * has not been set.
     */
    public ConfigurationAsyncClient buildAsync() {
        return build();
    }

    private ConfigurationAsyncClient build() {
        Configuration buildConfiguration = (configuration == null) ? ConfigurationManager.getConfiguration().clone() : configuration;
        ConfigurationClientCredentials configurationCredentials = getConfigurationCredentials(buildConfiguration);
        URL buildEndpoint = Objects.requireNonNull(getBuildEndpoint(configurationCredentials));

        if (pipeline != null) {
            return new ConfigurationAsyncClient(buildEndpoint, pipeline);
        }

        ConfigurationClientCredentials buildCredentials = (credential == null) ? configurationCredentials : credential;
        if (buildCredentials == null) {
            throw new IllegalStateException("'credential' is required.");
        }

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        policies.add(new UserAgentPolicy(AzureConfiguration.NAME, AzureConfiguration.VERSION, buildConfiguration));
        policies.add(new RequestIdPolicy());
        policies.add(new AddHeadersPolicy(headers));
        policies.add(new AddDatePolicy());
        policies.add(new ConfigurationCredentialsPolicy(buildCredentials));
        HttpPolicyProviders.addBeforeRetryPolicies(policies);

        policies.add(retryPolicy);

        policies.addAll(this.policies);
        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(httpLogDetailLevel));

        HttpPipeline pipeline = HttpPipeline.builder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();

        return new ConfigurationAsyncClient(buildEndpoint, pipeline);
    }

    /**
     * Sets the service endpoint for the Azure App Configuration instance.
     *
     * @param endpoint The URL of the Azure App Configuration instance to send {@link ConfigurationSetting}
     * service requests to and receive responses from.
     * @return The updated ConfigurationClientBuilder object.
     * @throws MalformedURLException if {@code endpoint} is null or it cannot be parsed into a valid URL.
     */
    public ConfigurationClientBuilder endpoint(String endpoint) throws MalformedURLException {
        this.endpoint = new URL(endpoint);
        return this;
    }

    /**
     * Sets the credential to use when authenticating HTTP requests. Also, sets the
     * {@link ConfigurationClientBuilder#endpoint(String) endpoint} for this ConfigurationClientBuilder.
     *
     * @param credential The credential to use for authenticating HTTP requests.
     * @return The updated ConfigurationlientBuilder object.
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public ConfigurationClientBuilder credential(ConfigurationClientCredentials credential) {
        Objects.requireNonNull(credential);
        this.credential = credential;
        this.endpoint = credential.baseUri();
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
     * Adds a policy to the set of existing policies that are executed after the required policies.
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
     * {@link ConfigurationClientBuilder#endpoint(String) endpoint} when building a Azure App Configuration client.
     *
     * @param pipeline The HTTP pipeline to use for sending service requests and receiving responses.
     * @return The updated ConfigurationClientBuilder object.
     * @throws NullPointerException If {@code pipeline} is {@code null}.
     */
    public ConfigurationClientBuilder pipeline(HttpPipeline pipeline) {
        Objects.requireNonNull(pipeline);
        this.pipeline = pipeline;
        return this;
    }

    /**
     * Sets the configuration store that is used during construction of the service client.
     *
     * The default configuration store is a clone of the {@link ConfigurationManager#getConfiguration() global
     * configuration store}, use {@link Configuration#NONE} to bypass using configuration settings during construction.
     *
     * @param configuration The configuration store used to
     * @return The updated ConfigurationClientBuilder object.
     */
    public ConfigurationClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    private ConfigurationClientCredentials getConfigurationCredentials(Configuration configuration) {
        String connectionString = configuration.get("AZURE_APPCONFIG_CONNECTION_STRING");
        if (ImplUtils.isNullOrEmpty(connectionString)) {
            return credential;
        }

        try {
            return new ConfigurationClientCredentials(connectionString);
        } catch (InvalidKeyException | NoSuchAlgorithmException ex) {
            return null;
        }
    }

    private URL getBuildEndpoint(ConfigurationClientCredentials buildCredentials) {
        if (endpoint != null) {
            return endpoint;
        } else if (buildCredentials != null) {
            return buildCredentials.baseUri();
        } else {
            return null;
        }
    }
}

