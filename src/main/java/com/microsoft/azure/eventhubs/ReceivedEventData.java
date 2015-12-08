package com.microsoft.azure.eventhubs;

import java.time.ZonedDateTime;


// TODO: GetBody, Properties, readonlySystemProperties
public final class ReceivedEventData
{
	private EventData receivedData;
	
	ReceivedEventData(EventData eventData) {
		this.receivedData = eventData;
	}
		
	public String getPartitionKey(){
		return this.receivedData.getPartitionKey();
	}
	
	public long getSequenceNumber() {
		return this.receivedData.getSequenceNumber();
	}
	
	public ZonedDateTime getEnqueuedTimeUtc() {
		return this.receivedData.getEnqueuedTimeUtc();
	}
	
	public String getOffset() {
		return this.receivedData.getOffset();
	}
}
