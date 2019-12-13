// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.TokenCredential;
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
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImplBuilder;
import com.azure.ai.textanalytics.models.TextAnalyticsClientOptions;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help instantiation of {@link TextAnalyticsClient TextAnalyticsClients}
 * and {@link TextAnalyticsAsyncClient TextAnalyticsAsyncClients}, call {@link #buildClient()} buildClient} and
 * {@link #buildAsyncClient() buildAsyncClient} respectively to construct an instance of the desired client.
 *
 * <p>The client needs the service endpoint of the Azure Text Analytics to access the resource service.
 * {@link #subscriptionKey(String) subscriptionKey(String)} or
 * {@link #credential(TokenCredential) credential(TokenCredential)} give the builder access credential. </p>
 *
 * <p>Another way to construct the client is using a {@link HttpPipeline}. The pipeline gives the client an
 * authenticated way to communicate with the service. Set the pipeline with {@link #pipeline(HttpPipeline) this} and
 * set the service endpoint with {@link #endpoint(String) this}. Using a
 * pipeline requires additional setup but allows for finer control on how the {@link TextAnalyticsClient} and
 * {@link TextAnalyticsAsyncClient} is built.</p>
 *
 * @see TextAnalyticsAsyncClient
 * @see TextAnalyticsClient
 */
@ServiceClientBuilder(serviceClients = {TextAnalyticsAsyncClient.class, TextAnalyticsClient.class})
public final class TextAnalyticsClientBuilder {
    private static final String ECHO_REQUEST_ID_HEADER = "x-ms-return-client-request-id";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String CONTENT_TYPE_HEADER_VALUE = "application/json";
    private static final String ACCEPT_HEADER = "Accept";
    private static final String ACCEPT_HEADER_VALUE = "application/json";
    private static final String TEXT_ANALYTICS_PROPERTIES = "azure-textanalytics.properties";
    private static final String OCP_APIM_SUBSCRIPTION_KEY = "Ocp-Apim-Subscription-Key";
    private static final String NAME = "name";
    private static final String VERSION = "version";
    private static final RetryPolicy DEFAULT_RETRY_POLICY = new RetryPolicy("retry-after-ms", ChronoUnit.MILLIS);
    private static final String DEFAULT_SCOPE = "https://cognitiveservices.azure.com/.default";

    private final ClientLogger logger = new ClientLogger(TextAnalyticsClientBuilder.class);
    private final List<HttpPipelinePolicy> policies;
    private final HttpHeaders headers;
    private final String clientName;
    private final String clientVersion;

    private String endpoint;
    private String subscriptionKey;
    private TokenCredential tokenCredential;
    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions;
    private HttpPipeline httpPipeline;
    private Configuration configuration;
    private HttpPipelinePolicy retryPolicy;
    private TextAnalyticsClientOptions clientOptions;
    private TextAnalyticsServiceVersion version;

    /**
     * The constructor with defaults.
     */
    public TextAnalyticsClientBuilder() {
        policies = new ArrayList<>();
        httpLogOptions = new HttpLogOptions();

        Map<String, String> properties = CoreUtils.getProperties(TEXT_ANALYTICS_PROPERTIES);
        clientName = properties.getOrDefault(NAME, "UnknownName");
        clientVersion = properties.getOrDefault(VERSION, "UnknownVersion");

        headers = new HttpHeaders()
            .put(ECHO_REQUEST_ID_HEADER, "true")
            .put(CONTENT_TYPE_HEADER, CONTENT_TYPE_HEADER_VALUE)
            .put(ACCEPT_HEADER, ACCEPT_HEADER_VALUE);
    }

    /**
     * TODO (shawn): add javadoc
     *
     * @return A TextAnalyticsClient with the options set from the builder.
     */
    public TextAnalyticsClient buildClient() {
        return new TextAnalyticsClient(buildAsyncClient());
    }

    /**
     * TODO (shawn): add javadoc
     *
     * @return A TextAnalyticsAsyncClient with the options set from the builder.
     */
    public TextAnalyticsAsyncClient buildAsyncClient() {
        // Global Env configuration store
        Configuration buildConfiguration = (configuration == null)
            ? Configuration.getGlobalConfiguration().clone() : configuration;
        // Service Version
        TextAnalyticsServiceVersion serviceVersion =
            version != null ? version : TextAnalyticsServiceVersion.getLatest();

        // endpoint cannot be null, which is required in request authentication
        Objects.requireNonNull(endpoint, "'Endpoint' is required and can not be null.");

        // Http pipeline is already defined, skip rest of customized pipeline process
        if (httpPipeline != null) {
            TextAnalyticsClientImpl textAnalyticsAPI = new TextAnalyticsClientImplBuilder()
                .endpoint(endpoint)
                .pipeline(httpPipeline)
                .build();
            return new TextAnalyticsAsyncClient(textAnalyticsAPI, serviceVersion);
        }

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        if (tokenCredential != null) {
            // User token based policy
            policies.add(new BearerTokenAuthenticationPolicy(tokenCredential, DEFAULT_SCOPE));
        } else if (subscriptionKey != null) {
            headers.put(OCP_APIM_SUBSCRIPTION_KEY, subscriptionKey);
        } else {
            // Throw exception that credential and tokenCredential cannot be null
            logger.logExceptionAsError(
                new IllegalArgumentException("Missing credential information while building a client."));
        }

        policies.add(new UserAgentPolicy(httpLogOptions.getApplicationId(), clientName, clientVersion,
            buildConfiguration));
        policies.add(new RequestIdPolicy());
        policies.add(new AddHeadersPolicy(headers));
        policies.add(new AddDatePolicy());

        HttpPolicyProviders.addBeforeRetryPolicies(policies);

        policies.add(retryPolicy == null ? DEFAULT_RETRY_POLICY : retryPolicy);

        policies.addAll(this.policies);
        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(httpLogOptions));

        // customized pipeline
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();

        TextAnalyticsClientImpl textAnalyticsAPI = new TextAnalyticsClientImplBuilder()
            .endpoint(endpoint)
            .pipeline(pipeline)
            .build();

        return new TextAnalyticsAsyncClient(textAnalyticsAPI, serviceVersion);
    }

    /**
     * Set the default client option for one client.
     *
     * @param clientOptions TextAnalyticsClientOptions model that includes
     * {@link TextAnalyticsClientOptions#getDefaultLanguage() default language} and
     * {@link TextAnalyticsClientOptions#getDefaultCountryHint() default country hint}
     * @return The updated TextAnalyticsClientBuilder object.
     */
    public TextAnalyticsClientBuilder clientOptions(TextAnalyticsClientOptions clientOptions) {
        this.clientOptions = clientOptions;
        return this;
    }

    /**
     * Sets the service endpoint for the Azure Text Analytics instance.
     *
     * @param endpoint The URL of the Azure Text Analytics instance service requests to and receive responses from.
     * @return The updated TextAnalyticsClientBuilder object.
     * @throws IllegalArgumentException if {@code endpoint} is null or it cannot be parsed into a valid URL.
     */
    public TextAnalyticsClientBuilder endpoint(String endpoint) {
        try {
            new URL(endpoint);
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("'endpoint' must be a valid URL", ex));
        }
        this.endpoint = endpoint;
        return this;
    }

    /**
     * Sets the credential to use when authenticating HTTP requests for this TextAnalyticsClientBuilder.
     *
     * @param subscriptionKey subscription key
     * @return The updated TextAnalyticsClientBuilder object.
     * @throws NullPointerException If {@code subscriptionKey} is {@code null}.
     */
    public TextAnalyticsClientBuilder subscriptionKey(String subscriptionKey) {
        Objects.requireNonNull(subscriptionKey);

        this.subscriptionKey = subscriptionKey;

        // Clear TokenCredential in favor of subscription key credential
        this.tokenCredential = null;
        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authenticate HTTP requests.
     *
     * @param tokenCredential TokenCredential used to authenticate HTTP requests.
     * @return The updated TextAnalyticsClientBuilder object.
     * @throws NullPointerException If {@code tokenCredential} is {@code null}.
     */
    public TextAnalyticsClientBuilder credential(TokenCredential tokenCredential) {
        // token credential can not be null value
        Objects.requireNonNull(tokenCredential, "'tokenCredential' cannot be null.");
        this.tokenCredential = tokenCredential;

        // Clear subscription key based credential in favor of TokenCredential
        this.subscriptionKey = null;
        return this;
    }

    /**
     * Sets the logging configuration for HTTP requests and responses.
     *
     * <p> If logLevel is not provided, default value of {@link HttpLogDetailLevel#NONE} is set.</p>
     *
     * @param logOptions The logging configuration to use when sending and receiving HTTP requests/responses.
     * @return The updated TextAnalyticsClientBuilder object.
     */
    public TextAnalyticsClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        this.httpLogOptions = logOptions;
        return this;
    }

    /**
     * Adds a policy to the set of existing policies that are executed after required policies.
     *
     * @param policy The retry policy for service requests.
     * @return The updated TextAnalyticsClientBuilder object.
     * @throws NullPointerException If {@code policy} is {@code null}.
     */
    public TextAnalyticsClientBuilder addPolicy(HttpPipelinePolicy policy) {
        Objects.requireNonNull(policy);
        policies.add(policy);
        return this;
    }

    /**
     * Sets the HTTP client to use for sending and receiving requests to and from the service.
     *
     * @param client The HTTP client to use for requests.
     * @return The updated TextAnalyticsClientBuilder object.
     */
    public TextAnalyticsClientBuilder httpClient(HttpClient client) {
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
     * {@link TextAnalyticsClientBuilder#endpoint(String) endpoint} to build {@link TextAnalyticsAsyncClient} or {@link
     * TextAnalyticsClient}.
     *
     * @param httpPipeline The HTTP pipeline to use for sending service requests and receiving responses.
     * @return The updated TextAnalyticsClientBuilder object.
     */
    public TextAnalyticsClientBuilder pipeline(HttpPipeline httpPipeline) {
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
     * @return The updated TextAnalyticsClientBuilder object.
     */
    public TextAnalyticsClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the {@link HttpPipelinePolicy} that is used when each request is sent.
     *
     * The default retry policy will be used if not provided {@link TextAnalyticsClientBuilder#buildAsyncClient()}
     * to build {@link TextAnalyticsAsyncClient} or {@link TextAnalyticsClient}.
     * @param retryPolicy user's retry policy applied to each request.
     * @return The updated TextAnalyticsClientBuilder object.
     */
    public TextAnalyticsClientBuilder retryPolicy(HttpPipelinePolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
        return this;
    }

    /**
     * Sets the {@link TextAnalyticsServiceVersion} that is used when making API requests.
     * <p>
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version the client library will have the result of potentially moving to a newer service version.
     *
     * @param version {@link TextAnalyticsServiceVersion} of the service to be used when making requests.
     * @return The updated TextAnalyticsClientBuilder object.
     */
    public TextAnalyticsClientBuilder serviceVersion(TextAnalyticsServiceVersion version) {
        this.version = version;
        return this;
    }
}
