// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.amqp.MessageConstant;
import com.azure.core.amqp.Retry;
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

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

public class EventReceiverTest extends ApiTestBase {
    private final ServiceLogger logger = new ServiceLogger(EventReceiverTest.class);

    private final String CONSUMER_GROUP_NAME = getConsumerGroupName();
    private static final String PARTITION_ID = "0";
    private static final String PAYLOAD = "TestMessage1";
    private static final String PROPERTY1 = "property1";
    private static final String PROPERTY_VALUE1 = "something1";
    private static final String PROPERTY2 = MessageConstant.MESSAGE_ID.getValue(); // TODO: need to verify it
    private static final String PROPERTY_VALUE2 = "something2";
    private static final int NUM_OF_EVENTS = 10;

    private EventData resendEventData;
    private EventHubClient client;
    private EventSender sender;
    private EventReceiver receiver;
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
        senderOptions = new EventSenderOptions().partitionId(PARTITION_ID).retry(Retry.getNoRetry()).timeout(Duration.ofSeconds(30));
    }

    @Override
    protected void afterTest() {
        logger.asInfo().log("[{}]: Performing test clean-up.", testName.getMethodName());

        if (client != null) {
            client.close();
        }

        if (sender != null) {
            try {
                sender.close();
            } catch (IOException e) {
                logger.asError().log("[{}]: Sender doesn't close properly", testName.getMethodName());
            }
        }

        if (receiver != null) {
            try {
                receiver.close();
            } catch (IOException e) {
                logger.asError().log("[{}]: Receiver doesn't close properly", testName.getMethodName());
            }
        }
    }

    /**
     *  Test for a simple receiving message
     */
    @Ignore
    @Test
    public void receiveMessage() {
        skipIfNotRecordMode();

        // Arrange
        receiver = client.createReceiver(PARTITION_ID, EventPosition.earliest(),
            new EventReceiverOptions().prefetchCount(2).consumerGroup(CONSUMER_GROUP_NAME));

        // Act & Assert
        StepVerifier.create(receiver.receive())
            .expectNextCount(NUM_OF_EVENTS)
            .verifyComplete();
    }

    /**
     * Test for receiving from start of stream
     */
    @Ignore
    @Test
    public void testReceiverStartOfStreamFilters() {
        skipIfNotRecordMode();

        // Arrange
        receiver = client.createReceiver(PARTITION_ID, EventPosition.earliest(),
            new EventReceiverOptions().consumerGroup(getConsumerGroupName()));
        final Flux<EventData> offsetReceivedData = receiver.receive();
        // Act & Assert
        StepVerifier.create(offsetReceivedData.take(NUM_OF_EVENTS))
            .expectNextCount(NUM_OF_EVENTS)
            .verifyComplete();

        // Arrange
        receiver = client.createReceiver(PARTITION_ID, EventPosition.fromEnqueuedTime(Instant.EPOCH),
            new EventReceiverOptions().consumerGroup(getConsumerGroupName()));
        final Flux<EventData> dateTimeReceivedData = receiver.receive();
        // Act & Assert
        StepVerifier.create(dateTimeReceivedData.take(NUM_OF_EVENTS))
            .expectNextCount(NUM_OF_EVENTS)
            .verifyComplete();

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
        receiver = client.createReceiver(PARTITION_ID, EventPosition.latest(),
            new EventReceiverOptions().consumerGroup(getConsumerGroupName()).prefetchCount(2));

        StepVerifier.create(receiver.receive().take(NUM_OF_EVENTS))
            .expectNextCount(0)
            .verifyComplete();

        // Action
        final Mono<Void> sentData = pushEventsToPartition(client, senderOptions, NUM_OF_EVENTS);
        StepVerifier.create(sentData)
            .verifyComplete();

        // Verification
        StepVerifier.create(receiver.receive().take(NUM_OF_EVENTS))
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
        receiver = client.createReceiver(PARTITION_ID, EventPosition.fromEnqueuedTime(Instant.EPOCH),
            new EventReceiverOptions().consumerGroup(getConsumerGroupName()));
        final Flux<EventData> enqueuedTimeData = receiver.receive();

        // Verification
        StepVerifier.create(enqueuedTimeData.take(NUM_OF_EVENTS))
            .expectNextCount(NUM_OF_EVENTS)
            .verifyComplete();
        final EventData enqueuedTimeEvent = enqueuedTimeData.toIterable().iterator().next();

        // Arrange
        receiver = client.createReceiver(PARTITION_ID, EventPosition.fromOffset(enqueuedTimeEvent.offset(), true),
            new EventReceiverOptions().consumerGroup(getConsumerGroupName()));
        final Flux<EventData> offsetData = receiver.receive();

        // Verification
        StepVerifier.create(offsetData.take(NUM_OF_EVENTS))
            .expectNextCount(NUM_OF_EVENTS)
            .verifyComplete();
        final EventData offsetEvent = offsetData.toIterable().iterator().next();

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

        receiver = client.createReceiver(PARTITION_ID, EventPosition.fromEnqueuedTime(Instant.EPOCH),
            new EventReceiverOptions().consumerGroup(getConsumerGroupName()));
        final Flux<EventData> enqueuedTimeData = receiver.receive();

        // Verification
        StepVerifier.create(enqueuedTimeData.take(NUM_OF_EVENTS))
            .expectNextCount(NUM_OF_EVENTS)
            .verifyComplete();
        final EventData event = enqueuedTimeData.toIterable().iterator().next();

        // Arrange
        receiver = client.createReceiver(PARTITION_ID, EventPosition.fromOffset(event.offset(), false),
            new EventReceiverOptions().consumerGroup(getConsumerGroupName()));
        final Flux<EventData> offsetData = receiver.receive();

        // Verification
        StepVerifier.create(offsetData.take(NUM_OF_EVENTS))
            .expectNextCount(NUM_OF_EVENTS)
            .verifyComplete();
        final EventData offsetEvent = offsetData.toIterable().iterator().next();

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
        receiver = client.createReceiver(PARTITION_ID, EventPosition.fromEnqueuedTime(Instant.EPOCH),
            new EventReceiverOptions().consumerGroup(getConsumerGroupName()));
        final Flux<EventData> enqueuedTimeData = receiver.receive();

        // Verification
        StepVerifier.create(enqueuedTimeData.take(NUM_OF_EVENTS))
            .expectNextCount(NUM_OF_EVENTS)
            .verifyComplete();
        final EventData enqueuedTimeEvent = enqueuedTimeData.toIterable().iterator().next();

        // Arrange: EventPosition.fromSequenceNumber
        receiver = client.createReceiver(PARTITION_ID, EventPosition.fromSequenceNumber(enqueuedTimeEvent.sequenceNumber(), true),
            new EventReceiverOptions().consumerGroup(getConsumerGroupName()));
        final Flux<EventData> sequenceNumData = receiver.receive();

        // Verification
        StepVerifier.create(sequenceNumData.take(NUM_OF_EVENTS))
            .expectNextCount(NUM_OF_EVENTS)
            .verifyComplete();
        final EventData sequenceNumEvent = sequenceNumData.toIterable().iterator().next();

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
        receiver = client.createReceiver(PARTITION_ID, EventPosition.fromEnqueuedTime(Instant.EPOCH),
            new EventReceiverOptions().consumerGroup(getConsumerGroupName()));
        final Flux<EventData> enqueuedTimeData = receiver.receive();

        // Verification
        StepVerifier.create(enqueuedTimeData.take(NUM_OF_EVENTS))
            .expectNextCount(NUM_OF_EVENTS)
            .verifyComplete();
        final EventData enqueuedTimeEvent = enqueuedTimeData.toIterable().iterator().next();

        // Arrange: EventPosition.fromSequenceNumber
        receiver = client.createReceiver(PARTITION_ID, EventPosition.fromSequenceNumber(enqueuedTimeEvent.sequenceNumber(), false),
            new EventReceiverOptions().consumerGroup(getConsumerGroupName()));
        final Flux<EventData> sequenceNumData = receiver.receive();

        // Verification
        StepVerifier.create(sequenceNumData.take(NUM_OF_EVENTS))
            .expectNextCount(NUM_OF_EVENTS)
            .verifyComplete();
        final EventData sequenceNumEvent = sequenceNumData.toIterable().iterator().next();

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
        receiver = client.createReceiver(PARTITION_ID, EventPosition.latest(),
            new EventReceiverOptions().consumerGroup(getConsumerGroupName()));

        final EventSender sender = client.createSender(senderOptions);
        sender.send(event);

        // Action and Verify
        StepVerifier.create(receiver.receive())
            .expectNextMatches(data -> {
                validateReceivedEvent(data);
                resendEventData = data;
                return true;
            })
            .verifyComplete();

        // Action and Verify
        sender.send(resendEventData);
        StepVerifier.create(receiver.receive())
            .expectNextMatches(data -> {
                validateReceivedEvent(data);
                return true;
            })
            .verifyComplete();
    }

    private void validateReceivedEvent(EventData eventData) {
        Objects.requireNonNull(eventData);
        Assert.assertEquals(PAYLOAD, new String(eventData.body().array()));

        final Map<String, Object> propertiesMap = eventData.properties();
        Assert.assertTrue(propertiesMap.containsKey(PROPERTY1) && propertiesMap.get(PROPERTY1).equals(PROPERTY_VALUE1));
        Assert.assertTrue(propertiesMap.containsKey(PROPERTY2) && propertiesMap.get(PROPERTY2).equals(PROPERTY_VALUE2));

        eventData.systemProperties();
        Assert.assertNotNull(eventData.offset());
        Assert.assertNotNull(eventData.enqueuedTime());

        Assert.assertTrue(eventData.sequenceNumber() > 0);
        Assert.assertNull(eventData.partitionKey());
//        Assert.assertNull(eventData.publisher()); //TODO: double check if publisher is needed in track 2
    }

    private Mono<Void> pushEventsToPartition(final EventHubClient client, final EventSenderOptions senderOptions, final int noOfEvents) {
        final Flux<EventData> events = Flux.range(0, noOfEvents).map(number -> {
            final EventData data = new EventData("testString".getBytes(UTF_8));
            return data;
        });
        sender = client.createSender(senderOptions);
        return sender.send(events);
    }
}
