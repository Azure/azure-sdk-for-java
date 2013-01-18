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
package com.microsoft.windowsazure.services.serviceBus.implementation;

import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.MediaType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.core.ServiceFilter;
import com.microsoft.windowsazure.services.core.utils.pipeline.ClientFilterAdapter;
import com.microsoft.windowsazure.services.serviceBus.ServiceBusContract;
import com.microsoft.windowsazure.services.serviceBus.models.AbstractListOptions;
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
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;

public class ServiceBusRestProxy implements ServiceBusContract {

    private Client channel;
    private final String uri;
    private final BrokerPropertiesMapper mapper;
    private final CustomPropertiesMapper customPropertiesMapper;
    static Log log = LogFactory.getLog(ServiceBusContract.class);

    ServiceFilter[] filters;

    @Inject
    public ServiceBusRestProxy(Client channel, @Named("serviceBus") WrapFilter authFilter,
            @Named("serviceBus.uri") String uri, BrokerPropertiesMapper mapper) {

        this.channel = channel;
        this.filters = new ServiceFilter[0];
        this.uri = uri;
        this.mapper = mapper;
        this.customPropertiesMapper = new CustomPropertiesMapper();
        channel.addFilter(authFilter);
    }

    public ServiceBusRestProxy(Client channel, ServiceFilter[] filters, String uri, BrokerPropertiesMapper mapper) {
        this.channel = channel;
        this.filters = filters;
        this.uri = uri;
        this.mapper = mapper;
        this.customPropertiesMapper = new CustomPropertiesMapper();
    }

    @Override
    public ServiceBusContract withFilter(ServiceFilter filter) {
        ServiceFilter[] newFilters = Arrays.copyOf(filters, filters.length + 1);
        newFilters[filters.length] = filter;
        return new ServiceBusRestProxy(channel, newFilters, uri, mapper);
    }

    public Client getChannel() {
        return channel;
    }

    public void setChannel(Client channel) {
        this.channel = channel;
    }

    private WebResource getResource() {
        WebResource resource = getChannel().resource(uri);
        for (ServiceFilter filter : filters) {
            resource.addFilter(new ClientFilterAdapter(filter));
        }
        return resource;
    }

    @Override
    public void sendMessage(String path, BrokeredMessage message) {
        Builder request = getResource().path(path).path("messages").getRequestBuilder();

        if (message.getContentType() != null)
            request = request.type(message.getContentType());

        if (message.getBrokerProperties() != null)
            request = request.header("BrokerProperties", mapper.toString(message.getBrokerProperties()));

        for (java.util.Map.Entry<String, Object> entry : message.getProperties().entrySet()) {
            request.header(entry.getKey(), customPropertiesMapper.toString(entry.getValue()));
        }

        request.post(message.getBody());
    }

    @Override
    public void sendQueueMessage(String path, BrokeredMessage message) throws ServiceException {
        sendMessage(path, message);
    }

    @Override
    public ReceiveQueueMessageResult receiveQueueMessage(String queueName) throws ServiceException {
        return receiveQueueMessage(queueName, ReceiveMessageOptions.DEFAULT);
    }

    @Override
    public ReceiveQueueMessageResult receiveQueueMessage(String queuePath, ReceiveMessageOptions options)
            throws ServiceException {

        WebResource resource = getResource().path(queuePath).path("messages").path("head");

        BrokeredMessage message = receiveMessage(options, resource);
        return new ReceiveQueueMessageResult(message);
    }

    @Override
    public ReceiveMessageResult receiveMessage(String path) throws ServiceException {
        return receiveMessage(path, ReceiveMessageOptions.DEFAULT);
    }

    @Override
    public ReceiveMessageResult receiveMessage(String path, ReceiveMessageOptions options) throws ServiceException {

        WebResource resource = getResource().path(path).path("messages").path("head");

        BrokeredMessage message = receiveMessage(options, resource);
        return new ReceiveMessageResult(message);
    }

