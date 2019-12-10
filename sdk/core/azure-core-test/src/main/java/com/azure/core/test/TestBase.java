// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.test;

import com.azure.core.test.utils.TestResourceNamer;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.util.Locale;

/**
 * Base class for running live and playback tests using {@link InterceptorManager}.
 */
public abstract class TestBase implements BeforeEachCallback {
    // Environment variable name used to determine the TestMode.
    private static final String AZURE_TEST_MODE = "AZURE_TEST_MODE";
    private static TestMode testMode;

    private final ClientLogger logger = new ClientLogger(TestBase.class);

    protected InterceptorManager interceptorManager;
    protected TestResourceNamer testResourceNamer;
    protected TestContextManager testContextManager;

    private ExtensionContext extensionContext;

    /**
     * Before tests are executed, determines the test mode by reading the {@link TestBase#AZURE_TEST_MODE} environment
     * variable. If it is not set, {@link TestMode#PLAYBACK}
     */
    @BeforeAll
    public static void setupClass() {
        testMode = initializeTestMode();
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        this.extensionContext = extensionContext;
    }

    /**
     * Sets-up the {@link TestBase#testResourceNamer} and {@link TestBase#interceptorManager} before each test case is
     * run. Then calls its implementing class to perform any other set-up commands.
     *
     * @param testInfo {@link TestInfo} to retrieve test method name.
     */
    @BeforeEach
    public void setupTest(TestInfo testInfo) {
        this.testContextManager = new TestContextManager(testInfo.getTestMethod().get(), testMode);
        logger.info("Test Mode: {}, Name: {}", testMode, testContextManager.getTestName());

        try {
            interceptorManager = new InterceptorManager(testContextManager);
        } catch (UncheckedIOException e) {
            logger.error("Could not create interceptor for {}", testContextManager.getTestName(), e);
            Assertions.fail();
        }
        testResourceNamer = new TestResourceNamer(testContextManager, interceptorManager.getRecordedData());

        beforeTest();
    }

    /**
     * Disposes of {@link InterceptorManager} and its inheriting class' resources.
     * @param testInfo the injected testInfo
     */
    @AfterEach
    public void teardownTest(TestInfo testInfo) {
        if (testContextManager.didTestRun()) {
            afterTest();
            interceptorManager.close();
        }
    }

    /**
     * Gets the TestMode that has been initialized.
     *
     * @return The TestMode that has been initialized.
     */
    public TestMode getTestMode() {
        return testMode;
    }

    /**
     * Gets the name of the current test being run.
     *
     * @deprecated This method is deprecated as JUnit 5 provides a simpler mechanism to get the test method name through
     * {@link TestInfo}. Keeping this for backward compatability of other client libraries that still override this
     * method. This method can be deleted when all client libraries remove this method. See {@link
     * #setupTest(TestInfo)}.
     * @return The name of the current test.
     */
    @Deprecated
    protected String getTestName() {
        if (extensionContext != null) {
            return extensionContext.getTestMethod().map(Method::getName).orElse(null);
        }
        return null;
    }

    /**
     * Performs any set-up before each test case. Any initialization that occurs in TestBase occurs first before this.
     * Can be overridden in an inheriting class to add additional functionality during test set-up.
     */
    protected void beforeTest() {
    }

    /**
     * Dispose of any resources and clean-up after a test case runs. Can be overridden in an inheriting class to add
     * additional functionality during test teardown.
     */
    protected void afterTest() {
    }

    private static TestMode initializeTestMode() {
        final ClientLogger logger = new ClientLogger(TestBase.class);
        final String azureTestMode = Configuration.getGlobalConfiguration().get(AZURE_TEST_MODE);

        if (azureTestMode != null) {
            try {
                return TestMode.valueOf(azureTestMode.toUpperCase(Locale.US));
            } catch (IllegalArgumentException e) {
                logger.error("Could not parse '{}' into TestEnum. Using 'Playback' mode.", azureTestMode);
                return TestMode.PLAYBACK;
            }
        }

        logger.info("Environment variable '{}' has not been set yet. Using 'Playback' mode.", AZURE_TEST_MODE);
        return TestMode.PLAYBACK;
    }

    /**
     * Sleeps the test for the given amount of milliseconds if {@link TestMode} isn't {@link TestMode#PLAYBACK}.
     *
     * @param millis Number of milliseconds to sleep the test.
     * @throws IllegalStateException If the sleep is interrupted.
     */
    protected void sleepIfRunningAgainstService(long millis) {
        if (testMode == TestMode.PLAYBACK) {
            return;
        }

        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            throw logger.logExceptionAsWarning(new IllegalStateException(ex));
        }
    }
}
