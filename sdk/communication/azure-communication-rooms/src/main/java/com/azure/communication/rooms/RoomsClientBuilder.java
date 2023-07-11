// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms;


import com.azure.communication.common.implementation.CommunicationConnectionString;
import com.azure.communication.common.implementation.HmacAuthenticationPolicy;
import com.azure.communication.rooms.implementation.AzureCommunicationRoomServiceImpl;
import com.azure.communication.rooms.implementation.AzureCommunicationRoomServiceImplBuilder;
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
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.builder.ClientBuilderUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * RoomsClientBuilder that creates RoomsAsyncClient and RoomsClient.
 *
 * <p>
 * <strong>Instantiating a Rooms CLient Builder</strong>
 * </p>
 *
 *
 * <!-- src_embed readme-sample-createRoomsCLientBuilder -->
 * <pre>
 * RoomsClientBuilder builder = new RoomsClientBuilder&#40;&#41;;
 * </pre>
 * <!-- end readme-sample-createRoomsCLientBuilder -->
 *
 *
 * <p>
 * <strong>Using room client builder build a Rooms Client</strong>
 * </p>
 *
 * <!-- src_embed readme-sample-createRoomsClientUsingAzureKeyCredential
 * -->
 *
 * <pre>
 * RoomsClient roomsClient = new RoomsClientBuilder()
 *      .endpoint&#40;endpoint&#41;
 *      .credential&#40;azureKeyCredential&#41;
 *      .buildClient&#40;&#41;;
 * </pre>
 *
 * <!-- end readme-sample-createRoomsClientUsingAzureKeyCredential -->
 */
@ServiceClientBuilder(serviceClients = {RoomsClient.class, RoomsAsyncClient.class})
public final class RoomsClientBuilder implements HttpTrait<RoomsClientBuilder>, ConfigurationTrait<RoomsClientBuilder>, ConnectionStringTrait<RoomsClientBuilder>, TokenCredentialTrait<RoomsClientBuilder>, AzureKeyCredentialTrait<RoomsClientBuilder>, EndpointTrait<RoomsClientBuilder> {
    private static final String SDK_NAME = "name";
    private static final String SDK_VERSION = "version";
    private static final String APP_CONFIG_PROPERTIES = "azure-communication-rooms.properties";

    private final ClientLogger logger = new ClientLogger(RoomsClientBuilder.class);
    private String endpoint;
    private AzureKeyCredential azureKeyCredential;
    private TokenCredential tokenCredential;
    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions = new HttpLogOptions();
    private HttpPipeline pipeline;
    private Configuration configuration;
    private final Map<String, String> properties = CoreUtils.getProperties(APP_CONFIG_PROPERTIES);
    private final List<HttpPipelinePolicy> customPolicies = new ArrayList<HttpPipelinePolicy>();
    private ClientOptions clientOptions;
    private RetryPolicy retryPolicy;
    private RetryOptions retryOptions;
    private RoomsServiceVersion serviceVersion;

    /**
     * Set endpoint of the service
     *
     * @param endpoint url of the service
     * @return RoomsClientBuilder
     */
    @Override
    public RoomsClientBuilder endpoint(String endpoint) {
        this.endpoint = Objects.requireNonNull(endpoint, "'endpoint' cannot be null.");
        return this;
    }

