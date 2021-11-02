// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid.cloudnative.cloudevents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.TestBase;
import com.azure.core.util.BinaryData;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.EventGridPublisherAsyncClient;
import com.azure.messaging.eventgrid.EventGridPublisherClientBuilder;
import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventData;
import io.cloudevents.SpecVersion;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
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
    }

    @Override
    protected void afterTest() {
        StepVerifier.resetDefaultTimeout();
    }


    @Test
    public void publishEventGridEvent() throws URISyntaxException {
        EventGridPublisherAsyncClient<com.azure.core.models.CloudEvent> egClient = builder
                                                                     .endpoint(getEndpoint(CLOUD_ENDPOINT))
                                                                     .credential(getKey(CLOUD_KEY))
                                                                     .buildCloudEventPublisherAsyncClient();

        final CloudEvent cloudEvent = getCloudEvent("1.0", UUID.randomUUID().toString(),  "User.Created.Text",
            new URI("http://localHost"), null, null, null, OffsetDateTime.now(),
            () -> "name".getBytes(StandardCharsets.UTF_8));

        StepVerifier.create(EventGridCloudNativeEventPublisher.sendEventAsync(egClient, cloudEvent))
            .verifyComplete();


        final List<CloudEvent> cloudEvents = new ArrayList<>();
        cloudEvents.add(cloudEvent);
        StepVerifier.create(EventGridCloudNativeEventPublisher.sendEventsAsync(egClient, cloudEvents))
            .verifyComplete();
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

    CloudEvent getCloudEvent(String specVersion, String id, String type, URI source, String dataContentType, URI dataSchema,
        String subject, OffsetDateTime time, CloudEventData data) {

        return new CloudEvent() {
            // Not supported
            @Override
            public Object getExtension(String s) {
                return null;
            }
            // Not supported
            @Override
            public Set<String> getExtensionNames() {
                return null;
            }

            @Override
            public SpecVersion getSpecVersion() {
                return SpecVersion.valueOf(specVersion);
            }

            @Override
            public String getId() {
                return id;
            }

            @Override
            public String getType() {
                return type;
            }

            @Override
            public URI getSource() {
                return source;
            }

            @Override
            public String getDataContentType() {
                return dataContentType;
            }

            @Override
            public URI getDataSchema() {
                return dataSchema;
            }

            @Override
            public String getSubject() {
                return subject;
            }

            @Override
            public OffsetDateTime getTime() {
                return time;
            }

            // Not supported
            @Override
            public Object getAttribute(String s) throws IllegalArgumentException {
                return null;
            }

            @Override
            public CloudEventData getData() {
                return data;
            }
        };

    }

}
