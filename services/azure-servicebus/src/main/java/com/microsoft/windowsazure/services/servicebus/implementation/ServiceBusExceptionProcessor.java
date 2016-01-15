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
package com.microsoft.windowsazure.services.servicebus.implementation;

import com.microsoft.windowsazure.core.pipeline.filter.ServiceRequestFilter;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceResponseFilter;
import com.microsoft.windowsazure.core.pipeline.jersey.ServiceFilter;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.exception.ServiceExceptionFactory;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.microsoft.windowsazure.services.servicebus.ServiceBusContract;
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
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;

public class ServiceBusExceptionProcessor implements ServiceBusContract {

    private final ServiceBusContract next;
    private static Log log = LogFactory.getLog(ServiceBusContract.class);

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

    @Override
    public ServiceBusContract withRequestFilterFirst(
            ServiceRequestFilter serviceRequestFilter) {
        return new ServiceBusExceptionProcessor(
                next.withRequestFilterFirst(serviceRequestFilter));
    }

    @Override
    public ServiceBusContract withRequestFilterLast(
            ServiceRequestFilter serviceRequestFilter) {
        return new ServiceBusExceptionProcessor(
                next.withRequestFilterLast(serviceRequestFilter));
    }

    @Override
    public ServiceBusContract withResponseFilterFirst(
            ServiceResponseFilter serviceResponseFilter) {
        return new ServiceBusExceptionProcessor(
                next.withResponseFilterFirst(serviceResponseFilter));
    }

    @Override
    public ServiceBusContract withResponseFilterLast(
            ServiceResponseFilter serviceResponseFilter) {
        return new ServiceBusExceptionProcessor(
                next.withResponseFilterLast(serviceResponseFilter));
    }

    private ServiceException processCatch(ServiceException e) {
        log.warn(e.getMessage(), e.getCause());
        return ServiceExceptionFactory.process("serviceBus", e);
    }

