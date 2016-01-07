package com.microsoft.azure.eventhubs.samples;

import java.io.IOException;
import java.nio.charset.*;
import java.util.*;
import java.util.concurrent.ExecutionException;

import com.google.gson.*;
import com.microsoft.azure.eventhubs.*;
import com.microsoft.azure.servicebus.*;

public class SendRecv
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
		EventHubClient ehClient = EventHubClient.createFromConnectionString(connStr.toString()).get();
		ehClient.send(sendEvent).get(); // basic send - not tied to any partition
		
		String partitionKey = "partitionTheStream";
		ehClient.send(sendEvent, partitionKey).get(); // all events with same partitionKey lands on Same partition
		
		PartitionSender sender = ehClient.createPartitionSender("0").get(); // Send to a specific partition
		sender.send(sendEvent).get();
		
		// receiver
		String partitionId = "0"; // API to get PartitionIds will be released as part of V0.2
		PartitionReceiver receiver = ehClient.createReceiver(
				EventHubClient.DefaultConsumerGroupName, 
				partitionId, 
				PartitionReceiver.StartOfStream, 
				false).get();
		Collection<EventData> receivedEvents = receiver.receive().get();
		
		for(EventData receivedEvent: receivedEvents)
		{
			System.out.println(String.format("Message Payload: %s", new String(receivedEvent.getBody(), Charset.defaultCharset())));
			System.out.println(String.format("Offset: %s, SeqNo: %s, EnqueueTime: %s", 
					receivedEvent.getSystemProperties().getOffset(), 
					receivedEvent.getSystemProperties().getSequenceNumber(), 
					receivedEvent.getSystemProperties().getEnqueuedTimeUtc()));
		}
		
		System.in.read();
	}

	/**
	 * actual application-payload, ex: a telemetry event
	 */
	static final class PayloadEvent
	{
		PayloadEvent() {  }
		
		public String strProperty;
		public long longProperty;
		public int intProperty;
	}
}
