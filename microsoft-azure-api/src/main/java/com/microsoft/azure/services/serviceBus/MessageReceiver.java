package com.microsoft.azure.services.serviceBus;


public interface MessageReceiver {
	Message receive(int timeout);
	Message peekLock(int timeout);
	void abandon(Message message);
	void complete(Message message);
}
