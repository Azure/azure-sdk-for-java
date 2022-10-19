// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.client.traits.ConfigurationTrait;
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
import com.azure.core.util.builder.ClientBuilderUtil;
import com.azure.core.util.serializer.JsonSerializer;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of {@link DigitalTwinsClient
 * DigitalTwinsClients} and {@link DigitalTwinsAsyncClient DigitalTwinsAsyncClients}, call {@link #buildClient() buildClient} and {@link
 * #buildAsyncClient() buildAsyncClient} respectively to construct an instance of the desired client.
 */
@ServiceClientBuilder(serviceClients = {DigitalTwinsClient.class, DigitalTwinsAsyncClient.class})
public final class DigitalTwinsClientBuilder implements
    ConfigurationTrait<DigitalTwinsClientBuilder>,
    EndpointTrait<DigitalTwinsClientBuilder>,
    HttpTrait<DigitalTwinsClientBuilder>,
    TokenCredentialTrait<DigitalTwinsClientBuilder> {
    private static final String[] ADT_PUBLIC_SCOPE = new String[]{"https://digitaltwins.azure.net" + "/.default"};

    // This is the name of the properties file in this repo that contains the default properties
    private static final String DIGITAL_TWINS_PROPERTIES = "azure-digital-twins.properties";

    // These are the keys to the above properties file that define the client library's name and version for use in the user agent string
    private static final String SDK_NAME = "name";
    private static final String SDK_VERSION = "version";

    private final List<HttpPipelinePolicy> perCallPolicies = new ArrayList<>();
    private final List<HttpPipelinePolicy> perRetryPolicies = new ArrayList<>();

    // mandatory
    private String endpoint;
    private TokenCredential tokenCredential;

    // optional/have default values
    private DigitalTwinsServiceVersion serviceVersion;
    private ClientOptions clientOptions;
    private HttpPipeline httpPipeline;
    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions;
    private RetryPolicy retryPolicy;
    private RetryOptions retryOptions;
    private JsonSerializer jsonSerializer;

    // Right now, Azure Digital Twins does not send a retry-after header on its throttling messages. If it adds support later, then
    // these values should match the header name (for instance, "x-ms-retry-after-ms" or "Retry-After") and the time unit
    // of the header's value. These null values are equivalent to just constructing "new RetryPolicy()". It is safe
    // to use a null retryAfterHeader and a null retryAfterTimeUnit when constructing this retry policy as this
    // constructor interprets that as saying "this service does not support retry after headers"
    private static final String RETRY_AFTER_HEADER = null;
    private static final ChronoUnit RETRY_AFTER_TIME_UNIT = null;
    private static final RetryPolicy DEFAULT_RETRY_POLICY = new RetryPolicy(RETRY_AFTER_HEADER, RETRY_AFTER_TIME_UNIT);

    private final Map<String, String> properties;

    private Configuration configuration;

    /**
     * The public constructor for DigitalTwinsClientBuilder
     */
    public DigitalTwinsClientBuilder() {
        properties = CoreUtils.getProperties(DIGITAL_TWINS_PROPERTIES);
        httpLogOptions = new HttpLogOptions();
    }

    private static HttpPipeline setupPipeline(
        TokenCredential tokenCredential,
        String endpoint,
        HttpLogOptions httpLogOptions,
        ClientOptions clientOptions,
        HttpClient httpClient,
        List<HttpPipelinePolicy> perCallPolicies,
        List<HttpPipelinePolicy> perRetryPolicies,
        HttpPipelinePolicy retryPolicy,
        Configuration configuration,
        Map<String, String> properties) {
        // Closest to API goes first, closest to wire goes last.
        List<HttpPipelinePolicy> policies = new ArrayList<>();

        String clientName = properties.getOrDefault(SDK_NAME, "UnknownName");
        String clientVersion = properties.getOrDefault(SDK_VERSION, "UnknownVersion");

        // Give precedence to applicationId configured in clientOptions over the one configured in httpLogOptions.
        // Azure.Core deprecated setting the applicationId in httpLogOptions, but we should still support it.
        String applicationId = clientOptions == null
            ? httpLogOptions.getApplicationId()
            : clientOptions.getApplicationId();

        policies.add(new UserAgentPolicy(applicationId, clientName, clientVersion, configuration));

        // Adds a "x-ms-client-request-id" header to each request. This header is useful for tracing requests through Azure ecosystems
        policies.add(new RequestIdPolicy());

        policies.addAll(perCallPolicies);

        // Only the RequestIdPolicy  and UserAgentPolicy will take effect prior to the retry policy since neither of those need
        // to change in any way upon retry
        HttpPolicyProviders.addBeforeRetryPolicies(policies);

        policies.add(retryPolicy);

        // Adds a date header to each HTTP request for tracking purposes
        policies.add(new AddDatePolicy());

        // Add authentication policy so that each HTTP request has authorization header
        HttpPipelinePolicy credentialPolicy = new BearerTokenAuthenticationPolicy(tokenCredential, ADT_PUBLIC_SCOPE);
        policies.add(credentialPolicy);

        policies.addAll(perRetryPolicies);

        // If client options has headers configured, add a policy for each
        if (clientOptions != null) {
            List<HttpHeader> httpHeaderList = new ArrayList<>();
            clientOptions.getHeaders().forEach(header ->
                httpHeaderList.add(new HttpHeader(header.getName(), header.getValue())));
            policies.add(new AddHeadersPolicy(new HttpHeaders(httpHeaderList)));
        }

        // Custom policies, authentication policy, and add date policy all take place after the retry policy which means
        // they will be applied once per http request, and once for every retried http request. For example, the
        // AddDatePolicy will add a date time header for each request that is sent, and if the http request fails
        // and the retry policy dictates that the request should be retried, then the date time header policy will
        // be applied again and the current date time will be put in the header instead of the date time from
        // the first http request attempt.
        HttpPolicyProviders.addAfterRetryPolicies(policies);

        policies.add(new HttpLoggingPolicy(httpLogOptions));

        return new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();
    }

    /**
     * Create a {@link DigitalTwinsClient} based on the builder settings.
     *
     * @return the created synchronous DigitalTwinsClient
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)}
     *      and {@link #retryPolicy(RetryPolicy)} have been set.
     */
    public DigitalTwinsClient buildClient() {
        return new DigitalTwinsClient(buildAsyncClient());
    }

    /**
     * Create a {@link DigitalTwinsAsyncClient} based on the builder settings.
     *
     * @return the created asynchronous DigitalTwinsAsyncClient
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)}
     *      and {@link #retryPolicy(RetryPolicy)} have been set.
     */
    public DigitalTwinsAsyncClient buildAsyncClient() {
        Objects.requireNonNull(tokenCredential, "'tokenCredential' cannot be null.");
        Objects.requireNonNull(endpoint, "'endpoint' cannot be null");

        Configuration buildConfiguration = this.configuration;
        if (buildConfiguration == null) {
            buildConfiguration = Configuration.getGlobalConfiguration().clone();
        }

        // Set defaults for these fields if they were not set while building the client
        DigitalTwinsServiceVersion serviceVersion = this.serviceVersion;
        if (serviceVersion == null) {
            serviceVersion = DigitalTwinsServiceVersion.getLatest();
        }

        // Default is exponential backoff
        HttpPipelinePolicy retryPolicy = ClientBuilderUtil.validateAndGetRetryPolicy(this.retryPolicy,
            retryOptions, DEFAULT_RETRY_POLICY);

        if (this.httpPipeline == null) {
            this.httpPipeline = setupPipeline(
                this.tokenCredential,
                this.endpoint,
                this.httpLogOptions,
                this.clientOptions,
                this.httpClient,
                this.perCallPolicies,
                this.perRetryPolicies,
                retryPolicy,
                buildConfiguration,
                this.properties);
        }

        return new DigitalTwinsAsyncClient(this.endpoint, this.httpPipeline, serviceVersion, this.jsonSerializer);
    }

    /**
     * Set the service endpoint that the built client will communicate with. This field is mandatory to set.
     *
     * @param endpoint URL of the service.
     * @return the updated DigitalTwinsClientBuilder instance for fluent building.
     */
    @Override
    public DigitalTwinsClientBuilder endpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authorize requests sent to the service. Refer to the Azure SDK for Java
     * <a href="https://aka.ms/azsdk/java/docs/identity">identity and authentication</a>
     * documentation for more details on proper usage of the {@link TokenCredential} type.
     *
     * @param tokenCredential {@link TokenCredential} used to authorize requests sent to the service.
     * @return the updated DigitalTwinsClientBuilder instance for fluent building.
     */
    @Override
    public DigitalTwinsClientBuilder credential(TokenCredential tokenCredential) {
        this.tokenCredential = tokenCredential;
        return this;
    }

    /**
     * Sets the {@link DigitalTwinsServiceVersion} that is used when making API requests.
     * <p>
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version of the client library will have the result of potentially moving to a newer service version.
     * <p>
     * Targeting a specific service version may also mean that the service will return an error for newer APIs.
     *
     * @param serviceVersion The service API version to use.
     * @return the updated DigitalTwinsClientBuilder instance for fluent building.
     */
    public DigitalTwinsClientBuilder serviceVersion(DigitalTwinsServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
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
     * @param httpClient The {@link HttpClient} to use for requests.
     * @return the updated DigitalTwinsClientBuilder instance for fluent building.
     */
    @Override
    public DigitalTwinsClientBuilder httpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    /**
     * Sets the {@link HttpLogOptions logging configuration} to use when sending and receiving requests to and from
     * the service. If a {@code logLevel} is not provided, default value of {@link HttpLogDetailLevel#NONE} is set.
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
     * @return the updated DigitalTwinsClientBuilder instance for fluent building.
     */
    @Override
    public DigitalTwinsClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        this.httpLogOptions = logOptions;
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
     * @param pipelinePolicy A {@link HttpPipelinePolicy pipeline policy}.
     * @return the updated DigitalTwinsClientBuilder instance for fluent building.
     * @throws NullPointerException If {@code pipelinePolicy} is {@code null}.
     */
    @Override
    public DigitalTwinsClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        Objects.requireNonNull(pipelinePolicy, "'pipelinePolicy' cannot be null.");

        if (pipelinePolicy.getPipelinePosition() == HttpPipelinePosition.PER_CALL) {
            perCallPolicies.add(pipelinePolicy);
        } else {
            perRetryPolicies.add(pipelinePolicy);
        }

        return this;
    }

    /**
     * Sets the {@link HttpPipelinePolicy} that is used as the retry policy for each request that is sent.
     *
     * The default retry policy will be used if not provided. The default retry policy is {@link RetryPolicy#RetryPolicy()}.
     * For implementing custom retry logic, see {@link RetryPolicy} as an example.
     *
     * Setting this is mutually exclusive with using {@link #retryOptions(RetryOptions)}.
     *
     * @param retryPolicy the retry policy applied to each request.
     * @return the updated DigitalTwinsClientBuilder instance for fluent building.
     */
    public DigitalTwinsClientBuilder retryPolicy(RetryPolicy retryPolicy) {
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
     * Setting this is mutually exclusive with using {@link #retryPolicy(RetryPolicy)}.
     *
     * @param retryOptions The {@link RetryOptions} to use for all the requests made through the client.
     * @return the updated DigitalTwinsClientBuilder instance for fluent building.
     */
    @Override
    public DigitalTwinsClientBuilder retryOptions(RetryOptions retryOptions) {
        this.retryOptions = retryOptions;
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
     * If {@code pipeline} is set, all other settings are ignored, aside from {@link #endpoint(String) endpoint}.
     *
     * @param httpPipeline {@link HttpPipeline} to use for sending service requests and receiving responses.
     * @return the updated DigitalTwinsClientBuilder instance for fluent building.
     */
    @Override
    public DigitalTwinsClientBuilder pipeline(HttpPipeline httpPipeline) {
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
     * @return The updated DigitalTwinsClientBuilder object for fluent building.
     */
    @Override
    public DigitalTwinsClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Custom JSON serializer that is used to handle model types that are not contained in the Azure Digital Twins library.
     *
     * @param jsonSerializer The serializer to deserialize response payloads into user defined models.
     * @return The updated DigitalTwinsClientBuilder object.
     */
    public DigitalTwinsClientBuilder serializer(JsonSerializer jsonSerializer) {
        this.jsonSerializer = jsonSerializer;
        return this;
    }

    /**
     * Allows for setting common properties such as application ID, headers, proxy configuration, etc. Note that it is
     * recommended that this method be called with an instance of the {@link HttpClientOptions}
     * class (a subclass of the {@link ClientOptions} base class). The HttpClientOptions subclass provides more
     * configuration options suitable for HTTP clients, which is applicable for any class that implements this HttpTrait
     * interface.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param clientOptions A configured instance of {@link HttpClientOptions}.
     * @return The updated DigitalTwinsClientBuilder object.
     * @see HttpClientOptions
     */
    @Override
    public DigitalTwinsClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
        return this;
    }
}
