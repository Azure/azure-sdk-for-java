/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.message.Message;

import com.microsoft.azure.servicebus.amqp.AmqpConstants;

/**
 * The data structure encapsulating the Event being sent-to and received-from EventHubs.
 * Each EventHubs partition can be visualized as a Stream of {@link EventData}.
 */
public class EventData implements Serializable
{
	private static final long serialVersionUID = -5631628195600014255L;

	transient private Binary bodyData;
	
	private Map<String, String> properties;
	private SystemProperties systemProperties;

	private EventData()
	{
	}

	/**
	 * Internal Constructor - intended to be used only by the {@link PartitionReceiver} to Create #EventData out of #Message
	 */
	@SuppressWarnings("unchecked")
	EventData(Message amqpMessage)
	{
		if (amqpMessage == null)
		{
			throw new IllegalArgumentException("amqpMessage cannot be null");
		}

		final Map<Symbol, Object> messageAnnotations = amqpMessage.getMessageAnnotations().getValue();
		final HashMap<String, Object> receiveProperties = new HashMap<String, Object>();
		
		for(Map.Entry<Symbol, Object> annotation: messageAnnotations.entrySet())
		{
			receiveProperties.put(annotation.getKey().toString(), annotation.getValue() != null ? annotation.getValue() : null);
		}
		
		if (amqpMessage.getProperties() != null)
		{
			if (amqpMessage.getMessageId() != null) receiveProperties.put(AmqpConstants.AMQP_PROPERTY_MESSAGE_ID, amqpMessage.getMessageId());
			if (amqpMessage.getUserId() != null) receiveProperties.put(AmqpConstants.AMQP_PROPERTY_USER_ID, amqpMessage.getUserId());
			if (amqpMessage.getAddress() != null) receiveProperties.put(AmqpConstants.AMQP_PROPERTY_TO, amqpMessage.getAddress());
			if (amqpMessage.getSubject() != null) receiveProperties.put(AmqpConstants.AMQP_PROPERTY_SUBJECT, amqpMessage.getSubject());
			if (amqpMessage.getReplyTo() != null) receiveProperties.put(AmqpConstants.AMQP_PROPERTY_REPLY_TO, amqpMessage.getReplyTo());
			if (amqpMessage.getCorrelationId() != null) receiveProperties.put(AmqpConstants.AMQP_PROPERTY_CORRELATION_ID, amqpMessage.getCorrelationId());
			if (amqpMessage.getContentType() != null) receiveProperties.put(AmqpConstants.AMQP_PROPERTY_CONTENT_TYPE, amqpMessage.getContentType());
			if (amqpMessage.getContentEncoding() != null) receiveProperties.put(AmqpConstants.AMQP_PROPERTY_CONTENT_ENCODING, amqpMessage.getContentEncoding());
			if (amqpMessage.getProperties().getAbsoluteExpiryTime() != null) receiveProperties.put(AmqpConstants.AMQP_PROPERTY_ABSOLUTE_EXPRITY_time, amqpMessage.getExpiryTime());
			if (amqpMessage.getProperties().getCreationTime() != null) receiveProperties.put(AmqpConstants.AMQP_PROPERTY_CREATION_TIME, amqpMessage.getCreationTime());
			if (amqpMessage.getGroupId() != null) receiveProperties.put(AmqpConstants.AMQP_PROPERTY_GROUP_ID, amqpMessage.getGroupId());
			if (amqpMessage.getProperties().getGroupSequence() != null) receiveProperties.put(AmqpConstants.AMQP_PROPERTY_GROUP_SEQUENCE, amqpMessage.getGroupSequence());
			if (amqpMessage.getReplyToGroupId() != null) receiveProperties.put(AmqpConstants.AMQP_PROPERTY_REPLY_TO_GROUP_ID, amqpMessage.getReplyToGroupId());
		}
		
		this.systemProperties = new SystemProperties(receiveProperties);	
		this.properties = amqpMessage.getApplicationProperties() == null ? null 
				: ((Map<String, String>)(amqpMessage.getApplicationProperties().getValue()));

		this.bodyData = amqpMessage.getBody() == null ? null : ((Data) amqpMessage.getBody()).getValue();

		amqpMessage.clear();
	}

