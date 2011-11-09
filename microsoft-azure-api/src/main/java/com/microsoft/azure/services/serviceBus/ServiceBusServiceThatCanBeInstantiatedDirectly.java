package com.microsoft.azure.services.serviceBus;

import com.microsoft.azure.ServiceException;
import com.microsoft.azure.configuration.Configuration;
import com.microsoft.azure.http.ServiceFilter;
import com.microsoft.azure.services.serviceBus.implementation.Entry;
import com.microsoft.azure.services.serviceBus.implementation.Feed;

public class ServiceBusServiceThatCanBeInstantiatedDirectly implements ServiceBusService {
	final ServiceBusService service;

	public ServiceBusServiceThatCanBeInstantiatedDirectly() throws Exception {
		this(Configuration.getInstance());
	}

	public ServiceBusServiceThatCanBeInstantiatedDirectly(Configuration config) throws Exception {
		service = config.create(ServiceBusService.class);
	}
	
	public ServiceBusService withFilter(ServiceFilter filter) {
		return service.withFilter(filter);
	}

	public void sendQueueMessage(String queueName, Message message)
			throws ServiceException {
		service.sendQueueMessage(queueName, message);
	}

	public Message receiveQueueMessage(String queueName)
			throws ServiceException {
		return service.receiveQueueMessage(queueName);
	}

	public Message receiveQueueMessage(String queueName,
			ReceiveMessageOptions options) throws ServiceException {
		return service.receiveQueueMessage(queueName, options);
	}

	public void sendTopicMessage(String topicName, Message message)
			throws ServiceException {
		service.sendTopicMessage(topicName, message);
	}

	public Message receiveSubscriptionMessage(String topicName,
			String subscriptionName) throws ServiceException {
		return service.receiveSubscriptionMessage(topicName, subscriptionName);
	}

	public Message receiveSubscriptionMessage(String topicName,
			String subscriptionName, ReceiveMessageOptions options)
			throws ServiceException {
		return service.receiveSubscriptionMessage(topicName, subscriptionName,
				options);
	}

	public void unlockMessage(Message message) throws ServiceException {
		service.unlockMessage(message);
	}

	public void deleteMessage(Message message) throws ServiceException {
		service.deleteMessage(message);
	}

	public Queue createQueue(Queue queue) throws ServiceException {
		return service.createQueue(queue);
	}

	public void deleteQueue(String queueName) throws ServiceException {
		service.deleteQueue(queueName);
	}

	public Queue getQueue(String queueName) throws ServiceException {
		return service.getQueue(queueName);
	}

	public ListQueuesResult listQueues() throws ServiceException {
		return service.listQueues();
	}

	public Topic createTopic(Topic topic) throws ServiceException {
		return service.createTopic(topic);
	}

	public void deleteTopic(String topicName) throws ServiceException {
		service.deleteTopic(topicName);
	}

	public Topic getTopic(String topicName) throws ServiceException {
		return service.getTopic(topicName);
	}

	public ListTopicsResult listTopics() throws ServiceException {
		return service.listTopics();
	}

	public void addSubscription(String topicName, String subscriptionName,
			Entry subscription) throws ServiceException {
		service.addSubscription(topicName, subscriptionName, subscription);
	}

	public void removeSubscription(String topicName, String subscriptionName)
			throws ServiceException {
		service.removeSubscription(topicName, subscriptionName);
	}

	public Entry getSubscription(String topicName, String subscriptionName)
			throws ServiceException {
		return service.getSubscription(topicName, subscriptionName);
	}

	public Feed getSubscriptions(String topicName) throws ServiceException {
		return service.getSubscriptions(topicName);
	}

	public void addRule(String topicName, String subscriptionName,
			String ruleName, Entry rule) throws ServiceException {
		service.addRule(topicName, subscriptionName, ruleName, rule);
	}

	public void removeRule(String topicName, String subscriptionName,
			String ruleName) throws ServiceException {
		service.removeRule(topicName, subscriptionName, ruleName);
	}

	public Entry getRule(String topicName, String subscriptionName,
			String ruleName) throws ServiceException {
		return service.getRule(topicName, subscriptionName, ruleName);
	}

	public Feed getRules(String topicName, String subscriptionName)
			throws ServiceException {
		return service.getRules(topicName, subscriptionName);
	}
}
