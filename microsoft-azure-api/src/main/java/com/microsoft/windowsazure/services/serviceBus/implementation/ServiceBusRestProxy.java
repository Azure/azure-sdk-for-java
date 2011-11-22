package com.microsoft.windowsazure.services.serviceBus.implementation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.MediaType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import com.microsoft.windowsazure.services.serviceBus.ServiceBusContract;
import com.microsoft.windowsazure.services.serviceBus.models.AbstractListOptions;
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
import com.microsoft.windowsazure.services.serviceBus.models.Topic;
import com.microsoft.windowsazure.common.ServiceException;
import com.microsoft.windowsazure.common.ServiceFilter;
import com.microsoft.windowsazure.utils.jersey.ClientFilterAdapter;


import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;

public class ServiceBusRestProxy implements ServiceBusContract {

    private Client channel;
    private final String uri;
    private final BrokerPropertiesMapper mapper;
    static Log log = LogFactory.getLog(ServiceBusContract.class);

    ServiceFilter[] filters;

    @Inject
    public ServiceBusRestProxy(
            Client channel,
            @Named("serviceBus") WrapFilter authFilter,
            @Named("serviceBus.uri") String uri,
            BrokerPropertiesMapper mapper) {

        this.channel = channel;
        this.filters = new ServiceFilter[0];
        this.uri = uri;
        this.mapper = mapper;
        channel.addFilter(authFilter);
    }

