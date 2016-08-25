package com.microsoft.azure.eventhubs.exceptioncontracts;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.lib.TestBase;
import com.microsoft.azure.eventhubs.lib.TestContext;
import com.microsoft.azure.servicebus.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.ReceiverDisconnectedException;
import com.microsoft.azure.servicebus.ServiceBusException;

public class ReceiverEpochTest extends ApiTestBase
{
	static final String cgName = TestContext.getConsumerGroupName();
	static final String partitionId = "0";

	static EventHubClient ehClient;
	
	PartitionReceiver receiver;
	
	@BeforeClass
	public static void initializeEventHub() throws ServiceBusException, IOException
	{
		final ConnectionStringBuilder connectionString = TestContext.getConnectionString();
		ehClient = EventHubClient.createFromConnectionStringSync(connectionString.toString());
	}
	
	@Test (expected = ReceiverDisconnectedException.class)
	public void testEpochReceiverWins() throws ServiceBusException, InterruptedException, ExecutionException
	{
		int sendEventCount = 5;
		
		PartitionReceiver receiverLowEpoch = ehClient.createReceiverSync(cgName, partitionId, Instant.now());
		receiverLowEpoch.setReceiveTimeout(Duration.ofSeconds(2));
		TestBase.pushEventsToPartition(ehClient, partitionId, sendEventCount).get();
		receiverLowEpoch.receiveSync(20);
		
		receiver = ehClient.createEpochReceiverSync(cgName, partitionId, Instant.now(), Long.MAX_VALUE);
		
		for (int retryCount = 0; retryCount < sendEventCount; retryCount ++) // retry to flush all msgs in cache
			receiverLowEpoch.receiveSync(10);
	}

	@Test (expected = ReceiverDisconnectedException.class)
	public void testOldHighestEpochWins() throws ServiceBusException, InterruptedException, ExecutionException
	{
		Instant testStartTime = Instant.now();
		long epoch = Math.abs(new Random().nextLong());

		if (epoch < 11L)
			epoch += 11L;
		
		receiver = ehClient.createEpochReceiverSync(cgName, partitionId, testStartTime, epoch);
		receiver.setReceiveTimeout(Duration.ofSeconds(10));
		ehClient.createEpochReceiverSync(cgName, partitionId, PartitionReceiver.START_OF_STREAM, false, epoch - 10);
		
		TestBase.pushEventsToPartition(ehClient, partitionId, 5).get();
		Assert.assertTrue(receiver.receiveSync(10).iterator().hasNext());
	}
	
	@Test (expected = ReceiverDisconnectedException.class)
	public void testNewHighestEpochWins() throws ServiceBusException, InterruptedException, ExecutionException
	{
		int sendEventCount = 5;
		long epoch = new Random().nextInt(Integer.MAX_VALUE);

		PartitionReceiver receiverLowEpoch = ehClient.createEpochReceiverSync(cgName, partitionId, Instant.now(), epoch);
		receiverLowEpoch.setReceiveTimeout(Duration.ofSeconds(2));
		TestBase.pushEventsToPartition(ehClient, partitionId, sendEventCount).get();
		receiverLowEpoch.receiveSync(20);
		
		receiver = ehClient.createEpochReceiverSync(cgName, partitionId, Instant.now(), Long.MAX_VALUE);
		
		for (int retryCount = 0; retryCount < sendEventCount; retryCount ++) // retry to flush all msgs in cache
			receiverLowEpoch.receiveSync(10);
	}
	
	@After
	public void testCleanup() throws ServiceBusException
	{
		if (receiver != null)
		{
			receiver.closeSync();
		}
	}
	
	@AfterClass
	public static void cleanup() throws ServiceBusException
	{
		ehClient.closeSync();
	}
}
