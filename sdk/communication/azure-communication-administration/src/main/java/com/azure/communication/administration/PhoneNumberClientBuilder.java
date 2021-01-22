// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.administration;

import com.azure.communication.administration.implementation.PhoneNumberAdminClientImpl;
import com.azure.communication.administration.implementation.PhoneNumberAdminClientImplBuilder;
import com.azure.communication.common.CommunicationClientCredential;
import com.azure.communication.common.ConnectionString;
import com.azure.communication.common.HmacAuthenticationPolicy;
import com.azure.core.annotation.ServiceClientBuilder;
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
 * Builder for creating clients of Communication Service phone number configuration
 */
@ServiceClientBuilder(serviceClients = {PhoneNumberClient.class, PhoneNumberAsyncClient.class})
public final class PhoneNumberClientBuilder {
    private static final Map<String, String> PROPERTIES =
        CoreUtils.getProperties("azure-communication-administration.properties");
    private static final String SDK_NAME = "name";
    private static final String SDK_VERSION = "version";

    private final ClientLogger logger = new ClientLogger(PhoneNumberClientBuilder.class);

    private PhoneNumberServiceVersion version;
    private String endpoint;
    private HttpPipeline pipeline;
    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions;
    private CommunicationClientCredential accessKeyCredential;
    private TokenCredential tokenCredential;
    private Configuration configuration;
    private final List<HttpPipelinePolicy> additionalPolicies = new ArrayList<>();

    /**
     * Set endpoint of the service
     *
     * @param endpoint url of the service
     * @return The updated {@link PhoneNumberClientBuilder} object.
     * @throws NullPointerException If {@code endpoint} is {@code null}.
     */
    public PhoneNumberClientBuilder endpoint(String endpoint) {
        this.endpoint = Objects.requireNonNull(endpoint, "'endpoint' cannot be null.");
        return this;
    }

    /**
     * Sets the HTTP pipeline to use for the service client
     * <p>
     * If {@code pipeline} is set, all other settings aside from
     * {@link PhoneNumberClientBuilder#endpoint(String) endpoint} are ignored.
     *
     * @param pipeline HttpPipeline to use
     * @return The updated {@link PhoneNumberClientBuilder} object.
     */
    public PhoneNumberClientBuilder pipeline(HttpPipeline pipeline) {
        this.pipeline = pipeline;
        return this;
    }

    /**
     * Set HttpClient to use
     *
     * @param httpClient HttpClient to use
     * @return The updated {@link PhoneNumberClientBuilder} object.
     * @throws NullPointerException If {@code httpClient} is {@code null}.
     */
    public PhoneNumberClientBuilder httpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    /**
     * Sets the logging configuration for HTTP requests and responses.
     *
     * <p> If logLevel is not provided, default value of {@link HttpLogDetailLevel#NONE} is set.</p>
     *
     * @param httpLogOptions The logging configuration to use when sending and receiving HTTP requests/responses.
     * @return the updated {@link PhoneNumberClientBuilder} object.
     */
    public PhoneNumberClientBuilder httpLogOptions(HttpLogOptions httpLogOptions) {
        this.httpLogOptions = httpLogOptions;
        return this;
    }

