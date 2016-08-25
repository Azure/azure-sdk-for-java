package com.microsoft.azure.eventhubs.lib;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Logger;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.PartitionSender;
import com.microsoft.azure.servicebus.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.ServiceBusException;

/**
 * all tests derive from this base - provides common functionality 
 * - provides a way to checkout EventHub for each test to exclusively run with
 * - ******* Before running all Tests - fill data here ********* 
 */
public abstract class TestBase 
{
	// fill - in eventHub details which has atleast 4 partitions
	final static String EVENT_HUB_ENV_NAME = "EVENT_HUB_NAME";
	final static String NAMESPACE_ENV_NAME = "NAMESPACE_NAME";
	final static String SAS_KEY_ENV_NAME = "SAS_KEY";
	final static String SAS_KEY_NAME_ENV_NAME = "SAS_KEY_NAME";
	
	public final static int PARTITION_COUNT = 4;
	
	public static final Logger TEST_LOGGER = Logger.getLogger("servicebus.test.trace");
		
	public static TestEventHubInfo checkoutTestEventHub()
	{
		HashMap<String, String> sasRule = new HashMap<String, String>();
		sasRule.put(System.getenv(SAS_KEY_NAME_ENV_NAME), System.getenv(SAS_KEY_ENV_NAME));
		
		return new TestEventHubInfo(System.getenv(EVENT_HUB_ENV_NAME), System.getenv(NAMESPACE_ENV_NAME), null, sasRule);
	}
	
	public static ConnectionStringBuilder getConnectionString(TestEventHubInfo eventHubInfo)
	{
		Map.Entry<String, String> sasRule = eventHubInfo.getSasRule();
		return new ConnectionStringBuilder(eventHubInfo.getNamespaceName(), eventHubInfo.getName(), sasRule.getKey(), sasRule.getValue());
	}
	
	public static boolean isTestConfigurationSet()
	{
		return System.getenv(EVENT_HUB_ENV_NAME) != null &&
				System.getenv(SAS_KEY_ENV_NAME) != null &&
				System.getenv(NAMESPACE_ENV_NAME) != null &&
				System.getenv(SAS_KEY_NAME_ENV_NAME) != null;
	}

	public static void checkinTestEventHub(String name)
	{
		// TODO: Implement Checkin-Checkout functionality	
		// useful when tests are running concurrently
	}
	
	public static CompletableFuture<Void> pushEventsToPartition(final EventHubClient ehClient, final String partitionId, final int noOfEvents) 
			throws ServiceBusException
	{
		return ehClient.createPartitionSender(partitionId)
				.thenAcceptAsync(new Consumer<PartitionSender>()
				{
					@Override
					public void accept(PartitionSender pSender)
					{
						for (int count = 0; count< noOfEvents; count++)
						{
							EventData sendEvent = new EventData("test string".getBytes());
							pSender.send(sendEvent);
						}
					}
				});
	}
}