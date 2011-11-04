package com.microsoft.azure.services.serviceBus;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;

import com.microsoft.azure.services.serviceBus.implementation.BrokerProperties;

public class Message {
	BrokerProperties properties;
	InputStream body;
	String contentType;
	Date date;

	public Message() {
		this.properties = new BrokerProperties();
	}
	
	public Message(InputStream body) {
		this.properties = new BrokerProperties();
		this.body = body;
	}	

	public Message(byte[] body) {
		this.properties = new BrokerProperties();
		this.body = (body == null) ? null : new ByteArrayInputStream(body);
	}

	public Message(String body) {
		this.properties = new BrokerProperties();
		this.body = (body == null) ? null : new ByteArrayInputStream(body.getBytes());
	}

	Message(BrokerProperties properties, InputStream body) {
		this.properties = properties;
		this.body = body;
	}
	
	public BrokerProperties getProperties() {
		return properties;
	}
	
	public Message setProperties(BrokerProperties properties) {
		this.properties = properties;
		return this;
	}

	public InputStream getBody() {
		return body;
	}

	public Message setBody(InputStream body) {
		this.body = body;
		return this;
	}

	public String getContentType() {
		return contentType;
	}

	public Message setContentType(String contentType) {
		this.contentType = contentType;
		return this;
	}

	public Date getDate() {
		return date;
	}

	public Message setDate(Date date) {
		this.date = date;
		return this;
	}

	public Integer getDeliveryCount() {
		return properties.getDeliveryCount();
	}

	public String getMessageId() {
		return properties.getMessageId();
	}

	public Message setMessageId(String messageId) {
		properties.setMessageId(messageId);
		return this;
	}

	public Long getSequenceNumber() {
		return properties.getSequenceNumber();
	}

	public Double getTimeToLive() {
		return properties.getTimeToLive();
	}

	public Message setTimeToLive(Double timeToLive) {
		properties.setTimeToLive(timeToLive);
		return this;
	}

	public String getLockToken() {
		return properties.getLockToken();
	}

	public Date getLockedUntilUtc() {
		return properties.getLockedUntilUtc();
	}

	public String getCorrelationId() {
		return properties.getCorrelationId();
	}

	public Message setCorrelationId(String correlationId) {
		properties.setCorrelationId(correlationId);
		return this;
	}

	public String getSessionId() {
		return properties.getSessionId();
	}

	public Message setSessionId(String sessionId) {
		properties.setSessionId(sessionId);
		return this;
	}

	public String getLabel() {
		return properties.getLabel();
	}

	public Message setLabel(String label) {
		properties.setLabel(label);
		return this;
	}

	public String getReplyTo() {
		return properties.getReplyTo();
	}

	public Message setReplyTo(String replyTo) {
		properties.setReplyTo(replyTo);
		return this;
	}

	public String getTo() {
		return properties.getTo();
	}

	public Message setTo(String to) {
		properties.setTo(to);
		return this;
	}

	public Date getScheduledEnqueueTimeUtc() {
		return properties.getScheduledEnqueueTimeUtc();
	}

	public Message setScheduledEnqueueTimeUtc(Date scheduledEnqueueTimeUtc) {
		properties.setScheduledEnqueueTimeUtc(scheduledEnqueueTimeUtc);
		return this;
	}

	public String getReplyToSessionId() {
		return properties.getReplyToSessionId();
	}

	public Message setReplyToSessionId(String replyToSessionId) {
		properties.setReplyToSessionId(replyToSessionId);
		return this;
	}

	public String getMessageLocation() {
		return properties.getMessageLocation();
	}

	public String getLockLocation() {
		return properties.getLockLocation();
	}
}
