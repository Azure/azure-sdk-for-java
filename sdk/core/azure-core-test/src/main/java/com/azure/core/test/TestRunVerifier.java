// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test;

import com.azure.core.test.annotation.DoNotRecord;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * This class validates that a test is able to run.
 */
public class TestRunVerifier {
    private volatile boolean doNotRecord;
    private volatile boolean skipInPlayback;
    private volatile boolean testRan;

    TestRunVerifier(Method testMethod) {
        DoNotRecord doNotRecordAnnotation = testMethod.getAnnotation(DoNotRecord.class);
        if (doNotRecordAnnotation != null) {
            doNotRecord = true;
            skipInPlayback = doNotRecordAnnotation.skipInPlayback();
        }
    }

    /**
     * Verifies whether the current test is allowed to run.
     *
     * @param testMode The {@link TestMode} tests are being ran in.
     */
    void verifyTestCanRun(TestMode testMode) {
        testRan = !(skipInPlayback && testMode == TestMode.PLAYBACK);
        assumeTrue(testRan, "Test does not allow playback and was ran in 'TestMode.PLAYBACK'.");
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
