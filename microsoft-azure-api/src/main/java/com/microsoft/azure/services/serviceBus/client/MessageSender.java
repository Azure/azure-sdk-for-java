package com.microsoft.azure.services.serviceBus.client;

import com.microsoft.azure.ServiceException;
import com.microsoft.azure.services.serviceBus.Message;

public interface MessageSender {
    void sendMessage(Message message) throws ServiceException;
}
