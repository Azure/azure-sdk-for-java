package com.microsoft.azure.services.serviceBus;


public interface MessageReceiver {
	Message receiveMessage(int timeout);
	Message peekLockMessage(int timeout);
	void abandonMessage(Message message);
	void completeMessage(Message message);
}
