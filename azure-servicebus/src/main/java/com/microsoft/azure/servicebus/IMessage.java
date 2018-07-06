// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.servicebus;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Represents the message that is exchanged between Azure Service Bus and its clients.
 *
 * @since 1.0
 * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-messages-payloads">Messages, Payloads, and Serialization</a>
 */
public interface IMessage {

    /**
     * Gets the number of the times this message was delivered to clients.
     * 
     * The count is incremented when a message lock expires, or the message is 
     * explicitly abandoned by the receiver. This property is read-only.
     *
     * @return delivery count of this message.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-transfers-locks-settlement">Message transfers, locks, and settlement.</a>
     */
    public long getDeliveryCount();

    /**
     * Gets the Id of this message.
     * 
     * The message identifier is an application-defined value that uniquely identifies the 
     * message and its payload. The identifier is a free-form string and can reflect a GUID 
     * or an identifier derived from the application context. If enabled, the 
     * <a href="https://docs.microsoft.com/azure/service-bus-messaging/duplicate-detection">duplicate detection</a> 
     * feature identifies and removes second and further submissions of messages with the 
     * same MessageId.
     * 
     * @return Id of this message
     */
    public String getMessageId();

    /**
     * Sets the Id of this message.
     * @param messageId Id of this message
     * @see #getMessageId()
     */
    public void setMessageId(String messageId);

    /**
     * Gets the duration before this message expires. 
     * 
     * This value is the relative duration after which the message expires, starting from 
     * the instant the message has been accepted and stored by the broker, as captured in 
     * {@link #getEnqueuedTimeUtc() getEnqueuedTimeUtc}. When not set explicitly, the 
     * assumed value is the DefaultTimeToLive set for the respective queue or topic. 
     * A message-level TimeToLive value cannot be longer than the entity's DefaultTimeToLive 
     * setting and it is silently adjusted if it does. 
     *
     * @return Time to Live duration of this message
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-expiration">Message Expiration</a>  
     */
    public Duration getTimeToLive();

    /**
     * Sets the duration of time before this message expires. 
     *
     * @param timeToLive Time to Live duration of this message
     * @see #getTimeToLive()
     */
    public void setTimeToLive(Duration timeToLive);

    /**
     * Gets the content type of this message.
     *
     * Optionally describes the payload of the message, with a descriptor following the format of 
     * RFC2045, Section 5, for example "application/json".
     * 
     * @return content type of this message
     */
    public String getContentType();

    /**
     * Sets the content type of this message.
     *
     * @param contentType content type of this message
     * @see #getContentType()
     */
    public void setContentType(String contentType);

    /**
     * Gets the instant at which this message will expire. 
     *
     * The value is the UTC instant for when the message is scheduled for removal 
     * and will no longer available for retrieval from the entity due to expiration. 
     * Expiry is controlled by the {@link #getTimeToLive() TimeToLive} property. 
     * This property is computed from {@link #getEnqueuedTimeUtc() EnqueuedTimeUtc}+{@link #getTimeToLive() TimeToLive}.
     * 
     * @return instant at which this message expires
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-expiration">Message Expiration</a>
     */
    public Instant getExpiresAtUtc();

    /**
     * Gets the instant at which the lock of this message expires. 
     * 
     * For messages retrieved under a lock (peek-lock receive mode, not pre-settled) this property reflects the UTC 
     * instant until which the message is held locked in the queue/subscription. When the lock expires, the {@link #getDeliveryCount() DeliveryCount} 
     * is incremented and the message is again available for retrieval. This property is read-only.
            
     * @return the instant at which the lock of this message expires if the message is received using PEEKLOCK mode. Otherwise it returns null.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-transfers-locks-settlement">Message transfers, locks, and settlement</a>
     */
    public Instant getLockedUntilUtc();

    /**
     * Gets the instant at which this message was enqueued in Azure Service Bus.
     * 
     * The UTC instant at which the message has been accepted and stored in the entity. 
     * For scheduled messages, this reflects the time when the message was activated.
     * This value can be used as an authoritative and neutral arrival time indicator when 
     * the receiver does not want to trust the sender's clock. This property is read-only.
     *
     * @return the instant at which the message was enqueued in Azure Service Bus
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-sequencing">Message Sequencing and Timestamps</a> 
     */
    public Instant getEnqueuedTimeUtc();

