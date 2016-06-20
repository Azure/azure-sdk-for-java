/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

import java.util.UUID;

import org.junit.Test;
import static org.junit.Assert.*;

public class CheckpointManagerTest
{
	//
	// Setup variables.
	//
	private boolean useAzureStorage = true; // false tests InMemoryCheckpointManager, true tests AzureStorageCheckpointLeaseManager
	private String azureStorageConnectionString = TestUtilities.getStorageConnectionString(); // EPHTESTSTORAGE environment var must be set to valid connection string to test AzureStorageCheckpointLeaseManager
	private int partitionCount = 64;
	
	private ILeaseManager[] leaseManagers;
	private ICheckpointManager[] checkpointManagers;
	private EventProcessorHost[] hosts;
	
	@Test
	public void singleManagerCheckpointSmokeTest() throws Exception
	{
		this.leaseManagers = new ILeaseManager[1];
		this.checkpointManagers = new ICheckpointManager[1];
		this.hosts = new EventProcessorHost[1];
		setupOneManager(0, "0", generateContainerName("0"));
		
		System.out.println("singleManagerCheckpointSmokeTest");
		System.out.println("USING " + (useAzureStorage ? "AzureStorageCheckpointLeaseManager" : "InMemoryCheckpointManager"));

		boolean boolret = this.checkpointManagers[0].checkpointStoreExists().get();
		assertFalse("checkpoint store should not exist yet", boolret);
		
		boolret = this.checkpointManagers[0].createCheckpointStoreIfNotExists().get();
		assertTrue("creating checkpoint store returned false", boolret);

		boolret = this.checkpointManagers[0].checkpointStoreExists().get();
		assertTrue("checkpoint store should exist but does not", boolret);
		
		Checkpoint[] checkpoints = new Checkpoint[this.partitionCount];
		for (int i = 0; i < this.partitionCount; i++)
		{
			Checkpoint createdCheckpoint = this.checkpointManagers[0].createCheckpointIfNotExists(String.valueOf(i)).get();
			checkpoints[i] = createdCheckpoint;
			assertNotNull("failed creating checkpoint for " + i, createdCheckpoint);
		}

		for (int i = 0; i < this.partitionCount; i++)
		{
			Checkpoint blah = this.checkpointManagers[0].getCheckpoint(String.valueOf(i)).get();
			assertNotNull("failed to retrieve checkpoint for " + i, blah);
		}
		
		// AzureStorageCheckpointLeaseManager tries to pretend that checkpoints and leases are separate, but they really aren't.
		// Because the checkpoint data is stored in the lease, updating the checkpoint means updating the lease, and it is
		// necessary to hold the lease in order to update it.
		if (this.useAzureStorage)
		{
			for (int i = 0; i < this.partitionCount; i++)
			{
				Lease l = this.leaseManagers[0].getLease(String.valueOf(i)).get();
				assertNotNull("failed to retrieve lease for " + i, l);
				boolret = this.leaseManagers[0].acquireLease(l).get();
				assertTrue("failed to acquire lease for " + i, boolret);
			}
		}
		
		for (int i = 0; i < this.partitionCount; i++)
		{
			// Arbitrary values, just checking that they are persisted
			checkpoints[i].setOffset(String.valueOf(i * 234));
			checkpoints[i].setSequenceNumber(i + 77);
			this.checkpointManagers[0].updateCheckpoint(checkpoints[i]).get();
		}
		for (int i = 0; i < this.partitionCount; i++)
		{
			Checkpoint blah = this.checkpointManagers[0].getCheckpoint(String.valueOf(i)).get();
			assertNotNull("failed to retrieve checkpoint for " + i, blah);
			assertEquals("retrieved offset does not match written offset", blah.getOffset(), checkpoints[i].getOffset());
			assertEquals("retrieved seqno does not match written seqno", blah.getSequenceNumber(), checkpoints[i].getSequenceNumber());
		}

		// Have to release the leases before we can delete the store.
		if (this.useAzureStorage)
		{
			for (int i = 0; i < this.partitionCount; i++)
			{
				Lease l = this.leaseManagers[0].getLease(String.valueOf(i)).get();
				assertNotNull("failed to retrieve lease for " + i, l);
				boolret = this.leaseManagers[0].releaseLease(l).get();
				assertTrue("failed to acquire lease for " + i, boolret);
			}
		}
		
		boolret = this.checkpointManagers[0].deleteCheckpointStore().get();
		assertTrue("failed while cleaning up store", boolret);
		
		System.out.println("singleManagerCheckpointSmokeTest DONE");
	}
	
