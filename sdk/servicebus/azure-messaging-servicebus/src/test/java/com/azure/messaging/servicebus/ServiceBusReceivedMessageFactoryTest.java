// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.models.AmqpMessageBodyType;
import com.azure.core.util.BinaryData;
import com.azure.messaging.servicebus.models.ServiceBusMessageState;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.AmqpSequence;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.DeliveryAnnotations;
import org.apache.qpid.proton.amqp.messaging.Footer;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.time.Instant;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;

class ServiceBusReceivedMessageFactoryTest {
    private static final String LOCK_TOKEN = "a0000000-b000-c000-d000-e00000000000";
    private static final byte[] BODY = { 0, (byte) 0xff, 42, (byte) 0x80 };

    @Test
    void decodesBodyAndMetadata() {
        Message message = Proton.message();
        message.setBody(new Data(new Binary(BODY)));
        message.setMessageId("message-id");
        message.setCorrelationId("correlation-id");
        message.setContentType("application/octet-stream");
        message.setSubject("subject");
        message.getProperties().setTo("destination");
        message.setReplyTo("reply");
        message.setGroupId("session");
        message.setReplyToGroupId("reply-session");
        message.setGroupSequence(4294967295L);
        message.setUserId(new byte[] { 1, 2 });
        message.setDeliveryCount(4);
        message.setDurable(true);
        message.setFirstAcquirer(true);
        message.setPriority((short) 7);
        message.setTtl(60000);

        Date timestamp = Date.from(Instant.parse("2026-10-21T12:00:00Z"));
        message.setCreationTime(timestamp.getTime());
        message.setExpiryTime(timestamp.getTime() + 60000);
        Map<Symbol, Object> annotations = new HashMap<>();
        annotations.put(Symbol.valueOf("x-opt-sequence-number"), Long.MAX_VALUE);
        annotations.put(Symbol.valueOf("x-opt-enqueue-sequence-number"), 9007199254740993L);
        annotations.put(Symbol.valueOf("x-opt-enqueued-time"), timestamp);
        annotations.put(Symbol.valueOf("x-opt-locked-until"), timestamp);
        annotations.put(Symbol.valueOf("x-opt-scheduled-enqueue-time"), timestamp);
        annotations.put(Symbol.valueOf("x-opt-partition-key"), "partition");
        annotations.put(Symbol.valueOf("x-opt-deadletter-source"), "deadletter");
        annotations.put(Symbol.valueOf("x-opt-message-state"), 1);
        message.setMessageAnnotations(new MessageAnnotations(annotations));
        message.setDeliveryAnnotations(
            new DeliveryAnnotations(Collections.singletonMap(Symbol.valueOf("delivery"), "value")));
        message.setFooter(new Footer(Collections.singletonMap(Symbol.valueOf("checksum"), 123L)));
        Map<String, Object> applicationProperties = new HashMap<>();
        applicationProperties.put("long", 9007199254740993L);
        applicationProperties.put("binary", new Binary(BODY));
        applicationProperties.put("DeadLetterReason", "reason");
        applicationProperties.put("DeadLetterErrorDescription", "description");
        message.setApplicationProperties(new ApplicationProperties(applicationProperties));

        ServiceBusReceivedMessage received
            = ServiceBusReceivedMessage.fromAmqpMessage(encode(message), LOCK_TOKEN.toUpperCase(java.util.Locale.ROOT));

        assertArrayEquals(BODY, received.getBody().toBytes());
        assertEquals(LOCK_TOKEN, received.getLockToken());
        assertEquals("message-id", received.getMessageId());
        assertEquals("correlation-id", received.getCorrelationId());
        assertEquals("application/octet-stream", received.getContentType());
        assertEquals("subject", received.getSubject());
        assertEquals("destination", received.getTo());
        assertEquals("reply", received.getReplyTo());
        assertEquals("session", received.getSessionId());
        assertEquals("reply-session", received.getReplyToSessionId());
        assertEquals(4, received.getDeliveryCount());
        assertEquals(Duration.ofMinutes(1), received.getTimeToLive());
        assertEquals(Long.MAX_VALUE, received.getSequenceNumber());
        assertEquals(9007199254740993L, received.getEnqueuedSequenceNumber());
        assertEquals(timestamp.toInstant(), received.getEnqueuedTime().toInstant());
        assertEquals(timestamp.toInstant(), received.getLockedUntil().toInstant());
        assertEquals(timestamp.toInstant(), received.getScheduledEnqueueTime().toInstant());
        assertEquals("partition", received.getPartitionKey());
        assertEquals("deadletter", received.getDeadLetterSource());
        assertEquals(ServiceBusMessageState.DEFERRED, received.getState());
        assertEquals("reason", received.getDeadLetterReason());
        assertEquals("description", received.getDeadLetterErrorDescription());
        assertEquals(9007199254740993L, received.getApplicationProperties().get("long"));
        assertEquals(new Binary(BODY), received.getApplicationProperties().get("binary"));
        assertEquals("value", received.getRawAmqpMessage().getDeliveryAnnotations().get("delivery"));
        assertEquals(123L, received.getRawAmqpMessage().getFooter().get("checksum"));
        assertEquals(true, received.getRawAmqpMessage().getHeader().isDurable());
        assertEquals(true, received.getRawAmqpMessage().getHeader().isFirstAcquirer());
        assertEquals((short) 7, received.getRawAmqpMessage().getHeader().getPriority());
        assertEquals(4294967295L, received.getRawAmqpMessage().getProperties().getGroupSequence());
        assertArrayEquals(new byte[] { 1, 2 }, received.getRawAmqpMessage().getProperties().getUserId());
        assertEquals(timestamp.toInstant(), received.getRawAmqpMessage().getProperties().getCreationTime().toInstant());
        assertEquals(timestamp.getTime() + 60000,
            received.getRawAmqpMessage().getProperties().getAbsoluteExpiryTime().toInstant().toEpochMilli());
    }

