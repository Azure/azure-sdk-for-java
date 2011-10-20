package com.microsoft.azure.services.serviceBus;

import org.w3._2005.atom.Entry;

import com.microsoft.azure.services.serviceBus.contract.EntryModel;
import com.microsoft.azure.services.serviceBus.contract.ServiceBusContract;

public class Entity<T> {

	protected ServiceBusClient client;
	private Entry entry;

	public Entity(ServiceBusClient client) {
		this.client = client;
		setEntry(new org.w3._2005.atom.Entry());
	}
	
	public Entity(ServiceBusClient client, Entry entry) {
		this.client = client;
		this.entry = entry;
	}
	
	protected ServiceBusClient getClient() {
		return client;
	}
	
	protected ServiceBusContract getContract() {
		return getClient().getContract();
	}


	protected Entry getEntry() {
		return entry;
	}

	protected void setEntry(Entry entry) {
		this.entry = entry;
	}


}