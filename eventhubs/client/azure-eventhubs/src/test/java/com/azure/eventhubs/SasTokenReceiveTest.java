// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.eventhubs.implementation.ApiTestBase;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

public class SasTokenReceiveTest extends SasTokenTestBase {

    private static EventReceiverTest receiverTest;

    @Rule
    public TestName testName = new TestName();

    @Override
    protected String testName() {
        return testName.getMethodName();
    }

    @BeforeClass
    public static void initialize() {
        Assert.assertTrue(ApiTestBase.getCredentialInfo().sharedAccessKeyName() == null);
        Assert.assertTrue(ApiTestBase.getCredentialInfo().sharedAccessKey() == null);
//        Assert.assertTrue(ApiTestBase.getCredentialInfo().sharedAccessSignature() != nill);
        receiverTest = new EventReceiverTest();
        EventReceiverTest.initialize();
    }

    @AfterClass
    public static void cleanup() {
        EventReceiverTest.cleanup();
    }

    @Ignore
    @Test
    public void receiverStartofStreamFilters() {
        receiverTest.testReceiverStartOfStreamFilters();
    }
}
