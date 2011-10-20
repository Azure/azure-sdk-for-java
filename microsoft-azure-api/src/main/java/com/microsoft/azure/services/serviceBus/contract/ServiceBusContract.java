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

