// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.samples;

import com.azure.core.util.polling.LongRunningOperationStatus;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Sample16CreateAnalyzerWithLabelsControlFlowTest {
    @Test
    public void environmentValueIsTrimmed() {
        assertEquals("endpoint",
            Sample16_CreateAnalyzerWithLabels.requireEnvironmentValue("CONTENTUNDERSTANDING_ENDPOINT", " endpoint "));
    }

    @Test
    public void missingEnvironmentValueIsRejected() {
        assertThrows(IllegalStateException.class,
            () -> Sample16_CreateAnalyzerWithLabels.requireEnvironmentValue("SETTING", null));
    }

    @Test
    public void blankEnvironmentValueIsRejected() {
        assertThrows(IllegalStateException.class,
            () -> Sample16_CreateAnalyzerWithLabels.requireEnvironmentValue("SETTING", " "));
    }

    @Test
    public void demoModeAcceptsNoStorageConfiguration() {
        Sample16_CreateAnalyzerWithLabels.validateTrainingDataConfiguration(null, null, null);
    }

    @Test
    public void sasUrlTakesPrecedenceOverPartialStorageConfiguration() {
        Sample16_CreateAnalyzerWithLabels.validateTrainingDataConfiguration(
            "https://account.blob.core.windows.net/container?sig=secret", "account", null);
    }

    @Test
    public void partialStorageConfigurationIsRejected() {
        assertThrows(IllegalStateException.class,
            () -> Sample16_CreateAnalyzerWithLabels.validateTrainingDataConfiguration(null, "account", null));
        assertThrows(IllegalStateException.class,
            () -> Sample16_CreateAnalyzerWithLabels.validateTrainingDataConfiguration(null, null, "container"));
    }

    @Test
    public void sasUrlIsSanitizedBeforeLogging() {
        String sanitized = Sample16_CreateAnalyzerWithLabels
            .sanitizeSasUrl("https://account.blob.core.windows.net/container?sv=1&sig=secret");

        assertEquals("https://account.blob.core.windows.net/container", sanitized);
        assertFalse(sanitized.contains("secret"));
    }

    @Test
    public void sasUrlWithoutCredentialsIsUnchanged() {
        assertEquals("https://account.blob.core.windows.net/container",
            Sample16_CreateAnalyzerWithLabels.sanitizeSasUrl("https://account.blob.core.windows.net/container"));
        assertNull(Sample16_CreateAnalyzerWithLabels.sanitizeSasUrl(null));
    }

    @Test
    public void successfulResultIsReturned() {
        assertEquals("result", Sample16_CreateAnalyzerWithLabels
            .requireSuccessfulResult(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, () -> "result", "Test"));
    }

    @Test
    public void failedStatusIsRejectedWithoutFetchingResult() {
        AtomicBoolean fetched = new AtomicBoolean();

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> Sample16_CreateAnalyzerWithLabels.requireSuccessfulResult(LongRunningOperationStatus.FAILED, () -> {
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
            = assertThrows(IllegalStateException.class, () -> Sample16_CreateAnalyzerWithLabels
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
            = assertThrows(IllegalStateException.class, () -> Sample16_CreateAnalyzerWithLabels
                .requireSuccessfulResult(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, () -> null, "Test"));

        assertTrue(exception.getMessage().contains("without a final result"));
    }

    @Test
    public void finalResultFailurePropagates() {
        RuntimeException expected = new RuntimeException("expected failure");

        RuntimeException actual = assertThrows(RuntimeException.class, () -> Sample16_CreateAnalyzerWithLabels
            .requireSuccessfulResult(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, () -> {
                throw expected;
            }, "Test"));

        assertSame(expected, actual);
    }
}