    @Override
    public void sendQueueMessage(String path, BrokeredMessage message)
            throws ServiceException {
        try {
            next.sendQueueMessage(path, message);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public ReceiveQueueMessageResult receiveQueueMessage(String queueName)
            throws ServiceException {
        try {
            return next.receiveQueueMessage(queueName);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public ReceiveQueueMessageResult receiveQueueMessage(String queueName,
            ReceiveMessageOptions options) throws ServiceException {
        try {
            return next.receiveQueueMessage(queueName, options);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public void sendTopicMessage(String path, BrokeredMessage message)
            throws ServiceException {
        try {
            next.sendTopicMessage(path, message);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public ReceiveSubscriptionMessageResult receiveSubscriptionMessage(
            String topicName, String subscriptionName) throws ServiceException {
        try {
            return next.receiveSubscriptionMessage(topicName, subscriptionName);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public ReceiveSubscriptionMessageResult receiveSubscriptionMessage(
            String topicName, String subscriptionName,
            ReceiveMessageOptions options) throws ServiceException {
        try {
            return next.receiveSubscriptionMessage(topicName, subscriptionName,
                    options);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public void unlockMessage(BrokeredMessage message) throws ServiceException {
        try {
            next.unlockMessage(message);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public void deleteMessage(BrokeredMessage message) throws ServiceException {
        try {
            next.deleteMessage(message);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public CreateQueueResult createQueue(QueueInfo queue)
            throws ServiceException {
        try {
            return next.createQueue(queue);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public void deleteQueue(String queuePath) throws ServiceException {
        try {
            next.deleteQueue(queuePath);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public GetQueueResult getQueue(String queuePath) throws ServiceException {
        try {
            return next.getQueue(queuePath);
        } catch (WebApplicationException e) {
            throw processCatch(new ServiceException(e));
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public ListQueuesResult listQueues() throws ServiceException {
        try {
            return next.listQueues();
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public QueueInfo updateQueue(QueueInfo queueInfo) throws ServiceException {
        try {
            return next.updateQueue(queueInfo);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public CreateEventHubResult createEventHub(EventHubInfo eventHub)
            throws ServiceException {
        try {
            return next.createEventHub(eventHub);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public void deleteEventHub(String eventHubPath) throws ServiceException {
        try {
            next.deleteEventHub(eventHubPath);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public GetEventHubResult getEventHub(String eventHubPath) throws ServiceException {
        try {
            return next.getEventHub(eventHubPath);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public ListEventHubsResult listEventHubs() throws ServiceException {
        try {
            return next.listEventHubs();
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public ListEventHubsResult listEventHubs(ListEventHubsOptions options)
            throws ServiceException {
        try {
            return next.listEventHubs(options);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public CreateTopicResult createTopic(TopicInfo topic)
            throws ServiceException {
        try {
            return next.createTopic(topic);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public void deleteTopic(String topicPath) throws ServiceException {
        try {
            next.deleteTopic(topicPath);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public GetTopicResult getTopic(String topicPath) throws ServiceException {
        try {
            return next.getTopic(topicPath);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public ListTopicsResult listTopics() throws ServiceException {
        try {
            return next.listTopics();
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public TopicInfo updateTopic(TopicInfo topicInfo) throws ServiceException {
        try {
            return next.updateTopic(topicInfo);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public CreateSubscriptionResult createSubscription(String topicPath,
            SubscriptionInfo subscription) throws ServiceException {
        try {
            return next.createSubscription(topicPath, subscription);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public void deleteSubscription(String topicPath, String subscriptionName)
            throws ServiceException {
        try {
            next.deleteSubscription(topicPath, subscriptionName);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public GetSubscriptionResult getSubscription(String topicPath,
            String subscriptionName) throws ServiceException {
        try {
            return next.getSubscription(topicPath, subscriptionName);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public ListSubscriptionsResult listSubscriptions(String topicPath)
            throws ServiceException {
        try {
            return next.listSubscriptions(topicPath);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public SubscriptionInfo updateSubscription(String topicName,
            SubscriptionInfo subscriptionInfo) throws ServiceException {
        try {
            return next.updateSubscription(topicName, subscriptionInfo);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public CreateRuleResult createRule(String topicPath,
            String subscriptionName, RuleInfo rule) throws ServiceException {
        try {
            return next.createRule(topicPath, subscriptionName, rule);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public void deleteRule(String topicPath, String subscriptionName,
            String ruleName) throws ServiceException {
        try {
            next.deleteRule(topicPath, subscriptionName, ruleName);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public GetRuleResult getRule(String topicPath, String subscriptionName,
            String ruleName) throws ServiceException {
        try {
            return next.getRule(topicPath, subscriptionName, ruleName);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public ListRulesResult listRules(String topicPath, String subscriptionName)
            throws ServiceException {
        try {
            return next.listRules(topicPath, subscriptionName);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public ListQueuesResult listQueues(ListQueuesOptions options)
            throws ServiceException {
        try {
            return next.listQueues(options);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public ListTopicsResult listTopics(ListTopicsOptions options)
            throws ServiceException {
        try {
            return next.listTopics(options);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public ListSubscriptionsResult listSubscriptions(String topicName,
            ListSubscriptionsOptions options) throws ServiceException {
        try {
            return next.listSubscriptions(topicName, options);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public ListRulesResult listRules(String topicName, String subscriptionName,
            ListRulesOptions options) throws ServiceException {
        try {
            return next.listRules(topicName, subscriptionName, options);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public void sendMessage(String path, BrokeredMessage message)
            throws ServiceException {
        try {
            next.sendMessage(path, message);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public ReceiveMessageResult receiveMessage(String path)
            throws ServiceException {
        try {
            return next.receiveMessage(path);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public ReceiveMessageResult receiveMessage(String path,
            ReceiveMessageOptions options) throws ServiceException {
        try {
            return next.receiveMessage(path, options);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public void renewQueueLock(String queueName, String messageId,
            String lockToken) throws ServiceException {
        try {
            next.renewQueueLock(queueName, messageId, lockToken);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public void renewSubscriptionLock(String topicName,
            String subscriptionName, String messageId, String lockToken)
            throws ServiceException {
        try {
            next.renewSubscriptionLock(topicName, subscriptionName, messageId,
                    lockToken);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

}
