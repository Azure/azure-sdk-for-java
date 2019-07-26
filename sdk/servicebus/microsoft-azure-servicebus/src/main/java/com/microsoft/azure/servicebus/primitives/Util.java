// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.primitives;

import java.lang.reflect.Array;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Decimal128;
import org.apache.qpid.proton.amqp.Decimal32;
import org.apache.qpid.proton.amqp.Decimal64;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.UnsignedByte;
import org.apache.qpid.proton.amqp.UnsignedInteger;
import org.apache.qpid.proton.amqp.UnsignedLong;
import org.apache.qpid.proton.amqp.UnsignedShort;
import org.apache.qpid.proton.amqp.messaging.AmqpSequence;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.apache.qpid.proton.amqp.transaction.Declare;
import org.apache.qpid.proton.amqp.transaction.Discharge;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.Link;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.message.Message;

import com.microsoft.azure.servicebus.ClientSettings;
import com.microsoft.azure.servicebus.security.ManagedIdentityTokenProvider;
import com.microsoft.azure.servicebus.security.SecurityConstants;
import com.microsoft.azure.servicebus.security.SharedAccessSignatureTokenProvider;
import com.microsoft.azure.servicebus.security.TokenProvider;

public class Util {
    private static final long EPOCHINDOTNETTICKS = 621355968000000000L;
    private static final int GUIDSIZE = 16;

    private Util() {
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
            Map map = (Map) obj;
            for (Object value : map.keySet()) {
                size += Util.sizeof(value);
            }

            for (Object value : map.values()) {
                size += Util.sizeof(value);
            }

            return size;
        }

        if (obj instanceof Iterable) {
            // Size and Count each take a max of 4 bytes
            int size = 8;
            for (Object innerObject : (Iterable) obj) {
                size += Util.sizeof(innerObject);
            }

            return size;
        }

        if (obj.getClass().isArray()) {
            // Size and Count each take a max of 4 bytes
            int size = 8;
            int length = Array.getLength(obj);
            for (int i = 0; i < length; i++) {
                size += Util.sizeof(Array.get(obj, i));
            }

            return size;
        }

