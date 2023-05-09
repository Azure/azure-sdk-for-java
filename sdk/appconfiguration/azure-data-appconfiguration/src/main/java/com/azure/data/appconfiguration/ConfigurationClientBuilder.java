// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.client.traits.ConfigurationTrait;
import com.azure.core.client.traits.ConnectionStringTrait;
import com.azure.core.client.traits.EndpointTrait;
import com.azure.core.client.traits.HttpTrait;
import com.azure.core.client.traits.TokenCredentialTrait;
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
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.HttpClientOptions;
import com.azure.core.util.TracingOptions;
import com.azure.core.util.builder.ClientBuilderUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.Tracer;
import com.azure.core.util.tracing.TracerProvider;
import com.azure.data.appconfiguration.implementation.AzureAppConfigurationImpl;
import com.azure.data.appconfiguration.implementation.ConfigurationClientCredentials;
import com.azure.data.appconfiguration.implementation.ConfigurationCredentialsPolicy;
import com.azure.data.appconfiguration.implementation.SyncTokenPolicy;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.azure.core.util.CoreUtils.getApplicationId;
import static com.azure.data.appconfiguration.implementation.ClientConstants.APP_CONFIG_TRACING_NAMESPACE_VALUE;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of
 * {@link ConfigurationClient ConfigurationClients} and {@link ConfigurationAsyncClient ConfigurationAsyncClients}, call
 * {@link #buildClient() buildClient} and {@link #buildAsyncClient() buildAsyncClient} respectively to construct an
 * instance of the desired client.
 *
 * <p>The client needs the service endpoint of the Azure App Configuration store and access credential.
 * {@link #connectionString(String) connectionString(String)} gives the builder the service endpoint and access
 * credential.</p>
 *
 * <p><strong>Instantiating an asynchronous Configuration Client</strong></p>
 *
 * <!-- src_embed com.azure.data.applicationconfig.async.configurationclient.instantiation -->
 * <pre>
 * ConfigurationAsyncClient configurationAsyncClient = new ConfigurationClientBuilder&#40;&#41;
 *     .connectionString&#40;connectionString&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.data.applicationconfig.async.configurationclient.instantiation -->
 *
 * <p><strong>Instantiating a synchronous Configuration Client</strong></p>
 *
 * <!-- src_embed com.azure.data.applicationconfig.configurationclient.instantiation -->
 * <pre>
 * ConfigurationClient configurationClient = new ConfigurationClientBuilder&#40;&#41;
 *     .connectionString&#40;connectionString&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.data.applicationconfig.configurationclient.instantiation -->
 *
 * <p>Another way to construct the client is using a {@link HttpPipeline}. The pipeline gives the client an
 * authenticated way to communicate with the service but it doesn't contain the service endpoint. Set the pipeline with
 * {@link #pipeline(HttpPipeline) this} and set the service endpoint with {@link #endpoint(String) this}. Using a
 * pipeline requires additional setup but allows for finer control on how the {@link ConfigurationClient} and
 * {@link ConfigurationAsyncClient} is built.</p>
 *
 * <!-- src_embed com.azure.data.applicationconfig.configurationclient.pipeline.instantiation -->
 * <pre>
 * HttpPipeline pipeline = new HttpPipelineBuilder&#40;&#41;
 *     .policies&#40;&#47;* add policies *&#47;&#41;
 *     .build&#40;&#41;;
 *
 * ConfigurationClient configurationClient = new ConfigurationClientBuilder&#40;&#41;
 *     .pipeline&#40;pipeline&#41;
 *     .endpoint&#40;&quot;https:&#47;&#47;myconfig.azure.net&#47;&quot;&#41;
 *     .connectionString&#40;connectionString&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.data.applicationconfig.configurationclient.pipeline.instantiation -->
 *
 * @see ConfigurationAsyncClient
 * @see ConfigurationClient
 */
@ServiceClientBuilder(serviceClients = {ConfigurationAsyncClient.class, ConfigurationClient.class})
public final class ConfigurationClientBuilder implements
    TokenCredentialTrait<ConfigurationClientBuilder>,
    ConnectionStringTrait<ConfigurationClientBuilder>,
    HttpTrait<ConfigurationClientBuilder>,
    ConfigurationTrait<ConfigurationClientBuilder>,
    EndpointTrait<ConfigurationClientBuilder> {

    private static final String CLIENT_NAME;
    private static final String CLIENT_VERSION;
    private static final HttpPipelinePolicy ADD_HEADERS_POLICY;

    static {
        Map<String, String> properties = CoreUtils.getProperties("azure-data-appconfiguration.properties");
        CLIENT_NAME = properties.getOrDefault("name", "UnknownName");
        CLIENT_VERSION = properties.getOrDefault("version", "UnknownVersion");
        ADD_HEADERS_POLICY = new AddHeadersPolicy(new HttpHeaders()
            .set("x-ms-return-client-request-id", "true")
            .set("Content-Type", "application/json")
            .set("Accept", "application/vnd.microsoft.azconfig.kv+json"));
    }

    private static final ClientLogger LOGGER = new ClientLogger(ConfigurationClientBuilder.class);
    private final List<HttpPipelinePolicy> perCallPolicies = new ArrayList<>();
    private final List<HttpPipelinePolicy> perRetryPolicies = new ArrayList<>();

    private ClientOptions clientOptions;
    private String connectionString;
    private TokenCredential tokenCredential;

    private String endpoint;
    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions;
    private HttpPipeline pipeline;
    private HttpPipelinePolicy retryPolicy;
    private RetryOptions retryOptions;
    private Configuration configuration;
    private ConfigurationServiceVersion version;

    /**
     * Constructs a new builder used to configure and build {@link ConfigurationClient ConfigurationClients} and
     * {@link ConfigurationAsyncClient ConfigurationAsyncClients}.
     */
    public ConfigurationClientBuilder() {
        httpLogOptions = new HttpLogOptions();
    }

    /**
     * Creates a {@link ConfigurationClient} based on options set in the Builder. Every time {@code buildClient()} is
     * called a new instance of {@link ConfigurationClient} is created.
     * <p>
     * If {@link #pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * {@link #endpoint(String) endpoint} are used to create the {@link ConfigurationClient client}. All other builder
     * settings are ignored.</p>
     *
     * @return A ConfigurationClient with the options set from the builder.
     * @throws NullPointerException If {@code endpoint} has not been set. This setting is automatically set when
     * {@link #connectionString(String) connectionString} is called. Or can be set explicitly by calling
     * {@link #endpoint(String)}.
     * @throws IllegalStateException If {@link #connectionString(String) connectionString} has not been set.
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)} and
     * {@link #retryPolicy(HttpPipelinePolicy)} have been set.
     */
    public ConfigurationClient buildClient() {
        final SyncTokenPolicy syncTokenPolicy = new SyncTokenPolicy();
        return new ConfigurationClient(buildInnerClient(syncTokenPolicy), syncTokenPolicy);
    }

    /**
     * Creates a {@link ConfigurationAsyncClient} based on options set in the Builder. Every time
     * {@code buildAsyncClient()} is called a new instance of {@link ConfigurationAsyncClient} is created.
     * <p>
     * If {@link #pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * {@link #endpoint(String) endpoint} are used to create the {@link ConfigurationAsyncClient client}. All other
     * builder settings are ignored.
     *
     * @return A ConfigurationAsyncClient with the options set from the builder.
     * @throws NullPointerException If {@code endpoint} has not been set. This setting is automatically set when
     * {@link #connectionString(String) connectionString} is called. Or can be set explicitly by calling
     * {@link #endpoint(String)}.
     * @throws IllegalStateException If {@link #connectionString(String) connectionString} has not been set.
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)} and
     * {@link #retryPolicy(HttpPipelinePolicy)} have been set.
     */
    public ConfigurationAsyncClient buildAsyncClient() {
        final SyncTokenPolicy syncTokenPolicy = new SyncTokenPolicy();
        return new ConfigurationAsyncClient(buildInnerClient(syncTokenPolicy), syncTokenPolicy);
    }

    /**
     * Builds an instance of ConfigurationClientImpl with the provided parameters.
     *
     * @return an instance of ConfigurationClientImpl.
     * @throws NullPointerException If {@code connectionString} is null.
     * @throws IllegalArgumentException If {@code connectionString} is an empty string, the {@code connectionString}
     * secret is invalid, or the HMAC-SHA256 MAC algorithm cannot be instantiated.
     * @throws IllegalArgumentException if {@code tokenCredential} is not null. App Configuration builder support single
     * authentication per builder instance.
     */
    private AzureAppConfigurationImpl buildInnerClient(SyncTokenPolicy syncTokenPolicy) {
        String endpointLocal = endpoint;
        ConfigurationClientCredentials credentialsLocal = null;
        TokenCredential tokenCredentialLocal = null;
        // validate the authentication setup
        if (tokenCredential == null && connectionString == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'tokenCredential' and 'connectionString' "
                + "both can not be null. Set one authentication before creating client."));
        } else if (tokenCredential != null && connectionString != null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Multiple forms of authentication found. "
                + "TokenCredential should be null if using connection string, vice versa."));
        } else if (tokenCredential == null) {
            if (connectionString.isEmpty()) {
                throw LOGGER.logExceptionAsError(
                    new IllegalArgumentException("'connectionString' cannot be an empty string."));
            }
            credentialsLocal = new ConfigurationClientCredentials(connectionString);
            endpointLocal = credentialsLocal.getBaseUri();
        } else {
            tokenCredentialLocal = this.tokenCredential;
        }

        // Service version
        ConfigurationServiceVersion serviceVersion = (version != null)
            ? version
            : ConfigurationServiceVersion.getLatest();
        // Don't share the default auto-created pipeline between App Configuration client instances.
        HttpPipeline buildPipeline = (pipeline == null)
            ? createDefaultHttpPipeline(syncTokenPolicy, credentialsLocal, tokenCredentialLocal)
            : pipeline;

        return new AzureAppConfigurationImpl(buildPipeline, null, endpointLocal, serviceVersion.getVersion());
    }

    private HttpPipeline createDefaultHttpPipeline(SyncTokenPolicy syncTokenPolicy,
        ConfigurationClientCredentials credentials, TokenCredential tokenCredential) {
        // Global Env configuration store
        Configuration buildConfiguration = (configuration == null)
            ? Configuration.getGlobalConfiguration()
            : configuration;

        // Endpoint
        String buildEndpoint = endpoint;
        if (credentials != null) {
            buildEndpoint = credentials.getBaseUri();
        }
        // endpoint cannot be null, which is required in request authentication
        Objects.requireNonNull(buildEndpoint, "'Endpoint' is required and can not be null.");

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new UserAgentPolicy(
            getApplicationId(clientOptions, httpLogOptions), CLIENT_NAME, CLIENT_VERSION, buildConfiguration));
        policies.add(new RequestIdPolicy());
        policies.add(new AddHeadersFromContextPolicy());
        policies.add(ADD_HEADERS_POLICY);

        policies.addAll(perCallPolicies);
        HttpPolicyProviders.addBeforeRetryPolicies(policies);

        policies.add(ClientBuilderUtil.validateAndGetRetryPolicy(retryPolicy, retryOptions,
            new RetryPolicy("retry-after-ms", ChronoUnit.MILLIS)));

        policies.add(new AddDatePolicy());

        if (tokenCredential != null) {
            // User token based policy
            policies.add(
                new BearerTokenAuthenticationPolicy(tokenCredential, String.format("%s/.default", endpoint)));
        } else if (credentials != null) {
            // Use credentialS based policy
            policies.add(new ConfigurationCredentialsPolicy(credentials));
        } else {
            // Throw exception that credentials and tokenCredential cannot be null
            throw LOGGER.logExceptionAsError(
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
        return new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .tracer(createTracer(clientOptions))
            .build();
    }

    private static Tracer createTracer(ClientOptions clientOptions) {
        TracingOptions tracingOptions = clientOptions == null ? null : clientOptions.getTracingOptions();
        return TracerProvider.getDefaultProvider()
            .createTracer(CLIENT_NAME, CLIENT_VERSION, APP_CONFIG_TRACING_NAMESPACE_VALUE, tracingOptions);
    }

    /**
     * Sets the service endpoint for the Azure App Configuration instance.
     *
     * @param endpoint The URL of the Azure App Configuration instance.
     * @return The updated ConfigurationClientBuilder object.
     * @throws IllegalArgumentException If {@code endpoint} is null, or it cannot be parsed into a valid URL.
     */
    @Override
    public ConfigurationClientBuilder endpoint(String endpoint) {
        try {
            new URL(endpoint);
        } catch (MalformedURLException ex) {
            throw LOGGER.logExceptionAsWarning(new IllegalArgumentException("'endpoint' must be a valid URL", ex));
        }
        this.endpoint = endpoint;
        return this;
    }

    /**
     * Allows for setting common properties such as application ID, headers, proxy configuration, etc. Note that it is
     * recommended that this method be called with an instance of the {@link HttpClientOptions} class (a subclass of the
     * {@link ClientOptions} base class). The HttpClientOptions subclass provides more configuration options suitable
     * for HTTP clients, which is applicable for any class that implements this HttpTrait interface.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param clientOptions A configured instance of {@link HttpClientOptions}.
     * @return the updated ConfigurationClientBuilder object
     * @see HttpClientOptions
     */
    @Override
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
     */
    @Override
    public ConfigurationClientBuilder connectionString(String connectionString) {
        this.connectionString = connectionString;
        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authorize requests sent to the service. Refer to the Azure SDK for Java
     * <a href="https://aka.ms/azsdk/java/docs/identity">identity and authentication</a>
     * documentation for more details on proper usage of the {@link TokenCredential} type.
     *
     * @param tokenCredential {@link TokenCredential} used to authorize requests sent to the service.
     * @return The updated ConfigurationClientBuilder object.
     */
    @Override
    public ConfigurationClientBuilder credential(TokenCredential tokenCredential) {
        this.tokenCredential = tokenCredential;
        return this;
    }

    /**
     * Sets the {@link HttpLogOptions logging configuration} to use when sending and receiving requests to and from the
     * service. If a {@code logLevel} is not provided, default value of {@link HttpLogDetailLevel#NONE} is set.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param logOptions The {@link HttpLogOptions logging configuration} to use when sending and receiving requests to
     * and from the service.
     * @return The updated ConfigurationClientBuilder object.
     */
    @Override
    public ConfigurationClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        httpLogOptions = logOptions;
        return this;
    }

    /**
     * Adds a {@link HttpPipelinePolicy pipeline policy} to apply on each request sent.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param policy A {@link HttpPipelinePolicy pipeline policy}.
     * @return The updated ConfigurationClientBuilder object.
     * @throws NullPointerException If {@code policy} is null.
     */
    @Override
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
     * Sets the {@link HttpClient} to use for sending and receiving requests to and from the service.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param client The {@link HttpClient} to use for requests.
     * @return The updated ConfigurationClientBuilder object.
     */
    @Override
    public ConfigurationClientBuilder httpClient(HttpClient client) {
        if (this.httpClient != null && client == null) {
            LOGGER.info("HttpClient is being set to 'null' when it was previously configured.");
        }

        this.httpClient = client;
        return this;
    }

    /**
     * Sets the {@link HttpPipeline} to use for the service client.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     * <p>
     * The {@link #endpoint(String) endpoint} is not ignored when {@code pipeline} is set.
     *
     * @param pipeline {@link HttpPipeline} to use for sending service requests and receiving responses.
     * @return The updated ConfigurationClientBuilder object.
     */
    @Override
    public ConfigurationClientBuilder pipeline(HttpPipeline pipeline) {
        if (this.pipeline != null && pipeline == null) {
            LOGGER.info("HttpPipeline is being set to 'null' when it was previously configured.");
        }

        this.pipeline = pipeline;
        return this;
    }

    /**
     * Sets the configuration store that is used during construction of the service client.
     *
     * The default configuration store is a clone of the
     * {@link Configuration#getGlobalConfiguration() global configuration store}, use {@link Configuration#NONE} to
     * bypass using configuration settings during construction.
     *
     * @param configuration The configuration store used to
     * @return The updated ConfigurationClientBuilder object.
     */
    @Override
    public ConfigurationClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the {@link HttpPipelinePolicy} that is used to retry requests.
     * <p>
     * The default retry policy will be used if not provided {@link ConfigurationClientBuilder#buildAsyncClient()} to
     * build {@link ConfigurationAsyncClient} or {@link ConfigurationClient}.
     * <p>
     * Setting this is mutually exclusive with using {@link #retryOptions(RetryOptions)}.
     *
     * @param retryPolicy The {@link HttpPipelinePolicy} that will be used to retry requests. For example,
     * {@link RetryPolicy} can be used to retry requests.
     * @return The updated ConfigurationClientBuilder object.
     */
    public ConfigurationClientBuilder retryPolicy(HttpPipelinePolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
        return this;
    }

    /**
     * Sets the {@link RetryOptions} for all the requests made through the client.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     * <p>
     * Setting this is mutually exclusive with using {@link #retryPolicy(HttpPipelinePolicy)}.
     *
     * @param retryOptions The {@link RetryOptions} to use for all the requests made through the client.
     * @return The updated {@link ConfigurationClientBuilder} object.
     */
    @Override
    public ConfigurationClientBuilder retryOptions(RetryOptions retryOptions) {
        this.retryOptions = retryOptions;
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
}

