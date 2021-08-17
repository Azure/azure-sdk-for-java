// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.networktraversal;

import com.azure.communication.common.implementation.CommunicationConnectionString;
import com.azure.communication.common.implementation.HmacAuthenticationPolicy;
import com.azure.communication.networktraversal.implementation.CommunicationNetworkingClientImpl;
import com.azure.communication.networktraversal.implementation.CommunicationNetworkingClientImplBuilder;
import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.CookiePolicy;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * CommunicationRelayClientBuilder that creates CommunicationRelayAsyncClient and CommunicationRelayClient.
 */
@ServiceClientBuilder(serviceClients = {CommunicationRelayClient.class, CommunicationRelayAsyncClient.class})
public final class CommunicationRelayClientBuilder {
    private static final String SDK_NAME = "name";
    private static final String SDK_VERSION = "version";

    private static final String COMMUNICATION_IDENTITY_PROPERTIES =
        "azure-communication-networktravesal.properties";

    private final ClientLogger logger = new ClientLogger(CommunicationRelayClientBuilder.class);
    private String endpoint;
    private AzureKeyCredential azureKeyCredential;
    private TokenCredential tokenCredential;
    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions = new HttpLogOptions();
    private HttpPipeline pipeline;
    private RetryPolicy retryPolicy;
    private Configuration configuration;
    private ClientOptions clientOptions;
    private String connectionString;
    private final Map<String, String> properties = CoreUtils.getProperties(COMMUNICATION_IDENTITY_PROPERTIES);
    private final List<HttpPipelinePolicy> customPolicies = new ArrayList<HttpPipelinePolicy>();

    /**
     * Set endpoint of the service
     *
     * @param endpoint url of the service
     * @return CommunicationRelayClientBuilder
     */
    public CommunicationRelayClientBuilder endpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    /**
     * Set endpoint of the service
     *
     * @param pipeline HttpPipeline to use, if a pipeline is not
     * supplied, the credential and httpClient fields must be set
     * @return CommunicationRelayClientBuilder
     */
    public CommunicationRelayClientBuilder pipeline(HttpPipeline pipeline) {
        this.pipeline = pipeline;
        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authenticate HTTP requests.
     *
     * @param tokenCredential {@link TokenCredential} used to authenticate HTTP requests.
     * @return The updated {@link CommunicationRelayClientBuilder} object.
     */
    public CommunicationRelayClientBuilder credential(TokenCredential tokenCredential) {
        this.tokenCredential = tokenCredential;
        return this;
    }

    /**
     * Sets the {@link AzureKeyCredential} used to authenticate HTTP requests.
     *
    * @param keyCredential The {@link AzureKeyCredential} used to authenticate HTTP requests.
     * @return The updated {@link CommunicationRelayClientBuilder} object.
     */
    public CommunicationRelayClientBuilder credential(AzureKeyCredential keyCredential)  {
        this.azureKeyCredential = keyCredential;
        return this;
    }

    /**
     * Set endpoint and credential to use
     *
     * @param connectionString connection string for setting endpoint and initalizing CommunicationClientCredential
     * @return CommunicationRelayClientBuilder
     */
    public CommunicationRelayClientBuilder connectionString(String connectionString) {
        CommunicationConnectionString connectionStringObject = new CommunicationConnectionString(connectionString);
        String endpoint = connectionStringObject.getEndpoint();
        String accessKey = connectionStringObject.getAccessKey();
        this
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(accessKey));
        return this;
    }

    /**
     * Set httpClient to use
     *
     * @param httpClient httpClient to use, overridden by the pipeline
     * field.
     * @return CommunicationRelayClientBuilder
     */
    public CommunicationRelayClientBuilder httpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    /**
     * Apply additional HttpPipelinePolicy
     *
     * @param customPolicy HttpPipelinePolicy object to be applied after
     * AzureKeyCredentialPolicy, UserAgentPolicy, RetryPolicy, and CookiePolicy
     * @return CommunicationRelayClientBuilder
     */
    public CommunicationRelayClientBuilder addPolicy(HttpPipelinePolicy customPolicy) {
        this.customPolicies.add(customPolicy);
        return this;
    }

