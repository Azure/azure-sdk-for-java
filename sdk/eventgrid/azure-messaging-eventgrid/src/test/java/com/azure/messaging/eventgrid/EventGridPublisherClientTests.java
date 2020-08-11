// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.azure.messaging.eventgrid;


import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestBase;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.JacksonAdapter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class EventGridPublisherClientTests extends TestBase {

    private EventGridPublisherClientBuilder builder;

    private static final String EVENTGRID_ENDPOINT = "EG_ENDPOINT";

    private static final String CLOUD_ENDPOINT = "EG_CLOUD_ENDPOINT";

    private static final String CUSTOM_ENDPOINT = "EG_CUSTOM_ENDPOINT";

    private static final String EVENTGRID_KEY = "EG_KEY";

    private static final String CLOUD_KEY = "EG_CLOUD_KEY";

    private static final String CUSTOM_KEY = "EG_CUSTOM_KEY";

    private static final String DUMMY_ENDPOINT = "dummyEndpoint";

    private static final String DUMMY_KEY = "dummyKey";

    @Override
    protected void beforeTest() {

        builder = new EventGridPublisherClientBuilder();

        if (interceptorManager.isPlaybackMode()) {
            builder.httpClient(interceptorManager.getPlaybackClient());
        } else {
            builder.addPolicy(interceptorManager.getRecordPolicy())
                .retryPolicy(new RetryPolicy());
        }
    }

    @Test
    public void testPublishEventGridEvents() {
        EventGridPublisherAsyncClient egClient = builder
            .endpoint(getEndpoint(EVENTGRID_ENDPOINT))
            .keyCredential(getKey(EVENTGRID_KEY))
            .buildAsyncClient();

        List<EventGridEvent> events = new ArrayList<>();
        events.add(new EventGridEvent("Test", "Microsoft.MockPublisher.TestEvent", "1.0")
            .setData(new HashMap<String, String>() {{
                put("Field1", "Value1");
                put("Field2", "Value2");
                put("Field3", "Value3");
            }})
            .setEventTime(OffsetDateTime.now()));

        Response<Void> response = egClient.sendEventsWithResponse(events).block();

        assertNotNull(response);
        assertEquals(response.getStatusCode(), 200);

    }

    @Test
    public void testSasToken() {
        String sasToken = EventGridSharedAccessSignatureCredential.createSharedAccessSignature(
            getEndpoint(EVENTGRID_ENDPOINT),
            OffsetDateTime.now().plusMinutes(20),
            getKey(EVENTGRID_KEY)
        );

        EventGridPublisherAsyncClient egClient = builder
            .sharedAccessSignatureCredential(new EventGridSharedAccessSignatureCredential(sasToken))
            .endpoint(getEndpoint(EVENTGRID_ENDPOINT))
            .buildAsyncClient();

        List<EventGridEvent> events = new ArrayList<>();
        events.add(new EventGridEvent("Test", "Microsoft.MockPublisher.TestEvent", "1.0")
            .setData(new HashMap<String, String>() {{
                put("Field1", "Value1");
                put("Field2", "Value2");
                put("Field3", "Value3");
            }})
            .setEventTime(OffsetDateTime.now()));

        Response<Void> response = egClient.sendEventsWithResponse(events).block();

        assertNotNull(response);
        assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void testPublishCloudEvents() {
        EventGridPublisherAsyncClient egClient = builder
            .endpoint(getEndpoint(CLOUD_ENDPOINT))
            .keyCredential(getKey(CLOUD_KEY))
            .buildAsyncClient();

        List<CloudEvent> events = new ArrayList<>();
        events.add(new CloudEvent("/microsoft/testEvent", "Microsoft.MockPublisher.TestEvent")
            .setSubject("Test")
            .setData(new HashMap<String, String>() {{
                put("Field1", "Value1");
                put("Field2", "Value2");
                put("Field3", "Value3");
            }})
            .setTime(OffsetDateTime.now()));

        Response<Void> response = egClient.sendCloudEventsWithResponse(events).block();

        assertNotNull(response);
        assertEquals(response.getStatusCode(), 200);
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

    @Test
    public void testPublishCloudEventsCustomSerializer() {

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
            .keyCredential(getKey(CLOUD_KEY))
            .endpoint(getEndpoint(CLOUD_ENDPOINT))
            .serializer(customSerializer)
            .buildAsyncClient();

        List<CloudEvent> events = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            events.add(new CloudEvent("/microsoft/testEvent", "Microsoft.MockPublisher.TestEvent")
                .setSubject("Test " + i)
                .setData(new TestData().setName("Hello " + i)));
        }

        Response<Void> response = egClient.sendCloudEventsWithResponse(events).block();

        assertNotNull(response);
        assertEquals(response.getStatusCode(), 200);
    }


    @Test
    public void testPublishCustomEvents() {
        EventGridPublisherAsyncClient egClient = builder
            .keyCredential(getKey(CUSTOM_KEY))
            .endpoint(getEndpoint(CUSTOM_ENDPOINT))
            .buildAsyncClient();

        List<Object> events = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            events.add(new HashMap<String, String>() {{
                put("id", UUID.randomUUID().toString());
                put("subject", "Test");
                put("foo", "bar");
                put("type", "Microsoft.MockPublisher.TestEvent");
            }});
        }
        Response<Void> response = egClient.sendCustomEventsWithResponse(events).block();

        assertNotNull(response);
        assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void testPublishEventGridEventsSync() {
        EventGridPublisherClient egClient = builder
            .keyCredential(getKey(EVENTGRID_KEY))
            .endpoint(getEndpoint(EVENTGRID_ENDPOINT))
            .buildClient();

        List<EventGridEvent> events = new ArrayList<>();
        events.add(new EventGridEvent("Test", "Microsoft.MockPublisher.TestEvent", "1.0")
            .setData(new HashMap<String, String>() {{
                put("Field1", "Value1");
                put("Field2", "Value2");
                put("Field3", "Value3");
            }})
            .setEventTime(OffsetDateTime.now()));

        Response<Void> response = egClient.sendEventsWithResponse(events, Context.NONE);

        assertNotNull(response);
        assertEquals(response.getStatusCode(), 200);


    }

    @Test
    public void testPublishCloudEventsSync() {
        EventGridPublisherClient egClient = builder
            .keyCredential(getKey(CLOUD_KEY))
            .endpoint(getEndpoint(CLOUD_ENDPOINT))
            .buildClient();

        List<CloudEvent> events = new ArrayList<>();
        events.add(new CloudEvent("/microsoft/testEvent", "Microsoft.MockPublisher.TestEvent")
            .setId(UUID.randomUUID().toString())
            .setSubject("Test")
            .setData(new HashMap<String, String>() {{
                put("Field1", "Value1");
                put("Field2", "Value2");
                put("Field3", "Value3");
            }})
            .setTime(OffsetDateTime.now()));

        Response<Void> response = egClient.sendCloudEventsWithResponse(events, Context.NONE);

        assertNotNull(response);
        assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void testPublishCustomEventsSync() {
        EventGridPublisherClient egClient = builder
            .keyCredential(getKey(CUSTOM_KEY))
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
        return System.getenv(liveEnvName);
    }

    private AzureKeyCredential getKey(String liveEnvName) {
        if (interceptorManager.isPlaybackMode()) {
            return new AzureKeyCredential(DUMMY_KEY);
        }
        return new AzureKeyCredential(System.getenv(liveEnvName));
    }
}
