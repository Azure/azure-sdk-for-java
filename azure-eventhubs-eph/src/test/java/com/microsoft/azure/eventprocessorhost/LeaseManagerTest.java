/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

import java.util.UUID;

import org.junit.Test;

import com.microsoft.azure.eventhubs.EventHubClient;

import static org.junit.Assert.*;

public class LeaseManagerTest
{
	private String azureStorageConnectionString = TestUtilities.getStorageConnectionString();
	
	private ILeaseManager[] leaseManagers;
	private EventProcessorHost[] hosts;
	
	@Test
	public void singleManangerInMemoryLeaseSmokeTest() throws Exception
	{
		singleManagerLeaseSmokeTest(false, 8);
	}
	
	@Test
	public void singleManagerAzureLeaseSmokeTest() throws Exception
	{
		RealEventHubUtilities rUtils = new RealEventHubUtilities();
		singleManagerLeaseSmokeTest(true, rUtils.getPartitionIdsForTest().size());
	}
	
	@Test
	public void twoManagerInMemoryLeaseStealingTest() throws Exception
	{
		twoManagerLeaseStealingTest(false);
	}
	
	@Test
	public void twoManangerAzureLeaseStealingTest() throws Exception
	{
		twoManagerLeaseStealingTest(true);
	}
	
