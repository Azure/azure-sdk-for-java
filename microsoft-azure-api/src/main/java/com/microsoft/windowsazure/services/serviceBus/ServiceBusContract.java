package com.microsoft.windowsazure.services.serviceBus;

import com.microsoft.windowsazure.common.FilterableService;
import com.microsoft.windowsazure.common.ServiceException;
import com.microsoft.windowsazure.services.serviceBus.models.BrokeredMessage;
import com.microsoft.windowsazure.services.serviceBus.models.CreateQueueResult;
import com.microsoft.windowsazure.services.serviceBus.models.CreateRuleResult;
import com.microsoft.windowsazure.services.serviceBus.models.CreateSubscriptionResult;
import com.microsoft.windowsazure.services.serviceBus.models.CreateTopicResult;
import com.microsoft.windowsazure.services.serviceBus.models.GetQueueResult;
import com.microsoft.windowsazure.services.serviceBus.models.GetRuleResult;
import com.microsoft.windowsazure.services.serviceBus.models.GetSubscriptionResult;
import com.microsoft.windowsazure.services.serviceBus.models.GetTopicResult;
import com.microsoft.windowsazure.services.serviceBus.models.ListQueuesOptions;
import com.microsoft.windowsazure.services.serviceBus.models.ListQueuesResult;
import com.microsoft.windowsazure.services.serviceBus.models.ListRulesOptions;
import com.microsoft.windowsazure.services.serviceBus.models.ListRulesResult;
import com.microsoft.windowsazure.services.serviceBus.models.ListSubscriptionsOptions;
import com.microsoft.windowsazure.services.serviceBus.models.ListSubscriptionsResult;
import com.microsoft.windowsazure.services.serviceBus.models.ListTopicsOptions;
import com.microsoft.windowsazure.services.serviceBus.models.ListTopicsResult;
import com.microsoft.windowsazure.services.serviceBus.models.QueueInfo;
import com.microsoft.windowsazure.services.serviceBus.models.ReceiveMessageOptions;
import com.microsoft.windowsazure.services.serviceBus.models.ReceiveMessageResult;
import com.microsoft.windowsazure.services.serviceBus.models.ReceiveQueueMessageResult;
import com.microsoft.windowsazure.services.serviceBus.models.ReceiveSubscriptionMessageResult;
import com.microsoft.windowsazure.services.serviceBus.models.RuleInfo;
import com.microsoft.windowsazure.services.serviceBus.models.SubscriptionInfo;
import com.microsoft.windowsazure.services.serviceBus.models.TopicInfo;

/**
 * 
 * Defines the service bus contract.
 * 
 */
public interface ServiceBusContract extends FilterableService<ServiceBusContract> {

    /**
     * Sends a queue message.
     * 
     * @param queuePath
     *            A <code>String</code> object that represents the name of the queue to which the message will be sent.
     * @param message
     *            A <code>Message</code> object that represents the message to send.
     * 
     * @exception ServiceException
     *                If a service exception is encountered.
     */
    void sendQueueMessage(String queuePath, BrokeredMessage message) throws ServiceException;

    /**
     * Receives a queue message.
     * 
     * @param queuePath
     *            A <code>String</code> object that represents the name of the queue from which to receive the message.
     * 
     * @return A <code>ReceiveQueueMessageResult</code> object that represents the result.
     * 
     * @exception ServiceException
     *                If a service exception is encountered.
     * 
     */
    ReceiveQueueMessageResult receiveQueueMessage(String queuePath) throws ServiceException;

    /**
     * Receives a queue message using the specified receive message options.
     * 
     * @param queuePath
     *            A <code>String</code> object that represents the name of the queue from which to receive the message.
     * 
     * @param options
     *            A <code>ReceiveMessageOptions</code> object that represents the receive message options.
     * 
     * @return A <code>ReceiveQueueMessageResult</code> object that represents the result.
     * 
     * @exception ServiceException
     *                If a service exception is encountered.
     */
    ReceiveQueueMessageResult receiveQueueMessage(String queuePath, ReceiveMessageOptions options)
            throws ServiceException;

    /**
     * Sends a topic message.
     * 
     * @param topicPath
     *            A <code>String</code> object that represents the name of the topic to which the message will be sent.
     * @param message
     *            A <code>Message</code> object that represents the message to send.
     * 
     * @exception ServiceException
     *                If a service exception is encountered.
     */
    void sendTopicMessage(String topicPath, BrokeredMessage message) throws ServiceException;

