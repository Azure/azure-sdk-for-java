package com.microsoft.azure.servicebus;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public interface IMessage {
	public long getDeliveryCount();
	
	public String getMessageId();

	public void setMessageId(String messageId);

	public Duration getTimeToLive();

	public void setTimeToLive(Duration timeToLive);
	
	public String getContentType();
	
	public void setContentType(String contentType);
	
	public Instant getExpiresAtUtc();
	
	public Instant getLockedUntilUtc();

	public Instant getEnqueuedTimeUtc();
	
	public Instant getScheduledEnqueuedTimeUtc();
	
	public void setScheduledEnqueuedTimeUtc(Instant scheduledEnqueueTimeUtc);
	
	public long getSequenceNumber();
	
	public String getSessionId();
	
	public void setSessionId(String sessionId);
	
	public byte[] getBody();
	
	public void setBody(byte[] body);
	
	public Map<String, String> getProperties();	

	void setProperties(Map<String, String> properties);
	
	public String getCorrelationId();
	
	public void setCorrelationId(String correlationId);
	
	public String getTo();
	
	public void setTo(String to);
	
	public String getReplyTo();
	
	public void setReplyTo(String replyTo);
	
	public String getLabel();
	
	public void setLabel(String label);
	
	public String getReplyToSessionId();
	
	public void setReplyToSessionId(String replyToSessionId);	
	
	public String getPartitionKey();
	
	public void setPartitionKey(String partitionKey);	
	
	public String getDeadLetterSource();
	
	public UUID getLockToken();	
}
