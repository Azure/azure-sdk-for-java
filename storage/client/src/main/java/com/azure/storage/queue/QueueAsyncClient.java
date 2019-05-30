package com.azure.storage.queue;

import com.azure.core.ServiceClient;
import com.azure.core.http.HttpPipeline;
import com.azure.storage.queue.implementation.AzureQueueStorageImpl;

import java.net.URL;

public class QueueAsyncClient extends ServiceClient {
    private final String endpoint;
    private final AzureQueueStorageImpl generateClient;
    private final String apiVersion;

    private QueueAsyncClient(URL endpoint, HttpPipeline httpPipeline) {
        super(httpPipeline);
        this.endpoint = endpoint.toString();
        this.generateClient = new AzureQueueStorageImpl(httpPipeline).withUrl(this.endpoint);
        this.apiVersion = this.generateClient.version();
    }

    /**
     * Creates a builder that can configure options for the SecretAsyncClient before creating an instance of it.
     * @return A new builder to create a SecretAsyncClient from.
     */
    public static QueueAsyncClientBuilder builder() {
        return new QueueAsyncClientBuilder();
    }
}
