package com.microsoft.windowsazure.services.serviceBus.models;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;

import com.microsoft.windowsazure.services.serviceBus.implementation.BrokerProperties;

/**
 * Represents a service bus message.
 */
public class Message {
    BrokerProperties properties;
    InputStream body;
    String contentType;
    Date date;

    /**
     * Creates an instance of the <code>Message</code> class.
     */
    public Message() {
        this.properties = new BrokerProperties();
    }

    /**
     * Creates an instance of the <code>Message</code> class using the specified <code>InputStream</code>.
     * 
     * @param body
     *            An <code>InputStream</code> object that represents the body of the message.
     */
    public Message(InputStream body) {
        this.properties = new BrokerProperties();
        this.body = body;
    }

    /**
     * Creates an instance of the <code>Message</code> class using the specified byte array.
     * 
     * @param body
     *            A byte array that represents the body of the message.
     */
    public Message(byte[] body) {
        this.properties = new BrokerProperties();
        this.body = (body == null) ? null : new ByteArrayInputStream(body);
    }

    /**
     * Creates an instance of the <code>Message</code> class using the specified
     * string.
     * 
     * @param body
     *            A <code>String</code> object that represents the body of the message.
     */
    public Message(String body) {
        this.properties = new BrokerProperties();
        this.body = (body == null) ? null : new ByteArrayInputStream(body.getBytes());
    }

    Message(BrokerProperties properties, InputStream body) {
        this.properties = properties;
        this.body = body;
    }

    /**
     * Returns the properties of the message.
     * 
     * @return A {@link BrokerProperties} object that represents the properties of the message.
     */
    public BrokerProperties getProperties() {
        return properties;
    }

    /**
     * Sets the properties of the message.
     * 
     * @param properties
     *            A {@link BrokerProperties} object that represents the properties of the message.
     */
    public Message setProperties(BrokerProperties properties) {
        this.properties = properties;
        return this;
    }

    /**
     * Returns the body of the message.
     * 
     * @return An <code>InputStream</code> object that represents the body of the message.
     */
    public InputStream getBody() {
        return body;
    }

