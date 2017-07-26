package com.microsoft.azure.servicebus;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a message that is exchanged between Azure Service Bus and clients.
 * @since 1.0
 *
 */
public interface IMessage {
    /**
     * Gets the number of the times this message was delivered to clients.
     * @return delivery count of this message.
     */
	public long getDeliveryCount();
	
	/**
	 * Gets the Id of this message. Clients can explicitly set an Id for a message. Otherwise, the service generates a GUID as message id.
	 * @return Id of this message
	 */
	public String getMessageId();

	/**
	 * Sets the Id of this message.
	 * @param messageId Id of this message
	 */
	public void setMessageId(String messageId);

	/**
	 * Gets the duration of time before this message expires. An expired message is deleted from the queue or subscription.
	 * @return Time to Live duration of this message
	 */
	public Duration getTimeToLive();

	/**
	 * Sets the duration of time before this message expires. An expired message is deleted from the queue or subscription.
	 * @param timeToLive Time to Live duration of this message
	 */
	public void setTimeToLive(Duration timeToLive);
	
	/**
	 * Gets the content type of this message.
	 * @return content type of this message
	 */
	public String getContentType();
	
	/**
	 * Sets the content type of this message.
	 * @param contentType content type of this message
	 */
	public void setContentType(String contentType);
	
	/**
	 * Gets the instant at which this message will expire. This method is just another form of {@link getTimeToLive}
	 * @return instant at which this message expires
	 */
	public Instant getExpiresAtUtc();
	
	/**
	 * Gets the instant at which the lock of this message expires. When a message is received in {@link ReceiveMode#PEEKLOCK} mode, it is locked by Azure Service Bus for the duration of lock duration
	 * specified in the entity description. If the message is not completed or abandoned or deferred or deadlettered before the lock expires, the message is made available to other receivers on lock expiration.
	 * @return the instant at which the lock of this message expires if the message is received using PEEKLOCK mode. Otherwise it returns null.
	 */
	public Instant getLockedUntilUtc();

	/**
	 * Gets the instant at which this message was enqueued in Azure Service Bus. A normal message is enqueued as soon as it is sent to Azure Service Bus. A scheduled message is enqueued at its scheduled enqueue time.
	 * @return the instant at which the message was enqueued in Azure Service Bus
	 */
	public Instant getEnqueuedTimeUtc();
	
	/**
	 * Gets the scheduled enqueue time of this message. A message sent with scheduled enqueue time will be enqueued in Azure Service Bus and made available to receivers only at the given enqueue time.
	 * @return the instant at which the message will be enqueued in Azure Service Bus
	 */
	public Instant getScheduledEnqueuedTimeUtc();
	
	/**
	 * Sets the instant at which this message should be enqueued in Azure Service Bus. A message sent with scheduled enqueue time will be enqueued in Azure Service Bus and made available to receivers only at the given enqueue time.
	 * @param scheduledEnqueueTimeUtc the instant at which this message should be enqueued in Azure Service Bus
	 */
	public void setScheduledEnqueuedTimeUtc(Instant scheduledEnqueueTimeUtc);
	
	/**
	 * Get the sequence number of this message. Azure Service Bus assigns a unique sequence number to every message enqueued in an entity.
	 * @return sequence number of this message
	 */
	public long getSequenceNumber();
	
	/**
	 * Gets the session id of this message, if it has one.
	 * @return session id of this message
	 */
	public String getSessionId();
	
	/**
	 * Sets the session id of this message. A session id string identifies a session within an entity in Azure Service Bus. All messages with the same session id belong to the same session.
	 * @param sessionId session id of this message
	 */
	public void setSessionId(String sessionId);
	
	/**
	 * Gets the body of this message as a byte array. It is up to client applications to decode the bytes.
	 * @return body of this message
	 */
	public byte[] getBody();
	
	/**
	 * Sets the body of this message as a byte array.
	 * @param body body of this message
	 */
	public void setBody(byte[] body);
	
	/**
	 * Gets the map of custom application properties of this message. Client applications can set custom properties on the message using this map.
	 * @return the map of custom application properties of this message
	 */
	public Map<String, String> getProperties();	

	/**
	 * Sets the map of custom application properties of this message. Client applications can set custom properties on the message using this map.
	 * @param properties the map of custom application properties of this message
	 */
	void setProperties(Map<String, String> properties);
	
	/**
	 * Gets the correlationId property of this message. Clients can set a correlation id on a message and use it in correlation filters.
	 * @return correlation Id of this message
	 */
	public String getCorrelationId();
	
	/**
	 * Sets the correlationId property of this message. Clients can set a correlation id on a message and use it in correlation filters.
	 * @param correlationId correlation Id of this message
	 */
	public void setCorrelationId(String correlationId);
	
	/**
	 * Gets the To property of this message. Clients use this property in correlation filters.
	 * @return To property value of this message
	 */
	public String getTo();
	
	/**
	 * Sets the To property of this message. Clients use this property in correlation filters.
	 * @param to To property value of this message
	 */
	public void setTo(String to);
	
	/**
	 * Gets the ReplyTo property of this message. Clients use this property in correlation filters.
	 * @return ReplyTo property value of this message
	 */
	public String getReplyTo();
	
	/**
	 * Sets the ReplyTo property of this message. Clients use this property in correlation filters.
	 * @param replyTo ReplyTo property value of this message
	 */
	public void setReplyTo(String replyTo);
	
	/**
	 * Gets the Label property of this message. Clients use this property in correlation filters.
	 * @return Label property value of this message
	 */
	public String getLabel();
	
	/**
	 * Sets the Label property of this message. Clients use this property in correlation filters.
	 * @param label Label property value of this message
	 */
	public void setLabel(String label);
	
	/**
	 * Gets the ReplyToSessionId property of this message. Clients use this property in correlation filters.
	 * @return ReplyToSessionId property value of this message
	 */
	public String getReplyToSessionId();
	
	/**
	 * Sets the ReplyToSessionId property of this message. Clients use this property in correlation filters.
	 * @param replyToSessionId ReplyToSessionId property value of this message
	 */
	public void setReplyToSessionId(String replyToSessionId);	
	
	/**
	 * Gets the partition key of this message. If this message is sent to a partitioned entity, this partition key is hashed to one partition of the entity, making all messages with the same partition key going to the same partition of the entity.
	 * @return partition key of this message
	 */
	public String getPartitionKey();
	
	/**
	 * Sets the partition key of this message. If this message is sent to a partitioned entity, this partition key is hashed to one partition of the entity, making all messages with the same partition key going to the same partition of the entity.
	 * @param partitionKey partition key of this message
	 */
	public void setPartitionKey(String partitionKey);	
	
	/**
	 * Gets the dead letter source of this message, if this message is a deadlettered message. This property helps to know what caused this message to be deadlettered.
	 * @return dead letter source of this message
	 */
	public String getDeadLetterSource();
	
	/**
	 * Gets the lock token of this message, if this message is received using {@link ReceiveMode#PEEKLOCK} mode. This token is used to complete or abandon or deadletter or defer this message.
	 * @return lock token of this message if this message is received using PEEKLOCK mode. Otherwise it returns a token of all zeroes.
	 */
	public UUID getLockToken();	
}
