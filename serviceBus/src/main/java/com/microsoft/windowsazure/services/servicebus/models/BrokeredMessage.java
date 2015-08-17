/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.services.servicebus.models;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.microsoft.windowsazure.services.servicebus.implementation.BrokerProperties;

/**
 * Represents a service bus message.
 */
public class BrokeredMessage {
    private BrokerProperties brokerProperties;
    private InputStream body;
    private String contentType;
    private Date date;
    private Map<String, Object> customProperties;

    /**
     * Creates an instance of the <code>Message</code> class.
     */
    public BrokeredMessage() {
        this(new BrokerProperties());
    }

    /**
     * Creates an instance of the <code>Message</code> class using the specified
     * <code>InputStream</code>.
     * 
     * @param body
     *            An <code>InputStream</code> object that represents the body of
     *            the message.
     */
    public BrokeredMessage(InputStream body) {
        this(new BrokerProperties());
        this.body = body;
    }

    /**
     * Creates an instance of the <code>Message</code> class using the specified
     * byte array.
     * 
     * @param body
     *            A byte array that represents the body of the message.
     */
    public BrokeredMessage(byte[] body) {
        this(new BrokerProperties());
        this.body = (body == null) ? null : new ByteArrayInputStream(body);
    }

    /**
     * Creates an instance of the <code>Message</code> class using the specified
     * string.
     * 
     * @param body
     *            A <code>String</code> object that represents the body of the
     *            message.
     */
    public BrokeredMessage(String body) {
        this(new BrokerProperties());
        try {
            this.body = (body == null) ? null : new ByteArrayInputStream(
                    body.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Internal
     * 
     * @param properties
     */
    public BrokeredMessage(BrokerProperties properties) {
        this.brokerProperties = properties;
        this.customProperties = new HashMap<String, Object>();
    }

    /**
     * Returns the properties of the message.
     * 
     * @return A {@link BrokerProperties} object that represents the properties
     *         of the message.
     */
    public BrokerProperties getBrokerProperties() {
        return brokerProperties;
    }

    /**
     * Returns the body of the message.
     * 
     * @return An <code>InputStream</code> object that represents the body of
     *         the message.
     */
    public InputStream getBody() {
        return body;
    }

    /**
     * Specifies the body of the message.
     * 
     * @param body
     *            An <code>InputStream</code> object that represents the body of
     *            the message.
     * 
     * @return A <code>Message</code> object that represents the updated
     *         message.
     */
    public BrokeredMessage setBody(InputStream body) {
        this.body = body;
        return this;
    }

    /**
     * Returns the content type of the message.
     * 
     * @return A <code>String</code> object that represents the content type.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets the content type of the message.
     * 
     * @param contentType
     *            A <code>String</code> object that represents the content type.
     * 
     * @return A <code>Message</code> object that represents the updated
     *         message.
     */
    public BrokeredMessage setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Returns the date/time of the message.
     * 
     * @return A <code>Date</code> object that represents the date/time of the
     *         object.
     */
    public Date getDate() {
        return date;
    }

    /**
     * Sets the date/time of the message.
     * 
     * @param date
     *            A <code>Date</code> object that represents the date/time of
     *            the object.
     * 
     * @return A <code>Message</code> object that represents the updated
     *         message.
     */
    public BrokeredMessage setDate(Date date) {
        this.date = date;
        return this;
    }

    /**
     * Returns a user defined property of the message.
     * 
     * @param name
     *            A <code>String</code> object that represents the name of the
     *            property.
     * @return An <code>Object</code> object that represents the value of the
     *         property.
     */
    public Object getProperty(String name) {
        return customProperties.get(name);
    }

    /**
     * Sets a user defined property of the message.
     * 
     * @param name
     *            A <code>String</code> object that represents the name of the
     *            property.
     * @param value
     *            An <code>Object</code> object that represents the value of the
     *            property.
     * @return A <code>Message</code> object that represents the updated
     *         message.
     */
    public BrokeredMessage setProperty(String name, Object value) {
        customProperties.put(name, value);
        return this;
    }

    /**
     * Returns the user defined properties of the message.
     * 
     * @return A <code>Map</code> object that represents the user defined
     *         properties.
     */
    public Map<String, Object> getProperties() {
        return customProperties;
    }

    /**
     * Returns the delivery count of the message.
     * 
     * @return The delivery count.
     */
    public Integer getDeliveryCount() {
        return brokerProperties.getDeliveryCount();
    }

    /**
     * Returns the message ID.
     * 
     * @return A <code>String</code> object that represents the message ID.
     */
    public String getMessageId() {
        return brokerProperties.getMessageId();
    }

    /**
     * Sets the message ID.
     * 
     * @param messageId
     *            A <code>String</code> object that represents the message ID.
     * 
     * @return A <code>Message</code> object that represents the updated
     *         message.
     */
    public BrokeredMessage setMessageId(String messageId) {
        brokerProperties.setMessageId(messageId);
        return this;
    }

    /**
     * Returns the message sequence number.
     * 
     * @return The message sequence number.
     * 
     */
    public Long getSequenceNumber() {
        return brokerProperties.getSequenceNumber();
    }

    /**
     * Returns the time-to-live for the message.
     * 
     * @return The time, in seconds, for the message to live.
     */
    public Double getTimeToLive() {
        return brokerProperties.getTimeToLive();
    }

    /**
     * Sets the time-to-live for the message.
     * 
     * @param timeToLive
     *            The time, in seconds, for the message to live.
     * 
     * @return A <code>Message</code> object that represents the updated
     *         message.
     */
    public BrokeredMessage setTimeToLive(Double timeToLive) {
        brokerProperties.setTimeToLive(timeToLive);
        return this;
    }

    /**
     * Returns the lock token for the message.
     * 
     * @return A <code>String</code> object that represents the lock token.
     */
    public String getLockToken() {
        return brokerProperties.getLockToken();
    }

    /**
     * Returns the locked-until date/time.
     * 
     * @return A <code>Date</code> object that represents the locked-until
     *         date/time, in UTC format.
     */
    public Date getLockedUntilUtc() {
        return brokerProperties.getLockedUntilUtc();
    }

    /**
     * Returns the correlation ID.
     * 
     * @return A <code>String</code> object that represents the correlation ID.
     * 
     */
    public String getCorrelationId() {
        return brokerProperties.getCorrelationId();
    }

    /**
     * Sets the correlation ID.
     * 
     * @param correlationId
     *            A <code>String</code> object that represents the correlation
     *            ID.
     * 
     * @return A <code>Message</code> object that represents the updated
     *         message.
     */
    public BrokeredMessage setCorrelationId(String correlationId) {
        brokerProperties.setCorrelationId(correlationId);
        return this;
    }

    /**
     * Returns the session ID for the message.
     * 
     * @return A <code>String</code> object that represents the session ID.
     * 
     */
    public String getSessionId() {
        return brokerProperties.getSessionId();
    }

    /**
     * Sets the session ID for the message.
     * 
     * @param sessionId
     *            A <code>String</code> object that represents the session ID.
     * 
     * @return A <code>Message</code> object that represents the updated
     *         message.
     */
    public BrokeredMessage setSessionId(String sessionId) {
        brokerProperties.setSessionId(sessionId);
        return this;
    }

    /**
     * Returns the label of the message.
     * 
     * @return A <code>String</code> object that represents the label.
     */
    public String getLabel() {
        return brokerProperties.getLabel();
    }

    /**
     * Sets the label of the message.
     * 
     * @param label
     *            A <code>String</code> object that represents the label.
     * 
     * @return A <code>Message</code> object that represents the updated
     *         message.
     */
    public BrokeredMessage setLabel(String label) {
        brokerProperties.setLabel(label);
        return this;
    }

    /**
     * Returns the Reply-To recipient of the message.
     * 
     * @return A <code>String</code> object that represents the Reply-To
     *         recipient.
     */
    public String getReplyTo() {
        return brokerProperties.getReplyTo();
    }

    /**
     * Sets the Reply-To recipient for the message.
     * 
     * @param replyTo
     *            A <code>String</code> object that represents the Reply-To
     *            recipient.
     * 
     * @return A <code>Message</code> object that represents the updated
     *         message.
     */
    public BrokeredMessage setReplyTo(String replyTo) {
        brokerProperties.setReplyTo(replyTo);
        return this;
    }

    /**
     * Returns the To recipient of the message.
     * 
     * @return A <code>String</code> object that represents the To recipient.
     */
    public String getTo() {
        return brokerProperties.getTo();
    }

    /**
     * Sets the To recipient of the message.
     * 
     * @param to
     *            A <code>String</code> object that represents the To recipient.
     * 
     * @return A <code>Message</code> object that represents the updated
     *         message.
     */
    public BrokeredMessage setTo(String to) {
        brokerProperties.setTo(to);
        return this;
    }

    /**
     * Returns the scheduled enqueue date/time.
     * 
     * @return A <code>Date</code> object that represents the date/time of the
     *         message in UTC format.
     */
    public Date getScheduledEnqueueTimeUtc() {
        return brokerProperties.getScheduledEnqueueTimeUtc();
    }

    /**
     * Sets the scheduled enqueue time.
     * 
     * @param scheduledEnqueueTimeUtc
     *            A <code>Date</code> object that represents the date/time of
     *            the message in UTC format.
     * 
     * @return A <code>Message</code> object that represents the updated
     *         message.
     */
    public BrokeredMessage setScheduledEnqueueTimeUtc(
            Date scheduledEnqueueTimeUtc) {
        brokerProperties.setScheduledEnqueueTimeUtc(scheduledEnqueueTimeUtc);
        return this;
    }

    /**
     * Returns the session ID of the Reply To recipient.
     * 
     * @return A <code>String</code> object that represents the session ID of
     *         the Reply To recipient.
     */
    public String getReplyToSessionId() {
        return brokerProperties.getReplyToSessionId();
    }

    /**
     * Sets the session ID of the Reply To recipient.
     * 
     * @param replyToSessionId
     *            A <code>String</code> object that represents the session ID of
     *            the Reply To recipient.
     * 
     * @return A <code>Message</code> object that represents the updated
     *         message.
     */
    public BrokeredMessage setReplyToSessionId(String replyToSessionId) {
        brokerProperties.setReplyToSessionId(replyToSessionId);
        return this;
    }

    /**
     * Returns the message location.
     * 
     * @return A <code>String</code> object that represents the message
     *         location.
     */
    public String getMessageLocation() {
        return brokerProperties.getMessageLocation();
    }

    /**
     * Returns the lock location.
     * 
     * @return A <code>String</code> object that represents the lock location.
     */
    public String getLockLocation() {
        return brokerProperties.getLockLocation();
    }
}
