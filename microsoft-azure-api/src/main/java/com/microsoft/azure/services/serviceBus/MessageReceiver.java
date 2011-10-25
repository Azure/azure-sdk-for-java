package com.microsoft.azure.services.serviceBus;


public interface MessageReceiver {
	Message receiveMessage();
	Message receiveMessage(ReceiveMessageOptions options);
	Message peekLockMessage();
	Message peekLockMessage(ReceiveMessageOptions options);
	void abandonMessage(Message message);
	void completeMessage(Message message);
}
