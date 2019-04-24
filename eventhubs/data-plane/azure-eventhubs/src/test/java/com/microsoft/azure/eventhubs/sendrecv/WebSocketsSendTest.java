// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.sendrecv;

import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.TransportType;
import com.microsoft.azure.eventhubs.lib.SasTokenTestBase;
import com.microsoft.azure.eventhubs.lib.TestContext;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class WebSocketsSendTest extends SasTokenTestBase {

    private static SendTest sendTest;

    @BeforeClass
    public static void initialize() throws Exception {

        Assert.assertTrue(TestContext.getConnectionString().getSharedAccessSignature() != null
                && TestContext.getConnectionString().getSasKey() == null
                && TestContext.getConnectionString().getSasKeyName() == null);

        sendTest = new SendTest();

        ConnectionStringBuilder connectionString = TestContext.getConnectionString();
        connectionString.setTransportType(TransportType.AMQP_WEB_SOCKETS);
        SendTest.initializeEventHub(connectionString);
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
