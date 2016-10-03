package com.microsoft.azure.eventhubs.exceptioncontracts;

import org.junit.After;
import org.junit.Test;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.lib.TestContext;
import com.microsoft.azure.servicebus.AuthorizationFailedException;
import com.microsoft.azure.servicebus.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.ServiceBusException;

public class SecurityExceptionsTest extends ApiTestBase
{
	final static String PARTITION_ID = "0"; 
	EventHubClient ehClient;
	
	@Test (expected = AuthorizationFailedException.class)
	public void testEventHubClientUnAuthorizedAccessKeyName() throws Throwable
	{
		final ConnectionStringBuilder correctConnectionString = TestContext.getConnectionString();
		final ConnectionStringBuilder connectionString = new ConnectionStringBuilder(
				correctConnectionString.getEndpoint(),
				correctConnectionString.getEntityPath(),
				"---------------wrongkey------------",
				correctConnectionString.getSasKey());
		
		ehClient = EventHubClient.createFromConnectionStringSync(connectionString.toString());
		ehClient.sendSync(new EventData("Test Message".getBytes()));
	}
	
	@Test (expected = AuthorizationFailedException.class)
	public void testEventHubClientUnAuthorizedAccessKey() throws Throwable
	{
		final ConnectionStringBuilder correctConnectionString = TestContext.getConnectionString();
		final ConnectionStringBuilder connectionString = new ConnectionStringBuilder(
				correctConnectionString.getEndpoint(),
				correctConnectionString.getEntityPath(),
				correctConnectionString.getSasKeyName(),
				"--------------wrongvalue-----------");
		
		ehClient = EventHubClient.createFromConnectionStringSync(connectionString.toString());
		ehClient.sendSync(new EventData("Test Message".getBytes()));
	}
	
	@Test (expected = AuthorizationFailedException.class)
	public void testUnAuthorizedAccessSenderCreation() throws Throwable
	{
		final ConnectionStringBuilder correctConnectionString = TestContext.getConnectionString();
		final ConnectionStringBuilder connectionString = new ConnectionStringBuilder(
				correctConnectionString.getEndpoint(),
				correctConnectionString.getEntityPath(),
				"---------------wrongkey------------",
				correctConnectionString.getSasKey());
		
		ehClient = EventHubClient.createFromConnectionStringSync(connectionString.toString());
		ehClient.createPartitionSenderSync(PARTITION_ID);
	}
	
	@Test (expected = AuthorizationFailedException.class)
	public void testUnAuthorizedAccessReceiverCreation() throws Throwable
	{
		final ConnectionStringBuilder correctConnectionString = TestContext.getConnectionString();
		final ConnectionStringBuilder connectionString = new ConnectionStringBuilder(
				correctConnectionString.getEndpoint(),
				correctConnectionString.getEntityPath(),
				"---------------wrongkey------------",
				correctConnectionString.getSasKey());
		
		ehClient = EventHubClient.createFromConnectionStringSync(connectionString.toString());
		ehClient.createReceiverSync(TestContext.getConsumerGroupName(), PARTITION_ID, PartitionReceiver.START_OF_STREAM);
	}
	
	@After
	public void cleanup() throws ServiceBusException
	{
		ehClient.closeSync();
	}
}