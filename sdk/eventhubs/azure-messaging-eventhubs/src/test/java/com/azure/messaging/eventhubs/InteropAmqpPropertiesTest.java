// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpMessageConstant;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.models.AmqpAddress;
import com.azure.core.amqp.models.AmqpAnnotatedMessage;
import com.azure.core.amqp.models.AmqpMessageBody;
import com.azure.core.amqp.models.AmqpMessageBodyType;
import com.azure.core.amqp.models.AmqpMessageId;
import com.azure.core.amqp.models.AmqpMessageProperties;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import com.azure.messaging.eventhubs.models.SendOptions;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.azure.core.amqp.AmqpMessageConstant.ENQUEUED_TIME_UTC_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.OFFSET_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.SEQUENCE_NUMBER_ANNOTATION_NAME;
import static com.azure.messaging.eventhubs.EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME;
import static com.azure.messaging.eventhubs.EventHubClientBuilder.DEFAULT_PREFETCH_COUNT;
import static com.azure.messaging.eventhubs.TestUtils.MESSAGE_ID;
import static com.azure.messaging.eventhubs.TestUtils.getSymbol;
import static com.azure.messaging.eventhubs.TestUtils.isMatchingEvent;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag(TestUtils.INTEGRATION)
public class InteropAmqpPropertiesTest extends IntegrationTestBase {
    private static final String PARTITION_ID = "4";
    private static final String PAYLOAD = "test-message";
    private static final byte[] PAYLOAD_BYTES = PAYLOAD.getBytes(UTF_8);

    private final MessageSerializer serializer = new EventHubMessageSerializer();
    private EventHubProducerAsyncClient producer;
    private EventHubConsumerAsyncClient consumer;
    private SendOptions sendOptions;

    public InteropAmqpPropertiesTest() {
        super(new ClientLogger(InteropAmqpPropertiesTest.class));
    }

    @Override
    protected void beforeTest() {
        sendOptions = new SendOptions().setPartitionId(PARTITION_ID);

        final EventHubClientBuilder builder = createBuilder().shareConnection()
            .consumerGroup(DEFAULT_CONSUMER_GROUP_NAME)
            .prefetchCount(DEFAULT_PREFETCH_COUNT);
        producer = builder.buildAsyncProducerClient();
        consumer = builder.buildAsyncConsumerClient();
    }

    @Override
    protected void afterTest() {
        dispose(producer, consumer);
    }

    /**
     * Test for interoperable with Direct Proton AMQP messaging
     */
    @Test
    public void interoperableWithDirectProtonAmqpMessage() {
        // Arrange
        final String messageTrackingValue = UUID.randomUUID().toString();

        final HashMap<String, Object> applicationProperties = new HashMap<>();
        applicationProperties.put(MESSAGE_ID, messageTrackingValue);
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

        message.setBody(new Data(Binary.create(ByteBuffer.wrap(PAYLOAD_BYTES))));
        final EventData msgEvent = serializer.deserialize(message, EventData.class);

        final EventPosition enqueuedTime = EventPosition.fromEnqueuedTime(Instant.now());
        producer.send(msgEvent, sendOptions).block(TIMEOUT);

        // Act & Assert
        // We're setting a tracking identifier because we don't want to receive some random operations. We want to
        // receive the event we sent.
        StepVerifier.create(consumer.receiveFromPartition(PARTITION_ID, enqueuedTime)
            .filter(event -> isMatchingEvent(event, messageTrackingValue)).take(1).map(PartitionEvent::getData))
            .assertNext(event -> {
                validateAmqpProperties(message, expectedAnnotations, applicationProperties, event);
                validateRawAmqpMessageProperties(message, expectedAnnotations, applicationProperties,
                    event.getRawAmqpMessage());

            })
            .expectComplete()
            .verify(TIMEOUT);
    }

