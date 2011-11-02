package com.microsoft.azure.services.serviceBus.messaging;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.microsoft.azure.services.serviceBus.schema.BrokerProperties;


public class Message {
	BrokerProperties properties;
	InputStream body;

	public Message() {
		this(new BrokerProperties(), null);
	}

	public Message(InputStream body) {
		this(new BrokerProperties(), body);
	}

	public Message(String body) {
		this(new BrokerProperties(), (body == null) ? null : new ByteArrayInputStream(body.getBytes()));
	}

	Message(BrokerProperties properties, InputStream body) {
		this.properties = properties;
		this.body = body;
	}

	BrokerProperties getProperties() {
		return properties;
	}

	public InputStream getBody() {
		return body;
	}

	public Message setBody(InputStream body) {
		this.body = body;
		return this;
	}

	public String getLockToken() {
		return properties.getLockToken();
	}

	public void setLockToken(String lockToken) {
		properties.setLockToken(lockToken);
	}

	public String getLockedUntilUtc() {
		return properties.getLockedUntilUtc();
	}

	public void setLockedUntilUtc(String lockedUntilUtc) {
		properties.setLockedUntilUtc(lockedUntilUtc);
	}

	public Integer getDeliveryCount() {
		return properties.getDeliveryCount();
	}

	public String getMessageId() {
		return properties.getMessageId();
	}

	public Long getSequenceNumber() {
		return properties.getSequenceNumber();
	}

	public Long getTimeToLive() {
		return properties.getTimeToLive();
	}

	public Message setTimeToLive(Long timeToLive) {
		properties.setTimeToLive(timeToLive);
		return this;
	}	
}
