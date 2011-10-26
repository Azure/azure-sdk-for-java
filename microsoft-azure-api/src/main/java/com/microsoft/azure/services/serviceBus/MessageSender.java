package com.microsoft.azure.services.serviceBus;

import com.microsoft.azure.ServiceException;


public interface MessageSender {
	void sendMessage(Message message) throws ServiceException;
	void sendMessage(Message message, SendMessageOptions options) throws ServiceException;
}
