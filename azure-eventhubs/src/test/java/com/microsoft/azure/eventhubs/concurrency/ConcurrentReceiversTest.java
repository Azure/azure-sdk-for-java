package com.microsoft.azure.eventhubs.concurrency;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
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

public class ConcurrentReceiversTest extends ApiTestBase
{
	static EventHubClient sender;
	static PartitionReceiver[] receivers;
	static EventHubClient ehClient;
	static String consumerGroupName;
	static ConnectionStringBuilder connStr;
	static int partitionCount;
	
	int eventSentPerPartition = 1;
	
	@BeforeClass
	public static void initialize() throws InterruptedException, ExecutionException, ServiceBusException, IOException
	{
		partitionCount = TestContext.getPartitionCount();
		connStr = TestContext.getConnectionString();

		sender = EventHubClient.createFromConnectionString(connStr.toString()).get();
		receivers = new PartitionReceiver[partitionCount];
		consumerGroupName = TestContext.getConsumerGroupName();
	}

	@Test()
	public void testParallelCreationOfReceivers() throws ServiceBusException, IOException, InterruptedException, ExecutionException, TimeoutException
	{
		ehClient = EventHubClient.createFromConnectionStringSync(connStr.toString());
		ReceiveAtleastOneEventValidator[] counter = new ReceiveAtleastOneEventValidator[partitionCount];
		
		@SuppressWarnings("unchecked") CompletableFuture<Void>[] validationSignals = new CompletableFuture[partitionCount];
		@SuppressWarnings("unchecked") CompletableFuture<Void>[] receiverFutures = new CompletableFuture[partitionCount];
		for(int i=0; i < partitionCount; i++)
		{
			final int index = i;
			receiverFutures[i] = ehClient.createReceiver(consumerGroupName, Integer.toString(i), Instant.now()).thenAcceptAsync(
					new Consumer<PartitionReceiver>(){
						@Override
						public void accept(final PartitionReceiver t) {
							receivers[index] = t;
							receivers[index].setReceiveTimeout(Duration.ofMillis(400));
							validationSignals[index] = new CompletableFuture<Void>();
							counter[index] = new ReceiveAtleastOneEventValidator(validationSignals[index], receivers[index]);
							receivers[index].setReceiveHandler(counter[index]);
						}});
		}
		
		CompletableFuture.allOf(receiverFutures).get(partitionCount * 10, TimeUnit.SECONDS);
		
		@SuppressWarnings("unchecked")
		CompletableFuture<Void>[] sendFutures = new CompletableFuture[partitionCount];
		for (int i=0; i < partitionCount; i++)
		{
			sendFutures[i] = TestBase.pushEventsToPartition(sender, Integer.toString(i), eventSentPerPartition);
		}
		
		CompletableFuture.allOf(sendFutures).get();		
		
		CompletableFuture.allOf(validationSignals).get(partitionCount * 10, TimeUnit.SECONDS);
	}
	
	@After
	public void cleanupTest()
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

	@AfterClass()
	public static void cleanup()
	{
		if (sender != null)
		{
			sender.close();
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
