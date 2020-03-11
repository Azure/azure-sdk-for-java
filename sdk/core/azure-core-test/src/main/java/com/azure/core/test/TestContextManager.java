// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.test;

import com.azure.core.test.annotation.DoNotRecord;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * This class handles managing context about a test, such as custom testing annotations and verifying whether the test
 * is capable of running.
 */
public class TestContextManager {
    private final String testName;
    private final TestMode testMode;
    private final boolean doNotRecord;
    private final boolean skipInPlayback;
    private final boolean testRan;

    /**
     * Constructs a {@link TestContextManager} based on the test method.
     *
     * @param testMethod Test method being ran.
     * @param testMode The {@link TestMode} the test is running in.
     */
    public TestContextManager(Method testMethod, TestMode testMode) {
        this.testName = testMethod.getName();
        this.testMode = testMode;

        DoNotRecord doNotRecordAnnotation = testMethod.getAnnotation(DoNotRecord.class);
        if (doNotRecordAnnotation != null) {
            this.doNotRecord = true;
            this.skipInPlayback = doNotRecordAnnotation.skipInPlayback();
        } else {
            this.doNotRecord = false;
            this.skipInPlayback = false;
        }

        this.testRan = !(skipInPlayback && testMode == TestMode.PLAYBACK);
        assumeTrue(testRan, "Test does not allow playback and was ran in 'TestMode.PLAYBACK'");
    }

    /**
     * Returns the name of the test being ran.
     *
     * @return The test name.
     */
    public String getTestName() {
        return testName;
    }

    /**
     * Returns the mode being used to run tests.
     *
     * @return The {@link TestMode} being used to run tests.
     */
    public TestMode getTestMode() {
        return testMode;
    }

    /**
     * Returns whether the test should have its network calls recorded during a {@link TestMode#RECORD record} test
     * run.
     *
     * @return Flag indicating whether to record test network calls.
     */
    public boolean doNotRecordTest() {
        return doNotRecord;
    }

    /**
     * Returns whether the current test was ran.
     *
     * @return Flag indicating whether the current test was ran.
     */
    public boolean didTestRun() {
        return testRan;
    }
}