    /**
     * Receives a subscription message.
     * 
     * @param topicPath
     *            A <code>String</code> object that represents the name of the topic to receive.
     * @param subscriptionName
     *            A <code>String</code> object that represents the name of the subscription from the message will be
     *            received.
     * 
     * @return A <code>ReceiveSubscriptionMessageResult</code> object that represents the result.
     * 
     * @exception ServiceException
     *                If a service exception is encountered.
     */
    ReceiveSubscriptionMessageResult receiveSubscriptionMessage(String topicPath, String subscriptionName)
            throws ServiceException;

    /**
     * Receives a subscription message using the specified receive message options.
     * 
     * @param topicPath
     *            A <code>String</code> object that represents the name of the topic to receive.
     * @param subscriptionName
     *            A <code>String</code> object that represents the name of the subscription from the message will be
     *            received.
     * @param options
     *            A <code>ReceiveMessageOptions</code> object that represents the receive message options.
     * 
     * @return A <code>ReceiveSubscriptionMessageResult</code> object that represents the result.
     * 
     * @exception ServiceException
     *                If a service exception is encountered.
     * 
     */
    ReceiveSubscriptionMessageResult receiveSubscriptionMessage(String topicPath, String subscriptionName,
            ReceiveMessageOptions options) throws ServiceException;

    /**
     * Unlocks a message.
     * 
     * @param message
     *            A <code>Message</code> object that represents the message to unlock.
     * 
     * @exception ServiceException
     *                If a service exception is encountered.
     */
    void unlockMessage(BrokeredMessage message) throws ServiceException;

    void sendMessage(String path, BrokeredMessage message) throws ServiceException;

    ReceiveMessageResult receiveMessage(String path) throws ServiceException;

    ReceiveMessageResult receiveMessage(String path, ReceiveMessageOptions options) throws ServiceException;

    /**
     * Deletes a message.
     * 
     * @param message
     *            A <code>Message</code> object that represents the message to delete.
     * 
     * @exception ServiceException
     *                If a service exception is encountered.
     */
    void deleteMessage(BrokeredMessage message) throws ServiceException;

    /**
     * Creates a queue.
     * 
     * @param queue
     *            A <code>Queue</code> object that represents the queue to create.
     * 
     * @return A <code>CreateQueueResult</code> object that represents the result.
     * 
     * @exception ServiceException
     *                If a service exception is encountered.
     */
    CreateQueueResult createQueue(QueueInfo queue) throws ServiceException;

    /**
     * Deletes a queue.
     * 
     * @param queuePath
     *            A <code>String</code> object that represents the name of the queue to delete.
     * 
     * @exception ServiceException
     *                If a service exception is encountered.
     */
    void deleteQueue(String queuePath) throws ServiceException;

    /**
     * Retrieves a queue.
     * 
     * @param queuePath
     *            A <code>String</code> object that represents the name of the queue to retrieve.
     * 
     * @return A <code>GetQueueResult</code> object that represents the result.
     * 
     * @exception ServiceException
     *                If a service exception is encountered.
     */
    GetQueueResult getQueue(String queuePath) throws ServiceException;

    /**
     * Returns a list of queues.
     * 
     * @return A <code>ListQueuesResult</code> object that represents the result.
     * 
     * @exception ServiceException
     *                If a service exception is encountered.
     */
    ListQueuesResult listQueues() throws ServiceException;

    ListQueuesResult listQueues(ListQueuesOptions options) throws ServiceException;

    /**
     * Creates a topic.
     * 
     * @param topic
     *            A <code>Topic</code> object that represents the topic to create.
     * 
     * @return A <code>CreateTopicResult</code> object that represents the result.
     * 
     * @exception ServiceException
     *                If a service exception is encountered.
     */
    CreateTopicResult createTopic(TopicInfo topic) throws ServiceException;

    /**
     * Deletes a topic.
     * 
     * @param topicPath
     *            A <code>String</code> object that represents the name of the queue to delete.
     * 
     * @exception ServiceException
     *                If a service exception is encountered.
     */
    void deleteTopic(String topicPath) throws ServiceException;

    /**
     * Retrieves a topic.
     * 
     * @param topicPath
     *            A <code>String</code> object that represents the name of the topic to retrieve.
     * 
     * @return A <code>GetTopicResult</code> object that represents the result.
     * 
     * @exception ServiceException
     *                If a service exception is encountered.
     */
    GetTopicResult getTopic(String topicPath) throws ServiceException;

    /**
     * Returns a list of topics.
     * 
     * @return A <code>ListTopicsResult</code> object that represents the result.
     * 
     * @exception ServiceException
     *                If a service exception is encountered.
     */
    ListTopicsResult listTopics() throws ServiceException;

