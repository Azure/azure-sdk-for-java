package com.microsoft.azure.eventhubs.sendrecv;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.PartitionReceiveHandler;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.eventhubs.PartitionSender;
import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.lib.TestContext;
import com.microsoft.azure.servicebus.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.ServiceBusException;

public class SendTest extends ApiTestBase
{
	static final String cgName = TestContext.getConsumerGroupName();
	static final String partitionId = "0";
	static final String ORDER_PROPERTY = "order";
	static EventHubClient ehClient;
	
	PartitionSender sender = null;
	PartitionReceiver receiver = null;

	@BeforeClass
	public static void initializeEventHub()  throws Exception
	{
		final ConnectionStringBuilder connectionString = TestContext.getConnectionString();
		ehClient = EventHubClient.createFromConnectionStringSync(connectionString.toString());
	}
	
	@Test
	public void sendBatchRetainsOrderWithinBatch() throws ServiceBusException, InterruptedException, ExecutionException, TimeoutException
	{
		LinkedList<EventData> batchEvents = new LinkedList<EventData>();
		final int batchSize = 50;
		for (int count = 0; count< batchSize; count++)
		{
			EventData event = new EventData("a".getBytes());
			event.getProperties().put(ORDER_PROPERTY, Integer.toString(count));
			batchEvents.add(event);
		}
		
		final CompletableFuture<Void> validator = new CompletableFuture<>();
		receiver = ehClient.createReceiverSync(cgName, partitionId, Instant.now());
		receiver.setReceiveTimeout(Duration.ofSeconds(1));
		receiver.setReceiveHandler(new OrderValidator(validator, batchSize));
		sender = ehClient.createPartitionSenderSync(partitionId);
		sender.sendSync(batchEvents);
		
		validator.get(10, TimeUnit.SECONDS);
	}
	
	@After
	public void cleanup() throws ServiceBusException
	{
		if (sender != null)
		{
			sender.closeSync();
			sender = null;
		}
		
		if (receiver != null)
		{
			receiver.closeSync();
			receiver = null;
		}
	}
	
	@AfterClass
	public static void cleanupClient() throws ServiceBusException
	{
		ehClient.closeSync();
	}
	
	public static class OrderValidator extends PartitionReceiveHandler
	{
		final CompletableFuture<Void> validateSignal;
		final int netEventCount;
		
		int currentCount = 0;
		
		public OrderValidator(final CompletableFuture<Void> validateSignal, final int netEventCount)
		{
			super(100);
			this.validateSignal = validateSignal;
			this.netEventCount = netEventCount;
		}

		@Override
		public void onReceive(Iterable<EventData> events)
		{
			if (events != null)
				for(EventData event: events)
				{
					final String currentEventOrder = event.getProperties().get(ORDER_PROPERTY);
					if (Integer.parseInt(currentEventOrder) != currentCount)
						this.validateSignal.completeExceptionally(new AssertionError(String.format("expected %s, got %s", currentCount, currentEventOrder)));
				
					currentCount++;
				}
			
			if (currentCount >= netEventCount)
				this.validateSignal.complete(null);
		}

		@Override
		public void onError(Throwable error)
		{
			this.validateSignal.completeExceptionally(error);
		}		
	}
}
