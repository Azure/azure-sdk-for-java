// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

import java.time.OffsetDateTime;

/**
 * Represents properties from Amqp message.
 */
public class AmqpMessageProperties {

    private OffsetDateTime absoluteExpiryTime;

    /**
     *
     * @return
     */
    public OffsetDateTime getAbsoluteExpiryTime() {
        return this.absoluteExpiryTime;
    }

    public AmqpMessageProperties setAbsoluteExpiryTime(OffsetDateTime absoluteExpiryTime) {
        this.absoluteExpiryTime =  absoluteExpiryTime;
        return this;
    }

    public String getContentEncoding() {

    }

    public AmqpMessageProperties setContentEncoding(String contentEncoding) {

    }

    public String getContentType() {

    }

    public AmqpMessageProperties setContentType(String contentType) {

    }

    public String getCorrelationId() {

    }

    public AmqpMessageProperties setCorrelationId(String correlationId) {

    }

    public OffsetDateTime getCreationTime() {

    }

    public AmqpMessageProperties setCreationTime(OffsetDateTime creationTime) {

    }

    public String getGroupId() {

    }

    public AmqpMessageProperties setGroupId(String groupId) {

    }

    public String getGroupSequence() {

    }

    public AmqpMessageProperties setGroupSequence(String groupSequence) {

    }

    public String getMessageId() {

    }

    public AmqpMessageProperties setMessageId(String messageId) {

    }

    public String getReplyTo() {

    }

    public AmqpMessageProperties setReplyTo(String replyTo) {

    }

    public String getReplyToGroupId() {

    }

    public AmqpMessageProperties setReplyToGroupId(String replyToGroupId) {

    }

    public String getSubject() {

    }

    public AmqpMessageProperties setSubject(String subject) {

    }

    public String getTo() {

    }

    public AmqpMessageProperties setTo(String to) {

    }

    public byte[] getUserId() {

    }

    public AmqpMessageProperties setUserId(byte[] userId) {

    }


}
