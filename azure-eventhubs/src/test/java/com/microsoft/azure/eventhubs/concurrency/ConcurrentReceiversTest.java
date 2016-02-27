package com.microsoft.azure.eventhubs.concurrency;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.logging.*;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import com.microsoft.azure.eventhubs.*;
import com.microsoft.azure.eventhubs.lib.TestBase;
import com.microsoft.azure.eventhubs.lib.TestEventHubInfo;
import com.microsoft.azure.servicebus.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.ServiceBusException;

public class ConcurrentReceiversTest
{
	TestEventHubInfo eventHubInfo;
	ConnectionStringBuilder connStr;
	int partitionCount = 4;
	EventHubClient sender;
	
	@Before
	public void initializeEventHub()  throws Exception
	{
		Assume.assumeTrue(TestBase.isServiceRun());
		
    	eventHubInfo = TestBase.checkoutTestEventHub();
		connStr = new ConnectionStringBuilder(
				eventHubInfo.getNamespaceName(), eventHubInfo.getName(), eventHubInfo.getSasRule().getKey(), eventHubInfo.getSasRule().getValue());
	
		sender = EventHubClient.createFromConnectionString(connStr.toString()).get();
		
		for (int i=0; i < partitionCount; i++)
		{
			TestBase.pushEventsToPartition(sender, Integer.toString(i), 10);
		}
	}
	
	@Test()
	public void testParallelReceivers() throws ServiceBusException, InterruptedException, ExecutionException, IOException
	{
		String consumerGroupName = eventHubInfo.getRandomConsumerGroup();
		
		for (int repeatCount = 0; repeatCount< 4; repeatCount++)
		{
			EventHubClient[] ehClients = new EventHubClient[partitionCount];
			PartitionReceiver[] receivers = new PartitionReceiver[partitionCount];
			try
			{
				for(int i=0; i < partitionCount; i++)
				{
					ehClients[i] = EventHubClient.createFromConnectionString(connStr.toString()).get();
					receivers[i] = ehClients[i].createReceiver(consumerGroupName, Integer.toString(i), Instant.now()).get();
					receivers[i].setReceiveHandler(new EventCounter());
				}
			}
			finally
			{
				for (int i=0; i < partitionCount; i++)
				{
					if (receivers[i] != null)
					{
						receivers[i].close();
					}
					
					if (ehClients[i] != null)
					{
						ehClients[i].close();
					}
				}
			}
		}
	}
	
	@After()
	public void cleanup()
	{
		if (this.sender != null)
		{
			this.sender.close();
		}
	}

	public static final class EventCounter extends PartitionReceiveHandler
	{
		private long count;
		
		public EventCounter()
		{ 
			count = 0;
		}
		
		@Override
		public void onReceive(Iterable<EventData> events)
		{
			for(EventData event: events)
			{
				count++;
			}
		}

		@Override
		public void onError(Throwable error)
		{
		}

		@Override
		public void onClose(Throwable error)
		{
		}		
	}
}
