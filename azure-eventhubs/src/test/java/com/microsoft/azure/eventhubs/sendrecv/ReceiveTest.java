package com.microsoft.azure.eventhubs.sendrecv;

import java.time.Instant;
import java.util.Iterator;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.eventhubs.PartitionSender;
import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.lib.TestBase;
import com.microsoft.azure.eventhubs.lib.TestContext;
import com.microsoft.azure.servicebus.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.ServiceBusException;
import com.microsoft.azure.servicebus.amqp.AmqpConstants;

public class ReceiveTest extends ApiTestBase
{
	static final String cgName = TestContext.getConsumerGroupName();
	static final String partitionId = "0";
	
	static EventHubClient ehClient;
	
	PartitionReceiver offsetReceiver = null;
	PartitionReceiver datetimeReceiver = null;
	
	@BeforeClass
	public static void initializeEventHub()  throws Exception
	{
		final ConnectionStringBuilder connectionString = TestContext.getConnectionString();
		ehClient = EventHubClient.createFromConnectionStringSync(connectionString.toString());
		TestBase.pushEventsToPartition(ehClient, partitionId, 25).get();
	}
	
	@Test()
	public void testReceiverStartOfStreamFilters() throws ServiceBusException
	{
		offsetReceiver = ehClient.createReceiverSync(cgName, partitionId, PartitionReceiver.START_OF_STREAM, false);
		Iterable<EventData> startingEventsUsingOffsetReceiver = offsetReceiver.receiveSync(100);
		
		Assert.assertTrue(startingEventsUsingOffsetReceiver != null && startingEventsUsingOffsetReceiver.iterator().hasNext());
		
		datetimeReceiver = ehClient.createReceiverSync(cgName, partitionId, Instant.EPOCH);
		Iterable<EventData> startingEventsUsingDateTimeReceiver = datetimeReceiver.receiveSync(100);
		
		Assert.assertTrue(startingEventsUsingOffsetReceiver != null && startingEventsUsingDateTimeReceiver.iterator().hasNext());
		
		Iterator<EventData> dateTimeIterator = startingEventsUsingDateTimeReceiver.iterator();
		for(EventData eventDataUsingOffset: startingEventsUsingOffsetReceiver)
		{
			EventData eventDataUsingDateTime = dateTimeIterator.next();
			Assert.assertTrue(
					String.format("START_OF_STREAM offset: %s, EPOCH offset: %s", eventDataUsingOffset.getSystemProperties().getOffset(), eventDataUsingDateTime.getSystemProperties().getOffset()),
					eventDataUsingOffset.getSystemProperties().getOffset().equalsIgnoreCase(eventDataUsingDateTime.getSystemProperties().getOffset()));
			
			if (!dateTimeIterator.hasNext())
				break;
		}
	}
	
	@Test()
	public void testReceiverOffsetInclusiveFilter() throws ServiceBusException
	{
		datetimeReceiver = ehClient.createReceiverSync(cgName, partitionId, Instant.EPOCH);
		Iterable<EventData> events = datetimeReceiver.receiveSync(100);
		
		Assert.assertTrue(events != null && events.iterator().hasNext());
		EventData event = events.iterator().next();
		
		offsetReceiver = ehClient.createReceiverSync(cgName, partitionId, event.getSystemProperties().getOffset(), true);
		EventData eventReturnedByOffsetReceiver = offsetReceiver.receiveSync(10).iterator().next();
		
		Assert.assertTrue(eventReturnedByOffsetReceiver.getSystemProperties().getOffset().equals(event.getSystemProperties().getOffset()));
		Assert.assertTrue(eventReturnedByOffsetReceiver.getSystemProperties().getSequenceNumber() == event.getSystemProperties().getSequenceNumber());
	}
	
	@Test()
	public void testReceiverOffsetNonInclusiveFilter() throws ServiceBusException
	{
		datetimeReceiver = ehClient.createReceiverSync(cgName, partitionId, Instant.EPOCH);
		Iterable<EventData> events = datetimeReceiver.receiveSync(100);
		
		Assert.assertTrue(events != null && events.iterator().hasNext());
		
		EventData event = events.iterator().next();
		offsetReceiver = ehClient.createReceiverSync(cgName, partitionId, event.getSystemProperties().getOffset(), false);
		EventData eventReturnedByOffsetReceiver= offsetReceiver.receiveSync(10).iterator().next();
		
		Assert.assertTrue(eventReturnedByOffsetReceiver.getSystemProperties().getSequenceNumber() == event.getSystemProperties().getSequenceNumber() + 1);
	}
	
	@Test()
	public void testReceivedBodyAndProperties() throws ServiceBusException
	{
		datetimeReceiver = ehClient.createReceiverSync(cgName, partitionId, Instant.now());
		
		final String payload = "TestMessage1";
		final String property1 =  "property1";
		final String propertyValue1 = "something1";
		final String property2 =  AmqpConstants.AMQP_PROPERTY_MESSAGE_ID;
		final String propertyValue2 = "something2";
		
		final Consumer<EventData> validateReceivedEvent = new Consumer<EventData>()
		{
			@Override
			public void accept(EventData event)
			{
				Assert.assertTrue(new String(event.getBody(), event.getBodyOffset(), event.getBodyLength()).equals(payload));
				Assert.assertTrue(event.getProperties().containsKey(property1) && event.getProperties().get(property1).equals(propertyValue1));
				Assert.assertTrue(event.getProperties().containsKey(property2) && event.getProperties().get(property2).equals(propertyValue2));
				Assert.assertTrue(event.getSystemProperties().getOffset() != null);
				Assert.assertTrue(event.getSystemProperties().getSequenceNumber() > 0L);
				Assert.assertTrue(event.getSystemProperties().getEnqueuedTime() != null);
				Assert.assertTrue(event.getSystemProperties().getPartitionKey() == null);
				Assert.assertTrue(event.getSystemProperties().getPublisher() == null);
			}
		};
			
		final EventData sentEvent = new EventData(payload.getBytes());
		sentEvent.getProperties().put(property1, propertyValue1);
		sentEvent.getProperties().put(property2, propertyValue2);
		final PartitionSender sender = ehClient.createPartitionSenderSync(partitionId);
		try
		{
			sender.sendSync(sentEvent);
			final EventData receivedEvent = datetimeReceiver.receiveSync(10).iterator().next();
			validateReceivedEvent.accept(receivedEvent);
				
			sender.sendSync(receivedEvent);
			final EventData reSendReceivedEvent = datetimeReceiver.receiveSync(10).iterator().next();
			validateReceivedEvent.accept(reSendReceivedEvent);
		}
		finally
		{
			sender.closeSync();
		}
	}
	
	@After
	public void testCleanup() throws ServiceBusException
	{
		if (offsetReceiver != null)
		{
			offsetReceiver.closeSync();
			offsetReceiver = null;
		}
		
		if (datetimeReceiver != null)
		{
			datetimeReceiver.closeSync();
			datetimeReceiver = null;
		}
	}
	
	@AfterClass()
	public static void cleanup() throws ServiceBusException
	{
		if (ehClient != null)
		{
			ehClient.closeSync();
		}
	}
}
