package com.microsoft.azure.services.serviceBus;

import com.microsoft.azure.services.serviceBus.contract.EntryModel;
import com.microsoft.azure.services.serviceBus.contract.ServiceBusContract;
import com.sun.syndication.feed.atom.Entry;

public class Entity<T> {

	protected ServiceBusClient client;
	private EntryModel<T> entryModel;

	public Entity(ServiceBusClient client) {
		this.client = client;
		this.entryModel = new EntryModel<T>();
		setEntry(new Entry());
	}
	
	public Entity(ServiceBusClient client, EntryModel<T> entryModel) {
		this.client = client;
		this.entryModel = entryModel;
	}
	
	protected ServiceBusClient getClient() {
		return client;
	}
	
	protected ServiceBusContract getContract() {
		return getClient().getContract();
	}

	protected EntryModel<T> getEntryModel() {
		return entryModel;
	}

	protected void setEntryModel(EntryModel<T> entryModel) {
		this.entryModel = entryModel;
	}

	protected Entry getEntry() {
		return getEntryModel().getEntry();
	}

	protected void setEntry(Entry entry) {
		getEntryModel().setEntry(entry);
	}

	protected T getModel() {
		return getEntryModel().getModel();
	}

	protected void setModel(T model) {
		getEntryModel().setModel(model);
	}

}