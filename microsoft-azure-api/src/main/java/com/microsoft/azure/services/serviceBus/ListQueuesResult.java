package com.microsoft.azure.services.serviceBus;

import java.util.List;

public class ListQueuesResult {
	
	private List<Queue> queues;

	List<Queue> getQueues() {
		return queues;
	}

	public void setQueues(List<Queue> queues) {
		this.queues = queues;
	}
}
