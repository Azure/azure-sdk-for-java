package com.microsoft.azure.eventhubs.samples;

import java.io.IOException;
import java.nio.charset.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;

import com.google.gson.*;
import com.microsoft.azure.eventhubs.*;
import com.microsoft.azure.servicebus.*;

public class Send
{

	public static void main(String[] args) 
			throws ServiceBusException, ExecutionException, InterruptedException, IOException
	{
		ConnectionStringBuilder connStr = new ConnectionStringBuilder("----namespaceName-----", "----EventHubName-----", "-----sayKeyName-----", "---SasKey----");
		
		Gson gson = new GsonBuilder().create();
		
		PayloadEvent payload = new PayloadEvent(1);
		byte[] payloadBytes = gson.toJson(payload).getBytes(Charset.defaultCharset());
		EventData sendEvent = new EventData(payloadBytes);
		
		// senders
		// Type-1 - Simple Send - not tied to any partition
		EventHubClient ehClient = EventHubClient.createFromConnectionString(connStr.toString()).get();
		ehClient.send(sendEvent).get();
		
		// Type-2 - Send using PartitionKey - all Events with Same partitionKey will land on the Same Partition
		String partitionKey = "partitionTheStream";
		ehClient.send(sendEvent, partitionKey).get();
		
		// Type-3 - Send to a Specific Partition
		EventHubSender sender = ehClient.createPartitionSender("0").get();
		sender.send(sendEvent).get();
		
		System.out.println(Instant.now() + ": Send Complete...");
		System.in.read();
	}

	/**
	 * actual application-payload, ex: a telemetry event
	 */
	static final class PayloadEvent
	{
		PayloadEvent(final int seed)
		{
			this.id = "telemetryEvent1-critical-eventid-2345" + seed;
			this.strProperty = "This is a sample payloadEvent, which could be wrapped using eventdata and sent to eventhub." +
			" None of the payload event properties will be looked-at by EventHubs client or Service." + 
			" As far as EventHubs service/client is concerted, it is plain bytes being sent as 1 Event.";
			this.longProperty = seed * new Random().nextInt(seed);
			this.intProperty = seed * new Random().nextInt(seed);
		}
		
		public String id;
		public String strProperty;
		public long longProperty;
		public int intProperty;
	}
}
