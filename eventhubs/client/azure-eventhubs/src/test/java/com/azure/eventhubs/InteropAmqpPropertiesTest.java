// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.amqp.MessageConstant;
import com.azure.eventhubs.implementation.ApiTestBase;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.message.Message;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.function.Consumer;

import static java.nio.charset.StandardCharsets.UTF_8;

public class InteropAmqpPropertiesTest extends ApiTestBase {
    private static final String PARTITION_ID = "0";
    private static final Message ORIGINAL_MESSAGE = Proton.message();
    private static final String APPLICATION_PROPERTY = "firstProp";
    private static final String MESSAGE_ANNOTATION = "message-annotation-1";
    private static final String PAYLOAD = "testmsg";

    private static EventHubClient ehClient;
    private static EventSender sender;
    private static EventReceiver receiver;
    private static EventData receivedEvent;
    private static EventData reSentAndReceivedEvent;
    private static Flux<EventData> receivedEventData;
    private static EventData msgEvent;

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

        final HashMap<String, Object> appProperties = new HashMap<>();
        appProperties.put(APPLICATION_PROPERTY, "value1");
        final ApplicationProperties applicationProperties = new ApplicationProperties(appProperties);
        ORIGINAL_MESSAGE.setApplicationProperties(applicationProperties);

        ORIGINAL_MESSAGE.setMessageId("id1");
        ORIGINAL_MESSAGE.setUserId("user1".getBytes());
        ORIGINAL_MESSAGE.setAddress("eventhub1");
        ORIGINAL_MESSAGE.setSubject("sub");
        ORIGINAL_MESSAGE.setReplyTo("replyingTo");
        ORIGINAL_MESSAGE.setExpiryTime(456L);
        ORIGINAL_MESSAGE.setGroupSequence(5555L);
        ORIGINAL_MESSAGE.setContentType("events");
        ORIGINAL_MESSAGE.setContentEncoding("UTF-8");
        ORIGINAL_MESSAGE.setCorrelationId("corid1");
        ORIGINAL_MESSAGE.setCreationTime(345L);
        ORIGINAL_MESSAGE.setGroupId("gid");
        ORIGINAL_MESSAGE.setReplyToGroupId("replyToGroupId");

        ORIGINAL_MESSAGE.setMessageAnnotations(new MessageAnnotations(new HashMap<>()));
        ORIGINAL_MESSAGE.getMessageAnnotations().getValue().put(Symbol.getSymbol(MESSAGE_ANNOTATION), "messageAnnotationValue");

        ORIGINAL_MESSAGE.setBody(new Data(Binary.create(ByteBuffer.wrap(PAYLOAD.getBytes()))));

        msgEvent = new EventData(ORIGINAL_MESSAGE);

        //TODO: delete it after receive() is fully implemented
        receiver.setTestEventData(msgEvent);

