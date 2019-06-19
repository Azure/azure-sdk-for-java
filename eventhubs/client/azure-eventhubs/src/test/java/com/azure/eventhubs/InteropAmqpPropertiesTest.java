// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.amqp.MessageConstant;
import com.azure.core.amqp.Retry;
import com.azure.core.implementation.logging.ServiceLogger;
import com.azure.eventhubs.implementation.ApiTestBase;
import com.azure.eventhubs.implementation.ReactorHandlerProvider;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.message.Message;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.function.Consumer;

import static java.nio.charset.StandardCharsets.UTF_8;

public class InteropAmqpPropertiesTest extends ApiTestBase {
    private final ServiceLogger logger = new ServiceLogger(EventReceiverTest.class);

    private static final String PARTITION_ID = "0";
    private static final Message ORIGINAL_MESSAGE = Proton.message();
    private static final String APPLICATION_PROPERTY = "firstProp";
    private static final String MESSAGE_ANNOTATION = "message-annotation-1";
    private static final String PAYLOAD = "testmsg";

    private EventHubClient client;
    private EventSender sender;
    private EventReceiver receiver;
    private EventSenderOptions senderOptions;
    private EventReceiverOptions receiverOptions;
    private ReactorHandlerProvider handlerProvider;
    private EventData resendEventData;

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
     * Test for interoperable with Direct Proton Amqp messaging
     */
    @Ignore
    @Test
    public void interoperableWithDirectProtonAmqpMessage() {
        skipIfNotRecordMode();

        // Arrange
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
        final EventData msgEvent = new EventData(ORIGINAL_MESSAGE);

        // Act
        sender.send(msgEvent);
        // Assert
        StepVerifier.create(receiver.receive())
            .expectNextMatches(event -> {
                validateAmqpPropertiesInEventData.accept(event);
                resendEventData = event;
                return true;
            })
            .verifyComplete();

        // Act
        sender.send(resendEventData);
        // Assert
        StepVerifier.create(receiver.receive())
            .expectNextMatches(event -> {
                validateAmqpPropertiesInEventData.accept(event);
                return true;
            })
            .verifyComplete();
    }

    private final Consumer<EventData> validateAmqpPropertiesInEventData = eData -> {
        Assert.assertTrue(eData.systemProperties().containsKey(MessageConstant.MESSAGE_ID.getValue()));
        Assert.assertEquals(ORIGINAL_MESSAGE.getMessageId(), eData.systemProperties().get(MessageConstant.MESSAGE_ID.getValue()));

        Assert.assertTrue(eData.systemProperties().containsKey(MessageConstant.USER_ID.getValue()));
        Assert.assertEquals(new String(ORIGINAL_MESSAGE.getUserId()), new String((byte[]) eData.systemProperties().get(MessageConstant.USER_ID.getValue())));

        Assert.assertTrue(eData.systemProperties().containsKey(MessageConstant.TO.getValue()));
        Assert.assertEquals(ORIGINAL_MESSAGE.getAddress(), eData.systemProperties().get(MessageConstant.TO.getValue()));

        Assert.assertTrue(eData.systemProperties().containsKey(MessageConstant.CONTENT_TYPE.getValue()));
        Assert.assertEquals(ORIGINAL_MESSAGE.getContentType(), eData.systemProperties().get(MessageConstant.CONTENT_TYPE.getValue()));

        Assert.assertTrue(eData.systemProperties().containsKey(MessageConstant.CONTENT_ENCODING.getValue()));
        Assert.assertEquals(ORIGINAL_MESSAGE.getContentEncoding(), eData.systemProperties().get(MessageConstant.CONTENT_ENCODING.getValue()));

        Assert.assertTrue(eData.systemProperties().containsKey(MessageConstant.CORRELATION_ID.getValue()));
        Assert.assertEquals(ORIGINAL_MESSAGE.getCorrelationId(), eData.systemProperties().get(MessageConstant.CORRELATION_ID.getValue()));

        Assert.assertTrue(eData.systemProperties().containsKey(MessageConstant.CREATION_TIME.getValue()));
        Assert.assertEquals(ORIGINAL_MESSAGE.getCreationTime(), eData.systemProperties().get(MessageConstant.CREATION_TIME.getValue()));

        Assert.assertTrue(eData.systemProperties().containsKey(MessageConstant.SUBJECT.getValue()));
        Assert.assertEquals(ORIGINAL_MESSAGE.getSubject(), eData.systemProperties().get(MessageConstant.SUBJECT.getValue()));

        Assert.assertTrue(eData.systemProperties().containsKey(MessageConstant.GROUP_ID.getValue()));
        Assert.assertEquals(ORIGINAL_MESSAGE.getGroupId(), eData.systemProperties().get(MessageConstant.GROUP_ID.getValue()));

        Assert.assertTrue(eData.systemProperties().containsKey(MessageConstant.REPLY_TO_GROUP_ID.getValue()));
        Assert.assertEquals(ORIGINAL_MESSAGE.getReplyToGroupId(), eData.systemProperties().get(MessageConstant.REPLY_TO_GROUP_ID.getValue()));

        Assert.assertTrue(eData.systemProperties().containsKey(MessageConstant.REPLY_TO.getValue()));
        Assert.assertEquals(ORIGINAL_MESSAGE.getReplyTo(), eData.systemProperties().get(MessageConstant.REPLY_TO.getValue()));

        Assert.assertTrue(eData.systemProperties().containsKey(MessageConstant.ABSOLUTE_EXPRITY_TIME.getValue()));
        Assert.assertEquals(ORIGINAL_MESSAGE.getExpiryTime(), eData.systemProperties().get(MessageConstant.ABSOLUTE_EXPRITY_TIME.getValue()));

        Assert.assertTrue(eData.systemProperties().containsKey(MESSAGE_ANNOTATION));
        Assert.assertEquals(ORIGINAL_MESSAGE.getMessageAnnotations().getValue().get(Symbol.getSymbol(MESSAGE_ANNOTATION)), eData.systemProperties().get(MESSAGE_ANNOTATION));

        Assert.assertTrue(eData.properties().containsKey(APPLICATION_PROPERTY));
        Assert.assertEquals(ORIGINAL_MESSAGE.getApplicationProperties().getValue().get(APPLICATION_PROPERTY), eData.properties().get(APPLICATION_PROPERTY));

        Assert.assertEquals(1, eData.properties().size());
        Assert.assertEquals(PAYLOAD, UTF_8.decode(eData.body()).toString());
    };
}
