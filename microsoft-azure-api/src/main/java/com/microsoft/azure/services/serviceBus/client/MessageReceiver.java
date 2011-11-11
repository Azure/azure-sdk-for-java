package com.microsoft.azure.services.serviceBus.client;

import com.microsoft.azure.ServiceException;
import com.microsoft.azure.services.serviceBus.Message;
import com.microsoft.azure.services.serviceBus.ReceiveMessageOptions;

public interface MessageReceiver {
    Message receiveMessage() throws ServiceException;

    Message receiveMessage(ReceiveMessageOptions options) throws ServiceException;

    void unlockMessage(Message message) throws ServiceException;

    void deleteMessage(Message message) throws ServiceException;
}
