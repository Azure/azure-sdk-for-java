package com.microsoft.azure.eventhubs.exceptioncontracts;

import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import com.microsoft.azure.eventhubs.*;
import com.microsoft.azure.eventhubs.lib.*;
import com.microsoft.azure.servicebus.*;

public class ReceiverEpochTest extends TestBase
{

	@Test (expected = ReceiverDisconnectedException.class)
	public void testEpochReceiver() throws Throwable
	{
		Assume.assumeTrue(TestBase.isServiceRun());
		
		TestEventHubInfo eventHubInfo = TestBase.checkoutTestEventHub();
		try 
		{
			ConnectionStringBuilder connectionString = TestBase.getConnectionString(eventHubInfo);
			EventHubClient ehClient = EventHubClient.createFromConnectionString(connectionString.toString()).get();		
			
			try
			{
				String cgName = eventHubInfo.getRandomConsumerGroup();
				String partitionId = "0";
				long epoch = 345632;
				PartitionReceiver receiver = ehClient.createEpochReceiver(cgName, partitionId, PartitionReceiver.START_OF_STREAM, false, epoch).get();
				EventCounter counter = new EventCounter();
				// receiver.setReceiveHandler(counter);
				
				try
				{
					ehClient.createEpochReceiver(cgName, partitionId, PartitionReceiver.START_OF_STREAM, false, epoch - 10).get();
				}
				catch(ExecutionException exp)
				{
					throw exp.getCause();
				}
				
				Assert.assertTrue(counter.count > 0);
			}
			finally
			{
				ehClient.close();
			}
		}
		finally
		{
			TestBase.checkinTestEventHub(eventHubInfo.getName());
		}
	}

	public static final class EventCounter extends PartitionReceiveHandler
	{
		public long count;
		
		public EventCounter()
		{ 
			count = 0;
		}

		@Override
		public void onReceive(Iterable<EventData> events)
		{
			count++;			
		}

		@Override
		public void onError(Exception exception)
		{	
		}
		
	}
}