	void singleManagerLeaseSmokeTest(boolean useAzureStorage, int partitionCount) throws Exception
	{
		this.leaseManagers = new ILeaseManager[1];
		this.hosts = new EventProcessorHost[1];
		setupOneManager(useAzureStorage, 0, "0", generateContainerName("0"));
		
		TestUtilities.log("singleManagerLeaseSmoke");
		TestUtilities.log("USING " + (useAzureStorage ? "AzureStorageCheckpointLeaseManager" : "InMemoryLeaseManager"));

		TestUtilities.log("Check whether lease store exists before create");
		Boolean boolret = this.leaseManagers[0].leaseStoreExists().get();
		assertFalse("lease store should not exist yet", boolret);
		
		TestUtilities.log("Creating lease store");
		boolret = this.leaseManagers[0].createLeaseStoreIfNotExists().get();
		assertTrue("creating lease store returned false", boolret);

		TestUtilities.log("Checking whether lease store exists after create");
		boolret = this.leaseManagers[0].leaseStoreExists().get();
		assertTrue("lease store should exist but does not", boolret);
		
		Lease[] leases = new Lease[partitionCount];
		TestUtilities.log("Creating leases for all partitions");
		for (int i = 0; i < partitionCount; i++)
		{
			Lease createdLease = this.leaseManagers[0].createLeaseIfNotExists(String.valueOf(i)).get();
			leases[i] = createdLease;
			assertNotNull("failed creating lease for " + i, createdLease);
		}
	
		TestUtilities.log("Acquiring leases for all partitions");
		for (int i = 0; i < partitionCount; i++)
		{
			TestUtilities.logConditional(useAzureStorage, "Partition " + i + " state before acquire: " + leases[i].getStateDebug());
			boolret = this.leaseManagers[0].acquireLease(leases[i]).get();
			assertTrue("failed to acquire lease for " + i, boolret);
			TestUtilities.logConditional(useAzureStorage, "Partition " + i + " state after acquire: " + leases[i].getStateDebug());
		}
	
		Thread.sleep(5000);

		TestUtilities.log("Renewing leases for all partitions");
		for (int i = 0; i < partitionCount; i++)
		{
			TestUtilities.logConditional(useAzureStorage, "Partition " + i + " state before: " + leases[i].getStateDebug());
			boolret = this.leaseManagers[0].renewLease(leases[i]).get();
			assertTrue("failed to renew lease for " + i, boolret);
			TestUtilities.logConditional(useAzureStorage, "Partition " + i + " state after: " + leases[i].getStateDebug());
		}
		
		int x = 1;
		while (!leases[0].isExpired())
		{
			Thread.sleep(5000);
			TestUtilities.log("Still waiting for lease on 0 to expire: " + (5 * x));
			assertFalse("lease 0 expiration is overdue", (5000 * x) > (this.leaseManagers[0].getLeaseDurationInMilliseconds() + 10000));
			for (int i = 1; i < partitionCount; i++)
			{
				boolret = this.leaseManagers[0].renewLease(leases[i]).get();
				assertTrue("failed to renew lease for " + i, boolret);
			}
			x++;
		}

		TestUtilities.log("Updating lease 1");
		leases[1].setEpoch(5);
		if (!useAzureStorage)
		{
			// AzureStorageCheckpointLeaseManager uses the token to manage Storage leases, only test when using InMemory
			leases[1].setToken("it's a cloudy day");
		}
		boolret = this.leaseManagers[0].updateLease(leases[1]).get();
		assertTrue("failed to update lease for 1", boolret);
		Lease retrievedLease = this.leaseManagers[0].getLease("1").get();
		assertNotNull("failed to get lease for 1", retrievedLease);
		assertEquals("epoch was not persisted, expected " + leases[1].getEpoch() + " got " + retrievedLease.getEpoch(), leases[1].getEpoch(), retrievedLease.getEpoch());
		if (!useAzureStorage)
		{
			assertEquals("token was not persisted, expected [" + leases[1].getToken() + "] got [" + retrievedLease.getToken() + "]", leases[1].getToken(), retrievedLease.getToken());
		}
		
		// Release for 0 is expected to fail because it has expired
		TestUtilities.log("Trying to release expired lease 0");
		boolret = this.leaseManagers[0].releaseLease(leases[0]).get();
		assertFalse("release lease on 0 succeeded unexpectedly", boolret);
		
		// Renew for 0 succeeds even though it has expired.
		// This is the behavior of AzureStorageCheckpointLeaseManager, which is dictated by the behavior of Azure Storage leases.
		TestUtilities.log("Renewing expired lease 0");
		boolret = this.leaseManagers[0].renewLease(leases[0]).get();
		assertTrue("renew lease on 0 failed unexpectedly", boolret);

		TestUtilities.log("Releasing leases for all partitions");
		for (int i = 0; i < partitionCount; i++)
		{
			TestUtilities.logConditional(useAzureStorage, "Partition " + i + " state before: " + leases[i].getStateDebug());
			boolret = this.leaseManagers[0].releaseLease(leases[i]).get();
			assertTrue("failed to release lease for " + i, boolret);
			TestUtilities.logConditional(useAzureStorage, "Partition " + i + " state after: " + leases[i].getStateDebug());
		}
		
		TestUtilities.log("Trying to acquire released lease 0");
		boolret = this.leaseManagers[0].acquireLease(leases[0]).get();
		assertTrue("failed to acquire previously released 0", boolret);

		TestUtilities.log("Trying to double release lease 0");
		boolret = this.leaseManagers[0].releaseLease(leases[0]).get();
		assertTrue("failed to release lease for 0", boolret);
		
		TestUtilities.log("Cleaning up lease store");
		boolret = this.leaseManagers[0].deleteLeaseStore().get();
		assertTrue("failed while cleaning up store", boolret);
		
		TestUtilities.log("singleManagerLeaseSmokeTest DONE");
	}

	
	void twoManagerLeaseStealingTest(boolean useAzureStorage) throws Exception
	{
		this.leaseManagers = new ILeaseManager[2];
		this.hosts = new EventProcessorHost[2];
		String containerName = generateContainerName(null);
		setupOneManager(useAzureStorage, 0, "StealTest", containerName);
		setupOneManager(useAzureStorage, 1, "StealTest", containerName);
		
		TestUtilities.log("twoManagerLeaseStealing");
		TestUtilities.log("USING " + (useAzureStorage ? "AzureStorageCheckpointLeaseManager" : "InMemoryLeaseManager"));

		TestUtilities.log("Check whether lease store exists before create");
		Boolean boolret = this.leaseManagers[0].leaseStoreExists().get();
		assertFalse("lease store should not exist yet", boolret);
		
		TestUtilities.log("Creating lease store");
		boolret = this.leaseManagers[0].createLeaseStoreIfNotExists().get();
		assertTrue("creating lease store returned false", boolret);

		TestUtilities.log("Check whether lease store exists after create");
		boolret = this.leaseManagers[0].leaseStoreExists().get();
		assertTrue("lease store should exist but does not", boolret);
		
		TestUtilities.log("Check whether second manager can see lease store");
		boolret = this.leaseManagers[1].leaseStoreExists().get();
		assertTrue("second manager cannot see lease store", boolret);

		TestUtilities.log("Creating lease for partition 0");
		Lease mgr1Lease = this.leaseManagers[0].createLeaseIfNotExists("0").get();
		assertNotNull("first manager failed creating lease for 0", mgr1Lease);
		
		TestUtilities.log("Checking whether second manager can see lease 0");
		Lease mgr2Lease = this.leaseManagers[1].getLease("0").get();
		assertNotNull("second manager cannot see lease for 0", mgr2Lease);

		TestUtilities.log("First manager acquiring lease 0");
		boolret = this.leaseManagers[0].acquireLease(mgr1Lease).get();
		assertTrue("first manager failed acquiring lease for 0", boolret);
		TestUtilities.logConditional(useAzureStorage, "Lease token is " + mgr1Lease.getToken());
		
		int x = 0;
		while (!mgr1Lease.isExpired())
		{
			assertFalse("lease 0 expiration is overdue", (5000 * x) > (this.leaseManagers[0].getLeaseDurationInMilliseconds() + 10000));
			Thread.sleep(5000);
			TestUtilities.log("Still waiting for lease on 0 to expire: " + (5 * ++x));
		}

		TestUtilities.log("Second manager acquiring lease 0");
		boolret = this.leaseManagers[1].acquireLease(mgr2Lease).get();
		assertTrue("second manager failed acquiring expired lease for 0", boolret);
		TestUtilities.logConditional(useAzureStorage, "Lease token is " + mgr2Lease.getToken());

		TestUtilities.log("First manager trying to renew lease 0");
		boolret = this.leaseManagers[0].renewLease(mgr1Lease).get();
		assertFalse("first manager unexpected success renewing lease for 0", boolret);
		
		TestUtilities.log("First manager getting lease 0");
		mgr1Lease = this.leaseManagers[0].getLease(mgr1Lease.getPartitionId()).get();
		assertNotNull("first manager cannot see lease for 0", mgr1Lease);
		
		TestUtilities.log("First manager stealing lease 0");
		boolret = this.leaseManagers[0].acquireLease(mgr1Lease).get();
		assertTrue("first manager failed stealing lease 0", boolret);
		TestUtilities.logConditional(useAzureStorage, "Lease token is " + mgr1Lease.getToken());
		
		TestUtilities.log("Second mananger getting lease 0");
		mgr2Lease = this.leaseManagers[1].getLease("0").get();
		assertNotNull("second manager cannot see lease for 0", mgr2Lease);

		TestUtilities.log("Second mananger stealing lease 0");
		boolret = this.leaseManagers[1].acquireLease(mgr2Lease).get();
		assertTrue("second manager failed stealing lease 0", boolret);
		TestUtilities.logConditional(useAzureStorage, "Lease token is " + mgr2Lease.getToken());
		
		TestUtilities.log("Second mananger releasing lease 0");
		boolret = this.leaseManagers[1].releaseLease(mgr2Lease).get();
		assertTrue("second manager failed to release lease 0", boolret);

		TestUtilities.log("First mananger tyring to release lease 0");
		boolret = this.leaseManagers[0].releaseLease(mgr1Lease).get();
		assertFalse("first manager unexpected success releasing lease 0", boolret);
		
		TestUtilities.log("Cleaning up lease store");
		boolret = this.leaseManagers[1].deleteLeaseStore().get();
		assertTrue("failed while cleaning up store", boolret);
		
		TestUtilities.log("twoManagerLeaseStealingTest DONE");
	}
	
