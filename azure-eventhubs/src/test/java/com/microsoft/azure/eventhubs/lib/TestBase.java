package com.microsoft.azure.eventhubs.lib;

import static org.junit.Assert.*;

import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

import com.microsoft.azure.eventhubs.*;
import com.microsoft.azure.servicebus.*;

/**
 * all tests derive from this base - provides common functionality 
 * - provides a way to checkout EventHub for each test to exclusively run with
 * - ******* Before running all Tests - fill data here ********* 
 */
public abstract class TestBase 
{
	final static String NoSasKey = "---------SasKey-----------";
	final static String SasKey = NoSasKey;
	public final static String SasRuleName = "RootManageSharedAccessKey";
	
	public static TestEventHubInfo checkoutTestEventHub()
	{
		HashMap<String, String> sasRule = new HashMap<String, String>();
		sasRule.put(TestBase.SasRuleName, SasKey);
		
		// fill - in eventHub details which has atleast 4 partitions
		return new TestEventHubInfo("deviceeventstream", "FirstEHub-ns", null, sasRule);
	}
	
	public static ConnectionStringBuilder getConnectionString(TestEventHubInfo eventHubInfo)
	{
		Map.Entry<String, String> sasRule = eventHubInfo.getSasRule();
		return new ConnectionStringBuilder(eventHubInfo.getNamespaceName(), eventHubInfo.getName(), sasRule.getKey(), sasRule.getValue());
	}
	
	public static boolean isServiceRun()
	{
		return !SasKey.equalsIgnoreCase(NoSasKey);
	}

	public static void checkinTestEventHub(String name)
	{
		// TODO: Implement Checkin-Checkout functionality	
	}
	
	public static CompletableFuture<Void> pushEventsToPartition(final EventHubClient ehClient, final String partitionId, final int noOfEvents) 
			throws ServiceBusException
	{
		return ehClient.createPartitionSender(partitionId)
				.thenAcceptAsync(new Consumer<EventHubSender>()
				{
					@Override
					public void accept(EventHubSender pSender)
					{
						for (int count = 0; count< noOfEvents; count++)
						{
							EventData sendEvent = new EventData("test string".getBytes());

							try
							{
								// don't send-batch here - tests depend on an increasing timestamp on events
								pSender.send(sendEvent);
							} catch (ServiceBusException e)
							{
								e.printStackTrace();
							} finally
							{
								// close sender
							}
						}
					}
				});
	}
}