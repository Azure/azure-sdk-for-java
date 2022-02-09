// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

import com.azure.core.annotation.Fluent;

import java.time.OffsetDateTime;
import java.util.Arrays;

/**
 * The representation of message properties as defined by AMQP protocol.
 *
 * @see <a href="http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-messaging-v1.0-os.html#section-message-format" target="_blank">
 *     Amqp Message Format.</a>
 */
@Fluent
public class AmqpMessageProperties {

    private OffsetDateTime absoluteExpiryTime;
    private String contentEncoding;
    private String contentType;
    private AmqpMessageId correlationId;
    private OffsetDateTime creationTime;
    private String groupId;
    private Long groupSequence;
    private AmqpMessageId messageId;
    private String replyToGroupId;
    private AmqpAddress replyTo;
    private AmqpAddress to;
    private String subject;
    private byte[] userId;

    AmqpMessageProperties() {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
    }

    /**
     * Gets {@code absoluteExpiryTime} from amqp message properties.
     *
     * @return the {@code absoluteExpiryTime} value.
     */
    public OffsetDateTime getAbsoluteExpiryTime() {
        return absoluteExpiryTime;
    }

    /**
     * Sets the given {@code absoluteExpiryTime} value on {@link AmqpMessageProperties} object.
     *
     * @param absoluteExpiryTime to be set.
     * @return updated {@link AmqpMessageProperties} object.
     */
    public AmqpMessageProperties setAbsoluteExpiryTime(OffsetDateTime absoluteExpiryTime) {
        this.absoluteExpiryTime = absoluteExpiryTime;
        return this;
    }

    /**
     * Gets AbsoluteExpiryTime from amqp message properties.
     *
     * @return the {@code absoluteExpiryTime} value.
     */
    public String getContentEncoding() {
        return contentEncoding;
    }

    /**
     * Sets the given {@code contentEncoding} value on {@link AmqpMessageProperties} object.
     *
     * @param contentEncoding to be set.
     *
     * @return updated {@link AmqpMessageProperties} object.
     */
    public AmqpMessageProperties setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
        return this;
    }

    /**
     * Gets {@code contentType} from amqp message properties.
     *
     * @return the {@code contentType} value.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets the given {@code contentType} value on {@link AmqpMessageProperties} object.
     *
     * @param contentType to be set.
     *
     * @return updated {@link AmqpMessageProperties} object.
     */
    public AmqpMessageProperties setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Gets {@code correlationId} from amqp message properties.
     *
     * @return the {@code correlationId} value.
     */
    public AmqpMessageId getCorrelationId() {
        return correlationId;
    }

    /**
     * Sets the given {@code correlationId} value on {@link AmqpMessageProperties} object.
     *
     * @param correlationId to be set.
     *
     * @return updated {@link AmqpMessageProperties} object.
     */
    public AmqpMessageProperties setCorrelationId(AmqpMessageId correlationId) {
        this.correlationId = correlationId;
        return this;
    }

    /**
     * Gets {@code creationTime} from amqp message properties.
     *
     * @return the {@code creationTime} value.
     */
    public OffsetDateTime getCreationTime() {
        return creationTime;
    }

    /**
     * Sets the given {@code creationTime} value on {@link AmqpMessageProperties} object.
     *
     * @param creationTime to be set.
     *
     * @return updated {@link AmqpMessageProperties} object.
     */
    public AmqpMessageProperties setCreationTime(OffsetDateTime creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    /**
     * Gets {@code groupId} from amqp message properties.
     *
     * @return the {@code groupId} value.
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Sets the given {@code groupId} value on {@link AmqpMessageProperties} object.
     *
     * @param groupId to be set.
     *
     * @return updated {@link AmqpMessageProperties} object.
     */
    public AmqpMessageProperties setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    /**
     * Gets {@code groupSequence} from amqp message properties.
     *
     * @return the {@code groupSequence} value.
     */
    public Long getGroupSequence() {
        return groupSequence;
    }

    /**
     * Sets the given {@code groupSequence} value on {@link AmqpMessageProperties} object.
     *
     * @param groupSequence to be set.
     *
     * @return updated {@link AmqpMessageProperties} object.
     */
    public AmqpMessageProperties setGroupSequence(Long groupSequence) {
        this.groupSequence = groupSequence;
        return this;
    }

    /**
     * Gets {@code messageId} from amqp message properties.
     *
     * @return the {@code messageId} value.
     */
    public AmqpMessageId getMessageId() {
        return messageId;
    }

    /**
     * Sets the given {@code messageId} value on {@link AmqpMessageProperties} object.
     *
     * @param messageId to be set .
     *
     * @return updated {@link AmqpMessageProperties} object.
     */
    public AmqpMessageProperties setMessageId(AmqpMessageId messageId) {
        this.messageId = messageId;
        return this;
    }

    /**
     * Gets {@code replyTo} from amqp message properties.
     *
     * @return The {@code replyTo} value.
     */
    public AmqpAddress getReplyTo() {
        return replyTo;
    }

    /**
     * Sets the given {@code replyTo} value on {@link AmqpMessageProperties} object.
     *
     * @param replyTo to be set.
     *
     * @return updated {@link AmqpMessageProperties} object.
     */
    public AmqpMessageProperties setReplyTo(AmqpAddress replyTo) {
        this.replyTo = replyTo;
        return this;
    }

    /**
     * Gets {@code replyToGroupId} from amqp message properties.
     *
     * @return The {@code replyToGroupId} value.
     */
    public String getReplyToGroupId() {
        return replyToGroupId;
    }

    /**
     * Sets the given {@code replyToGroupId} value on {@link AmqpMessageProperties} object.
     *
     * @param replyToGroupId to be set.
     *
     * @return updated {@link AmqpMessageProperties} object.
     */
    public AmqpMessageProperties setReplyToGroupId(String replyToGroupId) {
        this.replyToGroupId = replyToGroupId;
        return this;
    }

    /**
     * Gets {@code subject} from amqp message properties.
     *
     * @return the {@code subject} value.
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Sets the given {@code subject} value on {@link AmqpMessageProperties} object.
     *
     * @param subject to be set.
     *
     * @return updated {@link AmqpMessageProperties} object.
     */
    public AmqpMessageProperties setSubject(String subject) {
        this.subject = subject;
        return this;
    }

    /**
     * Gets {@code to} from amqp message properties.
     *
     * @return the {@code to} value.
     */
    public AmqpAddress getTo() {
        return to;
    }

    /**
     * Sets the given {@code to} value on {@link AmqpMessageProperties} object.
     *
     * @param to to be set.
     *
     * @return updated {@link AmqpMessageProperties} object.
     */
    public AmqpMessageProperties setTo(AmqpAddress to) {
        this.to = to;
        return this;
    }

    /**
     * Gets {@code userId} from amqp message properties.
     *
     * @return the {@code userId} value.
     */
    public byte[] getUserId() {
        return userId != null ? Arrays.copyOf(userId, userId.length) : new byte[0];
    }

    /**
     * Sets the given {@code userId} value on {@link AmqpMessageProperties} object.
     *
     * @param userId to be set .
     * @return updated {@link AmqpMessageProperties} object.
     */
    public AmqpMessageProperties setUserId(byte[] userId) {
        this.userId = userId != null ? Arrays.copyOf(userId, userId.length) : new byte[0];
        return this;
    }

}
