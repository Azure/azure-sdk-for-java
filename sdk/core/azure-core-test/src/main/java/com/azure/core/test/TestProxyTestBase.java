// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test;

import com.azure.core.test.utils.TestProxyManager;
import com.azure.core.test.utils.TestUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

/**
 * Base class for running live and playback tests using test-proxy
 */
public abstract class TestProxyTestBase extends TestBase {
    static {
        setTestProxyEnabled();
    }
    private static TestProxyManager testProxyManager;

    /**
     * Before tests are executed, determines the test mode by reading the {@code AZURE_TEST_MODE} environment variable.
     * If it is not set, {@link TestMode#PLAYBACK}
     */
    @BeforeAll
    public static void setup() {
        testMode = initializeTestMode();
        if (isTestProxyEnabled()) {
            testProxyManager = new TestProxyManager(TestUtils.getRecordFolder());
            testProxyManager.startProxy();
        }
    }

    /**
     * Performs cleanup actions after all tests are executed.
     */
    @AfterAll
    public static void teardown() {
        if (testProxyManager != null) {
            testProxyManager.stopProxy();
        }
    }
}
