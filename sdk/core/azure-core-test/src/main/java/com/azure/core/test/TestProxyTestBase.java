// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test;

import com.azure.core.test.utils.TestProxyManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

/**
 * Base class for running live and playback tests using test-proxy
 */
public class TestProxyTestBase extends TestBase {
    static {
        enableTestProxy = true;
    }
    private static TestProxyManager testProxyManager;

    /**
     * Before tests are executed, determines the test mode by reading the {@code AZURE_TEST_MODE} environment variable.
     * If it is not set, {@link TestMode#PLAYBACK}
     */
    @SuppressWarnings({"deprecation", "resource"})
    @BeforeAll
    public static void setupClass() {
        testMode = initializeTestMode();
        if (useTestProxy() && (testMode == TestMode.PLAYBACK || testMode == TestMode.RECORD)) {
            testProxyManager = new TestProxyManager(InterceptorManager.getRecordFolder());
            testProxyManager.startProxy();
        }
    }

    /**
     * Performs cleanup actions after all tests are executed.
     */
    @AfterAll
    public static void teardownClass() {
        if (testProxyManager != null) {
            testProxyManager.stopProxy();
        }
    }
}
