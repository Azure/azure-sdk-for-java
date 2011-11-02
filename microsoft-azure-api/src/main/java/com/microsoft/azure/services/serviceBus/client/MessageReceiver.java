package com.microsoft.azure.services.serviceBus.client;

import com.microsoft.azure.ServiceException;
import com.microsoft.azure.services.serviceBus.Message;


public interface MessageReceiver {
	Message receiveMessage() throws ServiceException;
	Message receiveMessage(ReceiveMessageOptions options) throws ServiceException;
	Message peekLockMessage() throws ServiceException;
	Message peekLockMessage(ReceiveMessageOptions options) throws ServiceException;
	void abandonMessage(Message message) throws ServiceException;
	void completeMessage(Message message) throws ServiceException;
}
