// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid;


import com.azure.core.credential.AzureSasCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.models.CloudEvent;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class EventGridPublisherClientTests extends EventGridTestBase {
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
    private EventGridPublisherClientBuilder builder;
    private EventGridPublisherClientBuilder syncBuilder;




    @Override
    protected void beforeTest() {
        builder = new EventGridPublisherClientBuilder();
        syncBuilder = new EventGridPublisherClientBuilder();

        if (interceptorManager.isPlaybackMode()) {
            builder.httpClient(buildAssertingClient(interceptorManager.getPlaybackClient(), false));
            syncBuilder.httpClient(buildAssertingClient(interceptorManager.getPlaybackClient(), true));
        } else { // both record and live will use these clients
            builder.httpClient(buildAssertingClient(HttpClient.createDefault(), false));
            syncBuilder.httpClient(buildAssertingClient(HttpClient.createDefault(), true));
        }

        if (interceptorManager.isRecordMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
                .retryPolicy(new RetryPolicy());
            syncBuilder.addPolicy(interceptorManager.getRecordPolicy())
                .retryPolicy(new RetryPolicy());
        }

        setupSanitizers();
    }



    @Test
    public void publishEventGridEvents() {
        EventGridPublisherAsyncClient<EventGridEvent> egClient = builder
            .endpoint(getEndpoint(EVENTGRID_ENDPOINT))
            .credential(getKey(EVENTGRID_KEY))
            .buildEventGridEventPublisherAsyncClient();

        List<EventGridEvent> events = new ArrayList<>();
        events.add(getEventGridEvent());

        StepVerifier.create(egClient.sendEventsWithResponse(events))
            .expectNextMatches(voidResponse -> voidResponse.getStatusCode() == 200)
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        StepVerifier.create(egClient.sendEvents(events))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    public void publishEventGridEvent() {
        EventGridPublisherAsyncClient<EventGridEvent> egClient = builder
            .endpoint(getEndpoint(EVENTGRID_ENDPOINT))
            .credential(getKey(EVENTGRID_KEY))
            .buildEventGridEventPublisherAsyncClient();

        EventGridEvent event = getEventGridEvent();
        StepVerifier.create(egClient.sendEvent(event))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
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
        events.add(getEventGridEvent());

        StepVerifier.create(egClient.sendEventsWithResponse(events, Context.NONE))
            .expectNextMatches(voidResponse -> voidResponse.getStatusCode() == 200)
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
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
        events.add(getCloudEvent());

        StepVerifier.create(egClient.sendEventsWithResponse(events, Context.NONE))
            .expectNextMatches(voidResponse -> voidResponse.getStatusCode() == 200)
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    public void publishCloudEvents() {
        EventGridPublisherAsyncClient<CloudEvent> egClient = builder
            .endpoint(getEndpoint(CLOUD_ENDPOINT))
            .credential(getKey(CLOUD_KEY))
            .buildCloudEventPublisherAsyncClient();

        List<CloudEvent> events = new ArrayList<>();
        events.add(getCloudEvent());

        StepVerifier.create(egClient.sendEventsWithResponse(events, Context.NONE))
            .expectNextMatches(voidResponse -> voidResponse.getStatusCode() == 200)
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    public void publishCloudEvent() {
        EventGridPublisherAsyncClient<CloudEvent> egClient = builder
            .endpoint(getEndpoint(CLOUD_ENDPOINT))
            .credential(getKey(CLOUD_KEY))
            .buildCloudEventPublisherAsyncClient();

        CloudEvent event = getCloudEvent();

        StepVerifier.create(egClient.sendEvent(event))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    public void publishCloudEventsToPartnerTopic() {
        EventGridPublisherAsyncClient<CloudEvent> egClient = builder
                .endpoint(getEndpoint(EVENTGRID_PARTNER_NAMESPACE_TOPIC_ENDPOINT))
                .credential(getKey(EVENTGRID_PARTNER_NAMESPACE_TOPIC_KEY))
                .addPolicy((httpPipelineCallContext, httpPipelineNextPolicy) -> {
                    HttpHeader httpHeader = httpPipelineCallContext.getHttpRequest().getHeaders().get("aeg-channel-name");
                    assertNotNull(httpHeader);
                    return httpPipelineNextPolicy.process();
                })
                .buildCloudEventPublisherAsyncClient();

        CloudEvent event = getCloudEvent();

        Mono<Response<Void>> responseMono = egClient.sendEventsWithResponse(Arrays.asList(event),
            getChannelName(EVENTGRID_PARTNER_CHANNEL_NAME));
        StepVerifier.create(responseMono)
                .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    public void publishEventGridEventToPartnerTopic() {
        EventGridPublisherAsyncClient<EventGridEvent> egClient = builder
                .endpoint(getEndpoint(EVENTGRID_PARTNER_NAMESPACE_TOPIC_ENDPOINT))
                .credential(getKey(EVENTGRID_PARTNER_NAMESPACE_TOPIC_KEY))
                .buildEventGridEventPublisherAsyncClient();

        EventGridEvent event = getEventGridEvent();

        Mono<Response<Void>> responseMono = egClient.sendEventsWithResponse(Arrays.asList(event),
            getChannelName(EVENTGRID_PARTNER_CHANNEL_NAME));
        StepVerifier.create(responseMono)
                .expectErrorSatisfies(exception -> {
                    assertEquals(HttpResponseException.class.getName(), exception.getClass().getName());
                    if (exception instanceof HttpResponseException) {
                        assertEquals(400,
                                ((HttpResponseException) exception).getResponse().getStatusCode());
                    }
                }).verify(DEFAULT_TIMEOUT);
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
            events.add(getCloudEvent(i)
            );
        }

        StepVerifier.create(egClient.sendEventsWithResponse(events, Context.NONE))
            .expectNextMatches(voidResponse -> voidResponse.getStatusCode() == 200)
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }


    @Test
    public void publishCustomEvents() {
        EventGridPublisherAsyncClient<BinaryData> egClient = builder
            .credential(getKey(CUSTOM_KEY))
            .endpoint(getEndpoint(CUSTOM_ENDPOINT))
            .buildCustomEventPublisherAsyncClient();

        List<BinaryData> events = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            events.add(getCustomEvent());
        }
        StepVerifier.create(egClient.sendEventsWithResponse(events))
            .expectNextMatches(voidResponse -> voidResponse.getStatusCode() == 200)
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    public void publishCustomEventsWithSerializer() {
        EventGridPublisherAsyncClient<BinaryData> egClient = builder
            .credential(getKey(CUSTOM_KEY))
            .endpoint(getEndpoint(CUSTOM_ENDPOINT))
            .buildCustomEventPublisherAsyncClient();

        List<BinaryData> events = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            events.add(getCustomEventWithSerializer());
        }
        StepVerifier.create(egClient.sendEventsWithResponse(events, Context.NONE))
            .expectNextMatches(voidResponse -> voidResponse.getStatusCode() == 200)
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    public void publishCustomEvent() {
        EventGridPublisherAsyncClient<BinaryData> egClient = builder
            .credential(getKey(CUSTOM_KEY))
            .endpoint(getEndpoint(CUSTOM_ENDPOINT))
            .buildCustomEventPublisherAsyncClient();

        BinaryData customEvent = getCustomEvent();
        StepVerifier.create(egClient.sendEvent(customEvent))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    public void publishEventGridEventsSync() {
        EventGridPublisherClient<EventGridEvent> egClient = syncBuilder
            .credential(getKey(EVENTGRID_KEY))
            .endpoint(getEndpoint(EVENTGRID_ENDPOINT))
            .buildEventGridEventPublisherClient();

        List<EventGridEvent> events = new ArrayList<>();
        events.add(getEventGridEvent());

        Response<Void> response = egClient.sendEventsWithResponse(events, Context.NONE);

        assertNotNull(response);
        assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void publishEventGridEventSync() {
        EventGridPublisherClient<EventGridEvent> egClient = syncBuilder
            .credential(getKey(EVENTGRID_KEY))
            .endpoint(getEndpoint(EVENTGRID_ENDPOINT))
            .buildEventGridEventPublisherClient();

        EventGridEvent event = getEventGridEvent();

        egClient.sendEvent(event);
    }

    @Test
    public void publishWithSasTokenSync() {

        String sasToken = EventGridPublisherAsyncClient.generateSas(
            getEndpoint(EVENTGRID_ENDPOINT),
            getKey(EVENTGRID_KEY),
            OffsetDateTime.now().plusMinutes(20)
        );

        EventGridPublisherClient<EventGridEvent> egClient = syncBuilder
            .credential(new AzureSasCredential(sasToken))
            .endpoint(getEndpoint(EVENTGRID_ENDPOINT))
            .buildEventGridEventPublisherClient();

        EventGridEvent event = getEventGridEvent();

        egClient.sendEvent(event);
    }

    @Disabled
    @Test
    public void publishWithTokenCredentialSync() {
        DefaultAzureCredential defaultCredential = new DefaultAzureCredentialBuilder().build();
        EventGridPublisherClient<CloudEvent> egClient = syncBuilder
            .credential(defaultCredential)
            .endpoint(getEndpoint(CLOUD_ENDPOINT))
            .buildCloudEventPublisherClient();

        List<CloudEvent> events = new ArrayList<>();
        events.add(getCloudEvent());

        egClient.sendEvents(events);
    }

    @Test
    public void publishCloudEventsSync() {
        EventGridPublisherClient<CloudEvent> egClient = syncBuilder
            .credential(getKey(CLOUD_KEY))
            .endpoint(getEndpoint(CLOUD_ENDPOINT))
            .buildCloudEventPublisherClient();

        List<CloudEvent> events = new ArrayList<>();
        events.add(getCloudEvent());

        Response<Void> response = egClient.sendEventsWithResponse(events, Context.NONE);

        assertNotNull(response);
        assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void publishCloudEventSync() {
        EventGridPublisherClient<CloudEvent> egClient = syncBuilder
            .credential(getKey(CLOUD_KEY))
            .endpoint(getEndpoint(CLOUD_ENDPOINT))
            .buildCloudEventPublisherClient();

        CloudEvent event = getCloudEvent();
        egClient.sendEvent(event);
    }

    @Test
    public void publishCloudEventsToPartnerTopicSync() {
        EventGridPublisherClient<CloudEvent> egClient = syncBuilder
            .endpoint(getEndpoint(EVENTGRID_PARTNER_NAMESPACE_TOPIC_ENDPOINT))
            .credential(getKey(EVENTGRID_PARTNER_NAMESPACE_TOPIC_KEY))
            .addPolicy((httpPipelineCallContext, httpPipelineNextPolicy) -> {
                HttpHeader httpHeader = httpPipelineCallContext.getHttpRequest().getHeaders().get("aeg-channel-name");
                assertNotNull(httpHeader);
                return httpPipelineNextPolicy.process();
            })
            .buildCloudEventPublisherClient();

        CloudEvent event = getCloudEvent();

        Response<Void> response = egClient.sendEventsWithResponse(Arrays.asList(event),
            getChannelName(EVENTGRID_PARTNER_CHANNEL_NAME), Context.NONE);
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void publishEventGridEventToPartnerTopicSync() {
        EventGridPublisherClient<EventGridEvent> egClient = syncBuilder
            .endpoint(getEndpoint(EVENTGRID_PARTNER_NAMESPACE_TOPIC_ENDPOINT))
            .credential(getKey(EVENTGRID_PARTNER_NAMESPACE_TOPIC_KEY))
            .buildEventGridEventPublisherClient();

        EventGridEvent event = getEventGridEvent();


        HttpResponseException exception = assertThrows(HttpResponseException.class, () -> {
            egClient.sendEventsWithResponse(Arrays.asList(event),
                getChannelName(EVENTGRID_PARTNER_CHANNEL_NAME), Context.NONE);
        });
        assertEquals(400, exception.getResponse().getStatusCode());
    }



    @Test
    public void publishCloudEventsCustomSerializerSync() {
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

        EventGridPublisherClient<CloudEvent> egClient = syncBuilder
            .credential(getKey(CLOUD_KEY))
            .endpoint(getEndpoint(CLOUD_ENDPOINT))
            .buildCloudEventPublisherClient();

        List<CloudEvent> events = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            events.add(getCloudEvent(i));
        }

        Response<Void> response = egClient.sendEventsWithResponse(events, Context.NONE);
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void publishCustomEventsSync() {
        EventGridPublisherClient<BinaryData> egClient = syncBuilder
            .credential(getKey(CUSTOM_KEY))
            .endpoint(getEndpoint(CUSTOM_ENDPOINT))
            .buildCustomEventPublisherClient();

        List<BinaryData> events = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            events.add(getCustomEvent());
        }
        Response<Void> response = egClient.sendEventsWithResponse(events, Context.NONE);

        assertNotNull(response);
        assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void publishCustomEventsWithSerializerSync() {
        EventGridPublisherClient<BinaryData> egClient = syncBuilder
            .credential(getKey(CUSTOM_KEY))
            .endpoint(getEndpoint(CUSTOM_ENDPOINT))
            .buildCustomEventPublisherClient();

        List<BinaryData> events = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            events.add(getCustomEventWithSerializer());
        }
        Response<Void> response = egClient.sendEventsWithResponse(events, Context.NONE);
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void publishCustomEventSync() {
        EventGridPublisherClient<BinaryData> egClient = syncBuilder
            .credential(getKey(CUSTOM_KEY))
            .endpoint(getEndpoint(CUSTOM_ENDPOINT))
            .buildCustomEventPublisherClient();

        egClient.sendEvent(getCustomEvent());
    }

    private String getChannelName(String liveEnvName) {
        if (interceptorManager.isPlaybackMode()) {
            return DUMMY_CHANNEL_NAME;
        }
        String channelName = System.getenv(liveEnvName);
        assertNotNull(channelName, "System environment variable " + liveEnvName + " is null");
        return channelName;
    }


    private HttpClient buildAssertingClient(HttpClient httpClient, boolean sync) {
        AssertingHttpClientBuilder builder = new AssertingHttpClientBuilder(httpClient)
            .skipRequest((ignored1, ignored2) -> false);
        if (sync) {
            builder.assertSync();
        } else {
            builder.assertAsync();
        }
        return builder.build();
    }
}
