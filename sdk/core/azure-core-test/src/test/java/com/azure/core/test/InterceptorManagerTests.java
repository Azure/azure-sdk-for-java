// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test;

import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link InterceptorManager}.
 */
public class InterceptorManagerTests {

    /**
     * Validates that a {@link NullPointerException} is thrown when the test name is null.
     */
    @Test
    public void nullTestName() {
        try {
            new InterceptorManager(null, TestMode.RECORD, false);
        } catch (Exception ex) {
            assertTrue(ex instanceof NullPointerException);
        }

        try {
            new InterceptorManager(null, new HashMap<>(), false);
        } catch (Exception ex) {
            assertTrue(ex instanceof NullPointerException);
        }
    }

    /**
     * Validates that {@link InterceptorManager#getRecordedData()} is {@code null} when testing in {@link
     * TestMode#LIVE}.
     */
    @Test
    public void recordedDataIsNullInLiveMode() {
        assertNull(new InterceptorManager("testName", TestMode.LIVE, false).getRecordedData());
        assertNull(new InterceptorManager("testName", TestMode.LIVE, true).getRecordedData());
    }

    /**
     * Validates that {@link InterceptorManager#getRecordedData()} is {@code null} when {@code doNotRecord} is passed as
     * {@code true}.
     */
    @Test
    public void recordedDataIsNullWhenDoNotRecord() {
        assertNull(new InterceptorManager("testName", TestMode.RECORD, true).getRecordedData());
        assertNull(new InterceptorManager("testName", TestMode.LIVE, true).getRecordedData());
        assertNull(new InterceptorManager("testName", TestMode.PLAYBACK, true).getRecordedData());
        assertNull(new InterceptorManager("testName", new HashMap<>(), true).getRecordedData());
    }


}