        receivedEventData = receiver.receive();
    }

    @AfterClass
    public static void cleanup() {
        if (ehClient != null) {
            ehClient.close();
        }
    }

    @Test
    public void interopWithDirectProtonAmqpMessage() {
        EventData msgEvent = new EventData(ORIGINAL_MESSAGE);
        StepVerifier.create(receivedEventData)
            .then(() -> sender.send(Mono.just(msgEvent)))
            .expectNextMatches(event -> {
                validateAmqpPropertiesInEventData.accept(event);
                receivedEvent = event;
                return true;
            })
            .verifyComplete();

        StepVerifier.create(receivedEventData)
            .then(() -> sender.send(receivedEventData))
            .expectNextMatches(event -> {
                validateAmqpPropertiesInEventData.accept(event);
                reSentAndReceivedEvent = event;
                return true;
            })
            .verifyComplete();
    }

    private final Consumer<EventData> validateAmqpPropertiesInEventData = eData -> {
        Assert.assertTrue(eData.systemProperties().containsKey(MessageConstant.MESSAGE_ID.getValue())
            && eData.systemProperties().get(MessageConstant.MESSAGE_ID.getValue()).equals(ORIGINAL_MESSAGE.getMessageId()));
        Assert.assertTrue(eData.systemProperties().containsKey(MessageConstant.USER_ID.getValue())
            && new String((byte[]) eData.systemProperties().get(MessageConstant.USER_ID.getValue())).equals(new String(ORIGINAL_MESSAGE.getUserId())));
        Assert.assertTrue(eData.systemProperties().containsKey(MessageConstant.TO.getValue())
            && eData.systemProperties().get(MessageConstant.TO.getValue()).equals(ORIGINAL_MESSAGE.getAddress()));
        Assert.assertTrue(eData.systemProperties().containsKey(MessageConstant.CONTENT_TYPE.getValue())
            && eData.systemProperties().get(MessageConstant.CONTENT_TYPE.getValue()).equals(ORIGINAL_MESSAGE.getContentType()));
        Assert.assertTrue(eData.systemProperties().containsKey(MessageConstant.CONTENT_ENCODING.getValue())
            && eData.systemProperties().get(MessageConstant.CONTENT_ENCODING.getValue()).equals(ORIGINAL_MESSAGE.getContentEncoding()));
        Assert.assertTrue(eData.systemProperties().containsKey(MessageConstant.CORRELATION_ID.getValue())
            && eData.systemProperties().get(MessageConstant.CORRELATION_ID.getValue()).equals(ORIGINAL_MESSAGE.getCorrelationId()));
        Assert.assertTrue(eData.systemProperties().containsKey(MessageConstant.CREATION_TIME.getValue())
            && eData.systemProperties().get(MessageConstant.CREATION_TIME.getValue()).equals(ORIGINAL_MESSAGE.getCreationTime()));
        Assert.assertTrue(eData.systemProperties().containsKey(MessageConstant.SUBJECT.getValue())
            && eData.systemProperties().get(MessageConstant.SUBJECT.getValue()).equals(ORIGINAL_MESSAGE.getSubject()));
        Assert.assertTrue(eData.systemProperties().containsKey(MessageConstant.GROUP_ID.getValue())
            && eData.systemProperties().get(MessageConstant.GROUP_ID.getValue()).equals(ORIGINAL_MESSAGE.getGroupId()));
        Assert.assertTrue(eData.systemProperties().containsKey(MessageConstant.REPLY_TO_GROUP_ID.getValue())
            && eData.systemProperties().get(MessageConstant.REPLY_TO_GROUP_ID.getValue()).equals(ORIGINAL_MESSAGE.getReplyToGroupId()));
        Assert.assertTrue(eData.systemProperties().containsKey(MessageConstant.REPLY_TO.getValue())
            && eData.systemProperties().get(MessageConstant.REPLY_TO.getValue()).equals(ORIGINAL_MESSAGE.getReplyTo()));
        Assert.assertTrue(eData.systemProperties().containsKey(MessageConstant.ABSOLUTE_EXPRITY_TIME.getValue())
            && eData.systemProperties().get(MessageConstant.ABSOLUTE_EXPRITY_TIME.getValue()).equals(ORIGINAL_MESSAGE.getExpiryTime()));

        Assert.assertTrue(eData.systemProperties().containsKey(MESSAGE_ANNOTATION)
            && eData.systemProperties().get(MESSAGE_ANNOTATION).equals(ORIGINAL_MESSAGE.getMessageAnnotations().getValue().get(Symbol.getSymbol(MESSAGE_ANNOTATION))));

        Assert.assertTrue(eData.properties().containsKey(APPLICATION_PROPERTY)
            && eData.properties().get(APPLICATION_PROPERTY).equals(ORIGINAL_MESSAGE.getApplicationProperties().getValue().get(APPLICATION_PROPERTY)));
        Assert.assertTrue(eData.properties().size() == 1);
        Assert.assertTrue(PAYLOAD.equals(UTF_8.decode(eData.body()).toString()));
    };
}
