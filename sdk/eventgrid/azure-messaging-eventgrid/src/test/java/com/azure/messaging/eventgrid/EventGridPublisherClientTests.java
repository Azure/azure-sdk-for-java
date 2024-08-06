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
import org.junit.jupiter.api.TestInstance;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EventGridPublisherClientTests extends EventGridTestBase {

    @Test
    public void publishEventGridEvents() {
        EventGridPublisherClient<EventGridEvent> egClient = syncBuilder
            .credential(TestUtil.getTestTokenCredential(interceptorManager))
            .endpoint(getEndpoint(EVENTGRID_ENDPOINT))
            .buildEventGridEventPublisherClient();

        List<EventGridEvent> events = new ArrayList<>();
        events.add(getEventGridEvent());

        egClient.sendEvents(events);
    }

    @Test
    public void publishEventGridEventsWithResponse() {
        EventGridPublisherClient<EventGridEvent> egClient = syncBuilder
            .credential(TestUtil.getTestTokenCredential(interceptorManager))
            .endpoint(getEndpoint(EVENTGRID_ENDPOINT))
            .buildEventGridEventPublisherClient();

        List<EventGridEvent> events = new ArrayList<>();
        events.add(getEventGridEvent());

        Response<Void> response = egClient.sendEventsWithResponse(events, Context.NONE);

        assertNotNull(response);
        assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void publishEventGridEvent() {
        EventGridPublisherClient<EventGridEvent> egClient = syncBuilder
            .credential(TestUtil.getTestTokenCredential(interceptorManager))
            .endpoint(getEndpoint(EVENTGRID_ENDPOINT))
            .buildEventGridEventPublisherClient();

        EventGridEvent event = getEventGridEvent();

        egClient.sendEvent(event);
    }

    @Test
    @LiveOnly
    public void publishWithSasToken() {
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

    @Test
    @LiveOnly
    public void publishWithAzureKeyCredential() {
        EventGridPublisherClient<EventGridEvent> egClient = syncBuilder
            .credential(getKey(EVENTGRID_KEY))
            .endpoint(getEndpoint(EVENTGRID_ENDPOINT))
            .buildEventGridEventPublisherClient();

        EventGridEvent event = getEventGridEvent();

        egClient.sendEvent(event);
    }

    @Test
    public void publishCloudEvents() {
        EventGridPublisherClient<CloudEvent> egClient = syncBuilder
            .credential(TestUtil.getTestTokenCredential(interceptorManager))
            .endpoint(getEndpoint(CLOUD_ENDPOINT))
            .buildCloudEventPublisherClient();

        List<CloudEvent> events = new ArrayList<>();
        events.add(getCloudEvent());

        egClient.sendEvents(events);
    }

    @Test
    public void publishCloudEventsWithResponse() {
        EventGridPublisherClient<CloudEvent> egClient = syncBuilder
            .credential(TestUtil.getTestTokenCredential(interceptorManager))
            .endpoint(getEndpoint(CLOUD_ENDPOINT))
            .buildCloudEventPublisherClient();

        List<CloudEvent> events = new ArrayList<>();
        events.add(getCloudEvent());

        Response<Void> response = egClient.sendEventsWithResponse(events, Context.NONE);

        assertNotNull(response);
        assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void publishCloudEvent() {
        EventGridPublisherClient<CloudEvent> egClient = syncBuilder
            .credential(TestUtil.getTestTokenCredential(interceptorManager))
            .endpoint(getEndpoint(CLOUD_ENDPOINT))
            .buildCloudEventPublisherClient();

        CloudEvent event = getCloudEvent();
        egClient.sendEvent(event);
    }

    @Test
    public void publishCloudEventToPartnerTopic() {
        EventGridPublisherClient<CloudEvent> egClient = syncBuilder
            .endpoint(getEndpoint(EVENTGRID_PARTNER_NAMESPACE_TOPIC_ENDPOINT))
            .credential(TestUtil.getTestTokenCredential(interceptorManager))
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
    public void publishEventGridEventToPartnerTopic() {
        EventGridPublisherClient<EventGridEvent> egClient = syncBuilder
            .endpoint(getEndpoint(EVENTGRID_PARTNER_NAMESPACE_TOPIC_ENDPOINT))
            .credential(TestUtil.getTestTokenCredential(interceptorManager))
            .buildEventGridEventPublisherClient();

        EventGridEvent event = getEventGridEvent();


        HttpResponseException exception = assertThrows(HttpResponseException.class, () -> {
            egClient.sendEventsWithResponse(Arrays.asList(event),
                getChannelName(EVENTGRID_PARTNER_CHANNEL_NAME), Context.NONE);
        });
        assertEquals(400, exception.getResponse().getStatusCode());
    }


    @Test
    public void publishCustomEvents() {
        EventGridPublisherClient<BinaryData> egClient = syncBuilder
            .credential(TestUtil.getTestTokenCredential(interceptorManager))
            .endpoint(getEndpoint(CUSTOM_ENDPOINT))
            .buildCustomEventPublisherClient();

        List<BinaryData> events = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            events.add(getCustomEvent());
        }
        egClient.sendEventsWithResponse(events, Context.NONE);
    }

    @Test
    public void publishCustomEventsWithResponse() {
        EventGridPublisherClient<BinaryData> egClient = syncBuilder
            .credential(TestUtil.getTestTokenCredential(interceptorManager))
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
    public void publishCustomEvent() {
        EventGridPublisherClient<BinaryData> egClient = syncBuilder
            .credential(TestUtil.getTestTokenCredential(interceptorManager))
            .endpoint(getEndpoint(CUSTOM_ENDPOINT))
            .buildCustomEventPublisherClient();

        egClient.sendEvent(getCustomEvent());
    }
}
