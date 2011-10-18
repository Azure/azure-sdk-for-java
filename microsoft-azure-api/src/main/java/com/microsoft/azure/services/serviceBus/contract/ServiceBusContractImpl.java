package com.microsoft.azure.services.serviceBus.contract;

import javax.inject.Inject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.api.representation.Form;
import com.sun.syndication.feed.atom.Entry;

public class ServiceBusContractImpl implements ServiceBusContract  {

	public class AtomFilter extends ClientFilter {

		@Override
		public ClientResponse handle(ClientRequest cr)
				throws ClientHandlerException {
			
			return getNext().handle(cr);
		}

	}

	private Client channel;

	@Inject
	public ServiceBusContractImpl(Client channel) {
		channel.addFilter(new AtomFilter());
		this.setChannel(channel);
	}
	

	public Client getChannel() {
		return channel;
	}



	public void setChannel(Client channel) {
		this.channel = channel;
	}


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

	public void createQueue(EntryModel<QueueDescription> entryModel) {
		Form form = new Form();
		form.add("wrap_name", "owner");
		form.add("wrap_password", "Zo3QCZ5jLlJofibEiifZyz7B3x6a5Suv2YoS1JAWopA=");
		form.add("wrap_scope", "http://lodejard.servicebus.windows.net/");
		
		Form wrapResponse = getChannel().resource("https://lodejard-sb.accesscontrol.windows.net/")
			.path("WRAPv0.9")
			.post(Form.class, form);
		String accessToken = wrapResponse.get("wrap_access_token").get(0);
		
		Entry entry = new Entry();
		getChannel().resource("https://lodejard.servicebus.windows.net/")
			.path(entryModel.getEntry().getTitle())
			.header("Authorization", "WRAP access_token=\"" + accessToken + "\"")
			.type("application/atom+xml")
			.put(entry);
	}

	public void deleteQueue(String queuePath) {
		
	}

	public EntryModel<QueueDescription> getQueue(String queuePath) {
		Form form = new Form();
		form.add("wrap_name", "owner");
		form.add("wrap_password", "Zo3QCZ5jLlJofibEiifZyz7B3x6a5Suv2YoS1JAWopA=");
		form.add("wrap_scope", "http://lodejard.servicebus.windows.net/");
		
		Form wrapResponse = getChannel().resource("https://lodejard-sb.accesscontrol.windows.net/")
			.path("WRAPv0.9")
			.post(Form.class, form);
		String accessToken = wrapResponse.get("wrap_access_token").get(0);
		
		GenericType<EntryModel<QueueDescription>> genericType = new GenericType<EntryModel<QueueDescription>>() { };
			
		return getChannel().resource("https://lodejard.servicebus.windows.net/")
			.path(queuePath)
			.header("Authorization", "WRAP access_token=\"" + accessToken + "\"")
			.get(genericType);
	}


	public void createTopic(String topicPath, TopicDescription description) {
		// TODO Auto-generated method stub

	}

	public void deleteTopic(String topicPath) {
		// TODO Auto-generated method stub

	}


	public void addSubscription(String topicPath, String subscriptionName,
			SubscriptionDescription description) {
		// TODO Auto-generated method stub

	}

	public void removeSubscription(String topicPath, String subscriptionName) {
		// TODO Auto-generated method stub

	}


	public void addRule(String topicPath, String subscriptionName,
			String ruleName, RuleDescription description) {
		// TODO Auto-generated method stub

	}

	public void removeRule(String topicPath, String subscriptionName,
			String ruleName) {
		// TODO Auto-generated method stub

	}





	public void createTopic(EntryModel<TopicDescription> topic) {
		// TODO Auto-generated method stub
		
	}


	public void addSubscription(String topicPath, String subscriptionName,
			EntryModel<SubscriptionDescription> subscription) {
		// TODO Auto-generated method stub
		
	}


	public void addRule(String topicPath, String subscriptionName,
			String ruleName, EntryModel<RuleDescription> rule) {
		// TODO Auto-generated method stub
		
	}


	public EntryModel<QueueDescription>[] getQueues() {
		// TODO Auto-generated method stub
		return null;
	}


	public EntryModel<TopicDescription> getTopic(String topicPath) {
		// TODO Auto-generated method stub
		return null;
	}


	public EntryModel<TopicDescription>[] getTopics() {
		// TODO Auto-generated method stub
		return null;
	}


	public EntryModel<SubscriptionDescription> getSubscription(
			String topicPath, String subscriptionName) {
		// TODO Auto-generated method stub
		return null;
	}


	public EntryModel<SubscriptionDescription>[] getSubscriptions(
			String topicPath) {
		// TODO Auto-generated method stub
		return null;
	}


	public EntryModel<RuleDescription> getRule(String topicPath,
			String subscriptionName, String ruleName) {
		// TODO Auto-generated method stub
		return null;
	}


	public EntryModel<RuleDescription>[] getRules(String topicPath,
			String subscriptionName) {
		// TODO Auto-generated method stub
		return null;
	}

}