	@Test
	public void twoManagerCheckpointSmokeTest() throws Exception
	{
		this.leaseManagers = new ILeaseManager[2];
		this.checkpointManagers = new ICheckpointManager[2];
		this.hosts = new EventProcessorHost[2];
		String containerName = generateContainerName(null);
		setupOneManager(0, "twoCheckpoint", containerName);
		setupOneManager(1, "twoCheckpoint", containerName);
		
		System.out.println("twoManagerCheckpointSmokeTest");
		System.out.println("USING " + (useAzureStorage ? "AzureStorageCheckpointLeaseManager" : "InMemoryLeaseManager"));
		
		boolean boolret = this.checkpointManagers[0].checkpointStoreExists().get();
		assertFalse("checkpoint store should not exist yet", boolret);
		
		boolret = this.checkpointManagers[1].createCheckpointStoreIfNotExists().get();
		assertTrue("creating checkpoint store returned false", boolret);

		boolret = this.checkpointManagers[0].checkpointStoreExists().get();
		assertTrue("checkpoint store should exist but does not", boolret);
		
		Checkpoint[] checkpoints = new Checkpoint[this.partitionCount];
		for (int i = 0; i < this.partitionCount; i++)
		{
			Checkpoint createdCheckpoint = this.checkpointManagers[i % 2].createCheckpointIfNotExists(String.valueOf(i)).get();
			checkpoints[i] = createdCheckpoint;
			assertNotNull("failed creating checkpoint for " + i, createdCheckpoint);
		}
		
		for (int i = 0; i < this.partitionCount; i++)
		{
			Checkpoint blah = this.checkpointManagers[(i + 1) % 2].getCheckpoint(String.valueOf(i)).get();
			assertNotNull("failed to retrieve checkpoint for " + i, blah);
		}
		
		// AzureStorageCheckpointLeaseManager tries to pretend that checkpoints and leases are separate, but they really aren't.
		// Because the checkpoint data is stored in the lease, updating the checkpoint means updating the lease, and it is
		// necessary to hold the lease in order to update it.
		if (this.useAzureStorage)
		{
			for (int i = 0; i < this.partitionCount; i++)
			{
				Lease l = this.leaseManagers[1].getLease(String.valueOf(i)).get();
				assertNotNull("failed to retrieve lease for " + i, l);
				boolret = this.leaseManagers[1].acquireLease(l).get();
				assertTrue("failed to acquire lease for " + i, boolret);
			}
		}
		
		for (int i = 0; i < this.partitionCount; i++)
		{
			// Arbitrary values, just checking that they are persisted
			checkpoints[i].setOffset(String.valueOf(i * 234));
			checkpoints[i].setSequenceNumber(i + 77);
			this.checkpointManagers[1].updateCheckpoint(checkpoints[i]).get();
		}
		for (int i = 0; i < this.partitionCount; i++)
		{
			Checkpoint blah = this.checkpointManagers[0].getCheckpoint(String.valueOf(i)).get();
			assertNotNull("failed to retrieve checkpoint for " + i, blah);
			assertEquals("retrieved offset does not match written offset", blah.getOffset(), checkpoints[i].getOffset());
			assertEquals("retrieved seqno does not match written seqno", blah.getSequenceNumber(), checkpoints[i].getSequenceNumber());
		}

		// Have to release the leases before we can delete the store.
		if (this.useAzureStorage)
		{
			for (int i = 0; i < this.partitionCount; i++)
			{
				Lease l = this.leaseManagers[1].getLease(String.valueOf(i)).get();
				assertNotNull("failed to retrieve lease for " + i, l);
				boolret = this.leaseManagers[1].releaseLease(l).get();
				assertTrue("failed to acquire lease for " + i, boolret);
			}
		}
		
		boolret = this.checkpointManagers[0].deleteCheckpointStore().get();
		assertTrue("failed while cleaning up store", boolret);
		
		System.out.println("twoManagerCheckpointSmokeTest DONE");
	}

	
	// Created while trying to find a possible handle leak when checkpointing.
	// Not for general testing use.
	//@Test
	public void checkpointBrutalityTest() throws Exception
	{
		final int checkpointers = 4;
		this.leaseManagers = new ILeaseManager[checkpointers];
		this.checkpointManagers = new ICheckpointManager[checkpointers];
		this.hosts = new EventProcessorHost[checkpointers];
		String containerName = generateContainerName(null);
		for (int i = 0; i < checkpointers; i++)
		{
			setupOneManager(i, "brutality", containerName);
		}
		
		System.out.println("checkpointBrutalityTest");
		System.out.println("USING " + (useAzureStorage ? "AzureStorageCheckpointLeaseManager" : "InMemoryLeaseManager"));
		
		boolean boolret = this.checkpointManagers[0].createCheckpointStoreIfNotExists().get();
		assertTrue("creating checkpoint store returned false", boolret);

		boolret = this.checkpointManagers[0].checkpointStoreExists().get();
		assertTrue("checkpoint store should exist but does not", boolret);
		
		Checkpoint[] checkpoints = new Checkpoint[this.partitionCount];
		for (int i = 0; i < this.partitionCount; i++)
		{
			Checkpoint createdCheckpoint = this.checkpointManagers[0].createCheckpointIfNotExists(String.valueOf(i)).get();
			checkpoints[i] = createdCheckpoint;
			assertNotNull("failed creating checkpoint for " + i, createdCheckpoint);
		}
		
		for (int i = 0; i < this.partitionCount; i++)
		{
			Checkpoint blah = this.checkpointManagers[0].getCheckpoint(String.valueOf(i)).get();
			assertNotNull("failed to retrieve checkpoint for " + i, blah);
		}
		
		// AzureStorageCheckpointLeaseManager tries to pretend that checkpoints and leases are separate, but they really aren't.
		// Because the checkpoint data is stored in the lease, updating the checkpoint means updating the lease, and it is
		// necessary to hold the lease in order to update it. Updating it renews the lease, so we don't have to worry about that.
		if (this.useAzureStorage)
		{
			for (int i = 0; i < this.partitionCount; i++)
			{
				Lease l = this.leaseManagers[i % checkpointers].getLease(String.valueOf(i)).get();
				assertNotNull("failed to retrieve lease for " + i, l);
				boolret = this.leaseManagers[i % checkpointers].acquireLease(l).get();
				assertTrue("failed to acquire lease for " + i, boolret);
			}
		}
		
		PartitionContext[] contexts = new PartitionContext[this.partitionCount];
		for (int i = 0; i < this.partitionCount; i++)
		{
			contexts[i] = new PartitionContext(this.hosts[i % checkpointers], String.valueOf(i), "not used", "not used");
		}
		
		for (int i = 0; i < checkpointers; i++)
		{
			final int iter = i; // allows handing current value of i to lambda
			EventProcessorHost.getExecutorService().submit(() -> 
			{
				while (true)
				{
					for (int j = iter; j < this.partitionCount; j += checkpointers)
					{
						long newSeq = checkpoints[j].getSequenceNumber() + 1;
						checkpoints[j].setOffset(String.valueOf(newSeq));
						checkpoints[j].setSequenceNumber(newSeq);
						
						// Pick one: either update the checkpoints directly through the checkpoint manager, or
						// do the read-modify-write cycle via PartitionContext.
						
						//this.checkpointManagers[iter].updateCheckpoint(checkpoints[j]);
						contexts[j].setOffsetAndSequenceNumber(checkpoints[j].getOffset(), newSeq); contexts[j].checkpoint();
						
						incrementPerSecondCount();
						if ((newSeq % 100) == 0)
						{
							System.out.println("Iter " + iter + " part " + j + " seq " + newSeq);
						}
						Thread.sleep(5);
					}
				}
			});
		}
		EventProcessorHost.getExecutorService().submit(() ->
		{
			while (true)
			{
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
				System.out.println("******************* total per second ish " + returnAndClearPerSecondCount());
			}
		}).get(); // this get will never return

		/* UNREACHABLE 
		if (this.useAzureStorage)
		{
			for (int i = 0; i < this.partitionCount; i++)
			{
				Lease l = this.leaseManagers[0].getLease(String.valueOf(i)).get();
				assertNotNull("failed to retrieve lease for " + i, l);
				boolret = this.leaseManagers[0].releaseLease(l).get();
				assertTrue("failed to acquire lease for " + i, boolret);
			}
		}
		
		boolret = this.checkpointManagers[0].deleteCheckpointStore().get();
		assertTrue("failed while cleaning up store", boolret);
		
		System.out.println("checkpointBrutalityTest DONE");
		*/
	}
	
