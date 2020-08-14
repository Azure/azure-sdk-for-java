// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.azure.messaging.eventgrid;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestBase;
import com.azure.messaging.eventgrid.implementation.EventGridPublisherClientImpl;
import com.azure.messaging.eventgrid.implementation.EventGridPublisherClientImplBuilder;
import com.azure.messaging.eventgrid.implementation.models.CloudEvent;
import com.azure.messaging.eventgrid.implementation.models.EventGridEvent;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class EventGridPublisherImplTests extends TestBase {

    HttpPipelineBuilder builder;

    private static final String EVENTGRID_ENDPOINT = "EG_ENDPOINT";

    private static final String CLOUD_ENDPOINT = "EG_CLOUD_ENDPOINT";

    private static final String CUSTOM_ENDPOINT = "EG_CUSTOM_ENDPOINT";

    private static final String EVENTGRID_KEY = "EG_KEY";

    private static final String CLOUD_KEY = "EG_CLOUD_KEY";

    private static final String CUSTOM_KEY = "EG_CUSTOM_KEY";

    private static final String DUMMY_ENDPOINT = "https://www.dummyEndpoint.com";

    private static final String DUMMY_KEY = "dummyKey";

    @Override
    protected void beforeTest() {
        builder = new HttpPipelineBuilder();

        if (interceptorManager.isPlaybackMode()) {
            builder.httpClient(interceptorManager.getPlaybackClient());
        } else {
            builder.policies(interceptorManager.getRecordPolicy(), new RetryPolicy());
        }
    }

    @Test
    public void testPublishEventGridEventsImpl() throws MalformedURLException {
        EventGridPublisherClientImpl egClient = new EventGridPublisherClientImplBuilder()
            .pipeline(builder.policies(
                new AddHeadersPolicy(new HttpHeaders().put("aeg-sas-key", getKey(EVENTGRID_KEY))))
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

        String hostname = new URL(getEndpoint(EVENTGRID_ENDPOINT)).getHost();
        Response<Void> response = egClient.publishEventsWithResponseAsync(hostname, events).block();

        assertNotNull(response);
        assertEquals(response.getStatusCode(), 200);

    }

    @Test
    public void testPublishCloudEventsImpl() throws MalformedURLException {
        EventGridPublisherClientImpl egClient = new EventGridPublisherClientImplBuilder()
            .pipeline(builder.policies(
                new AddHeadersPolicy(new HttpHeaders().put("aeg-sas-key", getKey(CLOUD_KEY))))
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

        String hostname = new URL(getEndpoint(CLOUD_ENDPOINT)).getHost();
        Response<Void> response = egClient.publishCloudEventEventsWithResponseAsync(hostname, events).block();

        assertNotNull(response);
        assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void TestPublishCustomEventsImpl() throws MalformedURLException {

        EventGridPublisherClientImpl egClient = new EventGridPublisherClientImplBuilder()
            .pipeline(builder.policies(
                new AddHeadersPolicy(new HttpHeaders().put("aeg-sas-key", getKey(CUSTOM_KEY))))
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

        String hostname = new URL(getEndpoint(CUSTOM_ENDPOINT)).getHost();
        Response<Void> response = egClient.publishCustomEventEventsWithResponseAsync(hostname, events).block();

        assertNotNull(response);
        assertEquals(response.getStatusCode(), 200);
    }


    private String getEndpoint(String liveEnvName) {
        if (interceptorManager.isPlaybackMode()) {
            return DUMMY_ENDPOINT;
        }
        return System.getenv(liveEnvName);
    }

    private String getKey(String liveEnvName) {
        if (interceptorManager.isPlaybackMode()) {
            return DUMMY_KEY;
        }
        return System.getenv(liveEnvName);
    }
}
