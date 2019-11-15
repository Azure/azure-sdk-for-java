// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.MessageConstant;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.SendOptions;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.core.amqp.MessageConstant.ENQUEUED_TIME_UTC_ANNOTATION_NAME;
import static com.azure.core.amqp.MessageConstant.OFFSET_ANNOTATION_NAME;
import static com.azure.core.amqp.MessageConstant.SEQUENCE_NUMBER_ANNOTATION_NAME;
import static com.azure.messaging.eventhubs.TestUtils.MESSAGE_TRACKING_ID;
import static com.azure.messaging.eventhubs.TestUtils.getSymbol;
import static com.azure.messaging.eventhubs.TestUtils.isMatchingEvent;
import static java.nio.charset.StandardCharsets.UTF_8;

public class InteropAmqpPropertiesTest extends IntegrationTestBase {
    private static final String PARTITION_ID = "0";
    private static final String PAYLOAD = "test-message";

    private final MessageSerializer serializer = new EventHubMessageSerializer();
    private EventHubAsyncClient client;
    private EventHubProducerAsyncClient producer;
    private EventHubConsumerAsyncClient consumer;
    private SendOptions sendOptions;

    public InteropAmqpPropertiesTest() {
        super(new ClientLogger(InteropAmqpPropertiesTest.class));
    }

    @Override
    protected void beforeTest() {
        sendOptions = new SendOptions().setPartitionId(PARTITION_ID);

        client = createBuilder().buildAsyncClient();
        producer = client.createProducer();
        consumer = client.createConsumer(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME, EventPosition.latest());
    }

    @Override
    protected void afterTest() {
        dispose(producer, consumer, client);
    }

    /**
     * Test for interoperable with Direct Proton AMQP messaging
     */
    @Test
    public void interoperableWithDirectProtonAmqpMessage() {
        // Arrange
        final AtomicReference<EventData> receivedEventData = new AtomicReference<>();
        final String messageTrackingValue = UUID.randomUUID().toString();

        final HashMap<String, Object> applicationProperties = new HashMap<>();
        applicationProperties.put(MESSAGE_TRACKING_ID, messageTrackingValue);
        applicationProperties.put("first-property", "value-1");

        final Message message = Proton.message();
        message.setApplicationProperties(new ApplicationProperties(applicationProperties));

        message.setMessageId("id1");
        message.setUserId("user1".getBytes());
        message.setAddress("event-hub-address");
        message.setSubject("sub");
        message.setReplyTo("replyingTo");
        message.setExpiryTime(456L);
        message.setGroupSequence(5555L);
        message.setContentType("events");
        message.setContentEncoding("UTF-8");
        message.setCorrelationId("correlation-id-1");
        message.setCreationTime(345L);
        message.setGroupId("group-id");
        message.setReplyToGroupId("replyToGroupId");

        final Map<Symbol, Object> expectedAnnotations = new HashMap<>();
        expectedAnnotations.put(Symbol.getSymbol("message-annotation-1"), "messageAnnotationValue");

        final Map<Symbol, Object> messageAnnotations = new HashMap<>(expectedAnnotations);
        messageAnnotations.put(getSymbol(OFFSET_ANNOTATION_NAME), "100");
        messageAnnotations.put(getSymbol(ENQUEUED_TIME_UTC_ANNOTATION_NAME), Date.from(Instant.now()));
        messageAnnotations.put(getSymbol(SEQUENCE_NUMBER_ANNOTATION_NAME), 15L);

        message.setMessageAnnotations(new MessageAnnotations(messageAnnotations));

        message.setBody(new Data(Binary.create(ByteBuffer.wrap(PAYLOAD.getBytes()))));
        final EventData msgEvent = serializer.deserialize(message, EventData.class);

        // Act & Assert
        // We're setting a tracking identifier because we don't want to receive some random operations. We want to
        // receive the event we sent.
        StepVerifier.create(consumer.receive(PARTITION_ID).filter(event -> isMatchingEvent(event, messageTrackingValue)).take(1).map(x -> x.getData()))
            .then(() -> producer.send(msgEvent, sendOptions).block(TIMEOUT))
            .assertNext(event -> {
                validateAmqpProperties(message, expectedAnnotations, applicationProperties, event);
                receivedEventData.set(event);
            })
            .verifyComplete();

        Assertions.assertNotNull(receivedEventData.get());

        StepVerifier.create(consumer.receive(PARTITION_ID).filter(event -> isMatchingEvent(event, messageTrackingValue)).take(1).map(x -> x.getData()))
            .then(() -> producer.send(receivedEventData.get(), sendOptions).block(TIMEOUT))
            .assertNext(event -> validateAmqpProperties(message, expectedAnnotations, applicationProperties, event))
            .verifyComplete();
    }

