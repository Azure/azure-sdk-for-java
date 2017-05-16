package com.microsoft.azure.servicebus;

public interface IQueueClient extends IMessageSender, IMessageAndSessionPump, IMessageEntity
{
	public ReceiveMode getReceiveMode();
	
	public String getQueueName();
}
