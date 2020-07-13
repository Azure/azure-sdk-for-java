// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.azure.messaging.eventgrid;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.Configuration;

/**
 * A Builder class to create service clients that can publish events to EventGrid.
 * @see EventGridPublisherAsyncClient
 * @see com.azure.messaging.eventgrid.models.EventGridEvent
 * @see com.azure.messaging.eventgrid.models.CloudEvent
 */
@ServiceClientBuilder(serviceClients = {EventGridPublisherClient.class, EventGridPublisherAsyncClient.class})
public class EventGridPublisherClientBuilder {

    /**
     * Constructs a new instance with a default Http pipeline. The endpoint and credential must be set in order for the
     * client to be correctly built.
     */
    public EventGridPublisherClientBuilder() {
        // TODO: implement method
    }

    /**
     * Adds a policy to the current pipeline.
     * @param httpPipelinePolicy the policy to add.
     *
     * @return the builder itself.
     */
    public EventGridPublisherClientBuilder addPolicy(HttpPipelinePolicy httpPipelinePolicy) {
        // TODO: implement method
        return null;
    }

    /**
     * Builds a publisher client with asynchronous publishing methods and the current settings. Endpoint and credential
     * must be set, all other settings have defaults and are optional.
     * @return a publisher client with asynchronous publishing methods.
     */
    public EventGridPublisherAsyncClient buildAsyncClient() {
        // TODO: implement method
        return null;
    }

    /**
     * Builds a publisher client with synchronous publishing methods and the current settings. Endpoint and credential
     * must be set, all other settings have defaults and are optional.
     * Note that currently the asynchronous client created by the method above is the recommended version for higher
     * performance, as the synchronous client simply blocks on the same asynchronous calls.
     * @return a publisher client with synchronous publishing methods.
     */
    public EventGridPublisherClient buildClient() {
        // TODO: implement method
        return null;
    }

    /**
     * Sets the configuration of HTTP and Azure values. A default is already set.
     * @param configuration the configuration to use.
     *
     * @return the builder itself.
     */
    public EventGridPublisherClientBuilder configuration(Configuration configuration) {
        // TODO: implement method
        return null;
    }

    /**
     * Sets the domain or topic credentials. This must be set in order to build a client.
     * @param credential the credential to authorize publishes to a specific topic/domain endpoint.
     *
     * @return the builder itself.
     */
    public EventGridPublisherClientBuilder credential(AzureKeyCredential credential) {
        // TODO: implement method
        return null;
    }

    /**
     * Sets the domain or topic endpoint. This is the address to publish events to.
     * @param endpoint the endpoint as a url.
     *
     * @return the builder itself.
     */
    public EventGridPublisherClientBuilder endpoint(String endpoint) {
        // TODO: implement method
        return null;
    }

    /**
     * Sets the HTTP Client that sends requests. Will use default if not set.
     * @param httpClient the HTTP Client to use.
     *
     * @return the builder itself.
     */
    public EventGridPublisherClientBuilder httpClient(HttpClient httpClient) {
        // TODO: implement method
        return null;
    }

    /**
     * Configures the logging of the HTTP requests and pipeline.
     * @param httpLogOptions the log options to use.
     *
     * @return the builder itself.
     */
    public EventGridPublisherClientBuilder httpLogOptions(HttpLogOptions httpLogOptions) {
        // TODO: implement method
        return null;
    }

    /**
     * Sets the HTTP pipeline to use. Note that this will reset all previously configured poicies and the client
     * @param httpPipeline the pipeline to use
     *
     * @return the builder itself
     */
    public EventGridPublisherClient pipeline(HttpPipeline httpPipeline) {
        // TODO: implement method
        return null;
    }

}
