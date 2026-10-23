// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.samples;

import com.azure.ai.contentunderstanding.models.ContentAnalyzer;
import com.azure.ai.contentunderstanding.models.ContentAnalyzerConfig;
import com.azure.ai.contentunderstanding.models.ContentAnalyzerWorkflow;
import com.azure.core.util.polling.LongRunningOperationStatus;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Sample17CreateAnalyzerWorkflowControlFlowTest {
    @Test
    public void successfulResultIsReturned() {
        assertEquals("result", Sample17_CreateAnalyzerWorkflow
            .requireSuccessfulResult(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, () -> "result", "Test"));
    }

    @Test
    public void failedStatusIsRejectedWithoutFetchingResult() {
        AtomicBoolean fetched = new AtomicBoolean();

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> Sample17_CreateAnalyzerWorkflow.requireSuccessfulResult(LongRunningOperationStatus.FAILED, () -> {
                fetched.set(true);
                return "result";
            }, "Test"));

        assertTrue(exception.getMessage().contains("FAILED"));
        assertFalse(fetched.get());
    }

    @Test
    public void cancelledStatusIsRejectedWithoutFetchingResult() {
        AtomicBoolean fetched = new AtomicBoolean();

        IllegalStateException exception
            = assertThrows(IllegalStateException.class, () -> Sample17_CreateAnalyzerWorkflow
                .requireSuccessfulResult(LongRunningOperationStatus.USER_CANCELLED, () -> {
                    fetched.set(true);
                    return "result";
                }, "Test"));

        assertTrue(exception.getMessage().contains("USER_CANCELLED"));
        assertFalse(fetched.get());
    }

    @Test
    public void emptyFinalResultIsRejected() {
        IllegalStateException exception
            = assertThrows(IllegalStateException.class, () -> Sample17_CreateAnalyzerWorkflow
                .requireSuccessfulResult(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, () -> null, "Test"));

        assertTrue(exception.getMessage().contains("without a final result"));
    }

    @Test
    public void finalResultFailurePropagates() {
        RuntimeException expected = new RuntimeException("expected failure");

        RuntimeException actual = assertThrows(RuntimeException.class, () -> Sample17_CreateAnalyzerWorkflow
            .requireSuccessfulResult(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, () -> {
                throw expected;
            }, "Test"));

        assertSame(expected, actual);
    }

    @Test
    public void resolvedWorkflowsAreAccepted() {
        ContentAnalyzer defaultAnalyzer
            = new ContentAnalyzer().setConfig(new ContentAnalyzerConfig().setWorkflow(ContentAnalyzerWorkflow.DEFAULT));
        ContentAnalyzer agenticAnalyzer
            = new ContentAnalyzer().setConfig(new ContentAnalyzerConfig().setWorkflow(ContentAnalyzerWorkflow.AGENTIC));

        Sample17_CreateAnalyzerWorkflow.verifyResolvedWorkflow(defaultAnalyzer, false, "Default");
        Sample17_CreateAnalyzerWorkflow.verifyResolvedWorkflow(agenticAnalyzer, true, "Agentic");
    }

    @Test
    public void unexpectedWorkflowIsRejected() {
        ContentAnalyzer analyzer
            = new ContentAnalyzer().setConfig(new ContentAnalyzerConfig().setWorkflow(ContentAnalyzerWorkflow.DEFAULT));

        assertThrows(IllegalStateException.class,
            () -> Sample17_CreateAnalyzerWorkflow.verifyResolvedWorkflow(analyzer, true, "Agentic"));
    }
}
