// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

import com.azure.core.annotation.Fluent;

import java.time.OffsetDateTime;

/**
 * Represents properties from Amqp message.
 */
@Fluent
public class AmqpMessageProperties {

    private OffsetDateTime absoluteExpiryTime;
    private String contentEncoding;
    private String contentType;
    private String correlationId;
    private OffsetDateTime creationTime;
    private String groupId;
    private String groupSequence;
    private String messageId;
    private String replyToGroupId;
    private String replyTo;
    private String to;
    private String subject;
    private byte[] userId;

    /**
     * Gets AbsoluteExpiryTime from Amqp message.
     *
     * @return The {@code absoluteExpiryTime}.
     */
    public OffsetDateTime getAbsoluteExpiryTime() {
        return this.absoluteExpiryTime;
    }

    /**
     * Sets the given {@code absoluteExpiryTime} value on {@link AmqpMessageProperties} object.
     *
     * @param absoluteExpiryTime to be set .
     * @return updated {@link AmqpMessageProperties} object.
     */
    public AmqpMessageProperties setAbsoluteExpiryTime(OffsetDateTime absoluteExpiryTime) {
        this.absoluteExpiryTime = absoluteExpiryTime;
        return this;
    }

    /**
     * Gets AbsoluteExpiryTime from Amqp message.
     *
     * @return The {@code absoluteExpiryTime}.
     */
    public String getContentEncoding() {
        return this.contentEncoding;
    }

    /**
     * Sets the given {@code contentEncoding} value on {@link AmqpMessageProperties} object.
     *
     * @param contentEncoding to be set .
     * @return updated {@link AmqpMessageProperties} object.
     */
    public AmqpMessageProperties setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
        return this;
    }

    /**
     * Gets {@code contentType} from the Amqp message.
     *
     * @return The {@code contentType}.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets the given {@code contentType} value on {@link AmqpMessageProperties} object.
     *
     * @param contentType to be set .
     * @return updated {@link AmqpMessageProperties} object.
     */
    public AmqpMessageProperties setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Gets {@code correlationId} from the Amqp message.
     *
     * @return The {@code correlationId}.
     */
    public String getCorrelationId() {
        return this.correlationId;
    }

    /**
     * Sets the given {@code correlationId} value on {@link AmqpMessageProperties} object.
     *
     * @param correlationId to be set .
     * @return updated {@link AmqpMessageProperties} object.
     */
    public AmqpMessageProperties setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
        return this;
    }

    /**
     * Gets {@code creationTime} from the Amqp message.
     *
     * @return The {@code creationTime}.
     */
    public OffsetDateTime getCreationTime() {
        return this.creationTime;
    }

    /**
     * Sets the given {@code creationTime} value on {@link AmqpMessageProperties} object.
     *
     * @param creationTime to be set .
     * @return updated {@link AmqpMessageProperties} object.
     */
    public AmqpMessageProperties setCreationTime(OffsetDateTime creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    /**
     * Gets {@code groupId} from the Amqp message.
     *
     * @return The {@code groupId}.
     */
    public String getGroupId() {
        return this.groupId;
    }

    /**
     * Sets the given {@code groupId} value on {@link AmqpMessageProperties} object.
     *
     * @param groupId to be set .
     * @return updated {@link AmqpMessageProperties} object.
     */
    public AmqpMessageProperties setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    /**
     * Gets {@code groupSequence} from the Amqp message.
     *
     * @return The {@code groupSequence}.
     */
    public String getGroupSequence() {
        return this.groupSequence;
    }

    /**
     * Sets the given {@code groupSequence} value on {@link AmqpMessageProperties} object.
     *
     * @param groupSequence to be set .
     * @return updated {@link AmqpMessageProperties} object.
     */
    public AmqpMessageProperties setGroupSequence(String groupSequence) {
        this.groupSequence = groupSequence;
        return this;
    }

    /**
     * Gets {@code messageId} from the Amqp message.
     *
     * @return The {@code messageId}.
     */
    public String getMessageId() {
        return this.messageId;
    }

    /**
     * Sets the given {@code messageId} value on {@link AmqpMessageProperties} object.
     *
     * @param messageId to be set .
     * @return updated {@link AmqpMessageProperties} object.
     */
    public AmqpMessageProperties setMessageId(String messageId) {
        this.messageId = messageId;
        return this;
    }

    /**
     * Gets {@code replyTo} from the Amqp message.
     *
     * @return The {@code replyTo}.
     */
    public String getReplyTo() {
        return this.replyTo;
    }

    /**
     * Sets the given {@code replyTo} value on {@link AmqpMessageProperties} object.
     *
     * @param replyTo to be set .
     * @return updated {@link AmqpMessageProperties} object.
     */
    public AmqpMessageProperties setReplyTo(String replyTo) {
        this.replyTo = replyTo;
        return this;
    }

    /**
     * Gets {@code replyToGroupId} from the Amqp message.
     *
     * @return The {@code replyToGroupId}.
     */
    public String getReplyToGroupId() {
        return this.replyToGroupId;
    }

    /**
     * Sets the given {@code replyToGroupId} value on {@link AmqpMessageProperties} object.
     *
     * @param replyToGroupId to be set .
     * @return updated {@link AmqpMessageProperties} object.
     */
    public AmqpMessageProperties setReplyToGroupId(String replyToGroupId) {
        this.replyToGroupId = replyToGroupId;
        return this;
    }

    /**
     * Gets {@code subject} from the Amqp message.
     *
     * @return The {@code subject}.
     */
    public String getSubject() {
        return this.subject;
    }

    /**
     * Sets the given {@code subject} value on {@link AmqpMessageProperties} object.
     *
     * @param subject to be set .
     * @return updated {@link AmqpMessageProperties} object.
     */
    public AmqpMessageProperties setSubject(String subject) {
        this.subject = subject;
        return this;
    }

    /**
     * Gets {@code to} from the Amqp message.
     *
     * @return The {@code to}.
     */
    public String getTo() {
        return this.to;
    }

    /**
     * Sets the given {@code to} value on {@link AmqpMessageProperties} object.
     *
     * @param to to be set .
     * @return updated {@link AmqpMessageProperties} object.
     */
    public AmqpMessageProperties setTo(String to) {
        this.to = to;
        return this;
    }

    /**
     * Gets {@code userId} from Amqp message.
     *
     * @return The {@code userId}.
     */
    public byte[] getUserId() {
        return this.userId;
    }

    /**
     * Sets the given {@code userId} value on {@link AmqpMessageProperties} object.
     *
     * @param userId to be set .
     * @return updated {@link AmqpMessageProperties} object.
     */
    public AmqpMessageProperties setUserId(byte[] userId) {
        this.userId = userId;
        return this;
    }


}
