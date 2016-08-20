/**
 * 
 */
package com.microsoft.azure.servicebus.messaging;

import java.io.Serializable;
import java.time.Duration;
import java.util.UUID;

/**
 * 
 * 
 */
public final class BrokeredMessage implements Serializable {
	private static final long serialVersionUID = 7849508139219590863L;

	private int deliveryCount;
	
	private String messageId;
	
	private Duration timeToLive;
	
	public BrokeredMessage()
	{
		this.messageId = UUID.randomUUID().toString();
	}

	public int getDeliveryCount() {
		return deliveryCount;
	}

	void setDeliveryCount(int deliveryCount) {
		this.deliveryCount = deliveryCount;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public Duration getTimeToLive() {
		return timeToLive;
	}

	public void setTimeToLive(Duration timeToLive) {
		this.timeToLive = timeToLive;
	}	
}