        throw new IllegalArgumentException(String.format(Locale.US, "Encoding Type: %s is not supported", obj.getClass()));
    }

    // .Net ticks are measured from 01/01/0001, java instants are measured from 01/01/1970
    public static Instant convertDotNetTicksToInstant(long dotNetTicks) {
        long ticksFromEpoch = dotNetTicks - EPOCHINDOTNETTICKS;
        long millisecondsFromEpoch = ticksFromEpoch / 10000;
        long fractionTicks = ticksFromEpoch % 10000;
        return Instant.ofEpochMilli(millisecondsFromEpoch).plusNanos(fractionTicks * 100);
    }

    public static long convertInstantToDotNetTicks(Instant instant) {
        return (instant.getEpochSecond() * 10000000) + (instant.getNano() / 100) + EPOCHINDOTNETTICKS;
    }

    //.Net GUID bytes are ordered in a different way.
    // First 4 bytes are in reverse order, 5th and 6th bytes are in reverse order, 7th and 8th bytes are also in reverse order
    public static UUID convertDotNetBytesToUUID(byte[] dotNetBytes) {
        if (dotNetBytes == null || dotNetBytes.length != GUIDSIZE) {
            return new UUID(0L, 0L);
        }

        byte[] reOrderedBytes = new byte[GUIDSIZE];
        for (int i = 0; i < GUIDSIZE; i++) {
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

    public static byte[] convertUUIDToDotNetBytes(UUID uniqueId) {
        if (uniqueId == null || uniqueId.equals(ClientConstants.ZEROLOCKTOKEN)) {
            return new byte[GUIDSIZE];
        }

        ByteBuffer buffer = ByteBuffer.allocate(GUIDSIZE);
        buffer.putLong(uniqueId.getMostSignificantBits());
        buffer.putLong(uniqueId.getLeastSignificantBits());
        byte[] javaBytes = buffer.array();

        byte[] dotNetBytes = new byte[GUIDSIZE];
        for (int i = 0; i < GUIDSIZE; i++) {
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

            dotNetBytes[indexInReorderedBytes] = javaBytes[i];
        }

        return dotNetBytes;
    }

    private static int getPayloadSize(Message msg) {
        if (msg == null || msg.getBody() == null) {
            return 0;
        }

        Section bodySection = msg.getBody();
        if (bodySection instanceof AmqpValue) {
            return Util.sizeof(((AmqpValue) bodySection).getValue());
        } else if (bodySection instanceof AmqpSequence) {
            return Util.sizeof(((AmqpSequence) bodySection).getValue());
        } else if (bodySection instanceof Data) {
            Data payloadSection = (Data) bodySection;
            Binary payloadBytes = payloadSection.getValue();
            return Util.sizeof(payloadBytes);
        } else {
            return 0;
        }
    }

    // Remove this.. Too many cases, too many types...
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
            annotationsSize += Util.sizeof(messageAnnotations.getValue());
        }

        if (applicationProperties != null) {
            applicationPropertiesSize += Util.sizeof(applicationProperties.getValue());
        }

        return annotationsSize + applicationPropertiesSize + payloadSize;
    }

    static Pair<byte[], Integer> encodeMessageToOptimalSizeArray(Message message, int maxMessageSize) throws PayloadSizeExceededException {
        int payloadSize = Util.getDataSerializedSize(message);
        int allocationSize = Math.min(payloadSize + ClientConstants.MAX_MESSAGING_AMQP_HEADER_SIZE_BYTES, maxMessageSize);
        byte[] encodedBytes = new byte[allocationSize];
        int encodedSize = encodeMessageToCustomArray(message, encodedBytes, 0, allocationSize);
        return new Pair<>(encodedBytes, encodedSize);
    }

    static Pair<byte[], Integer> encodeMessageToMaxSizeArray(Message message, int maxMessageSize) throws PayloadSizeExceededException {
        // May be we should reduce memory allocations. Use a pool of byte arrays or something
        byte[] encodedBytes = new byte[maxMessageSize];
        int encodedSize = encodeMessageToCustomArray(message, encodedBytes, 0, maxMessageSize);
        return new Pair<>(encodedBytes, encodedSize);
    }

    static int encodeMessageToCustomArray(Message message, byte[] encodedBytes, int offset, int length) throws PayloadSizeExceededException {
        try {
            return message.encode(encodedBytes, offset, length);
        } catch (BufferOverflowException exception) {
            throw new PayloadSizeExceededException(String.format("Size of the payload exceeded Maximum message size: %s KB", length / 1024), exception);
        }
    }

    // Pass little less than client timeout to the server so client doesn't time out before server times out
    public static Duration adjustServerTimeout(Duration clientTimeout) {
        return clientTimeout.minusMillis(100);
    }

    // This is not super stable for some reason
    static Message readMessageFromDelivery(Receiver receiveLink, Delivery delivery) {
        int msgSize = delivery.pending();
        byte[] buffer = new byte[msgSize];
        
        int read = receiveLink.recv(buffer, 0, msgSize);
        
        Message message = Proton.message();
        message.decode(buffer, 0, read);
        return message;
    }

    public static URI convertNamespaceToEndPointURI(String namespaceName) {
        try {
            return new URI(String.format(Locale.US, ClientConstants.END_POINT_FORMAT, namespaceName));
        } catch (URISyntaxException exception) {
            throw new IllegalConnectionStringFormatException(
                    String.format(Locale.US, "Invalid namespace name: %s", namespaceName),
                    exception);
        }
    }

    public static ClientSettings getClientSettingsFromConnectionStringBuilder(ConnectionStringBuilder builder) {
        TokenProvider tokenProvider = null;
        String authentication = builder.getAuthentication();
        if (authentication != null) {
            if (authentication.equalsIgnoreCase(ConnectionStringBuilder.MANAGED_IDENTITY_AUTHENTICATION_WITH_SPACE)
                    || authentication.equalsIgnoreCase(ConnectionStringBuilder.MANAGED_IDENTITY_AUTHENTICATION_WITHOUT_SPACE)) {
                tokenProvider = new ManagedIdentityTokenProvider();
            }
        } else if (builder.getSharedAccessSignatureToken() == null) {
            tokenProvider = new SharedAccessSignatureTokenProvider(builder.getSasKeyName(), builder.getSasKey(), SecurityConstants.DEFAULT_SAS_TOKEN_VALIDITY_IN_SECONDS);
        } else {
            tokenProvider = new SharedAccessSignatureTokenProvider(builder.getSharedAccessSignatureToken(), Instant.MAX); // Max validity as we will not generate another token
        }
        
        return new ClientSettings(tokenProvider, builder.getRetryPolicy(), builder.getOperationTimeout(), builder.getTransportType());
    }

    static int getTokenRenewIntervalInSeconds(int tokenValidityInSeconds) {
        if (tokenValidityInSeconds >= 300) {
            return tokenValidityInSeconds - 30;
        } else if (tokenValidityInSeconds >= 60) {
            return tokenValidityInSeconds - 10;
        } else {
            return (tokenValidityInSeconds - 1) > 0 ? tokenValidityInSeconds - 1 : 0;
        }
    }
    
    static int getMaxMessageSizeFromLink(Link link) {
        UnsignedLong maxMessageSize = link.getRemoteMaxMessageSize();
        if (maxMessageSize != null) {
            int maxMessageSizeAsInt = maxMessageSize.intValue();
            // A value of 0 means no limit. Treating no limit as 1024 KB thus putting a cap on max message size
            if (maxMessageSizeAsInt > 0) {
                return maxMessageSizeAsInt;
            }
        }

        // Default if link doesn't have the value
        return ClientConstants.MAX_MESSAGE_LENGTH_BYTES;
    }
}
