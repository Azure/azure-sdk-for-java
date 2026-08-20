// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.samples;

import com.azure.ai.contentunderstanding.models.ContentAnalyzer;
import com.azure.ai.contentunderstanding.models.ContentAnalyzerConfig;
import com.azure.core.util.polling.LongRunningOperationStatus;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SampleAdvancedClassifyInPageSegmentsControlFlowTest {
    @Test
    public void classifierUsesReleasedCategoryDescriptions() {
        ContentAnalyzer classifier = Sample_Advanced_ClassifyInPageSegments.createClassifier();
        ContentAnalyzerConfig config = classifier.getConfig();

        assertEquals(Boolean.TRUE, config.isReturnDetails());
        assertEquals(Boolean.TRUE, config.isSegmentEnabled());
        assertEquals(Boolean.TRUE, config.isAllowInPageSegments());
        assertEquals("An invoice requesting payment for goods or services, with line items, totals, and payment terms.",
            config.getContentCategories().get("Invoice").getDescription());
        assertEquals("A bank account statement listing balances, deposits, withdrawals, fees, and transactions.",
            config.getContentCategories().get("BankStatement").getDescription());
    }

    @Test
    public void successfulResultIsReturned() {
        assertEquals("result", Sample_Advanced_ClassifyInPageSegments
            .requireSuccessfulResult(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, () -> "result", "Test"));
    }

    @Test
    public void failedStatusIsRejectedWithoutFetchingResult() {
        AtomicBoolean fetched = new AtomicBoolean();

        IllegalStateException exception
            = assertThrows(IllegalStateException.class, () -> Sample_Advanced_ClassifyInPageSegments
                .requireSuccessfulResult(LongRunningOperationStatus.FAILED, () -> {
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
            = assertThrows(IllegalStateException.class, () -> Sample_Advanced_ClassifyInPageSegments
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
            = assertThrows(IllegalStateException.class, () -> Sample_Advanced_ClassifyInPageSegments
                .requireSuccessfulResult(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, () -> null, "Test"));

        assertTrue(exception.getMessage().contains("without a final result"));
    }

    @Test
    public void finalResultFailurePropagates() {
        RuntimeException expected = new RuntimeException("expected failure");

        RuntimeException actual = assertThrows(RuntimeException.class, () -> Sample_Advanced_ClassifyInPageSegments
            .requireSuccessfulResult(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, () -> {
                throw expected;
            }, "Test"));

        assertSame(expected, actual);
    }
}
