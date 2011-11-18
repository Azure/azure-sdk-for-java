package com.microsoft.windowsazure.services.serviceBus;

import com.microsoft.windowsazure.common.FilterableService;
import com.microsoft.windowsazure.common.ServiceException;
import com.microsoft.windowsazure.services.serviceBus.models.CreateQueueResult;
import com.microsoft.windowsazure.services.serviceBus.models.CreateRuleResult;
import com.microsoft.windowsazure.services.serviceBus.models.CreateSubscriptionResult;
import com.microsoft.windowsazure.services.serviceBus.models.CreateTopicResult;
import com.microsoft.windowsazure.services.serviceBus.models.GetQueueResult;
import com.microsoft.windowsazure.services.serviceBus.models.GetRuleResult;
import com.microsoft.windowsazure.services.serviceBus.models.GetSubscriptionResult;
import com.microsoft.windowsazure.services.serviceBus.models.GetTopicResult;
import com.microsoft.windowsazure.services.serviceBus.models.ListQueuesResult;
import com.microsoft.windowsazure.services.serviceBus.models.ListRulesResult;
import com.microsoft.windowsazure.services.serviceBus.models.ListSubscriptionsResult;
import com.microsoft.windowsazure.services.serviceBus.models.ListTopicsResult;
import com.microsoft.windowsazure.services.serviceBus.models.Message;
import com.microsoft.windowsazure.services.serviceBus.models.Queue;
import com.microsoft.windowsazure.services.serviceBus.models.ReceiveMessageOptions;
import com.microsoft.windowsazure.services.serviceBus.models.ReceiveQueueMessageResult;
import com.microsoft.windowsazure.services.serviceBus.models.ReceiveSubscriptionMessageResult;
import com.microsoft.windowsazure.services.serviceBus.models.Rule;
import com.microsoft.windowsazure.services.serviceBus.models.Subscription;
import com.microsoft.windowsazure.services.serviceBus.models.Topic;

public interface ServiceBusContract extends FilterableService<ServiceBusContract> {

    void sendQueueMessage(String queueName, Message message) throws ServiceException;

    ReceiveQueueMessageResult receiveQueueMessage(String queueName) throws ServiceException;

    ReceiveQueueMessageResult receiveQueueMessage(String queueName, ReceiveMessageOptions options)
            throws ServiceException;

    void sendTopicMessage(String topicName, Message message) throws ServiceException;

    ReceiveSubscriptionMessageResult receiveSubscriptionMessage(String topicName, String subscriptionName)
            throws ServiceException;

    ReceiveSubscriptionMessageResult receiveSubscriptionMessage(String topicName, String subscriptionName,
            ReceiveMessageOptions options) throws ServiceException;

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

    CreateRuleResult createRule(String topicName, String subscriptionName, Rule rule) throws ServiceException;

    void deleteRule(String topicName, String subscriptionName, String ruleName) throws ServiceException;

    GetRuleResult getRule(String topicName, String subscriptionName, String ruleName) throws ServiceException;

    ListRulesResult listRules(String topicName, String subscriptionName) throws ServiceException;
}
