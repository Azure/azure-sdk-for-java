// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.samples;

import com.azure.core.util.polling.LongRunningOperationStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Sample13DeleteResultControlFlowTest {
    @Test
    public void successfulCompletionIsAccepted() {
        Sample13_DeleteResult.requireSuccessfulCompletion(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, "Test");
    }

    @Test
    public void failedCompletionIsRejected() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> Sample13_DeleteResult.requireSuccessfulCompletion(LongRunningOperationStatus.FAILED, "Test"));

        assertTrue(exception.getMessage().contains("FAILED"));
    }

    @Test
    public void cancelledCompletionIsRejected() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> Sample13_DeleteResult.requireSuccessfulCompletion(LongRunningOperationStatus.USER_CANCELLED, "Test"));

        assertTrue(exception.getMessage().contains("USER_CANCELLED"));
    }
}
