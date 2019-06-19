// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.amqp.Retry;
import com.azure.core.implementation.logging.ServiceLogger;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import static java.nio.charset.StandardCharsets.UTF_8;

public class InteropEventBodyTest extends ApiTestBase {
    private final ServiceLogger logger = new ServiceLogger(EventReceiverTest.class);

    private static final String PARTITION_ID = "0";
    private static final String PAYLOAD = "testmsg";
    private static final Message ORIGINAL_MESSAGE = Proton.message();

    private static EventData msgEvent;
    private static EventData receivedEvent;

    private EventHubClient client;
    private EventSender sender;
    private EventReceiver receiver;
    private EventSenderOptions senderOptions;
    private EventReceiverOptions receiverOptions;
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
        receiverOptions = new EventReceiverOptions().consumerGroup(getConsumerGroupName()).retry(Retry.getNoRetry());
        sender = client.createSender(senderOptions);
        receiver = client.createReceiver(PARTITION_ID, EventPosition.latest(), receiverOptions);
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
     * Test for interoperable with Proton Amqp messaging body as AMQP value.
     */
    @Ignore
    @Test
    public void interopWithProtonAmqpMessageBodyAsAmqpValue() {
        skipIfNotRecordMode();

        // Arrange
        ORIGINAL_MESSAGE.setBody(new AmqpValue(PAYLOAD));
        ORIGINAL_MESSAGE.setMessageAnnotations(new MessageAnnotations(new HashMap<>()));
        msgEvent = new EventData(ORIGINAL_MESSAGE);
        // Action & Verify
        sender.send(msgEvent);
        StepVerifier.create(receiver.receive())
            .expectNextMatches(event -> {
                Assert.assertTrue(PAYLOAD.equals(UTF_8.decode(event.body()).toString()));
                receivedEvent = event;
                return true;
            })
            .verifyComplete();

        sender.send(receivedEvent);
        StepVerifier.create(receiver.receive())
            .expectNextMatches(event -> {
                Assert.assertTrue(PAYLOAD.equals(UTF_8.decode(event.body()).toString()));
                return true;
            })
            .verifyComplete();
    }

    /**
     * Test for interoperable with Proton Amqp messaging body as sequence.
     */
    @Ignore
    @Test
    public void interopWithProtonAmqpMessageBodyAsAmqpSequence() {
        skipIfNotRecordMode();

        // TODO: Refactor it
        final Message originalMessage = Proton.message();
        final LinkedList<Data> datas = new LinkedList<>();

        datas.add(new Data(new Binary(PAYLOAD.getBytes())));
        originalMessage.setBody(new AmqpSequence(datas));
        originalMessage.setMessageAnnotations(new MessageAnnotations(new HashMap<>()));

        final EventData msgEvent = new EventData(originalMessage);

        final Flux<EventData> receivedEvent = receiver.receive();

        StepVerifier.create(receivedEvent)
            .then(() -> sender.send(Mono.just(msgEvent)))
            .expectNextMatches(event -> {
                // TODO: can't convert ByteBuffer to List<Data>
                String val = UTF_8.decode(event.body()).toString();
                return PAYLOAD.equals(UTF_8.decode(event.body()).toString());
            })
            .verifyComplete();

        StepVerifier.create(receivedEvent)
            .then(() -> sender.send(receivedEvent))
            .expectNextMatches(v -> PAYLOAD.equals(UTF_8.decode(v.body()).toString()))
            .verifyComplete();
    }
}
