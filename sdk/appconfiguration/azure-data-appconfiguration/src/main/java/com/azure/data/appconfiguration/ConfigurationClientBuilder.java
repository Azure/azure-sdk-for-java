// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.core.util.Configuration;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.GeneralUtils;

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
 * {@link #connectionString(String) this}.</p>
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
    private String endpoint;
    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions;
    private HttpPipeline pipeline;
    private HttpPipelinePolicy retryPolicy;
    private Configuration configuration;
    private ConfigurationServiceVersion version;

    /**
     * The constructor with defaults.
     */
    public ConfigurationClientBuilder() {
        policies = new ArrayList<>();
        httpLogOptions = new HttpLogOptions();

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
     *     {@link #connectionString(String) connectionString} is called. Or can be set
     *     explicitly by calling {@link #endpoint(String)}.
     * @throws IllegalStateException If {@link #connectionString(String) connectionString} has not been set.
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
     *     {@link #connectionString(String) connectionString} is called. Or can be set
     *     explicitly by calling {@link #endpoint(String)}.
     * @throws IllegalStateException If {@link #connectionString(String) connectionString} has not been set.
     */
    public ConfigurationAsyncClient buildAsyncClient() {
        Configuration buildConfiguration =
            (configuration == null) ? Configuration.getGlobalConfiguration().clone() : configuration;
        ConfigurationClientCredentials configurationCredentials = getConfigurationCredentials(buildConfiguration);
        String buildEndpoint = getBuildEndpoint(configurationCredentials);

        Objects.requireNonNull(buildEndpoint);
        ConfigurationServiceVersion serviceVersion = version != null
            ? version : ConfigurationServiceVersion.getLatest();

        if (pipeline != null) {
            return new ConfigurationAsyncClient(buildEndpoint, pipeline, serviceVersion);
        }

        ConfigurationClientCredentials buildCredential = (credential == null) ? configurationCredentials : credential;
        if (buildCredential == null) {
            throw logger.logExceptionAsWarning(new IllegalStateException("'credential' is required."));
        }

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        policies.add(new UserAgentPolicy(AzureConfiguration.NAME, AzureConfiguration.VERSION, buildConfiguration,
            serviceVersion));
        policies.add(new RequestIdPolicy());
        policies.add(new AddHeadersPolicy(headers));
        policies.add(new AddDatePolicy());
        policies.add(new ConfigurationCredentialsPolicy(buildCredential));
        HttpPolicyProviders.addBeforeRetryPolicies(policies);

        policies.add(retryPolicy == null ? new RetryPolicy() : retryPolicy);

        policies.addAll(this.policies);
        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(httpLogOptions));

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();

        return new ConfigurationAsyncClient(buildEndpoint, pipeline, serviceVersion);
    }

    /**
     * Sets the service endpoint for the Azure App Configuration instance.
     *
     * @param endpoint The URL of the Azure App Configuration instance to send {@link ConfigurationSetting}
     *     service requests to and receive responses from.
     * @return The updated ConfigurationClientBuilder object.
     * @throws IllegalArgumentException if {@code endpoint} is null or it cannot be parsed into a valid URL.
     */
    public ConfigurationClientBuilder endpoint(String endpoint) {
        try {
            new URL(endpoint);
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("'endpoint' must be a valid URL"));
        }
        this.endpoint = endpoint;
        return this;
    }

    /**
     * Sets the credential to use when authenticating HTTP requests. Also, sets the {@link #endpoint(String) endpoint}
     * for this ConfigurationClientBuilder.
     *
     * @param connectionString Connection string in the format "endpoint={endpoint_value};id={id_value};
     * secret={secret_value}"
     * @return The updated ConfigurationClientBuilder object.
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public ConfigurationClientBuilder connectionString(String connectionString) {
        Objects.requireNonNull(connectionString);

        try {
            this.credential = new ConfigurationClientCredentials(connectionString);
        } catch (InvalidKeyException err) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                    "The secret is invalid and cannot instantiate the HMAC-SHA256 algorithm.", err));
        } catch (NoSuchAlgorithmException err) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("HMAC-SHA256 MAC algorithm cannot be instantiated.", err));
        }

        this.endpoint = credential.getBaseUri();

        return this;
    }

    /**
     * Sets the logging configuration for HTTP requests and responses.
     *
     * <p> If logLevel is not provided, default value of {@link HttpLogDetailLevel#NONE} is set.</p>
     *
     * @param logOptions The logging configuration to use when sending and receiving HTTP requests/responses.
     * @return The updated ConfigurationClientBuilder object.
     */
    public ConfigurationClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        httpLogOptions = logOptions;
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
     * {@link ConfigurationClientBuilder#endpoint(String) endpoint} to build {@link ConfigurationAsyncClient} or {@link
     * ConfigurationClient}.
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
     * The default configuration store is a clone of the {@link Configuration#getGlobalConfiguration() global
     * configuration store}, use {@link Configuration#NONE} to bypass using configuration settings during construction.
     *
     * @param configuration The configuration store used to
     * @return The updated ConfigurationClientBuilder object.
     */
    public ConfigurationClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the {@link HttpPipelinePolicy} that is used when each request is sent.
     *
     * The default retry policy will be used if not provided {@link ConfigurationClientBuilder#buildAsyncClient()}
     * to build {@link ConfigurationAsyncClient} or {@link ConfigurationClient}.
     * @param retryPolicy user's retry policy applied to each request.
     * @return The updated ConfigurationClientBuilder object.
     */
    public ConfigurationClientBuilder retryPolicy(HttpPipelinePolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
        return this;
    }

    /**
     * Sets the {@link ConfigurationServiceVersion} that is used when making API requests.
     * <p>
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version the client library will have the result of potentially moving to a newer service version.
     *
     * @param version {@link ConfigurationServiceVersion} of the service to be used when making requests.
     * @return The updated ConfigurationClientBuilder object.
     */
    public ConfigurationClientBuilder serviceVersion(ConfigurationServiceVersion version) {
        this.version = version;
        return this;
    }

    private ConfigurationClientCredentials getConfigurationCredentials(Configuration configuration) {
        String connectionString = configuration.get("AZURE_APPCONFIG_CONNECTION_STRING");
        if (GeneralUtils.isNullOrEmpty(connectionString)) {
            return credential;
        }

        try {
            return new ConfigurationClientCredentials(connectionString);
        } catch (InvalidKeyException | NoSuchAlgorithmException ex) {
            return null;
        }
    }

    private String getBuildEndpoint(ConfigurationClientCredentials buildCredentials) {
        if (endpoint != null) {
            return endpoint;
        } else if (buildCredentials != null) {
            return buildCredentials.getBaseUri();
        } else {
            return null;
        }
    }
}

