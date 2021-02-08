// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.administration;

import com.azure.communication.common.CommunicationClientCredential;
import com.azure.communication.common.ConnectionString;
import com.azure.communication.common.HmacAuthenticationPolicy;
import com.azure.communication.administration.implementation.CommunicationIdentityClientImpl;
import com.azure.communication.administration.implementation.CommunicationIdentityClientImplBuilder;
import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.CookiePolicy;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * CommunicationIdentityClientBuilder that creates CommunicationIdentityAsyncClient and CommunicationIdentityClient.
 */
@ServiceClientBuilder(serviceClients = {CommunicationIdentityClient.class, CommunicationIdentityAsyncClient.class})
public final class CommunicationIdentityClientBuilder {
    private static final String SDK_NAME = "name";
    private static final String SDK_VERSION = "version";
    private static final String COMMUNICATION_ADMINISTRATION_PROPERTIES = 
        "azure-communication-administration.properties";

    private final ClientLogger logger = new ClientLogger(CommunicationIdentityClientBuilder.class);
    private String endpoint;
    private CommunicationClientCredential accessKeyCredential;
    private TokenCredential tokenCredential;
    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions = new HttpLogOptions();
    private HttpPipeline pipeline;
    private Configuration configuration;    
    private final Map<String, String> properties = CoreUtils.getProperties(COMMUNICATION_ADMINISTRATION_PROPERTIES);    
    private final List<HttpPipelinePolicy> customPolicies = new ArrayList<HttpPipelinePolicy>();

    /**
     * Set endpoint of the service
     *
     * @param endpoint url of the service
     * @return CommunicationIdentityClientBuilder
     */
    public CommunicationIdentityClientBuilder endpoint(String endpoint) {
        this.endpoint = Objects.requireNonNull(endpoint, "'endpoint' cannot be null.");
        return this;
    }

    /**
     * Set endpoint of the service
     *
     * @param pipeline HttpPipeline to use, if a pipeline is not
     * supplied, the credential and httpClient fields must be set
     * @return CommunicationIdentityClientBuilder
     */
    public CommunicationIdentityClientBuilder pipeline(HttpPipeline pipeline) {
        this.pipeline = Objects.requireNonNull(pipeline, "'pipeline' cannot be null.");
        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authenticate HTTP requests.
     *
     * @param tokenCredential {@link TokenCredential} used to authenticate HTTP requests.
     * @return The updated {@link CommunicationIdentityClientBuilder} object.
     * @throws NullPointerException If {@code tokenCredential} is null.
     */
    public CommunicationIdentityClientBuilder credential(TokenCredential tokenCredential) {
        this.tokenCredential = Objects.requireNonNull(tokenCredential, "'tokenCredential' cannot be null.");
        return this;
    }

    /**
     * Set credential to use
     *
     * @param accessKey access key for initalizing CommunicationClientCredential
     * @return CommunicationIdentityClientBuilder
     */
    public CommunicationIdentityClientBuilder accessKey(String accessKey) {
        Objects.requireNonNull(accessKey, "'accessKey' cannot be null.");
        this.accessKeyCredential = new CommunicationClientCredential(accessKey);
        return this;
    }

     /**
     * Set endpoint and credential to use
     *
     * @param connectionString connection string for setting endpoint and initalizing CommunicationClientCredential
     * @return CommunicationIdentityClientBuilder
     */
    public CommunicationIdentityClientBuilder connectionString(String connectionString) {
        Objects.requireNonNull(connectionString, "'connectionString' cannot be null.");
        ConnectionString connectionStringObject = new ConnectionString(connectionString);
        String endpoint = connectionStringObject.getEndpoint();
        String accessKey = connectionStringObject.getAccessKey();
        this
            .endpoint(endpoint)
            .accessKey(accessKey);
        return this;
    }

    /**
     * Set httpClient to use
     *
     * @param httpClient httpClient to use, overridden by the pipeline
     * field.
     * @return CommunicationIdentityClientBuilder
     */
    public CommunicationIdentityClientBuilder httpClient(HttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient, "'httpClient' cannot be null.");
        return this;
    }

    /**
     * Apply additional HttpPipelinePolicy
     *
     * @param customPolicy HttpPipelinePolicy object to be applied after 
     * AzureKeyCredentialPolicy, UserAgentPolicy, RetryPolicy, and CookiePolicy
     * @return CommunicationIdentityClientBuilder
     */
    public CommunicationIdentityClientBuilder addPolicy(HttpPipelinePolicy customPolicy) {
        this.customPolicies.add(Objects.requireNonNull(customPolicy, "'customPolicy' cannot be null."));
        return this;
    }

