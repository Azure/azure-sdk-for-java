// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.amqp.MessageConstant;
import com.azure.eventhubs.implementation.ApiTestBase;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
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
    private static final String CONSUMER_GROUP_NAME = ApiTestBase.getConsumerGroupName();
    private static final String PARTITION_ID = "0";
    private static final String PAYLOAD = "TestMessage1";
    private static final String PROPERTY1 = "property1";
    private static final String PROPERTY_VALUE1 = "something1";
    private static final String PROPERTY2 = MessageConstant.MESSAGE_ID.getValue(); // TODO: need to verify it
    private static final String PROPERTY_VALUE2 = "something2";

    private static EventHubClient ehClient;
    private EventData resendEventData;

    @Rule
    public TestName testName = new TestName();

    @Override
    protected String testName() {
        return testName.getMethodName();
    }

    @BeforeClass
    public static void initialize() {
        ehClient = ApiTestBase.getEventHubClientBuilder().build();
    }

    @AfterClass
    public static void cleanup() {
        if (ehClient != null) {
            ehClient.close();
        }
    }

    @Ignore
    @Test
    public void testReceiverStartOfStreamFilters() {
        EventReceiver offsetReceiver = ehClient.createReceiver(PARTITION_ID,
            new EventReceiverOptions()
                .beginReceivingAt(EventPosition.firstAvailableEvent())
                .consumerGroup(CONSUMER_GROUP_NAME));
        final Flux<EventData> receivedData = offsetReceiver.receive();
        ApiTestBase.pushEventsToPartition(ehClient, PARTITION_ID, 25);
        Assert.assertTrue(receivedData != null && receivedData.toIterable().iterator().hasNext());

        EventReceiver dateTimeReceiver = ehClient.createReceiver(PARTITION_ID,
            new EventReceiverOptions()
                .beginReceivingAt(EventPosition.fromEnqueuedTime(Instant.EPOCH))
                .consumerGroup(CONSUMER_GROUP_NAME));
        final Flux<EventData> dateTimeData = dateTimeReceiver.receive();
        Assert.assertTrue(dateTimeData != null && dateTimeData.toIterable().iterator().hasNext());

        Iterator<EventData> dateTimeDataIterator = dateTimeData.toIterable().iterator();
        Iterable<EventData> offsetDatas = receivedData.toIterable();

        for (EventData offsetData : offsetDatas) {
            if (!dateTimeDataIterator.hasNext()) {
                break;
            }
            EventData dateTimeEventData = dateTimeDataIterator.next();
            Assert.assertTrue(
                String.format(Locale.US, "START_OF_STREAM offset: %s, EPOCH offset: %s", offsetData.offset(), dateTimeEventData.offset()),
                offsetData.offset().equalsIgnoreCase(dateTimeEventData.offset()));
        }
    }

    @Ignore
    @Test
    public void testReceiverLatestFilter() {
        EventReceiver offsetReceiver = ehClient.createReceiver(PARTITION_ID,
            new EventReceiverOptions()
                .beginReceivingAt(EventPosition.newEventsOnly())
                .consumerGroup(CONSUMER_GROUP_NAME));
        final Flux<EventData> receivedData = offsetReceiver.receive();
        StepVerifier.create(receivedData)
            .verifyComplete();

        ApiTestBase.pushEventsToPartition(ehClient, PARTITION_ID, 25);
        StepVerifier.create(receivedData)
            .expectNextCount(25)
            .verifyComplete();
    }

    @Ignore
    @Test
    public void testReceiverOffsetInclusiveFilter() {
        EventReceiver dateTimeReceiver = ehClient.createReceiver(PARTITION_ID,
            new EventReceiverOptions()
                .beginReceivingAt(EventPosition.fromEnqueuedTime(Instant.EPOCH))
                .consumerGroup(CONSUMER_GROUP_NAME));
        final Flux<EventData> dateTimeData = dateTimeReceiver.receive();
        // TODO: verity dateTimeData is not null and has values in StepVerify way
        Assert.assertTrue(dateTimeData != null && dateTimeData.toIterable().iterator().hasNext());
        EventData event = dateTimeData.toIterable().iterator().next();


        EventReceiver offsetReceiver = ehClient.createReceiver(PARTITION_ID,
           new EventReceiverOptions()
               .beginReceivingAt(EventPosition.fromOffset(event.offset(), true))
               .consumerGroup(CONSUMER_GROUP_NAME));
        final Flux<EventData> offsetData = offsetReceiver.receive();
        Assert.assertTrue(offsetData != null && offsetData.toIterable().iterator().hasNext());
        EventData offsetEvent = offsetData.toIterable().iterator().next();
        Assert.assertEquals(offsetEvent.offset(), event.offset());
        Assert.assertEquals(offsetEvent.sequenceNumber(), event.sequenceNumber());
    }

    @Ignore
    @Test
    public void testReceiverOffsetNonInclusiveFilter() {
        EventReceiver dateTimeReceiver = ehClient.createReceiver(PARTITION_ID,
            new EventReceiverOptions()
                .beginReceivingAt(EventPosition.fromEnqueuedTime(Instant.EPOCH))
                .consumerGroup(CONSUMER_GROUP_NAME));
        final Flux<EventData> dateTimeData = dateTimeReceiver.receive();
        // TODO: verity dateTimeData is not null and has values in StepVerify way
        Assert.assertTrue(dateTimeData != null && dateTimeData.toIterable().iterator().hasNext());
        EventData event = dateTimeData.toIterable().iterator().next();


        EventReceiver offsetReceiver = ehClient.createReceiver(PARTITION_ID,
            new EventReceiverOptions()
                .beginReceivingAt(EventPosition.fromOffset(event.offset(), false))
                .consumerGroup(CONSUMER_GROUP_NAME));
        final Flux<EventData> offsetData = offsetReceiver.receive();
        Assert.assertTrue(offsetData != null && offsetData.toIterable().iterator().hasNext());
        EventData offsetEvent = offsetData.toIterable().iterator().next();

        Assert.assertEquals(offsetEvent.sequenceNumber(), event.sequenceNumber() + 1);
    }

    @Ignore
    @Test
    public void testReceiverSequenceNumberInclusiveFilter() {
        EventReceiver dateTimeReceiver = ehClient.createReceiver(PARTITION_ID,
            new EventReceiverOptions()
                .beginReceivingAt(EventPosition.fromEnqueuedTime(Instant.EPOCH))
                .consumerGroup(CONSUMER_GROUP_NAME));
        final Flux<EventData> dateTimeData = dateTimeReceiver.receive();
        // TODO: verity dateTimeData is not null and has values in StepVerify way
        Assert.assertTrue(dateTimeData != null && dateTimeData.toIterable().iterator().hasNext());
        EventData event = dateTimeData.toIterable().iterator().next();

        EventReceiver sequenceNumReceiver = ehClient.createReceiver(PARTITION_ID,
            new EventReceiverOptions()
                .beginReceivingAt(EventPosition.fromSequenceNumber(event.sequenceNumber(), true))
                .consumerGroup(CONSUMER_GROUP_NAME));
        final Flux<EventData> sequenceNumData = sequenceNumReceiver.receive();
        Assert.assertTrue(sequenceNumData != null && sequenceNumData.toIterable().iterator().hasNext());
        EventData sequenceNumEvent = sequenceNumData.toIterable().iterator().next();

        Assert.assertEquals(sequenceNumEvent.offset(), event.offset());
        Assert.assertEquals(sequenceNumEvent.sequenceNumber(), event.sequenceNumber());
    }

    @Ignore
    @Test
    public void testReceiverSequenceNumberNonInclusiveFilter() {
        EventReceiver dateTimeReceiver = ehClient.createReceiver(PARTITION_ID,
            new EventReceiverOptions()
                .beginReceivingAt(EventPosition.fromEnqueuedTime(Instant.EPOCH))
                .consumerGroup(CONSUMER_GROUP_NAME));
        final Flux<EventData> dateTimeData = dateTimeReceiver.receive();
        // TODO: verity dateTimeData is not null and has values in StepVerify way
        Assert.assertTrue(dateTimeData != null && dateTimeData.toIterable().iterator().hasNext());
        EventData event = dateTimeData.toIterable().iterator().next();

        EventReceiver sequenceNumReceiver = ehClient.createReceiver(PARTITION_ID,
            new EventReceiverOptions()
                .beginReceivingAt(EventPosition.fromSequenceNumber(event.sequenceNumber(), false))
                .consumerGroup(CONSUMER_GROUP_NAME));
        final Flux<EventData> sequenceNumData = sequenceNumReceiver.receive();
        Assert.assertTrue(sequenceNumData != null && sequenceNumData.toIterable().iterator().hasNext());
        EventData sequenceNumEvent = sequenceNumData.toIterable().iterator().next();

        Assert.assertEquals(sequenceNumEvent.sequenceNumber(), event.sequenceNumber() + 1);
    }

    @Ignore
    @Test
    public void testReceivedBodyAndProperties() {
        EventReceiver dateTimeReceiver = ehClient.createReceiver(PARTITION_ID,
            new EventReceiverOptions()
                .beginReceivingAt(EventPosition.newEventsOnly())
                .consumerGroup(CONSUMER_GROUP_NAME));
        //TODO: setReceiveTimeout is missing in EventReceiver
        final Flux<EventData> dateTimeDates = dateTimeReceiver.receive();
        ApiTestBase.pushEventsToPartition(ehClient, PARTITION_ID, 25);
        StepVerifier.create(dateTimeDates)
            .expectNextCount(25)
            .verifyComplete();

        final EventData event = new EventData(PAYLOAD.getBytes());
        event.properties().put(PROPERTY1, PROPERTY_VALUE1);
        event.properties().put(PROPERTY2, PROPERTY_VALUE2);

        final EventReceiver receiver =  ehClient.createReceiver(PARTITION_ID,
            new EventReceiverOptions()
                .beginReceivingAt(EventPosition.newEventsOnly())
                .consumerGroup(CONSUMER_GROUP_NAME));

        EventSenderOptions senderOptions = new EventSenderOptions().partitionId(PARTITION_ID);
        final EventSender sender = ehClient.createSender(senderOptions);

        Flux<EventData> receivedData = receiver.receive();
        sender.send(Mono.just(event));

        StepVerifier.create(receivedData)
            .expectNextMatches(data -> {
                validateReceivedEvent(data);
                resendEventData = data;
                return true;
            })
            .verifyComplete();

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
