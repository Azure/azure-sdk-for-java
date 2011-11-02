package com.microsoft.azure.services.serviceBus;

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

	@JsonProperty("LockToken")
	String lockToken;
	
	@JsonProperty("LockedUntilUtc")
	String lockedUntilUtc;
	
	
	@JsonIgnore
	public Integer getDeliveryCount() {
		return deliveryCount;
	}

	public void setDeliveryCount(Integer deliveryCount) {
		this.deliveryCount = deliveryCount;
	}

	@JsonIgnore
	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	@JsonIgnore
	public Long getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(Long sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	@JsonIgnore
	public Long getTimeToLive() {
		return timeToLive;
	}

	public void setTimeToLive(Long timeToLive) {
		this.timeToLive = timeToLive;
	}

	@JsonIgnore
	public String getLockToken() {
		return lockToken;
	}

	public void setLockToken(String lockToken) {
		this.lockToken = lockToken;
	}

	@JsonIgnore
	public String getLockedUntilUtc() {
		return lockedUntilUtc;
	}

	public void setLockedUntilUtc(String lockedUntilUtc) {
		this.lockedUntilUtc = lockedUntilUtc;
	}

	
}
