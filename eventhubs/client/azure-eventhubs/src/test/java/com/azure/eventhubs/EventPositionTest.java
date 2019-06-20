// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.amqp.Retry;
import com.azure.core.util.logging.ClientLogger;
import com.azure.eventhubs.implementation.ApiTestBase;
import com.azure.eventhubs.implementation.ReactorHandlerProvider;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Locale;

import static java.nio.charset.StandardCharsets.UTF_8;

public class EventPositionTest extends ApiTestBase {
    private static final String PARTITION_ID = "0";
    private static final int NUMBER_OF_EVENTS = 10;

    private final ClientLogger logger = new ClientLogger(EventReceiverTest.class);

    private EventHubClient client;
    private EventSender sender;
    private EventReceiver receiver;

    @Rule
    public TestName testName = new TestName();

    @Override
    protected String testName() {
        return testName.getMethodName();
    }

    @Override
    protected void beforeTest() {
        logger.asInfo().log("[{}]: Performing test set-up.", testName.getMethodName());
        final EventSenderOptions senderOptions = new EventSenderOptions().partitionId(PARTITION_ID).retry(Retry.getNoRetry()).timeout(Duration.ofSeconds(30));
        final EventReceiverOptions receiverOptions = new EventReceiverOptions().prefetchCount(2).consumerGroup(getConsumerGroupName());
        final ReactorHandlerProvider handlerProvider = new ReactorHandlerProvider(getReactorProvider());
        client = new EventHubClient(getConnectionOptions(), getReactorProvider(), handlerProvider);
        sender = client.createSender(senderOptions);
        receiver = client.createReceiver(PARTITION_ID, EventPosition.earliest(), receiverOptions);
    }

    @Override
    protected void afterTest() {
        logger.asInfo().log("[{}]: Performing test clean-up.", testName.getMethodName());
        // TODO: close() will fail or hold the tests
//        if (client != null) {
//            client.close();
//        }

//        if (sender != null) {
//            try {
//                sender.close();
//            } catch (IOException e) {
//                logger.asError().log("[{}]: Sender doesn't close properly.", testName.getMethodName(), e);
//            }
//        }
//
//        if (receiver != null) {
//            try {
//                receiver.close();
//            } catch (IOException e) {
//                logger.asError().log("[{}]: Receiver doesn't close properly.", testName.getMethodName(), e);
//            }
//        }
    }

    /**
     * Test for receiving message from earliest offset
     */
    @Test
    public void receiveEarliestMessage() {
        skipIfNotRecordMode();
        // Act & Assert
        StepVerifier.create(receiver.receive().take(1))
            .expectNextCount(1)
            .verifyComplete();
    }