    public ServiceBusRestProxy(
            Client channel,
            ServiceFilter[] filters,
            String uri,
            BrokerPropertiesMapper mapper) {
        this.channel = channel;
        this.filters = filters;
        this.uri = uri;
        this.mapper = mapper;
    }

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
        WebResource resource = getChannel()
                .resource(uri);
        for (ServiceFilter filter : filters) {
            resource.addFilter(new ClientFilterAdapter(filter));
        }
        return resource;
    }

    void sendMessage(String path, Message message) {
        Builder request = getResource()
                .path(path)
                .path("messages")
                .getRequestBuilder();

        if (message.getContentType() != null)
            request = request.type(message.getContentType());

        if (message.getProperties() != null)
            request = request.header("BrokerProperties", mapper.toString(message.getProperties()));

        request.post(message.getBody());
    }

    public void sendQueueMessage(String path, Message message) throws ServiceException {
        sendMessage(path, message);
    }

    public ReceiveQueueMessageResult receiveQueueMessage(String queueName)
            throws ServiceException {
        return receiveQueueMessage(queueName, ReceiveMessageOptions.DEFAULT);
    }

    public ReceiveQueueMessageResult receiveQueueMessage(String queuePath, ReceiveMessageOptions options) throws ServiceException {

        WebResource resource = getResource()
                .path(queuePath)
                .path("messages")
                .path("head");

        Message message = receiveMessage(options, resource);
        return new ReceiveQueueMessageResult(message);
    }

    private Message receiveMessage(ReceiveMessageOptions options, WebResource resource) {
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

        String brokerProperties = clientResult.getHeaders().getFirst("BrokerProperties");
        String location = clientResult.getHeaders().getFirst("Location");
        MediaType contentType = clientResult.getType();
        Date date = clientResult.getResponseDate();

        Message message = new Message();
        if (brokerProperties != null) {
            message.setProperties(mapper.fromString(brokerProperties));
        }
        if (contentType != null) {
            message.setContentType(contentType.toString());
        }
        if (location != null) {
            message.getProperties().setLockLocation(location);
        }
        message.setDate(date);
        message.setBody(clientResult.getEntityInputStream());
        return message;
    }

    public void sendTopicMessage(String topicName, Message message) throws ServiceException {
        sendMessage(topicName, message);
    }

    public ReceiveSubscriptionMessageResult receiveSubscriptionMessage(String topicName,
            String subscriptionName) throws ServiceException {
        return receiveSubscriptionMessage(topicName, subscriptionName, ReceiveMessageOptions.DEFAULT);
    }

    public ReceiveSubscriptionMessageResult receiveSubscriptionMessage(String topicName,
            String subscriptionName, ReceiveMessageOptions options)
            throws ServiceException {
        WebResource resource = getResource()
                .path(topicName)
                .path("subscriptions")
                .path(subscriptionName)
                .path("messages")
                .path("head");

        Message message = receiveMessage(options, resource);
        return new ReceiveSubscriptionMessageResult(message);
    }

    public void unlockMessage(Message message) throws ServiceException {
        getChannel()
                .resource(message.getLockLocation())
                .put("");
    }

    public void deleteMessage(Message message) throws ServiceException {
        getChannel()
                .resource(message.getLockLocation())
                .delete();
    }

    public CreateQueueResult createQueue(QueueInfo entry) throws ServiceException {
        return new CreateQueueResult(getResource()
                .path(entry.getName())
                .type("application/atom+xml;type=entry;charset=utf-8")
                .put(QueueInfo.class, entry));
    }

    public void deleteQueue(String queuePath) throws ServiceException {
        getResource()
                .path(queuePath)
                .delete();
    }

    public GetQueueResult getQueue(String queuePath) throws ServiceException {
        return new GetQueueResult(getResource()
                .path(queuePath)
                .get(QueueInfo.class));
    }

    public ListQueuesResult listQueues(ListQueuesOptions options) throws ServiceException {
        Feed feed = listOptions(options, getResource()
                .path("$Resources/Queues"))
                .get(Feed.class);
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

    public CreateTopicResult createTopic(Topic entry) throws ServiceException {
        return new CreateTopicResult(getResource()
                .path(entry.getName())
                .type("application/atom+xml;type=entry;charset=utf-8")
                .put(Topic.class, entry));
    }

    public void deleteTopic(String TopicPath) throws ServiceException {
        getResource()
                .path(TopicPath)
                .delete();
    }

    public GetTopicResult getTopic(String TopicPath) throws ServiceException {
        return new GetTopicResult(getResource()
                .path(TopicPath)
                .get(Topic.class));
    }

    public ListTopicsResult listTopics(ListTopicsOptions options) throws ServiceException {
        Feed feed = listOptions(options, getResource()
                .path("$Resources/Topics"))
                .get(Feed.class);
        ArrayList<Topic> Topics = new ArrayList<Topic>();
        for (Entry entry : feed.getEntries()) {
            Topics.add(new Topic(entry));
        }
        ListTopicsResult result = new ListTopicsResult();
        result.setItems(Topics);
        return result;
    }

    public CreateSubscriptionResult createSubscription(String topicPath, Subscription subscription) {
        return new CreateSubscriptionResult(getResource()
                .path(topicPath)
                .path("subscriptions")
                .path(subscription.getName())
                .type("application/atom+xml;type=entry;charset=utf-8")
                .put(Subscription.class, subscription));
    }

    public void deleteSubscription(String topicPath, String subscriptionName) {
        getResource()
                .path(topicPath)
                .path("subscriptions")
                .path(subscriptionName)
                .delete();
    }

    public GetSubscriptionResult getSubscription(String topicPath, String subscriptionName) {
        return new GetSubscriptionResult(getResource()
                .path(topicPath)
                .path("subscriptions")
                .path(subscriptionName)
                .get(Subscription.class));
    }

    public ListSubscriptionsResult listSubscriptions(String topicPath, ListSubscriptionsOptions options) {
        Feed feed = listOptions(options, getResource()
                .path(topicPath)
                .path("subscriptions"))
                .get(Feed.class);
        ArrayList<Subscription> list = new ArrayList<Subscription>();
        for (Entry entry : feed.getEntries()) {
            list.add(new Subscription(entry));
        }
        ListSubscriptionsResult result = new ListSubscriptionsResult();
        result.setItems(list);
        return result;
    }

    public CreateRuleResult createRule(String topicPath, String subscriptionName,
            Rule rule) {
        return new CreateRuleResult(getResource()
                .path(topicPath)
                .path("subscriptions")
                .path(subscriptionName)
                .path("rules")
                .path(rule.getName())
                .type("application/atom+xml;type=entry;charset=utf-8")
                .put(Rule.class, rule));
    }

    public void deleteRule(String topicPath, String subscriptionName,
            String ruleName) {
        getResource()
                .path(topicPath)
                .path("subscriptions")
                .path(subscriptionName)
                .path("rules")
                .path(ruleName)
                .delete();
    }

    public GetRuleResult getRule(String topicPath, String subscriptionName,
            String ruleName) {
        return new GetRuleResult(getResource()
                .path(topicPath)
                .path("subscriptions")
                .path(subscriptionName)
                .path("rules")
                .path(ruleName)
                .get(Rule.class));
    }

    public ListRulesResult listRules(String topicPath, String subscriptionName, ListRulesOptions options) {
        Feed feed = listOptions(options, getResource()
                .path(topicPath)
                .path("subscriptions")
                .path(subscriptionName)
                .path("rules"))
                .get(Feed.class);
        ArrayList<Rule> list = new ArrayList<Rule>();
        for (Entry entry : feed.getEntries()) {
            list.add(new Rule(entry));
        }
        ListRulesResult result = new ListRulesResult();
        result.setItems(list);
        return result;
    }

    public ListQueuesResult listQueues() throws ServiceException {
        return listQueues(ListQueuesOptions.DEFAULT);
    }

    public ListTopicsResult listTopics() throws ServiceException {
        return listTopics(ListTopicsOptions.DEFAULT);
    }

    public ListSubscriptionsResult listSubscriptions(String topicName) throws ServiceException {
        return listSubscriptions(topicName, ListSubscriptionsOptions.DEFAULT);
    }

    public ListRulesResult listRules(String topicName, String subscriptionName) throws ServiceException {
        return listRules(topicName, subscriptionName, ListRulesOptions.DEFAULT);
    }

}
