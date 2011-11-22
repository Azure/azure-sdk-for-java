package com.microsoft.windowsazure.services.serviceBus;

import javax.inject.Inject;

import com.microsoft.windowsazure.common.Configuration;
import com.microsoft.windowsazure.common.ServiceException;
import com.microsoft.windowsazure.common.ServiceFilter;
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
import com.microsoft.windowsazure.services.serviceBus.models.Message;
import com.microsoft.windowsazure.services.serviceBus.models.QueueInfo;
import com.microsoft.windowsazure.services.serviceBus.models.ReceiveMessageOptions;
import com.microsoft.windowsazure.services.serviceBus.models.ReceiveQueueMessageResult;
import com.microsoft.windowsazure.services.serviceBus.models.ReceiveSubscriptionMessageResult;
import com.microsoft.windowsazure.services.serviceBus.models.Rule;
import com.microsoft.windowsazure.services.serviceBus.models.Subscription;
import com.microsoft.windowsazure.services.serviceBus.models.TopicInfo;

/**
 * 
 * Provides service bus functionality.
 * 
 */
public class ServiceBusService implements ServiceBusContract {
    final ServiceBusContract next;

    /**
     * Creates an instance of the <code>ServiceBusService</code> class.
     * 
     * @exception Exception
     *                If an exception is encountered.
     */
    public ServiceBusService() throws Exception {
        this(null, Configuration.getInstance());
    }

    /**
     * Creates an instance of the <code>ServiceBusService</code> class using the specified configuration.
     * 
     * @param config
     *            A <code>Configuration</code> object that represents the configuration for the service bus service.
     * 
     * @exception Exception
     *                If an exception is encountered.
     */
    public ServiceBusService(Configuration config) throws Exception {
        this(null, config);
    }

    /**
     * Creates an instance of the <code>ServiceBusService</code> class using the specified profile.
     * 
     * @param profile
     *            A <code>String</code> object that represents the profile for the service bus service.
     * 
     * @exception Exception
     *                If an exception is encountered.
     */
    public ServiceBusService(String profile) throws Exception {
        this(profile, Configuration.getInstance());
    }

    /**
     * Creates an instance of the <code>ServiceBusService</code> class using the specified profile and configuration.
     * 
     * @param profile
     *            A <code>String</code> object that represents the profile for the service bus service.
     * 
     * @param config
     *            A <code>Configuration</code> object that represents the configuration for the service bus service.
     * 
     * @exception Exception
     *                If an exception is encountered.
     */
    public ServiceBusService(String profile, Configuration config) throws Exception {
        next = config.create(profile, ServiceBusService.class);
    }

    @Inject
    public ServiceBusService(ServiceBusContract next) throws Exception {
        this.next = next;
    }

    /**
     * Returns a service bus contract with the specified filter.
     * 
     * @param filter
     *            A <code>ServiceFilter</code> object that represents the filter to use.
     * 
     * @return A <code>ServiceBusContract</code> object that represents the service bus contract with the
     *         specified filter applied.
     */
    public ServiceBusContract withFilter(ServiceFilter filter) {
        return next.withFilter(filter);
    }

    /**
     * Sends a queue message.
     * 
     * @param queueName
     *            A <code>String</code> object that represents the name of the queue to which the message will be sent.
     * @param message
     *            A <code>Message</code> object that represents the message to send.
     * 
     * @exception ServiceException
     *                If a service exception is encountered.
     */
    public void sendQueueMessage(String queueName, Message message) throws ServiceException {
        next.sendQueueMessage(queueName, message);
    }

    /**
     * Receives a queue message.
     * 
     * @param queueName
     *            A <code>String</code> object that represents the name of the queue from which to receive the message.
     * 
     * @return A <code>ReceiveQueueMessageResult</code> object that represents the result.
     * 
     * @exception ServiceException
     *                If a service exception is encountered.
     * 
     */
    public ReceiveQueueMessageResult receiveQueueMessage(String queueName) throws ServiceException {
        return next.receiveQueueMessage(queueName);
    }

