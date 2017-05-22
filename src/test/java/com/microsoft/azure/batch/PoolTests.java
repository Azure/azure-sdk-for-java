/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;
import com.microsoft.azure.batch.protocol.models.*;

public class PoolTests extends BatchTestBase {
    private static CloudPool livePool;

    @BeforeClass
    public static void setup() throws Exception {
        createClient(AuthMode.SharedKey);
        String poolId = getStringWithUserNamePrefix("-testpool");
        livePool = createIfNotExistPaaSPool(poolId);
        Assert.assertNotNull(livePool);
    }

    @AfterClass
    public static void cleanup() throws Exception {
        try {
            //batchClient.poolOperations().deletePool(livePool.id());
        }
        catch (Exception e) {
            // ignore any clean up exception
        }
    }

    @Test
    public void testPoolOData() throws Exception {
        CloudPool pool = batchClient.poolOperations().getPool(livePool.id(), new DetailLevel.Builder().withExpandClause("stats").build());
        Assert.assertNotNull(pool.stats());

        List<CloudPool> pools = batchClient.poolOperations().listPools(new DetailLevel.Builder().withSelectClause("id, state").build());
        Assert.assertTrue(pools.size() > 0);
        Assert.assertNotNull(pools.get(0).id());
        Assert.assertNull(pools.get(0).vmSize());

        pools = batchClient.poolOperations().listPools(new DetailLevel.Builder().withFilterClause("state eq 'deleting'").build());
        Assert.assertTrue(pools.size() == 0);
    }

    @Test
    public void canCRUDLowPriIaaSPool() throws Exception {
        // CREATE
        String poolId = getStringWithUserNamePrefix("-testpool2");

        // Create a pool with 3 Small VMs
        String POOL_VM_SIZE = "STANDARD_A1";
        int POOL_VM_COUNT = 0;
        int POOL_LOW_PRI_VM_COUNT = 2;

        // 5 minutes
        long POOL_STEADY_TIMEOUT_IN_SECONDS = 5 * 60 * 1000;

        // Check if pool exists
        if (!batchClient.poolOperations().existsPool(poolId)) {
            ImageReference imgRef = new ImageReference().withPublisher("Canonical").withOffer("UbuntuServer").withSku("16.04-LTS").withVersion("latest");
            VirtualMachineConfiguration configuration = new VirtualMachineConfiguration();
            configuration.withNodeAgentSKUId("batch.node.ubuntu 16.04").withImageReference(imgRef);

            batchClient.poolOperations().createPool(poolId, POOL_VM_SIZE, configuration, POOL_VM_COUNT, POOL_LOW_PRI_VM_COUNT);
        }

        try {
            // GET
            Assert.assertTrue(batchClient.poolOperations().existsPool(poolId));

            long startTime = System.currentTimeMillis();
            long elapsedTime = 0L;
            boolean steady = false;
            CloudPool pool = null;

            // Wait for the VM to be allocated
            while (elapsedTime < POOL_STEADY_TIMEOUT_IN_SECONDS) {
                pool = batchClient.poolOperations().getPool(poolId);
                Assert.assertNotNull(pool);

                if (pool.allocationState() == AllocationState.STEADY) {
                    steady = true;
                    break;
                }
                System.out.println("wait 30 seconds for pool steady...");
                Thread.sleep(30 * 1000);
                elapsedTime = (new Date()).getTime() - startTime;
            }

            Assert.assertTrue("The pool did not reach a steady state in the allotted time", steady);
            Assert.assertEquals((long)pool.currentDedicatedNodes(), POOL_VM_COUNT);
            Assert.assertEquals((long)pool.currentLowPriorityNodes(), POOL_LOW_PRI_VM_COUNT);

            // RESIZE
            batchClient.poolOperations().resizePool(poolId, 1, 1);

            pool = batchClient.poolOperations().getPool(poolId);
            Assert.assertEquals((long)pool.targetDedicatedNodes(), 1);
            Assert.assertEquals((long)pool.targetLowPriorityNodes(), 1);

            // DELETE
            boolean deleted = false;
            batchClient.poolOperations().deletePool(poolId);
            // Wait for the VM to be allocated
            while (elapsedTime < POOL_STEADY_TIMEOUT_IN_SECONDS) {
                try {
                    batchClient.poolOperations().getPool(poolId);
                } catch (BatchErrorException err) {
                    if (err.body().code().equals(BatchErrorCodeStrings.PoolNotFound)) {
                        deleted = true;
                        break;
                    } else {
                        throw err;
                    }
                }
                System.out.println("wait 5 seconds for pool delete...");
                Thread.sleep(5 * 1000);
                elapsedTime = (new Date()).getTime() - startTime;
            }
            Assert.assertTrue(deleted);
        }
        finally {
            try {
                if (batchClient.poolOperations().existsPool(poolId)) {
                    batchClient.poolOperations().deletePool(poolId);
                }
            }
            catch (Exception e)
            {
                // Ignore exception
            }
        }
    }

