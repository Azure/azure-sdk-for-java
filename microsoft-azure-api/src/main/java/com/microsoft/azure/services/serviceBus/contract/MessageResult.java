package com.microsoft.azure.services.serviceBus.contract;

import java.io.InputStream;

public class MessageResult {
	BrokerProperties brokerProperties;
	
	InputStream body;
	
	/**
	 * @return the brokerProperties
	 */
	public BrokerProperties getBrokerProperties() {
		return brokerProperties;
	}
	/**
	 * @param brokerProperties the brokerProperties to set
	 */
	public void setBrokerProperties(BrokerProperties brokerProperties) {
		this.brokerProperties = brokerProperties;
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
