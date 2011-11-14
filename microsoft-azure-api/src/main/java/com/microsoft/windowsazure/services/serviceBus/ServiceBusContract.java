package com.microsoft.windowsazure.services.serviceBus;

import com.microsoft.windowsazure.ServiceException;
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
import com.microsoft.windowsazure.services.serviceBus.models.ReceiveMessageOptions;
import com.microsoft.windowsazure.services.serviceBus.models.ReceiveQueueMessageResult;
import com.microsoft.windowsazure.services.serviceBus.models.ReceiveSubscriptionMessageResult;

public interface ServiceBusContract {
    ServiceBusContract withFilter(ServiceFilter filter);

    void sendQueueMessage(String queueName, Message message) throws ServiceException;

    ReceiveQueueMessageResult receiveQueueMessage(String queueName) throws ServiceException;

    ReceiveQueueMessageResult receiveQueueMessage(String queueName, ReceiveMessageOptions options) throws ServiceException;

    void sendTopicMessage(String topicName, Message message) throws ServiceException;

    ReceiveSubscriptionMessageResult receiveSubscriptionMessage(String topicName, String subscriptionName) throws ServiceException;

    ReceiveSubscriptionMessageResult receiveSubscriptionMessage(String topicName, String subscriptionName, ReceiveMessageOptions options)
            throws ServiceException;

    void unlockMessage(Message message) throws ServiceException;

    void deleteMessage(Message message) throws ServiceException;

    CreateQueueResult createQueue(Queue queue) throws ServiceException;

    void deleteQueue(String queueName) throws ServiceException;

    GetQueueResult getQueue(String queueName) throws ServiceException;

    ListQueuesResult listQueues() throws ServiceException;

    CreateTopicResult createTopic(Topic topic) throws ServiceException;

    void deleteTopic(String topicName) throws ServiceException;

    GetTopicResult getTopic(String topicName) throws ServiceException;

    ListTopicsResult listTopics() throws ServiceException;

    CreateSubscriptionResult createSubscription(String topicName, Subscription subscription) throws ServiceException;

    void deleteSubscription(String topicName, String subscriptionName) throws ServiceException;

    GetSubscriptionResult getSubscription(String topicName, String subscriptionName) throws ServiceException;

    ListSubscriptionsResult listSubscriptions(String topicName) throws ServiceException;

    void addRule(String topicName, String subscriptionName, String ruleName, Entry rule) throws ServiceException;

    void removeRule(String topicName, String subscriptionName, String ruleName) throws ServiceException;

    Entry getRule(String topicName, String subscriptionName, String ruleName) throws ServiceException;

    Feed getRules(String topicName, String subscriptionName) throws ServiceException;
}
