// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.configuration.Configuration;
import com.azure.core.credentials.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpPipelinePolicy;

public final class QueueServiceClientBuilder {
    private final QueueServiceAsyncClientBuilder builder;

    QueueServiceClientBuilder() {
        builder = new QueueServiceAsyncClientBuilder();
    }

    public QueueServiceClient build() {
        return new QueueServiceClient(builder.build());
    }

    public QueueServiceClientBuilder endpoint(String endpoint) {
        builder.endpoint(endpoint);
        return this;
    }

    public QueueServiceClientBuilder credentials(TokenCredential credentials) {
        builder.credentials(credentials);
        return this;
    }

    public QueueServiceClientBuilder connectionString(String connectionString) {
        builder.connectionString(connectionString);
        return this;
    }

    public QueueServiceClientBuilder httpClient(HttpClient httpClient) {
        builder.httpClient(httpClient);
        return this;
    }

    public QueueServiceClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        builder.addPolicy(pipelinePolicy);
        return this;
    }

    public QueueServiceClientBuilder httpLogDetailLevel(HttpLogDetailLevel logLevel) {
        builder.httpLogDetailLevel(logLevel);
        return this;
    }

    public QueueServiceClientBuilder configuration(Configuration configuration) {
        builder.configuration(configuration);
        return this;
    }
}