    @Test
    void supportsHeaderlessAndEmptyDataBodies() {
        for (byte[] body : Arrays.asList(BODY, new byte[0])) {
            ServiceBusReceivedMessage received = decodeBody(new Data(new Binary(body)));
            assertArrayEquals(body, received.getBody().toBytes());
            assertNull(received.getLockToken());
        }
    }

    @Test
    void supportsBodylessMessage() {
        Message message = Proton.message();
        message.setMessageId("bodyless");
        ServiceBusReceivedMessage received = ServiceBusReceivedMessage.fromAmqpMessage(encode(message), null);
        assertEquals("bodyless", received.getMessageId());
        assertArrayEquals(new byte[0], received.getBody().toBytes());
    }

    @Test
    void preservesBinarySlice() {
        Message message = Proton.message();
        message.setBody(new Data(new Binary(new byte[] { 99, 0, (byte) 0xff, 42, (byte) 0x80, 99 }, 1, 4)));
        ServiceBusReceivedMessage received = ServiceBusReceivedMessage.fromAmqpMessage(encode(message), null);
        assertArrayEquals(BODY, received.getBody().toBytes());
        ServiceBusReceivedMessage directlyMapped
            = new ServiceBusMessageSerializer().deserialize(message, ServiceBusReceivedMessage.class);
        assertArrayEquals(BODY, directlyMapped.getBody().toBytes());
    }

    @ParameterizedTest
    @ValueSource(longs = { 0, 1, 2147483647L, 2147483648L, 4294967295L })
    void preservesUnsignedGroupSequence(long groupSequence) {
        Message message = Proton.message();
        message.setBody(new Data(new Binary(BODY)));
        message.setGroupSequence(groupSequence);
        ServiceBusReceivedMessage received = ServiceBusReceivedMessage.fromAmqpMessage(encode(message), null);
        assertEquals(groupSequence, received.getRawAmqpMessage().getProperties().getGroupSequence());
    }

