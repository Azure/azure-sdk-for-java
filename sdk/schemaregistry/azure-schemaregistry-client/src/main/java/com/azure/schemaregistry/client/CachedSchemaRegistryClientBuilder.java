package com.azure.schemaregistry.client;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.AzureKeyCredentialPolicy;
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
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Map;
import java.util.function.Function;

public class CachedSchemaRegistryClientBuilder {
    private final ClientLogger log = new ClientLogger(CachedSchemaRegistryClientBuilder.class);

    private static final String DEFAULT_SCOPE = "https://eventhubs.azure.com/.default";
    private static final String CLIENT_PROPERTIES = "azure-schemaregistry-client.properties";
    private static final String NAME = "name";
    private static final String VERSION = "version";

    private final String schemaRegistryUrl;
    private final HashMap<String, Function<String, Object>> typeParserDictionary;
    private final List<HttpPipelinePolicy> policies;
    private final String clientName;
    private final String clientVersion;
    private final HttpHeaders headers;

    private HttpClient httpClient;
    private int maxSchemaMapSize;
    private TokenCredential credential;
    private HttpLogOptions httpLogOptions;
    private HttpPipeline httpPipeline;
    private RetryPolicy retryPolicy;

    /**
     * Constructor.
     * Sets the service endpoint for the Azure Schema Registry instance.
     * Supplies client defaults.
     *
     * @param schemaRegistryUrl The URL of the Azure Schema Registry instance service requests to and receive responses from.
     * @return The updated {@link CachedSchemaRegistryClientBuilder} object.
     * @throws NullPointerException if {@code endpoint} is null
     * @throws IllegalArgumentException if {@code endpoint} cannot be parsed into a valid URL.
     */
    public CachedSchemaRegistryClientBuilder(String schemaRegistryUrl) {
        Objects.requireNonNull(schemaRegistryUrl, "'schemaRegistryUrl' cannot be null.");

        try {
            new URL(schemaRegistryUrl);
        } catch (MalformedURLException ex) {
            throw log.logExceptionAsWarning(new IllegalArgumentException("'schemaRegistryUrl' must be a valid URL.", ex));
        }

        if (schemaRegistryUrl.endsWith("/")) {
            this.schemaRegistryUrl = schemaRegistryUrl.substring(0, schemaRegistryUrl.length() - 1);
        } else {
            this.schemaRegistryUrl = schemaRegistryUrl;
        }

        this.policies = new ArrayList<>();
        this.httpLogOptions = new HttpLogOptions();
        this.maxSchemaMapSize = CachedSchemaRegistryClient.MAX_SCHEMA_MAP_SIZE_DEFAULT;
        this.typeParserDictionary = new HashMap<>();
        this.httpClient = null;
        this.credential = null;
        this.retryPolicy = new RetryPolicy("retry-after-ms", ChronoUnit.MILLIS);

        Map<String, String> properties = CoreUtils.getProperties(CLIENT_PROPERTIES);
        clientName = properties.getOrDefault(NAME, "UnknownName");
        clientVersion = properties.getOrDefault(VERSION, "UnknownVersion");

        this.headers = new HttpHeaders();
    }

    public CachedSchemaRegistryClientBuilder maxSchemaMapSize(int maxSchemaMapSize) throws IllegalArgumentException {
        if (maxSchemaMapSize < CachedSchemaRegistryClient.MAX_SCHEMA_MAP_SIZE_MINIMUM) {
            throw new IllegalArgumentException(
                String.format("Schema map size must be greater than %s entries",
                    CachedSchemaRegistryClient.MAX_SCHEMA_MAP_SIZE_MINIMUM));
        }
        this.maxSchemaMapSize = maxSchemaMapSize;
        return this;
    }

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
            log.info("HttpPipeline is being set to 'null' when it was previously configured.");
        }

        this.httpPipeline = httpPipeline;
        return this;
    }


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

    public CachedSchemaRegistryClientBuilder loadSchemaParser(String serializationType, Function<String, Object> parseMethod) {
        if (serializationType == null || serializationType.isEmpty()) {
            throw new IllegalArgumentException("Serialization type cannot be null or empty.");
        }
        if (this.typeParserDictionary.containsKey(serializationType.toLowerCase())) {
            throw new IllegalArgumentException("Multiple parse methods for single serialization type may not be added.");
        }
        this.typeParserDictionary.put(serializationType.toLowerCase(), parseMethod);
        return this;
    }

    public CachedSchemaRegistryClient build() {
        HttpPipeline pipeline = this.httpPipeline;
        // Create a default Pipeline if it is not given
        if (pipeline == null) {
            // Closest to API goes first, closest to wire goes last.
            final List<HttpPipelinePolicy> policies = new ArrayList<>();

            policies.add(new UserAgentPolicy(httpLogOptions.getApplicationId(), clientName, clientVersion,
                Configuration.getGlobalConfiguration().clone()));
            policies.add(new RequestIdPolicy());
            policies.add(new AddHeadersPolicy(this.headers));

            HttpPolicyProviders.addBeforeRetryPolicies(policies);

            policies.add(retryPolicy);

            policies.add(new AddDatePolicy());
            // Authentications
            if (credential != null) {
                // User token based policy
                policies.add(new BearerTokenAuthenticationPolicy(credential, DEFAULT_SCOPE));
            } else {
                // Throw exception that credential and tokenCredential cannot be null
                throw log.logExceptionAsError(
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

        return new CachedSchemaRegistryClient(
            schemaRegistryUrl,
            pipeline,
            credential,
            maxSchemaMapSize,
            typeParserDictionary);
    }
}