    private BrokeredMessage receiveMessage(ReceiveMessageOptions options, WebResource resource) {
        if (options.getTimeout() != null) {
            resource = resource.queryParam("timeout", Integer.toString(options.getTimeout()));
        }

        ClientResponse clientResult;
        if (options.isReceiveAndDelete()) {
            clientResult = resource.delete(ClientResponse.class);
        }
        else if (options.isPeekLock()) {
            clientResult = resource.post(ClientResponse.class, "");
        }
        else {
            throw new RuntimeException("Unknown ReceiveMode");
        }

        BrokerProperties brokerProperties;
        if (clientResult.getHeaders().containsKey("BrokerProperties")) {
            brokerProperties = mapper.fromString(clientResult.getHeaders().getFirst("BrokerProperties"));
        }
        else {
            brokerProperties = new BrokerProperties();
        }

        String location = clientResult.getHeaders().getFirst("Location");
        if (location != null) {
            brokerProperties.setLockLocation(location);
        }

        BrokeredMessage message = new BrokeredMessage(brokerProperties);

        MediaType contentType = clientResult.getType();
        if (contentType != null) {
            message.setContentType(contentType.toString());
        }

        Date date = clientResult.getResponseDate();
        if (date != null) {
            message.setDate(date);
        }

        InputStream body = clientResult.getEntityInputStream();
        if (body != null) {
            message.setBody(body);
        }

        for (String key : clientResult.getHeaders().keySet()) {
            Object value = clientResult.getHeaders().getFirst(key);
            try {
                value = customPropertiesMapper.fromString(value.toString());
                message.setProperty(key, value);
            }
            catch (ParseException e) {
                //log.warn("Unable to parse custom header", e);
            }
            catch (NumberFormatException e) {
                //log.warn("Unable to parse custom header", e);
            }
        }

        return message;
    }

    @Override
    public void sendTopicMessage(String topicName, BrokeredMessage message) throws ServiceException {
        sendMessage(topicName, message);
    }

    @Override
    public ReceiveSubscriptionMessageResult receiveSubscriptionMessage(String topicName, String subscriptionName)
            throws ServiceException {
        return receiveSubscriptionMessage(topicName, subscriptionName, ReceiveMessageOptions.DEFAULT);
    }

    @Override
    public ReceiveSubscriptionMessageResult receiveSubscriptionMessage(String topicName, String subscriptionName,
            ReceiveMessageOptions options) throws ServiceException {
        WebResource resource = getResource().path(topicName).path("subscriptions").path(subscriptionName)
                .path("messages").path("head");

        BrokeredMessage message = receiveMessage(options, resource);
        return new ReceiveSubscriptionMessageResult(message);
    }

    @Override
    public void unlockMessage(BrokeredMessage message) throws ServiceException {
        getChannel().resource(message.getLockLocation()).put("");
    }

    @Override
    public void deleteMessage(BrokeredMessage message) throws ServiceException {
        getChannel().resource(message.getLockLocation()).delete();
    }

    @Override
    public CreateQueueResult createQueue(QueueInfo entry) throws ServiceException {
        return new CreateQueueResult(getResource().path(entry.getPath())
                .type("application/atom+xml;type=entry;charset=utf-8").put(QueueInfo.class, entry));
    }

    @Override
    public void deleteQueue(String queuePath) throws ServiceException {
        getResource().path(queuePath).delete();
    }

    @Override
    public GetQueueResult getQueue(String queuePath) throws ServiceException {
        return new GetQueueResult(getResource().path(queuePath).get(QueueInfo.class));
    }

    @Override
    public ListQueuesResult listQueues(ListQueuesOptions options) throws ServiceException {
        Feed feed = listOptions(options, getResource().path("$Resources/Queues")).get(Feed.class);
        ArrayList<QueueInfo> queues = new ArrayList<QueueInfo>();
        for (Entry entry : feed.getEntries()) {
            queues.add(new QueueInfo(entry));
        }
        ListQueuesResult result = new ListQueuesResult();
        result.setItems(queues);
        return result;
    }

    private WebResource listOptions(AbstractListOptions<?> options, WebResource path) {
        if (options.getTop() != null) {
            path = path.queryParam("$top", options.getTop().toString());
        }
        if (options.getSkip() != null) {
            path = path.queryParam("$skip", options.getSkip().toString());
        }
        return path;
    }

    @Override
    public CreateTopicResult createTopic(TopicInfo entry) throws ServiceException {
        return new CreateTopicResult(getResource().path(entry.getPath())
                .type("application/atom+xml;type=entry;charset=utf-8").put(TopicInfo.class, entry));
    }

