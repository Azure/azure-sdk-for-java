// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.amqp.Retry;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.implementation.logging.ServiceLogger;
import com.azure.eventhubs.implementation.ApiTestBase;
import com.azure.eventhubs.implementation.ReactorHandlerProvider;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.util.Random;
import java.util.UUID;

public class EventDataBatchTest extends ApiTestBase {
    private final ServiceLogger logger = new ServiceLogger(EventReceiverTest.class);

    private static final String PARTITION_ID = "0";
    private static final String PARTITION_KEY = "key1";
    private static final String APPLICATION_PROPERTY = "firstProp";
    private static final String MESSAGE_ANNOTATION = "message-annotation-1";
    private static final String PAYLOAD = "testmsg";

    private EventHubClient client;
    private static EventSender sender;

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

    @Ignore
    @Test
    public void sendSmallEventsFullBatch() {
        skipIfNotRecordMode();
        // TODO: event data batch has to have partition key??
        // Arrange
        final EventDataBatch batch = new EventDataBatch(EventSender.MAX_MESSAGE_LENGTH_BYTES, PARTITION_KEY);
        while (batch.tryAdd(new EventData("a".getBytes()))) {
            logger.asInfo().log(String.format("Batch size: %s", batch.getSize()));
        }

        final EventSenderOptions senderOptions = new EventSenderOptions()
            .partitionId(PARTITION_KEY)
            .retry(Retry.getNoRetry())
            .timeout(Duration.ofSeconds(30));
        sender = client.createSender(senderOptions);

        // Action & Verify
        StepVerifier.create(sender.send(batch.getEvents(), new SendOptions().maximumSizeInBytes(EventSender.MAX_MESSAGE_LENGTH_BYTES)))
            .verifyComplete();
    }

    @Test
    public void sendSmallEventsFullBatchPartitionKey() {
        skipIfNotRecordMode();

        // Arrange
        // Only Event Data batch has partition key information, none in SendOption and SenderOption
        final EventDataBatch batch = new EventDataBatch(EventSender.MAX_MESSAGE_LENGTH_BYTES, PARTITION_KEY);
        while (batch.tryAdd(new EventData("a".getBytes()))) {
            logger.asInfo().log(String.format("Batch size: %s", batch.getSize()));
        }

        final EventSenderOptions senderOptions = new EventSenderOptions().retry(Retry.getNoRetry()).timeout(Duration.ofSeconds(10));
        sender  = client.createSender(senderOptions);
        // Action & Verify
        StepVerifier.create(sender.send(batch.getEvents(), new SendOptions()))
            .verifyComplete();
    }

    @Ignore
    @Test
    public void sendBatchPartitionKeyValidate() {
        // TODO: after understand EventHubClient Runtime info functionality
        skipIfNotRecordMode();

    }

    @Test
    public void sendEventsFullBatchWithPartitionKey() {
        skipIfNotRecordMode();

        final EventSenderOptions senderOptions = new EventSenderOptions().retry(Retry.getNoRetry()).timeout(Duration.ofSeconds(10));
        sender = client.createSender(senderOptions);
        final EventDataBatch batch = new EventDataBatch(1024, PARTITION_KEY);

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

        StepVerifier.create(sender.send(batch.getEvents(), new SendOptions()))
            .verifyComplete();
    }
}
