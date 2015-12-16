package com.microsoft.azure.eventhubs;

import java.io.*;
import java.nio.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.*;
import org.apache.qpid.proton.amqp.messaging.*;
import org.apache.qpid.proton.message.*;
import com.microsoft.azure.servicebus.*;

public class EventData implements AutoCloseable {
	
	private String partitionKey;
	private String offset;
	private long sequenceNumber;
	private Date enqueuedTimeUtc;
	private boolean closed;
	private Binary bodyData;
	
	private Map<String, String> properties;
	
	// property bag intended to carry ServiceProperties
	private Map<Symbol, Object> systemProperties;
	
	private ReceivedSystemProperties receivedSystemProperties;
	
	EventData() {
		this.properties = new HashMap<String, String>();
		this.closed = false;
		this.systemProperties = new HashMap<Symbol, Object>();
	}
	
	/*
	 * Internal Constructor - intended to be used only by the @@PartitionReceiver
	 */
	EventData(Message amqpMessage) {
		if (amqpMessage == null) {
			throw new IllegalArgumentException("amqpMessage cannot be null");
		}
		
		this.systemProperties = Collections.unmodifiableMap(amqpMessage.getMessageAnnotations().getValue());
		
		Object partitionKeyObj = this.systemProperties.get(AmqpConstants.PartitionKey);
		if (partitionKeyObj != null) 
			this.partitionKey = partitionKeyObj.toString();
		
		Object sequenceNumberObj = this.systemProperties.get(AmqpConstants.SequenceNumber);
		this.sequenceNumber = (long) sequenceNumberObj;
		
		Object enqueuedTimeUtcObj = this.systemProperties.get(AmqpConstants.EnqueuedTimeUtc);
		this.enqueuedTimeUtc = (Date) enqueuedTimeUtcObj;
		
		this.offset = this.systemProperties.get(AmqpConstants.Offset).toString();
		
		this.properties = amqpMessage.getApplicationProperties() == null ? null 
				: (Map<String, String>) amqpMessage.getApplicationProperties().getValue();
		
		this.bodyData = ((Data) amqpMessage.getBody()).getValue();
		
		this.receivedSystemProperties = new ReceivedSystemProperties(this);
	}
	
	public EventData(byte[] data) {
		this();
		
		if (data == null) {
			throw new IllegalArgumentException("data cannot be null");
		}
		
		this.bodyData = new Binary(data);
	}
	
	public EventData(byte[] data, final int offset, final int length) {
		this();
		
		// TODO: evaluate if this(new ByteArrayInputStream(data)) - is required
		if (data == null) {
			throw new IllegalArgumentException("data cannot be null");
		}
		
		this.bodyData = new Binary(data, offset, length);
	}
	
	public EventData(ByteBuffer buffer){
		this();
		
		if (buffer == null) {
			throw new IllegalArgumentException("data cannot be null");
		}
		
		this.bodyData = Binary.create(buffer);
	}
	
	public byte[] getBody() {
		return this.bodyData.getArray();
	}
	
	/*
	 * Internal method to set partitionKey while sending the Message.
	 */
	void setPartitionKey(String partitionKey) {
		this.systemProperties.put(AmqpConstants.PartitionKey, partitionKey);
	}
	
	/*
	 * Application property bag
	 */
	public Map getProperties() {
		return this.properties;
	}
	
	/*
	 * SystemProperties populated by EventHubService
	 */
	public ReceivedSystemProperties getReceivedSystemProperties() {
		return this.receivedSystemProperties;
	}
	
	private void throwIfAutoClosed() {
		if (this.closed) {
			// TODO: TRACE
			throw new IllegalStateException("EventData is already disposed");
		}
	}
	
	Message toAmqpMessage() {
		this.throwIfAutoClosed();
		
		Message amqpMessage = Proton.message();
		
		if (this.properties != null && !this.properties.isEmpty()) {
			ApplicationProperties applicationProperties = new ApplicationProperties(this.properties);
			amqpMessage.setApplicationProperties(applicationProperties);
		}
		
		if (this.systemProperties != null && !this.systemProperties.isEmpty()) {
			MessageAnnotations messageAnnotations = new MessageAnnotations(this.systemProperties);
			amqpMessage.setMessageAnnotations(messageAnnotations);
		}
		
		if (this.bodyData != null) {
			amqpMessage.setBody(new Data(this.bodyData));
		}
		
		return amqpMessage;
	}

	@Override
	public void close() throws Exception {
		
		if (!this.closed) {
			// TODO: dispose native resources
		}
		
		this.closed = true;
	}
	
	public static final class ReceivedSystemProperties {
		EventData event;
		
		ReceivedSystemProperties(EventData event) {
			this.event = event;
		}
		
		public long getSequenceNumber() {
			return this.event.sequenceNumber;
		}
		
		public Date getEnqueuedTimeUtc() {
			return this.event.enqueuedTimeUtc;
		}
		
		public String getOffset() {
			return this.event.offset;
		}
		
		public String getPartitionKey() {
			return this.event.partitionKey;
		}
	}
}
