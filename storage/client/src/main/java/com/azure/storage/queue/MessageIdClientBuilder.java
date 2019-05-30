// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.configuration.Configuration;
import com.azure.core.credentials.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpPipelinePolicy;

import java.net.MalformedURLException;

public final class MessageIdClientBuilder {
    private final MessageIdClientBuilderBase builder;

    MessageIdClientBuilder() {
        this.builder = new MessageIdClientBuilderBase();
    }

    public MessageIdClient build() {
        return new MessageIdClient(new MessageIdRawClient(builder.build()));
    }

    public MessageIdClientBuilder endpoint(String endpoint) throws MalformedURLException {
        builder.endpoint(endpoint);
        return this;
    }

    public MessageIdClientBuilder credentials(TokenCredential credentials) {
        builder.credentials(credentials);
        return this;
    }

    public MessageIdClientBuilder connectionString(String connectionString) {
        // Do stuff with the connection string.
        return this;
    }

    public MessageIdClientBuilder httpClient(HttpClient httpClient) {
        builder.httpClient(httpClient);
        return this;
    }

    public MessageIdClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        builder.addPolicy(pipelinePolicy);
        return this;
    }

    public MessageIdClientBuilder httpLogDetailLevel(HttpLogDetailLevel logLevel) {
        builder.httpLogDetailLevel(logLevel);
        return this;
    }

    public MessageIdClientBuilder configuration(Configuration configuration) {
        builder.configuration(configuration);
        return this;
    }
}