	/**
	 * Construct EventData to Send to EventHubs.
	 * Typical pattern to create a Sending EventData is:
	 * <pre>
	 * i.	Serialize the sending ApplicationEvent to be sent to EventHubs into bytes.
	 * ii.	If complex serialization logic is involved (for example: multiple types of data) - add a Hint using the {@link #getProperties()} for the Consumer.
	 * </pre> 
	 * <p> Sample Code:
	 * <pre>
	 * EventData eventData = new EventData(telemetryEventBytes);
	 * HashMap{@literal <}String, String{@literal >} applicationProperties = new HashMap{@literal <}String, String{@literal >}();
	 * applicationProperties.put("eventType", "com.microsoft.azure.monitoring.EtlEvent");
	 * eventData.setProperties(applicationProperties);
	 * partitionSender.Send(eventData);
	 * </pre>
	 * @param data the actual payload of data in bytes to be Sent to EventHubs.
	 * @see EventHubClient#createFromConnectionString(String)
	 */
	public EventData(byte[] data)
	{
		this();

		if (data == null)
		{
			throw new IllegalArgumentException("data cannot be null");
		}

		this.bodyData = new Binary(data);
	}

	/**
	 * Construct EventData to Send to EventHubs.
	 * Typical pattern to create a Sending EventData is:
	 * <pre>
	 * i.	Serialize the sending ApplicationEvent to be sent to EventHubs into bytes.
	 * ii.	If complex serialization logic is involved (for example: multiple types of data) - add a Hint using the {@link #getProperties()} for the Consumer.
	 *  </pre> 
	 *  <p> Illustration:
	 *  <pre> {@code
	 *  EventData eventData = new EventData(telemetryEventBytes, offset, length);
	 *  HashMap{@literal <}String, String{@literal >} applicationProperties = new HashMap{@literal <}String, String{@literal >}();
	 *  applicationProperties.put("eventType", "com.microsoft.azure.monitoring.EtlEvent");
	 *	eventData.setProperties(applicationProperties);
	 *  partitionSender.Send(eventData);
	 *  }</pre>
	 * @param data the byte[] where the payload of the Event to be sent to EventHubs is present
	 * @param offset Offset in the byte[] to read from ; inclusive index
	 * @param length length of the byte[] to be read, starting from offset
	 * @see EventHubClient#createFromConnectionString(String)
	 */
	public EventData(byte[] data, final int offset, final int length)
	{
		this();

		if (data == null)
		{
			throw new IllegalArgumentException("data cannot be null");
		}

		this.bodyData = new Binary(data, offset, length);
	}

	/**
	 * Construct EventData to Send to EventHubs.
	 * Typical pattern to create a Sending EventData is:
	 * <pre>
	 * i.	Serialize the sending ApplicationEvent to be sent to EventHubs into bytes.
	 * ii.	If complex serialization logic is involved (for example: multiple types of data) - add a Hint using the {@link #getProperties()} for the Consumer.
	 *  </pre> 
	 *  <p> Illustration:
	 *  <code>
	 *  	EventData eventData = new EventData(telemetryEventByteBuffer);
	 *  	HashMap{@literal <}String, String{@literal >} applicationProperties = new HashMap{@literal <}String, String{@literal >}();
	 *  	applicationProperties.put("eventType", "com.microsoft.azure.monitoring.EtlEvent");
	 *		eventData.setProperties(applicationProperties);
	 *  	partitionSender.Send(eventData);
	 *  </code>
	 * @param buffer ByteBuffer which references the payload of the Event to be sent to EventHubs
	 * @see EventHubClient#createFromConnectionString(String)
	 */
	public EventData(ByteBuffer buffer)
	{
		this();

		if (buffer == null)
		{
			throw new IllegalArgumentException("data cannot be null");
		}

		this.bodyData = Binary.create(buffer);
	}

