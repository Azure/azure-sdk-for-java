package com.microsoft.azure.eventhubs.samples;

import java.nio.charset.*;
import java.util.*;

import com.google.gson.*;
import com.microsoft.azure.eventhubs.*;
import com.microsoft.azure.servicebus.*;

public class SendRecv {

	public static void main(String[] args) throws Exception {
		
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
		ehClient.send(sendEvent); // basic send - not tied to any partition
		
		String partitionKey = "partitionTheStream";
		ehClient.send(sendEvent, partitionKey); // all events with same partitionKey lands on Same partition
		
		PartitionSender sender = ehClient.createPartitionSender("0"); // Send to a specific partition
		sender.send(sendEvent); // a non-blocking CompletableFuture variant is available for all APIs
		
		// receiver
		String partitionId = "0"; // we will provide an API to get PartitionIds in V0.2
		String startReceivingFromOffset = "637272";
		PartitionReceiver receiver = ehClient.createReceiver(EventHubClient.DefaultConsumerGroupName, partitionId, startReceivingFromOffset, false);
		Collection<EventData> receivedEvents = receiver.receive(); // a non-blocking CompletableFuture variant is available
		
		for(EventData receivedEvent: receivedEvents) {
			System.out.println(String.format("Message Payload: %s", new String(receivedEvent.getBody(), Charset.defaultCharset())));
			System.out.println(String.format("Offset: %s, SeqNo: %s, EnqueueTime: %s", 
					receivedEvent.getSystemProperties().getOffset(), receivedEvent.getSystemProperties().getSequenceNumber(), receivedEvent.getSystemProperties().getEnqueuedTimeUtc()));
		}
		
		System.in.read();
	}

	public static final class PayloadEvent {
		PayloadEvent() {  }
		
		public String strProperty;
		public long longProperty;
		public int intProperty;
	}
}
