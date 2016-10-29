/**
 * 
 */
package com.microsoft.azure.servicebus.messaging;

import java.io.Serializable;
import java.time.Duration;
import java.util.UUID;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.message.Message;

/**
 * 
 * 
 */
final class BrokeredMessage implements Serializable, IBrokeredMessage {
	private static final long serialVersionUID = 7849508139219590863L;

	private int deliveryCount;
	
	private String messageId;
	
	private Duration timeToLive;
	
	private String content;
	
	public BrokeredMessage()
	{
		this.messageId = UUID.randomUUID().toString();
	}
	
	public BrokeredMessage(String content)
	{
		this();
		this.content = content;
	}

	@Override
	public int getDeliveryCount() {
		return deliveryCount;
	}

	void setDeliveryCount(int deliveryCount) {
		this.deliveryCount = deliveryCount;
	}

	@Override
	public String getMessageId() {
		return messageId;
	}

	@Override
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	@Override
	public Duration getTimeToLive() {
		return timeToLive;
	}

	@Override
	public void setTimeToLive(Duration timeToLive) {
		this.timeToLive = timeToLive;
	}
	
	Message toAmqpMessage()
	{
		Message amqpMessage = Proton.message();
		amqpMessage.setBody(new Data(new Binary(this.content.getBytes())));
		return amqpMessage;
	}
}
