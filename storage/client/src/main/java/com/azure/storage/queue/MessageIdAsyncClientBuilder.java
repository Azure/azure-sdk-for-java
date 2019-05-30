package com.azure.storage.queue;

import com.azure.core.configuration.Configuration;
import com.azure.core.credentials.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpPipelinePolicy;

import java.net.MalformedURLException;

public final class MessageIdAsyncClientBuilder {
    private final MessageIdClientBuilderBase builder;

    MessageIdAsyncClientBuilder() {
        this.builder = new MessageIdClientBuilderBase();
    }

    public MessageIdAsyncClient build() {
        return new MessageIdAsyncClient(builder.build());
    }

    public MessageIdAsyncClientBuilder endpoint(String endpoint) throws MalformedURLException {
        builder.endpoint(endpoint);
        return this;
    }

    public MessageIdAsyncClientBuilder credentials(TokenCredential credentials) {
        builder.credentials(credentials);
        return this;
    }

    public MessageIdAsyncClientBuilder connectionString(String connectionString) {
        // Do stuff with the connection string.
        return this;
    }

    public MessageIdAsyncClientBuilder httpClient(HttpClient httpClient) {
        builder.httpClient(httpClient);
        return this;
    }

    public MessageIdAsyncClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        builder.addPolicy(pipelinePolicy);
        return this;
    }

    public MessageIdAsyncClientBuilder httpLogDetailLevel(HttpLogDetailLevel logLevel) {
        builder.httpLogDetailLevel(logLevel);
        return this;
    }

    public MessageIdAsyncClientBuilder configuration(Configuration configuration) {
        builder.configuration(configuration);
        return this;
    }
}
