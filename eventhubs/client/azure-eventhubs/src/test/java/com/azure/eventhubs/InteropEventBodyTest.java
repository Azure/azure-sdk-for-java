// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.amqp.Retry;
import com.azure.core.util.logging.ClientLogger;
import com.azure.eventhubs.implementation.ApiTestBase;
import com.azure.eventhubs.implementation.ReactorHandlerProvider;
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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import static java.nio.charset.StandardCharsets.UTF_8;

public class InteropEventBodyTest extends ApiTestBase {
    private static final String PARTITION_ID = "0";
    private static final String PAYLOAD = "testmsg";

    private final ClientLogger logger = new ClientLogger(InteropEventBodyTest.class);

    private EventHubClient client;
    private EventHubProducer sender;
    private EventHubConsumer receiver;
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
        final EventHubProducerOptions senderOptions = new EventHubProducerOptions().partitionId(PARTITION_ID);
        final EventHubConsumerOptions receiverOptions = new EventHubConsumerOptions().retry(Retry.getNoRetry());
        sender = client.createProducer(senderOptions);
        receiver = client.createConsumer(getConsumerGroupName(), PARTITION_ID, EventPosition.latest(), receiverOptions);
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
                logger.asError().log("[{}]: Sender doesn't close properly.", testName.getMethodName(), e);
            }
        }

        if (receiver != null) {
            try {
                receiver.close();
            } catch (IOException e) {
                logger.asError().log("[{}]: Receiver doesn't close properly.", testName.getMethodName(), e);
            }
        }
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
        StepVerifier.create(receiver.receive())
            .then(() -> sender.send(msgEvent))
            .assertNext(event -> {
                Assert.assertEquals(PAYLOAD, UTF_8.decode(event.body()).toString());
                receivedEvent = event;
            })
            .verifyComplete();

        StepVerifier.create(receiver.receive())
            .then(() -> sender.send(receivedEvent))
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


        StepVerifier.create(receiver.receive())
            .then(() -> sender.send(Mono.just(msgEvent)))
            .expectNextMatches(event -> {
                // TODO: can't convert ByteBuffer to List<Data>
                String val = UTF_8.decode(event.body()).toString();
                return PAYLOAD.equals(UTF_8.decode(event.body()).toString());
            })
            .verifyComplete();

        StepVerifier.create(receiver.receive())
            .then(() -> sender.send(receivedEvent))
            .expectNextMatches(v -> PAYLOAD.equals(UTF_8.decode(v.body()).toString()))
            .verifyComplete();
    }
}
