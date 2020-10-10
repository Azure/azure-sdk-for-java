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

@ServiceClientBuilder(serviceClients = {ConfigurationClient.class, ConfigurationAsyncClient.class})
public final class ConfigurationClientBuilder {

    public ConfigurationClient buildClient() {
        // implementation
        return new ConfigurationClient();
    }

    public ConfigurationAsyncClient buildAsyncClient() {
        // implementation
        return new ConfigurationAsyncClient();
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
        // implementation
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
        // implementation
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

    public ConfigurationClientBuilder connectionString(String connectionString) {
        // implementation
        return this;
    }
}
