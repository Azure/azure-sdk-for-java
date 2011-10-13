package com.microsoft.azure.services.serviceBus;

import java.net.URI;

import com.microsoft.azure.http.ClientBase;
import com.microsoft.azure.services.serviceBus.model.QueueDescription;
import com.microsoft.azure.services.serviceBus.model.RuleDescription;
import com.microsoft.azure.services.serviceBus.model.SubscriptionDescription;
import com.microsoft.azure.services.serviceBus.model.TopicDescription;
import com.sun.jersey.api.client.Client;

class ServiceBusClientImpl extends ClientBase implements ServiceBusClient  {

	public ServiceBusClientImpl(Client client, URI url)  {
		super(client, url);
	}

	public void sendMessage(String path, BrokeredMessage message) {
		// TODO Auto-generated method stub

	}

	public BrokeredMessage receiveMessage(String queuePath, int timeout,
			RECEIVE_MODE receiveMode) {
		// TODO Auto-generated method stub
		return null;
	}

	public BrokeredMessage receiveMessage(String topicPath,
			String subscriptionName, int timeout, RECEIVE_MODE receiveMode) {
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
		resource().path(queuePath).put(description);
	}

	public void deleteQueue(String queuePath) {
		resource().path(queuePath).delete();
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
