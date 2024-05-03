// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test;

import com.azure.core.test.utils.TestProxyManager;
import org.junit.jupiter.api.BeforeAll;

/**
 * Base class for running live and playback tests using test-proxy
 */
public abstract class TestProxyTestBase extends TestBase {
    static {
        setTestProxyEnabled();
    }

    /**
     * Creates an instance of {@link TestProxyTestBase}.
     */
    public TestProxyTestBase() {
        super();
    }

    /**
     * Before tests are executed, determines the test mode by reading the {@code AZURE_TEST_MODE} environment variable.
     * If it is not set, {@link TestMode#PLAYBACK}
     */
    @BeforeAll
    public static void setupTestProxy() {
        testMode = initializeTestMode();
        if (isTestProxyEnabled() && (testMode == TestMode.PLAYBACK || testMode == TestMode.RECORD)) {
            TestProxyManager.startProxy();
        }
    }
}
