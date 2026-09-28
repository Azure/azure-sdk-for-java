// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.samples;

import com.azure.core.util.polling.LongRunningOperationStatus;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Sample15GrantCopyAuthControlFlowTest {
    @Test
    public void environmentValueIsTrimmed() {
        assertEquals("value", Sample15_GrantCopyAuth.requireEnvironmentValue("SETTING", " value "));
    }

    @Test
    public void missingEnvironmentValueIsRejected() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> Sample15_GrantCopyAuth.requireEnvironmentValue("SETTING", null));

        assertTrue(exception.getMessage().contains("SETTING"));
    }

    @Test
    public void blankEnvironmentValueIsRejected() {
        assertThrows(IllegalStateException.class,
            () -> Sample15_GrantCopyAuth.requireEnvironmentValue("SETTING", "  "));
    }

    @Test
    public void successfulResultIsReturned() {
        String result = Sample15_GrantCopyAuth
            .requireSuccessfulResult(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, () -> "result", "Test");

        assertEquals("result", result);
    }

    @Test
    public void failedStatusIsRejectedWithoutFetchingResult() {
        AtomicBoolean fetched = new AtomicBoolean();

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> Sample15_GrantCopyAuth.requireSuccessfulResult(LongRunningOperationStatus.FAILED, () -> {
                fetched.set(true);
                return "result";
            }, "Test"));

        assertTrue(exception.getMessage().contains("FAILED"));
        assertFalse(fetched.get());
    }

    @Test
    public void cancelledStatusIsRejectedWithoutFetchingResult() {
        AtomicBoolean fetched = new AtomicBoolean();

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> Sample15_GrantCopyAuth.requireSuccessfulResult(LongRunningOperationStatus.USER_CANCELLED, () -> {
                fetched.set(true);
                return "result";
            }, "Test"));

        assertTrue(exception.getMessage().contains("USER_CANCELLED"));
        assertFalse(fetched.get());
    }

    @Test
    public void emptyFinalResultIsRejected() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> Sample15_GrantCopyAuth
            .requireSuccessfulResult(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, () -> null, "Test"));

        assertTrue(exception.getMessage().contains("without a final result"));
    }

    @Test
    public void finalResultFailurePropagates() {
        RuntimeException expected = new RuntimeException("expected failure");

        RuntimeException actual = assertThrows(RuntimeException.class, () -> Sample15_GrantCopyAuth
            .requireSuccessfulResult(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, () -> {
                throw expected;
            }, "Test"));

        assertSame(expected, actual);
    }
}
