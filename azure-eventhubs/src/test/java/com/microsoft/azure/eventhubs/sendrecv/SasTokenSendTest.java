/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.sendrecv;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.azure.eventhubs.lib.SasTokenTestBase;
import com.microsoft.azure.eventhubs.lib.TestContext;
import com.microsoft.azure.servicebus.ServiceBusException;

public class SasTokenSendTest extends SasTokenTestBase {
    
    private static SendTest sendTest;
    
    @BeforeClass
    public static void initialize()  throws Exception {
        
        Assert.assertTrue(TestContext.getConnectionString().getSharedAccessSignature() != null
                            && TestContext.getConnectionString().getSasKey() == null
                            && TestContext.getConnectionString().getSasKeyName() == null);

        sendTest = new SendTest();
        SendTest.initializeEventHub();
    }
    
    @Test
    public void sendBatchRetainsOrderWithinBatch() throws ServiceBusException, InterruptedException, ExecutionException, TimeoutException {
        
        sendTest.sendBatchRetainsOrderWithinBatch();
    }
    
    @Test
    public void sendResultsInSysPropertiesWithPartitionKey() throws ServiceBusException, InterruptedException, ExecutionException, TimeoutException {
        
        sendTest.sendResultsInSysPropertiesWithPartitionKey();
    }
    
    @After
    public void cleanup() throws ServiceBusException {
        
        sendTest.cleanup();
    }
    
    @AfterClass
    public static void cleanupClient() throws ServiceBusException {

        SendTest.cleanupClient();
    }
}
