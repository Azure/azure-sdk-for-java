/**
 * Copyright 2011 Microsoft Corporation
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
package com.microsoft.windowsazure.services.serviceBus.implementation;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.core.ServiceFilter;
import com.microsoft.windowsazure.services.core.utils.ServiceExceptionFactory;
import com.microsoft.windowsazure.services.serviceBus.ServiceBusContract;
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

    @Override
    public ServiceBusContract withFilter(ServiceFilter filter) {
        return new ServiceBusExceptionProcessor(next.withFilter(filter));
    }

    private ServiceException processCatch(ServiceException e) {
        log.warn(e.getMessage(), e.getCause());
        return ServiceExceptionFactory.process("serviceBus", e);
    }

    @Override
    public void sendQueueMessage(String path, BrokeredMessage message) throws ServiceException {
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

    @Override
    public ReceiveQueueMessageResult receiveQueueMessage(String queueName) throws ServiceException {
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

    @Override
    public ReceiveQueueMessageResult receiveQueueMessage(String queueName, ReceiveMessageOptions options)
            throws ServiceException {
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

    @Override
    public void sendTopicMessage(String path, BrokeredMessage message) throws ServiceException {
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

    @Override
    public ReceiveSubscriptionMessageResult receiveSubscriptionMessage(String topicName, String subscriptionName)
            throws ServiceException {
        try {
            return next.receiveSubscriptionMessage(topicName, subscriptionName);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public ReceiveSubscriptionMessageResult receiveSubscriptionMessage(String topicName, String subscriptionName,
            ReceiveMessageOptions options) throws ServiceException {
        try {
            return next.receiveSubscriptionMessage(topicName, subscriptionName, options);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public void unlockMessage(BrokeredMessage message) throws ServiceException {
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

    @Override
    public void deleteMessage(BrokeredMessage message) throws ServiceException {
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

    @Override
    public CreateQueueResult createQueue(QueueInfo queue) throws ServiceException {
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

    @Override
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

    @Override
    public GetQueueResult getQueue(String queuePath) throws ServiceException {
        try {
            return next.getQueue(queuePath);
        }
        catch (WebApplicationException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
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

    @Override
    public CreateTopicResult createTopic(TopicInfo topic) throws ServiceException {
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

    @Override
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

    @Override
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

    @Override
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

    @Override
    public CreateSubscriptionResult createSubscription(String topicPath, SubscriptionInfo subscription)
            throws ServiceException {
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

    @Override
    public void deleteSubscription(String topicPath, String subscriptionName) throws ServiceException {
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

    @Override
    public GetSubscriptionResult getSubscription(String topicPath, String subscriptionName) throws ServiceException {
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

    @Override
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

    @Override
    public CreateRuleResult createRule(String topicPath, String subscriptionName, RuleInfo rule)
            throws ServiceException {
        try {
            return next.createRule(topicPath, subscriptionName, rule);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public void deleteRule(String topicPath, String subscriptionName, String ruleName) throws ServiceException {
        try {
            next.deleteRule(topicPath, subscriptionName, ruleName);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public GetRuleResult getRule(String topicPath, String subscriptionName, String ruleName) throws ServiceException {
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

    @Override
    public ListRulesResult listRules(String topicPath, String subscriptionName) throws ServiceException {
        try {
            return next.listRules(topicPath, subscriptionName);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public ListQueuesResult listQueues(ListQueuesOptions options) throws ServiceException {
        try {
            return next.listQueues(options);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public ListTopicsResult listTopics(ListTopicsOptions options) throws ServiceException {
        try {
            return next.listTopics(options);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public ListSubscriptionsResult listSubscriptions(String topicName, ListSubscriptionsOptions options)
            throws ServiceException {
        try {
            return next.listSubscriptions(topicName, options);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public ListRulesResult listRules(String topicName, String subscriptionName, ListRulesOptions options)
            throws ServiceException {
        try {
            return next.listRules(topicName, subscriptionName, options);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public void sendMessage(String path, BrokeredMessage message) throws ServiceException {
        try {
            next.sendMessage(path, message);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public ReceiveMessageResult receiveMessage(String path) throws ServiceException {
        try {
            return next.receiveMessage(path);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public ReceiveMessageResult receiveMessage(String path, ReceiveMessageOptions options) throws ServiceException {
        try {
            return next.receiveMessage(path, options);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

}
