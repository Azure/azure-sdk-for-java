// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.sms;

import com.azure.communication.common.implementation.CommunicationConnectionString;
import com.azure.communication.common.implementation.HmacAuthenticationPolicy;
import com.azure.communication.sms.implementation.AzureCommunicationSMSServiceImpl;
import com.azure.communication.sms.implementation.AzureCommunicationSMSServiceImplBuilder;
import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.client.traits.AzureKeyCredentialTrait;
import com.azure.core.client.traits.ConfigurationTrait;
import com.azure.core.client.traits.ConnectionStringTrait;
import com.azure.core.client.traits.EndpointTrait;
import com.azure.core.client.traits.HttpTrait;
import com.azure.core.client.traits.TokenCredentialTrait;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
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
import com.azure.core.util.CoreUtils;
import com.azure.core.util.HttpClientOptions;
import com.azure.core.util.builder.ClientBuilderUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * TelcoMessagingClientBuilder that creates TelcoMessagingAsyncClient and TelcoMessagingClient.
 */
@ServiceClientBuilder(serviceClients = { TelcoMessagingClient.class, TelcoMessagingAsyncClient.class })
public final class TelcoMessagingClientBuilder
    implements AzureKeyCredentialTrait<TelcoMessagingClientBuilder>, ConfigurationTrait<TelcoMessagingClientBuilder>,
    ConnectionStringTrait<TelcoMessagingClientBuilder>, EndpointTrait<TelcoMessagingClientBuilder>,
    HttpTrait<TelcoMessagingClientBuilder>, TokenCredentialTrait<TelcoMessagingClientBuilder> {

    private static final String SDK_NAME = "name";
    private static final String SDK_VERSION = "version";
    private static final String COMMUNICATION_CONNECTION_STRING = "communication.connection.string";

    private final ClientLogger logger = new ClientLogger(TelcoMessagingClientBuilder.class);
    private String endpoint;
    private AzureKeyCredential azureKeyCredential;
    private TokenCredential tokenCredential;
    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions;
    private HttpPipeline pipeline;
    private Configuration configuration;
    private final List<HttpPipelinePolicy> customPolicies = new ArrayList<>();
    private ClientOptions clientOptions;
    private RetryPolicy retryPolicy;
    private RetryOptions retryOptions;
    private SmsServiceVersion serviceVersion;

    /**
     * Create TelcoMessagingClientBuilder with default properties.
     */
    public TelcoMessagingClientBuilder() {
        httpLogOptions = new HttpLogOptions();
    }

    /**
     * Create TelcoMessagingAsyncClient with provided configuration.
     *
     * @return TelcoMessagingAsyncClient instance.
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)} and {@link #retryPolicy(RetryPolicy)}
     *     have been set.
     */
    public TelcoMessagingAsyncClient buildAsyncClient() {
        return new TelcoMessagingAsyncClient(createServiceImpl());
    }

    /**
     * Create TelcoMessagingClient with provided configuration.
     *
     * @return TelcoMessagingClient instance.
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)} and {@link #retryPolicy(RetryPolicy)}
     *     have been set.
     */
    public TelcoMessagingClient buildClient() {
        return new TelcoMessagingClient(buildAsyncClient());
    }

    /**
     * Set endpoint of the service.
     *
     * @param endpoint url of the service.
     * @return TelcoMessagingClientBuilder with updated endpoint.
     */
    @Override
    public TelcoMessagingClientBuilder endpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    /**
     * Set endpoint of the service with a fully built Http Pipeline.
     * Users can configure their own Http Pipeline as per their need.
     *
     * @param pipeline HttpPipeline to send requests through.
     * @return TelcoMessagingClientBuilder with updated endpoint.
     */
    @Override
    public TelcoMessagingClientBuilder pipeline(HttpPipeline pipeline) {
        this.pipeline = pipeline;
        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authorize requests sent to the service. Refer to the Azure SDK for Java
     * <a href="https://aka.ms/azsdk/java/docs/identity">identity and authentication</a>
     * documentation for more details on proper usage of the {@link TokenCredential} type.
     *
     * @param tokenCredential {@link TokenCredential} used to authorize requests sent to the service.
     * @return The updated {@link TelcoMessagingClientBuilder} object.
     */
    @Override
    public TelcoMessagingClientBuilder credential(TokenCredential tokenCredential) {
        this.tokenCredential = tokenCredential;
        return this;
    }

    /**
     * Sets the {@link AzureKeyCredential} used to authenticate HTTP requests.
     *
     * @param keyCredential The {@link AzureKeyCredential} used to authenticate HTTP requests.
     * @return The updated {@link TelcoMessagingClientBuilder} object.
     */
    @Override
    public TelcoMessagingClientBuilder credential(AzureKeyCredential keyCredential) {
        this.azureKeyCredential = keyCredential;
        return this;
    }

    /**
     * Set connectionString to use for creating TelcoMessagingAsyncClient.
     *
     * @param connectionString connectionString of the service.
     * @return TelcoMessagingClientBuilder with updated connectionString.
     */
    @Override
    public TelcoMessagingClientBuilder connectionString(String connectionString) {
        Objects.requireNonNull(connectionString, "'connectionString' cannot be null.");
        CommunicationConnectionString connectionStringObject = new CommunicationConnectionString(connectionString);
        String endpoint = connectionStringObject.getEndpoint();
        String accessKey = connectionStringObject.getAccessKey();
        this.endpoint(endpoint).credential(new AzureKeyCredential(accessKey));
        return this;
    }

    /**
     * Sets the {@link HttpClient} to use for sending and receiving requests to and from the service.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement
     * this trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param httpClient The {@link HttpClient} to use for requests.
     * @return The updated {@link TelcoMessagingClientBuilder} object.
     */
    @Override
    public TelcoMessagingClientBuilder httpClient(HttpClient httpClient) {
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
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement
     * this trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param logOptions The {@link HttpLogOptions logging configuration} to use when sending and receiving requests to
     * and from the service.
     * @return The updated {@link TelcoMessagingClientBuilder} object.
     */
    @Override
    public TelcoMessagingClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        this.httpLogOptions = logOptions;
        return this;
    }

    /**
     * Sets the {@link Configuration configuration} object used to retrieve environment configuration values during
     * building of the client.
     *
     * @param configuration {@link Configuration} object used to retrieve environment configuration values during
     * building of the client.
     * @return The updated {@link TelcoMessagingClientBuilder} object.
     */
    @Override
    public TelcoMessagingClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the {@link ClientOptions} which enables various options to be set on the client. For example setting an
     * {@code applicationId} using {@link ClientOptions#setApplicationId(String)} to configure
     * the {@link UserAgentPolicy} for telemetry/monitoring purposes.
     *
     * <p>More About <a href="https://azure.github.io/azure-sdk/general_azurecore.html#telemetry-policy">Azure Core: Telemetry policy</a>
     *
     * @param clientOptions the {@link ClientOptions} to be set on the client.
     * @return The updated {@link TelcoMessagingClientBuilder} object.
     */
    public TelcoMessagingClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
        return this;
    }

    /**
     * Adds a {@link HttpPipelinePolicy pipeline policy} to apply on each request sent.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement
     * this trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param customPolicy A {@link HttpPipelinePolicy pipeline policy}.
     * @return The updated {@link TelcoMessagingClientBuilder} object.
     * @throws NullPointerException If {@code customPolicy} is {@code null}.
     */
    public TelcoMessagingClientBuilder addPolicy(HttpPipelinePolicy customPolicy) {
        Objects.requireNonNull(customPolicy, "'customPolicy' cannot be null.");
        customPolicies.add(customPolicy);
        return this;
    }

    /**
     * Sets the {@link RetryPolicy} that is used when each request is sent.
     *
     * The default retry policy will be used if not provided by the user. Setting this is mutually exclusive with
     * using {@link #retryOptions(RetryOptions)}.
     *
     * @param retryPolicy user's retry policy applied to each request.
     * @return The updated {@link TelcoMessagingClientBuilder} object.
     */
    public TelcoMessagingClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
        return this;
    }

    /**
     * Sets the {@link RetryOptions} for all the requests made through the client.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement
     * this trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     * <p>
     * Setting this is mutually exclusive with using {@link #retryPolicy(RetryPolicy)}.
     *
     * @param retryOptions The {@link RetryOptions} to use for all the requests made through the client.
     * @return The updated {@link TelcoMessagingClientBuilder} object.
     */
    @Override
    public TelcoMessagingClientBuilder retryOptions(RetryOptions retryOptions) {
        this.retryOptions = retryOptions;
        return this;
    }

    /**
     * Sets the {@link SmsServiceVersion} that is used when making API requests.
     * <p>
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version and so newer version of the client library may result in moving to a newer service version.
     *
     * @param version {@link SmsServiceVersion} of the service API used when making requests.
     * @return The updated {@link TelcoMessagingClientBuilder} object.
     */
    public TelcoMessagingClientBuilder serviceVersion(SmsServiceVersion version) {
        this.serviceVersion = version;
        return this;
    }

    private AzureCommunicationSMSServiceImpl createServiceImpl() {
        Objects.requireNonNull(endpoint, "'endpoint' cannot be null.");

        HttpPipeline builderPipeline = this.pipeline;
        if (builderPipeline == null) {
            builderPipeline = createHttpPipeline();
        }

        SmsServiceVersion buildServiceVersion
            = (serviceVersion != null) ? serviceVersion : SmsServiceVersion.getLatest();

        return new AzureCommunicationSMSServiceImplBuilder().endpoint(endpoint)
            .pipeline(builderPipeline)
            .apiVersion(buildServiceVersion.getVersion())
            .buildClient();
    }

    private HttpPipeline createHttpPipeline() {
        Configuration buildConfiguration
            = (configuration == null) ? Configuration.getGlobalConfiguration() : configuration;
        ClientOptions buildClientOptions = (clientOptions == null) ? new ClientOptions() : clientOptions;
        HttpLogOptions buildLogOptions = (httpLogOptions == null) ? new HttpLogOptions() : httpLogOptions;

        String applicationId = CoreUtils.getApplicationId(buildClientOptions, buildLogOptions);

        // Closest scopes based on the endpoint
        String[] scopes = new String[] { endpoint.endsWith("/") ? endpoint + ".default" : endpoint + "/.default" };

        List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new UserAgentPolicy(applicationId, SDK_NAME, SDK_VERSION, buildConfiguration));
        policies.add(new RequestIdPolicy());

        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        policies.add(ClientBuilderUtil.validateAndGetRetryPolicy(retryPolicy, retryOptions));

        policies.add(new CookiePolicy());

        if (tokenCredential != null) {
            policies.add(new BearerTokenAuthenticationPolicy(tokenCredential, scopes));
        } else if (azureKeyCredential != null) {
            policies.add(new HmacAuthenticationPolicy(azureKeyCredential));
        } else {
            // Throw exception that credential and tokenCredential cannot both be null
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Missing credential information while building a client."));
        }

        policies.addAll(this.customPolicies);
        HttpPolicyProviders.addAfterRetryPolicies(policies);

        policies.add(new HttpLoggingPolicy(buildLogOptions));

        return new HttpPipelineBuilder().policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .clientOptions(buildClientOptions)
            .build();
    }

    /**
     * Utility class for providing HttpPipelinePolicy collections.
     */
    private static class HttpPolicyProviders {
        static void addBeforeRetryPolicies(List<HttpPipelinePolicy> policies) {
            // Add any pre-retry policies here
        }

        static void addAfterRetryPolicies(List<HttpPipelinePolicy> policies) {
            // Add any post-retry policies here
        }
    }
}
