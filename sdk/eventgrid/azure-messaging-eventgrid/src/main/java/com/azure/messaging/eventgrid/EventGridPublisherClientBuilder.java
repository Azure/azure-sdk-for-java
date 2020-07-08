package com.azure.messaging.eventgrid;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.Configuration;
import com.azure.core.util.ServiceVersion;

@ServiceClientBuilder(serviceClients = {EventGridPublisherClient.class, EventGridPublisherAsyncClient.class})
public class EventGridPublisherClientBuilder {

    public EventGridPublisherClientBuilder addPolicy(HttpPipelinePolicy httpPipelinePolicy) {
        // TODO: implement method
        return null;
    }

    public EventGridPublisherAsyncClient buildAsyncClient() {
        // TODO: implement method
        return null;
    }

    public EventGridPublisherClient buildClient() {
        // TODO: implement method
        return null;
    }

    public EventGridPublisherClientBuilder configuration(Configuration configuration) {
        // TODO: implement method
        return null;
    }

    public EventGridPublisherClientBuilder credential(AzureKeyCredential credential) {
        // TODO: implement method
        return null;
    }

    public EventGridPublisherClientBuilder endpoint(String endpoint) {
        // TODO: implement method
        return null;
    }

    public EventGridPublisherClientBuilder httpClient(HttpClient httpClient) {
        // TODO: implement method
        return null;
    }

    public EventGridPublisherClientBuilder httpLogOptions(HttpLogOptions httpLogOptions) {
        // TODO: implement method
        return null;
    }

    public EventGridPublisherClient pipeline(HttpPipeline httpPipeline) {
        // TODO: implement method
        return null;
    }

    public EventGridPublisherClientBuilder serviceVersion(ServiceVersion serviceVersion) {
        // TODO: implement method
        return null;
    }

}
