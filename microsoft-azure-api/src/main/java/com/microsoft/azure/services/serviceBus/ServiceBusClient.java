package com.microsoft.azure.services.serviceBus;

import com.microsoft.azure.services.serviceBus.contract.QueueDescription;
import com.microsoft.azure.services.serviceBus.contract.ServiceBusContract;

public class ServiceBusClient  {

	ServiceBusContract contract;

	public ServiceBusClient(ServiceBusContract contract) {
		this.contract = contract;
	}

	public ServiceBusContract getContract() {
		return contract;
	}

	public void setContract(ServiceBusContract contract) {
		this.contract = contract;
	}

	public Queue[] getQueues() {
		QueueDescription[] descriptions = contract.getQueues();
		Queue[] queues = new Queue[descriptions.length];
		for (int i = 0; i != queues.length; ++i) {
			queues[i] = new Queue(this, descriptions[i]);
		}
		return queues;
	}

	public Queue getQueue(String path) {
		return new Queue(this, path);
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
