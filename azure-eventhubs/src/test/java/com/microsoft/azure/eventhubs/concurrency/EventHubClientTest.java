/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.concurrency;

import java.awt.*;
import java.util.concurrent.*;

import org.junit.*;

import com.microsoft.azure.eventhubs.*;
import com.microsoft.azure.eventhubs.lib.*;

public class EventHubClientTest extends ApiTestBase {

	@Test()
	public void testParallelEventHubClients() throws Exception	{

		final String consumerGroupName = TestContext.getConsumerGroupName();
		final String partitionId = "0";
		final int noOfClients = 4;
		final ExecutorService executorService = Executors.newSingleThreadExecutor();
		
		@SuppressWarnings("unchecked")
		CompletableFuture<EventHubClient>[] createFutures = new CompletableFuture[noOfClients];
		try 
		{
			ConnectionStringBuilder connectionString = TestContext.getConnectionString();
			for (int i = 0; i < noOfClients; i ++) {
				createFutures[i] = EventHubClient.createFromConnectionString(connectionString.toString(), executorService);
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

				PartitionReceiver receiver = ehClient.createReceiverSync(consumerGroupName, partitionId, EventPosition.fromStartOfStream());
				try {
					Assert.assertTrue(receiver.receiveSync(100).iterator().hasNext());
				} finally {
					receiver.closeSync();
				}
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
						ehClient.closeSync();
					}
				}
			}

			executorService.shutdown();
		}
	}
	
}
