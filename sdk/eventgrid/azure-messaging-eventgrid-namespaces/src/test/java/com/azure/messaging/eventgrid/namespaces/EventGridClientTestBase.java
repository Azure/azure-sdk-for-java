// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.

package com.azure.messaging.eventgrid.namespaces;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.models.CloudEvent;
import com.azure.core.models.CloudEventDataFormat;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EventGridClientTestBase extends TestProxyTestBase {
    static final String EVENTGRID_TOPIC_NAME = "EVENTGRID_TOPIC_NAME";

    static final String EVENTGRID_EVENT_SUBSCRIPTION_NAME = "EVENTGRID_EVENT_SUBSCRIPTION_NAME";

    static final String EVENTGRID_ENDPOINT = "EVENTGRID_ENDPOINT";

    static final String EVENTGRID_KEY = "EVENTGRID_KEY";

    static final String DUMMY_ENDPOINT = "https://www.dummyEndpoint.com/api/events";
    static final String DUMMY_TOPIC_ENDPOINT = "https://www.dummyEndpoint.com";

    static final String DUMMY_KEY = "dummyKey";

    static final String DUMMY_CHANNEL_NAME = "dummy-channel";

    public static final String TOPIC_NAME
        = Configuration.getGlobalConfiguration().get(EVENTGRID_TOPIC_NAME, "testtopic1");
    public static final String EVENT_SUBSCRIPTION_NAME
        = Configuration.getGlobalConfiguration().get(EVENTGRID_EVENT_SUBSCRIPTION_NAME, "testsubscription1");

    EventGridReceiverClientBuilder receiverBuilder;
    EventGridSenderClientBuilder senderBuilder;

    protected void makeBuilders(boolean sync) {
        receiverBuilder = buildReceiverClientBuilder();
        senderBuilder = buildSenderClientBuilder();
        receiverBuilder.httpClient(
            buildAssertingClient(getHttpClientOrUsePlayback(getHttpClients().findFirst().orElse(null)), sync));
        senderBuilder.httpClient(
            buildAssertingClient(getHttpClientOrUsePlayback(getHttpClients().findFirst().orElse(null)), sync));

        if (interceptorManager.isRecordMode()) {
            receiverBuilder.addPolicy(interceptorManager.getRecordPolicy()).retryPolicy(new RetryPolicy());
            senderBuilder.addPolicy(interceptorManager.getRecordPolicy()).retryPolicy(new RetryPolicy());
        }
        setupSanitizers();
    }

    @Override
    protected void afterTest() {
        StepVerifier.resetDefaultTimeout();
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

    EventGridReceiverClientBuilder buildReceiverClientBuilder() {
        return new EventGridReceiverClientBuilder()
            .httpClient(getHttpClientOrUsePlayback(getHttpClients().findFirst().orElse(null)))
            .httpLogOptions(new HttpLogOptions())
            .subscriptionName(EVENT_SUBSCRIPTION_NAME)
            .topicName(TOPIC_NAME)
            .endpoint(getTopicEndpoint(EVENTGRID_ENDPOINT));
    }

    EventGridSenderClientBuilder buildSenderClientBuilder() {
        return new EventGridSenderClientBuilder()
            .httpClient(getHttpClientOrUsePlayback(getHttpClients().findFirst().orElse(null)))
            .httpLogOptions(new HttpLogOptions())
            .topicName(TOPIC_NAME)
            .endpoint(getTopicEndpoint(EVENTGRID_ENDPOINT));
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

    BinaryData getCustomEventWithSerializer() {
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
            }), CloudEventDataFormat.JSON, "application/json").setSubject("Test")
                .setTime(testResourceNamer.now())
                .setId(testResourceNamer.randomUuid());
    }

    CloudEvent getCloudEvent(int i) {
        return new CloudEvent("/microsoft/testEvent", "Microsoft.MockPublisher.TestEvent",
            BinaryData.fromObject(new TestData().setName("Hello " + i)), CloudEventDataFormat.JSON, null)
                .setSubject("Test " + i)
                .setTime(testResourceNamer.now())
                .setId(testResourceNamer.randomUuid());
    }

    String getEndpoint(String liveEnvName) {
        if (interceptorManager.isPlaybackMode()) {
            return DUMMY_ENDPOINT;
        }
        String endpoint = Configuration.getGlobalConfiguration().get(liveEnvName);
        assertNotNull(endpoint, "System environment variable " + liveEnvName + " is null");
        return endpoint;
    }

    String getTopicEndpoint(String liveEnvName) {
        if (interceptorManager.isPlaybackMode()) {
            return DUMMY_TOPIC_ENDPOINT;
        }
        String endpoint = Configuration.getGlobalConfiguration().get(liveEnvName);
        assertNotNull(endpoint, "System environment variable " + liveEnvName + " is null");
        return endpoint;
    }

    AzureKeyCredential getKey(String liveEnvName) {
        if (interceptorManager.isPlaybackMode()) {
            return new AzureKeyCredential(DUMMY_KEY);
        }
        AzureKeyCredential key = new AzureKeyCredential(Configuration.getGlobalConfiguration().get(liveEnvName));
        assertNotNull(key.getKey(), "System environment variable " + liveEnvName + " is null");
        return key;
    }

    HttpClient buildAssertingClient(HttpClient httpClient, boolean sync) {
        AssertingHttpClientBuilder builder
            = new AssertingHttpClientBuilder(httpClient).skipRequest((ignored1, ignored2) -> false);
        if (sync) {
            builder.assertSync();
        } else {
            builder.assertAsync();
        }
        return builder.build();
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
