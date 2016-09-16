/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import org.junit.Test;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.PartitionReceiveHandler;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.servicebus.DebugThread;
import com.microsoft.azure.servicebus.ServiceBusException;

public class SmokeTest
{
	//@Test
	public void smokeTest() throws Exception
	{
		PerTestSettings settings = testSetup("smokeTest");

		settings.utils.sendToAny(settings.telltale);
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
		receiveFromNowIteration(storedNow, 2, 2, firstSettings.storageContainerName);
	}
	
	private PerTestSettings receiveFromNowIteration(final Instant storedNow, int iteration, int expectedMessages, String containerName) throws Exception
	{
		EventProcessorOptions options = EventProcessorOptions.getDefaultOptions();
		options.setInitialOffsetProvider((partitionId) -> { return storedNow; });
		PerTestSettings settings = testSetup("receiveFromNowTest-iter-" + iteration, options, containerName);

		settings.utils.sendToAny(settings.telltale);
		waitForTelltale(settings);

		testFinish(settings, expectedMessages);
		
		return settings;
	}
	
	//@Test
	public void receiveAllPartitionsTest() throws Exception
	{
		EventProcessorOptions options = EventProcessorOptions.getDefaultOptions();
		options.setInitialOffsetProvider((partitionId) -> { return Instant.now(); });
		PerTestSettings settings = testSetup("receiveAllPartitionsTest", options);

		final int maxGeneration = 10;
		for (int generation = 0; generation < maxGeneration; generation++)
		{
			for (String id : settings.partitionIds)
			{
				settings.utils.sendToPartition(id, "receiveAllPartitionsTest-" + id + "-" + generation);
			}
			System.out.println("Generation " + generation + " sent");
		}
		for (String id : settings.partitionIds)
		{
			settings.utils.sendToPartition(id, settings.telltale);
			System.out.println("Telltale " + id + " sent");
		}
		for (String id : settings.partitionIds)
		{
			waitForTelltale(settings, id);
		}
		
		testFinish(settings, (settings.partitionIds.size() * (maxGeneration + 1))); // +1 for the telltales
	}
	
	@Test
	public void conflictingHosts() throws Exception
	{
		System.out.println("conflictingHosts starting");
		
		RealEventHubUtilities utils = new RealEventHubUtilities();
		utils.setup();
		
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
			DebugThread.printThreadStatuses();
			Thread.sleep(100);
		}
	}
	
	//@Test
	public void rawEpochStealing() throws Exception
	{
		RealEventHubUtilities utils = new RealEventHubUtilities();
		utils.setup();

		int clientSerialNumber = 0;
		while (true)
		{
			DebugThread.printThreadStatuses();
			
			System.out.println("Client " + clientSerialNumber + " starting");
			EventHubClient client = EventHubClient.createFromConnectionStringSync(utils.getConnectionString().toString());
			PartitionReceiver receiver =
					client.createEpochReceiver(utils.getConsumerGroup(), "0", PartitionReceiver.START_OF_STREAM, 1).get();
			
			Blah b = new Blah(clientSerialNumber++, receiver, client);
			receiver.setReceiveHandler(b);
			
			// wait for messages to start flowing
			b.waitForReceivedMessages().get();
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
			this.receiver.close();
			this.client.close();
			System.out.println("Client " + this.clientSerialNumber + " closed");
		}
		
	}
	
	PerTestSettings testSetup(String testName) throws Exception
	{
		return testSetup(testName, EventProcessorOptions.getDefaultOptions());
	}
	
	PerTestSettings testSetup(String testName, EventProcessorOptions options) throws Exception
	{
		return testSetup(testName, options, null);
	}
	
	PerTestSettings testSetup(String testName, EventProcessorOptions options, String forcedStorageContainerName) throws Exception
	{
		System.out.println(testName + " starting");
		
		PerTestSettings settings = new PerTestSettings(testName);
		
		settings.utils = new RealEventHubUtilities();
		settings.partitionIds = settings.utils.setup();

		settings.telltale = settings.testName + "-telltale-" + EventProcessorHost.safeCreateUUID();
		settings.general = new PrefabGeneralErrorHandler();
		settings.factory = new PrefabProcessorFactory(settings.telltale, false, true);
		
		settings.storageContainerName = (forcedStorageContainerName != null) ? forcedStorageContainerName : 
			(settings.testName.toLowerCase() + "-" + EventProcessorHost.safeCreateUUID());
		settings.host = new EventProcessorHost(settings.testName + "-1", settings.utils.getConnectionString().getEntityPath(),
				settings.utils.getConsumerGroup(), settings.utils.getConnectionString().toString(),
				TestUtilities.getStorageConnectionString(), settings.storageContainerName);
		options.setExceptionNotification(settings.general);
		settings.host.registerEventProcessorFactory(settings.factory, options).get();
		
		Thread.sleep(5000);
		
		return settings;
	}
	
	void waitForTelltale(PerTestSettings settings) throws InterruptedException
	{
		for (int i = 0; i < 100; i++)
		{
			if (settings.factory.getAnyTelltaleFound())
			{
				System.out.println("Telltale found");
				break;
			}
			Thread.sleep(5000);
			System.out.println();
		}
	}
	
	void waitForTelltale(PerTestSettings settings, String partitionId) throws InterruptedException
	{
		for (int i = 0; i < 100; i++)
		{
			if (settings.factory.getTelltaleFound(partitionId))
			{
				System.out.println("Telltale " + partitionId + " found");
				break;
			}
			Thread.sleep(5000);
			System.out.println();
		}
	}

	// if expectedMessages is -1, just check for > 0
	final static int ANY_NONZERO_COUNT = -1;
	void testFinish(PerTestSettings settings, int expectedMessages) throws InterruptedException, ExecutionException, ServiceBusException
	{
		settings.host.unregisterEventProcessor();
		System.out.println("Events received: " + settings.factory.getEventsReceivedCount());
		if (expectedMessages < 0)
		{
			assertTrue("no messages received", settings.factory.getEventsReceivedCount() > 0);
		}
		else
		{
			assertEquals("wrong number of messages received", expectedMessages, settings.factory.getEventsReceivedCount());
		}
		
		assertTrue("telltale message was not found", settings.factory.getAnyTelltaleFound());
		assertEquals("partition errors seen", 0, settings.factory.getErrors().size());
		assertEquals("general errors seen", 0, settings.general.getErrors().size());
		for (String err : settings.factory.getErrors())
		{
			System.out.println(err);
		}
		for (String err : settings.general.getErrors())
		{
			System.out.println(err);
		}
		
		//EventProcessorHost.forceExecutorShutdown(10);
		settings.utils.shutdown();
		
		System.out.println(settings.testName + " ended");
	}

	
	class PerTestSettings
	{
		PerTestSettings(String testName) { this.testName = testName; }
		
		public String testName;
		public String storageContainerName;
		public RealEventHubUtilities utils;
		public String telltale;
		public ArrayList<String> partitionIds;
		public PrefabGeneralErrorHandler general;
		public PrefabProcessorFactory factory;
		public EventProcessorHost host;
	}
}
