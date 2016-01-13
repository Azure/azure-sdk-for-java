package com.microsoft.azure.eventhubs.lib;

import static org.junit.Assert.*;
import java.util.*;
import com.microsoft.azure.servicebus.*;

/**
 * all tests derive from this base - provides common functionality 
 * - provides a way to checkout EventHub for each test to exclusively run with
 * - ******* Before running all Tests - fill data here ********* 
 */
public abstract class TestBase 
{
	public final static String SasRuleName = "RootManageSharedAccessKey";
		
	public static TestEventHubInfo checkoutTestEventHub()
	{
		HashMap<String, String> sasRule = new HashMap<String, String>();
		sasRule.put(TestBase.SasRuleName, "----SasKey-----");
		return new TestEventHubInfo("gojavago", "firstehub-ns", null, sasRule);
	}
	
	public static ConnectionStringBuilder getConnectionString(TestEventHubInfo eventHubInfo) {
		Map.Entry<String, String> sasRule = eventHubInfo.getSasRule();
		return new ConnectionStringBuilder(eventHubInfo.getNamespaceName(), eventHubInfo.getName(), sasRule.getKey(), sasRule.getValue());
	}

	public static void checkinTestEventHub(String name)
	{
		// TODO: Implement Checkin-Checkout functionality	
	}
}