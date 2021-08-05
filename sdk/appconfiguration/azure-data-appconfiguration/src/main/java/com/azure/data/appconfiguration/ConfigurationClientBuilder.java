// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.AddHeadersFromContextPolicy;
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
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.appconfiguration.implementation.ConfigurationClientCredentials;
import com.azure.data.appconfiguration.implementation.ConfigurationCredentialsPolicy;
import com.azure.data.appconfiguration.implementation.SyncTokenPolicy;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.azure.core.util.CoreUtils.getApplicationId;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of {@link
 * ConfigurationClient ConfigurationClients} and {@link ConfigurationAsyncClient ConfigurationAsyncClients}, call {@link
 * #buildClient() buildClient} and {@link #buildAsyncClient() buildAsyncClient} respectively to construct an instance of
 * the desired client.
 *
 * <p>The client needs the service endpoint of the Azure App Configuration store and access credential.
 * {@link #connectionString(String) connectionString(String)} gives the builder the service endpoint and access
 * credential.</p>
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
 * pipeline requires additional setup but allows for finer control on how the {@link ConfigurationClient} and {@link
 * ConfigurationAsyncClient} is built.</p>
 *
 * {@codesnippet com.azure.data.applicationconfig.configurationclient.pipeline.instantiation}
 *
 * @see ConfigurationAsyncClient
 * @see ConfigurationClient
 */
@ServiceClientBuilder(serviceClients = {ConfigurationAsyncClient.class, ConfigurationClient.class})
public final class ConfigurationClientBuilder {
    private static final RetryPolicy DEFAULT_RETRY_POLICY = new RetryPolicy("retry-after-ms", ChronoUnit.MILLIS);

    private static final String CLIENT_NAME;
    private static final String CLIENT_VERSION;
    private static final HttpPipelinePolicy ADD_HEADERS_POLICY;

    static {
        Map<String, String> properties = CoreUtils.getProperties("azure-data-appconfiguration.properties");
        CLIENT_NAME = properties.getOrDefault("name", "UnknownName");
        CLIENT_VERSION = properties.getOrDefault("version", "UnknownVersion");
        ADD_HEADERS_POLICY = new AddHeadersPolicy(new HttpHeaders()
            .put("x-ms-return-client-request-id", "true")
            .put("Content-Type", "application/json")
            .put("Accept", "application/vnd.microsoft.azconfig.kv+json"));
    }

    private final ClientLogger logger = new ClientLogger(ConfigurationClientBuilder.class);
    private final List<HttpPipelinePolicy> perCallPolicies = new ArrayList<>();
    private final List<HttpPipelinePolicy> perRetryPolicies = new ArrayList<>();

    private ClientOptions clientOptions;
    private ConfigurationClientCredentials credential;
    private TokenCredential tokenCredential;

    private String endpoint;
    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions;
    private HttpPipeline pipeline;
    private HttpPipelinePolicy retryPolicy;
    private Configuration configuration;
    private ConfigurationServiceVersion version;

    /**
     * Constructs a new builder used to configure and build {@link ConfigurationClient ConfigurationClients} and {@link
     * ConfigurationAsyncClient ConfigurationAsyncClients}.
     */
    public ConfigurationClientBuilder() {
        httpLogOptions = new HttpLogOptions();
    }

    /**
     * Creates a {@link ConfigurationClient} based on options set in the Builder. Every time {@code buildClient()} is
     * called a new instance of {@link ConfigurationClient} is created.
     * <p>
     * If {@link #pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and {@link #endpoint(String)
     * endpoint} are used to create the {@link ConfigurationClient client}. All other builder settings are ignored.</p>
     *
     * @return A ConfigurationClient with the options set from the builder.
     * @throws NullPointerException If {@code endpoint} has not been set. This setting is automatically set when {@link
     * #connectionString(String) connectionString} is called. Or can be set explicitly by calling {@link
     * #endpoint(String)}.
     * @throws IllegalStateException If {@link #connectionString(String) connectionString} has not been set.
     */
    public ConfigurationClient buildClient() {
        return new ConfigurationClient(buildAsyncClient());
    }

    /**
     * Creates a {@link ConfigurationAsyncClient} based on options set in the Builder. Every time {@code
     * buildAsyncClient()} is called a new instance of {@link ConfigurationAsyncClient} is created.
     * <p>
     * If {@link #pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and {@link #endpoint(String)
     * endpoint} are used to create the {@link ConfigurationAsyncClient client}. All other builder settings are
     * ignored.
     *
     * @return A ConfigurationAsyncClient with the options set from the builder.
     * @throws NullPointerException If {@code endpoint} has not been set. This setting is automatically set when {@link
     * #connectionString(String) connectionString} is called. Or can be set explicitly by calling {@link
     * #endpoint(String)}.
     * @throws IllegalStateException If {@link #connectionString(String) connectionString} has not been set.
     */
    public ConfigurationAsyncClient buildAsyncClient() {
        // Global Env configuration store
        Configuration buildConfiguration = (configuration == null)
            ? Configuration.getGlobalConfiguration()
            : configuration;

        // Service version
        ConfigurationServiceVersion serviceVersion = (version != null)
            ? version
            : ConfigurationServiceVersion.getLatest();

        // Endpoint
        String buildEndpoint = endpoint;
        if (tokenCredential == null) {
            buildEndpoint = getBuildEndpoint();
        }
        // endpoint cannot be null, which is required in request authentication
        Objects.requireNonNull(buildEndpoint, "'Endpoint' is required and can not be null.");

        SyncTokenPolicy syncTokenPolicy = new SyncTokenPolicy();

        // if http pipeline is already defined
        if (pipeline != null) {
            return new ConfigurationAsyncClient(buildEndpoint, pipeline, serviceVersion, syncTokenPolicy);
        }

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new UserAgentPolicy(
            getApplicationId(clientOptions, httpLogOptions), CLIENT_NAME, CLIENT_VERSION, buildConfiguration));
        policies.add(new RequestIdPolicy());
        policies.add(new AddHeadersFromContextPolicy());
        policies.add(ADD_HEADERS_POLICY);

        policies.addAll(perCallPolicies);
        HttpPolicyProviders.addBeforeRetryPolicies(policies);

        policies.add(retryPolicy == null ? DEFAULT_RETRY_POLICY : retryPolicy);

        policies.add(new AddDatePolicy());

        if (tokenCredential != null) {
            // User token based policy
            policies.add(
                new BearerTokenAuthenticationPolicy(tokenCredential, String.format("%s/.default", buildEndpoint)));
        } else if (credential != null) {
            // Use credential based policy
            policies.add(new ConfigurationCredentialsPolicy(credential));
        } else {
            // Throw exception that credential and tokenCredential cannot be null
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Missing credential information while building a client."));
        }
        policies.add(syncTokenPolicy);
        policies.addAll(perRetryPolicies);

        if (clientOptions != null) {
            List<HttpHeader> httpHeaderList = new ArrayList<>();
            clientOptions.getHeaders().forEach(
                header -> httpHeaderList.add(new HttpHeader(header.getName(), header.getValue())));
            policies.add(new AddHeadersPolicy(new HttpHeaders(httpHeaderList)));
        }

        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(httpLogOptions));

        // customized pipeline
        HttpPipeline pipeline = new HttpPipelineBuilder()
                                    .policies(policies.toArray(new HttpPipelinePolicy[0]))
                                    .httpClient(httpClient)
                                    .build();

        return new ConfigurationAsyncClient(buildEndpoint, pipeline, serviceVersion, syncTokenPolicy);
    }

    /**
     * Sets the service endpoint for the Azure App Configuration instance.
     *
     * @param endpoint The URL of the Azure App Configuration instance.
     * @return The updated ConfigurationClientBuilder object.
     * @throws IllegalArgumentException If {@code endpoint} is null or it cannot be parsed into a valid URL.
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
     * Sets the {@link ClientOptions} which enables various options to be set on the client. For example setting an
     * {@code applicationId} using {@link ClientOptions#setApplicationId(String)} to configure
     * the {@link UserAgentPolicy} for telemetry/monitoring purposes.
     *
     * <p>More About <a href="https://azure.github.io/azure-sdk/general_azurecore.html#telemetry-policy">Azure Core: Telemetry policy</a>
     *
     * @param clientOptions {@link ClientOptions}.
     *
     * @return the updated ConfigurationClientBuilder object
     */
    public ConfigurationClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
        return this;
    }

    /**
     * Sets the credential to use when authenticating HTTP requests. Also, sets the {@link #endpoint(String) endpoint}
     * for this ConfigurationClientBuilder.
     *
     * @param connectionString Connection string in the format "endpoint={endpoint_value};id={id_value};
     * secret={secret_value}"
     * @return The updated ConfigurationClientBuilder object.
     * @throws NullPointerException If {@code connectionString} is null.
     * @throws IllegalArgumentException If {@code connectionString} is an empty string, the {@code connectionString}
     * secret is invalid, or the HMAC-SHA256 MAC algorithm cannot be instantiated.
     */
    public ConfigurationClientBuilder connectionString(String connectionString) {
        Objects.requireNonNull(connectionString, "'connectionString' cannot be null.");

        if (connectionString.isEmpty()) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("'connectionString' cannot be an empty string."));
        }

        try {
            this.credential = new ConfigurationClientCredentials(connectionString);
        } catch (InvalidKeyException err) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "The secret contained within the connection string is invalid and cannot instantiate the HMAC-SHA256"
                    + " algorithm.", err));
        } catch (NoSuchAlgorithmException err) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("HMAC-SHA256 MAC algorithm cannot be instantiated.", err));
        }

        this.endpoint = credential.getBaseUri();
        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authenticate HTTP requests.
     *
     * @param tokenCredential TokenCredential used to authenticate HTTP requests.
     * @return The updated ConfigurationClientBuilder object.
     * @throws NullPointerException If {@code credential} is null.
     */
    public ConfigurationClientBuilder credential(TokenCredential tokenCredential) {
        // token credential can not be null value
        Objects.requireNonNull(tokenCredential);
        this.tokenCredential = tokenCredential;
        return this;
    }

    /**
     * Sets the logging configuration for HTTP requests and responses.
     * <p>
     * If logLevel is not provided, default value of {@link HttpLogDetailLevel#NONE} is set.
     *
     * @param logOptions The logging configuration to use when sending and receiving HTTP requests/responses.
     * @return The updated ConfigurationClientBuilder object.
     */
    public ConfigurationClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        httpLogOptions = logOptions;
        return this;
    }

    /**
     * Adds a policy to the set of existing policies.
     *
     * @param policy The policy for service requests.
     * @return The updated ConfigurationClientBuilder object.
     * @throws NullPointerException If {@code policy} is null.
     */
    public ConfigurationClientBuilder addPolicy(HttpPipelinePolicy policy) {
        Objects.requireNonNull(policy, "'policy' cannot be null.");

        if (policy.getPipelinePosition() == HttpPipelinePosition.PER_CALL) {
            perCallPolicies.add(policy);
        } else {
            perRetryPolicies.add(policy);
        }

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
     * <p>
     * If {@code pipeline} is set, all other settings are ignored, aside from {@link
     * ConfigurationClientBuilder#endpoint(String) endpoint} to build {@link ConfigurationAsyncClient} or {@link
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
     * Sets the {@link HttpPipelinePolicy} that is used to retry requests.
     * <p>
     * The default retry policy will be used if not provided {@link ConfigurationClientBuilder#buildAsyncClient()} to
     * build {@link ConfigurationAsyncClient} or {@link ConfigurationClient}.
     *
     * @param retryPolicy The {@link HttpPipelinePolicy} that will be used to retry requests. For example,
     * {@link RetryPolicy} can be used to retry requests.
     *
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

    private String getBuildEndpoint() {
        if (endpoint != null) {
            return endpoint;
        } else if (credential != null) {
            return credential.getBaseUri();
        } else {
            return null;
        }
    }
}

