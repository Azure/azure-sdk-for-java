package com.microsoft.azure.servicebus.primitives;

import org.apache.qpid.proton.message.Message;

public class MessageWithDeliveryTag {
	private final Message message;
	private final byte[] deliveryTag;	

	public MessageWithDeliveryTag(Message message, byte[] deliveryTag)
	{
		this.message = message;
		this.deliveryTag = deliveryTag;				
	}
	
	public Message getMessage() {
		return message;
	}

	public byte[] getDeliveryTag() {
		return deliveryTag;
	}	
}
