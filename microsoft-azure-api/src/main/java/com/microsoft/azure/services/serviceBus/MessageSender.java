package com.microsoft.azure.services.serviceBus;

public interface MessageSender {
	void send(BrokeredMessage message);
}
