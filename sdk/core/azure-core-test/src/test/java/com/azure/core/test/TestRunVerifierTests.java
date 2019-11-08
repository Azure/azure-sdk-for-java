// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test;

import com.azure.core.test.annotation.DoNotRecord;
import org.junit.jupiter.api.Test;
import org.opentest4j.TestAbortedException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link TestRunVerifier}.
 */
public class TestRunVerifierTests {

    /**
     * Validates that a test method without the {@link DoNotRecord} annotation is allowed to run in all test modes and
     * will record network calls and test values.
     */
    @Test
    public void testWithoutDoNotRecord() throws NoSuchMethodException {
        TestRunVerifier verifier = new TestRunVerifier(TestHelper.class.getMethod("testWithoutDoNotRecord"));

        assertFalse(verifier.doNotRecordTest());

        verifier.verifyTestCanRun(TestMode.PLAYBACK);
        assertTrue(verifier.wasTestRan());

        verifier.verifyTestCanRun(TestMode.LIVE);
        assertTrue(verifier.wasTestRan());

        verifier.verifyTestCanRun(TestMode.RECORD);
        assertTrue(verifier.wasTestRan());
    }

    /**
     * Validates that a test method with the default {@link DoNotRecord} annotation is allowed to run in all test modes
     * but doesn't have its network calls or test values recorded.
     */
    @Test
    public void testWithDoNotRecordRunInPlayback() throws NoSuchMethodException {
        TestRunVerifier verifier = new TestRunVerifier(TestHelper.class.getMethod("testWithDoNotRecordRunInPlayback"));

        assertTrue(verifier.doNotRecordTest());

        verifier.verifyTestCanRun(TestMode.PLAYBACK);
        assertTrue(verifier.wasTestRan());

        verifier.verifyTestCanRun(TestMode.LIVE);
        assertTrue(verifier.wasTestRan());

        verifier.verifyTestCanRun(TestMode.RECORD);
        assertTrue(verifier.wasTestRan());
    }

    /**
     * Validates that a test method with the {@link DoNotRecord} annotation having {@code skipInPlayback} set to true
     * is only allowed to run in {@link TestMode#RECORD} and {@link TestMode#LIVE} and won't have its network calls or
     * test values recorded.
     */
    @Test
    public void testWithDoNotRecordSkipInPlayback() throws NoSuchMethodException {
        TestRunVerifier verifier = new TestRunVerifier(TestHelper.class.getMethod("testWithDoNotRecordSkipInPlayback"));

        assertTrue(verifier.doNotRecordTest());

        try {
            verifier.verifyTestCanRun(TestMode.PLAYBACK);
        } catch (RuntimeException ex) {
            assertTrue(ex instanceof TestAbortedException);
        }

        verifier.verifyTestCanRun(TestMode.LIVE);
        assertTrue(verifier.wasTestRan());

        verifier.verifyTestCanRun(TestMode.RECORD);
        assertTrue(verifier.wasTestRan());
    }

    static class TestHelper {
        public void testWithoutDoNotRecord() {
        }

        @DoNotRecord
        public void testWithDoNotRecordRunInPlayback() {

        }

        @DoNotRecord(skipInPlayback = true)
        public void testWithDoNotRecordSkipInPlayback() {
        }
    }
}
