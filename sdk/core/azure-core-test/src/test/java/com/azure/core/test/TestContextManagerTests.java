// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test;

import com.azure.core.test.annotation.DoNotRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.opentest4j.TestAbortedException;

import java.lang.reflect.Method;

import static com.azure.core.test.FakeTestClass.DONOTRECORD_FALSE_SKIPINPLAYBACK;
import static com.azure.core.test.FakeTestClass.DONOTRECORD_SKIPINPLAYBACK;
import static com.azure.core.test.FakeTestClass.METHOD_WITHOUT_DONOTRECORD;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link TestContextManager}.
 */
public class TestContextManagerTests {

    /**
     * Validates that a test method without the {@link DoNotRecord} annotation is allowed to run in all test modes and
     * will record network calls and test values.
     */
    @ParameterizedTest(name = "[{index}] {displayName}")
    @EnumSource(TestMode.class)
    public void testWithoutDoNotRecord(TestMode testMode) {
        TestContextManager testContextManager = new TestContextManager(METHOD_WITHOUT_DONOTRECORD, testMode);

        assertFalse(testContextManager.doNotRecordTest());
        assertTrue(testContextManager.didTestRun());
    }

    /**
     * Validates that a test method with the default {@link DoNotRecord} annotation is allowed to run in all test modes
     * but doesn't have its network calls or test values recorded.
     */
    @ParameterizedTest(name = "[{index}] {displayName}")
    @EnumSource(TestMode.class)
    public void testWithDoNotRecordRunInPlayback(TestMode testMode) {
        TestContextManager testContextManager = new TestContextManager(DONOTRECORD_FALSE_SKIPINPLAYBACK, testMode);

        assertTrue(testContextManager.doNotRecordTest());
        assertTrue(testContextManager.didTestRun());
    }

    /**
     * Validates that a test method with the {@link DoNotRecord} annotation having {@code skipInPlayback} set to true is
     * only allowed to run in {@link TestMode#RECORD} and {@link TestMode#LIVE} and won't have its network calls or test
     * values recorded.
     */
    @Test
    public void testWithDoNotRecordSkipInPlayback() {
        Method testMethod = DONOTRECORD_SKIPINPLAYBACK;

        assertThrows(TestAbortedException.class, () -> new TestContextManager(testMethod, TestMode.PLAYBACK));

        TestContextManager testContextManager = new TestContextManager(testMethod, TestMode.LIVE);
        assertTrue(testContextManager.doNotRecordTest());
        assertTrue(testContextManager.didTestRun());

        testContextManager = new TestContextManager(testMethod, TestMode.RECORD);
        assertTrue(testContextManager.doNotRecordTest());
        assertTrue(testContextManager.didTestRun());
    }
}
