package com.azure.messaging.eventgrid.implementation;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.CookiePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.messaging.eventgrid.EventGridClient;

/** A builder for creating a new instance of the EventGridClient type. */
@ServiceClientBuilder(serviceClients = {EventGridClientImpl.class})
public final class EventGridClientImplBuilder {
    /*
     * The HTTP pipeline to send requests through
     */
    private HttpPipeline pipeline;

    /**
     * Sets The HTTP pipeline to send requests through.
     *
     * @param pipeline the pipeline value.
     * @return the EventGridClientImplBuilder.
     */
    public EventGridClientImplBuilder pipeline(HttpPipeline pipeline) {
        this.pipeline = pipeline;
        return this;
    }

    /**
     * Builds an instance of EventGridClient with the provided parameters.
     *
     * @return an instance of EventGridClient.
     */
    public EventGridClient buildClient() {
        if (pipeline == null) {
            this.pipeline =
                    new HttpPipelineBuilder()
                            .policies(new UserAgentPolicy(), new RetryPolicy(), new CookiePolicy())
                            .build();
        }
        EventGridClientImpl client = new EventGridClientImpl(pipeline);
        return client;
    }
}
