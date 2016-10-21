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

import com.microsoft.windowsazure.core.UserAgentFilter;
import com.microsoft.windowsazure.core.pipeline.PipelineHelpers;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceRequestFilter;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceResponseFilter;
import com.microsoft.windowsazure.core.pipeline.jersey.ClientFilterAdapter;
import com.microsoft.windowsazure.core.pipeline.jersey.ClientFilterRequestAdapter;
import com.microsoft.windowsazure.core.pipeline.jersey.ClientFilterResponseAdapter;
import com.microsoft.windowsazure.core.pipeline.jersey.ServiceFilter;
import com.microsoft.windowsazure.exception.ServiceException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import com.microsoft.windowsazure.services.servicebus.ServiceBusContract;
import com.microsoft.windowsazure.services.servicebus.models.AbstractListOptions;
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
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.filter.ClientFilter;

public class ServiceBusRestProxy implements ServiceBusContract {

    private Client channel;
    private final String uri;
    private final BrokerPropertiesMapper mapper;
    private final CustomPropertiesMapper customPropertiesMapper;

    private ClientFilter[] filters;

    @Inject
    public ServiceBusRestProxy(Client channel, WrapFilter authFilter,
            SasFilter sasAuthFilter,
            UserAgentFilter userAgentFilter,
            ServiceBusConnectionSettings connectionSettings,
            BrokerPropertiesMapper mapper) {

        this.channel = channel;
        this.filters = new ClientFilter[0];
        this.uri = connectionSettings.getUri();
        this.mapper = mapper;
        this.customPropertiesMapper = new CustomPropertiesMapper();
        if (connectionSettings.isSasAuthentication()) {
            channel.addFilter(sasAuthFilter);
        } else {
            channel.addFilter(authFilter);
        }
        channel.addFilter(new ClientFilterRequestAdapter(userAgentFilter));
    }

    public ServiceBusRestProxy(Client channel, ClientFilter[] filters,
            String uri, BrokerPropertiesMapper mapper) {
        this.channel = channel;
        this.filters = filters;
        this.uri = uri;
        this.mapper = mapper;
        this.customPropertiesMapper = new CustomPropertiesMapper();
    }

    @Override
    public ServiceBusContract withFilter(ServiceFilter filter) {
        ClientFilter[] newFilters = Arrays.copyOf(filters, filters.length + 1);
        newFilters[filters.length] = new ClientFilterAdapter(filter);
        return new ServiceBusRestProxy(channel, newFilters, uri, mapper);
    }

    @Override
    public ServiceBusContract withRequestFilterFirst(
            ServiceRequestFilter serviceRequestFilter) {
        ClientFilter[] currentFilters = filters;
        ClientFilter[] newFilters = new ClientFilter[currentFilters.length + 1];
        System.arraycopy(currentFilters, 0, newFilters, 1,
                currentFilters.length);
        newFilters[0] = new ClientFilterRequestAdapter(serviceRequestFilter);
        return new ServiceBusRestProxy(channel, newFilters, uri, mapper);
    }

    @Override
    public ServiceBusContract withRequestFilterLast(
            ServiceRequestFilter serviceRequestFilter) {
        ClientFilter[] currentFilters = filters;
        ClientFilter[] newFilters = Arrays.copyOf(currentFilters,
                currentFilters.length + 1);
        newFilters[currentFilters.length] = new ClientFilterRequestAdapter(
                serviceRequestFilter);
        return new ServiceBusRestProxy(channel, newFilters, uri, mapper);
    }

    @Override
    public ServiceBusContract withResponseFilterFirst(
            ServiceResponseFilter serviceResponseFilter) {
        ClientFilter[] currentFilters = filters;
        ClientFilter[] newFilters = new ClientFilter[currentFilters.length + 1];
        System.arraycopy(currentFilters, 0, newFilters, 1,
                currentFilters.length);
        newFilters[0] = new ClientFilterResponseAdapter(serviceResponseFilter);
        return new ServiceBusRestProxy(channel, newFilters, uri, mapper);
    }

    @Override
    public ServiceBusContract withResponseFilterLast(
            ServiceResponseFilter serviceResponseFilter) {
        ClientFilter[] currentFilters = filters;
        ClientFilter[] newFilters = Arrays.copyOf(currentFilters,
                currentFilters.length + 1);
        newFilters[currentFilters.length] = new ClientFilterResponseAdapter(
                serviceResponseFilter);
        return new ServiceBusRestProxy(channel, newFilters, uri, mapper);
    }

    public Client getChannel() {
        return channel;
    }

    public void setChannel(Client channel) {
        this.channel = channel;
    }

    private WebResource getResource() {
        return getResourceWithApiVersion("2013-07");
    }

