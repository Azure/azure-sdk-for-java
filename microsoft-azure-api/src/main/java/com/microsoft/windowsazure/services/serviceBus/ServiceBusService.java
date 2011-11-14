package com.microsoft.windowsazure.services.serviceBus;

import javax.inject.Inject;

import com.microsoft.windowsazure.ServiceException;
import com.microsoft.windowsazure.configuration.Configuration;
import com.microsoft.windowsazure.http.ServiceFilter;
import com.microsoft.windowsazure.services.serviceBus.implementation.Entry;
import com.microsoft.windowsazure.services.serviceBus.implementation.Feed;
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

public class ServiceBusService implements ServiceBusContract {
    final ServiceBusContract next;

    public ServiceBusService() throws Exception {
        this(null, Configuration.getInstance());
    }

    public ServiceBusService(Configuration config) throws Exception {
        this(null, config);
    }

    public ServiceBusService(String profile) throws Exception {
        this(profile, Configuration.getInstance());
    }

    public ServiceBusService(String profile, Configuration config) throws Exception {
        next = config.create(profile, ServiceBusService.class);
    }

    @Inject
    public ServiceBusService(ServiceBusContract next) throws Exception {
        this.next = next;
    }

    public ServiceBusContract withFilter(ServiceFilter filter) {
        return next.withFilter(filter);
    }

    public void sendQueueMessage(String queueName, Message message)
            throws ServiceException {
        next.sendQueueMessage(queueName, message);
    }

    public ReceiveQueueMessageResult receiveQueueMessage(String queueName)
            throws ServiceException {
        return next.receiveQueueMessage(queueName);
    }

    public ReceiveQueueMessageResult receiveQueueMessage(String queueName,
            ReceiveMessageOptions options) throws ServiceException {
        return next.receiveQueueMessage(queueName, options);
    }

    public void sendTopicMessage(String topicName, Message message)
            throws ServiceException {
        next.sendTopicMessage(topicName, message);
    }

    public ReceiveSubscriptionMessageResult receiveSubscriptionMessage(String topicName,
            String subscriptionName) throws ServiceException {
        return next.receiveSubscriptionMessage(topicName, subscriptionName);
    }

    public ReceiveSubscriptionMessageResult receiveSubscriptionMessage(String topicName,
            String subscriptionName, ReceiveMessageOptions options)
            throws ServiceException {
        return next.receiveSubscriptionMessage(topicName, subscriptionName,
                options);
    }

    public void unlockMessage(Message message) throws ServiceException {
        next.unlockMessage(message);
    }

    public void deleteMessage(Message message) throws ServiceException {
        next.deleteMessage(message);
    }

    public CreateQueueResult createQueue(Queue queue) throws ServiceException {
        return next.createQueue(queue);
    }

    public void deleteQueue(String queueName) throws ServiceException {
        next.deleteQueue(queueName);
    }

    public GetQueueResult getQueue(String queueName) throws ServiceException {
        return next.getQueue(queueName);
    }

    public ListQueuesResult listQueues() throws ServiceException {
        return next.listQueues();
    }

    public CreateTopicResult createTopic(Topic topic) throws ServiceException {
        return next.createTopic(topic);
    }

    public void deleteTopic(String topicName) throws ServiceException {
        next.deleteTopic(topicName);
    }

    public GetTopicResult getTopic(String topicName) throws ServiceException {
        return next.getTopic(topicName);
    }

    public ListTopicsResult listTopics() throws ServiceException {
        return next.listTopics();
    }

    public CreateSubscriptionResult createSubscription(String topicName, Subscription subscription) throws ServiceException {
        return next.createSubscription(topicName, subscription);
    }

    public void deleteSubscription(String topicName, String subscriptionName)
            throws ServiceException {
        next.deleteSubscription(topicName, subscriptionName);
    }

    public GetSubscriptionResult getSubscription(String topicName, String subscriptionName)
            throws ServiceException {
        return next.getSubscription(topicName, subscriptionName);
    }

    public ListSubscriptionsResult listSubscriptions(String topicName) throws ServiceException {
        return next.listSubscriptions(topicName);
    }

    public void addRule(String topicName, String subscriptionName,
            String ruleName, Entry rule) throws ServiceException {
        next.addRule(topicName, subscriptionName, ruleName, rule);
    }

    public void removeRule(String topicName, String subscriptionName,
            String ruleName) throws ServiceException {
        next.removeRule(topicName, subscriptionName, ruleName);
    }

    public Entry getRule(String topicName, String subscriptionName,
            String ruleName) throws ServiceException {
        return next.getRule(topicName, subscriptionName, ruleName);
    }

    public Feed getRules(String topicName, String subscriptionName)
            throws ServiceException {
        return next.getRules(topicName, subscriptionName);
    }
}
