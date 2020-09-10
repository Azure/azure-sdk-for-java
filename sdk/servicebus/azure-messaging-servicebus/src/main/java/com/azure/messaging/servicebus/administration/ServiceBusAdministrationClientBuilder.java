// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration;

import com.azure.core.amqp.implementation.ConnectionStringProperties;
import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.AzureException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AddHeadersFromContextPolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
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
 * {@link #buildClient() buildClient} and {@link #buildAsyncClient() buildAsyncClient} respectively to construct an
 * instance of the desired client.
 *
 * @see ServiceBusAdministrationClient
 * @see ServiceBusAdministrationAsyncClient
 */
@ServiceClientBuilder(serviceClients = {ServiceBusAdministrationClient.class,
    ServiceBusAdministrationAsyncClient.class})
public class ServiceBusAdministrationClientBuilder {
    private final ClientLogger logger = new ClientLogger(ServiceBusAdministrationClientBuilder.class);
    private final ServiceBusManagementSerializer serializer = new ServiceBusManagementSerializer();
    private final List<HttpPipelinePolicy> userPolicies = new ArrayList<>();
    private final Map<String, String> properties =
        CoreUtils.getProperties("azure-messaging-servicebus.properties");

    private Configuration configuration;

    // Endpoint of the Service Bus resource. It will be the fully-qualified domain name of the Service Bus namespace.
    private String endpoint;
    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions = new HttpLogOptions();
    private HttpPipeline pipeline;
    private HttpPipelinePolicy retryPolicy;
    private TokenCredential tokenCredential;
    private ServiceBusServiceVersion serviceVersion;

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
     *     #connectionString(String) connectionString} is set. Or, explicitly through {@link #endpoint(String)}.
     * @throws IllegalStateException If {@link #connectionString(String) connectionString} has not been set.
     */
    public ServiceBusAdministrationAsyncClient buildAsyncClient() {
        if (endpoint == null) {
            throw logger.logExceptionAsError(new NullPointerException("'endpoint' cannot be null."));
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
     * buildAsyncClient} is invoked, a new instance of the client is created.
     *
     * <p>If {@link #pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * {@link #endpoint(String) endpoint} are used to create the {@link ServiceBusAdministrationClient client}. All
     * other builder settings are ignored.</p>
     *
     * @return A {@link ServiceBusAdministrationClient} with the options set in the builder.
     * @throws NullPointerException if {@code endpoint} has not been set. This is automatically set when {@link
     *     #connectionString(String) connectionString} is set. Or it can be set explicitly through {@link
     *     #endpoint(String)}.
     * @throws IllegalStateException If {@link #connectionString(String) connectionString} has not been set.
     */
    public ServiceBusAdministrationClient buildClient() {
        return new ServiceBusAdministrationClient(buildAsyncClient());
    }

    /**
     * Adds a policy to the set of existing policies that are executed after required policies.
     *
     * @param policy The retry policy for service requests.
     *
     * @return The updated {@link ServiceBusAdministrationClientBuilder} object.
     * @throws NullPointerException If {@code policy} is {@code null}.
     */
    public ServiceBusAdministrationClientBuilder addPolicy(HttpPipelinePolicy policy) {
        Objects.requireNonNull(policy);
        userPolicies.add(policy);
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
    public ServiceBusAdministrationClientBuilder endpoint(String endpoint) {
        final URL url;
        try {
            url = new URL(Objects.requireNonNull(endpoint, "'endpoint' cannot be null."));
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("'endpoint' must be a valid URL"));
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
    public ServiceBusAdministrationClientBuilder connectionString(String connectionString) {
        Objects.requireNonNull(connectionString, "'connectionString' cannot be null.");

        final ConnectionStringProperties properties = new ConnectionStringProperties(connectionString);
        final TokenCredential tokenCredential;
        try {
            tokenCredential = new ServiceBusSharedKeyCredential(properties.getSharedAccessKeyName(),
                properties.getSharedAccessKey(), ServiceBusConstants.TOKEN_VALIDITY);
        } catch (Exception e) {
            throw logger.logExceptionAsError(
                new AzureException("Could not create the ServiceBusSharedKeyCredential.", e));
        }

        this.endpoint = properties.getEndpoint().getHost();
        if (properties.getEntityPath() != null && !properties.getEntityPath().isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
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
            throw logger.logExceptionAsError(
                new IllegalArgumentException("'fullyQualifiedNamespace' cannot be an empty string."));
        }

        return this;
    }

    /**
     * Sets the HTTP client to use for sending and receiving requests to and from the service.
     *
     * @param client The HTTP client to use for requests.
     *
     * @return The updated {@link ServiceBusAdministrationClientBuilder} object.
     */
    public ServiceBusAdministrationClientBuilder httpClient(HttpClient client) {
        if (this.httpClient != null && client == null) {
            logger.info("HttpClient is being set to 'null' when it was previously configured.");
        }

        this.httpClient = client;
        return this;
    }

    /**
     * Sets the logging configuration for HTTP requests and responses.
     *
     * <p>If logLevel is not provided, default value of {@link HttpLogDetailLevel#NONE} is set.</p>
     *
     * @param logOptions The logging configuration to use when sending and receiving HTTP requests/responses.
     *
     * @return The updated {@link ServiceBusAdministrationClientBuilder} object.
     */
    public ServiceBusAdministrationClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        httpLogOptions = logOptions;
        return this;
    }

    /**
     * Sets the HTTP pipeline to use for the service client.
     *
     * If {@code pipeline} is set, all other settings are ignored, aside from {@link
     * ServiceBusAdministrationClientBuilder#endpoint(String) endpoint} to build {@link ServiceBusAdministrationClient}
     * or {@link ServiceBusAdministrationAsyncClient}.
     *
     * @param pipeline The HTTP pipeline to use for sending service requests and receiving responses.
     *
     * @return The updated {@link ServiceBusAdministrationClientBuilder} object.
     */
    public ServiceBusAdministrationClientBuilder pipeline(HttpPipeline pipeline) {
        if (this.pipeline != null && pipeline == null) {
            logger.info("HttpPipeline is being set to 'null' when it was previously configured.");
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
     * @param retryPolicy user's retry policy applied to each request.
     *
     * @return The updated {@link ServiceBusAdministrationClientBuilder} object.
     */
    public ServiceBusAdministrationClientBuilder retryPolicy(HttpPipelinePolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
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
        final String clientName = properties.getOrDefault("name", "UnknownName");
        final String clientVersion = properties.getOrDefault("version", "UnknownVersion");

        httpPolicies.add(new UserAgentPolicy(httpLogOptions.getApplicationId(), clientName, clientVersion,
            buildConfiguration));
        httpPolicies.add(new ServiceBusTokenCredentialHttpPolicy(tokenCredential));
        httpPolicies.add(new AddHeadersFromContextPolicy());

        HttpPolicyProviders.addBeforeRetryPolicies(httpPolicies);

        httpPolicies.add(retryPolicy == null ? new RetryPolicy() : retryPolicy);
        httpPolicies.addAll(userPolicies);
        httpPolicies.add(new HttpLoggingPolicy(httpLogOptions));

        HttpPolicyProviders.addAfterRetryPolicies(httpPolicies);

        return new HttpPipelineBuilder()
            .policies(httpPolicies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();
    }
}
