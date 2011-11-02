package com.microsoft.azure.services.serviceBus;

import com.microsoft.azure.services.serviceBus.schema.Entry;
import com.microsoft.azure.services.serviceBus.schema.Feed;

import com.microsoft.azure.ServiceException;


public interface ServiceBusService {
	void sendMessage(String path, Message message) throws ServiceException;
	Message receiveMessage(String queuePath, Integer timeout, ReceiveMode receiveMode) throws ServiceException;
	//BrokeredMessage receiveMessage(String topicPath, String subscriptionName, int timeout, ReceiveMode receiveMode) throws ServiceException;
	//void abandonMessage(BrokeredMessage message) throws ServiceException;
	//void completeMessage(BrokeredMessage message) throws ServiceException;

	Queue createQueue(Queue queue) throws ServiceException;
	void deleteQueue(String queuePath) throws ServiceException;
	Queue getQueue(String queuePath) throws ServiceException;
	QueueList getQueueList() throws ServiceException;
	Iterable<Queue> iterateQueues() throws ServiceException;

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

