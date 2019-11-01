// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test;

import com.azure.core.test.annotation.DoNotRecord;
import org.junit.AssumptionViolatedException;
import org.junit.Test;
import org.junit.runner.Description;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestRunVerifierTests {
    @Test
    public void testWithoutDoNotRecord() {
        TestRunVerifier verifier = new TestRunVerifier();
        verifier.starting(Description.createTestDescription(TestHelper.class, "testWithoutDoNotRecord"));

        assertFalse(verifier.doNotRecordTest());

        verifier.verifyTestCanRun(TestMode.PLAYBACK);
        assertTrue(verifier.wasTestRan());

        verifier.verifyTestCanRun(TestMode.LIVE);
        assertTrue(verifier.wasTestRan());

        verifier.verifyTestCanRun(TestMode.RECORD);
        assertTrue(verifier.wasTestRan());
    }

    @Test
    public void testWithDoNotRecordRunInPlayback() {
        TestRunVerifier verifier = new TestRunVerifier();
        verifier.starting(Description.createTestDescription(TestHelper.class, "testWithDoNotRecordRunInPlayback"));

        assertTrue(verifier.doNotRecordTest());

        verifier.verifyTestCanRun(TestMode.PLAYBACK);
        assertTrue(verifier.wasTestRan());

        verifier.verifyTestCanRun(TestMode.LIVE);
        assertTrue(verifier.wasTestRan());

        verifier.verifyTestCanRun(TestMode.RECORD);
        assertTrue(verifier.wasTestRan());
    }

    @Test
    public void testWithDoNotRecordSkipInPlayback() {
        TestRunVerifier verifier = new TestRunVerifier();
        verifier.starting(Description.createTestDescription(TestHelper.class, "testWithDoNotRecordSkipInPlayback"));

        assertTrue(verifier.doNotRecordTest());

        try {
            verifier.verifyTestCanRun(TestMode.PLAYBACK);
        } catch (RuntimeException ex) {
            assertTrue(ex instanceof AssumptionViolatedException);
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
