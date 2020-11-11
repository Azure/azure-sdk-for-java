// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.signalr;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
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
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.signalr.implementation.AzureWebSocketServiceRestAPIImpl;
import com.azure.messaging.signalr.implementation.AzureWebSocketServiceRestAPIImplBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This class provides a fluent builder API to aid the configuration and instantiation of
 * {@link SignalRClient sync} and {@link SignalRAsyncClient async} SignalR clients, using the
 * {@link #buildClient() buildClient} and {@link #buildAsyncClient() buildAsyncClient} methods respectively.
 *
 * <p>To fully configure a SignalR client, it is necessary to supply a
 * {@link #connectionString(String) connection string} retrieved from the Azure Portal, or else a combination of
 * {@link #credential(AzureKeyCredential) credential} and {@link #endpoint(String) endpoint}.</p>
 *
 * <p>If the SignalR client is intended to connect to a specific hub, this may be achieved by specifying the appropriate
 * {@link #hub(String) hub} in the builder. If no hub is specified, a default hub will be used, which will be shared
 * across all SignalR client instances.</p>
 *
 * <p><strong>Code Samples</strong></p>
 *
 * {@codesnippet com.azure.messaging.signalr.secretclientbuilder.connectionstring.async}
 *
 * <p>This demonstrates using the connection string provided by the Azure Portal. Another approach is to use the
 * combination of credential and endpoint details, as shown below:</p>
 *
 * {@codesnippet com.azure.messaging.signalr.secretclientbuilder.credential.endpoint.async}
 *
 * <p>Of course, synchronous clients may also be instantiated, by calling {@link #buildClient() buildClient} rather than
 * {@link #buildAsyncClient() buildAsyncClient}.</p>
 *
 * @see SignalRAsyncClient
 * @see SignalRClient
 */
@ServiceClientBuilder(serviceClients = {SignalRAsyncClient.class})
public final class SignalRClientBuilder {
    private final ClientLogger logger = new ClientLogger(SignalRClientBuilder.class);

    private static final String SIGNALR_PROPERTIES = "azure-messaging-signalr.properties";
    private static final String SDK_NAME = "name";
    private static final String SDK_VERSION = "version";

    private static final HttpPipelinePolicy DEFAULT_RETRY_POLICY = new RetryPolicy();

    private final Map<String, String> properties;
    private final List<HttpPipelinePolicy> policies;

    // the user should set either the connectionString, or the endpoint and credential.
    // endpoint + credential will take precedence over connection string
    private String connectionString;
    private String endpoint;
    private AzureKeyCredential credential;

    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions;
    private HttpPipeline pipeline;
    private HttpPipelinePolicy retryPolicy;
    private Configuration configuration;
    private SignalRServiceVersion version;
    private String hub;
    private String group;

    /**
     * Creates a new builder instance with all values set to their default value.
     */
    public SignalRClientBuilder() {
        policies = new ArrayList<>();
        httpLogOptions = new HttpLogOptions();
        properties = CoreUtils.getProperties(SIGNALR_PROPERTIES);
    }

    /**
     * Sets the credential to use when authenticating HTTP requests.
     *
     * @param connectionString Connection string in the format "endpoint={endpoint_value};accesskey={accesskey_value}"
     * @return The updated SignalRClientBuilder object.
     * @throws NullPointerException If {@code connectionString} is {@code null}.
     */
    public SignalRClientBuilder connectionString(final String connectionString) {
        Objects.requireNonNull(connectionString, "'connectionString' cannot be null.");
        this.connectionString = connectionString;
        return this;
    }

    /**
     * Sets the service endpoint for the Azure SignalR instance.
     *
     * @param endpoint The URL of the Azure SignalR instance to send service requests to, and receive responses from.
     * @return The updated SignalRClientBuilder object.
     * @throws IllegalArgumentException if {@code endpoint} is {@code null}.
     */
    public SignalRClientBuilder endpoint(final String endpoint) {
        Objects.requireNonNull(connectionString, "'endpoint' cannot be null.");
        this.endpoint = endpoint;
        return this;
    }

    /**
     * Sets the {@link AzureKeyCredential} used to authenticate HTTP requests.
     *
     * @param credential AzureKeyCredential used to authenticate HTTP requests.
     * @return The updated SignalRClientBuilder object.
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public SignalRClientBuilder credential(final AzureKeyCredential credential) {
        Objects.requireNonNull(connectionString, "'credential' cannot be null.");
        this.credential = credential;
        return this;
    }

    /**
     * Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or
     * underscore.
     *
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric
     * characters or underscore.
     * @return The updated SignalRClientBuilder object.
     * @throws NullPointerException If {@code hub} is {@code null}.
     */
    public SignalRClientBuilder hub(final String hub) {
        Objects.requireNonNull(hub, "'hub' cannot be null.");
        this.hub = hub;
        return this;
    }

    /**
     * Target group name, which should start with alphabetic characters and only contain alpha-numeric characters or
     * underscore.
     *
     * @param group Target group name, which should start with alphabetic characters and only contain alpha-numeric
     * characters or underscore.
     * @return The updated SignalRClientBuilder object.
     */
    public SignalRClientBuilder group(final String group) {
        this.group = group;
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
    public SignalRClientBuilder httpLogOptions(final HttpLogOptions logOptions) {
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
    public SignalRClientBuilder addPolicy(final HttpPipelinePolicy policy) {
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
    public SignalRClientBuilder httpClient(final HttpClient client) {
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
    public SignalRClientBuilder pipeline(final HttpPipeline pipeline) {
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
    public SignalRClientBuilder configuration(final Configuration configuration) {
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
    public SignalRClientBuilder retryPolicy(final HttpPipelinePolicy retryPolicy) {
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
    public SignalRClientBuilder serviceVersion(final SignalRServiceVersion version) {
        this.version = version;
        return this;
    }

    /**
     * Builds an instance of SignalRAsyncClient with the provided parameters.
     *
     * @return an instance of SignalRAsyncClient.
     */
    public SignalRAsyncClient buildAsyncClient() {
        if (endpoint == null && credential == null) {
            final Map<String, String> csParams = parseConnectionString(connectionString);
            final String accessKey = csParams.get("accesskey");

            this.credential = new AzureKeyCredential(accessKey);
            this.endpoint = csParams.get("endpoint");

            if (!csParams.containsKey("endpoint") && !csParams.containsKey("accesskey")) {
                logger.logThrowableAsError(new IllegalArgumentException(
                    "Connection string does not contain required 'endpoint' and 'accesskey' values"));
            }
        }

        final AzureWebSocketServiceRestAPIImplBuilder innerBuilder = new AzureWebSocketServiceRestAPIImplBuilder();

        if (endpoint == null || endpoint.isEmpty()) {
            logger.logThrowableAsError(
                new IllegalStateException("endpoint is not valid - it must be non-null and non-empty."));
        }
        innerBuilder.host(endpoint);

        // Service version
        final SignalRServiceVersion serviceVersion =
            version != null ? version : SignalRServiceVersion.getLatest();

        if (pipeline != null) {
            innerBuilder.pipeline(pipeline);
            return buildAsyncClient(innerBuilder, hub, serviceVersion);
        }

        if (credential == null) {
            logger.logThrowableAsError(
                new IllegalStateException("No credential has been specified - it must be non-null and non-empty."));
        }

        // Global Env configuration store
        final Configuration buildConfiguration =
            (configuration == null) ? Configuration.getGlobalConfiguration().clone() : configuration;

        final String clientName = properties.getOrDefault(SDK_NAME, "UnknownName");
        final String clientVersion = properties.getOrDefault(SDK_VERSION, "UnknownVersion");

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new UserAgentPolicy(httpLogOptions.getApplicationId(), clientName, clientVersion,
            buildConfiguration));
        policies.add(new CookiePolicy());
        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        policies.add(retryPolicy == null ? DEFAULT_RETRY_POLICY : retryPolicy);
        policies.add(new SignalRAuthenticationPolicy(credential));
        policies.addAll(this.policies);
        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(httpLogOptions));

        innerBuilder.pipeline(new HttpPipelineBuilder()
                                  .policies(policies.toArray(new HttpPipelinePolicy[0]))
                                  .httpClient(httpClient)
                                  .build());
        return buildAsyncClient(innerBuilder, hub, serviceVersion);
    }

    private SignalRAsyncClient buildAsyncClient(final AzureWebSocketServiceRestAPIImplBuilder innerBuilder,
                                     final String hub,
                                     final SignalRServiceVersion serviceVersion) {
        final AzureWebSocketServiceRestAPIImpl client = innerBuilder.buildClient();
        return new SignalRAsyncClient(
            client.getWebSocketConnectionApis(),
            client.getHealthApis(),
            hub,
            serviceVersion);
    }

    /**
     * Builds an instance of SignalRClient with the provided parameters.
     *
     * @return an instance of SignalRClient.
     */
    public SignalRClient buildClient() {
        return new SignalRClient(buildAsyncClient());
    }

    public SignalRGroupClient buildGroupClient() {
        if (group == null || group.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalStateException(
                    "To build a group client, the 'group' builder property must be non-null and non-empty"));
        }
        return buildClient().getGroupClient(group);
    }

    public SignalRGroupAsyncClient buildGroupAsyncClient() {
        if (group == null || group.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalStateException(
                "To build a group client, the 'group' builder property must be non-null and non-empty"));
        }
        return buildAsyncClient().getGroupAsyncClient(group);
    }

    private Map<String, String> parseConnectionString(final String cs) {
        final String[] params = cs.split(";");

        final Map<String, String> connectionStringParams = new HashMap<>();
        for (final String param : params) {
            final String[] paramSplit = param.split("=", 2);
            if (paramSplit.length != 2) {
                continue;
            }

            final String key = paramSplit[0].trim().toLowerCase();

            if (connectionStringParams.containsKey(key)) {
                logger.logThrowableAsError(new IllegalArgumentException(
                    "Duplicate connection string key parameter provided for key '" + key + "'"));
            }

            final String value = paramSplit[1].trim();
            connectionStringParams.put(key, value);
        }
        return connectionStringParams;
    }
}
