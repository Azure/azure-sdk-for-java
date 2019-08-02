// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.implementation.annotation.ServiceClientBuilder;
import com.azure.core.util.logging.ClientLogger;
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
 * This class provides a fluent builder API to help aid the configuration and instantiation of
 * {@link ConfigurationClient ConfigurationClients} and {@link ConfigurationAsyncClient ConfigurationAsyncClients},
 * call {@link #buildClient() buildClient} and {@link #buildAsyncClient() buildAsyncClient} respectively to construct
 * an instance of the desired client.
 *
 * <p>The client needs the service endpoint of the Azure App Configuration store and access credential.
 * {@link ConfigurationClientCredentials} gives the builder the service endpoint and access credential it requires to
 * construct a client, set the ConfigurationClientCredentials with
 * {@link #credential(ConfigurationClientCredentials) this}.</p>
 *
 * <p><strong>Instantiating an asynchronous Configuration Client</strong></p>
 *
 * {@codesnippet com.azure.data.applicationconfig.async.configurationclient.instantiation}
 *
 * <p><strong>Instantiating a synchronous Configuration Client</strong></p>
 *
 * {@codesnippet com.azure.data.applicationconfig.configurationclient.instantiation}
 *
 * <p>Another way to construct the client is using a {@link HttpPipeline}. The pipeline gives the client an
 * authenticated way to communicate with the service but it doesn't contain the service endpoint. Set the pipeline with
 * {@link #pipeline(HttpPipeline) this} and set the service endpoint with {@link #endpoint(String) this}. Using a
 * pipeline requires additional setup but allows for finer control on how the {@link ConfigurationClient} and
 * {@link ConfigurationAsyncClient} is built.</p>
 *
 * {@codesnippet com.azure.data.applicationconfig.configurationclient.pipeline.instantiation}
 *
 * @see ConfigurationAsyncClient
 * @see ConfigurationClient
 * @see ConfigurationClientCredentials
 */
@ServiceClientBuilder(serviceClients = ConfigurationClient.class)
public final class ConfigurationClientBuilder {
    // This header tells the server to return the request id in the HTTP response. Useful for correlation with what
    // request was sent.
    private static final String ECHO_REQUEST_ID_HEADER = "x-ms-return-client-request-id";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String CONTENT_TYPE_HEADER_VALUE = "application/json";
    private static final String ACCEPT_HEADER = "Accept";
    private static final String ACCEPT_HEADER_VALUE = "application/vnd.microsoft.azconfig.kv+json";

    private final ClientLogger logger = new ClientLogger(ConfigurationClientBuilder.class);
    private final List<HttpPipelinePolicy> policies;
    private final HttpHeaders headers;

    private ConfigurationClientCredentials credential;
    private URL endpoint;
    private HttpClient httpClient;
    private HttpLogDetailLevel httpLogDetailLevel;
    private HttpPipeline pipeline;
    private RetryPolicy retryPolicy;
    private Configuration configuration;

    /**
     * The constructor with defaults.
     */
    public ConfigurationClientBuilder() {
        retryPolicy = new RetryPolicy();
        httpLogDetailLevel = HttpLogDetailLevel.NONE;
        policies = new ArrayList<>();

        headers = new HttpHeaders()
            .put(ECHO_REQUEST_ID_HEADER, "true")
            .put(CONTENT_TYPE_HEADER, CONTENT_TYPE_HEADER_VALUE)
            .put(ACCEPT_HEADER, ACCEPT_HEADER_VALUE);
    }
    /**
     * Creates a {@link ConfigurationClient} based on options set in the Builder. Every time {@code buildClient()} is
     * called a new instance of {@link ConfigurationClient} is created.
     *
     * <p>
     * If {@link #pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * {@link #endpoint(String) endpoint} are used to create the {@link ConfigurationClient client}. All other builder
     * settings are ignored.</p>
     *
     * @return A ConfigurationClient with the options set from the builder.
     * @throws NullPointerException If {@code endpoint} has not been set. This setting is automatically set when
     * {@link #credential(ConfigurationClientCredentials) credential} are set through the builder. Or can be set
     * explicitly by calling {@link #endpoint(String)}.
     * @throws IllegalStateException If {@link #credential(ConfigurationClientCredentials)} has not been set.
     */
    public ConfigurationClient buildClient() {
        return new ConfigurationClient(buildAsyncClient());
    }

    /**
     * Creates a {@link ConfigurationAsyncClient} based on options set in the Builder. Every time
     * {@code buildAsyncClient()} is called a new instance of {@link ConfigurationAsyncClient} is created.
     *
     * <p>
     * If {@link #pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * {@link #endpoint(String) endpoint} are used to create the {@link ConfigurationAsyncClient client}. All other
     * builder settings are ignored.
     * </p>
     *
     * @return A ConfigurationAsyncClient with the options set from the builder.
     * @throws NullPointerException If {@code endpoint} has not been set. This setting is automatically set when
     * {@link #credential(ConfigurationClientCredentials) credential} are set through the builder. Or can be set
     * explicitly by calling {@link #endpoint(String)}.
     * @throws IllegalStateException If {@link #credential(ConfigurationClientCredentials)} has not been set.
     */
    public ConfigurationAsyncClient buildAsyncClient() {
        Configuration buildConfiguration = (configuration == null) ? ConfigurationManager.getConfiguration().clone() : configuration;
        ConfigurationClientCredentials configurationCredentials = getConfigurationCredentials(buildConfiguration);
        URL buildEndpoint = getBuildEndpoint(configurationCredentials);

        Objects.requireNonNull(buildEndpoint);

        if (pipeline != null) {
            return new ConfigurationAsyncClient(buildEndpoint, pipeline);
        }

        ConfigurationClientCredentials buildCredential = (credential == null) ? configurationCredentials : credential;
        if (buildCredential == null) {
            logger.logAndThrow(new IllegalStateException("'credential' is required."));
        }

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        policies.add(new UserAgentPolicy(AzureConfiguration.NAME, AzureConfiguration.VERSION, buildConfiguration));
        policies.add(new RequestIdPolicy());
        policies.add(new AddHeadersPolicy(headers));
        policies.add(new AddDatePolicy());
        policies.add(new ConfigurationCredentialsPolicy(buildCredential));
        HttpPolicyProviders.addBeforeRetryPolicies(policies);

        policies.add(retryPolicy);

        policies.addAll(this.policies);
        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(httpLogDetailLevel));

        HttpPipeline pipeline = new HttpPipelineBuilder()
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
     * @throws IllegalArgumentException if {@code endpoint} is null or it cannot be parsed into a valid URL.
     */
    public ConfigurationClientBuilder endpoint(String endpoint) {
        try {
            this.endpoint = new URL(endpoint);
        } catch (MalformedURLException ex) {
            logger.logAndThrow(new IllegalArgumentException("'endpoint' must be a valid URL"));
        }

        return this;
    }

    /**
     * Sets the credential to use when authenticating HTTP requests. Also, sets the {@link #endpoint(String) endpoint}
     * for this ConfigurationClientBuilder.
     *
     * @param credential The credential to use for authenticating HTTP requests.
     * @return The updated ConfigurationClientBuilder object.
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public ConfigurationClientBuilder credential(ConfigurationClientCredentials credential) {
        this.credential = Objects.requireNonNull(credential);
        this.endpoint = credential.baseUri();
        return this;
    }

    /**
     * Sets the logging level for HTTP requests and responses.
     *
     * @param logLevel The amount of logging output when sending and receiving HTTP requests/responses.
     * @return The updated ConfigurationClientBuilder object.
     * @throws NullPointerException If {@code logLevel} is {@code null}.
     */
    public ConfigurationClientBuilder httpLogDetailLevel(HttpLogDetailLevel logLevel) {
        httpLogDetailLevel = Objects.requireNonNull(logLevel);
        return this;
    }

    /**
     * Adds a policy to the set of existing policies that are executed after required policies.
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
     */
    public ConfigurationClientBuilder httpClient(HttpClient client) {
        if (this.httpClient != null && client == null) {
            logger.info("HttpClient is being set to 'null' when it was previously configured.");
        }

        this.httpClient = client;
        return this;
    }

    /**
     * Sets the HTTP pipeline to use for the service client.
     *
     * If {@code pipeline} is set, all other settings are ignored, aside from
     * {@link ConfigurationClientBuilder#endpoint(String) endpoint} to build {@link ConfigurationAsyncClient} or {@link ConfigurationClient}.
     *
     * @param pipeline The HTTP pipeline to use for sending service requests and receiving responses.
     * @return The updated ConfigurationClientBuilder object.
     */
    public ConfigurationClientBuilder pipeline(HttpPipeline pipeline) {
        if (this.pipeline != null && pipeline == null) {
            logger.info("HttpPipeline is being set to 'null' when it was previously configured.");
        }

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

