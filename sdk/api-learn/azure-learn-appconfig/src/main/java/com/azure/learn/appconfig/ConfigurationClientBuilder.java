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
        internalBuilder.credential(credential);
        return this;
    }

    public ConfigurationClientBuilder httpClient(HttpClient httpClient) {
        internalBuilder.httpClient(httpClient);
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
        internalBuilder.httpLogOptions(logOptions);
        return this;
    }

    public ConfigurationClientBuilder addPolicy(HttpPipelinePolicy policy) {
        internalBuilder.addPolicy(policy);
        return this;
    }

    public ConfigurationClientBuilder pipeline(HttpPipeline pipeline) {
        internalBuilder.pipeline(pipeline);
        return this;
    }

    public ConfigurationClientBuilder configuration(Configuration configuration) {
        internalBuilder.configuration(configuration);
        return this;
    }

    public ConfigurationClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        internalBuilder.retryPolicy(retryPolicy);
        return this;
    }

    public ConfigurationClientBuilder serviceVersion(ServiceVersion version) {
        return this;
    }

    public ConfigurationClientBuilder connectionString(String connectionString) {
        // implementation
        return this;
    }
}
