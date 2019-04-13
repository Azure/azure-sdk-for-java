// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.common.test;

import com.azure.common.test.utils.SdkContext;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Base class for running live and playback tests using {@link InterceptorManager}.
 */
public abstract class TestBase {
    private final Logger logger = LoggerFactory.getLogger(TestBase.class);
    private final TestMode testMode;

    protected InterceptorManager interceptorManager;
    protected SdkContext sdkContext;

    /**
     * Creates a TestBase that runs in the specified {@code testMode}.
     *
     * @param testMode TestMode to run tests in.
     */
    protected TestBase(TestMode testMode) {
        this.testMode = testMode;
    }

    /**
     * Sets-up the {@link TestBase#sdkContext} and {@link TestBase#interceptorManager} before each test case is run.
     * Then calls its implementing class to perform any other set-up commands.
     */
    @Before
    public void setupTest() {
        final String testName = testName();
        logger.info("Test Mode: {}, Name: {}", testMode, testName);

        try {
            interceptorManager = new InterceptorManager(testName, testMode);
        } catch (IOException e) {
            logger.error("Could not create interceptor for {}", testName, e);
            Assert.fail();
        }

        sdkContext = new SdkContext(testMode, interceptorManager.getRecordedData());

        beforeTest();
    }

    /**
     * Disposes of {@link InterceptorManager} and its inheriting class' resources.
     */
    @After
    public void teardownTest() {
        interceptorManager.close();
        afterTest();
    }

    /**
     * Gets the name of the current test being run.
     *
     * NOTE: This could not be implemented in the base class using {@link TestName} because it always returns
     * {@code null}. See https://stackoverflow.com/a/16113631/4220757.
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
     * Dispose of any resources and clean-up after a test case runs.
     * Can be overridden in an inheriting class to add additional functionality during test teardown.
     */
    protected void afterTest() {
    }
}
