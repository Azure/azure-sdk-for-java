// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.azure.messaging.eventgrid;


import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.experimental.serializer.JsonSerializerProviders;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.Ignore;
import org.junit.Test;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EventGridPublisherClientTests {

    @Test
    @Ignore
    public void testPublishEventGridEvents() {
        // using @Ignore because it requires the correct environment variables
        String endpoint = System.getenv("EG_ENDPOINT");
        String key = System.getenv("EG_KEY");
        EventGridPublisherAsyncClient egClient = new EventGridPublisherClientBuilder()
            .keyCredential(new AzureKeyCredential(key))
            .endpoint(endpoint)
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
        System.out.println("Got response " + response.getStatusCode());
        assertEquals(response.getStatusCode(), 200);

    }

    @Test
    @Ignore
    public void testSasToken() {
        // using @Ignore because it requires the correct environment variables
        String endpoint = System.getenv("EG_ENDPOINT");
        String key = System.getenv("EG_KEY");
        EventGridSasTokenCredential sasToken = new EventGridSasTokenCredential(
            EventGridSasTokenCredential.createSharedAccessSignature(
                endpoint, OffsetDateTime.now().plusMinutes(20), new AzureKeyCredential(key)));
        System.out.println(sasToken.getAccessToken());

        EventGridPublisherAsyncClient egClient = new EventGridPublisherClientBuilder()
            .sharedAccessToken(sasToken)
            .endpoint(endpoint)
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
        System.out.println("Got response " + response.getStatusCode());
        assertEquals(response.getStatusCode(), 200);
    }

    @Test
    @Ignore
    public void testPublishCloudEvents() {
        // using @Ignore because it requires the correct environment variables
        String endpoint = System.getenv("EG_CLOUD_ENDPOINT");
        String key = System.getenv("EG_CLOUD_KEY");
        EventGridPublisherAsyncClient egClient = new EventGridPublisherClientBuilder()
            .keyCredential(new AzureKeyCredential(key))
            .endpoint(endpoint)
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
        System.out.println("Got response " + response.getStatusCode());
        assertEquals(response.getStatusCode(), 200);
    }

    public static class TestData {
        @JsonProperty
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
    @Ignore
    public void testPublishCloudEventsCustomSerializer() {
        String endpoint = System.getenv("EG_CLOUD_ENDPOINT");
        String key = System.getenv("EG_CLOUD_KEY");
        EventGridPublisherAsyncClient egClient = new EventGridPublisherClientBuilder()
            .keyCredential(new AzureKeyCredential(key))
            .endpoint(endpoint)
            .buildAsyncClient();

        List<CloudEvent> events = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            events.add(new CloudEvent("/microsoft/testEvent", "Microsoft.MockPublisher.TestEvent")
                .setSubject("Test " + i)
                .setData(new TestData().setName("Hello " + i),
                    JsonSerializerProviders.createInstance(), null));
        }

        Response<Void> response = egClient.sendCloudEventsWithResponse(events).block();

        assertNotNull(response);
        System.out.println("Got response " + response.getStatusCode());
        assertEquals(response.getStatusCode(), 200);
    }


    @Test
    @Ignore
    public void testPublishCustomEvents() {
        // using @Ignore because it requires the correct environment variables
        String endpoint = System.getenv("EG_CUSTOM_ENDPOINT");
        String key = System.getenv("EG_CUSTOM_KEY");
        EventGridPublisherAsyncClient egClient = new EventGridPublisherClientBuilder()
            .keyCredential(new AzureKeyCredential(key))
            .endpoint(endpoint)
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
        System.out.println("Got response " + response.getStatusCode());
        assertEquals(response.getStatusCode(), 200);
    }

    @Test
    @Ignore
    public void testPublishEventGridEventsSync() {
        // using @Ignore because it requires the correct environment variables
        String endpoint = System.getenv("EG_ENDPOINT");
        String key = System.getenv("EG_KEY");
        EventGridPublisherClient egClient = new EventGridPublisherClientBuilder()
            .keyCredential(new AzureKeyCredential(key))
            .endpoint(endpoint)
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
        System.out.println("Got response " + response.getStatusCode());
        assertEquals(response.getStatusCode(), 200);


    }

    @Test
    @Ignore
    public void testPublishCloudEventsSync() {
        // using @Ignore because it requires the correct environment variables
        String endpoint = System.getenv("EG_CLOUD_ENDPOINT");
        String key = System.getenv("EG_CLOUD_KEY");
        EventGridPublisherClient egClient = new EventGridPublisherClientBuilder()
            .keyCredential(new AzureKeyCredential(key))
            .endpoint(endpoint)
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
        System.out.println("Got response " + response.getStatusCode());
        assertEquals(response.getStatusCode(), 200);
    }

    @Test
    @Ignore
    public void testPublishCustomEventsSync() {
        // using @Ignore because it requires the correct environment variables
        String endpoint = System.getenv("EG_CUSTOM_ENDPOINT");
        String key = System.getenv("EG_CUSTOM_KEY");
        EventGridPublisherClient egClient = new EventGridPublisherClientBuilder()
            .keyCredential(new AzureKeyCredential(key))
            .endpoint(endpoint)
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
        System.out.println("Got response " + response.getStatusCode());
        assertEquals(response.getStatusCode(), 200);
    }
}
