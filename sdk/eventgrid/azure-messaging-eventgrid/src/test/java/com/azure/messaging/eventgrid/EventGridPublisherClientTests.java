// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid;


import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestBase;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.JacksonAdapter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class EventGridPublisherClientTests extends TestBase {

    private EventGridPublisherClientBuilder builder;

    // Event Grid endpoint for a topic accepting EventGrid schema events
    private static final String EVENTGRID_ENDPOINT = "AZURE_EVENTGRID_EVENT_ENDPOINT";

    // Event Grid endpoint for a topic accepting CloudEvents schema events
    private static final String CLOUD_ENDPOINT = "AZURE_EVENTGRID_CLOUDEVENT_ENDPOINT";

    // Event Grid endpoint for a topic accepting custom schema events
    private static final String CUSTOM_ENDPOINT = "AZURE_EVENTGRID_CUSTOM_ENDPOINT";

    // Event Grid access key for a topic accepting EventGrid schema events
    private static final String EVENTGRID_KEY = "AZURE_EVENTGRID_EVENT_KEY";

    // Event Grid access key for a topic accepting CloudEvents schema events
    private static final String CLOUD_KEY = "AZURE_EVENTGRID_CLOUDEVENT_KEY";

    // Event Grid access key for a topic accepting custom schema events
    private static final String CUSTOM_KEY = "AZURE_EVENTGRID_CUSTOM_KEY";

    private static final String DUMMY_ENDPOINT = "https://www.dummyEndpoint.com";

    private static final String DUMMY_KEY = "dummyKey";

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
    public void publishEventGridEvents() {
        EventGridPublisherAsyncClient egClient = builder
            .endpoint(getEndpoint(EVENTGRID_ENDPOINT))
            .credential(getKey(EVENTGRID_KEY))
            .buildAsyncClient();

        List<EventGridEvent> events = new ArrayList<>();
        events.add(new EventGridEvent("Test", "Microsoft.MockPublisher.TestEvent",
            new HashMap<String, String>() {{
                put("Field1", "Value1");
                put("Field2", "Value2");
                put("Field3", "Value3");
            }},
            "1.0")
            .setEventTime(OffsetDateTime.now()));

        StepVerifier.create(egClient.sendEventGridEventsWithResponse(events))
            .expectNextMatches(voidResponse -> voidResponse.getStatusCode() == 200)
            .verifyComplete();
    }

    @Test
    public void publishWithSasToken() {
        String sasToken = EventGridSasGenerator.generateSas(
            getEndpoint(EVENTGRID_ENDPOINT),
            getKey(EVENTGRID_KEY),
            OffsetDateTime.now().plusMinutes(20)
        );

        EventGridPublisherAsyncClient egClient = builder
            .credential(new AzureSasCredential(sasToken))
            .endpoint(getEndpoint(EVENTGRID_ENDPOINT))
            .buildAsyncClient();

        List<EventGridEvent> events = new ArrayList<>();
        events.add(new EventGridEvent("Test", "Microsoft.MockPublisher.TestEvent",
            new HashMap<String, String>() {{
                put("Field1", "Value1");
                put("Field2", "Value2");
                put("Field3", "Value3");
            }},
            "1.0")
            .setEventTime(OffsetDateTime.now()));

        StepVerifier.create(egClient.sendEventGridEventsWithResponse(events))
            .expectNextMatches(voidResponse -> voidResponse.getStatusCode() == 200)
            .verifyComplete();
    }

    @Test
    public void publishCloudEvents() {
        EventGridPublisherAsyncClient egClient = builder
            .endpoint(getEndpoint(CLOUD_ENDPOINT))
            .credential(getKey(CLOUD_KEY))
            .buildAsyncClient();

        List<CloudEvent> events = new ArrayList<>();
        events.add(new CloudEvent("/microsoft/testEvent", "Microsoft.MockPublisher.TestEvent",
            new HashMap<String, String>() {{
                put("Field1", "Value1");
                put("Field2", "Value2");
                put("Field3", "Value3");
            }})
            .setSubject("Test")
            .setTime(OffsetDateTime.now()));

        StepVerifier.create(egClient.sendCloudEventsWithResponse(events))
            .expectNextMatches(voidResponse -> voidResponse.getStatusCode() == 200)
            .verifyComplete();
    }

    public static class TestData {

        private String name;

        public TestData setName(String name) {
            this.name = name;
            return this;
        }

        public String getName() {
            return this.name;
        }
    }

    @Disabled
    @Test
    public void publishCloudEventsCustomSerializer() {
        // Custom Serializer for testData
        JacksonAdapter customSerializer = new JacksonAdapter();
        customSerializer.serializer().registerModule(new SimpleModule().addSerializer(TestData.class,
            new JsonSerializer<TestData>() {
                @Override
                public void serialize(TestData testData, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
                    throws IOException {
                    jsonGenerator.writeString(testData.getName());
                }
            }));

        EventGridPublisherAsyncClient egClient = builder
            .credential(getKey(CLOUD_KEY))
            .endpoint(getEndpoint(CLOUD_ENDPOINT))
            .buildAsyncClient();

        List<CloudEvent> events = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            events.add(new CloudEvent("/microsoft/testEvent", "Microsoft.MockPublisher.TestEvent",
                new TestData().setName("Hello " + i))
                .setSubject("Test " + i)
            );
        }

        StepVerifier.create(egClient.sendCloudEventsWithResponse(events))
            .expectNextMatches(voidResponse -> voidResponse.getStatusCode() == 200)
            .verifyComplete();
    }


    @Test
    public void publishCustomEvents() {
        EventGridPublisherAsyncClient egClient = builder
            .credential(getKey(CUSTOM_KEY))
            .endpoint(getEndpoint(CUSTOM_ENDPOINT))
            .buildAsyncClient();

        List<Object> events = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            events.add(new HashMap<String, String>() {{
                put("id", UUID.randomUUID().toString());
                put("time", OffsetDateTime.now().toString());
                put("subject", "Test");
                put("foo", "bar");
                put("type", "Microsoft.MockPublisher.TestEvent");
            }});
        }
        StepVerifier.create(egClient.sendCustomEventsWithResponse(events));
    }

    @Test
    public void publishEventGridEventsSync() {
        EventGridPublisherClient egClient = builder
            .credential(getKey(EVENTGRID_KEY))
            .endpoint(getEndpoint(EVENTGRID_ENDPOINT))
            .buildClient();

        List<EventGridEvent> events = new ArrayList<>();
        events.add(new EventGridEvent("Test", "Microsoft.MockPublisher.TestEvent",
            new HashMap<String, String>() {{
                put("Field1", "Value1");
                put("Field2", "Value2");
                put("Field3", "Value3");
            }},
            "1.0")
            .setEventTime(OffsetDateTime.now()));

        Response<Void> response = egClient.sendEventGridEventsWithResponse(events, Context.NONE);

        assertNotNull(response);
        assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void publishCloudEventsSync() {
        EventGridPublisherClient egClient = builder
            .credential(getKey(CLOUD_KEY))
            .endpoint(getEndpoint(CLOUD_ENDPOINT))
            .buildClient();

        List<CloudEvent> events = new ArrayList<>();
        events.add(new CloudEvent("/microsoft/testEvent", "Microsoft.MockPublisher.TestEvent",
            new HashMap<String, String>() {{
                put("Field1", "Value1");
                put("Field2", "Value2");
                put("Field3", "Value3");
            }})
            .setId(UUID.randomUUID().toString())
            .setSubject("Test")
            .setTime(OffsetDateTime.now()));

        Response<Void> response = egClient.sendCloudEventsWithResponse(events, Context.NONE);

        assertNotNull(response);
        assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void publishCustomEventsSync() {
        EventGridPublisherClient egClient = builder
            .credential(getKey(CUSTOM_KEY))
            .endpoint(getEndpoint(CUSTOM_ENDPOINT))
            .buildClient();

        List<Object> events = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            events.add(new HashMap<String, String>() {{
                put("id", UUID.randomUUID().toString());
                put("subject", "Test");
                put("foo", "bar");
                put("type", "Microsoft.MockPublisher.TestEvent");
            }});
        }
        Response<Void> response = egClient.sendCustomEventsWithResponse(events, Context.NONE);

        assertNotNull(response);
        assertEquals(response.getStatusCode(), 200);
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
