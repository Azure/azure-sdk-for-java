package com.microsoft.azure.services.serviceBus.contract;


public interface ServiceBusContract {
	void sendMessage(String path, BrokeredMessage message);
	BrokeredMessage receiveMessage(String queuePath, int timeout, ReceiveMode receiveMode);
	BrokeredMessage receiveMessage(String topicPath, String subscriptionName, int timeout, ReceiveMode receiveMode);
	void abandonMessage(BrokeredMessage message);
	void completeMessage(BrokeredMessage message);

	void createQueue(EntryModel<QueueDescription> queue);
	void deleteQueue(String queuePath);
	EntryModel<QueueDescription> getQueue(String queuePath);
	EntryModel<QueueDescription>[] getQueues();

	void createTopic(EntryModel<TopicDescription> topic);
	void deleteTopic(String topicPath);
	EntryModel<TopicDescription> getTopic(String topicPath);
	EntryModel<TopicDescription>[] getTopics();

	void addSubscription(String topicPath, String subscriptionName, EntryModel<SubscriptionDescription> subscription);
	void removeSubscription(String topicPath, String subscriptionName);
	EntryModel<SubscriptionDescription> getSubscription(String topicPath, String subscriptionName);
	EntryModel<SubscriptionDescription>[] getSubscriptions(String topicPath);

	void addRule(String topicPath, String subscriptionName, String ruleName, EntryModel<RuleDescription> rule);
	void removeRule(String topicPath, String subscriptionName, String ruleName);
	EntryModel<RuleDescription> getRule(String topicPath, String subscriptionName, String ruleName);
	EntryModel<RuleDescription> [] getRules(String topicPath, String subscriptionName);
}

