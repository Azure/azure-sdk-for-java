package com.microsoft.azure.servicebus.messaging;

import java.time.Duration;

public interface IBrokeredMessage {
	public int getDeliveryCount();
	
	public String getMessageId();

	public void setMessageId(String messageId);

	public Duration getTimeToLive();

	public void setTimeToLive(Duration timeToLive);
}
