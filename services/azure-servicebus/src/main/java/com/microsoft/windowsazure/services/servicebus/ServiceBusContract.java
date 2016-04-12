/**
 * Copyright Microsoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.services.servicebus;

import com.microsoft.windowsazure.core.pipeline.jersey.JerseyFilterableService;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.servicebus.models.BrokeredMessage;
import com.microsoft.windowsazure.services.servicebus.models.CreateEventHubResult;
import com.microsoft.windowsazure.services.servicebus.models.CreateQueueResult;
import com.microsoft.windowsazure.services.servicebus.models.CreateRuleResult;
import com.microsoft.windowsazure.services.servicebus.models.CreateSubscriptionResult;
import com.microsoft.windowsazure.services.servicebus.models.CreateTopicResult;
import com.microsoft.windowsazure.services.servicebus.models.EventHubInfo;
import com.microsoft.windowsazure.services.servicebus.models.GetEventHubResult;
import com.microsoft.windowsazure.services.servicebus.models.GetQueueResult;
import com.microsoft.windowsazure.services.servicebus.models.GetRuleResult;
import com.microsoft.windowsazure.services.servicebus.models.GetSubscriptionResult;
import com.microsoft.windowsazure.services.servicebus.models.GetTopicResult;
import com.microsoft.windowsazure.services.servicebus.models.ListEventHubsOptions;
import com.microsoft.windowsazure.services.servicebus.models.ListEventHubsResult;
import com.microsoft.windowsazure.services.servicebus.models.ListQueuesOptions;
import com.microsoft.windowsazure.services.servicebus.models.ListQueuesResult;
import com.microsoft.windowsazure.services.servicebus.models.ListRulesOptions;
import com.microsoft.windowsazure.services.servicebus.models.ListRulesResult;
import com.microsoft.windowsazure.services.servicebus.models.ListSubscriptionsOptions;
import com.microsoft.windowsazure.services.servicebus.models.ListSubscriptionsResult;
import com.microsoft.windowsazure.services.servicebus.models.ListTopicsOptions;
import com.microsoft.windowsazure.services.servicebus.models.ListTopicsResult;
import com.microsoft.windowsazure.services.servicebus.models.QueueInfo;
import com.microsoft.windowsazure.services.servicebus.models.ReceiveMessageOptions;
import com.microsoft.windowsazure.services.servicebus.models.ReceiveMessageResult;
import com.microsoft.windowsazure.services.servicebus.models.ReceiveQueueMessageResult;
import com.microsoft.windowsazure.services.servicebus.models.ReceiveSubscriptionMessageResult;
import com.microsoft.windowsazure.services.servicebus.models.RuleInfo;
import com.microsoft.windowsazure.services.servicebus.models.SubscriptionInfo;
import com.microsoft.windowsazure.services.servicebus.models.TopicInfo;

/**
 *
 * Defines the service bus contract.
 *
 */
