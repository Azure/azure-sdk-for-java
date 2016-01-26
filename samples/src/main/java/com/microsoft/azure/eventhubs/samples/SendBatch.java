package com.microsoft.azure.eventhubs.samples;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ExecutionException;

import com.google.gson.*;
import com.microsoft.azure.eventhubs.*;
import com.microsoft.azure.servicebus.*;

public class SendBatch
{

	public static void main(String[] args) 
			throws ServiceBusException, ExecutionException, InterruptedException, IOException
	{
		ConnectionStringBuilder connStr = new ConnectionStringBuilder("----namespaceName-----", "----EventHubName-----", "-----sayKeyName-----", "---SasKey----");
		
		Gson gson = new GsonBuilder().create();
		
		EventHubClient ehClient = EventHubClient.createFromConnectionString(connStr.toString()).get();
		EventHubSender sender = ehClient.createPartitionSender("0").get();
		
		while (true)
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

			sender.send(events).get();
			System.out.println(String.format("Sent Batch... Size: %s", events.size()));
		}		
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
