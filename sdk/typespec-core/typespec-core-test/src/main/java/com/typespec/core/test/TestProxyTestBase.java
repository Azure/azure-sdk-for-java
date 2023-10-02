// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.test;

import com.typespec.core.test.utils.TestProxyManager;
import com.typespec.core.util.logging.ClientLogger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInfo;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.typespec.core.test.utils.TestUtils.toURI;

/**
 * Base class for running live and playback tests using test-proxy
 */
public abstract class TestProxyTestBase extends TestBase {
    private static final ClientLogger LOGGER = new ClientLogger(TestProxyTestBase.class);
    static {
        setTestProxyEnabled();
    }

    /**
     * Creates an instance of {@link TestProxyTestBase}.
     */
    public TestProxyTestBase() {
        super();
    }

    private static TestProxyManager testProxyManager;

    /**
     * Before tests are executed, determines the test mode by reading the {@code AZURE_TEST_MODE} environment variable.
     * If it is not set, {@link TestMode#PLAYBACK}
     * @param testInfo {@link TestInfo} to retrieve test related metadata.
     */
    @BeforeAll
    public static void setupTestProxy(TestInfo testInfo) {
        testMode = initializeTestMode();
        Path testClassPath = Paths.get(toURI(testInfo.getTestClass().get().getResource(testInfo.getTestClass().get().getSimpleName() + ".class")));
        if (isTestProxyEnabled() && (testMode == TestMode.PLAYBACK || testMode == TestMode.RECORD)) {
            testProxyManager = new TestProxyManager(testClassPath);
            testProxyManager.startProxy();
        }
    }

    /**
     * Performs cleanup actions after all tests are executed.
     */
    @AfterAll
    public static void teardownTestProxy() {
        if (testProxyManager != null) {
            testProxyManager.stopProxy();
        }
    }
}
