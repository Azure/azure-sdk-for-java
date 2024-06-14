package com.azure.messaging.eventgrid.namespaces;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.models.CloudEvent;
import com.azure.core.models.CloudEventDataFormat;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.eventgrid.namespaces.models.AcknowledgeResult;
import com.azure.messaging.eventgrid.namespaces.models.ReceiveResult;
import com.azure.messaging.eventgrid.namespaces.models.RejectResult;
import com.azure.messaging.eventgrid.namespaces.models.ReleaseResult;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;

public class EventGridReceiverClientTests extends EventGridClientTestBase {


    EventGridReceiverClient buildReceiverAsyncClient(boolean useManagedIdentity) {
        if (useManagedIdentity) {
            return receiverBuilder.credential(new DefaultAzureCredentialBuilder().build()).buildClient();
        }
        return receiverBuilder.credential(getKey(EVENTGRID_KEY)).buildClient();
    }

    EventGridSenderClient buildSenderAsyncClient(boolean useManagedIdentity) {
        if (useManagedIdentity) {
            return senderBuilder.credential(new DefaultAzureCredentialBuilder().build()).buildClient();
        }
        return senderBuilder.credential(getKey(EVENTGRID_KEY)).buildClient();
    }
//
//
//    @Test
//    void publishCloudEventSync() {
//        EventGridClient client = buildSyncClient();
//        client.publishCloudEvent(TOPIC_NAME, getCloudEvent());
//    }
//
//    @Test
//    void publishCloudEventBinaryModeSync() {
//        EventGridClient client = buildSyncClient();
//        CloudEvent event = new CloudEvent("/microsoft/testEvent", "Microsoft.MockPublisher.TestEvent",
//            BinaryData.fromString("MyCoolString"), CloudEventDataFormat.BYTES, "text/plain")
//            .setSubject("Test")
//            .setTime(testResourceNamer.now())
//            .setId(testResourceNamer.randomUuid());
//        client.publishCloudEvent(TOPIC_NAME, event, true);
//    }
//
//    @Test
//    void publishBatchOfCloudEventsSync() {
//        EventGridClient client = buildSyncClient();
//        client.publishCloudEvents(TOPIC_NAME, Arrays.asList(getCloudEvent(), getCloudEvent()));
//    }
//
//    @Test
//    void receiveBatchOfCloudEventsSync() {
//        EventGridClient client = buildSyncClient();
//        client.publishCloudEvent(TOPIC_NAME, getCloudEvent());
//        ReceiveResult result = client.receiveCloudEvents(TOPIC_NAME, EVENT_SUBSCRIPTION_NAME, 10, Duration.ofSeconds(10));
//        assertNotNull(result);
//        assertFalse(result.getValue().isEmpty());
//    }
//
//    @Test
//    void acknowledgeBatchOfCloudEventsSync() {
//        EventGridClient client = buildSyncClient();
//        client.publishCloudEvent(TOPIC_NAME, getCloudEvent());
//        ReceiveResult receiveResult = client.receiveCloudEvents(TOPIC_NAME, EVENT_SUBSCRIPTION_NAME, 1, Duration.ofSeconds(10));
//        AcknowledgeOptions acknowledgeOptions = new AcknowledgeOptions(Arrays.asList(receiveResult.getValue().get(0).getBrokerProperties().getLockToken()));
//        AcknowledgeResult acknowledgeResult = client.acknowledgeCloudEvents(TOPIC_NAME, EVENT_SUBSCRIPTION_NAME, acknowledgeOptions);
//        assertNotNull(acknowledgeResult);
//        assertFalse(acknowledgeResult.getSucceededLockTokens().isEmpty());
//    }
//
//    @Test
//    void releaseBatchOfCloudEventsSync() {
//        EventGridClient client = buildSyncClient();
//        client.publishCloudEvent(TOPIC_NAME, getCloudEvent());
//        ReceiveResult receiveResult = client.receiveCloudEvents(TOPIC_NAME, EVENT_SUBSCRIPTION_NAME, 1, Duration.ofSeconds(10));
//        ReleaseOptions releaseOptions = new ReleaseOptions(Collections.singletonList(receiveResult.getValue().get(0).getBrokerProperties().getLockToken()));
//        ReleaseResult releaseResult = client.releaseCloudEvents(TOPIC_NAME, EVENT_SUBSCRIPTION_NAME, releaseOptions);
//        assertNotNull(releaseResult);
//        assertFalse(releaseResult.getSucceededLockTokens().isEmpty());
//    }
//
////    @Test
////    void releaseBatchOfCloudEventsWithDelaySync() {
////        EventGridClient client = buildSyncClient();
////        client.publishCloudEvent(TOPIC_NAME, getCloudEvent());
////        ReceiveResult receiveResult = client.receiveCloudEvents(TOPIC_NAME, EVENT_SUBSCRIPTION_NAME, 1, Duration.ofSeconds(10));
////        ReleaseOptions releaseOptions = new ReleaseOptions(Collections.singletonList(receiveResult.getValue().get(0).getBrokerProperties().getLockToken()));
////        ReleaseResult releaseResult = client.releaseCloudEvents(TOPIC_NAME, EVENT_SUBSCRIPTION_NAME, releaseOptions, ReleaseDelay.BY10_SECONDS);
////        assertNotNull(releaseResult);
////        assertFalse(releaseResult.getSucceededLockTokens().isEmpty());
////    }
//
//    @Test
//    void rejectBatchOfCloudEventsSync() {
//        EventGridClient client = buildSyncClient();
//        client.publishCloudEvent(TOPIC_NAME, getCloudEvent());
//        ReceiveResult receiveResult = client.receiveCloudEvents(TOPIC_NAME, EVENT_SUBSCRIPTION_NAME, 1, Duration.ofSeconds(10));
//        RejectOptions rejectOptions = new RejectOptions(Collections.singletonList(receiveResult.getValue().get(0).getBrokerProperties().getLockToken()));
//        RejectResult rejectResult = client.rejectCloudEvents(TOPIC_NAME, EVENT_SUBSCRIPTION_NAME, rejectOptions);
//        assertNotNull(rejectResult);
//        assertFalse(rejectResult.getSucceededLockTokens().isEmpty());
//    }
//
////    @Test
////    void renewBatchOfEventsSync() {
////        EventGridClient client = buildSyncClient();
////        client.publishCloudEvent(TOPIC_NAME, getCloudEvent());
////        ReceiveResult receiveResult = client.receiveCloudEvents(TOPIC_NAME, EVENT_SUBSCRIPTION_NAME, 1, Duration.ofSeconds(10));
////        RenewLockOptions options = new RenewLockOptions(Collections.singletonList(receiveResult.getValue().get(0).getBrokerProperties().getLockToken()));
////        RenewCloudEventLocksResult renewResult =  client.renewCloudEventLocks(TOPIC_NAME, EVENT_SUBSCRIPTION_NAME, options);
////        assertNotNull(renewResult);
////        assertFalse(renewResult.getSucceededLockTokens().isEmpty());
////    }
//
//    @Test
//    void publishCloudEvent() {
//        EventGridAsyncClient client = buildAsyncClient();
//        StepVerifier.create(client.publishCloudEvent(TOPIC_NAME, getCloudEvent()))
//            .verifyComplete();
//    }
//
//    @Test
//    void publishCloudEventBinaryMode() {
//        EventGridAsyncClient client = buildAsyncClient();
//        CloudEvent event = new CloudEvent("/microsoft/testEvent", "Microsoft.MockPublisher.TestEvent",
//            BinaryData.fromString("MyCoolString"), CloudEventDataFormat.BYTES, "text/plain")
//            .setSubject("Test")
//            .setTime(testResourceNamer.now())
//            .setId(testResourceNamer.randomUuid());
//        StepVerifier.create(client.publishCloudEvent(TOPIC_NAME, event, true))
//            .verifyComplete();
//    }
//
//    @Test
//    void publishBatchOfCloudEvents() {
//        EventGridAsyncClient client = buildAsyncClient();
//        StepVerifier.create(client.publishCloudEvents(TOPIC_NAME, Arrays.asList(getCloudEvent(), getCloudEvent())))
//            .verifyComplete();
//    }
//
//    @Test
//    void receiveBatchOfCloudEvents() {
//        EventGridAsyncClient client = buildAsyncClient();
//        client.publishCloudEvent(TOPIC_NAME, getCloudEvent()).block();
//        StepVerifier.create(client.receiveCloudEvents(TOPIC_NAME, EVENT_SUBSCRIPTION_NAME, 10, Duration.ofSeconds(10)))
//            .assertNext(receiveResult -> {
//                assertNotNull(receiveResult);
//                assertFalse(receiveResult.getValue().isEmpty());
//            })
//            .verifyComplete();
//    }
//
//    @Test
//    void acknowledgeBatchOfCloudEvents() {
//        EventGridAsyncClient client = buildAsyncClient();
//        client.publishCloudEvent(TOPIC_NAME, getCloudEvent()).block();
//        client.receiveCloudEvents(TOPIC_NAME, EVENT_SUBSCRIPTION_NAME, 1, Duration.ofSeconds(10))
//            .flatMap(receiveResult -> {
//                AcknowledgeOptions acknowledgeOptions = new AcknowledgeOptions(Arrays.asList(receiveResult.getValue().get(0).getBrokerProperties().getLockToken()));
//                return client.acknowledgeCloudEvents(TOPIC_NAME, EVENT_SUBSCRIPTION_NAME, acknowledgeOptions);
//            })
//            .as(StepVerifier::create)
//            .assertNext(acknowledgeResult -> {
//                assertNotNull(acknowledgeResult);
//                assertFalse(acknowledgeResult.getSucceededLockTokens().isEmpty());
//            })
//            .verifyComplete();
//    }
//
//    @Test
//    void releaseBatchOfCloudEvents() {
//        EventGridAsyncClient client = buildAsyncClient();
//        client.publishCloudEvent(TOPIC_NAME, getCloudEvent()).block();
//        client.receiveCloudEvents(TOPIC_NAME, EVENT_SUBSCRIPTION_NAME, 1, Duration.ofSeconds(10))
//            .flatMap(receiveResult -> {
//                ReleaseOptions releaseOptions = new ReleaseOptions(Arrays.asList(receiveResult.getValue().get(0).getBrokerProperties().getLockToken()));
//                return client.releaseCloudEvents(TOPIC_NAME, EVENT_SUBSCRIPTION_NAME, releaseOptions);
//            })
//            .as(StepVerifier::create)
//            .assertNext(releaseResult -> {
//                assertNotNull(releaseResult);
//                assertFalse(releaseResult.getSucceededLockTokens().isEmpty());
//            })
//            .verifyComplete();
//    }
//
////    @Test
////    void releaseBatchOfCloudEventsWithDelay() {
////        EventGridAsyncClient client = buildAsyncClient();
////        client.publishCloudEvent(TOPIC_NAME, getCloudEvent()).block();
////        client.receiveCloudEvents(TOPIC_NAME, EVENT_SUBSCRIPTION_NAME, 1, Duration.ofSeconds(10))
////            .flatMap(receiveResult -> {
////                ReleaseOptions releaseOptions = new ReleaseOptions(Arrays.asList(receiveResult.getValue().get(0).getBrokerProperties().getLockToken()));
////                return client.releaseCloudEvents(TOPIC_NAME, EVENT_SUBSCRIPTION_NAME, releaseOptions, ReleaseDelay.BY10_SECONDS);
////            })
////            .as(StepVerifier::create)
////            .assertNext(releaseResult -> {
////                assertNotNull(releaseResult);
////                assertFalse(releaseResult.getSucceededLockTokens().isEmpty());
////            })
////            .verifyComplete();
////    }
//
//    @Test
//    void rejectBatchOfCloudEvents() {
//        EventGridAsyncClient client = buildAsyncClient();
//        client.publishCloudEvent(TOPIC_NAME, getCloudEvent()).block();
//        client.receiveCloudEvents(TOPIC_NAME, EVENT_SUBSCRIPTION_NAME, 1, Duration.ofSeconds(10))
//            .flatMap(receiveResult -> {
//                RejectOptions rejectOptions = new RejectOptions(Arrays.asList(receiveResult.getValue().get(0).getBrokerProperties().getLockToken()));
//                return client.rejectCloudEvents(TOPIC_NAME, EVENT_SUBSCRIPTION_NAME, rejectOptions);
//            })
//            .as(StepVerifier::create)
//            .assertNext(rejectResult -> {
//                assertNotNull(rejectResult);
//                assertFalse(rejectResult.getSucceededLockTokens().isEmpty());
//            })
//            .verifyComplete();
//    }
//
////    @Test
////    void renewBatchOfCloudEvents() {
////        EventGridAsyncClient client = buildAsyncClient();
////        client.publishCloudEvent(TOPIC_NAME, getCloudEvent()).block();
////        client.receiveCloudEvents(TOPIC_NAME, EVENT_SUBSCRIPTION_NAME, 1, Duration.ofSeconds(10))
////            .flatMap(receiveResult -> {
////                RenewLockOptions options = new RenewLockOptions(Arrays.asList(receiveResult.getValue().get(0).getBrokerProperties().getLockToken()));
////                return client.renewCloudEventLocks(TOPIC_NAME, EVENT_SUBSCRIPTION_NAME, options);
////            })
////            .as(StepVerifier::create)
////            .assertNext(renewResult -> {
////                assertNotNull(renewResult);
////                assertFalse(renewResult.getSucceededLockTokens().isEmpty());
////            })
////            .verifyComplete();
////    }
}