    private WebResource getResourceWithApiVersion(String version) {
        WebResource resource = getChannel().resource(uri).queryParam(
                "api-version", version);
        for (ClientFilter filter : filters) {
            resource.addFilter(filter);
        }
        return resource;
    }

    @Override
    public void sendMessage(String path, BrokeredMessage message) {
        Builder request = getResource().path(path).path("messages")
                .getRequestBuilder();

        if (message.getContentType() != null) {
            request = request.type(message.getContentType());
        }

        if (message.getBrokerProperties() != null) {
            request = request.header("BrokerProperties",
                    mapper.toString(message.getBrokerProperties()));
        }

        for (java.util.Map.Entry<String, Object> entry : message
                .getProperties().entrySet()) {
            request.header(entry.getKey(),
                    customPropertiesMapper.toString(entry.getValue()));
        }

        request.post(message.getBody());
    }

    @Override
    public void sendQueueMessage(String path, BrokeredMessage message)
            throws ServiceException {
        sendMessage(path, message);
    }

    @Override
    public ReceiveQueueMessageResult receiveQueueMessage(String queueName)
            throws ServiceException {
        return receiveQueueMessage(queueName, ReceiveMessageOptions.DEFAULT);
    }

    @Override
    public ReceiveQueueMessageResult receiveQueueMessage(String queuePath,
            ReceiveMessageOptions options) throws ServiceException {

        WebResource resource = getResource().path(queuePath).path("messages")
                .path("head");

        BrokeredMessage message = receiveMessage(options, resource);
        return new ReceiveQueueMessageResult(message);
    }

    @Override
    public ReceiveMessageResult receiveMessage(String path)
            throws ServiceException {
        return receiveMessage(path, ReceiveMessageOptions.DEFAULT);
    }

    @Override
    public ReceiveMessageResult receiveMessage(String path,
            ReceiveMessageOptions options) throws ServiceException {

        WebResource resource = getResource().path(path).path("messages")
                .path("head");

        BrokeredMessage message = receiveMessage(options, resource);
        return new ReceiveMessageResult(message);
    }

    private BrokeredMessage receiveMessage(ReceiveMessageOptions options,
            WebResource resource) {
        if (options.getTimeout() != null) {
            resource = resource.queryParam("timeout",
                    Integer.toString(options.getTimeout()));
        }

        ClientResponse clientResult;
        if (options.isReceiveAndDelete()) {
            clientResult = resource.delete(ClientResponse.class);
        } else if (options.isPeekLock()) {
            // Passing 0 as request content just to force jersey client to add Content-Length header.
            // ServiceBus service doesn't read http request body for message receive requests.
            clientResult = resource.post(ClientResponse.class, "0");
        } else {
            throw new RuntimeException("Unknown ReceiveMode");
        }

        if (clientResult.getStatus() == 204) {
            return null;
        }

        BrokerProperties brokerProperties;
        if (clientResult.getHeaders().containsKey("BrokerProperties")) {
            brokerProperties = mapper.fromString(clientResult.getHeaders()
                    .getFirst("BrokerProperties"));
        } else {
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
            } catch (ParseException e) {
                // log.warn("Unable to parse custom header", e);
            } catch (NumberFormatException e) {
                // log.warn("Unable to parse custom header", e);
            }
        }

