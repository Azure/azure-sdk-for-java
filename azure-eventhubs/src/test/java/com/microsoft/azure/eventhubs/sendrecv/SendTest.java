package com.microsoft.azure.eventhubs.sendrecv;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
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

import junit.framework.AssertionFailedError;

public class SendTest extends ApiTestBase
{
	static final String cgName = TestContext.getConsumerGroupName();
	static final String partitionId = "0";
	static final String ORDER_PROPERTY = "order";
	static EventHubClient ehClient;
	
	PartitionSender sender = null;
	List<PartitionReceiver> receivers = new LinkedList<PartitionReceiver>();

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
		final PartitionReceiver receiver = ehClient.createReceiverSync(cgName, partitionId, Instant.now());
		this.receivers.add(receiver);
		receiver.setReceiveTimeout(Duration.ofSeconds(1));
		receiver.setReceiveHandler(new OrderValidator(validator, batchSize));
		sender = ehClient.createPartitionSenderSync(partitionId);
		sender.sendSync(batchEvents);
		
		validator.get(10, TimeUnit.SECONDS);
	}
	
	@Test
	public void sendResultsInSysPropertiesWithPartitionKey() throws ServiceBusException, InterruptedException, ExecutionException, TimeoutException
	{
		final int partitionCount = TestContext.getPartitionCount();
		final String partitionKey = UUID.randomUUID().toString();
		CompletableFuture<Void> validateSignal = new CompletableFuture<Void>();
		PartitionKeyValidator validator = new PartitionKeyValidator(validateSignal, partitionKey, 1);
		for (int receiversCount=0; receiversCount < partitionCount; receiversCount++)
		{
			final PartitionReceiver receiver = ehClient.createReceiverSync(cgName, Integer.toString(receiversCount), Instant.now());
			receivers.add(receiver);
			receiver.setReceiveHandler(validator);
		}
		
		ehClient.sendSync(new EventData("TestMessage".getBytes()), partitionKey);
		validateSignal.get(partitionCount * 5, TimeUnit.SECONDS);
	}
	
	@Test
	public void sendBatchResultsInSysPropertiesWithPartitionKey() throws ServiceBusException, InterruptedException, ExecutionException, TimeoutException
	{
		final int batchSize = 20;
		final int partitionCount = TestContext.getPartitionCount();
		final String partitionKey = UUID.randomUUID().toString();
		CompletableFuture<Void> validateSignal = new CompletableFuture<Void>();
		PartitionKeyValidator validator = new PartitionKeyValidator(validateSignal, partitionKey, batchSize);
		for (int receiversCount = 0; receiversCount < partitionCount; receiversCount++)
		{
			final PartitionReceiver receiver = ehClient.createReceiverSync(cgName, Integer.toString(receiversCount), Instant.now());
			receivers.add(receiver);
			receiver.setReceiveHandler(validator);
		}
		
		List<EventData> events = new LinkedList<EventData>();
		for(int index = 0; index < batchSize; index++)
			events.add(new EventData("TestMessage".getBytes()));
		
		ehClient.sendSync(events, partitionKey);
		validateSignal.get(partitionCount * 5, TimeUnit.SECONDS);
	}
	
	@After
	public void cleanup() throws ServiceBusException
	{
		if (sender != null)
		{
			sender.closeSync();
			sender = null;
		}
		
		if (receivers != null && !receivers.isEmpty())
		{
			for(PartitionReceiver receiver: receivers)
				receiver.closeSync();
			
			receivers.clear();
		}
	}
	
	@AfterClass
	public static void cleanupClient() throws ServiceBusException
	{
		ehClient.closeSync();
	}
	
	public static class PartitionKeyValidator extends PartitionReceiveHandler
	{
		final CompletableFuture<Void> validateSignal;
		final String partitionKey;
		final int eventCount;
		int currentEventCount = 0;
		
		protected PartitionKeyValidator(final CompletableFuture<Void> validateSignal, final String partitionKey, final int eventCount)
		{
			super(50);
			this.validateSignal = validateSignal;
			this.partitionKey = partitionKey;
			this.eventCount = eventCount;
		}

		@Override
		public void onReceive(Iterable<EventData> events)
		{
			if (events != null & events.iterator().hasNext())
			{
				for(EventData event : events)
				{
					if (!event.getSystemProperties().getPartitionKey().equals(partitionKey))
						this.validateSignal.completeExceptionally(
								new AssertionFailedError(String.format("received partitionKey: %s, expected partitionKey: %s", event.getSystemProperties().getPartitionKey(), partitionKey)));					
					
					this.currentEventCount++;
				}
				
				if (this.currentEventCount == this.eventCount)
					this.validateSignal.complete(null);
			}
		}

		@Override
		public void onError(Throwable error)
		{
			this.validateSignal.completeExceptionally(error);
		}
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
