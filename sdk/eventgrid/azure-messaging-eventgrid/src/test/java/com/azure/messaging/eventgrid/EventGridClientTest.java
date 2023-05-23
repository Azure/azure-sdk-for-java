// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.models.CloudEvent;
import com.azure.core.models.CloudEventDataFormat;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.messaging.eventgrid.models.AcknowledgeOptions;
import com.azure.messaging.eventgrid.models.AcknowledgeResult;
import com.azure.messaging.eventgrid.models.ReceiveResult;
import com.azure.messaging.eventgrid.models.RejectOptions;
import com.azure.messaging.eventgrid.models.RejectResult;
import com.azure.messaging.eventgrid.models.ReleaseOptions;
import com.azure.messaging.eventgrid.models.ReleaseResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled
class EventGridClientTest {

    // There is no ARM support yet, so these tests are disabled as they require running by hand.

    public static final String TOPICNAME = "billwertegv2-1-egv2-topic";
    public static final String EVENT_SUBSCRIPTION_NAME = "billwertegv2-1-egv2-es";
    public static final String ENDPOINT = "https://billwertegv2-1-egv2-ns.centraluseuap-1.eventgrid.azure.net";
    public static final AzureKeyCredential CREDENTIAL = new AzureKeyCredential(Configuration.getGlobalConfiguration().get("EG_KEY"));

    EventGridClientBuilder buildClientBuilder() {
        return new EventGridClientBuilder()
            .httpClient(HttpClient.createDefault())
            .endpoint(ENDPOINT)
            .serviceVersion(EventGridMessagingServiceVersion.V2023_06_01_PREVIEW)
            .credential(CREDENTIAL);
    }

    EventGridClient buildClient() {
        return buildClientBuilder().buildClient();
    }

    EventGridAsyncClient buildAsyncClient() {
        return buildClientBuilder().buildAsyncClient();
    }


    @Test
    void publishCloudEventSync() {
        EventGridClient client = buildClient();
        client.publishCloudEvent(TOPICNAME, getCloudEvent());
    }

    @Test
    void publishBatchOfCloudEventsSync() {
        EventGridClient client = buildClient();
        client.publishCloudEvents(TOPICNAME, Arrays.asList(getCloudEvent(), getCloudEvent()));
    }

    @Test
    void receiveBatchOfCloudEventsSync() {
        EventGridClient client = buildClient();
        client.publishCloudEvent(TOPICNAME, getCloudEvent());
        ReceiveResult result = client.receiveCloudEvents(TOPICNAME, EVENT_SUBSCRIPTION_NAME, 10, Duration.ofSeconds(10));
        assertNotNull(result);
        assertTrue(result.getValue().size() > 0);
    }

    @Test
    void acknowledgeBatchOfCloudEventsSync() {
        EventGridClient client = buildClient();
        client.publishCloudEvent(TOPICNAME, getCloudEvent());
        ReceiveResult receiveResult = client.receiveCloudEvents(TOPICNAME, EVENT_SUBSCRIPTION_NAME, 1, Duration.ofSeconds(10));
        AcknowledgeOptions acknowledgeOptions = new AcknowledgeOptions(Arrays.asList(receiveResult.getValue().get(0).getBrokerProperties().getLockToken()));
        AcknowledgeResult acknowledgeResult = client.acknowledgeCloudEvents(TOPICNAME, EVENT_SUBSCRIPTION_NAME, acknowledgeOptions);
        assertNotNull(acknowledgeResult);
        assertTrue(acknowledgeResult.getSucceededLockTokens().size() > 0);
    }

    @Test
    void releaseBatchOfCloudEventsSync() {
        EventGridClient client = buildClient();
        client.publishCloudEvent(TOPICNAME, getCloudEvent());
        ReceiveResult receiveResult = client.receiveCloudEvents(TOPICNAME, EVENT_SUBSCRIPTION_NAME, 1, Duration.ofSeconds(10));
        ReleaseOptions releaseOptions = new ReleaseOptions(Arrays.asList(receiveResult.getValue().get(0).getBrokerProperties().getLockToken()));
        ReleaseResult releaseResult = client.releaseCloudEvents(TOPICNAME, EVENT_SUBSCRIPTION_NAME, releaseOptions);
        assertNotNull(releaseResult);
        assertTrue(releaseResult.getSucceededLockTokens().size() > 0);
    }

