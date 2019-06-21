// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.Retry;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.ApiTestBase;
import com.azure.messaging.eventhubs.implementation.ReactorHandlerProvider;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.messaging.AmqpSequence;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.message.Message;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedList;

import static java.nio.charset.StandardCharsets.UTF_8;

public class InteropEventBodyTest extends ApiTestBase {
    private static final String PARTITION_ID = "0";
    private static final String PAYLOAD = "testmsg";

    private final ClientLogger logger = new ClientLogger(InteropEventBodyTest.class);

    private EventHubClient client;
    private EventHubProducer producer;
    private EventHubConsumer consumer;
    private EventData receivedEvent;

    @Rule
    public TestName testName = new TestName();

    @Override
    protected String testName() {
        return testName.getMethodName();
    }

    @Override
    protected void beforeTest() {
        logger.asInfo().log("[{}]: Performing test set-up.", testName.getMethodName());

        final ReactorHandlerProvider handlerProvider = new ReactorHandlerProvider(getReactorProvider());
        client = new EventHubClient(getConnectionOptions(), getReactorProvider(), handlerProvider);
        final EventHubProducerOptions producerOptions = new EventHubProducerOptions().partitionId(PARTITION_ID).retry(Retry.getNoRetry()).timeout(Duration.ofSeconds(30));
        producer = client.createProducer(producerOptions);
        consumer = client.createConsumer(getConsumerGroupName(), PARTITION_ID, EventPosition.latest());
    }

    @Override
    protected void afterTest() {
        logger.asInfo().log("[{}]: Performing test clean-up.", testName.getMethodName());
        closeClient(client, producer, consumer, testName, logger);
    }

    /**
     * Test for interoperable with Proton Amqp messaging body as AMQP value.
     */
    @Ignore
    @Test
    public void interopWithProtonAmqpMessageBodyAsAmqpValue() {
        skipIfNotRecordMode();

        // Arrange
        final Message originalMessage = Proton.message();
        originalMessage.setBody(new AmqpValue(PAYLOAD));
        originalMessage.setMessageAnnotations(new MessageAnnotations(new HashMap<>()));
        final EventData msgEvent = new EventData(originalMessage);

        // Act & Assert
        StepVerifier.create(consumer.receive().take(1))
            .then(() -> producer.send(msgEvent).block(TIMEOUT))
            .assertNext(event -> {
                Assert.assertEquals(PAYLOAD, UTF_8.decode(event.body()).toString());
                receivedEvent = event;
            })
            .verifyComplete();

        StepVerifier.create(consumer.receive().take(1))
            .then(() -> producer.send(receivedEvent).block(TIMEOUT))
            .assertNext(event -> Assert.assertEquals(PAYLOAD, UTF_8.decode(event.body()).toString()))
            .verifyComplete();
    }

    /**
     * Test for interoperable with Proton Amqp messaging body as sequence.
     */
    @Ignore("can't convert ByteBuffer to List<Data>")
    @Test
    public void interopWithProtonAmqpMessageBodyAsAmqpSequence() {
        skipIfNotRecordMode();

        // TODO: Refactor it
        final LinkedList<Data> dataList = new LinkedList<>();
        dataList.add(new Data(new Binary(PAYLOAD.getBytes())));

        final Message originalMessage = Proton.message();
        originalMessage.setBody(new AmqpSequence(dataList));
        originalMessage.setMessageAnnotations(new MessageAnnotations(new HashMap<>()));
        final EventData msgEvent = new EventData(originalMessage);

        StepVerifier.create(consumer.receive().take(1))
            .then(() -> producer.send(msgEvent).block(TIMEOUT))
            .assertNext(event -> {
                // TODO: can't convert ByteBuffer to List<Data>
                PAYLOAD.equals(UTF_8.decode(event.body()).toString());
            })
            .verifyComplete();

        StepVerifier.create(consumer.receive().take(1))
            .then(() -> producer.send(receivedEvent).block(TIMEOUT))
            .assertNext(v -> PAYLOAD.equals(UTF_8.decode(v.body()).toString()))
            .verifyComplete();
    }
}
