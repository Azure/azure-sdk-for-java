package com.microsoft.windowsazure.services.queue.implementation;

import java.util.HashMap;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.microsoft.windowsazure.ServiceException;
import com.microsoft.windowsazure.http.ServiceFilter;
import com.microsoft.windowsazure.services.queue.QueueServiceContract;
import com.microsoft.windowsazure.services.queue.models.CreateMessageOptions;
import com.microsoft.windowsazure.services.queue.models.CreateQueueOptions;
import com.microsoft.windowsazure.services.queue.models.GetQueueMetadataResult;
import com.microsoft.windowsazure.services.queue.models.ListMessagesOptions;
import com.microsoft.windowsazure.services.queue.models.ListMessagesResult;
import com.microsoft.windowsazure.services.queue.models.ListQueuesOptions;
import com.microsoft.windowsazure.services.queue.models.ListQueuesResult;
import com.microsoft.windowsazure.services.queue.models.PeekMessagesOptions;
import com.microsoft.windowsazure.services.queue.models.PeekMessagesResult;
import com.microsoft.windowsazure.services.queue.models.QueueServiceOptions;
import com.microsoft.windowsazure.services.queue.models.QueueServiceProperties;
import com.microsoft.windowsazure.services.queue.models.UpdateMessageResult;
import com.microsoft.windowsazure.utils.ServiceExceptionFactory;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;

public class QueueServiceImpl implements QueueServiceContract {
    private static Log log = LogFactory.getLog(QueueServiceImpl.class);
    private final QueueServiceContract service;

    @Inject
    public QueueServiceImpl(QueueServiceForJersey service) {
        this.service = service;
    }

    public QueueServiceImpl(QueueServiceContract service) {
        this.service = service;
    }

    public QueueServiceContract withFilter(ServiceFilter filter) {
        return new QueueServiceImpl(service.withFilter(filter));
    }

    private ServiceException processCatch(ServiceException e) {
        log.warn(e.getMessage(), e.getCause());
        return ServiceExceptionFactory.process("blob", e);
    }

    public QueueServiceProperties getServiceProperties() throws ServiceException {
        try {
            return service.getServiceProperties();
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public QueueServiceProperties getServiceProperties(QueueServiceOptions options) throws ServiceException {
        try {
            return service.getServiceProperties(options);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void setServiceProperties(QueueServiceProperties serviceProperties) throws ServiceException {
        try {
            service.setServiceProperties(serviceProperties);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void setServiceProperties(QueueServiceProperties serviceProperties, QueueServiceOptions options) throws ServiceException {
        try {
            service.setServiceProperties(serviceProperties, options);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void createQueue(String queue) throws ServiceException {
        try {
            service.createQueue(queue);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void createQueue(String queue, CreateQueueOptions options) throws ServiceException {
        try {
            service.createQueue(queue, options);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void deleteQueue(String queue) throws ServiceException {
        try {
            service.deleteQueue(queue);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void deleteQueue(String queue, QueueServiceOptions options) throws ServiceException {
        try {
            service.deleteQueue(queue, options);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public ListQueuesResult listQueues() throws ServiceException {
        try {
            return service.listQueues();
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public ListQueuesResult listQueues(ListQueuesOptions options) throws ServiceException {
        try {
            return service.listQueues(options);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public GetQueueMetadataResult getQueueMetadata(String queue) throws ServiceException {
        try {
            return service.getQueueMetadata(queue);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public GetQueueMetadataResult getQueueMetadata(String queue, QueueServiceOptions options) throws ServiceException {
        try {
            return service.getQueueMetadata(queue, options);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void setQueueMetadata(String queue, HashMap<String, String> metadata) throws ServiceException {
        try {
            service.setQueueMetadata(queue, metadata);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void setQueueMetadata(String queue, HashMap<String, String> metadata, QueueServiceOptions options) throws ServiceException {
        try {
            service.setQueueMetadata(queue, metadata, options);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void createMessage(String queue, String message) throws ServiceException {
        try {
            service.createMessage(queue, message);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void createMessage(String queue, String message, CreateMessageOptions options) throws ServiceException {
        try {
            service.createMessage(queue, message, options);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public UpdateMessageResult updateMessage(String queue, String message, String popReceipt, String text, int visibilityTimeoutInSeconds)
            throws ServiceException {
        try {
            return service.updateMessage(queue, message, popReceipt, text, visibilityTimeoutInSeconds);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public UpdateMessageResult updateMessage(String queue, String message, String popReceipt, String text, int visibilityTimeoutInSeconds,
            QueueServiceOptions options) throws ServiceException {
        try {
            return service.updateMessage(queue, message, popReceipt, text, visibilityTimeoutInSeconds, options);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public ListMessagesResult listMessages(String queue) throws ServiceException {
        try {
            return service.listMessages(queue);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public ListMessagesResult listMessages(String queue, ListMessagesOptions options) throws ServiceException {
        try {
            return service.listMessages(queue, options);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public PeekMessagesResult peekMessages(String queue) throws ServiceException {
        try {
            return service.peekMessages(queue);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public PeekMessagesResult peekMessages(String queue, PeekMessagesOptions options) throws ServiceException {
        try {
            return service.peekMessages(queue, options);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void deleteMessage(String queue, String message, String popReceipt) throws ServiceException {
        try {
            service.deleteMessage(queue, message, popReceipt);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void deleteMessage(String queue, String message, String popReceipt, QueueServiceOptions options) throws ServiceException {
        try {
            service.deleteMessage(queue, message, popReceipt, options);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void clearMessages(String queue) throws ServiceException {
        try {
            service.clearMessages(queue);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void clearMessages(String queue, QueueServiceOptions options) throws ServiceException {
        try {
            service.clearMessages(queue, options);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

}
