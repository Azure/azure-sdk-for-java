/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import org.junit.Test;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.PartitionReceiveHandler;
import com.microsoft.azure.eventhubs.PartitionReceiver;

public class SmokeTest extends TestBase
{
	/*
	@Test
	public void smokeTest() throws Exception
	{
		PerTestSettings settings = new PerTestSettings("smoketest");
		//settings.inBlobPrefix = "testprefix";
		settings = testSetup(settings); 

		settings.outUtils.sendToAny(settings.outTelltale);
		waitForTelltale(settings);

		testFinish(settings, SmokeTest.ANY_NONZERO_COUNT);
	}
	
	//@Test
	public void receiveFromNowTest() throws Exception
	{
		// Doing two iterations with the same "now" requires storing the "now" value instead of
		// using the current time when the initial offset provider is executed.
		final Instant storedNow = Instant.now();

		// Do the first iteration.
		PerTestSettings firstSettings = receiveFromNowIteration(storedNow, 1, 1, null);
		
		// Do a second iteration with the same "now". Because the first iteration does not checkpoint,
		// it should receive the telltale from the first iteration AND the telltale from this iteration.
		// The purpose of running a second iteration is to look for bugs that occur when leases have been
		// created and persisted but checkpoints have not, so it is vital that the second iteration uses the
		// same storage container.
		receiveFromNowIteration(storedNow, 2, 2, firstSettings.inoutStorageContainerName);
	}
	
	private PerTestSettings receiveFromNowIteration(final Instant storedNow, int iteration, int expectedMessages, String containerName) throws Exception
	{
		PerTestSettings settings = new PerTestSettings("receiveFromNowTest-iter-" + iteration);
		settings.inForcedContainerName = containerName;
		settings.inOptions.setInitialOffsetProvider((partitionId) -> { return storedNow; });
		settings = testSetup(settings);

		settings.outUtils.sendToAny(settings.outTelltale);
		waitForTelltale(settings);

		testFinish(settings, expectedMessages);
		
		return settings;
	}
	
	//@Test
	public void receiveFromCheckpoint() throws Exception
	{
		PerTestSettings firstSettings = receiveFromCheckpointIteration(1, SmokeTest.ANY_NONZERO_COUNT, null);
		receiveFromCheckpointIteration(2, firstSettings.outPartitionIds.size(), firstSettings.inoutStorageContainerName);
	}

	private PerTestSettings receiveFromCheckpointIteration(int iteration, int expectedMessages, String containerName) throws Exception
	{
		PerTestSettings settings = new PerTestSettings("receiveFromCkptTest-iter-" + iteration);
		settings.inForcedContainerName = containerName;
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
	
	//@Test
	public void receiveAllPartitionsTest() throws Exception
	{
		PerTestSettings settings = new PerTestSettings("receiveAllPartitionsTest");
		settings.inOptions.setInitialOffsetProvider((partitionId) -> { return Instant.now(); });
		settings = testSetup(settings);

		final int maxGeneration = 10;
		for (int generation = 0; generation < maxGeneration; generation++)
		{
			for (String id : settings.outPartitionIds)
			{
				settings.outUtils.sendToPartition(id, "receiveAllPartitionsTest-" + id + "-" + generation);
			}
			System.out.println("Generation " + generation + " sent");
		}
		for (String id : settings.outPartitionIds)
		{
			settings.outUtils.sendToPartition(id, settings.outTelltale);
			System.out.println("Telltale " + id + " sent");
		}
		for (String id : settings.outPartitionIds)
		{
			waitForTelltale(settings, id);
		}
		
		testFinish(settings, (settings.outPartitionIds.size() * (maxGeneration + 1))); // +1 for the telltales
	}
	
	//@Test
	public void conflictingHosts() throws Exception
	{
		System.out.println("conflictingHosts starting");
		
		RealEventHubUtilities utils = new RealEventHubUtilities();
		utils.setup(-1);
		
		String telltale = "conflictingHosts-telltale-" + EventProcessorHost.safeCreateUUID();
		String conflictingName = "conflictingHosts-NOTSAFE";
		String storageName = conflictingName.toLowerCase() + EventProcessorHost.safeCreateUUID();
		boolean doCheckpointing = false;
		boolean doMarker = false;
		
		PrefabGeneralErrorHandler general1 = new PrefabGeneralErrorHandler();
		PrefabProcessorFactory factory1 = new PrefabProcessorFactory(telltale, doCheckpointing, doMarker);
		EventProcessorHost host1 = new EventProcessorHost(conflictingName, utils.getConnectionString().getEntityPath(),
				utils.getConsumerGroup(), utils.getConnectionString().toString(),
				TestUtilities.getStorageConnectionString(), storageName);
		EventProcessorOptions options1 = EventProcessorOptions.getDefaultOptions();
		options1.setExceptionNotification(general1);
		
		PrefabGeneralErrorHandler general2 = new PrefabGeneralErrorHandler();
		PrefabProcessorFactory factory2 = new PrefabProcessorFactory(telltale, doCheckpointing, doMarker);
		EventProcessorHost host2 = new EventProcessorHost(conflictingName, utils.getConnectionString().getEntityPath(),
				utils.getConsumerGroup(), utils.getConnectionString().toString(),
				TestUtilities.getStorageConnectionString(), storageName);
		EventProcessorOptions options2 = EventProcessorOptions.getDefaultOptions();
		options2.setExceptionNotification(general2);

		host1.registerEventProcessorFactory(factory1, options1);
		host2.registerEventProcessorFactory(factory2, options2);
		
		int i = 0;
		while (true)
		{
			utils.sendToAny("conflict-" + i++, 10);
			System.out.println("\n." + factory1.getEventsReceivedCount() + "." + factory2.getEventsReceivedCount() + ":" +
					((ThreadPoolExecutor)EventProcessorHost.getExecutorService()).getPoolSize() + ":" +
					Thread.activeCount());
			//DebugThread.printThreadStatuses();
			Thread.sleep(100);
		}
	}
	*/
	
