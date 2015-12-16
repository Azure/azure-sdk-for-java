package com.microsoft.azure.eventhubs.samples;

import java.util.*;

import com.microsoft.azure.eventhubs.*;
import com.microsoft.azure.servicebus.*;

public class OnReceiveSample {

	public static void main(String[] args) throws Exception {
		
		ConnectionStringBuilder connStr = new ConnectionStringBuilder("----namespaceName-----", "----EventHubName-----", "-----sayKeyName-----", "---SasKey----");
		System.out.println(connStr.toString());
		
		EventHubClient ehClient = EventHubClient.createFromConnectionString(connStr.toString());
		
		PartitionReceiver receiver = ehClient.createReceiver(EventHubClient.DefaultConsumerGroupName, "0", "-1", false, new OnReceiveSample.EventPrinter());
		
		System.out.println("done...");
		System.in.read();
	}

	public static final class EventPrinter extends ReceiveHandler {
		
		public EventPrinter(){}
		
		@Override
		public void onReceive(Collection<EventData> events) {
			for(EventData event: events) {
				System.out.println(String.format("Offset: %s, SeqNo: %s, EnqueueTime: %s, PKey: %s", 
						event.getSystemProperties().getOffset(), event.getSystemProperties().getSequenceNumber(), event.getSystemProperties().getEnqueuedTimeUtc(), event.getSystemProperties().getPartitionKey()));
			}			
		}
		
	}
}
