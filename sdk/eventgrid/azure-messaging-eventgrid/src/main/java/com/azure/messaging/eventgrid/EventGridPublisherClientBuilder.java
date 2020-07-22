// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.azure.messaging.eventgrid;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.*;
import com.azure.core.http.policy.*;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

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
public class EventGridPublisherClientBuilder {

    private static final String AEG_SAS_KEY = "aeg-sas-key";

    private static final String AEG_SAS_TOKEN = "aeg-sas-token";

    private static final String EVENTGRID_PROPERTIES = "azure-messaging-eventgrid.properties";
    private static final String NAME = "name";
    private static final String VERSION = "version";

    private final String clientName;

    private final String clientVersion;

    private final ClientLogger logger = new ClientLogger(EventGridPublisherClientBuilder.class);

    private final List<HttpPipelinePolicy> policies = new ArrayList<>();

    private Configuration configuration;

    private AzureKeyCredential keyCredential;

    private EventGridSharedAccessSignatureCredential sasToken;

    private String hostname;

    private HttpClient httpClient;

    private HttpLogOptions httpLogOptions;

    private HttpPipeline httpPipeline;

    private EventGridServiceVersion serviceVersion;

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
     * an {@link EventGridSharedAccessSignatureCredential} or a {@link AzureKeyCredential} at the respective methods.
     * All other settings have defaults and are optional.
     * @return a publisher client with asynchronous publishing methods.
     */
    public EventGridPublisherAsyncClient buildAsyncClient() {
        Objects.requireNonNull(hostname, "endpoint cannot be null");

        if (httpPipeline != null) {
            return new EventGridPublisherAsyncClient(httpPipeline, hostname);
        }

        Configuration buildConfiguration = (configuration == null)
            ? Configuration.getGlobalConfiguration()
            : configuration;

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> httpPipelinePolicies = new ArrayList<>();

        httpPipelinePolicies.add(new UserAgentPolicy(httpLogOptions.getApplicationId(), clientName, clientVersion,
            buildConfiguration));
        httpPipelinePolicies.add(new RequestIdPolicy());

        HttpPolicyProviders.addBeforeRetryPolicies(httpPipelinePolicies);
        httpPipelinePolicies.add(retryPolicy == null ? new RetryPolicy() : retryPolicy);

        httpPipelinePolicies.add(new AddDatePolicy());

        // Using token before key if both are set
        if (sasToken != null) {
            httpPipelinePolicies.add((context, next) -> {
                context.getHttpRequest().getHeaders().put(AEG_SAS_TOKEN, sasToken.getToken());
                return next.process();
            });
        } else {
            httpPipelinePolicies.add(new AzureKeyCredentialPolicy(AEG_SAS_KEY, keyCredential));
        }

        httpPipelinePolicies.addAll(policies);

        HttpPolicyProviders.addAfterRetryPolicies(httpPipelinePolicies);

        httpPipelinePolicies.add(new HttpLoggingPolicy(httpLogOptions));

        HttpPipeline buildPipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(httpPipelinePolicies.toArray(new HttpPipelinePolicy[0]))
            .build();

        return new EventGridPublisherAsyncClient(buildPipeline, hostname);
    }

    /**
     * Build a publisher client with synchronous publishing methods and the current settings. Endpoint and a credential
     * must be set (either keyCredential or sharedAccessToken), all other settings have defaults and are optional.
     * Note that currently the asynchronous client created by the method above is the recommended version for higher
     * performance, as the synchronous client simply blocks on the same asynchronous calls.
     * @return a publisher client with synchronous publishing methods.
     */
    public EventGridPublisherClient buildClient() {
        return new EventGridPublisherClient(buildAsyncClient());
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

    public EventGridPublisherClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
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
    public EventGridPublisherClientBuilder keyCredential(AzureKeyCredential credential) {
        this.keyCredential = credential;
        return this;
    }

    /**
     * Set the domain or topic authentication using an already obtained Shared Access Signature token.
     * @param credential the token credential to use.
     *
     * @return the builder itself.
     */
    public EventGridPublisherClientBuilder sharedAccessToken(EventGridSharedAccessSignatureCredential credential) {
        this.sasToken = credential;
        return this;
    }

    /**
     * Set the domain or topic endpoint. This is the address to publish events to.
     * @param endpoint the endpoint as a url.
     *
     * @return the builder itself.
     */
    public EventGridPublisherClientBuilder endpoint(String endpoint) {
        try {
            URL url = new URL(endpoint);
            this.hostname = url.getHost();
        } catch (MalformedURLException e) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("'endpoint' must be a valid URL"));
        }
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


}
