package com.microsoft.azure.services.serviceBus.contract;

import com.sun.jersey.api.client.Client;

public class ServiceBusContractImpl implements ServiceBusContract  {


	public void sendMessage(String path, BrokeredMessage message) {
		// TODO Auto-generated method stub

	}

	public BrokeredMessage receiveMessage(String queuePath, int timeout,
			ReceiveMode receiveMode) {
		// TODO Auto-generated method stub
		return null;
	}

	public BrokeredMessage receiveMessage(String topicPath,
			String subscriptionName, int timeout, ReceiveMode receiveMode) {
		// TODO Auto-generated method stub
		return null;
	}

	public void abandonMessage(BrokeredMessage message) {
		// TODO Auto-generated method stub

	}

	public void completeMessage(BrokeredMessage message) {
		// TODO Auto-generated method stub

	}

	public void createQueue(String queuePath, QueueDescription description) {
		
	}

	public void deleteQueue(String queuePath) {
		
	}

	public QueueDescription getQueue(String queuePath) {
		// TODO Auto-generated method stub
		return null;
	}

	public QueueDescription[] getQueues() {
		// TODO Auto-generated method stub
		return null;
	}

	public void createTopic(String topicPath, TopicDescription description) {
		// TODO Auto-generated method stub

	}

	public void deleteTopic(String topicPath) {
		// TODO Auto-generated method stub

	}

	public TopicDescription getTopic(String topicPath) {
		// TODO Auto-generated method stub
		return null;
	}

	public TopicDescription[] getTopics() {
		// TODO Auto-generated method stub
		return null;
	}

	public void addSubscription(String topicPath, String subscriptionName,
			SubscriptionDescription description) {
		// TODO Auto-generated method stub

	}

	public void removeSubscription(String topicPath, String subscriptionName) {
		// TODO Auto-generated method stub

	}

	public SubscriptionDescription getSubscription(String topicPath,
			String subscriptionName) {
		// TODO Auto-generated method stub
		return null;
	}

	public SubscriptionDescription[] getSubscriptions(String topicPath) {
		// TODO Auto-generated method stub
		return null;
	}

	public void addRule(String topicPath, String subscriptionName,
			String ruleName, RuleDescription description) {
		// TODO Auto-generated method stub

	}

	public void removeRule(String topicPath, String subscriptionName,
			String ruleName) {
		// TODO Auto-generated method stub

	}

	public RuleDescription getRule(String topicPath, String subscriptionName,
			String ruleName) {
		// TODO Auto-generated method stub
		return null;
	}

	public RuleDescription[] getRules(String topicPath, String subscriptionName) {
		// TODO Auto-generated method stub
		return null;
	}

}
