// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat;

import com.azure.communication.chat.implementation.AzureCommunicationChatServiceImpl;
import com.azure.communication.chat.implementation.AzureCommunicationChatServiceImplBuilder;
import com.azure.communication.chat.implementation.CommunicationBearerTokenCredential;
import com.azure.communication.common.CommunicationTokenCredential;
import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.client.traits.ConfigurationTrait;
import com.azure.core.client.traits.EndpointTrait;
import com.azure.core.client.traits.HttpTrait;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.CookiePolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.HttpClientOptions;
import com.azure.core.util.builder.ClientBuilderUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Builder for creating clients of Azure Communication Service Chat Threads
 */
@ServiceClientBuilder(serviceClients = {ChatThreadAsyncClient.class, ChatThreadClient.class})
public final class ChatThreadClientBuilder implements
    ConfigurationTrait<ChatThreadClientBuilder>,
    EndpointTrait<ChatThreadClientBuilder>,
    HttpTrait<ChatThreadClientBuilder> {

    private String chatThreadId;
    private String endpoint;
    private HttpClient httpClient;
    private CommunicationTokenCredential communicationTokenCredential;
    private final List<HttpPipelinePolicy> customPolicies = new ArrayList<HttpPipelinePolicy>();
    private HttpLogOptions logOptions = new HttpLogOptions();
    private HttpPipeline httpPipeline;
    private Configuration configuration;
    private ClientOptions clientOptions;
    private RetryPolicy retryPolicy;
    private RetryOptions retryOptions;

    private static final String APP_CONFIG_PROPERTIES = "azure-communication-chat.properties";
    private static final String SDK_NAME = "name";
    private static final String SDK_VERSION = "version";

    /**
     * Set endpoint of the service
     *
     * @param endpoint url of the service
     * @return the updated ChatThreadClientBuilder object
     */
    @Override
    public ChatThreadClientBuilder endpoint(String endpoint) {
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
     * @return the updated ChatThreadClientBuilder object
     */
    @Override
    public ChatThreadClientBuilder httpClient(HttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient, "'httpClient' cannot be null.");
        return this;
    }

    /**
     * Set a token credential for authorization
     *
     * @param communicationTokenCredential valid token credential as a string
     * @return the updated ChatThreadClientBuilder object
     */
    public ChatThreadClientBuilder credential(CommunicationTokenCredential communicationTokenCredential) {
        this.communicationTokenCredential = Objects.requireNonNull(
            communicationTokenCredential, "'communicationTokenCredential' cannot be null.");
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
     * @return The updated ChatThreadClientBuilder object.
     * @see HttpClientOptions
     */
    @Override
    public ChatThreadClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
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
     * @return the updated ChatThreadClientBuilder object
     * @throws NullPointerException If {@code pipelinePolicy} is {@code null}.
     */
    @Override
    public ChatThreadClientBuilder addPolicy(HttpPipelinePolicy customPolicy) {
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
     * @return The updated ChatThreadClientBuilder object.
     */
    public ChatThreadClientBuilder retryPolicy(RetryPolicy retryPolicy) {
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
     * @return The updated ChatThreadClientBuilder object.
     */
    @Override
    public ChatThreadClientBuilder retryOptions(RetryOptions retryOptions) {
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
     * @return the updated ChatThreadClientBuilder object
     */
    @Override
    public ChatThreadClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        this.logOptions = Objects.requireNonNull(logOptions, "'logOptions' cannot be null.");
        return this;
    }

    /**
     * Sets the {@link ChatServiceVersion} that is used when making API requests.
     * <p>
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version of the client library will have the result of potentially moving to a newer service version.
     * <p>
     * Targeting a specific service version may also mean that the service will return an error for newer APIs.
     *
     * @param version {@link ChatServiceVersion} of the service to be used when making requests.
     * @return the updated ChatThreadClientBuilder object
     */
    public ChatThreadClientBuilder serviceVersion(ChatServiceVersion version) {
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
     * @return the updated ChatThreadClientBuilder object
     */
    @Override
    public ChatThreadClientBuilder pipeline(HttpPipeline httpPipeline) {
        this.httpPipeline = httpPipeline;
        return this;
    }

    /**
     * Sets the configuration object used to retrieve environment configuration values during building of the client.
     *
     * @param configuration Configuration store used to retrieve environment configurations.
     * @return the updated ChatThreadClientBuilder object
     */
    @Override
    public ChatThreadClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the ChatThreadId used to construct a client for this chat thread.
     *
     * @param chatThreadId The id of the chat thread.
     * @return the updated ChatThreadClientBuilder object
     */
    public ChatThreadClientBuilder chatThreadId(String chatThreadId) {
        this.chatThreadId = Objects.requireNonNull(chatThreadId, "'chatThreadId' cannot be null.");
        return this;
    }

    /**
     * Create synchronous chat thread client applying CommunicationTokenCredential, UserAgentPolicy,
     * RetryPolicy, and CookiePolicy.
     * Additional HttpPolicies specified by additionalPolicies will be applied after them
     *
     * @return ChatThreadClient instance
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)}
     * and {@link #retryPolicy(RetryPolicy)} have been set.
     */
    public ChatThreadClient buildClient() {
        Objects.requireNonNull(chatThreadId);
        ChatThreadAsyncClient asyncClient = buildAsyncClient();
        return new ChatThreadClient(asyncClient);
    }

    /**
     * Create asynchronous chat thread client applying CommunicationTokenCredential, UserAgentPolicy,
     * RetryPolicy, and CookiePolicy.
     * Additional HttpPolicies specified by additionalPolicies will be applied after them
     *
     * @return ChatThreadAsyncClient instance
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)}
     * and {@link #retryPolicy(RetryPolicy)} have been set.
     */
    public ChatThreadAsyncClient buildAsyncClient() {
        Objects.requireNonNull(chatThreadId);
        AzureCommunicationChatServiceImpl internalClient = createInternalClient();
        return new ChatThreadAsyncClient(internalClient, chatThreadId);
    }

    private AzureCommunicationChatServiceImpl createInternalClient() {
        Objects.requireNonNull(endpoint);

        HttpPipeline pipeline;
        if (httpPipeline != null) {
            pipeline = httpPipeline;
        } else {
            Objects.requireNonNull(communicationTokenCredential);
            Objects.requireNonNull(httpClient);
            CommunicationBearerTokenCredential tokenCredential =
                new CommunicationBearerTokenCredential(communicationTokenCredential);

            pipeline = createHttpPipeline(httpClient,
                new BearerTokenAuthenticationPolicy(tokenCredential, ""),
                customPolicies);
        }

        AzureCommunicationChatServiceImplBuilder clientBuilder = new AzureCommunicationChatServiceImplBuilder()
            .endpoint(endpoint)
            .pipeline(pipeline);

        return clientBuilder.buildClient();
    }

    private HttpPipeline createHttpPipeline(HttpClient httpClient,
                                            HttpPipelinePolicy authorizationPolicy,
                                            List<HttpPipelinePolicy> additionalPolicies) {

        List<HttpPipelinePolicy> policies = new ArrayList<HttpPipelinePolicy>();
        policies.add(getUserAgentPolicy());
        policies.add(new RequestIdPolicy());
        policies.add(ClientBuilderUtil.validateAndGetRetryPolicy(retryPolicy, retryOptions));
        policies.add(new CookiePolicy());
        // auth policy is per request, should be after retry
        policies.add(authorizationPolicy);
        policies.add(new HttpLoggingPolicy(logOptions));

        if (additionalPolicies != null && additionalPolicies.size() > 0) {
            policies.addAll(additionalPolicies);
        }

        return new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();
    }


    /*
     * Creates a {@link UserAgentPolicy} using the default chat service module name and version.
     *
     * @return The default {@link UserAgentPolicy} for the module.
     */
    private UserAgentPolicy getUserAgentPolicy() {
        Map<String, String> properties = CoreUtils.getProperties(APP_CONFIG_PROPERTIES);

        String clientName = properties.getOrDefault(SDK_NAME, "UnknownName");
        String clientVersion = properties.getOrDefault(SDK_VERSION, "UnknownVersion");

        String applicationId = logOptions.getApplicationId();
        if (this.clientOptions != null) {
            applicationId = this.clientOptions.getApplicationId();
        }

        return new UserAgentPolicy(applicationId, clientName, clientVersion, configuration);
    }
}