    @Test
    public void canCRUDIaaSPool() throws Exception {
        String poolId = getStringWithUserNamePrefix("-testpool2");

        // Create a pool with 3 Small VMs
        String POOL_VM_SIZE = "STANDARD_D1";
        int POOL_VM_COUNT = 3;

        // Use IaaS VM with Linux
        List<String> uris = new ArrayList<>();
        uris.add("http://image-A");
        uris.add("http://image-B");
        VirtualMachineConfiguration configuration = new VirtualMachineConfiguration();
        configuration.withNodeAgentSKUId("batch.node.ubuntu 16.04").withOsDisk(new OSDisk().withImageUris(uris));

        try
        {
            batchClient.poolOperations().createPool(poolId, POOL_VM_SIZE, configuration, POOL_VM_COUNT);
            throw new RuntimeException("Should throw exception");
        }
        catch (BatchErrorException ex)
        {
            if (!ex.getMessage().contains("In"))
            {
                throw new RuntimeException("Unexpected exception");
            }
        }
    }

    @Test
    public void canCRUDLowPriPaaSPool() throws Exception {
        // CREATE
        String poolId = getStringWithUserNamePrefix("-testpool4");

        // Create a pool with 3 Small VMs
        String POOL_VM_SIZE = "Small";
        int POOL_VM_COUNT = 1;
        int POOL_LOW_PRI_VM_COUNT = 2;
        String POOL_OS_FAMILY = "4";
        String POOL_OS_VERSION = "*";

        // 5 minutes
        long POOL_STEADY_TIMEOUT_IN_SECONDS = 5 * 60 * 1000;

        // Check if pool exists
        if (!batchClient.poolOperations().existsPool(poolId)) {
            // Use PaaS VM with Windows
            CloudServiceConfiguration configuration = new CloudServiceConfiguration();
            configuration.withOsFamily(POOL_OS_FAMILY).withTargetOSVersion(POOL_OS_VERSION);
            List<String> licenses = new ArrayList<>();
            licenses.add("maya");

            PoolAddParameter addParameter = new PoolAddParameter()
                    .withId(poolId)
                    .withTargetDedicatedNodes(POOL_VM_COUNT)
                    .withTargetLowPriorityNodes(POOL_LOW_PRI_VM_COUNT)
                    .withVmSize(POOL_VM_SIZE)
                    .withCloudServiceConfiguration(configuration)
                    .withApplicationLicenses(licenses);
            batchClient.poolOperations().createPool(addParameter);
        }

        try {
            // GET
            Assert.assertTrue(batchClient.poolOperations().existsPool(poolId));

            long startTime = System.currentTimeMillis();
            long elapsedTime = 0L;
            boolean steady = false;
            CloudPool pool = null;

            // Wait for the VM to be allocated
            while (elapsedTime < POOL_STEADY_TIMEOUT_IN_SECONDS) {
                pool = batchClient.poolOperations().getPool(poolId);
                Assert.assertNotNull(pool);

                if (pool.allocationState() == AllocationState.STEADY) {
                    steady = true;
                    break;
                }
                System.out.println("wait 30 seconds for pool steady...");
                Thread.sleep(30 * 1000);
                elapsedTime = (new Date()).getTime() - startTime;
            }

            Assert.assertTrue("The pool did not reach a steady state in the allotted time", steady);
            Assert.assertEquals((long)pool.currentDedicatedNodes(), POOL_VM_COUNT);
            Assert.assertEquals((long)pool.currentLowPriorityNodes(), POOL_LOW_PRI_VM_COUNT);

            // RESIZE
            batchClient.poolOperations().resizePool(poolId, null, 1);

            pool = batchClient.poolOperations().getPool(poolId);
            Assert.assertEquals((long)pool.targetDedicatedNodes(), POOL_VM_COUNT);
            Assert.assertEquals((long)pool.targetLowPriorityNodes(), 1);

            // DELETE
            boolean deleted = false;
            batchClient.poolOperations().deletePool(poolId);
            // Wait for the VM to be allocated
            while (elapsedTime < POOL_STEADY_TIMEOUT_IN_SECONDS) {
                try {
                    batchClient.poolOperations().getPool(poolId);
                } catch (BatchErrorException err) {
                    if (err.body().code().equals(BatchErrorCodeStrings.PoolNotFound)) {
                        deleted = true;
                        break;
                    } else {
                        throw err;
                    }
                }
                System.out.println("wait 5 seconds for pool delete...");
                Thread.sleep(5 * 1000);
                elapsedTime = (new Date()).getTime() - startTime;
            }
            Assert.assertTrue(deleted);
        }
        finally {
            try {
                if (batchClient.poolOperations().existsPool(poolId)) {
                    batchClient.poolOperations().deletePool(poolId);
                }
            }
            catch (Exception e)
            {
                // Ignore exception
            }
        }
    }

