package com.microsoft.azure.eventhubs.samples;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.*;
import com.microsoft.azure.eventhubs.*;
import com.microsoft.azure.servicebus.*;

public class OnReceive
{

	public static void main(String[] args) 
			throws ServiceBusException, ExecutionException, InterruptedException, IOException
	{
		ConnectionStringBuilder connStr = new ConnectionStringBuilder("----namespaceName-----", "----EventHubName-----", "-----sayKeyName-----", "---SasKey----");
		EventHubClient ehClient = EventHubClient.createFromConnectionString(connStr.toString()).get();
		
		String partitionId = "0";
		long epoch = 20001;
		PartitionReceiver receiver = ehClient.createEpochReceiver(
			EventHubClient.DefaultConsumerGroupName, 
			partitionId, 
			PartitionReceiver.StartOfStream, 
			false, 
			epoch, 
			new EventPrinter()).get();
			
		System.in.read();
	}

	static final class EventPrinter extends ReceiveHandler
	{
		public EventPrinter(){}
		
		@Override
		public void onReceive(Collection<EventData> events)
		{
			for(EventData event: events)
			{
				System.out.println(String.format("Offset: %s, SeqNo: %s, EnqueueTime: %s, Msg: %s", 
						event.getSystemProperties().getOffset(), 
						event.getSystemProperties().getSequenceNumber(), 
						event.getSystemProperties().getEnqueuedTime(),
						new String(event.getBody(), Charset.defaultCharset())));
			}
			
			System.out.println("Processing events...");
			System.out.println();
			System.out.println();
			System.out.println();
			
			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}		
	}
}
