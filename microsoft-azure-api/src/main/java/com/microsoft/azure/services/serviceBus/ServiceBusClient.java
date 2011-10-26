package com.microsoft.azure.services.serviceBus;


import java.util.ArrayList;

import javax.inject.Inject;

import org.w3._2005.atom.Feed;

import com.microsoft.azure.configuration.Configuration;
import com.microsoft.azure.services.serviceBus.contract.ServiceBusContract;

public class ServiceBusClient  {

	ServiceBusContract contract;

	public ServiceBusClient() throws Exception {
		this("", Configuration.load());
	}
	
	public ServiceBusClient(Configuration config) throws Exception {
		this("", config);
	}

	public ServiceBusClient(String profile) throws Exception {
		this(profile, Configuration.load());
	}
	
	public ServiceBusClient(String profile, Configuration config) throws Exception {
		this(config.create(profile, ServiceBusContract.class));
	}

	@Inject
	public ServiceBusClient(ServiceBusContract contract) {
		this.contract = contract;
	}

	public ServiceBusContract getContract() {
		return contract;
	}

	public void setContract(ServiceBusContract contract) {
		this.contract = contract;
	}

	public Iterable<Queue> listQueues() {
		return listQueues(ListQueuesOptions.DEFAULT);
	}
	
	// REVIEW: what is the generalized strategy for paginated, client-roundtrippable iteration
	public Iterable<Queue> listQueues(ListQueuesOptions options) {
		Feed descriptions = contract.getQueues();
		ArrayList<Queue> queues = new ArrayList<Queue>();
		for (int i = 0; i != descriptions.getEntries().size(); ++i) {
			queues.add(new Queue(this, descriptions.getEntries().get(i)));
		}
		return queues;
	}

	public Queue getQueue(String path) {
		return new Queue(this, path);
	}

	public Iterable<Topic> listTopics() {
		return null;
	}
}
