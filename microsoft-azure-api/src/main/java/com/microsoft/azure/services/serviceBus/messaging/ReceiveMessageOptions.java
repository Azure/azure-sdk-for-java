package com.microsoft.azure.services.serviceBus.messaging;

public class ReceiveMessageOptions {
	Integer timeout;

	public static final ReceiveMessageOptions DEFAULT = new ReceiveMessageOptions();

	public Integer getTimeout() {
		return timeout;
	}

	public ReceiveMessageOptions setTimeout(Integer timeout) {
		this.timeout = timeout;
		return this;
	}
}