    /**
     * Receives a queue message using the specified receive message options.
     * 
     * @param queueName
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
    public ReceiveQueueMessageResult receiveQueueMessage(String queueName, ReceiveMessageOptions options)
            throws ServiceException {
        return next.receiveQueueMessage(queueName, options);
    }

    /**
     * Sends a topic message.
     * 
     * @param topicName
     *            A <code>String</code> object that represents the name of the topic to which the message will be sent.
     * @param message
     *            A <code>Message</code> object that represents the message to send.
     * 
     * @exception ServiceException
     *                If a service exception is encountered.
     */
    public void sendTopicMessage(String topicName, Message message) throws ServiceException {
        next.sendTopicMessage(topicName, message);
    }

    /**
     * Receives a subscription message.
     * 
     * @param topicName
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
    public ReceiveSubscriptionMessageResult receiveSubscriptionMessage(String topicName, String subscriptionName)
            throws ServiceException {
        return next.receiveSubscriptionMessage(topicName, subscriptionName);
    }

    /**
     * Receives a subscription message using the specified receive message options.
     * 
     * @param topicName
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
    public ReceiveSubscriptionMessageResult receiveSubscriptionMessage(String topicName, String subscriptionName,
            ReceiveMessageOptions options) throws ServiceException {
        return next.receiveSubscriptionMessage(topicName, subscriptionName, options);
    }

    /**
     * Unlocks a message.
     * 
     * @param message
     *            A <code>Message</code> object that represents the message to unlock.
     * 
     * @exception ServiceException
     *                If a service exception is encountered.
     */
    public void unlockMessage(Message message) throws ServiceException {
        next.unlockMessage(message);
    }

    /**
     * Deletes a message.
     * 
     * @param message
     *            A <code>Message</code> object that represents the message to delete.
     * 
     * @exception ServiceException
     *                If a service exception is encountered.
     */
    public void deleteMessage(Message message) throws ServiceException {
        next.deleteMessage(message);
    }

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
    public CreateQueueResult createQueue(QueueInfo queue) throws ServiceException {
        return next.createQueue(queue);
    }

    /**
     * Deletes a queue.
     * 
     * @param queueName
     *            A <code>String</code> object that represents the name of the queue to delete.
     * 
     * @exception ServiceException
     *                If a service exception is encountered.
     */
    public void deleteQueue(String queueName) throws ServiceException {
        next.deleteQueue(queueName);
    }

    /**
     * Retrieves a queue.
     * 
     * @param queueName
     *            A <code>String</code> object that represents the name of the queue to retrieve.
     * 
     * @return A <code>GetQueueResult</code> object that represents the result.
     * 
     * @exception ServiceException
     *                If a service exception is encountered.
     */
    public GetQueueResult getQueue(String queueName) throws ServiceException {
        return next.getQueue(queueName);
    }

    /**
     * Returns a list of queues.
     * 
     * @return A <code>ListQueuesResult</code> object that represents the result.
     * 
     * @exception ServiceException
     *                If a service exception is encountered.
     */
    public ListQueuesResult listQueues() throws ServiceException {
        return next.listQueues();
    }

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
    public CreateTopicResult createTopic(TopicInfo topic) throws ServiceException {
        return next.createTopic(topic);
    }

    /**
     * Deletes a topic.
     * 
     * @param topicName
     *            A <code>String</code> object that represents the name of the queue to delete.
     * 
     * @exception ServiceException
     *                If a service exception is encountered.
     */
    public void deleteTopic(String topicName) throws ServiceException {
        next.deleteTopic(topicName);
    }

