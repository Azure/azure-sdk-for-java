package com.microsoft.azure.eventhubs.samples;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.Random;
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
		LinkedList<EventData> events = new LinkedList<EventData>();
		
		for (int count  = 1; count< 20; count++)
		{
			PayloadEvent payload = new PayloadEvent(count);
			byte[] payloadBytes = gson.toJson(payload).getBytes(Charset.defaultCharset());
			EventData sendEvent = new EventData(payloadBytes);
			events.add(sendEvent);
		}

		EventHubClient ehClient = EventHubClient.createFromConnectionString(connStr.toString()).get();
		PartitionSender sender = ehClient.createPartitionSender("1").get();
		
		sender.send(events).get();
		
		System.out.println("Send Complete...");
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
