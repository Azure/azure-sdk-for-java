package com.microsoft.azure.services.serviceBus.messaging;

import javax.ws.rs.core.MediaType;

import com.microsoft.azure.services.serviceBus.schema.Content;
import com.microsoft.azure.services.serviceBus.schema.Entry;

import com.microsoft.azure.services.serviceBus.ServiceBusService;

abstract class AbstractEntity {

	ServiceBusClient client;
	Entry entry;

	AbstractEntity(ServiceBusClient client) {
		this.client = client;
		setEntry(new Entry());
		getEntry().setContent(new Content());
		getEntry().getContent().setType(MediaType.APPLICATION_XML);
	}

	AbstractEntity(ServiceBusClient client, Entry entry) {
		this.client = client;
		this.entry = entry;
	}

	protected ServiceBusClient getClient() {
		return client;
	}

	protected ServiceBusService getContract() {
		return getClient().getContract();
	}

	protected Entry getEntry() {
		return entry;
	}

	protected void setEntry(Entry entry) {
		this.entry = entry;
	}
}
