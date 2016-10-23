/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

import org.junit.Test;

import com.microsoft.azure.eventhubs.EventHubClient;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class PartitionManagerTest
{
	private ILeaseManager[] leaseManagers;
	private ICheckpointManager[] checkpointManagers;
	private EventProcessorHost[] hosts;
	private TestPartitionManager[] partitionManagers;
	private boolean[] running;
	
	private int countOfChecks;
	private int desiredDistributionDetected;
	
	private boolean keepGoing;
	private boolean expectEqualDistribution;
	private boolean ignoreZeroes;
	private int maxChecks;
	private boolean shuttingDown;
	
	@Test
	public void partitionBalancingExactMultipleTest() throws Exception
	{
		TestUtilities.log("partitionBalancingExactMultipleTest");
		
		setup(2, 4); // two hosts, four partitions
		this.countOfChecks = 0;
		this.desiredDistributionDetected = 0;
		this.keepGoing = true;
		this.expectEqualDistribution = true;
		this.ignoreZeroes = false;
		this.maxChecks = 20;
		startManagers();
		
		// Poll until checkPartitionDistribution() declares that it's time to stop.
		while (this.keepGoing)
		{
			try
			{
				Thread.sleep(15000);
			}
			catch (InterruptedException e)
			{
				TestUtilities.log("Sleep interrupted, emergency bail");
				Thread.currentThread().interrupt();
				throw e;
			}
		}
		
		stopManagers();
		
		assertTrue("Desired distribution never reached or was not stable", this.desiredDistributionDetected >= this.partitionManagers.length);

		boolean boolret = this.leaseManagers[0].deleteLeaseStore().get();
		assertTrue("failed while cleaning up lease store", boolret);
		boolret = this.checkpointManagers[0].deleteCheckpointStore().get();
		assertTrue("failed while cleaning up checkpoint store", boolret);
		
		TestUtilities.log("DONE");
	}
	
	@Test
	public void partitionBalancingUnevenTest() throws Exception
	{
		TestUtilities.log("partitionBalancingUnevenTest");
		
		setup(5, 16); // five hosts, sixteen partitions
		this.countOfChecks = 0;
		this.desiredDistributionDetected = 0;
		this.keepGoing = true;
		this.expectEqualDistribution = false;
		this.ignoreZeroes = false;
		this.maxChecks = 35;
		startManagers();
		
		// Poll until checkPartitionDistribution() declares that it's time to stop.
		while (this.keepGoing)
		{
			try
			{
				Thread.sleep(15000);
			}
			catch (InterruptedException e)
			{
				TestUtilities.log("Sleep interrupted, emergency bail");
				Thread.currentThread().interrupt();
				throw e;
			}
		}
		
		stopManagers();
		
		assertTrue("Desired distribution never reached or was not stable", this.desiredDistributionDetected >= this.partitionManagers.length);

		boolean boolret = this.leaseManagers[0].deleteLeaseStore().get();
		assertTrue("failed while cleaning up lease store", boolret);
		boolret = this.checkpointManagers[0].deleteCheckpointStore().get();
		assertTrue("failed while cleaning up checkpoint store", boolret);
		
		TestUtilities.log("DONE");
	}
	
	@Test
	public void partitionRebalancingTest() throws Exception
	{
		TestUtilities.log("partitionRebalancingTest");
		
		setup(3,8); // three hosts, eight partitions
		
		//
		// Start two hosts of three, expect 4/4/0.
		//
		this.countOfChecks = 0;
		this.desiredDistributionDetected = 0;
		this.keepGoing = true;
		this.expectEqualDistribution = true; // only going to start two of the three hosts
		this.ignoreZeroes = true; // third host will be stuck at 0 because it's not started
		this.maxChecks = 20;
		startManagers(2);
		while (this.keepGoing)
		{
			try
			{
				Thread.sleep(15000);
			}
			catch (InterruptedException e)
			{
				TestUtilities.log("Sleep interrupted, emergency bail");
				Thread.currentThread().interrupt();
				throw e;
			}
		}
		assertTrue("Desired distribution 4/4/0 never reached or was not stable", this.desiredDistributionDetected >= this.partitionManagers.length);
		
		//
		// Start up the third host and wait for rebalance
		//
		this.countOfChecks = 0;
		this.desiredDistributionDetected = 0;
		this.keepGoing = true;
		this.expectEqualDistribution = false;
		this.ignoreZeroes = false;
		this.maxChecks = 30;
		startSingleManager(2);
		while (this.keepGoing)
		{
			try
			{
				Thread.sleep(15000);
			}
			catch (InterruptedException e)
			{
				TestUtilities.log("Sleep interrupted, emergency bail");
				Thread.currentThread().interrupt();
				throw e;
			}
		}
		assertTrue("Desired distribution never reached or was not stable", this.desiredDistributionDetected >= this.partitionManagers.length);
		
		//
		// Now stop host 0 and wait for 0/4/4
		//
		this.countOfChecks = 0;
		this.desiredDistributionDetected = 0;
		this.keepGoing = true;
		this.expectEqualDistribution = true; // only two of the three hosts running
		this.ignoreZeroes = true; // first host will be stuck at 0 because it's stopped
		this.maxChecks = 20;
		stopSingleManager(0);
		while (this.keepGoing)
		{
			try
			{
				Thread.sleep(15000);
			}
			catch (InterruptedException e)
			{
				TestUtilities.log("Sleep interrupted, emergency bail");
				Thread.currentThread().interrupt();
				throw e;
			}
		}
		assertTrue("Desired distribution 4/4/0 never reached or was not stable", this.desiredDistributionDetected >= this.partitionManagers.length);

		stopManagers();

		boolean boolret = this.leaseManagers[1].deleteLeaseStore().get();
		assertTrue("failed while cleaning up lease store", boolret);
		boolret = this.checkpointManagers[1].deleteCheckpointStore().get();
		assertTrue("failed while cleaning up checkpoint store", boolret);
		
		TestUtilities.log("DONE");
	}

	@Test
	public void partitionBalancingTooManyHostsTest() throws Exception
	{
		TestUtilities.log("partitionBalancingTooManyHostsTest");
		
		setup(10, 4); // ten hosts, four partitions
		this.countOfChecks = 0;
		this.desiredDistributionDetected = 0;
		this.keepGoing = true;
		this.expectEqualDistribution = false;
		this.ignoreZeroes = false;
		this.maxChecks = 20;
		startManagers();
		
		// Poll until checkPartitionDistribution() declares that it's time to stop.
		while (this.keepGoing)
		{
			try
			{
				Thread.sleep(15000);
			}
			catch (InterruptedException e)
			{
				TestUtilities.log("Sleep interrupted, emergency bail");
				Thread.currentThread().interrupt();
				throw e;
			}
		}
		
		stopManagers();
		
		assertTrue("Desired distribution never reached or was not stable", this.desiredDistributionDetected >= this.partitionManagers.length);

		boolean boolret = this.leaseManagers[0].deleteLeaseStore().get();
		assertTrue("failed while cleaning up lease store", boolret);
		boolret = this.checkpointManagers[0].deleteCheckpointStore().get();
		assertTrue("failed while cleaning up checkpoint store", boolret);
		
		TestUtilities.log("DONE");
	}
	
	synchronized void checkPartitionDistribution(boolean ignoreStopped)
	{
		if (this.shuttingDown)
		{
			return;
		}
		
		TestUtilities.log("Partitions redistributed");
		int[] countsPerHost = new int[this.partitionManagers.length];
		for (int i = 0; i < this.partitionManagers.length; i++)
		{
			this.partitionManagers[i].cleanStolen();
		}
		for (int i = 0; i < this.partitionManagers.length; i++)
		{
			StringBuilder blah = new StringBuilder();
			blah.append("\tHost ");
			blah.append(this.hosts[i].getHostName());
			blah.append(" has ");
			countsPerHost[i] = 0;
			for (String id : this.partitionManagers[i].getOwnedPartitions())
			{
				blah.append(id);
				blah.append(", ");
				countsPerHost[i]++;
			}
			TestUtilities.log(blah.toString());
		}
		
		boolean desired = true;
		int highest = Integer.MIN_VALUE;
		int lowest = Integer.MAX_VALUE;
		for (int i = 0; i < countsPerHost.length; i++)
		{
			if (!this.running[i] && ignoreStopped)
			{
				// Skip
			}
			else
			{
				highest = Integer.max(highest, countsPerHost[i]);
				lowest = Integer.min(lowest, countsPerHost[i]);
			}
		}
		TestUtilities.log("Check " + this.countOfChecks + "  Highest " + highest + "  Lowest " + lowest + "  Descnt " + this.desiredDistributionDetected);
		if (this.expectEqualDistribution)
		{
			// All hosts should have exactly equal counts, so highest == lowest
			desired = (highest == lowest);
		}
		else
		{
			// An equal distribution isn't possible, but the maximum difference between counts should be 1. 
			// Max(counts[]) - Min(counts[]) == 1
			desired = ((highest - lowest) == 1);
		}
		if (desired)
		{
			TestUtilities.log("Evenest distribution detected");
			this.desiredDistributionDetected++;
			if (this.desiredDistributionDetected > this.partitionManagers.length)
			{
				// Every partition manager has looked at the current distribution and
				// it has not changed. The algorithm is stable once it reaches the desired state.
				// No need to keep iterating.
				TestUtilities.log("Desired distribution is stable");
				this.keepGoing = false;
			}
		}
		else
		{
			if ((this.desiredDistributionDetected > 0) && !this.shuttingDown)
			{
				// If we have detected the desired distribution on previous iterations
				// but not on this one, then the algorithm is unstable. Bail and fail.
				TestUtilities.log("Desired distribution was not stable");
				this.keepGoing = false;
			}
		}
	
		this.countOfChecks++;
		if (this.countOfChecks > this.maxChecks)
		{
			// Ran out of iterations without reaching the desired distribution. Bail and fail.
			this.keepGoing = false;
		}
	}
	
	private void setup(int hostCount, int partitionCount)
	{
		this.leaseManagers = new ILeaseManager[hostCount];
		this.checkpointManagers = new ICheckpointManager[hostCount];
		this.hosts = new EventProcessorHost[hostCount];
		this.partitionManagers = new TestPartitionManager[hostCount];
		this.running = new boolean[hostCount];
		
		for (int i = 0; i < hostCount; i++)
		{
			InMemoryLeaseManager lm = new InMemoryLeaseManager(); 
			InMemoryCheckpointManager cm = new InMemoryCheckpointManager();
			
			// In order to test hosts competing for partitions, each host must have a unique name, but they must share the
			// target eventhub/consumer group.
			this.hosts[i] = new EventProcessorHost("dummyHost" + String.valueOf(i), "NOTREAL", EventHubClient.DEFAULT_CONSUMER_GROUP_NAME,
					TestUtilities.syntacticallyCorrectDummyConnectionString, cm, lm);

			lm.initialize(this.hosts[i]);
			this.leaseManagers[i] = lm;
			cm.initialize(this.hosts[i]);
			this.checkpointManagers[i] = cm;
			this.running[i] = false;
			
			this.partitionManagers[i] = new TestPartitionManager(this.hosts[i], partitionCount);
			this.hosts[i].setPartitionManager(this.partitionManagers[i]);
		}
	}
	
	private void startManagers() throws Exception
	{
		startManagers(this.partitionManagers.length);
	}
	
	private void startManagers(int maxIndex) throws Exception
	{
		this.shuttingDown = false;
		for (int i = 0; i < maxIndex; i++)
		{
			startSingleManager(i);
		}
	}
	
	private void startSingleManager(int index) throws Exception
	{
		try
		{
			EventProcessorHost.getExecutorService().submit(() -> this.partitionManagers[index].initialize()).get();
			this.running[index] = true;
		}
		catch (Exception e)
		{
			TestUtilities.log("TASK START FAILED " + e.toString() + " " + e.getMessage());
			throw e;
		}
	}
	
	private void stopManagers() throws InterruptedException, ExecutionException
	{
		TestUtilities.log("SHUTTING DOWN");
		this.shuttingDown = true;
		for (int i = 0; i < this.partitionManagers.length; i++)
		{
			if (this.running[i])
			{
				this.partitionManagers[i].stopPartitions().get();
				TestUtilities.log("Host " + i + " stopped");
			}
		}
	}
	
	private void stopSingleManager(int index) throws InterruptedException, ExecutionException
	{
		if (this.running[index])
		{
			this.partitionManagers[index].stopPartitions().get();
			TestUtilities.log("Host " + index + " stopped");
			this.running[index] = false;
		}
	}
	
	private class TestPartitionManager extends PartitionManager
	{
		private int partitionCount;
		
		TestPartitionManager(EventProcessorHost host, int partitionCount)
		{
			super(host);
			this.partitionCount = partitionCount;
		}
		
		Iterable<String> getOwnedPartitions()
		{
			Iterable<String> retval = null;
			if (this.pump != null)
			{
				retval = ((DummyPump)this.pump).getPumpsList();
			}
			else
			{
				// If the manager isn't started, return an empty list.
				retval = new ArrayList<String>();
			}
			return retval;
		}
		
		void cleanStolen()
		{
			// Skip cleanup if the manager isn't started.
			if (this.pump != null)
			{
				((DummyPump)this.pump).fastCleanup(); // fast cleanup of stolen partitions
			}
		}

		@Override
	    Iterable<String> getPartitionIds()
	    {
			ArrayList<String> ids = new ArrayList<String>();
			for (int i = 0; i < this.partitionCount; i++)
			{
				ids.add(String.valueOf(i));
			}
			return ids;
	    }
	    
		@Override
	    Pump createPumpTestHook()
	    {
			return new DummyPump(this.host);
	    }
		
		@Override
		void onInitializeCompleteTestHook()
		{
			TestUtilities.log("PartitionManager for host " + this.host.getHostName() + " initialized stores OK");
		}
		
		@Override
		void onPartitionCheckCompleteTestHook()
		{
			PartitionManagerTest.this.checkPartitionDistribution(PartitionManagerTest.this.ignoreZeroes);
		}
	}
}
