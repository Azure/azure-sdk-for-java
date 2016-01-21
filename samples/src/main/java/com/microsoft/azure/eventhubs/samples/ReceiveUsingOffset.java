package com.microsoft.azure.eventhubs.samples;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.logging.*;

import com.microsoft.azure.eventhubs.*;
import com.microsoft.azure.servicebus.*;

public class ReceiveUsingOffset
{
	public static void main(String[] args) 
			throws ServiceBusException, ExecutionException, InterruptedException, IOException
	{
		ConnectionStringBuilder connStr = new ConnectionStringBuilder("----namespaceName-----", "----EventHubName-----", "-----sayKeyName-----", "---SasKey----");
		
		EventHubClient ehClient = EventHubClient.createFromConnectionString(connStr.toString()).get();
		
		// receiver
		String partitionId = "0"; // API to get PartitionIds will be released as part of V0.2
		PartitionReceiver receiver = ehClient.createReceiver(
				EventHubClient.DefaultConsumerGroupName, 
				partitionId, 
				PartitionReceiver.StartOfStream,
				false).get();
		
		Iterable<EventData> receivedEvents = receiver.receive().get();
		
		while (true)
		{
			int batchSize = 0;
			for(EventData receivedEvent: receivedEvents)
			{
				System.out.println(String.format("Message Payload: %s", new String(receivedEvent.getBody(), Charset.defaultCharset())));
				System.out.println(String.format("Offset: %s, SeqNo: %s, EnqueueTime: %s", 
						receivedEvent.getSystemProperties().getOffset(), 
						receivedEvent.getSystemProperties().getSequenceNumber(), 
						receivedEvent.getSystemProperties().getEnqueuedTime()));
				batchSize++;
			}
			
			System.out.println(String.format("ReceivedBatch Size: %s", batchSize));
			receivedEvents = receiver.receive().get();
		}
	}

	/**
	 * actual application-payload, ex: a telemetry event
	 */
	static final class PayloadEvent
	{
		PayloadEvent()	{}
		
		public String strProperty;
		public long longProperty;
		public int intProperty;
	}

}
