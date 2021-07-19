// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.AzureKeyCredentialPolicy;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.models.CloudEvent;
import com.azure.core.util.BinaryData;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.TracerProxy;
import com.azure.messaging.eventgrid.implementation.CloudEventTracingPipelinePolicy;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A Builder class to create service clients that can publish events to EventGrid.
 * @see EventGridPublisherAsyncClient
 * @see EventGridEvent
 * @see CloudEvent
 */
@ServiceClientBuilder(serviceClients = {EventGridPublisherClient.class, EventGridPublisherAsyncClient.class})
public final class EventGridPublisherClientBuilder {

    private static final String AEG_SAS_KEY = "aeg-sas-key";

    private static final String AEG_SAS_TOKEN = "aeg-sas-token";

    private static final String EVENTGRID_PROPERTIES = "azure-messaging-eventgrid.properties";
    private static final String NAME = "name";
    private static final String VERSION = "version";
    private static final String DEFAULT_EVENTGRID_SCOPE = "https://eventgrid.azure.net/.default";

    private final String clientName;

    private final String clientVersion;

    private final ClientLogger logger = new ClientLogger(EventGridPublisherClientBuilder.class);

    private final List<HttpPipelinePolicy> policies = new ArrayList<>();

    private ClientOptions clientOptions;

    private Configuration configuration;

    private AzureKeyCredential keyCredential;

    private AzureSasCredential sasToken;

    private TokenCredential tokenCredential;

    private EventGridServiceVersion serviceVersion;

    private String endpoint;

    private HttpClient httpClient;

    private HttpLogOptions httpLogOptions;

    private HttpPipeline httpPipeline;

    private RetryPolicy retryPolicy;

    /**
     * Construct a new instance with default building settings. The endpoint and one credential method must be set
     * in order for the client to be built.
     */
    public EventGridPublisherClientBuilder() {
        this.httpLogOptions = new HttpLogOptions();
        Map<String, String> properties = CoreUtils.getProperties(EVENTGRID_PROPERTIES);
        clientName = properties.getOrDefault(NAME, "UnknownName");
        clientVersion = properties.getOrDefault(VERSION, "UnknownVersion");
    }


    /**
     * Build a publisher client with asynchronous publishing methods and the current settings. An endpoint must be set,
     * and either a pipeline with correct authentication must be set, or a credential must be set in the form of
     * an {@link AzureSasCredential} or a {@link AzureKeyCredential} at the respective methods.
     * All other settings have defaults and are optional.
     * @return a publisher client with asynchronous publishing methods.
     * @throws NullPointerException if {@code endpoint} is null.
     */
    private <T> EventGridPublisherAsyncClient<T> buildAsyncClient(Class<T> eventClass) {
        Objects.requireNonNull(endpoint, "'endpoint' is required and can not be null.");
        EventGridServiceVersion buildServiceVersion = serviceVersion == null
            ? EventGridServiceVersion.getLatest()
            : serviceVersion;

        if (httpPipeline != null) {
            return new EventGridPublisherAsyncClient<T>(httpPipeline, endpoint, buildServiceVersion, eventClass);
        }

        Configuration buildConfiguration = (configuration == null)
            ? Configuration.getGlobalConfiguration()
            : configuration;

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> httpPipelinePolicies = new ArrayList<>();

        String applicationId =
            clientOptions == null ? httpLogOptions.getApplicationId() : clientOptions.getApplicationId();

        httpPipelinePolicies.add(new UserAgentPolicy(applicationId, clientName, clientVersion,
            buildConfiguration));
        httpPipelinePolicies.add(new RequestIdPolicy());

        HttpPolicyProviders.addBeforeRetryPolicies(httpPipelinePolicies);
        httpPipelinePolicies.add(retryPolicy == null ? new RetryPolicy() : retryPolicy);

        httpPipelinePolicies.add(new AddDatePolicy());

        final int credentialCount = (sasToken != null ? 1 : 0) + (keyCredential != null ? 1 : 0)
            + (tokenCredential != null ? 1 : 0);
        if (credentialCount > 1) {
            throw logger.logExceptionAsError(
                new IllegalStateException("More than 1 credentials are set while building a client. "
                    + "You should set one and only one credential of type 'TokenCredential', 'AzureSasCredential', "
                    + "or 'AzureKeyCredential'."));
        } else if (credentialCount == 0) {
            throw logger.logExceptionAsError(
                new IllegalStateException("Missing credential information while building a client."
                    + "You should set one and only one credential of type 'TokenCredential', 'AzureSasCredential', "
                    + "or 'AzureKeyCredential'."));
        }
        if (sasToken != null) {
            httpPipelinePolicies.add((context, next) -> {
                context.getHttpRequest().getHeaders().set(AEG_SAS_TOKEN, sasToken.getSignature());
                return next.process();
            });
        } else if (keyCredential != null) {
            httpPipelinePolicies.add(new AzureKeyCredentialPolicy(AEG_SAS_KEY, keyCredential));
        } else {
            httpPipelinePolicies.add(new BearerTokenAuthenticationPolicy(this.tokenCredential,
                DEFAULT_EVENTGRID_SCOPE));
        }

        httpPipelinePolicies.addAll(policies);

        if (clientOptions != null) {
            List<HttpHeader> httpHeaderList = new ArrayList<>();
            clientOptions.getHeaders().forEach(header ->
                httpHeaderList.add(new HttpHeader(header.getName(), header.getValue())));
            policies.add(new AddHeadersPolicy(new HttpHeaders(httpHeaderList)));
        }

        HttpPolicyProviders.addAfterRetryPolicies(httpPipelinePolicies);

        if (TracerProxy.isTracingEnabled()) {
            httpPipelinePolicies.add(new CloudEventTracingPipelinePolicy());
        }
        httpPipelinePolicies.add(new HttpLoggingPolicy(httpLogOptions));

        HttpPipeline buildPipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(httpPipelinePolicies.toArray(new HttpPipelinePolicy[0]))
            .build();


        return new EventGridPublisherAsyncClient<T>(buildPipeline, endpoint, buildServiceVersion, eventClass);
    }

