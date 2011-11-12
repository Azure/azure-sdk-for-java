package com.microsoft.windowsazure.services.serviceBus.client;

import com.microsoft.windowsazure.ServiceException;
import com.microsoft.windowsazure.services.serviceBus.Message;

public interface MessageSender {
    void sendMessage(Message message) throws ServiceException;
}
