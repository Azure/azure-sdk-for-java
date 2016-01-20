package com.microsoft.azure.eventhubs.sendrecv;

import java.io.IOException;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

import org.junit.*;

import com.microsoft.azure.eventhubs.*;
import com.microsoft.azure.eventhubs.lib.*;
import com.microsoft.azure.servicebus.*;
import com.microsoft.azure.servicebus.amqp.*;

public class ReceiveTests extends TestBase
{
	@Test()
	public void testReceiverFilters() throws ServiceBusException, InterruptedException, ExecutionException, IOException
	{
		TestEventHubInfo eventHubInfo = TestBase.checkoutTestEventHub();
		EventHubClient ehClient = null;
		PartitionReceiver offsetReceiver = null;
		PartitionReceiver datetimeReceiver = null;
		
		try 
		{
			ConnectionStringBuilder connectionString = TestBase.getConnectionString(eventHubInfo);
			ehClient = EventHubClient.createFromConnectionString(connectionString.toString()).get();
			
			String cgName = eventHubInfo.getRandomConsumerGroup();
			String partitionId = "0";
			
			TestBase.pushEventsToPartition(ehClient, partitionId, 10).get();
			
			offsetReceiver = ehClient.createReceiver(cgName, partitionId, PartitionReceiver.StartOfStream).get();
			List<EventData> startingEventsUsingOffsetReceiver = new LinkedList<EventData>(offsetReceiver.receive().get());
			
			Assert.assertTrue(startingEventsUsingOffsetReceiver != null && startingEventsUsingOffsetReceiver.size() > 0);
			
			// Test1: Validate DateTimeReceiver returns correct startingOffset with startOfEpoch
			datetimeReceiver = ehClient.createReceiver(cgName, partitionId, Instant.EPOCH).get();
			LinkedList<EventData> startingEventsUsingDateTimeReceiver = new LinkedList<EventData>(datetimeReceiver.receive().get());
			
			Assert.assertTrue(startingEventsUsingOffsetReceiver != null && startingEventsUsingDateTimeReceiver.size() > 0);
			
			int counter = 0;
			for(EventData eventDataUsingOffset: startingEventsUsingOffsetReceiver)
			{
				EventData eventDataUsingDateTime = startingEventsUsingDateTimeReceiver.get(counter);
				System.out.println(String.format("recv by offset: %s.", eventDataUsingOffset.getSystemProperties().getOffset()));
				System.out.println(String.format("recv by dateTime: %s.", eventDataUsingDateTime.getSystemProperties().getOffset()));
				
				Assert.assertTrue(eventDataUsingOffset.getSystemProperties().getOffset().equalsIgnoreCase(eventDataUsingDateTime.getSystemProperties().getOffset()));
				
				counter++;
				if (startingEventsUsingDateTimeReceiver.size() <= counter)
					break;
			}
			
			datetimeReceiver.close();
			
			// Test2: pick a random event from OffsetReceiver and then validate DateTime receiver using SystemProperties
			if (startingEventsUsingOffsetReceiver.size() <= 1) {
				startingEventsUsingOffsetReceiver = new LinkedList<EventData>(offsetReceiver.receive().get());
			}
			
			Assert.assertTrue(startingEventsUsingOffsetReceiver.size() > 0);
			datetimeReceiver = ehClient.createReceiver(cgName, partitionId, 
				startingEventsUsingOffsetReceiver.get(0).getSystemProperties().getEnqueuedTime().minusMillis(1)).get();
			
			LinkedList<EventData> dateTimeEventsFromCustomOffset = new LinkedList<EventData>(datetimeReceiver.receive().get());
			System.out.println(dateTimeEventsFromCustomOffset.get(0).getSystemProperties().getEnqueuedTime());
			Assert.assertTrue(dateTimeEventsFromCustomOffset.size() > 0);
			Assert.assertTrue(dateTimeEventsFromCustomOffset.get(0).getSystemProperties().getOffset().
					equals(startingEventsUsingOffsetReceiver.get(0).getSystemProperties().getOffset()));
		}
		finally
		{
			TestBase.checkinTestEventHub(eventHubInfo.getName());
			if (offsetReceiver != null) {
				offsetReceiver.close();
			}
			if (datetimeReceiver != null) {
				datetimeReceiver.close();
			}
			
			if (ehClient != null) {
				ehClient.close();
			}
		}
	}

}
