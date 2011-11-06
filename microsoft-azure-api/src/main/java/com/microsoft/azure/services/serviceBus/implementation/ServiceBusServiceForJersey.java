package com.microsoft.azure.services.serviceBus.implementation;

import java.io.InputStream;
import java.rmi.UnexpectedException;
import java.util.ArrayList;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.MediaType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.microsoft.azure.services.serviceBus.ListTopicsResult;
import com.microsoft.azure.services.serviceBus.Message;
import com.microsoft.azure.services.serviceBus.Queue;
import com.microsoft.azure.services.serviceBus.ListQueuesResult;
import com.microsoft.azure.services.serviceBus.ReceiveMessageOptions;
import com.microsoft.azure.services.serviceBus.ReceiveMode;
import com.microsoft.azure.services.serviceBus.ServiceBusService;
import com.microsoft.azure.services.serviceBus.Topic;
import com.microsoft.azure.services.serviceBus.implementation.Entry;
import com.microsoft.azure.services.serviceBus.implementation.Feed;

import com.microsoft.azure.ServiceException;
import com.microsoft.azure.auth.wrap.WrapFilter;
import com.microsoft.azure.utils.ServiceExceptionFactory;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;

public class ServiceBusServiceForJersey implements ServiceBusService {

	private Client channel;
	private String uri;
	private BrokerPropertiesMapper mapper;
	static Log log = LogFactory.getLog(ServiceBusService.class);

	@Inject
	public ServiceBusServiceForJersey(
			Client channel, 
			@Named("serviceBus") WrapFilter authFilter,
			@Named("serviceBus.uri") String uri,
			BrokerPropertiesMapper mapper) {

		this.channel = channel;
		this.uri = uri;
		this.mapper = mapper;
		channel.addFilter(authFilter);
	}

	public Client getChannel() {
		return channel;
	}

	public void setChannel(Client channel) {
		this.channel = channel;
	}

	private WebResource getResource() {
		return getChannel()
			.resource(uri);
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
		if (brokerProperties != null)
		{
			result.setProperties(mapper.fromString(brokerProperties));
		}
		if (contentType != null)
		{
			result.setContentType(contentType.toString());
		}
		if (location != null)
		{
			result.getProperties().setLockLocation(location);
		}
		result.setDate(date);
		result.setBody(clientResult.getEntityInputStream());
		return result;
	}

	public void sendTopicMessage(String topicName, Message message) throws ServiceException
	{
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
		for(Entry entry : feed.getEntries()){
			queues.add(new Queue(entry));
		}
		ListQueuesResult result = new ListQueuesResult();
		result.setItems(queues);
		return result;
	}

	public Topic createTopic(Topic entry) throws ServiceException {
		return getResource()
			.path(entry.getName())
			.type("application/atom+xml")//;type=entry;charset=utf-8")
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
		for(Entry entry : feed.getEntries()){
			Topics.add(new Topic(entry));
		}
		ListTopicsResult result = new ListTopicsResult();
		result.setItems(Topics);
		return result;
	}

	public void addSubscription(String topicPath, String subscriptionName,
			Entry subscription) {
		// TODO Auto-generated method stub

	}

	public void removeSubscription(String topicPath, String subscriptionName) {
		// TODO Auto-generated method stub

	}

	public Entry getSubscription(String topicPath, String subscriptionName) {
		// TODO Auto-generated method stub
		return null;
	}

	public Feed getSubscriptions(String topicPath) {
		// TODO Auto-generated method stub
		return null;
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
