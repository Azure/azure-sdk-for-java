package com.microsoft.azure.eventhubs.concurrency;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.PartitionReceiveHandler;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.lib.TestBase;
import com.microsoft.azure.eventhubs.lib.TestContext;
import com.microsoft.azure.servicebus.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.ServiceBusException;

import junit.framework.AssertionFailedError;

public class ConcurrentReceiversTest extends ApiTestBase
{
	ConnectionStringBuilder connStr;
	int partitionCount;
	int eventSentPerPartition = 1;
	EventHubClient sender;
	
	@Before
	public void initializeEventHub()  throws Exception
	{
		partitionCount = TestContext.getPartitionCount();
		connStr = TestContext.getConnectionString();

		sender = EventHubClient.createFromConnectionString(connStr.toString()).get();

		@SuppressWarnings("unchecked")
		CompletableFuture<Void>[] sendFutures = new CompletableFuture[partitionCount];
		for (int i=0; i < partitionCount; i++)
		{
			sendFutures[i] = TestBase.pushEventsToPartition(sender, Integer.toString(i), eventSentPerPartition);
		}
		
		CompletableFuture.allOf(sendFutures).get();
	}

	@Test()
	public void testParallelReceivers() throws ServiceBusException, IOException, InterruptedException, ExecutionException
	{
		String consumerGroupName = TestContext.getConsumerGroupName();
		EventHubClient ehClient = EventHubClient.createFromConnectionStringSync(connStr.toString());
		PartitionReceiver[] receivers = new PartitionReceiver[partitionCount];
		ReceiveAtleastOneEventValidator[] counter = new ReceiveAtleastOneEventValidator[partitionCount];
		
		@SuppressWarnings("unchecked")
		CompletableFuture<Void>[] validationSignals = new CompletableFuture[partitionCount];
		try
		{
			for(int i=0; i < partitionCount; i++)
			{
				receivers[i] = ehClient.createReceiverSync(consumerGroupName, Integer.toString(i), PartitionReceiver.START_OF_STREAM);
				receivers[i].setReceiveTimeout(Duration.ofMillis(400));
				validationSignals[i] = new CompletableFuture<Void>();
				counter[i] = new ReceiveAtleastOneEventValidator(validationSignals[i], receivers[i]);
				receivers[i].setReceiveHandler(counter[i]);
			}
			
			try
			{
				CompletableFuture.allOf(validationSignals).get(partitionCount * 10, TimeUnit.SECONDS);
			}
			catch(TimeoutException toe)
			{
				throw new AssertionFailedError("few receivers failed to receive any message");
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

	public static final class ReceiveAtleastOneEventValidator extends PartitionReceiveHandler
	{
		final CompletableFuture<Void>signalReceived;
		final PartitionReceiver currentReceiver;
		
		public ReceiveAtleastOneEventValidator(final CompletableFuture<Void> signalReceived, final PartitionReceiver currentReceiver)
		{
			super(50);
			this.signalReceived = signalReceived;
			this.currentReceiver = currentReceiver;
		}

		@Override
		public void onReceive(Iterable<EventData> events)
		{
			if (events != null && events.iterator().hasNext())
			{
				this.signalReceived.complete(null);
				this.currentReceiver.setReceiveHandler(null);
			}
		}

		@Override
		public void onError(Throwable error)
		{
			this.signalReceived.completeExceptionally(error);
		}
	}
}
