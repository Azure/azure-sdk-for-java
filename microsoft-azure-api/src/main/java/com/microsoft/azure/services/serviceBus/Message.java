package com.microsoft.azure.services.serviceBus;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.microsoft.azure.services.serviceBus.implementation.BrokerProperties;

public class Message {
	BrokerProperties properties;
	InputStream body;

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
	
	/**
	 * @return the brokerProperties
	 */
	public BrokerProperties getProperties() {
		return properties;
	}
	
	/**
	 * @param properties the brokerProperties to set
	 */
	public void setProperties(BrokerProperties properties) {
		this.properties = properties;
	}
	/**
	 * @return the body
	 */
	public InputStream getBody() {
		return body;
	}
	/**
	 * @param body the body to set
	 */
	public void setBody(InputStream body) {
		this.body = body;
	}
}