	/**
	 * Get Actual Payload/Data wrapped by EventData.
	 * This is the underlying array and should be used in conjunction with {@link #getBodyOffset()} and {@link #getBodyLength()}.
 	 * @return returns the byte[] of the actual data
	 */
	public byte[] getBody()
	{
		return this.bodyData == null ? null : this.bodyData.getArray();
	}
	
	/**
	 * Get the offset of the current Payload/Data in the byte array returned by {@link #getBody()}.
	 * @return returns the byte[] of the actual data
	 * @see #getBodyLength()
	 * @see #getBody()
	 */
	public int getBodyOffset()
	{
		return this.bodyData == null ? 0 : this.bodyData.getArrayOffset();
	}
	
	/**
	 * Get the length of the Actual Payload/Data in the byte array returned by {@link #getBody()}.
	 * @return returns the byte[] of the actual data
	 * @see #getBody()
	 * @see #getBodyOffset()
	 */
	public int getBodyLength()
	{
		return this.bodyData == null ? 0 : this.bodyData.getLength();
	}

	/**
	 * Application property bag
	 * @return returns Application properties
	 */
	public Map<String, String> getProperties()
	{
		if (this.properties == null)
		{
			this.properties = new HashMap<String, String>();
		}

		return this.properties;
	}
	
	/**
	 * SystemProperties that are populated by EventHubService.
	 * <p>As these are populated by Service, they are only present on a Received EventData.
	 * <p>Usage:<p>
	 * <code>
	 * final String offset = eventData.getSystemProperties().getOffset();
	 * </code>
	 * @return an encapsulation of all SystemProperties appended by EventHubs service into EventData.
	 * <code>null</code> if the {@link #EventData()} is not received and is created by the public constructors.
	 * @see SystemProperties#getOffset
	 * @see SystemProperties#getSequenceNumber
	 * @see SystemProperties#getPartitionKey
	 * @see SystemProperties#getEnqueuedTime
	 */
	public SystemProperties getSystemProperties()
	{
		return this.systemProperties;
	}
	
	// This is intended to be used while sending EventData - so EventData.SystemProperties will not be copied over to the AmqpMessage
	Message toAmqpMessage()
	{
		final Message amqpMessage = Proton.message();

		if (this.properties != null && !this.properties.isEmpty())
		{
			final ApplicationProperties applicationProperties = new ApplicationProperties(this.properties);
			amqpMessage.setApplicationProperties(applicationProperties);
		}
		
		if (this.systemProperties != null && !this.systemProperties.isEmpty())
		{
			for(Map.Entry<String, Object> systemProperty: this.systemProperties.entrySet())
			{
				final String propertyName = systemProperty.getKey();
				if (!EventDataUtil.RESERVED_SYSTEM_PROPERTIES.contains(propertyName))
				{
					if (AmqpConstants.RESERVED_PROPERTY_NAMES.contains(propertyName))
						switch (propertyName)
						{
							case AmqpConstants.AMQP_PROPERTY_MESSAGE_ID: amqpMessage.setMessageId(systemProperty.getValue()); break;
							case AmqpConstants.AMQP_PROPERTY_USER_ID: amqpMessage.setUserId((byte[]) systemProperty.getValue()); break;
							case AmqpConstants.AMQP_PROPERTY_TO: amqpMessage.setAddress((String) systemProperty.getValue()); break;
							case AmqpConstants.AMQP_PROPERTY_SUBJECT: amqpMessage.setSubject((String) systemProperty.getValue()); break;
							case AmqpConstants.AMQP_PROPERTY_REPLY_TO: amqpMessage.setReplyTo((String) systemProperty.getValue()); break;
							case AmqpConstants.AMQP_PROPERTY_CORRELATION_ID: amqpMessage.setCorrelationId(systemProperty.getValue()); break;
							case AmqpConstants.AMQP_PROPERTY_CONTENT_TYPE: amqpMessage.setContentType((String) systemProperty.getValue()); break;
							case AmqpConstants.AMQP_PROPERTY_CONTENT_ENCODING: amqpMessage.setContentEncoding((String) systemProperty.getValue()); break;
							case AmqpConstants.AMQP_PROPERTY_ABSOLUTE_EXPRITY_time: amqpMessage.setExpiryTime((long) systemProperty.getValue()); break;
							case AmqpConstants.AMQP_PROPERTY_CREATION_TIME: amqpMessage.setCreationTime((long) systemProperty.getValue()); break;
							case AmqpConstants.AMQP_PROPERTY_GROUP_ID: amqpMessage.setGroupId((String) systemProperty.getValue()); break;
							case AmqpConstants.AMQP_PROPERTY_GROUP_SEQUENCE: amqpMessage.setGroupSequence((long) systemProperty.getValue()); break;
							case AmqpConstants.AMQP_PROPERTY_REPLY_TO_GROUP_ID: amqpMessage.setReplyToGroupId((String) systemProperty.getValue()); break;
							default: throw new RuntimeException("unreachable");
						}
					else
					{
						final MessageAnnotations messageAnnotations = (amqpMessage.getMessageAnnotations() == null) 
																		? new MessageAnnotations(new HashMap<Symbol, Object>()) 
																				: amqpMessage.getMessageAnnotations();		
						messageAnnotations.getValue().put(Symbol.getSymbol(systemProperty.getKey()), systemProperty.getValue());
						amqpMessage.setMessageAnnotations(messageAnnotations);
					}
				}
			}
		}

		if (this.bodyData != null)
		{
			amqpMessage.setBody(new Data(this.bodyData));
		}

		return amqpMessage;
	}