    private void validateAmqpProperties(Message message, Map<Symbol, Object> messageAnnotations,
                                        Map<String, Object> applicationProperties, EventData actual) {
        Assertions.assertTrue(actual.getSystemProperties().containsKey(MessageConstant.MESSAGE_ID.getValue()));
        Assertions.assertEquals(message.getMessageId(), actual.getSystemProperties().get(MessageConstant.MESSAGE_ID.getValue()));

        Assertions.assertTrue(actual.getSystemProperties().containsKey(MessageConstant.USER_ID.getValue()));
        Assertions.assertEquals(new String(message.getUserId()), new String((byte[]) actual.getSystemProperties().get(MessageConstant.USER_ID.getValue())));

        Assertions.assertTrue(actual.getSystemProperties().containsKey(MessageConstant.TO.getValue()));
        Assertions.assertEquals(message.getAddress(), actual.getSystemProperties().get(MessageConstant.TO.getValue()));

        Assertions.assertTrue(actual.getSystemProperties().containsKey(MessageConstant.CONTENT_TYPE.getValue()));
        Assertions.assertEquals(message.getContentType(), actual.getSystemProperties().get(MessageConstant.CONTENT_TYPE.getValue()));

        Assertions.assertTrue(actual.getSystemProperties().containsKey(MessageConstant.CONTENT_ENCODING.getValue()));
        Assertions.assertEquals(message.getContentEncoding(), actual.getSystemProperties().get(MessageConstant.CONTENT_ENCODING.getValue()));

        Assertions.assertTrue(actual.getSystemProperties().containsKey(MessageConstant.CORRELATION_ID.getValue()));
        Assertions.assertEquals(message.getCorrelationId(), actual.getSystemProperties().get(MessageConstant.CORRELATION_ID.getValue()));

        Assertions.assertTrue(actual.getSystemProperties().containsKey(MessageConstant.CREATION_TIME.getValue()));
        Assertions.assertEquals(message.getCreationTime(), actual.getSystemProperties().get(MessageConstant.CREATION_TIME.getValue()));

        Assertions.assertTrue(actual.getSystemProperties().containsKey(MessageConstant.SUBJECT.getValue()));
        Assertions.assertEquals(message.getSubject(), actual.getSystemProperties().get(MessageConstant.SUBJECT.getValue()));

        Assertions.assertTrue(actual.getSystemProperties().containsKey(MessageConstant.GROUP_ID.getValue()));
        Assertions.assertEquals(message.getGroupId(), actual.getSystemProperties().get(MessageConstant.GROUP_ID.getValue()));

        Assertions.assertTrue(actual.getSystemProperties().containsKey(MessageConstant.REPLY_TO_GROUP_ID.getValue()));
        Assertions.assertEquals(message.getReplyToGroupId(), actual.getSystemProperties().get(MessageConstant.REPLY_TO_GROUP_ID.getValue()));

        Assertions.assertTrue(actual.getSystemProperties().containsKey(MessageConstant.REPLY_TO.getValue()));
        Assertions.assertEquals(message.getReplyTo(), actual.getSystemProperties().get(MessageConstant.REPLY_TO.getValue()));

        Assertions.assertTrue(actual.getSystemProperties().containsKey(MessageConstant.ABSOLUTE_EXPIRY_TIME.getValue()));
        Assertions.assertEquals(message.getExpiryTime(), actual.getSystemProperties().get(MessageConstant.ABSOLUTE_EXPIRY_TIME.getValue()));

        Assertions.assertEquals(PAYLOAD, UTF_8.decode(actual.getBody()).toString());

        messageAnnotations.forEach((key, value) -> {
            Assertions.assertTrue(actual.getSystemProperties().containsKey(key.toString()));
            Assertions.assertEquals(value, actual.getSystemProperties().get(key.toString()));
        });

        Assertions.assertEquals(applicationProperties.size(), actual.getProperties().size());
        applicationProperties.forEach((key, value) -> {
            Assertions.assertTrue(actual.getProperties().containsKey(key));
            Assertions.assertEquals(value, actual.getProperties().get(key));
        });

    }
}
