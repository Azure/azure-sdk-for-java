// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.sendrecv;

import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.lib.SasTokenTestBase;
import com.microsoft.azure.eventhubs.lib.TestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class SasTokenReceiveTest extends SasTokenTestBase {

    private static ReceiveTest receiveTest;

    @BeforeAll
    public static void initialize() throws Exception {

        Assertions.assertTrue(TestContext.getConnectionString().getSharedAccessSignature() != null
                && TestContext.getConnectionString().getSasKey() == null
                && TestContext.getConnectionString().getSasKeyName() == null);

        receiveTest = new ReceiveTest();
        ReceiveTest.initializeEventHub(TestContext.getConnectionString());
    }

    @AfterAll
    public static void cleanup() throws EventHubException {
        ReceiveTest.cleanup();
    }

    @Test
    public void testReceiverStartOfStreamFilters() throws EventHubException {
        receiveTest.testReceiverStartOfStreamFilters();
    }

    @AfterEach
    public void testCleanup() throws EventHubException {
        receiveTest.testCleanup();
    }
}
