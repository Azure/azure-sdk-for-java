package com.microsoft.azure.servicebus;

public interface IQueueClient extends IMessageSender, IMessageSessionEntity, IMessageAndSessionPump, IMessageEntity
{
	public ReceiveMode getReceiveMode();
}
