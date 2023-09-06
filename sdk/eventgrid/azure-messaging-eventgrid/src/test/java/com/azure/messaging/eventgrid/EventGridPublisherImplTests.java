// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.models.CloudEvent;
import com.azure.messaging.eventgrid.implementation.EventGridPublisherClientImpl;
import com.azure.messaging.eventgrid.implementation.EventGridPublisherClientImplBuilder;
import com.azure.messaging.eventgrid.implementation.models.EventGridEvent;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.net.MalformedURLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class EventGridPublisherImplTests extends EventGridTestBase {

    private HttpPipelineBuilder pipelineBuilder;

    private EventGridPublisherClientImplBuilder clientBuilder;

    @Override
    protected void beforeTest() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));

        pipelineBuilder = new HttpPipelineBuilder();

        clientBuilder = new EventGridPublisherClientImplBuilder();

        if (interceptorManager.isPlaybackMode()) {
            pipelineBuilder.httpClient(interceptorManager.getPlaybackClient());
        } else if (interceptorManager.isRecordMode()) {
            pipelineBuilder.policies(interceptorManager.getRecordPolicy(), new RetryPolicy());
        }

        setupSanitizers();
    }

    @Test
    public void publishEventGridEventsImpl() throws MalformedURLException {
        EventGridPublisherClientImpl egClient = clientBuilder
            .pipeline(pipelineBuilder.policies(
                new AddHeadersPolicy(new HttpHeaders().put("aeg-sas-key", getKey(EVENTGRID_KEY).getKey())))
                .build())
            .buildClient();

        List<EventGridEvent> events = Collections.singletonList(getEventGridEvent().toImpl());

        StepVerifier.create(egClient.publishEventGridEventsWithResponseAsync(getEndpoint(EVENTGRID_ENDPOINT), events))
            .expectNextMatches(voidResponse -> voidResponse.getStatusCode() == 200)
            .verifyComplete();
    }

    @Test
    public void publishCloudEventsImpl() throws MalformedURLException {
        EventGridPublisherClientImpl egClient = clientBuilder
            .pipeline(pipelineBuilder.policies(
                new AddHeadersPolicy(new HttpHeaders().put("aeg-sas-key", getKey(CLOUD_KEY).getKey())))
                .build())
            .buildClient();

        List<CloudEvent> events = Collections.singletonList(getCloudEvent());

        StepVerifier.create(egClient.publishCloudEventEventsWithResponseAsync(getEndpoint(CLOUD_ENDPOINT), events, null))
            .expectNextMatches(voidResponse -> voidResponse.getStatusCode() == 200)
            .verifyComplete();
    }

    @Test
    public void publishCustomEventsImpl() throws MalformedURLException {
        EventGridPublisherClientImpl egClient = clientBuilder
            .pipeline(pipelineBuilder.policies(
                new AddHeadersPolicy(new HttpHeaders().put("aeg-sas-key", getKey(CUSTOM_KEY).getKey())))
                .build())
            .buildClient();

        List<Object> events = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            events.add(getCustomEvent());
        }

        StepVerifier.create(egClient.publishCustomEventEventsWithResponseAsync(getEndpoint(CUSTOM_ENDPOINT), events))
            .expectNextMatches(voidResponse -> voidResponse.getStatusCode() == 200)
            .verifyComplete();
    }
}
