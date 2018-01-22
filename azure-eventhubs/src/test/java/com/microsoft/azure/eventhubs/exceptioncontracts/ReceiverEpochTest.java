/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.exceptioncontracts;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import com.microsoft.azure.eventhubs.*;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.lib.TestBase;
import com.microsoft.azure.eventhubs.lib.TestContext;

public class ReceiverEpochTest extends ApiTestBase
{
	static final String cgName = TestContext.getConsumerGroupName();
	static final String partitionId = "0";

	static EventHubClient ehClient;
	
	PartitionReceiver receiver;
	
	@BeforeClass
	public static void initializeEventHub() throws EventHubException, IOException
	{
		final ConnectionStringBuilder connectionString = TestContext.getConnectionString();
		ehClient = EventHubClient.createFromConnectionStringSync(connectionString.toString(), TestContext.EXECUTOR_SERVICE);
	}
	
	@Test (expected = ReceiverDisconnectedException.class)
	public void testEpochReceiverWins() throws EventHubException, InterruptedException, ExecutionException
	{
		int sendEventCount = 5;
		
		PartitionReceiver receiverLowEpoch = ehClient.createReceiverSync(cgName, partitionId, EventPosition.fromEnqueuedTime(Instant.now()));
		receiverLowEpoch.setReceiveTimeout(Duration.ofSeconds(2));
		TestBase.pushEventsToPartition(ehClient, partitionId, sendEventCount).get();
		receiverLowEpoch.receiveSync(20);
		
		receiver = ehClient.createEpochReceiverSync(cgName, partitionId, EventPosition.fromEnqueuedTime(Instant.now()), Long.MAX_VALUE);
		
		for (int retryCount = 0; retryCount < sendEventCount; retryCount ++) // retry to flush all msgs in cache
			receiverLowEpoch.receiveSync(10);
	}

	@Test (expected = ReceiverDisconnectedException.class)
	public void testOldHighestEpochWins() throws EventHubException, InterruptedException, ExecutionException
	{
		Instant testStartTime = Instant.now();
		long epoch = Math.abs(new Random().nextLong());

		if (epoch < 11L)
			epoch += 11L;
		
		receiver = ehClient.createEpochReceiverSync(cgName, partitionId, EventPosition.fromEnqueuedTime(testStartTime), epoch);
		receiver.setReceiveTimeout(Duration.ofSeconds(10));
		ehClient.createEpochReceiverSync(cgName, partitionId, EventPosition.fromStartOfStream(), epoch - 10);
		
		TestBase.pushEventsToPartition(ehClient, partitionId, 5).get();
		Assert.assertTrue(receiver.receiveSync(10).iterator().hasNext());
	}
	
	@Test (expected = ReceiverDisconnectedException.class)
	public void testNewHighestEpochWins() throws EventHubException, InterruptedException, ExecutionException
	{
		int sendEventCount = 5;
		long epoch = new Random().nextInt(Integer.MAX_VALUE);

		PartitionReceiver receiverLowEpoch = ehClient.createEpochReceiverSync(cgName, partitionId, EventPosition.fromEnqueuedTime(Instant.now()), epoch);
		receiverLowEpoch.setReceiveTimeout(Duration.ofSeconds(2));
		TestBase.pushEventsToPartition(ehClient, partitionId, sendEventCount).get();
		receiverLowEpoch.receiveSync(20);
		
		receiver = ehClient.createEpochReceiverSync(cgName, partitionId, EventPosition.fromEnqueuedTime(Instant.now()), Long.MAX_VALUE);
		
		for (int retryCount = 0; retryCount < sendEventCount; retryCount ++) // retry to flush all msgs in cache
			receiverLowEpoch.receiveSync(10);
	}
	
	@After
	public void testCleanup() throws EventHubException
	{
		if (receiver != null)
		{
			receiver.closeSync();
		}
	}
	
	@AfterClass
	public static void cleanup() throws EventHubException
	{
		ehClient.closeSync();
	}
}
