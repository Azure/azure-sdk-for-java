package com.microsoft.azure.services.serviceBus.contract;

import org.codehaus.jackson.annotate.JsonProperty;

public class BrokerProperties {
	@JsonProperty("DeliveryCount")
	int deliveryCount;

	@JsonProperty("MessageId")
	String messageId;
	
	@JsonProperty("SequenceNumber")
	int sequenceNumber;
	
	@JsonProperty("TimeToLive")
	long timeToLive;

	/**
	 * @return the deliveryCount
	 */
	public int getDeliveryCount() {
		return deliveryCount;
	}

	/**
	 * @param deliveryCount the deliveryCount to set
	 */
	public void setDeliveryCount(int deliveryCount) {
		this.deliveryCount = deliveryCount;
	}

	/**
	 * @return the messageId
	 */
	public String getMessageId() {
		return messageId;
	}

	/**
	 * @param messageId the messageId to set
	 */
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	/**
	 * @return the sequenceNumber
	 */
	public int getSequenceNumber() {
		return sequenceNumber;
	}

	/**
	 * @param sequenceNumber the sequenceNumber to set
	 */
	public void setSequenceNumber(int sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	/**
	 * @return the timeToLive
	 */
	public long getTimeToLive() {
		return timeToLive;
	}

	/**
	 * @param timeToLive the timeToLive to set
	 */
	public void setTimeToLive(long timeToLive) {
		this.timeToLive = timeToLive;
	}

	
}
