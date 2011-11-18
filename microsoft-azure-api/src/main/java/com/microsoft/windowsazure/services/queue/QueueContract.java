package com.microsoft.windowsazure.services.queue;

import java.util.HashMap;

import com.microsoft.windowsazure.common.FilterableService;
import com.microsoft.windowsazure.common.ServiceException;
import com.microsoft.windowsazure.services.queue.models.CreateMessageOptions;
import com.microsoft.windowsazure.services.queue.models.CreateQueueOptions;
import com.microsoft.windowsazure.services.queue.models.GetQueueMetadataResult;
import com.microsoft.windowsazure.services.queue.models.GetServicePropertiesResult;
import com.microsoft.windowsazure.services.queue.models.ListMessagesOptions;
import com.microsoft.windowsazure.services.queue.models.ListMessagesResult;
import com.microsoft.windowsazure.services.queue.models.ListQueuesOptions;
import com.microsoft.windowsazure.services.queue.models.ListQueuesResult;
import com.microsoft.windowsazure.services.queue.models.PeekMessagesOptions;
import com.microsoft.windowsazure.services.queue.models.PeekMessagesResult;
import com.microsoft.windowsazure.services.queue.models.QueueServiceOptions;
import com.microsoft.windowsazure.services.queue.models.ServiceProperties;
import com.microsoft.windowsazure.services.queue.models.UpdateMessageResult;

public interface QueueContract extends FilterableService<QueueContract> {
    GetServicePropertiesResult getServiceProperties() throws ServiceException;

    GetServicePropertiesResult getServiceProperties(QueueServiceOptions options) throws ServiceException;

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

    void createMessage(String queue, String messageText) throws ServiceException;

    void createMessage(String queue, String messageText, CreateMessageOptions options) throws ServiceException;

    UpdateMessageResult updateMessage(String queue, String messageId, String popReceipt, String messageText, int visibilityTimeoutInSeconds)
            throws ServiceException;

    UpdateMessageResult updateMessage(String queue, String messageId, String popReceipt, String messageText, int visibilityTimeoutInSeconds,
            QueueServiceOptions options) throws ServiceException;

    void deleteMessage(String queue, String messageId, String popReceipt) throws ServiceException;

    void deleteMessage(String queue, String messageId, String popReceipt, QueueServiceOptions options) throws ServiceException;

    ListMessagesResult listMessages(String queue) throws ServiceException;

    ListMessagesResult listMessages(String queue, ListMessagesOptions options) throws ServiceException;

    PeekMessagesResult peekMessages(String queue) throws ServiceException;

    PeekMessagesResult peekMessages(String queue, PeekMessagesOptions options) throws ServiceException;

    void clearMessages(String queue) throws ServiceException;

    void clearMessages(String queue, QueueServiceOptions options) throws ServiceException;

}