    @Test
    void rejectBatchOfCloudEventsSync() {
        EventGridClient client = buildClient();
        client.publishCloudEvent(TOPICNAME, getCloudEvent());
        ReceiveResult receiveResult = client.receiveCloudEvents(TOPICNAME, EVENT_SUBSCRIPTION_NAME, 1, Duration.ofSeconds(10));
        RejectOptions rejectOptions = new RejectOptions(Arrays.asList(receiveResult.getValue().get(0).getBrokerProperties().getLockToken()));
        RejectResult rejectResult = client.rejectCloudEvents(TOPICNAME, EVENT_SUBSCRIPTION_NAME, rejectOptions);
        assertNotNull(rejectResult);
        assertTrue(rejectResult.getSucceededLockTokens().size() > 0);
    }

    @Test
    void publishCloudEvent() {
        EventGridAsyncClient client = buildAsyncClient();
        StepVerifier.create(client.publishCloudEvent(TOPICNAME, getCloudEvent()))
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();
    }

    @Test
    void publishBatchOfCloudEvents() {
        EventGridAsyncClient client = buildAsyncClient();
        StepVerifier.create(client.publishCloudEvents(TOPICNAME, Arrays.asList(getCloudEvent(), getCloudEvent())))
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();
    }

    @Test
    void receiveBatchOfCloudEvents() {
        EventGridAsyncClient client = buildAsyncClient();
        client.publishCloudEvent(TOPICNAME, getCloudEvent()).block();
        StepVerifier.create(client.receiveCloudEvents(TOPICNAME, EVENT_SUBSCRIPTION_NAME, 10, Duration.ofSeconds(10)))
            .assertNext(receiveResult -> {
                assertNotNull(receiveResult);
                assertTrue(receiveResult.getValue().size() > 0);
            })
            .verifyComplete();
    }

    @Test
    void acknowledgeBatchOfCloudEvents() {
        EventGridAsyncClient client = buildAsyncClient();
        client.publishCloudEvent(TOPICNAME, getCloudEvent()).block();
        client.receiveCloudEvents(TOPICNAME, EVENT_SUBSCRIPTION_NAME, 1, Duration.ofSeconds(10))
            .flatMap(receiveResult -> {
                AcknowledgeOptions acknowledgeOptions = new AcknowledgeOptions(Arrays.asList(receiveResult.getValue().get(0).getBrokerProperties().getLockToken()));
                return client.acknowledgeCloudEvents(TOPICNAME, EVENT_SUBSCRIPTION_NAME, acknowledgeOptions);
            })
            .as(StepVerifier::create)
            .assertNext(acknowledgeResult -> {
                assertNotNull(acknowledgeResult);
                assertTrue(acknowledgeResult.getSucceededLockTokens().size() > 0);
            })
            .verifyComplete();
    }

    @Test
    void releaseBatchOfCloudEvents() {
        EventGridAsyncClient client = buildAsyncClient();
        client.publishCloudEvent(TOPICNAME, getCloudEvent()).block();
        client.receiveCloudEvents(TOPICNAME, EVENT_SUBSCRIPTION_NAME, 1, Duration.ofSeconds(10))
            .flatMap(receiveResult -> {
                ReleaseOptions releaseOptions = new ReleaseOptions(Arrays.asList(receiveResult.getValue().get(0).getBrokerProperties().getLockToken()));
                return client.releaseCloudEvents(TOPICNAME, EVENT_SUBSCRIPTION_NAME, releaseOptions);
            })
            .as(StepVerifier::create)
            .assertNext(releaseResult -> {
                assertNotNull(releaseResult);
                assertTrue(releaseResult.getSucceededLockTokens().size() > 0);
            })
            .verifyComplete();
    }

    @Test
    void rejectBatchOfCloudEvents() {
        EventGridAsyncClient client = buildAsyncClient();
        client.publishCloudEvent(TOPICNAME, getCloudEvent()).block();
        client.receiveCloudEvents(TOPICNAME, EVENT_SUBSCRIPTION_NAME, 1, Duration.ofSeconds(10))
            .flatMap(receiveResult -> {
                RejectOptions rejectOptions = new RejectOptions(Arrays.asList(receiveResult.getValue().get(0).getBrokerProperties().getLockToken()));
                return client.rejectCloudEvents(TOPICNAME, EVENT_SUBSCRIPTION_NAME, rejectOptions);
            })
            .as(StepVerifier::create)
            .assertNext(rejectResult -> {
                assertNotNull(rejectResult);
                assertTrue(rejectResult.getSucceededLockTokens().size() > 0);
            })
            .verifyComplete();
    }

    private static CloudEvent getCloudEvent() {
        return new CloudEvent("/events/testEvent", "Microsoft.MockPublisher.TestEvent",
            BinaryData.fromObject(new HashMap<String, String>() {
                {
                    put("Field1", "Value1");
                    put("Field2", "Value2");
                    put("Field3", "Value3");
                }
            }), CloudEventDataFormat.JSON, "application/json")
            .setSubject("Test")
            .setTime(OffsetDateTime.now());
    }
}
