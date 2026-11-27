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
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServiceBusReceivedMessageFactoryTest {
    private static final String LOCK_TOKEN = "11223344-5566-7788-99aa-bbccddeeff00";
    private static final byte[] BODY = { 0, 1, 0x7f, (byte) 0x80, (byte) 0xff };

    // The fixtures below were produced by Azure.Messaging.ServiceBus 7.21.0 for .NET using
    // AmqpAnnotatedMessage.ToBytes(), the call the Functions host uses for its binding data. The Functions host
    // prepends a 16-byte lock token, which callers of fromAmqpMessage strip and pass separately.
    private static final String DOTNET_DATA_MESSAGE
        = "005370c00b0540407000015f90405203005372c1f510a31c782d6f70742d7363686564756c65642d656e71756575652d"
            + "74696d65830000018bcfe56800a313782d6f70742d706172746974696f6e2d6b6579a102706ba312782d6f70742d6c6f"
            + "636b65642d756e74696c830000018bcfe65260a315782d6f70742d73657175656e63652d6e756d62657281000000012a"
            + "05f201a317782d6f70742d646561646c65747465722d736f7572636540a31d782d6f70742d656e71756575652d736571"
            + "75656e63652d6e756d626572810000000100000004a313782d6f70742d656e7175657565642d74696d65830000018bcf"
            + "e56418a313782d6f70742d6d6573736167652d73746174655400005373c06c0da1056d73672d3140a104746f2d31a109"
            + "7375626a6563742d31a1077265706c792d31a106636f72722d31a3186170706c69636174696f6e2f6f637465742d7374"
            + "7265616d4083000001a19dcf4bb283000001a19dcdec22a102706b40a10d7265706c792d73657373696f6e005374c125"
            + "06a1056b2d737472a10176a1056b2d696e74542aa1066b2d6c6f6e6781000000012a05f200005375a00500017f80ff";
    private static final String DOTNET_VALUE_MESSAGE
        = "005373c0160da10776616c75652d31404040404040404040404040005377a10a76616c75652d626f6479";
    private static final String DOTNET_SEQUENCE_MESSAGE
        = "005373c0140da1057365712d31404040404040404040404040005376c00f03a1016154078100000001dcd65000";

    @Test
    void decodesDotNetDataMessage() {
        ServiceBusReceivedMessage received
            = ServiceBusReceivedMessage.fromAmqpMessage(BinaryData.fromBytes(fromHex(DOTNET_DATA_MESSAGE)), LOCK_TOKEN);

        assertArrayEquals(BODY, received.getBody().toBytes());
        assertEquals(LOCK_TOKEN, received.getLockToken());
        assertEquals("msg-1", received.getMessageId());
        assertEquals("corr-1", received.getCorrelationId());
        assertEquals("subject-1", received.getSubject());
        assertEquals("to-1", received.getTo());
        assertEquals("reply-1", received.getReplyTo());
        assertEquals("application/octet-stream", received.getContentType());
        assertEquals("pk", received.getSessionId());
        assertEquals("pk", received.getPartitionKey());
        assertEquals("reply-session", received.getReplyToSessionId());
        assertEquals(Duration.ofSeconds(90), received.getTimeToLive());
        assertEquals(3, received.getDeliveryCount());
        assertEquals(5_000_000_001L, received.getSequenceNumber());
        assertEquals(4_294_967_300L, received.getEnqueuedSequenceNumber());
        assertEquals(Instant.ofEpochMilli(1_699_999_999_000L), received.getEnqueuedTime().toInstant());
        assertEquals(Instant.ofEpochMilli(1_700_000_000_000L), received.getScheduledEnqueueTime().toInstant());
        assertEquals(Instant.ofEpochMilli(1_700_000_060_000L), received.getLockedUntil().toInstant());
        assertEquals("v", received.getApplicationProperties().get("k-str"));
        assertEquals(42, received.getApplicationProperties().get("k-int"));
        assertEquals(5_000_000_000L, received.getApplicationProperties().get("k-long"));
    }

    @Test
    void decodesDotNetValueMessageWithoutHeader() {
        ServiceBusReceivedMessage received = ServiceBusReceivedMessage
            .fromAmqpMessage(BinaryData.fromBytes(fromHex(DOTNET_VALUE_MESSAGE)), LOCK_TOKEN);

        assertEquals("value-1", received.getMessageId());
        assertEquals(AmqpMessageBodyType.VALUE, received.getRawAmqpMessage().getBody().getBodyType());
        assertEquals("value-body", received.getRawAmqpMessage().getBody().getValue());
    }

    @Test
    void decodesDotNetSequenceMessageWithoutHeader() {
        ServiceBusReceivedMessage received = ServiceBusReceivedMessage
            .fromAmqpMessage(BinaryData.fromBytes(fromHex(DOTNET_SEQUENCE_MESSAGE)), LOCK_TOKEN);

        assertEquals("seq-1", received.getMessageId());
        assertEquals(AmqpMessageBodyType.SEQUENCE, received.getRawAmqpMessage().getBody().getBodyType());
        assertEquals(Arrays.<Object>asList("a", 7, 8_000_000_000L),
            received.getRawAmqpMessage().getBody().getSequence());
    }

    @Test
    void decodesBodyAndMetadata() {
        Date timestamp = Date.from(Instant.parse("2026-10-21T12:00:00Z"));
        Message message = Proton.message();
        message.setBody(new Data(new Binary(BODY)));
        message.setMessageId("message-id");
        message.setSubject("subject");
        message.setGroupId("session");
        message.setDeliveryCount(4);
        message.setTtl(60000);
        message.setCreationTime(timestamp.getTime());
        message.setExpiryTime(timestamp.getTime() + 60000);

        Map<Symbol, Object> annotations = new HashMap<>();
        annotations.put(Symbol.valueOf("x-opt-sequence-number"), Long.MAX_VALUE);
        annotations.put(Symbol.valueOf("x-opt-enqueue-sequence-number"), 9007199254740993L);
        annotations.put(Symbol.valueOf("x-opt-enqueued-time"), timestamp);
        annotations.put(Symbol.valueOf("x-opt-message-state"), 1);
        message.setMessageAnnotations(new MessageAnnotations(annotations));
        message.setDeliveryAnnotations(
            new DeliveryAnnotations(Collections.singletonMap(Symbol.valueOf("delivery"), "value")));
        message.setFooter(new Footer(Collections.singletonMap(Symbol.valueOf("checksum"), 123L)));

        Map<String, Object> applicationProperties = new HashMap<>();
        applicationProperties.put("long", 9007199254740993L);
        applicationProperties.put("DeadLetterReason", "reason");
        message.setApplicationProperties(new ApplicationProperties(applicationProperties));

        ServiceBusReceivedMessage received = ServiceBusReceivedMessage.fromAmqpMessage(encode(message), LOCK_TOKEN);

        assertArrayEquals(BODY, received.getBody().toBytes());
        assertEquals("message-id", received.getMessageId());
        assertEquals("subject", received.getSubject());
        assertEquals("session", received.getSessionId());
        assertEquals(4, received.getDeliveryCount());
        assertEquals(Duration.ofMinutes(1), received.getTimeToLive());
        assertEquals(Long.MAX_VALUE, received.getSequenceNumber());
        assertEquals(9007199254740993L, received.getEnqueuedSequenceNumber());
        assertEquals(timestamp.toInstant(), received.getEnqueuedTime().toInstant());
        assertEquals(ServiceBusMessageState.DEFERRED, received.getState());
        assertEquals("reason", received.getDeadLetterReason());
        assertEquals(9007199254740993L, received.getApplicationProperties().get("long"));
        assertEquals("value", received.getRawAmqpMessage().getDeliveryAnnotations().get("delivery"));
        assertEquals(123L, received.getRawAmqpMessage().getFooter().get("checksum"));
    }

    @Test
    void decodesSequenceAndValueBodies() {
        List<Object> sequence = Arrays.<Object>asList("a", 1L, new Binary(BODY));
        ServiceBusReceivedMessage sequenceMessage = decodeBody(new AmqpSequence(sequence));
        assertEquals(AmqpMessageBodyType.SEQUENCE, sequenceMessage.getRawAmqpMessage().getBody().getBodyType());
        assertEquals(sequence, sequenceMessage.getRawAmqpMessage().getBody().getSequence());

        Map<String, Object> value = Collections.singletonMap("key", Arrays.asList(1L, "two"));
        ServiceBusReceivedMessage valueMessage = decodeBody(new AmqpValue(value));
        assertEquals(AmqpMessageBodyType.VALUE, valueMessage.getRawAmqpMessage().getBody().getBodyType());
        assertEquals(value, valueMessage.getRawAmqpMessage().getBody().getValue());
    }

    @Test
    void messageWithoutBodyHasEmptyDataBody() {
        ServiceBusReceivedMessage received = ServiceBusReceivedMessage.fromAmqpMessage(minimalMessage(), null);

        assertEquals(AmqpMessageBodyType.DATA, received.getRawAmqpMessage().getBody().getBodyType());
        assertEquals(0, received.getBody().toBytes().length);
    }

    @Test
    void setsLockTokenWhenProvided() {
        ServiceBusReceivedMessage received
            = ServiceBusReceivedMessage.fromAmqpMessage(minimalMessage(), LOCK_TOKEN.toUpperCase(Locale.ROOT));

        assertEquals(LOCK_TOKEN, received.getLockToken());
    }

    @Test
    void allowsNullLockToken() {
        assertNull(ServiceBusReceivedMessage.fromAmqpMessage(minimalMessage(), null).getLockToken());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "",
            "not-a-uuid",
            "1-2-3-4-5",
            " 11223344-5566-7788-99aa-bbccddeeff00",
            "11223344556677889aabbccddeeff00",
            "11223344-5566-7788-99aa-bbccddeeff0" })
    void rejectsInvalidLockToken(String lockToken) {
        assertThrows(IllegalArgumentException.class,
            () -> ServiceBusReceivedMessage.fromAmqpMessage(minimalMessage(), lockToken));
    }

    @Test
    void rejectsNullEncodedMessage() {
        assertThrows(NullPointerException.class, () -> ServiceBusReceivedMessage.fromAmqpMessage(null, LOCK_TOKEN));
    }

    @Test
    void rejectsEmptyEncodedMessage() {
        assertThrows(IllegalArgumentException.class,
            () -> ServiceBusReceivedMessage.fromAmqpMessage(BinaryData.fromBytes(new byte[0]), LOCK_TOKEN));
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 2, 5, 40, 200, 430 })
    void rejectsTruncatedMessage(int length) {
        byte[] truncated = Arrays.copyOf(fromHex(DOTNET_DATA_MESSAGE), length);

        assertThrows(IllegalArgumentException.class,
            () -> ServiceBusReceivedMessage.fromAmqpMessage(BinaryData.fromBytes(truncated), LOCK_TOKEN));
    }

    @ParameterizedTest
    @ValueSource(strings = { "ff", "0053ff", "005370a1026162", "005373a0020102", "0053707fffffff" })
    void rejectsMalformedMessage(String hex) {
        assertThrows(IllegalArgumentException.class,
            () -> ServiceBusReceivedMessage.fromAmqpMessage(BinaryData.fromBytes(fromHex(hex)), LOCK_TOKEN));
    }

    private static ServiceBusReceivedMessage decodeBody(org.apache.qpid.proton.amqp.messaging.Section body) {
        Message message = Proton.message();
        message.setBody(body);
        return ServiceBusReceivedMessage.fromAmqpMessage(encode(message), null);
    }

    private static BinaryData minimalMessage() {
        Message message = Proton.message();
        message.setMessageId("message-id");
        return encode(message);
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
