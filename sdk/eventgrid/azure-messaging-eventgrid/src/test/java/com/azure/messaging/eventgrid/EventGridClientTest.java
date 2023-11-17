// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.models.CloudEvent;
import com.azure.core.models.CloudEventDataFormat;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.messaging.eventgrid.models.AcknowledgeOptions;
import com.azure.messaging.eventgrid.models.AcknowledgeResult;
import com.azure.messaging.eventgrid.models.ReceiveResult;
import com.azure.messaging.eventgrid.models.RejectOptions;
import com.azure.messaging.eventgrid.models.RejectResult;
import com.azure.messaging.eventgrid.models.ReleaseDelay;
import com.azure.messaging.eventgrid.models.ReleaseOptions;
import com.azure.messaging.eventgrid.models.ReleaseResult;
import com.azure.messaging.eventgrid.models.RenewCloudEventLocksResult;
import com.azure.messaging.eventgrid.models.RenewLockOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@Disabled("recording for new feature not done yet")
class EventGridClientTest extends EventGridTestBase {



    public static final String TOPIC_NAME = Configuration.getGlobalConfiguration().get(EVENTGRID_TOPIC_NAME);
    public static final String EVENT_SUBSCRIPTION_NAME = Configuration.getGlobalConfiguration().get(EVENTGRID_EVENT_SUBSCRIPTION_NAME);


    EventGridClientBuilder buildClientBuilder() {
        return new EventGridClientBuilder()
            .httpClient(HttpClient.createDefault())
            .httpLogOptions(new HttpLogOptions())
            .endpoint(Configuration.getGlobalConfiguration().get(EVENTGRID_ENDPOINT))
            .serviceVersion(EventGridMessagingServiceVersion.V2023_10_01_PREVIEW)
            .credential(getKey(EVENTGRID_KEY));
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
        client.publishCloudEvent(TOPIC_NAME, getCloudEvent());
    }

    @Test
    void publishCloudEventBinaryModeSync() {
        EventGridClient client = buildClient();
        CloudEvent event = new CloudEvent("/microsoft/testEvent", "Microsoft.MockPublisher.TestEvent",
            BinaryData.fromObject(new HashMap<String, String>() {
                {
                    put("Field1", "Value1");
                    put("Field2", "Value2");
                    put("Field3", "Value3");
                }
            }), CloudEventDataFormat.JSON, "text/plain")
            .setSubject("Test")
            .setTime(testResourceNamer.now())
            .setId(testResourceNamer.randomUuid());
        client.publishCloudEvent(TOPIC_NAME, event, true);
    }

    @Test
    void publishBatchOfCloudEventsSync() {
        EventGridClient client = buildClient();
        client.publishCloudEvents(TOPIC_NAME, Arrays.asList(getCloudEvent(), getCloudEvent()));
    }

    @Test
    void receiveBatchOfCloudEventsSync() {
        EventGridClient client = buildClient();
        client.publishCloudEvent(TOPIC_NAME, getCloudEvent());
        ReceiveResult result = client.receiveCloudEvents(TOPIC_NAME, EVENT_SUBSCRIPTION_NAME, 10, Duration.ofSeconds(10));
        assertNotNull(result);
        assertFalse(result.getValue().isEmpty());
    }

    @Test
    void acknowledgeBatchOfCloudEventsSync() {
        EventGridClient client = buildClient();
        client.publishCloudEvent(TOPIC_NAME, getCloudEvent());
        ReceiveResult receiveResult = client.receiveCloudEvents(TOPIC_NAME, EVENT_SUBSCRIPTION_NAME, 1, Duration.ofSeconds(10));
        AcknowledgeOptions acknowledgeOptions = new AcknowledgeOptions(Arrays.asList(receiveResult.getValue().get(0).getBrokerProperties().getLockToken()));
        AcknowledgeResult acknowledgeResult = client.acknowledgeCloudEvents(TOPIC_NAME, EVENT_SUBSCRIPTION_NAME, acknowledgeOptions);
        assertNotNull(acknowledgeResult);
        assertFalse(acknowledgeResult.getSucceededLockTokens().isEmpty());
    }

    @Test
    void releaseBatchOfCloudEventsSync() {
        EventGridClient client = buildClient();
        client.publishCloudEvent(TOPIC_NAME, getCloudEvent());
        ReceiveResult receiveResult = client.receiveCloudEvents(TOPIC_NAME, EVENT_SUBSCRIPTION_NAME, 1, Duration.ofSeconds(10));
        ReleaseOptions releaseOptions = new ReleaseOptions(Arrays.asList(receiveResult.getValue().get(0).getBrokerProperties().getLockToken()));
        ReleaseResult releaseResult = client.releaseCloudEvents(TOPIC_NAME, EVENT_SUBSCRIPTION_NAME, releaseOptions);
        assertNotNull(releaseResult);
        assertFalse(releaseResult.getSucceededLockTokens().isEmpty());
    }

