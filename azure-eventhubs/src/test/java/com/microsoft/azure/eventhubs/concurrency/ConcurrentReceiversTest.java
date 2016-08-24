package com.microsoft.azure.eventhubs.concurrency;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.PartitionReceiveHandler;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.lib.TestBase;
import com.microsoft.azure.eventhubs.lib.TestEventHubInfo;
import com.microsoft.azure.servicebus.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.ServiceBusException;

public class ConcurrentReceiversTest extends ApiTestBase
{
	TestEventHubInfo eventHubInfo;
	ConnectionStringBuilder connStr;
	int partitionCount = 4;
	int eventSentPerPartition = 10;
	EventHubClient sender;
	Instant sendStartTime;
	
	@Before
	public void initializeEventHub()  throws Exception
	{
		sendStartTime = Instant.now();
		
		eventHubInfo = TestBase.checkoutTestEventHub();
		connStr = new ConnectionStringBuilder(
				eventHubInfo.getNamespaceName(), eventHubInfo.getName(), eventHubInfo.getSasRule().getKey(), eventHubInfo.getSasRule().getValue());

		sender = EventHubClient.createFromConnectionString(connStr.toString()).get();

		for (int i=0; i < partitionCount; i++)
		{
			TestBase.pushEventsToPartition(sender, Integer.toString(i), eventSentPerPartition);
		}
	}

	@Test()
	public void testParallelReceivers() throws ServiceBusException, IOException, InterruptedException
	{
		String consumerGroupName = eventHubInfo.getRandomConsumerGroup();
		EventHubClient ehClient = EventHubClient.createFromConnectionStringSync(connStr.toString());
		PartitionReceiver[] receivers = new PartitionReceiver[partitionCount];
		EventCounter[] counter = new EventCounter[partitionCount];
		
		try
		{
			for(int i=0; i < partitionCount; i++)
			{
				receivers[i] = ehClient.createReceiverSync(consumerGroupName, Integer.toString(i), sendStartTime);
				receivers[i].setReceiveTimeout(Duration.ofMillis(400));
				
				counter[i] = new EventCounter();
				receivers[i].setReceiveHandler(counter[i]);
			}
			
			int worstCaseRetryCount = partitionCount * 10;
			boolean receivedOnAllPartitions = true;
			
			do
			{
				worstCaseRetryCount--;
				Thread.sleep(500);
				
				receivedOnAllPartitions = true;
				for (int i =0; i < partitionCount; i++)
				{
					receivedOnAllPartitions = receivedOnAllPartitions && (counter[i].count > 0);
				}
			} while(!receivedOnAllPartitions && worstCaseRetryCount > 0);
			
			Assert.assertTrue(receivedOnAllPartitions);
		}
		finally
		{
			for (int i=0; i < partitionCount; i++)
			{
				if (receivers[i] != null)
				{
					receivers[i].close();
				}
			}

			if (ehClient != null)
			{
				ehClient.close();
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
		private int count;

		public EventCounter()
		{
			super(50);
			count = 0;
		}

		@Override
		public void onReceive(Iterable<EventData> events)
		{
			if (events != null)
				for(@SuppressWarnings("unused") EventData eData: events)
					count++;
		}

		@Override
		public void onError(Throwable error)
		{
		}
	}
}