public interface ServiceBusContract extends
        JerseyFilterableService<ServiceBusContract> {

    /**
     * Sends a queue message.
     *
     * @param queuePath
     *            A <code>String</code> object that represents the name of the
     *            queue to which the message will be sent.
     * @param message
     *            A <code>Message</code> object that represents the message to
     *            send.
     * @throws ServiceException
     *             If a service exception is encountered.
     */
    void sendQueueMessage(String queuePath, BrokeredMessage message)
            throws ServiceException;

    /**
     * Receives a queue message.
     *
     * @param queuePath
     *            A <code>String</code> object that represents the name of the
     *            queue from which to receive the message.
     * @return A <code>ReceiveQueueMessageResult</code> object that represents
     *         the result.
     * @throws ServiceException
     *             If a service exception is encountered.
     */
    ReceiveQueueMessageResult receiveQueueMessage(String queuePath)
            throws ServiceException;

    /**
     * Receives a queue message using the specified receive message options.
     *
     * @param queuePath
     *            A <code>String</code> object that represents the name of the
     *            queue from which to receive the message.
     * @param options
     *            A <code>ReceiveMessageOptions</code> object that represents
     *            the receive message options.
     * @return A <code>ReceiveQueueMessageResult</code> object that represents
     *         the result.
     * @throws ServiceException
     *             If a service exception is encountered.
     */
    ReceiveQueueMessageResult receiveQueueMessage(String queuePath,
            ReceiveMessageOptions options) throws ServiceException;

    /**
     * Sends a topic message.
     *
     * @param topicPath
     *            A <code>String</code> object that represents the name of the
     *            topic to which the message will be sent.
     * @param message
     *            A <code>Message</code> object that represents the message to
     *            send.
     * @throws ServiceException
     *             If a service exception is encountered.
     */
    void sendTopicMessage(String topicPath, BrokeredMessage message)
            throws ServiceException;

    /**
     * Receives a subscription message.
     *
     * @param topicPath
     *            A <code>String</code> object that represents the name of the
     *            topic to receive.
     * @param subscriptionName
     *            A <code>String</code> object that represents the name of the
     *            subscription from the message will be received.
     * @return A <code>ReceiveSubscriptionMessageResult</code> object that
     *         represents the result.
     * @throws ServiceException
     *             If a service exception is encountered.
     */
    ReceiveSubscriptionMessageResult receiveSubscriptionMessage(
            String topicPath, String subscriptionName) throws ServiceException;

    /**
     * Receives a subscription message using the specified receive message
     * options.
     *
     * @param topicPath
     *            A <code>String</code> object that represents the name of the
     *            topic to receive.
     * @param subscriptionName
     *            A <code>String</code> object that represents the name of the
     *            subscription from the message will be received.
     * @param options
     *            A <code>ReceiveMessageOptions</code> object that represents
     *            the receive message options.
     * @return A <code>ReceiveSubscriptionMessageResult</code> object that
     *         represents the result.
     * @throws ServiceException
     *             If a service exception is encountered.
     */
    ReceiveSubscriptionMessageResult receiveSubscriptionMessage(
            String topicPath, String subscriptionName,
            ReceiveMessageOptions options) throws ServiceException;

    /**
     * Unlocks a message.
     *
     * @param message
     *            A <code>Message</code> object that represents the message to
     *            unlock.
     * @throws ServiceException
     *             If a service exception is encountered.
     */
    void unlockMessage(BrokeredMessage message) throws ServiceException;

    /**
     * Sends a message.
     *
     * @param path
     *            A <code>String</code> object that represents the path to which
     *            the message will be sent. This may be the value of a queuePath
     *            or a topicPath.
     * @param message
     *            A <code>Message</code> object that represents the message to
     *            send.
     *
     * @throws ServiceException
     *             If a service exception is encountered.
     */
    void sendMessage(String path, BrokeredMessage message)
            throws ServiceException;

    /**
     * Receives a message.
     *
     * @param path
     *            A <code>String</code> object that represents the path from
     *            which a message will be received. This may either be the value
     *            of queuePath or a combination of the topicPath +
     *            "/subscriptions/" + subscriptionName.
     * @return A <code>ReceiveSubscriptionMessageResult</code> object that
     *         represents the result.
     * @throws ServiceException
     *             If a service exception is encountered.
     */
    ReceiveMessageResult receiveMessage(String path) throws ServiceException;

    /**
     * Receives a message using the specified receive message options.
     *
     * @param path
     *            A <code>String</code> object that represents the path from
     *            which a message will be received. This may either be the value
     *            of queuePath or a combination of the topicPath +
     *            "/subscriptions/" + subscriptionName.
     * @param options
     *            A <code>ReceiveMessageOptions</code> object that represents
     *            the receive message options.
     * @return A <code>ReceiveSubscriptionMessageResult</code> object that
     *         represents the result.
     * @throws ServiceException
     *             If a service exception is encountered.
     */
    ReceiveMessageResult receiveMessage(String path,
            ReceiveMessageOptions options) throws ServiceException;

    /**
     * Deletes a message.
     *
     * @param message
     *            A <code>Message</code> object that represents the message to
     *            delete.
     * @throws ServiceException
     *             If a service exception is encountered.
     */
    void deleteMessage(BrokeredMessage message) throws ServiceException;

    /**
     * Creates a queue.
     *
     * @param queueInfo
     *            A <code>QueueInfo</code> object that represents the queue to
     *            create.
     * @return A <code>CreateQueueResult</code> object that represents the
     *         result.
     * @throws ServiceException
     *             If a service exception is encountered.
     */
    CreateQueueResult createQueue(QueueInfo queueInfo) throws ServiceException;

    /**
     * Deletes a queue.
     *
     * @param queuePath
     *            A <code>String</code> object that represents the name of the
     *            queue to delete.
     * @throws ServiceException
     *             If a service exception is encountered.
     */
    void deleteQueue(String queuePath) throws ServiceException;

    /**
     * Retrieves a queue.
     *
     * @param queuePath
     *            A <code>String</code> object that represents the name of the
     *            queue to retrieve.
     * @return A <code>GetQueueResult</code> object that represents the result.
     * @throws ServiceException
     *             If a service exception is encountered.
     */
    GetQueueResult getQueue(String queuePath) throws ServiceException;

    /**
     * Returns a list of queues.
     *
     * @return A <code>ListQueuesResult</code> object that represents the
     *         result.
     * @throws ServiceException
     *             If a service exception is encountered.
     */
    ListQueuesResult listQueues() throws ServiceException;

    /**
     * Returns a list of queues.
     *
     * @param options
     *            A <code>ListQueueOptions</code> object that represents the
     *            options to list the queue.
     * @return A <code>ListQueuesResult</code> object that represents the
     *         result.
     * @throws ServiceException
     *             If a service exception is encountered.
     */
    ListQueuesResult listQueues(ListQueuesOptions options)
            throws ServiceException;

    /**
     * Updates the information of a queue.
     *
     * @param queueInfo
     *            The information of a queue to be updated.
     *
     * @return A <code>QueueInfo</code> object that represents the updated
     *         queue.
     *
     * @throws ServiceException
     *             If a service exception is encountered.
     */
    QueueInfo updateQueue(QueueInfo queueInfo) throws ServiceException;

    /**
     * Creates an event hub.
     *
     * @param eventHub
     *            A <code>EventHub</code> object that represents the event hub to
     *            create.
     * @return A <code>CreateEventHubResult</code> object that represents the
     *         result.
     * @throws ServiceException
     *             If a service exception is encountered.
     */
    CreateEventHubResult createEventHub(EventHubInfo eventHub) throws ServiceException;

    /**
     * Deletes an event hub.
     *
     * @param eventHubPath
     *            A <code>String</code> object that represents the name of the
     *            event hub to delete.
     * @throws ServiceException
     *             If a service exception is encountered.
     */
    void deleteEventHub(String eventHubPath) throws ServiceException;

    /**
     * Retrieves an event hub.
     *
     * @param eventHubPath
     *            A <code>String</code> object that represents the name of the
     *            event hub to retrieve.
     * @return A <code>GetEventHubResult</code> object that represents the result.
     * @throws ServiceException
     *             If a service exception is encountered.
     */
    GetEventHubResult getEventHub(String eventHubPath) throws ServiceException;

    /**
     * Returns a list of event hubs.
     *
     * @return A <code>ListEventHubsResult</code> object that represents the
     *         result.
     * @throws ServiceException
     *             If a service exception is encountered.
     */
    ListEventHubsResult listEventHubs() throws ServiceException;

    /**
     * Returns a list of event hubs.
     *
     * @param options
     *            A <code>ListEventHubsOptions</code> object that represents the
     *            options to list the topic.
     * @return A <code>ListEventHubsOptions</code> object that represents the
     *         result.
     * @throws ServiceException
     *             If a service exception is encountered.
     */
    ListEventHubsResult listEventHubs(ListEventHubsOptions options)
            throws ServiceException;


    /**
     * Creates a topic.
     *
     * @param topic
     *            A <code>Topic</code> object that represents the topic to
     *            create.
     * @return A <code>CreateTopicResult</code> object that represents the
     *         result.
     * @throws ServiceException
     *             If a service exception is encountered.
     */
    CreateTopicResult createTopic(TopicInfo topic) throws ServiceException;

    /**
     * Deletes a topic.
     *
     * @param topicPath
     *            A <code>String</code> object that represents the name of the
     *            queue to delete.
     * @throws ServiceException
     *             If a service exception is encountered.
     */
    void deleteTopic(String topicPath) throws ServiceException;

    /**
     * Retrieves a topic.
     *
     * @param topicPath
     *            A <code>String</code> object that represents the name of the
     *            topic to retrieve.
     * @return A <code>GetTopicResult</code> object that represents the result.
     * @throws ServiceException
     *             If a service exception is encountered.
     */
    GetTopicResult getTopic(String topicPath) throws ServiceException;

    /**
     * Returns a list of topics.
     *
     * @return A <code>ListTopicsResult</code> object that represents the
     *         result.
     * @throws ServiceException
     *             If a service exception is encountered.
     */
    ListTopicsResult listTopics() throws ServiceException;

    /**
     * Returns a list of topics.
     *
     * @param options
     *            A <code>ListTopicsOptions</code> object that represents the
     *            options to list the topic.
     * @return A <code>ListTopicsResult</code> object that represents the
     *         result.
     * @throws ServiceException
     *             If a service exception is encountered.
     */
    ListTopicsResult listTopics(ListTopicsOptions options)
            throws ServiceException;

    /**
     * Updates a topic.
     *
     * @param topicInfo
     *            A <code>TopicInfo</code> object that represents the topic to
     *            be updated.
     *
     * @return A <code>TopicInfo</code> object that represents the update topic
     *         result.
     * @throws ServiceException
     *             If a service exception is encountered.
     */
    TopicInfo updateTopic(TopicInfo topicInfo) throws ServiceException;

    /**
     * Creates a subscription.
     *
     * @param topicPath
     *            A <code>String</code> object that represents the name of the
     *            topic for the subscription.
     * @param subscription
     *            A <code>Subscription</code> object that represents the
     *            subscription to create.
     * @return A <code>CreateSubscriptionResult</code> object that represents
     *         the result.
     * @throws ServiceException
     *             If a service exception is encountered.
     */
    CreateSubscriptionResult createSubscription(String topicPath,
            SubscriptionInfo subscription) throws ServiceException;

    /**
     * Deletes a subscription.
     *
     * @param topicPath
     *            A <code>String</code> object that represents the name of the
     *            topic for the subscription.
     * @param subscriptionName
     *            A <code>String</code> object that represents the name of the
     *            subscription to delete.
     * @throws ServiceException
     *             If a service exception is encountered.
     */
    void deleteSubscription(String topicPath, String subscriptionName)
            throws ServiceException;

    /**
     * Retrieves a subscription.
     *
     * @param topicPath
     *            A <code>String</code> object that represents the name of the
     *            topic for the subscription.
     * @param subscriptionName
     *            A <code>String</code> object that represents the name of the
     *            subscription to retrieve.
     * @return A <code>GetSubscriptionResult</code> object that represents the
     *         result. A <code>String</code> object that represents the name of
     *         the subscription to retrieve.
     * @throws ServiceException
     *             If a service exception is encountered.
     */
    GetSubscriptionResult getSubscription(String topicPath,
            String subscriptionName) throws ServiceException;

    /**
     * Returns a list of subscriptions.
     *
     * @param topicPath
     *            A <code>String</code> object that represents the name of the
     *            topic for the subscriptions to retrieve.
     * @return A <code>ListSubscriptionsResult</code> object that represents the
     *         result.
     * @throws ServiceException
     *             If a service exception is encountered.
     */
    ListSubscriptionsResult listSubscriptions(String topicPath)
            throws ServiceException;

    /**
     * Returns a list of subscriptions.
     *
     * @param topicPath
     *            A <code>String</code> object that represents the name of the
     *            topic for the subscriptions to retrieve.
     *
     * @param options
     *            A <code>ListSubscriptionsOptions</code> object that represents
     *            the options to list subscriptions.
     *
     * @return A <code>ListSubscriptionsResult</code> object that represents the
     *         result.
     *
     * @throws ServiceException
     *             the service exception
     */
    ListSubscriptionsResult listSubscriptions(String topicPath,
            ListSubscriptionsOptions options) throws ServiceException;

    /**
     * Updates a subscription.
     *
     * @param topicName
     *            A <code>String</code> option which represents the name of the
     *            topic.
     * @param subscriptionInfo
     *            A <code>SubscriptionInfo</code> option which represents the
     *            information of the subscription.
     * @return A <code>SubscriptionInfo</code> object that represents the
     *         result.
     * @throws ServiceException
     *             If a service exception is encountered.
     */
    SubscriptionInfo updateSubscription(String topicName,
            SubscriptionInfo subscriptionInfo) throws ServiceException;

    /**
     * Creates a rule.
     *
     * @param topicPath
     *            A <code>String</code> object that represents the name of the
     *            topic for the subscription.
     * @param subscriptionName
     *            A <code>String</code> object that represents the name of the
     *            subscription for which the rule will be created.
     * @param rule
     *            A <code>Rule</code> object that represents the rule to create.
     * @return A <code>CreateRuleResult</code> object that represents the
     *         result.
     * @throws ServiceException
     *             If a service exception is encountered.
     */
    CreateRuleResult createRule(String topicPath, String subscriptionName,
            RuleInfo rule) throws ServiceException;

    /**
     * Deletes a rule.
     *
     * @param topicPath
     *            A <code>String</code> object that represents the name of the
     *            topic for the subscription.
     * @param subscriptionName
     *            A <code>String</code> object that represents the name of the
     *            subscription for which the rule will be deleted.
     * @param ruleName
     *            A <code>String</code> object that represents the name of the
     *            rule to delete.
     * @throws ServiceException
     *             If a service exception is encountered.
     */
    void deleteRule(String topicPath, String subscriptionName, String ruleName)
            throws ServiceException;

    /**
     * Retrieves a rule.
     *
     * @param topicPath
     *            A <code>String</code> object that represents the name of the
     *            topic for the subscription.
     * @param subscriptionName
     *            A <code>String</code> object that represents the name of the
     *            subscription for which the rule will be retrieved.
     * @param ruleName
     *            A <code>String</code> object that represents the name of the
     *            rule to retrieve.
     * @return A <code>GetRuleResult</code> object that represents the result.
     * @throws ServiceException
     *             If a service exception is encountered.
     */
    GetRuleResult getRule(String topicPath, String subscriptionName,
            String ruleName) throws ServiceException;

    /**
     * Returns a list of rules.
     *
     * @param topicPath
     *            A <code>String</code> object that represents the name of the
     *            topic for the subscription.
     * @param subscriptionName
     *            A <code>String</code> object that represents the name of the
     *            subscription whose rules are being retrieved.
     * @return A <code>ListRulesResult</code> object that represents the result.
     * @throws ServiceException
     *             If a service exception is encountered.
     */
    ListRulesResult listRules(String topicPath, String subscriptionName)
            throws ServiceException;

    /**
     * Returns a list of rules.
     *
     * @param topicPath
     *            A <code>String</code> object that represents the name of the
     *            topic for the subscription.
     * @param subscriptionName
     *            A <code>String</code> object that represents the name of the
     *            subscription whose rules are being retrieved.
     * @param options
     *            A <code>ListRulesOptions</code> object that represents the
     *            options to retrieve rules.
     * @return the list rules result
     * @throws ServiceException
     *             If a service exception is encountered.
     */
    ListRulesResult listRules(String topicPath, String subscriptionName,
            ListRulesOptions options) throws ServiceException;

    /**
     * Renew queue lock.
     *
     * @param queueName
     *            A <code>String</code> object that represents the name of the
     *            queue.
     * @param messageId
     *            A <code>String</code> object that represents the ID of the
     *            message.
     * @param lockToken
     *            A <code>String</code> object that represents the token of the
     *            lock.
     * @throws ServiceException
     *             If a service exception is encountered.
     */
    void renewQueueLock(String queueName, String messageId, String lockToken)
            throws ServiceException;

    /**
     * Renew subscription lock.
     *
     * @param topicName
     *            A <code>String</code> object that represents the name of the
     *            topic.
     * @param queueName
     *            A <code>String</code> object that represents the name of the
     *            queue.
     * @param messageId
     *            A <code>String</code> object that represents the ID of the
     *            message.
     * @param lockToken
     *            A <code>String</code> object that represents the token of the
     *            lock.
     * @throws ServiceException
     *             If a service exception is encountered.
     */
    void renewSubscriptionLock(String topicName, String subscriptionName,
            String messageId, String lockToken) throws ServiceException;


}