    @Override
    public void deleteTopic(String TopicPath) throws ServiceException {
        getResource().path(TopicPath).delete();
    }

    @Override
    public GetTopicResult getTopic(String TopicPath) throws ServiceException {
        return new GetTopicResult(getResource().path(TopicPath).get(TopicInfo.class));
    }

    @Override
    public ListTopicsResult listTopics(ListTopicsOptions options) throws ServiceException {
        Feed feed = listOptions(options, getResource().path("$Resources/Topics")).get(Feed.class);
        ArrayList<TopicInfo> Topics = new ArrayList<TopicInfo>();
        for (Entry entry : feed.getEntries()) {
            Topics.add(new TopicInfo(entry));
        }
        ListTopicsResult result = new ListTopicsResult();
        result.setItems(Topics);
        return result;
    }

    @Override
    public CreateSubscriptionResult createSubscription(String topicPath, SubscriptionInfo subscription) {
        return new CreateSubscriptionResult(getResource().path(topicPath).path("subscriptions")
                .path(subscription.getName()).type("application/atom+xml;type=entry;charset=utf-8")
                .put(SubscriptionInfo.class, subscription));
    }

    @Override
    public void deleteSubscription(String topicPath, String subscriptionName) {
        getResource().path(topicPath).path("subscriptions").path(subscriptionName).delete();
    }

    @Override
    public GetSubscriptionResult getSubscription(String topicPath, String subscriptionName) {
        return new GetSubscriptionResult(getResource().path(topicPath).path("subscriptions").path(subscriptionName)
                .get(SubscriptionInfo.class));
    }

    @Override
    public ListSubscriptionsResult listSubscriptions(String topicPath, ListSubscriptionsOptions options) {
        Feed feed = listOptions(options, getResource().path(topicPath).path("subscriptions")).get(Feed.class);
        ArrayList<SubscriptionInfo> list = new ArrayList<SubscriptionInfo>();
        for (Entry entry : feed.getEntries()) {
            list.add(new SubscriptionInfo(entry));
        }
        ListSubscriptionsResult result = new ListSubscriptionsResult();
        result.setItems(list);
        return result;
    }

    @Override
    public CreateRuleResult createRule(String topicPath, String subscriptionName, RuleInfo rule) {
        return new CreateRuleResult(getResource().path(topicPath).path("subscriptions").path(subscriptionName)
                .path("rules").path(rule.getName()).type("application/atom+xml;type=entry;charset=utf-8")
                .put(RuleInfo.class, rule));
    }

    @Override
    public void deleteRule(String topicPath, String subscriptionName, String ruleName) {
        getResource().path(topicPath).path("subscriptions").path(subscriptionName).path("rules").path(ruleName)
                .delete();
    }

    @Override
    public GetRuleResult getRule(String topicPath, String subscriptionName, String ruleName) {
        return new GetRuleResult(getResource().path(topicPath).path("subscriptions").path(subscriptionName)
                .path("rules").path(ruleName).get(RuleInfo.class));
    }

    @Override
    public ListRulesResult listRules(String topicPath, String subscriptionName, ListRulesOptions options) {
        Feed feed = listOptions(options,
                getResource().path(topicPath).path("subscriptions").path(subscriptionName).path("rules")).get(
                Feed.class);
        ArrayList<RuleInfo> list = new ArrayList<RuleInfo>();
        for (Entry entry : feed.getEntries()) {
            list.add(new RuleInfo(entry));
        }
        ListRulesResult result = new ListRulesResult();
        result.setItems(list);
        return result;
    }

    @Override
    public ListQueuesResult listQueues() throws ServiceException {
        return listQueues(ListQueuesOptions.DEFAULT);
    }

    @Override
    public ListTopicsResult listTopics() throws ServiceException {
        return listTopics(ListTopicsOptions.DEFAULT);
    }

    @Override
    public ListSubscriptionsResult listSubscriptions(String topicName) throws ServiceException {
        return listSubscriptions(topicName, ListSubscriptionsOptions.DEFAULT);
    }

    @Override
    public ListRulesResult listRules(String topicName, String subscriptionName) throws ServiceException {
        return listRules(topicName, subscriptionName, ListRulesOptions.DEFAULT);
    }

}
