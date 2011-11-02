package com.microsoft.azure.services.serviceBus.messaging;


import java.util.ArrayList;

import javax.inject.Inject;

import com.microsoft.azure.services.serviceBus.schema.Feed;

import com.microsoft.azure.ServiceException;
import com.microsoft.azure.configuration.Configuration;
import com.microsoft.azure.services.serviceBus.ServiceBusService;

public class ServiceBusClient  {

	ServiceBusService contract;

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
		this(config.create(profile, ServiceBusService.class));
	}

	@Inject
	public ServiceBusClient(ServiceBusService contract) {
		this.contract = contract;
	}

	public ServiceBusService getContract() {
		return contract;
	}

	public void setContract(ServiceBusService contract) {
		this.contract = contract;
	}

	public Iterable<Queue> listQueues() throws ServiceException {
		return listQueues(ListQueuesOptions.DEFAULT);
	}
	
	// REVIEW: what is the generalized strategy for paginated, client-roundtrippable iteration
	public Iterable<Queue> listQueues(ListQueuesOptions options) throws ServiceException {
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
