// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.models.CloudEvent;
import com.azure.core.models.CloudEventDataFormat;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.util.BinaryData;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EventGridTestBase extends TestProxyTestBase {
    static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    // Event Grid endpoint for a topic accepting EventGrid schema events
    static final String EVENTGRID_ENDPOINT = "AZURE_EVENTGRID_EVENT_ENDPOINT";

    // Event Grid endpoint for a topic accepting CloudEvents schema events
    static final String CLOUD_ENDPOINT = "AZURE_EVENTGRID_CLOUDEVENT_ENDPOINT";

    // Event Grid endpoint for a topic accepting custom schema events
    static final String CUSTOM_ENDPOINT = "AZURE_EVENTGRID_CUSTOM_ENDPOINT";

    // Event Grid access key for a topic accepting EventGrid schema events
    static final String EVENTGRID_KEY = "AZURE_EVENTGRID_EVENT_KEY";

    // Event Grid access key for a topic accepting CloudEvents schema events
    static final String CLOUD_KEY = "AZURE_EVENTGRID_CLOUDEVENT_KEY";

    // Event Grid access key for a topic accepting custom schema events
    static final String CUSTOM_KEY = "AZURE_EVENTGRID_CUSTOM_KEY";

    // Endpoint, key and channel name for publishing to partner topic
    static final String EVENTGRID_PARTNER_NAMESPACE_TOPIC_ENDPOINT = "EVENTGRID_PARTNER_NAMESPACE_TOPIC_ENDPOINT";
    static final String EVENTGRID_PARTNER_NAMESPACE_TOPIC_KEY = "EVENTGRID_PARTNER_NAMESPACE_TOPIC_KEY";
    static final String EVENTGRID_PARTNER_CHANNEL_NAME = "EVENTGRID_PARTNER_CHANNEL_NAME";

    static final String DUMMY_ENDPOINT = "https://www.dummyEndpoint.com/api/events";

    static final String DUMMY_KEY = "dummyKey";

    static final String DUMMY_CHANNEL_NAME = "dummy-channel";

    EventGridPublisherClientBuilder builder;
    EventGridPublisherClientBuilder syncBuilder;

    @Override
    protected void afterTest() {
        StepVerifier.resetDefaultTimeout();
    }

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
    void setupSanitizers() {
        if (!interceptorManager.isLiveMode()) {
            List<TestProxySanitizer> sanitizers = new ArrayList<>();
            sanitizers.add(new TestProxySanitizer("aeg-sas-token", null, "REDACTED", TestProxySanitizerType.HEADER));
            sanitizers.add(new TestProxySanitizer("aeg-sas-key", null, "REDACTED", TestProxySanitizerType.HEADER));
            sanitizers.add(new TestProxySanitizer("aeg-channel-name", null, "REDACTED", TestProxySanitizerType.HEADER));
            interceptorManager.addSanitizers(sanitizers);
        }
    }

    EventGridEvent getEventGridEvent() {
        EventGridEvent event = new EventGridEvent("Test", "Microsoft.MockPublisher.TestEvent",
            BinaryData.fromObject(new HashMap<String, String>() {
                {
                    put("Field1", "Value1");
                    put("Field2", "Value2");
                    put("Field3", "Value3");
                }
            }),
            "1.0")
            .setEventTime(testResourceNamer.now())
            .setId(testResourceNamer.randomUuid());
        return event;
    }

    BinaryData getCustomEvent() {
        return BinaryData.fromObject(new HashMap<String, String>() {
            {
                put("id", testResourceNamer.randomUuid());
                put("time", testResourceNamer.now().toString());
                put("subject", "Test");
                put("foo", "bar");
                put("type", "Microsoft.MockPublisher.TestEvent");
            }
        });
    }

    CloudEvent getCloudEvent() {
        return new CloudEvent("/microsoft/testEvent", "Microsoft.MockPublisher.TestEvent",
            BinaryData.fromObject(new HashMap<String, String>() {
                {
                    put("Field1", "Value1");
                    put("Field2", "Value2");
                    put("Field3", "Value3");
                }
            }), CloudEventDataFormat.JSON, "application/json")
            .setSubject("Test")
            .setTime(testResourceNamer.now())
            .setId(testResourceNamer.randomUuid());
    }

    String getEndpoint(String liveEnvName) {
        if (interceptorManager.isPlaybackMode()) {
            return DUMMY_ENDPOINT;
        }
        String endpoint = System.getenv(liveEnvName);
        assertNotNull(endpoint, "System environment variable " + liveEnvName + " is null");
        return endpoint;
    }

    AzureKeyCredential getKey(String liveEnvName) {
        if (interceptorManager.isPlaybackMode()) {
            return new AzureKeyCredential(DUMMY_KEY);
        }
        AzureKeyCredential key = new AzureKeyCredential(System.getenv(liveEnvName));
        assertNotNull(key.getKey(), "System environment variable " + liveEnvName + " is null");
        return key;
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

    String getChannelName(String liveEnvName) {
        if (interceptorManager.isPlaybackMode()) {
            return DUMMY_CHANNEL_NAME;
        }
        String channelName = System.getenv(liveEnvName);
        assertNotNull(channelName, "System environment variable " + liveEnvName + " is null");
        return channelName;
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
}
