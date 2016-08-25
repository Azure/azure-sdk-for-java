package com.microsoft.azure.eventhubs.concurrency;

import java.io.IOException;
import java.util.concurrent.*;

import org.junit.*;

import com.microsoft.azure.eventhubs.*;
import com.microsoft.azure.eventhubs.lib.*;
import com.microsoft.azure.servicebus.*;

public class EventHubClientTest extends ApiTestBase
{
	
	@Test()
	public void testParallelEventHubClients() throws ServiceBusException, InterruptedException, ExecutionException, IOException
	{
		TestEventHubInfo eventHubInfo = TestBase.checkoutTestEventHub();
		String consumerGroupName = eventHubInfo.getRandomConsumerGroup();
		String partitionId = "0";
		
		@SuppressWarnings("unchecked")
		CompletableFuture<EventHubClient>[] createFutures = new CompletableFuture[4];
		try 
		{
			ConnectionStringBuilder connectionString = TestBase.getConnectionString(eventHubInfo);
			for (int i = 0; i < 4 ; i ++)
			{
			 createFutures[i] = EventHubClient.createFromConnectionString(connectionString.toString());		
			}
			
			CompletableFuture.allOf(createFutures).get();
			boolean firstOne = true;
			for (CompletableFuture<EventHubClient> createFuture: createFutures)
			{
				final EventHubClient ehClient = createFuture.join();
				if (firstOne)
				{
					TestBase.pushEventsToPartition(ehClient, partitionId, 10).get();
					firstOne = false;
				}
				
				PartitionReceiver receiver = ehClient.createReceiverSync(consumerGroupName, partitionId, PartitionReceiver.START_OF_STREAM, false);
				Assert.assertTrue(receiver.receiveSync(100).iterator().hasNext());
			}
		}
		finally
		{
			if (createFutures != null)
			{
				for (CompletableFuture<EventHubClient> createFuture: createFutures)
				{
					if (!createFuture.isCancelled() || !createFuture.isCompletedExceptionally())
					{
						EventHubClient ehClient = createFuture.join();
						ehClient.close();
					}
				}
			}
		}
	}
	
}
