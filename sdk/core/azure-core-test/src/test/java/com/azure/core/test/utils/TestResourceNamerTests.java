// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.utils;

import com.azure.core.test.TestContextManager;
import com.azure.core.test.TestMode;
import com.azure.core.test.models.RecordedData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.NoSuchElementException;

import static com.azure.core.test.FakeTestClass.DONOTRECORD_FALSE_SKIPINPLAYBACK;
import static com.azure.core.test.FakeTestClass.METHOD_WITHOUT_DONOTRECORD;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link TestResourceNamer}.
 */
public class TestResourceNamerTests {
    private static final String A_VARIABLE = "aVariable";
    private static final String RANDOM_NAME_PREFIX = "prefix";
    private static final int RANDOM_NAME_LENGTH = 12;
    private static final String CONFIG_VALUE = "value";

    /**
     * Validates that a {@link NullPointerException} is thrown if {@code testMode} isn't {@link TestMode#LIVE} and
     * {@code doNotRecord} is {@code false}, otherwise no exception is thrown as having no {@link RecordedData} is
     * valid in those cases.
     */
    @Test
    public void nullRecordedData() {
        // Doesn't throw when TestMode.LIVE.
        assertDoesNotThrow(() ->
            new TestResourceNamer(new TestContextManager(METHOD_WITHOUT_DONOTRECORD, TestMode.LIVE), null));

        // Doesn't throw when 'doNotRecord' is true.
        assertDoesNotThrow(() ->
            new TestResourceNamer(new TestContextManager(DONOTRECORD_FALSE_SKIPINPLAYBACK, TestMode.RECORD), null));

        // Does throw when TestMode isn't LIVE and doNotRecord = false
        assertThrows(NullPointerException.class, () ->
            new TestResourceNamer(new TestContextManager(METHOD_WITHOUT_DONOTRECORD, TestMode.RECORD), null));
        assertThrows(NullPointerException.class, () ->
            new TestResourceNamer(new TestContextManager(METHOD_WITHOUT_DONOTRECORD, TestMode.PLAYBACK), null));
    }

    /**
     * Validates that when {@code doNotRecord} is {@code true} and {@code testMode} is {@link TestMode#PLAYBACK} then
     * the {@link RecordedData} within the {@link TestResourceNamer} cannot be read.
     */
    @Test
    public void recordedDataIsNotAllowedToReadRecordedValues() {
        TestResourceNamer resourceNamer = new TestResourceNamer(new TestContextManager(
            DONOTRECORD_FALSE_SKIPINPLAYBACK, TestMode.PLAYBACK), getRecordedDataWithValue());

        assertNotEquals(A_VARIABLE, resourceNamer.randomName("prefix", 12));
        assertNotEquals(A_VARIABLE, resourceNamer.randomUuid());
        assertNotEquals(A_VARIABLE, resourceNamer.now());
        assertEquals(A_VARIABLE, resourceNamer.recordValueFromConfig(A_VARIABLE));
    }

    /**
     * Validates that when {@code testMode} is {@link TestMode#LIVE} or {@code doNotRecord} is {@code true} then the
     * {@link RecordedData} within the {@link TestResourceNamer} cannot record values generated.
     */
    @Test
    public void recordedDataIsNotAllowedToRecordValues() {
        RecordedData recordedData = new RecordedData();

        callNamerMethds(new TestResourceNamer(
            new TestContextManager(METHOD_WITHOUT_DONOTRECORD, TestMode.LIVE), recordedData));
        validateNoRecordingsMade(new TestResourceNamer(
            new TestContextManager(METHOD_WITHOUT_DONOTRECORD, TestMode.PLAYBACK), recordedData));

        // Reset the recording data.
        recordedData = new RecordedData();

        callNamerMethds(new TestResourceNamer(
            new TestContextManager(DONOTRECORD_FALSE_SKIPINPLAYBACK, TestMode.RECORD), recordedData));
        validateNoRecordingsMade(new TestResourceNamer(
            new TestContextManager(METHOD_WITHOUT_DONOTRECORD, TestMode.PLAYBACK), recordedData));
    }

    private void callNamerMethds(TestResourceNamer resourceNamer) {
        resourceNamer.randomName(RANDOM_NAME_PREFIX, RANDOM_NAME_LENGTH);
        resourceNamer.randomUuid();
        resourceNamer.now();
        resourceNamer.recordValueFromConfig(CONFIG_VALUE);
    }

    private void validateNoRecordingsMade(TestResourceNamer resourceNamer) {
        assertNoSuchElementException(() -> resourceNamer.randomName(RANDOM_NAME_PREFIX, RANDOM_NAME_LENGTH));
        assertNoSuchElementException(resourceNamer::randomUuid);
        assertNoSuchElementException(resourceNamer::now);
        assertNoSuchElementException(() -> resourceNamer.recordValueFromConfig(CONFIG_VALUE));
    }

    private void assertNoSuchElementException(Executable executable) {
        assertThrows(NoSuchElementException.class, executable, "Expected 'NoSuchElementException' to be thrown.");
    }

    private RecordedData getRecordedDataWithValue() {
        RecordedData recordedData = new RecordedData();
        recordedData.addVariable(A_VARIABLE);

        return recordedData;
    }
}
