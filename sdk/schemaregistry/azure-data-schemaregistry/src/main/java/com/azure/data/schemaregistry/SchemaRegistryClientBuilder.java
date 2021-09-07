// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

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
import com.azure.data.schemaregistry.implementation.AzureSchemaRegistry;
import com.azure.data.schemaregistry.implementation.AzureSchemaRegistryBuilder;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Function;

/**
 * Fluent builder for interacting with the Schema Registry service via {@link SchemaRegistryAsyncClient} and
 * {@link SchemaRegistryClient}.  To build the client, the builder requires the service endpoint of the Schema Registry
 * and an Azure AD credential.
 *
 * <p><strong>Instantiating the client</strong></p>
 * {@codesnippet com.azure.data.schemaregistry.schemaregistryclient.instantiation}
 *
 * <p><strong>Instantiating the async client</strong></p>
 * {@codesnippet com.azure.data.schemaregistry.schemaregistryasyncclient.instantiation}
 *
 * <p><strong>Instantiating with custom retry policy and HTTP log options</strong></p>
 * {@codesnippet com.azure.data.schemaregistry.schemaregistryasyncclient.retrypolicy.instantiation}
 */
@ServiceClientBuilder(serviceClients = SchemaRegistryAsyncClient.class)
public class SchemaRegistryClientBuilder {
    static final int MAX_SCHEMA_MAP_SIZE_DEFAULT = 1000;
    static final int MAX_SCHEMA_MAP_SIZE_MINIMUM = 10;

    private final ClientLogger logger = new ClientLogger(SchemaRegistryClientBuilder.class);

    private static final String DEFAULT_SCOPE = "https://eventhubs.azure.net/.default";
    private static final String CLIENT_PROPERTIES = "azure-data-schemaregistry-client.properties";
    private static final String NAME = "name";
    private static final String VERSION = "version";
    private static final RetryPolicy DEFAULT_RETRY_POLICY = new RetryPolicy("retry-after-ms", ChronoUnit.MILLIS);
    private static final AddHeadersPolicy API_HEADER_POLICY = new AddHeadersPolicy(new HttpHeaders()
        .set("api-version", "2020-09-01-preview"));

    private final ConcurrentSkipListMap<String, Function<String, Object>> typeParserMap;

    private final List<HttpPipelinePolicy> perCallPolicies = new ArrayList<>();
    private final List<HttpPipelinePolicy> perRetryPolicies = new ArrayList<>();

    private final String clientName;
    private final String clientVersion;

    private String endpoint;
    private String host;
    private HttpClient httpClient;
    private Integer maxSchemaMapSize;
    private TokenCredential credential;
    private ClientOptions clientOptions;
    private HttpLogOptions httpLogOptions;
    private HttpPipeline httpPipeline;
    private RetryPolicy retryPolicy;
    private Configuration configuration;

    /**
     * Constructor for CachedSchemaRegistryClientBuilder.  Supplies client defaults.
     */
    public SchemaRegistryClientBuilder() {
        this.httpLogOptions = new HttpLogOptions();
        this.maxSchemaMapSize = null;
        this.typeParserMap = new ConcurrentSkipListMap<>(String.CASE_INSENSITIVE_ORDER);
        this.httpClient = null;
        this.credential = null;
        this.retryPolicy = new RetryPolicy("retry-after-ms", ChronoUnit.MILLIS);

        Map<String, String> properties = CoreUtils.getProperties(CLIENT_PROPERTIES);
        clientName = properties.getOrDefault(NAME, "UnknownName");
        clientVersion = properties.getOrDefault(VERSION, "UnknownVersion");
    }

    /**
     * Sets the service endpoint for the Azure Schema Registry instance.
     *
     * @param endpoint The URL of the Azure Schema Registry instance
     * @return The updated {@link SchemaRegistryClientBuilder} object.
     * @throws NullPointerException if {@code endpoint} is null
     * @throws IllegalArgumentException if {@code endpoint} cannot be parsed into a valid URL
     */
    public SchemaRegistryClientBuilder endpoint(String endpoint) {
        Objects.requireNonNull(endpoint, "'endpoint' cannot be null.");

        try {
            URL url = new URL(endpoint);
            this.host = url.getHost();
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsWarning(
                new IllegalArgumentException("'endpoint' must be a valid URL.", ex));
        }

        if (endpoint.endsWith("/")) {
            this.endpoint = endpoint.substring(0, endpoint.length() - 1);
        } else {
            this.endpoint = endpoint;
        }

        return this;
    }

