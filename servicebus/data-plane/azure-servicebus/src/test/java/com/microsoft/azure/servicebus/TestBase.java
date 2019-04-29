// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.servicebus;

import com.azure.common.test.TestMode;
import org.junit.Assume;
import org.junit.BeforeClass;

public class TestBase {

    @BeforeClass
    public static void skipIfNotConfigured() {
        Assume.assumeTrue("The test only runs in Live mode.", TestBase.getTestMode() == TestMode.RECORD);
    }

    public static TestMode getTestMode() {
        String testMode = System.getenv("AZURE_TEST_MODE");
        if (testMode == null) {
            return TestMode.PLAYBACK;
        }
        return TestMode.RECORD;
    }
}
