package com.microsoft.windowsazure.services.queue;

import java.util.HashMap;

import com.microsoft.windowsazure.ServiceException;
import com.microsoft.windowsazure.http.ServiceFilter;

public interface QueueService {
    QueueService withFilter(ServiceFilter filter);

    ServiceProperties getServiceProperties() throws ServiceException;

    ServiceProperties getServiceProperties(ServiceProperties properties, QueueServiceOptions options) throws ServiceException;

    void setServiceProperties(ServiceProperties serviceProperties) throws ServiceException;

    void setServiceProperties(ServiceProperties serviceProperties, QueueServiceOptions options) throws ServiceException;

    void createQueue(String queue) throws ServiceException;

    void createQueue(String queue, CreateQueueOptions options) throws ServiceException;

    void deleteQueue(String queue) throws ServiceException;

    void deleteQueue(String queue, QueueServiceOptions options) throws ServiceException;

    ListQueuesResult listQueues() throws ServiceException;

    ListQueuesResult listQueues(ListQueuesOptions options) throws ServiceException;

    GetQueueMetadataResult getQueueMetadata(String queue) throws ServiceException;

    GetQueueMetadataResult getQueueMetadata(String queue, QueueServiceOptions options) throws ServiceException;

    void setQueueMetadata(String queue, HashMap<String, String> metadata) throws ServiceException;

    void setQueueMetadata(String queue, HashMap<String, String> metadata, QueueServiceOptions options) throws ServiceException;

    void createMessage(String queue, String message) throws ServiceException;

    void createMessage(String queue, String message, CreateMessageOptions options) throws ServiceException;

    UpdateMessageResult updateMessage(String queue, String message, String popReceipt, String text, int visibilityTimeoutInSeconds) throws ServiceException;

    UpdateMessageResult updateMessage(String queue, String message, String popReceipt, String text, int visibilityTimeoutInSeconds, QueueServiceOptions options)
            throws ServiceException;

    ListMessagesResult listMessages(String queue) throws ServiceException;

    ListMessagesResult listMessages(String queue, ListMessagesOptions options) throws ServiceException;

    PeekMessagesResult peekMessages(String queue) throws ServiceException;

    PeekMessagesResult peekMessages(String queue, PeekMessagesOptions options) throws ServiceException;

    void deleteMessage(String queue, String message, String popReceipt) throws ServiceException;

    void deleteMessage(String queue, String message, String popReceipt, QueueServiceOptions options) throws ServiceException;

    void clearMessages(String queue) throws ServiceException;

    void clearMessages(String queue, QueueServiceOptions options) throws ServiceException;

}
