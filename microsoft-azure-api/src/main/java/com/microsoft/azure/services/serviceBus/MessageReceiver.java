package com.microsoft.azure.services.serviceBus;

public interface MessageReceiver {
	BrokeredMessage receive(int timeout, RECEIVE_MODE receiveMode);
	void abandon(BrokeredMessage message);
	void complete(BrokeredMessage message);
}
