package com.microsoft.azure.services.serviceBus.messaging;

public class ListQueuesOptions {

	public static final ListQueuesOptions DEFAULT = new ListQueuesOptions();

	Integer skip;
	public Integer getSkip() {
		return skip;
	}
	public ListQueuesOptions setSkip(Integer skip) {
		this.skip = skip;
		return this;
	}
	public Integer getMaxCount() {
		return maxCount;
	}
	public ListQueuesOptions setMaxCount(Integer maxCount) {
		this.maxCount = maxCount;
		return this;
	}
	Integer maxCount;
	
}
