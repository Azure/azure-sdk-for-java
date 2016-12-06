/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch;

import okhttp3.logging.HttpLoggingInterceptor;
import com.microsoft.azure.batch.BatchClient;
import com.microsoft.azure.batch.auth.BatchSharedKeyCredentials;
import com.microsoft.azure.batch.protocol.models.*;
import java.util.*;
import org.junit.Assert;

/**
 * The base for batch dataplane tests.
 */
public abstract class BatchTestBase {
    protected static BatchClient batchClient;

    protected static void createClient() {
        BatchSharedKeyCredentials credentials = new BatchSharedKeyCredentials(
                System.getenv("AZURE_BATCH_ENDPOINT"),
                System.getenv("AZURE_BATCH_ACCOUNT"),
                System.getenv("AZURE_BATCH_ACCESS_KEY"));
        batchClient = BatchClient.open(credentials);
    }

    protected static CloudPool createIfNotExistPaaSPool(String poolId) throws Exception {
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

        long startTime = System.currentTimeMillis();
        long elapsedTime = 0L;
        boolean steady = false;
        CloudPool pool;

        // Wait for the VM to be allocated
        while (elapsedTime < POOL_STEADY_TIMEOUT) {
            pool = batchClient.poolOperations().getPool(poolId);
            if (pool.allocationState() == AllocationState.STEADY) {
                steady = true;
                break;
            }
            System.out.println("wait 30 seconds for pool steady...");
            Thread.sleep(30 * 1000);
            elapsedTime = (new Date()).getTime() - startTime;
        }

        Assert.assertTrue("The pool did not reach a steady state in the allotted time", steady);

        return batchClient.poolOperations().getPool(poolId);
    }

    protected static String getStringWithUserNamePrefix(String name) {
        String userName = System.getProperty("user.name");
        return userName + name;
    }
}