	Message toAmqpMessage(final String partitionKey)
	{
		final Message amqpMessage = this.toAmqpMessage();

		final MessageAnnotations messageAnnotations = (amqpMessage.getMessageAnnotations() == null) 
				? new MessageAnnotations(new HashMap<Symbol, Object>()) 
						: amqpMessage.getMessageAnnotations();		
		messageAnnotations.getValue().put(AmqpConstants.PARTITION_KEY, partitionKey);
		amqpMessage.setMessageAnnotations(messageAnnotations);

		return amqpMessage;
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException
	{
		out.defaultWriteObject();
		
		out.writeInt(this.bodyData.getLength());
		out.write(this.bodyData.getArray(), this.bodyData.getArrayOffset(), this.bodyData.getLength());
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		
		final int length = in.readInt();
		final byte[] data = new byte[length];
		in.read(data, 0, length);
		this.bodyData = new Binary(data, 0, length);
	}

	public static class SystemProperties extends HashMap<String, Object>
	{
		private static final long serialVersionUID = -2827050124966993723L;
		
		public SystemProperties(final HashMap<String, Object> map)
		{
			super(Collections.unmodifiableMap(map));
		}
		
		public String getOffset()
		{
			return this.getSystemProperty(AmqpConstants.OFFSET_ANNOTATION_NAME);
		}
		
		public String getPartitionKey()
		{
			return this.getSystemProperty(AmqpConstants.PARTITION_KEY_ANNOTATION_NAME);
		}
		
		public Instant getEnqueuedTime()
		{
			final Date enqueuedTimeValue = this.getSystemProperty(AmqpConstants.ENQUEUED_TIME_UTC_ANNOTATION_NAME);
			return enqueuedTimeValue != null ? enqueuedTimeValue.toInstant() : null;
		}
		
		public long getSequenceNumber()
		{
			return this.getSystemProperty(AmqpConstants.SEQUENCE_NUMBER_ANNOTATION_NAME);
		}
		
		public String getPublisher()
		{
			return this.getSystemProperty(AmqpConstants.PUBLISHER_ANNOTATION_NAME);
		}
		
		@SuppressWarnings("unchecked")
		private <T> T getSystemProperty(final String key)
		{
			if (this.containsKey(key))
			{
				return (T) (this.get(key));
			}
			
			return null;
		}
	}
}