        /**
     * Sets the client options for all the requests made through the client.
     *
     * @param clientOptions {@link ClientOptions}.
     * @return The updated {@link CommunicationRelayClientBuilder} object.
     */
    public CommunicationRelayClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
        return this;
    }

    /**
     * Sets the configuration object used to retrieve environment configuration values during building of the client.
     *
     * @param configuration Configuration store used to retrieve environment configurations.
     * @return the updated CommunicationRelayClientBuilder object
     */
    public CommunicationRelayClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the {@link HttpLogOptions} for service requests.
     *
     * @param logOptions The logging configuration to use when sending and receiving HTTP requests/responses.
     * @return the updated CommunicationRelayClientBuilder object
     */
    public CommunicationRelayClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        this.httpLogOptions = logOptions;
        return this;
    }

    /**
     * Sets the {@link RetryPolicy} that is used when each request is sent.
     *
     * @param retryPolicy User's retry policy applied to each request.
     * @return The updated {@link CommunicationRelayClientBuilder} object.
     */
    public CommunicationRelayClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
        return this;
    }

    /**
     * Sets the {@link CommunicationRelayServiceVersion} that is used when making API requests.
     * <p>
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version of the client library will have the result of potentially moving to a newer service version.
     * <p>
     * Targeting a specific service version may also mean that the service will return an error for newer APIs.
     *
     * @param version {@link CommunicationRelayServiceVersion} of the service to be used when making requests.
     * @return the updated CommunicationRelayClientBuilder object
     */
    public CommunicationRelayClientBuilder serviceVersion(CommunicationRelayServiceVersion version) {
        return this;
    }

    /**
     * Create asynchronous client applying HMACAuthenticationPolicy, UserAgentPolicy,
     * RetryPolicy, and CookiePolicy.
     * Additional HttpPolicies specified by additionalPolicies will be applied after them
     *
     * @return CommunicationRelayAsyncClient instance
     */
    public CommunicationRelayAsyncClient buildAsyncClient() {
        Objects.requireNonNull(endpoint, "'endpoint' cannot be null.");
        Objects.requireNonNull(endpoint, "'credential' cannot be null.");
        return new CommunicationRelayAsyncClient(createServiceImpl());
    }

    /**
     * Create synchronous client applying HmacAuthenticationPolicy, UserAgentPolicy,
     * RetryPolicy, and CookiePolicy.
     * Additional HttpPolicies specified by additionalPolicies will be applied after them
     *
     * @return CommunicationRelayClient instance
     */
    public CommunicationRelayClient buildClient() {
        return new CommunicationRelayClient(buildAsyncClient());
    }

    private CommunicationNetworkingClientImpl createServiceImpl() {


        HttpPipeline builderPipeline = this.pipeline;
        if (this.pipeline == null) {
            builderPipeline = createHttpPipeline(httpClient,
                createHttpPipelineAuthPolicy(),
                customPolicies);
        }

        CommunicationNetworkingClientImplBuilder clientBuilder = new CommunicationNetworkingClientImplBuilder();
        clientBuilder.endpoint(endpoint)
            .pipeline(builderPipeline);

        return clientBuilder.buildClient();
    }

    private HttpPipelinePolicy createHttpPipelineAuthPolicy() {
        if (this.tokenCredential != null && this.azureKeyCredential != null) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Both 'credential' and 'accessKey' are set. Just one may be used."));
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

    private HttpPipeline createHttpPipeline(HttpClient httpClient,
                                            HttpPipelinePolicy authorizationPolicy,
                                            List<HttpPipelinePolicy> customPolicies) {

        List<HttpPipelinePolicy> policies = new ArrayList<HttpPipelinePolicy>();
        applyRequiredPolicies(policies, authorizationPolicy);

        if (customPolicies != null && customPolicies.size() > 0) {
            policies.addAll(customPolicies);
        }

        return new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .clientOptions(clientOptions)
            .build();
    }

    private void applyRequiredPolicies(List<HttpPipelinePolicy> policies, HttpPipelinePolicy authorizationPolicy) {
        String clientName = properties.getOrDefault(SDK_NAME, "UnknownName");
        String clientVersion = properties.getOrDefault(SDK_VERSION, "UnknownVersion");

        ClientOptions buildClientOptions = (clientOptions == null) ? new ClientOptions() : clientOptions;
        HttpLogOptions buildLogOptions = (httpLogOptions == null) ? new HttpLogOptions() : httpLogOptions;

        String applicationId = null;
        if (!CoreUtils.isNullOrEmpty(buildClientOptions.getApplicationId())) {
            applicationId = buildClientOptions.getApplicationId();
        } else if (!CoreUtils.isNullOrEmpty(buildLogOptions.getApplicationId())) {
            applicationId = buildLogOptions.getApplicationId();
        }
        
        policies.add(new UserAgentPolicy(applicationId, clientName, clientVersion, configuration));
        policies.add(new RequestIdPolicy());
        policies.add(this.retryPolicy == null ? new RetryPolicy() : this.retryPolicy);
        policies.add(new CookiePolicy());
        // auth policy is per request, should be after retry
        policies.add(authorizationPolicy);
        policies.add(new HttpLoggingPolicy(httpLogOptions));
    }
}
