package com.microsoft.azure.services.serviceBus;
import com.microsoft.azure.services.serviceBus.model.*;

public interface ServiceBusClient {
	void sendMessage(String path, BrokeredMessage message);
	BrokeredMessage receiveMessage(String queuePath, int timeout, ReceiveMode receiveMode);
	BrokeredMessage receiveMessage(String topicPath, String subscriptionName, int timeout, ReceiveMode receiveMode);
	void abandonMessage(BrokeredMessage message);
	void completeMessage(BrokeredMessage message);

	void createQueue(String queuePath, QueueDescription description);
	void deleteQueue(String queuePath);
	QueueDescription getQueue(String queuePath);
	QueueDescription[] getQueues();

	void createTopic(String topicPath, TopicDescription description);
	void deleteTopic(String topicPath);
	TopicDescription getTopic(String topicPath);
	TopicDescription[] getTopics();

	void addSubscription(String topicPath, String subscriptionName, SubscriptionDescription description);
	void removeSubscription(String topicPath, String subscriptionName);
	SubscriptionDescription getSubscription(String topicPath, String subscriptionName);
	SubscriptionDescription[] getSubscriptions(String topicPath);

	void addRule(String topicPath, String subscriptionName, String ruleName, RuleDescription description);
	void removeRule(String topicPath, String subscriptionName, String ruleName);
	RuleDescription getRule(String topicPath, String subscriptionName, String ruleName);
	RuleDescription [] getRules(String topicPath, String subscriptionName);
}

