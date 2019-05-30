// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.configuration.Configuration;
import com.azure.core.credentials.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpPipelinePolicy;

import java.net.MalformedURLException;

public final class QueueClientBuilder {
    private final QueueClientBuilderBase builder;

    QueueClientBuilder() {
        builder = new QueueClientBuilderBase();
    }

    public QueueClient build() {
        return new QueueClient(new QueueRawClient(builder.build()));
    }

    public QueueClientBuilder endpoint(String endpoint) throws MalformedURLException {
        builder.endpoint(endpoint);
        return this;
    }

    public QueueClientBuilder credentials(TokenCredential credentials) {
        builder.credentials(credentials);
        return this;
    }

    public QueueClientBuilder connectionString(String connectionString) {
        builder.connectionString(connectionString);
        return this;
    }

    public QueueClientBuilder httpClient(HttpClient httpClient) {
        builder.httpClient(httpClient);
        return this;
    }

    public QueueClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        builder.addPolicy(pipelinePolicy);
        return this;
    }

    public QueueClientBuilder httpLogDetailLevel(HttpLogDetailLevel logLevel) {
        builder.httpLogDetailLevel(logLevel);
        return this;
    }

    public QueueClientBuilder configuration(Configuration configuration) {
        builder.configuration(configuration);
        return this;
    }
}
