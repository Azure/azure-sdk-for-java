package com.azure.messaging.signalr;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.CookiePolicy;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.signalr.implementation.client.AzureWebSocketServiceRestAPIBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@ServiceClientBuilder(serviceClients = {SignalRAsyncClient.class})
public final class SignalRClientBuilder {
    private final ClientLogger logger = new ClientLogger(SignalRClientBuilder.class);

    private static final RetryPolicy DEFAULT_RETRY_POLICY = new RetryPolicy();

    private final List<HttpPipelinePolicy> policies;

    private String connectionString;
    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions;
    private HttpPipeline pipeline;
    private HttpPipelinePolicy retryPolicy;
    private Configuration configuration;
    private SignalRServiceVersion version;

    public SignalRClientBuilder() {
        policies = new ArrayList<>();
        httpLogOptions = new HttpLogOptions();
    }

    /**
     * Sets the credential to use when authenticating HTTP requests.
     *
     * @param connectionString Connection string in the format "endpoint={endpoint_value};accesskey={accesskey_value}"
     * @return The updated SignalRClientBuilder object.
     * @throws NullPointerException If {@code connectionString} is {@code null}.
     */
    public SignalRClientBuilder connectionString(String connectionString) {
        Objects.requireNonNull(connectionString, "'connectionString' cannot be null.");
        this.connectionString = connectionString;
        return this;
    }

    /**
     * Sets the logging configuration for HTTP requests and responses.
     *
     * <p> If logLevel is not provided, default value of {@link com.azure.core.http.policy.HttpLogDetailLevel#NONE} is
     * set.</p>
     *
     * @param logOptions The logging configuration to use when sending and receiving HTTP requests/responses.
     * @return The updated SignalRClientBuilder object.
     */
    public SignalRClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        httpLogOptions = logOptions;
        return this;
    }

    /**
     * Adds a policy to the set of existing policies that are executed after required policies.
     *
     * @param policy The retry policy for service requests.
     * @return The updated SignalRClientBuilder object.
     * @throws NullPointerException If {@code policy} is {@code null}.
     */
    public SignalRClientBuilder addPolicy(HttpPipelinePolicy policy) {
        Objects.requireNonNull(policy);
        policies.add(policy);
        return this;
    }

    /**
     * Sets the HTTP client to use for sending and receiving requests to and from the service.
     *
     * @param client The HTTP client to use for requests.
     * @return The updated SignalRClientBuilder object.
     */
    public SignalRClientBuilder httpClient(HttpClient client) {
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
     * {@link SignalRClientBuilder#connectionString(String) connectionString} to build {@link SignalRAsyncClient} or
     * {@link SignalRClient}.
     *
     * @param pipeline The HTTP pipeline to use for sending service requests and receiving responses.
     * @return The updated SignalRClientBuilder object.
     */
    public SignalRClientBuilder pipeline(HttpPipeline pipeline) {
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
     * @return The updated SignalRClientBuilder object.
     */
    public SignalRClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the {@link HttpPipelinePolicy} that is used when each request is sent. The default retry policy will be
     * used if not provided.
     *
     * @param retryPolicy user's retry policy applied to each request.
     * @return The updated SignalRClientBuilder object.
     */
    public SignalRClientBuilder retryPolicy(HttpPipelinePolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
        return this;
    }

    /**
     * Sets the {@link SignalRServiceVersion} that is used when making API requests.
     * <p>
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version the client library will have the result of potentially moving to a newer service version.
     *
     * @param version {@link SignalRServiceVersion} of the service to be used when making requests.
     * @return The updated SignalRClientBuilder object.
     */
    public SignalRClientBuilder serviceVersion(SignalRServiceVersion version) {
        this.version = version;
        return this;
    }

    /**
     * Builds an instance of SignalRAsyncClient with the provided parameters.
     *
     * @return an instance of SignalRAsyncClient.
     */
    public SignalRAsyncClient buildAsyncClient() {
        final Map<String, String> csParams = parseConnectionString(connectionString);
        final String accessKey = csParams.get("accesskey");
        final String endpoint = csParams.get("endpoint");

        if (!csParams.containsKey("endpoint") && !csParams.containsKey("accesskey")) {
            logger.logThrowableAsError(new IllegalArgumentException(
                "Connection string does not contain required 'endpoint' and 'accesskey' values"));
        }

        AzureWebSocketServiceRestAPIBuilder innerBuilder = new AzureWebSocketServiceRestAPIBuilder();

        if (endpoint == null || endpoint.isEmpty()) {
            logger.logThrowableAsError(new IllegalStateException("endpoint is not valid - it must be non-null and non-empty."));
        }
        innerBuilder.host(endpoint);

        // Service version
        final SignalRServiceVersion serviceVersion =
            version != null ? version : SignalRServiceVersion.getLatest();

        if (pipeline != null) {
            innerBuilder.pipeline(pipeline);
            return new SignalRAsyncClient(innerBuilder.build(), serviceVersion);
        }

        if (accessKey == null || accessKey.isEmpty()) {
            logger.logThrowableAsError(new IllegalStateException("AccessKey is not valid - it must be non-null and non-empty."));
        }

        // Global Env configuration store
        final Configuration buildConfiguration =
            (configuration == null) ? Configuration.getGlobalConfiguration().clone() : configuration;

        // FIXME
        String clientName = "SIGNALR"; //properties.getOrDefault(SDK_NAME, "UnknownName");
        String clientVersion = "1.0"; //properties.getOrDefault(SDK_VERSION, "UnknownVersion");

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new UserAgentPolicy(httpLogOptions.getApplicationId(), clientName, clientVersion,
            buildConfiguration));
        policies.add(new CookiePolicy());
        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        policies.add(retryPolicy == null ? DEFAULT_RETRY_POLICY : retryPolicy);
        policies.add(new AuthenticationPolicy(accessKey));
        policies.addAll(this.policies);
        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(httpLogOptions));

        innerBuilder.pipeline(new HttpPipelineBuilder()
                                  .policies(policies.toArray(new HttpPipelinePolicy[0]))
                                  .httpClient(httpClient)
                                  .build());
        return new SignalRAsyncClient(innerBuilder.build(), serviceVersion);
    }

    /**
     * Builds an instance of SignalRClient with the provided parameters.
     *
     * @return an instance of SignalRClient.
     */
    public SignalRClient buildClient() {
        return new SignalRClient(buildAsyncClient());
    }

    private Map<String, String> parseConnectionString(String cs) {
        String[] params = cs.split(";");

        final Map<String, String> connectionStringParams = new HashMap<>();
        for (final String param : params) {
            final String[] paramSplit = param.split("=", 2);
            if (paramSplit.length != 2) {
                continue;
            }

            String key = paramSplit[0].trim().toLowerCase();

            if (connectionStringParams.containsKey(key)) {
                logger.logThrowableAsError(new IllegalArgumentException(
                    "Duplicate connection string key parameter provided for key '" + key + "'"));
            }

            String value = paramSplit[1].trim();
            connectionStringParams.put(key, value);
        }
        return connectionStringParams;
    }
}
