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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import reactor.test.StepVerifier;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

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
@Execution(ExecutionMode.SAME_THREAD)
public class InteropAmqpPropertiesTest extends IntegrationTestBase {
    private static final ClientLogger LOGGER = new ClientLogger(InteropAmqpPropertiesTest.class);

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
    public void interoperableWithDirectProtonAmqpMessage() throws InterruptedException {
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
        producer.send(msgEvent, sendOptions).block();

        // Act & Assert
        // We're setting a tracking identifier because we don't want to receive some random operations. We want to
        // receive the event we sent.
        final List<EventData> partitionEventList = Collections.synchronizedList(new ArrayList<EventData>());
        Thread thread = new Thread(() -> {
            partitionEventList.addAll(consumer.receiveFromPartition(PARTITION_ID, enqueuedTime)
                .filter(event -> isMatchingEvent(event, messageTrackingValue)).take(1).map(PartitionEvent::getData)
                .collectList()
                .block());
        });

        thread.start();
        thread.join(TIMEOUT.toMillis());

        Assertions.assertEquals(1, partitionEventList.size());
        partitionEventList.stream().forEach(event -> {
            validateAmqpProperties(message, expectedAnnotations, applicationProperties, event);
            validateRawAmqpMessageProperties(message, expectedAnnotations, applicationProperties,
                event.getRawAmqpMessage());
        });
    }

