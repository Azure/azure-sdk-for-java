package com.microsoft.azure.services.serviceBus;


import javax.ws.rs.core.MediaType;
import javax.xml.datatype.Duration;

import com.microsoft.azure.services.serviceBus.schema.Content;
import com.microsoft.azure.services.serviceBus.schema.Entry;

import com.microsoft.azure.services.serviceBus.schema.QueueDescription;

public class Queue extends EntryModel<QueueDescription> {

	public Queue() {
		super(new Entry(), new QueueDescription());
		entry.setContent(new Content());
		entry.getContent().setType(MediaType.APPLICATION_XML);
	}

	public Queue(Entry entry) {
		super(entry, entry.getContent().getQueueDescription());
	}


	public String getName() {
		return entry.getTitle();
	}

	public Queue setName(String value) {
		entry.setTitle(value);
		return this;
	}

	public Duration getLockDuration() {
		return model.getLockDuration();
	}

	public Queue setLockDuration(Duration value) {
		model.setLockDuration(value);
		return this;
	}

	public Long getMaxSizeInMegabytes() {
		return model.getMaxSizeInMegabytes();
	}

	public Queue setMaxSizeInMegabytes(Long value) {
		model.setMaxSizeInMegabytes(value);
		return this;
	}

	public Queue setRequiresDuplicateDetection(Boolean value) {
		model.setRequiresDuplicateDetection(value);
		return this;
	}

	public Queue setRequiresSession(Boolean value) {
		model.setRequiresSession(value);
		return this;
	}

	public Duration getDefaultMessageTimeToLive() {
		return model.getDefaultMessageTimeToLive();
	}

	public Queue setDefaultMessageTimeToLive(Duration value) {
		model.setDefaultMessageTimeToLive(value);
		return this;
	}

	public Queue setDeadLetteringOnMessageExpiration(Boolean value) {
		model.setDeadLetteringOnMessageExpiration(value);
		return this;
	}

	public Duration getDuplicateDetectionHistoryTimeWindow() {
		return model.getDuplicateDetectionHistoryTimeWindow();
	}

	public Queue setDuplicateDetectionHistoryTimeWindow(Duration value) {
		model.setDuplicateDetectionHistoryTimeWindow(value);
		return this;
	}

	public Integer getMaxDeliveryCount() {
		return model.getMaxDeliveryCount();
	}

	public Queue setMaxDeliveryCount(Integer value) {
		model.setMaxDeliveryCount(value);
		return this;
	}

	public Queue setEnableBatchedOperations(Boolean value) {
		model.setEnableBatchedOperations(value);
		return this;
	}

	public Long getSizeInBytes() {
		return model.getSizeInBytes();
	}

	public Queue setSizeInBytes(Long value) {
		model.setSizeInBytes(value);
		return this;
	}

	public Long getMessageCount() {
		return model.getMessageCount();
	}

	public Queue setMessageCount(Long value) {
		model.setMessageCount(value);
		return this;
	}
}