    /**
     * Sets schema cache size limit.  If limit is exceeded on any cache, all caches are recycled.
     *
     * @param maxCacheSize max size for internal schema caches in {@link SchemaRegistryAsyncClient}
     * @return The updated {@link SchemaRegistryClientBuilder} object.
     * @throws IllegalArgumentException on invalid maxCacheSize value
     */
    SchemaRegistryClientBuilder maxCacheSize(int maxCacheSize) {
        if (maxCacheSize < MAX_SCHEMA_MAP_SIZE_MINIMUM) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                String.format("Schema map size must be greater than %s entries",
                    MAX_SCHEMA_MAP_SIZE_MINIMUM)));
        }

        this.maxSchemaMapSize = maxCacheSize;
        return this;
    }

    /**
     * Sets the HTTP client to use for sending and receiving requests to and from the service.
     *
     * @param httpClient The HTTP client to use for requests.
     * @return The updated {@link SchemaRegistryClientBuilder} object.
     */
    public SchemaRegistryClientBuilder httpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    /**
     * Sets the HTTP pipeline to use for the service client.
     * <p>
     * If {@code pipeline} is set, all other HTTP settings are ignored to build {@link SchemaRegistryAsyncClient}.
     *
     * @param httpPipeline The HTTP pipeline to use for sending service requests and receiving responses.
     * @return The updated {@link SchemaRegistryClientBuilder} object.
     */
    public SchemaRegistryClientBuilder pipeline(HttpPipeline httpPipeline) {
        if (this.httpPipeline != null && httpPipeline == null) {
            logger.info("HttpPipeline is being set to 'null' when it was previously configured.");
        }

        this.httpPipeline = httpPipeline;
        return this;
    }

    /**
     * Sets the configuration store that is used during construction of the service client.
     *
     * The default configuration store is a clone of the {@link Configuration#getGlobalConfiguration() global
     * configuration store}, use {@link Configuration#NONE} to bypass using configuration settings during construction.
     *
     * @param configuration The configuration store used to
     * @return The updated SchemaRegistryClientBuilder object.
     */
    public SchemaRegistryClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the {@link TokenCredential} to use when authenticating HTTP requests for this {@link
     * SchemaRegistryAsyncClient}.
     *
     * @param credential {@link TokenCredential}
     * @return The updated {@link SchemaRegistryClientBuilder} object.
     * @throws NullPointerException If {@code credential} is {@code null}
     */
    public SchemaRegistryClientBuilder credential(TokenCredential credential) {
        this.credential = Objects.requireNonNull(credential, "'credential' cannot be null.");
        return this;
    }

    /**
     * Sets the {@link ClientOptions} which enables various options to be set on the client. For example setting an
     * {@code applicationId} using {@link ClientOptions#setApplicationId(String)} to configure the {@link
     * UserAgentPolicy} for telemetry/monitoring purposes.
     *
     * <p>More About <a href="https://azure.github.io/azure-sdk/general_azurecore.html#telemetry-policy">Azure Core:
     * Telemetry policy</a>
     *
     * @param clientOptions {@link ClientOptions}.
     * @return The updated SchemaRegistryClientBuilder object.
     */
    public SchemaRegistryClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
        return this;
    }

    /**
     * Sets the logging configuration for HTTP requests and responses.
     *
     * <p> If logLevel is not provided, default value of {@link HttpLogDetailLevel#NONE} is set. </p>
     *
     * @param logOptions The logging configuration to use when sending and receiving HTTP requests/responses.
     * @return The updated {@link SchemaRegistryClientBuilder} object.
     */
    public SchemaRegistryClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        this.httpLogOptions = logOptions;
        return this;
    }

    /**
     * Sets the {@link RetryPolicy} that is used when each request is sent.
     * <p>
     * The default retry policy will be used if not provided to build {@link SchemaRegistryAsyncClient} .
     *
     * @param retryPolicy user's retry policy applied to each request.
     * @return The updated {@link SchemaRegistryClientBuilder} object.
     */
    public SchemaRegistryClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
        return this;
    }

    /**
     * Adds a policy to the set of existing policies that are executed after required policies.
     *
     * @param policy The retry policy for service requests.
     * @return The updated {@link SchemaRegistryClientBuilder} object.
     * @throws NullPointerException If {@code policy} is {@code null}.
     */
    public SchemaRegistryClientBuilder addPolicy(HttpPipelinePolicy policy) {
        Objects.requireNonNull(policy, "'policy' cannot be null.");

        if (policy.getPipelinePosition() == HttpPipelinePosition.PER_CALL) {
            perCallPolicies.add(policy);
        } else {
            perRetryPolicies.add(policy);
        }

        return this;
    }

    /**
     * Creates a {@link SchemaRegistryAsyncClient} based on options set in the builder. Every time {@code buildClient()}
     * is called a new instance of {@link SchemaRegistryAsyncClient} is created.
     *
     * If {@link #pipeline(HttpPipeline) pipeline} is set, then all HTTP pipeline related settings are ignored.
     *
     * @return A {@link SchemaRegistryAsyncClient} with the options set from the builder.
     * @throws NullPointerException if {@link #endpoint(String) endpoint} and {@link #credential(TokenCredential)
     * credential} are not set.
     */
    public SchemaRegistryAsyncClient buildAsyncClient() {
        Objects.requireNonNull(credential,
            "'credential' cannot be null and must be set via builder.credential(TokenCredential)");
        Objects.requireNonNull(endpoint, "'endpoint' cannot be null and must be set in the builder.endpoint(String)");

        Configuration buildConfiguration = (configuration == null)
            ? Configuration.getGlobalConfiguration()
            : configuration;

        HttpPipeline buildPipeline = this.httpPipeline;
        // Create a default Pipeline if it is not given
        if (buildPipeline == null) {
            // Closest to API goes first, closest to wire goes last.
            final List<HttpPipelinePolicy> policies = new ArrayList<>();

            policies.add(new UserAgentPolicy(CoreUtils.getApplicationId(clientOptions, httpLogOptions), clientName,
                clientVersion, buildConfiguration));
            policies.add(new RequestIdPolicy());
            policies.add(new AddHeadersFromContextPolicy());
            policies.add(API_HEADER_POLICY);

            policies.addAll(perCallPolicies);
            HttpPolicyProviders.addBeforeRetryPolicies(policies);

            policies.add(retryPolicy == null ? DEFAULT_RETRY_POLICY : retryPolicy);

            policies.add(new AddDatePolicy());
            policies.add(new BearerTokenAuthenticationPolicy(credential, DEFAULT_SCOPE));

            policies.addAll(perRetryPolicies);

            if (clientOptions != null) {
                List<HttpHeader> clientOptionsHeaders = new ArrayList<>();
                clientOptions.getHeaders()
                    .forEach(header -> clientOptionsHeaders.add(new HttpHeader(header.getName(), header.getValue())));

                if (!CoreUtils.isNullOrEmpty(clientOptionsHeaders)) {
                    policies.add(new AddHeadersPolicy(new HttpHeaders(clientOptionsHeaders)));
                }
            }

            HttpPolicyProviders.addAfterRetryPolicies(policies);

            policies.add(new HttpLoggingPolicy(httpLogOptions));

            buildPipeline = new HttpPipelineBuilder()
                .policies(policies.toArray(new HttpPipelinePolicy[0]))
                .httpClient(httpClient)
                .clientOptions(clientOptions)
                .build();
        }

        AzureSchemaRegistry restService = new AzureSchemaRegistryBuilder()
            .endpoint(host)
            .pipeline(buildPipeline)
            .buildClient();

        int buildMaxSchemaMapSize = (maxSchemaMapSize == null)
            ? MAX_SCHEMA_MAP_SIZE_DEFAULT
            : maxSchemaMapSize;

        return new SchemaRegistryAsyncClient(restService, buildMaxSchemaMapSize, typeParserMap);
    }

    /**
     * Creates synchronous {@link SchemaRegistryClient} instance. See async builder method for options validation.
     *
     * @return {@link SchemaRegistryClient} with the options set from the builder.
     * @throws NullPointerException if {@link #endpoint(String) endpoint} and {@link #credential(TokenCredential)
     * credential} are not set.
     */
    public SchemaRegistryClient buildClient() {
        return new SchemaRegistryClient(this.buildAsyncClient());
    }
}