    /**
     * Gets the scheduled enqueue time of this message. 
     * 
     * This value is used for delayed message availability. The message is safely added to 
     * the queue, but is not considered active and therefore not retrievable until the 
     * scheduled enqueue time. Mind that the message may not be activated (enqueued) at the exact given 
     * instant; the actual activation time depends on the queue's workload and its state.
     *
     * @return the instant at which the message will be enqueued in Azure Service Bus
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-sequencing">Message Sequencing and Timestamps</a> 
     * @deprecated Replaced by {@link #getScheduledEnqueueTimeUtc()
     */
    @Deprecated
    public Instant getScheduledEnqueuedTimeUtc();

    /**
     * Sets the scheduled enqueue time of this message.  
     * 
     * @param scheduledEnqueueTimeUtc the instant at which this message should be enqueued in Azure Service Bus
     * @see #getScheduledEnqueueTimeUtc()
     * @deprecated Replaced by {@link #setScheduledEnqueueTimeUtc(Instant)()
     */
    @Deprecated
    public void setScheduledEnqueuedTimeUtc(Instant scheduledEnqueueTimeUtc);
    
    /**
     * Gets the scheduled enqueue time of this message. 
     * 
     * This value is used for delayed message availability. The message is safely added to 
     * the queue, but is not considered active and therefore not retrievable until the 
     * scheduled enqueue time. Mind that the message may not be activated (enqueued) at the exact given 
     * instant; the actual activation time depends on the queue's workload and its state.
     *
     * @return the instant at which the message will be enqueued in Azure Service Bus
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-sequencing">Message Sequencing and Timestamps</a> 
     */
    public Instant getScheduledEnqueueTimeUtc();

    /**
     * Sets the scheduled enqueue time of this message.  
     * 
     * @param scheduledEnqueueTimeUtc the instant at which this message should be enqueued in Azure Service Bus
     * @see #getScheduledEnqueueTimeUtc()
     */
    public void setScheduledEnqueueTimeUtc(Instant scheduledEnqueueTimeUtc);

    /**
     * Gets the unique number assigned to a message by Service Bus.
     *
     * The sequence number is a unique 64-bit integer assigned to a message as it is accepted 
     * and stored by the broker and functions as its true identifier. For partitioned entities, 
     * the topmost 16 bits reflect the partition identifier. Sequence numbers monotonically increase 
     * and are gapless. They roll over to 0 when the 48-64 bit range is exhausted. This property is read-only.
     * 
     * @return sequence number of this message
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-sequencing">Message Sequencing and Timestamps</a> 
     */
    public long getSequenceNumber();

    /**
     * Gets the session identifier for a session-aware entity.
     *
     * For session-aware entities, this application-defined value specifies the session 
     * affiliation of the message. Messages with the same session identifier are subject 
     * to summary locking and enable exact in-order processing and demultiplexing. 
     * For session-unaware entities, this value is ignored.
     *
     * @return session id of this message
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-sessions">Message Sessions</a> 
     */
    public String getSessionId();

    /**
     * Sets the session identifier for a session-aware entity.
     *
     * @param sessionId session id of this message
     * @see #getSessionId()
     */
    public void setSessionId(String sessionId);

    /**
     * Gets the body of this message as a byte array. It is up to client applications 
     * to decode the bytes.
     *
     * @return body of this message
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-messages-payloads">Messages, payloads, and serialization</a>
     */
    public byte[] getBody();

    /**
     * Sets the body of this message as a byte array.
     *
     * @param body body of this message
     * @see #getBody()
     */
    public void setBody(byte[] body);

    /**
     * Gets the map of user application properties of this message. Client 
     * applications can set user properties (headers) on the message using this map.
     *
     * @return the map of user application properties of this message
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-messages-payloads">Messages, payloads, and serialization</a>
     */
    public Map<String, Object> getProperties();

    /**
     * Sets the map of user application properties of this message. Client applications 
     * can set user properties on the message using this map.
     *
     * @param properties the map of user application properties of this message
     * @see #getProperties()
     */
    void setProperties(Map<String, Object> properties);

    /**
     * Gets a correlation identifier.
     *
     * Allows an application to specify a context for the message for the purposes of correlation, 
     * for example reflecting the MessageId of a message that is being replied to.
     *
     * @return correlation Id of this message
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-messages-payloads?#message-routing-and-correlation">Message Routing and Correlation</a>.
     */
    public String getCorrelationId();

    /**
     * Sets a correlation identifier.
     *
     * @param correlationId correlation Id of this message
     * @see #getCorrelationId()
     */
    public void setCorrelationId(String correlationId);

    /**
     * Gets the "to" address.
     *
     * @return To property value of this message
     */
    public String getTo();

