// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.azure.messaging.eventgrid;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.TestBase;
import com.azure.messaging.eventgrid.implementation.EventGridPublisherClientImpl;
import com.azure.messaging.eventgrid.implementation.EventGridPublisherClientImplBuilder;
import com.azure.messaging.eventgrid.implementation.models.CloudEvent;
import com.azure.messaging.eventgrid.implementation.models.EventGridEvent;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;


public class EventGridPublisherImplTests extends TestBase {

    private HttpPipelineBuilder pipelineBuilder;

    private EventGridPublisherClientImplBuilder clientBuilder;

    // Event Grid endpoint for a topic accepting EventGrid schema events
    private static final String EVENTGRID_ENDPOINT = "AZURE_EVENTGRID_EVENTGRID_ENDPOINT";

    // Event Grid endpoint for a topic accepting CloudEvents schema events
    private static final String CLOUD_ENDPOINT = "AZURE_EVENTGRID_CLOUD_ENDPOINT";

    // Event Grid endpoint for a topic accepting custom schema events
    private static final String CUSTOM_ENDPOINT = "AZURE_EVENTGRID_CUSTOM_ENDPOINT";

    // Event Grid access key for a topic accepting EventGrid schema events
    private static final String EVENTGRID_KEY = "AZURE_EVENTGRID_EVENTGRID_KEY";

    // Event Grid access key for a topic accepting CloudEvents schema events
    private static final String CLOUD_KEY = "AZURE_EVENTGRID_CLOUD_KEY";

    // Event Grid access key for a topic accepting custom schema events
    private static final String CUSTOM_KEY = "AZURE_EVENTGRID_CUSTOM_KEY";

    private static final String DUMMY_ENDPOINT = "https://www.dummyEndpoint.com";

    private static final String DUMMY_KEY = "dummyKey";

    @Override
    protected void beforeTest() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));

        pipelineBuilder = new HttpPipelineBuilder();

        clientBuilder = new EventGridPublisherClientImplBuilder();

        if (interceptorManager.isPlaybackMode()) {
            pipelineBuilder.httpClient(interceptorManager.getPlaybackClient());
        } else {
            pipelineBuilder.policies(interceptorManager.getRecordPolicy(), new RetryPolicy());
        }
    }

    @Override
    protected void afterTest() {
        StepVerifier.resetDefaultTimeout();
    }

    @Test
    public void publishEventGridEventsImpl() throws MalformedURLException {
        EventGridPublisherClientImpl egClient = clientBuilder
            .pipeline(pipelineBuilder.policies(
                new AddHeadersPolicy(new HttpHeaders().put("aeg-sas-key", getKey(EVENTGRID_KEY))))
                .build())
            .buildClient();

        List<EventGridEvent> events = Collections.singletonList(
            new EventGridEvent()
                .setId(UUID.randomUUID().toString())
                .setSubject("Test")
                .setEventType("Microsoft.MockPublisher.TestEvent")
                .setData(new HashMap<String, String>() {{
                    put("Field1", "Value1");
                    put("Field2", "Value2");
                    put("Field3", "Value3");
                }})
                .setDataVersion("1.0")
                .setEventTime(OffsetDateTime.now())
        );

        String hostname = new URL(getEndpoint(EVENTGRID_ENDPOINT)).getHost();

        StepVerifier.create(egClient.publishEventsWithResponseAsync(hostname, events))
            .expectNextMatches(voidResponse -> voidResponse.getStatusCode() == 200)
            .verifyComplete();
    }

    @Test
    public void publishCloudEventsImpl() throws MalformedURLException {
        EventGridPublisherClientImpl egClient = clientBuilder
            .pipeline(pipelineBuilder.policies(
                new AddHeadersPolicy(new HttpHeaders().put("aeg-sas-key", getKey(CLOUD_KEY))))
                .build())
            .buildClient();

        List<CloudEvent> events = Collections.singletonList(
            new CloudEvent()
                .setId(UUID.randomUUID().toString())
                .setSubject("Test")
                .setType("Microsoft.MockPublisher.TestEvent")
                .setData(new HashMap<String, String>() {{
                    put("Field1", "Value1");
                    put("Field2", "Value2");
                    put("Field3", "Value3");
                }})
                .setSpecversion("1.0")
                .setTime(OffsetDateTime.now())
        );

        String hostname = new URL(getEndpoint(CLOUD_ENDPOINT)).getHost();

        StepVerifier.create(egClient.publishCloudEventEventsWithResponseAsync(hostname, events))
            .expectNextMatches(voidResponse -> voidResponse.getStatusCode() == 200)
            .verifyComplete();
    }

    @Test
    public void publishCustomEventsImpl() throws MalformedURLException {
        EventGridPublisherClientImpl egClient = clientBuilder
            .pipeline(pipelineBuilder.policies(
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

        StepVerifier.create(egClient.publishCustomEventEventsWithResponseAsync(hostname, events))
            .expectNextMatches(voidResponse -> voidResponse.getStatusCode() == 200)
            .verifyComplete();
    }


    private String getEndpoint(String liveEnvName) {
        if (interceptorManager.isPlaybackMode()) {
            return DUMMY_ENDPOINT;
        }
        String endpoint = System.getenv(liveEnvName);
        assertNotNull(endpoint, "System environment variable " + liveEnvName + "is null");
        return endpoint;
    }

    private String getKey(String liveEnvName) {
        if (interceptorManager.isPlaybackMode()) {
            return DUMMY_KEY;
        }
        String key = System.getenv(liveEnvName);
        assertNotNull(key, "System environment variable " + liveEnvName + "is null");
        return key;
    }
}
