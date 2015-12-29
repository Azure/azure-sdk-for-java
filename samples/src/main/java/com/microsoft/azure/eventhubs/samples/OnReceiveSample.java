package com.microsoft.azure.eventhubs.samples;

import java.util.*;
import java.util.concurrent.*;

import com.microsoft.azure.eventhubs.*;
import com.microsoft.azure.servicebus.*;

public class OnReceiveSample {

	public static void main(String[] args) throws Exception {
		
		ConnectionStringBuilder connStr = new ConnectionStringBuilder("----namespaceName-----", "----EventHubName-----", "-----sayKeyName-----", "---SasKey----");
		
		EventHubClient ehClient = EventHubClient.createFromConnectionString(connStr.toString()).get();
		
		String partitionId = "0";
		long epoch = 20000;
		PartitionReceiver receiver = ehClient.createEpochReceiver(EventHubClient.DefaultConsumerGroupName, partitionId, PartitionReceiver.StartOfStream, false, epoch, new OnReceiveSample.EventPrinter()).get();
		
		try {
			PartitionReceiver receiver2 = ehClient.createEpochReceiver(EventHubClient.DefaultConsumerGroupName, partitionId, PartitionReceiver.StartOfStream, false, epoch - 10, new OnReceiveSample.EventPrinter()).get();
		}
		catch(ExecutionException exception) {
			System.out.println("ExpectedException: " + exception.toString());
		}
		
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