	private String generateContainerName(String infix)
	{
		StringBuilder containerName = new StringBuilder(64);
		containerName.append("leasemgrtest-");
		if (infix != null)
		{
			containerName.append(infix);
			containerName.append('-');
		}
		containerName.append(UUID.randomUUID().toString());
		return containerName.toString();
	}
	
	private void setupOneManager(boolean useAzureStorage, int index, String suffix, String containerName) throws Exception
	{
		ILeaseManager leaseMgr = null;
		ICheckpointManager checkpointMgr = null;
		
		if (!useAzureStorage)
		{
			leaseMgr = new InMemoryLeaseManager();
			checkpointMgr = new InMemoryCheckpointManager();
		}
		else
		{
			TestUtilities.log("Container name: " + containerName);
			AzureStorageCheckpointLeaseManager azMgr = new AzureStorageCheckpointLeaseManager(this.azureStorageConnectionString, containerName);
			leaseMgr = azMgr;
			checkpointMgr = azMgr;
		}
		
		// Host name needs to be unique per host so use index. Event hub should be the same for all hosts in a test, so use the supplied suffix.
    	EventProcessorHost host = new EventProcessorHost("dummyHost" + String.valueOf(index), "NOTREAL" + suffix,
    			EventHubClient.DEFAULT_CONSUMER_GROUP_NAME, TestUtilities.syntacticallyCorrectDummyConnectionString + suffix, checkpointMgr, leaseMgr);
    	
    	try
    	{
    		if (!useAzureStorage)
    		{
    			((InMemoryLeaseManager)leaseMgr).initialize(host);
    			((InMemoryCheckpointManager)checkpointMgr).initialize(host);
    		}
    		else
    		{
    			((AzureStorageCheckpointLeaseManager)leaseMgr).initialize(host);
    		}
		}
    	catch (Exception e)
    	{
    		TestUtilities.log("Manager initializion failed");
    		throw e;
		}
		
    	this.leaseManagers[index] = leaseMgr;
    	this.hosts[index] = host;
	}
}
