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

public class WebSocketsReceiveTest extends SasTokenTestBase {

    private static ReceiveTest receiveTest;

    @BeforeClass
    public static void initialize() throws Exception {

        Assert.assertTrue(TestContext.getConnectionString().getSharedAccessSignature() != null
                && TestContext.getConnectionString().getSasKey() == null
                && TestContext.getConnectionString().getSasKeyName() == null);

        receiveTest = new ReceiveTest();
        ConnectionStringBuilder connectionString = TestContext.getConnectionString();
        connectionString.setTransportType(TransportType.AMQP_WEB_SOCKETS);
        ReceiveTest.initializeEventHub(connectionString);
    }

    @AfterClass()
    public static void cleanup() throws EventHubException {
        ReceiveTest.cleanup();
    }

    @Test()
    public void testReceiverStartOfStreamFilters() throws EventHubException {
        receiveTest.testReceiverStartOfStreamFilters();
    }

    @After
    public void testCleanup() throws EventHubException {
        receiveTest.testCleanup();
    }
}
