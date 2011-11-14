package com.microsoft.windowsazure.services.serviceBus.implementation;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.microsoft.windowsazure.ServiceException;
import com.microsoft.windowsazure.http.ServiceFilter;
import com.microsoft.windowsazure.services.serviceBus.ServiceBusContract;
import com.microsoft.windowsazure.services.serviceBus.models.CreateQueueResult;
import com.microsoft.windowsazure.services.serviceBus.models.CreateSubscriptionResult;
import com.microsoft.windowsazure.services.serviceBus.models.CreateTopicResult;
import com.microsoft.windowsazure.services.serviceBus.models.GetQueueResult;
import com.microsoft.windowsazure.services.serviceBus.models.GetSubscriptionResult;
import com.microsoft.windowsazure.services.serviceBus.models.GetTopicResult;
import com.microsoft.windowsazure.services.serviceBus.models.ListQueuesResult;
import com.microsoft.windowsazure.services.serviceBus.models.ListSubscriptionsResult;
import com.microsoft.windowsazure.services.serviceBus.models.ListTopicsResult;
import com.microsoft.windowsazure.services.serviceBus.models.Message;
import com.microsoft.windowsazure.services.serviceBus.models.Queue;
import com.microsoft.windowsazure.services.serviceBus.models.ReceiveMessageOptions;
import com.microsoft.windowsazure.services.serviceBus.models.ReceiveQueueMessageResult;
import com.microsoft.windowsazure.services.serviceBus.models.ReceiveSubscriptionMessageResult;
import com.microsoft.windowsazure.services.serviceBus.models.Subscription;
import com.microsoft.windowsazure.services.serviceBus.models.Topic;
import com.microsoft.windowsazure.utils.ServiceExceptionFactory;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;

public class ServiceBusExceptionProcessor implements ServiceBusContract {

    private final ServiceBusContract next;
    static Log log = LogFactory.getLog(ServiceBusContract.class);

    public ServiceBusExceptionProcessor(ServiceBusContract next) {
        this.next = next;
    }

    @Inject
    public ServiceBusExceptionProcessor(ServiceBusRestProxy next) {
        this.next = next;
    }

    public ServiceBusContract withFilter(ServiceFilter filter) {
        return new ServiceBusExceptionProcessor(next.withFilter(filter));
    }

    private ServiceException processCatch(ServiceException e) {
        log.warn(e.getMessage(), e.getCause());
        return ServiceExceptionFactory.process("serviceBus", e);
    }

    public void sendQueueMessage(String path, Message message)
            throws ServiceException {
        try {
            next.sendQueueMessage(path, message);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public ReceiveQueueMessageResult receiveQueueMessage(String queueName)
            throws ServiceException {
        try {
            return next.receiveQueueMessage(queueName);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public ReceiveQueueMessageResult receiveQueueMessage(String queueName,
            ReceiveMessageOptions options) throws ServiceException {
        try {
            return next.receiveQueueMessage(queueName, options);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void sendTopicMessage(String path, Message message)
            throws ServiceException {
        try {
            next.sendTopicMessage(path, message);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public ReceiveSubscriptionMessageResult receiveSubscriptionMessage(String topicName,
            String subscriptionName) throws ServiceException {
        try {
            return next.receiveSubscriptionMessage(topicName,
                    subscriptionName);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public ReceiveSubscriptionMessageResult receiveSubscriptionMessage(String topicName,
            String subscriptionName, ReceiveMessageOptions options)
            throws ServiceException {
        try {
            return next.receiveSubscriptionMessage(topicName,
                    subscriptionName, options);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void unlockMessage(Message message) throws ServiceException {
        try {
            next.unlockMessage(message);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void deleteMessage(Message message) throws ServiceException {
        try {
            next.deleteMessage(message);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public CreateQueueResult createQueue(Queue queue) throws ServiceException {
        try {
            return next.createQueue(queue);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void deleteQueue(String queuePath) throws ServiceException {
        try {
            next.deleteQueue(queuePath);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public GetQueueResult getQueue(String queuePath) throws ServiceException {
        try {
            return next.getQueue(queuePath);
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
            return next.listQueues();
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public CreateTopicResult createTopic(Topic topic) throws ServiceException {
        try {
            return next.createTopic(topic);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void deleteTopic(String topicPath) throws ServiceException {
        try {
            next.deleteTopic(topicPath);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public GetTopicResult getTopic(String topicPath) throws ServiceException {
        try {
            return next.getTopic(topicPath);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public ListTopicsResult listTopics() throws ServiceException {
        try {
            return next.listTopics();
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public CreateSubscriptionResult createSubscription(String topicPath, Subscription subscription) throws ServiceException {
        try {
            return next.createSubscription(topicPath, subscription);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void deleteSubscription(String topicPath, String subscriptionName)
            throws ServiceException {
        try {
            next.deleteSubscription(topicPath, subscriptionName);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public GetSubscriptionResult getSubscription(String topicPath, String subscriptionName)
            throws ServiceException {
        try {
            return next.getSubscription(topicPath, subscriptionName);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public ListSubscriptionsResult listSubscriptions(String topicPath) throws ServiceException {
        try {
            return next.listSubscriptions(topicPath);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void addRule(String topicPath, String subscriptionName,
            String ruleName, Entry rule) throws ServiceException {
        try {
            next.addRule(topicPath, subscriptionName, ruleName, rule);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public void removeRule(String topicPath, String subscriptionName,
            String ruleName) throws ServiceException {
        try {
            next.removeRule(topicPath, subscriptionName, ruleName);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public Entry getRule(String topicPath, String subscriptionName,
            String ruleName) throws ServiceException {
        try {
            return next.getRule(topicPath, subscriptionName, ruleName);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    public Feed getRules(String topicPath, String subscriptionName)
            throws ServiceException {
        try {
            return next.getRules(topicPath, subscriptionName);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

}