    /**
     * Test for receiving message from latest offset
     */
//        [AMQPConnection-6] INFO com.azure.eventhubs.implementation.handler.SendLinkHandler - onLinkFlow senderName[cbs], linkName[cbs:sender], unsettled[1], credit[99]
//        [AMQPConnection-6] INFO com.azure.eventhubs.implementation.handler.SendLinkHandler - onDelivery senderName[cbs], linkName[cbs:sender], unsettled[1], credit[99], deliveryState[Accepted{}], delivery.isBuffered[false], delivery.id[ecc096da9bda4897bcc3d68c693f0d30]
//        [AMQPConnection-6] INFO com.azure.eventhubs.implementation.ActiveClientTokenManager - Scheduling refresh token.
//[AMQPConnection-6] INFO com.azure.eventhubs.implementation.handler.ReceiveLinkHandler - onDelivery receiverName[cbs], linkName[cbs:receiver], updatedLinkCredit[0], remoteCredit[0], remoteCondition[Error{condition=null, description='null', info=null}], delivery.isPartial[false]
//        [AMQPConnection-6] INFO com.azure.eventhubs.implementation.handler.SendLinkHandler - onLinkLocalOpen senderName[PS_baf7ec_1560987086073], linkName[PS_baf7ec_1560987086073], localTarget[Target{address='conniey-test/Partitions/0', durable=NONE, expiryPolicy=SESSION_END, timeout=0, dynamic=false, dynamicNodeProperties=null, capabilities=null}]
//        [AMQPConnection-10] WARN com.azure.eventhubs.implementation.ReactorSender - Not connected. Not processing send work.
//        [AMQPConnection-7] INFO com.azure.eventhubs.implementation.handler.SendLinkHandler - onLinkRemoteOpen senderName[PS_baf7ec_1560987086073], linkName[PS_baf7ec_1560987086073], remoteTarget[Target{address='conniey-test/Partitions/0', durable=NONE, expiryPolicy=SESSION_END, timeout=0, dynamic=false, dynamicNodeProperties=null, capabilities=null}]
//        [AMQPConnection-7] WARN com.azure.eventhubs.implementation.ReactorSender - Connection state: ACTIVE
//[AMQPConnection-7] WARN com.azure.eventhubs.implementation.ReactorSender - Credits added: 1000
//        [AMQPConnection-7] INFO com.azure.eventhubs.implementation.handler.SendLinkHandler - onLinkFlow senderName[PS_baf7ec_1560987086073], linkName[PS_baf7ec_1560987086073], unsettled[0], credit[1000]
//        [AMQPConnection-9] WARN com.azure.eventhubs.implementation.ReactorSender - entityPath[conniey-test/Partitions/0], clinkName[PS_baf7ec_1560987086073], deliveryTag[57ec52456ac841199a4c79fbf66cb869]: Sent message
//[AMQPConnection-8] WARN com.azure.eventhubs.implementation.ReactorSender - Credits added: 999
//        [AMQPConnection-8] INFO com.azure.eventhubs.implementation.handler.SendLinkHandler - onLinkFlow senderName[PS_baf7ec_1560987086073], linkName[PS_baf7ec_1560987086073], unsettled[1], credit[999]
//        [AMQPConnection-9] INFO com.azure.eventhubs.implementation.handler.SendLinkHandler - onDelivery senderName[PS_baf7ec_1560987086073], linkName[PS_baf7ec_1560987086073], unsettled[1], credit[999], deliveryState[Accepted{}], delivery.isBuffered[false], delivery.id[57ec52456ac841199a4c79fbf66cb869]
//        [AMQPConnection-9] WARN com.azure.eventhubs.implementation.ReactorSender - entityPath[conniey-test/Partitions/0], clinkName[PS_baf7ec_1560987086073], deliveryTag[57ec52456ac841199a4c79fbf66cb869]: process delivered message
//[AMQPConnection-4] INFO com.azure.eventhubs.implementation.handler.ConnectionHandler - onConnectionRemoteOpen hostname[event-hubs-1.servicebus.windows.net:5671], connectionId[MF_19a49e_1560987085718], remoteContainer[69a0dbbb0bf24309ba1c191108d8236c_G11]
//        [AMQPConnection-3] INFO com.azure.eventhubs.implementation.handler.ConnectionHandler - onConnectionRemoteClose hostname[event-hubs-1.servicebus.windows.net:5671], connectionId[MF_19a49e_1560987085718], errorCondition[amqp:connection:forced], errorDescription[The connection was inactive for more than the allowed 60000 milliseconds and is closed by container 'LinkTracker'. TrackingId:547d31b640a749f58dff23017f768834_G1, SystemTracker:gateway5, Timestamp:2019-06-19T23:32:27]
//
    @Ignore("Connection closed but test keeping running ")
    @Test
    public void receiveLatestMessage() {
        skipIfNotRecordMode();
        // Arrange
        final EventReceiverOptions receiverOptions = new EventReceiverOptions().consumerGroup(getConsumerGroupName());
        receiver = client.createReceiver(PARTITION_ID, EventPosition.latest(), receiverOptions);

        // Act & Assert
        StepVerifier.create(receiver.receive().take(1))
            .then(() -> sender.send(new EventData("testString".getBytes(UTF_8))).block())
            .expectNextCount(1)
            .verifyComplete();
    }

    /**
     * Test for receiving messages start at enqueued time
     */
    @Test
    public void receiveMessageFromEnqueuedTime() {
        skipIfNotRecordMode();
        final EventPosition enqueuedTimeEventPosition = EventPosition.fromEnqueuedTime(Instant.EPOCH);
        receiveMessageHelper(enqueuedTimeEventPosition, 1);
    }

    /**
     * Test for receiving from start of stream
     */
    @Ignore
    @Test
    public void testReceiverStartOfStreamFilters() {
        skipIfNotRecordMode();

        final EventPosition earliestEventPosition = EventPosition.earliest();
        final EventPosition enqueuedTimeEventPosition = EventPosition.fromEnqueuedTime(Instant.EPOCH);
        final Flux<EventData> earliestOffsetReceivedData = receiveMessageHelper(earliestEventPosition, NUMBER_OF_EVENTS);
        final Flux<EventData> enqueuedTimeReceivedData = receiveMessageHelper(enqueuedTimeEventPosition, NUMBER_OF_EVENTS);

        //Assert
        Iterable<EventData> earliestOffsetDataIterable = earliestOffsetReceivedData.toIterable();
        Iterator<EventData> enqueuedTimeDataIterator = enqueuedTimeReceivedData.toIterable().iterator();

        for (EventData offsetData : earliestOffsetDataIterable) {
            if (!enqueuedTimeDataIterator.hasNext()) {
                break;
            }
            EventData dateTimeEventData = enqueuedTimeDataIterator.next();
            // Check if both received data has matched offset
            Assert.assertTrue(
                String.format(Locale.US, "START_OF_STREAM offset: %s, EPOCH offset: %s", offsetData.offset(), dateTimeEventData.offset()),
                offsetData.offset().equalsIgnoreCase(dateTimeEventData.offset()));
        }
    }

