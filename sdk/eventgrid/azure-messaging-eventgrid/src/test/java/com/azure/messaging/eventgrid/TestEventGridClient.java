// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.azure.messaging.eventgrid;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.messaging.eventgrid.implementation.EventGridPublisherClientImpl;
import com.azure.messaging.eventgrid.implementation.EventGridPublisherClientImplBuilder;
import com.azure.messaging.eventgrid.models.CloudEvent;
import com.azure.messaging.eventgrid.models.EventGridEvent;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestEventGridClient {

    @Test
    @Ignore
    public void testPublishEventGridEvents() throws InterruptedException {
        // using @Ignore because it requires the correct environment variables
        String endpoint = System.getenv("EG_ENDPOINT");
        String key = System.getenv("EG_KEY");
        EventGridPublisherClientImpl egClient = new EventGridPublisherClientImplBuilder()
            .pipeline(new HttpPipelineBuilder().policies(
                new AddHeadersPolicy(new HttpHeaders().put("aeg-sas-key", key)))
                .build())
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
        egClient.publishEventsWithResponseAsync(endpoint, events).
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
        EventGridPublisherClientImpl egClient = new EventGridPublisherClientImplBuilder()
            .pipeline(new HttpPipelineBuilder().policies(
                new AddHeadersPolicy(new HttpHeaders().put("aeg-sas-key", key)))
                .build())
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
        egClient.publishCloudEventEventsWithResponseAsync(endpoint, events).
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
        EventGridPublisherClientImpl egClient = new EventGridPublisherClientImplBuilder()
            .pipeline(new HttpPipelineBuilder().policies(
                new AddHeadersPolicy(new HttpHeaders().put("aeg-sas-key", key)))
                .build())
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
        egClient.publishCustomEventEventsWithResponseAsync(endpoint, events).
            subscribe(response -> {
                assertNotNull(response);
                System.out.println("Got response " + response.getStatusCode());
                assertEquals(response.getStatusCode(), 200);
            }, Throwable::printStackTrace);

        TimeUnit.SECONDS.sleep(3);
    }

    @Test
    public void testDeserializeEventGridEvents() throws Exception {
        String storageEventJson = "{\"topic\": \"/subscriptions/subscriptionID/resourceGroups/Storage/providers/Microsoft.Storage/storageAccounts/xstoretestaccount\",\"subject\": \"/blobServices/default/containers/testcontainer/blobs/testfile.txt\",   \"eventType\": \"Microsoft.Storage.BlobCreated\",  \"eventTime\": \"2017-06-26T18:41:00.9584103Z\",  \"id\": \"831e1650-001e-001b-66ab-eeb76e069631\",  \"data\": {    \"api\": \"PutBlockList\",    \"clientRequestId\": \"6d79dbfb-0e37-4fc4-981f-442c9ca65760\",    \"requestId\": \"831e1650-001e-001b-66ab-eeb76e000000\",    \"eTag\": \"0x8D4BCC2E4835CD0\",    \"contentType\": \"text/plain\",    \"contentLength\": 524288,    \"blobType\": \"BlockBlob\",    \"url\": \"https://example.blob.core.windows.net/testcontainer/testfile.txt\",    \"sequencer\": \"00000000000004420000000000028963\",    \"storageDiagnostics\": {      \"batchId\": \"b68529f3-68cd-4744-baa4-3c0498ec19f0\" }},  \"dataVersion\": \"\",  \"metadataVersion\": \"1\"}";

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule()
            .addDeserializer(OffsetDateTime.class, new JsonDeserializer<OffsetDateTime>() {
                @Override
                public OffsetDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
                    return OffsetDateTime.parse(jsonParser.getValueAsString());
                }
            }));

        EventGridEvent eventGridEvent = mapper.readValue(storageEventJson, EventGridEvent.class);

        assertNotNull(eventGridEvent);
        assertEquals("Event types do not match", eventGridEvent.getEventType(), "Microsoft.Storage.BlobCreated");
    }
}
