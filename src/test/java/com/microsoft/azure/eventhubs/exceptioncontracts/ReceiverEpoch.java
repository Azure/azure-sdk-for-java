package com.microsoft.azure.eventhubs.exceptioncontracts;

import java.util.Collection;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

import com.microsoft.azure.eventhubs.*;
import com.microsoft.azure.eventhubs.lib.*;
import com.microsoft.azure.servicebus.*;

public class ReceiverEpoch extends TestBase
{

	@Test (expected = ReceiverDisconnectedException.class)
	public void testEpochReceiver() throws Throwable
	{
		TestEventHubInfo eventHubInfo = TestBase.checkoutTestEventHub();
		try 
		{
			ConnectionStringBuilder connectionString = TestBase.getConnectionString(eventHubInfo);
			EventHubClient ehClient = EventHubClient.createFromConnectionString(connectionString.toString()).get();		
			String cgName = eventHubInfo.getRandomConsumerGroup();
			String partitionId = "0";
			long epoch = 345632;
			ehClient.createEpochReceiver(cgName, partitionId, PartitionReceiver.StartOfStream, false, epoch, new EventCounter()).get();
		
			try
			{
				ehClient.createEpochReceiver(cgName, partitionId, epoch - 10).get();
			}
			catch(ExecutionException exp)
			{
				throw exp.getCause();
			}
		}
		finally {
			TestBase.checkinTestEventHub(eventHubInfo.getName());
		}
	}

	public static final class EventCounter extends ReceiveHandler
	{
		private long count;
		
		public EventCounter()
		{ 
			count = 0;
		}
		
		@Override
		public void onReceive(Collection<EventData> events)
		{
			for(EventData event: events)
			{
				System.out.println(String.format("Counter: %s, Offset: %s, SeqNo: %s, EnqueueTime: %s, PKey: %s", 
						 this.count, event.getSystemProperties().getOffset(), event.getSystemProperties().getSequenceNumber(), event.getSystemProperties().getEnqueuedTimeUtc(), event.getSystemProperties().getPartitionKey()));
			}		
			
			count++;
		}
		
	}
}