	//@Test
	public void rawEpochStealing() throws Exception
	{
		RealEventHubUtilities utils = new RealEventHubUtilities();
		utils.setup(-1);

		int clientSerialNumber = 0;
		while (true)
		{
			Thread[] blah = new Thread[Thread.activeCount() + 10];
			int actual = Thread.enumerate(blah); 
			if (actual >= blah.length)
			{
				System.out.println("Lost some threads");
			}
			int parkedCount = 0;
			String selectingList = "";
			boolean display = true;
			for (int i = 0; i < actual; i++)
			{
				display = true;
				StackTraceElement[] bloo = blah[i].getStackTrace();
				String show = "nostack";
				if (bloo.length > 0)
				{
					show = bloo[0].getClassName() + "." + bloo[0].getMethodName();
					if (show.compareTo("sun.misc.Unsafe.park") == 0)
					{
						parkedCount++;
						display = false;
					}
					else if (show.compareTo("sun.nio.ch.WindowsSelectorImpl$SubSelector.poll0") == 0)
					{
						selectingList += (" " + blah[i].getId());
						display = false;
					}
				}
				if (display)
				{
					System.out.print(" " + blah[i].getId() + ":" + show);
				}
			}
			System.out.println("\nParked: " + parkedCount + "  SELECTING: " + selectingList);
			
			System.out.println("Client " + clientSerialNumber + " starting");
			EventHubClient client = EventHubClient.createFromConnectionStringSync(utils.getConnectionString().toString());
			PartitionReceiver receiver =
					client.createEpochReceiver(utils.getConsumerGroup(), "0", PartitionReceiver.START_OF_STREAM, 1).get();

			boolean useReceiveHandler = false;
			
			if (useReceiveHandler)
			{
				Blah b = new Blah(clientSerialNumber++, receiver, client);
				receiver.setReceiveHandler(b).get();
				// wait for messages to start flowing
				b.waitForReceivedMessages().get();
			}
			else
			{
				receiver.receiveSync(1);
				System.out.println("Received a message");
			}
			
			// Enable these lines to avoid overlap
			/* */
			try
			{
				System.out.println("Non-overlap close of PartitionReceiver");
				if (useReceiveHandler)
				{
					receiver.setReceiveHandler(null).get();
				}
				receiver.close().get();
			}
			catch (InterruptedException | ExecutionException e)
			{
				System.out.println("Client " + clientSerialNumber + " failed while closing PartitionReceiver: " + e.toString());
			}
			try
			{
				System.out.println("Non-overlap close of EventHubClient");
				client.close().get();
			}
			catch (InterruptedException | ExecutionException e)
			{
				System.out.println("Client " + clientSerialNumber + " failed while closing EventHubClient: " + e.toString());
			}
			System.out.println("Client " + clientSerialNumber + " closed");
			/* */

			System.out.println("Threads: " + Thread.activeCount());
		}
	}
	
	private class Blah extends PartitionReceiveHandler
	{
		private int clientSerialNumber;
		private PartitionReceiver receiver;
		private EventHubClient client;
		private CompletableFuture<Void> receivedMessages = null;
		private boolean firstEvents = true;
		
		protected Blah(int clientSerialNumber, PartitionReceiver receiver, EventHubClient client)
		{
			super(300);
			this.clientSerialNumber = clientSerialNumber;
			this.receiver = receiver;
			this.client = client;
		}
		
		CompletableFuture<Void> waitForReceivedMessages()
		{
			this.receivedMessages = new CompletableFuture<Void>();
			return this.receivedMessages;
		}

		@Override
		public void onReceive(Iterable<EventData> events)
		{
			if (this.firstEvents)
			{
				System.out.println("Client " + this.clientSerialNumber + " got events");
				this.receivedMessages.complete(null);
				this.firstEvents = false;
			}
		}

		@Override
		public void onError(Throwable error)
		{
			System.out.println("Client " + this.clientSerialNumber + " got " + error.toString());
			try
			{
				this.receiver.close().get();
			}
			catch (InterruptedException | ExecutionException e)
			{
				System.out.println("Client " + this.clientSerialNumber + " failed while closing PartitionReceiver: " + e.toString());
			}
			try
			{
				this.client.close().get();
			}
			catch (InterruptedException | ExecutionException e)
			{
				System.out.println("Client " + this.clientSerialNumber + " failed while closing EventHubClient: " + e.toString());
			}
			System.out.println("Client " + this.clientSerialNumber + " closed");
		}
		
	}
}
