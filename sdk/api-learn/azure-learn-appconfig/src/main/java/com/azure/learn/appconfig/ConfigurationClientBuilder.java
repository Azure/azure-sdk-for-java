// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.learn.appconfig;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.Configuration;
import com.azure.core.util.ServiceVersion;

import com.azure.core.util.logging.ClientLogger;
import com.azure.learn.appconfig.implementation.AzureAppConfigurationImpl;
import com.azure.learn.appconfig.implementation.AzureAppConfigurationImplBuilder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of {@link
 * ConfigurationClient ConfigurationClients} and {@link ConfigurationAsyncClient ConfigurationAsyncClients}, call {@link
 * #buildClient() buildClient} and {@link #buildAsyncClient() buildAsyncClient} respectively to construct an instance of
 * the desired client.
 *
 * <p>The client needs the service endpoint of the Azure App Configuration store and token credential.

 * <p><strong>Instantiating an asynchronous Configuration Client</strong></p>
 *
 * {@codesnippet com.azure.data.applicationconfig.async.configurationclient.instantiation}
 *
 * <p><strong>Instantiating a synchronous Configuration Client</strong></p>
 *
 * {@codesnippet com.azure.data.applicationconfig.configurationclient.instantiation}
 *
 * <p>Another way to construct the client is using a {@link HttpPipeline}. The pipeline gives the client an
 * authenticated way to communicate with the service but it doesn't contain the service endpoint. Set the pipeline
 * {@link #pipeline(HttpPipeline)} and set the service endpoint {@link #endpoint(String)}. Using a
 * pipeline requires additional setup but allows for finer control on how the {@link ConfigurationClient} and {@link
 * ConfigurationAsyncClient} is built.</p>
 *
 * {@codesnippet com.azure.data.applicationconfig.configurationclient.pipeline.instantiation}
 *
 * @see ConfigurationAsyncClient
 * @see ConfigurationClient
 */
@ServiceClientBuilder(serviceClients = {ConfigurationClient.class, ConfigurationAsyncClient.class})
public final class ConfigurationClientBuilder {

    private final AzureAppConfigurationImplBuilder internalBuilder;
    private final ClientLogger logger = new ClientLogger(ConfigurationClientBuilder.class);

    public ConfigurationClientBuilder() {
        this.internalBuilder = new AzureAppConfigurationImplBuilder();
    }

    public ConfigurationClient buildClient() {
        ConfigurationAsyncClient asyncClient = buildAsyncClient();
        return new ConfigurationClient(asyncClient);
    }

    public ConfigurationAsyncClient buildAsyncClient() {
        AzureAppConfigurationImpl internalClient = internalBuilder.buildClient();
        return new ConfigurationAsyncClient(internalClient);
    }

    public ConfigurationClientBuilder credential(TokenCredential credential) {
        // implementation
        return this;
    }

    public ConfigurationClientBuilder httpClient(HttpClient httpClient) {
        // implementation
        return this;
    }

    public ConfigurationClientBuilder endpoint(String endpoint) {
        Objects.requireNonNull(endpoint, "'endpoint' cannot be null");
        try {
            URL url = new URL(endpoint);
        } catch (MalformedURLException exception) {
            logger.logExceptionAsError(new IllegalArgumentException(exception));
        }
        internalBuilder.endpoint(endpoint);
        return this;
    }

    public ConfigurationClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        // implementation
        return this;
    }

    public ConfigurationClientBuilder addPolicy(HttpPipelinePolicy policy) {
        // implementation
        return this;
    }

    public ConfigurationClientBuilder pipeline(HttpPipeline pipeline) {
        // implementation
        return this;
    }

    public ConfigurationClientBuilder configuration(Configuration configuration) {
        internalBuilder.configuration(configuration);
        return this;
    }

    public ConfigurationClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        // implementation
        return this;
    }

    public ConfigurationClientBuilder serviceVersion(ServiceVersion version) {
        // implementation
        return this;
    }

}
