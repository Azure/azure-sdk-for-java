/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.sendrecv;

import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.lib.SasTokenTestBase;
import com.microsoft.azure.eventhubs.lib.TestContext;
import org.junit.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class SasTokenSendTest extends SasTokenTestBase {

    private static SendTest sendTest;

    @BeforeClass
    public static void initialize() throws Exception {

        Assert.assertTrue(TestContext.getConnectionString().getSharedAccessSignature() != null
                && TestContext.getConnectionString().getSasKey() == null
                && TestContext.getConnectionString().getSasKeyName() == null);

        sendTest = new SendTest();
        SendTest.initializeEventHub(TestContext.getConnectionString());
    }

    @AfterClass
    public static void cleanupClient() throws EventHubException {

        SendTest.cleanupClient();
    }

    @Test
    public void sendBatchRetainsOrderWithinBatch() throws EventHubException, InterruptedException, ExecutionException, TimeoutException {

        sendTest.sendBatchRetainsOrderWithinBatch();
    }

    @Test
    public void sendResultsInSysPropertiesWithPartitionKey() throws EventHubException, InterruptedException, ExecutionException, TimeoutException {

        sendTest.sendResultsInSysPropertiesWithPartitionKey();
    }

    @After
    public void cleanup() throws EventHubException {

        sendTest.cleanup();
    }
}
