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

	public Queue(Entry entry, QueueDescription model) {
		super(entry, model);
	}

	public String getTitle() {
		return entry.getTitle();
	}

	public Queue setTitle(String value) {
		entry.setTitle(value);
		return this;
	}

	public Duration getLockDuration() {
		return model.getLockDuration();
	}

	public void setLockDuration(Duration value) {
		model.setLockDuration(value);
	}

	public Long getMaxSizeInMegabytes() {
		return model.getMaxSizeInMegabytes();
	}

	public void setMaxSizeInMegabytes(Long value) {
		model.setMaxSizeInMegabytes(value);
	}

	public void setRequiresDuplicateDetection(Boolean value) {
		model.setRequiresDuplicateDetection(value);
	}

	public void setRequiresSession(Boolean value) {
		model.setRequiresSession(value);
	}

	public Duration getDefaultMessageTimeToLive() {
		return model.getDefaultMessageTimeToLive();
	}

	public void setDefaultMessageTimeToLive(Duration value) {
		model.setDefaultMessageTimeToLive(value);
	}

	public void setDeadLetteringOnMessageExpiration(Boolean value) {
		model.setDeadLetteringOnMessageExpiration(value);
	}

	public Duration getDuplicateDetectionHistoryTimeWindow() {
		return model.getDuplicateDetectionHistoryTimeWindow();
	}

	public void setDuplicateDetectionHistoryTimeWindow(Duration value) {
		model.setDuplicateDetectionHistoryTimeWindow(value);
	}

	public Integer getMaxDeliveryCount() {
		return model.getMaxDeliveryCount();
	}

	public void setMaxDeliveryCount(Integer value) {
		model.setMaxDeliveryCount(value);
	}

	public void setEnableBatchedOperations(Boolean value) {
		model.setEnableBatchedOperations(value);
	}

	public Long getSizeInBytes() {
		return model.getSizeInBytes();
	}

	public void setSizeInBytes(Long value) {
		model.setSizeInBytes(value);
	}

	public Long getMessageCount() {
		return model.getMessageCount();
	}

	public void setMessageCount(Long value) {
		model.setMessageCount(value);
	}
}
