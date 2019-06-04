// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.configuration.Configuration;
import com.azure.core.credentials.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpPipelinePolicy;

public final class QueueServiceAsyncClientBuilder {
    private final QueueServiceClientBuilderBase builder;

    QueueServiceAsyncClientBuilder() {
        builder = new QueueServiceClientBuilderBase();
    }

    public QueueServiceAsyncClient build() {
        return new QueueServiceAsyncClient(builder.build());
    }

    public QueueServiceAsyncClientBuilder endpoint(String endpoint) {
        builder.endpoint(endpoint);
        return this;
    }

    public QueueServiceAsyncClientBuilder credentials(TokenCredential credentials) {
        builder.credentials(credentials);
        return this;
    }

    public QueueServiceAsyncClientBuilder connectionString(String connectionString) {
        builder.connectionString(connectionString);
        return this;
    }

    public QueueServiceAsyncClientBuilder httpClient(HttpClient httpClient) {
        builder.httpClient(httpClient);
        return this;
    }

    public QueueServiceAsyncClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        builder.addPolicy(pipelinePolicy);
        return this;
    }

    public QueueServiceAsyncClientBuilder httpLogDetailLevel(HttpLogDetailLevel logLevel) {
        builder.httpLogDetailLevel(logLevel);
        return this;
    }

    public QueueServiceAsyncClientBuilder configuration(Configuration configuration) {
        builder.configuration(configuration);
        return this;
    }
}
