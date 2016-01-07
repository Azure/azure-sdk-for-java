package com.microsoft.azure.eventhubs.exceptioncontracts;

import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

import org.junit.*;

import com.microsoft.azure.eventhubs.*;
import com.microsoft.azure.eventhubs.lib.*;
import com.microsoft.azure.servicebus.*;

public class RetryPolicyTests extends TestBase
{
	@Test
	public void testRetryPolicy() throws Exception
	{
		String clientId = "someClientEntity";
		RetryPolicy retry = RetryPolicy.getDefault();
		
		retry.incrementRetryCount(clientId);
		Duration firstRetryInterval = retry.getNextRetryInterval(clientId, new InternalServerException(), Duration.ofSeconds(60));
		System.out.println(firstRetryInterval.toString());
		Assert.assertTrue(firstRetryInterval != null);
		
		retry.incrementRetryCount(clientId);
		Duration secondRetryInterval = retry.getNextRetryInterval(clientId, new InternalServerException(), Duration.ofSeconds(60));
		System.out.println(secondRetryInterval.toString());
		
		Assert.assertTrue(secondRetryInterval != null);
		Assert.assertTrue(secondRetryInterval.getSeconds() > firstRetryInterval.getSeconds() ||
				(secondRetryInterval.getSeconds() == firstRetryInterval.getSeconds() && secondRetryInterval.getNano() > firstRetryInterval.getNano()));
		
		retry.incrementRetryCount(clientId);
		Duration thirdRetryInterval = retry.getNextRetryInterval(clientId, new InternalServerException(), Duration.ofSeconds(60));
		System.out.println(thirdRetryInterval.toString());
		
		Assert.assertTrue(thirdRetryInterval != null);
		Assert.assertTrue(thirdRetryInterval.getSeconds() > secondRetryInterval.getSeconds() ||
				(thirdRetryInterval.getSeconds() == secondRetryInterval.getSeconds() && thirdRetryInterval.getNano() > secondRetryInterval.getNano()));
		
		retry.incrementRetryCount(clientId);
		Duration fourthRetryInterval = retry.getNextRetryInterval(clientId, new InternalServerException(), Duration.ofSeconds(60));
		System.out.println(fourthRetryInterval.toString());
		
		Assert.assertTrue(fourthRetryInterval != null);
		Assert.assertTrue(fourthRetryInterval.getSeconds() > thirdRetryInterval.getSeconds() ||
				(fourthRetryInterval.getSeconds() == thirdRetryInterval.getSeconds() && fourthRetryInterval.getNano() > thirdRetryInterval.getNano()));
		
		
		retry.incrementRetryCount(clientId);
		Duration nextRetryInterval = retry.getNextRetryInterval(clientId, new AuthorizationFailedException("authorizationerror"), Duration.ofSeconds(60));
		Assert.assertTrue(nextRetryInterval == null);
		
		retry.resetRetryCount(clientId);
		retry.incrementRetryCount(clientId);
		Duration firstRetryIntervalAfterReset = retry.getNextRetryInterval(clientId, new InternalServerException(), Duration.ofSeconds(60));
		Assert.assertTrue(firstRetryInterval.equals(firstRetryIntervalAfterReset));
	}
}