    /**
     * Retrieves a topic.
     * 
     * @param topicName
     *            A <code>String</code> object that represents the name of the topic to retrieve.
     * 
     * @return A <code>GetTopicResult</code> object that represents the result.
     * 
     * @exception ServiceException
     *                If a service exception is encountered.
     */
    public GetTopicResult getTopic(String topicName) throws ServiceException {
        return next.getTopic(topicName);
    }

    /**
     * Returns a list of topics.
     * 
     * @return A <code>ListTopicsResult</code> object that represents the result.
     * 
     * @exception ServiceException
     *                If a service exception is encountered.
     */
    public ListTopicsResult listTopics() throws ServiceException {
        return next.listTopics();
    }

    /**
     * Creates a subscription.
     * 
     * @param topicName
     *            A <code>String</code> object that represents the name of the topic for the subscription.
     * @param subscription
     *            A <code>Subscription</code> object that represents the subscription to create.
     * 
     * @return A <code>CreateSubscriptionResult</code> object that represents the result.
     * 
     * @exception ServiceException
     *                If a service exception is encountered.
     */
    public CreateSubscriptionResult createSubscription(String topicName, Subscription subscription)
            throws ServiceException {
        return next.createSubscription(topicName, subscription);
    }

    /**
     * Deletes a subscription.
     * 
     * @param topicName
     *            A <code>String</code> object that represents the name of the topic for the subscription.
     * 
     * @param subscriptionName
     *            A <code>String</code> object that represents the name of the subscription to delete.
     * 
     * @exception ServiceException
     *                If a service exception is encountered.
     */
    public void deleteSubscription(String topicName, String subscriptionName) throws ServiceException {
        next.deleteSubscription(topicName, subscriptionName);
    }

    /**
     * Retrieves a subscription.
     * 
     * @param topicName
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
    public GetSubscriptionResult getSubscription(String topicName, String subscriptionName) throws ServiceException {
        return next.getSubscription(topicName, subscriptionName);
    }

    /**
     * Returns a list of subscriptions.
     * 
     * @param topicName
     *            A <code>String</code> object that represents the name of the topic for the subscriptions to retrieve.
     * 
     * @return A <code>ListSubscriptionsResult</code> object that represents the result.
     * 
     * @exception ServiceException
     *                If a service exception is encountered.
     */
    public ListSubscriptionsResult listSubscriptions(String topicName) throws ServiceException {
        return next.listSubscriptions(topicName);
    }

    /**
     * Creates a rule.
     * 
     * @param topicName
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
    public CreateRuleResult createRule(String topicName, String subscriptionName, Rule rule) throws ServiceException {
        return next.createRule(topicName, subscriptionName, rule);
    }

    /**
     * Deletes a rule.
     * 
     * @param topicName
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
    public void deleteRule(String topicName, String subscriptionName, String ruleName) throws ServiceException {
        next.deleteRule(topicName, subscriptionName, ruleName);
    }

    /**
     * Retrieves a rule.
     * 
     * @param topicName
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
    public GetRuleResult getRule(String topicName, String subscriptionName, String ruleName) throws ServiceException {
        return next.getRule(topicName, subscriptionName, ruleName);
    }

    /**
     * Returns a list of rules.
     * 
     * @param topicName
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
    public ListRulesResult listRules(String topicName, String subscriptionName) throws ServiceException {
        return next.listRules(topicName, subscriptionName);
    }

    public ListQueuesResult listQueues(ListQueuesOptions options) throws ServiceException {
        return next.listQueues(options);
    }

    public ListTopicsResult listTopics(ListTopicsOptions options) throws ServiceException {
        return next.listTopics(options);
    }

    public ListSubscriptionsResult listSubscriptions(String topicName, ListSubscriptionsOptions options)
            throws ServiceException {
        return next.listSubscriptions(topicName, options);
    }

    public ListRulesResult listRules(String topicName, String subscriptionName, ListRulesOptions options)
            throws ServiceException {
        return next.listRules(topicName, subscriptionName, options);
    }
}