    @Test
    public void canCRUDPaaSPool() throws Exception {
        // CREATE
        String poolId = getStringWithUserNamePrefix("-testpool1");

        // Create a pool with 3 Small VMs
        String POOL_VM_SIZE = "Small";
        int POOL_VM_COUNT = 3;
        String POOL_OS_FAMILY = "4";
        String POOL_OS_VERSION = "*";

        // 5 minutes
        long POOL_STEADY_TIMEOUT_IN_SECONDS = 5 * 60 * 1000;

        // Check if pool exists
        if (!batchClient.poolOperations().existsPool(poolId)) {
            // Use PaaS VM with Windows
            CloudServiceConfiguration configuration = new CloudServiceConfiguration();
            configuration.withOsFamily(POOL_OS_FAMILY).withTargetOSVersion(POOL_OS_VERSION);

            List<UserAccount> userList = new ArrayList<>();
            userList.add(new UserAccount().withName("test-user-1").withPassword("kt#_gahr!@aGERDXA"));
            userList.add(new UserAccount().withName("test-user-2").withPassword("kt#_gahr!@aGERDXA").withElevationLevel(ElevationLevel.ADMIN));
            PoolAddParameter addParameter = new PoolAddParameter()
                    .withId(poolId)
                    .withTargetDedicatedNodes(POOL_VM_COUNT)
                    .withVmSize(POOL_VM_SIZE)
                    .withCloudServiceConfiguration(configuration)
                    .withUserAccounts(userList);
            batchClient.poolOperations().createPool(addParameter);
        }

        try {
            // GET
            Assert.assertTrue(batchClient.poolOperations().existsPool(poolId));

            long startTime = System.currentTimeMillis();
            long elapsedTime = 0L;
            boolean steady = false;
            CloudPool pool = null;

            // Wait for the VM to be allocated
            while (elapsedTime < POOL_STEADY_TIMEOUT_IN_SECONDS) {
                pool = batchClient.poolOperations().getPool(poolId);
                Assert.assertNotNull(pool);

                if (pool.allocationState() == AllocationState.STEADY) {
                    steady = true;
                    break;
                }
                System.out.println("wait 30 seconds for pool steady...");
                Thread.sleep(30 * 1000);
                elapsedTime = (new Date()).getTime() - startTime;
            }

            Assert.assertTrue("The pool did not reach a steady state in the allotted time", steady);
            Assert.assertNotNull(pool.userAccounts());
            Assert.assertEquals(pool.userAccounts().get(0).name(), "test-user-1");
            Assert.assertEquals(pool.userAccounts().get(0).elevationLevel(), ElevationLevel.NON_ADMIN);
            Assert.assertNull(pool.userAccounts().get(0).password());
            Assert.assertEquals(pool.userAccounts().get(1).elevationLevel(), ElevationLevel.ADMIN);

            // LIST
            List<CloudPool> pools = batchClient.poolOperations().listPools();
            Assert.assertTrue(pools.size() > 0);

            boolean found = false;
            for (CloudPool p : pools) {
                if (p.id().equals(poolId)) {
                    found = true;
                    break;
                }
            }

            Assert.assertTrue(found);

            // UPDATE
            LinkedList<MetadataItem> metadata = new LinkedList<>();
            metadata.add((new MetadataItem()).withName("key1").withValue("value1"));
            batchClient.poolOperations().patchPool(poolId, null, null, null, metadata);

            pool = batchClient.poolOperations().getPool(poolId);
            Assert.assertTrue(pool.metadata().size() == 1);
            Assert.assertTrue(pool.metadata().get(0).name().equals("key1"));

            batchClient.poolOperations().updatePoolProperties(poolId, null,
                    new LinkedList<CertificateReference>(),
                    new LinkedList<ApplicationPackageReference>(),
                    new LinkedList<MetadataItem>());
            pool = batchClient.poolOperations().getPool(poolId);
            Assert.assertNull(pool.metadata());

            // DELETE
            boolean deleted = false;
            batchClient.poolOperations().deletePool(poolId);
            // Wait for the VM to be allocated
            while (elapsedTime < POOL_STEADY_TIMEOUT_IN_SECONDS) {
                try {
                    batchClient.poolOperations().getPool(poolId);
                } catch (BatchErrorException err) {
                    if (err.body().code().equals(BatchErrorCodeStrings.PoolNotFound)) {
                        deleted = true;
                        break;
                    } else {
                        throw err;
                    }
                }
                System.out.println("wait 5 seconds for pool delete...");
                Thread.sleep(5 * 1000);
                elapsedTime = (new Date()).getTime() - startTime;
            }
            Assert.assertTrue(deleted);
        }
        finally {
            try {
                if (batchClient.poolOperations().existsPool(poolId)) {
                    batchClient.poolOperations().deletePool(poolId);
                }
            }
            catch (Exception e)
            {
                // Ignore exception
            }
        }
    }
}
