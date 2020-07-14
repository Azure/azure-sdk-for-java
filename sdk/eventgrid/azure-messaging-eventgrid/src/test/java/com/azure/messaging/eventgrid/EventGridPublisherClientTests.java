package com.azure.messaging.eventgrid;


import com.azure.core.http.rest.Response;
import com.azure.messaging.eventgrid.models.CloudEvent;
import com.azure.messaging.eventgrid.models.EventGridEvent;
import org.junit.Ignore;
import org.junit.Test;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EventGridPublisherClientTests {

    @Test
    @Ignore
    public void testPublishEventGridEvents() throws InterruptedException {
        // using @Ignore because it requires the correct environment variables
        String endpoint = System.getenv("EG_ENDPOINT");
        String key = System.getenv("EG_KEY");
        EventGridPublisherAsyncClient egClient = new EventGridPublisherClientBuilder()
            .credential(new EventGridSharedKeyCredential("Key 1", key))
            .endpoint(endpoint)
            .buildAsyncClient();

        List<EventGridEvent> events = new ArrayList<>();
        events.add(new EventGridEvent()
            .setId(UUID.randomUUID().toString())
            .setSubject("Test")
            .setEventType("Microsoft.MockPublisher.TestEvent")
            .setData(new HashMap<String, String>() {{
                put("Field1", "Value1");
                put("Field2", "Value2");
                put("Field3", "Value3");
            }})
            .setDataVersion("1.0")
            .setEventTime(OffsetDateTime.now()));
        egClient.publishEventsWithResponse(events).
            subscribe(response -> {
                assertNotNull(response);
                System.out.println("Got response " + response.getStatusCode());
                assertEquals(response.getStatusCode(), 200);
            }, Throwable::printStackTrace);

        TimeUnit.SECONDS.sleep(3);

    }

    @Test
    @Ignore
    public void testPublishCloudEvents() throws InterruptedException {
        // using @Ignore because it requires the correct environment variables
        String endpoint = System.getenv("EG_CLOUD_ENDPOINT");
        String key = System.getenv("EG_CLOUD_KEY");
        EventGridPublisherAsyncClient egClient = new EventGridPublisherClientBuilder()
            .credential(new EventGridSharedKeyCredential("Key 1", key))
            .endpoint(endpoint)
            .buildAsyncClient();

        List<CloudEvent> events = new ArrayList<>();
        events.add(new CloudEvent()
            .setId(UUID.randomUUID().toString())
            .setSubject("Test")
            .setType("Microsoft.MockPublisher.TestEvent")
            .setData(new HashMap<String, String>() {{
                put("Field1", "Value1");
                put("Field2", "Value2");
                put("Field3", "Value3");
            }})
            .setSpecversion("1.0")
            .setTime(OffsetDateTime.now()));
        egClient.publishCloudEventsWithResponse(events).
            subscribe(response -> {
                assertNotNull(response);
                System.out.println("Got response " + response.getStatusCode());
                assertEquals(response.getStatusCode(), 200);
            }, Throwable::printStackTrace);

        TimeUnit.SECONDS.sleep(3);
    }

    @Test
    @Ignore
    public void TestPublishCustomEvents() throws InterruptedException {
        // using @Ignore because it requires the correct environment variables
        String endpoint = System.getenv("EG_CUSTOM_ENDPOINT");
        String key = System.getenv("EG_CUSTOM_KEY");
        EventGridPublisherAsyncClient egClient = new EventGridPublisherClientBuilder()
            .credential(new EventGridSharedKeyCredential("Key 1", key))
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
        egClient.publishCustomEventsWithResponse(events).
            subscribe(response -> {
                assertNotNull(response);
                System.out.println("Got response " + response.getStatusCode());
                assertEquals(response.getStatusCode(), 200);
            }, Throwable::printStackTrace);

        TimeUnit.SECONDS.sleep(3);
    }

    @Test
    @Ignore
    public void testPublishEventGridEventsSync() {
        // using @Ignore because it requires the correct environment variables
        String endpoint = System.getenv("EG_ENDPOINT");
        String key = System.getenv("EG_KEY");
        EventGridPublisherClient egClient = new EventGridPublisherClientBuilder()
            .credential(new EventGridSharedKeyCredential("Key 1", key))
            .endpoint(endpoint)
            .buildClient();

        List<EventGridEvent> events = new ArrayList<>();
        events.add(new EventGridEvent()
            .setId(UUID.randomUUID().toString())
            .setSubject("Test")
            .setEventType("Microsoft.MockPublisher.TestEvent")
            .setData(new HashMap<String, String>() {{
                put("Field1", "Value1");
                put("Field2", "Value2");
                put("Field3", "Value3");
            }})
            .setDataVersion("1.0")
            .setEventTime(OffsetDateTime.now()));
        Response<Void> response = egClient.publishEventsWithResponse(events);
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
            .credential(new EventGridSharedKeyCredential("Key 1", key))
            .endpoint(endpoint)
            .buildClient();

        List<CloudEvent> events = new ArrayList<>();
        events.add(new CloudEvent()
            .setId(UUID.randomUUID().toString())
            .setSubject("Test")
            .setType("Microsoft.MockPublisher.TestEvent")
            .setData(new HashMap<String, String>() {{
                put("Field1", "Value1");
                put("Field2", "Value2");
                put("Field3", "Value3");
            }})
            .setSpecversion("1.0")
            .setTime(OffsetDateTime.now()));

        Response<Void> response = egClient.publishCloudEventsWithResponse(events);
        assertNotNull(response);
        System.out.println("Got response " + response.getStatusCode());
        assertEquals(response.getStatusCode(), 200);
    }

    @Test
    @Ignore
    public void TestPublishCustomEventsSync() {
        // using @Ignore because it requires the correct environment variables
        String endpoint = System.getenv("EG_CUSTOM_ENDPOINT");
        String key = System.getenv("EG_CUSTOM_KEY");
        EventGridPublisherClient egClient = new EventGridPublisherClientBuilder()
            .credential(new EventGridSharedKeyCredential("Key 1", key))
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
        Response<Void> response = egClient.publishCustomEventsWithResponse(events);
        assertNotNull(response);
        System.out.println("Got response " + response.getStatusCode());
        assertEquals(response.getStatusCode(), 200);
    }
}
