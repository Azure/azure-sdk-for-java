// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid.cloudnative.cloudevents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.TestBase;
import com.azure.messaging.eventgrid.EventGridPublisherAsyncClient;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.azure.messaging.eventgrid.EventGridPublisherClientBuilder;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EventGridCloudNativeEventPublisherTests extends TestBase {
    // Event Grid endpoint for a topic accepting CloudEvents schema events
    private static final String CLOUD_ENDPOINT = "AZURE_EVENTGRID_CLOUDEVENT_ENDPOINT";
    // Event Grid access key for a topic accepting CloudEvents schema events
    private static final String CLOUD_KEY = "AZURE_EVENTGRID_CLOUDEVENT_KEY";
    private static final String DUMMY_ENDPOINT = "https://www.dummyEndpoint.com/api/events";
    private static final String DUMMY_KEY = "dummyKey";

    private EventGridPublisherClientBuilder builder;

    @Override
    protected void beforeTest() {

        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));

        builder = new EventGridPublisherClientBuilder();

        if (interceptorManager.isPlaybackMode()) {
            builder.httpClient(interceptorManager.getPlaybackClient());
        } else {
            builder.addPolicy(interceptorManager.getRecordPolicy())
                .retryPolicy(new RetryPolicy());
        }

        builder.endpoint(getEndpoint(CLOUD_ENDPOINT)).credential(getKey(CLOUD_KEY));
    }

    @Override
    protected void afterTest() {
        StepVerifier.resetDefaultTimeout();
    }

    @Test
    public void publishEventGridEvents() {
        EventGridPublisherAsyncClient<com.azure.core.models.CloudEvent> egClientAsync =
            builder.buildCloudEventPublisherAsyncClient();
        EventGridPublisherClient<com.azure.core.models.CloudEvent> egClient =
            builder.buildCloudEventPublisherClient();
        // Single Event
        CloudEvent cloudEvent = CloudEventBuilder.v1()
                                    .withData("{\"name\": \"joe\"}".getBytes(StandardCharsets.UTF_8))
                                    .withId(UUID.randomUUID().toString())
                                    .withType("User.Created.Text")
                                    .withSource(URI.create("http://localHost"))
                                    .withDataContentType("application/json")
                                    .build();
        // Multiple Events
        final List<CloudEvent> cloudEvents = new ArrayList<>();
        cloudEvents.add(cloudEvent);

        // Async publishing
        StepVerifier.create(EventGridCloudNativeEventPublisher.sendEventAsync(egClientAsync, cloudEvent))
            .verifyComplete();
        StepVerifier.create(EventGridCloudNativeEventPublisher.sendEventsAsync(egClientAsync, cloudEvents))
            .verifyComplete();

        // Sync publishing
        EventGridCloudNativeEventPublisher.sendEvent(egClient, cloudEvent);
        EventGridCloudNativeEventPublisher.sendEvents(egClient, cloudEvents);
    }

    @Test
    public void publishEventGridEventsWithoutContentType() {
        EventGridPublisherAsyncClient<com.azure.core.models.CloudEvent> egClientAsync =
            builder.buildCloudEventPublisherAsyncClient();
        EventGridPublisherClient<com.azure.core.models.CloudEvent> egClient =
            builder.buildCloudEventPublisherClient();
        // Single Event
        CloudEvent cloudEvent = CloudEventBuilder.v1()
                               .withData("{\"name\": \"joe\"}".getBytes(StandardCharsets.UTF_8))
                               .withId(UUID.randomUUID().toString())
                               .withType("User.Created.Text")
                               .withSource(URI.create("http://localHost"))
                               .build();
        // Multiple Events
        final List<CloudEvent> cloudEvents = new ArrayList<>();
        cloudEvents.add(cloudEvent);

        // Async publishing
        StepVerifier.create(EventGridCloudNativeEventPublisher.sendEventAsync(egClientAsync, cloudEvent))
            .verifyComplete();
        StepVerifier.create(EventGridCloudNativeEventPublisher.sendEventsAsync(egClientAsync, cloudEvents))
            .verifyComplete();

        // Sync publishing
        EventGridCloudNativeEventPublisher.sendEvent(egClient, cloudEvent);
        EventGridCloudNativeEventPublisher.sendEvents(egClient, cloudEvents);
    }

    private String getEndpoint(String liveEnvName) {
        if (interceptorManager.isPlaybackMode()) {
            return DUMMY_ENDPOINT;
        }
        String endpoint = System.getenv(liveEnvName);
        assertNotNull(endpoint, "System environment variable " + liveEnvName + "is null");
        return endpoint;
    }

    private AzureKeyCredential getKey(String liveEnvName) {
        if (interceptorManager.isPlaybackMode()) {
            return new AzureKeyCredential(DUMMY_KEY);
        }
        AzureKeyCredential key = new AzureKeyCredential(System.getenv(liveEnvName));
        assertNotNull(key.getKey(), "System environment variable " + liveEnvName + "is null");
        return key;
    }
}
