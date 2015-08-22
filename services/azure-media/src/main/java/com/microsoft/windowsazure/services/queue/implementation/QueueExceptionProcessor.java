/**
 * 
 * Copyright (c) Microsoft and contributors.  All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.microsoft.windowsazure.services.queue.implementation;

import java.util.HashMap;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.microsoft.windowsazure.core.pipeline.filter.ServiceRequestFilter;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceResponseFilter;
import com.microsoft.windowsazure.core.pipeline.jersey.ServiceFilter;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.exception.ServiceExceptionFactory;
import com.microsoft.windowsazure.services.queue.QueueContract;
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
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;

public class QueueExceptionProcessor implements QueueContract {
    private static Log log = LogFactory.getLog(QueueExceptionProcessor.class);
    private final QueueContract service;

    @Inject
    public QueueExceptionProcessor(QueueRestProxy service) {
        this.service = service;
    }

    public QueueExceptionProcessor(QueueContract service) {
        this.service = service;
    }

    public QueueContract withFilter(ServiceFilter filter) {
        return new QueueExceptionProcessor(service.withFilter(filter));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.microsoft.windowsazure.services.core.FilterableService#
     * withRequestFilterFirst
     * (com.microsoft.windowsazure.services.core.ServiceFilter)
     */
    @Override
    public QueueContract withRequestFilterFirst(
            ServiceRequestFilter serviceRequestFilter) {
        return new QueueExceptionProcessor(
                service.withRequestFilterFirst(serviceRequestFilter));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.microsoft.windowsazure.services.core.FilterableService#
     * withRequestFilterLast
     * (com.microsoft.windowsazure.services.core.ServiceFilter)
     */
    @Override
    public QueueContract withRequestFilterLast(
            ServiceRequestFilter serviceRequestFilter) {
        return new QueueExceptionProcessor(
                service.withRequestFilterLast(serviceRequestFilter));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.microsoft.windowsazure.services.core.FilterableService#
     * withResponseFilterFirst
     * (com.microsoft.windowsazure.services.core.ServiceFilter)
     */
    @Override
    public QueueContract withResponseFilterFirst(
            ServiceResponseFilter serviceResponseFilter) {
        return new QueueExceptionProcessor(
                service.withResponseFilterFirst(serviceResponseFilter));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.microsoft.windowsazure.services.core.FilterableService#
     * withResponseFilterLast
     * (com.microsoft.windowsazure.services.core.ServiceFilter)
     */
    @Override
    public QueueContract withResponseFilterLast(
            ServiceResponseFilter serviceResponseFilter) {
        return new QueueExceptionProcessor(
                service.withResponseFilterLast(serviceResponseFilter));
    }

    private ServiceException processCatch(ServiceException e) {
        log.warn(e.getMessage(), e.getCause());
        return ServiceExceptionFactory.process("queue", e);
    }

    public GetServicePropertiesResult getServiceProperties()
            throws ServiceException {
        try {
            return service.getServiceProperties();
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public GetServicePropertiesResult getServiceProperties(
            QueueServiceOptions options) throws ServiceException {
        try {
            return service.getServiceProperties(options);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void setServiceProperties(ServiceProperties serviceProperties)
            throws ServiceException {
        try {
            service.setServiceProperties(serviceProperties);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void setServiceProperties(ServiceProperties serviceProperties,
            QueueServiceOptions options) throws ServiceException {
        try {
            service.setServiceProperties(serviceProperties, options);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void createQueue(String queue) throws ServiceException {
        try {
            service.createQueue(queue);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void createQueue(String queue, CreateQueueOptions options)
            throws ServiceException {
        try {
            service.createQueue(queue, options);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void deleteQueue(String queue) throws ServiceException {
        try {
            service.deleteQueue(queue);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void deleteQueue(String queue, QueueServiceOptions options)
            throws ServiceException {
        try {
            service.deleteQueue(queue, options);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public ListQueuesResult listQueues() throws ServiceException {
        try {
            return service.listQueues();
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public ListQueuesResult listQueues(ListQueuesOptions options)
            throws ServiceException {
        try {
            return service.listQueues(options);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public GetQueueMetadataResult getQueueMetadata(String queue)
            throws ServiceException {
        try {
            return service.getQueueMetadata(queue);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public GetQueueMetadataResult getQueueMetadata(String queue,
            QueueServiceOptions options) throws ServiceException {
        try {
            return service.getQueueMetadata(queue, options);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void setQueueMetadata(String queue, HashMap<String, String> metadata)
            throws ServiceException {
        try {
            service.setQueueMetadata(queue, metadata);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void setQueueMetadata(String queue,
            HashMap<String, String> metadata, QueueServiceOptions options)
            throws ServiceException {
        try {
            service.setQueueMetadata(queue, metadata, options);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void createMessage(String queue, String messageText)
            throws ServiceException {
        try {
            service.createMessage(queue, messageText);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void createMessage(String queue, String messageText,
            CreateMessageOptions options) throws ServiceException {
        try {
            service.createMessage(queue, messageText, options);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public UpdateMessageResult updateMessage(String queue, String messageId,
            String popReceipt, String messageText,
            int visibilityTimeoutInSeconds) throws ServiceException {
        try {
            return service.updateMessage(queue, messageId, popReceipt,
                    messageText, visibilityTimeoutInSeconds);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public UpdateMessageResult updateMessage(String queue, String messageId,
            String popReceipt, String messageText,
            int visibilityTimeoutInSeconds, QueueServiceOptions options)
            throws ServiceException {
        try {
            return service.updateMessage(queue, messageId, popReceipt,
                    messageText, visibilityTimeoutInSeconds, options);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public ListMessagesResult listMessages(String queue)
            throws ServiceException {
        try {
            return service.listMessages(queue);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public ListMessagesResult listMessages(String queue,
            ListMessagesOptions options) throws ServiceException {
        try {
            return service.listMessages(queue, options);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public PeekMessagesResult peekMessages(String queue)
            throws ServiceException {
        try {
            return service.peekMessages(queue);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public PeekMessagesResult peekMessages(String queue,
            PeekMessagesOptions options) throws ServiceException {
        try {
            return service.peekMessages(queue, options);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void deleteMessage(String queue, String messageId, String popReceipt)
            throws ServiceException {
        try {
            service.deleteMessage(queue, messageId, popReceipt);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void deleteMessage(String queue, String messageId,
            String popReceipt, QueueServiceOptions options)
            throws ServiceException {
        try {
            service.deleteMessage(queue, messageId, popReceipt, options);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void clearMessages(String queue) throws ServiceException {
        try {
            service.clearMessages(queue);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void clearMessages(String queue, QueueServiceOptions options)
            throws ServiceException {
        try {
            service.clearMessages(queue, options);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }
}
