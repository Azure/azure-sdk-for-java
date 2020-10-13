// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.learn.appconfig;

// imports are not included to keep the code snippet focused on API methods

import com.azure.core.http.HttpPipeline;
import com.azure.learn.appconfig.implementation.AzureAppConfigurationImplBuilder;

public class ConfigurationClientBuilder {
    private String endpoint;
    private HttpPipeline pipeline;
    public ConfigurationClientBuilder endpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public ConfigurationClientBuilder pipeline(HttpPipeline pipeline) {
        this.pipeline = pipeline;
        return this;
    }

    public ConfigurationClient buildClient() {
        return new ConfigurationClient(buildAsyncClient());
    }

    public ConfigurationAsyncClient buildAsyncClient() {
        return new ConfigurationAsyncClient(new AzureAppConfigurationImplBuilder()
            .endpoint(endpoint)
            .pipeline(pipeline)
            .buildClient());
    }
}
