// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration;

import com.azure.core.amqp.implementation.ConnectionStringProperties;
import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.client.traits.ConfigurationTrait;
import com.azure.core.client.traits.ConnectionStringTrait;
import com.azure.core.client.traits.EndpointTrait;
import com.azure.core.client.traits.HttpTrait;
import com.azure.core.client.traits.TokenCredentialTrait;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.AzureException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.policy.AddHeadersFromContextPolicy;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.HttpClientOptions;
import com.azure.core.util.builder.ClientBuilderUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusServiceVersion;
import com.azure.messaging.servicebus.implementation.ServiceBusConstants;
import com.azure.messaging.servicebus.implementation.ServiceBusManagementClientImpl;
import com.azure.messaging.servicebus.implementation.ServiceBusManagementClientImplBuilder;
import com.azure.messaging.servicebus.implementation.ServiceBusManagementSerializer;
import com.azure.messaging.servicebus.implementation.ServiceBusSharedKeyCredential;
import com.azure.messaging.servicebus.implementation.ServiceBusTokenCredentialHttpPolicy;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of {@link
 * ServiceBusAdministrationClient} and {@link ServiceBusAdministrationAsyncClient}. Call
 * {@link #buildClient() buildClient()} and {@link #buildAsyncClient() buildAsyncClient()} respectively to construct an
 * instance of the desired client.
 *
 * <p><strong>Create the sync client using a connection string</strong></p>
 * <!-- src_embed com.azure.messaging.servicebus.administration.servicebusadministrationclient.instantiation -->
 * <pre>
 * &#47;&#47; Retrieve 'connectionString' from your configuration.
 *
 * HttpLogOptions logOptions = new HttpLogOptions&#40;&#41;
 *     .setLogLevel&#40;HttpLogDetailLevel.HEADERS&#41;;
 *
 * ServiceBusAdministrationClient client = new ServiceBusAdministrationClientBuilder&#40;&#41;
 *     .connectionString&#40;connectionString&#41;
 *     .httpLogOptions&#40;logOptions&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.administration.servicebusadministrationclient.instantiation -->
 *
 * <p><strong>Create the async client using Azure Identity</strong></p>
 * <!-- src_embed com.azure.messaging.servicebus.administration.servicebusadministrationasyncclient.instantiation -->
 * <pre>
 * &#47;&#47; DefaultAzureCredential creates a credential based on the environment it is executed in.
 * TokenCredential credential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 *
 * ServiceBusAdministrationAsyncClient client = new ServiceBusAdministrationClientBuilder&#40;&#41;
 *     .connectionString&#40;&quot;&lt;&lt; Service Bus NAMESPACE connection string&gt;&gt;&quot;&#41;
 *     .credential&#40;&quot;&lt;&lt; my-sb-namespace.servicebus.windows.net &gt;&gt;&quot;, credential&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.administration.servicebusadministrationasyncclient.instantiation -->
 *
 * @see ServiceBusAdministrationClient
 * @see ServiceBusAdministrationAsyncClient
 */
@ServiceClientBuilder(serviceClients = {ServiceBusAdministrationClient.class,
    ServiceBusAdministrationAsyncClient.class})
public final class ServiceBusAdministrationClientBuilder implements
    TokenCredentialTrait<ServiceBusAdministrationClientBuilder>,
    ConnectionStringTrait<ServiceBusAdministrationClientBuilder>,
    HttpTrait<ServiceBusAdministrationClientBuilder>,
    ConfigurationTrait<ServiceBusAdministrationClientBuilder>,
    EndpointTrait<ServiceBusAdministrationClientBuilder> {
    private static final String CLIENT_NAME;
    private static final String CLIENT_VERSION;

    static {
        Map<String, String> properties = CoreUtils.getProperties("azure-messaging-servicebus.properties");

        CLIENT_NAME = properties.getOrDefault("name", "UnknownName");
        CLIENT_VERSION = properties.getOrDefault("version", "UnknownVersion");
    }

    private static final ClientLogger LOGGER = new ClientLogger(ServiceBusAdministrationClientBuilder.class);
    private final ServiceBusManagementSerializer serializer = new ServiceBusManagementSerializer();

    private final List<HttpPipelinePolicy> perCallPolicies = new ArrayList<>();
    private final List<HttpPipelinePolicy> perRetryPolicies = new ArrayList<>();

    private Configuration configuration;

    // Endpoint of the Service Bus resource. It will be the fully-qualified domain name of the Service Bus namespace.
    private String endpoint;
    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions = new HttpLogOptions();
    private HttpPipeline pipeline;
    private HttpPipelinePolicy retryPolicy;
    private RetryOptions retryOptions;
    private TokenCredential tokenCredential;
    private ServiceBusServiceVersion serviceVersion;
    private ClientOptions clientOptions;

    /**
     * Constructs a builder with the default parameters.
     */
    public ServiceBusAdministrationClientBuilder() {
    }

    /**
     * Creates a {@link ServiceBusAdministrationAsyncClient} based on options set in the builder. Every time {@code
     * buildAsyncClient} is invoked, a new instance of the client is created.
     *
     * <p>If {@link #pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * {@link #endpoint(String) endpoint} are used to create the {@link ServiceBusAdministrationAsyncClient client}. All
     * other builder settings are ignored.</p>
     *
     * @return A {@link ServiceBusAdministrationAsyncClient} with the options set in the builder.
     * @throws NullPointerException if {@code endpoint} has not been set. This is automatically set when {@link
     *     #connectionString(String) connectionString} is set. Explicitly through {@link #endpoint(String)}, or through
     *     {@link #credential(String, TokenCredential)}.
     * @throws IllegalStateException If applicationId if set in both {@code httpLogOptions} and {@code clientOptions}
     *     and not same.
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)}
     * and {@link #retryPolicy(HttpPipelinePolicy)} have been set.
     */
    public ServiceBusAdministrationAsyncClient buildAsyncClient() {
        if (endpoint == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'endpoint' cannot be null."));
        }

        final ServiceBusServiceVersion apiVersion = serviceVersion == null
            ? ServiceBusServiceVersion.getLatest()
            : serviceVersion;
        final HttpPipeline httpPipeline = createPipeline();
        final ServiceBusManagementClientImpl client = new ServiceBusManagementClientImplBuilder()
            .pipeline(httpPipeline)
            .serializerAdapter(serializer)
            .endpoint(endpoint)
            .apiVersion(apiVersion.getVersion())
            .buildClient();

        return new ServiceBusAdministrationAsyncClient(client, serializer);
    }

    /**
     * Creates a {@link ServiceBusAdministrationClient} based on options set in the builder. Every time {@code
     * buildClient} is invoked, a new instance of the client is created.
     *
     * <p>If {@link #pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * {@link #endpoint(String) endpoint} are used to create the {@link ServiceBusAdministrationClient client}. All
     * other builder settings are ignored.</p>
     *
     * @return A {@link ServiceBusAdministrationClient} with the options set in the builder.
     * @throws NullPointerException if {@code endpoint} has not been set. This is automatically set when {@link
     *     #connectionString(String) connectionString} is set. Explicitly through {@link #endpoint(String)}, or through
     *     {@link #credential(String, TokenCredential)}.
     * @throws IllegalStateException If applicationId if set in both {@code httpLogOptions} and {@code clientOptions}
     *     and not same.
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)}
     * and {@link #retryPolicy(HttpPipelinePolicy)} have been set.
     */
    public ServiceBusAdministrationClient buildClient() {
        return new ServiceBusAdministrationClient(buildAsyncClient());
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
     * @return The updated {@link ServiceBusAdministrationClientBuilder} object.
     * @throws NullPointerException If {@code policy} is {@code null}.
     */
    @Override
    public ServiceBusAdministrationClientBuilder addPolicy(HttpPipelinePolicy policy) {
        Objects.requireNonNull(policy);
        if (policy.getPipelinePosition() == HttpPipelinePosition.PER_CALL) {
            perCallPolicies.add(policy);
        } else {
            perRetryPolicies.add(policy);
        }

        return this;
    }

    /**
     * Sets the service endpoint for the Service Bus namespace.
     *
     * @param endpoint The URL of the Service Bus namespace.
     *
     * @return The updated {@link ServiceBusAdministrationClientBuilder} object.
     * @throws NullPointerException if {@code endpoint} is null.
     * @throws IllegalArgumentException if {@code endpoint} cannot be parsed into a valid URL.
     */
    @Override
    public ServiceBusAdministrationClientBuilder endpoint(String endpoint) {
        final URL url;
        try {
            url = new URL(Objects.requireNonNull(endpoint, "'endpoint' cannot be null."));
        } catch (MalformedURLException ex) {
            throw LOGGER.logExceptionAsWarning(new IllegalArgumentException("'endpoint' must be a valid URL", ex));
        }

        this.endpoint = url.getHost();
        return this;
    }

    /**
     * Sets the configuration store that is used during construction of the service client.
     *
     * The default configuration store is a clone of the {@link Configuration#getGlobalConfiguration() global
     * configuration store}, use {@link Configuration#NONE} to bypass using configuration settings during construction.
     *
     * @param configuration The configuration store used to
     *
     * @return The updated {@link ServiceBusAdministrationClientBuilder} object.
     */
    @Override
    public ServiceBusAdministrationClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the connection string for a Service Bus namespace or a specific Service Bus resource.
     *
     * @param connectionString Connection string for a Service Bus namespace or a specific Service Bus resource.
     *
     * @return The updated {@link ServiceBusAdministrationClientBuilder} object.
     * @throws NullPointerException If {@code connectionString} is {@code null}.
     * @throws IllegalArgumentException If {@code connectionString} is an entity specific connection string, and not
     *     a {@code connectionString} for the Service Bus namespace.
     */
    @Override
    public ServiceBusAdministrationClientBuilder connectionString(String connectionString) {
        Objects.requireNonNull(connectionString, "'connectionString' cannot be null.");

        final ConnectionStringProperties properties = new ConnectionStringProperties(connectionString);
        final TokenCredential tokenCredential;
        try {
            tokenCredential = new ServiceBusSharedKeyCredential(properties.getSharedAccessKeyName(),
                properties.getSharedAccessKey(), ServiceBusConstants.TOKEN_VALIDITY);
        } catch (Exception e) {
            throw LOGGER.logExceptionAsError(
                new AzureException("Could not create the ServiceBusSharedKeyCredential.", e));
        }

        this.endpoint = properties.getEndpoint().getHost();
        if (properties.getEntityPath() != null && !properties.getEntityPath().isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "'connectionString' cannot contain an EntityPath. It should be a namespace connection string."));
        }

        return credential(properties.getEndpoint().getHost(), tokenCredential);
    }

    /**
     * Sets the credential used to authenticate HTTP requests to the Service Bus namespace.
     *
     * @param fullyQualifiedNamespace for the Service Bus.
     * @param credential {@link TokenCredential} to be used for authentication.
     *
     * @return The updated {@link ServiceBusAdministrationClientBuilder} object.
     */
    public ServiceBusAdministrationClientBuilder credential(String fullyQualifiedNamespace,
        TokenCredential credential) {
        this.endpoint = Objects.requireNonNull(fullyQualifiedNamespace,
            "'fullyQualifiedNamespace' cannot be null.");
        this.tokenCredential = Objects.requireNonNull(credential, "'credential' cannot be null.");

        if (CoreUtils.isNullOrEmpty(fullyQualifiedNamespace)) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("'fullyQualifiedNamespace' cannot be an empty string."));
        }

        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authorize requests sent to the service. Refer to the Azure SDK for Java
     * <a href="https://aka.ms/azsdk/java/docs/identity">identity and authentication</a>
     * documentation for more details on proper usage of the {@link TokenCredential} type.
     *
     * @param credential {@link TokenCredential} used to authorize requests sent to the service.
     *
     * @return The updated {@link ServiceBusAdministrationClientBuilder} object.
     */
    @Override
    public ServiceBusAdministrationClientBuilder credential(TokenCredential credential) {
        this.tokenCredential = Objects.requireNonNull(credential, "'credential' cannot be null.");
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
     * @param client The {@link HttpClient} to use for requests.
     * @return The updated {@link ServiceBusAdministrationClientBuilder} object.
     */
    @Override
    public ServiceBusAdministrationClientBuilder httpClient(HttpClient client) {
        if (this.httpClient != null && client == null) {
            LOGGER.info("HttpClient is being set to 'null' when it was previously configured.");
        }

        this.httpClient = client;
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
     * @return The updated {@link ServiceBusAdministrationClientBuilder} object.
     */
    @Override
    public ServiceBusAdministrationClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        httpLogOptions = logOptions;
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
     * @see HttpClientOptions
     * @return The updated {@link ServiceBusAdministrationClientBuilder} object.
     */
    @Override
    public ServiceBusAdministrationClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
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
     * The {@link #endpoint(String) endpoint} is not ignored when
     * {@code pipeline} is set.
     *
     * @param pipeline {@link HttpPipeline} to use for sending service requests and receiving responses.
     * @return The updated {@link ServiceBusAdministrationClientBuilder} object.
     */
    @Override
    public ServiceBusAdministrationClientBuilder pipeline(HttpPipeline pipeline) {
        if (this.pipeline != null && pipeline == null) {
            LOGGER.info("HttpPipeline is being set to 'null' when it was previously configured.");
        }

        this.pipeline = pipeline;
        return this;
    }

    /**
     * Sets the {@link HttpPipelinePolicy} that is used when each request is sent.
     *
     * The default retry policy will be used if not provided {@link #buildAsyncClient()}
     * to build {@link ServiceBusAdministrationClient} or {@link ServiceBusAdministrationAsyncClient}.
     *
     * Setting this is mutually exclusive with using {@link #retryOptions(RetryOptions)}.
     *
     * @param retryPolicy The user's retry policy applied to each request.
     *
     * @return The updated {@link ServiceBusAdministrationClientBuilder} object.
     */
    public ServiceBusAdministrationClientBuilder retryPolicy(HttpPipelinePolicy retryPolicy) {
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
     * Setting this is mutually exclusive with using {@link #retryPolicy(HttpPipelinePolicy)}.
     *
     * @param retryOptions The {@link RetryOptions} to use for all the requests made through the client.
     * @return The updated {@link ServiceBusAdministrationClientBuilder} object.
     */
    @Override
    public ServiceBusAdministrationClientBuilder retryOptions(RetryOptions retryOptions) {
        this.retryOptions = retryOptions;
        return this;
    }

    /**
     * Sets the {@link ServiceBusServiceVersion} that is used. By default {@link ServiceBusServiceVersion#getLatest()}
     * is used when none is specified.
     *
     * @param serviceVersion Service version to use.
     * @return The updated {@link ServiceBusAdministrationClientBuilder} object.
     */
    public ServiceBusAdministrationClientBuilder serviceVersion(ServiceBusServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
        return this;
    }

    /**
     * Builds a new HTTP pipeline if none is set, or returns a user-provided one.
     *
     * @return A new HTTP pipeline or the user-defined one from {@link #pipeline(HttpPipeline)}.
     * @throws IllegalStateException if applicationId is not same in httpLogOptions and clientOptions.
     */
    private HttpPipeline createPipeline() {
        if (pipeline != null) {
            return pipeline;
        }

        final Configuration buildConfiguration = configuration == null
            ? Configuration.getGlobalConfiguration().clone()
            : configuration;

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> httpPolicies = new ArrayList<>();

        // Find applicationId to use
        final String applicationId = CoreUtils.getApplicationId(clientOptions, httpLogOptions);

        httpPolicies.add(new UserAgentPolicy(applicationId, CLIENT_NAME, CLIENT_VERSION, buildConfiguration));
        httpPolicies.add(new ServiceBusTokenCredentialHttpPolicy(tokenCredential));
        httpPolicies.add(new AddHeadersFromContextPolicy());
        httpPolicies.add(new ServiceBusSupplementaryAuthHeaderPolicy(tokenCredential));

        httpPolicies.addAll(perCallPolicies);

        HttpPolicyProviders.addBeforeRetryPolicies(httpPolicies);

        httpPolicies.add(ClientBuilderUtil.validateAndGetRetryPolicy(retryPolicy, retryOptions));
        httpPolicies.addAll(perRetryPolicies);

        if (clientOptions != null) {
            List<HttpHeader> httpHeaderList = new ArrayList<>();
            clientOptions.getHeaders().forEach(h -> httpHeaderList.add(new HttpHeader(h.getName(), h.getValue())));

            if (!httpHeaderList.isEmpty()) {
                httpPolicies.add(new AddHeadersPolicy(new HttpHeaders(httpHeaderList)));
            }
        }

        httpPolicies.add(new HttpLoggingPolicy(httpLogOptions));

        HttpPolicyProviders.addAfterRetryPolicies(httpPolicies);

        return new HttpPipelineBuilder()
            .policies(httpPolicies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .clientOptions(clientOptions)
            .build();
    }
}
