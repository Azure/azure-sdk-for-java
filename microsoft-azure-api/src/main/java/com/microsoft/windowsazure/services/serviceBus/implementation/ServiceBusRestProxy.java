package com.microsoft.windowsazure.services.serviceBus.implementation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.MediaType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.microsoft.windowsazure.ServiceException;
import com.microsoft.windowsazure.auth.wrap.WrapFilter;
import com.microsoft.windowsazure.http.ClientFilterAdapter;
import com.microsoft.windowsazure.http.ServiceFilter;
import com.microsoft.windowsazure.services.serviceBus.ListQueuesResult;
import com.microsoft.windowsazure.services.serviceBus.ListSubscriptionsResult;
import com.microsoft.windowsazure.services.serviceBus.ListTopicsResult;
import com.microsoft.windowsazure.services.serviceBus.Message;
import com.microsoft.windowsazure.services.serviceBus.Queue;
import com.microsoft.windowsazure.services.serviceBus.ReceiveMessageOptions;
import com.microsoft.windowsazure.services.serviceBus.ServiceBusContract;
import com.microsoft.windowsazure.services.serviceBus.Subscription;
import com.microsoft.windowsazure.services.serviceBus.Topic;
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

    public Message receiveQueueMessage(String queueName)
            throws ServiceException {
        return receiveQueueMessage(queueName, ReceiveMessageOptions.DEFAULT);
    }

    public Message receiveQueueMessage(String queuePath, ReceiveMessageOptions options) throws ServiceException {

        WebResource resource = getResource()
                .path(queuePath)
                .path("messages")
                .path("head");

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

        Message result = new Message();
        if (brokerProperties != null) {
            result.setProperties(mapper.fromString(brokerProperties));
        }
        if (contentType != null) {
            result.setContentType(contentType.toString());
        }
        if (location != null) {
            result.getProperties().setLockLocation(location);
        }
        result.setDate(date);
        result.setBody(clientResult.getEntityInputStream());
        return result;
    }

    public void sendTopicMessage(String topicName, Message message) throws ServiceException {
        sendMessage(topicName, message);
    }

    public Message receiveSubscriptionMessage(String topicName,
            String subscriptionName) throws ServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    public Message receiveSubscriptionMessage(String topicName,
            String subscriptionName, ReceiveMessageOptions options)
            throws ServiceException {
        // TODO Auto-generated method stub
        return null;
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

    public Queue createQueue(Queue entry) throws ServiceException {
        return getResource()
                .path(entry.getName())
                .type("application/atom+xml")//;type=entry;charset=utf-8")
                .put(Queue.class, entry);
    }

    public void deleteQueue(String queuePath) throws ServiceException {
        getResource()
                .path(queuePath)
                .delete();
    }

    public Queue getQueue(String queuePath) throws ServiceException {
        return getResource()
                .path(queuePath)
                .get(Queue.class);
    }

    public ListQueuesResult listQueues() throws ServiceException {
        Feed feed = getResource()
                .path("$Resources/Queues")
                .get(Feed.class);
        ArrayList<Queue> queues = new ArrayList<Queue>();
        for (Entry entry : feed.getEntries()) {
            queues.add(new Queue(entry));
        }
        ListQueuesResult result = new ListQueuesResult();
        result.setItems(queues);
        return result;
    }

    public Topic createTopic(Topic entry) throws ServiceException {
        return getResource()
                .path(entry.getName())
                .type("application/atom+xml;type=entry;charset=utf-8")
                .put(Topic.class, entry);
    }

    public void deleteTopic(String TopicPath) throws ServiceException {
        getResource()
                .path(TopicPath)
                .delete();
    }

    public Topic getTopic(String TopicPath) throws ServiceException {
        return getResource()
                .path(TopicPath)
                .get(Topic.class);
    }

    public ListTopicsResult listTopics() throws ServiceException {
        Feed feed = getResource()
                .path("$Resources/Topics")
                .get(Feed.class);
        ArrayList<Topic> Topics = new ArrayList<Topic>();
        for (Entry entry : feed.getEntries()) {
            Topics.add(new Topic(entry));
        }
        ListTopicsResult result = new ListTopicsResult();
        result.setItems(Topics);
        return result;
    }

    public Subscription createSubscription(String topicPath, Subscription subscription) {
        return getResource()
                .path(topicPath)
                .path("subscriptions")
                .path(subscription.getName())
                .type("application/atom+xml;type=entry;charset=utf-8")
                .put(Subscription.class, subscription);
    }

    public void deleteSubscription(String topicPath, String subscriptionName) {
        getResource()
                .path(topicPath)
                .path("subscriptions")
                .path(subscriptionName)
                .delete();
    }

    public Subscription getSubscription(String topicPath, String subscriptionName) {
        return getResource()
                .path(topicPath)
                .path("subscriptions")
                .path(subscriptionName)
                .get(Subscription.class);
    }

    public ListSubscriptionsResult listSubscriptions(String topicPath) {
        Feed feed = getResource()
                .path(topicPath)
                .path("subscriptions")
                .get(Feed.class);
        ArrayList<Subscription> list = new ArrayList<Subscription>();
        for (Entry entry : feed.getEntries()) {
            list.add(new Subscription(entry));
        }
        ListSubscriptionsResult result = new ListSubscriptionsResult();
        result.setItems(list);
        return result;
    }

    public void addRule(String topicPath, String subscriptionName,
            String ruleName, Entry rule) {
        // TODO Auto-generated method stub

    }

    public void removeRule(String topicPath, String subscriptionName,
            String ruleName) {
        // TODO Auto-generated method stub

    }

    public Entry getRule(String topicPath, String subscriptionName,
            String ruleName) {
        // TODO Auto-generated method stub
        return null;
    }

    public Feed getRules(String topicPath, String subscriptionName) {
        // TODO Auto-generated method stub
        return null;
    }

}
