// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.exceptioncontracts;

import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.PayloadSizeExceededException;
import com.microsoft.azure.eventhubs.TransportType;
import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.lib.TestContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class WebSocketsSendLargeMessageTest extends ApiTestBase {
    private static SendLargeMessageTest sendLargeMessageTest;

    @BeforeClass
    public static void initialize() throws Exception {
        final ConnectionStringBuilder connectionStringBuilder = TestContext.getConnectionString();
        connectionStringBuilder.setTransportType(TransportType.AMQP_WEB_SOCKETS);
        sendLargeMessageTest = new SendLargeMessageTest();
        SendLargeMessageTest.initializeEventHubClients(connectionStringBuilder);
    }

    @AfterClass()
    public static void cleanup() throws EventHubException {
        SendLargeMessageTest.cleanup();
    }

    @Test()
    public void sendMsgLargerThan64k() throws EventHubException {
        sendLargeMessageTest.sendMsgLargerThan64k();
    }

    @Test(expected = PayloadSizeExceededException.class)
    public void sendMsgLargerThan1024K() throws EventHubException {
        sendLargeMessageTest.sendMsgLargerThan1024K();
    }

    @Test()
    public void sendMsgLargerThan128k() throws EventHubException {
        sendLargeMessageTest.sendMsgLargerThan128k();
    }
}
