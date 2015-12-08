package com.microsoft.azure.eventhubs;

import java.io.*;
import java.time.*;
import java.util.*;

public class EventData {
	private String partitionKey;
	private String offset;
	private long sequenceNumber;
	private ZonedDateTime enqueuedTimeUtc;
	private InputStream stream;
	
	private Map<String, String> properties;
	
	// INVESTIGATE: readonly system properties bag
	private Map<String, String> systemProperties;
	
	EventData() {
		this.properties = new HashMap<String, String>();
	}
	
	Map<String, String> getSystemProperties() {
		return this.systemProperties;
	}
	
	public EventData(byte[] data) {
		this(new ByteArrayInputStream(data));
	}
	
	// TODO: FIND out what Stream datatype is used by jackson json lib; research for a general java standard
	public EventData(InputStream stream) {
		this.stream = stream;
	}
	
	protected String getPartitionKey(){
		return this.partitionKey;
	}
	
	protected long getSequenceNumber() {
		return this.sequenceNumber;
	}
	
	protected ZonedDateTime getEnqueuedTimeUtc() {
		return this.enqueuedTimeUtc;
	}
	
	public Map<String, String> getProperties() {
		return this.properties;
	}
	
	protected String getOffset() {
		return this.offset;
	}	
}