    /**
     * Sets the "to" address.
     *
     * This property is reserved for future use in routing scenarios and presently ignored by the broker itself. 
     * Applications can use this value in rule-driven 
     * <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-auto-forwarding">auto-forward chaining</a> scenarios to indicate the 
     * intended logical destination of the message.
     *   
     * @param to To property value of this message
     */
    public void setTo(String to);

    /**
     * Gets the address of an entity to send replies to.
     * 
     * This optional and application-defined value is a standard way to express a reply path 
     * to the receiver of the message. When a sender expects a reply, it sets the value to the 
     * absolute or relative path of the queue or topic it expects the reply to be sent to.
     *
     * @return ReplyTo property value of this message
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-messages-payloads?#message-routing-and-correlation">Message Routing and Correlation</a>.
     */
    public String getReplyTo();

    /**
     * Sets the address of an entity to send replies to.
     *
     * @param replyTo ReplyTo property value of this message
     * @see #getReplyTo()
     */
    public void setReplyTo(String replyTo);

    /**
     * Gets the application specific message label.
     *
     * This property enables the application to indicate the purpose of the message to the receiver in a standardized 
     * fashion, similar to an email subject line. The mapped AMQP property is "subject".
     *
     * @return Label property value of this message
     */
    public String getLabel();

    /**
     * Sets an application specific message label.
     *
     * @param label Label property value of this message
     * @see #getLabel()
     */
    public void setLabel(String label);

    /**
     * Gets or sets a session identifier augmenting the {@link #getReplyTo() ReplyTo} address.
     *
     * This value augments the ReplyTo information and specifies which SessionId should be set 
     * for the reply when sent to the reply entity. 
     *
     * @return ReplyToSessionId property value of this message
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-messages-payloads?#message-routing-and-correlation">Message Routing and Correlation</a>
     */
    public String getReplyToSessionId();

    /**
     * Gets or sets a session identifier augmenting the {@link #getReplyTo() ReplyTo} address.
     *
     * @param replyToSessionId ReplyToSessionId property value of this message
     */
    public void setReplyToSessionId(String replyToSessionId);

    /**
     * Gets the partition key for sending a message to a partitioned entity.
     * 
     * For <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-partitioning">partitioned entities</a>, 
     * setting this value enables assigning related messages to the same internal partition, so that submission sequence 
     * order is correctly recorded. The partition is chosen by a hash function over this value and cannot be chosen 
     * directly. For session-aware entities, the {@link #getSessionId() SessionId } property overrides this value.
     *
     * @return partition key of this message
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-partitioning">Partitioned entities</a>
     */
    public String getPartitionKey();

    /**
     * Sets a partition key for sending a message to a partitioned entity
     *
     * @param partitionKey partition key of this message
     * @see #getPartitionKey()
     */
    public void setPartitionKey(String partitionKey);

    /**
     * Gets the partition key for sending a message to a entity via another partitioned transfer entity.
     *
     * If a message is sent via a transfer queue in the scope of a transaction, this value selects the
     * transfer queue partition: This is functionally equivalent to {@link #getPartitionKey()} and ensures that
     * messages are kept together and in order as they are transferred.
     *
     * @return partition key on the via queue.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-transactions#transfers-and-send-via">Transfers and Send Via</a>.
     */
    public String getViaPartitionKey();

    /**
     * Sets a via-partition key for sending a message to a destination entity via another partitioned entity
     *
     * @param viaPartitionKey via-partition key of this message
     * @see #getViaPartitionKey()
     */
    public void setViaPartitionKey(String viaPartitionKey);

    /**
     * Gets the name of the queue or subscription that this message was enqueued on, before it was deadlettered.
     * 
     * This value is only set in messages that have been dead-lettered and subsequently auto-forwarded from the 
     * dead-letter queue  to another entity. Indicates the entity in which the message was dead-lettered. This property is read-only.
     *
     * @return dead letter source of this message
     * @see <a href="https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-dead-letter-queues">Dead-letter queues</a>
     */
    public String getDeadLetterSource();

    /**
     * Gets the lock token for the current message.
     *
     * The lock token is a reference to the lock that is being held by the broker in PEEKLOCK mode. 
     * Locks are used to explicitly settle messages as explained in the <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-transfers-locks-settlement">product documentation in more detail</a>.
     * The token can also be used to pin the lock permanently through the <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-deferral">Deferral API</a> and, with that, take the message out of the 
     * regular delivery state flow. This property is read-only.
     *
     * @return lock token of this message. 
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-transfers-locks-settlement">Message transfers, locks, and settlement</a>
     */
    public UUID getLockToken();
}
