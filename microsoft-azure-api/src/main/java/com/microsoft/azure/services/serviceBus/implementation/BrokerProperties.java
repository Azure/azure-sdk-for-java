package com.microsoft.azure.services.serviceBus.implementation;

import java.util.Date;

import org.codehaus.jackson.annotate.JsonGetter;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonWriteNullProperties;


//
// members of this class defined per specification at
// http://msdn.microsoft.com/en-us/library/windowsazure/hh367521.aspx#BKMK_REST3prod
//

@JsonIgnoreProperties(ignoreUnknown=true)
@JsonWriteNullProperties(false)
public class BrokerProperties {

	@JsonProperty("CorrelationId")
	String correlationId;
	
	@JsonProperty("SessionId")
	String sessionId;
	
	@JsonProperty("DeliveryCount")
	Integer deliveryCount;

	@JsonProperty("LockedUntil")
	Date lockedUntilUtc;

	@JsonProperty("LockToken")
	String lockToken;

	@JsonProperty("MessageId")
	String messageId;
	
	@JsonProperty("Label")
	String label;
	
	@JsonProperty("ReplyTo")
	String replyTo;

	@JsonProperty("SequenceNumber")
	Long sequenceNumber;

	@JsonProperty("TimeToLive")
	Long timeToLive;

	@JsonProperty("To")
	String to;

	@JsonProperty("ScheduledEnqueueTimeUtc")
	Date scheduledEnqueueTimeUtc;

	@JsonProperty("ReplyToSessionId")
	String replyToSessionId;

	@JsonProperty("MessageLocation")
	String messageLocation;

	@JsonProperty("LockLocation")
	String lockLocation;

	
	
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
	public Date getLockedUntilUtc() {
		return lockedUntilUtc;
	}

	public void setLockedUntilUtc(Date lockedUntilUtc) {
		this.lockedUntilUtc = lockedUntilUtc;
	}

	@JsonIgnore
	public String getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}

	@JsonIgnore
	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	@JsonIgnore
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@JsonIgnore
	public String getReplyTo() {
		return replyTo;
	}

	public void setReplyTo(String replyTo) {
		this.replyTo = replyTo;
	}

	@JsonIgnore
	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	@JsonIgnore
	public Date getScheduledEnqueueTimeUtc() {
		return scheduledEnqueueTimeUtc;
	}

	public void setScheduledEnqueueTimeUtc(Date scheduledEnqueueTimeUtc) {
		this.scheduledEnqueueTimeUtc = scheduledEnqueueTimeUtc;
	}

	@JsonIgnore
	public String getReplyToSessionId() {
		return replyToSessionId;
	}

	public void setReplyToSessionId(String replyToSessionId) {
		this.replyToSessionId = replyToSessionId;
	}

	@JsonIgnore
	public String getMessageLocation() {
		return messageLocation;
	}

	public void setMessageLocation(String messageLocation) {
		this.messageLocation = messageLocation;
	}

	@JsonIgnore
	public String getLockLocation() {
		return lockLocation;
	}

	public void setLockLocation(String lockLocation) {
		this.lockLocation = lockLocation;
	}

	
}
