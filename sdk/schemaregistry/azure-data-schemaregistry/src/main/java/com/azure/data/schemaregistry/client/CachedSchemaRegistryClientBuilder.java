// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.client;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AddDatePolicy;
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
import com.azure.data.schemaregistry.Codec;
import com.azure.data.schemaregistry.client.implementation.AzureSchemaRegistryRestService;
import com.azure.data.schemaregistry.client.implementation.AzureSchemaRegistryRestServiceClientBuilder;

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
 * Builder implementation for {@link CachedSchemaRegistryClient}.
 */
@ServiceClientBuilder(serviceClients = CachedSchemaRegistryClient.class)
public class CachedSchemaRegistryClientBuilder {
    private final ClientLogger logger = new ClientLogger(CachedSchemaRegistryClientBuilder.class);

    private static final String DEFAULT_SCOPE = "https://eventhubs.azure.com/.default";
    private static final String CLIENT_PROPERTIES = "azure-data-schemaregistry-client.properties";
    private static final String NAME = "name";
    private static final String VERSION = "version";
    private static final RetryPolicy DEFAULT_RETRY_POLICY =
        new RetryPolicy("retry-after-ms", ChronoUnit.MILLIS);

    private final ConcurrentSkipListMap<String, Function<String, Object>> typeParserMap;
    private final List<HttpPipelinePolicy> policies;
    private final String clientName;
    private final String clientVersion;

    private String schemaRegistryUrl;
    private HttpClient httpClient;
    private Integer maxSchemaMapSize;
    private TokenCredential credential;
    private HttpLogOptions httpLogOptions;
    private HttpPipeline httpPipeline;
    private RetryPolicy retryPolicy;

