package com.microsoft.azure.services.serviceBus;

import javax.inject.Inject;

import com.microsoft.azure.configuration.Configuration;
import com.microsoft.azure.services.serviceBus.contract.EntryModel;
import com.microsoft.azure.services.serviceBus.contract.QueueDescription;
import com.microsoft.azure.services.serviceBus.contract.ServiceBusContract;

public class ServiceBusClient  {

	ServiceBusContract contract;

	public ServiceBusClient() {
	}
	
	public ServiceBusClient(Configuration configuration) throws Exception {
		this.contract = configuration.build(ServiceBusContract.class);
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

	public Entity[] getQueues() {
		EntryModel<QueueDescription>[] descriptions = contract.getQueues();
		Entity[] queues = new Entity[descriptions.length];
		for (int i = 0; i != queues.length; ++i) {
			queues[i] = new Queue(this, null);
			queues[i].setEntryModel(descriptions[i]);
		}
		return queues;
	}

	public Queue getQueue(String path) {
		Queue queue = new Queue(this, path);
		queue.setEntryModel(contract.getQueue(path));
		return queue;
	}

	public Queue createQueue(String path) {
		Queue queue = new Queue(this, path);
		queue.create();
		return queue;
	}

	public Topic[] getTopics() {
		return null;
	}
}
