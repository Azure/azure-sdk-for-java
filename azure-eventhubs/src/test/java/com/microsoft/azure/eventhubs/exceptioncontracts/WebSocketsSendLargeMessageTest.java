/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
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

import java.io.IOException;
import java.util.concurrent.ExecutionException;

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
    public void sendMsgLargerThan64k() throws EventHubException, InterruptedException, ExecutionException, IOException {
        sendLargeMessageTest.sendMsgLargerThan64k();
    }

    @Test(expected = PayloadSizeExceededException.class)
    public void sendMsgLargerThan1024K() throws EventHubException, InterruptedException, ExecutionException, IOException {
        sendLargeMessageTest.sendMsgLargerThan1024K();
    }

    @Test()
    public void sendMsgLargerThan128k() throws EventHubException, InterruptedException, ExecutionException, IOException {
        sendLargeMessageTest.sendMsgLargerThan128k();
    }
}
