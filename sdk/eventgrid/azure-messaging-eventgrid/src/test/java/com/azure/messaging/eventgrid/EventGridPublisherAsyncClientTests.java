// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.rest.Response;
import com.azure.core.models.CloudEvent;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class EventGridPublisherAsyncClientTests extends EventGridTestBase {

    @Test
    public void publishEventGridEvents() {
        EventGridPublisherAsyncClient<EventGridEvent> egClient = builder
            .endpoint(getEndpoint(EVENTGRID_ENDPOINT))
            .credential(TestUtil.getTestTokenCredential(interceptorManager))
            .buildEventGridEventPublisherAsyncClient();

        List<EventGridEvent> events = new ArrayList<>();
        events.add(getEventGridEvent());

        StepVerifier.create(egClient.sendEvents(events))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    public void publishEventGridEventsWithResponse() {
        EventGridPublisherAsyncClient<EventGridEvent> egClient = builder
            .endpoint(getEndpoint(EVENTGRID_ENDPOINT))
            .credential(TestUtil.getTestTokenCredential(interceptorManager))
            .buildEventGridEventPublisherAsyncClient();

        List<EventGridEvent> events = new ArrayList<>();
        events.add(getEventGridEvent());

        StepVerifier.create(egClient.sendEventsWithResponse(events))
            .expectNextMatches(voidResponse -> voidResponse.getStatusCode() == 200)
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    public void publishEventGridEvent() {
        EventGridPublisherAsyncClient<EventGridEvent> egClient = builder
            .endpoint(getEndpoint(EVENTGRID_ENDPOINT))
            .credential(TestUtil.getTestTokenCredential(interceptorManager))
            .buildEventGridEventPublisherAsyncClient();

        EventGridEvent event = getEventGridEvent();
        StepVerifier.create(egClient.sendEvent(event))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    @LiveOnly
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
    @LiveOnly
    public void publishWithAzureKeyCredential() {
        EventGridPublisherAsyncClient<EventGridEvent> egClient = builder
            .credential(getKey(EVENTGRID_KEY))
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
    public void publishCloudEvents() {
        EventGridPublisherAsyncClient<CloudEvent> egClient = builder
            .endpoint(getEndpoint(CLOUD_ENDPOINT))
            .credential(TestUtil.getTestTokenCredential(interceptorManager))
            .buildCloudEventPublisherAsyncClient();

        List<CloudEvent> events = new ArrayList<>();
        events.add(getCloudEvent());

        StepVerifier.create(egClient.sendEvents(events, Context.NONE))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    public void publishCloudEventsWithResponse() {
        EventGridPublisherAsyncClient<CloudEvent> egClient = builder
            .endpoint(getEndpoint(CLOUD_ENDPOINT))
            .credential(TestUtil.getTestTokenCredential(interceptorManager))
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
            .credential(TestUtil.getTestTokenCredential(interceptorManager))
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
            .credential(TestUtil.getTestTokenCredential(interceptorManager))
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
            .credential(TestUtil.getTestTokenCredential(interceptorManager))
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

    @Test
    public void publishCustomEvents() {
        EventGridPublisherAsyncClient<BinaryData> egClient = builder
            .credential(TestUtil.getTestTokenCredential(interceptorManager))
            .endpoint(getEndpoint(CUSTOM_ENDPOINT))
            .buildCustomEventPublisherAsyncClient();

        List<BinaryData> events = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            events.add(getCustomEvent());
        }
        StepVerifier.create(egClient.sendEvents(events))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    public void publishCustomEventsWithResponse() {
        EventGridPublisherAsyncClient<BinaryData> egClient = builder
            .credential(TestUtil.getTestTokenCredential(interceptorManager))
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
    public void publishCustomEvent() {
        EventGridPublisherAsyncClient<BinaryData> egClient = builder
            .credential(TestUtil.getTestTokenCredential(interceptorManager))
            .endpoint(getEndpoint(CUSTOM_ENDPOINT))
            .buildCustomEventPublisherAsyncClient();

        BinaryData customEvent = getCustomEvent();
        StepVerifier.create(egClient.sendEvent(customEvent))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }
}
