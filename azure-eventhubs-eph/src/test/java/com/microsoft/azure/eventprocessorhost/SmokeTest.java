/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import org.junit.Test;

import com.microsoft.azure.eventhubs.EventHubClient;
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
		EventProcessorOptions options = EventProcessorOptions.getDefaultOptions();
		options.setInitialOffsetProvider((partitionId) -> { return Instant.now(); });
		PerTestSettings settings = testSetup("receiveFromNowTest", options);

		settings.utils.sendToAny(settings.telltale);
		waitForTelltale(settings);

		testFinish(settings, 1);
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
		settings.utils.sendToAny(settings.telltale);
		waitForTelltale(settings);
		
		testFinish(settings, (settings.partitionIds.size() * maxGeneration) + 1);
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
				EventHubClient.DEFAULT_CONSUMER_GROUP_NAME, utils.getConnectionString().toString(),
				TestUtilities.getStorageConnectionString(), storageName);
		EventProcessorOptions options1 = EventProcessorOptions.getDefaultOptions();
		options1.setExceptionNotification(general1);
		
		PrefabGeneralErrorHandler general2 = new PrefabGeneralErrorHandler();
		PrefabProcessorFactory factory2 = new PrefabProcessorFactory(telltale, doCheckpointing, doMarker);
		EventProcessorHost host2 = new EventProcessorHost(conflictingName, utils.getConnectionString().getEntityPath(),
				EventHubClient.DEFAULT_CONSUMER_GROUP_NAME, utils.getConnectionString().toString(),
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
			Thread.sleep(100);
		}
	}
	
	PerTestSettings testSetup(String testName) throws Exception
	{
		return testSetup(testName, EventProcessorOptions.getDefaultOptions());
	}
	
	PerTestSettings testSetup(String testName, EventProcessorOptions options) throws Exception
	{
		System.out.println(testName + " starting");
		
		PerTestSettings settings = new PerTestSettings(testName);
		
		settings.utils = new RealEventHubUtilities();
		settings.partitionIds = settings.utils.setup();

		settings.telltale = settings.testName + "-telltale-" + EventProcessorHost.safeCreateUUID();
		settings.general = new PrefabGeneralErrorHandler();
		settings.factory = new PrefabProcessorFactory(settings.telltale, false, true);
		
		String storageContainerName = settings.testName.toLowerCase() + "-" + EventProcessorHost.safeCreateUUID();
		settings.host = new EventProcessorHost(settings.testName + "-1", settings.utils.getConnectionString().getEntityPath(),
				EventHubClient.DEFAULT_CONSUMER_GROUP_NAME, settings.utils.getConnectionString().toString(),
				TestUtilities.getStorageConnectionString(), storageContainerName);
		options.setExceptionNotification(settings.general);
		settings.host.registerEventProcessorFactory(settings.factory, options);
		
		Thread.sleep(5000);
		
		return settings;
	}
	
	void waitForTelltale(PerTestSettings settings) throws InterruptedException
	{
		for (int i = 0; i < 100; i++)
		{
			if (settings.factory.getTelltaleFound())
			{
				System.out.println("Telltale found");
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
			assertEquals("wrong number of messages received", settings.factory.getEventsReceivedCount(), expectedMessages);
		}
		
		assertTrue("telltale message was not found", settings.factory.getTelltaleFound());
		assertEquals("partition errors seen", settings.factory.getErrors().size(), 0);
		assertEquals("general errors seen", settings.general.getErrors().size(), 0);
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
		public RealEventHubUtilities utils;
		public String telltale;
		public ArrayList<String> partitionIds;
		public PrefabGeneralErrorHandler general;
		public PrefabProcessorFactory factory;
		public EventProcessorHost host;
	}
}
