package com.microsoft.windowsazure.services.queue;

import java.util.HashMap;

import javax.inject.Inject;

import com.microsoft.windowsazure.services.core.Configuration;
import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.core.ServiceFilter;
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

public class QueueService implements QueueContract {
    final QueueContract next;

    public QueueService() throws Exception {
        this(null, Configuration.getInstance());
    }

    public QueueService(Configuration config) throws Exception {
        this(null, config);
    }

    public QueueService(String profile) throws Exception {
        this(profile, Configuration.getInstance());
    }

    public QueueService(String profile, Configuration config) throws Exception {
        next = config.create(profile, QueueContract.class);
    }

    @Inject
    public QueueService(QueueContract next) throws Exception {
        this.next = next;
    }

    public QueueContract withFilter(ServiceFilter filter) {
        return next.withFilter(filter);
    }

    public GetServicePropertiesResult getServiceProperties() throws ServiceException {
        return next.getServiceProperties();
    }

    public GetServicePropertiesResult getServiceProperties(QueueServiceOptions options) throws ServiceException {
        return next.getServiceProperties(options);
    }

    public void setServiceProperties(ServiceProperties serviceProperties) throws ServiceException {
        next.setServiceProperties(serviceProperties);
    }

    public void setServiceProperties(ServiceProperties serviceProperties, QueueServiceOptions options) throws ServiceException {
        next.setServiceProperties(serviceProperties, options);
    }

    public void createQueue(String queue) throws ServiceException {
        next.createQueue(queue);
    }

    public void createQueue(String queue, CreateQueueOptions options) throws ServiceException {
        next.createQueue(queue, options);
    }

    public void deleteQueue(String queue) throws ServiceException {
        next.deleteQueue(queue);
    }

    public void deleteQueue(String queue, QueueServiceOptions options) throws ServiceException {
        next.deleteQueue(queue, options);
    }

    public ListQueuesResult listQueues() throws ServiceException {
        return next.listQueues();
    }

    public ListQueuesResult listQueues(ListQueuesOptions options) throws ServiceException {
        return next.listQueues(options);
    }

    public GetQueueMetadataResult getQueueMetadata(String queue) throws ServiceException {
        return next.getQueueMetadata(queue);
    }

    public GetQueueMetadataResult getQueueMetadata(String queue, QueueServiceOptions options) throws ServiceException {
        return next.getQueueMetadata(queue, options);
    }

    public void setQueueMetadata(String queue, HashMap<String, String> metadata) throws ServiceException {
        next.setQueueMetadata(queue, metadata);
    }

    public void setQueueMetadata(String queue, HashMap<String, String> metadata, QueueServiceOptions options) throws ServiceException {
        next.setQueueMetadata(queue, metadata, options);
    }

    public void createMessage(String queue, String messageText) throws ServiceException {
        next.createMessage(queue, messageText);
    }

    public void createMessage(String queue, String messageText, CreateMessageOptions options) throws ServiceException {
        next.createMessage(queue, messageText, options);
    }

    public UpdateMessageResult updateMessage(String queue, String messageId, String popReceipt, String messageText, int visibilityTimeoutInSeconds)
            throws ServiceException {
        return next.updateMessage(queue, messageId, popReceipt, messageText, visibilityTimeoutInSeconds);
    }

    public UpdateMessageResult updateMessage(String queue, String messageId, String popReceipt, String messageText, int visibilityTimeoutInSeconds,
            QueueServiceOptions options) throws ServiceException {
        return next.updateMessage(queue, messageId, popReceipt, messageText, visibilityTimeoutInSeconds, options);
    }

    public void deleteMessage(String queue, String messageId, String popReceipt) throws ServiceException {
        next.deleteMessage(queue, messageId, popReceipt);
    }

    public void deleteMessage(String queue, String messageId, String popReceipt, QueueServiceOptions options) throws ServiceException {
        next.deleteMessage(queue, messageId, popReceipt, options);
    }

    public ListMessagesResult listMessages(String queue) throws ServiceException {
        return next.listMessages(queue);
    }

    public ListMessagesResult listMessages(String queue, ListMessagesOptions options) throws ServiceException {
        return next.listMessages(queue, options);
    }

    public PeekMessagesResult peekMessages(String queue) throws ServiceException {
        return next.peekMessages(queue);
    }

    public PeekMessagesResult peekMessages(String queue, PeekMessagesOptions options) throws ServiceException {
        return next.peekMessages(queue, options);
    }

    public void clearMessages(String queue) throws ServiceException {
        next.clearMessages(queue);
    }

    public void clearMessages(String queue, QueueServiceOptions options) throws ServiceException {
        next.clearMessages(queue, options);
    }
}