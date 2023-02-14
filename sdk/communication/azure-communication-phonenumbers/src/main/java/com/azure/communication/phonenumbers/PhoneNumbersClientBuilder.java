// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.phonenumbers;

import com.azure.communication.phonenumbers.implementation.PhoneNumberAdminClientImpl;
import com.azure.communication.phonenumbers.implementation.PhoneNumberAdminClientImplBuilder;
import com.azure.communication.common.implementation.CommunicationConnectionString;
import com.azure.communication.common.implementation.HmacAuthenticationPolicy;
import com.azure.core.annotation.ServiceClientBuilder;
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
 * Builder for creating clients of Communication Service phone number configuration
 */
@ServiceClientBuilder(serviceClients = {PhoneNumbersClient.class, PhoneNumbersAsyncClient.class})
public final class PhoneNumbersClientBuilder {
    private static final Map<String, String> PROPERTIES =
        CoreUtils.getProperties("azure-communication-phonenumbers.properties");
    private static final String SDK_NAME = "name";
    private static final String SDK_VERSION = "version";

    private final ClientLogger logger = new ClientLogger(PhoneNumbersClientBuilder.class);

    private PhoneNumbersServiceVersion version;
    private String endpoint;
    private HttpPipeline pipeline;
    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions;
    private AzureKeyCredential azureKeyCredential;
    private TokenCredential tokenCredential;
    private Configuration configuration;
    private ClientOptions clientOptions;
    private RetryPolicy retryPolicy;
    private final List<HttpPipelinePolicy> additionalPolicies = new ArrayList<>();

    /**
     * Set endpoint of the service
     *
     * @param endpoint url of the service
     * @return The updated {@link PhoneNumbersClientBuilder} object.
     * @throws NullPointerException If {@code endpoint} is {@code null}.
     */
    public PhoneNumbersClientBuilder endpoint(String endpoint) {
        this.endpoint = Objects.requireNonNull(endpoint, "'endpoint' cannot be null.");
        return this;
    }

    /**
     * Sets the HTTP pipeline to use for the service client
     * <p>
     * If {@code pipeline} is set, all other settings aside from
     * {@link PhoneNumbersClientBuilder#endpoint(String) endpoint} are ignored.
     *
     * @param pipeline HttpPipeline to use
     * @return The updated {@link PhoneNumbersClientBuilder} object.
     */
    public PhoneNumbersClientBuilder pipeline(HttpPipeline pipeline) {
        this.pipeline = pipeline;
        return this;
    }

    /**
     * Set HttpClient to use
     *
     * @param httpClient HttpClient to use
     * @return The updated {@link PhoneNumbersClientBuilder} object.
     * @throws NullPointerException If {@code httpClient} is {@code null}.
     */
    public PhoneNumbersClientBuilder httpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    /**
     * Sets the logging configuration for HTTP requests and responses.
     *
     * <p> If logLevel is not provided, default value of {@link HttpLogDetailLevel#NONE} is set.</p>
     *
     * @param httpLogOptions The logging configuration to use when sending and receiving HTTP requests/responses.
     * @return the updated {@link PhoneNumbersClientBuilder} object.
     */
    public PhoneNumbersClientBuilder httpLogOptions(HttpLogOptions httpLogOptions) {
        this.httpLogOptions = httpLogOptions;
        return this;
    }

    /**
     * Sets the {@link AzureKeyCredential} used to authenticate HTTP requests.
     *
     * @param keyCredential The {@link AzureKeyCredential} used to authenticate HTTP requests.
     * @return The updated {@link PhoneNumbersClientBuilder} object.
     * @throws NullPointerException If {@code keyCredential} is null.
     */
    public PhoneNumbersClientBuilder credential(AzureKeyCredential keyCredential)  {
        this.azureKeyCredential = Objects.requireNonNull(keyCredential, "'keyCredential' cannot be null.");
        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authenticate HTTP requests.
     *
     * @param tokenCredential {@link TokenCredential} used to authenticate HTTP requests.
     * @return The updated {@link PhoneNumbersClientBuilder} object.
     * @throws NullPointerException If {@code tokenCredential} is null.
     */
    public PhoneNumbersClientBuilder credential(TokenCredential tokenCredential) {
        this.tokenCredential = Objects.requireNonNull(tokenCredential, "'tokenCredential' cannot be null.");
        return this;
    }


    /**
     * Set the endpoint and AzureKeyCredential for authorization
     *
     * @param connectionString connection string for setting endpoint and initalizing AzureKeyCredential
     * @return The updated {@link PhoneNumbersClientBuilder} object.
     * @throws NullPointerException If {@code connectionString} is {@code null}.
     */
    public PhoneNumbersClientBuilder connectionString(String connectionString) {
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
     * Sets the configuration object used to retrieve environment configuration values during building of the client.
     *
     * @param configuration Configuration store used to retrieve environment configurations.
     * @return The updated {@link PhoneNumbersClientBuilder} object.
     */
    public PhoneNumbersClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Adds a policy to the set of existing policies that are executed after required policies.
     *
     * @param policy The retry policy for service requests.
     * @return The updated {@link PhoneNumbersClientBuilder} object.
     * @throws NullPointerException If {@code policy} is {@code null}.
     */
    public PhoneNumbersClientBuilder addPolicy(HttpPipelinePolicy policy) {
        this.additionalPolicies.add(Objects.requireNonNull(policy, "'policy' cannot be null."));
        return this;
    }

    /**
     * Sets the client options for all the requests made through the client.
     *
     * @param clientOptions {@link ClientOptions}.
     * @return The updated {@link PhoneNumbersClientBuilder} object.
     * @throws NullPointerException If {@code clientOptions} is {@code null}.
     */
    public PhoneNumbersClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = Objects.requireNonNull(clientOptions, "'clientOptions' cannot be null.");
        return this;
    }

    /**
     * Sets the {@link PhoneNumbersServiceVersion} that is used when making API requests.
     * <p>
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version the client library will have the result of potentially moving to a newer service version.
     *
     * @param version {@link PhoneNumbersServiceVersion} of the service to be used when making requests.
     * @return The updated {@link PhoneNumbersClientBuilder} object.
     */
    public PhoneNumbersClientBuilder serviceVersion(PhoneNumbersServiceVersion version) {
        this.version = version;
        return this;
    }

     /**
     * Sets the {@link RetryPolicy} that is used when each request is sent.
     *
     * @param retryPolicy User's retry policy applied to each request.
     * @return The updated {@link PhoneNumbersClientBuilder} object.
     * @throws NullPointerException If the specified {@code retryPolicy} is null.
     */
    public PhoneNumbersClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = Objects.requireNonNull(retryPolicy, "The retry policy cannot be null");
        return this;
    }

