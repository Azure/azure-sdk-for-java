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

/**
 * EventGrid cloud native cloud event tests.
 */
public class EventGridCloudNativeEventPublisherTests extends TestBase {
    // Event Grid endpoint for a topic accepting CloudEvents schema events
    private static final String CLOUD_ENDPOINT = "AZURE_EVENTGRID_CLOUDEVENT_ENDPOINT";
    // Event Grid access key for a topic accepting CloudEvents schema events
    private static final String CLOUD_KEY = "AZURE_EVENTGRID_CLOUDEVENT_KEY";
    private static final String DUMMY_ENDPOINT = "https://www.dummyEndpoint.com/api/events";
    private static final String DUMMY_KEY = "dummyKey";
    private static final String EVENT_GRID_DOMAIN_RESOURCE_NAME = "domaintopictest";

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
    public void publishEventGridEventsToTopic() {
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
    public void publishEventGridEventsToDomain() {
        // When publishing to an Event Grid domain with cloud events, the cloud event source is used as the domain topic.
        // The Event Grid service doesn't support using an absolute URI for a domain topic, so you would need to do
        // something like the following to integrate with the cloud native cloud events:
        builder.endpoint(getEndpoint("AZURE_EVENTGRID_CLOUDEVENT_DOMAIN_ENDPOINT"))
            // Event Grid Domain endpoint with CloudEvent Schema
            .credential(getKey("AZURE_EVENTGRID_CLOUDEVENT_DOMAIN_KEY"));

        EventGridPublisherAsyncClient<com.azure.core.models.CloudEvent> egClientAsync =
            builder.buildCloudEventPublisherAsyncClient();
        EventGridPublisherClient<com.azure.core.models.CloudEvent> egClient =
            builder.buildCloudEventPublisherClient();

        CloudEvent cloudEvent =
            CloudEventBuilder.v1()
                .withData("{\"name\": \"joe\"}".getBytes(StandardCharsets.UTF_8)) // Replace it
                .withId(UUID.randomUUID().toString()) // Replace it
                .withType("User.Created.Text") // Replace it
                // Replace it. Event Grid does not allow absolute URIs as the domain topic.
                // For example, use the Event Grid Domain resource name as the relative path.
                .withSource(URI.create(EVENT_GRID_DOMAIN_RESOURCE_NAME))
                .withDataContentType("application/json") // Replace it
                .build();

        // Prepare multiple native cloud events input
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
