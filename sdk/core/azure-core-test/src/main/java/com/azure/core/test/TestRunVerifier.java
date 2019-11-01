// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test;

import com.azure.core.test.annotation.DoNotRecord;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import static org.junit.Assume.assumeTrue;

/**
 * This class validates that a test is able to run.
 */
class TestRunVerifier extends TestWatcher {
    private volatile boolean doNotRecord;
    private volatile boolean skipInPlayback;
    private volatile boolean testRan;

    @Override
    protected void starting(Description description) {
        try {
            DoNotRecord doNotRecordAnnotation = description.getTestClass()
                .getMethod(description.getMethodName())
                .getAnnotation(DoNotRecord.class);

            if (doNotRecordAnnotation != null) {
                doNotRecord = true;
                skipInPlayback = doNotRecordAnnotation.skipInPlayback();
            }
        } catch (NoSuchMethodException ex) {
            doNotRecord = false;
            skipInPlayback = false;
        }
    }

    /**
     * Verifies whether the current test is allowed to run.
     *
     * @param testMode The {@link TestMode} tests are being ran in.
     */
    void verifyTestCanRun(TestMode testMode) {
        testRan = !(skipInPlayback && testMode == TestMode.PLAYBACK);
        assumeTrue("Test does not allow playback and was ran in 'TestMode.PLAYBACK'.", testRan);
    }

    /**
     * Returns whether the test should have its network calls recorded during a {@link TestMode#RECORD record} test run.
     *
     * @return Flag indicating whether to record test network calls.
     */
    boolean doNotRecordTest() {
        return doNotRecord;
    }

    /**
     * Returns whether the current test was ran.
     *
     * @return Flag indicating whether the current test was ran.
     */
    boolean wasTestRan() {
        return testRan;
    }
}
