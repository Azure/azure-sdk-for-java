// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.phonenumbers;

import com.azure.communication.phonenumbers.implementation.PhoneNumberAdminClientImpl;
import com.azure.communication.phonenumbers.implementation.PhoneNumberAdminClientImplBuilder;
import com.azure.communication.common.implementation.CommunicationConnectionString;
import com.azure.communication.common.implementation.HmacAuthenticationPolicy;
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
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.HttpClientOptions;
import com.azure.core.util.builder.ClientBuilderUtil;
import com.azure.core.util.logging.ClientLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Builder for creating clients of Communication Service phone number configuration
 */
@ServiceClientBuilder(serviceClients = {PhoneNumbersClient.class, PhoneNumbersAsyncClient.class})
public final class PhoneNumbersClientBuilder implements
    AzureKeyCredentialTrait<PhoneNumbersClientBuilder>,
    ConfigurationTrait<PhoneNumbersClientBuilder>,
    ConnectionStringTrait<PhoneNumbersClientBuilder>,
    EndpointTrait<PhoneNumbersClientBuilder>,
    HttpTrait<PhoneNumbersClientBuilder>,
    TokenCredentialTrait<PhoneNumbersClientBuilder> {
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
    private RetryOptions retryOptions;
    private final List<HttpPipelinePolicy> additionalPolicies = new ArrayList<>();
    private String acceptLanguage;

    /**
     * Set endpoint of the service
     *
     * @param endpoint url of the service
     * @return The updated {@link PhoneNumbersClientBuilder} object.
     * @throws NullPointerException If {@code endpoint} is {@code null}.
     */
    @Override
    public PhoneNumbersClientBuilder endpoint(String endpoint) {
        this.endpoint = Objects.requireNonNull(endpoint, "'endpoint' cannot be null.");
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
     * If {@code pipeline} is set, all other settings aside from
     * {@link PhoneNumbersClientBuilder#endpoint(String) endpoint} are ignored.
     *
     * @param pipeline {@link HttpPipeline} to use for sending service requests and receiving responses.
     * @return The updated {@link PhoneNumbersClientBuilder} object.
     */
    @Override
    public PhoneNumbersClientBuilder pipeline(HttpPipeline pipeline) {
        this.pipeline = pipeline;
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
     * @return The updated {@link PhoneNumbersClientBuilder} object.
     */
    @Override
    public PhoneNumbersClientBuilder httpClient(HttpClient httpClient) {
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
     * @param httpLogOptions The {@link HttpLogOptions logging configuration} to use when sending and receiving requests
     * to  and from the service.
     * @return the updated {@link PhoneNumbersClientBuilder} object.
     */
    @Override
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
    @Override
    public PhoneNumbersClientBuilder credential(AzureKeyCredential keyCredential)  {
        this.azureKeyCredential = Objects.requireNonNull(keyCredential, "'keyCredential' cannot be null.");
        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authorize requests sent to the service. Refer to the Azure SDK for Java
     * <a href="https://aka.ms/azsdk/java/docs/identity">identity and authentication</a>
     * documentation for more details on proper usage of the {@link TokenCredential} type.
     *
     * @param tokenCredential {@link TokenCredential} used to authorize requests sent to the service.
     * @return The updated {@link PhoneNumbersClientBuilder} object.
     * @throws NullPointerException If {@code tokenCredential} is null.
     */
    @Override
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
    @Override
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
    @Override
    public PhoneNumbersClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
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
     * @param policy A {@link HttpPipelinePolicy pipeline policy}.
     * @return The updated {@link PhoneNumbersClientBuilder} object.
     * @throws NullPointerException If {@code policy} is {@code null}.
     */
    @Override
    public PhoneNumbersClientBuilder addPolicy(HttpPipelinePolicy policy) {
        this.additionalPolicies.add(Objects.requireNonNull(policy, "'policy' cannot be null."));
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
     * @return The updated {@link PhoneNumbersClientBuilder} object.
     * @throws NullPointerException If {@code clientOptions} is {@code null}.
     * @see HttpClientOptions
     */
    @Override
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
     * <p>
     * Setting this is mutually exclusive with using {@link #retryOptions(RetryOptions)}.
     *
     * @param retryPolicy User's retry policy applied to each request.
     * @return The updated {@link PhoneNumbersClientBuilder} object.
     */
    public PhoneNumbersClientBuilder retryPolicy(RetryPolicy retryPolicy) {
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
     * @return The updated {@link PhoneNumbersClientBuilder} object.
     */
    @Override
    public PhoneNumbersClientBuilder retryOptions(RetryOptions retryOptions) {
        this.retryOptions = retryOptions;
        return this;
    }

    /**
     * Sets the accepted language to be used in the client.
     *
     * @param acceptLanguage The locale to be used in the client. E.g. "en-US"
     * @return The updated {@link PhoneNumbersClientBuilder} object.
     */
    public PhoneNumbersClientBuilder acceptLanguage(String acceptLanguage) {
        this.acceptLanguage = acceptLanguage;
        return this;
    }

    /**
     * Create synchronous client applying CommunicationClientCredentialPolicy,
     * UserAgentPolicy, RetryPolicy, and CookiePolicy.
     * Additional HttpPolicies specified by additionalPolicies will be applied after them
     *
     * @return {@link PhoneNumbersClient} instance
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)}
     * and {@link #retryPolicy(RetryPolicy)} have been set.
     */
    public PhoneNumbersClient buildClient() {
        this.validateRequiredFields();

        if (this.version != null) {
            logger.info("Build client for service version" + this.version.getVersion());
        }
        PhoneNumberAdminClientImpl adminClient = this.createPhoneNumberAdminClient();
        return new PhoneNumbersClient(adminClient, this.createPhoneNumberAsyncClient(adminClient, this.acceptLanguage));
    }

    /**
     * Create asynchronous client applying CommunicationClientCredentialPolicy,
     * UserAgentPolicy, RetryPolicy, and CookiePolicy.
     * Additional HttpPolicies specified by additionalPolicies will be applied after them
     *
     * @return {@link PhoneNumbersAsyncClient} instance
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)}
     * and {@link #retryPolicy(RetryPolicy)} have been set.
     */
    public PhoneNumbersAsyncClient buildAsyncClient() {
        this.validateRequiredFields();

        if (this.version != null) {
            logger.info("Build client for service version" + this.version.getVersion());
        }

        return this.createPhoneNumberAsyncClient(this.createPhoneNumberAdminClient(), this.acceptLanguage);
    }

    PhoneNumbersAsyncClient createPhoneNumberAsyncClient(PhoneNumberAdminClientImpl phoneNumberAdminClient) {
        return this.createPhoneNumberAsyncClient(phoneNumberAdminClient, null);
    }

    PhoneNumbersAsyncClient createPhoneNumberAsyncClient(PhoneNumberAdminClientImpl phoneNumberAdminClient, String acceptLanguage) {
        return new PhoneNumbersAsyncClient(phoneNumberAdminClient, acceptLanguage);
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
        policyList.add(ClientBuilderUtil.validateAndGetRetryPolicy(retryPolicy, retryOptions));
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
