// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid;


import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.models.CloudEventDataFormat;
import com.azure.core.serializer.json.jackson.JacksonJsonSerializerBuilder;
import com.azure.core.test.TestBase;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.models.CloudEvent;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
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
import java.util.Map;
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

    private static final String DUMMY_ENDPOINT = "https://www.dummyEndpoint.com/api/events";

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
        EventGridPublisherAsyncClient<EventGridEvent> egClient = builder
            .endpoint(getEndpoint(EVENTGRID_ENDPOINT))
            .credential(getKey(EVENTGRID_KEY))
            .buildEventGridEventPublisherAsyncClient();

        List<EventGridEvent> events = new ArrayList<>();
        events.add(new EventGridEvent("Test", "Microsoft.MockPublisher.TestEvent",
            BinaryData.fromObject(new HashMap<String, String>() {
                {
                    put("Field1", "Value1");
                    put("Field2", "Value2");
                    put("Field3", "Value3");
                }
            }),
            "1.0")
            .setEventTime(OffsetDateTime.now()));

        StepVerifier.create(egClient.sendEventsWithResponse(events))
            .expectNextMatches(voidResponse -> voidResponse.getStatusCode() == 200)
            .verifyComplete();

        StepVerifier.create(egClient.sendEvents(events))
            .verifyComplete();
    }

    @Test
    public void publishEventGridEvent() {
        EventGridPublisherAsyncClient<EventGridEvent> egClient = builder
            .endpoint(getEndpoint(EVENTGRID_ENDPOINT))
            .credential(getKey(EVENTGRID_KEY))
            .buildEventGridEventPublisherAsyncClient();

        EventGridEvent event = new EventGridEvent("Test", "Microsoft.MockPublisher.TestEvent",
            BinaryData.fromObject(new HashMap<String, String>() {
                {
                    put("Field1", "Value1");
                    put("Field2", "Value2");
                    put("Field3", "Value3");
                }
            }),
            "1.0")
            .setEventTime(OffsetDateTime.now());
        StepVerifier.create(egClient.sendEvent(event))
            .verifyComplete();
    }

    @Test
    public void publishWithSasToken() {
        String sasToken = EventGridPublisherAsyncClient.generateSas(
            getEndpoint(EVENTGRID_ENDPOINT),
            getKey(EVENTGRID_KEY),
            OffsetDateTime.now().plusMinutes(20)
        );

        EventGridPublisherAsyncClient<EventGridEvent> egClient = builder
            .credential(new AzureSasCredential(sasToken))
            .endpoint(getEndpoint(EVENTGRID_ENDPOINT))
            .buildEventGridEventPublisherAsyncClient();

        List<EventGridEvent> events = new ArrayList<>();
        events.add(new EventGridEvent("Test", "Microsoft.MockPublisher.TestEvent",
            BinaryData.fromObject(new HashMap<String, String>() {
                {
                    put("Field1", "Value1");
                    put("Field2", "Value2");
                    put("Field3", "Value3");
                }
            }),
            "1.0")
            .setEventTime(OffsetDateTime.now()));

        StepVerifier.create(egClient.sendEventsWithResponse(events, Context.NONE))
            .expectNextMatches(voidResponse -> voidResponse.getStatusCode() == 200)
            .verifyComplete();
    }

    @Test
    @Disabled
    public void publishWithTokenCredential() {
        DefaultAzureCredential defaultCredential = new DefaultAzureCredentialBuilder().build();
        EventGridPublisherAsyncClient<CloudEvent> egClient = builder
            .credential(defaultCredential)
            .endpoint(getEndpoint(CLOUD_ENDPOINT))
            .buildCloudEventPublisherAsyncClient();

        List<CloudEvent> events = new ArrayList<>();
        events.add(new CloudEvent("/microsoft/testEvent", "Microsoft.MockPublisher.TestEvent",
            BinaryData.fromObject(new HashMap<String, String>() {
                {
                    put("Field1", "Value1");
                    put("Field2", "Value2");
                    put("Field3", "Value3");
                }
            }), CloudEventDataFormat.JSON, "application/json")
            .setSubject("Test")
            .setTime(OffsetDateTime.now()));

        StepVerifier.create(egClient.sendEventsWithResponse(events, Context.NONE))
            .expectNextMatches(voidResponse -> voidResponse.getStatusCode() == 200)
            .verifyComplete();
    }

    @Test
    public void publishCloudEvents() {
        EventGridPublisherAsyncClient<CloudEvent> egClient = builder
            .endpoint(getEndpoint(CLOUD_ENDPOINT))
            .credential(getKey(CLOUD_KEY))
            .buildCloudEventPublisherAsyncClient();

        List<CloudEvent> events = new ArrayList<>();
        events.add(new CloudEvent("/microsoft/testEvent", "Microsoft.MockPublisher.TestEvent",
            BinaryData.fromObject(new HashMap<String, String>() {
                {
                    put("Field1", "Value1");
                    put("Field2", "Value2");
                    put("Field3", "Value3");
                }
            }), CloudEventDataFormat.JSON, "application/json")
            .setSubject("Test")
            .setTime(OffsetDateTime.now()));

        StepVerifier.create(egClient.sendEventsWithResponse(events, Context.NONE))
            .expectNextMatches(voidResponse -> voidResponse.getStatusCode() == 200)
            .verifyComplete();
    }

    @Test
    public void publishCloudEvent() {
        EventGridPublisherAsyncClient<CloudEvent> egClient = builder
            .endpoint(getEndpoint(CLOUD_ENDPOINT))
            .credential(getKey(CLOUD_KEY))
            .buildCloudEventPublisherAsyncClient();

        CloudEvent event = new CloudEvent("/microsoft/testEvent", "Microsoft.MockPublisher.TestEvent",
            BinaryData.fromObject(new HashMap<String, String>() {
                {
                    put("Field1", "Value1");
                    put("Field2", "Value2");
                    put("Field3", "Value3");
                }
            }), CloudEventDataFormat.JSON, "application/json")
            .setSubject("Test")
            .setTime(OffsetDateTime.now());

        StepVerifier.create(egClient.sendEvent(event))
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

        EventGridPublisherAsyncClient<CloudEvent> egClient = builder
            .credential(getKey(CLOUD_KEY))
            .endpoint(getEndpoint(CLOUD_ENDPOINT))
            .buildCloudEventPublisherAsyncClient();

        List<CloudEvent> events = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            events.add(new CloudEvent("/microsoft/testEvent", "Microsoft.MockPublisher.TestEvent",
                BinaryData.fromObject(new TestData().setName("Hello " + i)), CloudEventDataFormat.JSON, null)
                .setSubject("Test " + i)
            );
        }

        StepVerifier.create(egClient.sendEventsWithResponse(events, Context.NONE))
            .expectNextMatches(voidResponse -> voidResponse.getStatusCode() == 200)
            .verifyComplete();
    }


    @Test
    public void publishCustomEvents() {
        EventGridPublisherAsyncClient<BinaryData> egClient = builder
            .credential(getKey(CUSTOM_KEY))
            .endpoint(getEndpoint(CUSTOM_ENDPOINT))
            .buildCustomEventPublisherAsyncClient();

        List<BinaryData> events = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            events.add(BinaryData.fromObject(new HashMap<String, String>() {
                {
                    put("id", UUID.randomUUID().toString());
                    put("time", OffsetDateTime.now().toString());
                    put("subject", "Test");
                    put("foo", "bar");
                    put("type", "Microsoft.MockPublisher.TestEvent");
                }
            }));
        }
        StepVerifier.create(egClient.sendEventsWithResponse(events))
            .expectNextMatches(voidResponse -> voidResponse.getStatusCode() == 200)
            .verifyComplete();
    }

    @Test
    public void publishCustomEventsWithSerializer() {
        EventGridPublisherAsyncClient<BinaryData> egClient = builder
            .credential(getKey(CUSTOM_KEY))
            .endpoint(getEndpoint(CUSTOM_ENDPOINT))
            .buildCustomEventPublisherAsyncClient();

        List<BinaryData> events = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            events.add(BinaryData.fromObject(new HashMap<String, String>() {
                {
                    put("id", UUID.randomUUID().toString());
                    put("time", OffsetDateTime.now().toString());
                    put("subject", "Test");
                    put("foo", "bar");
                    put("type", "Microsoft.MockPublisher.TestEvent");
                }
            }, new JacksonJsonSerializerBuilder().build()));
        }
        StepVerifier.create(egClient.sendEventsWithResponse(events, Context.NONE))
            .expectNextMatches(voidResponse -> voidResponse.getStatusCode() == 200)
            .verifyComplete();
    }

    @Test
    public void publishCustomEvent() {
        EventGridPublisherAsyncClient<BinaryData> egClient = builder
            .credential(getKey(CUSTOM_KEY))
            .endpoint(getEndpoint(CUSTOM_ENDPOINT))
            .buildCustomEventPublisherAsyncClient();

        BinaryData event = BinaryData.fromObject(new HashMap<String, String>() {
            {
                put("id", UUID.randomUUID().toString());
                put("time", OffsetDateTime.now().toString());
                put("subject", "Test");
                put("foo", "bar");
                put("type", "Microsoft.MockPublisher.TestEvent");
            }
        });
        StepVerifier.create(egClient.sendEvent(event)).verifyComplete();
    }

    @Test
    public void publishEventGridEventsSync() {
        EventGridPublisherClient<EventGridEvent> egClient = builder
            .credential(getKey(EVENTGRID_KEY))
            .endpoint(getEndpoint(EVENTGRID_ENDPOINT))
            .buildEventGridEventPublisherClient();

        List<EventGridEvent> events = new ArrayList<>();
        events.add(new EventGridEvent("Test", "Microsoft.MockPublisher.TestEvent",
            BinaryData.fromObject(new HashMap<String, String>() {
                {
                    put("Field1", "Value1");
                    put("Field2", "Value2");
                    put("Field3", "Value3");
                }
            }),
            "1.0")
            .setEventTime(OffsetDateTime.now()));

        Response<Void> response = egClient.sendEventsWithResponse(events, Context.NONE);

        assertNotNull(response);
        assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void publishEventGridEventSync() {
        EventGridPublisherClient<EventGridEvent> egClient = builder
            .credential(getKey(EVENTGRID_KEY))
            .endpoint(getEndpoint(EVENTGRID_ENDPOINT))
            .buildEventGridEventPublisherClient();

        EventGridEvent event = new EventGridEvent("Test", "Microsoft.MockPublisher.TestEvent",
            BinaryData.fromObject(new HashMap<String, String>() {
                {
                    put("Field1", "Value1");
                    put("Field2", "Value2");
                    put("Field3", "Value3");
                }
            }),
            "1.0")
            .setEventTime(OffsetDateTime.now());

        egClient.sendEvent(event);
    }

    @Test
    public void publishCloudEventsSync() {
        EventGridPublisherClient<CloudEvent> egClient = builder
            .credential(getKey(CLOUD_KEY))
            .endpoint(getEndpoint(CLOUD_ENDPOINT))
            .buildCloudEventPublisherClient();

        List<CloudEvent> events = new ArrayList<>();
        events.add(new CloudEvent("/microsoft/testEvent", "Microsoft.MockPublisher.TestEvent",
            BinaryData.fromObject(new HashMap<String, String>() {
                {
                    put("Field1", "Value1");
                    put("Field2", "Value2");
                    put("Field3", "Value3");
                }
            }), CloudEventDataFormat.JSON, "application/json")
            .setId(UUID.randomUUID().toString())
            .setSubject("Test")
            .setTime(OffsetDateTime.now()));

        Response<Void> response = egClient.sendEventsWithResponse(events, Context.NONE);

        assertNotNull(response);
        assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void publishCloudEventSync() {
        EventGridPublisherClient<CloudEvent> egClient = builder
            .credential(getKey(CLOUD_KEY))
            .endpoint(getEndpoint(CLOUD_ENDPOINT))
            .buildCloudEventPublisherClient();

        CloudEvent event = new CloudEvent("/microsoft/testEvent", "Microsoft.MockPublisher.TestEvent",
            BinaryData.fromObject(new HashMap<String, String>() {
                {
                    put("Field1", "Value1");
                    put("Field2", "Value2");
                    put("Field3", "Value3");
                }
            }), CloudEventDataFormat.JSON, "application/json")
            .setId(UUID.randomUUID().toString())
            .setSubject("Test")
            .setTime(OffsetDateTime.now());
        egClient.sendEvent(event);
    }

    @Test
    public void publishCustomEventsSync() {
        EventGridPublisherClient<BinaryData> egClient = builder
            .credential(getKey(CUSTOM_KEY))
            .endpoint(getEndpoint(CUSTOM_ENDPOINT))
            .buildCustomEventPublisherClient();

        List<BinaryData> events = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            events.add(BinaryData.fromObject(new HashMap<String, String>() {
                {
                    put("id", UUID.randomUUID().toString());
                    put("subject", "Test");
                    put("foo", "bar");
                    put("type", "Microsoft.MockPublisher.TestEvent");
                }
            }));
        }
        Response<Void> response = egClient.sendEventsWithResponse(events, Context.NONE);

        assertNotNull(response);
        assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void publishCustomEventSync() {
        EventGridPublisherClient<BinaryData> egClient = builder
            .credential(getKey(CUSTOM_KEY))
            .endpoint(getEndpoint(CUSTOM_ENDPOINT))
            .buildCustomEventPublisherClient();

        Map<String, String> event = new HashMap<String, String>() {
                {
                    put("id", UUID.randomUUID().toString());
                    put("subject", "Test");
                    put("foo", "bar");
                    put("type", "Microsoft.MockPublisher.TestEvent");
                }
            };
        egClient.sendEvent(BinaryData.fromObject(event));
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