    /**
     * Build a publisher client with synchronous publishing methods and the current settings. Endpoint and a credential
     * must be set (either keyCredential or sharedAccessSignatureCredential), all other settings have defaults and/or are optional.
     * Note that currently the asynchronous client created by the method above is the recommended version for higher
     * performance, as the synchronous client simply blocks on the same asynchronous calls.
     * @return a publisher client with synchronous publishing methods.
     */
    private <T> EventGridPublisherClient<T> buildClient(Class<T> eventClass) {
        return new EventGridPublisherClient<T>(buildAsyncClient(eventClass));
    }

    /**
     * Add a policy to the current pipeline.
     * @param httpPipelinePolicy the policy to add.
     *
     * @return the builder itself.
     */
    public EventGridPublisherClientBuilder addPolicy(HttpPipelinePolicy httpPipelinePolicy) {
        this.policies.add(Objects.requireNonNull(httpPipelinePolicy));
        return this;
    }

    /**
     * Add a custom retry policy to the pipeline. The default is {@link RetryPolicy#RetryPolicy()}
     * @param retryPolicy the retry policy to add.
     *
     * @return the builder itself.
     */
    public EventGridPublisherClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
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
     * @return The updated EventGridPublisherClientBuilder object.
     */
    public EventGridPublisherClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
        return this;
    }

    /**
     * Set the configuration of HTTP and Azure values. A default is already set.
     * @param configuration the configuration to use.
     *
     * @return the builder itself.
     */
    public EventGridPublisherClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Set the domain or topic authentication using a key obtained from Azure CLI, Azure portal, or the ARM SDKs.
     * @param credential the key credential to use to authorize the publisher client.
     *
     * @return the builder itself.
     */
    public EventGridPublisherClientBuilder credential(AzureKeyCredential credential) {
        this.keyCredential = credential;
        return this;
    }

    /**
     * Set the domain or topic authentication using an already obtained Shared Access Signature token.
     * @param credential the sas credential to use.
     *
     * @return the builder itself.
     */
    public EventGridPublisherClientBuilder credential(AzureSasCredential credential) {
        this.sasToken = credential;
        return this;
    }

    /**
     * Set the domain or topic authentication using Azure Activity Directory authentication.
     * Refer to <a href="https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity">azure-identity</a>
     *
     * @param credential the token credential to use.
     *
     * @return the builder itself.
     */
    public EventGridPublisherClientBuilder credential(TokenCredential credential) {
        this.tokenCredential = credential;
        return this;
    }

    /**
     * Set the domain or topic endpoint. This is the address to publish events to.
     * It must be the full url of the endpoint instead of just the hostname.
     * @param endpoint the endpoint as a url.
     *
     * @return the builder itself.
     * @throws NullPointerException if {@code endpoint} is null.
     * @throws IllegalArgumentException if {@code endpoint} cannot be parsed into a valid URL.
     */
    public EventGridPublisherClientBuilder endpoint(String endpoint) {
        try {
            new URL(Objects.requireNonNull(endpoint, "'endpoint' cannot be null."));
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("'endpoint' must be a valid URL", ex));
        }
        this.endpoint = endpoint;
        return this;
    }

    /**
     * Set the HTTP Client that sends requests. Will use default if not set.
     * @param httpClient the HTTP Client to use.
     *
     * @return the builder itself.
     */
    public EventGridPublisherClientBuilder httpClient(HttpClient httpClient) {
        if (this.httpClient != null && httpClient == null) {
            logger.info("Http client is set to null when it was not previously null");
        }
        this.httpClient = httpClient;
        return this;
    }

    /**
     * Configure the logging of the HTTP requests and pipeline.
     * @param httpLogOptions the log options to use.
     *
     * @return the builder itself.
     */
    public EventGridPublisherClientBuilder httpLogOptions(HttpLogOptions httpLogOptions) {
        this.httpLogOptions = httpLogOptions;
        return this;
    }

    /**
     * Set the HTTP pipeline to use when sending calls to the service.
     * @param httpPipeline the pipeline to use.
     *
     * @return the builder itself.
     */
    public EventGridPublisherClientBuilder pipeline(HttpPipeline httpPipeline) {
        if (this.httpPipeline != null && httpPipeline == null) {
            logger.info("Http client is set to null when it was not previously null");
        }
        this.httpPipeline = httpPipeline;
        return this;
    }

    /**
     * Set the service version to use for requests to the event grid service. See {@link EventGridServiceVersion} for
     * more information about possible service versions.
     * @param serviceVersion the service version to set. By default this will use the latest available version.
     *
     * @return the builder itself
     */
    public EventGridPublisherClientBuilder serviceVersion(EventGridServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
        return this;
    }

    /**
     * Build a {@link CloudEvent} publisher client with asynchronous publishing methods and the current settings. An endpoint must be set,
     * and either a pipeline with correct authentication must be set, or a credential must be set in the form of
     * an {@link AzureSasCredential} or a {@link AzureKeyCredential} at the respective methods.
     * All other settings have defaults and are optional.
     * @return a publisher client with asynchronous publishing methods.
     */
    public EventGridPublisherAsyncClient<CloudEvent> buildCloudEventPublisherAsyncClient() {
        return this.buildAsyncClient(CloudEvent.class);
    }

    /**
     * Build an {@link EventGridEvent} publisher client with asynchronous publishing methods and the current settings. An endpoint must be set,
     * and either a pipeline with correct authentication must be set, or a credential must be set in the form of
     * an {@link AzureSasCredential} or a {@link AzureKeyCredential} at the respective methods.
     * All other settings have defaults and are optional.
     * @return a publisher client with asynchronous publishing methods.
     */
    public EventGridPublisherAsyncClient<EventGridEvent> buildEventGridEventPublisherAsyncClient() {
        return this.buildAsyncClient(EventGridEvent.class);
    }

    /**
     * Build a custom event publisher client with asynchronous publishing methods and the current settings. An endpoint must be set,
     * and either a pipeline with correct authentication must be set, or a credential must be set in the form of
     * an {@link AzureSasCredential} or a {@link AzureKeyCredential} at the respective methods.
     * All other settings have defaults and are optional.
     * @return a publisher client with asynchronous publishing methods.
     */
    public EventGridPublisherAsyncClient<BinaryData> buildCustomEventPublisherAsyncClient() {
        return this.buildAsyncClient(BinaryData.class);
    }

    /**
     * Build a {@link CloudEvent} publisher client with synchronous publishing methods and the current settings. Endpoint and a credential
     * must be set (either keyCredential or sharedAccessSignatureCredential), all other settings have defaults and/or are optional.
     * @return a publisher client with synchronous publishing methods.
     */
    public EventGridPublisherClient<CloudEvent> buildCloudEventPublisherClient() {
        return this.buildClient(CloudEvent.class);
    }

    /**
     * Build an {@link EventGridEvent} publisher client with synchronous publishing methods and the current settings. Endpoint and a credential
     * must be set (either keyCredential or sharedAccessSignatureCredential), all other settings have defaults and/or are optional.
     * @return a publisher client with synchronous publishing methods.
     */
    public EventGridPublisherClient<EventGridEvent> buildEventGridEventPublisherClient() {
        return this.buildClient(EventGridEvent.class);
    }

    /**
     * Build a custom event publisher client with synchronous publishing methods and the current settings. Endpoint and a credential
     * must be set (either keyCredential or sharedAccessSignatureCredential), all other settings have defaults and/or are optional.
     * @return a publisher client with synchronous publishing methods.
     */
    public EventGridPublisherClient<BinaryData> buildCustomEventPublisherClient() {
        return this.buildClient(BinaryData.class);
    }
}
