// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import com.microsoft.azure.eventhubs.EventData;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.AmqpSequence;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.apache.qpid.proton.message.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class EventDataImpl implements EventData {
    private static final long serialVersionUID = -5631628195600014255L;
    private static final int BODY_DATA_NULL = -1;

    private transient Binary bodyData;
    private transient Object amqpBody;

    private Map<String, Object> properties;
    private SystemProperties systemProperties;

    private EventDataImpl() {
    }

    @SuppressWarnings("unchecked")
    EventDataImpl(Message amqpMessage) {
        if (amqpMessage == null) {
            throw new IllegalArgumentException("amqpMessage cannot be null");
        }

        final Map<Symbol, Object> messageAnnotations = amqpMessage.getMessageAnnotations().getValue();
        final HashMap<String, Object> receiveProperties = new HashMap<>();

        for (Map.Entry<Symbol, Object> annotation : messageAnnotations.entrySet()) {
            receiveProperties.put(annotation.getKey().toString(), annotation.getValue() != null ? annotation.getValue() : null);
        }

        if (amqpMessage.getProperties() != null) {
            if (amqpMessage.getMessageId() != null) {
                receiveProperties.put(AmqpConstants.AMQP_PROPERTY_MESSAGE_ID, amqpMessage.getMessageId());
            }
            if (amqpMessage.getUserId() != null) {
                receiveProperties.put(AmqpConstants.AMQP_PROPERTY_USER_ID, amqpMessage.getUserId());
            }
            if (amqpMessage.getAddress() != null) {
                receiveProperties.put(AmqpConstants.AMQP_PROPERTY_TO, amqpMessage.getAddress());
            }
            if (amqpMessage.getSubject() != null) {
                receiveProperties.put(AmqpConstants.AMQP_PROPERTY_SUBJECT, amqpMessage.getSubject());
            }
            if (amqpMessage.getReplyTo() != null) {
                receiveProperties.put(AmqpConstants.AMQP_PROPERTY_REPLY_TO, amqpMessage.getReplyTo());
            }
            if (amqpMessage.getCorrelationId() != null) {
                receiveProperties.put(AmqpConstants.AMQP_PROPERTY_CORRELATION_ID, amqpMessage.getCorrelationId());
            }
            if (amqpMessage.getContentType() != null) {
                receiveProperties.put(AmqpConstants.AMQP_PROPERTY_CONTENT_TYPE, amqpMessage.getContentType());
            }
            if (amqpMessage.getContentEncoding() != null) {
                receiveProperties.put(AmqpConstants.AMQP_PROPERTY_CONTENT_ENCODING, amqpMessage.getContentEncoding());
            }
            if (amqpMessage.getProperties().getAbsoluteExpiryTime() != null) {
                receiveProperties.put(AmqpConstants.AMQP_PROPERTY_ABSOLUTE_EXPRITY_TIME, amqpMessage.getExpiryTime());
            }
            if (amqpMessage.getProperties().getCreationTime() != null) {
                receiveProperties.put(AmqpConstants.AMQP_PROPERTY_CREATION_TIME, amqpMessage.getCreationTime());
            }
            if (amqpMessage.getGroupId() != null) {
                receiveProperties.put(AmqpConstants.AMQP_PROPERTY_GROUP_ID, amqpMessage.getGroupId());
            }
            if (amqpMessage.getProperties().getGroupSequence() != null) {
                receiveProperties.put(AmqpConstants.AMQP_PROPERTY_GROUP_SEQUENCE, amqpMessage.getGroupSequence());
            }
            if (amqpMessage.getReplyToGroupId() != null) {
                receiveProperties.put(AmqpConstants.AMQP_PROPERTY_REPLY_TO_GROUP_ID, amqpMessage.getReplyToGroupId());
            }
        }

        this.systemProperties = new SystemProperties(receiveProperties);
        this.properties = amqpMessage.getApplicationProperties() == null ? null : amqpMessage.getApplicationProperties().getValue();

        final Section bodySection = amqpMessage.getBody();
        if (bodySection != null) {
            if (bodySection instanceof Data) {
                this.bodyData = ((Data) bodySection).getValue();
                this.amqpBody = this.bodyData;
            } else if (bodySection instanceof AmqpValue) {
                this.amqpBody = ((AmqpValue) bodySection).getValue();
            } else if (bodySection instanceof AmqpSequence) {
                this.amqpBody = ((AmqpSequence) bodySection).getValue();
            }
        }

        amqpMessage.clear();
    }

    public EventDataImpl(byte[] data) {
        this();

        if (data == null) {
            throw new IllegalArgumentException("data cannot be null.");
        }

        this.bodyData = new Binary(data);
    }

    public EventDataImpl(byte[] data, final int offset, final int length) {
        this();

        if (data == null) {
            throw new IllegalArgumentException("data cannot be null");
        }

        this.bodyData = new Binary(data, offset, length);
    }

    public EventDataImpl(ByteBuffer buffer) {
        this();

        if (buffer == null) {
            throw new IllegalArgumentException("data cannot be null");
        }

        this.bodyData = Binary.create(buffer);
    }

    public Object getObject() {
        return this.amqpBody;
    }

    public byte[] getBytes() {

        if (this.bodyData == null) {
            return null;
        }

        return this.bodyData.getArray();
    }

    public Map<String, Object> getProperties() {
        if (this.properties == null) {
            this.properties = new HashMap<>();
        }

        return this.properties;
    }

    public SystemProperties getSystemProperties() {
        return this.systemProperties;
    }

    public void setSystemProperties(EventData.SystemProperties props) {
        this.systemProperties = props;
    }

    // This is intended to be used while sending EventData - so EventData.SystemProperties will not be copied over to the AmqpMessage
    Message toAmqpMessage() {
        final Message amqpMessage = Proton.message();

        if (this.properties != null && !this.properties.isEmpty()) {
            final ApplicationProperties applicationProperties = new ApplicationProperties(this.properties);
            amqpMessage.setApplicationProperties(applicationProperties);
        }

        if (this.systemProperties != null && !this.systemProperties.isEmpty()) {
            for (Map.Entry<String, Object> systemProperty : this.systemProperties.entrySet()) {
                final String propertyName = systemProperty.getKey();
                if (!EventDataUtil.RESERVED_SYSTEM_PROPERTIES.contains(propertyName)) {
                    if (AmqpConstants.RESERVED_PROPERTY_NAMES.contains(propertyName)) {
                        switch (propertyName) {
                            case AmqpConstants.AMQP_PROPERTY_MESSAGE_ID:
                                amqpMessage.setMessageId(systemProperty.getValue());
                                break;
                            case AmqpConstants.AMQP_PROPERTY_USER_ID:
                                amqpMessage.setUserId((byte[]) systemProperty.getValue());
                                break;
                            case AmqpConstants.AMQP_PROPERTY_TO:
                                amqpMessage.setAddress((String) systemProperty.getValue());
                                break;
                            case AmqpConstants.AMQP_PROPERTY_SUBJECT:
                                amqpMessage.setSubject((String) systemProperty.getValue());
                                break;
                            case AmqpConstants.AMQP_PROPERTY_REPLY_TO:
                                amqpMessage.setReplyTo((String) systemProperty.getValue());
                                break;
                            case AmqpConstants.AMQP_PROPERTY_CORRELATION_ID:
                                amqpMessage.setCorrelationId(systemProperty.getValue());
                                break;
                            case AmqpConstants.AMQP_PROPERTY_CONTENT_TYPE:
                                amqpMessage.setContentType((String) systemProperty.getValue());
                                break;
                            case AmqpConstants.AMQP_PROPERTY_CONTENT_ENCODING:
                                amqpMessage.setContentEncoding((String) systemProperty.getValue());
                                break;
                            case AmqpConstants.AMQP_PROPERTY_ABSOLUTE_EXPRITY_TIME:
                                amqpMessage.setExpiryTime((long) systemProperty.getValue());
                                break;
                            case AmqpConstants.AMQP_PROPERTY_CREATION_TIME:
                                amqpMessage.setCreationTime((long) systemProperty.getValue());
                                break;
                            case AmqpConstants.AMQP_PROPERTY_GROUP_ID:
                                amqpMessage.setGroupId((String) systemProperty.getValue());
                                break;
                            case AmqpConstants.AMQP_PROPERTY_GROUP_SEQUENCE:
                                amqpMessage.setGroupSequence((long) systemProperty.getValue());
                                break;
                            case AmqpConstants.AMQP_PROPERTY_REPLY_TO_GROUP_ID:
                                amqpMessage.setReplyToGroupId((String) systemProperty.getValue());
                                break;
                            default:
                                throw new RuntimeException("unreachable");
                        }
                    } else {
                        final MessageAnnotations messageAnnotations = (amqpMessage.getMessageAnnotations() == null)
                                ? new MessageAnnotations(new HashMap<>())
                                : amqpMessage.getMessageAnnotations();
                        messageAnnotations.getValue().put(Symbol.getSymbol(systemProperty.getKey()), systemProperty.getValue());
                        amqpMessage.setMessageAnnotations(messageAnnotations);
                    }
                }
            }
        }

        if (this.bodyData != null) {
            amqpMessage.setBody(new Data(this.bodyData));
        } else if (this.amqpBody != null) {
            if (this.amqpBody instanceof List) {
                amqpMessage.setBody(new AmqpSequence((List) this.amqpBody));
            } else {
                amqpMessage.setBody(new AmqpValue(this.amqpBody));
            }
        }

        return amqpMessage;
    }

    Message toAmqpMessage(final String partitionKey) {
        final Message amqpMessage = this.toAmqpMessage();

        final MessageAnnotations messageAnnotations = (amqpMessage.getMessageAnnotations() == null)
                ? new MessageAnnotations(new HashMap<>())
                : amqpMessage.getMessageAnnotations();
        messageAnnotations.getValue().put(AmqpConstants.PARTITION_KEY, partitionKey);
        amqpMessage.setMessageAnnotations(messageAnnotations);

        return amqpMessage;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();

        out.writeInt(this.bodyData == null ? BODY_DATA_NULL : this.bodyData.getLength());
        if (this.bodyData != null) {
            out.write(this.bodyData.getArray(), this.bodyData.getArrayOffset(), this.bodyData.getLength());
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        final int length = in.readInt();
        if (length != BODY_DATA_NULL) {

            final byte[] data = new byte[length];
            in.readFully(data, 0, length);
            this.bodyData = new Binary(data, 0, length);
        }
    }

    @Override
    public int compareTo(EventData other) {
        return Long.compare(
                this.getSystemProperties().getSequenceNumber(),
                other.getSystemProperties().getSequenceNumber()
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EventDataImpl eventData = (EventDataImpl) o;
        return Objects.equals(bodyData, eventData.bodyData)
            && Objects.equals(amqpBody, eventData.amqpBody);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bodyData, amqpBody);
    }
}