    ListTopicsResult listTopics(ListTopicsOptions options) throws ServiceException;

    /**
     * Creates a subscription.
     * 
     * @param topicPath
     *            A <code>String</code> object that represents the name of the topic for the subscription.
     * @param subscription
     *            A <code>Subscription</code> object that represents the subscription to create.
     * 
     * @return A <code>CreateSubscriptionResult</code> object that represents the result.
     * 
     * @exception ServiceException
     *                If a service exception is encountered.
     */
    CreateSubscriptionResult createSubscription(String topicPath, SubscriptionInfo subscription)
            throws ServiceException;

    /**
     * Deletes a subscription.
     * 
     * @param topicPath
     *            A <code>String</code> object that represents the name of the topic for the subscription.
     * 
     * @param subscriptionName
     *            A <code>String</code> object that represents the name of the subscription to delete.
     * 
     * @exception ServiceException
     *                If a service exception is encountered.
     */
    void deleteSubscription(String topicPath, String subscriptionName) throws ServiceException;

    /**
     * Retrieves a subscription.
     * 
     * @param topicPath
     *            A <code>String</code> object that represents the name of the topic for the subscription.
     * @param subscriptionName
     *            A <code>String</code> object that represents the name of the subscription to retrieve.
     * 
     * @return A <code>GetSubscriptionResult</code> object that represents the result.
     *         A <code>String</code> object that represents the name of the subscription to retrieve.
     * 
     * @exception ServiceException
     *                If a service exception is encountered.
     */
    GetSubscriptionResult getSubscription(String topicPath, String subscriptionName) throws ServiceException;

    /**
     * Returns a list of subscriptions.
     * 
     * @param topicPath
     *            A <code>String</code> object that represents the name of the topic for the subscriptions to retrieve.
     * 
     * @return A <code>ListSubscriptionsResult</code> object that represents the result.
     * 
     * @exception ServiceException
     *                If a service exception is encountered.
     */
    ListSubscriptionsResult listSubscriptions(String topicPath) throws ServiceException;

    ListSubscriptionsResult listSubscriptions(String topicPath, ListSubscriptionsOptions options)
            throws ServiceException;

    /**
     * Creates a rule.
     * 
     * @param topicPath
     *            A <code>String</code> object that represents the name of the topic for the subscription.
     * @param subscriptionName
     *            A <code>String</code> object that represents the name of the subscription for which the rule will be
     *            created.
     * @param rule
     *            A <code>Rule</code> object that represents the rule to create.
     * 
     * @return A <code>CreateRuleResult</code> object that represents the result.
     * 
     * @exception ServiceException
     *                If a service exception is encountered.
     */
    CreateRuleResult createRule(String topicPath, String subscriptionName, RuleInfo rule) throws ServiceException;

    /**
     * Deletes a rule.
     * 
     * @param topicPath
     *            A <code>String</code> object that represents the name of the topic for the subscription.
     * @param subscriptionName
     *            A <code>String</code> object that represents the name of the subscription for which the rule will be
     *            deleted.
     * @param ruleName
     *            A <code>String</code> object that represents the name of the rule to delete.
     * 
     * @exception ServiceException
     *                If a service exception is encountered.
     */
    void deleteRule(String topicPath, String subscriptionName, String ruleName) throws ServiceException;

    /**
     * Retrieves a rule.
     * 
     * @param topicPath
     *            A <code>String</code> object that represents the name of the topic for the subscription.
     * @param subscriptionName
     *            A <code>String</code> object that represents the name of the subscription for which the rule will be
     *            retrieved.
     * @param ruleName
     *            A <code>String</code> object that represents the name of the rule to retrieve.
     * 
     * @return A <code>GetRuleResult</code> object that represents the result.
     * 
     * @exception ServiceException
     *                If a service exception is encountered.
     */
    GetRuleResult getRule(String topicPath, String subscriptionName, String ruleName) throws ServiceException;

    /**
     * Returns a list of rules.
     * 
     * @param topicPath
     *            A <code>String</code> object that represents the name of the topic for the subscription.
     * @param subscriptionName
     *            A <code>String</code> object that represents the name of the subscription whose rules are being
     *            retrieved.
     * 
     * @return A <code>ListRulesResult</code> object that represents the result.
     * 
     * @exception ServiceException
     *                If a service exception is encountered.
     */
    ListRulesResult listRules(String topicPath, String subscriptionName) throws ServiceException;

    ListRulesResult listRules(String topicPath, String subscriptionName, ListRulesOptions options)
            throws ServiceException;
}
