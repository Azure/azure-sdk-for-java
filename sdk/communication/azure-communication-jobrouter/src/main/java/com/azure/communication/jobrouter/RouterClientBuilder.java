// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter;

import com.azure.communication.common.implementation.CommunicationConnectionString;
import com.azure.communication.common.implementation.HmacAuthenticationPolicy;
import com.azure.communication.jobrouter.implementation.AzureCommunicationRoutingServiceImpl;
import com.azure.communication.jobrouter.implementation.AzureCommunicationRoutingServiceImplBuilder;
import com.azure.communication.jobrouter.implementation.utils.BuilderHelper;
import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.client.traits.ConfigurationTrait;
import com.azure.core.client.traits.EndpointTrait;
import com.azure.core.client.traits.HttpTrait;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.FixedDelayOptions;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.HttpClientOptions;
import com.azure.core.util.logging.ClientLogger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Client builder for sync and async job router clients.
 */
@ServiceClientBuilder(serviceClients = {RouterAsyncClient.class, RouterClient.class})
public class RouterClientBuilder implements ConfigurationTrait<RouterClientBuilder>,
    EndpointTrait<RouterClientBuilder>,
    HttpTrait<RouterClientBuilder> {
    private static final ClientLogger LOGGER = new ClientLogger(RouterClientBuilder.class);

    private String endpoint;
    private HttpClient httpClient;
    private AzureKeyCredential credential;
    private HttpPipeline httpPipeline;
    private final List<HttpPipelinePolicy> customPolicies = new ArrayList<HttpPipelinePolicy>();
    private RetryPolicy retryPolicy;
    private HttpLogOptions logOptions;
    private ClientOptions clientOptions;
    private RetryOptions retryOptions;
    private Configuration configuration;
    private CommunicationConnectionString connectionString;

    /**
     * Sets the configuration object used to retrieve environment configuration values during building of the client.
     *
     * @param configuration Configuration store used to retrieve environment configurations.
     * @return the updated RouterClientBuilder object
     */
    @Override
    public RouterClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Set endpoint of the service
     *
     * @param endpoint url of the service
     * @return the updated RouterClientBuilder object
     */
    @Override
    public RouterClientBuilder endpoint(String endpoint) {
        this.endpoint = Objects.requireNonNull(endpoint, "'endpoint' cannot be null.");
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
     * @return the updated RouterClientBuilder object
     */
    @Override
    public RouterClientBuilder httpClient(HttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient, "'httpClient' cannot be null.");
        return this;
    }

    /**
     * Set a connection string for authorization
     *
     * @param connectionString valid token credential as a string
     * @return the updated RouterClientBuilder object
     */
    public RouterClientBuilder connectionString(String connectionString) {
        this.connectionString = new CommunicationConnectionString(connectionString);
        this.credential(new AzureKeyCredential(this.connectionString.getAccessKey()));
        this.endpoint(this.connectionString.getEndpoint());
        return this;
    }

    /**
     * Set a key credential for authorization
     *
     * @param credential valid credential as a string
     * @return the updated RouterClientBuilder object
     */
    public RouterClientBuilder credential(AzureKeyCredential credential) {
        this.credential = Objects.requireNonNull(
            credential, "'credential' cannot be null.");
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
     *
     * If {@code pipeline} is set, all other settings are ignored, aside from {@link #endpoint(String) endpoint}.
     *
     * @param httpPipeline {@link HttpPipeline} to use for sending service requests and receiving responses.
     * @return the updated RouterClientBuilder object
     */
    @Override
    public RouterClientBuilder pipeline(HttpPipeline httpPipeline) {
        this.httpPipeline = httpPipeline;
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
     * @param customPolicy A {@link HttpPipelinePolicy pipeline policy}.
     * @return the updated RouterClientBuilder object
     * @throws NullPointerException If {@code pipelinePolicy} is {@code null}.
     */
    @Override
    public RouterClientBuilder addPolicy(HttpPipelinePolicy customPolicy) {
        this.customPolicies.add(Objects.requireNonNull(customPolicy, "'customPolicy' cannot be null."));
        return this;
    }

    /**
     * Sets the {@link HttpPipelinePolicy} that will attempt to retry requests when needed.
     * <p>
     * A default retry policy will be supplied if one isn't provided.
     * <p>
     * Setting this is mutually exclusive with using {@link #retryOptions(RetryOptions)}.
     *
     * @param retryPolicy The {@link RetryPolicy} that will attempt to retry requests when needed.
     * @return The updated RouterClientBuilder object.
     */
    public RouterClientBuilder retryPolicy(RetryPolicy retryPolicy) {
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
     * @return The updated RouterClientBuilder object.
     */
    @Override
    public RouterClientBuilder retryOptions(RetryOptions retryOptions) {
        this.retryOptions = retryOptions;
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
     * @return the updated RouterClientBuilder object
     */
    @Override
    public RouterClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        this.logOptions = Objects.requireNonNull(logOptions, "'logOptions' cannot be null.");
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
     * @return The updated RouterClientBuilder object.
     * @see HttpClientOptions
     */
    @Override
    public RouterClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
        return this;
    }

    /**
     * Create synchronous router client applying CommunicationTokenCredential, UserAgentPolicy,
     * RetryPolicy, and CookiePolicy.
     * Additional HttpPolicies specified by additionalPolicies will be applied after them
     *
     * @return RouterClient instance
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)}
     * and {@link #retryPolicy(RetryPolicy)} have been set.
     */
    public RouterClient buildClient() {
        RouterAsyncClient asyncClient = buildAsyncClient();
        return new RouterClient(asyncClient);
    }

    /**
     * Create asynchronous job router client applying CommunicationTokenCredential, UserAgentPolicy,
     * RetryPolicy, and CookiePolicy.
     * Additional HttpPolicies specified by additionalPolicies will be applied after them
     *
     * @return RouterAsyncClient instance
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)}
     * and {@link #retryPolicy(RetryPolicy)} have been set.
     */
    public RouterAsyncClient buildAsyncClient() {
        AzureCommunicationRoutingServiceImpl internalClient = createInternalClient();
        return new RouterAsyncClient(internalClient);
    }

    private AzureCommunicationRoutingServiceImpl createInternalClient() {
        HttpPipeline pipeline;
        if (httpPipeline != null) {
            pipeline = httpPipeline;
        } else {
            retryOptions = retryOptions != null ? retryOptions : new RetryOptions(new FixedDelayOptions(3, Duration.ofMillis(5)));
            logOptions = logOptions != null ? logOptions : new HttpLogOptions();
            clientOptions = clientOptions != null ? clientOptions :  new ClientOptions();
            pipeline = BuilderHelper.buildPipeline(
                new HmacAuthenticationPolicy(credential),
                retryOptions,
                logOptions,
                clientOptions,
                httpClient,
                customPolicies,
                null,
                configuration,
                LOGGER);
        }

        AzureCommunicationRoutingServiceImplBuilder clientBuilder = new AzureCommunicationRoutingServiceImplBuilder()
            .endpoint(endpoint)
            .pipeline(pipeline);

        return clientBuilder.buildClient();
    }
}
