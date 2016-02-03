package com.microsoft.azure.eventhubs.samples;

import java.io.*;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.temporal.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

import com.google.gson.*;
import com.microsoft.azure.eventhubs.*;
import com.microsoft.azure.servicebus.*;

public class SendReceiveAsync
{
	public static void main(String[] args) 
			throws ServiceBusException, ExecutionException, InterruptedException, IOException
	{
		String username = "RootManageSharedAccessKey";
		String password = "LHbmplGdVC7Lo7A1RAXXDgeHSM9WHIRvZmIt7m1y5w0=";
		String namespaceName = "firstehub-ns";
		
		ConnectionStringBuilder connStr = new ConnectionStringBuilder(namespaceName, "gojavago", username, password);
		// ConnectionStringBuilder connStr = new ConnectionStringBuilder("----namespaceName-----", "----EventHubName-----", "-----sayKeyName-----", "---SasKey----");
		
		final Gson gson = new GsonBuilder().create();
		
		final String partitionId = "0";
		CompletableFuture<EventHubClient> ehClientCreateTask = EventHubClient.createFromConnectionString(connStr.toString());
		
		Function<EventHubSender, CompletableFuture<Void>> sendBatch = new Function<EventHubSender, CompletableFuture<Void>>()
		{
			@Override
			public CompletableFuture<Void> apply(EventHubSender sender)
			{
				LinkedList<EventData> events = new LinkedList<EventData>();
				for (int count = 1; count < 11; count++)
				{
					PayloadEvent payload = new PayloadEvent(count);
					byte[] payloadBytes = gson.toJson(payload).getBytes(Charset.defaultCharset());
					EventData sendEvent = new EventData(payloadBytes);
					Map<String, String> applicationProperties = new HashMap<String, String>();
					applicationProperties.put("from", "javaClient");
					sendEvent.setProperties(applicationProperties);
					events.add(sendEvent);
				}

				System.out.println(String.format("Sent Batch... Size: %s", events.size()));
				try
				{
					return sender.send(events);
				}
				catch(ServiceBusException exception)
				{
					exception.printStackTrace();
					throw new CompletionException(exception);
				}
			}	
		};
		
		Function<PartitionReceiver, CompletableFuture<Iterable<EventData>>> receiveBatch = new Function<PartitionReceiver, CompletableFuture<Iterable<EventData>>>()
		{
			@Override
			public CompletableFuture<Iterable<EventData>> apply(PartitionReceiver receiver)
			{
				try
				{
					return receiver.receive();
				}
				catch (ServiceBusException e)
				{
					throw new CompletionException(e);
				}
			}
		};
		
		Consumer<Iterable<EventData>> printEvents = new Consumer<Iterable<EventData>>()
		{
			@Override
			public void accept(Iterable<EventData> receivedEvents)
			{
				int batchSize = 0;
				if (receivedEvents != null)
				{
					for(EventData receivedEvent: receivedEvents)
					{
						System.out.print(String.format("Offset: %s, SeqNo: %s, EnqueueTime: %s", 
								receivedEvent.getSystemProperties().getOffset(), 
								receivedEvent.getSystemProperties().getSequenceNumber(), 
								receivedEvent.getSystemProperties().getEnqueuedTime()));
						System.out.println(String.format("| Message Payload: %s", new String(receivedEvent.getBody(), Charset.defaultCharset())));
						batchSize++;
					}
				}
				
				System.out.println(String.format("ReceivedBatch Size: %s", batchSize));					
			}
		};
		
		Function<EventHubClient, CompletableFuture<EventHubSender>> senderCreate = new Function<EventHubClient, CompletableFuture<EventHubSender>>()
		{
			@Override
			public CompletableFuture<EventHubSender> apply(EventHubClient ehClient)
			{
				try {
					return ehClient.createPartitionSender(partitionId);
				} catch (ServiceBusException e) {
					e.printStackTrace();
					throw new CompletionException(null);
				}
			}
		};		

		CompletableFuture<PartitionReceiver> createReceiverTask = ehClientCreateTask
				.thenComposeAsync(new Function<EventHubClient, CompletableFuture<PartitionReceiver>>()
				{
					@Override
					public CompletableFuture<PartitionReceiver> apply(EventHubClient ehClient)
					{
						try {
							return ehClient.createReceiver(EventHubClient.DefaultConsumerGroupName, 
									partitionId, Instant.now().minus(1, ChronoUnit.MINUTES));
						} catch (ServiceBusException e) {
							e.printStackTrace();
							throw new CompletionException(e);
						}
					}
				});
		
		
		// start sending & receiving...
		CompletableFuture<EventHubSender> senderCreateTask = ehClientCreateTask.thenComposeAsync(senderCreate);
		
	}

	/**
	 * actual application-payload, ex: a telemetry event
	 */
	static final class PayloadEvent
	{
		PayloadEvent(final int seed)
		{
			this.id = "telemetryEvent1-critical-eventid-2345" + seed;
			this.strProperty = "I am a mock telemetry event from JavaClient.";
			this.longProperty = seed * new Random().nextInt(seed);
			this.intProperty = seed * new Random().nextInt(seed);
		}
		
		public String id;
		public String strProperty;
		public long longProperty;
		public int intProperty;
	}
}
