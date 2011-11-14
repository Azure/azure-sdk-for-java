package com.microsoft.windowsazure.services.serviceBus.client;

import com.microsoft.windowsazure.common.ServiceException;
import com.microsoft.windowsazure.services.serviceBus.Message;
import com.microsoft.windowsazure.services.serviceBus.ReceiveMessageOptions;

public interface MessageReceiver {
    Message receiveMessage() throws ServiceException;

    Message receiveMessage(ReceiveMessageOptions options) throws ServiceException;

    void unlockMessage(Message message) throws ServiceException;

    void deleteMessage(Message message) throws ServiceException;
}
