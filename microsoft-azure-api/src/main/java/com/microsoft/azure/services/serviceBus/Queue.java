package com.microsoft.azure.services.serviceBus;


import javax.ws.rs.core.MediaType;
import javax.xml.datatype.Duration;

import com.microsoft.azure.services.serviceBus.implementation.Content;
import com.microsoft.azure.services.serviceBus.implementation.Entry;
import com.microsoft.azure.services.serviceBus.implementation.EntryModel;
import com.microsoft.azure.services.serviceBus.implementation.QueueDescription;


public class Queue extends EntryModel<QueueDescription> {

	public Queue() {
		super(new Entry(), new QueueDescription());
		getEntry().setContent(new Content());
		getEntry().getContent().setType(MediaType.APPLICATION_XML);
		getEntry().getContent().setQueueDescription(getModel());
	}

	public Queue(Entry entry) {
		super(entry, entry.getContent().getQueueDescription());
	}


	public String getName() {
		return getEntry().getTitle();
	}

	public Queue setName(String value) {
		getEntry().setTitle(value);
		return this;
	}

	public Duration getLockDuration() {
		return getModel().getLockDuration();
	}

	public Queue setLockDuration(Duration value) {
		getModel().setLockDuration(value);
		return this;
	}

	public Long getMaxSizeInMegabytes() {
		return getModel().getMaxSizeInMegabytes();
	}

	public Queue setMaxSizeInMegabytes(Long value) {
		getModel().setMaxSizeInMegabytes(value);
		return this;
	}

	public Queue setRequiresDuplicateDetection(Boolean value) {
		getModel().setRequiresDuplicateDetection(value);
		return this;
	}

	public Queue setRequiresSession(Boolean value) {
		getModel().setRequiresSession(value);
		return this;
	}

	public Duration getDefaultMessageTimeToLive() {
		return getModel().getDefaultMessageTimeToLive();
	}

	public Queue setDefaultMessageTimeToLive(Duration value) {
		getModel().setDefaultMessageTimeToLive(value);
		return this;
	}

	public Queue setDeadLetteringOnMessageExpiration(Boolean value) {
		getModel().setDeadLetteringOnMessageExpiration(value);
		return this;
	}

	public Duration getDuplicateDetectionHistoryTimeWindow() {
		return getModel().getDuplicateDetectionHistoryTimeWindow();
	}

	public Queue setDuplicateDetectionHistoryTimeWindow(Duration value) {
		getModel().setDuplicateDetectionHistoryTimeWindow(value);
		return this;
	}

	public Integer getMaxDeliveryCount() {
		return getModel().getMaxDeliveryCount();
	}

	public Queue setMaxDeliveryCount(Integer value) {
		getModel().setMaxDeliveryCount(value);
		return this;
	}

	public Queue setEnableBatchedOperations(Boolean value) {
		getModel().setEnableBatchedOperations(value);
		return this;
	}

	public Long getSizeInBytes() {
		return getModel().getSizeInBytes();
	}

	public Queue setSizeInBytes(Long value) {
		getModel().setSizeInBytes(value);
		return this;
	}

	public Long getMessageCount() {
		return getModel().getMessageCount();
	}

	public Queue setMessageCount(Long value) {
		getModel().setMessageCount(value);
		return this;
	}
}
