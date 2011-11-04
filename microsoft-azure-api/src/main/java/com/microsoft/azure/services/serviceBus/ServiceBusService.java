package com.microsoft.azure.services.serviceBus;

import com.microsoft.azure.services.serviceBus.implementation.Entry;
import com.microsoft.azure.services.serviceBus.implementation.Feed;

import com.microsoft.azure.ServiceException;


public interface ServiceBusService {
	void sendMessage(String path, Message message) throws ServiceException;
	
	Message receiveQueueMessage(String queueName) throws ServiceException;
	Message receiveQueueMessage(String queueName, ReceiveMessageOptions options) throws ServiceException;

	Message receiveSubscriptionMessage(String topicName, String subscriptionName) throws ServiceException;
	Message receiveSubscriptionMessage(String topicName, String subscriptionName, ReceiveMessageOptions options) throws ServiceException;

	void unlockMessage(Message message) throws ServiceException;
	void deleteMessage(Message message) throws ServiceException;

	Queue createQueue(Queue queue) throws ServiceException;
	void deleteQueue(String queuePath) throws ServiceException;
	Queue getQueue(String queuePath) throws ServiceException;
	QueueList getQueueList() throws ServiceException;

	Entry createTopic(Entry topic) throws ServiceException;
	void deleteTopic(String topicPath) throws ServiceException;
	Entry getTopic(String topicPath) throws ServiceException;
	Feed getTopics() throws ServiceException;

	void addSubscription(String topicPath, String subscriptionName, Entry subscription) throws ServiceException;
	void removeSubscription(String topicPath, String subscriptionName) throws ServiceException;
	Entry getSubscription(String topicPath, String subscriptionName) throws ServiceException;
	Feed getSubscriptions(String topicPath) throws ServiceException;

	void addRule(String topicPath, String subscriptionName, String ruleName, Entry rule) throws ServiceException;
	void removeRule(String topicPath, String subscriptionName, String ruleName) throws ServiceException;
	Entry getRule(String topicPath, String subscriptionName, String ruleName) throws ServiceException;
	Feed getRules(String topicPath, String subscriptionName) throws ServiceException;
}

