// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpMessageConstant;
import com.azure.core.amqp.models.AmqpAnnotatedMessage;
import com.azure.core.amqp.models.AmqpMessageProperties;
import com.azure.core.util.logging.ClientLogger;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Provides an abstraction over {@link AmqpAnnotatedMessage} and properties that are published by the service. This is a
 * read-only view. The properties themselves can be updated via {@link AmqpAnnotatedMessage}.
 *
 * @see <a href="https://github.com/Azure/azure-sdk-for-net/blob/main/sdk/eventhub/Azure.Messaging.EventHubs/src/Amqp/AmqpSystemProperties.cs">AmqpSystemProperties</a>
 */
final class SystemProperties implements Map<String, Object> {
    private final ClientLogger logger = new ClientLogger(SystemProperties.class);
    private final Long offset;
    private final String partitionKey;
    private final Instant enqueuedTime;
    private final Long sequenceNumber;
    private final AmqpAnnotatedMessage message;

    /**
     * Creates an empty set of system properties. This is the case where a message was not received.
     */
    SystemProperties() {
        this.message = null;
        this.offset = null;
        this.enqueuedTime = null;
        this.partitionKey = null;
        this.sequenceNumber = null;
    }

    SystemProperties(final AmqpAnnotatedMessage message, long offset, Instant enqueuedTime, long sequenceNumber,
        String partitionKey) {
        this.message = Objects.requireNonNull(message, "'message' cannot be null.");
        this.offset = offset;
        this.enqueuedTime = enqueuedTime;
        this.sequenceNumber = sequenceNumber;
        this.partitionKey = partitionKey;
    }

    /**
     * Gets the offset within the Event Hubs stream.
     *
     * @return The offset within the Event Hubs stream.
     */
    Long getOffset() {
        return offset;
    }

    /**
     * Gets a partition key used for message partitioning. If it exists, this value was used to compute a hash to select
     * a partition to send the message to.
     *
     * @return A partition key for this Event Data.
     */
    String getPartitionKey() {
        return partitionKey;
    }

    /**
     * Gets the time this event was enqueued in the Event Hub.
     *
     * @return The time this was enqueued in the service.
     */
    Instant getEnqueuedTime() {
        return enqueuedTime;
    }

    /**
     * Gets the sequence number in the event stream for this event. This is unique for every message received in the
     * Event Hub.
     *
     * @return Sequence number for this event.
     *
     * @throws IllegalStateException if {@link SystemProperties} does not contain the sequence number in a retrieved
     *     event.
     */
    Long getSequenceNumber() {
        return sequenceNumber;
    }

    @Override
    public int size() {
        if (message == null) {
            return 0;
        }

        return entrySet().size();
    }

    @Override
    public Collection<Object> values() {
        if (message == null) {
            return Collections.emptyList();
        }

        return entrySet().stream().map(Entry::getValue).collect(Collectors.toList());
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        if (message == null) {
            return Collections.emptySet();
        }

        final AmqpMessageProperties properties = message.getProperties();

        final HashSet<Entry<String, Object>> entries = new HashSet<>();
        if (properties.getMessageId() != null) {
            entries.add(new SystemPropertiesEntry(
                AmqpMessageConstant.MESSAGE_ID.getValue(), properties.getMessageId().toString(), logger));
        }
        if (properties.getUserId() != null && properties.getUserId().length > 0) {
            entries.add(new SystemPropertiesEntry(
                AmqpMessageConstant.USER_ID.getValue(), properties.getUserId(), logger));
        }
        if (properties.getTo() != null) {
            entries.add(new SystemPropertiesEntry(
                AmqpMessageConstant.TO.getValue(), properties.getTo(), logger));
        }
        if (properties.getSubject() != null) {
            entries.add(new SystemPropertiesEntry(
                AmqpMessageConstant.SUBJECT.getValue(), properties.getSubject(), logger));
        }
        if (properties.getReplyTo() != null) {
            entries.add(new SystemPropertiesEntry(
                AmqpMessageConstant.REPLY_TO.getValue(), properties.getReplyTo(), logger));
        }
        if (properties.getCorrelationId() != null) {
            entries.add(new SystemPropertiesEntry(
                AmqpMessageConstant.CORRELATION_ID.getValue(), properties.getCorrelationId().toString(), logger));
        }
        if (properties.getContentType() != null) {
            entries.add(new SystemPropertiesEntry(
                AmqpMessageConstant.CONTENT_TYPE.getValue(), properties.getContentType(), logger));
        }
        if (properties.getContentEncoding() != null) {
            entries.add(new SystemPropertiesEntry(
                AmqpMessageConstant.CONTENT_ENCODING.getValue(), properties.getContentEncoding(), logger));
        }
        if (properties.getContentEncoding() != null) {
            entries.add(new SystemPropertiesEntry(
                AmqpMessageConstant.ABSOLUTE_EXPIRY_TIME.getValue(), properties.getContentEncoding(), logger));
        }
        if (properties.getCreationTime() != null) {
            entries.add(new SystemPropertiesEntry(
                AmqpMessageConstant.CREATION_TIME.getValue(), properties.getCreationTime(), logger));
        }
        if (properties.getGroupId() != null) {
            entries.add(new SystemPropertiesEntry(
                AmqpMessageConstant.GROUP_ID.getValue(), properties.getGroupId(), logger));
        }
        if (properties.getGroupSequence() != null) {
            entries.add(new SystemPropertiesEntry(
                AmqpMessageConstant.GROUP_SEQUENCE.getValue(), properties.getGroupSequence(), logger));
        }
        if (properties.getReplyToGroupId() != null) {
            entries.add(new SystemPropertiesEntry(
                AmqpMessageConstant.REPLY_TO_GROUP_ID.getValue(), properties.getReplyToGroupId(), logger));
        }

        message.getMessageAnnotations().forEach((key, value) ->
            entries.add(new SystemPropertiesEntry(key, value, logger)));

        return entries;
    }