    /**
     * Test all the supported types.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void interoperableAmqpTypes() {
        // Arrange
        final String body = "java " + Instant.now();
        final Map<Integer, String> mapOfStrings = new HashMap<>();
        mapOfStrings.put(10, "ten");
        mapOfStrings.put(2, "two");

        final List<String> listOfStrings = new ArrayList<>();
        listOfStrings.add("foo");
        listOfStrings.add("bar");

        final BigInteger bigInteger = new BigInteger("1000");

        final List<Pair> applicationPairTypes = new ArrayList<>();
        applicationPairTypes.add(new Pair("null", null));
        applicationPairTypes.add(new Pair("boolean", true));
        applicationPairTypes.add(new Pair("byte", (byte) 65));
        applicationPairTypes.add(new Pair("short", (short) 15));
        applicationPairTypes.add(new Pair("int", 10));
        applicationPairTypes.add(new Pair("float", 14.10f));
        applicationPairTypes.add(new Pair("long", bigInteger.longValue()));
        applicationPairTypes.add(new Pair("double", 15.6));
        applicationPairTypes.add(new Pair("string", "String value"));
        applicationPairTypes.add(new Pair("char", 'c'));
        applicationPairTypes.add(new Pair("timestamp", new Date(1670289289)));
        applicationPairTypes.add(new Pair("uuid", UUID.fromString("a0739e7a-9926-4dd0-b4db-ee3c6ae255b3")));

        applicationPairTypes.add(new Pair("byte[]", new Byte[]{10, 1},
            (expected, actual) -> assertEqualsArray((Byte[]) expected, actual, (a) -> {
                assertTrue(a instanceof byte[]);
                final byte[] bytes = (byte[]) a;
                final Byte[] returned = new Byte[bytes.length];

                for (int i = 0; i < bytes.length; i++) {
                    returned[i] = bytes[i];
                }
                return returned;
            })));

        applicationPairTypes.add(new Pair("map", mapOfStrings, (expected, actual) -> {
            assertTrue(expected instanceof Map<?, ?>);

            final Map<Integer, String> expectedContents = (Map<Integer, String>) expected;
            final Map<Integer, String> actualContents = (Map<Integer, String>) actual;

            assertEquals(expectedContents.size(), actualContents.size());

            expectedContents.forEach((key, value) -> {
                assertTrue(actualContents.containsKey(key));
                assertEquals(value, actualContents.get(key));
            });
        }));

        applicationPairTypes.add(new Pair("list", listOfStrings,
            (expected, actual) -> assertEqualsList((List<String>) expected, actual)));

        applicationPairTypes.add(new Pair("array", new Float[]{15.5f, 10.2f}, (expected, actual) -> {
            assertEqualsArray((Float[]) expected, actual, (a) -> {
                assertTrue(a instanceof float[]);
                final float[] primitive = (float[]) a;
                final Float[] returned = new Float[primitive.length];

                for (int i = 0; i < primitive.length; i++) {
                    returned[i] = primitive[i];
                }

                return returned;
            });
        }));

        final String partitionId = "1";
        final PartitionProperties partitionProperties = producer.getPartitionProperties(partitionId)
            .block();

        assertNotNull(partitionProperties);

        // Get the partition information, so we can start fetching after the last offset.
        final long lastOffset = Long.parseLong(partitionProperties.getLastEnqueuedOffset());
        final SendOptions options = new SendOptions().setPartitionId(partitionId);

        LOGGER.info("Last offset: {}", partitionProperties.getLastEnqueuedOffset());

        final EventData data = new EventData(body);
        applicationPairTypes.forEach(pair -> data.getProperties().put(pair.getKey(), pair.getValue()));

        // Act & Assert
        StepVerifier.create(producer.send(data, options))
            .expectComplete()
            .verify(TIMEOUT);

        StepVerifier.create(consumer.receiveFromPartition(partitionId, EventPosition.fromOffset(lastOffset)))
            .assertNext(partitionEvent -> {
                final EventData eventData = partitionEvent.getData();
                final Map<String, Object> properties = eventData.getProperties();

                final String bodyContents = partitionEvent.getData().getBodyAsString();
                assertEquals(body, bodyContents);

                assertEquals(applicationPairTypes.size(), properties.size());
                for (Pair expected : applicationPairTypes) {
                    LOGGER.info("\tComparing {}", expected.getKey());

                    final Object actual = properties.get(expected.getKey());

                    if (expected.assertConsumer == null) {
                        assertEquals(expected.getValue(), actual);
                    } else {
                        expected.assertEquals(actual);
                    }
                }
            })
            .thenCancel()
            .verify(TIMEOUT);
    }

    private void validateAmqpProperties(Message message, Map<Symbol, Object> messageAnnotations,
        Map<String, Object> applicationProperties, EventData actual) {
        assertTrue(actual.getSystemProperties().containsKey(AmqpMessageConstant.MESSAGE_ID.getValue()));
        Assertions.assertEquals(message.getMessageId(), actual.getSystemProperties().get(AmqpMessageConstant.MESSAGE_ID.getValue()));
        Assertions.assertEquals(message.getMessageId(), actual.getMessageId());

        assertTrue(actual.getSystemProperties().containsKey(AmqpMessageConstant.USER_ID.getValue()));
        Assertions.assertEquals(new String(message.getUserId()), new String((byte[]) actual.getSystemProperties().get(AmqpMessageConstant.USER_ID.getValue())));

        assertTrue(actual.getSystemProperties().containsKey(AmqpMessageConstant.TO.getValue()));
        Assertions.assertEquals(message.getAddress(), actual.getSystemProperties().get(AmqpMessageConstant.TO.getValue()));

        assertTrue(actual.getSystemProperties().containsKey(AmqpMessageConstant.CONTENT_TYPE.getValue()));
        Assertions.assertEquals(message.getContentType(), actual.getSystemProperties().get(AmqpMessageConstant.CONTENT_TYPE.getValue()));
        Assertions.assertEquals(message.getContentType(), actual.getContentType());

        assertTrue(actual.getSystemProperties().containsKey(AmqpMessageConstant.CONTENT_ENCODING.getValue()));
        Assertions.assertEquals(message.getContentEncoding(), actual.getSystemProperties().get(AmqpMessageConstant.CONTENT_ENCODING.getValue()));

        assertTrue(actual.getSystemProperties().containsKey(AmqpMessageConstant.CORRELATION_ID.getValue()));
        Assertions.assertEquals(message.getCorrelationId(), actual.getSystemProperties().get(AmqpMessageConstant.CORRELATION_ID.getValue()));
        Assertions.assertEquals(message.getCorrelationId(), actual.getCorrelationId());

        assertTrue(actual.getSystemProperties().containsKey(AmqpMessageConstant.CREATION_TIME.getValue()));
        Assertions.assertEquals(message.getProperties().getCreationTime().toInstant().atOffset(ZoneOffset.UTC),
            actual.getSystemProperties().get(AmqpMessageConstant.CREATION_TIME.getValue()));

        assertTrue(actual.getSystemProperties().containsKey(AmqpMessageConstant.SUBJECT.getValue()));
        Assertions.assertEquals(message.getSubject(), actual.getSystemProperties().get(AmqpMessageConstant.SUBJECT.getValue()));

        assertTrue(actual.getSystemProperties().containsKey(AmqpMessageConstant.GROUP_ID.getValue()));
        Assertions.assertEquals(message.getGroupId(), actual.getSystemProperties().get(AmqpMessageConstant.GROUP_ID.getValue()));

        assertTrue(actual.getSystemProperties().containsKey(AmqpMessageConstant.REPLY_TO_GROUP_ID.getValue()));
        Assertions.assertEquals(message.getReplyToGroupId(), actual.getSystemProperties().get(AmqpMessageConstant.REPLY_TO_GROUP_ID.getValue()));

        assertTrue(actual.getSystemProperties().containsKey(AmqpMessageConstant.REPLY_TO.getValue()));
        Assertions.assertEquals(message.getReplyTo(), actual.getSystemProperties().get(AmqpMessageConstant.REPLY_TO.getValue()));

        assertTrue(actual.getSystemProperties().containsKey(AmqpMessageConstant.ABSOLUTE_EXPIRY_TIME.getValue()));
        Assertions.assertEquals(message.getProperties().getAbsoluteExpiryTime().toInstant().atOffset(ZoneOffset.UTC),
            actual.getSystemProperties().get(AmqpMessageConstant.ABSOLUTE_EXPIRY_TIME.getValue()));

        Assertions.assertEquals(PAYLOAD, new String(actual.getBody(), UTF_8));

        messageAnnotations.forEach((key, value) -> {
            assertTrue(actual.getSystemProperties().containsKey(key.toString()));
            Assertions.assertEquals(value, actual.getSystemProperties().get(key.toString()));
        });

        Assertions.assertEquals(applicationProperties.size(), actual.getProperties().size());
        applicationProperties.forEach((key, value) -> {
            assertTrue(actual.getProperties().containsKey(key));
            Assertions.assertEquals(value, actual.getProperties().get(key));
        });
    }

    private void validateRawAmqpMessageProperties(Message message, Map<Symbol, Object> messageAnnotations,
        Map<String, Object> applicationProperties, AmqpAnnotatedMessage actual) {
        final AmqpMessageProperties actualProperties = actual.getProperties();

        assertNotNull(actualProperties.getMessageId());
        Assertions.assertEquals(message.getMessageId(), actualProperties.getMessageId().toString());

        final byte[] userId = actualProperties.getUserId();
        assertTrue(userId != null && userId.length > 0);
        assertArrayEquals(message.getUserId(), userId);

        final AmqpAddress to = actualProperties.getTo();
        assertNotNull(to);
        Assertions.assertEquals(message.getAddress(), to.toString());

        Assertions.assertEquals(message.getContentType(), actualProperties.getContentType());
        Assertions.assertEquals(message.getContentEncoding(), actualProperties.getContentEncoding());

        final AmqpMessageId correlationId = actualProperties.getCorrelationId();
        assertNotNull(correlationId);
        Assertions.assertEquals(message.getCorrelationId(), correlationId.toString());

        final OffsetDateTime creationTime = actualProperties.getCreationTime();
        assertNotNull(creationTime);

        final long creationTimeMs = creationTime.toInstant().toEpochMilli();
        Assertions.assertEquals(message.getCreationTime(), creationTimeMs);

        Assertions.assertEquals(message.getSubject(), actualProperties.getSubject());
        Assertions.assertEquals(message.getGroupId(), actualProperties.getGroupId());

        Assertions.assertEquals(message.getReplyToGroupId(), actualProperties.getReplyToGroupId());

        final AmqpAddress replyTo = actualProperties.getReplyTo();
        assertNotNull(replyTo);
        Assertions.assertEquals(message.getReplyTo(), replyTo.toString());


        final OffsetDateTime absoluteExpiryTime = actualProperties.getAbsoluteExpiryTime();
        assertNotNull(absoluteExpiryTime);

        final long absoluteEpochMs = absoluteExpiryTime.toInstant().toEpochMilli();
        Assertions.assertEquals(message.getExpiryTime(), absoluteEpochMs);

        final Instant absoluteExpiryInstant = message.getProperties().getAbsoluteExpiryTime().toInstant();
        Assertions.assertEquals(absoluteExpiryInstant, absoluteExpiryTime.toInstant());

        final AmqpMessageBody body = actual.getBody();
        Assertions.assertEquals(AmqpMessageBodyType.DATA, body.getBodyType());
        assertArrayEquals(PAYLOAD_BYTES, body.getFirstData());

        messageAnnotations.forEach((key, value) -> {
            assertTrue(actual.getMessageAnnotations().containsKey(key.toString()));
            Assertions.assertEquals(value, actual.getMessageAnnotations().get(key.toString()));
        });

        Assertions.assertEquals(applicationProperties.size(), actual.getApplicationProperties().size());
        applicationProperties.forEach((key, value) -> {
            assertTrue(actual.getApplicationProperties().containsKey(key));
            Assertions.assertEquals(value, actual.getApplicationProperties().get(key));
        });

    }

    @SuppressWarnings("unchecked")
    private static <T> void assertEqualsList(List<T> expected, Object actual) {
        assertTrue(actual instanceof List<?>);

        final List<T> actualContents = (List<T>) actual;

        assertEquals(expected.size(), actualContents.size());
        for (int i = 0; i < expected.size(); i++) {
            final T a = expected.get(i);
            final T b = actualContents.get(i);

            assertEquals(a, b, String.format("T[] at index %d did not match. Expected: %s. Actual: %s",
                i, expected, actual));
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> void assertEqualsArray(T[] expected, Object actual, Function<Object, T[]> conversion) {
        final Class<?> clazz = actual.getClass();

        assertTrue(clazz.isArray());

        final Class<?> componentType = clazz.getComponentType();

        final T[] actualContents;
        if (componentType.isPrimitive()) {
            actualContents = conversion.apply(actual);
        } else {
            actualContents = (T[]) actual;
        }

        assertEquals(expected.length, actualContents.length);

        for (int i = 0; i < expected.length; i++) {
            final T a = expected[i];
            final T b = expected[i];

            assertEquals(a, b, String.format("T[] at index %d did not match. Expected: %s. Actual: %s", i,
                expected, actual));
        }
    }

    private static final class Pair {
        private final String key;
        private final Object value;
        private BiConsumer<Object, Object> assertConsumer;

        private Pair(String key, Object value) {
            this(key, value, null);
        }

        private Pair(String key, Object value, BiConsumer<Object, Object> assertConsumer) {
            this.key = key;
            this.value = value;
            this.assertConsumer = assertConsumer;
        }

        String getKey() {
            return key;
        }

        Object getValue() {
            return value;
        }

        void assertEquals(Object actual) {
            assertConsumer.accept(value, actual);
        }
    }
}
