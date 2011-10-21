package com.microsoft.azure.services.serviceBus.contract;

import org.w3._2005.atom.Entry;
import org.w3._2005.atom.Feed;


public interface ServiceBusContract {
	void sendMessage(String path, BrokeredMessage message);
	BrokeredMessage receiveMessage(String queuePath, int timeout, ReceiveMode receiveMode);
	BrokeredMessage receiveMessage(String topicPath, String subscriptionName, int timeout, ReceiveMode receiveMode);
	void abandonMessage(BrokeredMessage message);
	void completeMessage(BrokeredMessage message);

	void createQueue(Entry queue);
	void deleteQueue(String queuePath);
	Entry getQueue(String queuePath);
	Feed getQueues();

	void createTopic(Entry topic);
	void deleteTopic(String topicPath);
	Entry getTopic(String topicPath);
	Feed getTopics();

	void addSubscription(String topicPath, String subscriptionName, Entry subscription);
	void removeSubscription(String topicPath, String subscriptionName);
	Entry getSubscription(String topicPath, String subscriptionName);
	Feed getSubscriptions(String topicPath);

	void addRule(String topicPath, String subscriptionName, String ruleName, Entry rule);
	void removeRule(String topicPath, String subscriptionName, String ruleName);
	Entry getRule(String topicPath, String subscriptionName, String ruleName);
	Feed getRules(String topicPath, String subscriptionName);
}

