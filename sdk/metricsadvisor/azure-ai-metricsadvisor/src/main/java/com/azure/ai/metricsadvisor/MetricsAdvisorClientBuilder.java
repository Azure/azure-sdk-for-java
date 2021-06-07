// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.implementation.AzureCognitiveServiceMetricsAdvisorRestAPIOpenAPIV2Impl;
import com.azure.ai.metricsadvisor.implementation.AzureCognitiveServiceMetricsAdvisorRestAPIOpenAPIV2ImplBuilder;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;
import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.ContentType;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AddDatePolicy;
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

import java.net.MalformedURLException;
import java.net.URL;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help instantiation of {@link MetricsAdvisorClient MetricsAdvisorClients}
 * and {@link MetricsAdvisorAsyncClient MetricsAdvisorAsyncClients}, call {@link #buildClient()} buildClient} and
 * {@link #buildAsyncClient() buildAsyncClient} respectively to construct an instance of the desired client.
 *
 * <p>
 * The client needs the service endpoint of the Azure Metrics Advisor to access the resource service.
 * {@link #credential(MetricsAdvisorKeyCredential)} gives the builder access to credential.
 * </p>
 *
 * <p><strong>Instantiating an asynchronous Metrics Advisor Client</strong></p>
 *
 * {@codesnippet com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.instantiation}
 *
 * <p><strong>Instantiating a synchronous Metrics Advisor Client</strong></p>
 *
 * {@codesnippet com.azure.ai.metricsadvisor.MetricsAdvisorClient.instantiation}
 *
 * <p>
 * Another way to construct the client is using a {@link HttpPipeline}. The pipeline gives the client an
 * authenticated way to communicate with the service. Set the pipeline with {@link #pipeline(HttpPipeline) this} and
 * set the service endpoint with {@link #endpoint(String) this}. Using a
 * pipeline requires additional setup but allows for finer control on how the {@link MetricsAdvisorClient} and
 * {@link MetricsAdvisorAsyncClient} is built.
 * </p>
 *
 * {@codesnippet com.azure.ai.metricsadvisor.MetricsAdvisorClient.pipeline.instantiation}
 *
 * @see MetricsAdvisorAsyncClient
 * @see MetricsAdvisorClient
 */
@ServiceClientBuilder(serviceClients = {MetricsAdvisorAsyncClient.class, MetricsAdvisorClient.class})
public final class MetricsAdvisorClientBuilder {

    private static final String ECHO_REQUEST_ID_HEADER = "x-ms-return-client-request-id";
    private static final String CONTENT_TYPE_HEADER_VALUE = ContentType.APPLICATION_JSON;
    private static final String ACCEPT_HEADER = "Accept";
    private static final String METRICSADVISOR_PROPERTIES = "azure-ai-metricsadvisor.properties";
    private static final String NAME = "name";
    private static final String VERSION = "version";
    private static final RetryPolicy DEFAULT_RETRY_POLICY = new RetryPolicy("retry-after-ms",
        ChronoUnit.MILLIS);
    private static final String DEFAULT_SCOPE = "https://cognitiveservices.azure.com/.default";
    private static final HttpLogOptions DEFAULT_LOG_OPTIONS = new HttpLogOptions();
    private static final ClientOptions DEFAULT_CLIENT_OPTIONS = new ClientOptions();

    private final ClientLogger logger = new ClientLogger(MetricsAdvisorClientBuilder.class);
    private final List<HttpPipelinePolicy> policies;
    private final HttpHeaders headers;
    private final String clientName;
    private final String clientVersion;

    private String endpoint;
    private MetricsAdvisorKeyCredential metricsAdvisorKeyCredential;
    private TokenCredential tokenCredential;
    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions;
    private ClientOptions clientOptions;
    private HttpPipeline httpPipeline;
    private Configuration configuration;
    private RetryPolicy retryPolicy;
    private MetricsAdvisorServiceVersion version;

    static final String OCP_APIM_SUBSCRIPTION_KEY = "Ocp-Apim-Subscription-Key";
    static final String API_KEY = "x-api-key";

    /**
     * The constructor with defaults.
     */
    public MetricsAdvisorClientBuilder() {
        policies = new ArrayList<>();
        httpLogOptions = new HttpLogOptions();

        Map<String, String> properties = CoreUtils.getProperties(METRICSADVISOR_PROPERTIES);
        clientName = properties.getOrDefault(NAME, "UnknownName");
        clientVersion = properties.getOrDefault(VERSION, "UnknownVersion");

        headers = new HttpHeaders()
            .set(ECHO_REQUEST_ID_HEADER, "true")
            .set(ACCEPT_HEADER, CONTENT_TYPE_HEADER_VALUE);
    }

    /**
     * Creates a {@link MetricsAdvisorClient} based on options set in the builder. Every time
     * {@code buildClient()} is called a new instance of {@link MetricsAdvisorClient} is created.
     *
     * <p>
     * If {@link #pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * {@link #endpoint(String) endpoint} are used to create the {@link MetricsAdvisorClient client}. All other builder
     * settings are ignored.
     * </p>
     *
     * @return A MetricsAdvisorClient with the options set from the builder.
     * @throws NullPointerException if {@link #endpoint(String) endpoint} or
     * {@link #credential(MetricsAdvisorKeyCredential)} has not been set.
     * @throws IllegalArgumentException if {@link #endpoint(String) endpoint} cannot be parsed into a valid URL.
     */
    public MetricsAdvisorClient buildClient() {
        return new MetricsAdvisorClient(buildAsyncClient());
    }

    /**
     * Creates a {@link MetricsAdvisorAsyncClient} based on options set in the builder. Every time
     * {@code buildAsyncClient()} is called a new instance of {@link MetricsAdvisorAsyncClient} is created.
     *
     * <p>
     * If {@link #pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * {@link #endpoint(String) endpoint} are used to create the {@link MetricsAdvisorClient client}. All other builder
     * settings are ignored.
     * </p>
     *
     * @return A MetricsAdvisorAsyncClient with the options set from the builder.
     * @throws NullPointerException if {@link #endpoint(String) endpoint} or
     * {@link #credential(MetricsAdvisorKeyCredential)}
     * has not been set.
     * @throws IllegalArgumentException if {@link #endpoint(String) endpoint} cannot be parsed into a valid URL.
     */
    public MetricsAdvisorAsyncClient buildAsyncClient() {
        // Endpoint cannot be null, which is required in request authentication
        Objects.requireNonNull(endpoint, "'Endpoint' is required and can not be null.");

        // Global Env configuration store
        final Configuration buildConfiguration = (configuration == null)
            ? Configuration.getGlobalConfiguration().clone() : configuration;

        // Service Version
        final MetricsAdvisorServiceVersion serviceVersion =
            version != null ? version : MetricsAdvisorServiceVersion.getLatest();

        HttpPipeline pipeline = httpPipeline;
        // Create a default Pipeline if it is not given
        if (pipeline == null) {
            pipeline = getDefaultHttpPipeline(buildConfiguration);
        }
        final AzureCognitiveServiceMetricsAdvisorRestAPIOpenAPIV2Impl advisorRestAPIOpenAPIV2 =
            new AzureCognitiveServiceMetricsAdvisorRestAPIOpenAPIV2ImplBuilder()
                .endpoint(endpoint)
                .pipeline(pipeline)
                .buildClient();

        return new MetricsAdvisorAsyncClient(advisorRestAPIOpenAPIV2, serviceVersion);
    }

    private HttpPipeline getDefaultHttpPipeline(Configuration buildConfiguration) {
        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        // Authentications
        if (tokenCredential != null) {
            policies.add(new BearerTokenAuthenticationPolicy(tokenCredential, DEFAULT_SCOPE));
        } else if (!CoreUtils.isNullOrEmpty(metricsAdvisorKeyCredential.getKeys().getSubscriptionKey())
            || !CoreUtils.isNullOrEmpty(metricsAdvisorKeyCredential.getKeys().getApiKey())) {
            headers.set(OCP_APIM_SUBSCRIPTION_KEY, metricsAdvisorKeyCredential.getKeys().getSubscriptionKey());
            headers.set(API_KEY, metricsAdvisorKeyCredential.getKeys().getApiKey());
        } else {
            // Throw exception that credential cannot be null
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Missing credential information while building a client."));
        }

        ClientOptions buildClientOptions = this.clientOptions == null ? DEFAULT_CLIENT_OPTIONS : this.clientOptions;
        HttpLogOptions buildLogOptions = this.httpLogOptions == null ? DEFAULT_LOG_OPTIONS : this.httpLogOptions;
        final String applicationId = CoreUtils.getApplicationId(buildClientOptions, buildLogOptions);

        policies.add(new UserAgentPolicy(applicationId, clientName, clientVersion,
            buildConfiguration));
        policies.add(new RequestIdPolicy());
        policies.add(new AddHeadersPolicy(headers));

        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        policies.add(retryPolicy == null ? DEFAULT_RETRY_POLICY : retryPolicy);
        policies.add(new AddDatePolicy());
        policies.add(new HttpLoggingPolicy(httpLogOptions));

        policies.addAll(this.policies);
        HttpPolicyProviders.addAfterRetryPolicies(policies);

        return new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();
    }

    /**
     * Sets the service endpoint for the Azure Metrics Advisor instance.
     *
     * @param endpoint The URL of the Azure Metrics Advisor instance service requests to and receive responses from.
     *
     * @return The updated MetricsAdvisorClientBuilder object.
     * @throws NullPointerException if {@code endpoint} is null
     * @throws IllegalArgumentException if {@code endpoint} cannot be parsed into a valid URL.
     */
    public MetricsAdvisorClientBuilder endpoint(String endpoint) {
        Objects.requireNonNull(endpoint, "'endpoint' cannot be null.");

        try {
            new URL(endpoint);
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("'endpoint' must be a valid URL.", ex));
        }

        if (endpoint.endsWith("/")) {
            this.endpoint = endpoint.substring(0, endpoint.length() - 1);
        } else {
            this.endpoint = endpoint;
        }

        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authenticate HTTP requests.
     *
     * @param tokenCredential {@link TokenCredential} used to authenticate HTTP requests.
     * @return The updated {@link MetricsAdvisorClientBuilder} object.
     * @throws NullPointerException If {@code tokenCredential} is null.
     */
    public MetricsAdvisorClientBuilder credential(TokenCredential tokenCredential) {
        this.tokenCredential = Objects.requireNonNull(tokenCredential, "'tokenCredential' cannot be null.");
        return this;
    }

    /**
     * Sets the {@link MetricsAdvisorKeyCredential} to use when authenticating HTTP requests for this
     * MetricsAdvisorClientBuilder.
     *
     * @param metricsAdvisorKeyCredential {@link MetricsAdvisorKeyCredential} API key credential
     *
     * @return The updated MetricsAdvisorClientBuilder object.
     * @throws NullPointerException If {@code metricsAdvisorKeyCredential} is null.
     */
    public MetricsAdvisorClientBuilder credential(MetricsAdvisorKeyCredential metricsAdvisorKeyCredential) {
        this.metricsAdvisorKeyCredential = Objects.requireNonNull(metricsAdvisorKeyCredential,
            "'metricsAdvisorKeyCredential' cannot be null.");
        return this;
    }

    /**
     * Sets the logging configuration for HTTP requests and responses.
     *
     * <p>If {@code logOptions} isn't provided, the default options will use {@link HttpLogDetailLevel#NONE}
     * which will prevent logging.</p>
     *
     * @param logOptions The logging configuration to use when sending and receiving HTTP requests/responses.
     *
     * @return The updated MetricsAdvisorClientBuilder object.
     */
    public MetricsAdvisorClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        this.httpLogOptions = logOptions;
        return this;
    }

    /**
     * Sets the client options such as application ID and custom headers to set on a request.
     *
     * @param clientOptions The client options.
     * @return The updated MetricsAdvisorClientBuilder object.
     */
    public MetricsAdvisorClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
        return this;
    }

    /**
     * Adds a policy to the set of existing policies that are executed after required policies.
     *
     * @param policy The retry policy for service requests.
     *
     * @return The updated MetricsAdvisorClientBuilder object.
     * @throws NullPointerException If {@code policy} is null.
     */
    public MetricsAdvisorClientBuilder addPolicy(HttpPipelinePolicy policy) {
        policies.add(Objects.requireNonNull(policy, "'policy' cannot be null."));
        return this;
    }

    /**
     * Sets the HTTP client to use for sending and receiving requests to and from the service.
     *
     * @param client The HTTP client to use for requests.
     *
     * @return The updated MetricsAdvisorClientBuilder object.
     */
    public MetricsAdvisorClientBuilder httpClient(HttpClient client) {
        if (this.httpClient != null && client == null) {
            logger.info("HttpClient is being set to 'null' when it was previously configured.");
        }

        this.httpClient = client;
        return this;
    }

    /**
     * Sets the HTTP pipeline to use for the service client.
     * <p>
     * If {@code pipeline} is set, all other settings are ignored, aside from
     * {@link MetricsAdvisorClientBuilder#endpoint(String) endpoint} to build {@link MetricsAdvisorAsyncClient} or
     * {@link MetricsAdvisorClient}.
     *
     * @param httpPipeline The HTTP pipeline to use for sending service requests and receiving responses.
     *
     * @return The updated MetricsAdvisorClientBuilder object.
     */
    public MetricsAdvisorClientBuilder pipeline(HttpPipeline httpPipeline) {
        if (this.httpPipeline != null && httpPipeline == null) {
            logger.info("HttpPipeline is being set to 'null' when it was previously configured.");
        }

        this.httpPipeline = httpPipeline;
        return this;
    }

    /**
     * Sets the configuration store that is used during construction of the service client.
     * <p>
     * The default configuration store is a clone of the {@link Configuration#getGlobalConfiguration() global
     * configuration store}, use {@link Configuration#NONE} to bypass using configuration settings during construction.
     *
     * @param configuration The configuration store used to.
     *
     * @return The updated MetricsAdvisorClientBuilder object.
     */
    public MetricsAdvisorClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the {@link RetryPolicy#RetryPolicy()} that is used when each request is sent.
     * <p>
     * The default retry policy will be used if not provided {@link MetricsAdvisorClientBuilder#buildAsyncClient()}
     * to build {@link MetricsAdvisorAsyncClient} or {@link MetricsAdvisorClient}.
     *
     * @param retryPolicy user's retry policy applied to each request.
     *
     * @return The updated MetricsAdvisorClientBuilder object.
     */
    public MetricsAdvisorClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
        return this;
    }

    /**
     * Sets the {@link MetricsAdvisorServiceVersion} that is used when making API requests.
     * <p>
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version the client library will have the result of potentially moving to a newer service version.
     *
     * @param version {@link MetricsAdvisorServiceVersion} of the service to be used when making requests.
     *
     * @return The updated MetricsAdvisorClientBuilder object.
     */
    public MetricsAdvisorClientBuilder serviceVersion(MetricsAdvisorServiceVersion version) {
        this.version = version;
        return this;
    }
}