    /**
     * Sets the configuration object used to retrieve environment configuration values during building of the client.
     *
     * @param configuration Configuration store used to retrieve environment configurations.
     * @return the updated CommunicationIdentityClientBuilder object
     */
    public CommunicationIdentityClientBuilder configuration(Configuration configuration) {
        this.configuration = Objects.requireNonNull(configuration, "'configuration' cannot be null.");
        return this;
    }

    /**
     * Sets the {@link HttpLogOptions} for service requests.
     *
     * @param logOptions The logging configuration to use when sending and receiving HTTP requests/responses.
     * @return the updated CommunicationIdentityClientBuilder object
     */
    public CommunicationIdentityClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        this.httpLogOptions = Objects.requireNonNull(logOptions, "'logOptions' cannot be null.");
        return this;
    }

    /**
     * Sets the {@link CommunicationIdentityServiceVersion} that is used when making API requests.
     * <p>
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version of the client library will have the result of potentially moving to a newer service version.
     * <p>
     * Targeting a specific service version may also mean that the service will return an error for newer APIs.
     *
     * @param version {@link CommunicationIdentityServiceVersion} of the service to be used when making requests.
     * @return the updated CommunicationIdentityClientBuilder object
     */
    public CommunicationIdentityClientBuilder serviceVersion(CommunicationIdentityServiceVersion version) {
        return this;
    }

    /**
     * Create asynchronous client applying HMACAuthenticationPolicy, UserAgentPolicy,
     * RetryPolicy, and CookiePolicy.
     * Additional HttpPolicies specified by additionalPolicies will be applied after them
     *
     * @return CommunicationIdentityAsyncClient instance
     */
    public CommunicationIdentityAsyncClient buildAsyncClient() {
        return new CommunicationIdentityAsyncClient(createServiceImpl());
    }

    /**
     * Create synchronous client applying HmacAuthenticationPolicy, UserAgentPolicy,
     * RetryPolicy, and CookiePolicy.
     * Additional HttpPolicies specified by additionalPolicies will be applied after them
     *
     * @return CommunicationIdentityClient instance
     */
    public CommunicationIdentityClient buildClient() {
        return new CommunicationIdentityClient(buildAsyncClient());
    }

    private CommunicationIdentityClientImpl createServiceImpl() {
        Objects.requireNonNull(endpoint);

        HttpPipeline builderPipeline = this.pipeline;
        if (this.pipeline == null) {
            builderPipeline = createHttpPipeline(httpClient, 
                createHttpPipelineAuthPolicy(), 
                customPolicies);
        }

        CommunicationIdentityClientImplBuilder clientBuilder = new CommunicationIdentityClientImplBuilder();
        clientBuilder.endpoint(endpoint)
            .pipeline(builderPipeline);
        
        return clientBuilder.buildClient();
    }

    private HttpPipelinePolicy createHttpPipelineAuthPolicy() {
        if (this.tokenCredential != null && this.accessKeyCredential != null) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Both 'credential' and 'accessKey' are set. Just one may be used."));
        }
        if (this.tokenCredential != null) { 
            return new BearerTokenAuthenticationPolicy(
                this.tokenCredential, "https://communication.azure.com//.default");          
        } else if (this.accessKeyCredential != null) {
            return new HmacAuthenticationPolicy(this.accessKeyCredential);            
        } else {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Missing credential information while building a client."));
        }
    }

    private HttpPipeline createHttpPipeline(HttpClient httpClient,
                                            HttpPipelinePolicy authorizationPolicy,
                                            List<HttpPipelinePolicy> customPolicies) {

        List<HttpPipelinePolicy> policies = new ArrayList<HttpPipelinePolicy>();
        policies.add(authorizationPolicy);
        applyRequiredPolicies(policies);

        if (customPolicies != null && customPolicies.size() > 0) {
            policies.addAll(customPolicies);
        }

        return new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();
    }

    private void applyRequiredPolicies(List<HttpPipelinePolicy> policies) {
        String clientName = properties.getOrDefault(SDK_NAME, "UnknownName");
        String clientVersion = properties.getOrDefault(SDK_VERSION, "UnknownVersion");

        policies.add(new UserAgentPolicy(httpLogOptions.getApplicationId(), clientName, clientVersion, configuration));
        policies.add(new RetryPolicy());
        policies.add(new CookiePolicy());
        policies.add(new HttpLoggingPolicy(httpLogOptions));
    }  
}