    /**
     * Test a receiver with inclusive offset
     */
    @Ignore
    @Test
    public void testReceiverOffsetInclusiveFilter() {
        skipIfNotRecordMode();
        // Arrange
        final EventPosition enqueuedTimeEventPosition = EventPosition.fromEnqueuedTime(Instant.EPOCH);
        final EventData enqueuedTimeEvent = receiveMessageHelper(enqueuedTimeEventPosition, 1).toIterable().iterator().next();

        final EventPosition offsetEventPosition = EventPosition.fromOffset(enqueuedTimeEvent.offset(), true);
        final EventData offsetEvent = receiveMessageHelper(offsetEventPosition, 1).toIterable().iterator().next();
        // Assert
        Assert.assertEquals(offsetEvent.offset(), enqueuedTimeEvent.offset());
        Assert.assertEquals(offsetEvent.sequenceNumber(), enqueuedTimeEvent.sequenceNumber());
    }

    /**
     * Test for receiving offset without inclusive filter
     */
    @Ignore
    @Test
    public void testReceiverOffsetNonInclusiveFilter() {
        skipIfNotRecordMode();

        // Arrange
        final EventPosition enqueuedTimeEventPosition = EventPosition.fromEnqueuedTime(Instant.EPOCH);
        final EventData enqueuedTimeEvent = receiveMessageHelper(enqueuedTimeEventPosition, 1).toIterable().iterator().next();

        final EventPosition offsetEventPosition = EventPosition.fromOffset(enqueuedTimeEvent.offset(), false);
        final EventData offsetEvent = receiveMessageHelper(offsetEventPosition, 1).toIterable().iterator().next();

        // Assert
        Assert.assertEquals(offsetEvent.sequenceNumber(), enqueuedTimeEvent.sequenceNumber() + 1);
    }

    /**
     * Test for receiving sequence number with inclusive filter
     */
    @Ignore
    @Test
    public void testReceiverSequenceNumberInclusiveFilter() {
        skipIfNotRecordMode();

        // Arrange
        final EventPosition enqueuedTimeEventPosition = EventPosition.fromEnqueuedTime(Instant.EPOCH);
        final EventData enqueuedTimeEvent = receiveMessageHelper(enqueuedTimeEventPosition, 1).toIterable().iterator().next();

        final EventPosition nextSequenceNumberEventPosition = EventPosition.fromSequenceNumber(enqueuedTimeEvent.sequenceNumber(), true);
        final EventData sequenceNumEvent = receiveMessageHelper(nextSequenceNumberEventPosition, 1).toIterable().iterator().next();

        // Assert
        Assert.assertEquals(sequenceNumEvent.offset(), enqueuedTimeEvent.offset());
        Assert.assertEquals(sequenceNumEvent.sequenceNumber(), enqueuedTimeEvent.sequenceNumber());
    }

    /**
     * Test for receiving sequence number without inclusive filter
     */
    @Ignore
    @Test
    public void sequenceNumberNonInclusiveFilterFromEnqueuedTime() {
        skipIfNotRecordMode();

        // Arrange
        final EventPosition enqueuedTimeEventPosition = EventPosition.fromEnqueuedTime(Instant.EPOCH);
        final EventData enqueuedTimeEvent = receiveMessageHelper(enqueuedTimeEventPosition, 1).toIterable().iterator().next();

        final EventPosition nextSequenceNumberEventPosition = EventPosition.fromSequenceNumber(enqueuedTimeEvent.sequenceNumber(), false);
        final EventData sequenceNumberEvent = receiveMessageHelper(nextSequenceNumberEventPosition, 1).toIterable().iterator().next();

        // Assert
        Assert.assertEquals(sequenceNumberEvent.sequenceNumber(), enqueuedTimeEvent.sequenceNumber() + 1);
    }


    private Flux<EventData> receiveMessageHelper(EventPosition eventPosition, int numberOfEvents) {
        receiver = client.createReceiver(PARTITION_ID, eventPosition,
            new EventReceiverOptions().consumerGroup(getConsumerGroupName()));
        final Flux<EventData> receivedData = receiver.receive();
        // Act
        StepVerifier.create(receivedData.take(numberOfEvents))
            .expectNextCount(numberOfEvents)
            .verifyComplete();

        return receivedData;
    }
}
