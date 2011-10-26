package com.microsoft.azure.services.serviceBus;

import com.microsoft.azure.ServiceException;


public interface MessageReceiver {
	Message receiveMessage() throws ServiceException;
	Message receiveMessage(ReceiveMessageOptions options) throws ServiceException;
	Message peekLockMessage() throws ServiceException;
	Message peekLockMessage(ReceiveMessageOptions options) throws ServiceException;
	void abandonMessage(Message message) throws ServiceException;
	void completeMessage(Message message) throws ServiceException;
}