    /**
     * Set endpoint of the service
     *
     * @param pipeline HttpPipeline to use, if a pipeline is not
     * supplied, the credential and httpClient fields must be set
     * @return RoomsClientBuilder
     */
    @Override
    public RoomsClientBuilder pipeline(HttpPipeline pipeline) {
        this.pipeline = Objects.requireNonNull(pipeline, "'pipeline' cannot be null.");
        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authenticate HTTP requests.
     *
     * @param tokenCredential {@link TokenCredential} used to authenticate HTTP requests.
     * @return The updated {@link RoomsClientBuilder} object.
     * @throws NullPointerException If {@code tokenCredential} is null.
     */
    @Override
    public RoomsClientBuilder credential(TokenCredential tokenCredential) {
        this.tokenCredential = Objects.requireNonNull(tokenCredential, "'tokenCredential' cannot be null.");
        return this;
    }

    /**
     * Sets the {@link AzureKeyCredential} used to authenticate HTTP requests.
     *
     * @param keyCredential The {@link AzureKeyCredential} used to authenticate HTTP requests.
     * @return The updated {@link RoomsClientBuilder} object.
     * @throws NullPointerException If {@code keyCredential} is null.
     */
    @Override
    public RoomsClientBuilder credential(AzureKeyCredential keyCredential)  {
        this.azureKeyCredential = Objects.requireNonNull(keyCredential, "'keyCredential' cannot be null.");
        return this;
    }

    /**
     * Sets the {@link RetryOptions} for all the requests made through the client.
     *
     * @param retryOptions The {@link RetryOptions} to use for all the requests made through the client.
     * @return Updated {@link RoomsClientBuilder} object.
     */
    @Override
    public RoomsClientBuilder retryOptions(RetryOptions retryOptions) {
        this.retryOptions = Objects.requireNonNull(retryOptions, "'retryOptions' cannot be null.");
        return this;
    }

     /**
     * Set endpoint and credential to use
     *
     * @param connectionString connection string for setting endpoint and initalizing AzureKeyCredential
     * @return RoomsClientBuilder
     */
    @Override
    public RoomsClientBuilder connectionString(String connectionString) {
        Objects.requireNonNull(connectionString, "'connectionString' cannot be null.");
        CommunicationConnectionString connectionStringObject = new CommunicationConnectionString(connectionString);
        String endpoint = connectionStringObject.getEndpoint();
        String accessKey = connectionStringObject.getAccessKey();
        this
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(accessKey));
        return this;
    }

