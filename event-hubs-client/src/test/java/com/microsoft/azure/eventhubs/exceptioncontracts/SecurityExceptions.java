package com.microsoft.azure.eventhubs.exceptioncontracts;

import static org.junit.Assert.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.logging.*;
import org.junit.*;

import com.microsoft.azure.eventhubs.*;
import com.microsoft.azure.eventhubs.lib.*;
import com.microsoft.azure.servicebus.*;

public class SecurityExceptions extends TestBase
{
	private static final Logger LOGGER = Logger.getLogger(SecurityExceptions.class.getName());
	
	@Test (expected = AuthorizationFailedException.class)
	public void testEventHubClientUnAuthorizedAccess() throws Throwable
	{
		TestEventHubInfo eventHubInfo = TestBase.checkoutTestEventHub();
		try {
			ConnectionStringBuilder connectionString = new ConnectionStringBuilder(eventHubInfo.getNamespaceName(), eventHubInfo.getName(), TestBase.SasRuleName, "wrongvalue");
			
			try
			{
				EventHubClient.createFromConnectionString(connectionString.toString()).get();		
			}
			catch(ExecutionException exp) {
				throw exp.getCause();
			}
		}
		finally {
			TestBase.checkinTestEventHub(eventHubInfo.getName());
		}
	}
}