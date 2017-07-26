package com.microsoft.azure.servicebus;

public interface IQueueClient extends IMessageSender, IMessageAndSessionPump, IMessageEntityClient
{
	public ReceiveMode getReceiveMode();
	
	public String getQueueName();
}