        return message;
    }

    @Override
    public void sendTopicMessage(String topicName, BrokeredMessage message)
            throws ServiceException {
        sendMessage(topicName, message);
    }

    @Override
    public ReceiveSubscriptionMessageResult receiveSubscriptionMessage(
            String topicName, String subscriptionName) throws ServiceException {
        return receiveSubscriptionMessage(topicName, subscriptionName,
                ReceiveMessageOptions.DEFAULT);
    }

    @Override
    public ReceiveSubscriptionMessageResult receiveSubscriptionMessage(
            String topicName, String subscriptionName,
            ReceiveMessageOptions options) throws ServiceException {
        WebResource resource = getResource().path(topicName)
                .path("subscriptions").path(subscriptionName).path("messages")
                .path("head");

        BrokeredMessage message = receiveMessage(options, resource);
        return new ReceiveSubscriptionMessageResult(message);
    }

    @Override
    public void unlockMessage(BrokeredMessage message) throws ServiceException {
        getChannel().resource(message.getLockLocation()).put("0");
    }

    @Override
    public void deleteMessage(BrokeredMessage message) throws ServiceException {
        getChannel().resource(message.getLockLocation()).delete();
    }

    @Override
    public CreateQueueResult createQueue(QueueInfo queueInfo)
            throws ServiceException {
        Builder webResourceBuilder = getResource().path(queueInfo.getPath())
                .type("application/atom+xml;type=entry;charset=utf-8");
        if ((queueInfo.getForwardTo() != null)
                && !queueInfo.getForwardTo().isEmpty()) {
            webResourceBuilder.header("ServiceBusSupplementaryAuthorization",
                    queueInfo.getForwardTo());
        }
        return new CreateQueueResult(webResourceBuilder.put(QueueInfo.class,
                queueInfo));
    }

    @Override
    public void deleteQueue(String queuePath) throws ServiceException {
        getResource().path(queuePath).delete();
    }

    @Override
    public GetQueueResult getQueue(String queuePath) throws ServiceException {
        return new GetQueueResult(getResource().path(queuePath).get(
                QueueInfo.class));
    }

    @Override
    public ListQueuesResult listQueues(ListQueuesOptions options)
            throws ServiceException {
        Feed feed = listOptions(options,
                getResource().path("$Resources/Queues")).get(Feed.class);
        ArrayList<QueueInfo> queues = new ArrayList<QueueInfo>();
        for (Entry entry : feed.getEntries()) {
            queues.add(new QueueInfo(entry));
        }
        ListQueuesResult result = new ListQueuesResult();
        result.setItems(queues);
        return result;
    }

    @Override
    public QueueInfo updateQueue(QueueInfo queueInfo) throws ServiceException {
        Builder webResourceBuilder = getResource().path(queueInfo.getPath())
                .type("application/atom+xml;type=entry;charset=utf-8")
                .header("If-Match", "*");
        if ((queueInfo.getForwardTo() != null)
                && !queueInfo.getForwardTo().isEmpty()) {
            webResourceBuilder.header("ServiceBusSupplementaryAuthorization",
                    queueInfo.getForwardTo());
        }
        return webResourceBuilder.put(QueueInfo.class, queueInfo);
    }

    private WebResource listOptions(AbstractListOptions<?> options,
            WebResource path) {
        if (options.getTop() != null) {
            path = path.queryParam("$top", options.getTop().toString());
        }
        if (options.getSkip() != null) {
            path = path.queryParam("$skip", options.getSkip().toString());
        }
        if (options.getFilter() != null) {
            path = path.queryParam("$filter", options.getFilter());
        }
        return path;
    }

    @Override
    public CreateEventHubResult createEventHub(EventHubInfo entry)
            throws ServiceException {
        return new CreateEventHubResult(getResourceWithApiVersion("2014-01").path(entry.getPath())
                .type("application/atom+xml;type=entry;charset=utf-8")
                .put(EventHubInfo.class, entry));
    }

    @Override
    public void deleteEventHub(String eventHubPath) throws ServiceException {
        getResourceWithApiVersion("2014-01").path(eventHubPath).delete();
    }

    @Override
    public GetEventHubResult getEventHub(String eventHubPath) throws ServiceException {
        return new GetEventHubResult(getResourceWithApiVersion("2014-01").path(eventHubPath).get(
                EventHubInfo.class));
    }

    @Override
    public ListEventHubsResult listEventHubs(ListEventHubsOptions options)
            throws ServiceException {
        Feed feed = listOptions(options,
                getResourceWithApiVersion("2014-01").path("$Resources/EventHubs")).get(Feed.class);
        ArrayList<EventHubInfo> eventHubs = new ArrayList<EventHubInfo>();
        for (Entry entry : feed.getEntries()) {
            eventHubs.add(new EventHubInfo(entry));
        }
        ListEventHubsResult result = new ListEventHubsResult();
        result.setItems(eventHubs);
        return result;
    }

    @Override
    public ListEventHubsResult listEventHubs() throws ServiceException {
        return listEventHubs(ListEventHubsOptions.DEFAULT);
    }

    @Override
    public CreateTopicResult createTopic(TopicInfo entry)
            throws ServiceException {
        return new CreateTopicResult(getResource().path(entry.getPath())
                .type("application/atom+xml;type=entry;charset=utf-8")
                .put(TopicInfo.class, entry));
    }

    @Override
    public void deleteTopic(String topicPath) throws ServiceException {
        getResource().path(topicPath).delete();
    }

    @Override
    public GetTopicResult getTopic(String topicPath) throws ServiceException {
        return new GetTopicResult(getResource().path(topicPath).get(
                TopicInfo.class));
    }

    @Override
    public ListTopicsResult listTopics(ListTopicsOptions options)
            throws ServiceException {
        Feed feed = listOptions(options,
                getResource().path("$Resources/Topics")).get(Feed.class);
        ArrayList<TopicInfo> topics = new ArrayList<TopicInfo>();
        for (Entry entry : feed.getEntries()) {
            topics.add(new TopicInfo(entry));
        }
        ListTopicsResult result = new ListTopicsResult();
        result.setItems(topics);
        return result;
    }

    @Override
    public TopicInfo updateTopic(TopicInfo topicInfo) throws ServiceException {
        return getResource().path(topicInfo.getPath())
                .type("application/atom+xml;type=entry;charset=utf-8")
                .header("If-Match", "*").put(TopicInfo.class, topicInfo);
    }

    @Override
    public CreateSubscriptionResult createSubscription(String topicPath,
            SubscriptionInfo subscriptionInfo) {
        Builder webResourceBuilder = getResource().path(topicPath)
                .path("subscriptions").path(subscriptionInfo.getName())
                .type("application/atom+xml;type=entry;charset=utf-8");
        if ((subscriptionInfo.getForwardTo() != null)
                && (!subscriptionInfo.getForwardTo().isEmpty())) {
            webResourceBuilder.header("ServiceBusSupplementaryAuthorization",
                    subscriptionInfo.getForwardTo());

        }
        return new CreateSubscriptionResult(webResourceBuilder.put(
                SubscriptionInfo.class, subscriptionInfo));
    }

    @Override
    public void deleteSubscription(String topicPath, String subscriptionName) {
        getResource().path(topicPath).path("subscriptions")
                .path(subscriptionName).delete();
    }

    @Override
    public GetSubscriptionResult getSubscription(String topicPath,
            String subscriptionName) {
        return new GetSubscriptionResult(getResource().path(topicPath)
                .path("subscriptions").path(subscriptionName)
                .get(SubscriptionInfo.class));
    }

    @Override
    public ListSubscriptionsResult listSubscriptions(String topicPath,
            ListSubscriptionsOptions options) {
        Feed feed = listOptions(options,
                getResource().path(topicPath).path("subscriptions")).get(
                Feed.class);
        ArrayList<SubscriptionInfo> list = new ArrayList<SubscriptionInfo>();
        for (Entry entry : feed.getEntries()) {
            list.add(new SubscriptionInfo(entry));
        }
        ListSubscriptionsResult result = new ListSubscriptionsResult();
        result.setItems(list);
        return result;
    }

    @Override
    public SubscriptionInfo updateSubscription(String topicName,
            SubscriptionInfo subscriptionInfo) throws ServiceException {
        Builder webResourceBuilder = getResource().path(topicName)
                .path("subscriptions").path(subscriptionInfo.getName())
                .type("application/atom+xml;type=entry;charset=utf-8")
                .header("If-Match", "*");
        if ((subscriptionInfo.getForwardTo() != null)
                && !subscriptionInfo.getForwardTo().isEmpty()) {
            webResourceBuilder.header("ServiceBusSupplementaryAuthorization",
                    subscriptionInfo.getForwardTo());
        }
        return webResourceBuilder.put(SubscriptionInfo.class, subscriptionInfo);
    }

    @Override
    public CreateRuleResult createRule(String topicPath,
            String subscriptionName, RuleInfo rule) {
        return new CreateRuleResult(getResource().path(topicPath)
                .path("subscriptions").path(subscriptionName).path("rules")
                .path(rule.getName())
                .type("application/atom+xml;type=entry;charset=utf-8")
                .put(RuleInfo.class, rule));
    }

    @Override
    public void deleteRule(String topicPath, String subscriptionName,
            String ruleName) {
        getResource().path(topicPath).path("subscriptions")
                .path(subscriptionName).path("rules").path(ruleName).delete();
    }

    @Override
    public GetRuleResult getRule(String topicPath, String subscriptionName,
            String ruleName) {
        return new GetRuleResult(getResource().path(topicPath)
                .path("subscriptions").path(subscriptionName).path("rules")
                .path(ruleName).get(RuleInfo.class));
    }

    @Override
    public ListRulesResult listRules(String topicPath, String subscriptionName,
            ListRulesOptions options) {
        Feed feed = listOptions(
                options,
                getResource().path(topicPath).path("subscriptions")
                        .path(subscriptionName).path("rules")).get(Feed.class);
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
    public ListSubscriptionsResult listSubscriptions(String topicName)
            throws ServiceException {
        return listSubscriptions(topicName, ListSubscriptionsOptions.DEFAULT);
    }

    @Override
    public ListRulesResult listRules(String topicName, String subscriptionName)
            throws ServiceException {
        return listRules(topicName, subscriptionName, ListRulesOptions.DEFAULT);
    }

    @Override
    public void renewQueueLock(String queueName, String messageId,
            String lockToken) throws ServiceException {
        ClientResponse clientResponse = getResource().path(queueName)
                .path("messages").path(messageId).path(lockToken)
                .post(ClientResponse.class, "0");
        PipelineHelpers.throwIfNotSuccess(clientResponse);
    }

    @Override
    public void renewSubscriptionLock(String topicName,
            String subscriptionName, String messageId, String lockToken)
            throws ServiceException {
        ClientResponse clientResponse = getResource().path(topicName)
                .path("Subscriptions").path(subscriptionName).path("messages")
                .path(messageId).path(lockToken).post(ClientResponse.class, "0");
        PipelineHelpers.throwIfNotSuccess(clientResponse);
    }

}