    @Override
    public Object getOrDefault(Object key, Object defaultValue) {
        if (message == null) {
            return defaultValue;
        }

        if (!containsKey(key)) {
            return defaultValue;
        }

        return get(key);
    }

    @Override
    public Set<String> keySet() {
        if (message == null) {
            return Collections.emptySet();
        }

        return entrySet().stream().map(Entry::getKey).collect(Collectors.toSet());
    }

    @Override
    public boolean containsKey(Object key) {
        if (message == null) {
            return false;
        }

        if (key == null) {
            throw logger.logExceptionAsError(new NullPointerException("'key' cannot be null"));
        } else if (!(key instanceof String)) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                String.format("'key' is not a string. key: %s. class: %s", key, key.getClass())));
        }

        return keySet().contains(key);
    }

    @Override
    public boolean containsValue(Object value) {
        if (message == null) {
            return false;
        }

        return values().contains(value);
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public Object get(Object key) {
        if (message == null) {
            return null;
        }

        if (key == null) {
            throw logger.logExceptionAsError(new NullPointerException("'key' cannot be null"));
        } else if (!(key instanceof String)) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                String.format("'key' is not a string. key: %s. class: %s", key, key.getClass())));
        }

        final String keyValue = (String) key;
        if (AmqpMessageConstant.MESSAGE_ID.getValue().equals(keyValue)) {
            if (message.getProperties().getMessageId() != null) {
                return message.getProperties().getMessageId().toString();
            } else {
                return null;
            }
        }

        if (AmqpMessageConstant.USER_ID.getValue().equals(keyValue)) {
            return message.getProperties().getUserId();
        }

        if (AmqpMessageConstant.TO.getValue().equals(keyValue)) {
            if (message.getProperties().getTo() != null) {
                return message.getProperties().getTo().toString();
            } else {
                return null;
            }
        }

        if (AmqpMessageConstant.SUBJECT.getValue().equals(keyValue)) {
            return message.getProperties().getSubject();
        }

        if (AmqpMessageConstant.REPLY_TO.getValue().equals(keyValue)) {
            if (message.getProperties().getReplyTo() != null) {
                return message.getProperties().getReplyTo().toString();
            } else {
                return null;
            }
        }

        if (AmqpMessageConstant.CORRELATION_ID.getValue().equals(keyValue)) {
            if (message.getProperties().getCorrelationId() != null) {
                return message.getProperties().getCorrelationId().toString();
            } else {
                return null;
            }
        }

        if (AmqpMessageConstant.CONTENT_TYPE.getValue().equals(keyValue)) {
            return message.getProperties().getContentType();
        }
        if (AmqpMessageConstant.CONTENT_ENCODING.getValue().equals(keyValue)) {
            return message.getProperties().getContentEncoding();
        }
        if (AmqpMessageConstant.ABSOLUTE_EXPIRY_TIME.getValue().equals(keyValue)) {
            return message.getProperties().getAbsoluteExpiryTime();
        }
        if (AmqpMessageConstant.CREATION_TIME.getValue().equals(keyValue)) {
            return message.getProperties().getCreationTime();
        }
        if (AmqpMessageConstant.GROUP_ID.getValue().equals(keyValue)) {
            return message.getProperties().getGroupId();
        }

        if (AmqpMessageConstant.GROUP_SEQUENCE.getValue().equals(keyValue)) {
            return message.getProperties().getGroupSequence();
        }

        if (AmqpMessageConstant.REPLY_TO_GROUP_ID.getValue().equals(keyValue)) {
            return message.getProperties().getReplyToGroupId();
        }

        return message.getMessageAnnotations().get(keyValue);
    }

    @Override
    public int hashCode() {
        if (message == null) {
            return super.hashCode();
        }

        return Objects.hash(message, message.getMessageAnnotations());
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public Object put(String key, Object value) {
        throw logger.logExceptionAsError(
            new UnsupportedOperationException("System properties are read-only. Cannot perform 'put' operation."));
    }

    @Override
    public boolean remove(Object key, Object value) {
        throw logger.logExceptionAsError(
            new UnsupportedOperationException("System properties are read-only. Cannot perform 'remove' operation."));
    }

    @Override
    public Object remove(Object key) {
        throw logger.logExceptionAsError(
            new UnsupportedOperationException("System properties are read-only. Cannot perform 'remove' operation."));
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        throw logger.logExceptionAsError(
            new UnsupportedOperationException("System properties are read-only. Cannot perform 'putAll' operation."));
    }

    @Override
    public void clear() {
        throw logger.logExceptionAsError(
            new UnsupportedOperationException("System properties are read-only. Cannot perform 'clear' operation."));
    }

    /**
     * Represents a read-only system properties entry.
     */
    private static class SystemPropertiesEntry implements Map.Entry<String, Object> {
        private final ClientLogger logger;
        private final String key;
        private final Object value;

        SystemPropertiesEntry(String key, Object value, ClientLogger logger) {
            this.key = key;
            this.value = value;
            this.logger = logger;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public Object setValue(Object value) {
            throw logger.logExceptionAsError(
                new UnsupportedOperationException("Cannot update entry. System properties is read-only."));
        }
    }
}
