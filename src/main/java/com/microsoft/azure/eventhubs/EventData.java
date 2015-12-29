package com.microsoft.azure.eventhubs;

import java.nio.*;
import java.util.*;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.*;
import org.apache.qpid.proton.amqp.messaging.*;
import org.apache.qpid.proton.message.*;
import com.microsoft.azure.servicebus.*;

/**
 * The data structure encapsulating the Event being sent to and received from EventHubs.
 * Each EventHubs partition is nothing but a Stream of EventData.
 */
public class EventData implements AutoCloseable {
	
	private String partitionKey;
	private String offset;
	private long sequenceNumber;
	private Date enqueuedTimeUtc;
	private boolean closed;
	private Binary bodyData;
	private boolean isReceivedEvent;
	private Map<String, String> properties;
	
	private SystemProperties systemProperties;
	
	EventData() {
		this.closed = false;
	}
	
	/*
	 * Internal Constructor - intended to be used only by the @@PartitionReceiver to Create #EventData out of #Message
	 */
	@SuppressWarnings("unchecked")
	EventData(Message amqpMessage) {
		if (amqpMessage == null) {
			throw new IllegalArgumentException("amqpMessage cannot be null");
		}
		
		Map<Symbol, Object> messageAnnotations = amqpMessage.getMessageAnnotations().getValue();
		
		Object partitionKeyObj = messageAnnotations.get(AmqpConstants.PartitionKey);
		if (partitionKeyObj != null) 
			this.partitionKey = partitionKeyObj.toString();
		
		Object sequenceNumberObj = messageAnnotations.get(AmqpConstants.SequenceNumber);
		this.sequenceNumber = (Long) sequenceNumberObj;
		
		Object enqueuedTimeUtcObj = messageAnnotations.get(AmqpConstants.EnqueuedTimeUtc);
		this.enqueuedTimeUtc = (Date) enqueuedTimeUtcObj;
		
		this.offset = messageAnnotations.get(AmqpConstants.Offset).toString();
		
		this.properties = amqpMessage.getApplicationProperties() == null ? null 
				: ((Map<String, String>)(amqpMessage.getApplicationProperties().getValue()));
		
		this.bodyData = ((Data) amqpMessage.getBody()).getValue();
		
		this.isReceivedEvent = true;
	}
	
	/**
	 * Construct EventData to Send to EventHubs.
	 * Typical pattern to create a Sending EventData is:
	 * <pre>
	 * i.	Serialize the sending ApplicationEvent to be sent to EventHubs into bytes.
	 * ii.	If complex serialization logic is involved (for example: multiple types of data) - add a Hint using the {@link #getProperties()} for the Consumer.
	 *  </pre> 
	 *  <p> Illustration:
	 *  <pre>
	 *  	EventData eventData = new EventData(telemetryEventBytes);
	 *  	HashMap<String, String> applicationProperties = new HashMap<String, String>();
	 *  	applicationProperties.put("eventType", "com.microsoft.azure.monitoring.EtlEvent");
	 *		eventData.setProperties(applicationProperties);
	 *  	partitionSender.Send(eventData);
	 *  </pre>
	 * @param data the actual payload of data in bytes to be Sent to EventHubs.
	 * @see to start sending to EventHubs refer to {@link EventHubClient#createFromConnectionString(String)}
	 */
	public EventData(byte[] data) {
		this();
		
		if (data == null) {
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
	 *  <pre>
	 *  	EventData eventData = new EventData(telemetryEventBytes, offset, length);
	 *  	HashMap<String, String> applicationProperties = new HashMap<String, String>();
	 *  	applicationProperties.put("eventType", "com.microsoft.azure.monitoring.EtlEvent");
	 *		eventData.setProperties(applicationProperties);
	 *  	partitionSender.Send(eventData);
	 *  </pre>
	 * @param data the byte[] where the payload of the Event to be sent to EventHubs is present
	 * @param offset Offset in the byte[] to read from ; inclusive index
	 * @param length length of the byte[] to be read, starting from offset
	 * @see to start sending to EventHubs refer to {@link EventHubClient#createFromConnectionString(String)}
	 */
	public EventData(byte[] data, final int offset, final int length) {
		this();
		
		if (data == null) {
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
	 *  <pre>
	 *  	EventData eventData = new EventData(telemetryEventByteBuffer);
	 *  	HashMap<String, String> applicationProperties = new HashMap<String, String>();
	 *  	applicationProperties.put("eventType", "com.microsoft.azure.monitoring.EtlEvent");
	 *		eventData.setProperties(applicationProperties);
	 *  	partitionSender.Send(eventData);
	 *  </pre>
	 * @param buffer ByteBuffer which references the payload of the Event to be sent to EventHubs
	 * @see to start sending to EventHubs refer to {@link EventHubClient#createFromConnectionString(String)}
	 */
	public EventData(ByteBuffer buffer){
		this();
		
		if (buffer == null) {
			throw new IllegalArgumentException("data cannot be null");
		}
		
		this.bodyData = Binary.create(buffer);
	}
	
	/**
	 * Get Actual Payload/Data wrapped by EventData.
	 * This is intended to be used after receiving EventData using @@PartitionReceiver.
	 * @return returns the byte[] of the actual data 
	 */
	public byte[] getBody() {
		return this.bodyData.getArray();
	}
	
	/**
	 * Application property bag
	 */
	public Map<String, String> getProperties() {
		return this.properties;
	}
	
	public void setProperties(Map applicationProperties) {
		this.properties = applicationProperties;
	}
	
	/**
	 * SystemProperties that are populated by EventHubService.
	 * <pr>As these are populated by Service, they are only present on a Received EventData.
	 * @return an encapsulation of all SystemProperties appended by EventHubs service into EventData
	 */
	public SystemProperties getSystemProperties() {
		if (this.isReceivedEvent && this.systemProperties == null) {
			this.systemProperties = new SystemProperties(this);
		}
		
		return this.systemProperties;
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
		
		if (this.bodyData != null) {
			amqpMessage.setBody(new Data(this.bodyData));
		}
		
		return amqpMessage;
	}

	public void close() throws Exception {
		
		if (!this.closed) {
			// TODO: dispose native resources
		}
		
		this.closed = true;
	}
	
	public static final class SystemProperties {
		EventData event;
		
		SystemProperties(EventData event) {
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
