// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.test;

import com.azure.core.util.configuration.ConfigurationManager;
import com.azure.core.test.utils.TestResourceNamer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Locale;

/**
 * Base class for running live and playback tests using {@link InterceptorManager}.
 */
public abstract class TestBase {
    // Environment variable name used to determine the TestMode.
    private static final String AZURE_TEST_MODE = "AZURE_TEST_MODE";
    private static TestMode testMode;

    private final Logger logger = LoggerFactory.getLogger(TestBase.class);

    protected InterceptorManager interceptorManager;
    protected TestResourceNamer testResourceNamer;

    /**
     * Before tests are executed, determines the test mode by reading the {@link TestBase#AZURE_TEST_MODE} environment
     * variable. If it is not set, {@link TestMode#PLAYBACK}
     */
    @BeforeClass
    public static void setupClass() {
        testMode = initializeTestMode();
    }

    /**
     * Sets-up the {@link TestBase#testResourceNamer} and {@link TestBase#interceptorManager} before each test case is run.
     * Then calls its implementing class to perform any other set-up commands.
     */
    @Before
    public void setupTest() {
        final String testName = testName();
        if (logger.isInfoEnabled()) {
            logger.info("Test Mode: {}, Name: {}", testMode, testName);
        }

        try {
            interceptorManager = new InterceptorManager(testName, testMode);
        } catch (IOException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Could not create interceptor for {}", testName, e);
            }
            Assert.fail();
        }
        testResourceNamer = new TestResourceNamer(testName, testMode, interceptorManager.getRecordedData());

        beforeTest();
    }

    /**
     * Disposes of {@link InterceptorManager} and its inheriting class' resources.
     */
    @After
    public void teardownTest() {
        afterTest();
        interceptorManager.close();
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
     * <p>
     * NOTE: This could not be implemented in the base class using {@link TestName} because it always returns
     * {@code null}. See https://stackoverflow.com/a/16113631/4220757.
     *
     * @return The name of the current test.
     */
    protected abstract String testName();

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
        final Logger logger = LoggerFactory.getLogger(TestBase.class);
        final String azureTestMode = ConfigurationManager.getConfiguration().get(AZURE_TEST_MODE);

        if (azureTestMode != null) {
            try {
                return TestMode.valueOf(azureTestMode.toUpperCase(Locale.US));
            } catch (IllegalArgumentException e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Could not parse '{}' into TestEnum. Using 'Playback' mode.", azureTestMode);
                }

                return TestMode.PLAYBACK;
            }
        }

        if (logger.isInfoEnabled()) {
            logger.info("Environment variable '{}' has not been set yet. Using 'Playback' mode.", AZURE_TEST_MODE);
        }
        return TestMode.PLAYBACK;
    }
}
