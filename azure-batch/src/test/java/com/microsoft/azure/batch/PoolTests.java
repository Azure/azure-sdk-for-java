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
    static CloudPool livePool;

    @BeforeClass
    public static void setup() throws Exception {
        createClient();
        String poolId = getStringWithUserNamePrefix("-testpool");
        livePool = createIfNotExistPaaSPool(poolId);
        Assert.assertNotNull(livePool);
    }

    @AfterClass
    public static void cleanup() throws Exception {
        try {
            batchClient.poolOperations().deletePool(livePool.id());
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
    public void canCRUDPaaSPool() throws Exception {
        // CREATE
        String poolId = getStringWithUserNamePrefix("-testpool1");

        // Create a pool with 3 Small VMs
        String POOL_VM_SIZE = "Small";
        int POOL_VM_COUNT = 3;
        String POOL_OS_FAMILY = "4";
        String POOL_OS_VERSION = "*";

        // 5 minutes
        long POOL_STEADY_TIMEOUT = 5 * 60 * 60;

        // Check if pool exists
        if (!batchClient.poolOperations().existsPool(poolId)) {
            // Use PaaS VM with Windows
            CloudServiceConfiguration configuration = new CloudServiceConfiguration();
            configuration.withOsFamily(POOL_OS_FAMILY).withTargetOSVersion(POOL_OS_VERSION);

            batchClient.poolOperations().createPool(poolId, POOL_VM_SIZE, configuration, POOL_VM_COUNT);
        }

        try {
            // GET
            Assert.assertTrue(batchClient.poolOperations().existsPool(poolId));

            long startTime = System.currentTimeMillis();
            long elapsedTime = 0L;
            boolean steady = false;
            CloudPool pool;

            // Wait for the VM to be allocated
            while (elapsedTime < POOL_STEADY_TIMEOUT) {
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
            LinkedList<MetadataItem> metadata = new LinkedList<MetadataItem>();
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
            while (elapsedTime < POOL_STEADY_TIMEOUT) {
                try {
                    pool = batchClient.poolOperations().getPool(poolId);
                } catch (BatchErrorException err) {
                    if (err.getBody().code().equals(BatchErrorCodeStrings.PoolNotFound)) {
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
            if (batchClient.poolOperations().existsPool(poolId)) {
                batchClient.poolOperations().deletePool(poolId);
            }
        }
    }
}
