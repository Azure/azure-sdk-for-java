// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventprocessorhost;

import com.microsoft.azure.eventhubs.EventHubClient;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class LeaseManagerTest extends TestBase {
    private ILeaseManager[] leaseManagers;
    private EventProcessorHost[] hosts;

    @Test
    public void singleManangerInMemoryLeaseSmokeTest() throws Exception {
        singleManagerLeaseSmokeTest(false, 8);
    }

    @Test
    public void singleManagerAzureLeaseSmokeTest() throws Exception {
        singleManagerLeaseSmokeTest(true, 8);
    }

    @Test
    public void twoManagerInMemoryLeaseStealingTest() throws Exception {
        twoManagerLeaseStealingTest(false);
    }

    @Test
    public void twoManangerAzureLeaseStealingTest() throws Exception {
        twoManagerLeaseStealingTest(true);
    }

    void singleManagerLeaseSmokeTest(boolean useAzureStorage, int partitionCount) throws Exception {
        this.leaseManagers = new ILeaseManager[1];
        this.hosts = new EventProcessorHost[1];
        setupOneManager(useAzureStorage, 0, "0", generateContainerName("0"));

        TestBase.logInfo("Check whether lease store exists before create");
        Boolean boolret = this.leaseManagers[0].leaseStoreExists().get();
        assertFalse("lease store should not exist yet", boolret);

        TestBase.logInfo("Creating lease store");
        this.leaseManagers[0].createLeaseStoreIfNotExists().get();

        TestBase.logInfo("Checking whether lease store exists after create");
        boolret = this.leaseManagers[0].leaseStoreExists().get();
        assertTrue("lease store should exist but does not", boolret);

        ArrayList<String> partitionIds = new ArrayList<String>();
        for (int i = 0; i < partitionCount; i++) {
            partitionIds.add(String.valueOf(i));
        }
        TestBase.logInfo("Creating leases for all partitions");
        this.leaseManagers[0].createAllLeasesIfNotExists(partitionIds).get(); // throws on failure

        CompleteLease[] leases = new CompleteLease[partitionCount];
        TestBase.logInfo("Getting leases for all partitions");
        for (int i = 0; i < partitionIds.size(); i++) {
            leases[i] = this.leaseManagers[0].getLease(partitionIds.get(i)).get();
            assertNotNull("getLease returned null", leases[i]);
        }

        TestBase.logInfo("Acquiring leases for all partitions");
        for (int i = 0; i < partitionCount; i++) {
            if (useAzureStorage) {
                TestBase.logInfo("Partition " + i + " state before: " + leases[i].getStateDebug());
            }
            boolret = this.leaseManagers[0].acquireLease(leases[i]).get();
            assertTrue("failed to acquire lease for " + i, boolret);
            if (useAzureStorage) {
                TestBase.logInfo("Partition " + i + " state after: " + leases[i].getStateDebug());
            }
        }

        Thread.sleep(5000);

        TestBase.logInfo("Getting state for all leases");
        List<BaseLease> states = this.leaseManagers[0].getAllLeases().get(); // throws on failure
        for (BaseLease s : states) {
            TestBase.logInfo("Partition " + s.getPartitionId() + " owned by " + s.getOwner() + " isowned: " + s.getIsOwned());
        }

        TestBase.logInfo("Renewing leases for all partitions");
        for (int i = 0; i < partitionCount; i++) {
            if (useAzureStorage) {
                TestBase.logInfo("Partition " + i + " state before: " + leases[i].getStateDebug());
            }
            boolret = this.leaseManagers[0].renewLease(leases[i]).get();
            assertTrue("failed to renew lease for " + i, boolret);
            if (useAzureStorage) {
                TestBase.logInfo("Partition " + i + " state after: " + leases[i].getStateDebug());
            }
        }

        int x = 1;
        while (getOneState(leases[0].getPartitionId(), this.leaseManagers[0]).getIsOwned()) {
            Thread.sleep(5000);
            TestBase.logInfo("Still waiting for lease on 0 to expire: " + (5 * x));
            assertFalse("lease 0 expiration is overdue", (5000 * x) > (this.leaseManagers[0].getLeaseDurationInMilliseconds() + 10000));
            for (int i = 1; i < partitionCount; i++) {
                boolret = this.leaseManagers[0].renewLease(leases[i]).get();
                assertTrue("failed to renew lease for " + i, boolret);
            }
            x++;
        }

        TestBase.logInfo("Updating lease 1");
        leases[1].setEpoch(5);
        boolret = this.leaseManagers[0].updateLease(leases[1]).get();
        assertTrue("failed to update lease for 1", boolret);
        CompleteLease retrievedLease = this.leaseManagers[0].getLease("1").get();
        assertNotNull("failed to get lease for 1", retrievedLease);
        assertEquals("epoch was not persisted, expected " + leases[1].getEpoch() + " got " + retrievedLease.getEpoch(), leases[1].getEpoch(), retrievedLease.getEpoch());

        // Release for 0 should not throw even though lease has expired -- it just won't do anything
        TestBase.logInfo("Trying to release expired lease 0");
        this.leaseManagers[0].releaseLease(leases[0]).get();

        // Renew for 0 succeeds even though it has expired.
        // This is the behavior of AzureStorageCheckpointLeaseManager, which is dictated by the behavior of Azure Storage leases.
        TestBase.logInfo("Renewing expired lease 0");
        boolret = this.leaseManagers[0].renewLease(leases[0]).get();
        assertTrue("renew lease on 0 failed unexpectedly", boolret);

        TestBase.logInfo("Releasing leases for all partitions");
        for (int i = 0; i < partitionCount; i++) {
            if (useAzureStorage) {
                TestBase.logInfo("Partition " + i + " state before: " + leases[i].getStateDebug());
            }
            this.leaseManagers[0].releaseLease(leases[i]).get();
            if (useAzureStorage) {
                TestBase.logInfo("Partition " + i + " state after: " + leases[i].getStateDebug());
            }
        }

        TestBase.logInfo("Trying to acquire released lease 0");
        boolret = this.leaseManagers[0].acquireLease(leases[0]).get();
        assertTrue("failed to acquire previously released 0", boolret);

        TestBase.logInfo("Trying to release lease 0");
        this.leaseManagers[0].releaseLease(leases[0]).get();

        TestBase.logInfo("Cleaning up lease store");
        this.leaseManagers[0].deleteLeaseStore().get();
    }


    void twoManagerLeaseStealingTest(boolean useAzureStorage) throws Exception {
        this.leaseManagers = new ILeaseManager[2];
        this.hosts = new EventProcessorHost[2];
        String containerName = generateContainerName(null);
        setupOneManager(useAzureStorage, 0, "StealTest", containerName);
        setupOneManager(useAzureStorage, 1, "StealTest", containerName);

        TestBase.logInfo("Check whether lease store exists before create");
        Boolean boolret = this.leaseManagers[0].leaseStoreExists().get();
        assertFalse("lease store should not exist yet", boolret);

        TestBase.logInfo("Creating lease store");
        this.leaseManagers[0].createLeaseStoreIfNotExists().get();

        TestBase.logInfo("Check whether lease store exists after create");
        boolret = this.leaseManagers[0].leaseStoreExists().get();
        assertTrue("lease store should exist but does not", boolret);

        TestBase.logInfo("Check whether second manager can see lease store");
        boolret = this.leaseManagers[1].leaseStoreExists().get();
        assertTrue("second manager cannot see lease store", boolret);

        TestBase.logInfo("First manager creating lease for partition 0");
        ArrayList<String> partitionIds = new ArrayList<String>();
        partitionIds.add("0");
        this.leaseManagers[0].createAllLeasesIfNotExists(partitionIds).get();

        TestBase.logInfo("Checking whether second manager can see lease 0");
        CompleteLease mgr2Lease = this.leaseManagers[1].getLease("0").get();
        assertNotNull("second manager cannot see lease for 0", mgr2Lease);

        TestBase.logInfo("Checking whether first manager can see lease 0");
        CompleteLease mgr1Lease = this.leaseManagers[0].getLease("0").get();
        assertNotNull("second manager cannot see lease for 0", mgr1Lease);

        TestBase.logInfo("First manager acquiring lease 0");
        boolret = this.leaseManagers[0].acquireLease(mgr1Lease).get();
        assertTrue("first manager failed acquiring lease for 0", boolret);
        if (useAzureStorage) {
            TestBase.logInfo("Lease token is " + ((AzureBlobLease) mgr1Lease).getToken());
        }

        int x = 0;
        while (getOneState("0", this.leaseManagers[0]).getIsOwned()) {
            assertFalse("lease 0 expiration is overdue", (5000 * x) > (this.leaseManagers[0].getLeaseDurationInMilliseconds() + 10000));
            Thread.sleep(5000);
            TestBase.logInfo("Still waiting for lease on 0 to expire: " + (5 * ++x));
        }

        TestBase.logInfo("Second manager acquiring lease 0");
        boolret = this.leaseManagers[1].acquireLease(mgr2Lease).get();
        assertTrue("second manager failed acquiring expired lease for 0", boolret);
        if (useAzureStorage) {
            TestBase.logInfo("Lease token is " + ((AzureBlobLease) mgr2Lease).getToken());
        }

        TestBase.logInfo("First manager trying to renew lease 0");
        boolret = this.leaseManagers[0].renewLease(mgr1Lease).get();
        assertFalse("first manager unexpected success renewing lease for 0", boolret);

        TestBase.logInfo("First manager getting lease 0");
        mgr1Lease = this.leaseManagers[0].getLease("0").get();
        assertNotNull("first manager cannot see lease for 0", mgr1Lease);

        TestBase.logInfo("First manager stealing lease 0");
        boolret = this.leaseManagers[0].acquireLease(mgr1Lease).get();
        assertTrue("first manager failed stealing lease 0", boolret);
        if (useAzureStorage) {
            TestBase.logInfo("Lease token is " + ((AzureBlobLease) mgr1Lease).getToken());
        }

        TestBase.logInfo("Second mananger getting lease 0");
        mgr2Lease = this.leaseManagers[1].getLease("0").get();
        assertNotNull("second manager cannot see lease for 0", mgr2Lease);

        TestBase.logInfo("Second mananger stealing lease 0");
        boolret = this.leaseManagers[1].acquireLease(mgr2Lease).get();
        assertTrue("second manager failed stealing lease 0", boolret);
        if (useAzureStorage) {
            TestBase.logInfo("Lease token is " + ((AzureBlobLease) mgr2Lease).getToken());
        }

        TestBase.logInfo("Second mananger releasing lease 0");
        this.leaseManagers[1].releaseLease(mgr2Lease).get();

        // Won't do anything because first manager didn't own lease 0, but shouldn't throw either
        TestBase.logInfo("First mananger tyring to release lease 0");
        this.leaseManagers[0].releaseLease(mgr1Lease).get();

        TestBase.logInfo("Cleaning up lease store");
        this.leaseManagers[1].deleteLeaseStore().get();
    }

    private String generateContainerName(String infix) {
        StringBuilder containerName = new StringBuilder(64);
        containerName.append("leasemgrtest-");
        if (infix != null) {
            containerName.append(infix);
            containerName.append('-');
        }
        containerName.append(UUID.randomUUID().toString());
        return containerName.toString();
    }

    private BaseLease getOneState(String partitionId, ILeaseManager leaseMgr) throws InterruptedException, ExecutionException {
        List<BaseLease> states = leaseMgr.getAllLeases().get();
        BaseLease returnState = null;
        for (BaseLease s : states) {
            if (s.getPartitionId().compareTo(partitionId) == 0) {
                returnState = s;
                break;
            }
        }
        return returnState;
    }

    private void setupOneManager(boolean useAzureStorage, int index, String suffix, String containerName) throws Exception {
        ILeaseManager leaseMgr = null;
        ICheckpointManager checkpointMgr = null;

        if (!useAzureStorage) {
            leaseMgr = new InMemoryLeaseManager();
            checkpointMgr = new InMemoryCheckpointManager();
        } else {
            TestBase.logInfo("Container name: " + containerName);
            String azureStorageConnectionString = TestUtilities.getStorageConnectionString();
            AzureStorageCheckpointLeaseManager azMgr = new AzureStorageCheckpointLeaseManager(azureStorageConnectionString, containerName, null);
            leaseMgr = azMgr;
            checkpointMgr = azMgr;
        }

        // Host name needs to be unique per host so use index. Event hub should be the same for all hosts in a test, so use the supplied suffix.
        EventProcessorHost host = EventProcessorHost.EventProcessorHostBuilder.newBuilder("dummyHost" + String.valueOf(index), EventHubClient.DEFAULT_CONSUMER_GROUP_NAME)
                .useUserCheckpointAndLeaseManagers(checkpointMgr, leaseMgr)
                .useEventHubConnectionString(RealEventHubUtilities.SYNTACTICALLY_CORRECT_DUMMY_CONNECTION_STRING + suffix,
                        RealEventHubUtilities.SYNTACTICALLY_CORRECT_DUMMY_EVENT_HUB_PATH + suffix)
                .build();

        try {
            if (!useAzureStorage) {
                ((InMemoryLeaseManager) leaseMgr).initialize(host.getHostContext());
                ((InMemoryCheckpointManager) checkpointMgr).initialize(host.getHostContext());
            } else {
                ((AzureStorageCheckpointLeaseManager) leaseMgr).initialize(host.getHostContext());
            }
        } catch (Exception e) {
            TestBase.logError("Manager initializion failed");
            throw e;
        }

        this.leaseManagers[index] = leaseMgr;
        this.hosts[index] = host;
    }
}
