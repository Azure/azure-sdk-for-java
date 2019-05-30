// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.configuration.Configuration;
import com.azure.core.credentials.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpPipelinePolicy;

import java.net.MalformedURLException;

public final class QueueAsyncClientBuilder {
    private final QueueClientBuilderBase builder;

    QueueAsyncClientBuilder() {
        builder = new QueueClientBuilderBase();
    }

    public QueueAsyncClient build() {
        return new QueueAsyncClient(builder.build());
    }

    public QueueAsyncClientBuilder endpoint(String endpoint) throws MalformedURLException {
        builder.endpoint(endpoint);
        return this;
    }

    public QueueAsyncClientBuilder credentials(TokenCredential credentials) {
        builder.credentials(credentials);
        return this;
    }

    public QueueAsyncClientBuilder connectionString(String connectionString) {
        builder.connectionString(connectionString);
        return this;
    }

    public QueueAsyncClientBuilder httpClient(HttpClient httpClient) {
        builder.httpClient(httpClient);
        return this;
    }

    public QueueAsyncClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        builder.addPolicy(pipelinePolicy);
        return this;
    }

    public QueueAsyncClientBuilder httpLogDetailLevel(HttpLogDetailLevel logLevel) {
        builder.httpLogDetailLevel(logLevel);
        return this;
    }

    public QueueAsyncClientBuilder configuration(Configuration configuration) {
        builder.configuration(configuration);
        return this;
    }
}
