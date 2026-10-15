// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.samples;

import com.azure.ai.contentunderstanding.models.DocumentContent;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Sample18AnalyzeChunkingControlFlowTest {
    @Test
    public void successfulResultIsReturned() {
        assertEquals("result", Sample18_AnalyzeChunking
            .requireSuccessfulResult(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, () -> "result", "Test"));
    }

    @Test
    public void failedStatusIsRejectedWithoutFetchingResult() {
        AtomicBoolean fetched = new AtomicBoolean();

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> Sample18_AnalyzeChunking.requireSuccessfulResult(LongRunningOperationStatus.FAILED, () -> {
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
            () -> Sample18_AnalyzeChunking.requireSuccessfulResult(LongRunningOperationStatus.USER_CANCELLED, () -> {
                fetched.set(true);
                return "result";
            }, "Test"));

        assertTrue(exception.getMessage().contains("USER_CANCELLED"));
        assertFalse(fetched.get());
    }

    @Test
    public void emptyFinalResultIsRejected() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> Sample18_AnalyzeChunking
            .requireSuccessfulResult(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, () -> null, "Test"));

        assertTrue(exception.getMessage().contains("without a final result"));
    }

    @Test
    public void finalResultFailurePropagates() {
        RuntimeException expected = new RuntimeException("expected failure");

        RuntimeException actual = assertThrows(RuntimeException.class, () -> Sample18_AnalyzeChunking
            .requireSuccessfulResult(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, () -> {
                throw expected;
            }, "Test"));

        assertSame(expected, actual);
    }

    @Test
    public void multipleSpansAreJoinedWithLineSeparator() {
        DocumentContent document = parseDocument("{\"mimeType\":\"application/pdf\",\"startPageNumber\":1,"
            + "\"endPageNumber\":1,\"markdown\":\"alpha beta gamma\",\"chunks\":[{\"spans\":["
            + "{\"offset\":0,\"length\":5},{\"offset\":11,\"length\":5}]}]}");

        List<String> chunks = Sample18_AnalyzeChunking.renderChunks(document);

        assertEquals(1, chunks.size());
        assertEquals("alpha" + System.lineSeparator() + "gamma", chunks.get(0));
    }

    @Test
    public void missingChunksAreRejected() {
        DocumentContent document = parseDocument("{\"mimeType\":\"application/pdf\",\"startPageNumber\":1,"
            + "\"endPageNumber\":1,\"markdown\":\"content\"}");

        assertThrows(IllegalStateException.class, () -> Sample18_AnalyzeChunking.renderChunks(document));
    }

    @Test
    public void invalidSpanBoundsAreRejected() {
        DocumentContent document = parseDocument("{\"mimeType\":\"application/pdf\",\"startPageNumber\":1,"
            + "\"endPageNumber\":1,\"markdown\":\"short\",\"chunks\":[{\"spans\":["
            + "{\"offset\":3,\"length\":5}]}]}");

        assertThrows(IllegalStateException.class, () -> Sample18_AnalyzeChunking.renderChunks(document));
    }

    private static DocumentContent parseDocument(String json) {
        try (JsonReader reader = JsonProviders.createReader(json)) {
            return DocumentContent.fromJson(reader);
        } catch (IOException exception) {
            throw new RuntimeException("Failed to parse document fixture.", exception);
        }
    }
}
