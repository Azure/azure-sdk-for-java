// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test;

import com.azure.core.test.annotation.IgnoreRecording;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import static org.junit.Assume.assumeTrue;

/**
 * This class validates that a test is able to run.
 */
class TestRunVerifier extends TestWatcher {
    private volatile boolean isPlaybackAllowed;
    private volatile boolean testRan;

    @Override
    protected void starting(Description description) {
        try {
            isPlaybackAllowed = description.getTestClass()
                .getMethod(description.getMethodName())
                .getAnnotation(IgnoreRecording.class) == null;
        } catch (NoSuchMethodException ex) {
            isPlaybackAllowed = true;
        }
    }

    /**
     * Verifies whether the current test is allowed to run.
     *
     * @param testMode The {@link TestMode} tests are being ran in.
     */
    void verifyTestCanRun(TestMode testMode) {
        testRan = isPlaybackAllowed || testMode != TestMode.PLAYBACK;
        assumeTrue("Test does not allow playback and was ran in 'TestMode.PLAYBACK'.", testRan);
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
