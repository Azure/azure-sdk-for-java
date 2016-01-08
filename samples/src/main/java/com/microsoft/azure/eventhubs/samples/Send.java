package com.microsoft.azure.eventhubs.samples;

import java.io.IOException;
import java.nio.charset.*;
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
		
		PayloadEvent payload = new PayloadEvent();
		payload.strProperty = "telemetryEvent1-critical-eventid";
		payload.intProperty = 4;
		payload.longProperty = 34343;
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
		PartitionSender sender = ehClient.createPartitionSender("0").get();
		sender.send(sendEvent).get();
		
		System.out.println("Send Complete...");
		System.in.read();
	}

	/**
	 * actual application-payload, ex: a telemetry event
	 */
	static final class PayloadEvent
	{
		PayloadEvent()	{}
		
		public String strProperty;
		public long longProperty;
		public int intProperty;
	}
}
