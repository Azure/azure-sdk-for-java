package com.microsoft.azure.services.serviceBus.contract;

import org.codehaus.jackson.annotate.JsonGetter;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonWriteNullProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
@JsonWriteNullProperties(false)
public class BrokerProperties {
	@JsonProperty("DeliveryCount")
	Integer deliveryCount;

	@JsonProperty("MessageId")
	String messageId;
	
	@JsonProperty("SequenceNumber")
	Long sequenceNumber;
	
	@JsonProperty("TimeToLive")
	Long timeToLive;

	/**
	 * @return the deliveryCount
	 */
	@JsonIgnore
	public Integer getDeliveryCount() {
		return deliveryCount;
	}

	/**
	 * @param deliveryCount the deliveryCount to set
	 */
	public void setDeliveryCount(Integer deliveryCount) {
		this.deliveryCount = deliveryCount;
	}

	/**
	 * @return the messageId
	 */
	@JsonIgnore
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
	@JsonIgnore
	public Long getSequenceNumber() {
		return sequenceNumber;
	}

	/**
	 * @param sequenceNumber the sequenceNumber to set
	 */
	public void setSequenceNumber(Long sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	/**
	 * @return the timeToLive
	 */
	@JsonIgnore
	public Long getTimeToLive() {
		return timeToLive;
	}

	/**
	 * @param timeToLive the timeToLive to set
	 */
	public void setTimeToLive(Long timeToLive) {
		this.timeToLive = timeToLive;
	}

	
}