    /**
     * Constructor for CachedSchemaRegistryClientBuilder.  Supplies client defaults.
     */
    public CachedSchemaRegistryClientBuilder() {
        this.policies = new ArrayList<>();
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
     * @return The updated {@link CachedSchemaRegistryClientBuilder} object.
     * @param schemaRegistryUrl The URL of the Azure Schema Registry instance
     * @throws NullPointerException if {@code schemaRegistryUrl} is null
     * @throws IllegalArgumentException if {@code schemaRegistryUrl} cannot be parsed into a valid URL
     */
    public CachedSchemaRegistryClientBuilder endpoint(String schemaRegistryUrl) {
        Objects.requireNonNull(schemaRegistryUrl, "'schemaRegistryUrl' cannot be null.");

        try {
            new URL(schemaRegistryUrl);
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsWarning(
                new IllegalArgumentException("'schemaRegistryUrl' must be a valid URL.", ex));
        }

        if (schemaRegistryUrl.endsWith("/")) {
            this.schemaRegistryUrl = schemaRegistryUrl.substring(0, schemaRegistryUrl.length() - 1);
        } else {
            this.schemaRegistryUrl = schemaRegistryUrl;
        }

        return this;
    }

    /**
     * Sets schema cache size limit.  If limit is exceeded on any cache, all caches are recycled.
     *
     * @param maxSchemaMapSize max size for internal schema caches in {@link CachedSchemaRegistryClient}
     * @return The updated {@link CachedSchemaRegistryClientBuilder} object.
     * @throws IllegalArgumentException on invalid maxSchemaMapSize value
     */
    public CachedSchemaRegistryClientBuilder maxSchemaMapSize(int maxSchemaMapSize) {
        if (maxSchemaMapSize < CachedSchemaRegistryClient.MAX_SCHEMA_MAP_SIZE_MINIMUM) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                String.format("Schema map size must be greater than %s entries",
                    CachedSchemaRegistryClient.MAX_SCHEMA_MAP_SIZE_MINIMUM)));
        }

        this.maxSchemaMapSize = maxSchemaMapSize;
        return this;
    }

    /**
     * Sets the HTTP client to use for sending and receiving requests to and from the service.
     *
     * @param httpClient The HTTP client to use for requests.
     * @return The updated {@link CachedSchemaRegistryClientBuilder} object.
     */
    public CachedSchemaRegistryClientBuilder httpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    /**
     * Sets the HTTP pipeline to use for the service client.
     * <p>
     * If {@code pipeline} is set, all other HTTP settings are ignored to build {@link CachedSchemaRegistryClient}.
     *
     * @param httpPipeline The HTTP pipeline to use for sending service requests and receiving responses.
     * @return The updated {@link CachedSchemaRegistryClientBuilder} object.
     */
    public CachedSchemaRegistryClientBuilder pipeline(HttpPipeline httpPipeline) {
        if (this.httpPipeline != null && httpPipeline == null) {
            logger.info("HttpPipeline is being set to 'null' when it was previously configured.");
        }

        this.httpPipeline = httpPipeline;
        return this;
    }


    /**
     * Sets the {@link TokenCredential} to use when authenticating HTTP requests for this
     * {@link CachedSchemaRegistryClient}.
     *
     * @param credential {@link TokenCredential}
     * @return The updated {@link CachedSchemaRegistryClientBuilder} object.
     * @throws NullPointerException If {@code credential} is {@code null}
     */
    public CachedSchemaRegistryClientBuilder credential(TokenCredential credential) {
        this.credential = Objects.requireNonNull(credential, "'credential' cannot be null.");
        return this;
    }

    /**
     * Sets the logging configuration for HTTP requests and responses.
     *
     * <p> If logLevel is not provided, default value of {@link HttpLogDetailLevel#NONE} is set. </p>
     *
     * @param logOptions The logging configuration to use when sending and receiving HTTP requests/responses.
     * @return The updated {@link CachedSchemaRegistryClientBuilder} object.
     */
    public CachedSchemaRegistryClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        this.httpLogOptions = logOptions;
        return this;
    }

    /**
     * Sets the {@link RetryPolicy} that is used when each request is sent.
     * <p>
     * The default retry policy will be used if not provided to build {@link CachedSchemaRegistryClient} .
     *
     * @param retryPolicy user's retry policy applied to each request.
     * @return The updated {@link CachedSchemaRegistryClientBuilder} object.
     */
    public CachedSchemaRegistryClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
        return this;
    }

    /**
     * Adds a policy to the set of existing policies that are executed after required policies.
     *
     * @param policy The retry policy for service requests.
     * @return The updated {@link CachedSchemaRegistryClientBuilder} object.
     * @throws NullPointerException If {@code policy} is {@code null}.
     */
    public CachedSchemaRegistryClientBuilder addPolicy(HttpPipelinePolicy policy) {
        policies.add(Objects.requireNonNull(policy, "'policy' cannot be null."));
        return this;
    }

    /**
     * Loads a parser method Function object used to convert schema strings returned from the Schema Registry
     * service into useable schema objects.
     *
     * Any com.azure.data.schemaregistry.ByteEncoder or com.azure.data.schemaregistry.ByteDecoder class will implement
     * - schemaType(), which specifies schema type, and
     * - parseSchemaString(), which parses schemas of the specified schema type from String to Object.
     *
     * The parseMethod argument should be a stateless, idempotent function.
     *
     * @param codec Codec class implementation
     * @return The updated {@link CachedSchemaRegistryClientBuilder} object.
     */
    public CachedSchemaRegistryClientBuilder addSchemaParser(Codec codec) {
        Objects.requireNonNull(codec, "'codec' cannot be null.");
        if (CoreUtils.isNullOrEmpty(codec.schemaType())) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Serialization type cannot be null or empty."));
        }
        if (this.typeParserMap.containsKey(codec.schemaType())) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Multiple parse methods for single serialization type may not be added."));
        }
        this.typeParserMap.put(codec.schemaType(), codec::parseSchemaString);
        return this;
    }

    /**
     * Creates a {@link CachedSchemaRegistryClient} based on options set in the builder.
     * Every time {@code buildClient()} is called a new instance of {@link CachedSchemaRegistryClient} is created.
     *
     * If {@link #pipeline(HttpPipeline) pipeline} is set, then all HTTP pipeline related settings are ignored
     * endpoint} are when creating the {@link CachedSchemaRegistryClient client}.
     *
     * @return A {@link CachedSchemaRegistryClient} with the options set from the builder.
     * @throws NullPointerException if parameters are incorrectly set.
     * @throws IllegalArgumentException if credential is not set.
     */
    public CachedSchemaRegistryClient buildClient() {
        // Authentications
        if (credential == null) {
            // Throw exception that credential and tokenCredential cannot be null
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Missing credential information while building a client."));
        }

        HttpPipeline pipeline = this.httpPipeline;
        // Create a default Pipeline if it is not given
        if (pipeline == null) {
            // Closest to API goes first, closest to wire goes last.
            final List<HttpPipelinePolicy> policies = new ArrayList<>();

            policies.add(new UserAgentPolicy(httpLogOptions.getApplicationId(), clientName, clientVersion,
                Configuration.getGlobalConfiguration().clone()));
            policies.add(new RequestIdPolicy());

            HttpPolicyProviders.addBeforeRetryPolicies(policies);

            policies.add(retryPolicy == null ? DEFAULT_RETRY_POLICY : retryPolicy);

            policies.add(new AddDatePolicy());

            policies.add(new BearerTokenAuthenticationPolicy(credential, DEFAULT_SCOPE));

            policies.addAll(this.policies);
            HttpPolicyProviders.addAfterRetryPolicies(policies);

            policies.add(new HttpLoggingPolicy(httpLogOptions));

            pipeline = new HttpPipelineBuilder()
                .policies(policies.toArray(new HttpPipelinePolicy[0]))
                .httpClient(httpClient)
                .build();
        }

        AzureSchemaRegistryRestService restService = new AzureSchemaRegistryRestServiceClientBuilder()
            .host(this.schemaRegistryUrl)
            .pipeline(pipeline)
            .buildClient();

        this.maxSchemaMapSize = this.maxSchemaMapSize != null
            ? this.maxSchemaMapSize
            : CachedSchemaRegistryClient.MAX_SCHEMA_MAP_SIZE_DEFAULT;

        return new CachedSchemaRegistryClient(restService, maxSchemaMapSize, typeParserMap);
    }
}
