// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.identity;

import com.azure.communication.common.implementation.CommunicationConnectionString;
import com.azure.communication.common.implementation.HmacAuthenticationPolicy;
import com.azure.communication.identity.implementation.CommunicationIdentityClientImpl;
import com.azure.communication.identity.implementation.CommunicationIdentityClientImplBuilder;
import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.client.traits.AzureKeyCredentialTrait;
import com.azure.core.client.traits.ConfigurationTrait;
import com.azure.core.client.traits.ConnectionStringTrait;
import com.azure.core.client.traits.EndpointTrait;
import com.azure.core.client.traits.HttpTrait;
import com.azure.core.client.traits.TokenCredentialTrait;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.AzureKeyCredential;
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
 * This class provides a fluent builder API to help aid the configuration and instantiation of {@link
 * CommunicationIdentityClient CommunicationIdentityClients} and {@link CommunicationIdentityAsyncClient CommunicationIdentityAsyncClients}, call {@link
 * #buildClient() buildClient} and {@link #buildAsyncClient() buildAsyncClient} respectively to construct an instance of
 * the desired client.
 *
 * <p><strong>Instantiating an asynchronous Azure Communication Service Identity Client</strong></p>
 *
 * <!-- src_embed readme-sample-createCommunicationIdentityAsyncClient -->
 * <pre>
 * &#47;&#47; You can find your endpoint and access key from your resource in the Azure Portal
 * String endpoint = &quot;https:&#47;&#47;&lt;RESOURCE_NAME&gt;.communication.azure.com&quot;;
 * AzureKeyCredential keyCredential = new AzureKeyCredential&#40;&quot;&lt;access-key&gt;&quot;&#41;;
 *
 * CommunicationIdentityAsyncClient communicationIdentityAsyncClient = new CommunicationIdentityClientBuilder&#40;&#41;
 *         .endpoint&#40;endpoint&#41;
 *         .credential&#40;keyCredential&#41;
 *         .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end readme-sample-createCommunicationIdentityAsyncClient -->
 *
 * <p><strong>Instantiating a synchronous Azure Communication Service Identity Client</strong></p>
 *
 * <!-- src_embed readme-sample-createCommunicationIdentityClient -->
 * <pre>
 * &#47;&#47; You can find your endpoint and access key from your resource in the Azure Portal
 * String endpoint = &quot;https:&#47;&#47;&lt;RESOURCE_NAME&gt;.communication.azure.com&quot;;
 * AzureKeyCredential keyCredential = new AzureKeyCredential&#40;&quot;&lt;access-key&gt;&quot;&#41;;
 *
 * CommunicationIdentityClient communicationIdentityClient = new CommunicationIdentityClientBuilder&#40;&#41;
 *     .endpoint&#40;endpoint&#41;
 *     .credential&#40;keyCredential&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end readme-sample-createCommunicationIdentityClient -->
 *
 * @see CommunicationIdentityAsyncClient
 * @see CommunicationIdentityClient
 */
@ServiceClientBuilder(serviceClients = {CommunicationIdentityClient.class, CommunicationIdentityAsyncClient.class})
public final class CommunicationIdentityClientBuilder implements
    AzureKeyCredentialTrait<CommunicationIdentityClientBuilder>,
    ConfigurationTrait<CommunicationIdentityClientBuilder>,
    ConnectionStringTrait<CommunicationIdentityClientBuilder>,
    EndpointTrait<CommunicationIdentityClientBuilder>,
    HttpTrait<CommunicationIdentityClientBuilder>,
    TokenCredentialTrait<CommunicationIdentityClientBuilder> {
    private static final String SDK_NAME = "name";
    private static final String SDK_VERSION = "version";

    private static final String COMMUNICATION_IDENTITY_PROPERTIES =
        "azure-communication-identity.properties";

    private final ClientLogger logger = new ClientLogger(CommunicationIdentityClientBuilder.class);
    private String endpoint;
    private AzureKeyCredential azureKeyCredential;
    private TokenCredential tokenCredential;
    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions = new HttpLogOptions();
    private HttpPipeline pipeline;
    private RetryPolicy retryPolicy;
    private RetryOptions retryOptions;
    private Configuration configuration;
    private ClientOptions clientOptions;
    private final Map<String, String> properties = CoreUtils.getProperties(COMMUNICATION_IDENTITY_PROPERTIES);
    private final List<HttpPipelinePolicy> customPolicies = new ArrayList<HttpPipelinePolicy>();
    private CommunicationIdentityServiceVersion serviceVersion;

    /**
     * Set endpoint of the service
     *
     * @param endpoint url of the service
     * @return CommunicationIdentityClientBuilder
     */
    @Override
    public CommunicationIdentityClientBuilder endpoint(String endpoint) {
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
     * If a pipeline is not supplied, the credential and httpClient fields must be set
     * </p>
     *
     * @param pipeline {@link HttpPipeline} to use for sending service requests and receiving responses.
     * @return CommunicationIdentityClientBuilder
     */
    @Override
    public CommunicationIdentityClientBuilder pipeline(HttpPipeline pipeline) {
        this.pipeline = Objects.requireNonNull(pipeline, "'pipeline' cannot be null.");
        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authorize requests sent to the service. Refer to the Azure SDK for Java
     * <a href="https://aka.ms/azsdk/java/docs/identity">identity and authentication</a>
     * documentation for more details on proper usage of the {@link TokenCredential} type.
     *
     * @param tokenCredential {@link TokenCredential} used to authorize requests sent to the service.
     * @return The updated {@link CommunicationIdentityClientBuilder} object.
     * @throws NullPointerException If {@code tokenCredential} is null.
     */
    @Override
    public CommunicationIdentityClientBuilder credential(TokenCredential tokenCredential) {
        this.tokenCredential = Objects.requireNonNull(tokenCredential, "'tokenCredential' cannot be null.");
        return this;
    }

    /**
     * Sets the {@link AzureKeyCredential} used to authenticate HTTP requests.
     *
    * @param keyCredential The {@link AzureKeyCredential} used to authenticate HTTP requests.
     * @return The updated {@link CommunicationIdentityClientBuilder} object.
     * @throws NullPointerException If {@code keyCredential} is null.
     */
    @Override
    public CommunicationIdentityClientBuilder credential(AzureKeyCredential keyCredential)  {
        this.azureKeyCredential = Objects.requireNonNull(keyCredential, "'keyCredential' cannot be null.");
        return this;
    }

    /**
     * Set endpoint and credential to use
     *
     * @param connectionString connection string for setting endpoint and initalizing CommunicationClientCredential
     * @return CommunicationIdentityClientBuilder
     */
    @Override
    public CommunicationIdentityClientBuilder connectionString(String connectionString) {
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
     * @return CommunicationIdentityClientBuilder
     */
    @Override
    public CommunicationIdentityClientBuilder httpClient(HttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient, "'httpClient' cannot be null.");
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
     * @return CommunicationIdentityClientBuilder
     * @throws NullPointerException If {@code pipelinePolicy} is {@code null}.
     */
    @Override
    public CommunicationIdentityClientBuilder addPolicy(HttpPipelinePolicy customPolicy) {
        this.customPolicies.add(Objects.requireNonNull(customPolicy, "'customPolicy' cannot be null."));
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
     * @return The updated {@link CommunicationIdentityClientBuilder} object.
     * @throws NullPointerException If {@code clientOptions} is {@code null}.
     * @see HttpClientOptions
     */
    @Override
    public CommunicationIdentityClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = Objects.requireNonNull(clientOptions, "'clientOptions' cannot be null.");
        return this;
    }

    /**
     * Sets the configuration object used to retrieve environment configuration values during building of the client.
     *
     * @param configuration Configuration store used to retrieve environment configurations.
     * @return the updated CommunicationIdentityClientBuilder object
     */
    @Override
    public CommunicationIdentityClientBuilder configuration(Configuration configuration) {
        this.configuration = Objects.requireNonNull(configuration, "'configuration' cannot be null.");
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
     * @return the updated CommunicationIdentityClientBuilder object
     */
    @Override
    public CommunicationIdentityClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        this.httpLogOptions = Objects.requireNonNull(logOptions, "'logOptions' cannot be null.");
        return this;
    }

    /**
     * Sets the {@link RetryPolicy} that is used when each request is sent.
     * <p>
     * A default retry policy will be supplied if one isn't provided.
     * <p>
     * Setting this is mutually exclusive with using {@link #retryOptions(RetryOptions)}.
     *
     * @param retryPolicy User's retry policy applied to each request.
     * @return The updated {@link CommunicationIdentityClientBuilder} object.
     */
    public CommunicationIdentityClientBuilder retryPolicy(RetryPolicy retryPolicy) {
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
     * @return The updated {@link CommunicationIdentityClientBuilder} object.
     */
    @Override
    public CommunicationIdentityClientBuilder retryOptions(RetryOptions retryOptions) {
        this.retryOptions = retryOptions;
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
        this.serviceVersion = version;
        return this;
    }

    /**
     * Create asynchronous client applying HMACAuthenticationPolicy, UserAgentPolicy,
     * RetryPolicy, and CookiePolicy.
     * Additional HttpPolicies specified by additionalPolicies will be applied after them
     *
     * @return CommunicationIdentityAsyncClient instance
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)}
     * and {@link #retryPolicy(RetryPolicy)} have been set.
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
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)}
     * and {@link #retryPolicy(RetryPolicy)} have been set.
     */
    public CommunicationIdentityClient buildClient() {
        return new CommunicationIdentityClient(createServiceImpl());
    }

    private CommunicationIdentityClientImpl createServiceImpl() {
        Objects.requireNonNull(endpoint);

        HttpPipeline builderPipeline = this.pipeline;
        if (this.pipeline == null) {
            builderPipeline = createHttpPipeline(httpClient,
                createHttpPipelineAuthPolicy(),
                customPolicies);
        }

        CommunicationIdentityServiceVersion apiVersion = serviceVersion != null ? serviceVersion : CommunicationIdentityServiceVersion.getLatest();

        CommunicationIdentityClientImplBuilder clientBuilder = new CommunicationIdentityClientImplBuilder();
        clientBuilder.endpoint(endpoint)
            .apiVersion(apiVersion.getVersion())
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
        policies.add(ClientBuilderUtil.validateAndGetRetryPolicy(retryPolicy, retryOptions));
        policies.add(new CookiePolicy());
        // auth policy is per request, should be after retry
        policies.add(authorizationPolicy);
        policies.add(new HttpLoggingPolicy(httpLogOptions));
    }
}
