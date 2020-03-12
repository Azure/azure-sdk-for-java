// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Decimal128;
import org.apache.qpid.proton.amqp.Decimal32;
import org.apache.qpid.proton.amqp.Decimal64;
import org.apache.qpid.proton.amqp.messaging.AmqpSequence;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.transaction.Declare;
import org.apache.qpid.proton.amqp.transaction.Discharge;
import org.apache.qpid.proton.amqp.UnsignedByte;
import org.apache.qpid.proton.amqp.UnsignedInteger;
import org.apache.qpid.proton.amqp.UnsignedLong;
import org.apache.qpid.proton.amqp.UnsignedShort;
import org.apache.qpid.proton.message.Message;


import java.lang.reflect.Array;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Contains helper methods for message conversions.
 */
final class MessageUtils {
    public static final UUID ZERO_LOCK_TOKEN = new UUID(0L, 0L);
    private static final int LOCK_TOKEN_SIZE = 16;
    private static final int GUID_SIZE = 16;
    private static final int MAX_MESSAGING_AMQP_HEADER_SIZE_BYTES = 512;

    private MessageUtils() {
    }

    /**
     * Converts a .NET GUID to its Java UUID representation.
     *
     * @param dotNetBytes .NET GUID to convert.
     *
     * @return the equivalent UUID.
     */
    static UUID convertDotNetBytesToUUID(byte[] dotNetBytes) {
        // First 4 bytes are in reverse order, 5th and 6th bytes are in reverse order,
        // 7th and 8th bytes are also in reverse order
        if (dotNetBytes == null || dotNetBytes.length != GUID_SIZE) {
            return ZERO_LOCK_TOKEN;
        }

        byte[] reOrderedBytes = new byte[GUID_SIZE];
        for (int i = 0; i < GUID_SIZE; i++) {
            int indexInReorderedBytes;
            switch (i) {
                case 0:
                    indexInReorderedBytes = 3;
                    break;
                case 1:
                    indexInReorderedBytes = 2;
                    break;
                case 2:
                    indexInReorderedBytes = 1;
                    break;
                case 3:
                    indexInReorderedBytes = 0;
                    break;
                case 4:
                    indexInReorderedBytes = 5;
                    break;
                case 5:
                    indexInReorderedBytes = 4;
                    break;
                case 6:
                    indexInReorderedBytes = 7;
                    break;
                case 7:
                    indexInReorderedBytes = 6;
                    break;
                default:
                    indexInReorderedBytes = i;
            }

            reOrderedBytes[indexInReorderedBytes] = dotNetBytes[i];
        }

        ByteBuffer buffer = ByteBuffer.wrap(reOrderedBytes);
        long mostSignificantBits = buffer.getLong();
        long leastSignificantBits = buffer.getLong();
        return new UUID(mostSignificantBits, leastSignificantBits);
    }



    private static int getPayloadSize(Message msg) {
        if (msg == null || msg.getBody() == null) {
            return 0;
        }

        Section bodySection = msg.getBody();
        if (bodySection instanceof AmqpValue) {
            return sizeof(((AmqpValue) bodySection).getValue());
        } else if (bodySection instanceof AmqpSequence) {
            return sizeof(((AmqpSequence) bodySection).getValue());
        } else if (bodySection instanceof Data) {
            Data payloadSection = (Data) bodySection;
            Binary payloadBytes = payloadSection.getValue();
            return sizeof(payloadBytes);
        } else {
            return 0;
        }
    }

    public static int getDataSerializedSize(Message amqpMessage) {
        if (amqpMessage == null) {
            return 0;
        }

        int payloadSize = getPayloadSize(amqpMessage);

        // EventData - accepts only PartitionKey - which is a String & stuffed into MessageAnnotation
        MessageAnnotations messageAnnotations = amqpMessage.getMessageAnnotations();
        ApplicationProperties applicationProperties = amqpMessage.getApplicationProperties();

        int annotationsSize = 0;
        int applicationPropertiesSize = 0;

        if (messageAnnotations != null) {
            annotationsSize += sizeof(messageAnnotations.getValue());
        }

        if (applicationProperties != null) {
            applicationPropertiesSize += sizeof(applicationProperties.getValue());
        }

        return annotationsSize + applicationPropertiesSize + payloadSize;
    }
    static Pair<byte[], Integer> encodeMessageToOptimalSizeArray(Message message, int maxMessageSize)  {
        int payloadSize = MessageUtils.getDataSerializedSize(message);
        int allocationSize = Math.min(payloadSize + MAX_MESSAGING_AMQP_HEADER_SIZE_BYTES, maxMessageSize);
        byte[] encodedBytes = new byte[allocationSize];
        int encodedSize = encodeMessageToCustomArray(message, encodedBytes, 0, allocationSize);
        return new Pair<>(encodedBytes, encodedSize);
    }