    @Test
    void releaseBatchOfCloudEventsWithDelaySync() {
        EventGridClient client = buildClient();
        client.publishCloudEvent(TOPIC_NAME, getCloudEvent());
        ReceiveResult receiveResult = client.receiveCloudEvents(TOPIC_NAME, EVENT_SUBSCRIPTION_NAME, 1, Duration.ofSeconds(10));
        ReleaseOptions releaseOptions = new ReleaseOptions(Arrays.asList(receiveResult.getValue().get(0).getBrokerProperties().getLockToken()));
        ReleaseResult releaseResult = client.releaseCloudEvents(TOPIC_NAME, EVENT_SUBSCRIPTION_NAME, releaseOptions, ReleaseDelay.BY10_SECONDS);
        assertNotNull(releaseResult);
        assertFalse(releaseResult.getSucceededLockTokens().isEmpty());
    }

    @Test
    void rejectBatchOfCloudEventsSync() {
        EventGridClient client = buildClient();
        client.publishCloudEvent(TOPIC_NAME, getCloudEvent());
        ReceiveResult receiveResult = client.receiveCloudEvents(TOPIC_NAME, EVENT_SUBSCRIPTION_NAME, 1, Duration.ofSeconds(10));
        RejectOptions rejectOptions = new RejectOptions(Arrays.asList(receiveResult.getValue().get(0).getBrokerProperties().getLockToken()));
        RejectResult rejectResult = client.rejectCloudEvents(TOPIC_NAME, EVENT_SUBSCRIPTION_NAME, rejectOptions);
        assertNotNull(rejectResult);
        assertFalse(rejectResult.getSucceededLockTokens().isEmpty());
    }

    @Test
    void renewBatchOfEventsSync() {
        EventGridClient client = buildClient();
        client.publishCloudEvent(TOPIC_NAME, getCloudEvent());
        ReceiveResult receiveResult = client.receiveCloudEvents(TOPIC_NAME, EVENT_SUBSCRIPTION_NAME, 1, Duration.ofSeconds(10));
        RenewLockOptions options = new RenewLockOptions(Arrays.asList(receiveResult.getValue().get(0).getBrokerProperties().getLockToken()));
        RenewCloudEventLocksResult renewResult =  client.renewCloudEventLocks(TOPIC_NAME, EVENT_SUBSCRIPTION_NAME, options);
        assertNotNull(renewResult);
        assertFalse(renewResult.getSucceededLockTokens().isEmpty());
    }

    @Test
    void publishCloudEvent() {
        EventGridAsyncClient client = buildAsyncClient();
        StepVerifier.create(client.publishCloudEvent(TOPIC_NAME, getCloudEvent()))
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();
    }

    @Test
    void publishCloudEventBinaryMode() {
        EventGridAsyncClient client = buildAsyncClient();
        CloudEvent event = new CloudEvent("/microsoft/testEvent", "Microsoft.MockPublisher.TestEvent",
            BinaryData.fromObject(new HashMap<String, String>() {
                {
                    put("Field1", "Value1");
                    put("Field2", "Value2");
                    put("Field3", "Value3");
                }
            }), CloudEventDataFormat.JSON, "text/plain")
            .setSubject("Test")
            .setTime(testResourceNamer.now())
            .setId(testResourceNamer.randomUuid());
        StepVerifier.create(client.publishCloudEvent(TOPIC_NAME, event, true))
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();
    }

    @Test
    void publishBatchOfCloudEvents() {
        EventGridAsyncClient client = buildAsyncClient();
        StepVerifier.create(client.publishCloudEvents(TOPIC_NAME, Arrays.asList(getCloudEvent(), getCloudEvent())))
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();
    }

    @Test
    void receiveBatchOfCloudEvents() {
        EventGridAsyncClient client = buildAsyncClient();
        client.publishCloudEvent(TOPIC_NAME, getCloudEvent()).block();
        StepVerifier.create(client.receiveCloudEvents(TOPIC_NAME, EVENT_SUBSCRIPTION_NAME, 10, Duration.ofSeconds(10)))
            .assertNext(receiveResult -> {
                assertNotNull(receiveResult);
                assertFalse(receiveResult.getValue().isEmpty());
            })
            .verifyComplete();
    }