    /**
     * Create synchronous client applying CommunicationClientCredentialPolicy,
     * UserAgentPolicy, RetryPolicy, and CookiePolicy.
     * Additional HttpPolicies specified by additionalPolicies will be applied after them
     *
     * @return {@link PhoneNumbersClient} instance
     */
    public PhoneNumbersClient buildClient() {
        this.validateRequiredFields();

        if (this.version != null) {
            logger.info("Build client for service version" + this.version.getVersion());
        }
        PhoneNumberAdminClientImpl adminClient = this.createPhoneNumberAdminClient();
        return new PhoneNumbersClient(adminClient, this.createPhoneNumberAsyncClient(adminClient));
    }

    /**
     * Create asynchronous client applying CommunicationClientCredentialPolicy,
     * UserAgentPolicy, RetryPolicy, and CookiePolicy.
     * Additional HttpPolicies specified by additionalPolicies will be applied after them
     *
     * @return {@link PhoneNumbersAsyncClient} instance
     */
    public PhoneNumbersAsyncClient buildAsyncClient() {
        this.validateRequiredFields();

        if (this.version != null) {
            logger.info("Build client for service version" + this.version.getVersion());
        }

        return this.createPhoneNumberAsyncClient(this.createPhoneNumberAdminClient());
    }

    PhoneNumbersAsyncClient createPhoneNumberAsyncClient(PhoneNumberAdminClientImpl phoneNumberAdminClient) {
        return new PhoneNumbersAsyncClient(phoneNumberAdminClient);
    }

    HttpPipelinePolicy createAuthenticationPolicy() {
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
                new NullPointerException("Missing credential information while building a client."));
        }
    }

    UserAgentPolicy createUserAgentPolicy(
        String applicationId, String sdkName, String sdkVersion, Configuration configuration) {
        return new UserAgentPolicy(applicationId, sdkName, sdkVersion, configuration);
    }

    HttpPipelinePolicy createRequestIdPolicy() {
        return new RequestIdPolicy();
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

        ClientOptions buildClientOptions = (clientOptions == null) ? new ClientOptions() : clientOptions;
        HttpLogOptions buildLogOptions = (httpLogOptions == null) ? new HttpLogOptions() : httpLogOptions;

        String applicationId = null;
        if (!CoreUtils.isNullOrEmpty(buildClientOptions.getApplicationId())) {
            applicationId = buildClientOptions.getApplicationId();
        } else if (!CoreUtils.isNullOrEmpty(buildLogOptions.getApplicationId())) {
            applicationId = buildLogOptions.getApplicationId();
        }

        // Add required policies
        policyList.add(this.createUserAgentPolicy(
            applicationId,
            PROPERTIES.get(SDK_NAME),
            PROPERTIES.get(SDK_VERSION),
            this.configuration
        ));
        policyList.add(this.createRequestIdPolicy());
        policyList.add(this.retryPolicy == null ? new RetryPolicy() : this.retryPolicy);
        // auth policy is per request, should be after retry
        policyList.add(this.createAuthenticationPolicy());
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
            .clientOptions(clientOptions)
            .build();
    }

    private HttpLogOptions getHttpLogOptions() {
        if (this.httpLogOptions == null) {
            this.httpLogOptions = this.createDefaultHttpLogOptions();
        }

        return this.httpLogOptions;
    }
}