    private void validateAmqpProperties(Message message, Map<Symbol, Object> messageAnnotations,
        Map<String, Object> applicationProperties, EventData actual) {
        assertTrue(actual.getSystemProperties().containsKey(AmqpMessageConstant.MESSAGE_ID.getValue()));
        assertEquals(message.getMessageId(), actual.getSystemProperties().get(AmqpMessageConstant.MESSAGE_ID.getValue()));
        assertEquals(message.getMessageId(), actual.getMessageId());

        assertTrue(actual.getSystemProperties().containsKey(AmqpMessageConstant.USER_ID.getValue()));
        assertEquals(new String(message.getUserId()), new String((byte[]) actual.getSystemProperties().get(AmqpMessageConstant.USER_ID.getValue())));

        assertTrue(actual.getSystemProperties().containsKey(AmqpMessageConstant.TO.getValue()));
        assertEquals(message.getAddress(), actual.getSystemProperties().get(AmqpMessageConstant.TO.getValue()));

        assertTrue(actual.getSystemProperties().containsKey(AmqpMessageConstant.CONTENT_TYPE.getValue()));
        assertEquals(message.getContentType(), actual.getSystemProperties().get(AmqpMessageConstant.CONTENT_TYPE.getValue()));
        assertEquals(message.getContentType(), actual.getContentType());

        assertTrue(actual.getSystemProperties().containsKey(AmqpMessageConstant.CONTENT_ENCODING.getValue()));
        assertEquals(message.getContentEncoding(), actual.getSystemProperties().get(AmqpMessageConstant.CONTENT_ENCODING.getValue()));

        assertTrue(actual.getSystemProperties().containsKey(AmqpMessageConstant.CORRELATION_ID.getValue()));
        assertEquals(message.getCorrelationId(), actual.getSystemProperties().get(AmqpMessageConstant.CORRELATION_ID.getValue()));
        assertEquals(message.getCorrelationId(), actual.getCorrelationId());

        assertTrue(actual.getSystemProperties().containsKey(AmqpMessageConstant.CREATION_TIME.getValue()));
        assertEquals(message.getProperties().getCreationTime().toInstant().atOffset(ZoneOffset.UTC),
            actual.getSystemProperties().get(AmqpMessageConstant.CREATION_TIME.getValue()));

        assertTrue(actual.getSystemProperties().containsKey(AmqpMessageConstant.SUBJECT.getValue()));
        assertEquals(message.getSubject(), actual.getSystemProperties().get(AmqpMessageConstant.SUBJECT.getValue()));

        assertTrue(actual.getSystemProperties().containsKey(AmqpMessageConstant.GROUP_ID.getValue()));
        assertEquals(message.getGroupId(), actual.getSystemProperties().get(AmqpMessageConstant.GROUP_ID.getValue()));

        assertTrue(actual.getSystemProperties().containsKey(AmqpMessageConstant.REPLY_TO_GROUP_ID.getValue()));
        assertEquals(message.getReplyToGroupId(), actual.getSystemProperties().get(AmqpMessageConstant.REPLY_TO_GROUP_ID.getValue()));

        assertTrue(actual.getSystemProperties().containsKey(AmqpMessageConstant.REPLY_TO.getValue()));
        assertEquals(message.getReplyTo(), actual.getSystemProperties().get(AmqpMessageConstant.REPLY_TO.getValue()));

        assertTrue(actual.getSystemProperties().containsKey(AmqpMessageConstant.ABSOLUTE_EXPIRY_TIME.getValue()));
        assertEquals(message.getProperties().getAbsoluteExpiryTime().toInstant().atOffset(ZoneOffset.UTC),
            actual.getSystemProperties().get(AmqpMessageConstant.ABSOLUTE_EXPIRY_TIME.getValue()));

        assertEquals(PAYLOAD, new String(actual.getBody(), UTF_8));

        messageAnnotations.forEach((key, value) -> {
            assertTrue(actual.getSystemProperties().containsKey(key.toString()));
            assertEquals(value, actual.getSystemProperties().get(key.toString()));
        });

        assertEquals(applicationProperties.size(), actual.getProperties().size());
        applicationProperties.forEach((key, value) -> {
            assertTrue(actual.getProperties().containsKey(key));
            assertEquals(value, actual.getProperties().get(key));
        });
    }

    private void validateRawAmqpMessageProperties(Message message, Map<Symbol, Object> messageAnnotations,
        Map<String, Object> applicationProperties, AmqpAnnotatedMessage actual) {
        final AmqpMessageProperties actualProperties = actual.getProperties();

        assertNotNull(actualProperties.getMessageId());
        assertEquals(message.getMessageId(), actualProperties.getMessageId().toString());

        final byte[] userId = actualProperties.getUserId();
        assertTrue(userId != null && userId.length > 0);
        assertArrayEquals(message.getUserId(), userId);

        final AmqpAddress to = actualProperties.getTo();
        assertNotNull(to);
        assertEquals(message.getAddress(), to.toString());

        assertEquals(message.getContentType(), actualProperties.getContentType());
        assertEquals(message.getContentEncoding(), actualProperties.getContentEncoding());

        final AmqpMessageId correlationId = actualProperties.getCorrelationId();
        assertNotNull(correlationId);
        assertEquals(message.getCorrelationId(), correlationId.toString());

        final OffsetDateTime creationTime = actualProperties.getCreationTime();
        assertNotNull(creationTime);

        final long creationTimeMs = creationTime.toInstant().toEpochMilli();
        assertEquals(message.getCreationTime(), creationTimeMs);

        assertEquals(message.getSubject(), actualProperties.getSubject());
        assertEquals(message.getGroupId(), actualProperties.getGroupId());

        assertEquals(message.getReplyToGroupId(), actualProperties.getReplyToGroupId());

        final AmqpAddress replyTo = actualProperties.getReplyTo();
        assertNotNull(replyTo);
        assertEquals(message.getReplyTo(), replyTo.toString());


        final OffsetDateTime absoluteExpiryTime = actualProperties.getAbsoluteExpiryTime();
        assertNotNull(absoluteExpiryTime);

        final long absoluteEpochMs = absoluteExpiryTime.toInstant().toEpochMilli();
        assertEquals(message.getExpiryTime(), absoluteEpochMs);

        final Instant absoluteExpiryInstant = message.getProperties().getAbsoluteExpiryTime().toInstant();
        assertEquals(absoluteExpiryInstant, absoluteExpiryTime.toInstant());

        final AmqpMessageBody body = actual.getBody();
        assertEquals(AmqpMessageBodyType.DATA, body.getBodyType());
        assertArrayEquals(PAYLOAD_BYTES,  body.getFirstData());

        messageAnnotations.forEach((key, value) -> {
            assertTrue(actual.getMessageAnnotations().containsKey(key.toString()));
            assertEquals(value, actual.getMessageAnnotations().get(key.toString()));
        });

        assertEquals(applicationProperties.size(), actual.getApplicationProperties().size());
        applicationProperties.forEach((key, value) -> {
            assertTrue(actual.getApplicationProperties().containsKey(key));
            assertEquals(value, actual.getApplicationProperties().get(key));
        });

    }
}