    /**
     * Specifies the body of the message.
     * 
     * @param body
     *            An <code>InputStream</code> object that represents the body of the message.
     * 
     * @return A <code>Message</code> object that represents the updated message.
     */
    public Message setBody(InputStream body) {
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
     * @return A <code>Message</code> object that represents the updated message.
     */
    public Message setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Returns the date/time of the message.
     * 
     * @return A <code>Date</code> object that represents the date/time of the object.
     */
    public Date getDate() {
        return date;
    }

    /**
     * Sets the date/time of the message.
     * 
     * @param date
     *            A <code>Date</code> object that represents the date/time of the object.
     * 
     * @return A <code>Message</code> object that represents the updated message.
     */
    public Message setDate(Date date) {
        this.date = date;
        return this;
    }

    /**
     * Returns the delivery count of the message.
     * 
     * @return The delivery count.
     */
    public Integer getDeliveryCount() {
        return properties.getDeliveryCount();
    }

    /**
     * Returns the message ID.
     * 
     * @return A <code>String</code> object that represents the message ID.
     */
    public String getMessageId() {
        return properties.getMessageId();
    }

    /**
     * Sets the message ID.
     * 
     * @param messageId
     *            A <code>String</code> object that represents the message ID.
     * 
     * @return A <code>Message</code> object that represents the updated message.
     */
    public Message setMessageId(String messageId) {
        properties.setMessageId(messageId);
        return this;
    }

    /**
     * Returns the message sequence number.
     * 
     * @return The message sequence number.
     * 
     */
    public Long getSequenceNumber() {
        return properties.getSequenceNumber();
    }

    /**
     * Returns the time-to-live for the message.
     * 
     * @return The time, in seconds, for the message to live.
     */
    public Double getTimeToLive() {
        return properties.getTimeToLive();
    }

    /**
     * Sets the time-to-live for the message.
     * 
     * @param timeToLive
     *            The time, in seconds, for the message to live.
     * 
     * @return A <code>Message</code> object that represents the updated message.
     */
    public Message setTimeToLive(Double timeToLive) {
        properties.setTimeToLive(timeToLive);
        return this;
    }

    /**
     * Returns the lock token for the message.
     * 
     * @return A <code>String</code> object that represents the lock token.
     */
    public String getLockToken() {
        return properties.getLockToken();
    }

    /**
     * Returns the locked-until date/time.
     * 
     * @return A <code>Date</code> object that represents the locked-until date/time, in UTC format.
     */
    public Date getLockedUntilUtc() {
        return properties.getLockedUntilUtc();
    }

    /**
     * Returns the correlation ID.
     * 
     * @return A <code>String</code> object that represents the correlation ID.
     * 
     */
    public String getCorrelationId() {
        return properties.getCorrelationId();
    }

    /**
     * Sets the correlation ID.
     * 
     * @param correlationId
     *            A <code>String</code> object that represents the correlation ID.
     * 
     * @return A <code>Message</code> object that represents the updated message.
     */
    public Message setCorrelationId(String correlationId) {
        properties.setCorrelationId(correlationId);
        return this;
    }

    /**
     * Returns the session ID for the message.
     * 
     * @return A <code>String</code> object that represents the session ID.
     * 
     */
    public String getSessionId() {
        return properties.getSessionId();
    }

    /**
     * Sets the session ID for the message.
     * 
     * @param sessionId
     *            A <code>String</code> object that represents the session ID.
     * 
     * @return A <code>Message</code> object that represents the updated message.
     */
    public Message setSessionId(String sessionId) {
        properties.setSessionId(sessionId);
        return this;
    }

    /**
     * Returns the label of the message.
     * 
     * @return A <code>String</code> object that represents the label.
     */
    public String getLabel() {
        return properties.getLabel();
    }

    /**
     * Sets the label of the message.
     * 
     * @param label
     *            A <code>String</code> object that represents the label.
     * 
     * @return A <code>Message</code> object that represents the updated message.
     */
    public Message setLabel(String label) {
        properties.setLabel(label);
        return this;
    }

    /**
     * Returns the Reply-To recipient of the message.
     * 
     * @return A <code>String</code> object that represents the Reply-To recipient.
     */
    public String getReplyTo() {
        return properties.getReplyTo();
    }

    /**
     * Sets the Reply-To recipient for the message.
     * 
     * @param replyTo
     *            A <code>String</code> object that represents the Reply-To recipient.
     * 
     * @return A <code>Message</code> object that represents the updated message.
     */
    public Message setReplyTo(String replyTo) {
        properties.setReplyTo(replyTo);
        return this;
    }

    /**
     * Returns the To recipient of the message.
     * 
     * @return A <code>String</code> object that represents the To recipient.
     */
    public String getTo() {
        return properties.getTo();
    }

    /**
     * Sets the To recipient of the message.
     * 
     * @param A
     *            <code>String</code> object that represents the To recipient.
     * 
     * @return A <code>Message</code> object that represents the updated message.
     */
    public Message setTo(String to) {
        properties.setTo(to);
        return this;
    }

    /**
     * Returns the scheduled enqueue date/time.
     * 
     * @return A <code>Date</code> object that represents the date/time of the message in UTC format.
     */
    public Date getScheduledEnqueueTimeUtc() {
        return properties.getScheduledEnqueueTimeUtc();
    }

    /**
     * Sets the scheduled enqueue time.
     * 
     * @param scheduledEnqueueTimeUtc
     *            A <code>Date</code> object that represents the date/time of the message in UTC format.
     * 
     * @return A <code>Message</code> object that represents the updated message.
     */
    public Message setScheduledEnqueueTimeUtc(Date scheduledEnqueueTimeUtc) {
        properties.setScheduledEnqueueTimeUtc(scheduledEnqueueTimeUtc);
        return this;
    }

    /**
     * Returns the session ID of the Reply To recipient.
     * 
     * @return A <code>String</code> object that represents the session ID of the Reply To recipient.
     */
    public String getReplyToSessionId() {
        return properties.getReplyToSessionId();
    }

    /**
     * Sets the session ID of the Reply To recipient.
     * 
     * @param replyToSessionId
     *            A <code>String</code> object that represents the session ID of the Reply To recipient.
     * 
     * @return A <code>Message</code> object that represents the updated message.
     */
    public Message setReplyToSessionId(String replyToSessionId) {
        properties.setReplyToSessionId(replyToSessionId);
        return this;
    }

    /**
     * Returns the message location.
     * 
     * @return A <code>String</code> object that represents the message location.
     */
    public String getMessageLocation() {
        return properties.getMessageLocation();
    }

    /**
     * Returns the lock location.
     * 
     * @return A <code>String</code> object that represents the lock location.
     */
    public String getLockLocation() {
        return properties.getLockLocation();
    }
}
