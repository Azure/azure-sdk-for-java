// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.implementation.logging.ServiceLogger;
import com.azure.eventhubs.implementation.ApiTestBase;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Random;
import java.util.UUID;

public class EventDataBatchTest extends ApiTestBase {
    ServiceLogger logger = new ServiceLogger(EventDataBatchTest.class);

    private static final String CONSUMER_GROUP_NAME = ApiTestBase.getConsumerGroupName();
    private static final String PARTITION_ID = "0";
    private static final String PARTITION_KEY = "key1";
    private static final String APPLICATION_PROPERTY = "firstProp";
    private static final String MESSAGE_ANNOTATION = "message-annotation-1";
    private static final String PAYLOAD = "testmsg";

    private static EventHubClient ehClient;
    private static EventSender sender;
    private static EventReceiver receiver;

    @Rule
    public TestName testName = new TestName();

    @Override
    protected String testName() {
        return testName.getMethodName();
    }

    @BeforeClass
    public static void initialize() {
        ehClient = ApiTestBase.getEventHubClientBuilder().build();

        EventSenderOptions senderOptions = new EventSenderOptions().partitionId(PARTITION_ID);
        sender = ehClient.createSender(senderOptions);

        EventReceiverOptions receiverOptions = new EventReceiverOptions()
            .consumerGroup(ApiTestBase.getConsumerGroupName())
            .beginReceivingAt(EventPosition.newEventsOnly());
        receiver = ehClient.createReceiver(PARTITION_ID, receiverOptions);
    }

    @AfterClass
    public static void cleanupClient() {
        if (ehClient != null) {
            ehClient.close();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullEventData() {
        final EventDataBatch batch = new EventDataBatch(1, PARTITION_KEY);
        batch.tryAdd(null);
    }

    @Test(expected = AmqpException.class)
    public void payloadExceededException() {
        final EventDataBatch batch = new EventDataBatch(1024, PARTITION_KEY);
        final EventData tooBig = new EventData(new byte[1024 * 1024 * 2]);
        batch.tryAdd(tooBig);
    }

    @Test
    public void withinPayloadSize() {
        final EventDataBatch batch = new EventDataBatch(EventSender.MAX_MESSAGE_LENGTH_BYTES, PARTITION_KEY);
        final EventData within = new EventData(new byte[1024]);
        Assert.assertTrue(batch.tryAdd(within));
    }

    // TODO: maybe duplicate with EventSenderTest, here we are using EventDataBatch
    @Test
    public void sendSmallEventsFullBatch() {
        final EventDataBatch batch = new EventDataBatch(EventSender.MAX_MESSAGE_LENGTH_BYTES, PARTITION_KEY);
        while (batch.tryAdd(new EventData("a".getBytes()))) {
            System.out.println(String.format("Batch size: %s", batch.getSize()));
            logger.asInfo().log(String.format("Batch size: %s", batch.getSize()));
        }
        StepVerifier.create(sender.send(batch.getInternalIterable()))
            .verifyComplete();
    }

    // TODO: maybe duplicate with EventSenderTest, here we are using EventDataBatch
    @Test
    public void sendSmallEventsFullBatchPartitionKey() {
        final EventBatchingOptions batchOptions = new EventBatchingOptions().partitionKey(PARTITION_KEY);
        final EventDataBatch batch = new EventDataBatch(EventSender.MAX_MESSAGE_LENGTH_BYTES, PARTITION_KEY);
        while (batch.tryAdd(new EventData("a".getBytes()))) {
            logger.asInfo().log(String.format("Batch size: %s", batch.getSize()));
        }
        StepVerifier.create(sender.send(batch.getInternalIterable(), batchOptions))
            .verifyComplete();
    }

    @Ignore
    @Test
    public void sendBatchPartitionKeyValidate() {
        // TODO: after understand EventReceiver functionality
    }

    @Ignore
    @Test
    public void sendEventsFullBatchWithAppProps() {
        // TODO: after understand EventReceiver functionality
    }

    @Test
    public void sendEventsFullBatchWithPartitionKey() {
        final EventBatchingOptions batchOptions = new EventBatchingOptions().partitionKey(UUID.randomUUID().toString());
        final EventDataBatch batch = new EventDataBatch(EventSender.MAX_MESSAGE_LENGTH_BYTES, UUID.randomUUID().toString());

        int count = 0;
        while (true) {
            final EventData eventData = new EventData("a".getBytes());
            for (int i = 0; i < new Random().nextInt(20); i++) {
                eventData.properties().put("key" + i, "value");
            }

            if (batch.tryAdd(eventData)) {
                count++;
            } else {
                break;
            }
        }

        Assert.assertEquals(count, batch.getSize());
        StepVerifier.create(sender.send(batch.getInternalIterable(), batchOptions))
            .verifyComplete();
    }
}