	int perSecondCount = 0;
	
	synchronized void incrementPerSecondCount()
	{
		this.perSecondCount++;
	}
	
	synchronized int returnAndClearPerSecondCount()
	{
		int retval = this.perSecondCount;
		this.perSecondCount = 0;
		return retval;
	}
	
	private String generateContainerName(String infix)
	{
		StringBuilder containerName = new StringBuilder(64);
		containerName.append("ckptmgrtest-");
		if (infix != null)
		{
			containerName.append(infix);
			containerName.append('-');
		}
		containerName.append(UUID.randomUUID().toString());
		return containerName.toString();
	}
	
	private void setupOneManager(int index, String suffix, String containerName) throws Exception
	{
		ILeaseManager leaseMgr = null;
		ICheckpointManager checkpointMgr = null;
		
		if (!this.useAzureStorage)
		{
			leaseMgr = new InMemoryLeaseManager();
			checkpointMgr = new InMemoryCheckpointManager();
		}
		else
		{
			System.out.println("Container name: " + containerName);
			AzureStorageCheckpointLeaseManager azMgr = new AzureStorageCheckpointLeaseManager(this.azureStorageConnectionString, containerName);
			leaseMgr = azMgr;
			checkpointMgr = azMgr;
		}
		
		// Host name needs to be unique per host so use index. Event hub and consumer group should be the same for all hosts in a test, so
		// use the supplied suffix.
    	EventProcessorHost host = new EventProcessorHost("dummyHost" + String.valueOf(index), "dummyEventHub" + suffix,
				"dummyConsumerGroup" + suffix, "dummyEventHubConnectionString", checkpointMgr, leaseMgr);
    	
    	try
    	{
    		if (!this.useAzureStorage)
    		{
    			((InMemoryLeaseManager)leaseMgr).initialize(host);
    			((InMemoryCheckpointManager)checkpointMgr).initialize(host);
    		}
    		else
    		{
    			((AzureStorageCheckpointLeaseManager)checkpointMgr).initialize(host);
    		}
		}
    	catch (Exception e)
    	{
    		System.out.println("Manager initializion failed");
    		throw e;
		}
		
    	this.leaseManagers[index] = leaseMgr;
    	this.checkpointManagers[index] = checkpointMgr;
    	this.hosts[index] = host;
	}
}
