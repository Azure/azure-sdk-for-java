package com.microsoft.azure.services.serviceBus.contract;

import java.rmi.UnexpectedException;

import javax.inject.Inject;
import javax.inject.Named;

import org.w3._2005.atom.Entry;
import org.w3._2005.atom.Feed;

import com.microsoft.azure.auth.wrap.WrapFilter;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class ServiceBusContractImpl implements ServiceBusContract {

	private Client channel;
	private String uri;
	private BrokerPropertiesMapper mapper;

	@Inject
	public ServiceBusContractImpl(
			Client channel, WrapFilter authFilter,
			@Named("serviceBus.uri") String uri,
			BrokerPropertiesMapper mapper) {

		this.channel = channel;
		this.uri = uri;
		this.mapper = mapper;
		channel.addFilter(authFilter);
	}

	public Client getChannel() {
		return channel;
	}

	public void setChannel(Client channel) {
		this.channel = channel;
	}

	public void sendMessage(String path, BrokerProperties properties) {
		getResource()
			.path(path)
			.path("messages")
			.header("BrokerProperties", mapper.toString(properties))
			.header("Content-Length", 11)
			.post("Hello world");
	}

	public MessageResult receiveMessage(String queuePath, int timeout,
			ReceiveMode receiveMode) {
		MessageResult result = new MessageResult();
		if (receiveMode == ReceiveMode.RECEIVE_AND_DELETE) {
			ClientResponse clientResult = getResource()
				.path(queuePath)
				.path("messages")
				.path("head")
				.queryParam("timeout", Integer.toString(timeout))
				.delete(ClientResponse.class);
			
			result.setBrokerProperties(mapper.fromString(clientResult.getHeaders().getFirst("BrokerProperties")));
			result.setBody(clientResult.getEntityInputStream());
			return result;
		}
		throw new RuntimeException("Unknown ReceiveMode");
	}


	public Entry createQueue(Entry entry) {
		return getResource()
			.path(entry.getTitle())
			.type("application/atom+xml")//;type=entry;charset=utf-8")
			.put(Entry.class, entry);
	}

	public void deleteQueue(String queuePath) {
		getResource()
			.path(queuePath)
			.delete();
	}

	private WebResource getResource() {
		return getChannel()
			.resource(uri);
	}

	public Entry getQueue(String queuePath) {
		return getResource()
				.path(queuePath)
				.get(Entry.class);
	}

	public Feed getQueues() {
		return getResource()
				.path("$Resources/Queues")
				.get(Feed.class);
	}

	public Entry createTopic(Entry entry) {
		return getResource()
			.path(entry.getTitle())
			.type("application/atom+xml")//;type=entry;charset=utf-8")
			.put(Entry.class, entry);
	}

	public void deleteTopic(String topicPath) {
		getResource()
			.path(topicPath)
			.delete();
	}

	public Entry getTopic(String topicPath) {
		// TODO Auto-generated method stub
		return null;
	}

	public Feed getTopics() {
		// TODO Auto-generated method stub
		return null;
	}

	public void addSubscription(String topicPath, String subscriptionName,
			Entry subscription) {
		// TODO Auto-generated method stub

	}

	public void removeSubscription(String topicPath, String subscriptionName) {
		// TODO Auto-generated method stub

	}

	public Entry getSubscription(String topicPath, String subscriptionName) {
		// TODO Auto-generated method stub
		return null;
	}

	public Feed getSubscriptions(String topicPath) {
		// TODO Auto-generated method stub
		return null;
	}

	public void addRule(String topicPath, String subscriptionName,
			String ruleName, Entry rule) {
		// TODO Auto-generated method stub

	}

	public void removeRule(String topicPath, String subscriptionName,
			String ruleName) {
		// TODO Auto-generated method stub

	}

	public Entry getRule(String topicPath, String subscriptionName,
			String ruleName) {
		// TODO Auto-generated method stub
		return null;
	}

	public Feed getRules(String topicPath, String subscriptionName) {
		// TODO Auto-generated method stub
		return null;
	}


}
