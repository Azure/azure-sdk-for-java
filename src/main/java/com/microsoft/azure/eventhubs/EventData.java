package com.microsoft.azure.eventhubs;

import java.io.*;
import java.time.*;
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
	private ZonedDateTime enqueuedTimeUtc;
	private InputStream stream;
	private boolean ownsStream;
	private boolean closed;
	private Binary bodyData;
	
	private Map<String, String> properties;
	
	// INVESTIGATE: readonly system properties bag
	private Map<Symbol, Object> systemProperties;
	
	EventData() {
		this.properties = new HashMap<String, String>();
		this.closed = false;
		this.systemProperties = new HashMap<Symbol, Object>();
	}
	
	EventData(Message amqpMessage) {
		this();
		
		if (amqpMessage == null) {
			throw new IllegalArgumentException("amqpMessage cannot be null");
		}
		
		MessageUtil.UpdateEventDataHeaderAndProperties(amqpMessage, this);
		// TODO: this.bodyData = amqpMessage.getBody();
	}
	
	public EventData(byte[] data) {
		// TODO: evaluate if this(new ByteArrayInputStream(data)) - is required
		if (data == null) {
			throw new IllegalArgumentException("data cannot be null");
		}
		
		this.bodyData = new Binary(data);
		this.ownsStream = true;
	}
	
	// TODO: Investigate the need of Stream constructor - we cannot pass/get Stream handle from ProtonJ lib
	// TODO: FIND out what Stream datatype is used by jackson json lib; research for a general java standard
	EventData(InputStream stream) {
		this.stream = stream;
	}
	
	protected String getPartitionKey(){
		return this.partitionKey;
	}
	
	void setPartitionKey(String partitionKey) {
		this.partitionKey = partitionKey;
	}
	
	protected long getSequenceNumber() {
		return this.sequenceNumber;
	}
	
	protected ZonedDateTime getEnqueuedTimeUtc() {
		return this.enqueuedTimeUtc;
	}
	
	protected String getOffset() {
		return this.offset;
	}

	Map<String, String> getProperties() {
		return this.properties;
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
			if (this.stream != null && this.ownsStream) {
				this.stream.close();
				this.stream = null;
			}
		}
		
		this.closed = true;
	}
}
