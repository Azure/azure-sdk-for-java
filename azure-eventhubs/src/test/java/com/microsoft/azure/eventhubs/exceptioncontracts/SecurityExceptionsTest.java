package com.microsoft.azure.eventhubs.exceptioncontracts;

import org.junit.Test;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.lib.TestBase;
import com.microsoft.azure.eventhubs.lib.TestEventHubInfo;
import com.microsoft.azure.servicebus.AuthorizationFailedException;
import com.microsoft.azure.servicebus.ConnectionStringBuilder;

public class SecurityExceptionsTest extends ApiTestBase
{
	@Test (expected = AuthorizationFailedException.class)
	public void testEventHubClientUnAuthorizedAccess() throws Throwable
	{
		TestEventHubInfo eventHubInfo = TestBase.checkoutTestEventHub();
		try
		{
			ConnectionStringBuilder connectionString = new ConnectionStringBuilder(
					eventHubInfo.getNamespaceName(),
					eventHubInfo.getName(),
					"---------------wrongkey------------",
					"--------------wrongvalue-----------");
			EventHubClient ehClient = EventHubClient.createFromConnectionStringSync(connectionString.toString());
			ehClient.sendSync(new EventData("Test Message".getBytes()));
		}
		finally
		{
			TestBase.checkinTestEventHub(eventHubInfo.getName());
		}
	}
}