    static Pair<byte[], Integer> encodeMessageToMaxSizeArray(Message message, int maxMessageSize) {
        // May be we should reduce memory allocations. Use a pool of byte arrays or something
        byte[] encodedBytes = new byte[maxMessageSize];
        int encodedSize = encodeMessageToCustomArray(message, encodedBytes, 0, maxMessageSize);
        return new Pair<>(encodedBytes, encodedSize);
    }

    static int encodeMessageToCustomArray(Message message, byte[] encodedBytes, int offset, int length) {
        try {
            return message.encode(encodedBytes, offset, length);
        } catch (BufferOverflowException exception) {
            System.out.println("MessageUtils.encodeMessageToCustomArray BufferOverflowException "+exception);
            throw exception;
            //throw new PayloadSizeExceededException(String.format("Size of the payload exceeded Maximum message size: %s KB", length / 1024), exception);
        }
    }
    static int sizeof(Object obj) {
        if (obj == null) {
            return 0;
        }

        if (obj instanceof String) {
            return obj.toString().length() << 1;
        }

        if (obj instanceof Symbol) {
            return ((Symbol) obj).length() << 1;
        }

        if (obj instanceof Byte || obj instanceof UnsignedByte) {
            return Byte.BYTES;
        }

        if (obj instanceof Integer || obj instanceof UnsignedInteger) {
            return Integer.BYTES;
        }

        if (obj instanceof Long || obj instanceof UnsignedLong || obj instanceof Date) {
            return Long.BYTES;
        }

        if (obj instanceof Short || obj instanceof UnsignedShort) {
            return Short.BYTES;
        }

        if (obj instanceof Boolean) {
            return 1;
        }

        if (obj instanceof Character) {
            return 4;
        }

        if (obj instanceof Float) {
            return Float.BYTES;
        }

        if (obj instanceof Double) {
            return Double.BYTES;
        }

        if (obj instanceof UUID) {
            // UUID is internally represented as 16 bytes. But how does ProtonJ encode it? To be safe..we can treat it as a string of 36 chars = 72 bytes.
            //return 72;
            return 16;
        }

        if (obj instanceof Decimal32) {
            return 4;
        }

        if (obj instanceof Decimal64) {
            return 8;
        }

        if (obj instanceof Decimal128) {
            return 16;
        }

        if (obj instanceof Binary) {
            return ((Binary) obj).getLength();
        }

        if (obj instanceof Declare) {
            // Empty declare command takes up 7 bytes.
            return 7;
        }

        if (obj instanceof Discharge) {
            Discharge discharge = (Discharge) obj;
            return 12 + discharge.getTxnId().getLength();
        }

        if (obj instanceof Map) {
            // Size and Count each take a max of 4 bytes
            int size = 8;
            @SuppressWarnings("unchecked")
            Map<Object, Object> map = (Map<Object, Object>) obj;
            for (Object value : map.keySet()) {
                size += sizeof(value);
            }

            for (Object value : map.values()) {
                size += sizeof(value);
            }

            return size;
        }

        if (obj instanceof Iterable) {
            // Size and Count each take a max of 4 bytes
            int size = 8;
            for (Object innerObject : (Iterable) obj) {
                size += sizeof(innerObject);
            }

            return size;
        }

        if (obj.getClass().isArray()) {
            // Size and Count each take a max of 4 bytes
            int size = 8;
            int length = Array.getLength(obj);
            for (int i = 0; i < length; i++) {
                size += sizeof(Array.get(obj, i));
            }

            return size;
        }

        throw new IllegalArgumentException(String.format(Locale.US, "Encoding Type: %s is not supported", obj.getClass()));
    }

    // Pass little less than client timeout to the server so client doesn't time out before server times out
    public static Duration adjustServerTimeout(Duration clientTimeout) {
        return clientTimeout.minusMillis(200);
    }
}