    @Test
    void acknowledgeBatchOfCloudEvents() {
        EventGridAsyncClient client = buildAsyncClient();
        client.publishCloudEvent(TOPIC_NAME, getCloudEvent()).block();
        client.receiveCloudEvents(TOPIC_NAME, EVENT_SUBSCRIPTION_NAME, 1, Duration.ofSeconds(10))
            .flatMap(receiveResult -> {
                AcknowledgeOptions acknowledgeOptions = new AcknowledgeOptions(Arrays.asList(receiveResult.getValue().get(0).getBrokerProperties().getLockToken()));
                return client.acknowledgeCloudEvents(TOPIC_NAME, EVENT_SUBSCRIPTION_NAME, acknowledgeOptions);
            })
            .as(StepVerifier::create)
            .assertNext(acknowledgeResult -> {
                assertNotNull(acknowledgeResult);
                assertFalse(acknowledgeResult.getSucceededLockTokens().isEmpty());
            })
            .verifyComplete();
    }

    @Test
    void releaseBatchOfCloudEvents() {
        EventGridAsyncClient client = buildAsyncClient();
        client.publishCloudEvent(TOPIC_NAME, getCloudEvent()).block();
        client.receiveCloudEvents(TOPIC_NAME, EVENT_SUBSCRIPTION_NAME, 1, Duration.ofSeconds(10))
            .flatMap(receiveResult -> {
                ReleaseOptions releaseOptions = new ReleaseOptions(Arrays.asList(receiveResult.getValue().get(0).getBrokerProperties().getLockToken()));
                return client.releaseCloudEvents(TOPIC_NAME, EVENT_SUBSCRIPTION_NAME, releaseOptions);
            })
            .as(StepVerifier::create)
            .assertNext(releaseResult -> {
                assertNotNull(releaseResult);
                assertFalse(releaseResult.getSucceededLockTokens().isEmpty());
            })
            .verifyComplete();
    }

    @Test
    void releaseBatchOfCloudEventsWithDelay() {
        EventGridAsyncClient client = buildAsyncClient();
        client.publishCloudEvent(TOPIC_NAME, getCloudEvent()).block();
        client.receiveCloudEvents(TOPIC_NAME, EVENT_SUBSCRIPTION_NAME, 1, Duration.ofSeconds(10))
            .flatMap(receiveResult -> {
                ReleaseOptions releaseOptions = new ReleaseOptions(Arrays.asList(receiveResult.getValue().get(0).getBrokerProperties().getLockToken()));
                return client.releaseCloudEvents(TOPIC_NAME, EVENT_SUBSCRIPTION_NAME, releaseOptions, ReleaseDelay.BY10_SECONDS);
            })
            .as(StepVerifier::create)
            .assertNext(releaseResult -> {
                assertNotNull(releaseResult);
                assertFalse(releaseResult.getSucceededLockTokens().isEmpty());
            })
            .verifyComplete();
    }

    @Test
    void rejectBatchOfCloudEvents() {
        EventGridAsyncClient client = buildAsyncClient();
        client.publishCloudEvent(TOPIC_NAME, getCloudEvent()).block();
        client.receiveCloudEvents(TOPIC_NAME, EVENT_SUBSCRIPTION_NAME, 1, Duration.ofSeconds(10))
            .flatMap(receiveResult -> {
                RejectOptions rejectOptions = new RejectOptions(Arrays.asList(receiveResult.getValue().get(0).getBrokerProperties().getLockToken()));
                return client.rejectCloudEvents(TOPIC_NAME, EVENT_SUBSCRIPTION_NAME, rejectOptions);
            })
            .as(StepVerifier::create)
            .assertNext(rejectResult -> {
                assertNotNull(rejectResult);
                assertFalse(rejectResult.getSucceededLockTokens().isEmpty());
            })
            .verifyComplete();
    }

    @Test
    void renewBatchOfCloudEvents() {
        EventGridAsyncClient client = buildAsyncClient();
        client.publishCloudEvent(TOPIC_NAME, getCloudEvent()).block();
        client.receiveCloudEvents(TOPIC_NAME, EVENT_SUBSCRIPTION_NAME, 1, Duration.ofSeconds(10))
            .flatMap(receiveResult -> {
                RenewLockOptions options = new RenewLockOptions(Arrays.asList(receiveResult.getValue().get(0).getBrokerProperties().getLockToken()));
                return client.renewCloudEventLocks(TOPIC_NAME, EVENT_SUBSCRIPTION_NAME, options);
            })
            .as(StepVerifier::create)
            .assertNext(renewResult -> {
                assertNotNull(renewResult);
                assertFalse(renewResult.getSucceededLockTokens().isEmpty());
            })
            .verifyComplete();
    }
}
