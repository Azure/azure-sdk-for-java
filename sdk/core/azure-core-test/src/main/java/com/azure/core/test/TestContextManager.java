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
    private final boolean doNotRecord;
    private final boolean skipInPlayback;
    private volatile boolean testRan;

    /**
     * Constructs a {@link TestContextManager} based on the test method.
     *
     * @param testMethod Test method being ran.
     */
    public TestContextManager(Method testMethod) {
        DoNotRecord doNotRecordAnnotation = testMethod.getAnnotation(DoNotRecord.class);
        if (doNotRecordAnnotation != null) {
            this.doNotRecord = true;
            this.skipInPlayback = doNotRecordAnnotation.skipInPlayback();
        } else {
            this.doNotRecord = false;
            this.skipInPlayback = false;
        }
    }

    /**
     * Verifies whether the current test is allowed to run.
     *
     * @param testMode The {@link TestMode} tests are being ran in.
     */
    public void verifyTestCanRunInTestMode(TestMode testMode) {
        this.testRan = !(skipInPlayback && testMode == TestMode.PLAYBACK);
        assumeTrue(testRan, "Test ddes not allow playback and was ran in 'TestMode.PLAYBACK'");
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
    public boolean wasTestRan() {
        return testRan;
    }
}
