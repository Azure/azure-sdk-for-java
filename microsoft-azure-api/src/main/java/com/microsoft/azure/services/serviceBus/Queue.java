package com.microsoft.azure.services.serviceBus;

import com.microsoft.azure.services.serviceBus.contract.BrokeredMessage;
import com.microsoft.azure.services.serviceBus.contract.QueueDescription;
import com.microsoft.azure.services.serviceBus.contract.ReceiveMode;

public class Queue implements MessageSender, MessageReceiver {
	private ServiceBusClient client;
	private String path;
	private QueueDescription description;

	public Queue(ServiceBusClient client, String path) {
		this.client = client;
		this.path = path;
	}

	public Queue(ServiceBusClient client, QueueDescription queueDescription) {
		this.client = client;
		
	}

	public void create() {
		client.getContract().createQueue(path, description);
	}

	public void delete() {
		client.getContract().deleteQueue(path);
	}
	
	public void commit() {
		//TODO protocol operation to put data?
	}


	public void send(BrokeredMessage message) {
		// TODO Auto-generated method stub
		
	}	
	
	public BrokeredMessage receive(int timeout, ReceiveMode receiveMode) {
		// TODO Auto-generated method stub
		return null;
	}

	public void abandon(BrokeredMessage message) {
		// TODO Auto-generated method stub
		
	}

	public void complete(BrokeredMessage message) {
		// TODO Auto-generated method stub
		
	}

}
