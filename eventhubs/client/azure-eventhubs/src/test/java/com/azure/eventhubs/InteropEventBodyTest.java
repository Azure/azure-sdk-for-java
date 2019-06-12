// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.eventhubs.implementation.ApiTestBase;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.messaging.AmqpSequence;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.message.Message;
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

import java.util.HashMap;
import java.util.LinkedList;

import static java.nio.charset.StandardCharsets.UTF_8;

public class InteropEventBodyTest extends ApiTestBase {
    private static final String PARTITION_ID = "0";
    private static final String PAYLOAD = "testmsg";
    private static final Message ORIGINAL_MESSAGE = Proton.message();

    private static EventHubClient ehClient;
    private static EventSender sender;
    private static EventReceiver receiver;
    private static EventData msgEvent;
    private static EventData receivedEvent;

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
    public static void cleanup() {
        if (ehClient != null) {
            ehClient.close();
        }
    }

    @Ignore
    @Test
    public void interopWithProtonAmqpMessageBodyAsAmqpValue() {
        ORIGINAL_MESSAGE.setBody(new AmqpValue(PAYLOAD));
        ORIGINAL_MESSAGE.setMessageAnnotations(new MessageAnnotations(new HashMap<>()));
        msgEvent = new EventData(ORIGINAL_MESSAGE);

        //TODO: delete it after receive() is fully implemented
        receiver.setTestEventData(msgEvent);

        Flux<EventData> receivedEventData = receiver.receive();

        StepVerifier.create(receivedEventData)
            .then(() -> sender.send(Mono.just(msgEvent)))
            .expectNextMatches(event -> {
                Assert.assertTrue(PAYLOAD.equals(UTF_8.decode(event.body()).toString()));
                receivedEvent = event;
                return true;
            })
            .verifyComplete();

        StepVerifier.create(receivedEventData)
            .then(() -> sender.send(Mono.just(receivedEvent)))
            .expectNextMatches(event -> {
                Assert.assertTrue(PAYLOAD.equals(UTF_8.decode(event.body()).toString()));
                return true;
            })
            .verifyComplete();
    }

    @Ignore
    @Test
    public void interopWithProtonAmqpMessageBodyAsAmqpSequence() {
        final Message originalMessage = Proton.message();
        final LinkedList<Data> datas = new LinkedList<>();

        datas.add(new Data(new Binary(PAYLOAD.getBytes())));
        originalMessage.setBody(new AmqpSequence(datas));
        originalMessage.setMessageAnnotations(new MessageAnnotations(new HashMap<>()));

        final EventData msgEvent = new EventData(originalMessage);


        //TODO: delete it after receive() is fully implemented
        receiver.setTestEventData(msgEvent);


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
