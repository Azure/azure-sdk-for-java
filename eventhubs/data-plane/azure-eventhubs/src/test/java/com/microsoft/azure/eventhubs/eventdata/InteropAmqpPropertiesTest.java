// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.eventdata;

import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.EventPosition;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.eventhubs.PartitionSender;
import com.microsoft.azure.eventhubs.impl.AmqpConstants;
import com.microsoft.azure.eventhubs.impl.MessageReceiver;
import com.microsoft.azure.eventhubs.impl.MessageSender;
import com.microsoft.azure.eventhubs.impl.MessagingFactory;
import com.microsoft.azure.eventhubs.impl.ReceiverSettingsProvider;
import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.lib.TestContext;
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
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class InteropAmqpPropertiesTest extends ApiTestBase {
    private static final String PARTITION_ID = "0";
    private static final Message ORIGINAL_MESSAGE = Proton.message();
    private static final String APPLICATION_PROPERTY = "firstProp";
    private static final String MESSAGE_ANNOTATION = "message-annotation-1";
    private static final String PAYLOAD = "testmsg";

    private static EventHubClient ehClient;
    private static MessagingFactory msgFactory;
    private static PartitionReceiver receiver;
    private static MessageReceiver msgReceiver;
    private static MessageSender partitionMsgSender;
    private static PartitionSender partitionEventSender;
    private static EventData receivedEvent;
    private static EventData reSentAndReceivedEvent;
    private static Message reSendAndReceivedMessage;

    private final Consumer<EventData> validateAmqpPropertiesInEventData = new Consumer<EventData>() {
        @Override
        public void accept(EventData eData) {
            Assert.assertTrue(eData.getSystemProperties().containsKey(AmqpConstants.AMQP_PROPERTY_MESSAGE_ID)
                    && eData.getSystemProperties().get(AmqpConstants.AMQP_PROPERTY_MESSAGE_ID).equals(ORIGINAL_MESSAGE.getMessageId()));
            Assert.assertTrue(eData.getSystemProperties().containsKey(AmqpConstants.AMQP_PROPERTY_USER_ID)
                    && new String((byte[]) eData.getSystemProperties().get(AmqpConstants.AMQP_PROPERTY_USER_ID)).equals(new String(ORIGINAL_MESSAGE.getUserId())));
            Assert.assertTrue(eData.getSystemProperties().containsKey(AmqpConstants.AMQP_PROPERTY_TO)
                    && eData.getSystemProperties().get(AmqpConstants.AMQP_PROPERTY_TO).equals(ORIGINAL_MESSAGE.getAddress()));
            Assert.assertTrue(eData.getSystemProperties().containsKey(AmqpConstants.AMQP_PROPERTY_CONTENT_TYPE)
                    && eData.getSystemProperties().get(AmqpConstants.AMQP_PROPERTY_CONTENT_TYPE).equals(ORIGINAL_MESSAGE.getContentType()));
            Assert.assertTrue(eData.getSystemProperties().containsKey(AmqpConstants.AMQP_PROPERTY_CONTENT_ENCODING)
                    && eData.getSystemProperties().get(AmqpConstants.AMQP_PROPERTY_CONTENT_ENCODING).equals(ORIGINAL_MESSAGE.getContentEncoding()));
            Assert.assertTrue(eData.getSystemProperties().containsKey(AmqpConstants.AMQP_PROPERTY_CORRELATION_ID)
                    && eData.getSystemProperties().get(AmqpConstants.AMQP_PROPERTY_CORRELATION_ID).equals(ORIGINAL_MESSAGE.getCorrelationId()));
            Assert.assertTrue(eData.getSystemProperties().containsKey(AmqpConstants.AMQP_PROPERTY_CREATION_TIME)
                    && eData.getSystemProperties().get(AmqpConstants.AMQP_PROPERTY_CREATION_TIME).equals(ORIGINAL_MESSAGE.getCreationTime()));
            Assert.assertTrue(eData.getSystemProperties().containsKey(AmqpConstants.AMQP_PROPERTY_SUBJECT)
                    && eData.getSystemProperties().get(AmqpConstants.AMQP_PROPERTY_SUBJECT).equals(ORIGINAL_MESSAGE.getSubject()));
            Assert.assertTrue(eData.getSystemProperties().containsKey(AmqpConstants.AMQP_PROPERTY_GROUP_ID)
                    && eData.getSystemProperties().get(AmqpConstants.AMQP_PROPERTY_GROUP_ID).equals(ORIGINAL_MESSAGE.getGroupId()));
            Assert.assertTrue(eData.getSystemProperties().containsKey(AmqpConstants.AMQP_PROPERTY_REPLY_TO_GROUP_ID)
                    && eData.getSystemProperties().get(AmqpConstants.AMQP_PROPERTY_REPLY_TO_GROUP_ID).equals(ORIGINAL_MESSAGE.getReplyToGroupId()));
            Assert.assertTrue(eData.getSystemProperties().containsKey(AmqpConstants.AMQP_PROPERTY_REPLY_TO)
                    && eData.getSystemProperties().get(AmqpConstants.AMQP_PROPERTY_REPLY_TO).equals(ORIGINAL_MESSAGE.getReplyTo()));
            Assert.assertTrue(eData.getSystemProperties().containsKey(AmqpConstants.AMQP_PROPERTY_ABSOLUTE_EXPRITY_TIME)
                    && eData.getSystemProperties().get(AmqpConstants.AMQP_PROPERTY_ABSOLUTE_EXPRITY_TIME).equals(ORIGINAL_MESSAGE.getExpiryTime()));

            Assert.assertTrue(eData.getSystemProperties().containsKey(MESSAGE_ANNOTATION)
                    && eData.getSystemProperties().get(MESSAGE_ANNOTATION).equals(ORIGINAL_MESSAGE.getMessageAnnotations().getValue().get(Symbol.getSymbol(MESSAGE_ANNOTATION))));

            Assert.assertTrue(eData.getProperties().containsKey(APPLICATION_PROPERTY)
                    && eData.getProperties().get(APPLICATION_PROPERTY).equals(ORIGINAL_MESSAGE.getApplicationProperties().getValue().get(APPLICATION_PROPERTY)));

            Assert.assertTrue(eData.getProperties().size() == 1);

            Assert.assertTrue(new String(eData.getBytes()).equals(PAYLOAD));
        }
    };

    @BeforeClass
    public static void initialize() throws EventHubException, IOException, InterruptedException, ExecutionException {
        final ConnectionStringBuilder connStrBuilder = TestContext.getConnectionString();
        final String connectionString = connStrBuilder.toString();

        ehClient = EventHubClient.createSync(connectionString, TestContext.EXECUTOR_SERVICE);
        msgFactory = MessagingFactory.createFromConnectionString(connectionString, TestContext.EXECUTOR_SERVICE).get();
        receiver = ehClient.createReceiverSync(TestContext.getConsumerGroupName(), PARTITION_ID, EventPosition.fromEnqueuedTime(Instant.now()));
        partitionMsgSender = MessageSender.create(msgFactory, "link1", connStrBuilder.getEventHubName() + "/partitions/" + PARTITION_ID).get();
        partitionEventSender = ehClient.createPartitionSenderSync(PARTITION_ID);

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

        partitionMsgSender.send(ORIGINAL_MESSAGE).get();
        receivedEvent = receiver.receiveSync(10).iterator().next();

        partitionEventSender.sendSync(receivedEvent);
        reSentAndReceivedEvent = receiver.receiveSync(10).iterator().next();

        partitionEventSender.sendSync(reSentAndReceivedEvent);
        msgReceiver = MessageReceiver.create(
                msgFactory,
                "receiver1",
                connStrBuilder.getEventHubName() + "/ConsumerGroups/" + TestContext.getConsumerGroupName() + "/Partitions/" + PARTITION_ID,
                100,
                (ReceiverSettingsProvider) ehClient.createReceiver(TestContext.getConsumerGroupName(), PARTITION_ID, EventPosition.fromOffset(reSentAndReceivedEvent.getSystemProperties().getOffset(), false)).get()).get();

        reSendAndReceivedMessage = msgReceiver.receive(10).get().iterator().next();
    }

    @AfterClass
    public static void cleanup() throws EventHubException {
        if (msgReceiver != null) {
            msgReceiver.closeSync();
        }

        if (partitionEventSender != null) {
            partitionEventSender.closeSync();
        }

        if (partitionMsgSender != null) {
            partitionMsgSender.closeSync();
        }

        if (receiver != null) {
            receiver.closeSync();
        }

        if (ehClient != null) {
            ehClient.closeSync();
        }

        if (msgFactory != null) {
            msgFactory.closeSync();
        }
    }

    @Test
    public void interopWithDirectProtonAmqpMessage() {
        validateAmqpPropertiesInEventData.accept(receivedEvent);
    }

    @Test
    public void interopWithDirectProtonEventDataReSend() {
        validateAmqpPropertiesInEventData.accept(reSentAndReceivedEvent);
    }

    @Test
    public void resentAmqpMessageShouldRetainAllOriginalProps() {
        Assert.assertTrue(reSendAndReceivedMessage.getMessageId().equals(ORIGINAL_MESSAGE.getMessageId()));
        Assert.assertTrue(reSendAndReceivedMessage.getAddress().equals(ORIGINAL_MESSAGE.getAddress()));
        Assert.assertTrue(reSendAndReceivedMessage.getContentEncoding().equals(ORIGINAL_MESSAGE.getContentEncoding()));
        Assert.assertTrue(reSendAndReceivedMessage.getContentType().equals(ORIGINAL_MESSAGE.getContentType()));
        Assert.assertTrue(new String(reSendAndReceivedMessage.getUserId()).equals(new String(ORIGINAL_MESSAGE.getUserId())));
        Assert.assertTrue(reSendAndReceivedMessage.getCorrelationId().equals(ORIGINAL_MESSAGE.getCorrelationId()));
        Assert.assertTrue(reSendAndReceivedMessage.getGroupId().equals(ORIGINAL_MESSAGE.getGroupId()));
        Assert.assertTrue(reSendAndReceivedMessage.getReplyTo().equals(ORIGINAL_MESSAGE.getReplyTo()));
        Assert.assertTrue(reSendAndReceivedMessage.getReplyToGroupId().equals(ORIGINAL_MESSAGE.getReplyToGroupId()));
        Assert.assertTrue(reSendAndReceivedMessage.getSubject().equals(ORIGINAL_MESSAGE.getSubject()));
        Assert.assertTrue(reSendAndReceivedMessage.getExpiryTime() == ORIGINAL_MESSAGE.getExpiryTime());
        Assert.assertTrue(reSendAndReceivedMessage.getCreationTime() == ORIGINAL_MESSAGE.getCreationTime());
        Assert.assertTrue(reSendAndReceivedMessage.getExpiryTime() == ORIGINAL_MESSAGE.getExpiryTime());
        Assert.assertTrue(reSendAndReceivedMessage.getGroupSequence() == ORIGINAL_MESSAGE.getGroupSequence());

        Assert.assertTrue(reSendAndReceivedMessage.getApplicationProperties().getValue().get(APPLICATION_PROPERTY)
                .equals(ORIGINAL_MESSAGE.getApplicationProperties().getValue().get(APPLICATION_PROPERTY)));

        Assert.assertTrue(reSendAndReceivedMessage.getMessageAnnotations().getValue().get(Symbol.getSymbol(MESSAGE_ANNOTATION))
                .equals(ORIGINAL_MESSAGE.getMessageAnnotations().getValue().get(Symbol.getSymbol(MESSAGE_ANNOTATION))));

        Binary payloadBytes = ((Data) reSendAndReceivedMessage.getBody()).getValue();
        Assert.assertTrue(new String(payloadBytes.getArray(), payloadBytes.getArrayOffset(), payloadBytes.getLength()).equals(PAYLOAD));
    }
}