    /**
     * Set CommunicationClientCredential for authorization
     *
     * @param accessKey access key for initalizing CommunicationClientCredential
     * @return The updated {@link PhoneNumberClientBuilder} object.
     * @throws NullPointerException If {@code accessKey} is {@code null}.
     */
    public PhoneNumberClientBuilder accessKey(String accessKey) {
        Objects.requireNonNull(accessKey, "'accessKey' cannot be null.");
        this.accessKeyCredential = new CommunicationClientCredential(accessKey);
        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authenticate HTTP requests.
     *
     * @param tokenCredential {@link TokenCredential} used to authenticate HTTP requests.
     * @return The updated {@link CommunicationIdentityClientBuilder} object.
     * @throws NullPointerException If {@code tokenCredential} is null.
     */
    public PhoneNumberClientBuilder credential(TokenCredential tokenCredential) {
        this.tokenCredential = Objects.requireNonNull(tokenCredential, "'tokenCredential' cannot be null.");
        return this;
    }


    /**
     * Set the endpoint and CommunicationClientCredential for authorization
     *
     * @param connectionString connection string for setting endpoint and initalizing CommunicationClientCredential
     * @return The updated {@link PhoneNumberClientBuilder} object.
     * @throws NullPointerException If {@code connectionString} is {@code null}.
     */
    public PhoneNumberClientBuilder connectionString(String connectionString) {
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
     * Sets the configuration object used to retrieve environment configuration values during building of the client.
     *
     * @param configuration Configuration store used to retrieve environment configurations.
     * @return The updated {@link PhoneNumberClientBuilder} object.
     */
    public PhoneNumberClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Adds a policy to the set of existing policies that are executed after required policies.
     *
     * @param policy The retry policy for service requests.
     * @return The updated {@link PhoneNumberClientBuilder} object.
     * @throws NullPointerException If {@code policy} is {@code null}.
     */
    public PhoneNumberClientBuilder addPolicy(HttpPipelinePolicy policy) {
        this.additionalPolicies.add(Objects.requireNonNull(policy, "'policy' cannot be null."));
        return this;
    }

    /**
     * Sets the {@link PhoneNumberServiceVersion} that is used when making API requests.
     * <p>
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version the client library will have the result of potentially moving to a newer service version.
     *
     * @param version {@link PhoneNumberServiceVersion} of the service to be used when making requests.
     * @return The updated {@link PhoneNumberClientBuilder} object.
     */
    public PhoneNumberClientBuilder serviceVersion(PhoneNumberServiceVersion version) {
        this.version = version;
        return this;
    }

    /**
     * Create synchronous client applying CommunicationClientCredentialPolicy,
     * UserAgentPolicy, RetryPolicy, and CookiePolicy.
     * Additional HttpPolicies specified by additionalPolicies will be applied after them
     *
     * @return {@link PhoneNumberClient} instance
     */
    public PhoneNumberClient buildClient() {
        return new PhoneNumberClient(this.buildAsyncClient());
    }

    /**
     * Create asynchronous client applying CommunicationClientCredentialPolicy,
     * UserAgentPolicy, RetryPolicy, and CookiePolicy.
     * Additional HttpPolicies specified by additionalPolicies will be applied after them
     *
     * @return {@link PhoneNumberAsyncClient} instance
     */
    public PhoneNumberAsyncClient buildAsyncClient() {
        this.validateRequiredFields();

        if (this.version != null) {
            logger.info("Build client for service version" + this.version.getVersion());
        }

        return this.createPhoneNumberAsyncClient(this.createPhoneNumberAdminClient());
    }

    PhoneNumberAsyncClient createPhoneNumberAsyncClient(PhoneNumberAdminClientImpl phoneNumberAdminClient) {
        return new PhoneNumberAsyncClient(phoneNumberAdminClient);
    }

    HttpPipelinePolicy createAuthenticationPolicy() {
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
                new NullPointerException("Missing credential information while building a client."));
        }
    }

    UserAgentPolicy createUserAgentPolicy(
        String applicationId, String sdkName, String sdkVersion, Configuration configuration) {
        return new UserAgentPolicy(applicationId, sdkName, sdkVersion, configuration);
    }

    RetryPolicy createRetryPolicy() {
        return new RetryPolicy();
    }

    CookiePolicy createCookiePolicy() {
        return new CookiePolicy();
    }

    HttpLoggingPolicy createHttpLoggingPolicy(HttpLogOptions httpLogOptions) {
        return new HttpLoggingPolicy(httpLogOptions);
    }

    HttpLogOptions createDefaultHttpLogOptions() {
        return new HttpLogOptions();
    }

    private void validateRequiredFields() {
        Objects.requireNonNull(this.endpoint);

        if (this.pipeline == null) {
            Objects.requireNonNull(this.httpClient);
        }
    }

    private PhoneNumberAdminClientImpl createPhoneNumberAdminClient() {
        PhoneNumberAdminClientImplBuilder clientBuilder = new PhoneNumberAdminClientImplBuilder();
        return clientBuilder
            .endpoint(this.endpoint)
            .pipeline(this.createHttpPipeline())
            .buildClient();
    }

    private HttpPipeline createHttpPipeline() {
        if (this.pipeline != null) {
            return this.pipeline;
        }

        List<HttpPipelinePolicy> policyList = new ArrayList<>();

        // Add required policies
        policyList.add(this.createAuthenticationPolicy());
        policyList.add(this.createUserAgentPolicy(
            this.getHttpLogOptions().getApplicationId(),
            PROPERTIES.get(SDK_NAME),
            PROPERTIES.get(SDK_VERSION),
            this.configuration
        ));
        policyList.add(this.createRetryPolicy());
        policyList.add(this.createCookiePolicy());

        // Add additional policies
        if (this.additionalPolicies.size() > 0) {
            policyList.addAll(this.additionalPolicies);
        }

        // Add logging policy
        policyList.add(this.createHttpLoggingPolicy(this.getHttpLogOptions()));

        return new HttpPipelineBuilder()
            .policies(policyList.toArray(new HttpPipelinePolicy[0]))
            .httpClient(this.httpClient)
            .build();
    }

    private HttpLogOptions getHttpLogOptions() {
        if (this.httpLogOptions == null) {
            this.httpLogOptions = this.createDefaultHttpLogOptions();
        }

        return this.httpLogOptions;
    }
}
