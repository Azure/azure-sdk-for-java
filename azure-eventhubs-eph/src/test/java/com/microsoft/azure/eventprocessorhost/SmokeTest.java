/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.junit.Test;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.PartitionReceiveHandler;
import com.microsoft.azure.eventhubs.PartitionReceiver;

public class SmokeTest extends TestBase
{
	@Test
	public void SendRecv1MsgTest() throws Exception
	{
		PerTestSettings settings = new PerTestSettings("SendRecv1Msg");
		settings = testSetup(settings); 

		settings.outUtils.sendToAny(settings.outTelltale);
		waitForTelltale(settings);

		testFinish(settings, SmokeTest.ANY_NONZERO_COUNT);
	}
	
	@Test
	public void receiveFromNowTest() throws Exception
	{
		// Doing two iterations with the same "now" requires storing the "now" value instead of
		// using the current time when the initial offset provider is executed. It also requires
		// that the "now" be before the first send.
		final Instant storedNow = Instant.now();

		// Do the first iteration.
		PerTestSettings firstSettings = receiveFromNowIteration(storedNow, 1, 1, null);
		
		// Do a second iteration with the same "now". Because the first iteration does not checkpoint,
		// it should receive the telltale from the first iteration AND the telltale from this iteration.
		// The purpose of running a second iteration is to look for bugs that occur when leases have been
		// created and persisted but checkpoints have not, so it is vital that the second iteration uses the
		// same storage container.
		receiveFromNowIteration(storedNow, 2, 2, firstSettings.inoutEPHConstructorArgs.getStorageContainerName());
	}
	
	private PerTestSettings receiveFromNowIteration(final Instant storedNow, int iteration, int expectedMessages, String containerName) throws Exception
	{
		PerTestSettings settings = new PerTestSettings("receiveFromNow-iter-" + iteration);
		if (containerName != null)
		{
			settings.inoutEPHConstructorArgs.setStorageContainerName(containerName);
		}
		settings.inOptions.setInitialOffsetProvider((partitionId) -> { return storedNow; });
		settings = testSetup(settings);

		settings.outUtils.sendToAny(settings.outTelltale);
		waitForTelltale(settings);

		testFinish(settings, expectedMessages);
		
		return settings;
	}

	@Test
	public void receiveFromCheckpoint() throws Exception
	{
		PerTestSettings firstSettings = receiveFromCheckpointIteration(1, SmokeTest.ANY_NONZERO_COUNT, null);
		
		receiveFromCheckpointIteration(2, firstSettings.outPartitionIds.size(), firstSettings.inoutEPHConstructorArgs.getStorageContainerName());
	}

	private PerTestSettings receiveFromCheckpointIteration(int iteration, int expectedMessages, String containerName) throws Exception
	{
		PerTestSettings settings = new PerTestSettings("receiveFromCkpt-iter-" + iteration);
		if (containerName != null)
		{
			settings.inoutEPHConstructorArgs.setStorageContainerName(containerName);
		}
		settings.inDoCheckpoint = true;
		settings = testSetup(settings);

		for (String id: settings.outPartitionIds)
		{
			settings.outUtils.sendToPartition(id, settings.outTelltale);
			waitForTelltale(settings, id);
		}

		testFinish(settings, expectedMessages);
		
		return settings;
	}
	
	@Test
	public void receiveAllPartitionsTest() throws Exception
	{
		// Save "now" to avoid race with sender startup.
		final Instant savedNow = Instant.now();
		
		PerTestSettings settings = new PerTestSettings("receiveAllPartitions");
		settings.inOptions.setInitialOffsetProvider((partitionId) -> { return savedNow; });
		settings = testSetup(settings);

		final int maxGeneration = 10;
		for (int generation = 0; generation < maxGeneration; generation++)
		{
			for (String id : settings.outPartitionIds)
			{
				settings.outUtils.sendToPartition(id, "receiveAllPartitions-" + id + "-" + generation);
			}
			TestUtilities.log("Generation " + generation + " sent\n");
		}
		for (String id : settings.outPartitionIds)
		{
			settings.outUtils.sendToPartition(id, settings.outTelltale);
			TestUtilities.log("Telltale " + id + " sent\n");
		}
		for (String id : settings.outPartitionIds)
		{
			waitForTelltale(settings, id);
		}
		
		testFinish(settings, (settings.outPartitionIds.size() * (maxGeneration + 1))); // +1 for the telltales
	}
	
	@Test
	public void receiveAllPartitionsWithUserExecutorTest() throws Exception
	{
		// Save "now" to avoid race with sender startup.
		final Instant savedNow = Instant.now();
		
		PerTestSettings settings = new PerTestSettings("rcvAllPartsUserExec");
		settings.inOptions.setInitialOffsetProvider((partitionId) -> { return savedNow; });
		settings.inoutEPHConstructorArgs.setExecutor(Executors.newCachedThreadPool());
		settings = testSetup(settings);

		final int maxGeneration = 10;
		for (int generation = 0; generation < maxGeneration; generation++)
		{
			for (String id : settings.outPartitionIds)
			{
				settings.outUtils.sendToPartition(id, "receiveAllPartitionsWithUserExecutor-" + id + "-" + generation);
			}
			TestUtilities.log("Generation " + generation + " sent\n");
		}
		for (String id : settings.outPartitionIds)
		{
			settings.outUtils.sendToPartition(id, settings.outTelltale);
			TestUtilities.log("Telltale " + id + " sent\n");
		}
		for (String id : settings.outPartitionIds)
		{
			waitForTelltale(settings, id);
		}
		
		testFinish(settings, (settings.outPartitionIds.size() * (maxGeneration + 1))); // +1 for the telltales
	}
}
