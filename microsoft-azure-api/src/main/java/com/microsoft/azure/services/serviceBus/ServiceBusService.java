package com.microsoft.azure.services.serviceBus;

import com.microsoft.azure.services.serviceBus.implementation.Entry;
import com.microsoft.azure.services.serviceBus.implementation.Feed;

import com.microsoft.azure.ServiceException;


public interface ServiceBusService {
	void sendMessage(String queueOrTopicName, Message message) throws ServiceException;
	
	Message receiveQueueMessage(String queueName) throws ServiceException;
	Message receiveQueueMessage(String queueName, ReceiveMessageOptions options) throws ServiceException;

	Message receiveSubscriptionMessage(String topicName, String subscriptionName) throws ServiceException;
	Message receiveSubscriptionMessage(String topicName, String subscriptionName, ReceiveMessageOptions options) throws ServiceException;

	void unlockMessage(Message message) throws ServiceException;
	void deleteMessage(Message message) throws ServiceException;

	Queue createQueue(Queue queue) throws ServiceException;
	void deleteQueue(String queueName) throws ServiceException;
	Queue getQueue(String queueName) throws ServiceException;
	ListQueuesResult listQueues() throws ServiceException;

	Topic createTopic(Topic topic) throws ServiceException;
	void deleteTopic(String topicName) throws ServiceException;
	Topic getTopic(String topicName) throws ServiceException;
	ListTopicsResult listTopics() throws ServiceException;

	void addSubscription(String topicName, String subscriptionName, Entry subscription) throws ServiceException;
	void removeSubscription(String topicName, String subscriptionName) throws ServiceException;
	Entry getSubscription(String topicName, String subscriptionName) throws ServiceException;
	Feed getSubscriptions(String topicName) throws ServiceException;

	void addRule(String topicName, String subscriptionName, String ruleName, Entry rule) throws ServiceException;
	void removeRule(String topicName, String subscriptionName, String ruleName) throws ServiceException;
	Entry getRule(String topicName, String subscriptionName, String ruleName) throws ServiceException;
	Feed getRules(String topicName, String subscriptionName) throws ServiceException;
}