    @Test
    void supportsSequenceAndValueBodies() {
        ServiceBusReceivedMessage sequence = decodeBody(new AmqpSequence(Arrays.asList("text", Long.MAX_VALUE)));
        assertEquals(AmqpMessageBodyType.SEQUENCE, sequence.getRawAmqpMessage().getBody().getBodyType());
        assertEquals(Arrays.asList("text", Long.MAX_VALUE), sequence.getRawAmqpMessage().getBody().getSequence());
        assertThrows(UnsupportedOperationException.class, sequence::getBody);

        for (Object value : Arrays.asList("text", Long.MAX_VALUE, new Binary(BODY),
            Collections.singletonMap("key", "value"), Arrays.asList("item", 1))) {
            ServiceBusReceivedMessage received = decodeBody(new AmqpValue(value));
            assertEquals(AmqpMessageBodyType.VALUE, received.getRawAmqpMessage().getBody().getBodyType());
            assertEquals(value, received.getRawAmqpMessage().getBody().getValue());
            assertThrows(UnsupportedOperationException.class, received::getBody);
        }
    }

    @Test
    void supportsNestedContainersAndArrays() {
        String longString = new String(new char[300]).replace('\0', 'a');
        Object nested = Collections.singletonMap("nested", Arrays.asList(longString, Collections.emptyList()));
        ServiceBusReceivedMessage message = decodeBody(new AmqpValue(nested));
        assertEquals(nested, message.getRawAmqpMessage().getBody().getValue());
        for (String[] array : Arrays.asList(new String[] { "one", "two" }, new String[] { longString, "two" },
            new String[0])) {
            ServiceBusReceivedMessage received = decodeBody(new AmqpValue(array));
            assertArrayEquals(array, (String[]) received.getRawAmqpMessage().getBody().getValue());
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "005376c0020141",
            "005376d0000000050000000141",
            "005377c103024141",
            "005377d100000006000000024141",
            "005377e00502a1016100",
            "005377f00000000800000002a1016100" })
    void acceptsValidContainerBoundaries(String hex) {
        ServiceBusReceivedMessage received
            = ServiceBusReceivedMessage.fromAmqpMessage(BinaryData.fromBytes(fromHex(hex)), null);
        assertEquals(hex.startsWith("005376") ? AmqpMessageBodyType.SEQUENCE : AmqpMessageBodyType.VALUE,
            received.getRawAmqpMessage().getBody().getBodyType());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "005376c0ff0141",
            "005376c0000141",
            "005376c0030141",
            "005376c0020241",
            "005376d0000000ff0000000141",
            "005376d0000000000000000141",
            "005370c0ff0141",
            "005373c0ff0141",
            "005377c00501c0ff0141",
            "005377c1ff024141",
            "005377c103014141",
            "005377c103034141",
            "005377d1000000ff000000024141",
            "005377d100000006000000014141",
            "005377e0ff02a1016100",
            "005377e00503a1016100",
            "005377f0000000ff00000002a1016100",
            "005377c00601e00301a1ff" })
    void rejectsInvalidContainerBoundaries(String hex) {
        assertThrows(IllegalArgumentException.class,
            () -> ServiceBusReceivedMessage.fromAmqpMessage(BinaryData.fromBytes(fromHex(hex)), null));
    }

    @Test
    void rejectsExcessiveNesting() {
        Object nested = "value";
        for (int i = 0; i < 127; i++) {
            nested = Collections.singletonList(nested);
        }
        assertEquals(AmqpMessageBodyType.VALUE,
            decodeBody(new AmqpValue(nested)).getRawAmqpMessage().getBody().getBodyType());
        Message message = Proton.message();
        message.setBody(new AmqpValue(Collections.singletonList(nested)));
        assertThrows(IllegalArgumentException.class,
            () -> ServiceBusReceivedMessage.fromAmqpMessage(encode(message), null));
    }

    @Test
    void rejectsExcessiveContainerEntries() {
        for (String hex : Arrays.asList("005377f0000000057fffffff43", "005377f000000005000f424143",
            "005377c01f03f0000000050007a12043f0000000050007a12043f0000000050000000143")) {
            assertTimeout(Duration.ofSeconds(1), () -> {
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> ServiceBusReceivedMessage.fromAmqpMessage(BinaryData.fromBytes(fromHex(hex)), null));
                assertEquals("AMQP messages cannot contain more than 1,000,000 container entries.",
                    exception.getCause().getMessage());
            });
        }
        ByteBuffer maximumEntries = ByteBuffer.allocate(1_000_012);
        maximumEntries.put(fromHex("005377d0")).putInt(1_000_004).putInt(1_000_000);
        Arrays.fill(maximumEntries.array(), maximumEntries.position(), maximumEntries.capacity(), (byte) 0x41);
        ServiceBusReceivedMessage received
            = ServiceBusReceivedMessage.fromAmqpMessage(BinaryData.fromBytes(maximumEntries.array()), null);
        assertEquals(1_000_000, ((List<?>) received.getRawAmqpMessage().getBody().getValue()).size());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "",
            "invalid",
            "1-1-1-1-1",
            "{a0000000-b000-c000-d000-e00000000000}",
            " a0000000-b000-c000-d000-e00000000000" })
    void rejectsInvalidLockToken(String lockToken) {
        Message message = Proton.message();
        message.setBody(new Data(new Binary(BODY)));
        assertThrows(IllegalArgumentException.class,
            () -> ServiceBusReceivedMessage.fromAmqpMessage(encode(message), lockToken));
    }

    @Test
    void rejectsMalformedAndTruncatedMessages() {
        assertThrows(NullPointerException.class, () -> ServiceBusReceivedMessage.fromAmqpMessage(null, null));
        for (byte[] invalid : Arrays.asList(new byte[0], new byte[] { 0 }, new byte[] { 0x40 }, new byte[] { 0x41 },
            new byte[] { (byte) 0xff })) {
            assertThrows(IllegalArgumentException.class,
                () -> ServiceBusReceivedMessage.fromAmqpMessage(BinaryData.fromBytes(invalid), null));
        }
        Message message = Proton.message();
        message.setBody(new Data(new Binary(BODY)));
        byte[] encoded = encode(message).toBytes();
        for (int length = 1; length < encoded.length; length++) {
            byte[] truncated = Arrays.copyOf(encoded, length);
            assertThrows(IllegalArgumentException.class,
                () -> ServiceBusReceivedMessage.fromAmqpMessage(BinaryData.fromBytes(truncated), null));
        }
        byte[] trailing = Arrays.copyOf(encoded, encoded.length + 1);
        trailing[encoded.length] = 0x40;
        assertThrows(IllegalArgumentException.class,
            () -> ServiceBusReceivedMessage.fromAmqpMessage(BinaryData.fromBytes(trailing), null));
    }

    @Test
    void rejectsUnsupportedOrRepeatedSections() {
        Message message = Proton.message();
        message.setBody(new AmqpValue(null));
        assertThrows(IllegalArgumentException.class,
            () -> ServiceBusReceivedMessage.fromAmqpMessage(encode(message), null));
        for (Section body : Arrays.asList(new Data(new Binary(BODY)), new AmqpSequence(Arrays.asList(1, 2)))) {
            message.setBody(body);
            byte[] encoded = encode(message).toBytes();
            byte[] repeated = Arrays.copyOf(encoded, encoded.length * 2);
            System.arraycopy(encoded, 0, repeated, encoded.length, encoded.length);
            assertThrows(IllegalArgumentException.class,
                () -> ServiceBusReceivedMessage.fromAmqpMessage(BinaryData.fromBytes(repeated), null));
        }
        Message header = Proton.message();
        header.setDurable(true);
        Message body = Proton.message();
        body.setBody(new Data(new Binary(BODY)));
        byte[] bodyBytes = encode(body).toBytes();
        byte[] headerBytes = encode(header).toBytes();
        byte[] outOfOrder = Arrays.copyOf(bodyBytes, bodyBytes.length + headerBytes.length);
        System.arraycopy(headerBytes, 0, outOfOrder, bodyBytes.length, headerBytes.length);
        assertThrows(IllegalArgumentException.class,
            () -> ServiceBusReceivedMessage.fromAmqpMessage(BinaryData.fromBytes(outOfOrder), null));
    }

    private static ServiceBusReceivedMessage decodeBody(Section body) {
        Message message = Proton.message();
        message.setBody(body);
        return ServiceBusReceivedMessage.fromAmqpMessage(encode(message), null);
    }

    private static BinaryData encode(Message message) {
        byte[] buffer = new byte[4096];
        int length = message.encode(buffer, 0, buffer.length);
        return BinaryData.fromBytes(Arrays.copyOf(buffer, length));
    }

    private static byte[] fromHex(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return bytes;
    }
}
