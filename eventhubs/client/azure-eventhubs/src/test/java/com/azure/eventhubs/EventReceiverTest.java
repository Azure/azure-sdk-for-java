// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.amqp.MessageConstant;
import com.azure.core.implementation.logging.ServiceLogger;
import com.azure.eventhubs.implementation.ApiTestBase;
import com.azure.eventhubs.implementation.ReactorHandlerProvider;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class EventReceiverTest extends ApiTestBase {
    private final ServiceLogger logger = new ServiceLogger(EventHubClientTest.class);

    private static final String CONSUMER_GROUP_NAME = ApiTestBase.getConsumerGroupName();
    private static final String PARTITION_ID = "0";
    private static final String PAYLOAD = "TestMessage1";
    private static final String PROPERTY1 = "property1";
    private static final String PROPERTY_VALUE1 = "something1";
    private static final String PROPERTY2 = MessageConstant.MESSAGE_ID.getValue(); // TODO: need to verify it
    private static final String PROPERTY_VALUE2 = "something2";
    private static final int NUM_OF_EVENTS = 10;

    private EventData resendEventData;
    private EventHubClient client;
    private EventSenderOptions senderOptions;
    private ReactorHandlerProvider handlerProvider;

    @Rule
    public TestName testName = new TestName();

    @Override
    protected String testName() {
        return testName.getMethodName();
    }

    @Override
    protected void beforeTest() {
        logger.asInfo().log("[{}]: Performing test set-up.", testName.getMethodName());

        handlerProvider = new ReactorHandlerProvider(getReactorProvider());
        client = new EventHubClient(getConnectionOptions(), getReactorProvider(), handlerProvider);
        senderOptions = new EventSenderOptions().partitionId(PARTITION_ID);
    }

    @Override
    protected void afterTest() {
        logger.asInfo().log("[{}]: Performing test clean-up.", testName.getMethodName());

        if (client != null) {
            client.close();
        }
    }

    /**
     *  Test for a simple receiving message
     */
    @Test
    public void receiveMessage() {
        skipIfNotRecordMode();

        // Arrange
        final EventReceiverOptions options = new EventReceiverOptions()
            .prefetchCount(5).consumerGroup(CONSUMER_GROUP_NAME);
        final EventReceiver receiver = client.createReceiver(PARTITION_ID, EventPosition.earliest(), options);

        // Act & Assert
        StepVerifier.create(receiver.receive().take(NUM_OF_EVENTS))
            .expectNextCount(NUM_OF_EVENTS)
            .expectComplete()
            .verify();
    }

    /**
     * Test for receiving from start of stream
     */
    @Ignore
    @Test
    public void testReceiverStartOfStreamFilters() {
        skipIfNotRecordMode();
//        ApiTestBase.pushEventsToPartition(client, PARTITION_ID, 25);

        // Arrange
        final EventReceiver offsetReceiver = client.createReceiver(PARTITION_ID, EventPosition.earliest(),
            new EventReceiverOptions().consumerGroup(getConsumerGroupName()));
        final Flux<EventData> offsetReceivedData = offsetReceiver.receive();
        // Act & Assert
        StepVerifier.create(offsetReceivedData.take(NUM_OF_EVENTS))
            .expectNextCount(NUM_OF_EVENTS)
            .expectComplete()
            .verify();

        // Arrange
        final EventReceiver dateTimeReceiver = client.createReceiver(PARTITION_ID, EventPosition.fromEnqueuedTime(Instant.EPOCH),
            new EventReceiverOptions().consumerGroup(getConsumerGroupName()));
        final Flux<EventData> dateTimeReceivedData = dateTimeReceiver.receive();
        // Act & Assert
        StepVerifier.create(dateTimeReceivedData.take(NUM_OF_EVENTS))
            .expectNextCount(NUM_OF_EVENTS)
            .expectComplete()
            .verify();

        Iterator<EventData> dateTimeDataIterator = dateTimeReceivedData.toIterable().iterator();
        Iterable<EventData> offsetDataIterable = offsetReceivedData.toIterable();

        // Act & Assert
        for (EventData offsetData : offsetDataIterable) {
            if (!dateTimeDataIterator.hasNext()) {
                break;
            }
            EventData dateTimeEventData = dateTimeDataIterator.next();
            // Check if both received data has matched offset
            Assert.assertTrue(
                String.format(Locale.US, "START_OF_STREAM offset: %s, EPOCH offset: %s", offsetData.offset(), dateTimeEventData.offset()),
                offsetData.offset().equalsIgnoreCase(dateTimeEventData.offset()));
        }
    }

    /**
     * Test receiving message from a receiver with latest event position
     */
    @Ignore
    @Test
    public void testReceiverLatestFilter() {
        skipIfNotRecordMode();

        // Arrange
        // TODO: latest operation not working or misunderstood
        final EventReceiver offsetReceiver = client.createReceiver(PARTITION_ID, EventPosition.latest(),
            new EventReceiverOptions().consumerGroup(getConsumerGroupName()).prefetchCount(2));

        StepVerifier.create(offsetReceiver.receive().take(NUM_OF_EVENTS))
            .expectNextCount(0)
            .verifyComplete();

        // Action
        Mono<Void> sentData = ApiTestBase.pushEventsToPartition(client, senderOptions, NUM_OF_EVENTS);
        StepVerifier.create(sentData).verifyComplete();

        // Verification
        StepVerifier.create(offsetReceiver.receive().take(NUM_OF_EVENTS))
            .expectNextCount(NUM_OF_EVENTS)
            .verifyComplete();
    }

    /**
     * Test a receiver with inclusive offset
     */
    @Ignore
    @Test
    public void testReceiverOffsetInclusiveFilter() {
        skipIfNotRecordMode();
        // Arrange
        EventReceiver enqueuedTimeReceiver = client.createReceiver(PARTITION_ID, EventPosition.fromEnqueuedTime(Instant.EPOCH),
            new EventReceiverOptions().consumerGroup(getConsumerGroupName()));
        final Flux<EventData> enqueuedTimeData = enqueuedTimeReceiver.receive();

        // Verification
        StepVerifier.create(enqueuedTimeData.take(NUM_OF_EVENTS))
            .expectNextCount(NUM_OF_EVENTS)
            .verifyComplete();
        EventData enqueuedTimeEvent = enqueuedTimeData.toIterable().iterator().next();

        // Arrange
        EventReceiver offsetReceiver = client.createReceiver(PARTITION_ID, EventPosition.fromOffset(enqueuedTimeEvent.offset(), true),
            new EventReceiverOptions().consumerGroup(getConsumerGroupName()));
        final Flux<EventData> offsetData = offsetReceiver.receive();


        // Verification
        StepVerifier.create(offsetData.take(NUM_OF_EVENTS))
            .expectNextCount(NUM_OF_EVENTS)
            .verifyComplete();
        EventData offsetEvent = offsetData.toIterable().iterator().next();

        // Assertion
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

        EventReceiver enqueuedTimeReceiver = client.createReceiver(PARTITION_ID, EventPosition.fromEnqueuedTime(Instant.EPOCH),
            new EventReceiverOptions().consumerGroup(getConsumerGroupName()));
        final Flux<EventData> enqueuedTimeData = enqueuedTimeReceiver.receive();

        // Verification
        StepVerifier.create(enqueuedTimeData.take(NUM_OF_EVENTS))
            .expectNextCount(NUM_OF_EVENTS)
            .verifyComplete();
        EventData event = enqueuedTimeData.toIterable().iterator().next();

        // Arrange
        EventReceiver offsetReceiver = client.createReceiver(PARTITION_ID, EventPosition.fromOffset(event.offset(), false),
            new EventReceiverOptions().consumerGroup(getConsumerGroupName()));
        final Flux<EventData> offsetData = offsetReceiver.receive();

        // Verification
        StepVerifier.create(offsetData.take(NUM_OF_EVENTS))
            .expectNextCount(NUM_OF_EVENTS)
            .verifyComplete();
        EventData offsetEvent = offsetData.toIterable().iterator().next();

        // Assertion
        Assert.assertEquals(offsetEvent.sequenceNumber(), event.sequenceNumber() + 1);
    }

    /**
     * Test for receiving sequence number with inclusive filter
     */
    @Ignore
    @Test
    public void testReceiverSequenceNumberInclusiveFilter() {
        skipIfNotRecordMode();
        // Arrange: EventPosition.fromEnqueuedTime
        EventReceiver enqueuedTimeReceiver = client.createReceiver(PARTITION_ID, EventPosition.fromEnqueuedTime(Instant.EPOCH),
            new EventReceiverOptions().consumerGroup(getConsumerGroupName()));
        final Flux<EventData> enqueuedTimeData = enqueuedTimeReceiver.receive();

        // Verification
        StepVerifier.create(enqueuedTimeData.take(NUM_OF_EVENTS))
            .expectNextCount(NUM_OF_EVENTS)
            .verifyComplete();
        EventData enqueuedTimeEvent = enqueuedTimeData.toIterable().iterator().next();

        // Arrange: EventPosition.fromSequenceNumber
        EventReceiver sequenceNumReceiver = client.createReceiver(PARTITION_ID, EventPosition.fromSequenceNumber(enqueuedTimeEvent.sequenceNumber(), true),
            new EventReceiverOptions().consumerGroup(getConsumerGroupName()));
        final Flux<EventData> sequenceNumData = sequenceNumReceiver.receive();


        // Verification
        StepVerifier.create(sequenceNumData.take(NUM_OF_EVENTS))
            .expectNextCount(NUM_OF_EVENTS)
            .verifyComplete();
        EventData sequenceNumEvent = sequenceNumData.toIterable().iterator().next();

        // Assertion
        Assert.assertEquals(sequenceNumEvent.offset(), enqueuedTimeEvent.offset());
        Assert.assertEquals(sequenceNumEvent.sequenceNumber(), enqueuedTimeEvent.sequenceNumber());
    }

    /**
     * Test for receiving sequence number without inclusive filter
     */
    @Ignore
    @Test
    public void testReceiverSequenceNumberNonInclusiveFilter() {
        skipIfNotRecordMode();
        // Arrange: EventPosition.fromEnqueuedTime
        EventReceiver enqueuedTimeReceiver = client.createReceiver(PARTITION_ID, EventPosition.fromEnqueuedTime(Instant.EPOCH),
            new EventReceiverOptions().consumerGroup(getConsumerGroupName()));
        final Flux<EventData> enqueuedTimeData = enqueuedTimeReceiver.receive();

        // Verification
        StepVerifier.create(enqueuedTimeData.take(NUM_OF_EVENTS))
            .expectNextCount(NUM_OF_EVENTS)
            .verifyComplete();
        EventData enqueuedTimeEvent = enqueuedTimeData.toIterable().iterator().next();

        // Arrange: EventPosition.fromSequenceNumber
        EventReceiver sequenceNumReceiver = client.createReceiver(PARTITION_ID, EventPosition.fromSequenceNumber(enqueuedTimeEvent.sequenceNumber(), false),
            new EventReceiverOptions().consumerGroup(getConsumerGroupName()));
        final Flux<EventData> sequenceNumData = sequenceNumReceiver.receive();

        // Verification
        StepVerifier.create(sequenceNumData.take(NUM_OF_EVENTS))
            .expectNextCount(NUM_OF_EVENTS)
            .verifyComplete();
        EventData sequenceNumEvent = sequenceNumData.toIterable().iterator().next();

        // Assertion
        Assert.assertEquals(sequenceNumEvent.sequenceNumber(), enqueuedTimeEvent.sequenceNumber() + 1);
    }

    /**
     * Test for received body and properties of a same event
     */
    @Ignore
    @Test
    public void testReceivedBodyAndProperties() {
        skipIfNotRecordMode();
        // Data
        final EventData event = new EventData(PAYLOAD.getBytes());
        event.properties().put(PROPERTY2, PROPERTY_VALUE2);
        event.properties().put(PROPERTY1, PROPERTY_VALUE1);

        // Arrange
        final EventReceiver secLatestReceiver = client.createReceiver(PARTITION_ID, EventPosition.latest(),
            new EventReceiverOptions().consumerGroup(getConsumerGroupName()));
        Flux<EventData> receivedData = secLatestReceiver.receive();

        final EventSender sender = client.createSender(senderOptions);
        sender.send(Mono.just(event));

        // Action and Verify
        StepVerifier.create(receivedData)
            .expectNextMatches(data -> {
                validateReceivedEvent(data);
                resendEventData = data;
                return true;
            })
            .verifyComplete();

        // Action and Verify
        sender.send(Mono.just(resendEventData));
        StepVerifier.create(receivedData)
            .expectNextMatches(data -> {
                validateReceivedEvent(data);
                return true;
            })
            .verifyComplete();
    }

    private void validateReceivedEvent(EventData eventData) {
        Objects.requireNonNull(eventData);
        Assert.assertEquals(PAYLOAD, new String(eventData.body().array()));

        Map<String, Object> propertiesMap = eventData.properties();
        Assert.assertTrue(propertiesMap.containsKey(PROPERTY1) && propertiesMap.get(PROPERTY1).equals(PROPERTY_VALUE1));
        Assert.assertTrue(propertiesMap.containsKey(PROPERTY2) && propertiesMap.get(PROPERTY2).equals(PROPERTY_VALUE2));

        eventData.systemProperties();
        Assert.assertNotNull(eventData.offset());
        Assert.assertNotNull(eventData.enqueuedTime());

        Assert.assertTrue(eventData.sequenceNumber() > 0);
        Assert.assertNull(eventData.partitionKey());
//        Assert.assertNull(eventData.publisher()); //TODO: double check if publisher is needed in track 2
    }
}