    /**
     * Sets the retry policy to use (using the RetryPolicy type).
     *
     * @param retryPolicy object to be applied
     * @return RoomsClientBuilder
     */
    public RoomsClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = Objects.requireNonNull(retryPolicy, "'retryPolicy' cannot be null.");
        return this;
    }

    /**
     * Sets the configuration object used to retrieve environment configuration values during building of the client.
     *
     * @param configuration Configuration store used to retrieve environment configurations.
     * @return the updated RoomsClientBuilder object
     */
    @Override
    public RoomsClientBuilder configuration(Configuration configuration) {
        this.configuration = Objects.requireNonNull(configuration, "'configuration' cannot be null.");
        return this;
    }

    /**
     * Sets the {@link HttpLogOptions} for service requests.
     *
     * @param logOptions The logging configuration to use when sending and receiving HTTP requests/responses.
     * @return the updated RoomsClientBuilder object
     */
    @Override
    public RoomsClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        this.httpLogOptions = Objects.requireNonNull(logOptions, "'logOptions' cannot be null.");
        return this;
    }

    /**
     * Sets the {@link RoomsServiceVersion} that is used when making API requests.
     * <p>
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version of the client library will have the result of potentially moving to a newer service version.
     * <p>
     * Targeting a specific service version may also mean that the service will return an error for newer APIs.
     *
     * @param version {@link RoomsServiceVersion} of the service to be used when making requests.
     * @return the updated RoomsClientBuilder object
     */
    public RoomsClientBuilder serviceVersion(RoomsServiceVersion version) {
        this.serviceVersion = version;
        return this;
    }

    /**
     * Set httpClient to use
     *
     * @param httpClient httpClient to use, overridden by the pipeline
     * field.
     * @return RoomsClientBuilder
     */
    @Override
    public RoomsClientBuilder httpClient(HttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient, "'httpClient' cannot be null.");
        return this;
    }

    /**
     * Apply additional HttpPipelinePolicy
     *
     * @param customPolicy HttpPipelinePolicy object to be applied after
     *                       AzureKeyCredentialPolicy, UserAgentPolicy, RetryPolicy, and CookiePolicy
     * @return RoomsClientBuilder
     */
    @Override
    public RoomsClientBuilder addPolicy(HttpPipelinePolicy customPolicy) {
        this.customPolicies.add(Objects.requireNonNull(customPolicy, "'customPolicy' cannot be null."));
        return this;
    }

    /**
     * Create asynchronous client applying HMACAuthenticationPolicy, UserAgentPolicy,
     * RetryPolicy, and CookiePolicy.
     * Additional HttpPolicies specified by additionalPolicies will be applied after them
     *
     * @return RoomsAsyncClient instance
     */
    public RoomsAsyncClient buildAsyncClient() {
        return new RoomsAsyncClient(createServiceImpl());
    }

    /**
     * Create synchronous client applying HmacAuthenticationPolicy, UserAgentPolicy,
     * RetryPolicy, and CookiePolicy.
     * Additional HttpPolicies specified by additionalPolicies will be applied after them
     *
     * @return RoomsClient instance
     */
    public RoomsClient buildClient() {
        return new RoomsClient(buildAsyncClient());
    }

    private AzureCommunicationRoomServiceImpl createServiceImpl() {
        Objects.requireNonNull(endpoint);

        HttpPipeline builderPipeline = this.pipeline;

        if (this.pipeline == null) {
            builderPipeline = createHttpPipeline(httpClient);
        }

        RoomsServiceVersion apiVersion = serviceVersion != null ? serviceVersion : RoomsServiceVersion.getLatest();

        AzureCommunicationRoomServiceImplBuilder clientBuilder = new AzureCommunicationRoomServiceImplBuilder();
        clientBuilder.endpoint(endpoint)
            .apiVersion(apiVersion.getVersion())
            .pipeline(builderPipeline);

        return clientBuilder.buildClient();
    }

    /**
     * Allows the user to set a variety of client-related options, such as user-agent string, headers, etc.
     *
     * @param clientOptions object to be applied
     * @return RoomsClientBuilder
     */
    @Override
    public RoomsClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
        return this;
    }

    private HttpPipelinePolicy createHttpPipelineAuthPolicy() {
        if (this.tokenCredential != null && this.azureKeyCredential != null) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Both 'credential' and 'keyCredential' are set. Just one may be used."));
        }
        if (this.tokenCredential != null) {
            return new BearerTokenAuthenticationPolicy(
                this.tokenCredential, "https://communication.azure.com//.default");
        } else if (this.azureKeyCredential != null) {
            return new HmacAuthenticationPolicy(this.azureKeyCredential);
        } else {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Missing credential information while building a client."));
        }
    }

    private HttpPipeline createHttpPipeline(HttpClient httpClient) {
        if (this.pipeline != null) {
            return this.pipeline;
        }

        List<HttpPipelinePolicy> policyList = new ArrayList<>();

        ClientOptions buildClientOptions = (clientOptions == null) ? new ClientOptions() : clientOptions;
        HttpLogOptions buildLogOptions = (httpLogOptions == null) ? new HttpLogOptions() : httpLogOptions;

        String applicationId = null;
        if (!CoreUtils.isNullOrEmpty(buildClientOptions.getApplicationId())) {
            applicationId = buildClientOptions.getApplicationId();
        } else if (!CoreUtils.isNullOrEmpty(buildLogOptions.getApplicationId())) {
            applicationId = buildLogOptions.getApplicationId();
        }

        // Add required policies
        String clientName = properties.getOrDefault(SDK_NAME, "UnknownName");
        String clientVersion = properties.getOrDefault(SDK_VERSION, "UnknownVersion");
        policyList.add(new UserAgentPolicy(applicationId, clientName, clientVersion, configuration));
        policyList.add(new RequestIdPolicy());
        policyList.add(ClientBuilderUtil.validateAndGetRetryPolicy(retryPolicy, retryOptions));

        // auth policy is per request, should be after retry
        policyList.add(this.createHttpPipelineAuthPolicy());
        policyList.add(new CookiePolicy());

        // Add additional policies
        if (!this.customPolicies.isEmpty()) {
            policyList.addAll(this.customPolicies);
        }

         // Add logging policy
        policyList.add(this.createHttpLoggingPolicy(this.getHttpLogOptions()));

        return new HttpPipelineBuilder()
            .policies(policyList.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();
    }

    private HttpLogOptions getHttpLogOptions() {
        if (this.httpLogOptions == null) {
            this.httpLogOptions = this.createDefaultHttpLogOptions();
        }

        return this.httpLogOptions;
    }

    HttpLogOptions createDefaultHttpLogOptions() {
        return new HttpLogOptions();
    }

    HttpLoggingPolicy createHttpLoggingPolicy(HttpLogOptions httpLogOptions) {
        return new HttpLoggingPolicy(httpLogOptions);
    }
}
