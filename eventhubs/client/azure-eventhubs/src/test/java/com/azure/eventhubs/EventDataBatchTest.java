// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.amqp.Retry;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.util.logging.ClientLogger;
import com.azure.eventhubs.implementation.ApiTestBase;
import com.azure.eventhubs.implementation.ReactorHandlerProvider;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.util.Random;

public class EventDataBatchTest extends ApiTestBase {
    private static final String PARTITION_KEY = "PartitionIDCopyFromSenderOption";

    private final ClientLogger logger = new ClientLogger(EventDataBatchTest.class);

    private EventHubClient client;
    private EventHubProducer sender;
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
        final EventHubProducerOptions senderOptions = new EventHubProducerOptions()
            .retry(Retry.getNoRetry())
            .timeout(Duration.ofSeconds(30));
        sender = client.createProducer(senderOptions);
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
                logger.asError().log(String.format("[%s]: Sender doesn't close properly.", testName.getMethodName()), e);
            }
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
        final EventDataBatch batch = new EventDataBatch(EventHubProducer.MAX_MESSAGE_LENGTH_BYTES, PARTITION_KEY);
        final EventData within = new EventData(new byte[1024]);
        Assert.assertTrue(batch.tryAdd(within));
    }

    /**
     * Test for sending full batch without partition key
     */
    @Test
    public void sendSmallEventsFullBatch() {
        skipIfNotRecordMode();

        // Arrange
        final EventDataBatch batch = new EventDataBatch(EventHubProducer.MAX_MESSAGE_LENGTH_BYTES, PARTITION_KEY);
        while (batch.tryAdd(new EventData("a".getBytes()))) {
            logger.asVerbose().log("Batch size: {}", batch.getSize());
        }

        // Act & Assert
        StepVerifier.create(sender.send(batch.getEvents(), new SendOptions()))
            .verifyComplete();
    }

    /**
     * Test for sending full batch with partition key
     */
    @Test
    public void sendSmallEventsFullBatchPartitionKey() {
        skipIfNotRecordMode();

        // Arrange
        // Only Event Data batch has partition key information, none in SendOption and SenderOption
        final EventDataBatch batch = new EventDataBatch(EventHubProducer.MAX_MESSAGE_LENGTH_BYTES, PARTITION_KEY);
        while (batch.tryAdd(new EventData("a".getBytes()))) {
            logger.asVerbose().log("Batch size: {}", batch.getSize());
        }

        // Act & Assert
        StepVerifier.create(sender.send(batch.getEvents(), new SendOptions()))
            .verifyComplete();
    }

    @Test
    public void sendBatchPartitionKeyValidate() {
        // TODO: after understand EventHubClient Runtime info functionality
        skipIfNotRecordMode();
    }

    /**
     * Test for sending a full batch with partition key
     */
    @Test
    public void sendEventsFullBatchWithPartitionKey() {
        skipIfNotRecordMode();

        // Arrange
        final EventDataBatch batch = new EventDataBatch(1024, PARTITION_KEY);
        final Random random = new Random();
        final SendOptions sendOptions = new SendOptions();
        int count = 0;

        while (true) {
            final EventData eventData = new EventData("a".getBytes());
            for (int i = 0; i < random.nextInt(20); i++) {
                eventData.properties().put("key" + i, "value");
            }

            if (batch.tryAdd(eventData)) {
                count++;
            } else {
                break;
            }
        }

        // Act & Assert
        Assert.assertEquals(count, batch.getSize());
        StepVerifier.create(sender.send(batch.getEvents(), sendOptions))
            .verifyComplete();
    }

    /**
     * Test for sending full batch with both sender's partitionID and batch's partitionKey
     * Both event data batch a
     */
    @Test
    public void sendBatchWithPartitionKeyOnPartitionSenderTest() {
        skipIfNotRecordMode();
        // EventDataBatch is only accessible from internal, so the partition key
        // Arrange
        final EventDataBatch batch = new EventDataBatch(EventHubProducer.MAX_MESSAGE_LENGTH_BYTES, PARTITION_KEY);
        final SendOptions sendOptions = new SendOptions();

        while (batch.tryAdd(new EventData("a".getBytes()))) {
            logger.asVerbose().log("Batch size: {}", batch.getSize());
        }

        // Act & Assert
        StepVerifier.create(sender.send(batch.getEvents(), sendOptions))
            .verifyComplete();
    }
}
